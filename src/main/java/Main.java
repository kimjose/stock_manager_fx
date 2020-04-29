import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import models.auth.User;
import network.ApiService;
import network.RetrofitBuilder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import utils.SessionManager;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        login();
        Parent root = FXMLLoader.load(getClass().getResource("fxml/main_stage.fxml"));
        primaryStage.setTitle("Inventory Manager");
        primaryStage.setScene(new Scene(root, 1020, 700));
        primaryStage.setMinHeight(600);
        primaryStage.setMinWidth(900);
        primaryStage.show();
    }
    private void login(){
        ApiService apiService = RetrofitBuilder.createService(ApiService.class);
        Call<User> call = apiService.login("admin", "admin123", "admin123");
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()){
                    SessionManager sessionManager = SessionManager.INSTANCE;
                    sessionManager.setUser(response.body());
                }else System.exit(-2);
            }

            @Override
            public void onFailure(Call<User> call, Throwable throwable) {
                System.out.println(throwable.getMessage());
                System.exit(-3);
            }
        });
    }


    public static void main(String[] args) {
        launch(args);
    }
}
