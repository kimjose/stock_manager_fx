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
import models.customers.Receipt;
import models.finance.Bank;
import models.vendors.PaymentVoucher;
import models.vendors.Vendor;
import network.ApiService;
import network.RetrofitBuilder;
import org.controlsfx.control.NotificationPane;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import utils.SessionManager;
import utils.Utility;

import java.lang.reflect.Array;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class PaymentVoucherController implements Initializable {

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
    private ComboBox<Vendor> cbVendor;

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
    private PaymentVoucher paymentVoucher;
    final User user = SessionManager.INSTANCE.getUser();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        Utility.setupNotificationPane(notificationPane, vbHolder);
        apiService = RetrofitBuilder.createService(ApiService.class);
        Utility.restrictInputDec(tfAmount);

        
        cbVendor.setOnAction(event -> {
            Vendor v = cbVendor.getValue();
            labelBalance.setText(String.valueOf(v.getBalance()));
        });

        
        btnSave.setOnAction(event -> save());
        btnPost.setOnAction(event -> post());
        btnReverse.setOnAction(event -> reverse());
        btnCancel.setOnAction(event -> Utility.closeWindow(vbParent));
        Platform.runLater(()->Utility.setLogo(vbParent));
        loadData();
    }

    private void loadData() {
        Call<Bank[]> getBanks = apiService.banks();
        getBanks.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Bank[]> call, Response<Bank[]> response) {
                if (response.isSuccessful()){
                    Platform.runLater(()->{
                        cbBank.setItems(FXCollections.observableArrayList(response.body()));
                        if(paymentVoucher!=null) {
                            for (Bank b : response.body()) {
                                if (b.getId() == paymentVoucher.getBank().getId()) cbBank.getSelectionModel().select(b);
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
        Call<Vendor[]> getVendors = apiService.vendors();
        getVendors.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Vendor[]> call, Response<Vendor[]> response) {
                if (response.isSuccessful()){
                    Platform.runLater(()->{
                        cbVendor.setItems(FXCollections.observableArrayList(response.body()));
                        if (paymentVoucher != null){
                            for (Vendor v:response.body()) {
                                if (v.getId() == paymentVoucher.getVendor().getId()) cbVendor.getSelectionModel().select(v);
                            }
                        }
                    });
                }else Platform.runLater(()->notificationPane.show(response.message()));
            }

            @Override
            public void onFailure(Call<Vendor[]> call, Throwable throwable) {
                Platform.runLater(()->notificationPane.show(throwable.getMessage()));
            }
        });
    }

    private void save(){
        String no = tfNo.getText().trim();
        String amountString = tfAmount.getText().trim();
        Vendor vendor = cbVendor.getValue();
        Bank bank = cbBank.getValue();
        String extDocNo = tfExtDocNo.getText().trim();
        String date = null;
        String errorMessage = "";
        try{
            date = dbDate.getValue().toString();
        }catch (Exception e){
            errorMessage = errorMessage.concat("Select a valid date.");
        }
        //if (no.equals("")) errorMessage += errorMessage.equals("")?"Voucher no is required.":"\nVoucher no is required.";
        if (amountString.equals("")) errorMessage += errorMessage.equals("")?"Amount is required.":"\nAmount is required.";
        if (vendor == null) errorMessage += errorMessage.equals("")?"Select a valid vendor.":"\nSelect a valid vendor.";
        if (bank == null) errorMessage += errorMessage.equals("")?"Select a valid bank.":"\nSelect a valid bank.";
        else if (bank.isRequireRefNo() && extDocNo.equals("")) errorMessage += errorMessage.equals("")?"Reference number is required.":"\nReference number is required.";
        double amount = 0;
        try {
            amount = Double.parseDouble(amountString);
            if (amount<=0)throw new Exception();
        }catch (Exception e){
            errorMessage += errorMessage.equals("")?"Invalid voucher amount.":"\nInvalid voucher amount.";
        }
        if (!errorMessage.equals("")){
            notificationPane.show(errorMessage);
            return;
        }
        Call<PaymentVoucher[]> call;
        if (paymentVoucher == null) call = apiService.addPaymentVoucher(vendor.getId(), date, bank.getId(), amount, extDocNo, user.getId());
        else {
            no = paymentVoucher.getVoucherNo().equals(no)?null:no;
            call = apiService.updatePaymentVoucher(paymentVoucher.getId(),vendor.getId(), date, bank.getId(), amount, extDocNo);
        }
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<PaymentVoucher[]> call, Response<PaymentVoucher[]> response) {
                if (response.isSuccessful()){
                    Platform.runLater(()->{
                        Utility.closeWindow(vbHolder);
                        PaymentVoucher[] paymentVouchers = {};
                        List<PaymentVoucher> filtered = new ArrayList<>();
                        for (PaymentVoucher p: response.body()) {
                            if (!p.isPosted()) filtered.add(p);
                        }
                        dataInterface.updateData("The voucher has been saved", filtered.toArray(paymentVouchers));
                    });
                }else {
                    Platform.runLater(()->notificationPane.show(Utility.handleApiErrors(response.message(), response.errorBody(),
                            new String[]{"vendorId","voucherDate","no","amount","bankId","createdBy"})));
                }
            }

            @Override
            public void onFailure(Call<PaymentVoucher[]> call, Throwable throwable) {
                Platform.runLater(()->notificationPane.show(throwable.getMessage()));
            }
        });
    }
    private void post(){
        assert paymentVoucher != null;
        if (paymentVoucher == null) return;
        Call<PaymentVoucher[]> call = apiService.postPaymentVoucher(paymentVoucher.getId(), user.getId());
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<PaymentVoucher[]> call, Response<PaymentVoucher[]> response) {
                if (response.isSuccessful()) {
                    Platform.runLater(() -> {
                        Utility.closeWindow(vbHolder);
                        List<PaymentVoucher> filtered = new ArrayList<>();
                        PaymentVoucher[] paymentVouchers = {};
                        for (PaymentVoucher p: response.body()) {
                            if (!p.isPosted()) filtered.add(p);
                        }
                        dataInterface.updateData("The voucher has been posted", filtered.toArray(paymentVouchers));
                    });
                } else
                    Platform.runLater(() -> notificationPane.show(Utility.handleApiErrors(response.message(), response.errorBody(),
                            new String[]{"message"})));
            }

            @Override
            public void onFailure(Call<PaymentVoucher[]> call, Throwable throwable) {
                Platform.runLater(() -> notificationPane.show(throwable.getMessage()));
            }
        });
    }
    private void reverse(){
        if (paymentVoucher == null) throw new AssertionError();
        Call<PaymentVoucher[]> call = apiService.reversePaymentVoucher(paymentVoucher.getId(), user.getId());
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<PaymentVoucher[]> call, Response<PaymentVoucher[]> response) {
                if (response.isSuccessful()){
                    Platform.runLater(()->{
                        Utility.closeWindow(vbHolder);
                        PaymentVoucher[] paymentVouchers = {};
                        List<PaymentVoucher> filtered = new ArrayList<>();
                        for (PaymentVoucher v: response.body()) {
                            if (v.isPosted()) filtered.add(v);
                        }
                        dataInterface.updateData("The voucher has been reversed", filtered.toArray(paymentVouchers));
                    });
                }else Platform.runLater(()->notificationPane.show(Utility.handleApiErrors(response.message(), response.errorBody(),
                        new String[]{"message"})));
            }

            @Override
            public void onFailure(Call<PaymentVoucher[]> call, Throwable throwable) {
                Platform.runLater(()->notificationPane.show(throwable.getMessage()));
            }
        });
    }


    public void setDataInterface(HomeDataInterface dataInterface) {
        this.dataInterface = dataInterface;
    }

    public void setPaymentVoucher(PaymentVoucher paymentVoucher) {
        this.paymentVoucher = paymentVoucher;
        labelId.setText(String.valueOf(paymentVoucher.getId()));
        tfNo.setText(paymentVoucher.getVoucherNo());
        tfAmount.setText(String.valueOf(paymentVoucher.getAmount()));
        tfExtDocNo.setText(paymentVoucher.getExtDocNo());
        btnPost.setDisable(paymentVoucher.isPosted());
        dbDate.setValue(LocalDate.parse(paymentVoucher.getVoucherDate()));
        btnSave.setDisable(paymentVoucher.isPosted());
        if (!paymentVoucher.isPosted()) btnPost.setDisable(false);
        if (paymentVoucher.isPosted() && !paymentVoucher.isReversed()) btnReverse.setDisable(false);
    }
}
