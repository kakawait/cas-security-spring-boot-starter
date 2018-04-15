package com.kakawait.spring.security.cas.client.validation;

import org.jasig.cas.client.validation.Assertion;

/**
 * @author Jonathan Coueraud
 * @author Thibaud Leprêtre
 */
public interface AssertionProvider {

    Assertion getAssertion();
}
