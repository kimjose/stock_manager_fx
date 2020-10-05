package controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
    private VBox vbData;

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
    private ScrollPane spDashboard;

    @FXML
    private VBox vbCharts;

    @FXML
    private LineChart<String, Double> lineChart;

    @FXML
    private BarChart<String, Double> bcMonthlySales;

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
/*
        vbSales.prefWidthProperty().bind(vbParent.widthProperty().subtract(15).divide(3));
        vbCustomers.prefWidthProperty().bind(vbParent.widthProperty().subtract(15).divide(3));
        vbVendors.prefWidthProperty().bind(vbParent.widthProperty().subtract(15).divide(3));*/

        spDashboard.prefHeightProperty().bind(vbParent.heightProperty().subtract(60));
        spDashboard.prefWidthProperty().bind(vbParent.widthProperty());
        hbSummary.prefHeightProperty().bind(vbParent.heightProperty().subtract(80));
        hbSummary.prefWidthProperty().bind(vbParent.widthProperty().subtract(10));

        vbCharts.prefWidthProperty().bind(hbSummary.widthProperty().subtract(196));
        vbCharts.prefHeightProperty().bind(hbSummary.heightProperty().subtract(10));
        lineChart.prefWidthProperty().bind(vbCharts.widthProperty());
        lineChart.prefHeightProperty().bind(vbCharts.heightProperty().divide(2));
        bcMonthlySales.prefWidthProperty().bind(vbCharts.widthProperty());
        bcMonthlySales.prefHeightProperty().bind(vbCharts.heightProperty().divide(2));

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
        vbParent.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.F5) loadData(false);
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
                        XYChart.Series<String, Double> dailySales = new XYChart.Series<>();
                        dailySales.setName("Daily Sales");
                        for (DashboardData.ChartData data : chartData) {
                            dailySales.getData().add(new XYChart.Data<>(data.getName(), data.getValue()));
                        }
                        DashboardData.ChartData[] pnls = dashboardData.getPnls();
                        XYChart.Series<String, Double> profits = new XYChart.Series<>();
                        profits.setName("Profits/Losses");
                        for (DashboardData.ChartData pnl : pnls ) {
                            profits.getData().add(new XYChart.Data<>(pnl.getName(), pnl.getValue()));
                        }
                        XYChart.Series<String, Double> monthSales = new XYChart.Series<>();
                        monthSales.setName("Sales");
                        for (DashboardData.ChartData sales : dashboardData.getBarChartData() ) {
                            monthSales.getData().add(new XYChart.Data<>(sales.getName(), sales.getValue()));
                        }
                        XYChart.Series<String, Double> monthProfits = new XYChart.Series<>();
                        monthProfits.setName("Profits/Losses");
                        for (DashboardData.ChartData monthPnLs : dashboardData.getMonthPNLs() ) {
                            monthProfits.getData().add(new XYChart.Data<>(monthPnLs.getName(), monthPnLs.getValue()));
                        }
                        lineChart.getData().setAll(dailySales, profits);
                        bcMonthlySales.getData().setAll(monthSales, monthProfits);
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
