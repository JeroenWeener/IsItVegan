package com.jwindustries.isitvegan.helpers;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.jwindustries.isitvegan.R;

public final class SnackbarHelper {
    private boolean isShowing = false;

    public void showMessage(Activity activity, String message) {
        if (!isShowing) {
            isShowing = true;
            activity.runOnUiThread(() -> {
                TextView scanningCueTextView = activity.findViewById(R.id.scanning_cue);
                scanningCueTextView.setVisibility(View.VISIBLE);
                scanningCueTextView.setText(message);
            });
        }
    }

    public void hide(Activity activity) {
        if (isShowing) {
            isShowing = false;
            activity.runOnUiThread(() -> {
                TextView scanningCueTextView = activity.findViewById(R.id.scanning_cue);
                scanningCueTextView.setVisibility(View.GONE);
            });
        }
    }
}
