package controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import models.auth.User;
import network.ApiService;
import network.RetrofitBuilder;
import org.controlsfx.control.NotificationPane;
import utils.SessionManager;
import utils.Utility;

import java.net.URL;
import java.util.ResourceBundle;

public class UserProfile implements Initializable {

    @FXML
    private VBox vpParent;

    @FXML
    private HBox hbHolder;

    @FXML
    private ImageView ivPhoto;

    @FXML
    private Label labelUserName;

    @FXML
    private Label labelEmail;

    @FXML
    private Label labelFirst;

    @FXML
    private Label labelLast;

    @FXML
    private Label labelPhone;

    @FXML
    private Label labelGender;

    @FXML
    private Label labelDOB;

    @FXML
    private Label labelId;

    @FXML
    private CheckBox cbAdmin;

    @FXML
    private PasswordField pfOld;

    @FXML
    private PasswordField pfNew;

    @FXML
    private PasswordField pfConfirm;

    @FXML
    private Button btnSave;

    @FXML
    private Button btnClose;

    @FXML
    private NotificationPane notificationPane;

    private ApiService apiService = RetrofitBuilder.createService(ApiService.class);
    private User user = SessionManager.INSTANCE.getUser();



    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Utility.setupNotificationPane(notificationPane, hbHolder);

        btnClose.setOnAction(event -> Utility.closeWindow(hbHolder));
        btnSave.setOnAction(event -> changePassword());

        Platform.runLater(()->{

            Utility.setLogo(hbHolder);
            labelDOB.setText(user.getDob());
            labelUserName.setText(user.getUserName());
            labelEmail.setText(user.getEmail());
            labelFirst.setText(user.getFirstName());
            labelLast.setText(user.getLastName());
            labelPhone.setText(user.getPhoneNo());
            labelGender.setText(user.getGender());
            if (user.getPhoto() != null && !user.getPhoto().equals("")){
                Utility.decodeImage(user.getPhoto(), ivPhoto, "user.png");
            }
            labelId.setText(String.valueOf(user.getNationalId()));
            cbAdmin.setSelected(user.isAdmin());
        });


        //function keys
        vpParent.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            KeyCode keyCode = event.getCode();
            if (keyCode.equals(KeyCode.F9)) Utility.closeWindow(vpParent);
        });
    }

    private void changePassword(){//todo
        notificationPane.show("WTF hkdbfkjfnkjhduihkj");
    }

}
