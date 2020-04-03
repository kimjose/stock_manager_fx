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
import models.products.*;
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


            //Vendors
            case "All Vendors":{

            }
            case "Vendor Invoices":{

            }


            //Customers
            case "All Customers":{

            }
            case "Customer Invoices":{

            }
            System.out.println(tvGeneral.getColumns());
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


            //Vendors
            case "All Vendors":{

            }
            case "Vendor Invoices":{

            }


            //Customers
            case "All Customers":{

            }
            case "Customer Invoices":{

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
            //Vendors
            case "All Vendors":{

            }
            case "Vendor Invoices":{

            }
            //Customers
            case "All Customers":{

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
