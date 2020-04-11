package utils;

import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import network.ApiError;
import network.RetrofitBuilder;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.controlsfx.control.NotificationPane;
import org.jetbrains.annotations.NotNull;
import retrofit2.Converter;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

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

    /**
     * This method is for handling api errors.
     * When an API request returns an error the message is too general and this method gets the specific details of the error.
     * @param defaultMessage
     * The default general message.
     * @param responseBody
     * The error body from the response.
     * @param keys
     * An array of strings against which specific messages are obtained.
     *
     * @return The specific message or general message if an error is caught.
     * ****/
    public static String handleApiErrors(@NotNull String defaultMessage, @NotNull ResponseBody responseBody, String[] keys){
        Converter<ResponseBody, ApiError> converter = RetrofitBuilder.retrofit.responseBodyConverter(ApiError.class, new Annotation[0]);
        try{
            ApiError apiError = converter.convert(responseBody);
            String message = "";
            assert apiError != null;
            for (Map.Entry<String, List<String>> error: apiError.getErrors().entrySet()) {
                for (String key: keys) {
                    if (error.getKey().equals(key)) message = message.concat(message.equals("")?error.getValue().get(0):"\n"+error.getValue().get(0));
                }
            }
            if (message.equals("")) throw new Exception();
            return message;
        }catch (Exception e) {
            e.printStackTrace();
        }
        return defaultMessage;
    }

}
