package com.kakawait.spring.boot.security.cas;

import org.jasig.cas.client.validation.Assertion;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.security.cas.userdetails.AbstractCasAssertionUserDetailsService;
import org.springframework.security.cas.userdetails.GrantedAuthorityFromAssertionAttributesUserDetailsService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Thibaud Leprêtre
 */
@ConditionalOnMissingBean(AbstractCasAssertionUserDetailsService.class)
class CasAssertionUserDetailsServiceConfiguration {

    private static final String ROLES_ATTRS_PROPERTY = "security.cas.user.roles-attributes";

    @Bean
    @Conditional(RolesAttributesAbsentCondition.class)
    AbstractCasAssertionUserDetailsService defaultRolesAuthenticationUserDetailsService(
            SecurityProperties securityProperties) {
        Set<SimpleGrantedAuthority> authorities = securityProperties.getUser().getRole()
                .stream()
                .map(r -> r.replaceFirst("^ROLE_", ""))
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                .collect(Collectors.toSet());
        return new DefaultRolesCasAssertionUserDetailsService(authorities);
    }

    @Bean
    @Conditional(RolesAttributesPresentCondition.class)
    AbstractCasAssertionUserDetailsService grantedAuthorityFromAssertionAttributesUserDetailsService(
            CasSecurityProperties casSecurityProperties) {
        String[] userRolesAttributes = casSecurityProperties.getUser().getRolesAttributes();
        return new GrantedAuthorityFromAssertionAttributesUserDetailsService(userRolesAttributes);
    }

    private static class RolesAttributesPresentCondition extends SpringBootCondition {
        @Override
        public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
            String property = context.getEnvironment().getProperty(ROLES_ATTRS_PROPERTY);
            return new ConditionOutcome(property != null && property.length() > 0,
                    "Property (" + ROLES_ATTRS_PROPERTY + ") found '"
                            + ROLES_ATTRS_PROPERTY.substring(ROLES_ATTRS_PROPERTY.lastIndexOf('.') + 1)
                            + "' (" + RolesAttributesPresentCondition.class.getSimpleName() + ")");
        }
    }

    private static class RolesAttributesAbsentCondition extends SpringBootCondition {
        @Override
        public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
            String property = context.getEnvironment().getProperty(ROLES_ATTRS_PROPERTY);
            return new ConditionOutcome(property == null || property.length() == 0,
                    "Property (" + ROLES_ATTRS_PROPERTY + ") did not find '"
                            + ROLES_ATTRS_PROPERTY.substring(ROLES_ATTRS_PROPERTY.lastIndexOf('.') + 1)
                            + "' (" + RolesAttributesAbsentCondition.class.getSimpleName() + ")");
        }
    }

    private static class DefaultRolesCasAssertionUserDetailsService extends AbstractCasAssertionUserDetailsService {

        private static final String NON_EXISTENT_PASSWORD_VALUE = "NO_PASSWORD";

        private final Collection<? extends GrantedAuthority> defaultGrantedAuthorities;

        DefaultRolesCasAssertionUserDetailsService(
                Collection<? extends GrantedAuthority> defaultGrantedAuthorities) {
            this.defaultGrantedAuthorities = defaultGrantedAuthorities;
        }

        protected UserDetails loadUserDetails(Assertion assertion) {
            String username = assertion.getPrincipal().getName();
            if (!StringUtils.hasText(username)) {
                throw new UsernameNotFoundException("Unable to retrieve username from CAS assertion");
            }

            return new User(username, NON_EXISTENT_PASSWORD_VALUE, defaultGrantedAuthorities);
        }
    }
}
