package com.example.database.replication.controller;

import com.example.database.replication.dto.CreateUserRequest;
import com.example.database.replication.dto.UserResponse;
import com.example.database.replication.entity.User;
import com.example.database.replication.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        var saved = userService.createUser(request);
        var afterWrite = userService.findByIdAfterWrite(saved.getId());
        return afterWrite
                .map(u -> ResponseEntity.ok(UserResponse.from(u)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public Page<UserResponse> findAll(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return userService.findAll(PageRequest.of(page, size)).map(UserResponse::from);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        var user = userService.findById(id);
        return user.map(u -> ResponseEntity.ok(UserResponse.from(u))).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<List<UserResponse>> getByName(@PathVariable String name) {
        return ResponseEntity.ok(userService.findByName(name).stream().map(UserResponse::from).toList());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
