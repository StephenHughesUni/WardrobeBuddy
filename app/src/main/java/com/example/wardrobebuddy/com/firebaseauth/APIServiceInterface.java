package com.example.wardrobebuddy.com.firebaseauth;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface APIServiceInterface {
    @GET("/fetch-product-info/")
    Call<ProductInfoResponse> fetchProductInfo(@Query("product_url") String productUrl);
}
