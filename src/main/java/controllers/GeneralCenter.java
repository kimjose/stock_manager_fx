package controllers;

import interfaces.HomeDataInterface;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import models.MyTableColumn;
import models.customers.Customer;
import models.products.*;
import models.vendors.Invoice;
import models.vendors.Vendor;
import network.ApiService;
import network.RetrofitBuilder;
import org.controlsfx.control.Notifications;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.net.URL;
import java.util.*;

/**
 * @author kim jose
 * @see javafx.fxml.Initializable
 *
 * This class controls the center scene and loads different data to it.
 * ***/

public class GeneralCenter implements Initializable, HomeDataInterface {



    @FXML
    private VBox vbParent;

    @FXML
    private Button btnAdd;

    @FXML
    private Button btnEdit;

    @FXML
    private Button btnDelete;

    @FXML
    private TextField tfSearch;

    @FXML
    private TableView<Object> tvGeneral;

    private String type;
    private Object[] data;
    private ApiService apiService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        Platform.runLater(this::setupColumns);
        Platform.runLater(this::loadData);
        tvGeneral.prefWidthProperty().bind(vbParent.widthProperty());
        tvGeneral.prefHeightProperty().bind(vbParent.heightProperty().subtract(140));

        apiService = RetrofitBuilder.createService(ApiService.class);

