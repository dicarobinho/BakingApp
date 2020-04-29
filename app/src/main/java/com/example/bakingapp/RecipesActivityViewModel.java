package com.example.bakingapp;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.bakingapp.data.Repository;
import com.example.bakingapp.model.Recipe;
import com.example.bakingapp.utils.ResultsDisplay;

import java.util.List;

public class RecipesActivityViewModel extends AndroidViewModel {
    private final Repository repository;

    public RecipesActivityViewModel(@NonNull Application application) {
        super(application);
        repository = Repository.getInstance();
    }

    public LiveData<ResultsDisplay<List<Recipe>>> getRecipesFromServer() {
        return repository.getRecipesFromServer();
    }
}