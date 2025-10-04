package com.debajyoti.service;

import com.debajyoti.entity.User;
import java.util.List;

public interface UserService {
    User save(User user);
    User findByUsername(String username);
    User findByEmail(String email);
    User findById(Long id);
    List<User> findAll();
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    void deleteById(Long id);
}