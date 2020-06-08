package controllers;

import com.google.gson.Gson;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import interfaces.HomeDataInterface;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import models.MyTableColumn;
import models.SuperModel;
import models.customers.Customer;
import models.customers.Receipt;
import models.finance.Bank;
import models.products.*;
import models.vendors.Invoice;
import models.vendors.PaymentVoucher;
import models.vendors.Vendor;
import network.ApiService;
import network.RetrofitBuilder;
import org.controlsfx.control.MaskerPane;
import org.controlsfx.control.Notifications;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import utils.Utility;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;

/**
 * @author kim jose
 * @see javafx.fxml.Initializable
 * <p>
 * This class controls the center scene and loads different data to it.
 ***/

public class GeneralCenter implements Initializable, HomeDataInterface {


    @FXML
    private VBox vbParent;

    @FXML
    private HBox hbActions;

    @FXML
    private Button btnAdd;

    @FXML
    private Button btnEdit;

    @FXML
    private Button btnDelete;

    @FXML
    private Button btnRefresh;

    @FXML
    private HBox hbReports;

    @FXML
    private TextField tfSearch;

    @FXML
    private TableView<SuperModel> tvGeneral;

    @FXML
    private MaskerPane maskerPane;

    private String type;
    private SuperModel[] data;
    private ApiService apiService;
    private Separator separator = new Separator(Orientation.VERTICAL);
    private final Gson gson = new Gson();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        Platform.runLater(this::setupColumns);
        Platform.runLater(this::loadData);
        Platform.runLater(this::reportBtns);
        Platform.runLater(this::firstTabBtns);
        tvGeneral.prefWidthProperty().bind(vbParent.widthProperty());
        tvGeneral.prefHeightProperty().bind(vbParent.heightProperty().subtract(140));

        apiService = RetrofitBuilder.createService(ApiService.class);
        tfSearch.textProperty().addListener((observable, oldValue, newValue) -> searchData(newValue));

