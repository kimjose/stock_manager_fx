package controllers;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import interfaces.HomeDataInterface;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import models.customers.Customer;
import models.customers.Invoice;
import models.products.Warehouse;
import network.ApiService;
import network.RetrofitBuilder;
import org.controlsfx.control.NotificationPane;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import utils.Utility;

import java.net.URL;
import java.util.ResourceBundle;

public class CustomerInvoice implements Initializable {

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
    private ComboBox<Customer> cbOwner;

    @FXML
    private ComboBox<Warehouse> cbWarehouse;

    @FXML
    private DatePicker dpDate;

    @FXML
    private Label labelTotal;

    @FXML
    private ComboBox<?> cbType;

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
    private Invoice invoice;
    private HomeDataInterface dataInterface;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Utility.setupNotificationPane(notificationPane, vbHolder);
        apiService = RetrofitBuilder.createService(ApiService.class);

        Platform.runLater(()->{
            labelOwner.setText("Customer");
            cbOwner.setPromptText("Select Customer");
            loadData();
        });


        btnSave.setOnAction(event -> save());
        btnPost.setOnAction(event -> post());
        btnReverse.setOnAction(event -> reverse());
        btnCancel.setOnAction(event -> Utility.closeWindow(vbHolder));
        btnAddLine.setOnAction(event -> addLine());
    }

    private void loadData(){
        Call<Customer[]> getCustomers = apiService.customers();
        Call<Warehouse[]> getWarehouses = apiService.warehouses();
        getCustomers.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Customer[]> call, Response<Customer[]> response) {
                if (response.isSuccessful()) cbOwner.setItems(FXCollections.observableArrayList(response.body()));
                else notificationPane.show(response.message());
            }

            @Override
            public void onFailure(Call<Customer[]> call, Throwable throwable) {
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
    private void save(){
        String invNo = tfNo.getText().trim();
        Customer customer = cbOwner.getValue();
        Warehouse warehouse = cbWarehouse.getValue();
        String date = "";
        String errorMessage = "";
        if (invNo.equals("")) errorMessage += "Invoice number is required.";
        if (customer == null) errorMessage += errorMessage.equals("")?"Select a valid customer":"\nSelect a valid customer";
        if (warehouse == null) errorMessage += errorMessage.equals("")?"Select a valid warehouse":"\nSelect a valid warehouse";
        try {
            date = dpDate.getValue().toString();
            if (date.equals("")) throw new Exception();
        }catch (Exception e){
            errorMessage += errorMessage.equals("")?"The invoice date is invalid.":"The invoice date is invalid";
        }
        if (!errorMessage.equals("")){
            notificationPane.show(errorMessage);return;
        }
        Call<Invoice[]> call;
        if (invoice == null){
            call = apiService.addCustomerInvoice(invNo, customer.getId(), warehouse.getId(), date, 1);
        }else{}
    }
    private void post(){}
    private void reverse(){}
    private void addLine(){}


    public void setDataInterface(HomeDataInterface dataInterface) {
        this.dataInterface = dataInterface;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
        tfNo.setText(invoice.getInvoiceNo());
        btnAddLine.setDisable(invoice.isPosted());
        btnSave.setDisable(invoice.isPosted());
        btnPost.setDisable(invoice.isPosted());
        btnReverse.setDisable(!invoice.isPosted() && invoice.isReversed());
    }
}
