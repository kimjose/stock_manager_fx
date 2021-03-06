package controllers;

import interfaces.HomeDataInterface;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import models.products.Brand;
import network.ApiService;
import network.RetrofitBuilder;
import org.controlsfx.control.Notifications;
import org.controlsfx.validation.*;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import utils.Utility;

import java.net.URL;
import java.util.Iterator;
import java.util.ResourceBundle;

public class CreateBrand implements Initializable {

    @FXML
    private VBox vbParent;

    @FXML
    private Label labelId;

    @FXML
    private TextField tfName;

    @FXML
    private Button btnSave;

    @FXML
    private Button btnCancel;

    private Brand brand;
    private ApiService apiService;
    private HomeDataInterface dataInterface;
    ValidationSupport vs = new ValidationSupport();


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        apiService = RetrofitBuilder.createService(ApiService.class);
        Platform.runLater(()->{
            Utility.setLogo(vbParent);
            btnSave.setOnAction(event -> save());
            btnCancel.setOnAction(event -> Utility.closeWindow(vbParent));
        });

        vs.registerValidator(tfName, true, Validator.createEmptyValidator("Name is required."));
    }

    public void setBrand(Brand brand) {
        this.brand = brand;
        labelId.setText(String.valueOf(brand.getId()));
        tfName.setText(brand.getName());
    }

    public void setDataInterface(HomeDataInterface dataInterface) {
        this.dataInterface = dataInterface;
    }

    private void save(){
        ValidationResult vr = vs.getValidationResult();
        Iterator<ValidationMessage> iterator = vr.getErrors().iterator();
        StringBuilder message = new StringBuilder();
        while (iterator.hasNext()){
            message.append(iterator.next().getText());
            message.append("\n");
        }
        if (!message.toString().isEmpty()) createNotification(-1, message.toString());
        else {
            Call<Brand[]> call;
            String name = tfName.getText().trim();
            if (brand == null) {
                call = apiService.addBrand(name);
            }else{
                if (brand.getName().equals(name)){
                    Platform.runLater(()-> Utility.closeWindow(vbParent));
                    return;
                }
                call = apiService.updateBrand(brand.getId(), name);
            }
            assert call != null;
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(Call<Brand[]> call, Response<Brand[]> response) {
                    if (response.isSuccessful()) {
                        Platform.runLater(()->createNotification( 0,"Brand has been saved."));
                        if (dataInterface!=null){
                            Platform.runLater(()->{
                                Stage stage = (Stage) vbParent.getScene().getWindow();
                                stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
                                dataInterface.updateData("Brand has been saved successfully.", response.body());
                            });
                        }
                    }else{
                        assert response.errorBody() != null;
                        createNotification(-1, Utility.handleApiErrors(response.message(),response.errorBody(), new String[]{"name"}));
                    }
                }

                @Override
                public void onFailure(Call<Brand[]> call, Throwable throwable) {
                    createNotification(-1, throwable.getMessage());
                }
            });

        }
    }

    private void createNotification(int type, String message){
        Notifications n = Notifications.create()
                .owner(vbParent)
                .text(message)
                .hideAfter(Duration.seconds(8))
                .darkStyle()
                .position(Pos.BOTTOM_RIGHT);
        Platform.runLater(()->{
            switch (type){
                case -1: n.showError();break;
                case 0: n.showInformation();break;
                default: n.showWarning();
            }

        });

    }
}