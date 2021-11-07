package com.baczan.session_authorization_server.config;

import com.baczan.session_authorization_server.entryPoints.ApiAccessDeniedHandler;
import com.baczan.session_authorization_server.entryPoints.ApiEntryPoint;
import com.baczan.session_authorization_server.handlers.FormLoginSuccessHandler;
import com.baczan.session_authorization_server.handlers.Oauth2LoginSuccessHandler;
import com.baczan.session_authorization_server.helpers.Tier;
import com.baczan.session_authorization_server.service.TierService;
import com.baczan.session_authorization_server.service.UserDetailsServiceImplementation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
@EnableWebMvc
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig implements WebMvcConfigurer {

    private static final String[] CLASSPATH_RESOURCE_LOCATIONS = {
            "classpath:/public/"
    };

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        if (!registry.hasMappingForPattern("/**")) {
            registry.addResourceHandler("/**").addResourceLocations(CLASSPATH_RESOURCE_LOCATIONS);
        }
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedMethods("HEAD", "GET", "PUT", "POST", "DELETE", "PATCH")
                .allowedOriginPatterns("http://localhost:4200")
                .allowCredentials(true);
    }

    @Autowired
    private Environment env;

    @Autowired
    private FormLoginSuccessHandler formLoginSuccessHandler;

    @Autowired
    private Oauth2LoginSuccessHandler oauth2LoginSuccessHandler;


    private static final String[] UNAUTHORIZED_URLS = {
            "/files/**",
            "/register",
            "/activate",
            "/password_change_request",
            "/password_change",
            "/logout",
            "/login",
            "/afterLogin",
            "/api/stripe/events",
            "/api/stripe/success",
            "/testLogin"
    };

    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {

        http.authorizeRequests(
                authorizeRequests ->
                        authorizeRequests
                                .antMatchers(UNAUTHORIZED_URLS)
                                .permitAll()
                                .anyRequest()
                                .authenticated())
                .formLogin()
                .loginPage("/login")
                .successHandler(formLoginSuccessHandler)
                .and()
                .oauth2Login()
                .loginPage("/login")
                .successHandler(oauth2LoginSuccessHandler)
                .failureUrl("/login?errorOAuth2")
                .and()
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .addLogoutHandler(new SecurityContextLogoutHandler())
                )
                .exceptionHandling()
                .defaultAuthenticationEntryPointFor(new ApiEntryPoint(), new AntPathRequestMatcher("/api/**"))
                .defaultAccessDeniedHandlerFor(new ApiAccessDeniedHandler(), new AntPathRequestMatcher("/api/**"));

        http.cors();
        http.csrf().ignoringAntMatchers("/api/stripe/events");

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new UserDetailsServiceImplementation();
    }

    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        return new InMemoryClientRegistrationRepository(facebookClient(), googleClient());
    }

    private ClientRegistration facebookClient() {
        return CommonOAuth2Provider.FACEBOOK
                .getBuilder("facebook")
                .clientId(env.getProperty("app.facebook.client.id"))
                .clientSecret(env.getProperty("app.facebook.client.secret"))
                .redirectUri(String.format("%s/login/oauth2/code/facebook", env.getProperty("app.url")))
                .build();
    }

    private ClientRegistration googleClient() {
        return CommonOAuth2Provider.GOOGLE
                .getBuilder("google")
                .clientId(env.getProperty("app.google.client.id"))
                .clientSecret(env.getProperty("app.google.client.secret"))
                .redirectUri(String.format("%s/login/oauth2/code/google", env.getProperty("app.url")))
                .build();
    }



    @Bean
    public TierService tierService(){
        TierService tierService = new TierService();
        tierService.add(new Tier("free","", 5000000000L));
        tierService.add(new Tier("basic","price_1JbwP5BZFHL9NjRl6w86kL8x", 15000000000L));
        tierService.add(new Tier("standard","price_1JbwPpBZFHL9NjRlqri7q65A", 50000000000L));
        tierService.add(new Tier("premium","price_1JbwQhBZFHL9NjRlVNfoG7QH", 100000000000L));
        return tierService;
    }

}
