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
import models.customers.InvoiceLine;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

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
    private ComboBox<Object> cbPS;

    @FXML
    private TextField tfUnitPrice;

    @FXML
    private TextField tfQuantity;

    @FXML
    private Button btnAddLine;

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
    private HomeDataInterface dataInterface;
    private final User user = SessionManager.INSTANCE.getUser();
    private Service[] services;
    private Product[] products;

    ValidationSupport vsSale = new ValidationSupport();
    ValidationSupport vsLine = new ValidationSupport();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        Utility.setupNotificationPane(notificationPane, vbHolder);
        Utility.restrictInputDec(tfUnitPrice);
        Utility.restrictInputNum(tfQuantity);

        apiService = RetrofitBuilder.createService(ApiService.class);

        Platform.runLater(()->{
            Utility.setLogo(vbParent);
            if (expressSale == null) dpDate.setValue(LocalDate.now());
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

            cbPS.setOnAction(event -> {
                if (cbType.getValue().equals("Product")) {
                    Product product = (Product) cbPS.getValue();
                    tfUnitPrice.setText(String.valueOf(product.getSellingPrice()));
                    tfQuantity.setText("1");
                }
            });

            vsSale.registerValidator(cbWarehouse, true, Validator.createEmptyValidator("Warehouse is required."));
            vsSale.registerValidator(cbBank, true, Validator.createEmptyValidator("Bank account is required."));
            vsSale.registerValidator(dpDate, true, Validator.createEmptyValidator("Enter a valid date."));

            vsLine.registerValidator(cbType, true, Validator.createEmptyValidator("Select a valid type"));
            vsLine.registerValidator(cbPS, true, Validator.createEmptyValidator("Select a valid product/service"));
            vsLine.registerValidator(tfUnitPrice, true, Validator.createEmptyValidator("Enter a valid unit price."));
            vsLine.registerValidator(tfQuantity, true, Validator.createEmptyValidator("Enter a valid quantity"));
        });


        tcPS.setCellValueFactory(new PropertyValueFactory<>("name"));
        tcPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        tcQuantity.setCellValueFactory(new PropertyValueFactory<>("quantityTf"));
        tcTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        tcRemove.setCellValueFactory(new PropertyValueFactory<>("removeLine"));

        btnSave.setOnAction(event -> save());
        btnPost.setOnAction(event -> post());
        btnReverse.setOnAction(event -> reverse());
        btnCancel.setOnAction(event -> Utility.closeWindow(vbParent));
        btnAddLine.setOnAction(event -> addLine());

        loadData();
    }

    private void loadData(){
        Call<Bank[]> getBanks = apiService.banks();
        Call<Warehouse[]> getWarehouses = apiService.warehouses();
        Call<Product[]> getProducts = apiService.products();
        Call<Service[]> getServices = apiService.services();
        getBanks.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Bank[]> call, Response<Bank[]> response) {
                if (response.isSuccessful()){
                    Platform.runLater(()->{
                        cbBank.setItems(FXCollections.observableArrayList(response.body()));
                        if(expressSale!=null) {
                            cbBank.getSelectionModel().select(expressSale.getBank());
                        }
                    });
                } else notificationPane.show(response.message());
            }

            @Override
            public void onFailure(Call<Bank[]> call, Throwable throwable) {
                notificationPane.show(throwable.getMessage());
            }
        });
        getWarehouses.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Warehouse[]> call, Response<Warehouse[]> response) {
                if (response.isSuccessful()){
                    Platform.runLater(()->{
                        cbWarehouse.setItems(FXCollections.observableArrayList(response.body()));
                        if (expressSale!=null) cbWarehouse.getSelectionModel().select(expressSale.getWarehouse());
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
        try{
            notify();
        } catch (IllegalMonitorStateException ignore){}
    }

    private void save(){
        // "saleNo", "description", "saleDate", "bankId", "warehouseId", "refNo", "createdBy"
        ValidationResult vr = vsSale.getValidationResult();
        Iterator<ValidationMessage> iterator = vr.getErrors().iterator();
        StringBuilder message = new StringBuilder();
        while (iterator.hasNext()){
            message.append(iterator.next().getText());
            message.append("\n");
        }
        if (!vr.getErrors().isEmpty()){
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
        try{
            date = dpDate.getValue().toString();
        }catch (Exception e){
            errorMessage += errorMessage.equals("")?"Select a valid date":"\nSelect a valid date";
        }
        if (bank == null) errorMessage += errorMessage.equals("")?"Select a valid bank":"\nSelect a valid bank";
        else if(bank.isRequireRefNo()){
            if (refNo.equals("")) errorMessage += errorMessage.equals("")?"Reference number is required for this form of payment":
                    "\nReference number is required for this form of payment";
        }
        if (warehouse == null) errorMessage += errorMessage.equals("")?"Select a valid warehouse":"\nSelect a valid warehouse";
         if (!errorMessage.equals("")){
            notificationPane.show(errorMessage);
            return;
        }
        Call<ExpressSale[]> call;
        if (expressSale == null) call = apiService.addSale("", date, bank.getId(), warehouse.getId(), refNo, user.getId());
        else{
            saleNo = expressSale.getSaleNo().equals(saleNo)?null:saleNo;
            call = apiService.updateSale(expressSale.getId(), "", date, bank.getId(), warehouse.getId(), refNo);
        }
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ExpressSale[]> call, Response<ExpressSale[]> response) {
                if (response.isSuccessful()){
                    if (dataInterface!=null){
                        ExpressSale[] eSale = {};
                        List<ExpressSale> filtered = new ArrayList<>();
                        for (ExpressSale sale : response.body()) {
                            if (!sale.isPosted()) filtered.add(sale);
                        }
                        Platform.runLater(()->{
                            Utility.closeWindow(vbHolder);
                            dataInterface.updateData("The sale has been saved.", filtered.toArray(eSale));
                        });
                    }
                }else{
                    Platform.runLater(()-> notificationPane.show(Utility.handleApiErrors(response.message(), response.errorBody(),
                            new String[]{"saleNo", "description", "saleDate", "bankId", "warehouseId", "refNo", "createdBy"})));
                }
            }

            @Override
            public void onFailure(Call<ExpressSale[]> call, Throwable throwable) {
                Platform.runLater(()->notificationPane.show(throwable.getMessage()));
            }
        });
    }
    private void post(){
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
                    }
                }else {
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
    private void reverse(){
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
    private void addLine(){
        ValidationResult vr = vsLine.getValidationResult();
        Iterator<ValidationMessage> iterator = vr.getErrors().iterator();
        StringBuilder message = new StringBuilder();
        while (iterator.hasNext()){
            message.append(iterator.next().getText());
            message.append("\n");
        }
        if (!vr.getErrors().isEmpty()){
            notificationPane.show(message.toString());
            return;
        }

        String errorMessage = "";
        String type = cbType.getValue();
        Object object = cbPS.getValue();
        int typeId = type.equals("Product")?((Product)object).getId():((Service)object).getId();
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
        double buyingPrice = 0;
        if (type.equals("Product")){
            buyingPrice = ((Product)object).getBuyingPrice();
        }

        if(expressSale == null){
            ValidationResult validationResult = vsSale.getValidationResult();
            Iterator<ValidationMessage> validationMessageIterator = validationResult.getErrors().iterator();
            StringBuilder stringBuilder = new StringBuilder();
            while (validationMessageIterator.hasNext()){
                stringBuilder.append(validationMessageIterator.next().getText());
                stringBuilder.append("\n");
            }
            if (!validationResult.getErrors().isEmpty()){
                notificationPane.show(stringBuilder.toString());
                return;
            }
            ExpressSale sale = new ExpressSale();
            sale.setBank(cbBank.getValue());
            sale.setWarehouse(cbWarehouse.getValue());
            sale.setSaleDate(dpDate.getValue().toString());
            sale.setRefNo(tfRefNo.getText() == null ? "" : tfRefNo.getText());
            Gson gson = new Gson();
            String json = gson.toJson(sale, ExpressSale.class);
            Call<ExpressSale> call = apiService.addSaleLine(json, type, typeId, price,buyingPrice, q, user.getId());
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(Call<ExpressSale> call, Response<ExpressSale> response) {
                    Platform.runLater(()->{
                        if (response.isSuccessful()){
                            setExpressSale(response.body());
                            tfUnitPrice.setText("");
                            tfQuantity.setText("");
                        } else notificationPane.show(response.message());
                    });
                }

                @Override
                public void onFailure(Call<ExpressSale> call, Throwable throwable) {
                    Platform.runLater(()->notificationPane.show(throwable.getMessage()));
                }
            });
        }

        Call<ExpressSaleLine[]> call = apiService.addSaleLine(expressSale.getId(), type, typeId, price,buyingPrice, q);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ExpressSaleLine[]> call, Response<ExpressSaleLine[]> response) {
                if (response.isSuccessful()) {
                    setLines(response.body());
                    tfUnitPrice.setText("");
                    tfQuantity.setText("");
                }
                else {
                    Platform.runLater(()-> notificationPane.show(Utility.handleApiErrors(response.message(), response.errorBody(),
                            new String[]{"saleId", "type", "typeId", "unitPrice", "quantity"})));
                }
            }

            @Override
            public void onFailure(Call<ExpressSaleLine[]> call, Throwable throwable) {
                Platform.runLater(()->notificationPane.show(throwable.getMessage()));
            }
        });
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
        if (expressSale.isPosted()){
            btnSave.setDisable(true);
            btnPost.setDisable(true);
            btnReverse.setDisable(expressSale.isReversed());
            tvLines.setDisable(true);
            btnAddLine.setDisable(true);
        }
    }

    private void setLines(ExpressSaleLine[] lines){
        double total = 0;
        for (ExpressSaleLine line: lines) {
            line.setLinesInterface(this);
            total += line.getTotal();
        }
        expressSale.setLines(lines);
        Platform.runLater(()->{
            labelTotal.setText(expressSale.getTotalString());
            tvLines.setItems(FXCollections.observableArrayList(lines));
        });
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
    public void updateQuantity(Object line) {
        ExpressSaleLine saleLine = (ExpressSaleLine) line;
        Call<ExpressSaleLine[]> call = apiService.addSaleLine(expressSale.getId(), saleLine.getType(), saleLine.getTypeId(), saleLine.getUnitPrice(), saleLine.getBuyingPrice(), saleLine.getQuantity());
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ExpressSaleLine[]> call, Response<ExpressSaleLine[]> response) {
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
            public void onFailure(Call<ExpressSaleLine[]> call, Throwable throwable) {
                notificationPane.show(throwable.getMessage());
            }
        });
    }
}
