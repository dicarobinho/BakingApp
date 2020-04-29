package com.example.bakingapp.ui.fragments;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.bakingapp.ui.activities.InstructionsActivity;
import com.example.bakingapp.R;
import com.example.bakingapp.databinding.FragmentPresentsStepBinding;
import com.example.bakingapp.model.Recipe;
import com.example.bakingapp.utils.Constants;
import com.example.bakingapp.utils.NetworkUtils;
import com.example.bakingapp.utils.SharedPreferenceUtils;
import com.example.bakingapp.widget.StepWidget;
import com.example.bakingapp.widget.UpdateIntentService;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Log;
import com.google.android.exoplayer2.util.Util;

import java.util.Objects;

public class PresentsStepFragment extends Fragment implements InstructionsActivity.IOnBackPressed {
    private FragmentPresentsStepBinding binding;
    private SimpleExoPlayer player;
    private Recipe mRecipe;
    private int stepId;
    private long mVideoPosition = 0;

    private PlaybackStateListener playbackStateListener;
    private static final String TAG = FragmentPresentsStepBinding.class.getName();

    private boolean playWhenReady = true;
    private int currentWindow = 0;

    static PresentsStepFragment newInstance(Recipe recipe, int stepId) {
        PresentsStepFragment fragment = new PresentsStepFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.SHARED_PREFERENCE_RECIPE_KEY, recipe);
        bundle.putInt(Constants.SHARED_PREFERENCE_STEP_ID_KEY, stepId);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPresentsStepBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        playbackStateListener = new PlaybackStateListener();

        getSharedObjects();

        binding.nextStepButton.setOnClickListener(v -> launchNextStep());
        binding.previousStepButton.setOnClickListener(v -> launchPreviousStep());

        boolean tabletSize = getResources().getBoolean(R.bool.isTablet);
        if (tabletSize) {
            binding.nextStepButton.setVisibility(View.GONE);
            binding.previousStepButton.setVisibility(View.GONE);
        }

