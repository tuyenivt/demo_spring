package com.example.caching.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/api/cache")
@RequiredArgsConstructor
public class CacheController {

    private final CacheManager cacheManager;

    @GetMapping("/names")
    public Collection<String> getCacheNames() {
        return cacheManager.getCacheNames();
    }

    @DeleteMapping("/{cacheName}")
    public ResponseEntity<Void> clearCache(@PathVariable String cacheName) {
        var cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{cacheName}/{key}")
    public ResponseEntity<Void> evictKey(@PathVariable String cacheName, @PathVariable String key) {
        var cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
