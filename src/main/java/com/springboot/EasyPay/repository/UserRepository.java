package com.springboot.EasyPay.repository;

import com.springboot.EasyPay.Dto.request.EmployeeDto;
import com.springboot.EasyPay.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    @Query("""
            select u
            from User u
            where u.username=?1 AND u.isActive = ?2
            """)
    User loadUserByUsername(String username,boolean isActive);

    @Query("SELECT u FROM User u WHERE u.username = ?1")
    Optional<User> findUserByUsername( String username);
}
