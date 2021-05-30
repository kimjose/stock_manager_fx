package interfaces;

import models.products.Product;

public interface HomeDataInterface {
    void updateData(String message, Object[] data);
    void sellProduct(Product product);
}
