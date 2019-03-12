package com.threathunter.common.array;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import java.nio.charset.Charset;

/**
 * created by www.threathunter.cn
 */
public class LimitMaxHashArraySet {
    private final HashFunction function;
    private final int capacity;
    private final int threshold;
    private final int maxProbeLength;
    private final byte[] target;
    private final byte[] states;

    private int size;

    static final int MAXIMUM_CAPACITY = 1 << 30;
    static final int DEFAULT_CAPACITY = 1 << 7; // 128
    static final float DEFAULT_LOAD_FACTOR = 0.75F;
    static final int SHIFT_UNIT = 2;
    static final int DEFAULT_MAX_PROBE_LENGTH = 3;

    int probCount = 0;

    public int getTotalProb() {
        return probCount;
    }

    public int getCapacity() {
        return this.capacity;
    }

    public LimitMaxHashArraySet(int maxCount) {
        this(maxCount, DEFAULT_LOAD_FACTOR, DEFAULT_MAX_PROBE_LENGTH);
    }

    public LimitMaxHashArraySet(int maxCount, float loadFactor, int maxProbeLength) {
        this.function = Hashing.murmur3_32();

        if (maxCount > MAXIMUM_CAPACITY) {
            maxCount = MAXIMUM_CAPACITY;
        }
        if (maxCount <= 0) {
            maxCount = DEFAULT_CAPACITY;
        }
        this.threshold = maxCount;

        int cap = (int)((float) maxCount / loadFactor) + 1;
        this.capacity = tableSizeFor(cap);

        this.target = new byte[this.capacity << SHIFT_UNIT];
        this.states = new byte[this.capacity];
        this.maxProbeLength = maxProbeLength;
    }

    public LimitMaxHashArraySet(int maxCount, byte[] target, byte[] states) {
        this.function = Hashing.murmur3_32();
        this.capacity = states.length;
        this.threshold = maxCount;
        this.maxProbeLength = DEFAULT_MAX_PROBE_LENGTH;
        this.target = target;
        this.states = states;
    }

    public static int capacityForMaxCount(int maxCount, float loadFactor) {
        int cap = (int)((float) maxCount / loadFactor) + 1;
        return tableSizeFor(cap);
    }

    public static int capacityForMaxCount(int maxCount) {
        return capacityForMaxCount(maxCount, DEFAULT_LOAD_FACTOR);
    }

    public int add(String origin) {
        if (origin == null) {
            return -1;
        }
        int hash = function.hashString(origin, Charset.defaultCharset()).asInt();
        if (this.size >= this.threshold) {
            int fb = find(hash);
            if (fb > 0) {
                return fb;
            }
            return -1;
        }

        for (int i = 0; i < this.maxProbeLength; i++) {
            int bucket = (this.capacity - 1) & (hash + i);
            int offset = bucket << SHIFT_UNIT;
            if (this.states[bucket] == 0) {
                ByteArrayUtil.putInt(offset, target, hash);
                this.states[bucket] = 1;
                this.size++;
                return bucket;
            }
            if (ByteArrayUtil.getInt(offset, target) == hash) {
                return bucket;
            }
            probCount++;
        }
        return -1;
    }

    public boolean contains(String origin) {
        if (origin == null) {
            return false;
        }

        int hash = function.hashString(origin, Charset.defaultCharset()).asInt();
        return find(hash) >= 0;
    }

    public int getPosition(String origin) {
        if (origin == null) {
            return -1;
        }

        int hash = function.hashString(origin, Charset.defaultCharset()).asInt();
        return find(hash);
    }

    public int size() {
        return this.size;
    }

    private int find(int hash) {
        for (int i = 0; i < this.maxProbeLength; i++) {
            int bucket = (this.capacity - 1) & (hash + i);
            if (this.states[bucket] == 0) {
                return -1;
            }
            int offset = bucket << SHIFT_UNIT;
            if (ByteArrayUtil.getInt(offset, target) == hash) {
                return bucket;
            }
            probCount++;
        }

        return -1;
    }

    static final int tableSizeFor(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }
}
