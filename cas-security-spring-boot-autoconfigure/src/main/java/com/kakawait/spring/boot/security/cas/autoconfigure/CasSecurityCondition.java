package com.kakawait.spring.boot.security.cas.autoconfigure;

import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

/**
 * @author Thibaud Leprêtre
 */
@SuppressWarnings("unused")
public class CasSecurityCondition extends AllNestedConditions {

    public CasSecurityCondition() {
        super(ConfigurationPhase.REGISTER_BEAN);
    }

    @ConditionalOnProperty(value = "security.cas.enabled", havingValue = "true", matchIfMissing = true)
    static class EnabledProperty {
    }

    @ConditionalOnProperty(value = "security.cas.server.base-url")
    static class ServerInstanceProperty {
    }
}
