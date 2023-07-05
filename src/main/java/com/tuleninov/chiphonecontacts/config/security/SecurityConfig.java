package com.tuleninov.chiphonecontacts.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuleninov.chiphonecontacts.Routes;
import com.tuleninov.chiphonecontacts.config.security.filters.JWTAuthenticationFilter;
import com.tuleninov.chiphonecontacts.config.security.filters.JWTAuthorizationFilter;
import com.tuleninov.chiphonecontacts.config.security.properties.CustomSecurityProperties;
import com.tuleninov.chiphonecontacts.model.user.request.SaveUserRequest;
import com.tuleninov.chiphonecontacts.service.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides a convenient base class for creating a WebSecurityConfigurer instance.
 * The implementation allows customization by overriding methods.
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(CustomSecurityProperties.class)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    private final CustomSecurityProperties securityProperties;

    private final UserService userService;

    private final PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper;

    public SecurityConfig(CustomSecurityProperties securityProperties,
                          UserService userService,
                          PasswordEncoder passwordEncoder,
                          ObjectMapper objectMapper) {
        this.securityProperties = securityProperties;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
    }

    /**
     * Create default admins.
     * */
    @PostConstruct
    public void init() {
        setupDefaultAdmins();
    }

    /**
     * Checks that such admin have not yet been created. If they are created, checks
     * that does not have duplicate nickname and updates the password, adds authority, and save in the database.
     * */
    private void setupDefaultAdmins() {
        List<SaveUserRequest> requests = securityProperties.getAdmins().entrySet().stream()
                .map(entry -> new SaveUserRequest(
                        entry.getValue().getEmail(),
                        new String(entry.getValue().getPassword()),
                        entry.getKey()))
                .peek(admin -> log.info("Default admin found: {} <{}>", admin.nickname(), admin.email()))
                .collect(Collectors.toList());
        userService.mergeAdmins(requests);
    }

    /**
     * Overriding the userDetailsService method. Configure AuthenticationManagerBuilder,
     * add userService and passwordEncoder to it.
     * */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService).passwordEncoder(passwordEncoder);
    }

    /**
     * Override this method to configure HttpSecurity.
     * Specify any endpoint that needs protection from common vulnerabilities, including public ones.
     * */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                // open static resources
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                // open swagger-ui
                .antMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                // allow user registration and refresh, ignore authorization filters on login
                .antMatchers(HttpMethod.POST, Routes.USERS, Routes.TOKEN + "/refresh").permitAll()
                // user can get activation code from DB for activation
                .antMatchers(HttpMethod.GET, Routes.USERS + "/{code}/activate").permitAll()
                // user can get new password from server
                .antMatchers(HttpMethod.PATCH, Routes.USERS + "/forgot").permitAll()
                //
                .antMatchers(HttpMethod.GET, Routes.PHONES, Routes.PHONES + "/{id:\\d+}/phones", Routes.PHONES + "/{id:\\d+}").permitAll()
                // admin can register new admins
                .antMatchers(HttpMethod.POST, Routes.USERS + "/admins").hasRole("ADMIN")
                // regular users can view basic user info for other users
                .antMatchers(HttpMethod.GET, Routes.USERS + "/{id:\\d+}").authenticated()
                // admin can manage users by id
                .antMatchers(Routes.USERS + "/{id:\\d+}/**").hasRole("ADMIN")
                // admin can use Actuator endpoints
                .requestMatchers(EndpointRequest.toAnyEndpoint()).hasRole("ADMIN")
                // by default, require authentication
                .anyRequest().authenticated()
                .and()
                // auth filter
                .addFilter(jwtAuthenticationFilter())
                // jwt-verification filter
                .addFilter(jwtAuthorizationFilter())
                // for unauthorized requests return 401
                .exceptionHandling().authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                .and()
                // allow cross-origin requests for all endpoints
                .cors().configurationSource(corsConfigurationSource())
                .and()
                .csrf().disable() // вимикаємо кроссорсреквестфорджері - тому, що він вимагає наявності кукі, а у нас стєйтлесс додаток
                // this disables session creation on Spring Security
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    /**
     * Authentication filter.
     * */
    private JWTAuthenticationFilter jwtAuthenticationFilter() throws Exception {
        var filter = new JWTAuthenticationFilter(authenticationManager(), objectMapper);
        filter.setFilterProcessesUrl(Routes.TOKEN); // для запиту на Routes.TOKEN буде використовуватися цей фільтр
        return filter;
    }

    /**
     * Authorization filter.
     * */
    private JWTAuthorizationFilter jwtAuthorizationFilter() throws Exception {
        return new JWTAuthorizationFilter(authenticationManager(), securityProperties.getJwt());
    }

    /**
     * Allow cross-origin requests for all endpoints.
     * */
    private CorsConfigurationSource corsConfigurationSource() {
        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", new CorsConfiguration().applyPermitDefaultValues());
        return source;
    }
}
