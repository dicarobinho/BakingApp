package com.example.bakingapp.adapters;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bakingapp.R;
import com.example.bakingapp.databinding.AdapterListItemRecipeBinding;
import com.example.bakingapp.model.Recipe;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class RecipesAdapter extends RecyclerView.Adapter<RecipesAdapter.ViewHolder> {

    private final ListItemClickListener mOnClickListener;

    public interface ListItemClickListener {
        void onItemClickListener(int viewIndex);
    }

    private List<Recipe> mRecipes = new ArrayList<Recipe>() {
    };

    public RecipesAdapter(ListItemClickListener listener) {
        mOnClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_list_item_recipe, parent, false);
        return new ViewHolder(view);
    }

    public void setRecipes(List<Recipe> recipes) {
        mRecipes = recipes;
        notifyDataSetChanged();
    }

    public List<Recipe> getRecipes() {
        return mRecipes;
    }

    @Override
    public int getItemCount() {
        return mRecipes != null ? mRecipes.size() : 0;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        Recipe currentRecipe = mRecipes.get(position);

        if (currentRecipe.getImage().equals("")) {
            Picasso.get().load(R.drawable.test_image).into(holder.binding.recipeGeneralImage);
        } else Picasso.get().load(currentRecipe.getImage()).into(holder.binding.recipeGeneralImage);

        holder.binding.recipeName.setText(currentRecipe.getName());

        String finalMessageRecipeServings = "Servings: " + currentRecipe.getServings();
        holder.binding.recipeServings.setText(finalMessageRecipeServings);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        AdapterListItemRecipeBinding binding;

        ViewHolder(View itemView) {
            super(itemView);
            binding = AdapterListItemRecipeBinding.bind(itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int viewIndex = getAdapterPosition();
            mOnClickListener.onItemClickListener(viewIndex);
        }
    }
}

