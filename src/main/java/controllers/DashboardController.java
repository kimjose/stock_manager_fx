package controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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

import java.net.URL;
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
    private Label labelTodaySale, labelTotalSale, labelCustomerBalance, labelVendorBalance;

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
        Platform.runLater(()->loadData(true));
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
                            series.getData().add(new XYChart.Data<>(data.getDate(), data.getSales()));
                        }
                        lineChart.getData().setAll(series);
                        labelTodaySale.setText(dashboardData.getTodaySalesString());
                        labelTotalSale.setText(dashboardData.getTotalSalesString());
                        labelCustomerBalance.setText(dashboardData.getCustomerBalanceString());
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
