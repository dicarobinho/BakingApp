package com.example.bakingapp.widget;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.Nullable;


public class UpdateIntentService extends IntentService {
    public static final String ACTION_UPDATE_STEP_WIDGET = "com.example.bakingapp.action.update_step_widget";

    public UpdateIntentService() {
        super("UpdateIntentService");
    }

    public static void startActionUpdateStepWidget(Context context, String stepDescription) {
        Intent intent = new Intent(context, UpdateIntentService.class);
        intent.setAction(ACTION_UPDATE_STEP_WIDGET);
        intent.putExtra("stepDescription", stepDescription);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_UPDATE_STEP_WIDGET.equals(action)) {
                String stepDescription = intent.getStringExtra("stepDescription");
                handleActionUpdateStepWidget(stepDescription);
            }
        }
    }

    private void handleActionUpdateStepWidget(String stepDescription) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(getApplicationContext(), StepWidget.class));
        for (int appWidgetId : appWidgetIds)
            StepWidget.updateStepWidget(getApplicationContext(), appWidgetManager, appWidgetId, stepDescription);
    }
}
