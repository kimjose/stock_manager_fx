package network;

import models.ApiResponse;
import models.customers.Customer;
import models.customers.Invoice;
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
    Call<models.vendors.Invoice> vendorInvoices();

    @GET("products_pack")
    Call<ProductsPack[]> ProductsPacks();



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


    /***
     * PUT requests
     * ***/

    @PUT("brand/{id}")
    Call<Brand[]> updateBrand(@Path("id")int id, @Query("name") String name);

    @PUT("warehouse/{id}")
    Call<Warehouse[]> updateWarehouse(@Path("id")int id, @Query("name") String name, @Query("location") String location);


    /**
     * DELETE requests
     * ***/
    @DELETE("brand/{id}")
    Call<Brand[]> deleteBrand(@Path("id")int id);

    @DELETE("warehouse/{id}")
    Call<Warehouse[]> deleteWarehouse(@Path("id") int id);
}
