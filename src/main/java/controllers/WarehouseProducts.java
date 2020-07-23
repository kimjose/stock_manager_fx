package controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import models.products.Product;
import models.products.Warehouse;
import network.ApiService;
import network.RetrofitBuilder;
import org.controlsfx.control.MaskerPane;
import org.controlsfx.control.NotificationPane;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import utils.Utility;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class WarehouseProducts implements Initializable {

    @FXML
    AnchorPane apParent;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private Button btnRefresh;

    @FXML
    private GridPane gpProducts;

    @FXML
    private AnchorPane apHolder;

    @FXML
    private NotificationPane notificationPane;

    @FXML
    private MaskerPane maskerPane;

    private ApiService apiService = RetrofitBuilder.createService(ApiService.class);
    private Warehouse[] warehouses;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Utility.setupNotificationPane(notificationPane, apHolder);
        apHolder.prefWidthProperty().bind(apParent.widthProperty());
        apHolder.prefHeightProperty().bind(apParent.heightProperty());
        btnRefresh.setOnAction(event -> loadData());
        loadData();
        Platform.runLater(()->Utility.setLogo(apParent));
    }

    private void loadData() {
        maskerPane.setVisible(true);
        Call<Product[]> call = apiService.products();
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Product[]> call, Response<Product[]> response) {
                Platform.runLater(() -> {
                    if (response.isSuccessful()) {
                        if (warehouses != null) loadToGrid(response.body());
                    } else {
                        Platform.runLater(() -> {
                            notificationPane.show(response.message());
                            maskerPane.setVisible(false);
                        });
                    }
                });
            }

            @Override
            public void onFailure(Call<Product[]> call, Throwable throwable) {
                Platform.runLater(() -> {
                    notificationPane.show();
                    maskerPane.setVisible(false);
                });            }
        });
        if (warehouses == null) {
            Call<Warehouse[]> getHouses = apiService.warehouses();
            getHouses.enqueue(new Callback<>() {
                @Override
                public void onResponse(Call<Warehouse[]> call, Response<Warehouse[]> response) {
                    Platform.runLater(() -> {
                        if (response.isSuccessful()) {
                            warehouses = response.body();
                            Platform.runLater(() -> {
                                maskerPane.setVisible(false);
                            });
                        }
                        else {
                            Platform.runLater(() -> {
                                notificationPane.show(response.message());
                                maskerPane.setVisible(false);
                            });
                        }
                    });
                }

                @Override
                public void onFailure(Call<Warehouse[]> call, Throwable throwable) {
                    Platform.runLater(() -> {
                        notificationPane.show();
                        maskerPane.setVisible(false);
                    });
                }
            });
        }
    }

    private void loadToGrid(Product[] products) {
        gpProducts.getRowConstraints().removeAll(gpProducts.getRowConstraints());
        gpProducts.getColumnConstraints().removeAll(gpProducts.getColumnConstraints());
        List<ColumnConstraints> columnConstraintsList = new ArrayList<>();
        List<RowConstraints> rowConstraintsList = new ArrayList<>();
        Label warehousesLabel = new Label("Warehouses");
        warehousesLabel.setRotate(28);
        warehousesLabel.setStyle("-fx-font-weight: bold;");
        warehousesLabel.setAlignment(Pos.CENTER);
        Label productsLabel = new Label("Products");
        productsLabel.setRotate(28);
        productsLabel.setAlignment(Pos.CENTER);
        Separator separator = new Separator(Orientation.HORIZONTAL);
        separator.setRotate(28);
        VBox vBox = new VBox();
        vBox.setStyle("-fx-background-color: #ADD8E6; ");
        vBox.setAlignment(Pos.CENTER);
        vBox.getChildren().addAll(warehousesLabel, separator, productsLabel);
        gpProducts.add(vBox, 0, 0, 1, 1);
        for (int i = 0; i <= products.length; i++) {
            if (i > 0) {
                Product product = products[i - 1];
                CustomPane pane = new CustomPane(product.getName());
                gpProducts.add(pane, 0, i);
            }
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setPrefHeight(60);
            rowConstraintsList.add(rowConstraints);
        }
        for (int i = 0; i <= warehouses.length; i++) {
            if (i > 0) {
                Warehouse warehouse = warehouses[i - 1];
                CustomPane pane = new CustomPane(warehouse.getName());
                gpProducts.add(pane, i, 0);
            }
            ColumnConstraints columnConstraints = new ColumnConstraints();
            columnConstraints.setPrefWidth(160);
            columnConstraintsList.add(columnConstraints);
        }
        gpProducts.getColumnConstraints().addAll(columnConstraintsList);
        gpProducts.getRowConstraints().addAll(rowConstraintsList);
        for (int i = 1; i <= products.length; i++) {
            for (int j = 1; j <= warehouses.length; j++) {
                CustomPane pane = new CustomPane("");
                Product product = products[i - 1];
                for (Warehouse.ProductsAvailable available : warehouses[j - 1].getAvailables()) {
                    if (available.getId() == product.getId())
                        pane.setLabelText(String.valueOf(available.getQuantity()));
                }
                gpProducts.add(pane, j, i, 1, 1);
            }
        }
        maskerPane.setProgress(100);
        maskerPane.setVisible(false);
    }

    public void setWarehouses(Warehouse[] warehouses) {
        this.warehouses = warehouses;
    }

    static class CustomPane extends StackPane {
        Label label;

        public CustomPane(String text) {
            this.label = new Label(text);
            label.setTextAlignment(TextAlignment.CENTER);
            setStyle("-fx-background-color: #FFFDD0; ");
            getChildren().add(label);
            setBorder(new Border(new BorderStroke(Color.YELLOWGREEN, BorderStrokeStyle.SOLID,
                    CornerRadii.EMPTY, BorderWidths.DEFAULT)));
            setAlignment(Pos.CENTER);
        }

        public void setLabelText(String text) {
            label.setText(text);
        }
    }
}
