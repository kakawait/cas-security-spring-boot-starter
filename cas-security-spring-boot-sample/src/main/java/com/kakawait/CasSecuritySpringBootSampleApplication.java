package com.kakawait;

import com.kakawait.spring.boot.security.cas.CasHttpSecurityConfigurer;
import com.kakawait.spring.boot.security.cas.CasSecurityConfigurerAdapter;
import com.kakawait.spring.boot.security.cas.CasSecurityProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.Http401AuthenticationEntryPoint;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.filter.ForwardedHeaderFilter;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.Principal;

/**
 * @author Thibaud Leprêtre
 */
@SpringBootApplication
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
        @Override
        public void configure(HttpSecurity http) throws Exception {
            // Allow GET method to /logout even if CSRF is enabled
            http.logout().logoutRequestMatcher(new AntPathRequestMatcher("/logout"));
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
        private final CasSecurityProperties casSecurityProperties;

        public CustomLogoutConfiguration(CasSecurityProperties casSecurityProperties) {
            this.casSecurityProperties = casSecurityProperties;
        }

        @Override
        public void configure(HttpSecurity http) throws Exception {
            http.logout()
                .permitAll()
                .logoutSuccessUrl("/logout.html")
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"));
            String logoutUrl = UriComponentsBuilder
                    .fromUri(casSecurityProperties.getServer().getBaseUrl())
                    .path(casSecurityProperties.getServer().getPaths().getLogout())
                    .toUriString();
            LogoutFilter filter = new LogoutFilter(logoutUrl, new SecurityContextLogoutHandler());
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
        public String hello(Principal principal, Model model) {
            if (StringUtils.hasText(principal.getName())) {
                model.addAttribute("username", principal.getName());
            }
            return "index";
        }

        @RequestMapping(path = "/ignored")
        public String ignored() {
            return "index";
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
