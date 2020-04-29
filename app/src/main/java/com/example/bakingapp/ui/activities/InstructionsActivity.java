package com.example.bakingapp.ui.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.example.bakingapp.R;
import com.example.bakingapp.databinding.ActivityInstructionsBinding;
import com.example.bakingapp.model.Recipe;
import com.example.bakingapp.ui.fragments.StepsListFragment;
import com.example.bakingapp.utils.Constants;

import java.util.Objects;

public class InstructionsActivity extends AppCompatActivity {
    ActivityInstructionsBinding binding;
    boolean fromWidget = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInstructionsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        Recipe recipe = null;
        if (getIntent() != null && getIntent().hasExtra(Constants.SHARED_PREFERENCE_RECIPE_KEY)) {
            recipe = (Recipe) getIntent().getSerializableExtra(Constants.SHARED_PREFERENCE_RECIPE_KEY);
        }

        if (getIntent() != null && getIntent().hasExtra(Constants.SHARED_PREFERENCE_FROM_WIDGET_KEY))
            if (getIntent().getBooleanExtra(Constants.SHARED_PREFERENCE_FROM_WIDGET_KEY, false)) {
                fromWidget = true;
            }

        if (savedInstanceState == null) {
            assert recipe != null;
            shareForwardReceivedObject(recipe);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            onBackPressed();
        return true;
    }

    private void shareForwardReceivedObject(Recipe recipe) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment fragment = StepsListFragment.newInstance(recipe, fromWidget);
        ft.replace(R.id.fragment_container, fragment);
        ft.commit();
    }

    public interface IOnBackPressed {
        boolean onBackPressed();
    }

    @Override
    public void onBackPressed() {
        if (!fromWidget) {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.step_instructions);
            if (!(fragment instanceof IOnBackPressed) || !((IOnBackPressed) fragment).onBackPressed()) {
                super.onBackPressed();
            }
        } else {
            finish();
        }
    }
}

