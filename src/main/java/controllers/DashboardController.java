package controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.controlsfx.control.MaskerPane;
import org.controlsfx.control.NotificationPane;

import java.net.URL;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML
    private VBox vbParent;

    @FXML
    private NotificationPane notificationPane;

    @FXML
    private Button btnRefresh;

    @FXML
    private HBox hbSummary;

    @FXML
    private Label labelTodaySale;

    @FXML
    private LineChart<?, ?> chartData;

    private MaskerPane maskerPane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        MaskerPane maskerPane = new MaskerPane();
        maskerPane.prefHeightProperty().bind(vbParent.heightProperty().multiply(0.98));
        maskerPane.prefWidthProperty().bind(vbParent.widthProperty().multiply(0.98));
        vbParent.getChildren().add(maskerPane);
        maskerPane.setVisible(false);

    }

    private void loadData(){

    }
}
