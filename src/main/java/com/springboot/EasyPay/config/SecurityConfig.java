package com.springboot.EasyPay.config;

import com.springboot.EasyPay.service.MyUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final MyUserDetailsService myUserDetailsService;

    @Bean
    public SecurityFilterChain defaultFilterChain(HttpSecurity http)
            throws Exception {

        http
                .cors(Customizer.withDefaults())

                // CSRF is disabled because this API uses stateless JWT
                // authentication through the Authorization header.
                .csrf(AbstractHttpConfigurer::disable)

                // Do not create HTTP sessions.
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(authorize -> authorize

                        // Admin onboarding
                        .requestMatchers(HttpMethod.POST, "/api/admin/add")
                        .permitAll()

                        // Login
                        .requestMatchers(HttpMethod.GET, "/api/auth/login")
                        .permitAll()

                        // Admin onboarding manager and HR
                        .requestMatchers(HttpMethod.POST, "/api/manager/add")
                        .hasAuthority("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/hr/add")
                        .hasAuthority("ADMIN")

                        // HR onboarding employee
                        .requestMatchers(HttpMethod.POST, "/api/employee/add")
                        .hasAuthority("HR")

                        // Employee leave application
                        .requestMatchers(HttpMethod.POST, "/api/leavereq/apply")
                        .hasAuthority("EMPLOYEE")

                        // Manager reviews leave
                        .requestMatchers(HttpMethod.PUT, "/api/reviewleave/{leaveId}")
                        .hasAuthority("MANAGER")

                        // Employee details
                        .requestMatchers(HttpMethod.GET, "/api/employee/{id}")
                        .permitAll()

                        // Manager details
                        .requestMatchers(HttpMethod.GET, "/api/manager/{id}")
                        .hasAuthority("MANAGER")

                        // HR details
                        .requestMatchers(HttpMethod.GET, "/api/hr/{id}")
                        .hasAuthority("HR")

                        // Update manager details
                        .requestMatchers(HttpMethod.PUT, "/api/manager")
                        .hasAuthority("MANAGER")

                        // Update HR details
                        .requestMatchers(HttpMethod.PUT, "/api/hr")
                        .hasAuthority("HR")

                        // HR assigns manager to employee
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/employee/assign/{managerId}/{employeeId}"
                        ).hasAuthority("HR")

                        // Create payroll
                        .requestMatchers(HttpMethod.POST, "/api/payroll/{employeeId}")
                        .hasAuthority("HR")

                        // Process payroll
                        .requestMatchers(HttpMethod.POST, "/api/payroll/process/{payrollId}")
                        .hasAuthority("HR")

                        // Mark payroll as paid
                        .requestMatchers(HttpMethod.PUT, "/api/payroll/pay/{payrollId}")
                        .hasAuthority("HR")

                        // Payslip access for HR and employee
                        .requestMatchers(HttpMethod.GET, "/api/payroll/payslip/{payrollId}")
                        .hasAnyAuthority("HR", "EMPLOYEE")

                        // Employee payroll history
                        .requestMatchers(HttpMethod.GET, "/api/payroll/employee/{employeeId}")
                        .hasAnyAuthority("EMPLOYEE", "HR")

                        // Add payroll components
                        .requestMatchers(HttpMethod.POST, "/api/payroll-components/{payrollId}")
                        .hasAuthority("HR")

                        // Get payroll components
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/payroll-components/payroll/{payrollId}"
                        ).hasAnyAuthority("HR", "EMPLOYEE")

                        // Manager leave requests
                        .requestMatchers(HttpMethod.GET, "/api/leavereq/manager")
                        .hasAuthority("MANAGER")

                        // Employee leave requests
                        .requestMatchers(HttpMethod.GET, "/api/leavereq/employee")
                        .hasAuthority("EMPLOYEE")

                        // Delete payroll component
                        .requestMatchers(HttpMethod.DELETE, "/api/payroll-components/{componentId}")
                        .hasAuthority("HR")

                        // Update components
                        .requestMatchers(HttpMethod.PATCH, "/components/**")
                        .hasAuthority("HR")

                        // Update employee
                        .requestMatchers(HttpMethod.PUT, "/api/employee/{id}")
                        .hasAnyAuthority("HR", "EMPLOYEE")

                        // Payroll status
                        .requestMatchers(HttpMethod.GET, "/api/payroll/status/{status}")
                        .hasAuthority("HR")

                        // Authentication user details
                        .requestMatchers(HttpMethod.GET, "/api/auth/user-details")
                        .authenticated()

                        // Payroll by date range
                        .requestMatchers(HttpMethod.GET, "/api/payroll/period")
                        .hasAuthority("HR")

                        // Search payroll
                        .requestMatchers(HttpMethod.GET, "/api/payroll/search")
                        .hasAuthority("HR")

                        // Employee earnings history
                        .requestMatchers(HttpMethod.GET, "/api/employee/earnings/{id}")
                        .hasAuthority("EMPLOYEE")

                        // Employee deductions history
                        .requestMatchers(HttpMethod.GET, "/api/employee/deductions/{id}")
                        .hasAuthority("EMPLOYEE")

                        // Monthly payroll report
                        .requestMatchers(HttpMethod.GET, "/api/payroll/reports/monthly")
                        .hasAuthority("HR")

                        // Employee payslip text
                        .requestMatchers(HttpMethod.GET, "/api/payroll/payslip-text/{payrollId}")
                        .hasAuthority("EMPLOYEE")

                        // Payroll payment status
                        .requestMatchers(HttpMethod.GET, "/api/payroll/payment-status/{payrollId}")
                        .hasAuthority("HR")

                        // My payroll
                        .requestMatchers(HttpMethod.GET, "/api/payroll/my-payroll")
                        .hasAnyAuthority("HR", "EMPLOYEE", "MANAGER")

                        // All other endpoints require authentication
                        .anyRequest().authenticated()
                );

        // Add JWT filter before username/password authentication.
        http.addFilterBefore(
                jwtFilter,
                UsernamePasswordAuthenticationFilter.class
        );

        // Authentication is handled by JWT.
        // Do not enable HTTP Basic for this stateless API.
        return http.build();
    }

    @Bean
    public AuthenticationProvider getProvider() {

        DaoAuthenticationProvider authenticationProvider =
                new DaoAuthenticationProvider(myUserDetailsService);

        authenticationProvider.setPasswordEncoder(getEncoder());

        return authenticationProvider;
    }

    @Bean
    public PasswordEncoder getEncoder() {
        return new BCryptPasswordEncoder();
    }
}