package com.kakawait.spring.boot.security.cas;

import org.jasig.cas.client.session.SessionMappingStorage;
import org.jasig.cas.client.session.SingleSignOutFilter;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Comparator;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Thibaud Leprêtre
 */
public class CasSingleSignOutFilterConfigurerTest {

    @Test
    public void configure_WithAnyParameters_InjectInsideSingleSignOutHandler() {
        SessionMappingStorage sessionMappingStorage = Mockito.mock(SessionMappingStorage.class);

        SingleSignOutFilter filter = new SingleSignOutFilter();

        CasSingleSignOutFilterConfigurer configurer = new CasSingleSignOutFilterConfigurer();
        configurer.artifactParameterName("dummyArtifactParameterName")
                  .frontLogoutParameterName("dummyFrontLogoutParameterName")
                  .logoutParameterName("dummyLogoutParameterName")
                  .relayStateParameterName("dummyRelayStateParameterName")
                  .sessionMappingStorage(sessionMappingStorage)
                  .configure(filter);

        assertThat(ReflectionTestUtils.getField(filter, "HANDLER"))
                .hasFieldOrPropertyWithValue("artifactParameterName", "dummyArtifactParameterName")
                .hasFieldOrPropertyWithValue("frontLogoutParameterName", "dummyFrontLogoutParameterName")
                .hasFieldOrPropertyWithValue("logoutParameterName", "dummyLogoutParameterName")
                .hasFieldOrPropertyWithValue("relayStateParameterName", "dummyRelayStateParameterName")
                .extracting("sessionMappingStorage")
                .usingElementComparator((Comparator<Object>) (o1, o2) -> (o1 == o2) ? 0 : -1)
                .containsOnly(sessionMappingStorage);
    }

}
