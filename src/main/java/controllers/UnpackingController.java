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
import models.products.Product;
import models.products.ProductGroup;
import models.products.Unpacking;
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

public class UnpackingController implements Initializable {

    @FXML
    private VBox vbParent;

    @FXML
    private NotificationPane notificationPane;

    @FXML
    private VBox vbHolder;

    @FXML
    private Label labelId;

    @FXML
    private ComboBox<ProductGroup> cbGroup;

    @FXML
    private TextField tfQuantity;

    @FXML
    private TextField tfPQuantity;

    @FXML
    private ComboBox<Warehouse> cbWarehouse;

    @FXML
    private Button btnCancel;

    @FXML
    private Button btnSave;

    @FXML
    private Button btnPost;

    @FXML
    private Button btnReverse;

    private ApiService apiService;
    private Unpacking unpacking;
    private HomeDataInterface dataInterface;
    private final User user = SessionManager.INSTANCE.getUser();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Utility.setupNotificationPane(notificationPane, vbHolder);
        apiService = RetrofitBuilder.createService(ApiService.class);
        Utility.restrictInputNum(tfQuantity);

        tfQuantity.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals("")){
                int quantity = Integer.parseInt(newValue);
                ProductGroup group = cbGroup.getValue();
                assert group != null : "Group is null.";
                if (group == null ) return;
                int products = group.getQuantity() * quantity;
                tfPQuantity.setText(String.valueOf(products));
            }
        });

        btnSave.setOnAction(event -> save());
        btnCancel.setOnAction(event -> Utility.closeWindow(vbHolder));
        btnPost.setOnAction(event -> post());
        btnReverse.setOnAction(event -> reverse());

        loadData();
    }

    private void loadData(){
        Call<ProductGroup[]> getGroups = apiService.productGroups();
        Call<Warehouse[]> getWarehouses = apiService.warehouses();
        getGroups.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ProductGroup[]> call, Response<ProductGroup[]> response) {
                Platform.runLater(() -> {
                    if (response.isSuccessful()) {
                        cbGroup.setItems(FXCollections.observableArrayList(response.body()));
                        if (unpacking != null) cbGroup.getSelectionModel().select(unpacking.getGroup());
                    } else notificationPane.show(response.message());
                });
            }

            @Override
            public void onFailure(Call<ProductGroup[]> call, Throwable throwable) {
                Platform.runLater(() -> notificationPane.show(throwable.getMessage()));
            }
        });
        getWarehouses.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Warehouse[]> call, Response<Warehouse[]> response) {
                Platform.runLater(() -> {
                    if (response.isSuccessful()) {
                        cbWarehouse.setItems(FXCollections.observableArrayList(response.body()));
                        if (unpacking != null) cbWarehouse.getSelectionModel().select(unpacking.getWarehouse());
                    } else notificationPane.show(response.message());
                });
            }

            @Override
            public void onFailure(Call<Warehouse[]> call, Throwable throwable) {
                Platform.runLater(() -> notificationPane.show(throwable.getMessage()));
            }
        });
    }

    private void save(){
        Call<Unpacking[]> call;
        ProductGroup group = null;
        Warehouse warehouse = null;
        String q = tfQuantity.getText().trim();
        String pq = tfPQuantity.getText().trim();
        int quantity = 0, products = 0;
        String errorMessage = "";
        try{
            group = cbGroup.getValue();
        }catch (Exception e){
            errorMessage = "Select a valid product group";
        }
        if (q.equals("")){
            errorMessage += errorMessage.equals("")?"Enter a valid quantity.":"\nEnter a valid quantity.";
        } else {
            try{
                quantity = Integer.parseInt(q);
                products = quantity * group.getQuantity();
            }catch (Exception e){
                errorMessage += errorMessage.equals("")?"Enter a valid quantity.":"\nEnter a valid quantity.";
            }
        }
        try {
            warehouse = cbWarehouse.getValue();
        } catch (Exception e) {
            errorMessage += errorMessage.equals("")?"Select a valid warehouse.":"\nSelect a valid warehouse.";
        }
        if (!errorMessage.equals("")) {
            notificationPane.show(errorMessage); return;
        }
        if (unpacking != null) call = apiService.updateUnpacking(unpacking.getId(), group.getId(), quantity, products, warehouse.getId());
        else call = apiService.addUnpacking(group.getId(), quantity, products, warehouse.getId(), user.getId());
        call.enqueue(new Callback<Unpacking[]>() {
            @Override
            public void onResponse(Call<Unpacking[]> call, Response<Unpacking[]> response) {
                Platform.runLater(()->{
                    if (response.isSuccessful()){
                        if (dataInterface != null ){
                            Utility.closeWindow(vbHolder);
                            dataInterface.updateData("Unpacking has been saved.", response.body());
                        }
                    }else Platform.runLater(()->notificationPane.show(Utility.handleApiErrors(response.message(), response.errorBody(),
                            new String[]{"groupId", "quantity", "productQuantity", "warehouseId", "createdBy"})));
                });
            }

            @Override
            public void onFailure(Call<Unpacking[]> call, Throwable throwable) {
                Platform.runLater(()->notificationPane.show(throwable.getMessage()));
            }
        });
    }
    private void post(){
        if (unpacking == null) return;
        Call<Unpacking[]> call = apiService.postUnpacking(unpacking.getId(), user.getId());
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Unpacking[]> call, Response<Unpacking[]> response) {
                Platform.runLater(()->{
                    if (response.isSuccessful()){
                        if (dataInterface != null ){
                            Utility.closeWindow(vbHolder);
                            dataInterface.updateData("Unpacking has been posted.", response.body());
                        }
                    } else Platform.runLater(()->notificationPane.show(response.message()));
                });
            }

            @Override
            public void onFailure(Call<Unpacking[]> call, Throwable throwable) {
                Platform.runLater(()->notificationPane.show(throwable.getMessage()));
            }
        });
    }
    private void reverse(){
        if (unpacking == null) return;
        Call<Unpacking[]> call = apiService.reverseUnpacking(unpacking.getId(), user.getId());
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Unpacking[]> call, Response<Unpacking[]> response) {
                Platform.runLater(()->{
                    if (response.isSuccessful()){
                        if (dataInterface != null ){
                            Utility.closeWindow(vbHolder);
                            dataInterface.updateData("Unpacking has been reversed.", response.body());
                        }
                    } else Platform.runLater(()->notificationPane.show(response.message()));
                });
            }

            @Override
            public void onFailure(Call<Unpacking[]> call, Throwable throwable) {
                Platform.runLater(()->notificationPane.show(throwable.getMessage()));
            }
        });
    }

    public void setDataInterface(HomeDataInterface dataInterface) {
        this.dataInterface = dataInterface;
    }

    public void setUnpacking(Unpacking unpacking) {
        this.unpacking = unpacking;
        labelId.setText(String.valueOf(unpacking.getId()));
        tfQuantity.setText(String.valueOf(unpacking.getQuantity()));
        tfPQuantity.setText(String.valueOf(unpacking.getProductQuantity()));
        if (unpacking.isPosted()){
            btnSave.setDisable(true);
            btnPost.setDisable(true);
            btnReverse.setDisable(unpacking.isReversed());
        }
    }
}
