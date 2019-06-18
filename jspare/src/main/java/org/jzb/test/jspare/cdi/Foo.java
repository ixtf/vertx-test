package org.jzb.test.jspare.cdi;

import javax.annotation.Resource;

/**
 * @author jzb 2019-06-05
 */
@Resource
public class Foo {

    void bar() {
        System.out.println("something");
    }
}
