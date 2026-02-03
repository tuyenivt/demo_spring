package com.example.database.replication.service;

import com.example.database.replication.entity.User;
import com.example.database.replication.repository.read.UserReadRepository;
import com.example.database.replication.repository.write.UserWriteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserWriteRepository writeRepository;

    private final UserReadRepository readRepository;

    @Transactional("writerTransactionManager")
    public User createUser(User user) {
        return writeRepository.save(user);
    }

    // Read-after-write: Use writer for immediate consistency post-create
    @Transactional("writerTransactionManager")
    public Optional<User> findByIdAfterWrite(Long id) {
        return writeRepository.findById(id);
    }

    // Regular read: Use reader for scalability
    @Retryable(
            retryFor = {SQLException.class, DataAccessException.class},
            maxAttempts = 5,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    @Transactional("readerTransactionManager")
    public Optional<User> findById(Long id) {
        return readRepository.findById(id);
    }

    @Transactional("readerTransactionManager")
    public List<User> findByName(String name) {
        return readRepository.findByName(name);
    }

    @Transactional("writerTransactionManager")
    public void deleteById(Long id) {
        writeRepository.deleteById(id);
    }
}
