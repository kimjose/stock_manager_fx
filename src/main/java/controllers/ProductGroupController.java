package controllers;

import interfaces.HomeDataInterface;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import models.products.*;
import network.ApiService;
import network.RetrofitBuilder;
import org.controlsfx.control.NotificationPane;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import utils.Utility;

import java.net.URL;
import java.util.ResourceBundle;

public class ProductGroupController implements Initializable {

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
    private TextField tfPrice;

    @FXML
    private ComboBox<Product> cbProduct;

    @FXML
    private TextField tfQuantity;

    @FXML
    private Button btnSave;

    @FXML
    private Button btnCancel;

    private ProductGroup group;
    private ApiService apiService;
    private HomeDataInterface dataInterface;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        Utility.setupNotificationPane(notificationPane, vbHolder);
        apiService = RetrofitBuilder.createService(ApiService.class);
        Utility.restrictInputNum(tfQuantity);
        Utility.restrictInputDec(tfPrice);

        btnSave.setOnAction(event -> save());
        btnCancel.setOnAction(event -> Utility.closeWindow(vbHolder));
        Platform.runLater(()->Utility.setLogo(vbParent));
        loadData();

        //function keys
        vbParent.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            KeyCode keyCode = event.getCode();
            if (keyCode.equals(KeyCode.F9)) Utility.closeWindow(vbHolder);
            else if (keyCode.equals(KeyCode.F5)) loadData();
//            event.consume();
        });
    }

    private void save() {
        String name = tfName.getText().trim();
        String desc = tfDescription.getText().trim();
        Product product = null;
        String errorMessage = "";
        String quantity = tfQuantity.getText().trim();
        String price = tfPrice.getText().trim();
        int q = 0;
        double p = 0;
        if (name.equals("")) errorMessage = errorMessage.concat("Name is required.");
        if (quantity.equals("")) {
            errorMessage = errorMessage.concat(errorMessage.equals("") ? "Enter a valid quantity" : "\nEnter a valid quantity");
        } else {
            try {
                q = Integer.parseInt(quantity);
                if (q <= 0) throw new NumberFormatException();
            } catch (Exception e) {
                errorMessage = errorMessage.equals("") ? "Enter a valid quantity" : "\nEnter a valid quantity";
            }
        }
        if (price.equals("")) {
            errorMessage = errorMessage.equals("") ? "Enter a valid price" : "\nEnter a valid price";
        } else {
            try {
                p = Double.parseDouble(price);
                if (p <= 0) throw new NumberFormatException();
            } catch (Exception e) {
                errorMessage += errorMessage.equals("") ? "Enter a valid price" : "\nEnter a valid price";
            }
        }
        try {
            product = cbProduct.getValue();
        } catch (Exception e) {
            errorMessage += errorMessage.equals("") ? "Select a valid product" : "\nSelect a valid product";
        }
        if (!errorMessage.equals("")) {
            notificationPane.show(errorMessage);
            return;
        }
        Call<ProductGroup[]> call = apiService.addGroup(name, desc, product.getId(), q, p);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ProductGroup[]> call, Response<ProductGroup[]> response) {
                Platform.runLater(()->{
                    if (response.isSuccessful()){
                        Utility.closeWindow(vbParent);
                        if (dataInterface != null) dataInterface.updateData("The group has been saved", response.body());
                    }else {
                        notificationPane.show(Utility.handleApiErrors(response.message(), response.errorBody(),
                                new String[]{"name", "productId", "price", "quantity"}));
                    }
                });
            }

            @Override
            public void onFailure(Call<ProductGroup[]> call, Throwable throwable) {
                Platform.runLater(()->notificationPane.show(throwable.getMessage()));
            }
        });
    }

    private void loadData() {
        Call<Product[]> call = apiService.products();
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Product[]> call, Response<Product[]> response) {
                if (response.isSuccessful()) {
                    Platform.runLater(() -> {
                        cbProduct.setItems(FXCollections.observableArrayList(response.body()));
                        if (group != null) cbProduct.getSelectionModel().select(group.getProduct());
                    });
                } else Platform.runLater(() -> notificationPane.show(response.message()));
            }

            @Override
            public void onFailure(Call<Product[]> call, Throwable throwable) {
                Platform.runLater(() -> notificationPane.show(throwable.getMessage()));
            }
        });
    }

    public void setDataInterface(HomeDataInterface dataInterface) {
        this.dataInterface = dataInterface;
    }

    public void setGroup(ProductGroup group) {
        this.group = group;
        labelId.setText(String.valueOf(group.getId()));
        tfName.setText(group.getName());
        tfDescription.setText(group.getDescription());
        tfPrice.setText(String.valueOf(group.getPrice()));
        tfQuantity.setText(String.valueOf(group.getQuantity()));
    }
}
