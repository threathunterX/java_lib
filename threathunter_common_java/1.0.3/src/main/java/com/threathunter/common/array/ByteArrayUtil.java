package com.threathunter.common.array;

/**
 * Created by daisy on 17/3/22.
 */
public class ByteArrayUtil {
    /**
     * Search the key in the target array, the search range is restricted by the total length of elements
     * in this target array.
     * Always return the offset that the new key should be inserted if needed.
     * Generally will not return 0, because the firstOffset will not be 0 in fact.
     *
     * Compared by the first-4 bytes(int) of each element.
     *
     * There will be no more than 20 element.
     * Each element may have 4, 8, 22*4 bytes, and the first byte will be used to sort and search
     * @param target
     * @param firstOffset offset of first element
     * @param count current count of element in the target
     * @param elementSize
     * @param key
     * @return if searched, return the offset, or the targetOffset * -1, targetOffset: offset that the key need to insert
     */
    public static int binarySearchInt32(byte[] target, int firstOffset, int count, int elementSize, int key) {
        int virLow = 0;
        int virHigh = count - 1;

        while (virLow <= virHigh) {
            int virMid = (virLow + virHigh) >>> 1;
            int midVal = getInt(virMid * elementSize + firstOffset, target);

            if (midVal < key) {
                virLow = virMid + 1;
            } else if (midVal > key) {
                virHigh = virMid - 1;
            } else {
                return virMid * elementSize + firstOffset;
            }
        }
        return -virLow * elementSize - firstOffset;
    }

    /**
     * Insert an element to target array.
     * When insert, just insert the first 4-bytes.
     * @param target
     * @param firstOffset offset of first element
     * @param insertOffset target index that the key to be added to the array
     * @param currentCount count of element in the target array before insert
     * @param elementSize size of a element
     * @param key
     * @return the inserted index
     */
    public static void insertInt32(byte[] target, int firstOffset, int insertOffset, int currentCount, int elementSize, int key) {
        checkLength(target, firstOffset + (currentCount + 1) * elementSize);

        int totalElementLength = currentCount * elementSize;
        int moveLength = totalElementLength - (insertOffset - firstOffset);

        System.arraycopy(target, insertOffset, target, insertOffset + elementSize, moveLength);

        putInt32(insertOffset, target, key);
    }

    public static byte getByte(int offset, byte[] target) {
        checkLength(target, 1 + offset);

        return target[offset];
    }

    public static byte putByte(int offset, byte[] target, byte b) {
        checkLength(target, 1 + offset);

        target[offset] = b;
        return b;
    }

    public static int addInt(int offset, byte[] target, int add) {
        checkLength(target, 4 + offset);

        int current = target[offset] << 24 | (target[offset + 1] & 255) << 16 | (target[offset + 2] & 255) << 8 | target[offset + 3] & 255;
        int result = current + add;

        return putInt32(offset, target, result);
    }

    public static int putInt(int offset, byte[] target, int value) {
        checkLength(target, 4 + offset);
        return putInt32(offset, target, value);
    }

    public static int getInt(int offset, byte[] target) {
        checkLength(target, 4 + offset);
        return getInt32(offset, target);
    }

    public static double putDouble(int offset, byte[] target, double value) {
        checkLength(target, 8 + offset);
        return putDouble64(offset, target, value);
    }

    public static double getDouble(int offset, byte[] target) {
        checkLength(target, 8 + offset);
        return getDouble64(offset, target);
    }

    public static double addDouble(int offset, byte[] target, double value) {
        checkLength(target, 8 + offset);
        double result = getDouble64(offset, target) + value;
        return putDouble64(offset, target, result);
    }

    public static long putLong(int offset, byte[] target, long value) {
        checkLength(target, 8 + offset);
        return putLong64(offset, target, value);
    }

    public static long getLong(int offset, byte[] target) {
        checkLength(target, 8 + offset);
        return getLong64(offset, target);
    }

    // TODO add test
    public static long addLong(int offset, byte[] target, long value) {
        checkLength(target, 8 + offset);
        long result = getLong64(offset, target) + value;
        return putLong64(offset, target, result);
    }

    private static void checkLength(byte[] target, int length) {
        if (target.length < length) {
            throw new RuntimeException("byte array length is less than: " + length);
        }
    }

    private static int putInt32(int offset, byte[] target, int value) {
        target[offset] = (byte) (value >> 24);
        target[offset + 1] = (byte) (value >> 16);
        target[offset + 2] = (byte) (value >> 8);
        target[offset + 3] = (byte) value;

        return value;
    }

    private static int getInt32(int offset, byte[] target) {
        return target[offset + 0] << 24 | (target[offset + 1] & 255) << 16 | (target[offset + 2] & 255) << 8 | target[offset + 3] & 255;
    }

    private static long putLong64(int offset, byte[] target, long value) {
        long temp = value;
        for (int i = 7; i >= 0; --i) {
            target[i + offset] = (byte) ((int) (temp & 255L));
            temp >>= 8;
        }

        return value;
    }

    private static long getLong64(int offset, byte[] target) {
        return ((long) target[offset + 0] & 255L) << 56 | ((long) target[offset + 1] & 255L) << 48 |
                ((long) target[offset + 2] & 255L) << 40 | ((long) target[offset + 3] & 255L) << 32 |
                ((long) target[offset + 4] & 255L) << 24 | ((long) target[offset + 5] & 255L) << 16 |
                ((long) target[offset + 6] & 255L) << 8 | (long) target[offset + 7] & 255L;
    }

    private static double putDouble64(int offset, byte[] target, double value) {
        putLong64(offset, target, Double.doubleToLongBits(value));
        return value;
    }

    private static double getDouble64(int offset, byte[] target) {
        return Double.longBitsToDouble(getLong64(offset, target));
    }
}
