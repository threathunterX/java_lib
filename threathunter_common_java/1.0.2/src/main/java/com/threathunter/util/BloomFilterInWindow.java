package com.threathunter.util;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author Wen Lu
 */
public class BloomFilterInWindow {
    // the expected number should be no less than the following value
    private static final int MIN_EXPECTEDNUMBERS = 100;

    // the interval that one bloom filter is for, in milliseconds.
    private final int interval;

    // how many bloom filters should be maintained
    private final int slots;

    // the required false positive rate
    private final double falsePositiveProbability;

    // the start expected number. Usually we will use the estimate value from
    // the last bloom filter's size, but we need an default value if there
    // is none bloom filter in the last interval.
    private final int initExpectedNumbers;

    // (slots) bloom filters, each is for the statistics in (interval) milliseconds
    private final BucketCircularArray<BloomFilterBucket> bloomFilters;

    public BloomFilterInWindow(int interval, int slots, double falsePositiveProbability,
                               int initExpectedNumbers) {
        this.interval = interval;
        this.slots = slots;
        this.falsePositiveProbability = falsePositiveProbability;
        this.initExpectedNumbers = Math.max(MIN_EXPECTEDNUMBERS, initExpectedNumbers);
        this.bloomFilters = new BucketCircularArray(slots);
    }

    public void add(byte[] item, long currentTime) {
        BloomFilterBucket currentBucket = getCurrentBucket(currentTime);
        currentBucket.add(item);
    }

    public void add(byte[] item) {
        add(item, SystemClock.getCurrentTimestamp());
    }

    public boolean contains(byte[] item) {
        // update time
        getCurrentBucket(SystemClock.getCurrentTimestamp());

        Iterator<BloomFilterBucket> it = bloomFilters.iterator();
        while(it != null && it.hasNext()) {
            BloomFilterBucket b = it.next();
            if (b != null && b.contains(item)) {
                return true;
            }
        }

        return false;
    }

    public BloomFilterBucket getCurrentBucket(long currentTime) {
        BloomFilterBucket currentBucket = bloomFilters.peekLast();
        if (currentBucket != null && currentTime < currentBucket.getWindowStart() + interval) {
            // we are in the window
            return currentBucket;
        }

        // the current bucket is not right, we need to adjust.
        synchronized (this) {
            BloomFilterBucket lastBucket = bloomFilters.peekLast();
            if (lastBucket == null) {
                // empty list
                BloomFilterBucket newBucket = new BloomFilterBucket(currentTime, falsePositiveProbability, initExpectedNumbers);
                bloomFilters.addLast(newBucket);
                return newBucket;
            }

            // We go into a loop so that it will create as many buckets as needed to catch up to the current time
            // as we want the buckets complete even if we don't have transactions during a period of time.
            for (int i = 0; i < slots; i++) {
                if (currentTime < lastBucket.getWindowStart() + interval) {
                    // added by other threads
                    return lastBucket;
                } else if (currentTime - (lastBucket.getWindowStart() + interval) > interval * slots) {
                    // the time passed is greater than the entire window so we want to clear it all and start from scratch
                    bloomFilters.clear();
                    // recursively call adjustTime which will create a new bucket and return it
                    return getCurrentBucket(currentTime);
                } else { // we're past the window so we need to create a new bucket
                    // create a new bucket and add it as the new 'last'
                    BloomFilter lastFilter = lastBucket.getPayload();
                    BloomFilterBucket newBucket = new BloomFilterBucket(lastBucket.getWindowStart() + interval, falsePositiveProbability,
                            Math.max(lastFilter.count(), MIN_EXPECTEDNUMBERS));
                    bloomFilters.addLast(newBucket);
                    lastBucket = newBucket;
                }
            }

            // shouldn't be here actually
            return lastBucket;
        }
    }

    /*package*/ static class BloomFilterBucket extends BucketCircularArray.Bucket<BloomFilter> {
        BloomFilterBucket(long startTime, BloomFilter payload) {
            super(startTime, payload);
        }

        BloomFilterBucket(long startTime, double falsePositiveProbability, int expectedNumbers) {
            super(startTime, new BloomFilter(falsePositiveProbability, expectedNumbers));
        }

        public void add(byte[] bytes) {
            getPayload().add(bytes);
        }

        public void add(Object element) {
            getPayload().add(element);
        }

        public boolean contains(byte[] bytes) {
            return getPayload().contains(bytes);
        }

        public boolean contains(Object element) {
            return getPayload().contains(element);
        }

        public void clear() {
            getPayload().clear();
        }

        public void addAll(Collection c) {
            getPayload().addAll(c);
        }

        public void containsAll(Collection c) {
            getPayload().containsAll(c);
        }
    }
}
