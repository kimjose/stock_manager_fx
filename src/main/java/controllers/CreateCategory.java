package controllers;

import interfaces.HomeDataInterface;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import models.products.Category;
import network.ApiService;
import network.RetrofitBuilder;
import org.controlsfx.control.NotificationPane;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import utils.Utility;

import java.net.URL;
import java.util.ResourceBundle;

public class CreateCategory implements Initializable {

    @FXML
    private VBox vbParent;

    @FXML
    private NotificationPane notificationPane;

    @FXML
    private VBox vbHolder;

    @FXML
    private Label labelId;


    @FXML
    private TextField tfName;

    @FXML
    private Button btnSave;

    @FXML
    private Button btnCancel;

    private Category category;
    private HomeDataInterface dataInterface;
    private ApiService apiService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        apiService = RetrofitBuilder.createService(ApiService.class);
        Utility.setupNotificationPane(notificationPane, vbHolder);

        btnSave.setOnAction(event -> save());
        btnCancel.setOnAction(event -> {
            Stage stage = (Stage) vbParent.getScene().getWindow();
            stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
        });

    }

    private void save(){
        String name = tfName.getText().trim();
        if (name.equals("")){
            notificationPane.show("Category name is required.");
            return;
        }
        Call<Category[]> call;
        if (category == null){
            call = apiService.addCategory(name);
        }else{
            if (category.getName().equals(name)){
                Stage stage = (Stage) vbParent.getScene().getWindow();
                stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
                return;
            }else{
                call = apiService.updateCategory(category.getId(), name);
            }
        }
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Category[]> call, Response<Category[]> response) {
                if (response.isSuccessful()) {
                    Platform.runLater(() -> {
                        Stage stage = (Stage) vbParent.getScene().getWindow();
                        stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
                        if (dataInterface != null) {
                            dataInterface.updateData("The category has been saved.", response.body());
                        }
                    });
                } else {
                    notificationPane.show(response.message());
                }
            }

            @Override
            public void onFailure(Call<Category[]> call, Throwable throwable) {
                notificationPane.show(throwable.getMessage());
            }
        });
    }


    public void setDataInterface(HomeDataInterface dataInterface) {
        this.dataInterface = dataInterface;
    }

    public void setCategory(Category category) {
        this.category = category;
        labelId.setText(String.valueOf(category.getId()));
        tfName.setText(category.getName());
    }
}
