package com.jwindustries.isitvegan.activities;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;

import com.jwindustries.isitvegan.OnSwipeListener;
import com.jwindustries.isitvegan.R;
import com.jwindustries.isitvegan.Utils;

public class MainActivity extends BaseActivity {
    private View hazeView;
    private View cameraOverlay;
    private View dragHandle;

    // Animation duration in ms
    private static final int ANIMATION_DURATION = 250;
    private ValueAnimator hazeAnimator;
    private ValueAnimator overlayAnimator;

    private boolean isInPreviewMode = true;

    private String appLocale;
    private String ingredientsLocale;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Utils.handleTheme(this);

        // Trigger update of DEBUG variable in Utils
        Utils.isDebugging(this);

        hazeView = findViewById(R.id.haze_view);
        cameraOverlay = findViewById(R.id.camera_overlay);
        dragHandle = findViewById(R.id.drag_handle);
        // Use onTouch rather than onClick as this fires even if the motionEvent ends outside of the view
        dragHandle.setOnTouchListener((view, motionEvent) -> {
            view.performClick();
            startARScanning();
            return true;
        });

        // Listen for rendering to finish before initializing value animators, as they depend on rendered height
        final View layoutContainer = findViewById(R.id.layout_container);
        layoutContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                layoutContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                initValueAnimators();
            }
        });

        // Store locales so we can refresh the activity upon locale change
        appLocale = Utils.handleAppLocale(this);
        ingredientsLocale = Utils.getIngredientLocale(this);

        setupSwipeDetector();
    }

    @Override
    public void onRestart() {
        super.onRestart();

        // Recreate when language has changed
        if (!Utils.getAppLocale(this).equals(appLocale) ||
                !Utils.getIngredientLocale(this).equals(ingredientsLocale)) {
            this.recreate();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.actionbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            this.startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (isInPreviewMode) {
            super.onBackPressed();
        } else {
            stopARScanning();
        }
    }

    private void setupSwipeDetector() {
        final GestureDetector stopDetector = new GestureDetector(this, new OnSwipeListener() {
            @Override
            public boolean onSwipe(Direction direction) {
                if (direction == Direction.up) {
                    stopARScanning();
                }
                return true;
            }
        });

        hazeView.setOnTouchListener((view, motionEvent) -> {
            view.performClick();
            stopDetector.onTouchEvent(motionEvent);
            return true;
        });
    }

    private void initValueAnimators() {
        hazeAnimator = ValueAnimator.ofFloat(hazeView.getAlpha(), 0f);
        hazeAnimator.addUpdateListener(valueAnimator -> {
            float intermediateAlpha = (Float) valueAnimator.getAnimatedValue();
            hazeView.setAlpha(intermediateAlpha);
        });

        overlayAnimator = ValueAnimator.ofInt(cameraOverlay.getTop(), cameraOverlay.getMeasuredHeight());
        overlayAnimator.addUpdateListener(valueAnimator -> {
            int intermediateY = (Integer) overlayAnimator.getAnimatedValue();
            cameraOverlay.setTop(intermediateY);
        });

        hazeAnimator.setDuration(ANIMATION_DURATION);
        overlayAnimator.setDuration(ANIMATION_DURATION);
    }

    private void startARScanning() {
        if (isInPreviewMode) {
            hideKeyboard();

            hazeAnimator.start();
            overlayAnimator.start();

            setPreviewMode(false);
        }
    }

    private void stopARScanning() {
        if (!isInPreviewMode) {
            hazeAnimator.reverse();
            overlayAnimator.reverse();

            setPreviewMode(true);
        }
    }

    private void setPreviewMode(boolean isInPreviewMode) {
        this.isInPreviewMode = isInPreviewMode;

        Bundle result = new Bundle();
        result.putBoolean(getString(R.string.key_bundle_is_in_preview_mode), isInPreviewMode);
        getSupportFragmentManager().setFragmentResult(getString(R.string.key_fragment_result), result);
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }
}