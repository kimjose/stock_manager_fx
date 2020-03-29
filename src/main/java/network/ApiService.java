package network;

import models.customers.Customer;
import models.customers.Invoice;
import models.products.*;
import models.vendors.Vendor;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {
    /**
     * GET requests
     * ***/
    @GET("brand")
    Call<Brand[]> brands();

    @GET("product")
    Call<Product[]> products();

    @GET("category")
    Call<Category[]> categories();

    @GET("uom")
    Call<UnitOfMeasure[]> unitsOfMeasure();

    @GET("warehouse")
    Call<Warehouse[]> warehouses();

    @GET("customer")
    Call<Customer[]> customers();

    @GET("vendor")
    Call<Vendor[]> vendors();

    @GET("customer_invoice")
    Call<Invoice[]> customerInvoices();

    @GET("vendor_invoice")
    Call<models.vendors.Invoice> vendorInvoices();




    /**
     * POST requests
     * ***/



    /**
     * DELETE requests
     * ***/
}
