package com.kakawait.security.cas;

import org.springframework.security.cas.ServiceProperties;
import org.springframework.util.Assert;

/**
 * @author Thibaud Leprêtre
 */
public class LaxServiceProperties extends ServiceProperties {

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.hasLength(getArtifactParameter(), "artifactParameter cannot be empty.");
        Assert.hasLength(getServiceParameter(), "serviceParameter cannot be empty.");
    }
}
