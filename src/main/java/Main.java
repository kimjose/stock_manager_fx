import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import models.auth.User;
import network.ApiService;
import network.RetrofitBuilder;
import org.controlsfx.control.textfield.CustomPasswordField;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import utils.SessionManager;
import utils.Utility;

import java.io.IOException;

public class Main extends Application{
    @Override
    public void start(Stage primaryStage) throws Exception{
        Dialog dialog = new Dialog<>();
        DialogPane pane = null;
        try {
            pane = FXMLLoader.load(getClass().getResource("fxml/login.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        ButtonType loginButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        pane.getButtonTypes().addAll(loginButtonType, cancelButtonType);
        pane.getContent().setStyle("-fx-background-color:#fff;");
        Node loginButton = pane.lookupButton(loginButtonType);
        Node cancelButton = pane.lookupButton(cancelButtonType);
        loginButton.setStyle("-fx-background-color:#19D019;");
        cancelButton.setStyle("-fx-background-color:#f80707; -fx-text-fill: white;");
        ApiService apiService = RetrofitBuilder.createService(ApiService.class);
        dialog.setDialogPane(pane);
        VBox vBox = (VBox) pane.getContent();
        TextField tfName = (TextField)  vBox.getChildren().get(2);
        tfName.requestFocus();
        CustomPasswordField passwordField = (CustomPasswordField)  vBox.getChildren().get(4);
        Label myLabel = (Label) vBox.getChildren().get(5);
        tfName.setText("admin");
        passwordField.setText("admin123");
        tfName.textProperty().addListener((observable, oldValue, newValue) -> loginButton.setDisable(newValue.trim().equals("")));
        loginButton.addEventFilter(ActionEvent.ACTION, event -> {
            event.consume();
            String userName = tfName.getText().trim();
            String password = passwordField.getText().trim();
            Call<User> call = apiService.login(userName, password, password);
            myLabel.setText("Logging in. Please wait...");
            myLabel.setStyle("-fx-text-fill: #19D019; -fx-font-family:Arial;");
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if (response.isSuccessful()){
                        SessionManager sessionManager = SessionManager.INSTANCE;
                        sessionManager.setUser(response.body());
                        Platform.runLater(()->{
                            myLabel.setText("Login successful");
                            dialog.close();
                        });
                    }else {
                        Platform.runLater(()->{
                            myLabel.setText("Login Failed. "+response.message());
                            myLabel.setStyle("-fx-text-fill: #DC143C");
                        });
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable throwable) {
                    System.out.println(throwable.getMessage());
                    Platform.runLater(()->{
                        myLabel.setText(throwable.getMessage());
                        myLabel.setStyle("-fx-text-fill: #DC143C");
                    });
                }
            });
        });
        dialog.initStyle(StageStyle.UNDECORATED);
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getScene().getStylesheets().add("file:/"+Utility.USER_DIR.replace("\\", "/")+"/src/main/resources/css/Main.css");
        stage.getIcons().add(new Image(Utility.IM_LOGO_IMAGE));
        stage.requestFocus();
        dialog.showAndWait();
        if (SessionManager.INSTANCE.getUser() == null) System.exit(-2);
        Parent root = FXMLLoader.load(getClass().getResource("fxml/main_stage.fxml"));
        primaryStage.setTitle("Inventory Manager");
        primaryStage.setMinHeight(550);
        primaryStage.setMinWidth(1020);
        primaryStage.setScene(new Scene(root));
        primaryStage.getIcons().add(new Image(Utility.IM_LOGO_IMAGE));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
