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
import models.products.UnitOfMeasure;
import network.ApiService;
import network.RetrofitBuilder;
import org.controlsfx.control.NotificationPane;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import utils.Utility;

import java.net.URL;
import java.util.ResourceBundle;

public class CreateUOM implements Initializable {


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

    private UnitOfMeasure uom;
    private HomeDataInterface dataInterface;
    private ApiService apiService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        apiService = RetrofitBuilder.createService(ApiService.class);
        Utility.setupNotificationPane(notificationPane, vbHolder);

        btnSave.setOnAction(event -> save());
        btnCancel.setOnAction(event -> {
            Stage stage = (Stage) vbHolder.getScene().getWindow();
            stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
        });
    }

    private void save(){
        String name = tfName.getText().trim();
        String description = tfDescription.getText().trim();
        if (name.equals("")){
            notificationPane.show("Unit name is required.");return;
        }
        Call<UnitOfMeasure[]> call;
        if (uom == null){
            call = apiService.addUOM(name, description);
        }else{
            name = uom.getName().equals(name)?null:name;
            call = apiService.updateUOM(uom.getId(), name, description);
        }
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<UnitOfMeasure[]> call, Response<UnitOfMeasure[]> response) {
                if (response.isSuccessful()){
                    Platform.runLater(()->{
                        Stage stage = (Stage) vbHolder.getScene().getWindow();
                        stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
                        if (dataInterface!=null){
                            dataInterface.updateData("The Unit of measure has been saved", response.body());
                        }
                    });
                }else{
                    notificationPane.show(response.message());
                }
            }

            @Override
            public void onFailure(Call<UnitOfMeasure[]> call, Throwable throwable) {
                notificationPane.show(throwable.getMessage());
            }
        });
    }

    public void setDataInterface(HomeDataInterface dataInterface) {
        this.dataInterface = dataInterface;
    }

    public void setUom(UnitOfMeasure uom) {
        this.uom = uom;
        labelId.setText(String.valueOf(uom.getId()));
        tfName.setText(uom.getName());
        tfDescription.setText(uom.getDescription());
    }
}
