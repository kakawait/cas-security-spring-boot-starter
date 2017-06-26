package com.kakawait.spring.boot.security.cas;

import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jasig.cas.client.session.SessionMappingStorage;
import org.jasig.cas.client.session.SingleSignOutFilter;
import org.springframework.util.StringUtils;

/**
 * @author Thibaud Leprêtre
 */
@Setter
@Accessors(fluent = true)
public class CasSingleSignOutFilterConfigurer {

    @NonNull
    private SessionMappingStorage sessionMappingStorage;

    @NonNull
    private String relayStateParameterName;

    @NonNull
    private String frontLogoutParameterName;

    @NonNull
    private String logoutParameterName;

    @NonNull
    private String artifactParameterName;

    void configure(SingleSignOutFilter filter) {
        if (sessionMappingStorage != null) {
            filter.setSessionMappingStorage(sessionMappingStorage);
        }

        if (StringUtils.hasText(relayStateParameterName)) {
            filter.setRelayStateParameterName(relayStateParameterName);
        }

        if (StringUtils.hasText(frontLogoutParameterName)) {
            filter.setFrontLogoutParameterName(frontLogoutParameterName);
        }

        if (StringUtils.hasText(logoutParameterName)) {
            filter.setLogoutParameterName(logoutParameterName);
        }

        if (StringUtils.hasText(artifactParameterName)) {
            filter.setArtifactParameterName(artifactParameterName);
        }
    }
}
