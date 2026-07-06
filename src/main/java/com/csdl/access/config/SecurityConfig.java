package com.csdl.access.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Cau hinh bao mat. Xac thuc thuc te qua AD (AuthService); Spring Security
 * giu SecurityContext de phan quyen theo active role.
 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    /** Khai bao chuoi filter bao mat: phan quyen URL, CSRF, logout va trang tu choi truy cap. */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeRequests(auth -> auth
                // Tai nguyen cong khai: dang nhap, captcha, tai nguyen tinh, h2-console
                .antMatchers("/login", "/session/role", "/captcha", "/css/**", "/js/**", "/img/**", "/webjars/**", "/favicon.ico", "/h2-console/**").permitAll()
                // Khu vuc quan tri chi danh cho vai tro ADMIN
                .antMatchers("/config/**", "/admin/**").hasRole("ADMIN")
                // Cac request con lai deu can dang nhap
                .anyRequest().authenticated()
            )
            // Bo qua CSRF cho h2-console (dung khi phat trien)
            .csrf(csrf -> csrf.ignoringRequestMatchers(new AntPathRequestMatcher("/h2-console/**")))
            // Cho phep hien h2-console trong iframe cung nguon
            .headers(headers -> headers.frameOptions().sameOrigin())
            // Tat form login mac dinh cua Spring (dang nhap qua AuthService)
            .formLogin(form -> form.disable())
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
            )
            // Khi bi tu choi truy cap thi ve trang dang nhap voi thong bao denied
            .exceptionHandling(ex -> ex.accessDeniedPage("/login?denied"));

        return http.build();
    }
}
