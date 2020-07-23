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
import models.products.Service;
import network.ApiService;
import network.RetrofitBuilder;
import org.controlsfx.control.NotificationPane;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import utils.Utility;

import java.net.URL;
import java.util.ResourceBundle;

public class CreateService implements Initializable {


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
    private TextField tfDescription;

    @FXML
    private Button btnSave;

    @FXML
    private Button btnCancel;

    private HomeDataInterface dataInterface;
    private ApiService apiService;
    private Service service;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        apiService = RetrofitBuilder.createService(ApiService.class);
        Utility.setupNotificationPane(notificationPane, vbHolder);
        Platform.runLater(()->Utility.setLogo(vbParent));
        btnSave.setOnAction(event -> save());
        btnCancel.setOnAction(event -> Utility.closeWindow(vbParent));
    }

    private void save(){
        String name = tfName.getText().trim();
        String description = tfDescription.getText().trim();
        if (name.equals("")) {
            notificationPane.show("Service name is required.");return;
        }
        Call<Service[]> call;
        if (service==null) call = apiService.addService(name, description);
        else {
            name = service.getName().equals(name)?null:name;
            call = apiService.updateService(service.getId(), name, description);
        }
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Service[]> call, Response<Service[]> response) {
                if (response.isSuccessful()){
                    if (dataInterface!=null){
                        Platform.runLater(()->{
                            Utility.closeWindow(vbParent);
                            dataInterface.updateData("Service has been saved.", response.body());
                        });
                    }else {
                        Platform.runLater(() -> {
                            assert response.errorBody() != null;
                            notificationPane.show(Utility.handleApiErrors(response.message(), response.errorBody(), new String[]{"name"}));
                        });
                    }
                }
            }

            @Override
            public void onFailure(Call<Service[]> call, Throwable throwable) {
                notificationPane.show(throwable.getMessage());
            }
        });
    }

    public void setDataInterface(HomeDataInterface dataInterface) {
        this.dataInterface = dataInterface;
    }

    public void setService(Service service) {
        this.service = service;
        labelId.setText(String.valueOf(service.getId()));
        tfName.setText(service.getName());
        tfDescription.setText(service.getDescription());
    }
}
