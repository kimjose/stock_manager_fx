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
import network.ApiService;
import network.RetrofitBuilder;
import org.controlsfx.control.NotificationPane;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import utils.Utility;

import java.net.URL;
import java.util.ResourceBundle;

public class CreateCustomer implements Initializable {

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

    private Customer customer;
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
            notificationPane.show("Customer name is required.");return;
        }
        Call<Customer[]> call;
        if (customer==null) call = apiService.addCustomer(name, email, phone, 1);
        else{
            name = customer.getName().equals(name)?null:name;
            email = customer.getEmail().equals(email)?null:email;
            phone = customer.getPhone().equals(phone)?null:phone;
            call = apiService.updateCustomer(customer.getId(), name, email, phone);
        }
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Customer[]> call, Response<Customer[]> response) {
                if (response.isSuccessful()){
                    if (dataInterface!=null){
                        Platform.runLater(()->{
                            dataInterface.updateData("The customer has been saved", response.body());
                            Stage stage = (Stage) vbParent.getScene().getWindow();
                            stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
                        });
                    }
                }else{
                    Platform.runLater(()-> notificationPane.show(response.message()));
                }
            }

            @Override
            public void onFailure(Call<Customer[]> call, Throwable throwable) {
                notificationPane.show(throwable.getMessage());
            }
        });
    }


    public void setDataInterface(HomeDataInterface dataInterface) {
        this.dataInterface = dataInterface;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
        labelId.setText(String.valueOf(customer.getId()));
        tfName.setText(customer.getName());
        tfEmail.setText(customer.getEmail());
        tfPhone.setText(customer.getPhone());
    }
}
