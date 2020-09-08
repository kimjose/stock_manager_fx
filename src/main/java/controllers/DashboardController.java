package controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import models.DashboardData;
import network.ApiService;
import network.RetrofitBuilder;
import org.controlsfx.control.MaskerPane;
import org.controlsfx.control.NotificationPane;
import org.controlsfx.control.Notifications;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import utils.Utility;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {


    @FXML
    private AnchorPane vbParent;

    @FXML
    private NotificationPane notificationPane;

    @FXML
    private Button btnRefresh;

    @FXML
    private HBox hbSummary;

    @FXML
    private VBox vbSales;

    @FXML
    private Label labelTodaySale;

    @FXML
    private Label labelTotalSale;

    @FXML
    private Label labelNewSale;

    @FXML
    private VBox vbCustomers;

    @FXML
    private Label labelCustomers;

    @FXML
    private Label labelCustomerBalance;

    @FXML
    private Label labelNewCustomer;

    @FXML
    private VBox vbVendors;

    @FXML
    private Label labelVendors;

    @FXML
    private Label labelVendorBalance;

    @FXML
    private Label labelNewVendor;

    @FXML
    private LineChart<String, Double> lineChart;

    @FXML
    private MaskerPane maskerPane;
    private final ApiService apiService = RetrofitBuilder.createService(ApiService.class);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnRefresh.setOnAction(event -> loadData(false));
        lineChart.setTitle("Daily Sales For Last 7 Days.");
        XYChart.Series<String, Double> data = new XYChart.Series<>();
        for (int i = 0; i < 7; i++) {
            data.getData().add(new XYChart.Data<>("date", (double) i));
        }
        Platform.runLater(()->{
            vbParent.getScene().getStylesheets().add(getClass().getClassLoader().getResource("css/dashboard.css").toExternalForm());
            loadData(true);
        });

        vbSales.prefWidthProperty().bind(vbParent.widthProperty().subtract(15).divide(3));
        vbCustomers.prefWidthProperty().bind(vbParent.widthProperty().subtract(15).divide(3));
        vbVendors.prefWidthProperty().bind(vbParent.widthProperty().subtract(15).divide(3));

        labelNewCustomer.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            try {
                FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getClassLoader().getResource("fxml/create_customer.fxml")));
                VBox vBox = loader.load();
                Scene scene = new Scene(vBox);
                Stage stage = new Stage();
                stage.initOwner(vbParent.getScene().getWindow());
                stage.setScene(scene);
                CreateCustomer controller = loader.getController();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setResizable(false);
                stage.setTitle("Create Customer");
                stage.showAndWait();
                loadData(false);
            } catch (Exception e){
                notificationPane.show("We are unable to load new window.");
                e.printStackTrace();
            }
        });
        labelNewSale.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            try {
                FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getClassLoader().getResource("fxml/express_sale.fxml")));
                VBox vBox = loader.load();
                Scene scene = new Scene(vBox);
                Stage stage = new Stage();
                stage.setScene(scene);
                stage.initOwner(vbParent.getScene().getWindow());
                ExpressSaleController controller = loader.getController();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setResizable(false);
                stage.setTitle("Create Sale");
                stage.showAndWait();
                loadData(false);
            } catch (Exception e){
                notificationPane.show("We are unable to load new window.");
                e.printStackTrace();
            }
        });
        labelNewVendor.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            try {
                FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getClassLoader().getResource("fxml/create_vendor.fxml")));
                VBox vBox = loader.load();
                Scene scene = new Scene(vBox);
                Stage stage = new Stage();
                stage.setScene(scene);
                stage.initOwner(vbParent.getScene().getWindow());
                CreateVendor controller = loader.getController();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setResizable(false);
                stage.setTitle("Create Vendor");
                stage.showAndWait();
                loadData(false);
            }catch (Exception e){
                notificationPane.show("We are unable to load new window.");
                e.printStackTrace();
            }
        });
    }


    private void loadData(boolean first){
        maskerPane.setVisible(true);
        Call<DashboardData> call = apiService.dashboardData();
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<DashboardData> call, Response<DashboardData> response) {
                Platform.runLater(() -> {
                    maskerPane.setVisible(false);
                    if (response.isSuccessful()){
                        DashboardData dashboardData = response.body();
                        DashboardData.ChartData[] chartData = dashboardData.getChartData();
                        XYChart.Series<String, Double> series = new XYChart.Series<>();
                        series.setName("Daily Sales");
                        for (DashboardData.ChartData data : chartData) {
                            series.getData().add(new XYChart.Data<>(data.getName(), data.getValue()));
                        }
                        DashboardData.ChartData[] pnls = dashboardData.getPnls();
                        XYChart.Series<String, Double> profits = new XYChart.Series<>();
                        profits.setName("Profits/Losses");
                        for (DashboardData.ChartData pnl : pnls ) {
                            profits.getData().add(new XYChart.Data<>(pnl.getName(), pnl.getValue()));
                        }
                        lineChart.getData().setAll(series, profits);
                        labelTodaySale.setText(dashboardData.getTodaySalesString());
                        labelTotalSale.setText(dashboardData.getTotalSalesString());
                        labelCustomers.setText(String.valueOf(dashboardData.getCustomers()));
                        labelCustomerBalance.setText(dashboardData.getCustomerBalanceString());
                        labelVendors.setText(String.valueOf(dashboardData.getVendors()));
                        labelVendorBalance.setText(dashboardData.getVendorBalanceString());
                        if (first) {
                            loadData(false);
                        }
                    }else createNotification(-1, response.message());
                });
            }

            @Override
            public void onFailure(Call<DashboardData> call, Throwable throwable) {
                Platform.runLater(()->{
                    maskerPane.setVisible(false);
                    createNotification(-1, throwable.getMessage());
                });
            }
        });
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

}
