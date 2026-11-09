package com.springboot.EasyPay.repository;

import com.springboot.EasyPay.Dto.response.EmployeeInfoByHrDto;
import com.springboot.EasyPay.model.Employee;
import com.springboot.EasyPay.model.HR;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee,Long> {

    @Query("""
            select new com.springboot.EasyPay.Dto.response.EmployeeInfoByHrDto(e.id,e.fullName, e.email, e.phone, e.address, e.designation, e.department,e.joiningDate, e.basicSalary, u.username, h.name)
            from Employee e
            JOIN e.user u
            JOIN e.hr h
            where h.id = ?1
            """)
    List<EmployeeInfoByHrDto> getEmployeeByHrId(Long hrId, Pageable pageable);

    @Query("""
            select e from Employee e
            JOIN e.user u
            where u.username=?1
            """)
    Employee getByUsername(String employeeUsername);

    @Query("""
    SELECT e
    FROM Employee e
    WHERE e.user.username = :username
    """)
    Optional<Employee> findEmployeeByUsername(
            @Param("username") String username);
}
