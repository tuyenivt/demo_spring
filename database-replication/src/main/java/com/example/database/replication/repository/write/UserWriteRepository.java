package com.example.database.replication.repository.write;

import com.example.database.replication.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional("writerTransactionManager")
public interface UserWriteRepository extends JpaRepository<User, Long> {
}