        btnAdd.setOnAction(event -> addObject());
        btnEdit.setOnAction(event -> {
            Object o = tvGeneral.getSelectionModel().getSelectedItem();
            if (o == null) createNotification(1, "You must select an item first");
            else editObject(o);
        });
        btnDelete.setOnAction(event -> {
            Object o = tvGeneral.getSelectionModel().getSelectedItem();
            if (o == null) createNotification(1, "You must select an item first");
            else deleteObject(o);
        });
        btnRefresh.setOnAction(event -> loadData());
    }

    @Override
    public void updateData(String message, Object[] data) {
        createNotification(data==null?1:0, message);
        if (data != null) setData((SuperModel[]) data);
    }

    @Override
    public void sellProduct(Product product) {
        if (product.getQuantity() <= 0) {
            createNotification(1, "There is not enough products.");return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getClassLoader().getResource("fxml/sell_product.fxml")));
            VBox vBox = loader.load();
            Scene scene = new Scene(vBox);
            SellProduct sellProduct = loader.getController();
            sellProduct.setDataInterface(this);
            sellProduct.setProduct(product);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.showAndWait();
            stage.setResizable(false);
            loadData();
        } catch (Exception e){
            e.printStackTrace();
            createNotification(-1, "We are unable to load the scene.");
        }
    }

    private void setupColumns() {
        assert type != null;
        System.out.println("Setting up " + type);
        switch (type) {
            case "Products": {
                /**
                 * 'name', 'is_active', 'description', 'sku_code', 'upc_code', 'quantity',
                 *         'price', 'brand', 'category', 'uom', 'image'**/
                tvGeneral.getColumns().removeAll(tvGeneral.getColumns());
                Platform.runLater(() -> {
                    tvGeneral.getColumns().addAll(createColumns(new MyTableColumn[]{
                            new MyTableColumn("Product Name", "name", 0.25),
                            new MyTableColumn("SKU Code", "skuCode", 0.25),
                            new MyTableColumn("UPC Code", "upcCode", 0.25),
                            new MyTableColumn("Quantity", "quantity", 0.10),
                    }));
                    TableColumn sellCol = new TableColumn<>("Sell");
                    sellCol.setCellValueFactory(new PropertyValueFactory<>("sellButton"));
                    sellCol.setPrefWidth(80);
                    sellCol.setResizable(false);
                    sellCol.setReorderable(false);
                    tvGeneral.getColumns().add(sellCol);
                });
                break;
            }
            case "Brands": {
                tvGeneral.getColumns().removeAll(tvGeneral.getColumns());
                TableColumn[] columns = createColumns(new MyTableColumn[]{
                        new MyTableColumn("Brand Id", "id", 0.25),
                        new MyTableColumn("Brand Name", "name", 0.75),
                });
                tvGeneral.getColumns().setAll(columns);
                break;
            }
            case "Categories": {
                tvGeneral.getColumns().removeAll(tvGeneral.getColumns());
                tvGeneral.getColumns().addAll(createColumns(new MyTableColumn[]{
                        new MyTableColumn("Category Id", "id", 0.25),
                        new MyTableColumn("Category Name", "name", 0.75),
                }));
                break;
            }
            case "Units of Measure": {
                tvGeneral.getColumns().removeAll(tvGeneral.getColumns());
                tvGeneral.getColumns().addAll(createColumns(new MyTableColumn[]{
                        new MyTableColumn("UOM Id", "id", 0.10),
                        new MyTableColumn("UOM Name", "name", 0.35),
                        new MyTableColumn("UOM Description", "description", 0.55),
                }));
                break;
            }
            case "Warehouses": {
                tvGeneral.getColumns().removeAll(tvGeneral.getColumns());
                tvGeneral.getColumns().addAll(createColumns(new MyTableColumn[]{
                        new MyTableColumn("Id", "id", 0.10),
                        new MyTableColumn("Name", "name", 0.35),
                        new MyTableColumn("Location", "location", 0.55),
                }));
                break;
            }
            case "Product Groups": {
                tvGeneral.getColumns().removeAll(tvGeneral.getColumns());
                tvGeneral.getColumns().addAll(createColumns(new MyTableColumn[]{
                        new MyTableColumn("Id", "id", 0.10),
                        new MyTableColumn("Name", "name", 0.20),
                        new MyTableColumn("Description", "description", 0.30),
                        new MyTableColumn("Product", "product", 0.20),
                        new MyTableColumn("No of Products", "quantity", 0.10),
                        new MyTableColumn("Price", "price", 0.10),
                }));
                break;
            }
            case "Banks": {//'name', 'branch', 'accountNo','enabled','addedBy','requireRefNo',
                tvGeneral.getColumns().removeAll(tvGeneral.getColumns());
                tvGeneral.getColumns().addAll(createColumns(new MyTableColumn[]{
                        new MyTableColumn("Id", "id", 0.15),
                        new MyTableColumn("Name", "name", 0.20),
                        new MyTableColumn("Branch", "branch", 0.30),
                        new MyTableColumn("Account Number", "accountNo", 0.20),
                        new MyTableColumn("Posting Allowed", "enabledBox", 0.15),
                }));
                break;
            }
            case "Unpackings": {//'groupId', 'quantity', 'productQuantity', 'warehouseId',
                tvGeneral.getColumns().removeAll(tvGeneral.getColumns());
                tvGeneral.getColumns().addAll(createColumns(new MyTableColumn[]{
                        new MyTableColumn("Id", "id", 0.10),
                        new MyTableColumn("Package", "group", 0.30),
                        new MyTableColumn("Warehouse", "warehouse", 0.30),
                        new MyTableColumn("Quantity", "quantity", 0.15),
                        new MyTableColumn("No of Products", "productQuantity", 0.15),
                }));
                break;
            }
            case "Posted Unpackings": {//'groupId', 'quantity', 'productQuantity', 'warehouseId',
                tvGeneral.getColumns().removeAll(tvGeneral.getColumns());
                tvGeneral.getColumns().addAll(createColumns(new MyTableColumn[]{
                        new MyTableColumn("Id", "id", 0.10),
                        new MyTableColumn("Package", "group", 0.20),
                        new MyTableColumn("Warehouse", "warehouse", 0.20),
                        new MyTableColumn("Quantity", "quantity", 0.10),
                        new MyTableColumn("No of Products", "productQuantity", 0.10),
                        new MyTableColumn("Posted On", "postedOn", 0.30),
                }));
                break;
            }
            case "Reversed Unpackings": {//'groupId', 'quantity', 'productQuantity', 'warehouseId',
                tvGeneral.getColumns().removeAll(tvGeneral.getColumns());
                tvGeneral.getColumns().addAll(createColumns(new MyTableColumn[]{
                        new MyTableColumn("Id", "id", 0.10),
                        new MyTableColumn("Package", "group", 0.15),
                        new MyTableColumn("Warehouse", "warehouse", 0.15),
                        new MyTableColumn("Quantity", "quantity", 0.10),
                        new MyTableColumn("No of Products", "productQuantity", 0.10),
                        new MyTableColumn("Posted On", "postedOn", 0.20),
                        new MyTableColumn("Reversed On", "reversedOn", 0.20),
                }));
                break;
            }
            case "Services": {
                tvGeneral.getColumns().removeAll(tvGeneral.getColumns());
                tvGeneral.getColumns().addAll(createColumns(new MyTableColumn[]{
                        new MyTableColumn("Service Id", "id", 0.10),
                        new MyTableColumn("Service Name", "name", 0.35),
                        new MyTableColumn("Description", "description", 0.55),
                }));
                break;
            }
            case "Sales": {
                tvGeneral.getColumns().removeAll(tvGeneral.getColumns());
                tvGeneral.getColumns().addAll(createColumns(new MyTableColumn[]{
                        new MyTableColumn("Sale No", "saleNo", 0.15),
                        new MyTableColumn("Bank", "bank", 0.20),
                        new MyTableColumn("Sale Date", "saleDate", 0.25),
                        new MyTableColumn("Warehouse", "warehouse", 0.20),
                        new MyTableColumn("Total", "totalString", 0.20),
                }));
                break;
            }
            case "Posted Sales": {
                tvGeneral.getColumns().removeAll(tvGeneral.getColumns());
                tvGeneral.getColumns().addAll(createColumns(new MyTableColumn[]{
                        new MyTableColumn("Sale No", "saleNo", 0.15),
                        new MyTableColumn("Bank", "bank", 0.15),
                        new MyTableColumn("Sale Date", "saleDate", 0.20),
                        new MyTableColumn("Warehouse", "warehouse", 0.15),
                        new MyTableColumn("Total", "totalString", 0.15),
                        new MyTableColumn("Posted On", "postedOn", 0.20),
                }));
                break;
            }
            case "Reversed Sales": {
                tvGeneral.getColumns().removeAll(tvGeneral.getColumns());
                tvGeneral.getColumns().addAll(createColumns(new MyTableColumn[]{
                        new MyTableColumn("Sale No", "saleNo", 0.10),
                        new MyTableColumn("Bank", "bank", 0.15),
                        new MyTableColumn("Sale Date", "saleDate", 0.20),
                        new MyTableColumn("Warehouse", "warehouse", 0.15),
                        new MyTableColumn("Total", "totalString", 0.10),
                        new MyTableColumn("Posted On", "postedOn", 0.15),
                        new MyTableColumn("Reversed On", "reversedOn", 0.15),
                }));
                break;
            }


            //Vendors
            case "All Vendors": {
                tvGeneral.getColumns().removeAll(tvGeneral.getColumns());
                tvGeneral.getColumns().addAll(createColumns(new MyTableColumn[]{
                        new MyTableColumn("Vendor Name", "name", 0.30),
                        new MyTableColumn("Email", "email", 0.30),
                        new MyTableColumn("Phone No.", "phone", 0.25),
                        new MyTableColumn("Balance.", "balance", 0.15),
                }));
                break;
            }
            case "Vendor Invoices": {
                tvGeneral.getColumns().removeAll(tvGeneral.getColumns());
                tvGeneral.getColumns().addAll(createColumns(new MyTableColumn[]{
                        new MyTableColumn("Invoice No", "invoiceNo", 0.15),
                        new MyTableColumn("Vendor", "vendor", 0.20),
                        new MyTableColumn("Invoice Date", "invoiceDate", 0.25),
                        new MyTableColumn("Warehouse", "warehouse", 0.20),
                        new MyTableColumn("Total", "total", 0.20),
                }));
                break;
            }
            case "Posted Vendor Invoices": {
                tvGeneral.getColumns().removeAll(tvGeneral.getColumns());
                tvGeneral.getColumns().addAll(createColumns(new MyTableColumn[]{
                        new MyTableColumn("Invoice No", "invoiceNo", 0.15),
                        new MyTableColumn("Vendor", "vendor", 0.15),
                        new MyTableColumn("Invoice Date", "invoiceDate", 0.20),
                        new MyTableColumn("Warehouse", "warehouse", 0.15),
                        new MyTableColumn("Total", "total", 0.15),
                        new MyTableColumn("Posted On", "postedOn", 0.20),
                }));
                break;
            }
            case "Reversed Vendor Invoices": {
                tvGeneral.getColumns().removeAll(tvGeneral.getColumns());
                tvGeneral.getColumns().addAll(createColumns(new MyTableColumn[]{
                        new MyTableColumn("Invoice No", "invoiceNo", 0.10),
                        new MyTableColumn("Vendor", "vendor", 0.15),
                        new MyTableColumn("Invoice Date", "invoiceDate", 0.20),
                        new MyTableColumn("Warehouse", "warehouse", 0.15),
                        new MyTableColumn("Total", "total", 0.10),
                        new MyTableColumn("Posted On", "postedOn", 0.15),
                        new MyTableColumn("Reversed On", "reversedOn", 0.15),
                }));
                break;
            }
            case "Payment Vouchers": {
                tvGeneral.getColumns().removeAll(tvGeneral.getColumns());
                tvGeneral.getColumns().addAll(createColumns(new MyTableColumn[]{
                        new MyTableColumn("Voucher No", "voucherNo", 0.1),
                        new MyTableColumn("Date", "voucherDate", 0.3),
                        new MyTableColumn("Vendor", "vendor", 0.2),
                        new MyTableColumn("Bank", "bank", 0.2),
                        new MyTableColumn("Amount", "amountString", 0.2),
                }));
                break;
            }
            case "Posted Payment Vouchers": {
                tvGeneral.getColumns().removeAll(tvGeneral.getColumns());
                tvGeneral.getColumns().addAll(createColumns(new MyTableColumn[]{
                        new MyTableColumn("Voucher No", "voucherNo", 0.15),
                        new MyTableColumn("Date", "voucherDate", 0.20),
                        new MyTableColumn("Vendor", "vendor", 0.15),
                        new MyTableColumn("Bank", "bank", 0.15),
                        new MyTableColumn("Posted On", "postedOn", 0.20),
                        new MyTableColumn("Amount", "amountString", 0.15),
                }));
                break;
            }
            case "Reversed Payment Vouchers": {
                tvGeneral.getColumns().removeAll(tvGeneral.getColumns());
                tvGeneral.getColumns().addAll(createColumns(new MyTableColumn[]{
                        new MyTableColumn("Voucher No", "voucherNo", 0.1),
                        new MyTableColumn("Date", "voucherDate", 0.20),
                        new MyTableColumn("Vendor", "vendor", 0.15),
                        new MyTableColumn("Bank", "bank", 0.15),
                        new MyTableColumn("Posted On", "postedOn", 0.15),
                        new MyTableColumn("Reversed On", "reversedOn", 0.15),
                        new MyTableColumn("Amount", "amountString", 0.1),
                }));
                break;
            }


            //Customers
            case "All Customers": {
                tvGeneral.getColumns().removeAll(tvGeneral.getColumns());
                tvGeneral.getColumns().addAll(createColumns(new MyTableColumn[]{
                        new MyTableColumn("Customer Name", "name", 0.30),
                        new MyTableColumn("Email", "email", 0.30),
                        new MyTableColumn("Phone No.", "phone", 0.25),
                        new MyTableColumn("Balance", "balanceString", 0.15),
                }));
                break;
            }
            case "Customer Invoices": {
                tvGeneral.getColumns().removeAll(tvGeneral.getColumns());
                tvGeneral.getColumns().addAll(createColumns(new MyTableColumn[]{
                        new MyTableColumn("Invoice No", "invoiceNo", 0.15),
                        new MyTableColumn("Customer", "customer", 0.20),
                        new MyTableColumn("Invoice Date", "invoiceDate", 0.25),
                        new MyTableColumn("Warehouse", "warehouse", 0.20),
                        new MyTableColumn("Total", "total", 0.20),
                }));
                break;
            }
            case "Posted Customer Invoices": {
                tvGeneral.getColumns().removeAll(tvGeneral.getColumns());
                tvGeneral.getColumns().addAll(createColumns(new MyTableColumn[]{
                        new MyTableColumn("Invoice No", "invoiceNo", 0.15),
                        new MyTableColumn("Customer", "customer", 0.15),
                        new MyTableColumn("Invoice Date", "invoiceDate", 0.20),
                        new MyTableColumn("Warehouse", "warehouse", 0.15),
                        new MyTableColumn("Total", "total", 0.15),
                        new MyTableColumn("Posted On", "postedOn", 0.20),
                }));
                break;
            }
            case "Reversed Customer Invoices": {
                tvGeneral.getColumns().removeAll(tvGeneral.getColumns());
                tvGeneral.getColumns().addAll(createColumns(new MyTableColumn[]{
                        new MyTableColumn("Invoice No", "invoiceNo", 0.10),
                        new MyTableColumn("Customer", "customer", 0.15),
                        new MyTableColumn("Invoice Date", "invoiceDate", 0.20),
                        new MyTableColumn("Warehouse", "warehouse", 0.15),
                        new MyTableColumn("Total", "total", 0.10),
                        new MyTableColumn("Posted On", "postedOn", 0.15),
                        new MyTableColumn("Reversed On", "reversedOn", 0.15),
                }));
                break;
            }
            case "Receipts": {
                tvGeneral.getColumns().removeAll(tvGeneral.getColumns());
                tvGeneral.getColumns().addAll(createColumns(new MyTableColumn[]{
                        new MyTableColumn("Receipt No", "no", 0.1),
                        new MyTableColumn("Date", "receiptDate", 0.3),
                        new MyTableColumn("Customer", "customer", 0.2),
                        new MyTableColumn("Bank", "bank", 0.2),
                        new MyTableColumn("Amount", "amountString", 0.2),
                }));
                break;
            }
            case "Posted Receipts": {
                tvGeneral.getColumns().removeAll(tvGeneral.getColumns());
                tvGeneral.getColumns().addAll(createColumns(new MyTableColumn[]{
                        new MyTableColumn("Voucher No", "no", 0.15),
                        new MyTableColumn("Date", "receiptDate", 0.20),
                        new MyTableColumn("Customer", "customer", 0.15),
                        new MyTableColumn("Bank", "bank", 0.15),
                        new MyTableColumn("Posted On", "postedOn", 0.20),
                        new MyTableColumn("Amount", "amountString", 0.15),
                }));
                break;
            }
            case "Reversed Receipts": {
                tvGeneral.getColumns().removeAll(tvGeneral.getColumns());
                tvGeneral.getColumns().addAll(createColumns(new MyTableColumn[]{
                        new MyTableColumn("Receipt No", "no", 0.1),
                        new MyTableColumn("Date", "receiptDate", 0.20),
                        new MyTableColumn("Customer", "customer", 0.15),
                        new MyTableColumn("Bank", "bank", 0.15),
                        new MyTableColumn("Posted On", "postedOn", 0.15),
                        new MyTableColumn("Reversed On", "reversedOn", 0.15),
                        new MyTableColumn("Amount", "amountString", 0.1),
                }));
                break;
            }
        }
    }

    private void reportBtns() {
        switch (type) {
            case "All Customers": {
                FontAwesomeIconView icon = new FontAwesomeIconView(FontAwesomeIcon.FILE_PDF_ALT);
                icon.setFill(Paint.valueOf("#800000"));
                CustomButton customButton = new CustomButton("Customer Report", icon);
                customButton.setOnAction(event -> {
                    Object o = tvGeneral.getSelectionModel().getSelectedItem();
                    if (o == null) {
                        createNotification(1, "You must select a customer first.");
                        return;
                    }
                    Customer c = (Customer) o;
                });
                hbReports.getChildren().add(customButton);
            }
            case "Posted Sales": {
                FontAwesomeIconView icon = new FontAwesomeIconView(FontAwesomeIcon.FILE_PDF_ALT);
                icon.setFill(Paint.valueOf("#800000"));
                CustomButton customButton = new CustomButton("Sale Report", icon);
                customButton.setOnAction(event -> {
                    Object o = tvGeneral.getSelectionModel().getSelectedItem();
                    if (o == null) {
                        createNotification(1, "You must select a sale first.");
                        return;
                    }
                    ExpressSale sale = (ExpressSale) o;
                    HashMap<String, Object> params = new LinkedHashMap<>();
                    params.put("saleNo", sale.getSaleNo());
                    params.put("saleDate", sale.getSaleDate());
                    params.put("saleTotal", "Kshs " + sale.getTotalString());
                    Node n = Utility.showProgressBar("Getting report...");
                    Call<Object[]> call = apiService.saleReport(sale.getId());
                    call.enqueue(new Callback<>() {
                        @Override
                        public void onResponse(Call<Object[]> call, Response<Object[]> response) {
                            Platform.runLater(() -> {
                                Utility.closeWindow(n);
                                if (response.isSuccessful())
                                    Utility.showReport("sale_report.jasper", params, gson.toJson(response.body()));
                                else createNotification(-1, response.message());
                            });
                        }

                        @Override
                        public void onFailure(Call<Object[]> call, Throwable throwable) {
                            Utility.closeWindow(n);
                            createNotification(-1, throwable.getMessage());
                        }
                    });
                });
                CustomButton salesReport = new CustomButton("General Report", icon);
                salesReport.setOnAction(event -> {
                    Dialog dialog = new Dialog<>();
                    DialogPane pane = null;
                    try {
                        pane = FXMLLoader.load(getClass().getClassLoader().getResource("fxml/date_filter.fxml"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    ButtonType nextButtonType = new ButtonType("Go", ButtonBar.ButtonData.OK_DONE);
                    ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
                    pane.getButtonTypes().addAll(nextButtonType, cancelButtonType);
                    Node nextButton = pane.lookupButton(nextButtonType);
                    Node cancelButton = pane.lookupButton(cancelButtonType);
                    nextButton.setStyle("-fx-background-color:#19D019;");
                    cancelButton.setStyle("-fx-background-color:#f80707; -fx-text-fill: white;");
                    VBox parent = (VBox) pane.getContent();
                    DatePicker startDatePicker = (DatePicker) ((HBox)parent.getChildren().get(1)).getChildren().get(1);
                    startDatePicker.setValue(LocalDate.now());
                    DatePicker endDatePicker = (DatePicker) ((HBox)parent.getChildren().get(2)).getChildren().get(1);
                    endDatePicker.setValue(LocalDate.now());
                    nextButton.addEventFilter(ActionEvent.ACTION, event1 -> {
                        try {
                            String startDate = startDatePicker.getValue().toString();
                            String endDate = endDatePicker.getValue().toString();
                            if (startDate.equals("")||endDate.equals("")) throw new Exception("Invalid date values");
                            dialog.close();

                            Node n = Utility.showProgressBar("Getting report...");
                            Call<Object[]> call = apiService.salesReport(startDate, endDate);
                            call.enqueue(new Callback<>() {
                                @Override
                                public void onResponse(Call<Object[]> call, Response<Object[]> response) {
                                    Platform.runLater(() -> {
                                        if (response.isSuccessful()) {
                                            Utility.closeWindow(n);
                                            if (response.body().length == 0){
                                                createNotification(-1, "No data found for this period.");
                                            }
                                            Utility.showReport("sales_report.jasper", null, gson.toJson(response.body()));
                                        }
                                    });
                                }

                                @Override
                                public void onFailure(Call<Object[]> call, Throwable throwable) {
                                    Platform.runLater(() -> createNotification(-1, throwable.getMessage()));
                                }
                            });

                        } catch (Exception e){
                            e.printStackTrace();
                            dialog.close();
                            createNotification(-1, "We are unable to proceed with your request.");
                        }
                    });
                    cancelButton.addEventFilter(ActionEvent.ACTION, event1 -> dialog.close());
                    dialog.setDialogPane(pane);
                    dialog.show();
                });
                hbReports.getChildren().addAll(customButton, separator, salesReport);
            }
        }
    }

    private void firstTabBtns() {
        switch (type) {
            case "Products":{
                FontAwesomeIconView fontAwesomeIconView = new FontAwesomeIconView(FontAwesomeIcon.PLUS_SQUARE_ALT);
                fontAwesomeIconView.setFill(Paint.valueOf("#19D019"));
                CustomButton customButton = new CustomButton("Add Stock", fontAwesomeIconView);
                customButton.setOnAction(event -> {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("fxml/add_to_stock.fxml"));
                        VBox vBox = loader.load();
                        AddToStock addToStock = loader.getController();
                        addToStock.setDataInterface(this);
                        Scene scene = new Scene(vBox);
                        Stage stage = new Stage();
                        stage.setScene(scene);
                        stage.setResizable(false);
                        stage.initModality(Modality.APPLICATION_MODAL);
                        stage.setTitle("Add To Stock");
                        stage.show();
                    } catch (Exception e){
                        e.printStackTrace();
                        createNotification(-1, "Error encountered. Consult ICT.");
                    }
                });
                hbActions.getChildren().addAll(customButton, separator);
            }
        }
    }


    /**
     * This method searches the table data
     *
     * @param searchString string to be searched
     **/
    private void searchData(String searchString) {
        if (searchString.trim().equals("")) {
            tvGeneral.setItems(FXCollections.observableArrayList(data));
            return;
        }
        List<SuperModel> filtered = new ArrayList<>();
        for (SuperModel model : data) {
            if (model.getSearchString().contains(searchString)) filtered.add(model);
        }
        tvGeneral.setItems(FXCollections.observableArrayList(filtered));
    }

    /**
     * This method fetches data from the server and loads it to a table
     ***/
    private void loadData() {
        maskerPane.setVisible(true);
        assert type != null;
        switch (type) {
            //for shop
            case "Products": {
                Call<Product[]> call = apiService.products();
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<Product[]> call, Response<Product[]> response) {
                        if (response.isSuccessful()) {
                            for (Product p: response.body()) {
                                p.setDataInterface(GeneralCenter.this);
                            }
                            setData(response.body());
                        } else {
                            tvGeneral.setItems(FXCollections.emptyObservableList());
                        }
                        Platform.runLater(() -> maskerPane.setVisible(false));
                    }

                    @Override
                    public void onFailure(Call<Product[]> call, Throwable throwable) {
                        createNotification(-1, throwable.getMessage());
                    }
                });
                break;
            }
            case "Brands": {
                Call<Brand[]> call = apiService.brands();
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<Brand[]> call, Response<Brand[]> response) {
                        System.out.println(response.body());
                        if (response.isSuccessful()) {
                            setData(response.body());
                        } else {
                            tvGeneral.setItems(FXCollections.emptyObservableList());
                        }
                    }

                    @Override
                    public void onFailure(Call<Brand[]> call, Throwable throwable) {
                        createNotification(-1, throwable.getMessage());
                    }
                });
                break;
            }
            case "Categories": {
                Call<Category[]> call = apiService.categories();
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<Category[]> call, Response<Category[]> response) {
                        if (response.isSuccessful()) {
                            setData(response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<Category[]> call, Throwable throwable) {
                        createNotification(-1, throwable.getMessage());
                    }
                });
                break;
            }
            case "Units of Measure": {
                Call<UnitOfMeasure[]> call = apiService.unitsOfMeasure();
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<UnitOfMeasure[]> call, Response<UnitOfMeasure[]> response) {
                        if (response.isSuccessful()) setData(response.body());
                    }

                    @Override
                    public void onFailure(Call<UnitOfMeasure[]> call, Throwable throwable) {
                        createNotification(-1, throwable.getMessage());
                    }
                });
                break;
            }
            case "Warehouses": {
                Call<Warehouse[]> call = apiService.warehouses();
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<Warehouse[]> call, Response<Warehouse[]> response) {
                        if (response.isSuccessful()) setData(response.body());
                    }

                    @Override
                    public void onFailure(Call<Warehouse[]> call, Throwable throwable) {
                        createNotification(-1, throwable.getMessage());
                    }
                });
                break;
            }
            case "Product Groups": {
                Call<ProductGroup[]> call = apiService.productGroups();
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<ProductGroup[]> call, Response<ProductGroup[]> response) {
                        if (response.isSuccessful()) setData(response.body());
                        else createNotification(-1, response.message());
                    }

                    @Override
                    public void onFailure(Call<ProductGroup[]> call, Throwable throwable) {
                        createNotification(-1, throwable.getMessage());
                    }
                });
                break;
            }
            case "Unpackings":
            case "Posted Unpackings":
            case "Reversed Unpackings": {
                Call<Unpacking[]> call = apiService.unpackings();
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<Unpacking[]> call, Response<Unpacking[]> response) {
                        if (response.isSuccessful()) {
                            Unpacking[] unpackings = {};
                            List<Unpacking> filtered = new ArrayList<>();
                            for (Unpacking u : response.body()) {
                                switch (type) {
                                    case "Unpackings":
                                        if (!u.isPosted()) filtered.add(u);
                                        break;
                                    case "Posted Unpackings":
                                        if (u.isPosted() && !u.isReversed()) filtered.add(u);
                                        break;
                                    case "Reversed Unpackings":
                                        if (u.isReversed()) filtered.add(u);
                                        break;
                                }
                            }
                            setData(filtered.toArray(unpackings));
                        } else createNotification(-1, response.message());
                    }

                    @Override
                    public void onFailure(Call<Unpacking[]> call, Throwable throwable) {
                        createNotification(-1, throwable.getMessage());
                    }
                });
                break;
            }
            case "Banks": {
                Call<Bank[]> call = apiService.banks();
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<Bank[]> call, Response<Bank[]> response) {
                        if (response.isSuccessful()) setData(response.body());
                        else createNotification(-1, response.message());
                        Platform.runLater(() -> maskerPane.setVisible(false));
                    }

                    @Override
                    public void onFailure(Call<Bank[]> call, Throwable throwable) {
                        createNotification(-1, throwable.getMessage());
                    }
                });
                break;
            }
            case "Services": {
                Call<Service[]> call = apiService.services();
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<Service[]> call, Response<Service[]> response) {
                        if (response.isSuccessful()) setData(response.body());
                        else createNotification(-1, response.message());
                    }

                    @Override
                    public void onFailure(Call<Service[]> call, Throwable throwable) {
                        createNotification(-1, throwable.getMessage());
                    }
                });
                break;
            }
            case "Sales":
            case "Posted Sales":
            case "Reversed Sales": {
                Call<ExpressSale[]> call = apiService.expressSales();
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<ExpressSale[]> call, Response<ExpressSale[]> response) {
                        if (response.isSuccessful()) {
                            ExpressSale[] sales = {};
                            List<ExpressSale> filtered = new ArrayList<>();
                            for (ExpressSale sale : response.body()) {
                                switch (type) {
                                    case "Sales":
                                        if (!sale.isPosted()) filtered.add(sale);
                                        break;
                                    case "Posted Sales":
                                        if (sale.isPosted() && !sale.isReversed()) filtered.add(sale);
                                        break;
                                    case "Reversed Sales":
                                        if (sale.isReversed()) filtered.add(sale);
                                        break;
                                }
                            }
                            setData(filtered.toArray(sales));
                        } else {
                            createNotification(-1, response.message());
                        }
                        Platform.runLater(() -> maskerPane.setVisible(false));
                    }

                    @Override
                    public void onFailure(Call<ExpressSale[]> call, Throwable throwable) {
                        createNotification(-1, throwable.getMessage());
                    }
                });
                break;
            }

            //Vendors
            case "All Vendors": {
                Call<Vendor[]> call = apiService.vendors();
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<Vendor[]> call, Response<Vendor[]> response) {
                        if (response.isSuccessful()) {
                            setData(response.body());
                        } else {
                            createNotification(-1, response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<Vendor[]> call, Throwable throwable) {
                        createNotification(-1, throwable.getMessage());
                    }
                });
                break;
            }
            case "Vendor Invoices": {
                Call<models.vendors.Invoice[]> call = apiService.vendorInvoices();
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<Invoice[]> call, Response<Invoice[]> response) {
                        if (response.isSuccessful()) {
                            Invoice[] invoices = {};
                            List<Invoice> filtered = new ArrayList<>();
                            for (Invoice i : response.body()) {
                                if (!i.isPosted()) filtered.add(i);
                            }
                            setData(filtered.toArray(invoices));
                        } else createNotification(-1, response.message());
                    }

                    @Override
                    public void onFailure(Call<Invoice[]> call, Throwable throwable) {
                        createNotification(-1, throwable.getMessage());
                    }
                });
                break;
            }
            case "Posted Vendor Invoices": {
                Call<models.vendors.Invoice[]> call = apiService.vendorInvoices();
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<Invoice[]> call, Response<Invoice[]> response) {
                        if (response.isSuccessful()) {
                            Invoice[] invoices = {};
                            List<Invoice> filtered = new ArrayList<>();
                            for (Invoice i : response.body()) {
                                if (i.isPosted() && !i.isReversed()) filtered.add(i);
                            }
                            setData(filtered.toArray(invoices));
                        } else createNotification(-1, response.message());
                    }

                    @Override
                    public void onFailure(Call<Invoice[]> call, Throwable throwable) {
                        createNotification(-1, throwable.getMessage());
                    }
                });
                break;
            }
            case "Reversed Vendor Invoices": {
                Call<models.vendors.Invoice[]> call = apiService.vendorInvoices();
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<Invoice[]> call, Response<Invoice[]> response) {
                        if (response.isSuccessful()) {
                            Invoice[] invoices = {};
                            List<Invoice> filtered = new ArrayList<>();
                            for (Invoice i : response.body()) {
                                if (i.isPosted() && i.isReversed()) filtered.add(i);
                            }
                            setData(filtered.toArray(invoices));
                        } else createNotification(-1, response.message());
                    }

                    @Override
                    public void onFailure(Call<Invoice[]> call, Throwable throwable) {
                        createNotification(-1, throwable.getMessage());
                    }
                });
                break;
            }
            case "Payment Vouchers": {
                Call<PaymentVoucher[]> call = apiService.paymentVouchers();
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<PaymentVoucher[]> call, Response<PaymentVoucher[]> response) {
                        if (response.isSuccessful()) {
                            List<PaymentVoucher> filtered = new ArrayList<>();
                            for (PaymentVoucher voucher : response.body()) {
                                if (!voucher.isPosted()) filtered.add(voucher);
                            }
                            Platform.runLater(() -> tvGeneral.setItems(FXCollections.observableArrayList(filtered)));
                        } else createNotification(-1, response.message());
                    }

                    @Override
                    public void onFailure(Call<PaymentVoucher[]> call, Throwable throwable) {
                        createNotification(-1, throwable.getMessage());
                    }
                });
                break;
            }
            case "Posted Payment Vouchers": {
                Call<PaymentVoucher[]> call = apiService.paymentVouchers();
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<PaymentVoucher[]> call, Response<PaymentVoucher[]> response) {
                        if (response.isSuccessful()) {
                            List<PaymentVoucher> filtered = new ArrayList<>();
                            for (PaymentVoucher voucher : response.body()) {
                                if (voucher.isPosted() && !voucher.isReversed()) filtered.add(voucher);
                            }
                            Platform.runLater(() -> tvGeneral.setItems(FXCollections.observableArrayList(filtered)));
                        } else createNotification(-1, response.message());
                    }

                    @Override
                    public void onFailure(Call<PaymentVoucher[]> call, Throwable throwable) {
                        createNotification(-1, throwable.getMessage());
                    }
                });
                break;
            }
            case "Reversed Payment Vouchers": {
                Call<PaymentVoucher[]> call = apiService.paymentVouchers();
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<PaymentVoucher[]> call, Response<PaymentVoucher[]> response) {
                        if (response.isSuccessful()) {
                            List<PaymentVoucher> filtered = new ArrayList<>();
                            for (PaymentVoucher voucher : response.body()) {
                                if (voucher.isReversed()) filtered.add(voucher);
                            }
                            Platform.runLater(() -> tvGeneral.setItems(FXCollections.observableArrayList(filtered)));
                        } else createNotification(-1, response.message());
                    }

                    @Override
                    public void onFailure(Call<PaymentVoucher[]> call, Throwable throwable) {
                        createNotification(-1, throwable.getMessage());
                    }
                });
                break;
            }


            //Customers
            case "All Customers": {
                Call<Customer[]> call = apiService.customers();
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<Customer[]> call, Response<Customer[]> response) {
                        if (response.isSuccessful()) {
                            setData(response.body());
                        } else {
                            createNotification(-1, response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<Customer[]> call, Throwable throwable) {
                        createNotification(-1, throwable.getMessage());
                    }
                });
                break;
            }
            case "Customer Invoices": {
                Call<models.customers.Invoice[]> call = apiService.customerInvoices();
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<models.customers.Invoice[]> call, Response<models.customers.Invoice[]> response) {
                        if (response.isSuccessful()) {
                            models.customers.Invoice[] invoices = {};
                            List<models.customers.Invoice> filtered = new ArrayList<>();
                            for (models.customers.Invoice i : response.body()) {
                                if (!i.isPosted()) filtered.add(i);
                            }
                            setData(filtered.toArray(invoices));
                        } else {
                            createNotification(-1, response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<models.customers.Invoice[]> call, Throwable throwable) {
                        createNotification(-1, throwable.getMessage());
                    }
                });
                break;
            }
            case "Posted Customer Invoices": {
                Call<models.customers.Invoice[]> call = apiService.customerInvoices();
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<models.customers.Invoice[]> call, Response<models.customers.Invoice[]> response) {
                        if (response.isSuccessful()) {
                            models.customers.Invoice[] invoices = {};
                            List<models.customers.Invoice> filtered = new ArrayList<>();
                            for (models.customers.Invoice i : response.body()) {
                                if (i.isPosted() && !i.isReversed()) filtered.add(i);
                            }
                            setData(filtered.toArray(invoices));
                        } else {
                            createNotification(-1, response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<models.customers.Invoice[]> call, Throwable throwable) {
                        createNotification(-1, throwable.getMessage());
                    }
                });
                break;
            }
            case "Reversed Customer Invoices": {
                Call<models.customers.Invoice[]> call = apiService.customerInvoices();
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<models.customers.Invoice[]> call, Response<models.customers.Invoice[]> response) {
                        if (response.isSuccessful()) {
                            models.customers.Invoice[] invoices = {};
                            List<models.customers.Invoice> filtered = new ArrayList<>();
                            for (models.customers.Invoice i : response.body()) {
                                if (i.isReversed()) filtered.add(i);
                            }
                            setData(filtered.toArray(invoices));
                        } else {
                            createNotification(-1, response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<models.customers.Invoice[]> call, Throwable throwable) {
                        createNotification(-1, throwable.getMessage());
                    }
                });
                break;
            }
            case "Receipts": {
                Call<Receipt[]> call = apiService.receipts();
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<Receipt[]> call, Response<Receipt[]> response) {
                        if (response.isSuccessful()) {
                            List<Receipt> filtered = new ArrayList<>();
                            for (Receipt r : response.body()) {
                                if (!r.isPosted()) filtered.add(r);
                            }
                            Platform.runLater(() -> tvGeneral.setItems(FXCollections.observableArrayList(filtered)));
                        } else createNotification(-1, response.message());
                    }

                    @Override
                    public void onFailure(Call<Receipt[]> call, Throwable throwable) {
                        createNotification(-1, throwable.getMessage());
                    }
                });
                break;
            }
            case "Posted Receipts": {
                Call<Receipt[]> call = apiService.receipts();
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<Receipt[]> call, Response<Receipt[]> response) {
                        if (response.isSuccessful()) {
                            List<Receipt> filtered = new ArrayList<>();
                            for (Receipt r : response.body()) {
                                if (r.isPosted() && !r.isReversed()) filtered.add(r);
                            }
                            Platform.runLater(() -> tvGeneral.setItems(FXCollections.observableArrayList(filtered)));
                        } else createNotification(-1, response.message());
                    }

                    @Override
                    public void onFailure(Call<Receipt[]> call, Throwable throwable) {
                        createNotification(-1, throwable.getMessage());
                    }
                });
                break;
            }
            case "Reversed Receipts": {
                Call<Receipt[]> call = apiService.receipts();
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<Receipt[]> call, Response<Receipt[]> response) {
                        if (response.isSuccessful()) {
                            List<Receipt> filtered = new ArrayList<>();
                            for (Receipt r : response.body()) {
                                if (r.isReversed()) filtered.add(r);
                            }
                            Platform.runLater(() -> tvGeneral.setItems(FXCollections.observableArrayList(filtered)));
                        } else createNotification(-1, response.message());
                    }

                    @Override
                    public void onFailure(Call<Receipt[]> call, Throwable throwable) {
                        createNotification(-1, throwable.getMessage());
                    }
                });
                break;
            }
        }
    }

    /**
     * This method triggers a scene that adds an object
     ****/
    private void addObject() {
        try {
            switch (type) {
                case "Brands": {

                    FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getClassLoader().getResource("fxml/create_brand.fxml")));
                    VBox vBox = loader.load();
                    Scene scene = new Scene(vBox, 450, 200);
                    Stage stage = new Stage();
                    stage.setScene(scene);
                    CreateBrand createBrand = loader.getController();
                    createBrand.setDataInterface(this);
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.setResizable(false);
                    stage.setTitle("Create Brand");
                    stage.show();

                    break;
                }
                case "Warehouses": {

                    FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getClassLoader().getResource("fxml/create_warehouse.fxml")));
                    VBox vBox = loader.load();
                    Scene scene = new Scene(vBox, 450, 200);
                    Stage stage = new Stage();
                    stage.setScene(scene);
                    CreateWarehouse createWarehouse = loader.getController();
                    createWarehouse.setDataInterface(this);
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.setResizable(false);
                    stage.setTitle("Create Warehouse");
                    stage.show();

                    break;
                }
                case "Categories": {

                    FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getClassLoader().getResource("fxml/create_category.fxml")));
                    VBox vBox = loader.load();
                    Scene scene = new Scene(vBox, 450, 200);
                    Stage stage = new Stage();
                    stage.setScene(scene);
                    CreateCategory controller = loader.getController();
                    controller.setDataInterface(this);
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.setResizable(false);
                    stage.setTitle("Create Category");
                    stage.show();

                    break;
                }
                case "Units of Measure": {

                    FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getClassLoader().getResource("fxml/create_uom.fxml")));
                    VBox vBox = loader.load();
                    Scene scene = new Scene(vBox, 450, 200);
                    Stage stage = new Stage();
                    stage.setScene(scene);
                    CreateUOM controller = loader.getController();
                    controller.setDataInterface(this);
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.setResizable(false);
                    stage.setTitle("Create Unit Of Measure");
                    stage.show();

                    break;
                }
                case "Products": {

                    FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getClassLoader().getResource("fxml/create_product.fxml")));
                    VBox vBox = loader.load();
                    Scene scene = new Scene(vBox, 870, 370);
                    Stage stage = new Stage();
                    stage.setScene(scene);
                    CreateProduct controller = loader.getController();
                    controller.setDataInterface(this);
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.setResizable(false);
                    stage.setTitle("Create Product");
                    stage.show();

                    break;
                }
                case "Product Groups": {
                    FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getClassLoader().getResource("fxml/create_product_group.fxml")));
                    VBox vBox = loader.load();
                    Scene scene = new Scene(vBox);
                    Stage stage = new Stage();
                    stage.setScene(scene);
                    ProductGroupController controller = loader.getController();
                    controller.setDataInterface(this);
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.setResizable(false);
                    stage.setTitle("Create Product Group");
                    stage.show();
                    break;
                }
                case "Banks": {
                    FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getClassLoader().getResource("fxml/create_bank.fxml")));
                    VBox vBox = loader.load();
                    Scene scene = new Scene(vBox);
                    Stage stage = new Stage();
                    stage.setScene(scene);
                    BankController controller = loader.getController();
                    controller.setDataInterface(this);
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.setResizable(false);
                    stage.setTitle("Create Bank");
                    stage.show();
                    break;
                }
                case "Unpackings": {
                    FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getClassLoader().getResource("fxml/create_unpacking.fxml")));
                    VBox vBox = loader.load();
                    Scene scene = new Scene(vBox);
                    Stage stage = new Stage();
                    stage.setScene(scene);
                    UnpackingController controller = loader.getController();
                    controller.setDataInterface(this);
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.setResizable(false);
                    stage.setTitle("Unpack Product");
                    stage.show();
                    break;
                }
                case "Services": {

                    FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getClassLoader().getResource("fxml/create_service.fxml")));
                    VBox vBox = loader.load();
                    Scene scene = new Scene(vBox, 450, 200);
                    Stage stage = new Stage();
                    stage.setScene(scene);
                    CreateService controller = loader.getController();
                    controller.setDataInterface(this);
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.setResizable(false);
                    stage.setTitle("Create Service");
                    stage.show();

                    break;
                }
                case "Sales": {
                    FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getClassLoader().getResource("fxml/express_sale.fxml")));
                    VBox vBox = loader.load();
                    Scene scene = new Scene(vBox, 730, 460);
                    Stage stage = new Stage();
                    stage.setScene(scene);
                    ExpressSaleController controller = loader.getController();
                    controller.setDataInterface(this);
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.setResizable(false);
                    stage.setTitle("Create Sale");
                    stage.show();

                    break;
                }
                case "All Customers": {

                    FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getClassLoader().getResource("fxml/create_customer.fxml")));
                    VBox vBox = loader.load();
                    Scene scene = new Scene(vBox, 450, 200);
                    Stage stage = new Stage();
                    stage.setScene(scene);
                    CreateCustomer controller = loader.getController();
                    controller.setDataInterface(this);
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.setResizable(false);
                    stage.setTitle("Create Customer");
                    stage.show();

                    break;
                }
                case "All Vendors": {

                    FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getClassLoader().getResource("fxml/create_vendor.fxml")));
                    VBox vBox = loader.load();
                    Scene scene = new Scene(vBox, 450, 200);
                    Stage stage = new Stage();
                    stage.setScene(scene);
                    CreateVendor controller = loader.getController();
                    controller.setDataInterface(this);
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.setResizable(false);
                    stage.setTitle("Create Vendor");
                    stage.show();

                    break;
                }
                case "Vendor Invoices": {

                    FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getClassLoader().getResource("fxml/invoices_creator.fxml")));
                    VendorInvoice controller = new VendorInvoice();
                    loader.setController(controller);
                    VBox vBox = loader.load();
                    Scene scene = new Scene(vBox, 730, 460);
                    Stage stage = new Stage();
                    stage.setScene(scene);
                    controller.setDataInterface(this);
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.setResizable(false);
                    stage.setTitle("Create Vendor Invoice");
                    stage.show();

                    break;
                }
                case "Payment Vouchers": {

                    FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getClassLoader().getResource("fxml/payment_voucher.fxml")));
                    VBox vBox = loader.load();
                    PaymentVoucherController controller = loader.getController();
                    Scene scene = new Scene(vBox, 490, 320);
                    Stage stage = new Stage();
                    stage.setScene(scene);
                    controller.setDataInterface(this);
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.setResizable(false);
                    stage.setTitle("Create Payment Voucher");
                    stage.show();

                    break;
                }
                case "Customer Invoices": {

                    FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getClassLoader().getResource("fxml/invoices_creator.fxml")));
                    CustomerInvoice controller = new CustomerInvoice();
                    loader.setController(controller);
                    VBox vBox = loader.load();
                    Scene scene = new Scene(vBox, 730, 460);
                    Stage stage = new Stage();
                    stage.setScene(scene);
                    controller.setDataInterface(this);
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.setResizable(false);
                    stage.setTitle("Create Customer Invoice");
                    stage.show();

                    break;
                }
                case "Receipts": {

                    FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getClassLoader().getResource("fxml/customer_receipt.fxml")));
                    VBox vBox = loader.load();
                    CustomerReceipt controller = loader.getController();
                    controller.setDataInterface(this);
                    Scene scene = new Scene(vBox, 490, 320);
                    Stage stage = new Stage();
                    stage.setScene(scene);
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.setResizable(false);
                    stage.setTitle("Create Customer Receipt");
                    stage.show();

                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            createNotification(-1, "The program is unable to properly load new view.");
        }
    }

    /**
     * This method triggers a interface that edits/updates an object
     *
     * @param object the object being edited
     **/
    private void editObject(Object object) {
        try {

            switch (type) {
                case "Brands": {

                    FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getClassLoader().getResource("fxml/create_brand.fxml")));
                    VBox vBox = loader.load();
                    Scene scene = new Scene(vBox, 450, 200);
                    Stage stage = new Stage();
                    stage.setScene(scene);
                    CreateBrand createBrand = loader.getController();
                    createBrand.setDataInterface(this);
                    createBrand.setBrand((Brand) object);
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.setResizable(false);
                    stage.setTitle("Edit Brand");
                    stage.show();

                    break;
                }
                case "Warehouses": {

                    FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getClassLoader().getResource("fxml/create_warehouse.fxml")));
                    VBox vBox = loader.load();
                    Scene scene = new Scene(vBox, 450, 200);
                    Stage stage = new Stage();
                    stage.setScene(scene);
                    CreateWarehouse controller = loader.getController();
                    controller.setDataInterface(this);
                    controller.setWarehouse((Warehouse) object);
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.setResizable(false);
                    stage.setTitle("Edit Warehouse");
                    stage.show();

                    break;
                }
                case "Categories": {

                    FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getClassLoader().getResource("fxml/create_category.fxml")));
                    VBox vBox = loader.load();
                    Scene scene = new Scene(vBox, 450, 200);
                    Stage stage = new Stage();
                    stage.setScene(scene);
                    CreateCategory controller = loader.getController();
                    controller.setDataInterface(this);
                    controller.setCategory((Category) object);
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.setResizable(false);
                    stage.setTitle("Edit Category");
                    stage.show();

                    break;
                }
                case "Units of Measure": {

                    FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getClassLoader().getResource("fxml/create_uom.fxml")));
                    VBox vBox = loader.load();
                    Scene scene = new Scene(vBox, 450, 200);
                    Stage stage = new Stage();
                    stage.setScene(scene);
                    CreateUOM controller = loader.getController();
                    controller.setDataInterface(this);
                    controller.setUom((UnitOfMeasure) object);
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.setResizable(false);
                    stage.setTitle("Edit unit of measure");
                    stage.show();

                    break;
                }
                case "Products": {

                    FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getClassLoader().getResource("fxml/create_product.fxml")));
                    VBox vBox = loader.load();//, 870, 370
                    Scene scene = new Scene(vBox);
                    Stage stage = new Stage();
                    stage.setScene(scene);
                    CreateProduct controller = loader.getController();
                    controller.setDataInterface(this);
                    controller.setProduct((Product) object);
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.setResizable(false);
                    stage.setTitle("Edit Product");
                    stage.show();

                    break;
                }
                case "Product Groups": {
                    FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getClassLoader().getResource("fxml/create_product_group.fxml")));
                    VBox vBox = loader.load();//, 870, 370
                    Scene scene = new Scene(vBox);
                    Stage stage = new Stage();
                    stage.setScene(scene);
                    ProductGroupController controller = loader.getController();
                    controller.setDataInterface(this);
                    controller.setGroup((ProductGroup) object);
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.setResizable(false);
                    stage.setTitle("Edit Group");
                    stage.show();

                    break;
                }
                case "Banks": {
                    FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getClassLoader().getResource("fxml/create_bank.fxml")));
                    VBox vBox = loader.load();//, 870, 370
                    Scene scene = new Scene(vBox);
                    Stage stage = new Stage();
                    stage.setScene(scene);
                    BankController controller = loader.getController();
                    controller.setDataInterface(this);
                    controller.setBank((Bank) object);
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.setResizable(false);
                    stage.setTitle("Edit/View Bank");
                    stage.show();

                    break;
                }
                case "Unpackings":
                case "Posted Unpackings":
                case "Reversed Unpackings": {
                    FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getClassLoader().getResource("fxml/create_unpacking.fxml")));
                    VBox vBox = loader.load();//, 870, 370
                    Scene scene = new Scene(vBox);
                    Stage stage = new Stage();
                    stage.setScene(scene);
                    UnpackingController controller = loader.getController();
                    controller.setDataInterface(this);
                    controller.setUnpacking((Unpacking) object);
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.setResizable(false);
                    stage.setTitle("Edit Unpacking");
                    stage.show();
                    break;
                }
                case "Services": {

                    FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getClassLoader().getResource("fxml/create_service.fxml")));
                    VBox vBox = loader.load();
                    Scene scene = new Scene(vBox, 450, 200);
                    Stage stage = new Stage();
                    stage.setScene(scene);
                    CreateService controller = loader.getController();
                    controller.setDataInterface(this);
                    controller.setService((Service) object);
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.setResizable(false);
                    stage.setTitle("Edit Service");
                    stage.show();

                    break;
                }
                case "Sales":
                case "Posted Sales":
                case "Reversed Sales": {
                    FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getClassLoader().getResource("fxml/express_sale.fxml")));
                    VBox vBox = loader.load();
                    Scene scene = new Scene(vBox, 730, 460);
                    Stage stage = new Stage();
                    stage.setScene(scene);
                    ExpressSaleController controller = loader.getController();
                    controller.setDataInterface(this);
                    controller.setExpressSale((ExpressSale) object);
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.setResizable(false);
                    stage.setTitle("Edit Sale");
                    stage.showAndWait();
                    loadData();
                    break;
                }
                case "All Customers": {

                    FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getClassLoader().getResource("fxml/create_customer.fxml")));
                    VBox vBox = loader.load();
                    Scene scene = new Scene(vBox, 450, 200);
                    Stage stage = new Stage();
                    stage.setScene(scene);
                    CreateCustomer controller = loader.getController();
                    controller.setDataInterface(this);
                    controller.setCustomer((Customer) object);
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.setResizable(false);
                    stage.setTitle("Edit customer");
                    stage.show();

                    break;
                }
                case "Receipts":
                case "Posted Receipts":
                case "Reversed Receipts": {
                    Receipt receipt = (Receipt) object;

                    FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getClassLoader().getResource("fxml/customer_receipt.fxml")));
                    VBox vBox = loader.load();
                    CustomerReceipt controller = loader.getController();
                    Scene scene = new Scene(vBox, 490, 320);
                    Stage stage = new Stage();
                    stage.setScene(scene);
                    controller.setDataInterface(this);
                    controller.setReceipt(receipt);
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.setResizable(false);
                    stage.setTitle("Edit Customer Receipt");
                    stage.show();

                    break;
                }
                case "All Vendors": {

                    FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getClassLoader().getResource("fxml/create_vendor.fxml")));
                    VBox vBox = loader.load();
                    Scene scene = new Scene(vBox, 450, 200);
                    Stage stage = new Stage();
                    stage.setScene(scene);
                    CreateVendor controller = loader.getController();
                    controller.setDataInterface(this);
                    controller.setVendor((Vendor) object);
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.setResizable(false);
                    stage.setTitle("Edit Vendor");
                    stage.show();

                    break;
                }
                case "Vendor Invoices":
                case "Posted Vendor Invoices":
                case "Reversed Vendor Invoices": {
                    models.vendors.Invoice invoice = (Invoice) object;

                    FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getClassLoader().getResource("fxml/invoices_creator.fxml")));
                    VendorInvoice controller = new VendorInvoice();
                    controller.setDataInterface(this);
                    loader.setController(controller);
                    VBox vBox = loader.load();
                    controller.setInvoice(invoice);
                    Scene scene = new Scene(vBox, 730, 460);
                    Stage stage = new Stage();
                    stage.setScene(scene);
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.setResizable(false);
                    stage.setTitle("Edit Vendor Invoice");
                    stage.showAndWait();
                    loadData();

                    break;
                }
                case "Payment Vouchers":
                case "Posted Payment Vouchers":
                case "Reversed Payment Vouchers": {
                    PaymentVoucher voucher = (PaymentVoucher) object;

                    FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getClassLoader().getResource("fxml/payment_voucher.fxml")));
                    VBox vBox = loader.load();
                    PaymentVoucherController controller = loader.getController();
                    Scene scene = new Scene(vBox, 490, 320);
                    Stage stage = new Stage();
                    stage.setScene(scene);
                    controller.setDataInterface(this);
                    controller.setPaymentVoucher(voucher);
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.setResizable(false);
                    stage.setTitle("Edit Payment Voucher");
                    stage.showAndWait();
                    loadData();

                    break;
                }
                case "Customer Invoices":
                case "Posted Customer Invoices":
                case "Reversed Customer Invoices": {
                    models.customers.Invoice invoice = (models.customers.Invoice) object;

                    FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getClassLoader().getResource("fxml/invoices_creator.fxml")));
                    CustomerInvoice controller = new CustomerInvoice();
                    controller.setDataInterface(this);
                    loader.setController(controller);
                    controller.setInvoice(invoice);
                    VBox vBox = loader.load();
                    Scene scene = new Scene(vBox, 730, 460);
                    Stage stage = new Stage();
                    stage.setScene(scene);
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.setResizable(false);
                    stage.setTitle("Edit Customer Invoice");
                    stage.showAndWait();
                    loadData();

                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            createNotification(-1, "The program is unable to properly load new view.");
        }
    }

    /**
     * This method invokes delete requests
     *
     * @param object the object being deleted
     ***/
    private void deleteObject(Object object) {
        switch (type) {
            //Products
            case "Brands": {
                Brand brand = (Brand) object;
                Call<Brand[]> call = apiService.deleteBrand(brand.getId());
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<Brand[]> call, Response<Brand[]> response) {
                        if (response.isSuccessful()) {
                            setData(response.body());
                            createNotification(0, "The brand has been deleted successfully.");
                        } else {
                            createNotification(-1, response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<Brand[]> call, Throwable throwable) {
                        createNotification(-1, throwable.getMessage());
                    }
                });
                break;
            }
            case "Warehouses": {
                Warehouse warehouse = (Warehouse) object;
                Call<Warehouse[]> call = apiService.deleteWarehouse(warehouse.getId());
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<Warehouse[]> call, Response<Warehouse[]> response) {
                        if (response.isSuccessful()) {
                            setData(response.body());
                            createNotification(0, "The warehouse has been deleted successfully.");
                        } else {
                            createNotification(-1, response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<Warehouse[]> call, Throwable throwable) {
                        createNotification(-1, throwable.getMessage());
                    }
                });
                break;
            }
            case "Categories": {
                Category category = (Category) object;
                Call<Category[]> call = apiService.deleteCategory(category.getId());
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<Category[]> call, Response<Category[]> response) {
                        if (response.isSuccessful()) {
                            setData(response.body());
                            createNotification(0, "The category has been deleted successfully.");
                        } else {
                            createNotification(-1, response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<Category[]> call, Throwable throwable) {
                        createNotification(-1, throwable.getMessage());
                    }
                });
                break;
            }
            case "Units of Measure": {
                UnitOfMeasure uom = (UnitOfMeasure) object;
                Call<UnitOfMeasure[]> call = apiService.deleteUOM(uom.getId());
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<UnitOfMeasure[]> call, Response<UnitOfMeasure[]> response) {
                        if (response.isSuccessful()) {
                            setData(response.body());
                            createNotification(0, "The unit has been deleted successfully.");
                        } else {
                            createNotification(-1, response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<UnitOfMeasure[]> call, Throwable throwable) {
                        createNotification(-1, throwable.getMessage());
                    }
                });
                break;
            }
            case "Product Groups": {
                ProductGroup group = (ProductGroup) object;
                Call<ProductGroup[]> call = apiService.deleteGroup(group.getId());
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<ProductGroup[]> call, Response<ProductGroup[]> response) {
                        if (response.isSuccessful()) {
                            setData(response.body());
                            createNotification(0, "The group has been deleted successfully.");
                        } else {
                            createNotification(-1, response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<ProductGroup[]> call, Throwable throwable) {
                        createNotification(-1, throwable.getMessage());
                    }
                });

                break;
            }
            case "Banks": {
                Bank bank = (Bank) object;
                Call<Bank[]> call = apiService.deleteBank(bank.getId());
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<Bank[]> call, Response<Bank[]> response) {
                        if (response.isSuccessful()) {
                            setData(response.body());
                            createNotification(0, "The bank has been deleted successfully.");
                        } else createNotification(-1, response.message());
                    }

                    @Override
                    public void onFailure(Call<Bank[]> call, Throwable throwable) {
                        createNotification(-1, throwable.getMessage());
                    }
                });
                break;
            }
            case "Unpackings":
            case "Posted Unpackings":
            case "Reversed Unpackings": {
                Unpacking unpacking = (Unpacking) object;
                Call<Unpacking[]> call = apiService.deleteUnpacking(unpacking.getId());
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<Unpacking[]> call, Response<Unpacking[]> response) {
                        if (response.isSuccessful()) {
                            Unpacking[] unpackings = {};
                            List<Unpacking> filtered = new ArrayList<>();
                            for (Unpacking u : response.body()) {
                                switch (type) {
                                    case "Unpackings": {
                                        if (!u.isPosted()) filtered.add(u);
                                        break;
                                    }
                                    case "Posted Unpackings": {
                                        if (u.isPosted() && !u.isReversed()) filtered.add(u);
                                        break;
                                    }
                                    case "Reversed Unpackings": {
                                        if (u.isReversed()) filtered.add(u);
                                        break;
                                    }
                                }
                            }
                            setData(filtered.toArray(unpackings));

                        } else createNotification(-1, response.message());
                    }

                    @Override
                    public void onFailure(Call<Unpacking[]> call, Throwable throwable) {
                        createNotification(-1, throwable.getMessage());
                    }
                });
            }
            case "Services": {
                Service service = (Service) object;
                Call<Service[]> call = apiService.deleteService(service.getId());
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<Service[]> call, Response<Service[]> response) {
                        if (response.isSuccessful()) {
                            setData(response.body());
                            createNotification(0, "The service has been deleted successfully.");
                        } else {
                            createNotification(-1, response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<Service[]> call, Throwable throwable) {
                        createNotification(-1, throwable.getMessage());
                    }
                });
                break;
            }
            case "Sales":
            case "Posted Sales":
            case "Reversed Sales": {
                ExpressSale sale = (ExpressSale) object;
                Call<ExpressSale[]> call = apiService.deleteSale(sale.getId());
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<ExpressSale[]> call, Response<ExpressSale[]> response) {
                        if (response.isSuccessful()) {
                            ExpressSale[] sales = {};
                            List<ExpressSale> filtered = new ArrayList<>();
                            for (ExpressSale sale : response.body()) {
                                switch (type) {
                                    case "Sales":
                                        if (!sale.isPosted()) filtered.add(sale);
                                        break;
                                    case "Posted Sales":
                                        if (sale.isPosted() && !sale.isReversed()) filtered.add(sale);
                                        break;
                                    case "Reversed Sales":
                                        if (sale.isReversed()) filtered.add(sale);
                                        break;
                                }
                            }
                            setData(filtered.toArray(sales));
                        } else {
                            createNotification(-1, response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<ExpressSale[]> call, Throwable throwable) {
                        createNotification(-1, throwable.getMessage());
                    }
                });
            }
            //Vendors
            case "All Vendors": {
                Vendor vendor = (Vendor) object;
                Call<Vendor[]> call = apiService.deleteVendor(vendor.getId());
                call.enqueue(new Callback<Vendor[]>() {
                    @Override
                    public void onResponse(Call<Vendor[]> call, Response<Vendor[]> response) {
                        if (response.isSuccessful()) {
                            setData(response.body());
                            createNotification(0, "Vendor has been deleted");
                        } else createNotification(-1, response.message());
                    }

                    @Override
                    public void onFailure(Call<Vendor[]> call, Throwable throwable) {
                        createNotification(-1, throwable.getMessage());
                    }
                });
                break;
            }
            case "Vendor Invoices":
            case "Posted Vendor Invoices":
            case "Recersed Vendor Invoices": {
                Invoice invoice = (Invoice) object;
                Call<Invoice[]> call = apiService.deleteVendorInvoice(invoice.getId());
                call.enqueue(new Callback<Invoice[]>() {
                    @Override
                    public void onResponse(Call<Invoice[]> call, Response<Invoice[]> response) {
                        if (response.isSuccessful()) {
                            setData(response.body());
                            createNotification(0, "Vendor invoice has been deleted");
                        } else createNotification(-1, response.message());
                    }

                    @Override
                    public void onFailure(Call<Invoice[]> call, Throwable throwable) {
                        createNotification(-1, throwable.getMessage());
                    }
                });
            }
            //Customers
            case "All Customers": {
                Customer customer = (Customer) object;
                Call<Customer[]> call = apiService.deleteCustomer(customer.getId());
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<Customer[]> call, Response<Customer[]> response) {
                        if (response.isSuccessful()) {
                            setData(response.body());
                            createNotification(0, "Customer has been deleted");
                        } else createNotification(-1, response.message());
                    }

                    @Override
                    public void onFailure(Call<Customer[]> call, Throwable throwable) {
                        createNotification(-1, throwable.getMessage());
                    }
                });
                break;
            }

            case "Receipts":
            case "Posted Receipts":
            case "Reversed Receipts": {
                Receipt receipt = (Receipt) object;
                Call<Receipt[]> call = apiService.deleteReceipt(receipt.getId());
                call.enqueue(new Callback<Receipt[]>() {
                    @Override
                    public void onResponse(Call<Receipt[]> call, Response<Receipt[]> response) {
                        if (response.isSuccessful()) {
                            setData(response.body());
                            createNotification(0, "Receipt has been deleted");
                        } else createNotification(-1, response.message());
                    }

                    @Override
                    public void onFailure(Call<Receipt[]> call, Throwable throwable) {
                        createNotification(-1, throwable.getMessage());
                    }
                });
                break;
            }
            case "Payment Vouchers":
            case "Posted Payment Vouchers":
            case "Reversed Payment Vouchers": {
                PaymentVoucher voucher = (PaymentVoucher) object;
                Call<PaymentVoucher[]> call = apiService.deletePaymentVoucher(voucher.getId());
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<PaymentVoucher[]> call, Response<PaymentVoucher[]> response) {
                        if (response.isSuccessful()) {
                            setData(response.body());
                            createNotification(0, "The voucher has been deleted");
                        } else createNotification(-1, response.message());
                    }

                    @Override
                    public void onFailure(Call<PaymentVoucher[]> call, Throwable throwable) {
                        createNotification(-1, throwable.getMessage());
                    }
                });
                break;
            }

            case "Customer Invoices":
            case "Posted Customer Invoices":
            case "Reversed Customer Invoices": {
                models.customers.Invoice invoice = (models.customers.Invoice) object;
                Call<models.customers.Invoice[]> call = apiService.deleteCustomerInvoice(invoice.getId());
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<models.customers.Invoice[]> call, Response<models.customers.Invoice[]> response) {
                        if (response.isSuccessful()) {
                            setData(response.body());
                            createNotification(0, "The customer invoice has been deleted");
                        } else createNotification(-1, response.message());
                    }

                    @Override
                    public void onFailure(Call<models.customers.Invoice[]> call, Throwable throwable) {
                        createNotification(-1, throwable.getMessage());
                    }
                });
                break;
            }
        }
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setData(SuperModel[] data) {
        this.data = data;
        Platform.runLater(() -> tvGeneral.setItems(FXCollections.observableArrayList(Arrays.asList(data))));
    }

    private TableColumn[] createColumns(MyTableColumn[] myTableColumns) {
        TableColumn[] tableColumns = new TableColumn[]{};
        List<TableColumn<String, Object>> tableColumnList = new ArrayList<>();
        for (MyTableColumn m : myTableColumns) {
            TableColumn<String, Object> tableColumn = new TableColumn<>(m.getName());
            tableColumn.setCellValueFactory(new PropertyValueFactory<>(m.getProperty()));
            tableColumn.prefWidthProperty().bind(tvGeneral.prefWidthProperty().multiply(m.getMultiplier()));
            tableColumn.setResizable(true);
            tableColumnList.add(tableColumn);
        }
        return tableColumnList.toArray(tableColumns);
    }

    private void createNotification(int type, String message) {
        Notifications n = Notifications.create()
                .owner(vbParent)
                .text(message)
                .hideAfter(Duration.seconds(8))
                .darkStyle()
                .position(Pos.BOTTOM_RIGHT);
        Platform.runLater(() -> {
            switch (type) {
                case -1:
                    n.showError();
                    break;
                case 0:
                    n.showInformation();
                    break;
                default:
                    n.showWarning();
            }

        });

    }

    public static class CustomButton extends Button {
        public CustomButton(String text, FontAwesomeIconView icon) {
            super(text);
            icon.setSize("18.0");
            setGraphic(icon);
            setStyle("-fx-background-color: transparent;");
            setPrefHeight(76);
            setContentDisplay(ContentDisplay.TOP);
        }
    }
}
