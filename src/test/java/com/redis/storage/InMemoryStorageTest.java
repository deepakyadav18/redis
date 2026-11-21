package com.redis.storage;

public class InMemoryStorageTest {

    public static void main(String[] args) throws InterruptedException {
        InMemoryStorage storage = new InMemoryStorage();

        // Test 1: Normal SET and GET
        storage.set("name", "Deepak");
        assert "Deepak".equals(storage.get("name"));
        System.out.println("Test 1 passed");

        // Test 2: Key without expiration
        assert storage.ttl("name") == -1;
        System.out.println("Test 2 passed");

        // Test 3: Expire existing key
        assert storage.expire("name", 2);
        assert storage.ttl("name") >= 1;
        System.out.println("Test 3 passed");

        // Test 4: Key expires after 2 seconds
        Thread.sleep(2200);
        assert storage.get("name") == null;
        assert storage.ttl("name") == -2;
        System.out.println("Test 4 passed");

        // Test 5: Expire nonexistent key
        assert !storage.expire("unknown", 5);
        System.out.println("Test 5 passed");

        // Test 6: SET clears previous expiration
        storage.set("city", "Lucknow");
        storage.expire("city", 10);
        storage.set("city", "Delhi");
        assert storage.ttl("city") == -1;
        assert "Delhi".equals(storage.get("city"));
        System.out.println("Test 6 passed");

        // Test 7: Expire immediately
        storage.set("temp", "value");
        assert storage.expire("temp", 0);
        assert storage.get("temp") == null;
        assert storage.ttl("temp") == -2;
        System.out.println("Test 7 passed");

        // Test 8: DEL also removes expiration
        storage.set("session", "abc");
        storage.expire("session", 10);
        assert storage.delete(java.util.List.of("session")) == 1;
        assert storage.ttl("session") == -2;
        System.out.println("Test 8 passed");

        // Active expiration test
        storage.set("a", "1");
        storage.set("b", "2");
        storage.set("c", "3");

        storage.expire("a", 0);
        storage.expire("b", 100);
        storage.expire("c", 0);

        int removed = storage.activeExpire(3);

        assert removed == 2;
        assert storage.get("a") == null;
        assert storage.get("b").equals("2");
        assert storage.get("c") == null;

        System.out.println("Active expiration test passed");

    }
}