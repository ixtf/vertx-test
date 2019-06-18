package org.jzb.test.jspare.cdi;

import org.jspare.vertx.JspareVerticle;

import javax.inject.Inject;

/**
* @author jzb 2019-06-05
*/
public class MyApp extends JspareVerticle {

   @Inject
   private Foo someResource;

   @Override
   public void start() {
       someResource.bar();
   }
}
