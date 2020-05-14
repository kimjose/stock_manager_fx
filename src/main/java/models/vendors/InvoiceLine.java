package models.vendors;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import interfaces.LinesInterface;
import javafx.scene.control.Button;
import javafx.scene.paint.Paint;
import network.ApiService;
import network.RetrofitBuilder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InvoiceLine {
    //'invId', 'type','typeId','description','unitPrice','quantity'

    private int id;
    private int invId;
    private String type;
    private int typeId;
    private String description;
    private double unitPrice;
    private int quantity;
    private LinesInterface linesInterface;
    private String name;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getInvId() {
        return invId;
    }

    public void setInvId(int invId) {
        this.invId = invId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setLinesInterface(LinesInterface linesInterface) {
        this.linesInterface = linesInterface;
    }

    public double getTotal(){
        return unitPrice * quantity;
    }

    public Button getRemoveLine(){
        Button deleteBtn = new Button("Remove");
        FontAwesomeIconView icon = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
        icon.setFill(Paint.valueOf("#DC143C"));
        icon.setSize("16.0");
        deleteBtn.setGraphic(icon);
        deleteBtn.setOnAction(event -> {
            ApiService apiService = RetrofitBuilder.createService(ApiService.class);
            Call<InvoiceLine[]> call = apiService.deleteVendorInvoiceLine(id);
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(Call<InvoiceLine[]> call, Response<InvoiceLine[]> response) {
                    if (linesInterface!=null){
                        if (response.isSuccessful()) linesInterface.updateData(response.body());
                        else linesInterface.notifyError(response.message());
                    }
                }

                @Override
                public void onFailure(Call<InvoiceLine[]> call, Throwable throwable) {
                    if (linesInterface!=null) linesInterface.notifyError(throwable.getMessage());
                }
            });
        });

        return deleteBtn;
    }
}
