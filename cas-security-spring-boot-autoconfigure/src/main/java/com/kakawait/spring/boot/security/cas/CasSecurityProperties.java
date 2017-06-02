package com.kakawait.spring.boot.security.cas;

import lombok.Data;
import org.springframework.boot.autoconfigure.security.SecurityAuthorizeMode;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;
import java.util.UUID;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * @author Thibaud Leprêtre
 */
@Data
@ConfigurationProperties(prefix = "security.cas")
public class CasSecurityProperties {

    private boolean enabled = true;

    private User user = new User();

    private Server server = new Server();

    private Service service = new Service();

    /**
     * @see org.springframework.security.cas.authentication.CasAuthenticationProvider#key
     */
    private String key = UUID.randomUUID().toString();

    /**
     * Comma-separated list of paths to secure.
     */
    private String[] paths = new String[] { "/**" };

    /**
     * Security authorize mode to apply.
     */
    private SecurityAuthorizeMode authorizeMode = SecurityAuthorizeMode.ROLE;

    @Data
    public static class User {
        private String[] rolesAttributes = new String[0];

        private String[] defaultRoles = new String[] { "USER" };
    }

    @Data
    public static class Server {

        /**
         * CAS Server protocol version used to define which {@link org.jasig.cas.client.validation.TicketValidator} to
         * use.
         *
         * By default {@code ProxyTicketValidator} is selected rather than {@code ServiceTicketValidator}.
         *
         * @see org.jasig.cas.client.validation.Cas30ProxyTicketValidator
         * @see org.jasig.cas.client.validation.Cas20ProxyTicketValidator
         * @see org.jasig.cas.client.validation.Cas10TicketValidationFilter
         */
        @Min(1)
        @Max(3)
        private int protocolVersion = 3;

        /**
         * CAS Server base url, example https://my-cas.server.com/
         */
        private URI baseUrl;

        private Paths paths = new Paths();

        @Data
        static class Paths {
            /**
             * CAS Server login path that will be append to {@link Server#baseUrl}
             *
             * @see org.springframework.security.cas.web.CasAuthenticationEntryPoint#loginUrl
             */
            private String login = "/login";

            /**
             * CAS Server logout path that will be append to {@link Server#baseUrl}
             */
            private String logout = "/logout";
        }
    }

    @Data
    public static class Service {

        private ServiceResolutionMode resolutionMode = ServiceResolutionMode.STATIC;

        /**
         * CAS Service base url (your application base url)
         */
        private URI baseUrl;

        private Paths paths = new Paths();

        @Data
        static class Paths {

            /**
             * CAS Service login path that will be append to {@link Service#baseUrl}
             */
            private String login = "/login";

            /**
             * CAS Service logout path that will be append to {@link Service#baseUrl}
             *
             * @see org.springframework.security.web.authentication.logout.LogoutFilter#logoutRequestMatcher
             */
            private String logout = "/logout";

            /**
             * CAS Service proxy callback path that will be append to {@link Service#baseUrl}
             *
             * @see org.jasig.cas.client.validation.Cas20ServiceTicketValidator#proxyCallbackUrl
             */
            private String proxyCallback = "/cas/proxy-callback";
        }

    }

    public enum ServiceResolutionMode {
        STATIC,
        DYNAMIC
    }
}
