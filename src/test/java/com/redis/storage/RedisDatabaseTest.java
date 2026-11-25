package com.redis.storage;

public class RedisDatabaseTest {

    public static void main(String[] args) throws InterruptedException {

        testRemoveExistingKey();
        testRemoveMissingKey();
        testRemoveExpiredKey();
        testRemoveClearsExpiry();
        testRemoveTwice();

        System.out.println("All RedisDatabase tests passed!");
    }

    private static RedisObject createStringObject(String value) {
        return RedisObject.stringRaw(value);
    }

    private static void testRemoveExistingKey() {
        RedisDatabase database = new RedisDatabase();

        RedisObject object = createStringObject("Deepak");
        database.put("name", object);

        RedisObject removed = database.remove("name");

        assertSame(object, removed, "Should return the removed object");
        assertNull(database.get("name"), "Key should no longer exist");

        System.out.println("PASS: Remove existing key");
    }

    private static void testRemoveMissingKey() {
        RedisDatabase database = new RedisDatabase();

        RedisObject removed = database.remove("missing");

        assertNull(removed, "Removing a missing key should return null");

        System.out.println("PASS: Remove missing key");
    }

    private static void testRemoveExpiredKey() throws InterruptedException {
        RedisDatabase database = new RedisDatabase();

        database.put("name", createStringObject("Deepak"));

        // Expire immediately.
        database.expire("name", 0);

        RedisObject removed = database.remove("name");

        assertNull(removed, "Expired key should return null");
        assertNull(database.get("name"), "Expired key should not exist");

        System.out.println("PASS: Remove expired key");
    }

    private static void testRemoveClearsExpiry() {
        RedisDatabase database = new RedisDatabase();

        database.put("name", createStringObject("Deepak"));
        database.expire("name", 100);

        RedisObject removed = database.remove("name");

        assertNotNull(removed, "Should return existing object");
        assertEquals(-2L, database.ttl("name"), "Key should not exist");

        System.out.println("PASS: Remove clears expiry");
    }

    private static void testRemoveTwice() {
        RedisDatabase database = new RedisDatabase();

        database.put("name", createStringObject("Deepak"));

        RedisObject firstRemoval = database.remove("name");
        RedisObject secondRemoval = database.remove("name");

        assertNotNull(firstRemoval, "First removal should return object");
        assertNull(secondRemoval, "Second removal should return null");

        System.out.println("PASS: Remove same key twice");
    }

    private static void assertNull(Object actual, String message) {
        if (actual != null) {
            throw new AssertionError(message + ". Actual: " + actual);
        }
    }

    private static void assertNotNull(Object actual, String message) {
        if (actual == null) {
            throw new AssertionError(message);
        }
    }

    private static void assertSame(
            Object expected,
            Object actual,
            String message
    ) {
        if (expected != actual) {
            throw new AssertionError(message);
        }
    }

    private static void assertEquals(
            long expected,
            long actual,
            String message
    ) {
        if (expected != actual) {
            throw new AssertionError(
                    message + ". Expected: " + expected + ", Actual: " + actual
            );
        }
    }
}