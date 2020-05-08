package controllers;

import interfaces.HomeDataInterface;
import interfaces.LinesInterface;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import models.auth.User;
import models.products.Product;
import models.products.Service;
import models.products.Warehouse;
import models.vendors.Invoice;
import models.vendors.Vendor;
import network.ApiService;
import network.RetrofitBuilder;
import org.controlsfx.control.NotificationPane;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import utils.SessionManager;
import utils.Utility;

import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import models.vendors.*;

public class VendorInvoice implements Initializable, LinesInterface {

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
    private ComboBox<Object> cbPS;

    @FXML
    private TextField tfDescription;

    @FXML
    private TextField tfUnitPrice;

    @FXML
    private TextField tfQuantity;

    @FXML
    private Button btnAddLine;

    @FXML
    private TableView<InvoiceLine> tvLines;

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
    private Service[] services;
    private Product[] products;
    final User user = SessionManager.INSTANCE.getUser();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        apiService = RetrofitBuilder.createService(ApiService.class);
        Utility.setupNotificationPane(notificationPane, vbHolder);
        String[] typeValues = {"Product","Service"};

        Platform.runLater(()->{
            labelOwner.setText("Vendor");
            cbOwner.setPromptText("Select Vendor");
            cbType.setItems(FXCollections.observableArrayList(typeValues));

            Utility.restrictInputDec(tfUnitPrice);
            Utility.restrictInputNum(tfQuantity);

            if (invoice!=null){
                tfNo.setText(invoice.getInvoiceNo());
                btnAddLine.setDisable(invoice.isPosted());
                btnSave.setDisable(invoice.isPosted());
                btnPost.setDisable(invoice.isPosted());
                btnReverse.setDisable(!invoice.isPosted() && invoice.isReversed());
                setLines(invoice.getInvoiceLines());
                dpDate.setValue(LocalDate.parse(invoice.getInvoiceDate()));
                tvLines.setDisable(invoice.isPosted());
            }else{
                btnPost.setDisable(true);
                btnAddLine.setDisable(true);
            }

            cbType.setOnAction(event -> {
                Task<Object> myTask = new Task<>() {
                    @Override
                    protected Object call() {
                        System.out.println(cbType.getValue());
                        if (products==null||services==null) {
                            loadData();
                            try {
                                Thread.currentThread().wait();
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                e.printStackTrace();
                            }
                        }
                        String type = cbType.getValue();
                        Platform.runLater(()->{
                            if (type.equals("Product")) cbPS.setItems(FXCollections.observableArrayList(products));
                            else if(type.equals("Service")) cbPS.setItems(FXCollections.observableArrayList(services));
                            cbPS.getSelectionModel().select(0);
                        });
                        return null;
                    }
                };
                Thread thread = new Thread(myTask);
                thread.setDaemon(true);
                thread.start();
            });

            tcPS.setCellValueFactory(new PropertyValueFactory<>("name"));
            tcPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
            tcQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
            tcTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
            tcRemove.setCellValueFactory(new PropertyValueFactory<>("removeLine"));


            loadData();
        });

