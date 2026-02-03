package com.example.database.replication.repository.read;

import com.example.database.replication.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional("readerTransactionManager")
public interface UserReadRepository extends JpaRepository<User, Long> {
    List<User> findByName(String name);

    Optional<User> findByEmail(String email);
}
