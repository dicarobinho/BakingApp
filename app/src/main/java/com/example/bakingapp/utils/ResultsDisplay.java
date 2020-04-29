package com.example.bakingapp.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ResultsDisplay<T> {
    public static final int STATE_LOADING = 0;
    public static final int STATE_SUCCESS = 1;
    public static final int STATE_ERROR = 2;

    public final T data;
    public final int state;
    public final String errorMessage;

    private ResultsDisplay(@Nullable T data, int state, @Nullable String errorMessage) {
        this.data = data;
        this.state = state;
        this.errorMessage = errorMessage;
    }

    public static <T> ResultsDisplay<T> success(@NonNull T data) {
        return new ResultsDisplay<>(data, STATE_SUCCESS, null);
    }

    public static <T> ResultsDisplay<T> loading(@Nullable T data) {
        return new ResultsDisplay<>(data, STATE_LOADING, null);
    }

    public static <T> ResultsDisplay<T> error(String errorMessage, @Nullable T data) {
        return new ResultsDisplay<>(data, STATE_ERROR, errorMessage);
    }
}