        btnSave.setOnAction(event -> save());
        btnPost.setOnAction(event -> post());
        btnReverse.setOnAction(event -> reverse());
        btnCancel.setOnAction(event -> Utility.closeWindow(vbHolder));
        btnAddLine.setOnAction(event -> addLine());
    }


    @Override
    public void updateData(Object[] lines) {
        setLines((InvoiceLine[]) lines);
    }

    @Override
    public void notifyError(String errorMessage) {
        notificationPane.show(errorMessage);
    }

    private void save(){
        String invNo = tfNo.getText().trim();
        String date = null;
        Vendor vendor = cbOwner.getValue();
        Warehouse warehouse = cbWarehouse.getValue();
        String errorMessage = "";
        //if (invNo.equals("")) errorMessage = errorMessage.concat("Invoice Number is required.");
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
            if (invoice == null) {
                assert vendor != null;
                assert warehouse != null;
                call = apiService.addVendorInvoice(vendor.getId(), warehouse.getId(), date, user.getId());
            }
            else{
                invNo = invNo.equals(invoice.getInvoiceNo())?null:invNo;
                assert vendor != null;
                assert warehouse != null;
                call = apiService.updateVendorInvoice(invoice.getId(), vendor.getId(), warehouse.getId(), date);
            }
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(Call<Invoice[]> call, Response<Invoice[]> response) {
                    if (response.isSuccessful()) {
                        Platform.runLater(()->{
                            Utility.closeWindow(vbParent);
                            List<Invoice> filtered = new ArrayList<>();
                            for (Invoice i:response.body()) {
                                if (!i.isPosted())filtered.add(i);
                            }
                            dataInterface.updateData("The invoice has been saved successfully", filtered.toArray());
                        });
                    } else{
                        Platform.runLater(() -> {
                            assert response.errorBody() != null;
                            notificationPane.show(Utility.handleApiErrors(response.message(), response.errorBody(), new String[]{"invoiceNo"}));
                        });
                    }
                }

                @Override
                public void onFailure(Call<Invoice[]> call, Throwable throwable) {
                    notificationPane.show(throwable.getMessage());
                }
            });
        }
    }
    private void post(){
        Call<Invoice[]> call = apiService.postVendorInvoice(invoice.getId(), user.getId());
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Invoice[]> call, Response<Invoice[]> response) {
                if (response.isSuccessful()){
                    if (dataInterface!=null){
                        Platform.runLater(()->{
                            Utility.closeWindow(vbHolder);
                            dataInterface.updateData("The invoice has been posted successfully", response.body());
                        });
                    }
                }else Platform.runLater(()->notificationPane.show(Utility.handleApiErrors(response.message(), response.errorBody(),
                        new String[]{"message"})));
            }

            @Override
            public void onFailure(Call<Invoice[]> call, Throwable throwable) {
                Platform.runLater(()->notificationPane.show(throwable.getMessage()));
            }
        });
    }
    private void reverse(){
        Call<Invoice[]> call = apiService.reverseVendorInvoice(invoice.getId(), user.getId());
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Invoice[]> call, Response<Invoice[]> response) {
                if (response.isSuccessful()){
                    if (dataInterface!=null){
                        Platform.runLater(()->{
                            Utility.closeWindow(vbHolder);
                            dataInterface.updateData("The invoice has been reversed successfully", response.body());
                        });
                    }
                }else Platform.runLater(()->notificationPane.show(Utility.handleApiErrors(response.message(), response.errorBody(),
                        new String[]{"message"})));
            }

            @Override
            public void onFailure(Call<Invoice[]> call, Throwable throwable) {
                Platform.runLater(()->notificationPane.show(throwable.getMessage()));
            }
        });
    }
    private void addLine(){
        String errorMessage = "";
        String type = cbType.getValue();
        Object object = cbPS.getValue();
        int typeId = type.equals("Product")?((Product)object).getId():((Service)object).getId();
        String description = tfDescription.getText();
        String unitPrice = tfUnitPrice.getText();
        String quantity = tfQuantity.getText();


        if (type.equals(""))errorMessage = errorMessage.concat("Select a valid type.");
        if (object == null){
            errorMessage = errorMessage.concat(errorMessage.equals("")?"Unit price is required":"\nUnit price is required");
        }
        if (unitPrice.equals("")){
            errorMessage = errorMessage.concat(errorMessage.equals("")?"Unit price is required":"\nUnit price is required");
        }
        if (quantity.equals("")){
            errorMessage = errorMessage.concat(errorMessage.equals("")?"Quantity is required":"\nQuantity is required");
        }
        if (!errorMessage.equals("")){
            notificationPane.show(errorMessage);return;
        }

        double price = Double.parseDouble(unitPrice);
        int q = Integer.parseInt(quantity);

        Call<InvoiceLine[]> call = apiService.addVendorInvoiceLine(invoice.getId(), type, typeId, price, q, description);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<InvoiceLine[]> call, Response<InvoiceLine[]> response) {
                assert response.body() != null;
                if (response.isSuccessful()) {
                    setLines(response.body());
                    Platform.runLater(()->{
                        tfDescription.setText("");
                        tfUnitPrice.setText("");
                        tfQuantity.setText("");
                    });
                }
                else {
                    assert response.errorBody() != null;
                    notificationPane.show(Utility.handleApiErrors(response.message(), response.errorBody(),
                            new String[]{"invId","type","typeId","description"}));
                }
            }

            @Override
            public void onFailure(Call<InvoiceLine[]> call, Throwable throwable) {
                notificationPane.show(throwable.getMessage());
            }
        });
    }

    private void loadData(){
        Call<Vendor[]> getVendors = apiService.vendors();
        Call<Warehouse[]> getWarehouses = apiService.warehouses();
        Call<Product[]> getProducts = apiService.products();
        Call<Service[]> getServices = apiService.services();
        getVendors.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Vendor[]> call, Response<Vendor[]> response) {
                if (response.isSuccessful()){
                    Platform.runLater(()->{
                        cbOwner.setItems(FXCollections.observableArrayList(response.body()));
                        if (invoice!=null)
                            for (Vendor v: response.body()) {
                                if (v.getId() == invoice.getVendor().getId()) cbOwner.getSelectionModel().select(v);
                            }
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
        getProducts.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Product[]> call, Response<Product[]> response) {
                if (response.isSuccessful()) products = response.body();
                else Platform.runLater(()->notificationPane.show(response.message()));
            }

            @Override
            public void onFailure(Call<Product[]> call, Throwable throwable) {
                notificationPane.show(throwable.getMessage());
            }
        });
        getServices.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Service[]> call, Response<Service[]> response) {
                if (response.isSuccessful()) services = response.body();
                else notificationPane.show(response.message());
            }

            @Override
            public void onFailure(Call<Service[]> call, Throwable throwable) {
                notificationPane.show(throwable.getMessage());
            }
        });
    }

    private void setLines(InvoiceLine[] lines){
        double invTotal = 0;
        for (InvoiceLine line: lines) {
            line.setLinesInterface(this);
            invTotal += line.getTotal();
            System.out.println(line.getName());
        }
        invoice.setInvoiceLines(lines);
        invoice.setTotal(invTotal);
        Platform.runLater(()->{
            labelTotal.setText(String.valueOf(invoice.getTotal()));
            tvLines.setItems(FXCollections.observableArrayList(lines));
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