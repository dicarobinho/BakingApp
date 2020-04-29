package com.example.bakingapp.retrofit;


import com.example.bakingapp.model.Recipe;

import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import static com.example.bakingapp.utils.Constants.BASE_URL;

public class ApiManager {

    private static ApiManager sApiManager;
    private static ApiInterface sApiInterfaceForced;

    private ApiManager() {
        Retrofit retrofitForced = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        sApiInterfaceForced = retrofitForced.create(ApiInterface.class);
    }

    public static ApiManager getInstance() {
        if (sApiManager == null) {
            sApiManager = new ApiManager();
        }
        return sApiManager;
    }

    public void getRecipes(Callback<List<Recipe>> callback) {
        Call<List<Recipe>> response = sApiInterfaceForced.getRecipes();
        response.enqueue(callback);
    }
}
