package com.kakawait.spring.boot.security.cas.autoconfigure;

import org.jasig.cas.client.session.SessionMappingStorage;
import org.jasig.cas.client.session.SingleSignOutFilter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Thibaud Lepretre
 */
public class CasSingleSignOutFilterConfigurerTest {

    @Test
    public void configure_WithAnyParameters_InjectInsideSingleSignOutHandler() {
        SessionMappingStorage sessionMappingStorage = Mockito.mock(SessionMappingStorage.class);

        SingleSignOutFilter filter = new SingleSignOutFilter();

        CasSingleSignOutFilterConfigurer configurer = new CasSingleSignOutFilterConfigurer();
        configurer.artifactParameterName("dummyArtifactParameterName")
                  .logoutParameterName("dummyLogoutParameterName")
                  .relayStateParameterName("dummyRelayStateParameterName")
                  .sessionMappingStorage(sessionMappingStorage)
                  .configure(filter);

        assertThat(ReflectionTestUtils.getField(filter, "HANDLER"))
                .hasFieldOrPropertyWithValue("artifactParameterName", "dummyArtifactParameterName")
                .hasFieldOrPropertyWithValue("logoutParameterName", "dummyLogoutParameterName")
                .hasFieldOrPropertyWithValue("relayStateParameterName", "dummyRelayStateParameterName")
                .hasFieldOrPropertyWithValue("sessionMappingStorage", sessionMappingStorage);
    }

}
