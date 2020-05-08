package controllers;


import interfaces.HomeDataInterface;
import interfaces.LinesInterface;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import models.auth.User;
import models.customers.Customer;
import models.customers.Invoice;
import models.customers.InvoiceLine;
import models.products.Product;
import models.products.Service;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class CustomerInvoice implements Initializable, LinesInterface {

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
    private Invoice invoice;
    private HomeDataInterface dataInterface;
    private final User user = SessionManager.INSTANCE.getUser();
    private Service[] services;
    private Product[] products;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Utility.setupNotificationPane(notificationPane, vbHolder);
        apiService = RetrofitBuilder.createService(ApiService.class);

        Platform.runLater(()->{
            labelOwner.setText("Customer");
            cbOwner.setPromptText("Select Customer");
            Utility.restrictInputDec(tfUnitPrice);
            Utility.restrictInputNum(tfQuantity);

            String[] types = {"Product", "Service"};
            cbType.setItems(FXCollections.observableArrayList(types));
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

        if (invoice!=null){
            tfNo.setText(invoice.getInvoiceNo());
            btnAddLine.setDisable(invoice.isPosted());
            btnSave.setDisable(invoice.isPosted());
            btnPost.setDisable(invoice.isPosted());
            btnReverse.setDisable(!invoice.isPosted() && invoice.isReversed());
            setLines(invoice.getInvoiceLines());
            dpDate.setValue(LocalDate.parse(invoice.getInvoiceDate()));
        }


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

    private void loadData(){
        Call<Customer[]> getCustomers = apiService.customers();
        Call<Warehouse[]> getWarehouses = apiService.warehouses();
        Call<Product[]> getProducts = apiService.products();
        Call<Service[]> getServices = apiService.services();
        getCustomers.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Customer[]> call, Response<Customer[]> response) {
                if (response.isSuccessful()){
                    cbOwner.setItems(FXCollections.observableArrayList(response.body()));
                    Platform.runLater(()->{
                        for (Customer c: response.body()) {
                            if (invoice != null) {
                                if (c.getId() == invoice.getCustomer().getId()) cbOwner.getSelectionModel().select(c);
                            }
                        }
                    });
                }
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
        getProducts.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Product[]> call, Response<Product[]> response) {
                if (response.isSuccessful()) products = response.body();
                else notificationPane.show(response.message());
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
    private void save(){
        String invNo = tfNo.getText().trim();
        Customer customer = cbOwner.getValue();
        Warehouse warehouse = cbWarehouse.getValue();
        String date = "";
        String errorMessage = "";
        //if (invNo.equals("")) errorMessage += "Invoice number is required.";
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
            call = apiService.addCustomerInvoice(customer.getId(), warehouse.getId(), date, user.getId());
        }else{
            invNo = invoice.getInvoiceNo().equals(invNo)?null:invNo;
            call = apiService.updateCustomerInvoice(invoice.getId(), customer.getId(), warehouse.getId(), date);
        }
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Invoice[]> call, Response<Invoice[]> response) {
                if (response.isSuccessful()) {
                    if (dataInterface!=null){
                        Platform.runLater(()->{
                            Utility.closeWindow(vbHolder);
                            List<Invoice> filtered = new ArrayList<>();
                            for (Invoice i:response.body()) {
                                if (!i.isPosted())filtered.add(i);
                            }
                            dataInterface.updateData("The Customer invoice has been saved.", filtered.toArray());
                        });
                    }
                } else {
                    System.out.println(response.errorBody().toString());
                    notificationPane.show(Utility.handleApiErrors(response.message(), response.errorBody(),
                            new String[]{"invNo"}));
                }
            }

            @Override
            public void onFailure(Call<Invoice[]> call, Throwable throwable) {
                notificationPane.show(throwable.getMessage());
            }
        });
    }
    private void post(){
        Call<Invoice[]> call = apiService.postCustomerInvoice(invoice.getId(), user.getId());
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
        Call<Invoice[]> call = apiService.reverseCustomerInvoice(invoice.getId(), user.getId());
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

        Call<InvoiceLine[]> call = apiService.addCustomerInvoiceLine(invoice.getId(), type, typeId, price, q, description);
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


    public void setDataInterface(HomeDataInterface dataInterface) {
        this.dataInterface = dataInterface;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }



}
