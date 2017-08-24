package com.kakawait;

import org.jasig.cas.client.authentication.AttributePrincipal;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.cas.authentication.CasAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/**
 * @author Thibaud Leprêtre
 */
@SpringBootApplication
public class CasSecuritySpringBootSampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(CasSecuritySpringBootSampleApplication.class, args);
    }

    @Controller
    @RequestMapping(value = "/")
    static class IndexController {

        @RequestMapping
        public String hello(Principal principal, Model model) {
            if (StringUtils.hasText(principal.getName())) {
                model.addAttribute("principal", principal);

                model.addAttribute("username", principal.getName());

                CasAuthenticationToken authentication = ((CasAuthenticationToken) SecurityContextHolder.getContext().getAuthentication());
                AttributePrincipal principalFromContext = authentication.getAssertion().getPrincipal();
                model.addAttribute("pgt", principalFromContext.getProxyTicketFor("https://localhost:30000"));
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

        @GetMapping
        public @ResponseBody String hello(Principal principal) {
            return principal == null ? "Hello anonymous" : "Hello " + principal.getName();
        }
    }

}
