package com.jwindustries.isitvegan.activities;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.jwindustries.isitvegan.R;

public class MainActivity extends BaseActivity {
    private View hazeView;
    private View cameraOverlay;

    // Animation duration in ms
    private static final int ANIMATION_DURATION = 250;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        hazeView = findViewById(R.id.haze_view);
        cameraOverlay = findViewById(R.id.camera_overlay);
        View dragHandle = findViewById(R.id.drag_handle);
        dragHandle.setOnClickListener(view -> startARScanning());

        findViewById(R.id.settings_button).setOnClickListener(view ->
                startActivity(new Intent(this, SettingsActivity.class)));
        findViewById(R.id.ingredient_list_button).setOnClickListener(view ->
                startActivity(new Intent(this, IngredientOverviewActivity.class)));
    }

    private void startARScanning() {
        ValueAnimator hazeAnimator = ValueAnimator.ofFloat(hazeView.getAlpha(), 0f);
        hazeAnimator.addUpdateListener(valueAnimator -> {
            float intermediateAlpha = (Float) valueAnimator.getAnimatedValue();
            hazeView.setAlpha(intermediateAlpha);
        });
        ValueAnimator overlayAnimator = ValueAnimator.ofInt(cameraOverlay.getTop(), cameraOverlay.getMeasuredHeight());
        overlayAnimator.addUpdateListener(valueAnimator -> {
            int intermediateY = (Integer) overlayAnimator.getAnimatedValue();
            cameraOverlay.setTop(intermediateY);
        });

        hazeAnimator.setDuration(ANIMATION_DURATION);
        hazeAnimator.start();
        overlayAnimator.setDuration(ANIMATION_DURATION);
        overlayAnimator.start();

        Bundle result = new Bundle();
        result.putBoolean("isInPreviewMode", false);
        getSupportFragmentManager().setFragmentResult("requestKey", result);
    }
}