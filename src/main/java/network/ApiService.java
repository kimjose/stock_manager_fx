package network;

import models.ApiResponse;
import models.customers.Customer;
import models.customers.Invoice;
import models.customers.InvoiceLine;
import models.products.*;
import models.vendors.Vendor;
import retrofit2.Call;
import retrofit2.http.*;

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
    Call<models.vendors.Invoice[]> vendorInvoices();

    @GET("products_pack")
    Call<ProductsPack[]> ProductsPacks();

    @GET("service")
    Call<Service[]> services();





    /**
     * POST requests
     * ***/
    @POST("brand")
    @FormUrlEncoded
    Call<Brand[]> addBrand(@Field("name") String name);

    @POST("category")
    @FormUrlEncoded
    Call<Category[]> addCategory(@Field("name") String name);

    @POST("warehouse")
    @FormUrlEncoded
    Call<Warehouse[]> addWarehouse(@Field("name") String name, @Field("location") String location);

    @POST("uom")
    @FormUrlEncoded
    Call<UnitOfMeasure[]> addUOM(@Field("name") String name, @Field("description") String description);

    @POST("product")
    @FormUrlEncoded
    Call<Product[]> addProduct(@Field("name")String name, @Field("description")String description,
                             @Field("price")double price, @Field("brandId")int brandId, @Field("uomId")int uomId,
                             @Field("categoryId")int categoryId, @Field("sku_code")String skuCode, @Field("upc_code")String upcCode,
                             @Field("image")String image);

    @POST("customer")
    @FormUrlEncoded
    Call<Customer[]> addCustomer(@Field("name") String name, @Field("email") String email,
                                 @Field("phone")String phone, @Field("addedBy") int addedBy);

    @POST("vendor")
    @FormUrlEncoded
    Call<Vendor[]> addVendor(@Field("name") String name, @Field("email") String email,
                                 @Field("phone")String phone, @Field("addedBy") int addedBy);

    @POST("service")
    @FormUrlEncoded
    Call<Service[]> addService(@Field("name") String name, @Field("description") String description);

    @POST("customer_invoice")
    @FormUrlEncoded
    Call<Invoice[]> addCustomerInvoice(@Field("invoiceNo") String invoiceNo, @Field("customerId") int customer,
                                       @Field("warehouseId") int warehouse,@Field("invoiceDate")String date,@Field("addedBy") int addedBy);

    @POST("customer_invoice_line")
    @FormUrlEncoded
    Call<InvoiceLine[]> addCustomerInvoiceLine(@Field("invId")int invId, @Field("type")String type, @Field("typeId")int typeId,
                                               @Field("unitPrice")double unitPrice,@Field("quantity")int quantity,@Field("description")String description);
    //'invId', 'type','typeId','description','unitPrice','quantity'
    @POST("vendor_invoice_line")
    @FormUrlEncoded
    Call<models.vendors.InvoiceLine[]> addVendorInvoiceLine(@Field("invId")int invId, @Field("type")String type, @Field("typeId")int typeId,
                                                            @Field("unitPrice")double unitPrice,@Field("quantity")int quantity,@Field("description")String description);


    @POST("vendor_invoice")
    @FormUrlEncoded
    Call<models.vendors.Invoice[]> addVendorInvoice(@Field("invoiceNo") String invoiceNo, @Field("vendorId") int vendor,
                                       @Field("warehouseId") int warehouse,@Field("invoiceDate")String date, @Field("addedBy") int addedBy);

    /***
     * PUT requests
     * ***/

    @PUT("brand/{id}")
    Call<Brand[]> updateBrand(@Path("id")int id, @Query("name") String name);

    @PUT("warehouse/{id}")
    Call<Warehouse[]> updateWarehouse(@Path("id")int id, @Query("name") String name, @Query("location") String location);

    @PUT("category/{id}")
    Call<Category[]> updateCategory(@Path("id") int id, @Query("name") String name);

    @PUT("uom/{id}")
    Call<UnitOfMeasure[]> updateUOM(@Path("id")int id, @Query("name") String name, @Query("description") String description);

    @PUT("customer/{id}")
    Call<Customer[]> updateCustomer(@Path("id") int id, @Query("name") String name, @Query("email") String email, @Query("phone")String phone);

    @PUT("vendor/{id}")
    Call<Vendor[]> updateVendor(@Path("id") int id, @Query("name") String name, @Query("email") String email, @Query("phone")String phone);

    @PUT("service/{id}")
    Call<Service[]> updateService(@Path("id")int id, @Query("name") String name, @Query("description") String description);

    @PUT("customer_invoice/{id}")
    Call<Invoice[]> updateCustomerInvoice(@Path("id")int id,@Query("invoiceNo") String invoiceNo, @Query("customerId") int customer,
                                          @Query("warehouseId") int warehouse,@Query("invoiceDate")String date);

    @PUT("vendor_invoice/{id}")
    Call<models.vendors.Invoice[]> updateVendorInvoice(@Path("id")int id,@Query("invoiceNo") String invoiceNo, @Query("vendorId") int vendor,
                                          @Query("warehouseId") int warehouse,@Query("invoiceDate")String date);

    /**
     * DELETE requests
     * ***/
    @DELETE("brand/{id}")
    Call<Brand[]> deleteBrand(@Path("id")int id);

    @DELETE("warehouse/{id}")
    Call<Warehouse[]> deleteWarehouse(@Path("id") int id);

    @DELETE("category/{id}")
    Call<Category[]> deleteCategory(@Path("id") int id);

    @DELETE("uom/{id}")
    Call<UnitOfMeasure[]> deleteUOM(@Path("id") int id);

    @DELETE("customer/{id}")
    Call<Customer[]> deleteCustomer(@Path("id") int id);

    @DELETE("vendor/{id}")
    Call<Vendor[]> deleteVendor(@Path("id") int id);

    @DELETE("service/{id}")
    Call<Service[]> deleteService(@Path("id") int id);

    @DELETE("customer_invoice/{id}")
    Call<Invoice[]> deleteCustomerInvoice(@Path("id") int id);

    @DELETE("vendor_invoice/{id}")
    Call<models.vendors.Invoice[]> deleteVendorInvoice(@Path("id") int id);

    @DELETE("customer_invoice_line/{id}")
    Call<InvoiceLine[]> deleteCustomerInvoiceLine(@Path("id")int id);

    @DELETE("vendor_invoice_line/{id}")
    Call<models.vendors.InvoiceLine[]> deleteVendorInvoiceLine(@Path("id")int id);

}
