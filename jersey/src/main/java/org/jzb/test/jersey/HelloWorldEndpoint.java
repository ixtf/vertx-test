package org.jzb.test.jersey;

import javax.ws.rs.GET;

/** @author jzb 2019-06-05 */
public class HelloWorldEndpoint {
  @GET
  public String doGet() {
    return "Hello World!";
  }
}
