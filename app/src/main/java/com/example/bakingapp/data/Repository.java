package com.example.bakingapp.data;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.bakingapp.model.Recipe;
import com.example.bakingapp.retrofit.ApiManager;
import com.example.bakingapp.utils.ResultsDisplay;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Repository {
    private static volatile Repository sInstance;

    private MutableLiveData<ResultsDisplay<List<Recipe>>> mRecipesObservable;

    private Repository() {
    }

    public static Repository getInstance() {
        if (sInstance == null) {
            synchronized (Repository.class) {
                if (sInstance == null) {
                    sInstance = new Repository();
                }
            }
        }
        return sInstance;
    }

    public LiveData<ResultsDisplay<List<Recipe>>> getRecipesFromServer() {
        mRecipesObservable = new MutableLiveData<>();
        mRecipesObservable.setValue(ResultsDisplay.loading(null));

        ApiManager.getInstance().getRecipes(new Callback<List<Recipe>>() {
            @Override
            public void onResponse(@NonNull Call<List<Recipe>> call, @NonNull Response<List<Recipe>> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        List<Recipe> recipes = response.body();
                        mRecipesObservable.setValue(ResultsDisplay.success(recipes));
                    }
                } else
                    mRecipesObservable.setValue(ResultsDisplay.error(String.valueOf(response.code()), null));
            }

            @Override
            public void onFailure(@NonNull Call<List<Recipe>> call, @NonNull Throwable t) {
                Log.v("Error", "Http Recipes Error");
                mRecipesObservable.setValue(ResultsDisplay.error(t.getMessage(), null));
            }
        });

        return mRecipesObservable;
    }
}
