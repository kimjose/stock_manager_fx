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
import org.controlsfx.validation.*;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import utils.SessionManager;
import utils.Utility;

import java.net.URL;
import java.util.Iterator;
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
    private ValidationSupport vs = new ValidationSupport();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        apiService = RetrofitBuilder.createService(ApiService.class);
        Utility.setupNotificationPane(notificationPane, vbHolder);
        Utility.restrictInputNum(tfPhone);

        btnSave.setOnAction(event -> save());
        btnCancel.setOnAction(event -> Utility.closeWindow(vbParent));
        Platform.runLater(() -> Utility.setLogo(vbParent));
        vs.registerValidator(tfName, true, Validator.createEmptyValidator("Name is required."));
        vs.registerValidator(tfEmail, false, Validator.createRegexValidator("Enter a valid email.", Utility.EMAIL_PATTERN, Severity.ERROR));
        vs.registerValidator(tfPhone, false, Validator.createRegexValidator("Enter a valid number", Utility.MOBILENUM_PATTERN, Severity.ERROR));
    }

    private void save() {
        ValidationResult vr = vs.getValidationResult();
        Iterator<ValidationMessage> iterator = vr.getErrors().iterator();
        StringBuilder message = new StringBuilder();
        while (iterator.hasNext()) {
            message.append(iterator.next().getText());
            message.append("\n");
        }
        if (!message.toString().isEmpty()) {
            notificationPane.show(message.toString());
            return;
        }
        String name = tfName.getText().trim();
        String email = tfEmail.getText() == null ? "" : tfEmail.getText().trim();
        String phone = tfPhone.getText() == null ? "" : tfPhone.getText().trim();
        if (name.equals("")) {
            notificationPane.show("vendor name is required.");
            return;
        }
        Call<Vendor[]> call;
        if (vendor == null) call = apiService.addVendor(name, email, phone, SessionManager.INSTANCE.getUser().getId());
        else {
            name = vendor.getName().equals(name) ? null : name;
            email = vendor.getEmail().equals(email) ? null : email;
            phone = vendor.getPhone().equals(phone) ? null : phone;
            call = apiService.updateVendor(vendor.getId(), name, email, phone);
        }
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Vendor[]> call, Response<Vendor[]> response) {
                if (response.isSuccessful()) {
                    Platform.runLater(() -> {
                        if (dataInterface != null) {
                            dataInterface.updateData("The Vendor has been saved", response.body());
                        } else {
                            notificationPane.show("The Vendor has been saved");
                        }
                        Utility.closeWindow(vbParent);

                    });
                } else {
                    Platform.runLater(() -> {
                        assert response.errorBody() != null;
                        notificationPane.show(Utility.handleApiErrors(response.message(), response.errorBody(), new String[]{"name", "email", "phone"}));
                    });
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
        tfPhone.setText(vendor.getPhone() == null ? "" : vendor.getPhone());
    }
}
