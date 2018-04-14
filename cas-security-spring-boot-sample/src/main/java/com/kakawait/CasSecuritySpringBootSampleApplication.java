package com.kakawait;

import com.kakawait.spring.boot.security.cas.CasHttpSecurityConfigurer;
import com.kakawait.spring.boot.security.cas.CasSecurityConfigurerAdapter;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.authentication.AttributePrincipalImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.Http401AuthenticationEntryPoint;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.cas.authentication.CasAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.filter.ForwardedHeaderFilter;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.lang.reflect.Field;
import java.security.Principal;
import java.util.Optional;

/**
 * @author Thibaud Leprêtre
 */
@SpringBootApplication
@EnableGlobalMethodSecurity(securedEnabled = true)
public class CasSecuritySpringBootSampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(CasSecuritySpringBootSampleApplication.class, args);
    }

    @Bean
    FilterRegistrationBean forwardedHeaderFilter() {
        FilterRegistrationBean filterRegBean = new FilterRegistrationBean();
        filterRegBean.setFilter(new ForwardedHeaderFilter());
        filterRegBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return filterRegBean;
    }

    @Profile("!custom-logout")
    @Configuration
    static class LogoutConfiguration extends CasSecurityConfigurerAdapter {

        private final LogoutSuccessHandler casLogoutSuccessHandler;

        public LogoutConfiguration(LogoutSuccessHandler casLogoutSuccessHandler) {
            this.casLogoutSuccessHandler = casLogoutSuccessHandler;
        }

        @Override
        public void configure(HttpSecurity http) throws Exception {
            // Allow GET method to /logout even if CSRF is enabled
            http.logout()
                .logoutSuccessHandler(casLogoutSuccessHandler)
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"));
        }
    }

    @Configuration
    static class ApiSecurityConfiguration extends WebSecurityConfigurerAdapter {
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.antMatcher("/api/**").authorizeRequests().anyRequest().authenticated();
            // Applying CAS security on current HttpSecurity (FilterChain)
            // I'm not using .apply() from HttpSecurity due to following issue
            // https://github.com/spring-projects/spring-security/issues/4422
            CasHttpSecurityConfigurer.cas().configure(http);
            http.exceptionHandling().authenticationEntryPoint(new Http401AuthenticationEntryPoint("CAS"));
        }
    }

    @Profile("custom-logout")
    @Configuration
    static class CustomLogoutConfiguration extends CasSecurityConfigurerAdapter {

        private final LogoutSuccessHandler casLogoutSuccessHandler;

        public CustomLogoutConfiguration(LogoutSuccessHandler casLogoutSuccessHandler) {
            this.casLogoutSuccessHandler = casLogoutSuccessHandler;
        }

        @Override
        public void configure(HttpSecurity http) throws Exception {
            http.logout()
                .permitAll()
                // Add null logoutSuccessHandler to disable CasLogoutSuccessHandler
                .logoutSuccessHandler(null)
                .logoutSuccessUrl("/logout.html")
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"));
            LogoutFilter filter = new LogoutFilter(casLogoutSuccessHandler, new SecurityContextLogoutHandler());
            filter.setFilterProcessesUrl("/cas/logout");
            http.addFilterBefore(filter, LogoutFilter.class);
        }
    }

    @Profile("custom-logout")
    @Configuration
    static class WebMvcConfiguration extends WebMvcConfigurerAdapter {
        @Override
        public void addViewControllers(ViewControllerRegistry registry) {
            registry.addViewController("/logout.html").setViewName("logout");
            registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
        }
    }

    @Controller
    @RequestMapping(value = "/")
    static class IndexController {

        @RequestMapping
        public String hello(Authentication authentication, Model model) {
            if (authentication != null && StringUtils.hasText(authentication.getName())) {
                model.addAttribute("username", authentication.getName());
                model.addAttribute("principal", authentication.getPrincipal());
                model.addAttribute("pgt", getProxyGrantingTicket(authentication).orElse(null));
            }
            return "index";
        }

        @RequestMapping(path = "/ignored")
        public String ignored() {
            return "index";
        }

        @Secured("ROLE_ADMIN")
        @RequestMapping(path = "/admin")
        public @ResponseBody String roleUsingAnnotation() {
            return "You're admin";
        }

        /**
         * Hacky code please do not use that in production
         */
        private Optional<String> getProxyGrantingTicket(Authentication authentication) {
            if (!(authentication instanceof CasAuthenticationToken)) {
                return Optional.empty();
            }
            AttributePrincipal principal = ((CasAuthenticationToken) authentication).getAssertion().getPrincipal();
            if (!(principal instanceof AttributePrincipalImpl)) {
                return Optional.empty();
            }
            Field field = ReflectionUtils.findField(AttributePrincipalImpl.class, "proxyGrantingTicket");
            ReflectionUtils.makeAccessible(field);
            return Optional.ofNullable(ReflectionUtils.getField(field, principal)).map(Object::toString);
        }
    }

    @RestController
    @RequestMapping(value = "/api")
    static class HelloWorldController {

        @RequestMapping
        public @ResponseBody String hello(Principal principal) {
            return principal == null ? "Hello anonymous" : "Hello " + principal.getName();
        }
    }

}
