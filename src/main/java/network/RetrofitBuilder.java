package network;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;

public class RetrofitBuilder {
    private static final String BASE_URL = "http://localhost/StockManagerP/public/api/";

    public static final OkHttpClient httpClient = buildClient();
    public static final Retrofit retrofit = buildRetrofit();

    private static OkHttpClient buildClient(){
        final OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request request = chain.request();
                    Request.Builder requestBuilder = request.newBuilder()
                            .addHeader("Accept", "application/json")
                            .addHeader("Connection", "keep-alive");
                    request = requestBuilder.build();
                    return chain.proceed(request);
                });
        return builder.build();
    }


    private static Retrofit buildRetrofit(){
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static <T> T createService(Class<T> service){
        return retrofit.create(service);
    }
}
