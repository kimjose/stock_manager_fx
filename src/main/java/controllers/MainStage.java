package controllers;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
/**
 * @author kim jose
 * @see javafx.beans.value.ChangeListener
 * @see javafx.fxml.Initializable
 *
 * ****/

public class MainStage implements Initializable, ChangeListener {


    @FXML
    private TreeView<String> tvMain;

    @FXML
    private VBox vbTop;

    @FXML
    private AnchorPane apCenter;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        createTreeItems();
        tvMain.getSelectionModel().selectedItemProperty().addListener(this);
    }
    private void createTreeItems(){
        List<TreeItem<String>> treeItemList = new ArrayList<>();
        TreeItem<String> homeTree = new TreeItem<>("Dashboard");
        TreeItem<String> shopTree = new TreeItem<>("Shop");
        TreeItem<String> vendorsTree = new TreeItem<>("Vendors");
        TreeItem<String> customerTree = new TreeItem<>("Customers");

        TreeItem<String> productsTree = new TreeItem<>("Products");
        TreeItem<String> brandsTI = new TreeItem<>("Brands");
        TreeItem<String> categoriesTI = new TreeItem<>("Categories");
        TreeItem<String> uomTI = new TreeItem<>("Units of Measure");
        TreeItem<String> warehouseTI = new TreeItem<>("Warehouses");
        shopTree.getChildren().addAll(productsTree, brandsTI, categoriesTI, uomTI, warehouseTI);

        TreeItem<String> allVendors = new TreeItem<>("All Vendors");
        TreeItem<String> vInvoiceTree = new TreeItem<>("Vendor Invoices");
        vendorsTree.getChildren().addAll(allVendors, vInvoiceTree);

        TreeItem<String> allCustomers = new TreeItem<>("All Customers");
        TreeItem<String> cInvoiceTree = new TreeItem<>("Customer Invoices");
        customerTree.getChildren().addAll(allCustomers, cInvoiceTree);


        treeItemList.add(homeTree);
        treeItemList.add(shopTree);
        treeItemList.add(vendorsTree);
        treeItemList.add(customerTree);
        TreeItem<String> rootItem = new TreeItem<>("Inventory Manager");
        rootItem.getChildren().addAll(treeItemList);
        tvMain.setRoot(rootItem);
    }


    @Override
    public void changed(ObservableValue observable, Object oldValue, Object newValue) {
        TreeItem<String> selected = (TreeItem) newValue;
        System.out.println(selected.getValue());
        switch (selected.getValue()){
            //for shop
            case "Products":
            case "Categories":
            case "Units of Measure":
            case "Warehouses":
            case "Brands":
            case "All Customers":
            case "Customer Invoices":
            case "All Vendors":
            case "Vendor Invoices":{
                try {
                    FXMLLoader loader = new FXMLLoader();
                    apCenter.getChildren().clear();
                    VBox vBox = loader.load(getClass().getResource("/fxml/general_center.fxml").openStream());
                    System.out.println("params: width "+apCenter.widthProperty()+" height: "+apCenter.heightProperty());
                    vBox.prefWidthProperty().bind(apCenter.widthProperty());
                    vBox.prefHeightProperty().bind(apCenter.heightProperty());
                    GeneralCenter generalCenter = loader.getController();
                    generalCenter.setType(selected.getValue());
                    apCenter.getChildren().setAll(vBox);
                }catch (IOException e){
                    e.printStackTrace();
                }
                break;
            }


        }
    }
    @FXML
    void showDims(ActionEvent event) {
        System.out.println("params: width "+apCenter.widthProperty()+" height: "+apCenter.heightProperty());
    }
}
