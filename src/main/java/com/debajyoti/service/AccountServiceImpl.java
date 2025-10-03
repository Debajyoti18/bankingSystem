package com.debajyoti.service;

import com.debajyoti.dao.AccountRepository;
import com.debajyoti.entity.Account;
import com.debajyoti.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Override
    public List<Account> findAll() {
        return accountRepository.findAll();
    }

    @Override
    public List<Account> findByOwner(User owner) {
        return accountRepository.findByOwner(owner);
    }

    @Override
    public Account findById(Long id, User currentUser) {
        // Admin and Manager can access any account
        if (currentUser.getRole() == User.Role.ADMIN || currentUser.getRole() == User.Role.MANAGER) {
            return accountRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Account not found"));
        }
        
        // Customers can only access their own accounts
        return accountRepository.findByIdAndOwner(id, currentUser)
                .orElseThrow(() -> new SecurityException("Access denied to account"));
    }

    @Override
    @Transactional
    public Account save(Account account, User currentUser) {
        // For existing accounts, check ownership
        if (account.getId() != null) {
            Account existing = accountRepository.findById(account.getId())
                    .orElseThrow(() -> new RuntimeException("Account not found"));
            
            // Only admin, manager, or account owner can update
            if (currentUser.getRole() != User.Role.ADMIN && 
                currentUser.getRole() != User.Role.MANAGER && 
                !existing.getOwner().getId().equals(currentUser.getId())) {
                throw new SecurityException("Access denied to update account");
            }
            
            // Preserve the original owner
            account.setOwner(existing.getOwner());
        }
        
        return accountRepository.save(account);
    }

    @Override
    @Transactional
    public void delete(Long id, User currentUser) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        
        // Only admin and manager can delete accounts
        if (currentUser.getRole() != User.Role.ADMIN && currentUser.getRole() != User.Role.MANAGER) {
            throw new SecurityException("Access denied to delete account");
        }
        
        accountRepository.deleteById(id);
    }
}
