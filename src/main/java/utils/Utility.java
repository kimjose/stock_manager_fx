package utils;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JsonDataSource;
import net.sf.jasperreports.view.JasperViewer;
import network.ApiError;
import network.RetrofitBuilder;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.commons.io.FileUtils;
import org.controlsfx.control.NotificationPane;
import org.jetbrains.annotations.NotNull;
import retrofit2.Converter;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.annotation.Annotation;
import java.text.NumberFormat;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author kim jose
 * This class holds methods that will be used and re-used throughout the program
 ***/

public class Utility {

    public static final String USER_DIR = System.getProperty("user.dir");

    /**
     * adds properties to a notificationpane
     *
     * @param notificationPane The notificationpane to which content and other properties are to be added.
     * @param content          The holder of the notificationpane
     **/
    public static void setupNotificationPane(@NotNull NotificationPane notificationPane, Node content) {
        notificationPane.setContent(content);
        notificationPane.getStyleClass().add(NotificationPane.STYLE_CLASS_DARK);
        notificationPane.setCloseButtonVisible(true);
    }

    public static void closeWindow(Node node) {
        Stage stage = (Stage) node.getScene().getWindow();
        Platform.runLater(() -> stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST)));
    }

    /**
     * This method sets a textfield to only accept decimals.
     *
     * @param t the textfield
     **/
    public static void restrictInputDec(TextField t) {
        t.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*([.]\\d{0,4})?")) {
                t.setText(oldValue != null ? oldValue : "");
            }
        });
    }

    /**
     * This method sets a textfield to only accept numbers(+integers).
     * of length 15 characters
     *
     * @param t the textfield
     **/
    public static void restrictInputNum(TextField t) {
        t.textProperty().addListener((observable, oldValue, newValue) -> {
            assert newValue != null;
            if (!newValue.matches("\\d{0,15}")) {
                t.setText(oldValue != null ? oldValue : "");
            }
        });
    }

    /**
     * This method is for handling api errors.
     * When an API request returns an error the message is too general and this method gets the specific details of the error.
     *
     * @param defaultMessage The default general message.
     * @param responseBody   The error body from the response.
     * @param keys           An array of strings against which specific messages are obtained.
     * @return The specific message or general message if an error is caught.
     ****/
    public static String handleApiErrors(@NotNull String defaultMessage, @NotNull ResponseBody responseBody, String[] keys) {
        Converter<ResponseBody, ApiError> converter = RetrofitBuilder.retrofit.responseBodyConverter(ApiError.class, new Annotation[0]);
        try {
            ApiError apiError = converter.convert(responseBody);
            System.out.println(apiError.toString());
            String message = "";
            assert apiError != null;
            for (Map.Entry<String, List<String>> error : apiError.getErrors().entrySet()) {
                for (String key : keys) {
                    if (error.getKey().equals(key))
                        message = message.concat(message.equals("") ? error.getValue().get(0) : "\n" + error.getValue().get(0));
                }
            }
            if (message.equals("")) throw new Exception();
            return message;
        } catch (Exception ignored) {
        }
        return defaultMessage;
    }

    /**
     * Formats a double number to a string that is easily readable
     *
     * @param number The number
     ***/
    public static String formatNumber(double number) {
        return NumberFormat.getInstance().format(number);

    }

    /**
     * Removes commas from a string number format
     *
     * @param number The string number
     ***/
    public static String reformatNumber(String number) {
        return number.replace(",", "");
    }

    public static String encodeImage(String imagePath) {
        try {
            byte[] fileContent = FileUtils.readFileToByteArray(new File(imagePath));
            return Base64.getEncoder().encodeToString(fileContent);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void decodeImage(String base64Image, ImageView holder) {
        try {
            File outputFile = new File(System.getProperty("user.dir") + File.separator + "temp.png");
            byte[] imageData = Base64.getDecoder().decode(base64Image);
            FileUtils.writeByteArrayToFile(outputFile, imageData);
            holder.setImage(new Image(outputFile.toURI().toString()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Node showProgressBar(String message) {
        try {
            FXMLLoader loader = new FXMLLoader(Utility.class.getClassLoader().getResource("fxml/loading.fxml"));
            AnchorPane pane = loader.load();
            Label label = (Label) pane.getChildren().get(1);
            label.setText(message);
            Scene scene = new Scene(pane);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.setTitle("Loading");
            Platform.runLater(stage::show);
            return pane;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void showReport(String reportName, Map<String, Object> variables, String json) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(json.getBytes());
            JsonDataSource dataSource = new JsonDataSource(inputStream);
            String path = getReportLocation()+reportName;
            System.out.println(path);
            JasperPrint print = JasperFillManager.fillReport(path, variables, dataSource);
            JasperViewer viewer = new JasperViewer(print, false);
            viewer.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            viewer.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
            viewer.setTitle("My Report");
            viewer.setExtendedState(Frame.MAXIMIZED_BOTH);
            viewer.setAlwaysOnTop(true);
            viewer.setVisible(true);
            viewer.requestFocus();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getReportLocation() {
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream("im.properties"));
            String loc = USER_DIR+properties.getProperty("reportsLocation");
            loc = loc.replace("\\", "/");
            return loc;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
