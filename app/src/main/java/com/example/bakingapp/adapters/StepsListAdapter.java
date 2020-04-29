package com.example.bakingapp.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bakingapp.R;
import com.example.bakingapp.databinding.FragmentListStepsItemBinding;
import com.example.bakingapp.model.Step;
import com.example.bakingapp.ui.fragments.StepsListFragment;
import com.example.bakingapp.utils.Constants;
import com.example.bakingapp.utils.SharedPreferenceUtils;

import java.util.ArrayList;
import java.util.List;

public class StepsListAdapter extends RecyclerView.Adapter<StepsListAdapter.ViewHolder> {

    private final Context mContext;
    private List<Step> mSteps = new ArrayList<Step>() {
    };
    private final ListItemClickListener mOnClickListener;

    public interface ListItemClickListener {
        void onItemClickListener(int stepId, View view);
    }

    public StepsListAdapter(Context context, ListItemClickListener listener) {
        mContext = context;
        mOnClickListener = listener;
    }

    public void setSteps(List<Step> steps) {
        mSteps = steps;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(mContext).inflate(R.layout.fragment_list_steps_item, parent, false);
        view.setFocusable(false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        Step currentStep = mSteps.get(position);
        holder.binding.shortDescription.setText(currentStep.getShortDescription());

        boolean tabletSize = mContext.getResources().getBoolean(R.bool.isTablet);
        int stepId = SharedPreferenceUtils.getIdFromSharedPreference(mContext, Constants.SHARED_PREFERENCE_STEP_ID_KEY);

        if (tabletSize && stepId != -1 && SharedPreferenceUtils.getIdFromSharedPreference(mContext, Constants.SHARED_PREFERENCE_FROM_WIDGET_KEY) == 1) {
            if (stepId == position) {
                holder.binding.shortDescription.setBackgroundColor(ContextCompat.getColor(mContext, R.color.selected_step_color));
                StepsListFragment.previousSelectedView = holder.binding.shortDescription;
            }
        }
    }

    @Override
    public int getItemCount() {
        return mSteps != null ? mSteps.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        FragmentListStepsItemBinding binding;

        ViewHolder(View itemView) {
            super(itemView);
            binding = FragmentListStepsItemBinding.bind(itemView);
            binding.shortDescription.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            mOnClickListener.onItemClickListener(getAdapterPosition(), view);
        }
    }
}
