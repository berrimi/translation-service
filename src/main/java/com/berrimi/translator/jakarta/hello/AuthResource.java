package com.berrimi.translator.jakarta.hello;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("auth")
public class AuthResource {

  @POST
  @Path("signup")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response signup(User user) {
    if (user.getUsername() == null || user.getPassword() == null ||
        user.getEmail() == null || user.getPhone() == null) {
      return Response.status(400)
          .entity("{\"error\":\"All fields (username, password, email, phone) are required\"}")
          .build();
    }

    boolean success = UserRepository.register(user);
    if (!success) {
      return Response.status(409).entity("{\"error\":\"Username already exists\"}").build();
    }

    return Response.ok("{\"message\":\"User registered\"}").build();
  }

  @POST
  @Path("login")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response login(User user) {
    if (user.getUsername() == null || user.getPassword() == null) {
      return Response.status(400)
          .entity("{\"error\":\"Username and password required\"}")
          .build();
    }

    boolean success = UserRepository.login(user.getUsername(), user.getPassword());
    if (!success) {
      return Response.status(401).entity("{\"error\":\"Invalid credentials\"}").build();
    }

    return Response.ok("{\"message\":\"Login successful\"}").build();
  }
}
