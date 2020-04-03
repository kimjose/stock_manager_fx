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
import models.products.Warehouse;
import network.ApiService;
import network.RetrofitBuilder;
import org.controlsfx.control.NotificationPane;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.net.URL;
import java.util.ResourceBundle;

public class CreateWarehouse implements Initializable {

    @FXML
    private VBox vbParent;
/*
    @FXML
    private NotificationPane notificationPane;*/

    @FXML
    private VBox vbHolder;

    @FXML
    private Label labelId;

    @FXML
    private Label labelId1;

    @FXML
    private TextField tfName;

    @FXML
    private Label labelId11;

    @FXML
    private TextField tfLocation;

    @FXML
    private Button btnSave;

    @FXML
    private Button btnCancel;

    private Warehouse warehouse;
    private HomeDataInterface dataInterface;
    private ApiService apiService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        apiService = RetrofitBuilder.createService(ApiService.class);

        btnSave.setOnAction(event -> save());
        btnCancel.setOnAction(event -> {
            Stage stage = (Stage) vbHolder.getScene().getWindow();
            stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
        });

    }
    private void save(){
        String name = tfName.getText();
        String location = tfLocation.getText();
        if (name.trim().equals("")){//todo notify and return
        }
        if (location.trim().equals("")){//todo notify and return
        }
        Call<Warehouse[]> call = warehouse==null?apiService.addWarehouse(name, location):apiService.updateWarehouse(warehouse.getId(), name, location);;
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Warehouse[]> call, Response<Warehouse[]> response) {
                if (response.isSuccessful()){
                    if (dataInterface!=null){
                        Platform.runLater(()->{
                            Stage stage = (Stage) vbParent.getScene().getWindow();
                            stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
                            dataInterface.updateData("Warehouse has been saved successfully.", response.body());
                        });
                    }
                }else{
                    //todo notify
                }
            }

            @Override
            public void onFailure(Call<Warehouse[]> call, Throwable throwable) {
                //todo notify
            }
        });

    }


    public void setDataInterface(HomeDataInterface dataInterface) {
        this.dataInterface = dataInterface;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
        labelId.setText(String.valueOf(warehouse.getId()));
        tfName.setText(warehouse.getName());
        tfLocation.setText(warehouse.getLocation());
    }
}
