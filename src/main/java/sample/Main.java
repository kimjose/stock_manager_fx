package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import models.products.Brand;
import network.ApiService;
import network.RetrofitBuilder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.Arrays;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        ApiService apiService = RetrofitBuilder.createService(ApiService.class);
        Call<Brand[]> call = apiService.brands();
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Brand[]> call, Response<Brand[]> response) {
                if (response.isSuccessful()) {
                    System.out.println(Arrays.toString(response.body()));
                }
            }

            @Override
            public void onFailure(Call<Brand[]> call, Throwable throwable) {

            }
        });

/*
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();*/
    }


    public static void main(String[] args) {
        launch(args);
    }
}
