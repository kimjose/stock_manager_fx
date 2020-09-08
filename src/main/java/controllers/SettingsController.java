package controllers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jfoenix.controls.JFXComboBox;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.animation.Animation;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.controlsfx.control.NotificationPane;
import utils.Utility;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {

    @FXML
    private VBox vbParent;

    @FXML
    private NotificationPane notificationPane;

    @FXML
    private VBox vbHolder;

    @FXML
    private FontAwesomeIconView faivSetting;

    @FXML
    private JFXComboBox<String> cbCode;

    @FXML
    private JFXComboBox<String> cbPrinter;

    @FXML
    private Button btnSave;

    private RotateTransition rotateTransition = new RotateTransition();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Utility.setupNotificationPane(notificationPane, vbHolder);
        String[] printers = {"me", "u"};
        String[] codes = {"254", "256", "257"};
        cbCode.setItems(FXCollections.observableArrayList(codes));
        cbPrinter.setItems(FXCollections.observableArrayList(printers));
        rotateTransition.setByAngle(360);
        rotateTransition.setDuration(Duration.millis(1500));
        rotateTransition.setCycleCount(Animation.INDEFINITE);
        rotateTransition.setNode(faivSetting);
        rotateTransition.play();
        Platform.runLater(() -> vbHolder.getScene().getWindow().setOnCloseRequest(event -> rotateTransition.stop()));
        loadDefaults();
        btnSave.setOnAction(event -> {
            String code = cbCode.getValue();
            String printer = cbPrinter.getValue();
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("countryCode", code);
            jsonObject.addProperty("defaultPrinter", printer);
            try {
                FileWriter writer = new FileWriter("settings.json", false);
                writer.write(jsonObject.toString());
                writer.flush();
                writer.close();
                Utility.closeWindow(vbHolder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    void cancel(ActionEvent event) {
        Utility.closeWindow(vbHolder);
        event.consume();
    }

    private void loadDefaults() {
        JsonParser parser = new JsonParser();
        try {
            JsonElement element = parser.parse(new FileReader("settings.json"));
            JsonObject jsonObject = element.getAsJsonObject();
            String code = jsonObject.get("countryCode").toString();
            String printer = jsonObject.get("defaultPrinter").toString();
            cbCode.getSelectionModel().select(code);
            cbPrinter.getSelectionModel().select(printer);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}
