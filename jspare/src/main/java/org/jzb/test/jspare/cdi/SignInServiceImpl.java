package org.jzb.test.jspare.cdi;

/**
 * @author jzb 2019-06-05
 */
public class SignInServiceImpl implements SignInService {
    @Override
    public boolean signIn(String username, String password) {
        return "admin".equals(username) && "admin".equals(password);
    }
}
