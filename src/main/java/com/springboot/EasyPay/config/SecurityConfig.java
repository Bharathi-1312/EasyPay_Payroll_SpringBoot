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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

    // I will define a bean of type UserDetailsService
    // You are telling Spring: I don't want to use your details service,
    // I want to define my own Service and my own credentials.
/*
    @Bean
    public UserDetailsService users() {
        UserDetails customer1 = User.builder()
                .username("customer1")
                .password("{noop}customer1")
                .authorities("CUSTOMER")

                .build();
        UserDetails executive1 = User.builder()
                .username("executive1")
                .password("{noop}executive1")
                .authorities("EXECUTIVE")
                .build();
        UserDetails manager1 = User.builder()
                .username("manager1")
                .password("{noop}manager1")
                .authorities("MANAGER")
                .build();
        return new InMemoryUserDetailsManager(customer1, executive1, manager1);

    }

 */
    private final JwtFilter jwtFilter;
    private final MyUserDetailsService myUserDetailsService ;
    @Bean
    public SecurityFilterChain defaultFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                                // Admin onboard API (permitAll to add) (After addition - denyAll)
                                .requestMatchers(HttpMethod.POST,"/api/admin/add").permitAll()

                                // login API
                                .requestMatchers(HttpMethod.GET,"/api/auth/login").authenticated()

                                //Admin onboard manager
                                .requestMatchers(HttpMethod.POST,"/api/manager/add").hasAuthority("ADMIN")
                                //Admin onboard HR
                                .requestMatchers(HttpMethod.POST,"/api/hr/add").hasAuthority("ADMIN")
                                //HR onboard employee
                                .requestMatchers(HttpMethod.POST,"/api/employee/add").hasAuthority("HR")

                                //Employee can apply leave
                                .requestMatchers(HttpMethod.POST, "/api/leavereq/apply").hasAuthority("EMPLOYEE")


                                //Manager Can review the leave
                                .requestMatchers(HttpMethod.PUT,"/api/reviewleave/{leaveId}").hasAuthority("MANAGER")

                                //Employee details
                                .requestMatchers(HttpMethod.GET,"/api/employee/{id}").permitAll()

                                //Manager details
                                .requestMatchers(HttpMethod.GET,"/api/manager/{id}").hasAnyAuthority("MANAGER")

                                //HR details
                                .requestMatchers(HttpMethod.GET,"/api/hr/{id}").hasAnyAuthority("HR")

                                //Update manager details
                                .requestMatchers(HttpMethod.PUT,"/api/manager").hasAuthority("MANAGER")

                                //Update hr details
                                .requestMatchers(HttpMethod.PUT,"/api/hr").hasAuthority("HR")

                                // HR assigning the manager to employee
                                .requestMatchers(HttpMethod.PUT,"/api/employee/assign/{managerId}/{employeeId}").hasAuthority("HR")

                                //Create payroll by HR
                                .requestMatchers(HttpMethod.POST,"/api/payroll/{employeeId}").hasAuthority("HR")

                                //Process payroll by HR
                                .requestMatchers(HttpMethod.POST,"/api/payroll/process/{payrollId}").hasAuthority("HR")

                                //Markas paid  by HR
                                .requestMatchers(HttpMethod.PUT,"/api/payroll/pay/{payrollId}").hasAuthority("HR")

                                //Getting  Payslip by HR
                                .requestMatchers(HttpMethod.GET,"/api/payroll/payslip/{payrollId}").hasAuthority("HR")

//                        //Getting  Payslip by Employee
                                .requestMatchers(HttpMethod.GET,"/api/payroll/payslip/{payrollId}").hasAuthority("EMPLOYEE")

                                //Getting  Payslip with empId by employee
                                .requestMatchers(HttpMethod.GET,"/api/payroll/employee/{employeeId}").hasAnyAuthority("EMPLOYEE","HR")

                                //Adding PayrollComponents
                                .requestMatchers(HttpMethod.POST,"/api/payroll-components/{payrollId}").hasAuthority("HR")

                                //Getting  components by payroll_id by hr
                                .requestMatchers(HttpMethod.GET,"/api/payroll-components/payroll/{payrollId}").hasAnyAuthority("HR","EMPLOYEE")


                                .requestMatchers(HttpMethod.GET,"/api/leavereq/manager").hasAuthority("MANAGER")
                                .requestMatchers(HttpMethod.GET,"/api/leavereq/employee").hasAuthority("EMPLOYEE")



                                //Getting  components by payroll_id by emp
                                .requestMatchers(HttpMethod.GET,"/api/payroll-components/payroll/{payrollId}").hasAuthority("EMPLOYEE")

                                //Getting  payslip with emplId by HR
                                .requestMatchers(HttpMethod.DELETE,"/api/payroll-components/{componentId}").hasAuthority("HR")

                                .requestMatchers(HttpMethod.PATCH, "/components/**").hasAuthority("HR")
                                .requestMatchers(HttpMethod.PUT, "/api/employee/{id}").hasAnyAuthority("HR", "EMPLOYEE")


                                //Getting  PayrollStatus by HR
                                .requestMatchers(HttpMethod.GET,"/api/payroll/status/{status}").hasAuthority("HR")
                                .requestMatchers(HttpMethod.GET, "/api/auth/user-details").authenticated()
                                //Getting  Payroll By date rang ( by HR)
                                .requestMatchers(HttpMethod.GET,"/api/payroll/period").hasAuthority("HR")
                                //Search by employee and date range
                                .requestMatchers(HttpMethod.GET,"/api/payroll/search").hasAuthority("HR")
                                //Earning history
                                .requestMatchers(HttpMethod.GET,"/api/employee/earnings/{id}").hasAuthority("EMPLOYEE")
                                //Deduction history
                                .requestMatchers(HttpMethod.GET,"/api/employee/deductions/{id}").hasAuthority("EMPLOYEE")
                                //Monthly Report By HR
                                .requestMatchers(HttpMethod.GET,"/api/payroll/reports/monthly").hasAuthority("HR")
                                //Payslip with payrollId By Employee
                                .requestMatchers(HttpMethod.GET,"/api/payroll/payslip-text/{payrollId}").hasAuthority("EMPLOYEE")
                                //payroll Status by HR
                                .requestMatchers(HttpMethod.GET,"/api/payroll/payment-status/{payrollId}").hasAuthority("HR")
                                //My Payroll
                                .requestMatchers(HttpMethod.GET,"/api/payroll/my-payroll").hasAnyAuthority("HR","EMPLOYEE","MANAGER")




                                .anyRequest().permitAll()
                );
        // Add jwt filter before Spring's Auth Filter
        http.addFilterBefore(jwtFilter , UsernamePasswordAuthenticationFilter.class);
        http.httpBasic(Customizer.withDefaults());
        return http.build();
    }
    @Bean
    public AuthenticationProvider getProvider(){
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(myUserDetailsService);
        authenticationProvider.setPasswordEncoder(getEncoder());
        return authenticationProvider;
    }

    @Bean
    public PasswordEncoder getEncoder(){
        return new BCryptPasswordEncoder();
    }
}
