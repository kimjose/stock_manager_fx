package controllers;

import interfaces.HomeDataInterface;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import models.customers.Customer;
import network.ApiService;
import network.RetrofitBuilder;
import org.controlsfx.control.NotificationPane;
import org.controlsfx.validation.*;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import utils.SessionManager;
import utils.Utility;

import java.net.URL;
import java.util.Iterator;
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
    private ValidationSupport vs = new ValidationSupport();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(()->Utility.setLogo(vbParent));
        apiService = RetrofitBuilder.createService(ApiService.class);
        Utility.setupNotificationPane(notificationPane, vbHolder);
        Utility.restrictInputNum(tfPhone);

        btnSave.setOnAction(event -> save());
        btnCancel.setOnAction(event -> Utility.closeWindow(vbParent));

        vs.registerValidator(tfName, true, Validator.createEmptyValidator("Name is required."));
        vs.registerValidator(tfEmail, false, Validator.createRegexValidator("Enter a valid email.", Utility.EMAIL_PATTERN, Severity.ERROR));
        vs.registerValidator(tfPhone, false, Validator.createRegexValidator("Enter a valid number", Utility.MOBILENUM_PATTERN, Severity.ERROR));
    }
    private void save(){
        ValidationResult vr = vs.getValidationResult();
        Iterator<ValidationMessage> iterator = vr.getErrors().iterator();
        StringBuilder message = new StringBuilder();
        while (iterator.hasNext()){
            message.append(iterator.next().getText());
            message.append("\n");
        }
        if (!message.toString().isEmpty()) {
            notificationPane.show(message.toString()); return;
        }
        String name = tfName.getText().trim();
        String email = tfEmail.getText() == null ? "" : tfEmail.getText().trim();
        String phone = tfPhone.getText() == null ? "" : tfPhone.getText().trim();
        Call<Customer[]> call;
        if (customer==null) call = apiService.addCustomer(name, email, phone, SessionManager.INSTANCE.getUser().getId());
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
                            Utility.closeWindow(vbParent);
                        });
                    }
                }else{
                    Platform.runLater(()-> {
                        assert response.errorBody() != null;
                        notificationPane.show(Utility.handleApiErrors(response.message(), response.errorBody(), new String[]{"name", "email","phone"}));
                    });
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
