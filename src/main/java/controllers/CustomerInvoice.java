package controllers;


import com.google.gson.Gson;
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
import org.controlsfx.validation.ValidationMessage;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import utils.SessionManager;
import utils.Utility;

import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
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

    private ValidationSupport vsInvoice = new ValidationSupport();
    private ValidationSupport vsLine = new ValidationSupport();


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        Utility.setupNotificationPane(notificationPane, vbHolder);
        apiService = RetrofitBuilder.createService(ApiService.class);

        Platform.runLater(() -> {
            Utility.setLogo(vbParent);
            labelOwner.setText("Customer");
            cbOwner.setPromptText("Select Customer");
            Utility.restrictInputDec(tfUnitPrice);
            Utility.restrictInputNum(tfQuantity);
            dpDate.setValue(LocalDate.now());

            String[] types = {"Product", "Service"};
            cbType.setItems(FXCollections.observableArrayList(types));
            cbType.setOnAction(event -> {
                Task<Object> myTask = new Task<>() {
                    @Override
                    protected Object call() {
                        System.out.println(cbType.getValue());
                        if (products == null || services == null) {
                            loadData();
                            try {
                                Thread.currentThread().wait();
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                e.printStackTrace();
                            }
                        }
                        String type = cbType.getValue();
                        Platform.runLater(() -> {
                            if (type.equals("Product")) cbPS.setItems(FXCollections.observableArrayList(products));
                            else if (type.equals("Service")) cbPS.setItems(FXCollections.observableArrayList(services));
                            cbPS.getSelectionModel().select(0);
                        });
                        return null;
                    }
                };
                Thread thread = new Thread(myTask);
                thread.setDaemon(true);
                thread.start();
            });

            cbPS.setOnAction(event -> {
                if (cbType.getValue().equals("Product")) {
                    Product product = (Product) cbPS.getValue();
                    tfUnitPrice.setText(String.valueOf(product.getSellingPrice()));
                    tfQuantity.setText("1");
                }
            });

            tcPS.setCellValueFactory(new PropertyValueFactory<>("name"));
            tcPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
            tcQuantity.setCellValueFactory(new PropertyValueFactory<>("quantityTf"));
            tcTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
            tcRemove.setCellValueFactory(new PropertyValueFactory<>("removeLine"));

            vsInvoice.registerValidator(cbOwner, true, Validator.createEmptyValidator("Select a valid customer."));
            vsInvoice.registerValidator(cbWarehouse, true, Validator.createEmptyValidator("Select a valid warehouse."));
            vsInvoice.registerValidator(dpDate, true, Validator.createEmptyValidator("Select a valid Date."));

            vsLine.registerValidator(cbType, true, Validator.createEmptyValidator("Select a valid type"));
            vsLine.registerValidator(tfUnitPrice, true, Validator.createEmptyValidator("Enter a valid unit price."));
            vsLine.registerValidator(tfQuantity, true, Validator.createEmptyValidator("Enter a valid quantity"));
            vsLine.registerValidator(cbPS, true, Validator.createEmptyValidator("Select a product/service"));

            loadData();
        });

        /*if (invoice != null) {
            tfNo.setText(invoice.getInvoiceNo());
            btnAddLine.setDisable(invoice.isPosted());
            btnSave.setDisable(invoice.isPosted());
            btnPost.setDisable(invoice.isPosted());
            btnReverse.setDisable(invoice.isReversed());
            setLines(invoice.getInvoiceLines());
            dpDate.setValue(LocalDate.parse(invoice.getInvoiceDate()));
            tvLines.setDisable(invoice.isPosted());
        }*/


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

    @Override
    public void updateQuantity(Object line) {
        InvoiceLine invoiceLine = (InvoiceLine) line;
        Call<InvoiceLine[]> call = apiService.addCustomerInvoiceLine(invoice.getId(), invoiceLine.getType(), invoiceLine.getTypeId(), invoiceLine.getUnitPrice(), invoiceLine.getBuyingPrice(), invoiceLine.getQuantity(), "");
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<InvoiceLine[]> call, Response<InvoiceLine[]> response) {
                assert response.body() != null;
                if (response.isSuccessful()) {
                    setLines(response.body());
                } else {
                    assert response.errorBody() != null;
                    notificationPane.show(Utility.handleApiErrors(response.message(), response.errorBody(),
                            new String[]{"invId", "type", "typeId", "description"}));
                }
            }

            @Override
            public void onFailure(Call<InvoiceLine[]> call, Throwable throwable) {
                notificationPane.show(throwable.getMessage());
            }
        });
    }

    private void setLines(InvoiceLine[] lines) {
        double invTotal = 0;
        for (InvoiceLine line : lines) {
            line.setLinesInterface(this);
            invTotal += line.getTotal();
            System.out.println(line.getName());
        }
        invoice.setInvoiceLines(lines);
        invoice.setTotal(invTotal);
        Platform.runLater(() -> {
            labelTotal.setText(String.valueOf(invoice.getTotal()));
            tvLines.setItems(FXCollections.observableArrayList(lines));
        });
    }

    private void loadData() {
        Call<Customer[]> getCustomers = apiService.customers();
        Call<Warehouse[]> getWarehouses = apiService.warehouses();
        Call<Product[]> getProducts = apiService.products();
        Call<Service[]> getServices = apiService.services();
        getCustomers.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Customer[]> call, Response<Customer[]> response) {
                if (response.isSuccessful()) {
                    cbOwner.setItems(FXCollections.observableArrayList(response.body()));
                    Platform.runLater(() -> {
                        for (Customer c : response.body()) {
                            if (invoice != null) {
                                if (c.getId() == invoice.getCustomer().getId()) cbOwner.getSelectionModel().select(c);
                            }
                        }
                    });
                } else notificationPane.show(response.message());
            }

            @Override
            public void onFailure(Call<Customer[]> call, Throwable throwable) {
                notificationPane.show(throwable.getMessage());
            }
        });
        getWarehouses.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Warehouse[]> call, Response<Warehouse[]> response) {
                if (response.isSuccessful()) {
                    Platform.runLater(() -> {
                        cbWarehouse.setItems(FXCollections.observableArrayList(response.body()));
                        if (invoice != null) cbWarehouse.getSelectionModel().select(invoice.getWarehouse());
                    });
                } else notificationPane.show(response.message());
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

    private void save() {
        ValidationResult invoiceValidationResult = vsInvoice.getValidationResult();
        Iterator<ValidationMessage> messageIterator = invoiceValidationResult.getErrors().iterator();
        StringBuilder stringBuilder = new StringBuilder();
        while (messageIterator.hasNext()) {
            stringBuilder.append(messageIterator.next().getText());
            stringBuilder.append("\n");
        }
        if (!invoiceValidationResult.getErrors().isEmpty()) {
            notificationPane.show(stringBuilder.toString());
            return;
        }
        Customer customer = cbOwner.getValue();
        Warehouse warehouse = cbWarehouse.getValue();
        String date = "";
        String errorMessage = "";
        if (customer == null)
            errorMessage += errorMessage.equals("") ? "Select a valid customer" : "\nSelect a valid customer";
        if (warehouse == null)
            errorMessage += errorMessage.equals("") ? "Select a valid warehouse" : "\nSelect a valid warehouse";
        try {
            date = dpDate.getValue().toString();
            if (date.equals("")) throw new Exception();
        } catch (Exception e) {
            errorMessage += errorMessage.equals("") ? "The invoice date is invalid." : "The invoice date is invalid";
        }
        if (!errorMessage.equals("")) {
            notificationPane.show(errorMessage);
            return;
        }
        Call<Invoice[]> call;
        if (invoice == null) {
            call = apiService.addCustomerInvoice(customer.getId(), warehouse.getId(), date, user.getId());
        } else {
            call = apiService.updateCustomerInvoice(invoice.getId(), customer.getId(), warehouse.getId(), date);
        }
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Invoice[]> call, Response<Invoice[]> response) {
                if (response.isSuccessful()) {
                    if (dataInterface != null) {
                        Platform.runLater(() -> {
                            Utility.closeWindow(vbHolder);
                            List<Invoice> filtered = new ArrayList<>();
                            Invoice[] invoices = {};
                            for (Invoice i : response.body()) {
                                if (!i.isPosted()) filtered.add(i);
                            }
                            dataInterface.updateData("The Customer invoice has been saved.", filtered.toArray(invoices));
                        });
                    }
                } else {
                    System.out.println(response.errorBody().toString());
                    notificationPane.show(Utility.handleApiErrors(response.message(), response.errorBody(),
                            new String[]{"invNo", "customerId", "invoiceDate", "warehouseId"}));
                }
            }

            @Override
            public void onFailure(Call<Invoice[]> call, Throwable throwable) {
                notificationPane.show(throwable.getMessage());
            }
        });
    }

    private void post() {
        Call<Invoice[]> call = apiService.postCustomerInvoice(invoice.getId(), user.getId());
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Invoice[]> call, Response<Invoice[]> response) {
                if (response.isSuccessful()) {
                    if (dataInterface != null) {
                        Platform.runLater(() -> {
                            Utility.closeWindow(vbHolder);
                            dataInterface.updateData("The invoice has been posted successfully", response.body());
                        });
                    }
                } else
                    Platform.runLater(() -> notificationPane.show(Utility.handleApiErrors(response.message(), response.errorBody(),
                            new String[]{"message"})));
            }

            @Override
            public void onFailure(Call<Invoice[]> call, Throwable throwable) {
                Platform.runLater(() -> notificationPane.show(throwable.getMessage()));
            }
        });
    }

    private void reverse() {
        Call<Invoice[]> call = apiService.reverseCustomerInvoice(invoice.getId(), user.getId());
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Invoice[]> call, Response<Invoice[]> response) {
                if (response.isSuccessful()) {
                    if (dataInterface != null) {
                        Platform.runLater(() -> {
                            Utility.closeWindow(vbHolder);
                            dataInterface.updateData("The invoice has been posted successfully", response.body());
                        });
                    }
                } else
                    Platform.runLater(() -> notificationPane.show(Utility.handleApiErrors(response.message(), response.errorBody(),
                            new String[]{"message"})));
            }

            @Override
            public void onFailure(Call<Invoice[]> call, Throwable throwable) {
                Platform.runLater(() -> notificationPane.show(throwable.getMessage()));
            }
        });
    }

    private void addLine() {
        ValidationResult vr = vsLine.getValidationResult();
        Iterator<ValidationMessage> iterator = vr.getErrors().iterator();
        StringBuilder message = new StringBuilder();
        while (iterator.hasNext()) {
            message.append(iterator.next().getText());
            message.append("\n");
        }
        if (!vr.getErrors().isEmpty()) {
            notificationPane.show(message.toString());
            return;
        }
        String errorMessage = "";
        String type = cbType.getValue();
        Object object = cbPS.getValue();
        int typeId = type.equals("Product") ? ((Product) object).getId() : ((Service) object).getId();
        String description = tfDescription.getText();
        String unitPrice = tfUnitPrice.getText();
        String quantity = tfQuantity.getText();


        if (type.equals("")) errorMessage = errorMessage.concat("Select a valid type.");
        if (object == null) {
            errorMessage = errorMessage.concat(errorMessage.equals("") ? "Unit price is required" : "\nUnit price is required");
        }
        if (unitPrice.equals("")) {
            errorMessage = errorMessage.concat(errorMessage.equals("") ? "Unit price is required" : "\nUnit price is required");
        }
        if (quantity.equals("")) {
            errorMessage = errorMessage.concat(errorMessage.equals("") ? "Quantity is required" : "\nQuantity is required");
        }
        if (!errorMessage.equals("")) {
            notificationPane.show(errorMessage);
            return;
        }


        double price = Double.parseDouble(unitPrice);
        double buyingPrice = 0;
        if (type.equals("Product")) {
            buyingPrice = ((Product) object).getBuyingPrice();
        }
        int q = Integer.parseInt(quantity);

        if (invoice == null) {
            ValidationResult invoiceValidationResult = vsInvoice.getValidationResult();
            Iterator<ValidationMessage> messageIterator = invoiceValidationResult.getErrors().iterator();
            StringBuilder stringBuilder = new StringBuilder();
            while (messageIterator.hasNext()) {
                stringBuilder.append(messageIterator.next().getText());
                stringBuilder.append("\n");
            }
            if (!invoiceValidationResult.getErrors().isEmpty()) {
                notificationPane.show(stringBuilder.toString());
                return;
            }
            Invoice mInvoice = new Invoice();
            mInvoice.setCustomer(cbOwner.getValue());
            mInvoice.setInvoiceDate(dpDate.getValue().toString());
            mInvoice.setWarehouse(cbWarehouse.getValue());
            Gson gson = new Gson();
            String json = gson.toJson(mInvoice, Invoice.class);
            System.out.println(json);
            Call<Invoice> call = apiService.addCustomerInvoiceLine(json, type, typeId, price, buyingPrice, q, description, user.getId());
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(Call<Invoice> call, Response<Invoice> response) {
                    Platform.runLater(() -> {
                        if (response.isSuccessful()) {
                            setInvoice(response.body());
                            Platform.runLater(()->{
                                tfDescription.setText("");
                                tfUnitPrice.setText("");
                                tfQuantity.setText("");
                            });
                        }
                        else notificationPane.show(response.message());
                    });
                }

                @Override
                public void onFailure(Call<Invoice> call, Throwable throwable) {
                    System.out.println(throwable.toString());
                    Platform.runLater(throwable::getMessage);
                }
            });
            return;
        }

        Call<InvoiceLine[]> call = apiService.addCustomerInvoiceLine(invoice.getId(), type, typeId, price, buyingPrice, q, description);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<InvoiceLine[]> call, Response<InvoiceLine[]> response) {
                assert response.body() != null;
                if (response.isSuccessful()) {
                    setLines(response.body());
                    Platform.runLater(() -> {
                        tfDescription.setText("");
                        tfUnitPrice.setText("");
                        tfQuantity.setText("");
                    });
                } else {
                    assert response.errorBody() != null;
                    notificationPane.show(Utility.handleApiErrors(response.message(), response.errorBody(),
                            new String[]{"invId", "type", "typeId", "description"}));
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
        tfNo.setText(invoice.getInvoiceNo());
        btnAddLine.setDisable(invoice.isPosted());
        btnSave.setDisable(invoice.isPosted());
        btnPost.setDisable(invoice.isPosted());
        btnReverse.setDisable(invoice.isReversed() || !invoice.isPosted());
        setLines(invoice.getInvoiceLines());
        dpDate.setValue(LocalDate.parse(invoice.getInvoiceDate()));
        tvLines.setDisable(invoice.isPosted());
    }


}
