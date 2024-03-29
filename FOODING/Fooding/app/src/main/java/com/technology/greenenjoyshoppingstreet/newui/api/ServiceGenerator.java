package com.technology.greenenjoyshoppingstreet.newui.api;

import com.technology.greenenjoyshoppingstreet.utils.constant.URLConstant;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServiceGenerator {
    private static final String BASE_URL = URLConstant.HOST;



    private static Retrofit.Builder builder = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create());

    private static Retrofit retrofit = builder.build();

    private static HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY);


    private static OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();


    public static <S> S createService(Class<S> serviceClass){

        if (!httpClientBuilder.interceptors().contains(loggingInterceptor)) {
            httpClientBuilder.addInterceptor(loggingInterceptor);
            builder.client(httpClientBuilder.build());
            retrofit = builder.build();
        }

        return retrofit.create(serviceClass);
    }

}
