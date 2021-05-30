package models.products;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import interfaces.LinesInterface;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import network.ApiService;
import network.RetrofitBuilder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import utils.Utility;

public class ExpressSaleLine {
    //'type', 'typeId', 'unitPrice', 'quantity'
    private String type, name;
    private int quantity, typeId;
    private double unitPrice;
    private double buyingPrice;
    private int id;
    private LinesInterface linesInterface;

    public int getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public double getBuyingPrice() {
        return buyingPrice;
    }

    public void setBuyingPrice(double buyingPrice) {
        this.buyingPrice = buyingPrice;
    }

    public TextField getQuantityTf() {
        TextField textField = new TextField(String.valueOf(quantity));
        textField.setAlignment(Pos.BASELINE_CENTER);
        Utility.restrictInputNum(textField);
        textField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                try {
                    String q = textField.getText().trim();
                    int newQ = Integer.parseInt(q);
                    int diff = newQ - quantity;
                    ExpressSaleLine newLine = this;
                    newLine.setQuantity(diff);
                    if (linesInterface != null) linesInterface.updateLine(newLine);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        return textField;
    }

    public TextField getUnitPriceTf() {
        TextField textField = new TextField(String.valueOf(unitPrice));
        textField.setAlignment(Pos.BASELINE_CENTER);
        Utility.restrictInputDec(textField);
        textField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue){
                try{
                    String q = textField.getText().trim();
                    double nPrice = Double.parseDouble(q);
                    if (nPrice <= 0) {
                        textField.setText(String.valueOf(unitPrice));
                        return;
                    }
                    ExpressSaleLine newLine = this;
                    newLine.setUnitPrice(nPrice);
                    newLine.setQuantity(0);
                    if (linesInterface != null) linesInterface.updateLine(newLine);
                }catch(Exception e){ e.printStackTrace();}
            }
        });
        return textField;
    }

    public Button getRemoveLine() {
        Button deleteBtn = new Button();
        FontAwesomeIconView icon = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
        icon.setStyle("-fx-fill: #d32f2f;");
        icon.setSize("16.0");
        deleteBtn.setGraphic(icon);
        deleteBtn.setOnAction(event -> {
            ApiService apiService = RetrofitBuilder.createService(ApiService.class);
            Call<ExpressSaleLine[]> call = apiService.deleteSaleLine(id);
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(Call<ExpressSaleLine[]> call, Response<ExpressSaleLine[]> response) {
                    if (linesInterface != null) {
                        if (response.isSuccessful()) linesInterface.updateData(response.body());
                        else linesInterface.notifyError(response.message());
                    }
                }

                @Override
                public void onFailure(Call<ExpressSaleLine[]> call, Throwable throwable) {
                    if (linesInterface != null) linesInterface.notifyError(throwable.getMessage());
                }
            });
        });

        return deleteBtn;
    }

    public double getTotal() {
        return unitPrice * quantity;
    }

    public void setLinesInterface(LinesInterface linesInterface) {
        this.linesInterface = linesInterface;
    }
}
