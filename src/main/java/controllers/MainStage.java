package controllers;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import models.auth.User;
import utils.SessionManager;
import utils.Utility;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * @author kim jose
 * @see javafx.beans.value.ChangeListener
 * @see javafx.fxml.Initializable
 ****/

public class MainStage implements Initializable, ChangeListener {

    @FXML
    private BorderPane bpParent;

    @FXML
    private TreeView<String> tvMain;

    @FXML
    private Label labelUserName;

    @FXML
    private FontAwesomeIconView faivExit;

    @FXML
    private ImageView ivUserPhoto;


    @FXML
    private AnchorPane apCenter;

    private final User user = SessionManager.INSTANCE.getUser();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        createTreeItems();
        tvMain.getSelectionModel().selectedItemProperty().addListener(this);
        tvMain.getSelectionModel().select(1);
        if (user != null) {
            labelUserName.setText(user.getUserName());
            if (user.getPhoto() != null && !user.getPhoto().equals("")) {
                Utility.decodeImage(user.getPhoto(), ivUserPhoto, "user.png");
            }
        }
        faivExit.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> System.exit(0));
        ivUserPhoto.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("fxml/user_profile.fxml"));
                VBox box = loader.load();
                Scene scene = new Scene(box);
                Stage stage = new Stage();
                stage.setScene(scene);
                stage.setTitle("My Profile");
                stage.setResizable(false);
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.initStyle(StageStyle.UNIFIED);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }

        });
        Platform.runLater(() -> {
            LocalDate expiresOn = Utility.checkExpiresOn();
            if (expiresOn.isBefore(LocalDate.now()) || expiresOn.isEqual(LocalDate.now())) Utility.subscribe();
        });
    }

    private void createTreeItems() {
        List<TreeItem<String>> treeItemList = new ArrayList<>();
        TreeItem<String> homeTree = new TreeItem<>("Dashboard");
        FontAwesomeIconView iconView = new FontAwesomeIconView();
        iconView.setIcon(FontAwesomeIcon.DASHBOARD);
        iconView.setFill(Paint.valueOf("#FFFFFF"));
        homeTree.setGraphic(iconView);
        TreeItem<String> shopTree = new TreeItem<>("Shop");
        iconView = new FontAwesomeIconView();
        iconView.setIcon(FontAwesomeIcon.SHOPPING_CART);
        iconView.setFill(Paint.valueOf("#FFFFFF"));
        shopTree.setGraphic(iconView);
        TreeItem<String> financeTree = new TreeItem<>("Finance");
        iconView = new FontAwesomeIconView();
        iconView.setIcon(FontAwesomeIcon.MONEY);
        iconView.setFill(Paint.valueOf("#FFFFFF"));
        financeTree.setGraphic(iconView);
        iconView = new FontAwesomeIconView();
        iconView.setIcon(FontAwesomeIcon.USERS);
        iconView.setFill(Paint.valueOf("#FFFFFF"));
        TreeItem<String> usersTree = new TreeItem<>("Users");
        usersTree.setGraphic(iconView);

        TreeItem<String> vendorsTree = new TreeItem<>("Vendors");
        TreeItem<String> customerTree = new TreeItem<>("Customers");

        TreeItem<String> banksTI = new TreeItem<>("Banks");
        TreeItem<String> profitNLossTI = new TreeItem<>("Profits and Losses");
        financeTree.getChildren().addAll(banksTI, profitNLossTI);

        TreeItem<String> productsTree = new TreeItem<>("Products");
        TreeItem<String> brandsTI = new TreeItem<>("Brands");
        TreeItem<String> categoriesTI = new TreeItem<>("Categories");
        TreeItem<String> uomTI = new TreeItem<>("Units of Measure");
        TreeItem<String> warehouseTI = new TreeItem<>("Warehouses");
        TreeItem<String> productGroupsTI = new TreeItem<>("Product Groups");
        TreeItem<String> unpackingTI = new TreeItem<>("Unpackings");
        TreeItem<String> postedUnpackingTI = new TreeItem<>("Posted Unpackings");
        TreeItem<String> reversedUnpackingTI = new TreeItem<>("Reversed Unpackings");
        TreeItem<String> serviceTI = new TreeItem<>("Services");
        TreeItem<String> eSaleTI = new TreeItem<>("Express Sales");
        TreeItem<String> peSaleTI = new TreeItem<>("Posted Express Sales");
        TreeItem<String> reSaleTI = new TreeItem<>("Reversed Express Sales");
        shopTree.getChildren().addAll(productsTree, brandsTI, categoriesTI, uomTI, warehouseTI, productGroupsTI, unpackingTI,
                postedUnpackingTI, reversedUnpackingTI, serviceTI, eSaleTI, peSaleTI, reSaleTI);

        TreeItem<String> allVendors = new TreeItem<>("All Vendors");
        TreeItem<String> vInvoiceTree = new TreeItem<>("Vendor Invoices");
        TreeItem<String> pvInvoiceTree = new TreeItem<>("Posted Vendor Invoices");
        TreeItem<String> rvInvoiceTree = new TreeItem<>("Reversed Vendor Invoices");
        TreeItem<String> paymentVoucherTree = new TreeItem<>("Payment Vouchers");
        TreeItem<String> pPaymentVoucherTree = new TreeItem<>("Posted Payment Vouchers");
        TreeItem<String> rPaymentVoucherTree = new TreeItem<>("Reversed Payment Vouchers");
        vendorsTree.getChildren().addAll(allVendors, vInvoiceTree, pvInvoiceTree, rvInvoiceTree,
                paymentVoucherTree, pPaymentVoucherTree, rPaymentVoucherTree);

        TreeItem<String> allCustomers = new TreeItem<>("All Customers");
        TreeItem<String> cInvoiceTree = new TreeItem<>("Customer Invoices");
        TreeItem<String> pcInvoiceTree = new TreeItem<>("Posted Customer Invoices");
        TreeItem<String> rcInvoiceTree = new TreeItem<>("Reversed Customer Invoices");
        TreeItem<String> receiptsTree = new TreeItem<>("Receipts");
        TreeItem<String> pReceiptsTree = new TreeItem<>("Posted Receipts");
        TreeItem<String> rReceiptsTree = new TreeItem<>("Reversed Receipts");
        customerTree.getChildren().addAll(allCustomers, cInvoiceTree, pcInvoiceTree, rcInvoiceTree,
                receiptsTree, pReceiptsTree, rReceiptsTree);


        treeItemList.add(homeTree);
        treeItemList.add(shopTree);
        treeItemList.add(financeTree);
        treeItemList.add(vendorsTree);
        treeItemList.add(customerTree);
        treeItemList.add(usersTree);
        TreeItem<String> rootItem = new TreeItem<>("Inventory Manager");
        rootItem.getChildren().addAll(treeItemList);
        rootItem.setExpanded(true);
        tvMain.setRoot(rootItem);
    }


    @Override
    public void changed(ObservableValue observable, Object oldValue, Object newValue) {
        TreeItem<String> selected = (TreeItem) newValue;
        System.out.println(selected.getValue());
        switch (selected.getValue()) {

            case "Dashboard": {
                try {
                    FXMLLoader loader = new FXMLLoader();
                    AnchorPane anchorPane = loader.load(getClass().getResource("/fxml/dashboard.fxml").openStream());
                    anchorPane.prefWidthProperty().bind(bpParent.widthProperty().subtract(220));
                    anchorPane.prefHeightProperty().bind(bpParent.heightProperty().subtract(50));
                    System.out.println("params: width " + apCenter.widthProperty() + " height: " + apCenter.heightProperty());
                    List<Node> nodeList = new ArrayList<>();
                    nodeList.add(anchorPane);
                    apCenter.getChildren().setAll(nodeList);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }

            case "Users":
            case "Banks":
            case "Profits and Losses":
            case "Products":
            case "Categories":
            case "Units of Measure":
            case "Warehouses":
            case "Brands":
            case "Product Groups":
            case "Unpackings":
            case "Posted Unpackings":
            case "Reversed Unpackings":
            case "Services":
            case "Express Sales":
            case "Posted Express Sales":
            case "Reversed Express Sales":
            case "All Customers":
            case "Customer Invoices":
            case "Posted Customer Invoices":
            case "Reversed Customer Invoices":
            case "Receipts":
            case "Posted Receipts":
            case "Reversed Receipts":
            case "All Vendors":
            case "Payment Vouchers":
            case "Posted Payment Vouchers":
            case "Reversed Payment Vouchers":
            case "Vendor Invoices":
            case "Posted Vendor Invoices":
            case "Reversed Vendor Invoices": {
                try {
                    FXMLLoader loader = new FXMLLoader();
                    VBox vBox = loader.load(getClass().getResource("/fxml/general_center.fxml").openStream());
                    System.out.println("params: width " + apCenter.widthProperty() + " height: " + apCenter.heightProperty());
                    GeneralCenter generalCenter = loader.getController();
                    generalCenter.setType(selected.getValue());
                    apCenter.getChildren().setAll(vBox);
                    vBox.prefWidthProperty().bind(bpParent.widthProperty().subtract(220));
                    vBox.prefHeightProperty().bind(bpParent.heightProperty().subtract(50));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }


        }
    }

    @FXML
    void showDims(ActionEvent event) {
        System.out.println("params: width " + apCenter.widthProperty() + " height: " + apCenter.heightProperty());
        SessionManager sessionManager = SessionManager.INSTANCE;
        System.out.println(sessionManager.getUser().getUserName());
    }
}
