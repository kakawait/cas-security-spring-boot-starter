package com.kakawait.spring.security.cas;

import org.springframework.security.cas.ServiceProperties;
import org.springframework.util.Assert;

/**
 * @author Thibaud Leprêtre
 */
public class LaxServiceProperties extends ServiceProperties {

    private final boolean dynamicServiceResolution;

    public LaxServiceProperties() {
        this(true);
    }

    public LaxServiceProperties(boolean dynamicServiceResolution) {
        this.dynamicServiceResolution = dynamicServiceResolution;
    }

    @Override
    public void afterPropertiesSet() {
        if (!dynamicServiceResolution) {
            try {
                super.afterPropertiesSet();
            } catch (Exception e) {
                // Old version of spring security throw Exception for afterPropertiesSet()
                throw new RuntimeException(e);
            }
        } else {
            Assert.hasLength(getArtifactParameter(), "artifactParameter cannot be empty.");
            Assert.hasLength(getServiceParameter(), "serviceParameter cannot be empty.");
        }
    }

    public boolean isDynamicServiceResolution() {
        return dynamicServiceResolution;
    }
}
