package com.springboot.EasyPay.repository;

import com.springboot.EasyPay.enums.Status;
import com.springboot.EasyPay.model.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRequestRepository  extends JpaRepository<LeaveRequest,Long> {
    @Query("""
        SELECT l FROM LeaveRequest l 
        WHERE l.employee.id = :employeeId 
        AND l.status = :status 
        AND l.startDate <= :periodEnd 
        AND l.endDate >= :periodStart
        """)
    List<LeaveRequest> findOverlappingLeaves(
            Long employeeId,
            Status status,
            LocalDate periodEnd,
            LocalDate periodStart
    );

    // Get all leave requests assigned to a particular manager
    @Query("""
        SELECT l FROM LeaveRequest l  
        WHERE l.manager.id = ?1  
        ORDER BY l.appliedAt DESC
        """)
    List<LeaveRequest> findByManagerIdOrderByAppliedAtDesc(Long managerId);


    // Get all leave requests submitted by a particular employee
    @Query("""
        SELECT l FROM LeaveRequest l 
        JOIN l.employee e 
        JOIN e.user u 
        WHERE u.username = ?1 
        ORDER BY l.appliedAt DESC
        """)
    List<LeaveRequest> findByEmployeeUsername(String username);

    // Check whether the employee already has a leave request
// overlapping the requested dates.
    @Query("""
    SELECT COUNT(l)
    FROM LeaveRequest l
    WHERE l.employee.id = :employeeId
    AND l.startDate <= :endDate
    AND l.endDate >= :startDate
    """)
    long countOverlappingLeaves(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

}
