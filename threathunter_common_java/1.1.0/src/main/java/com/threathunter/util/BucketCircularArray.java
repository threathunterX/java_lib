package com.threathunter.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * This is a circular array acting as a FIFO queue.
 * <p>
 * It purposefully does NOT implement Deque or some other Collection interface as it only implements functionality necessary for this RollingNumber use case.
 * <p>
 * Important Thread-Safety Note: This is ONLY thread-safe within the context of RollingNumber and the protection it gives in the <code>getCurrentBucket</code> method. It uses AtomicReference
 * objects to ensure anything done outside of <code>getCurrentBucket</code> is thread-safe, and to ensure visibility of changes across threads (ie. volatility) but the addLast and removeFirst
 * methods are NOT thread-safe for external access they depend upon the lock.tryLock() protection in <code>getCurrentBucket</code> which ensures only a single thread will access them at at time.
 * <p>
 * benjchristensen => This implementation was chosen based on performance testing I did and documented at: http://benjchristensen.com/2011/10/08/atomiccirculararray/
 *
 * From the Hystrix project in Netflix, pls see https://github.com/Netflix/Hystrix
 */
public class BucketCircularArray<B extends BucketCircularArray.Bucket> implements Iterable<B> {
    private final AtomicReference<ListState> state;
    private final int dataLength; // we don't resize, we always stay the same, so remember this
    private final int numBuckets;

    /**
     * Immutable object that is atomically set every time the state of the BucketCircularArray changes
     * <p>
     * This handles the compound operations
     */
    private class ListState {
        /*
         * this is an AtomicReferenceArray and not a normal Array because we're copying the reference
         * between ListState objects and multiple threads could maintain references across these
         * compound operations so I want the visibility/concurrency guarantees
         */
        private final AtomicReferenceArray<B> data;
        private final int size;
        private final int tail;
        private final int head;

        private ListState(AtomicReferenceArray<B> data, int head, int tail) {
            this.head = head;
            this.tail = tail;
            if (head == 0 && tail == 0) {
                size = 0;
            } else {
                this.size = (tail + dataLength - head) % dataLength;
            }
            this.data = data;
        }

        public B tail() {
            if (size == 0) {
                return null;
            } else {
                // we want to get the last item, so size()-1
                return data.get(convert(size - 1));
            }
        }

        private List<B> getList() {
            /*
             * this isn't technically thread-safe since it requires multiple reads on something that can change
             * but since we never clear the data directly, only increment/decrement head/tail we would never get a NULL
             * just potentially return stale data which we are okay with doing
             */
            ArrayList<B> result = new ArrayList<>();
            for (int i = 0; i < size; i++) {
               result.add(data.get(convert(i)));
            }
            return result;
        }

        private ListState incrementTail() {
                /* if incrementing results in growing larger than 'length' which is the max we should be at, then also increment head (equivalent of removeFirst but done atomically) */
            if (size == numBuckets) {
                // increment tail and head
                return new ListState(data, (head + 1) % dataLength, (tail + 1) % dataLength);
            } else {
                // increment only tail
                return new ListState(data, head, (tail + 1) % dataLength);
            }
        }

        public ListState clear() {
            return new ListState(new AtomicReferenceArray<B>(dataLength), 0, 0);
        }

        public ListState addBucket(B b) {
                /*
                 * We could in theory have 2 threads addBucket concurrently and this compound operation would interleave.
                 * <p>
                 * This should NOT happen since getCurrentBucket is supposed to be executed by a single thread.
                 * <p>
                 * If it does happen, it's not a huge deal as incrementTail() will be protected by compareAndSet and one of the two addBucket calls will succeed with one of the Buckets.
                 * <p>
                 * In either case, a single Bucket will be returned as "last" and data loss should not occur and everything keeps in sync for head/tail.
                 * <p>
                 * Also, it's fine to set it before incrementTail because nothing else should be referencing that index position until incrementTail occurs.
                 */
            data.set(tail, b);
            return incrementTail();
        }

        // The convert() method takes a logical index (as if head was
        // always 0) and calculates the index within elementData
        private int convert(int index) {
            return (index + head) % dataLength;
        }
    }

    BucketCircularArray(int size) {
        AtomicReferenceArray<B> _buckets = new AtomicReferenceArray<B>(size + 1); // + 1 as extra room for the add/remove;
        state = new AtomicReference<ListState>(new ListState(_buckets, 0, 0));
        dataLength = _buckets.length();
        numBuckets = size;
    }

    public void clear() {
        while (true) {
                /*
                 * it should be very hard to not succeed the first pass thru since this is typically is only called from
                 * a single thread protected by a tryLock, but there is at least 1 other place (at time of writing this comment)
                 * where reset can be called from (CircuitBreaker.markSuccess after circuit was tripped) so it can
                 * in an edge-case conflict.
                 *
                 * Instead of trying to determine if someone already successfully called clear() and we should skip
                 * we will have both calls reset the circuit, even if that means losing data added in between the two
                 * depending on thread scheduling.
                 *
                 * The rare scenario in which that would occur, we'll accept the possible data loss while clearing it
                 * since the code has stated its desire to clear() anyways.
                 */
            ListState current = state.get();
            ListState newState = current.clear();
            if (state.compareAndSet(current, newState)) {
                return;
            }
        }
    }

    /**
     * Returns an iterator on a copy of the internal array so that the iterator won't fail by buckets being added/removed concurrently.
     */
    public Iterator<B> iterator() {
        return Collections.unmodifiableList(getList()).iterator();
    }

    public void addLast(B o) {
        ListState currentState = state.get();
        // create new version of state (what we want it to become)
        ListState newState = currentState.addBucket(o);

            /*
             * use compareAndSet to set in case multiple threads are attempting (which shouldn't be the case because since addLast will ONLY be called by a single thread at a time due to protection
             * provided in <code>getCurrentBucket</code>)
             */
        if (state.compareAndSet(currentState, newState)) {
            // we succeeded
            return;
        } else {
            // we failed, someone else was adding or removing
            // instead of trying again and risking multiple addLast concurrently (which shouldn't be the case)
            // we'll just return and let the other thread 'win' and if the timing is off the next call to getCurrentBucket will fix things
            return;
        }
    }

    public B getLast() {
        return peekLast();
    }

    public int size() {
        // the size can also be worked out each time as:
        // return (tail + data.length() - head) % data.length();
        return state.get().size;
    }

    public B peekLast() {
        return state.get().tail();
    }

    private List<B> getList() {
        return state.get().getList();
    }

    /**
     * Counters for a given 'bucket' of time.
     *
     * @Param T the data payload stored in the bucket.
     */
    public abstract static class Bucket<T> {
        private final long windowStart;
        private final T payload;

        Bucket(long startTime, T payload) {
            this.windowStart = startTime;
            this.payload = payload;
        }

        public long getWindowStart() {
            return windowStart;
        }

        public T getPayload() {
            return payload;
        }

    }
}
