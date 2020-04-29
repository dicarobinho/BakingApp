package com.example.bakingapp.ui.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.bakingapp.R;
import com.example.bakingapp.RecipesActivityViewModel;
import com.example.bakingapp.adapters.RecipesAdapter;
import com.example.bakingapp.databinding.ActivityRecipesBinding;
import com.example.bakingapp.model.Recipe;
import com.example.bakingapp.utils.Constants;
import com.example.bakingapp.utils.NetworkUtils;
import com.example.bakingapp.utils.ResultsDisplay;
import com.example.bakingapp.utils.SharedPreferenceUtils;

import java.util.List;

public class RecipesActivity extends AppCompatActivity implements RecipesAdapter.ListItemClickListener {

    ActivityRecipesBinding binding;
    RecipesAdapter mAdapter;
    boolean fromWidget;
    private RecipesActivityViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRecipesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SharedPreferenceUtils.saveToSharedPreference(this, -1, Constants.SHARED_PREFERENCE_FROM_WIDGET_KEY);

        if (getIntent() != null && getIntent().hasExtra(Constants.SHARED_PREFERENCE_FROM_WIDGET_KEY))
            fromWidget = getIntent().getBooleanExtra(Constants.SHARED_PREFERENCE_FROM_WIDGET_KEY, false);

        mViewModel = new ViewModelProvider(this).get(RecipesActivityViewModel.class);

        mAdapter = new RecipesAdapter(this);
        setupAdapter();
        getRecipesFromServer();
    }

    public void getRecipesFromServer() {
        if (NetworkUtils.isConnected(this)) {
            mViewModel.getRecipesFromServer().observe(this, checkResultDisplay -> {
                if (checkResultDisplay != null) {
                    switch (checkResultDisplay.state) {
                        case ResultsDisplay.STATE_LOADING:
                            loadingStateUi();
                            break;
                        case ResultsDisplay.STATE_ERROR:
                            Toast.makeText(this, R.string.error_loading_recipes, Toast.LENGTH_LONG).show();
                            break;
                        case ResultsDisplay.STATE_SUCCESS:
                            List<Recipe> recipes = checkResultDisplay.data;
                            mAdapter.setRecipes(recipes);
                            successStateUi();
                            if (fromWidget)
                                onItemClickAuto();
                            break;
                    }
                }
            });
        } else {
            Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_LONG).show();
            binding.emptyView.setVisibility(View.INVISIBLE);
            successStateUi();
        }
    }

    private void setupAdapter() {
        binding.myRecyclerView.setEmptyView(binding.emptyView);
        binding.myRecyclerView.setHasFixedSize(true);
        boolean tabletSize = getResources().getBoolean(R.bool.isTablet);
        boolean isLand = getResources().getBoolean(R.bool.isLand);

        if (tabletSize && isLand) {
            binding.myRecyclerView.setLayoutManager(new GridLayoutManager(RecipesActivity.this, 3));
        } else if (tabletSize || isLand) {
            binding.myRecyclerView.setLayoutManager(new GridLayoutManager(RecipesActivity.this, 2));
        } else
            binding.myRecyclerView.setLayoutManager(new LinearLayoutManager(RecipesActivity.this));

        binding.myRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onItemClickListener(int viewIndex) {
        SharedPreferenceUtils.saveToSharedPreference(getApplicationContext(), viewIndex, Constants.SHARED_PREFERENCE_RECIPE_KEY);

        Intent intent = new Intent(RecipesActivity.this, InstructionsActivity.class);
        intent.putExtra(Constants.SHARED_PREFERENCE_RECIPE_KEY, mAdapter.getRecipes().get(viewIndex));
        startActivity(intent);
    }

    private void onItemClickAuto() {
        int id = SharedPreferenceUtils.getIdFromSharedPreference(this, Constants.SHARED_PREFERENCE_RECIPE_KEY);

        if (id != -1) {
            Intent intent = new Intent(RecipesActivity.this, InstructionsActivity.class);
            intent.putExtra(Constants.SHARED_PREFERENCE_RECIPE_KEY, mAdapter.getRecipes().get(id));
            intent.putExtra(Constants.SHARED_PREFERENCE_FROM_WIDGET_KEY, true);
            startActivity(intent);
        }
    }

    private void loadingStateUi() {
        binding.loadingSpinner.setVisibility(View.VISIBLE);
    }

    private void successStateUi() {
        binding.loadingSpinner.setVisibility(View.INVISIBLE);
    }
}
