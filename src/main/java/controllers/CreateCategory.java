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
import org.controlsfx.validation.ValidationMessage;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import utils.Utility;

import java.net.URL;
import java.util.Iterator;
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
    ValidationSupport vs = new ValidationSupport();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        apiService = RetrofitBuilder.createService(ApiService.class);
        Utility.setupNotificationPane(notificationPane, vbHolder);
        Platform.runLater(()->Utility.setLogo(vbParent));
        btnSave.setOnAction(event -> save());
        btnCancel.setOnAction(event -> Utility.closeWindow(vbParent));

        vs.registerValidator(tfName, true, Validator.createEmptyValidator("Name is required."));
    }

    private void save(){
        ValidationResult vr = vs.getValidationResult();
        Iterator<ValidationMessage> iterator = vr.getErrors().iterator();
        StringBuilder message = new StringBuilder();
        while (iterator.hasNext()){
            message.append(iterator.next().getText());
            message.append("\n");
        }
        if (!vr.getErrors().isEmpty()){
            notificationPane.show(message.toString());
            return;
        }
        String name = tfName.getText().trim();
        Call<Category[]> call;
        if (category == null){
            call = apiService.addCategory(name);
        }else{
            if (category.getName().equals(name)){
                Utility.closeWindow(vbParent);
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
                        Utility.closeWindow(vbParent);
                        if (dataInterface != null) {
                            dataInterface.updateData("The category has been saved.", response.body());
                        } else {
                            notificationPane.show("The category has been saved.");
                        }
                    });
                } else {
                    Platform.runLater(() -> {
                        assert response.errorBody() != null;
                        notificationPane.show(Utility.handleApiErrors(response.message(), response.errorBody(), new String[]{"name"}));
                    });
                }
            }

            @Override
            public void onFailure(Call<Category[]> call, Throwable throwable) {
                Platform.runLater(()->notificationPane.show(throwable.getMessage()));
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
