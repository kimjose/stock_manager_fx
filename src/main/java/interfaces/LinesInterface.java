package interfaces;

import models.SuperModel;

public interface LinesInterface {
    void updateData(Object[] lines);
    void notifyError(String errorMessage);
    void updateLine(Object line);
    void addItem(SuperModel item, String type);
}
