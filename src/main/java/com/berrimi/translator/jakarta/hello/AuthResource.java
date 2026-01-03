package com.berrimi.translator.jakarta.hello;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.*;
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
      return Response.status(409)
          .entity("{\"error\":\"Username already exists\"}")
          .build();
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
      return Response.status(401)
          .entity("{\"error\":\"Invalid credentials\"}")
          .build();
    }

    // Get user details to return
    User foundUser = UserRepository.getUser(user.getUsername());
    if (foundUser != null) {
      JsonObject json = Json.createObjectBuilder()
          .add("message", "Login successful")
          .add("username", foundUser.getUsername())
          .add("email", foundUser.getEmail())
          .add("phone", foundUser.getPhone())
          .build();
      return Response.ok(json.toString()).build();
    }

    return Response.ok("{\"message\":\"Login successful\"}").build();
  }

  @GET
  @Path("user/{username}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getUser(@PathParam("username") String username) {
    if (username == null || username.isBlank()) {
      return Response.status(400)
          .entity("{\"error\":\"Username is required\"}")
          .build();
    }

    User user = UserRepository.getUser(username);
    if (user == null) {
      return Response.status(404)
          .entity("{\"error\":\"User not found\"}")
          .build();
    }

    JsonObject json = Json.createObjectBuilder()
        .add("username", user.getUsername())
        .add("email", user.getEmail())
        .add("phone", user.getPhone())
        .build();

    return Response.ok(json.toString()).build();
  }

  @PUT
  @Path("user/{username}")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response updateUser(@PathParam("username") String username, UserUpdateRequest request) {
    if (username == null || username.isBlank()) {
      return Response.status(400)
          .entity("{\"error\":\"Username is required\"}")
          .build();
    }

    // Check if user exists
    if (!UserRepository.userExists(username)) {
      return Response.status(404)
          .entity("{\"error\":\"User not found\"}")
          .build();
    }

    // Validate request
    if (request.getEmail() == null && request.getPhone() == null) {
      return Response.status(400)
          .entity("{\"error\":\"At least email or phone must be provided\"}")
          .build();
    }

    // Get current user data
    User currentUser = UserRepository.getUser(username);
    if (currentUser == null) {
      return Response.status(404)
          .entity("{\"error\":\"User not found\"}")
          .build();
    }

    // Use existing values if not provided
    String newEmail = request.getEmail() != null ? request.getEmail() : currentUser.getEmail();
    String newPhone = request.getPhone() != null ? request.getPhone() : currentUser.getPhone();

    boolean success = UserRepository.updateUser(username, newEmail, newPhone);
    if (!success) {
      return Response.status(500)
          .entity("{\"error\":\"Failed to update user\"}")
          .build();
    }

    JsonObject json = Json.createObjectBuilder()
        .add("message", "User updated successfully")
        .add("username", username)
        .add("email", newEmail)
        .add("phone", newPhone)
        .build();

    return Response.ok(json.toString()).build();
  }

  @PUT
  @Path("user/{username}/password")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response updatePassword(@PathParam("username") String username, PasswordUpdateRequest request) {
    if (username == null || username.isBlank()) {
      return Response.status(400)
          .entity("{\"error\":\"Username is required\"}")
          .build();
    }

    if (request.getOldPassword() == null || request.getNewPassword() == null) {
      return Response.status(400)
          .entity("{\"error\":\"Old password and new password are required\"}")
          .build();
    }

    if (request.getNewPassword().length() < 6) {
      return Response.status(400)
          .entity("{\"error\":\"New password must be at least 6 characters\"}")
          .build();
    }

    boolean success = UserRepository.updatePassword(username, request.getOldPassword(), request.getNewPassword());
    if (!success) {
      return Response.status(401)
          .entity("{\"error\":\"Invalid old password or user not found\"}")
          .build();
    }

    return Response.ok("{\"message\":\"Password updated successfully\"}").build();
  }

  @DELETE
  @Path("user/{username}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response deleteUser(@PathParam("username") String username,
      @QueryParam("password") String password) {
    if (username == null || username.isBlank()) {
      return Response.status(400)
          .entity("{\"error\":\"Username is required\"}")
          .build();
    }

    if (password == null || password.isBlank()) {
      return Response.status(400)
          .entity("{\"error\":\"Password is required for account deletion\"}")
          .build();
    }

    // Verify password before deletion
    boolean authenticated = UserRepository.login(username, password);
    if (!authenticated) {
      return Response.status(401)
          .entity("{\"error\":\"Invalid password\"}")
          .build();
    }

    boolean success = UserRepository.deleteUser(username);
    if (!success) {
      return Response.status(500)
          .entity("{\"error\":\"Failed to delete user\"}")
          .build();
    }

    return Response.ok("{\"message\":\"User account deleted successfully\"}").build();
  }
}
