package utils;

import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.controlsfx.control.NotificationPane;
import org.jetbrains.annotations.NotNull;

/**
 * @author kim jose
 * This class holds methods that will be used and re-used throughout the program
 *
 * ***/

public class Utility {

    /**
     * adds properties to a notificationpane
     * @param notificationPane The notificationpane to which content and other properties are to be added.
     * @param content The holder of the notificationpane
     * **/
    public static void setupNotificationPane(@NotNull NotificationPane notificationPane, Node content){
        notificationPane.setContent(content);
        notificationPane.getStyleClass().add(NotificationPane.STYLE_CLASS_DARK);
        notificationPane.setCloseButtonVisible(true);
    }

    public static void closeWindow(Node node){
        Stage stage = (Stage) node.getScene().getWindow();
        stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    public static void restrictInputDec(TextField t){
        t.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*([.]\\d{0,4})?")) {
                t.setText(oldValue!=null?oldValue:"");
            }
        });
    }

    public static void restrictInputNum(TextField t){
        t.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d{0,15}")) {
                t.setText(oldValue!=null?oldValue:"");
            }
        });
    }

}
