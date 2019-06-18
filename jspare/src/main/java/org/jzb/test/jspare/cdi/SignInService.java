package org.jzb.test.jspare.cdi;

import org.jspare.core.Component;

/**
 * @author jzb 2019-06-05
 */
@Component
public interface SignInService {
    boolean signIn(String username, String password);
}
