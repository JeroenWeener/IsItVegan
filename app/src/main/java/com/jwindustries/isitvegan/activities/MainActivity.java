package com.jwindustries.isitvegan.activities;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.jwindustries.isitvegan.R;
import com.jwindustries.isitvegan.Utils;

public class MainActivity extends AppCompatActivity {
    private View hazeView;
    private View cameraOverlay;
    private View dragHandle;
    private ImageButton closeButton;
    private ImageButton torchButton;
    private ImageButton settingsButton;

    // Animation duration in ms
    private static final int ANIMATION_DURATION = 250;
    private ValueAnimator hazeAnimator;
    private ValueAnimator overlayAnimator;

    private boolean isInPreviewMode = true;
    private boolean isTorchOn = false;

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
            startScanning();
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

        setupSystemBars();
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
    public void onBackPressed() {
        if (isInPreviewMode) {
            super.onBackPressed();
        } else {
            stopScanning();
        }
    }

    /**
     * Programmatically set system bar properties
     * - Status bar
     * - Action bar
     * - Navigation bar
     */
    private void setupSystemBars() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setCustomView(R.layout.action_bar);

            closeButton = findViewById(R.id.action_bar_button_close);
            closeButton.setOnClickListener(view -> stopScanning());

            torchButton = findViewById(R.id.action_bar_button_torch);
            torchButton.setOnClickListener(view -> toggleTorch());

            settingsButton = findViewById(R.id.action_bar_button_settings);
            settingsButton.setOnClickListener(view -> startActivity(new Intent(this, SettingsActivity.class)));
        }

        // Account for navigation bar
        ViewGroup view = findViewById(R.id.layout_container);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
        params.bottomMargin = Utils.getNavigationBarHeight(this);
        view.setLayoutParams(params);
    }

    private void initValueAnimators() {
        hazeAnimator = ValueAnimator.ofFloat(hazeView.getAlpha(), 0f);
        hazeAnimator.addUpdateListener(valueAnimator -> {
            float intermediateAlpha = (Float) valueAnimator.getAnimatedValue();
            hazeView.setAlpha(intermediateAlpha);
        });

        int measuredHeight = cameraOverlay.getMeasuredHeight();
        overlayAnimator = ValueAnimator.ofInt(cameraOverlay.getTop(), measuredHeight);
        overlayAnimator.addUpdateListener(valueAnimator -> {
            int intermediateY = (Integer) overlayAnimator.getAnimatedValue();
            cameraOverlay.setTop(intermediateY);
            cameraOverlay.setVisibility(intermediateY == measuredHeight ? View.INVISIBLE : View.VISIBLE);
        });

        hazeAnimator.setDuration(ANIMATION_DURATION);
        overlayAnimator.setDuration(ANIMATION_DURATION);
    }

    private void startScanning() {
        if (isInPreviewMode) {
            hideKeyboard();

            hazeAnimator.start();
            overlayAnimator.start();

            setPreviewMode(false);
        }
    }

    private void stopScanning() {
        if (!isInPreviewMode) {
            hazeAnimator.reverse();
            overlayAnimator.reverse();

            setPreviewMode(true);
        }
    }

    public void updateActionButtons(boolean isInPreviewMode) {
        closeButton.setVisibility(isInPreviewMode ? View.INVISIBLE : View.VISIBLE);
        torchButton.setVisibility(isInPreviewMode ? View.INVISIBLE : View.VISIBLE);
        settingsButton.setVisibility(isInPreviewMode ? View.VISIBLE : View.INVISIBLE);
    }

    private void setPreviewMode(boolean isInPreviewMode) {
        this.isInPreviewMode = isInPreviewMode;

        // Show/hide close button
        updateActionButtons(isInPreviewMode);

        // Communicate to scan fragment
        Bundle result = new Bundle();
        result.putBoolean(getString(R.string.key_bundle_is_in_preview_mode), isInPreviewMode);
        getSupportFragmentManager().setFragmentResult(getString(R.string.key_fragment_result), result);
    }

    private void toggleTorch() {
        this.isTorchOn = !this.isTorchOn;

        torchButton.setImageResource(this.isTorchOn ? R.drawable.flash_on_white : R.drawable.flash_off_white);

        // Communicate to scan fragment
        Bundle result = new Bundle();
        result.putBoolean(getString(R.string.key_bundle_is_torch_on), isTorchOn);
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