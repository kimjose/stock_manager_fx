package controllers;

import interfaces.HomeDataInterface;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import models.auth.User;
import models.products.Product;
import network.ApiService;
import network.RetrofitBuilder;
import org.controlsfx.control.NotificationPane;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import utils.SessionManager;
import utils.Utility;

public class AddToStock {

    @FXML
    private VBox vbParent;

    @FXML
    private NotificationPane notificationPane;

    @FXML
    private VBox vbHolder;

    @FXML
    private ComboBox<Product> cbProduct;

    @FXML
    private Label labelAvailable;

    @FXML
    private TextField tfQuantity;

    @FXML
    private Button btnAdd;

    @FXML
    private Button btnMinus;

    private User user = SessionManager.INSTANCE.getUser();
    private ApiService apiService = RetrofitBuilder.createService(ApiService.class);
    private HomeDataInterface dataInterface;

    @FXML
    void initialize(){
        Utility.setupNotificationPane(notificationPane, vbHolder);
        Utility.restrictInputNum(tfQuantity);
        loadData();
        cbProduct.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null)labelAvailable.setText(String.valueOf(newValue.getQuantity()));
        });
        btnAdd.setOnAction(event -> save(1));
        btnMinus.setOnAction(event -> save(-1));
    }

    private void loadData(){
        Call<Product[]> getProducts = apiService.products();
        getProducts.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Product[]> call, Response<Product[]> response) {
                Platform.runLater(()->{
                    if (response.isSuccessful()) cbProduct.setItems(FXCollections.observableArrayList(response.body()));
                    else notificationPane.show(response.message());
                });
            }

            @Override
            public void onFailure(Call<Product[]> call, Throwable throwable) {
                Platform.runLater(()->notificationPane.show(throwable.getMessage()));
            }
        });
    }

    private void save(int i) {
        String error = "";
        Product product = cbProduct.getValue();
        int quantity = 0;
        if (product == null) error += "Select a product.";
        try {
            quantity = Integer.parseInt(tfQuantity.getText().trim());
            if (quantity == 0) throw new Exception();
        } catch (Exception e){
            error = "Enter a valid quantity.";
        }
        if (!error.equals("")) {
            notificationPane.show(error); return;
        }
        Call<Product[]> call = apiService.addStock(user.getId(), product.getId(), quantity*i);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Product[]> call, Response<Product[]> response) {
                Platform.runLater(()->{
                    if (response.isSuccessful()) {
                        if (dataInterface != null ) dataInterface.updateData("Quantity updated successfully", response.body());
                        Utility.closeWindow(vbHolder);
                    }else notificationPane.show(response.message());
                });
            }

            @Override
            public void onFailure(Call<Product[]> call, Throwable throwable) {
                Platform.runLater(()->notificationPane.show(throwable.getMessage()));
            }
        });
    }

    public void setDataInterface(HomeDataInterface dataInterface) {
        this.dataInterface = dataInterface;
    }
}
