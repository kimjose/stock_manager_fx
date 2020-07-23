package controllers;

import com.google.gson.Gson;
import com.jfoenix.controls.JFXTabPane;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.controlsfx.control.MaskerPane;
import org.controlsfx.control.Notifications;
import org.controlsfx.validation.*;
import utils.Utility;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

public class Subscription {

    @FXML
    private JFXTabPane tpParent;

    @FXML
    private AnchorPane apPay;

    @FXML
    private VBox vbPay;

    @FXML
    private TextField tfNumber;

    @FXML
    private TextField tfAmount;

    @FXML
    private Label labelRate;

    @FXML
    private Label labelInfo;

    @FXML
    private Button btnPay;

    @FXML
    private Button btnClose;

    @FXML
    private MaskerPane maskerPane;

    @FXML
    private AnchorPane apRenew;

    @FXML
    private VBox vbPay1;

    @FXML
    private Label labelExpire;

    @FXML
    private TextField tfCode;

    @FXML
    private Label labelInfo1;

    @FXML
    private Button btnUseCode;

    @FXML
    private Button btnClose1;

    @FXML
    private MaskerPane maskerPane1;

    ValidationSupport vsTabOne = new ValidationSupport();
    ValidationSupport vsTabTwo = new ValidationSupport();

    @FXML
    void initialize() {

        Utility.restrictInputNum(tfNumber);
        Utility.restrictInputNum(tfAmount);
        /*tfCode.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[A-Z0-9]")) {
                tfCode.setText(oldValue != null ? oldValue : "");
            }
        });*/
        btnPay.setOnAction(event -> pay());
        btnUseCode.setOnAction(event -> renew());
        btnClose.setOnAction(event -> Utility.closeWindow(tpParent));
        btnClose1.setOnAction(event -> Utility.closeWindow(tpParent));

        String text = labelExpire.getText().trim();
        Platform.runLater(()->labelExpire.setText(text + " " + Utility.checkExpiresOn()));

        tfAmount.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals("0")) tfAmount.setText("");
        });

        Platform.runLater(()->{
            Utility.setLogo(tpParent);
            vsTabOne.registerValidator(tfNumber, true, Validator.createRegexValidator("Enter a valid number", Utility.MOBILENUM_PATTERN, Severity.ERROR));
            vsTabOne.registerValidator(tfAmount, true, Validator.createRegexValidator("Enter a valid amount, greater than 10", Pattern.compile("^([1-9]\\d|[0-9]\\d{2,4})$"), Severity.ERROR));
            vsTabTwo.registerValidator(tfCode, true, Validator.createRegexValidator("Invalid confirmation code", Utility.MPESACODE_PATTERN, Severity.ERROR));
        });
    }

    private void pay() {
        ValidationResult vr = vsTabOne.getValidationResult();
        Iterator<ValidationMessage> iterator = vr.getErrors().iterator();
        StringBuilder message = new StringBuilder();
        while (iterator.hasNext()){
            message.append(iterator.next().getText());
            message.append("\n");
        }
        if (!vr.getErrors().isEmpty()){
            createNotification(-1, message.toString());
            return;
        }
        String amount = tfAmount.getText().trim();
        String no = tfNumber.getText();
        no = no.replaceFirst("0", Utility.COUNTRY_CODE);
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("phoneNo", no);
        params.put("amount", amount);
        Task<String> task = new Task<>() {
            @Override
            protected String call() {
                Platform.runLater(() -> maskerPane.setVisible(true));
                return connect(Utility.STK_URL, params);
            }
        };
        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                try {
                    Platform.runLater(() -> maskerPane.setVisible(false));
                    String result = task.get();
                    createNotification(1, result);
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    createNotification(-1, Utility.SUBSCRIBE_ERROR);
                }
                event.consume();
            });
        });
        task.setOnFailed(event -> Platform.runLater(()-> maskerPane.setVisible(false)));
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void renew() {
        LocalDate date = Utility.checkExpiresOn().isAfter(LocalDate.now()) ? Utility.checkExpiresOn() : LocalDate.now();
        String code = tfCode.getText().trim();
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("transId", code);
        params.put("lastDate", date);
        params.put("productId", Utility.VERSION);
        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                Platform.runLater(() -> maskerPane1.setVisible(true));
                return connect(Utility.RENEW_URL, params);
            }
        };
        task.setOnSucceeded(event -> Platform.runLater(() -> {
            try {
                maskerPane1.setVisible(false);
                String result = task.get();
                System.out.println(result);
                Gson gson = new Gson();
                SubscriptionResponse response = gson.fromJson(result, SubscriptionResponse.class);
                if (response.responseCode == 200) {
                    Utility.updateExpireDate(response.expiresOn);
                    if (LocalDate.now().isAfter(LocalDate.parse(response.expiresOn))) {
                        createNotification(1, "Not completed. Try again");
                        return;
                    }
                    createNotification(0, "Updated subscription all through "+response.expiresOn+". Thanks.");
                    Utility.closeWindow(tpParent);
                }
                else createNotification(-1, response.message);
            } catch (Exception e) {
                e.printStackTrace();
                createNotification(-1, Utility.SUBSCRIBE_ERROR);
            }
            event.consume();
        }));
        task.setOnFailed(event -> Platform.runLater(()-> maskerPane1.setVisible(false)));
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private String connect(String link, Map<String, Object> params) {
        try {
            String response;
            URL url = new URL(link);
            StringBuilder stringBuilder = new StringBuilder();
            for (HashMap.Entry<String, Object> param : params.entrySet()) {
                if (stringBuilder.length() != 0) {
                    stringBuilder.append('&');
                }
                stringBuilder.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                stringBuilder.append('=');
                stringBuilder.append(URLEncoder.encode(param.getValue().toString(), "UTF-8"));
            }
            byte[] dataBytes = stringBuilder.toString().getBytes("UTF-8");
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            httpURLConnection.setRequestProperty("Content-Length", String.valueOf(dataBytes.length));
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.getOutputStream().write(dataBytes);
            BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "UTF-8"));
            String line = "";
            StringBuffer buffer = new StringBuffer();
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            response = buffer.toString();
            reader.close();
            httpURLConnection.disconnect();
            return response;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return e.getMessage();
        }
    }

    private void createNotification(int type, String message) {
        Notifications n = Notifications.create()
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

    static class SubscriptionResponse{
        private int responseCode;
        private String message, expiresOn;

        public int getResponseCode() {
            return responseCode;
        }

        public void setResponseCode(int responseCode) {
            this.responseCode = responseCode;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getExpiresOn() {
            return expiresOn;
        }

        public void setExpiresOn(String expiresOn) {
            this.expiresOn = expiresOn;
        }
    }
}
