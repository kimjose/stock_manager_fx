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
import models.customers.Customer;
import models.vendors.Vendor;
import network.ApiService;
import network.RetrofitBuilder;
import org.controlsfx.control.NotificationPane;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import utils.Utility;

import java.net.URL;
import java.util.ResourceBundle;

public class CreateVendor implements Initializable {
    @FXML
    private VBox vbParent;

    @FXML
    private VBox vbHolder;

    @FXML
    private Label labelId;

    @FXML
    private TextField tfName;

    @FXML
    private TextField tfEmail;

    @FXML
    private TextField tfPhone;

    @FXML
    private Button btnSave;

    @FXML
    private Button btnCancel;

    @FXML
    private NotificationPane notificationPane;

    private Vendor vendor;
    private HomeDataInterface dataInterface;
    private ApiService apiService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        apiService = RetrofitBuilder.createService(ApiService.class);
        Utility.setupNotificationPane(notificationPane, vbHolder);
        Utility.restrictInputNum(tfPhone);

        btnSave.setOnAction(event -> save());
        btnCancel.setOnAction(event -> {
            Stage stage = (Stage) vbParent.getScene().getWindow();
            stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
        });
    }

    private void save(){
        String name = tfName.getText().trim();
        String email = tfEmail.getText().trim();
        String phone = tfPhone.getText().trim();
        if (name.equals("")){
            notificationPane.show("vendor name is required.");return;
        }
        Call<Vendor[]> call;
        if (vendor == null) call = apiService.addVendor(name, email, phone, 1);
        else {
            name = vendor.getName().equals(name)?null:name;
            email = vendor.getEmail().equals(email)?null:email;
            phone = vendor.getPhone().equals(phone)?null:phone;
            call = apiService.updateVendor(vendor.getId(), name, email, phone);
        }
        call.enqueue(new Callback<Vendor[]>() {
            @Override
            public void onResponse(Call<Vendor[]> call, Response<Vendor[]> response) {
                if (response.isSuccessful()){
                    if (dataInterface!=null){
                        Platform.runLater(()->{
                            dataInterface.updateData("The Vendor has been saved", response.body());
                            Stage stage = (Stage) vbParent.getScene().getWindow();
                            stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
                        });
                    }
                }else{
                    Platform.runLater(()-> notificationPane.show(response.message()));
                }
            }

            @Override
            public void onFailure(Call<Vendor[]> call, Throwable throwable) {
                notificationPane.show(throwable.getMessage());
            }
        });
    }


    public void setDataInterface(HomeDataInterface dataInterface) {
        this.dataInterface = dataInterface;
    }

    public void setVendor(Vendor vendor) {
        this.vendor = vendor;
        labelId.setText(String.valueOf(vendor.getId()));
        tfName.setText(vendor.getName());
        tfEmail.setText(vendor.getEmail());
        tfPhone.setText(vendor.getPhone());
    }
}
