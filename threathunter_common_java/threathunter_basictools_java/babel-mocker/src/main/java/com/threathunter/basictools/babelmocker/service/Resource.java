package com.threathunter.basictools.babelmocker.service;

import com.threathunter.basictools.babelmocker.MockerServer;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Created by daisy on 17-11-8
 */
@Path("mock/client")
public class Resource {

    @GET
    @Path("{service}/{condition}")
    @Produces(MediaType.TEXT_PLAIN)
    public String showMsg(@PathParam("service") String service, @PathParam("condition") String condition) {
        try {
            MockerServer.getInstance().invokeClient(service, condition);
        } catch (Exception e) {
            return e.getMessage();
        }
        return "ok";
    }

    @GET
    @Path("{service}")
    @Produces(MediaType.TEXT_PLAIN)
    public String showMsg(@PathParam("service") String service) {
        try {
            MockerServer.getInstance().invokeClient(service, "");
        } catch (Exception e) {
            return e.getMessage();
        }
        return "ok";
    }
}
