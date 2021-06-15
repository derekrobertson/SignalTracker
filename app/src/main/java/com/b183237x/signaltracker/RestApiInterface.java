package com.b183237x.signaltracker;

import java.util.List;


import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;


public interface RestApiInterface {

    // Get all users, will fail if user role is not ADMIN
    @GET("users")
    Call<List<User>> getUsers();

    // Get a single user by specifying its email
    // Unless ADMIN role, only the currently authenticated email can be retrieved
    @GET("users/{email}")
    Call<User> getUserByEmail(@Path("email") String email);

    // Get a single user by specifying its user_id
    // Unless ADMIN role, only the currently authenticated user_id can be retrieved
    @GET("users/{user_id}")
    Call<User> getUserById(@Path("user_id") String user_id);



}
