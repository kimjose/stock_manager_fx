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
import javafx.scene.layout.VBox;
import models.auth.User;
import models.customers.Customer;
import models.customers.Receipt;
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
import java.time.LocalDate;
import java.util.ResourceBundle;

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
    private DatePicker dbDate;

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
    final User user = SessionManager.INSTANCE.getUser();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Utility.setupNotificationPane(notificationPane, vbHolder);
        apiService = RetrofitBuilder.createService(ApiService.class);
        Utility.restrictInputDec(tfAmount);


        cbCustomer.setOnAction(event -> {
            Customer c = cbCustomer.getValue();
            labelBalance.setText(String.valueOf(c.getBalance()));
        });

        loadData();
        btnSave.setOnAction(event -> save());
        btnPost.setOnAction(event -> post());
        btnReverse.setOnAction(event -> reverse());
        btnCancel.setOnAction(event -> Utility.closeWindow(vbParent));

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
                } else notificationPane.show(response.message());
            }

            @Override
            public void onFailure(Call<Bank[]> call, Throwable throwable) {
                notificationPane.show(throwable.getMessage());
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
                        }
                    });
                } else notificationPane.show(response.message());
            }

            @Override
            public void onFailure(Call<Customer[]> call, Throwable throwable) {
                notificationPane.show(throwable.getMessage());
            }
        });
    }

    private void save(){
        String no = tfNo.getText().trim();
        String amountString = tfAmount.getText().trim();
        Customer customer = cbCustomer.getValue();
        Bank bank = cbBank.getValue();
        String extDocNo = tfExtDocNo.getText().trim();
        String date = null;
        String errorMessage = "";
        try{
            date = dbDate.getValue().toString();
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
        Call<Receipt[]> call;
        if (receipt == null) call = apiService.addReceipt(customer.getId(), date, user.getId(), bank.getId(), amount, extDocNo);
        else {
            no = receipt.getNo().equals(no)?null:no;
            call = apiService.updateReceipt(receipt.getId(), customer.getId(), date, bank.getId(), amount, extDocNo);
        }
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Receipt[]> call, Response<Receipt[]> response) {
                if (response.isSuccessful()){
                    Platform.runLater(()->{
                        Utility.closeWindow(vbHolder);
                        dataInterface.updateData("The receipt has been saved", response.body());
                    });
                }else {
                    Platform.runLater(()->notificationPane.show(Utility.handleApiErrors(response.message(), response.errorBody(),
                            new String[]{"receiptId","receiptDate","voucherNo","amount","bankId","createdBy"})));
                }
            }

            @Override
            public void onFailure(Call<Receipt[]> call, Throwable throwable) {
                System.out.println(throwable.toString());
                Platform.runLater(()->notificationPane.show(throwable.getMessage()));
            }
        });

    }
    private void post(){
        if (receipt == null) throw new AssertionError();
        Call<Receipt[]> call = apiService.postCustomerReceipt(receipt.getId(), user.getId());
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Receipt[]> call, Response<Receipt[]> response) {
                if (response.isSuccessful()){
                    Platform.runLater(()->{
                        Utility.closeWindow(vbHolder);
                        dataInterface.updateData("The receipt has been posted", response.body());
                    });
                }else Platform.runLater(()->notificationPane.show(Utility.handleApiErrors(response.message(), response.errorBody(),
                        new String[]{"message"})));
            }

            @Override
            public void onFailure(Call<Receipt[]> call, Throwable throwable) {
                Platform.runLater(()->notificationPane.show(throwable.getMessage()));
            }
        });
    }
    private void reverse(){
        if (receipt == null) throw new AssertionError();
        Call<Receipt[]> call = apiService.reverseCustomerReceipt(receipt.getId(), user.getId());
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Receipt[]> call, Response<Receipt[]> response) {
                if (response.isSuccessful()){
                    Platform.runLater(()->{
                        Utility.closeWindow(vbHolder);
                        dataInterface.updateData("The receipt has been reversed", response.body());
                    });
                }else Platform.runLater(()->notificationPane.show(Utility.handleApiErrors(response.message(), response.errorBody(),
                        new String[]{"message"})));
            }

            @Override
            public void onFailure(Call<Receipt[]> call, Throwable throwable) {
                Platform.runLater(()->notificationPane.show(throwable.getMessage()));
            }
        });
    }


    public void setDataInterface(HomeDataInterface dataInterface) {
        this.dataInterface = dataInterface;
    }

    public void setReceipt(Receipt receipt) {
        this.receipt = receipt;
        labelId.setText(String.valueOf(receipt.getId()));
        tfNo.setText(receipt.getNo());
        tfAmount.setText(String.valueOf(receipt.getAmount()));
        tfExtDocNo.setText(receipt.getExtDocNo());
        btnPost.setDisable(receipt.isPosted());
        dbDate.setValue(LocalDate.parse(receipt.getReceiptDate()));
        if (!receipt.isPosted()) {
            btnPost.setDisable(false);
            btnSave.setDisable(true);
        }
        if (receipt.isPosted() && !receipt.isReversed()) btnReverse.setDisable(false);
    }
}
