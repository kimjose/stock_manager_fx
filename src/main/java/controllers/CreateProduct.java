package controllers;

import interfaces.HomeDataInterface;
import interfaces.Shared;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
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
    private TextField tfSkuCode;

    @FXML
    private TextField tfUpcCode;

    @FXML
    private TextField tfPrice;

    @FXML
    private TextField tfSellingPrice;

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
    private String base64Image = null;
    //private final Image image = new Image(userDir+"tops.PNG");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Utility.setupNotificationPane(notificationPane, hbHolder);
        apiService = RetrofitBuilder.createService(ApiService.class);
        Utility.restrictInputDec(tfPrice);
        Utility.restrictInputNum(tfUpcCode);
        ivPhoto.setImage(new Image(Shared.LOGO_IMAGE));

        ContextMenu contextMenu = new ContextMenu();
        MenuItem miSelect = new MenuItem("Select from Files");
        MenuItem miReset = new MenuItem("Reset");
        miSelect.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Product Image");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("All Images", "*.jpg",".png"),
                    new FileChooser.ExtensionFilter("JPEG", "*.jpg"),
                    new FileChooser.ExtensionFilter("PNG", "*.png")
            );
            File file = fileChooser.showOpenDialog(vbParent.getScene().getWindow());
            if (file != null) {
                ivPhoto.setImage(new Image(file.toURI().toString()));
                base64Image = Utility.encodeImage(file.getPath());
            }
        });
        miReset.setOnAction(event -> {
            base64Image = null;
            System.out.println(Shared.LOGO_IMAGE);
            ivPhoto.setImage(new Image(Shared.LOGO_IMAGE));
        });
        contextMenu.getItems().add(miSelect);
        contextMenu.getItems().add(miReset);
        ivPhoto.setOnContextMenuRequested(event -> contextMenu.show(ivPhoto, event.getScreenX(), event.getScreenY()));

        loadData();

        btnSave.setOnAction(event -> save());
        btnCancel.setOnAction(event -> Utility.closeWindow(vbParent));
    }

    private void loadData() {
        Platform.runLater(() -> {
            if (product == null || product.getImage() == null || product.getImage().equals("")) {
                //ivPhoto.setImage(image);
            } else Utility.decodeImage(product.getImage(), ivPhoto);
        });
    }

    private void save() {
        String name = tfName.getText().trim(),
                desc = taDescription.getText().trim(),
                skuCode = tfSkuCode.getText().trim(),
                upcCode = tfUpcCode.getText().trim(),
                bp = tfPrice.getText().trim(),
                sp = tfSellingPrice.getText().trim(),
                errorMessage = "";
        double buyingPrice = 0, sellingPrice = 0;
        if (name.equals("")) errorMessage = "Product name is required.";
        try {
            buyingPrice = Double.parseDouble(bp);
            if (buyingPrice == 0) throw new Exception();
        } catch (Exception e) {
            errorMessage += errorMessage.equals("") ? "Enter a valid price of purchase." : "\nEnter a valid price of purchase.";
        }
        try {
            sellingPrice = Double.parseDouble(sp);
            if (sellingPrice == 0) throw new Exception();
        } catch (Exception e) {
            errorMessage += errorMessage.equals("") ? "Enter a valid price of purchase." : "\nEnter a valid price of purchase.";
        }
        if (!errorMessage.equals("")) {
            notificationPane.show(errorMessage);
            return;
        }
        Call<Product[]> call;
        if (product != null) {
            name = name.equals(product.getName()) ? null : name;
            call = apiService.updateProduct(product.getId(), name, desc, buyingPrice, sellingPrice, skuCode, upcCode, base64Image);
        }else call = apiService.addProduct(name, desc, buyingPrice, sellingPrice, 1, 1, 1, skuCode, upcCode, base64Image);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Product[]> call, Response<Product[]> response) {
                Platform.runLater(() -> {
                    if (response.isSuccessful()) {
                        dataInterface.updateData("The product has been saved", response.body());
                        Utility.closeWindow(vbParent);
                    } else
                        notificationPane.show(Utility.handleApiErrors(response.message(), response.errorBody(), new String[]{
                                "name", "description", "buyingPrice", "sellingPrice", "brandId", "uomId", "categoryId",
                        }));
                });
            }

            @Override
            public void onFailure(Call<Product[]> call, Throwable throwable) {
                Platform.runLater(() -> notificationPane.show(throwable.getMessage()));
            }
        });
    }

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
        tfPrice.setText(String.valueOf(product.getBuyingPrice()));
        tfSellingPrice.setText(String.valueOf(product.getSellingPrice()));
        base64Image = product.getImage();
    }
}
