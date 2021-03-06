package me.nizheg.telegram.bot.chgk.config;

import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;

import javax.sql.DataSource;

/**
 * @author Nikolay Zhegalin
 */
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final DataSource dataSource;

    public SecurityConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .cors()
                .and().csrf().disable()
                .authorizeRequests()
                .antMatchers(HttpMethod.OPTIONS).permitAll()
                .antMatchers(HttpMethod.POST, "/api/users").permitAll()
                .antMatchers(HttpMethod.POST, "/api/payment/yandex/result").permitAll()
                .antMatchers("/0d2f72c055554a3d83c31744f7f451e0").permitAll()
                .antMatchers("/app/admin/**/*.html").hasAuthority("manage_application")
                .antMatchers("/api/manage/**").hasAuthority("manage_application")
                .antMatchers("/app/task/import.html").hasAuthority("db_load")
                .antMatchers("api/dbTask/**").hasAuthority("db_load")
                .mvcMatchers(HttpMethod.PATCH, "/api/tour/**").hasAuthority("manage_tour_status")
                .antMatchers(
                        "/api/answer/**",
                        "/api/category/**",
                        "/api/picture/**",
                        "/api/task/**",
                        "/api/tour/**",
                        "/api/message/**").hasAuthority("manage_tasks")
                .anyRequest().authenticated()
                .and().formLogin()
                .and().httpBasic();
    }

    @Override
    public void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        JdbcUserDetailsManager userDetailsService = authenticationManagerBuilder
                .jdbcAuthentication()
                .dataSource(dataSource)
                .passwordEncoder(passwordEncoder())
                .getUserDetailsService();
        userDetailsService.setEnableGroups(true);
    }

    @Bean
    public JdbcUserDetailsManager userDetailsManager() {
        JdbcUserDetailsManager jdbcUserDetailsManager = new JdbcUserDetailsManager();
        jdbcUserDetailsManager.setDataSource(dataSource);
        return jdbcUserDetailsManager;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
