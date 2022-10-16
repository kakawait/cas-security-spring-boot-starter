package com.kakawait.spring.security.cas.client.validation;

import org.jasig.cas.client.validation.AssertionImpl;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.cas.authentication.CasAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Thibaud Lepretre
 */
public class SecurityContextHolderAssertionProviderTest {

    @Test
    public void getAssertion_NullAuthentication_IllegalStateException() {
        SecurityContextHolder.getContext().setAuthentication(null);

        AssertionProvider provider = new SecurityContextHolderAssertionProvider();
        assertThrows(IllegalStateException.class, provider::getAssertion);
    }

    @Test
    public void getAssertion_UnwantedAuthenticationType_IllegalStateException() {
        Authentication authentication = new UsernamePasswordAuthenticationToken("casuser", "Mellon");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        AssertionProvider provider = new SecurityContextHolderAssertionProvider();
        assertThrows(IllegalStateException.class, provider::getAssertion);
    }

    @Test
    public void getAssertion_ValidSecurityContextHolder_Assertion() {
        String user = "casuser";
        String ticket = "ST-21-c1gk6jBcfYnatLbNExfx-0623277bc36a";
        Set<SimpleGrantedAuthority> roles = Collections.singleton(new SimpleGrantedAuthority("MEMBER"));

        CasAuthenticationToken authentication = new CasAuthenticationToken(UUID.randomUUID().toString(), user,
                ticket, roles, new User(user, ticket, roles), new AssertionImpl(user));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        AssertionProvider provider = new SecurityContextHolderAssertionProvider();
        assertThat(provider.getAssertion()).isNotNull();
        assertThat(provider.getAssertion().getPrincipal().getName()).isEqualTo(user);
    }

}
