package models.products;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import interfaces.LinesInterface;
import javafx.scene.control.Button;
import javafx.scene.paint.Paint;
import models.SuperModel;

public class Service implements SuperModel {
    private int id;
    private String name, description;
    private LinesInterface linesInterface;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Button getAddBtn(){
        Button addBtn = new Button();
        FontAwesomeIconView icon = new FontAwesomeIconView(FontAwesomeIcon.PLUS_CIRCLE);
        icon.setFill(Paint.valueOf("#19D019"));
        icon.setSize("16.0");
        addBtn.setGraphic(icon);
        addBtn.setStyle("-fx-background-color: transparent;");
        addBtn.setOnAction(event -> {
            if(linesInterface != null){
                linesInterface.addItem(this, "Service");
            }
        });
        return addBtn;
    }

    public void setLinesInterface(LinesInterface linesInterface) {
        this.linesInterface = linesInterface;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public String getSearchString() {
        return name;
    }
}
