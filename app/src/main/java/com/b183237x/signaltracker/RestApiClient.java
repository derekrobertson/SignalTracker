package com.b183237x.signaltracker;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import java.io.IOException;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RestApiClient {

    private static Retrofit retrofit;

    public static Retrofit getClient(Context context, String Username, String Password) {
        String API_URL = context.getString(R.string.API_URL);
        String API_KEY = context.getString(R.string.API_KEY);
        String credentials = Username + ":" + Password;
        String base64EncodedCreds = Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);

        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request newRequest = chain.request().newBuilder()
                            .addHeader("Content-Type", "application/json")
                            .addHeader("Authorization", "Basic " + base64EncodedCreds)
                            .addHeader("x-api-key", API_KEY)
                            .build();
                    return chain.proceed(newRequest);
                }
            }).build();

            retrofit = new Retrofit.Builder()
                    .client(client)
                    .baseUrl(API_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }


}
