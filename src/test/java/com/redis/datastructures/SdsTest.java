package com.redis.datastructures;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class SdsTest {

    public static void main(String[] args) {
        testCreation();
        testLengthAndCapacity();
        testHeaderSelection();
        testAppend();
        testAppendGrowth();
        testAppendSds();
        testSelfAppend();
        testClear();
        testAppendAfterClear();
        testRemoveFreeSpace();
        testSetLength();
        testRemoveRange();
        testRange();
        testRangeSds();
        testTrim();
        testBinarySafety();
        testCompare();
        testEqualsAndHashCode();
        testGetBytesCopy();

        System.out.println("All SDS tests passed");
    }

    private static void testCreation() {
        Sds sds = Sds.fromString("Hello");

        assertEquals("Hello", sds.getString());
        assertEquals(5, sds.length());

        byte[] bytes = "Hello".getBytes(StandardCharsets.UTF_8);
        Sds fromBytes = Sds.fromBytes(bytes);

        assertEquals("Hello", fromBytes.getString());

        bytes[0] = 'X';

        assertEquals("Hello", fromBytes.getString());

        Sds empty = Sds.fromString("");

        assertEquals(0, empty.length());
        assertEquals("", empty.getString());

        System.out.println("Creation tests passed");
    }

    private static void testLengthAndCapacity() {
        Sds sds = Sds.fromString("Hello");

        assertEquals(5, sds.length());
        assertTrue(sds.capacity() >= sds.length());
        assertEquals(sds.capacity() - sds.length(), sds.available());
        assertEquals(Sds.HeaderType.TYPE_8, sds.getHeaderType());

        Sds empty = Sds.fromString("");

        assertEquals(Sds.HeaderType.TYPE_8, empty.getHeaderType());

        System.out.println("Length and capacity tests passed");
    }

    private static void testHeaderSelection() {
        assertEquals(Sds.HeaderType.TYPE_5, Sds.selectHeaderType(0));
        assertEquals(Sds.HeaderType.TYPE_5, Sds.selectHeaderType(31));

        assertEquals(Sds.HeaderType.TYPE_8, Sds.selectHeaderType(32));
        assertEquals(Sds.HeaderType.TYPE_8, Sds.selectHeaderType(255));

        assertEquals(Sds.HeaderType.TYPE_16, Sds.selectHeaderType(256));
        assertEquals(Sds.HeaderType.TYPE_16, Sds.selectHeaderType(65535));

        assertEquals(Sds.HeaderType.TYPE_32, Sds.selectHeaderType(65536));

        System.out.println("Header selection tests passed");
    }

    private static void testAppend() {
        Sds sds = Sds.fromString("Hello");

        sds.append(" World");

        assertEquals("Hello World", sds.getString());
        assertEquals(11, sds.length());

        sds.append("!");

        assertEquals("Hello World!", sds.getString());

        sds.append("");

        assertEquals("Hello World!", sds.getString());

        Sds bytesSds = Sds.fromString("Hello");
        bytesSds.append(" World".getBytes(StandardCharsets.UTF_8));

        assertEquals("Hello World", bytesSds.getString());

        System.out.println("Append tests passed");
    }

    private static void testAppendGrowth() {
        Sds sds = Sds.fromString("Hello");
        int originalCapacity = sds.capacity();

        sds.append(" World this is a much longer string");

        assertEquals("Hello World this is a much longer string", sds.getString());
        assertTrue(sds.capacity() > originalCapacity);
        assertTrue(sds.capacity() >= sds.length());

        Sds type8 = Sds.fromString("A".repeat(250));

        assertEquals(Sds.HeaderType.TYPE_8, type8.getHeaderType());

        type8.append("1234567890");

        assertEquals(Sds.HeaderType.TYPE_16, type8.getHeaderType());

        Sds large = Sds.fromString("A");
        large.append("B".repeat(1_100_000));

        assertEquals(1_100_001, large.length());
        assertEquals(Sds.HeaderType.TYPE_32, large.getHeaderType());

        System.out.println("Append growth tests passed");
    }

    private static void testAppendSds() {
        Sds first = Sds.fromString("Hello");
        Sds second = Sds.fromString(" World");

        first.append(second);

        assertEquals("Hello World", first.getString());

        System.out.println("Append SDS tests passed");
    }

    private static void testSelfAppend() {
        Sds sds = Sds.fromString("Hello");

        sds.append(sds);

        assertEquals("HelloHello", sds.getString());
        assertEquals(10, sds.length());

        System.out.println("Self append tests passed");
    }

    private static void testClear() {
        Sds sds = Sds.fromString("Hello World");
        int originalCapacity = sds.capacity();

        sds.clear();

        assertEquals(0, sds.length());
        assertEquals("", sds.getString());
        assertEquals(originalCapacity, sds.capacity());

        System.out.println("Clear tests passed");
    }

    private static void testAppendAfterClear() {
        Sds sds = Sds.fromString("Hello");

        sds.clear();
        sds.append("World");

        assertEquals("World", sds.getString());
        assertEquals(5, sds.length());

        System.out.println("Append after clear tests passed");
    }

    private static void testRemoveFreeSpace() {
        Sds sds = Sds.fromString("Hello");

        sds.append(" World this is a longer string");

        assertTrue(sds.capacity() > sds.length());

        sds.removeFreeSpace();

        assertEquals("Hello World this is a longer string", sds.getString());
        assertEquals(sds.length(), sds.capacity());
        assertEquals(0, sds.available());

        Sds larger = Sds.fromString("A".repeat(300));

        assertEquals(Sds.HeaderType.TYPE_16, larger.getHeaderType());

        larger.setLength(100);
        larger.removeFreeSpace();

        assertEquals(100, larger.capacity());
        assertEquals(Sds.HeaderType.TYPE_8, larger.getHeaderType());

        Sds empty = Sds.fromString("Hello");

        empty.clear();
        empty.removeFreeSpace();

        assertEquals(0, empty.length());
        assertEquals(0, empty.capacity());
        assertEquals(Sds.HeaderType.TYPE_8, empty.getHeaderType());

        System.out.println("Remove free space tests passed");
    }

    private static void testSetLength() {
        Sds sds = Sds.fromString("Hello World");

        sds.setLength(5);

        assertEquals("Hello", sds.getString());
        assertEquals(5, sds.length());

        sds.setLength(0);

        assertEquals("", sds.getString());
        assertEquals(0, sds.length());

        assertThrows(() -> sds.setLength(-1));
        assertThrows(() -> sds.setLength(1));

        System.out.println("Set length tests passed");
    }

    private static void testRemoveRange() {
        Sds middle = Sds.fromString("HelloWorld");

        middle.removeRange(2, 5);

        assertEquals("Heorld", middle.getString());

        Sds beginning = Sds.fromString("HelloWorld");

        beginning.removeRange(0, 4);

        assertEquals("World", beginning.getString());

        Sds end = Sds.fromString("HelloWorld");

        end.removeRange(5, 9);

        assertEquals("Hello", end.getString());

        Sds entire = Sds.fromString("Hello");

        entire.removeRange(0, 4);

        assertEquals("", entire.getString());
        assertEquals(0, entire.length());

        assertThrows(() -> Sds.fromString("Hello").removeRange(-1, 2));
        assertThrows(() -> Sds.fromString("Hello").removeRange(1, 5));
        assertThrows(() -> Sds.fromString("Hello").removeRange(3, 2));

        System.out.println("Remove range tests passed");
    }

    private static void testRange() {
        Sds sds = Sds.fromString("Hello World");

        assertEquals("Hello", toString(sds.range(0, 4)));
        assertEquals("World", toString(sds.range(6, 10)));
        assertEquals("World", toString(sds.range(-5, -1)));
        assertEquals("Hello World", sds.rangeSds(0, 100).getString());

        assertArrayEquals(new byte[0], sds.range(20, 30));
        assertArrayEquals(new byte[0], sds.range(4, 2));
        assertArrayEquals(new byte[0], sds.range(-100, -90));

        System.out.println("Range tests passed");
    }

    private static void testRangeSds() {
        Sds sds = Sds.fromString("Hello World");

        Sds result = sds.rangeSds(6, 10);

        assertEquals("World", result.getString());

        System.out.println("Range SDS tests passed");
    }

    private static void testTrim() {
        Sds spaces = Sds.fromString("   Hello World   ");

        spaces.trim(" ".getBytes(StandardCharsets.UTF_8));

        assertEquals("Hello World", spaces.getString());

        Sds multiple = Sds.fromString("xxHello Worldyy");

        multiple.trim("xy".getBytes(StandardCharsets.UTF_8));

        assertEquals("Hello World", multiple.getString());

        Sds stars = Sds.fromString("***Hello***");

        stars.trim("*".getBytes(StandardCharsets.UTF_8));

        assertEquals("Hello", stars.getString());

        Sds all = Sds.fromString("xxxxxxxx");

        all.trim("x".getBytes(StandardCharsets.UTF_8));

        assertEquals("", all.getString());
        assertEquals(0, all.length());

        Sds unchanged = Sds.fromString("Hello World");

        unchanged.trim("xyz".getBytes(StandardCharsets.UTF_8));

        assertEquals("Hello World", unchanged.getString());

        Sds emptySet = Sds.fromString("Hello");

        emptySet.trim(new byte[0]);

        assertEquals("Hello", emptySet.getString());

        System.out.println("Trim tests passed");
    }

    private static void testBinarySafety() {
        byte[] data = {'A', 0, 'B', 0, 'C'};

        Sds sds = Sds.fromBytes(data);

        assertEquals(5, sds.length());
        assertArrayEquals(data, sds.getBytes());

        sds.append(new byte[]{0, 'D'});

        assertArrayEquals(new byte[]{'A', 0, 'B', 0, 'C', 0, 'D'}, sds.getBytes());

        Sds range = Sds.fromBytes(new byte[]{0, 1, 2, 3, 4, 5});

        assertArrayEquals(new byte[]{2, 3, 4}, range.range(2, 4));

        Sds remove = Sds.fromBytes(new byte[]{'A', 0, 'B', 0, 'C'});

        remove.removeRange(1, 2);

        assertArrayEquals(new byte[]{'A', 0, 'C'}, remove.getBytes());

        Sds trim = Sds.fromBytes(new byte[]{0, 1, 2, 3, 0, 1, 2});

        trim.trim(new byte[]{0, 1});

        assertArrayEquals(new byte[]{2, 3, 0, 1, 2}, trim.getBytes());

        System.out.println("Binary safety tests passed");
    }

    private static void testCompare() {
        Sds hello1 = Sds.fromString("Hello");
        Sds hello2 = Sds.fromString("Hello");

        assertEquals(0, hello1.compare(hello2));

        Sds apple = Sds.fromString("Apple");
        Sds banana = Sds.fromString("Banana");

        assertTrue(apple.compare(banana) < 0);
        assertTrue(banana.compare(apple) > 0);

        Sds prefix = Sds.fromString("Hello");
        Sds longer = Sds.fromString("Hello World");

        assertTrue(prefix.compare(longer) < 0);
        assertTrue(longer.compare(prefix) > 0);

        Sds high = Sds.fromBytes(new byte[]{(byte) 0xFF});
        Sds low = Sds.fromBytes(new byte[]{(byte) 0x80});

        assertTrue(high.compare(low) > 0);

        System.out.println("Compare tests passed");
    }

    private static void testEqualsAndHashCode() {
        Sds first = Sds.fromString("Hello");
        Sds second = Sds.fromString("Hello");
        Sds different = Sds.fromString("World");

        assertEquals(first, second);
        assertNotEquals(first, different);
        assertEquals(first.hashCode(), second.hashCode());

        Sds binary1 = Sds.fromBytes(new byte[]{'A', 0, 'B'});
        Sds binary2 = Sds.fromBytes(new byte[]{'A', 0, 'B'});

        assertEquals(binary1, binary2);
        assertEquals(binary1.hashCode(), binary2.hashCode());

        assertEquals("Hello", first.toString());

        System.out.println("Equals and hashCode tests passed");
    }

    private static void testGetBytesCopy() {
        Sds sds = Sds.fromString("Hello");

        byte[] bytes = sds.getBytes();

        bytes[0] = 'X';

        assertEquals("Hello", sds.getString());

        System.out.println("Get bytes copy tests passed");
    }

    private static String toString(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private static void assertEquals(Object expected, Object actual) {
        if (!java.util.Objects.equals(expected, actual)) {
            throw new AssertionError("Expected: " + expected + ", Actual: " + actual);
        }
    }

    private static void assertTrue(boolean condition) {
        if (!condition) {
            throw new AssertionError("Expected condition to be true");
        }
    }

    private static void assertArrayEquals(byte[] expected, byte[] actual) {
        if (!Arrays.equals(expected, actual)) {
            throw new AssertionError("Expected: " + Arrays.toString(expected) + ", Actual: " + Arrays.toString(actual));
        }
    }

    private static void assertNotEquals(Object first, Object second) {
        if (java.util.Objects.equals(first, second)) {
            throw new AssertionError("Expected values to be different");
        }
    }

    private static void assertThrows(Runnable runnable) {
        try {
            runnable.run();
        } catch (IllegalArgumentException expected) {
            return;
        }

        throw new AssertionError("Expected IllegalArgumentException");
    }
}