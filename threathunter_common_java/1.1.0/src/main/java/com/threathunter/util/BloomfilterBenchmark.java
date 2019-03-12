/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.threathunter.util;

import java.util.*;

/**
 * A (very) simple benchmark to evaluate the performance of the Bloom filter class.
 *
 * created by www.threathunter.cn
 */
public class BloomfilterBenchmark {
    static int elementCount = 500000; // Number of elements to test

    public static void Assert(boolean shouldBeTrue) {
        if (!shouldBeTrue) {
            throw new RuntimeException("fail");
        }
    }

    public static void printStat(long start, long end) {
        double diff = (end - start) / 1000.0;
        System.out.println(diff + "s, " + (elementCount / diff) + " elements/s");
    }

    public static void multiAdd(final List<String> toBeAdd, final BloomFilter<String> bf,
                                final int threadCounts) {
        final int length = toBeAdd.size();
        final Thread[] threads = new Thread[threadCounts];
        for (int i = 0; i < threadCounts; i++) {
            final int id = i;
            threads[i] = new Thread() {
                @Override
                public void run() {
                    for(int j = id; j < length; j+=threadCounts) {
                        bf.add(toBeAdd.get(j));
                    }
                }
            };
        }

        for (int i = 0; i < threadCounts; i++) {
            threads[i].start();
        }

        for (int i = 0; i < threadCounts; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private List<String> existingElements = new ArrayList<>();
    private List<String> nonExistingElements = new ArrayList<>();
    private BloomFilter<String> bf = new BloomFilter<String>(0.001, elementCount);

    public void setUpData() {
        final Random r = new Random();

        // Generate existing elements first
        Set<String> existingSet = new HashSet<>();
        Set<String> nonExistingSet = new HashSet<>();
        while (existingSet.size() < elementCount) {
            byte[] b = new byte[200];
            r.nextBytes(b);
            String str = new String(b);
            existingSet.add(str);
        }

        while (nonExistingSet.size() < elementCount) {
            byte[] b = new byte[200];
            r.nextBytes(b);
            String str = new String(b);
            if (!existingSet.contains(str)) {
                nonExistingSet.add(str);
            }
        }

        existingElements = new ArrayList<>(existingSet);
        nonExistingElements = new ArrayList<>(nonExistingSet);
    }

    public void addTest() {
        System.out.println("Testing " + elementCount + " elements");
        System.out.println("k is " + bf.getK());

        // Add elements
        System.out.print("add(): ");
        long start_add = System.currentTimeMillis();
        for(String s : existingElements) {
            bf.add(s);
        }
        long end_add = System.currentTimeMillis();
        printStat(start_add, end_add);
    }

    public void getTest() {
        // Check for existing elements with contains()
        System.out.print("contains(), existing: ");
        long start_contains = System.currentTimeMillis();
        for(String s : existingElements) {
            Assert(bf.contains(s));
        }
        long end_contains = System.currentTimeMillis();
        printStat(start_contains, end_contains);

        // Check for existing elements with containsAll()
        System.out.print("containsAll(), existing: ");
        long start_containsAll = System.currentTimeMillis();
        Assert(bf.containsAll(existingElements));
        long end_containsAll = System.currentTimeMillis();
        printStat(start_containsAll, end_containsAll);

        // Check for nonexisting elements with contains()
        System.out.print("contains(), nonexisting: ");
        int falsePositive = 0;
        long start_ncontains = System.currentTimeMillis();
        for(String s : nonExistingElements) {
            if (bf.contains(s)) {
                falsePositive++;
            }
        }
        long end_ncontains = System.currentTimeMillis();
        printStat(start_ncontains, end_ncontains);
        System.out.println("the falsePositive is " + 1.0*falsePositive/elementCount);

        // Check for nonexisting elements with containsAll()
        System.out.print("containsAll(), nonexisting: ");
        long start_ncontainsAll = System.currentTimeMillis();
        bf.containsAll(nonExistingElements);
        long end_ncontainsAll = System.currentTimeMillis();
        printStat(start_ncontainsAll, end_ncontainsAll);
    }

    public void multithreadTest() {
        bf.clear();
        addTest();
        BitSet rightValue = (BitSet)bf.getBitSet().clone();

        // add 1
        bf.clear();
        addTest();
        Assert(rightValue.equals(bf.getBitSet()));

        // add by 1 thread
        bf.clear();
        multiAdd(existingElements, bf, 1);
        Assert(rightValue.equals(bf.getBitSet()));

        // add by 10 threads
        bf.clear();
        multiAdd(existingElements, bf, 300);
        Assert(rightValue.equals(bf.getBitSet()));

    }

    public static void main(String[] argv) {
        BloomfilterBenchmark test = new BloomfilterBenchmark();
        test.setUpData();

//        test.addTest();
//        test.getTest();

        for (int i = 0; i < 10; i++) {
            test.multithreadTest();
        }
    }
}
