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
import javafx.scene.layout.VBox;
import models.auth.User;
import models.finance.Bank;
import models.products.ExpressSale;
import models.products.Product;
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
    private User user = SessionManager.INSTANCE.getUser();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Utility.setupNotificationPane(notificationPane, vbHolder);
        Utility.restrictInputNum(tfQuantity);
        Utility.restrictInputDec(tfPrice);
        apiService = RetrofitBuilder.createService(ApiService.class);
        loadData();

        btnCancel.setOnAction(event -> Utility.closeWindow(vbParent));
        btnSell.setOnAction(event -> sell());
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
    }
    private void sell(){
        String sp = tfPrice.getText(),
                q = tfQuantity.getText(),
                error = "",
                refNo = tfRefNo.getText();
        double price = 0;
        int quantity = 0;
        Bank bank = cbBank.getValue();
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
        labelAvailable.setText(String.valueOf(product.getQuantity()));
    }

    public void setDataInterface(HomeDataInterface dataInterface) {
        this.dataInterface = dataInterface;
    }
}
