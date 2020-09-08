package utils;

import com.jfoenix.controls.JFXTabPane;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.*;
import javafx.stage.Window;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.engine.data.JsonDataSource;
import net.sf.jasperreports.view.JasperViewer;
import network.ApiError;
import network.RetrofitBuilder;
import okhttp3.ResponseBody;
import org.apache.commons.io.FileUtils;
import org.controlsfx.control.NotificationPane;
import org.jetbrains.annotations.NotNull;
import retrofit2.Converter;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.lang.annotation.Annotation;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author kim jose
 * This class holds methods and Objects that will be used and re-used throughout the program
 ***/
public class Utility {

    public static final String USER_DIR = System.getProperty("user.dir");
    public static final String USER_IMAGE = "file:/"+USER_DIR+ File.separator+"user.png";
    public static final String LOGO_IMAGE = "file:/"+USER_DIR+ File.separator+"logo.png";
    public static final String IM_LOGO_IMAGE = "file:/"+USER_DIR+ File.separator+"im_logo.png";
    public static final Pattern MOBILENUM_PATTERN = Pattern.compile("0[0-9]{9}");
    public static final Pattern MPESACODE_PATTERN = Pattern.compile("[A-Z0-9]{10,12}");
    public static final Pattern EMAIL_PATTERN = Pattern.compile("[A-Za-z0-9.%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}");
    public static final String VERSION = readProperty("version", "IM001");
    public static final String SUBSCRIBE_ERROR = readProperty("subscribeError", "Error");
    public static final String BASE_URL = readProperty("baseUrl", "http://localhost/StockManager/public/api/");
    public static final String STK_URL = readProperty("stkUrl", "https://infinitops.co.ke/subscribe/Subscribe/?request=lipa");
    public static final String RENEW_URL = readProperty("renewUrl", "https://infinitops.co.ke/subscribe/Subscribe/?request=renew");
    public static final String COUNTRY_CODE = readProperty("countryCode", "254");
    public static final String CLIENT_NAME = readProperty("clientName", "Client Name");

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
        Platform.runLater(()->stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST)));
    }

    public static void setLogo(Node node) {
        Stage stage = (Stage) node.getScene().getWindow();
        Platform.runLater(()->stage.getIcons().add(new Image(IM_LOGO_IMAGE)));
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

    public static void decodeImage(String base64Image, ImageView holder, String name) {
        try {
            File outputFile = new File(USER_DIR + File.separator + name);
            byte[] imageData = Base64.getDecoder().decode(base64Image);
            FileUtils.writeByteArrayToFile(outputFile, imageData);
            holder.setImage(new Image(outputFile.toURI().toString()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method creates and displays a progress dialog.
     * @param message Info to be displayed.
     * @param owner The window to be the owner of this progress dialog
     *
     * @return A node in the dialog.
     * **/
    public static Node showProgressBar(String message, javafx.stage.Window owner) {
        try {
            FXMLLoader loader = new FXMLLoader(Utility.class.getClassLoader().getResource("fxml/loading.fxml"));
            AnchorPane pane = loader.load();
            Label label = (Label) pane.getChildren().get(1);
            label.setText(message);
            Scene scene = new Scene(pane);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.initOwner(owner);
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
            String path = getReportLocation() + reportName;
            JasperPrint print = JasperFillManager.fillReport(path, variables, dataSource);
            JasperViewer viewer = new JasperViewer(print, false);
            viewer.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            viewer.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
            viewer.setTitle("My Report");
            viewer.setExtendedState(Frame.MAXIMIZED_BOTH);
            viewer.setAlwaysOnTop(true);
            Platform.runLater(()->{
                viewer.setVisible(true);
                viewer.requestFocus();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /***
     * @deprecated This method no longer applies.
     * */
    public static void printReport(String reportName, Map<String, Object> variables, String json){
        try{
            ByteArrayInputStream inputStream = new ByteArrayInputStream(json.getBytes());
            JsonDataSource dataSource = new JsonDataSource(inputStream);
            String path = getReportLocation() + reportName;
            JasperPrint print = JasperFillManager.fillReport(path, variables, dataSource);
            JasperPrintManager.printPage(print, 0, true);
            //JasperPrintManager.printPages(print, 0, print.getPages().size(), true);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static String getReportLocation() {
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream("im.properties"));
            String loc = USER_DIR + properties.getProperty("reportsLocation");
            loc = loc.replace("\\", "/");
            return loc;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void subscribe(Window owner){
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(Utility.class.getClassLoader().getResource("fxml/subscription.fxml")));
            JFXTabPane pane = loader.load();
            Scene scene = new Scene(pane);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.initOwner(owner);
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.setResizable(false);
            stage.setOnCloseRequest(event -> {
                LocalDate expiresOn = checkExpiresOn();
                if (expiresOn.equals(LocalDate.now()) || expiresOn.isBefore(LocalDate.now())) System.exit(3);
            });
            stage.show();
        } catch (Exception e){ e.printStackTrace();}
    }

    public static LocalDate checkExpiresOn(){
        String date = readProperty("expiresOn", LocalDate.now().toString());
        return LocalDate.parse(date);
    }
    public static void updateExpireDate(String expiresOn){
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream("im.properties"));
            properties.replace("expiresOn", expiresOn);
            properties.store(new FileOutputStream("im.properties"), "Subscription update.");
            properties.store(new FileOutputStream("expire.im"), "Subscription update.");
            properties.storeToXML(new FileOutputStream("properties.xml"), "Subscription update.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static String readProperty(String property, String defaultResponse){
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream("im.properties"));
            return properties.getProperty(property, defaultResponse);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return defaultResponse;
    }
}
