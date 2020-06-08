package controllers;

import interfaces.HomeDataInterface;
import interfaces.LinesInterface;
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
    private ComboBox<Bank> cbBank;

    @FXML
    private TextField tfRefNo;

    @FXML
    private DatePicker dpDate;

    @FXML
    private Label labelTotal;

    @FXML
    private ComboBox<Product> cbProduct;

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
    private Product[] products;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Utility.setupNotificationPane(notificationPane, vbHolder);
        Utility.restrictInputDec(tfUnitPrice);
        Utility.restrictInputNum(tfQuantity);

        apiService = RetrofitBuilder.createService(ApiService.class);

        cbProduct.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            double price = newValue.getSellingPrice();
            tfUnitPrice.setText(String.valueOf(price));
            tfQuantity.setText("1");
        });

        if (expressSale == null) dpDate.setValue(LocalDate.now());

        tcPS.setCellValueFactory(new PropertyValueFactory<>("name"));
        tcPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        tcQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        tcTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        tcRemove.setCellValueFactory(new PropertyValueFactory<>("removeLine"));

        cbProduct.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> tfUnitPrice.setText(String.valueOf(newValue.getSellingPrice())));

        btnSave.setOnAction(event -> save());
        btnPost.setOnAction(event -> post());
        btnReverse.setOnAction(event -> reverse());
        btnCancel.setOnAction(event -> Utility.closeWindow(vbParent));
        btnAddLine.setOnAction(event -> addLine());

        loadData();
    }

    private void loadData() {
        Call<Bank[]> getBanks = apiService.banks();
        Call<Product[]> getProducts = apiService.products();
        getBanks.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Bank[]> call, Response<Bank[]> response) {
                if (response.isSuccessful()) {
                    Platform.runLater(() -> {
                        cbBank.setItems(FXCollections.observableArrayList(response.body()));
                        if (expressSale != null) {
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
        getProducts.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Product[]> call, Response<Product[]> response) {
                if (response.isSuccessful()) {
                    products = response.body();
                    Platform.runLater(()->cbProduct.setItems(FXCollections.observableArrayList(products)));
                }
                else notificationPane.show(response.message());
            }

            @Override
            public void onFailure(Call<Product[]> call, Throwable throwable) {
                notificationPane.show(throwable.getMessage());
            }
        });
        try {
            notify();
        } catch (IllegalMonitorStateException ignore) {
        }
    }

    private void save() {
        // "saleNo", "description", "saleDate", "bankId", "warehouseId", "refNo", "createdBy"
        String date = null;
        Bank bank = cbBank.getValue();
        String refNo = tfRefNo.getText().trim();
        String errorMessage = "";
        //if (saleNo.equals("")) errorMessage += "Sale number is required.";
        try {
            date = dpDate.getValue().toString();
        } catch (Exception e) {
            errorMessage += errorMessage.equals("") ? "Select a valid date" : "\nSelect a valid date";
        }
        if (bank == null) errorMessage += errorMessage.equals("") ? "Select a valid payment method" : "\nSelect a valid payment method";
        else if (bank.isRequireRefNo()) {
            if (refNo.equals(""))
                errorMessage += errorMessage.equals("") ? "Reference number is required for this form of payment" :
                        "\nReference number is required for this form of payment";
        }
        if (!errorMessage.equals("")) {
            notificationPane.show(errorMessage);
            return;
        }
        Call<ExpressSale[]> call;
        if (expressSale == null) call = apiService.addSale("", date, bank.getId(), 1, refNo, user.getId());
        else {
            call = apiService.updateSale(expressSale.getId(), "", date, bank.getId(), 1, refNo);
        }
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ExpressSale[]> call, Response<ExpressSale[]> response) {
                if (response.isSuccessful()) {
                    if (dataInterface != null) {
                        List<ExpressSale> filtered = new ArrayList<>();
                        ExpressSale[] sales = new ExpressSale[]{};
                        for (ExpressSale sale : response.body()) {
                            if (!sale.isPosted()) filtered.add(sale);
                        }
                        Platform.runLater(() -> {
                            Utility.closeWindow(vbHolder);
                            dataInterface.updateData("The sale has been saved.", filtered.toArray(sales));
                        });
                    }
                } else {
                    Platform.runLater(() -> notificationPane.show(Utility.handleApiErrors(response.message(), response.errorBody(),
                            new String[]{"saleNo", "description", "saleDate", "bankId", "warehouseId", "refNo", "createdBy"})));
                }
            }

            @Override
            public void onFailure(Call<ExpressSale[]> call, Throwable throwable) {
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
                        List<ExpressSale> filtered = new ArrayList<>();
                        ExpressSale[] sales = new ExpressSale[]{};
                        for (ExpressSale sale : response.body()) {
                            if (!sale.isPosted()) filtered.add(sale);
                        }
                        Platform.runLater(() -> {
                            Utility.closeWindow(vbHolder);
                            dataInterface.updateData("The sale has been posted.", filtered.toArray(sales));
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

    private void reverse() {
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

    private void addLine() {
        String errorMessage = "";
        String type = "Product";
        Product object = cbProduct.getValue();
        int typeId = object.getId();
        String unitPrice = tfUnitPrice.getText();
        String quantity = tfQuantity.getText();
        double buyingPrice = 0;


        if (object == null) {
            errorMessage = errorMessage.concat("Select a valid product");
        } else {
            buyingPrice = object.getBuyingPrice();
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
        int q = Integer.parseInt(quantity);

        Call<ExpressSaleLine[]> call = apiService.addSaleLine(expressSale.getId(), type, typeId, buyingPrice, price, q);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ExpressSaleLine[]> call, Response<ExpressSaleLine[]> response) {
                if (response.isSuccessful()) {
                    setLines(response.body());
                    tfUnitPrice.setText("");
                    tfQuantity.setText("");
                } else {
                    Platform.runLater(() -> notificationPane.show(Utility.handleApiErrors(response.message(), response.errorBody(),
                            new String[]{"saleId", "type", "typeId", "unitPrice", "quantity"})));
                }
            }

            @Override
            public void onFailure(Call<ExpressSaleLine[]> call, Throwable throwable) {
                Platform.runLater(() -> notificationPane.show(throwable.getMessage()));
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
        if (expressSale.isPosted()) {
            btnSave.setDisable(true);
            btnPost.setDisable(true);
            btnReverse.setDisable(expressSale.isReversed());
            tvLines.setDisable(true);
            btnAddLine.setDisable(true);
        }
    }

    private void setLines(ExpressSaleLine[] lines) {
        double total = 0;
        for (ExpressSaleLine line : lines) {
            line.setLinesInterface(this);
            total += line.getTotal();
        }
        expressSale.setLines(lines);
        Platform.runLater(() -> {
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
