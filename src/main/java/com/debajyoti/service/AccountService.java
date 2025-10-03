package com.debajyoti.service;

import com.debajyoti.entity.Account;
import com.debajyoti.entity.User;

import java.util.List;

public interface AccountService {
    List<Account> findAll();
    List<Account> findByOwner(User owner);
    Account findById(Long id, User currentUser);
    Account save(Account account, User currentUser);
    void delete(Long id, User currentUser);
}
