package controllers;

import interfaces.HomeDataInterface;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import models.auth.User;
import models.customers.Customer;
import models.customers.Receipt;
import models.finance.Bank;
import network.ApiService;
import network.RetrofitBuilder;
import org.controlsfx.control.NotificationPane;
import org.controlsfx.validation.*;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import utils.SessionManager;
import utils.Utility;

import java.net.URL;
import java.time.LocalDate;
import java.util.*;

public class CustomerReceipt implements Initializable {

    @FXML
    private VBox vbParent;

    @FXML
    private VBox vbHolder;

    @FXML
    private Label labelId;

    @FXML
    private TextField tfNo;

    @FXML
    private DatePicker dpDate;

    @FXML
    private ComboBox<Customer> cbCustomer;

    @FXML
    private Label labelBalance;

    @FXML
    private TextField tfAmount;

    @FXML
    private ComboBox<Bank> cbBank;

    @FXML
    private TextField tfExtDocNo;

    @FXML
    private Button btnCancel;

    @FXML
    private Button btnSave;

    @FXML
    private Button btnPost;

    @FXML
    private Button btnReverse;

    @FXML
    private NotificationPane notificationPane;

    private ApiService apiService;
    private HomeDataInterface dataInterface;
    private Receipt receipt;
    private Bank[] banks;
    private Customer[] customers;
    private Map<String, Object> payData;
    private ValidationSupport validationSupport = new ValidationSupport();
    final User user = SessionManager.INSTANCE.getUser();


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        Utility.setupNotificationPane(notificationPane, vbHolder);
        apiService = RetrofitBuilder.createService(ApiService.class);
        Utility.restrictInputDec(tfAmount);

        Platform.runLater(()->{
            dpDate.setValue(LocalDate.now());
            Utility.setLogo(vbParent);
            validationSupport.registerValidator(tfAmount, true, Validator.createEmptyValidator("Receipt amount is required"));
            validationSupport.registerValidator(cbCustomer, true, Validator.createEmptyValidator("Select a valid customer"));
            validationSupport.registerValidator(cbBank, true, Validator.createEmptyValidator("Select a valid method of payment."));
            validationSupport.registerValidator(tfExtDocNo, false, Validator.createEmptyValidator("Enter ref No", Severity.WARNING));
        });
        cbCustomer.setOnAction(event -> {
            Customer c = cbCustomer.getValue();
            labelBalance.setText(String.valueOf(c.getBalance()));
        });

        loadData();
        btnSave.setOnAction(event -> save(0));
        btnPost.setOnAction(event -> post());
        btnReverse.setOnAction(event -> reverse());
        btnCancel.setOnAction(event -> Utility.closeWindow(vbParent));

