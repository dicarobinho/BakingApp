package com.example.bakingapp.ui.fragments;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.bakingapp.R;
import com.example.bakingapp.adapters.StepsListAdapter;
import com.example.bakingapp.databinding.FragmentStepsListBinding;
import com.example.bakingapp.model.Recipe;
import com.example.bakingapp.ui.activities.InstructionsActivity;
import com.example.bakingapp.utils.Constants;
import com.example.bakingapp.utils.SharedPreferenceUtils;
import com.example.bakingapp.widget.StepWidget;
import com.example.bakingapp.widget.UpdateIntentService;

import java.util.Objects;

public class StepsListFragment extends Fragment implements StepsListAdapter.ListItemClickListener {
    private FragmentStepsListBinding binding;
    private StepsListAdapter mAdapter;
    private Recipe mRecipe;

    @SuppressLint("StaticFieldLeak")
    public static TextView previousSelectedView = null; // solutie temporara  / caz in care revin in aplicatie din widget, trebuie sa memorez faptul ca acest textview a fost modificat

    public static StepsListFragment newInstance(Recipe recipe, boolean fromWidget) {
        StepsListFragment fragment = new StepsListFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.SHARED_PREFERENCE_RECIPE_KEY, recipe);
        bundle.putBoolean(Constants.SHARED_PREFERENCE_FROM_WIDGET_KEY, fromWidget);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStepsListBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        getSharedObjects();

        mAdapter = new StepsListAdapter(getContext(), this);
        setupRecyclerView();

        return view;
    }

    private void getSharedObjects() {
        if (getArguments() != null && getArguments().containsKey(Constants.SHARED_PREFERENCE_RECIPE_KEY)) {
            mRecipe = (Recipe) getArguments().getSerializable(Constants.SHARED_PREFERENCE_RECIPE_KEY);
            boolean fromWidget = getArguments().getBoolean(Constants.SHARED_PREFERENCE_FROM_WIDGET_KEY);
            setUiContent();
            if (fromWidget) {
                SharedPreferenceUtils.saveToSharedPreference(getContext(), 1, Constants.SHARED_PREFERENCE_FROM_WIDGET_KEY);
                onItemClickAuto();
            }
        }
    }

    private void setUiContent() {
        binding.ingredientsTitle.setText(R.string.ingredients_title);
        setIngredientsText();
    }

    private void setupRecyclerView() {

        boolean tabletSize = getResources().getBoolean(R.bool.isTablet);
        boolean isLand = getResources().getBoolean(R.bool.isLand);

        binding.myRecyclerView.setHasFixedSize(true);
        binding.myRecyclerView.setFocusable(false);
        binding.myRecyclerView.setNestedScrollingEnabled(false);

        if (!tabletSize && isLand)
            binding.myRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        else
            binding.myRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        binding.myRecyclerView.setAdapter(mAdapter);
        if (mRecipe != null)
            mAdapter.setSteps(mRecipe.getSteps());
    }

    private InstructionsActivity getActivityCast() {
        return (InstructionsActivity) getActivity();
    }


    @Override
    public void onItemClickListener(int stepId, View view) {
        SharedPreferenceUtils.saveToSharedPreference(getContext(), stepId, Constants.SHARED_PREFERENCE_STEP_ID_KEY);
        SharedPreferenceUtils.saveToSharedPreference(getContext(), 1, Constants.SHARED_PREFERENCE_FROM_WIDGET_KEY);

        launchStepFragment(stepId, view);
        if (widgetsAvailable())
            UpdateIntentService.startActionUpdateStepWidget(getContext(), mRecipe.getSteps().get(stepId).getDescription());
    }


    private void onItemClickAuto() {
        int id = SharedPreferenceUtils.getIdFromSharedPreference(getContext(), Constants.SHARED_PREFERENCE_STEP_ID_KEY);

        if (id != -1) {
            launchStepFragment(id, null);
        }
    }


    private void launchStepFragment(int stepId, @Nullable View view) {
        if (getActivityCast() != null) {
            FragmentTransaction transaction = getActivityCast().getSupportFragmentManager().beginTransaction();
            Fragment fragment = PresentsStepFragment.newInstance(mRecipe, stepId);

            boolean tabletSize = getResources().getBoolean(R.bool.isTablet);
            if (tabletSize) {
                transaction.replace(R.id.step_instructions, fragment);
                if (view != null)
                    markViewClicked(view);
            } else {
                transaction.replace(R.id.fragment_container, fragment);
            }

            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

    private void setIngredientsText() {
        for (int i = 0; i < mRecipe.getIngredients().size(); i++) {
            String currentText = binding.ingredients.getText().toString();
            if (i != 0)
                currentText += "\n";
            currentText += i + ". " + mRecipe.getIngredients().get(i).getIngredient() + ", " +
                    mRecipe.getIngredients().get(i).getMeasure() + ", " +
                    mRecipe.getIngredients().get(i).getQuantity();
            binding.ingredients.setText(currentText);
        }
    }

    private void markViewClicked(View view) {
        TextView textView = (TextView) view;
        if (previousSelectedView != null) {
            previousSelectedView.setBackgroundColor(ContextCompat.getColor(Objects.requireNonNull(getContext()), R.color.color_white));
        }

        previousSelectedView = (TextView) view;
        textView.setBackgroundColor(ContextCompat.getColor(Objects.requireNonNull(getContext()), R.color.selected_step_color));
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }

    private boolean widgetsAvailable() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getContext());
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(Objects.requireNonNull(getContext()), StepWidget.class));
        return appWidgetIds.length > 0;
    }
}
