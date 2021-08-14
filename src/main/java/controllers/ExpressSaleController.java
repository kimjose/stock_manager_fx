package controllers;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import interfaces.HomeDataInterface;
import interfaces.LinesInterface;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
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
import javafx.scene.layout.VBox;
import models.SuperModel;
import models.auth.User;
import models.finance.Bank;
import models.products.*;
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

public class ExpressSaleController implements Initializable, LinesInterface {

    @FXML
    private VBox vbParent;

    @FXML
    private NotificationPane notificationPane;

    @FXML
    private VBox vbHolder;

    @FXML
    private TextField tfNo;

    @FXML
    private ComboBox<Warehouse> cbWarehouse;

    @FXML
    private ComboBox<Bank> cbBank;

    @FXML
    private TextField tfRefNo;

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
    private TextField tfSearch;

    @FXML
    private TableView<ExpressSaleLine> tvLines;

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
    private ExpressSale expressSale;
    private ExpressSaleLine[] saleLines = new ExpressSaleLine[]{};
    private HomeDataInterface dataInterface;
    private final User user = SessionManager.INSTANCE.getUser();
    private Service[] services;
    private Product[] products;
    private final Gson gson = new Gson();

    ValidationSupport vsSale = new ValidationSupport();
    ValidationSupport vsLine = new ValidationSupport();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        Utility.setupNotificationPane(notificationPane, vbHolder);

        apiService = RetrofitBuilder.createService(ApiService.class);

        Platform.runLater(() -> {
            Utility.setLogo(vbParent);
            if (expressSale == null) dpDate.setValue(LocalDate.now());
            String[] types = {"Product", "Service"};
            cbType.setItems(FXCollections.observableArrayList(types));
            cbType.getSelectionModel().selectedItemProperty().addListener(this::typeChanged);
            /*cbType.setOnAction(event -> {
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
                                tcItemPrice.setCellValueFactory(new PropertyValueFactory<>("description"));
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
            });*/


            vsSale.registerValidator(cbWarehouse, true, Validator.createEmptyValidator("Warehouse is required."));
            vsSale.registerValidator(cbBank, true, Validator.createEmptyValidator("Bank account is required."));
            vsSale.registerValidator(dpDate, true, Validator.createEmptyValidator("Enter a valid date."));

            vsLine.registerValidator(cbType, true, Validator.createEmptyValidator("Select a valid type"));
         });

        tcPS.setCellValueFactory(new PropertyValueFactory<>("name"));
        tcPrice.setCellValueFactory(new PropertyValueFactory<>("unitPriceTf"));
        tcQuantity.setCellValueFactory(new PropertyValueFactory<>("quantityTf"));
        tcTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        tcRemove.setCellValueFactory(new PropertyValueFactory<>("removeLine"));

        btnSave.setOnAction(event -> save());
        btnPost.setOnAction(event -> post());
        btnReverse.setOnAction(event -> reverse());
        btnCancel.setOnAction(event -> Utility.closeWindow(vbParent));

        tfSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().equals("")) {
                tvItems.setItems(FXCollections.observableArrayList(cbType.getValue().equals("Product") ? products : services));
                return;
            }
            List<SuperModel> filtered = new ArrayList<>();
            for (SuperModel model : cbType.getValue().equals("Product") ? products : services) {
                if (model.getSearchString().toLowerCase().contains(newValue.toLowerCase())) filtered.add(model);
            }
            tvItems.setItems(FXCollections.observableArrayList(filtered));
        });

        //function keys
        vbParent.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            KeyCode keyCode = event.getCode();
            if (keyCode.equals(KeyCode.F11)) save();
            else if (keyCode.equals(KeyCode.F10)) {
                if (expressSale == null ) return;
                if (expressSale.isPosted()) return;
                post();
            }
            else if (keyCode.equals(KeyCode.F8)) {
                if (expressSale == null ) return;
                if (!expressSale.isPosted()) return;
                if (expressSale.isReversed()) return;
                reverse();
            }
            else if (keyCode.equals(KeyCode.F9)) Utility.closeWindow(vbHolder);
            else if (keyCode.equals(KeyCode.P) && event.isControlDown()) {
                if (expressSale == null ) return;
                if (!expressSale.isPosted()) return;
                if (expressSale.isReversed()) return;
                HashMap<String, Object> params = new LinkedHashMap<>();
                params.put("saleNo", expressSale.getSaleNo());
                params.put("saleDate", expressSale.getSaleDate());
                params.put("saleTotal", "Kshs " + expressSale.getTotalString());
                Node n = Utility.showProgressBar("Generating report...", vbHolder.getScene().getWindow());
                Call<Object[]> call = apiService.saleReport(expressSale.getId());
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<Object[]> call, Response<Object[]> response) {
                        Utility.closeWindow(n);
                        if (response.isSuccessful())
                            Utility.showReport("sale_report.jasper", params, gson.toJson(response.body()));
                        else Platform.runLater(() -> notificationPane.show(response.message()));
                    }

                    @Override
                    public void onFailure(Call<Object[]> call, Throwable throwable) {
                        Utility.closeWindow(n);
                        Platform.runLater(()->notificationPane.show(throwable.getMessage()));
                    }
                });
            }
            else if (keyCode.equals(KeyCode.F5)) loadData();
        });

        loadData();
    }

    private void typeChanged(Observable observable, String oldValue, String newValue){
        Task<Object> myTask = new Task<>() {
            @Override
            protected Object call() {
                if (products == null || services == null) {
                    loadData();
                    try {
                        Thread.currentThread().wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        e.printStackTrace();
                    }
                }
                Platform.runLater(() -> {
                    if (newValue.equals("Product")) {
                        tcItemName.setCellValueFactory(new PropertyValueFactory<>("name"));
                        tcItemPrice.setCellValueFactory(new PropertyValueFactory<>("sellingPrice"));
                        tcItemAdd.setCellValueFactory(new PropertyValueFactory<>("addBtn"));
                        tvItems.setItems(FXCollections.observableArrayList(products));
                    } else if (newValue.equals("Service")) {
                        tcItemName.setCellValueFactory(new PropertyValueFactory<>("name"));
                        tcItemPrice.setCellValueFactory(new PropertyValueFactory<>("description"));
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
    }

    private void loadData() {
        Call<Bank[]> getBanks = apiService.banks();
        Call<Warehouse[]> getWarehouses = apiService.warehouses();
        Call<Product[]> getProducts = apiService.products();
        Call<Service[]> getServices = apiService.services();
        getBanks.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Bank[]> call, Response<Bank[]> response) {
                if (response.isSuccessful()) {
                    Platform.runLater(() -> {
                        cbBank.setItems(FXCollections.observableArrayList(response.body()));
                        if (expressSale != null)
                            cbBank.getSelectionModel().select(expressSale.getBank());
                        else cbBank.getSelectionModel().selectFirst();
                    });
                } else Platform.runLater(() ->notificationPane.show(response.message()));
            }

            @Override
            public void onFailure(Call<Bank[]> call, Throwable throwable) {
                Platform.runLater(() ->notificationPane.show(throwable.getMessage()));
            }
        });
        getWarehouses.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Warehouse[]> call, Response<Warehouse[]> response) {
                if (response.isSuccessful()) {
                    Platform.runLater(() -> {
                        cbWarehouse.setItems(FXCollections.observableArrayList(response.body()));
                        if (expressSale != null) cbWarehouse.getSelectionModel().select(expressSale.getWarehouse());
                        else cbWarehouse.getSelectionModel().selectFirst();
                    });
                } else Platform.runLater(() ->notificationPane.show(response.message()));
            }

            @Override
            public void onFailure(Call<Warehouse[]> call, Throwable throwable) {
                Platform.runLater(() ->notificationPane.show(throwable.getMessage()));
            }
        });
        getServices.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Service[]> call, Response<Service[]> response) {
                if (response.isSuccessful()) setServices(response.body());
                else Platform.runLater( ()-> notificationPane.show(response.message()));
            }

            @Override
            public void onFailure(Call<Service[]> call, Throwable throwable) {
                Platform.runLater( ()-> notificationPane.show(throwable.getMessage()));
            }
        });
        getProducts.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Product[]> call, Response<Product[]> response) {
                if (response.isSuccessful()) {
                    setProducts(response.body());
                    Platform.runLater(()->cbType.getSelectionModel().select("Product"));
                }
                else Platform.runLater(()->notificationPane.show(response.message()));
            }

            @Override
            public void onFailure(Call<Product[]> call, Throwable throwable) {
                Platform.runLater( ()-> notificationPane.show(throwable.getMessage()));
            }
        });
        try {
            notify();
        } catch (IllegalMonitorStateException ignore) {
        }
    }

    private void save() {
        // "saleNo", "description", "saleDate", "bankId", "warehouseId", "refNo", "createdBy"
        ValidationResult vr = vsSale.getValidationResult();
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
        String saleNo = tfNo.getText().trim();
        String date = null;
        Bank bank = cbBank.getValue();
        Warehouse warehouse = cbWarehouse.getValue();
        String refNo = tfRefNo.getText().trim();
        String errorMessage = "";
        //if (saleNo.equals("")) errorMessage += "Sale number is required.";
        try {
            date = dpDate.getValue().toString();
        } catch (Exception e) {
            errorMessage += errorMessage.equals("") ? "Select a valid date" : "\nSelect a valid date";
        }
        if (bank == null) errorMessage += errorMessage.equals("") ? "Select a valid bank" : "\nSelect a valid bank";
        else if (bank.isRequireRefNo()) {
            if (refNo.equals(""))
                errorMessage += errorMessage.equals("") ? "Reference number is required for this form of payment" :
                        "\nReference number is required for this form of payment";
        }
        if (warehouse == null)
            errorMessage += errorMessage.equals("") ? "Select a valid warehouse" : "\nSelect a valid warehouse";
        if (!errorMessage.equals("")) {
            notificationPane.show(errorMessage);
            return;
        }
        System.out.println(saleLines.toString());
        Call<ExpressSale> call;
        if (expressSale == null)
            call = apiService.addSale("", date, bank.getId(), warehouse.getId(), refNo, gson.toJson(saleLines), user.getId());
        else {
            call = apiService.updateSale(expressSale.getId(), "", date, bank.getId(), warehouse.getId(), refNo, gson.toJson(saleLines));
        }
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ExpressSale> call, Response<ExpressSale> response) {
                if (response.isSuccessful()) {
                    Platform.runLater(() -> {
                        notificationPane.show("The sale has been saved.");
                        setExpressSale(response.body());
                    });
                } else {
                    Platform.runLater(() -> notificationPane.show(Utility.handleApiErrors(response.message(), response.errorBody(),
                            new String[]{"saleNo", "description", "saleDate", "bankId", "warehouseId", "refNo", "createdBy"})));
                }
            }

            @Override
            public void onFailure(Call<ExpressSale> call, Throwable throwable) {
                Platform.runLater(() -> notificationPane.show(throwable.getMessage()));
            }
        });
    }

    private void post() {
        assert expressSale != null;
        Call<ExpressSale[]> call = apiService.postSale(expressSale.getId(), user.getId());
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ExpressSale[]> call, Response<ExpressSale[]> response) {
                if (response.isSuccessful()) {
                    if (dataInterface != null) {
                        ExpressSale[] expressSales = {};
                        List<ExpressSale> filtered = new ArrayList<>();
                        for (ExpressSale sale : response.body()) {
                            if (!sale.isPosted()) filtered.add(sale);
                        }
                        Platform.runLater(() -> {
                            Utility.closeWindow(vbHolder);
                            dataInterface.updateData("The sale has been posted.", filtered.toArray(expressSales));
                        });
                    } else Utility.closeWindow(vbHolder);
                } else {
                    Platform.runLater(() -> notificationPane.show(Utility.handleApiErrors(response.message(), response.errorBody(),
                            new String[]{"message"})));
                }
            }

            @Override
            public void onFailure(Call<ExpressSale[]> call, Throwable throwable) {
                Platform.runLater(() -> notificationPane.show(throwable.getMessage()));
            }
        });
    }

    private void reverse() {
        Call<ExpressSale[]> call = apiService.reverseSale(expressSale.getId(), user.getId());
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ExpressSale[]> call, Response<ExpressSale[]> response) {
                if (response.isSuccessful()) {
                    if (dataInterface != null) {
                        ExpressSale[] expressSales = {};
                        List<ExpressSale> filtered = new ArrayList<>();
                        for (ExpressSale sale : response.body()) {
                            if (sale.isPosted() && !sale.isReversed()) filtered.add(sale);
                        }
                        Platform.runLater(() -> {
                            Utility.closeWindow(vbHolder);
                            dataInterface.updateData("The sale has been reversed.", filtered.toArray(expressSales));
                        });
                    }
                } else {
                    Platform.runLater(() -> notificationPane.show(Utility.handleApiErrors(response.message(), response.errorBody(),
                            new String[]{"message"})));
                }
            }

            @Override
            public void onFailure(Call<ExpressSale[]> call, Throwable throwable) {
                Platform.runLater(() -> notificationPane.show(throwable.getMessage()));
            }
        });
    }

    private void addLine(String type, int typeId, double price, double buyingPrice, int q, String name) {
        String errorMessage = "";

        if (!errorMessage.equals("")) {
            notificationPane.show(errorMessage);
            return;
        }
        boolean added = false;
        List<ExpressSaleLine> saleLineList = new ArrayList<>();
        for( int i = 0; i < saleLines.length; i++) {
            ExpressSaleLine saleLine = saleLines[i];
            if(saleLine.getTypeId() == typeId && saleLine.getType().equals(type)) {
                ExpressSaleLine newLine = new ExpressSaleLine(type, typeId, price, buyingPrice, saleLine.getQuantity() + q, this, name);
                saleLineList.add(newLine);
                added = true;
            } else {
                saleLineList.add(saleLine);
            }

        }
        if (!added) {
            ExpressSaleLine saleLine = new ExpressSaleLine(type, typeId, price, buyingPrice, q, this, name);
            saleLineList.add(saleLine);
        }
        setLines( saleLineList.toArray(saleLines));
    }


    public void setDataInterface(HomeDataInterface dataInterface) {
        this.dataInterface = dataInterface;
    }

    public void setExpressSale(ExpressSale expressSale) {
        this.expressSale = expressSale;
        tfNo.setText(expressSale.getSaleNo());
        tfRefNo.setText(expressSale.getRefNo());
        dpDate.setValue(LocalDate.parse(expressSale.getSaleDate()));
        setLines(expressSale.getLines());
        btnPost.setDisable(false);
        if (expressSale.isPosted()) {
            btnSave.setDisable(true);
            btnPost.setDisable(true);
            btnReverse.setDisable(expressSale.isReversed());
            tvLines.setDisable(true);
            tvItems.setDisable(true);
        }
    }

    private void setLines(ExpressSaleLine[] lines) {
        double total = 0;
        this.saleLines = lines;
        for (ExpressSaleLine line : lines) {
            line.setLinesInterface(this);
            total += line.getTotal();
        }
        if ( expressSale != null )expressSale.setLines(lines);
        final String totalString = Utility.formatNumber(total);
        Platform.runLater(() -> {
            labelTotal.setText(totalString);
            tvLines.setItems(FXCollections.observableArrayList(lines));
        });
    }

    public void setProducts(Product[] products) {
        for (Product p : products) {
            p.setLinesInterface(this);
        }
        this.products = products;
    }

    public void setServices(Service[] services) {
        for (Service s : services) {
            s.setLinesInterface(this);
        }
        this.services = services;
    }

    @Override
    public void updateData(Object[] lines) {
        setLines((ExpressSaleLine[]) lines);
    }

    @Override
    public void notifyError(String errorMessage) {
        notificationPane.show(errorMessage);
    }

    @Override
    public void updateLine(Object line) {
        ExpressSaleLine newLine = (ExpressSaleLine) line;
        List<ExpressSaleLine> saleLineList = new ArrayList<>();
        for( int i = 0; i < saleLines.length; i++) {
            ExpressSaleLine saleLine = saleLines[i];
            if(saleLine.getTypeId() == newLine.getTypeId() && saleLine.getType().equals(newLine.getType())) {
                saleLineList.add(newLine);
            } else {
                saleLineList.add(saleLine);
            }

        }
        setLines( saleLineList.toArray(saleLines));
    }

    @Override
    public void addItem(SuperModel item, String type) {
        if (type.equals("Service")) {
            Service service = (Service) item;
            addLine(type, service.getId(), 100, 100, 1, service.getName());
        } else if (type.equals("Product")) {
            Product product = (Product) item;
            addLine(type, product.getId(), product.getSellingPrice(), product.getBuyingPrice(), 1, product.getName());
        }
    }
}
