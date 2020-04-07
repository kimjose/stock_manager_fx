package controllers;

import interfaces.HomeDataInterface;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import models.products.Warehouse;
import models.vendors.Invoice;
import models.vendors.Vendor;
import network.ApiService;
import network.RetrofitBuilder;
import org.controlsfx.control.NotificationPane;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import utils.Utility;

import java.net.URL;
import java.util.ResourceBundle;

public class VendorInvoice implements Initializable {

    @FXML
    private VBox vbParent;

    @FXML
    private NotificationPane notificationPane;

    @FXML
    private VBox vbHolder;

    @FXML
    private TextField tfNo;

    @FXML
    private Label labelOwner;

    @FXML
    private ComboBox<Vendor> cbOwner;

    @FXML
    private ComboBox<Warehouse> cbWarehouse;

    @FXML
    private DatePicker dpDate;

    @FXML
    private Label labelTotal;

    @FXML
    private ComboBox<String> cbType;

    @FXML
    private TextField tfDescription;

    @FXML
    private TextField tfUnitPrice;

    @FXML
    private TextField tfQuantity;

    @FXML
    private Button btnAddLine;

    @FXML
    private TableView<?> tvLines;

    @FXML
    private TableColumn<?, ?> tcPS;

    @FXML
    private TableColumn<?, ?> tcPrice;

    @FXML
    private TableColumn<?, ?> tcQuantity;

    @FXML
    private TableColumn<?, ?> tcTotal;

    @FXML
    private TableColumn<?, ?> tcRemove;

    @FXML
    private Button btnSave;

    @FXML
    private Button btnCancel;

    @FXML
    private Button btnReverse;

    @FXML
    private Button btnPost;

    private ApiService apiService;
    private HomeDataInterface dataInterface;
    private Invoice invoice;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        apiService = RetrofitBuilder.createService(ApiService.class);
        Utility.setupNotificationPane(notificationPane, vbHolder);
        String[] typeValues = {"product","service"};

        Platform.runLater(()->{
            labelOwner.setText("Vendor");
            cbOwner.setPromptText("Select Vendor");
            cbType.setItems(FXCollections.observableArrayList(typeValues));
            loadData();
        });

        btnSave.setOnAction(event -> save());
        btnPost.setOnAction(event -> post());
        btnReverse.setOnAction(event -> reverse());
        btnCancel.setOnAction(event -> Utility.closeWindow(vbHolder));
        btnAddLine.setOnAction(event -> addLine());
    }

    private void save(){
        String invNo = tfNo.getText().trim();
        String date = null;
        Vendor vendor = cbOwner.getValue();
        Warehouse warehouse = cbWarehouse.getValue();
        String errorMessage = "";
        boolean error = false;
        if (invNo.equals("")) errorMessage = errorMessage.concat("Invoice Number is required.");
        try{
            date = dpDate.getValue().toString();
            if (date == null ) throw new Exception();
        }catch (Exception e) {
            errorMessage = errorMessage.concat(errorMessage.equals("")?"Select a valid date":"\nSelect a valid date");
        }
        if (vendor == null) errorMessage = errorMessage.concat(errorMessage.equals("")?"Select a valid vendor":"\nSelect a valid vendor");
        if (warehouse == null) errorMessage = errorMessage.concat(errorMessage.equals("")?"Select a valid warehouse":"\nSelect a valid warehouse");
        if (!errorMessage.equals("")) notificationPane.show(errorMessage);
        else{
            Call<Invoice[]> call;
            if (invoice == null) call = apiService.addVendorInvoice(invNo, vendor.getId(), warehouse.getId(), date, 1);
            else{
                invNo = invNo.equals(invoice.getInvoiceNo())?null:invNo;
                call = apiService.updateVendorInvoice(invoice.getId(), invNo, vendor.getId(), warehouse.getId(), date);
            }
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(Call<Invoice[]> call, Response<Invoice[]> response) {
                    if (response.isSuccessful()) {
                        Utility.closeWindow(vbParent);
                        dataInterface.updateData("The invoice has been saved successfully", response.body());
                    } else notificationPane.show(response.message());
                }

                @Override
                public void onFailure(Call<Invoice[]> call, Throwable throwable) {
                    notificationPane.show(throwable.getMessage());
                }
            });
        }
    }
    private void post(){}
    private void reverse(){}
    private void addLine(){}

    private void loadData(){
        Call<Vendor[]> getVendors = apiService.vendors();
        Call<Warehouse[]> getWarehouses = apiService.warehouses();
        getVendors.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Vendor[]> call, Response<Vendor[]> response) {
                if (response.isSuccessful()){
                    Platform.runLater(()->{
                        cbOwner.setItems(FXCollections.observableArrayList(response.body()));
                        if (invoice!=null) cbOwner.getSelectionModel().select(invoice.getVendor());
                    });
                }else notificationPane.show(response.message());
            }

            @Override
            public void onFailure(Call<Vendor[]> call, Throwable throwable) {
                notificationPane.show(throwable.getMessage());
            }
        });
        getWarehouses.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Warehouse[]> call, Response<Warehouse[]> response) {
                if (response.isSuccessful()){
                    Platform.runLater(()->{
                        cbWarehouse.setItems(FXCollections.observableArrayList(response.body()));
                        if (invoice!=null) cbWarehouse.getSelectionModel().select(invoice.getWarehouse());
                    });
                }else notificationPane.show(response.message());
            }

            @Override
            public void onFailure(Call<Warehouse[]> call, Throwable throwable) {
                notificationPane.show(throwable.getMessage());
            }
        });
    }


    public void setDataInterface(HomeDataInterface dataInterface) {
        this.dataInterface = dataInterface;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
        tfNo.setText(invoice.getInvoiceNo());

    }
}
