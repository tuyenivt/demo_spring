package com.example.websocket.service;

import com.example.websocket.dto.ChatResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class MessageHistoryService {

    private static final int MAX_HISTORY_SIZE = 50;

    private final Deque<ChatResponse> history = new ArrayDeque<>();
    private final ReentrantLock lock = new ReentrantLock();

    public void add(ChatResponse chatResponse) {
        lock.lock();
        try {
            if (history.size() >= MAX_HISTORY_SIZE) {
                history.removeFirst();
            }
            history.addLast(chatResponse);
        } finally {
            lock.unlock();
        }
    }

    public List<ChatResponse> getRecentMessages() {
        lock.lock();
        try {
            return new ArrayList<>(history);
        } finally {
            lock.unlock();
        }
    }
}
