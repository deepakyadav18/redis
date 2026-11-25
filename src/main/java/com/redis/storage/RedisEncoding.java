package com.redis.storage;

public enum RedisEncoding {
    INT,
    EMBSTR,
    RAW,
    QUICKLIST,
    LISTPACK,
    INTSET,
    HASHTABLE,
    SKIPLIST,

    // Temporary Java backing structure; not Redis's QUICKLIST.
    DEQUE
}