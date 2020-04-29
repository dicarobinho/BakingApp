package com.example.bakingapp.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.example.bakingapp.R;
import com.example.bakingapp.ui.activities.RecipesActivity;
import com.example.bakingapp.utils.Constants;

public class StepWidget extends AppWidgetProvider {

    static void updateStepWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId, String stepDescription) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.step_widget);

        if (stepDescription != null) {
            views.setTextViewText(R.id.instructions_widget, stepDescription);
        }

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.step_widget);
        Intent configIntent = new Intent(context, RecipesActivity.class);
        configIntent.putExtra(Constants.SHARED_PREFERENCE_FROM_WIDGET_KEY, true);

        PendingIntent configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.widget_parent, configPendingIntent);

        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

}