package com.kakawait.spring.boot.security.cas.autoconfigure;

import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.DeprecatedConfigurationProperty;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.springframework.boot.autoconfigure.security.SecurityProperties.BASIC_AUTH_ORDER;

/**
 * @author Thibaud Leprêtre
 */
@Data
@ConfigurationProperties(prefix = "security.cas")
public class CasSecurityProperties {

    public static final int CAS_AUTH_ORDER = BASIC_AUTH_ORDER - 1;

    private boolean enabled = true;

    private Authorization authorization = new Authorization();

    private User user = new User();

    private Server server = new Server();

    private Service service = new Service();

    /**
     * @see org.springframework.security.cas.authentication.CasAuthenticationProvider
     */
    private String key = UUID.randomUUID().toString();

    /**
     * Comma-separated list of paths to secure.
     */
    private String[] paths = new String[] { "/**" };

    /**
     * Security authorize mode to apply.
     */
    @Getter(onMethod_ = {@Deprecated,
            @DeprecatedConfigurationProperty(replacement = "security.cas.authorization.mode")})
    private SecurityAuthorizeMode authorizeMode = SecurityAuthorizeMode.AUTHENTICATED;

    private ProxyValidation proxyValidation = new ProxyValidation();

    public enum ServiceResolutionMode {
        STATIC, DYNAMIC
    }

    public enum SecurityAuthorizeMode {
        /**
         * Must be a member of one of the security roles.
         */
        ROLE,

        /**
         * Must be an authenticated user.
         */
        AUTHENTICATED,

        /**
         * No security authorization is setup.
         */
        NONE,

        /**
         * No authorization is setup by default, User is required to configure request authorization
         */
        CUSTOM,
    }

    @Data
    public static class Authorization {
        private SecurityAuthorizeMode mode = SecurityAuthorizeMode.AUTHENTICATED;

        private String[] roles = new String[] { "USER" };
    }

    @Data
    public static class User {
        private String[] rolesAttributes = new String[0];

        /**
         * Default roles are roles that will be automatically appends to other roles definitions
         * For example if you defined: {@code security.cas.user.roles = USER}
         * and {@code security.cas.user.defaultRoles = MEMBER}. At the end the user will have both {@code USER} and
         * {@code MEMBER} role.
         * <p>
         * Same thing if you're using {@link #rolesAttributes}, {@code defaultRoles} will be append to the list of
         * roles retrieve from {@code CAS Attributes}.
         */
        private String[] defaultRoles = new String[0];
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
        private int protocolVersion = 3;

        /**
         * CAS Server base url, example https://my-cas.server.com/
         */
        private URI baseUrl;

        /**
         * CAS Server validation base url, example https://my-cas.server.internal/
         *
         * If defined it will be used to compute complete <i>validation base url</i> (for ticket validation)
         * instead of using {@link Server#baseUrl}.
         *
         * <i>Validation url</i> request is executed by the <i>java CAS client</i> when intercepting a
         * <i>service ticket</i> or <i>proxy ticket</i>. Thus it can be useful to be different than
         * {@link Server#baseUrl} when CAS server can't share the same network as your browser (for example).
         *
         * For example when using containers (<i>Docker</i> or others) or VM, you can't use {@code localhost} hostname
         * in your <i>validation url</i> since your CAS service inside a container or VM doesn't have the same
         * {@code localhost} as you host machine.
         *
         * @see Server#baseUrl
         * @see Paths#validationBaseUrl
         * @see Service#callbackBaseUrl
         */
        private URI validationBaseUrl;

        private Paths paths = new Paths();

        @Data
        public static class Paths {
            /**
             * CAS Server login path that will be append to {@link Server#baseUrl}
             *
             * @see org.springframework.security.cas.web.CasAuthenticationEntryPoint
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

        /**
         * CAS Service callback base url, example https://my.service.com/
         *
         * If defined it will be used to compute complete <i>proxy callback url</i> instead of using
         * {@link Service#baseUrl}.
         * It will also be use even if {@link #baseUrl} is not defined and you're using {@link Service#resolutionMode}
         * is equals to {@link ServiceResolutionMode#DYNAMIC}.
         *
         * <i>Proxy callback</i> request is a fully new request executed from CAS server (using its own http client)
         * to your service. Thus it can be useful to be different than {@link Service#baseUrl} when CAS server
         * can't share the same network as your browser (for example).
         *
         * For example when using containers (<i>Docker</i> or others) or VM, you can't use {@code localhost} hostname
         * in your <i>proxy callback url</i> since CAS server inside a container or VM doesn't have the same
         * {@code localhost} as your host machine.
         *
         * @see Service#baseUrl
         * @see Paths#proxyCallback
         * @see Server#validationBaseUrl
         */
        private URI callbackBaseUrl;

        private Paths paths = new Paths();

        @Data
        public static class Paths {

            /**
             * CAS Service login path that will be append to {@link Service#baseUrl}
             */
            private String login = "/login";

            /**
             * CAS Service logout path that will be append to {@link Service#baseUrl}
             *
             * @see org.springframework.security.web.authentication.logout.LogoutFilter
             */
            private String logout = "/logout";

            /**
             * CAS Service proxy callback path that will be append to {@link Service#callbackBaseUrl} if defined else
             * fallback to {@link Service#baseUrl}
             *
             * @see Service#callbackBaseUrl
             * @see org.jasig.cas.client.validation.Cas20ServiceTicketValidator
             */
            private String proxyCallback;
        }

    }

    @Data
    public static class ProxyValidation {

        private boolean enabled = true;

        private List<List<String>> chains = new ArrayList<>();
    }

}
