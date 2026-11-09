package com.springboot.EasyPay.repository;

import com.springboot.EasyPay.model.HR;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface HRRepository extends JpaRepository<HR,Long> {
    @Query("""
            select h from HR h
            JOIN h.user u
            where u.username=?1
            """)
    HR getByUsername(String hrUsername);
}
