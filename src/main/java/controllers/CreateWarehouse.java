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
import org.jetbrains.annotations.NotNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import utils.Utility;

import java.net.URL;
import java.util.ResourceBundle;

public class CreateWarehouse implements Initializable {

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
        Platform.runLater(()->Utility.setLogo(vbParent));
        btnSave.setOnAction(event -> save());
        btnCancel.setOnAction(event -> Utility.closeWindow(vbParent));
        Utility.setupNotificationPane(notificationPane, vbHolder);
    }
    private void save(){
        String name = tfName.getText().trim();
        String location = tfLocation.getText().trim();
        if (name.equals("")){
            notificationPane.show("name is required");
            return;
        }
        if (location.equals("")){
            notificationPane.show("location is required");
            return;
        }
        Call<Warehouse[]> call;
        if(warehouse==null){
            call = apiService.addWarehouse(name, location);
        }else{
            name = name.equals(warehouse.getName())?null:name;
            call = apiService.updateWarehouse(warehouse.getId(), name, location);;
        }
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Warehouse[]> call, Response<Warehouse[]> response) {
                if (response.isSuccessful()){
                    if (dataInterface!=null){
                        Platform.runLater(()->{
                            Utility.closeWindow(vbParent);
                            dataInterface.updateData("Warehouse has been saved successfully.", response.body());
                        });
                    }
                }else{
                    Platform.runLater(() -> {
                        assert response.errorBody() != null;
                        notificationPane.show(Utility.handleApiErrors(response.message(), response.errorBody(), new String[]{"name"}));
                    });
                }
            }

            @Override
            public void onFailure(Call<Warehouse[]> call, Throwable throwable) {
                notificationPane.show(throwable.getMessage());
            }
        });

    }


    public void setDataInterface(HomeDataInterface dataInterface) {
        this.dataInterface = dataInterface;
    }

    public void setWarehouse(@NotNull Warehouse warehouse) {
        this.warehouse = warehouse;
        labelId.setText(String.valueOf(warehouse.getId()));
        tfName.setText(warehouse.getName());
        tfLocation.setText(warehouse.getLocation());
    }
}
