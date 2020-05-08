package controllers;

import interfaces.HomeDataInterface;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import models.auth.User;
import models.finance.Bank;
import network.ApiService;
import network.RetrofitBuilder;
import org.controlsfx.control.NotificationPane;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import utils.SessionManager;
import utils.Utility;

import java.net.URL;
import java.util.ResourceBundle;

public class BankController implements Initializable {

    @FXML
    private VBox vbParent;

    @FXML
    private NotificationPane notificationPane;

    @FXML
    private VBox vbHolder;

    @FXML
    private Label labelId;

    @FXML
    private TextField tfName;

    @FXML
    private TextField tfBranch;

    @FXML
    private TextField tfAccNo;

    @FXML
    private CheckBox cbRefNo;

    @FXML
    private Button btnCancel;

    @FXML
    private Button btnSave;

    @FXML
    private Button btnBlock;

    @FXML
    private Button btnUnblock;

    private final ApiService apiService = RetrofitBuilder.createService(ApiService.class);
    private HomeDataInterface dataInterface;
    private Bank bank;
    private final User user = SessionManager.INSTANCE.getUser();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Utility.setupNotificationPane(notificationPane, vbHolder);
        Utility.restrictInputNum(tfAccNo);

        btnCancel.setOnAction(event -> Utility.closeWindow(vbHolder));
        btnSave.setOnAction(event -> save());
        btnBlock.setOnAction(event -> block(true));
        btnUnblock.setOnAction(event -> block(false));
    }

    private void save(){
        String name = tfName.getText().trim();
        String branch = tfBranch.getText().trim();
        String accNo = tfAccNo.getText().trim();
        boolean refNo = cbRefNo.isSelected();
        if (name.equals("")) {
            notificationPane.show("Bank account name is required.");
            return;
        }
        Call<Bank[]> call;
        if (bank == null) call = apiService.addBank(name, branch, accNo, refNo?1:0, user.getId());
        else {
            name = name.equals(bank.getName())?null:name;
            call = apiService.updateBank(bank.getId(), name, branch, accNo, refNo?1:0, bank.isEnabled()?1:0);
        }
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Bank[]> call, Response<Bank[]> response) {
                Platform.runLater(()->{
                    if (response.isSuccessful()){
                        if (dataInterface != null ){
                            Utility.closeWindow(vbHolder);
                            dataInterface.updateData("Bank acount has been saved.", response.body());
                        }
                    }else notificationPane.show(Utility.handleApiErrors(response.message(), response.errorBody(),
                            new String[]{"name", "branch", "accountNo", "addedBy"}));
                });
            }

            @Override
            public void onFailure(Call<Bank[]> call, Throwable throwable) {
                Platform.runLater(()->notificationPane.show(throwable.getMessage()));
            }
        });
    }
    private void block(boolean block){
        if (bank == null ) return;
        Call<Bank[]> call = apiService.updateBank(bank.getId(), null, bank.getBranch(), bank.getAccountNo(), bank.isRequireRefNo()?1:0, !block?1:0);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Bank[]> call, Response<Bank[]> response) {
                Platform.runLater(()->{
                    if (response.isSuccessful()){
                        if (dataInterface != null ){
                            Utility.closeWindow(vbHolder);
                            dataInterface.updateData("Bank acount has been saved.", response.body());
                        }
                    }else notificationPane.show(Utility.handleApiErrors(response.message(), response.errorBody(),
                            new String[]{"name", "branch", "accountNo", "addedBy"}));
                });
            }

            @Override
            public void onFailure(Call<Bank[]> call, Throwable throwable) {
                Platform.runLater(()->notificationPane.show(throwable.getMessage()));
            }
        });
    }

    public void setDataInterface(HomeDataInterface dataInterface) {
        this.dataInterface = dataInterface;
    }

    public void setBank(Bank bank) {
        this.bank = bank;
        tfName.setText(bank.getName());
        tfBranch.setText(bank.getBranch());
        tfAccNo.setText(bank.getAccountNo());
        cbRefNo.setSelected(bank.isRequireRefNo());
        labelId.setText(String.valueOf(bank.getId()));
        btnBlock.setDisable(!bank.isEnabled());
        btnUnblock.setDisable(bank.isEnabled());
    }
}
