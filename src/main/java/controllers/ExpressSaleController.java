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
import models.finance.Bank;
import models.products.*;
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


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Utility.setupNotificationPane(notificationPane, vbHolder);
        Utility.restrictInputDec(tfUnitPrice);
        Utility.restrictInputNum(tfQuantity);

        apiService = RetrofitBuilder.createService(ApiService.class);

        Platform.runLater(()->{
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
        });


        tcPS.setCellValueFactory(new PropertyValueFactory<>("name"));
        tcPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        tcQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
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
        String saleNo = tfNo.getText().trim();
        String date = null;
        Bank bank = cbBank.getValue();
        Warehouse warehouse = cbWarehouse.getValue();
        String refNo = tfRefNo.getText().trim();
        String errorMessage = "";
        if (saleNo.equals("")) errorMessage += "Sale number is required.";
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
        if (expressSale == null) call = apiService.addSale(saleNo, "", date, bank.getId(), warehouse.getId(), refNo, user.getId());
        else{
            saleNo = expressSale.getSaleNo().equals(saleNo)?null:saleNo;
            call = apiService.updateSale(expressSale.getId(), saleNo, "", date, bank.getId(), warehouse.getId(), refNo);
        }
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ExpressSale[]> call, Response<ExpressSale[]> response) {
                if (response.isSuccessful()){
                    if (dataInterface!=null){
                        List<ExpressSale> filtered = new ArrayList<>();
                        for (ExpressSale sale : response.body()) {
                            if (!sale.isPosted()) filtered.add(sale);
                        }
                        Platform.runLater(()->{
                            dataInterface.updateData("The sale has been saved.", filtered.toArray());
                            Utility.closeWindow(vbHolder);
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
                        List<ExpressSale> filtered = new ArrayList<>();
                        for (ExpressSale sale : response.body()) {
                            if (!sale.isPosted()) filtered.add(sale);
                        }
                        Platform.runLater(() -> {
                            Utility.closeWindow(vbHolder);
                            dataInterface.updateData("The sale has been posted.", filtered.toArray());
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
                        List<ExpressSale> filtered = new ArrayList<>();
                        for (ExpressSale sale : response.body()) {
                            if (sale.isPosted() && !sale.isReversed()) filtered.add(sale);
                        }
                        Platform.runLater(() -> {
                            Utility.closeWindow(vbHolder);
                            dataInterface.updateData("The sale has been reversed.", filtered.toArray());
                        });
                    }
                } else {
                    Platform.runLater(() -> notificationPane.show(response.message()));
                }
            }

            @Override
            public void onFailure(Call<ExpressSale[]> call, Throwable throwable) {
                Platform.runLater(() -> notificationPane.show(throwable.getMessage()));
            }
        });
    }
    private void addLine(){
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

        Call<ExpressSaleLine[]> call = apiService.addSaleLine(expressSale.getId(), type, typeId, price, q);
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
}
