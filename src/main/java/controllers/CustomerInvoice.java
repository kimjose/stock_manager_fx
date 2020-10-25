package controllers;


import com.google.gson.Gson;
import interfaces.HomeDataInterface;
import interfaces.LinesInterface;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.SuperModel;
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
import java.util.*;

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
    private TableView<SuperModel> tvItems;

    @FXML
    private TableColumn<?, ?> tcItemName;

    @FXML
    private TableColumn<?, ?> tcItemPrice;

    @FXML
    private TableColumn<?, ?> tcItemAdd;

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

    @FXML
    private Label labelPay;

    @FXML
    private TextField tfSearch;

    private ApiService apiService;
    private Invoice invoice;
    private HomeDataInterface dataInterface;
    private final User user = SessionManager.INSTANCE.getUser();
    private Service[] services;
    private Product[] products;

    private final ValidationSupport vsInvoice = new ValidationSupport();
    private final ValidationSupport vsLine = new ValidationSupport();


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        Utility.setupNotificationPane(notificationPane, vbHolder);
        apiService = RetrofitBuilder.createService(ApiService.class);

        Platform.runLater(() -> {
            Utility.setLogo(vbParent);
            labelOwner.setText("Customer");
            cbOwner.setPromptText("Select Customer");
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
                            if (type.equals("Product")) {
                                tcItemName.setCellValueFactory(new PropertyValueFactory<>("name"));
                                tcItemPrice.setCellValueFactory(new PropertyValueFactory<>("sellingPrice"));
                                tcItemAdd.setCellValueFactory(new PropertyValueFactory<>("addBtn"));
                                tvItems.setItems(FXCollections.observableArrayList(products));
                            } else if (type.equals("Service")) {
                                tcItemName.setCellValueFactory(new PropertyValueFactory<>("name"));
                                tcItemPrice.setCellValueFactory(new PropertyValueFactory<>("sellingPrice"));
                                tcItemAdd.setCellValueFactory(new PropertyValueFactory<>("addBtn"));
                                tvItems.setItems(FXCollections.observableArrayList(services));
                            }
                        });
                        return null;
                    }
                };
                Thread thread = new Thread(myTask);
                thread.setDaemon(true);
                thread.start();
            });
            tcPS.setCellValueFactory(new PropertyValueFactory<>("name"));
            tcPrice.setCellValueFactory(new PropertyValueFactory<>("unitPriceTf"));
            tcQuantity.setCellValueFactory(new PropertyValueFactory<>("quantityTf"));
            tcTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
            tcRemove.setCellValueFactory(new PropertyValueFactory<>("removeLine"));

            vsInvoice.registerValidator(cbOwner, true, Validator.createEmptyValidator("Select a valid customer."));
            vsInvoice.registerValidator(cbWarehouse, true, Validator.createEmptyValidator("Select a valid warehouse."));
            vsInvoice.registerValidator(dpDate, true, Validator.createEmptyValidator("Select a valid Date."));

            vsLine.registerValidator(cbType, true, Validator.createEmptyValidator("Select a valid type"));

            tfSearch.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.trim().equals("")) {
                    tvItems.setItems(FXCollections.observableArrayList(cbType.getValue().equals("Product") ? products : services));
                    return;
                }
                List<SuperModel> filtered = new ArrayList<>();
                for (SuperModel model : cbType.getValue().equals("Product") ? products : services) {
                    if (model.getSearchString().toLowerCase().contains(newValue)) filtered.add(model);
                }
                tvItems.setItems(FXCollections.observableArrayList(filtered));
            });

            loadData();
        });

        //function keys
        vbParent.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            KeyCode keyCode = event.getCode();
            if (keyCode.equals(KeyCode.F11)) save();
            else if (keyCode.equals(KeyCode.F10)) {
                if (invoice == null ) return;
                if (invoice.isPosted()) return;
                post();
            }
            else if (keyCode.equals(KeyCode.F8)) {
                if (invoice == null ) return;
                if (!invoice.isPosted()) return;
                if (invoice.isReversed()) return;
                reverse();
            }
            else if (keyCode.equals(KeyCode.F9)) Utility.closeWindow(vbHolder);
            else if (keyCode.equals(KeyCode.F5)) loadData();
        });

        labelPay.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            if (invoice == null){
                notificationPane.show("No invoice.");
                return;
            }
            if (!invoice.isPosted() || invoice.isReversed()){
                notificationPane.show("Invoice has not been posted/ has been reversed.");
                return;
            }
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("customerId", invoice.getCustomer().getId());
            data.put("amount", invoice.getTotal());
            try {
                FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getClassLoader().getResource("fxml/customer_receipt.fxml")));
                VBox vBox = loader.load();
                CustomerReceipt controller = loader.getController();
                controller.setPayData(data);
                Scene scene = new Scene(vBox);
                Stage stage = new Stage();
                stage.setScene(scene);
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setResizable(false);
                stage.initOwner(vbParent.getScene().getWindow());
                stage.setTitle("Create Customer Receipt");
                stage.show();
            } catch (Exception e) {
                notificationPane.show("We are unable to proceed with your request.");
                e.printStackTrace();
            }
            event.consume();
        });
        btnSave.setOnAction(event -> save());
        btnPost.setOnAction(event -> post());
        btnReverse.setOnAction(event -> reverse());
        btnCancel.setOnAction(event -> Utility.closeWindow(vbHolder));

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
    public void updateLine(Object line) {
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

    @Override
    public void addItem(SuperModel item, String type) {
        if (type.equals("Service")) {
            Service service = (Service) item;
            addLine(type, service.getId(), 100, 100, 1);
        } else if (type.equals("Product")) {
            Product product = (Product) item;
            addLine(type, product.getId(), product.getSellingPrice(), product.getBuyingPrice(), 1);
        }
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
                    Platform.runLater(() -> {
                        cbOwner.setItems(FXCollections.observableArrayList(response.body()));
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
                        else cbWarehouse.getSelectionModel().selectFirst();
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
                if (response.isSuccessful()) {
                    setProducts(response.body());
                    Platform.runLater(()->cbType.getSelectionModel().select(0));
                }
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
                if (response.isSuccessful()) setServices(response.body());
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
        Call<Invoice> call;
        if (invoice == null) {
            call = apiService.addCustomerInvoice(customer.getId(), warehouse.getId(), date, user.getId());
        } else {
            call = apiService.updateCustomerInvoice(invoice.getId(), customer.getId(), warehouse.getId(), date);
        }
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Invoice> call, Response<Invoice> response) {
                if (response.isSuccessful()) {
                    setInvoice(response.body());
                    notificationPane.show("The invoice has been saved.");
                } else {
                    notificationPane.show(Utility.handleApiErrors(response.message(), response.errorBody(),
                            new String[]{"invNo", "customerId", "invoiceDate", "warehouseId"}));
                }
            }

            @Override
            public void onFailure(Call<Invoice> call, Throwable throwable) {
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
                            dataInterface.updateData("The invoice has been reversed successfully", response.body());
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

    private void addLine(String type, int typeId, double price, double buyingPrice, int q) {
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
        /*String errorMessage = "";
        String type = cbType.getValue();
        Object object = cbPS.getValue();
        int typeId = type.equals("Product") ? ((Product) object).getId() : ((Service) object).getId();
        String description = tfDescription.getText();
        String unitPrice = tfUnitPrice.getText();
        String quantity = tfQuantity.getText();*/

/*
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
        }*/


      /*  double price = Double.parseDouble(unitPrice);
        double buyingPrice = 0;
        if (type.equals("Product")) {
            buyingPrice = ((Product) object).getBuyingPrice();
        }
        int q = Integer.parseInt(quantity);
*/
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
            Call<Invoice> call = apiService.addCustomerInvoiceLine(json, type, typeId, price, buyingPrice, q, "description", user.getId());
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(Call<Invoice> call, Response<Invoice> response) {
                    Platform.runLater(() -> {
                        if (response.isSuccessful()) {
                            setInvoice(response.body());
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

        Call<InvoiceLine[]> call = apiService.addCustomerInvoiceLine(invoice.getId(), type, typeId, price, buyingPrice, q, "description");
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


    public void setDataInterface(HomeDataInterface dataInterface) {
        this.dataInterface = dataInterface;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
        tfNo.setText(invoice.getInvoiceNo());
        btnSave.setDisable(invoice.isPosted());
        btnPost.setDisable(invoice.isPosted());
        btnReverse.setDisable(invoice.isReversed() || !invoice.isPosted());
        setLines(invoice.getInvoiceLines());
        dpDate.setValue(LocalDate.parse(invoice.getInvoiceDate()));
        tvLines.setDisable(invoice.isPosted());
        tvItems.setDisable(invoice.isPosted());
    }

    public void setProducts(Product[] products) {
        for (Product p : products ) {
            p.setLinesInterface(this);
        }
        this.products = products;
    }

    public void setServices(Service[] services) {
        for (Service s : services ) {
            s.setLinesInterface(this);
        }
        this.services = services;
    }
}
