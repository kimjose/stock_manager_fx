package controllers;

import interfaces.HomeDataInterface;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import models.auth.User;
import network.ApiService;
import network.RetrofitBuilder;
import org.controlsfx.control.NotificationPane;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import utils.SessionManager;
import utils.Utility;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class CreateUser implements Initializable {

    @FXML
    private VBox vbParent;

    @FXML
    private NotificationPane notificationPane;

    @FXML
    private HBox hbHolder;

    @FXML
    private TextField tfUsername;

    @FXML
    private TextField tfEmail;

    @FXML
    private TextField tfLastName;

    @FXML
    private TextField tfPhoneNo;

    @FXML
    private TextField tfFirstName;

    @FXML
    private TextField tfNationalId;

    @FXML
    private DatePicker dpDOB;

    @FXML
    private PasswordField pfPassword;

    @FXML
    private RadioButton rbMale;

    @FXML
    private RadioButton rbFemale;

    @FXML
    private CheckBox cbAdmin;

    @FXML
    private ImageView ivPhoto;

    @FXML
    private Button btnSave;

    @FXML
    private Button btnCancel;

    private final ApiService apiService = RetrofitBuilder.createService(ApiService.class);
    private final User current = SessionManager.INSTANCE.getUser();
    private User user;
    private HomeDataInterface dataInterface;
    private String base64Image = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        Utility.setupNotificationPane(notificationPane, hbHolder);
        Utility.restrictInputNum(tfNationalId);
        Utility.restrictInputNum(tfPhoneNo);
        ToggleGroup toggleGroup = new ToggleGroup();
        rbFemale.setToggleGroup(toggleGroup);
        rbMale.setToggleGroup(toggleGroup);
        rbMale.setSelected(true);
        dpDOB.setValue(LocalDate.now());

        ContextMenu contextMenu = new ContextMenu();
        MenuItem miSelect = new MenuItem("Select from Files");
        MenuItem miReset = new MenuItem("Reset");
        miSelect.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Product Image");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("All Images", "*.jpg", ".png"),
                    new FileChooser.ExtensionFilter("JPEG", "*.jpg"),
                    new FileChooser.ExtensionFilter("PNG", "*.png")
            );
            File file = fileChooser.showOpenDialog(vbParent.getScene().getWindow());
            if (file != null) {
                ivPhoto.setImage(new Image(file.toURI().toString()));
                base64Image = Utility.encodeImage(file.getPath());
            }
        });
        miReset.setOnAction(event -> {
            base64Image = null;
            ivPhoto.setImage(new Image(Utility.USER_IMAGE));
        });
        contextMenu.getItems().add(miSelect);
        contextMenu.getItems().add(miReset);
        ivPhoto.setOnContextMenuRequested(event -> contextMenu.show(ivPhoto, event.getScreenX(), event.getScreenY()));
        Platform.runLater(() -> Utility.setLogo(vbParent));
        btnSave.setOnAction(event -> save());
        btnCancel.setOnAction(event -> Utility.closeWindow(hbHolder));
    }

    private void save() {
        String errorMessage = "",
                username = tfUsername.getText().trim(),
                firstName = tfFirstName.getText().trim(),
                lastName = tfLastName.getText().trim(),
                email = tfEmail.getText().trim(),
                password = pfPassword.getText().trim(),
                nationalId = tfNationalId.getText().trim(),
                phoneNo = tfPhoneNo.getText().trim(),
                gender = rbMale.isSelected() ? "Male" : "Female",
                date = "";
        boolean admin = cbAdmin.isSelected();
        if (username.equals("")) errorMessage = "Enter a valid username";
        if (email.equals("")) errorMessage += errorMessage.equals("") ? "Enter a valid email" : "\nEnter a valid email";
        else {
            if (!email.matches(Utility.EMAIL_PATTERN.pattern()))
                errorMessage += errorMessage.equals("") ? "Enter a valid email" : "\nEnter a valid email";
        }
        if (firstName.equals(""))
            errorMessage += errorMessage.equals("") ? "First name is required" : "\nFirst name is required";
        if (lastName.equals(""))
            errorMessage += errorMessage.equals("") ? "Last name is required" : "\nLast name is required";
        if (phoneNo.equals(""))
            errorMessage += errorMessage.equals("") ? "Phone No is required" : "\nPhone No is required";
        if (password.length() < 6 && user == null && !password.equals(""))
            errorMessage += errorMessage.equals("") ? "Password must have atleast 6 characters" : "\nPassword must have atleast 6 characters";
        if (nationalId.length() < 5)
            errorMessage += errorMessage.equals("") ? "Enter a valid national Id" : "\nEnter a valid national Id";
        try {
            date = dpDOB.getValue().toString();
            if (date.equals("")) throw new Exception();
        } catch (Exception e) {
            e.printStackTrace();
            errorMessage += errorMessage.equals("") ? "Enter a valid Date of Birth" : "\nEnter a valid Date of Birth";
        }
        if (!errorMessage.equals("")) {
            notificationPane.show(errorMessage);
            return;
        }
        Call<Void> call;
        if (user == null)
            call = apiService.createUser(username, email, firstName, lastName, nationalId, date, gender, base64Image, admin ? 1 : 0, password, phoneNo);
        else {
            username = username.equals(user.getUserName()) ? null : username;
            email = email.equals(user.getEmail()) ? null : email;
            nationalId = nationalId.equals(user.getNationalId() + "") ? null : nationalId;
            phoneNo = phoneNo.equals(user.getPhoneNo()) ? null : phoneNo;
            call = apiService.updateUser(user.getId(), username, email, firstName, lastName, nationalId, date, gender, base64Image, admin ? 1 : 0, password, phoneNo, 0);
        }
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Platform.runLater(() -> {
                    if (response.isSuccessful()) {
                        Utility.closeWindow(hbHolder);
                    } else
                        notificationPane.show(Utility.handleApiErrors(response.message(), response.errorBody(), new String[]{
                                "userName", "lastName", "firstName", "email", "nationalId", "gender", "password"
                        }));
                });
            }

            @Override
            public void onFailure(Call<Void> call, Throwable throwable) {
                Platform.runLater(() -> notificationPane.show(throwable.getMessage()));
            }
        });
    }

    public void setDataInterface(HomeDataInterface dataInterface) {
        this.dataInterface = dataInterface;
    }

    public void setUser(User user) {
        this.user = user;
        tfUsername.setText(user.getUserName());
        tfFirstName.setText(user.getFirstName());
        tfLastName.setText(user.getLastName());
        tfEmail.setText(user.getEmail());
        tfPhoneNo.setText(user.getPhoneNo());
        dpDOB.setValue(LocalDate.parse(user.getDob()));
        if (user.getGender().equals("Male")) rbMale.setSelected(true);
        else rbFemale.setSelected(true);
        cbAdmin.setSelected(user.isAdmin());
        tfNationalId.setText(String.valueOf(user.getNationalId()));
        if (user.getPhoto() != null && !user.getPhoto().equals("")) {
            base64Image = user.getPhoto();
            Utility.decodeImage(user.getPhoto(), ivPhoto, "mUser.png");
        }
        btnSave.setText("Update");
    }
}
