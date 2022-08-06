package com.padingpading.interview.collection.b_linkedhashmap;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author libin
 * @description
 * @date 2022-05-16
 */
public class LruCache<K, V> extends LinkedHashMap<K, V> {
    
    private static final int MAX_NODE_NUM = 2 << 4;
    
    private int limit;
    
    public LruCache() {
        this(MAX_NODE_NUM);
    }
    
    public LruCache(int limit) {
        super(limit, 0.75f, true);
        this.limit = limit;
    }
    
    public V putValue(K key, V val) {
        return put(key, val);
    }
    
    public V getValue(K key) {
        return get(key);
    }
    
    /**
     * 判断存储元素个数是否预定阈值
     * @return 超限返回 true，否则返回 false
     */
    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > limit;
    }
}