        vbParent.addEventFilter(KeyEvent.KEY_PRESSED, e ->{
            KeyCode keyCode = e.getCode();
            if (keyCode.equals(KeyCode.F11)) save(0);
            else if (keyCode.equals(KeyCode.F10)) {
                if (receipt == null ) return;
                if (receipt.isPosted()) return;
                post();
            }
            else if (keyCode.equals(KeyCode.F8)) {
                if (receipt == null ) return;
                if (!receipt.isPosted()) return;
                if (receipt.isReversed()) return;
                reverse();
            }
            else if (keyCode.equals(KeyCode.F9)) Utility.closeWindow(vbHolder);
            else if (keyCode.equals(KeyCode.F5)) loadData();
        });

    }

    private void loadData(){
        Call<Bank[]> getBanks = apiService.banks();
        getBanks.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Bank[]> call, Response<Bank[]> response) {
                if (response.isSuccessful()){
                    Platform.runLater(()->{
                        cbBank.setItems(FXCollections.observableArrayList(response.body()));
                        if(receipt!=null) {
                            for (Bank b : response.body()) {
                                if (b.getId() == receipt.getBank().getId()) cbBank.getSelectionModel().select(b);
                            }
                        }
                    });
                } else Platform.runLater(() ->notificationPane.show(response.message()));
            }

            @Override
            public void onFailure(Call<Bank[]> call, Throwable throwable) {
                Platform.runLater(() ->notificationPane.show(throwable.getMessage()));
            }
        });
        Call<Customer[]> getCustomers = apiService.customers();
        getCustomers.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Customer[]> call, Response<Customer[]> response) {
                if (response.isSuccessful()){
                    Platform.runLater(()->{
                        cbCustomer.setItems(FXCollections.observableArrayList(response.body()));
                        if(receipt!=null) {
                            for (Customer b : response.body()) {
                                if (b.getId() == receipt.getCustomer().getId()) {
                                    cbCustomer.getSelectionModel().select(b);
                                }
                            }
                        } else if(payData != null){
                            int customer = (int) payData.get("customerId");
                            for (Customer b : response.body()) {
                                if (b.getId() == customer) {
                                    cbCustomer.getSelectionModel().select(b);
                                }
                            }
                        }
                    });
                } else Platform.runLater(() ->notificationPane.show(response.message()));
            }

            @Override
            public void onFailure(Call<Customer[]> call, Throwable throwable) {
                Platform.runLater(() ->notificationPane.show(throwable.getMessage()));
            }
        });
    }

    /**
     * @param post (1 the receipt will be posted )
     * **/
    private void save(int post){
        ValidationResult vr = validationSupport.getValidationResult();
        Iterator<ValidationMessage> iterator = vr.getErrors().iterator();
        StringBuilder message = new StringBuilder();
        while (iterator.hasNext()){
            message.append(iterator.next().getText());
            message.append("\n");
        }
        if (!vr.getErrors().isEmpty()){
            notificationPane.show(message.toString());
            return;
        }
        String amountString = tfAmount.getText().trim();
        Customer customer = cbCustomer.getValue();
        Bank bank = cbBank.getValue();
        String extDocNo = tfExtDocNo.getText().trim();
        String date = null;
        String errorMessage = "";
        try{
            date = dpDate.getValue().toString();
        }catch (Exception e){
            errorMessage = errorMessage.concat("Select a valid date.");
        }
        //if (no.equals("")) errorMessage += errorMessage.equals("")?"Receipt no is required.":"\nReceipt no is required.";
        if (amountString.equals("")) errorMessage += errorMessage.equals("")?"Amount is required.":"\nAmount is required.";
        if (customer == null) errorMessage += errorMessage.equals("")?"Select a valid customer.":"\nSelect a valid customer.";
        if (bank == null) errorMessage += errorMessage.equals("")?"Select a valid bank.":"\nSelect a valid bank.";
        else {
            if (bank.isRequireRefNo() && extDocNo.equals(""))
                errorMessage += errorMessage.equals("")?"Reference number is required for this mode of payment.":"\nReference number is required for this mode of payment.";
        }
        double amount = 0;
        try {
            amount = Double.parseDouble(amountString);
            if (amount<=0)throw new Exception();
        }catch (Exception e){
            errorMessage += errorMessage.equals("")?"Invalid receipt amount.":"\nInvalid receipt amount.";
        }
        if (!errorMessage.equals("")){
            notificationPane.show(errorMessage);
            return;
        }
        Call<Receipt> call;
        if (receipt == null) call = apiService.addReceipt(customer.getId(), date, user.getId(), bank.getId(), amount, extDocNo, post);
        else {
            call = apiService.updateReceipt(receipt.getId(), customer.getId(), date, bank.getId(), amount, extDocNo);
        }
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Receipt> call, Response<Receipt> response) {
                if (response.isSuccessful()){
                    Platform.runLater(()->{
                        notificationPane.show("The receipt has been saved.");
                        setReceipt(response.body());
                    });
                }else {
                    Platform.runLater(()->notificationPane.show(Utility.handleApiErrors(response.message(), response.errorBody(),
                            new String[]{"receiptId","receiptDate","voucherNo","amount","bankId","createdBy"})));
                }
            }

            @Override
            public void onFailure(Call<Receipt> call, Throwable throwable) {
                System.out.println(throwable.toString());
                Platform.runLater(()->notificationPane.show(throwable.getMessage()));
            }
        });

    }
    private void post(){
        if (receipt == null) {
            save(1);
            return;
        }
        Call<Receipt> call = apiService.postCustomerReceipt(receipt.getId(), user.getId());
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Receipt> call, Response<Receipt> response) {
                if (response.isSuccessful()){
                    Platform.runLater(()->{
                        setReceipt(response.body());
                        notificationPane.show("The receipt has been posted");
                    });
                }else Platform.runLater(()->notificationPane.show(Utility.handleApiErrors(response.message(), response.errorBody(),
                        new String[]{"message"})));
            }

            @Override
            public void onFailure(Call<Receipt> call, Throwable throwable) {
                Platform.runLater(()->notificationPane.show(throwable.getMessage()));
            }
        });
    }
    private void reverse(){
        if (receipt == null) throw new AssertionError();
        Call<Receipt> call = apiService.reverseCustomerReceipt(receipt.getId(), user.getId());
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Receipt> call, Response<Receipt> response) {
                if (response.isSuccessful()){
                    Platform.runLater(()->{
                        setReceipt(response.body());
                        notificationPane.show("The receipt has been reversed.");
                    });
                }else Platform.runLater(()->notificationPane.show(Utility.handleApiErrors(response.message(), response.errorBody(),
                        new String[]{"message"})));
            }

            @Override
            public void onFailure(Call<Receipt> call, Throwable throwable) {
                Platform.runLater(()->notificationPane.show(throwable.getMessage()));
            }
        });
    }


    public void setDataInterface(HomeDataInterface dataInterface) {
        this.dataInterface = dataInterface;
    }

    public void setPayData(Map<String, Object> payData) {
        this.payData = payData;
        tfAmount.setText(String.valueOf(payData.get("amount")));
    }

    public void setReceipt(Receipt receipt) {
        this.receipt = receipt;
        labelId.setText(String.valueOf(receipt.getId()));
        tfNo.setText(receipt.getNo());
        tfAmount.setText(String.valueOf(receipt.getAmount()));
        tfExtDocNo.setText(receipt.getExtDocNo());
        btnPost.setDisable(receipt.isPosted());
        btnSave.setDisable(receipt.isPosted());
        dpDate.setValue(LocalDate.parse(receipt.getReceiptDate()));
        if (!receipt.isPosted()) {
            btnPost.setDisable(false);
        }
        if (receipt.isPosted() && !receipt.isReversed()) btnReverse.setDisable(false);
    }
}