        return view;
    }

    private void launchNextStep() {
        if (stepId < mRecipe.getSteps().size() - 1) {
            if (getActivityCast() != null) {
                FragmentTransaction transaction = getActivityCast().getSupportFragmentManager().beginTransaction();
                Fragment fragment = PresentsStepFragment.newInstance(mRecipe, stepId + 1);
                transaction.replace(R.id.fragment_container, fragment);
                transaction.commit();
            }

            if (widgetsAvailable())
                UpdateIntentService.startActionUpdateStepWidget(getContext(), mRecipe.getSteps().get(stepId + 1).getDescription());

            SharedPreferenceUtils.saveToSharedPreference(getContext(), stepId + 1, Constants.SHARED_PREFERENCE_STEP_ID_KEY);

        } else {

            SharedPreferenceUtils.saveToSharedPreference(getContext(), -1, Constants.SHARED_PREFERENCE_STEP_ID_KEY);
            SharedPreferenceUtils.saveToSharedPreference(getContext(), -1, Constants.SHARED_PREFERENCE_RECIPE_KEY);

            if (widgetsAvailable())
                UpdateIntentService.startActionUpdateStepWidget(getContext(), getString(R.string.no_selected_step));

            if (getActivityCast() != null) {
                getActivityCast().finish();
            }
        }
    }

    private void launchPreviousStep() {
        if (stepId > 0) {
            if (getActivityCast() != null) {
                FragmentTransaction transaction = getActivityCast().getSupportFragmentManager().beginTransaction();
                Fragment fragment = PresentsStepFragment.newInstance(mRecipe, stepId - 1);
                transaction.replace(R.id.fragment_container, fragment);
                transaction.commit();
            }

            if (widgetsAvailable())
                UpdateIntentService.startActionUpdateStepWidget(getContext(), mRecipe.getSteps().get(stepId - 1).getDescription());

            SharedPreferenceUtils.saveToSharedPreference(getContext(), stepId - 1, Constants.SHARED_PREFERENCE_STEP_ID_KEY);
        }
    }

    private boolean widgetsAvailable() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getContext());
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(Objects.requireNonNull(getContext()), StepWidget.class));
        return appWidgetIds.length > 0;
    }

    private void getSharedObjects() {
        if (getArguments() != null && getArguments().containsKey(Constants.SHARED_PREFERENCE_RECIPE_KEY)) {
            mRecipe = (Recipe) getArguments().getSerializable(Constants.SHARED_PREFERENCE_RECIPE_KEY);
            stepId = getArguments().getInt(Constants.SHARED_PREFERENCE_STEP_ID_KEY);

            setUiContent();
        }
    }

    private void setUiContent() {
        assert mRecipe != null;
        binding.instructions.setText(mRecipe.getSteps().get(stepId).getDescription());

        if (stepId == mRecipe.getSteps().size() - 1) {
            binding.nextStepButton.setText(R.string.steps_done);
        } else if (stepId == 0) {
            binding.previousStepButton.setVisibility(View.GONE);
        } else {
            binding.previousStepButton.setVisibility(View.VISIBLE);
        }
    }

    private void initializePlayer(String videoUrl) {
        if (!videoUrl.equals("") && NetworkUtils.isConnected(Objects.requireNonNull(getContext()))) {
            player = ExoPlayerFactory.newSimpleInstance(Objects.requireNonNull(getActivityCast()), new DefaultTrackSelector());
            binding.videoView.setPlayer(player);

            Uri uri = Uri.parse(videoUrl);
            MediaSource mediaSource = buildMediaSource(uri);

            player.setPlayWhenReady(playWhenReady);
            player.seekTo(currentWindow, mVideoPosition);
            player.prepare(mediaSource, false, false);
            player.addListener(playbackStateListener);
            binding.spinnerVideoDetails.setVisibility(View.GONE);

            boolean tabletSize = getResources().getBoolean(R.bool.isTablet);
            boolean isLand = getResources().getBoolean(R.bool.isLand);
            if (isLand && !tabletSize)
                hideBars();
            if (tabletSize && isLand) {
                binding.instructions.setVisibility(View.INVISIBLE);
            }

        } else {
            boolean isLand = getResources().getBoolean(R.bool.isLand);
            if (!NetworkUtils.isConnected(Objects.requireNonNull(getContext()))) {
                Toast.makeText(getContext(), R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
                binding.imageNoVideo.setVisibility(View.VISIBLE);
            } else if (videoUrl.equals("") && !isLand) {
                if (binding.cardviewMediaPlayer != null)
                    binding.cardviewMediaPlayer.setVisibility(View.GONE);
            } else if (videoUrl.equals("")) {
                binding.videoView.setVisibility(View.GONE);
                binding.spinnerVideoDetails.setVisibility(View.GONE);
                binding.imageNoVideo.setVisibility(View.GONE);
            } else binding.imageNoVideo.setVisibility(View.VISIBLE);
        }
    }

    private MediaSource buildMediaSource(Uri uri) {
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(Objects.requireNonNull(getActivityCast()), getString(R.string.exoplayer_codelab));
        return new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT >= 24) {
            initializePlayer(getVideoUrl());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if ((Util.SDK_INT < 24 || player == null)) {
            initializePlayer(getVideoUrl());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT < 24) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT >= 24) {
            releasePlayer();
        }
    }

    private void releasePlayer() {
        if (player != null) {
            playWhenReady = player.getPlayWhenReady();
            mVideoPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            player.removeListener(playbackStateListener);
            player.release();
            player = null;
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mVideoPosition = savedInstanceState.getLong(Constants.SHARED_PREFERENCE_VIDEO_POSITION_KEY);
            playWhenReady = savedInstanceState.getBoolean(Constants.SHARED_PREFERENCE_VIDEO_STATUS);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(Constants.SHARED_PREFERENCE_VIDEO_POSITION_KEY, mVideoPosition);
        outState.putBoolean(Constants.SHARED_PREFERENCE_VIDEO_STATUS, playWhenReady);
    }

    private String getVideoUrl() {
        String videoUrl = "";
        if (!mRecipe.getSteps().get(stepId).getVideoURL().equals(""))
            videoUrl = mRecipe.getSteps().get(stepId).getVideoURL();
        else if (!mRecipe.getSteps().get(stepId).getThumbnailURL().equals(""))
            videoUrl = mRecipe.getSteps().get(stepId).getThumbnailURL();

        return videoUrl;
    }

    @Override
    public boolean onBackPressed() {
        boolean tabletSize = getResources().getBoolean(R.bool.isTablet);
        if (tabletSize) {
            if (getActivityCast() != null)
                getActivityCast().finish();
            SharedPreferenceUtils.saveToSharedPreference(getContext(), -1, Constants.SHARED_PREFERENCE_STEP_ID_KEY);
            return true;
        }
        return true;
    }

    private InstructionsActivity getActivityCast() {
        return (InstructionsActivity) getActivity();
    }

    private static class PlaybackStateListener implements Player.EventListener {

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            String stateString;
            switch (playbackState) {
                case ExoPlayer.STATE_IDLE:
                    stateString = "ExoPlayer.STATE_IDLE      -";
                    break;
                case ExoPlayer.STATE_BUFFERING:
                    stateString = "ExoPlayer.STATE_BUFFERING -";
                    break;
                case ExoPlayer.STATE_READY:
                    stateString = "ExoPlayer.STATE_READY     -";
                    break;
                case ExoPlayer.STATE_ENDED:
                    stateString = "ExoPlayer.STATE_ENDED     -";
                    break;
                default:
                    stateString = "UNKNOWN_STATE             -";
                    break;
            }
            Log.d(TAG, "changed state to " + stateString
                    + " playWhenReady: " + playWhenReady);
        }
    }

    private void hideBars() {
        View decorView = Objects.requireNonNull(getActivityCast()).getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        Objects.requireNonNull(((AppCompatActivity) getActivityCast()).getSupportActionBar()).hide();
    }
}
