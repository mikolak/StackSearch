package com.kojdecki.stacksearch.retrofit;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by calot on 2/15/16.
 */
public interface StackOverflowAPI {
    @GET("/2.2/search?order=desc&sort=relevance&site=stackoverflow&pagesize=20")
    Call<Search> loadSearch(@Query("tagged") String tags, @Query("page") int page);
}
