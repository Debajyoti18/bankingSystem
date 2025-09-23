package com.debajyoti.controller;

import com.debajyoti.entity.Account;
import com.debajyoti.entity.User;
import com.debajyoti.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @GetMapping("/accounts")
    public ResponseEntity<List<Account>> getAllAccounts() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        
        List<Account> accounts;
        
        // Admin and Manager can see all accounts, customers see only their own
        if (currentUser.getRole() == User.Role.ADMIN || currentUser.getRole() == User.Role.MANAGER) {
            accounts = accountService.findAll();
        } else {
            accounts = accountService.findByOwner(currentUser);
        }
        
        return ResponseEntity.ok(accounts);
    }

    @PostMapping("/accounts")
    public ResponseEntity<?> createAccount(@Valid @RequestBody Account account) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        
        // Set the current user as owner for new accounts
        account.setOwner(currentUser);
        
        try {
            Account savedAccount = accountService.save(account, currentUser);
            return ResponseEntity.ok(savedAccount);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Failed to create account: " + e.getMessage()));
        }
    }

    @GetMapping("/accounts/{id}")
    public ResponseEntity<?> getAccount(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        
        try {
            Account account = accountService.findById(id, currentUser);
            return ResponseEntity.ok(account);
        } catch (SecurityException e) {
            return ResponseEntity.status(403)
                .body(Map.of("error", "Access denied: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/accounts/{id}")
    public ResponseEntity<?> updateAccount(@PathVariable Long id, @Valid @RequestBody Account account) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        
        account.setId(id);
        
        try {
            Account updatedAccount = accountService.save(account, currentUser);
            return ResponseEntity.ok(updatedAccount);
        } catch (SecurityException e) {
            return ResponseEntity.status(403)
                .body(Map.of("error", "Access denied: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Failed to update account: " + e.getMessage()));
        }
    }

    @DeleteMapping("/accounts/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> deleteAccount(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        
        try {
            accountService.delete(id, currentUser);
            return ResponseEntity.ok(Map.of("message", "Account deleted successfully"));
        } catch (SecurityException e) {
            return ResponseEntity.status(403)
                .body(Map.of("error", "Access denied: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Failed to delete account: " + e.getMessage()));
        }
    }

    // Admin-only endpoints
    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsers() {
        // Implementation for user management
        return ResponseEntity.ok(Map.of("message", "Admin user management endpoint"));
    }
}
