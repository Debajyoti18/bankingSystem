package com.debajyoti.dao;

import com.debajyoti.entity.Account;
import com.debajyoti.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    
    // Find accounts by owner (for customer access)
    List<Account> findByOwner(User owner);
    
    // Find account by ID and owner (for security)
    Optional<Account> findByIdAndOwner(Long id, User owner);
    
    // Find by account number
    Optional<Account> findByAccNumber(Integer accNumber);
    
    // Custom query for admin/manager access with user details
    @Query("SELECT a FROM Account a JOIN FETCH a.owner WHERE a.id = :id")
    Optional<Account> findByIdWithOwner(@Param("id") Long id);
}
