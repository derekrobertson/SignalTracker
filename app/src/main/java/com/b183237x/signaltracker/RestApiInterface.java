package com.b183237x.signaltracker;

import com.b183237x.signaltracker.pojomodels.Celltower;
import com.b183237x.signaltracker.pojomodels.Device;
import com.b183237x.signaltracker.pojomodels.Reading;
import com.b183237x.signaltracker.pojomodels.User;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;


public interface RestApiInterface {

    // Retrieve a user account by specifying its email address
    // Unless account is ADMIN role, only the authenticated user account can be retrieved
    @GET("users/{email}")
    Call<User> getUserByEmail(@Path("email") String email);

    // Create a new user
    @POST("users")
    Call<User> createUser(@Body User user);

    // Create a new device
    @POST("devices")
    Call<Device> createDevice(@Body Device device);

    // Create a new cell tower
    @POST("celltowers")
    Call<Celltower> createCelltower(@Body Celltower celltower);

    // Create a new reading
    @POST("readings")
    Call<Reading> createReading(@Body Reading reading);

}
