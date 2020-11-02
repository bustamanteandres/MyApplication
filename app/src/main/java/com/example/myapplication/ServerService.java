package com.example.myapplication;

import android.media.session.MediaSession;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface ServerService {
    @POST("api/register")
    Call<UserResponse> register(@Body UserRequest request);

    @POST("api/login")
    Call<UserResponse> login(@Body UserRequest request);

    @POST("api/event")
    Call<EventResponse> event(@Header("Authorization") String token, @Body EventRequest request);

    @PUT("api/refresh")
    Call<UserResponse> refresh(@Header("Authorization") String tokenRefresh);
}