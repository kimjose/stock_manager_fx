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
import models.products.Warehouse;
import network.ApiService;
import network.RetrofitBuilder;
import org.controlsfx.control.NotificationPane;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import utils.SessionManager;
import utils.Utility;

public class AddToStock {

    public ComboBox<Warehouse> cbWarehouse;
    public Label labelProduct;
    @FXML
    private VBox vbParent;

    @FXML
    private NotificationPane notificationPane;

    @FXML
    private VBox vbHolder;

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
    private Product product;

    @FXML
    void initialize(){

        Utility.setupNotificationPane(notificationPane, vbHolder);
        Utility.restrictInputNum(tfQuantity);
        loadData();
        btnAdd.setOnAction(event -> save(1));
        btnMinus.setOnAction(event -> save(-1));
        cbWarehouse.setOnAction(event -> {
            Warehouse warehouse = cbWarehouse.getValue();
            for (Warehouse.ProductsAvailable a: warehouse.getAvailables()) {
                if (a.getId() == product.getId()) labelAvailable.setText(String.valueOf(a.getQuantity()));
            }
        });
        Platform.runLater(()->Utility.setLogo(vbParent));
    }

    private void loadData(){
        Call<Warehouse[]> call = apiService.warehouses();
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Warehouse[]> call, Response<Warehouse[]> response) {
                Platform.runLater(()->{
                    if (response.isSuccessful()){
                        cbWarehouse.setItems(FXCollections.observableArrayList(response.body()));
                    }else notificationPane.show(response.message()+". Re-open this window.");
                });
            }

            @Override
            public void onFailure(Call<Warehouse[]> call, Throwable throwable) {
                Platform.runLater(()->notificationPane.show(throwable.getMessage()+". Re-open this window."));
            }
        });
    }

    private void save(int i) {
        String error = "";
        int quantity = 0;
        Warehouse warehouse = cbWarehouse.getValue();
        try {
            quantity = Integer.parseInt(tfQuantity.getText().trim());
            if (quantity == 0) throw new Exception();
        } catch (Exception e){
            error += "Enter a valid quantity.";
        }
        if (warehouse == null) error += error.equals("")?"Select a warehouse.":"\nSelect a warehouse.";
        else {
            int available = Integer.parseInt(labelAvailable.getText());
            if (i < 0 && quantity>available) error += error.equals("")?"Final amount must not be less than 0.":"\nFinal amount must not be less than 0.";
        }
        if (!error.equals("")) {
            notificationPane.show(error); return;
        }
        Call<Product[]> call = apiService.addStock(user.getId(), product.getId(), warehouse.getId(), quantity*i);
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

    public void setProduct(Product product) {
        this.product = product;
        labelProduct.setText(product.getName());
    }
}
