package com.kakawait;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.filter.ForwardedHeaderFilter;

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

    @RestController
    @RequestMapping(value = "/")
    static class HelloWorldController {

        @GetMapping
        public @ResponseBody String hello(Principal principal) {
            return principal == null ? "Hello anonymous" : "Hello " + principal.getName();
        }

        @GetMapping(path = "/ignored")
        public @ResponseBody String ignored() {
            return "Hello world!";
        }
    }

}
