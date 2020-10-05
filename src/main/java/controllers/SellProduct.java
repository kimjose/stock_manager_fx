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
import models.auth.User;
import models.finance.Bank;
import models.products.ExpressSale;
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

import java.net.URL;
import java.util.ResourceBundle;

public class SellProduct implements Initializable {

    public ComboBox<Warehouse> cbWarehouse;
    @FXML
    private VBox vbParent;

    @FXML
    private VBox vbHolder;

    @FXML
    private Label labelProduct;

    @FXML
    private TextField tfPrice;

    @FXML
    private Label labelAvailable;

    @FXML
    private TextField tfQuantity;

    @FXML
    private ComboBox<Bank> cbBank;

    @FXML
    private TextField tfRefNo;

    @FXML
    private Button btnSell;

    @FXML
    private Button btnCancel;

    @FXML
    private NotificationPane notificationPane;

    private Product product;
    private ApiService apiService;
    private HomeDataInterface dataInterface;
    private final User user = SessionManager.INSTANCE.getUser();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Utility.setupNotificationPane(notificationPane, vbHolder);
        Utility.restrictInputNum(tfQuantity);
        Utility.restrictInputDec(tfPrice);
        apiService = RetrofitBuilder.createService(ApiService.class);
        loadData();

        btnCancel.setOnAction(event -> Utility.closeWindow(vbParent));
        btnSell.setOnAction(event -> sell());
        cbWarehouse.setOnAction(event -> {
            Warehouse warehouse = cbWarehouse.getValue();
            for (Warehouse.ProductsAvailable a: warehouse.getAvailables()) {
                if (a.getId() == product.getId()) labelAvailable.setText(String.valueOf(a.getQuantity()));
            }
        });
        Platform.runLater(()->Utility.setLogo(vbParent));

        //function keys
        vbParent.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            KeyCode keyCode = event.getCode();
            if (keyCode.equals(KeyCode.F10)) {
                sell();
            }
            else if (keyCode.equals(KeyCode.F9)) Utility.closeWindow(vbHolder);
            else if (keyCode.equals(KeyCode.F5)) loadData();
        });
    }
    private void loadData(){
        Call<Bank[]> getBanks = apiService.banks();
        getBanks.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Bank[]> call, Response<Bank[]> response) {
                if (response.isSuccessful()) {
                    Platform.runLater(() -> {
                        cbBank.setItems(FXCollections.observableArrayList(response.body()));
                    });
                } else notificationPane.show(response.message());
            }

            @Override
            public void onFailure(Call<Bank[]> call, Throwable throwable) {
                notificationPane.show(throwable.getMessage());
            }
        });
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
    private void sell(){
        String sp = tfPrice.getText(),
                q = tfQuantity.getText(),
                error = "",
                refNo = tfRefNo.getText();
        double price = 0;
        int quantity = 0;
        Bank bank = cbBank.getValue();
        Warehouse warehouse = cbWarehouse.getValue();
        if (sp.equals("")) error += "Enter a valid price";
        else {
            try {
                price = Double.parseDouble(sp);
                if (price <= 0) throw new Exception();
            } catch (Exception e){
                e.printStackTrace();
                error += "Enter a valid price";
            }
        }
        if (q.equals("")) error += error.equals("")?"Enter a valid quantity.":"\nEnter a valid quantity.";
        else {
            try{
                quantity = Integer.parseInt(q);
                if (quantity <= 0) throw new Exception();
            } catch (Exception e) {
                e.printStackTrace();
                error += error.equals("")?"Enter a valid quantity.":"\nEnter a valid quantity.";
            }
        }
        if (warehouse == null) error += error.equals("")?"Select a warehouse.":"\nSelect a warehouse.";
        else {
            int available = Integer.parseInt(labelAvailable.getText());
            if ( quantity>available) error += error.equals("")?"Products are not available.":"\nProducts are not available";
        }
        if (bank == null) error += error.equals("")?"Select a valid bank.":"\nSelect a valid bank.";
        else if (!bank.isEnabled()) error += error.equals("")?"Selected bank has been blocked.":"\nSelected bank has been blocked.";
        else if (bank.isRequireRefNo() && refNo.equals("")){
            error += error.equals("")?"Reference number is required.":"\nReference number is required.";
        }
        if (!error.equals("")){
            notificationPane.show(error);return;
        }
        Call<ExpressSale[]> call = apiService.sellProduct(user.getId(), product.getId(), price, quantity, bank.getId(), refNo);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ExpressSale[]> call, Response<ExpressSale[]> response) {
                Platform.runLater(()->{
                    if (response.isSuccessful()) dataInterface.updateData("Product(s) have been sold", response.body());
                    else dataInterface.updateData("Failed."+response.message(), null);
                    Utility.closeWindow(vbHolder);
                });
            }

            @Override
            public void onFailure(Call<ExpressSale[]> call, Throwable throwable) {
                Platform.runLater(()->{
                    dataInterface.updateData("Failed."+throwable.getMessage(), null);
                    Utility.closeWindow(vbHolder);
                });
            }
        });
    }

    public void setProduct(Product product) {
        this.product = product;
        labelProduct.setText(product.getName());
        tfPrice.setText(String.valueOf(product.getSellingPrice()));
        tfQuantity.setText("1");
    }

    public void setDataInterface(HomeDataInterface dataInterface) {
        this.dataInterface = dataInterface;
    }
}
