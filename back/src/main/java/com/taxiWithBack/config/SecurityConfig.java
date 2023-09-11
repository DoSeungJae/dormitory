package com.taxiWithBack.config;
import com.taxiWithBack.domain.member.service.UserDetailService;
import com.taxiWithBack.jwt.JwtSecurityConfig;
import com.taxiWithBack.jwt.TokenProvider;
import com.taxiWithBack.jwt.exception.JwtAccessDeniedHandler;
import com.taxiWithBack.jwt.exception.JwtAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    private final TokenProvider tokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    @Autowired
    private final UserDetailService userDetailService;


    public SecurityConfig(
            TokenProvider tokenProvider,
            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
            JwtAccessDeniedHandler jwtAccessDeniedHandler,
            UserDetailService userDetailService){

        this.tokenProvider=tokenProvider;
        this.jwtAuthenticationEntryPoint=jwtAuthenticationEntryPoint;
        this.jwtAccessDeniedHandler=jwtAccessDeniedHandler;
        this.userDetailService=userDetailService;
    }

    @Bean
    public PasswordEncoder passwordEncoder(){

        return new BCryptPasswordEncoder();
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .exceptionHandling()
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDeniedHandler)

                //h2-console 사용
                .and()
                .headers()
                .frameOptions()
                .sameOrigin()

                //세션을 사용하지 않으므로 stateless로 설정
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                .and()
                .authorizeHttpRequests()
                .requestMatchers("api/v1/user/**")
                .permitAll()
                .anyRequest()
                .authenticated()

                .and()
                .apply(new JwtSecurityConfig(tokenProvider));

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(){
        DaoAuthenticationProvider daoAuthenticationProvider =  new DaoAuthenticationProvider();
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        daoAuthenticationProvider.setUserDetailsService(userDetailService);

        return daoAuthenticationProvider;
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer(){

    }


}

