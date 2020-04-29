package controllers;

import interfaces.HomeDataInterface;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import models.products.Brand;
import models.products.Category;
import models.products.Product;
import models.products.UnitOfMeasure;
import network.ApiService;
import network.RetrofitBuilder;
import org.controlsfx.control.NotificationPane;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import utils.Utility;

import java.net.URL;
import java.util.ResourceBundle;

public class CreateProduct implements Initializable {

    @FXML
    private VBox vbParent;

    @FXML
    private NotificationPane notificationPane;

    @FXML
    private HBox hbHolder;

    @FXML
    private Label labelId;

    @FXML
    private TextField tfName;

    @FXML
    private TextArea taDescription;

    @FXML
    private ComboBox<Brand> cbBrand;

    @FXML
    private ComboBox<Category> cbCategory;

    @FXML
    private ComboBox<UnitOfMeasure> cbUOM;

    @FXML
    private TextField tfSkuCode;

    @FXML
    private TextField tfUpcCode;

    @FXML
    private TextField tfPrice;

    @FXML
    private ImageView ivPhoto;

    @FXML
    private Button btnSave;

    @FXML
    private Button btnCancel;

    private ApiService apiService;
    private Product product;
    private HomeDataInterface dataInterface;
    private final String userDir = System.getProperty("user.dir");
    //private final Image image = new Image(userDir+"tops.PNG");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Utility.setupNotificationPane(notificationPane, hbHolder);
        apiService = RetrofitBuilder.createService(ApiService.class);
        Utility.restrictInputDec(tfPrice);
        Utility.restrictInputNum(tfUpcCode);
        
        loadData();

        btnSave.setOnAction(event -> save());
        btnCancel.setOnAction(event -> Utility.closeWindow(vbParent));
    }

    private void loadData(){
        Call<UnitOfMeasure[]> getUom = apiService.unitsOfMeasure();
        getUom.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<UnitOfMeasure[]> call, Response<UnitOfMeasure[]> response) {
                if (response.isSuccessful()){
                    Platform.runLater(()->{
                        cbUOM.setItems(FXCollections.observableArrayList(response.body()));
                        if (product != null ) cbUOM.getSelectionModel().select(product.getUom());
                    });
                }else Platform.runLater(()->notificationPane.show(response.message()));
            }

            @Override
            public void onFailure(Call<UnitOfMeasure[]> call, Throwable throwable) {
                Platform.runLater(()->notificationPane.show(throwable.getMessage()));
            }
        });
        Call<Brand[]> getBrands = apiService.brands();
        getBrands.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Brand[]> call, Response<Brand[]> response) {
                if (response.isSuccessful()) {
                    Platform.runLater(() -> {
                        cbBrand.setItems(FXCollections.observableArrayList(response.body()));
                        if (product != null) cbBrand.getSelectionModel().select(product.getBrand());
                    });
                } else Platform.runLater(() -> notificationPane.show(response.message()));
            }

            @Override
            public void onFailure(Call<Brand[]> call, Throwable throwable) {
                Platform.runLater(() -> notificationPane.show(throwable.getMessage()));
            }
        });
        Call<Category[]> getCategories = apiService.categories();
        getCategories.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Category[]> call, Response<Category[]> response) {
                if (response.isSuccessful()) {
                    Platform.runLater(() -> {
                        cbCategory.setItems(FXCollections.observableArrayList(response.body()));
                        if (product != null) cbCategory.getSelectionModel().select(product.getCategory());
                    });
                } else Platform.runLater(() -> notificationPane.show(response.message()));
            }

            @Override
            public void onFailure(Call<Category[]> call, Throwable throwable) {
                Platform.runLater(() -> notificationPane.show(throwable.getMessage()));
            }
        });
        Platform.runLater(()->{
            if (product == null || product.getImage() == null || product.getImage().equals("")){
                //ivPhoto.setImage(image);
            }else{/* TODO: 25-Apr-20 decode image and add to iv*/ }
        });
    }

    private void save(){}

    public void setDataInterface(HomeDataInterface dataInterface) {
        this.dataInterface = dataInterface;
    }

    public void setProduct(Product product) {
        this.product = product;
        labelId.setText(String.valueOf(product.getId()));
        tfName.setText(product.getName());
        taDescription.setText(product.getDescription());
        tfSkuCode.setText(product.getSkuCode() == null ? "" : product.getSkuCode());
        tfUpcCode.setText(product.getUpcCode() == null ? "" : product.getUpcCode());
        tfPrice.setText(String.valueOf(product.getPrice()));
    }
}
