package models.products;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import interfaces.LinesInterface;
import javafx.scene.control.Button;
import models.customers.InvoiceLine;
import network.ApiService;
import network.RetrofitBuilder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExpressSaleLine {
    //'type', 'typeId', 'unitPrice', 'quantity'
    private String type, name;
    private int quantity, typeId;
    private double unitPrice;
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

    public Button getRemoveLine(){
        Button deleteBtn = new Button("Remove");
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
                    if (linesInterface!=null){
                        if (response.isSuccessful()) linesInterface.updateData(response.body());
                        else linesInterface.notifyError(response.message());
                    }
                }

                @Override
                public void onFailure(Call<ExpressSaleLine[]> call, Throwable throwable) {
                    if (linesInterface!=null) linesInterface.notifyError(throwable.getMessage());
                }
            });
        });

        return deleteBtn;
    }
    public double getTotal(){
        return unitPrice * quantity;
    }

    public void setLinesInterface(LinesInterface linesInterface) {
        this.linesInterface = linesInterface;
    }
}