        btnAdd.setOnAction(event -> addObject());
        btnEdit.setOnAction(event -> {
            Object o = tvGeneral.getSelectionModel().getSelectedItem();
            if (o==null)createNotification(1, "You must select an item first");
            else editObject(o);
        });
        btnDelete.setOnAction(event -> {
            Object o = tvGeneral.getSelectionModel().getSelectedItem();
            if (o==null)createNotification(1, "You must select an item first");
            else deleteObject(o);
        });
    }

    @Override
    public void updateData(String message, Object[] data) {
        createNotification(0, message);
        setData(data);
    }

    private void setupColumns(){
        assert type!=null;
        System.out.println("Setting up "+type);
        switch (type){
            case "Products":{
                /**
                 * 'name', 'is_active', 'description', 'sku_code', 'upc_code', 'quantity',
                 *         'price', 'brand', 'category', 'uom', 'image'**/
                tvGeneral.getColumns().removeAll(tvGeneral.getColumns());
                Platform.runLater(()->{
                    tvGeneral.getColumns().addAll(createColumns(new MyTableColumn[]{
                            new MyTableColumn("Product Name", "name", 0.15),
                            new MyTableColumn("SKU Code", "skuCode", 0.25),
                            new MyTableColumn("Brand", "brand", 0.10),
                            new MyTableColumn("Category", "category", 0.20),
                            new MyTableColumn("Quantity", "quantity", 0.15),
                            new MyTableColumn("Unit of Measure", "uom", 0.15),
                    }));
                });
                break;
            }
            case "Brands":{
                tvGeneral.getColumns().removeAll(tvGeneral.getColumns());
                TableColumn[] columns = createColumns(new MyTableColumn[]{
                        new MyTableColumn("Brand Id", "id", 0.25),
                        new MyTableColumn("Brand Name", "name", 0.75),
                });
                tvGeneral.getColumns().setAll(columns);
                break;
            }
            case "Categories":{
                tvGeneral.getColumns().removeAll(tvGeneral.getColumns());
                tvGeneral.getColumns().addAll(createColumns(new MyTableColumn[]{
                        new MyTableColumn("Category Id", "id", 0.25),
                        new MyTableColumn("Category Name", "name", 0.75),
                }));
                break;
            }
            case "Units of Measure":{
                tvGeneral.getColumns().removeAll(tvGeneral.getColumns());
                tvGeneral.getColumns().addAll(createColumns(new MyTableColumn[]{
                        new MyTableColumn("UOM Id", "id", 0.10),
                        new MyTableColumn("UOM Name", "name", 0.35),
                        new MyTableColumn("UOM Description", "description", 0.55),
                }));
                break;
            }
            case "Warehouses":{
                tvGeneral.getColumns().removeAll(tvGeneral.getColumns());
                tvGeneral.getColumns().addAll(createColumns(new MyTableColumn[]{
                        new MyTableColumn("Id", "id", 0.10),
                        new MyTableColumn("Name", "name", 0.35),
                        new MyTableColumn("Location", "location", 0.55),
                }));
                break;
            }
            case "Services":{
                tvGeneral.getColumns().removeAll(tvGeneral.getColumns());
                tvGeneral.getColumns().addAll(createColumns(new MyTableColumn[]{
                        new MyTableColumn("Service Id", "id", 0.10),
                        new MyTableColumn("Service Name", "name", 0.35),
                        new MyTableColumn("Description", "description", 0.55),
                }));
                break;
            }


            //Vendors
            case "All Vendors":{
                tvGeneral.getColumns().removeAll(tvGeneral.getColumns());
                tvGeneral.getColumns().addAll(createColumns(new MyTableColumn[]{
                        new MyTableColumn("Vendor Name", "name", 0.35),
                        new MyTableColumn("Email", "email", 0.35),
                        new MyTableColumn("Phone No.", "phone", 0.30),
                }));
                break;
            }
            case "Vendor Invoices":{
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
            case "Posted Vendor Invoices":{
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
            case "Reversed Vendor Invoices":{
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


            //Customers
            case "All Customers":{
                tvGeneral.getColumns().removeAll(tvGeneral.getColumns());
                tvGeneral.getColumns().addAll(createColumns(new MyTableColumn[]{
                        new MyTableColumn("Customer Name", "name", 0.35),
                        new MyTableColumn("Email", "email", 0.35),
                        new MyTableColumn("Phone No.", "phone", 0.30),
                }));
                break;
            }
            case "Customer Invoices":{
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
            case "Posted Customer Invoices":{
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
            case "Reversed Customer Invoices":{
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
        }
    }

    /**
     * This method fetches data from the server and loads it to a table
     *
     * ***/
    private void loadData(){
        assert type!=null;
        switch (type){
            //for shop
            case "Products":{
                Call<Product[]> call = apiService.products();
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<Product[]> call, Response<Product[]> response) {
                        if (response.isSuccessful()){
                            setData(response.body());
                        }else{
                            tvGeneral.setItems(FXCollections.emptyObservableList());
                        }
                    }

                    @Override
                    public void onFailure(Call<Product[]> call, Throwable throwable) {
                        createNotification(-1, throwable.getMessage());
                    }
                });
                break;
            }
            case "Brands":{
                Call<Brand[]> call = apiService.brands();
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<Brand[]> call, Response<Brand[]> response) {
                        System.out.println(response.body());
                        if (response.isSuccessful()){
                            setData(response.body());
                        }else{
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
            case "Categories":{
                Call<Category[]> call = apiService.categories();
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<Category[]> call, Response<Category[]> response) {
                        if (response.isSuccessful()){
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
            case "Units of Measure":{
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
            case "Warehouses":{
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
            case "Services":{
                Call<Service[]> call = apiService.services();
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<Service[]> call, Response<Service[]> response) {
                        if (response.isSuccessful()) setData(response.body());
                    }

                    @Override
                    public void onFailure(Call<Service[]> call, Throwable throwable) {
                        createNotification(-1, throwable.getMessage());
                    }
                });
                break;
            }


            //Vendors
            case "All Vendors":{
                Call<Vendor[]> call = apiService.vendors();
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<Vendor[]> call, Response<Vendor[]> response) {
                        if (response.isSuccessful()){
                            setData(response.body());
                        }else{
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
            case "Vendor Invoices":{
                Call<models.vendors.Invoice[]> call = apiService.vendorInvoices();
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<Invoice[]> call, Response<Invoice[]> response) {
                        if (response.isSuccessful()){
                            List<Invoice> filtered = new ArrayList<>();
                            for (Invoice i : response.body()) {
                                if (!i.isPosted()) filtered.add(i);
                            }
                            setData(filtered.toArray());
                        }else createNotification(-1, response.message());
                    }

                    @Override
                    public void onFailure(Call<Invoice[]> call, Throwable throwable) {
                        createNotification(-1, throwable.getMessage());
                    }
                });
                break;
            }
            case "Posted Vendor Invoices":{
                Call<models.vendors.Invoice[]> call = apiService.vendorInvoices();
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<Invoice[]> call, Response<Invoice[]> response) {
                        if (response.isSuccessful()){
                            List<Invoice> filtered = new ArrayList<>();
                            for (Invoice i : response.body()) {
                                if (i.isPosted() && !i.isReversed()) filtered.add(i);
                            }
                            setData(filtered.toArray());
                        }else createNotification(-1, response.message());
                    }

                    @Override
                    public void onFailure(Call<Invoice[]> call, Throwable throwable) {
                        createNotification(-1, throwable.getMessage());
                    }
                });
                break;
            }
            case "Reversed Vendor Invoices":{
                Call<models.vendors.Invoice[]> call = apiService.vendorInvoices();
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<Invoice[]> call, Response<Invoice[]> response) {
                        if (response.isSuccessful()){
                            List<Invoice> filtered = new ArrayList<>();
                            for (Invoice i : response.body()) {
                                if (i.isPosted() && i.isReversed()) filtered.add(i);
                            }
                            setData(filtered.toArray());
                        }else createNotification(-1, response.message());
                    }

                    @Override
                    public void onFailure(Call<Invoice[]> call, Throwable throwable) {
                        createNotification(-1, throwable.getMessage());
                    }
                });
                break;
            }


            //Customers
            case "All Customers":{
                Call<Customer[]> call = apiService.customers();
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<Customer[]> call, Response<Customer[]> response) {
                        if (response.isSuccessful()){
                            setData(response.body());
                        }else{
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
            case "Customer Invoices":{
                Call<models.customers.Invoice[]> call = apiService.customerInvoices();
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<models.customers.Invoice[]> call, Response<models.customers.Invoice[]> response) {
                        if (response.isSuccessful()){
                            List<models.customers.Invoice> filtered = new ArrayList<>();
                            for (models.customers.Invoice i: response.body()) {
                                if (!i.isPosted()) filtered.add(i);
                            }
                            setData(filtered.toArray());
                        }else{
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
            case "Posted Customer Invoices":{
                Call<models.customers.Invoice[]> call = apiService.customerInvoices();
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<models.customers.Invoice[]> call, Response<models.customers.Invoice[]> response) {
                        if (response.isSuccessful()){
                            List<models.customers.Invoice> filtered = new ArrayList<>();
                            for (models.customers.Invoice i: response.body()) {
                                if (i.isPosted() && !i.isReversed()) filtered.add(i);
                            }
                            setData(filtered.toArray());
                        }else{
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
            case "Reversed Customer Invoices":{
                Call<models.customers.Invoice[]> call = apiService.customerInvoices();
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<models.customers.Invoice[]> call, Response<models.customers.Invoice[]> response) {
                        if (response.isSuccessful()){
                            List<models.customers.Invoice> filtered = new ArrayList<>();
                            for (models.customers.Invoice i: response.body()) {
                                if (i.isReversed()) filtered.add(i);
                            }
                            setData(filtered.toArray());
                        }else{
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
        }
    }

    /**
     * This method triggers a scene that adds an object
     *
     * ****/
    private void addObject(){
        switch (type){
            case "Brands":{
                try{
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
                }catch(Exception e){
                    e.printStackTrace();
                }
                break;
            }
            case "Warehouses":{
                try{
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
                }catch(Exception e){
                    e.printStackTrace();
                }
                break;
            }
            case "Categories":{
                try{
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
                }catch(Exception e){
                    e.printStackTrace();
                }
                break;
            }
            case "Units of Measure":{
                try{
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
                }catch(Exception e){
                    e.printStackTrace();
                }
                break;
            }
            case "Services":{
                try{
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
                }catch(Exception e){
                    e.printStackTrace();
                }
                break;
            }
            case "All Customers":{
                try{
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
                }catch(Exception e){
                    e.printStackTrace();
                }
                break;
            }
            case "All Vendors":{
                try{
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
                }catch(Exception e){
                    e.printStackTrace();
                }
                break;
            }
            case "Vendor Invoices":{
                try{
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
                }catch(Exception e){
                    e.printStackTrace();
                }
                break;
            }
            case "Customer Invoices":{
                try{
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
                }catch(Exception e){
                    e.printStackTrace();
                }
                break;
            }
        }
    }
    /**
     * This method triggers a interface that edits/updates an object
     * @param object the object being edited
     * **/
    private void editObject(Object object){
        switch (type){
            case "Brands":{
                try{
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
                }catch(Exception e){
                    e.printStackTrace();
                }
                break;
            }
            case "Warehouses":{
                try{
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
                }catch(Exception e){
                    e.printStackTrace();
                }
                break;
            }
            case "Categories":{
                try{
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
                }catch(Exception e){
                    e.printStackTrace();
                }
                break;
            }
            case "Units of Measure":{
                try{
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
                }catch(Exception e){
                    e.printStackTrace();
                }
                break;
            }
            case "Services":{
                try{
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
                }catch(Exception e){
                    e.printStackTrace();
                }
                break;
            }
            case "All Customers":{
                try{
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
                }catch(Exception e){
                    e.printStackTrace();
                }
                break;
            }
            case "All Vendors":{
                try{
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
                }catch(Exception e){
                    e.printStackTrace();
                }
                break;
            }
            case "Vendor Invoices":
            case "Posted Vendor Invoices":
            case "Reversed Vendor Invoices":{
                models.vendors.Invoice invoice = (Invoice) object;
                try{
                    FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getClassLoader().getResource("fxml/invoice_creator.fxml")));
                    VendorInvoice controller = new VendorInvoice();
                    controller.setDataInterface(this);
                    controller.setInvoice(invoice);
                    loader.setController(controller);
                    VBox vBox = loader.load();
                    Scene scene = new Scene(vBox, 730, 460);
                    Stage stage = new Stage();
                    stage.setScene(scene);
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.setResizable(false);
                    stage.setTitle("Edit Vendor Invoice");
                    stage.show();
                }catch(Exception e){
                    e.printStackTrace();
                }
                break;
            }
            case "Customer Invoices":
            case "Posted Customer Invoices":
            case "Reversed Customer Invoices":{
                models.customers.Invoice invoice = (models.customers.Invoice ) object;
                try{
                    FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getClassLoader().getResource("fxml/invoice_creator.fxml")));
                    CustomerInvoice controller = new CustomerInvoice();
                    controller.setDataInterface(this);
                    controller.setInvoice(invoice);
                    loader.setController(controller);
                    VBox vBox = loader.load();
                    Scene scene = new Scene(vBox, 730, 460);
                    Stage stage = new Stage();
                    stage.setScene(scene);
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.setResizable(false);
                    stage.setTitle("Edit Customer Invoice");
                    stage.show();
                }catch(Exception e){
                    e.printStackTrace();
                }
                break;
            }
        }
    }
    /**
     * This method invokes delete requests
     * @param object the object being deleted
     * ***/
    private void deleteObject(Object object){
        switch (type){
            //Products
            case "Brands":{
                Brand brand = (Brand) object;
                Call<Brand[]> call = apiService.deleteBrand(brand.getId());
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<Brand[]> call, Response<Brand[]> response) {
                        if (response.isSuccessful()){
                            setData(response.body());
                            createNotification(0, "The brand has been deleted successfully.");
                        }else{
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
            case "Warehouses":{
                Warehouse warehouse = (Warehouse) object;
                Call<Warehouse[]> call = apiService.deleteWarehouse(warehouse.getId());
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<Warehouse[]> call, Response<Warehouse[]> response) {
                        if (response.isSuccessful()){
                            setData(response.body());
                            createNotification(0, "The warehouse has been deleted successfully.");
                        }else{
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
            case "Categories":{
                Category category = (Category) object;
                Call<Category[]> call = apiService.deleteCategory(category.getId());
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<Category[]> call, Response<Category[]> response) {
                        if (response.isSuccessful()){
                            setData(response.body());
                            createNotification(0, "The category has been deleted successfully.");
                        }else{
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
            case "Units of Measure":{
                UnitOfMeasure uom = (UnitOfMeasure) object;
                Call<UnitOfMeasure[]> call = apiService.deleteUOM(uom.getId());
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<UnitOfMeasure[]> call, Response<UnitOfMeasure[]> response) {
                        if (response.isSuccessful()){
                            setData(response.body());
                            createNotification(0, "The unit has been deleted successfully.");
                        }else{
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
            case "Services":{
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
            //Vendors
            case "All Vendors":{
                Vendor vendor = (Vendor) object;
                Call<Vendor[]> call = apiService.deleteVendor(vendor.getId());
                call.enqueue(new Callback<Vendor[]>() {
                    @Override
                    public void onResponse(Call<Vendor[]> call, Response<Vendor[]> response) {
                        if (response.isSuccessful()){
                            setData(response.body());
                            createNotification(0, "Vendor has been deleted");
                        }else createNotification(-1, response.message());
                    }

                    @Override
                    public void onFailure(Call<Vendor[]> call, Throwable throwable) {
                        createNotification(-1, throwable.getMessage());
                    }
                });
                break;
            }
            case "Vendor Invoices":{

            }
            //Customers
            case "All Customers":{
                Customer customer = (Customer) object;
                Call<Customer[]> call = apiService.deleteCustomer(customer.getId());
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<Customer[]> call, Response<Customer[]> response) {
                        if (response.isSuccessful()){
                            setData(response.body());
                            createNotification(0, "Customer has been deleted");
                        }else createNotification(-1, response.message());
                    }

                    @Override
                    public void onFailure(Call<Customer[]> call, Throwable throwable) {
                        createNotification(-1, throwable.getMessage());
                    }
                });
                break;
            }
            case "Customer Invoices":{

            }
        }
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setData(Object[] data) {
        this.data = data;
        tvGeneral.setItems(FXCollections.observableArrayList(Arrays.asList(data)));
    }

    private TableColumn[] createColumns(MyTableColumn[] myTableColumns){
        TableColumn[] tableColumns = new TableColumn[]{};
        List<TableColumn<String, String>> tableColumnList = new ArrayList<>();
        for (MyTableColumn m: myTableColumns) {
            TableColumn<String, String> tableColumn = new TableColumn<>(m.getName());
            tableColumn.setCellValueFactory(new PropertyValueFactory<>(m.getProperty()));
            tableColumn.prefWidthProperty().bind(tvGeneral.prefWidthProperty().multiply(m.getMultiplier()));
            tableColumn.setResizable(true);
            tableColumnList.add(tableColumn);
        }
        return tableColumnList.toArray(tableColumns);
    }

    private void createNotification(int type, String message){
        Notifications n = Notifications.create()
                .owner(vbParent)
                .text(message)
                .hideAfter(Duration.seconds(8))
                .darkStyle()
                .position(Pos.BOTTOM_RIGHT);
        Platform.runLater(()->{
            switch (type){
                case -1: n.showError();break;
                case 0: n.showInformation();break;
                default: n.showWarning();
            }

        });

    }
}
