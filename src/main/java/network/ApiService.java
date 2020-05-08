package network;

import models.auth.User;
import models.customers.Customer;
import models.customers.Invoice;
import models.customers.InvoiceLine;
import models.customers.Receipt;
import models.finance.Bank;
import models.products.*;
import models.vendors.*;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {
    @POST("user/login ")
    @FormUrlEncoded
    Call<User> login(@Field("userName") String username, @Field("password")String password, @Field("password_confirmation")String passwordConfirm);




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

    @GET("receipt")
    Call<Receipt[]> receipts();

    @GET("payment_voucher")
    Call<PaymentVoucher[]> paymentVouchers();

    @GET("bank")
    Call<Bank[]> banks();

    @GET("express_sale")
    Call<ExpressSale[]> expressSales();

    @GET("product_group")
    Call<ProductGroup[]> productGroups();

    @GET("unpacking")
    Call<Unpacking[]> unpackings();



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

    @POST("bank")
    @FormUrlEncoded
    Call<Bank[]> addBank(@Field("name") String name, @Field("branch") String branch, @Field("accountNo") String accountNo,
                         @Field("requireRefNo") int requireRefNo, @Field("addedBy") int addedBy);

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
    Call<Invoice[]> addCustomerInvoice(@Field("customerId") int customer,
                                       @Field("warehouseId") int warehouse,@Field("invoiceDate")String date,@Field("createdBy") int createdBy);

    @POST("post_customer_invoice/{id}/{postedBy}")
    Call<Invoice[]> postCustomerInvoice(@Path("id") int id, @Path("postedBy") int postedBy);

    @POST("reverse_customer_invoice/{id}/{postedBy}")
    Call<Invoice[]> reverseCustomerInvoice(@Path("id") int id, @Path("postedBy") int postedBy);

    @POST("post_customer_receipt/{id}/{postedBy}")
    Call<Receipt[]> postCustomerReceipt(@Path("id") int id, @Path("postedBy") int postedBy);

    @POST("reverse_customer_receipt/{id}/{postedBy}")
    Call<Receipt[]> reverseCustomerReceipt(@Path("id") int id, @Path("postedBy") int postedBy);

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
    Call<models.vendors.Invoice[]> addVendorInvoice(@Field("vendorId") int vendor,
                                       @Field("warehouseId") int warehouse,@Field("invoiceDate")String date, @Field("addedBy") int addedBy);

    @POST("post_vendor_invoice/{id}/{postedBy}")
    Call<models.vendors.Invoice[]> postVendorInvoice(@Path("id") int id, @Path("postedBy") int postedBy);

    @POST("reverse_vendor_invoice/{id}/{postedBy}")
    Call<models.vendors.Invoice[]> reverseVendorInvoice(@Path("id") int id, @Path("postedBy") int postedBy);

    @POST("post_vendor_voucher/{id}/{postedBy}")
    Call<PaymentVoucher[]> postPaymentVoucher(@Path("id") int id, @Path("postedBy") int postedBy);

    @POST("reverse_vendor_voucher/{id}/{postedBy}")
    Call<PaymentVoucher[]> reversePaymentVoucher(@Path("id") int id, @Path("postedBy") int postedBy);

    @POST("payment_voucher")
    @FormUrlEncoded
    Call<PaymentVoucher[]> addPaymentVoucher(@Field("vendorId")int vendorId ,@Field("voucherDate") String voucherDate,
                                             @Field("bankId")int bankId , @Field("amount") double amount ,@Field("extDocNo")String extDocNo, @Field("createdBy") int id);

    @POST("receipt")
    @FormUrlEncoded
    Call<Receipt[]> addReceipt(@Field("customerId")int customerId, @Field("receiptDate")String receiptDate,
                               @Field("createdBy")int createdBy,@Field("bankId")int bankId,@Field("amount")double amount,
                               @Field("extDocNo")String extDocNo);

    @POST("express_sale")
    @FormUrlEncoded
    Call<ExpressSale[]> addSale(@Field("description") String description, @Field("saleDate") String saleDate,
                                @Field("bankId") int bankId, @Field("warehouseId") int warehouseId, @Field("refNo") String refNo,
                                @Field("createdBy") int createdBy);

    @POST("post_sale/{id}/{postedBy}")
    Call<ExpressSale[]> postSale(@Path("id") int id, @Path("postedBy") int postedBy);

    @POST("reverse_sale/{id}/{postedBy}")
    Call<ExpressSale[]> reverseSale(@Path("id") int id, @Path("postedBy") int postedBy);


    @POST("add_sale_line/{id}")
    @FormUrlEncoded
    Call<ExpressSaleLine[]> addSaleLine(@Path("id") int id, @Field("type") String type, @Field("typeId") int typeId,
                                        @Field("unitPrice") double unitPrice, @Field("quantity") int quantity);

    @POST("product_group")
    @FormUrlEncoded
    Call<ProductGroup[]> addGroup(@Field("name") String name, @Field("description") String description, @Field("productId") int product,
                                  @Field("quantity") int quantity,@Field("price") double price);

    @POST("unpacking")
    @FormUrlEncoded
    Call<Unpacking[]> addUnpacking(@Field("groupId") int groupId, @Field("quantity") int quantity, @Field("productQuantity") int productQuantity,
                                   @Field("warehouseId") int warehouseId, @Field("createdBy") int createdBy);

    @POST("post_unpacking/{id}/{postedBy}")
    Call<Unpacking[]> postUnpacking(@Path("id")int id, @Path("postedBy") int postedBy);

    @POST("reverse_unpacking/{id}/{postedBy}")
    Call<Unpacking[]> reverseUnpacking(@Path("id")int id, @Path("postedBy") int postedBy);


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

    @PUT("bank/{id}")
    Call<Bank[]> updateBank(@Path("id") int id, @Query("name") String name, @Query("branch") String branch, @Query("accountNo") String accountNo,
                         @Query("requireRefNo") int requireRefNo, @Query("enabled") int enabled);

    @PUT("customer/{id}")
    Call<Customer[]> updateCustomer(@Path("id") int id, @Query("name") String name, @Query("email") String email, @Query("phone")String phone);

    @PUT("vendor/{id}")
    Call<Vendor[]> updateVendor(@Path("id") int id, @Query("name") String name, @Query("email") String email, @Query("phone")String phone);

    @PUT("service/{id}")
    Call<Service[]> updateService(@Path("id")int id, @Query("name") String name, @Query("description") String description);

    @PUT("customer_invoice/{id}")
    Call<Invoice[]> updateCustomerInvoice(@Path("id")int id,@Query("customerId") int customer,
                                          @Query("warehouseId") int warehouse,@Query("invoiceDate")String date);

    @PUT("vendor_invoice/{id}")
    Call<models.vendors.Invoice[]> updateVendorInvoice(@Path("id")int id,@Query("vendorId") int vendor,
                                          @Query("warehouseId") int warehouse,@Query("invoiceDate")String date);


    @PUT("payment_voucher/{id}")
    Call<PaymentVoucher[]> updatePaymentVoucher(@Path("id")int id, @Query("vendorId")int vendorId ,@Query("voucherDate") String voucherDate,
                                             @Query("bankId")int bankId , @Query("amount") double amount ,@Query("extDocNo")String extDocNo);

    @PUT("receipt/{id}")
    Call<Receipt[]> updateReceipt(@Path("id")int id, @Query("customerId")int customerId, @Query("receiptDate")String receiptDate,
                               @Query("bankId")int bankId,@Query("amount")double amount, @Query("extDocNo")String extDocNo);

    @PUT("express_sale/{id}")
    Call<ExpressSale[]> updateSale(@Path("id") int id, @Query("description") String description, @Query("saleDate") String saleDate,
                                   @Query("bankId") int bankId, @Query("warehouseId") int warehouseId, @Query("refNo") String refNo);

    @POST("product_group/{id}")
    Call<ProductGroup[]> updateGroup(@Path("id") int id, @Field("name") String name, @Field("description") String description, @Field("productId") int product,
                                  @Field("quantity") int quantity,@Field("price") double price);

    @PUT("unpacking/{id}")
    Call<Unpacking[]> updateUnpacking(@Path("id") int id, @Query("groupId") int groupId, @Query("quantity") int quantity,
                                      @Query("productQuantity") int productQuantity,@Query("warehouseId") int warehouseId);
    
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

    @DELETE("receipt/{id}")
    Call<Receipt[]> deleteReceipt(@Path("id")int id);

    @DELETE("payment_voucher/{id}")
    Call<PaymentVoucher[]> deletePaymentVoucher(@Path("id")int id);

    @DELETE("bank/{id}")
    Call<Bank[]> deleteBank(@Path("id") int id);

    @DELETE("express_sale/{id}")
    Call<ExpressSale[]> deleteSale(@Path("id") int id);

    @DELETE("remove_sale_line/{id}")
    Call<ExpressSaleLine[]> deleteSaleLine(@Path("id") int id);

    @DELETE("product_group/{id}")
    Call<ProductGroup[]> deleteGroup(@Path("id") int id);

    @DELETE("unpacking/{id}")
    Call<Unpacking[]> deleteUnpacking(@Path("id") int id);

}
