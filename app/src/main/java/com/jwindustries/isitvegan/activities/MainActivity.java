package com.jwindustries.isitvegan.activities;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.jwindustries.isitvegan.R;
import com.jwindustries.isitvegan.Utils;
import com.jwindustries.isitvegan.fragments.IngredientOverviewFragment;

public class MainActivity extends BaseActivity {
    private View hazeView;
    private View cameraOverlay;

    // Animation duration in ms
    private static final int ANIMATION_DURATION = 250;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Utils.handleTheme(this);

        // Trigger update of DEBUG variable in Utils
        Utils.isDebugging(this);

        hazeView = findViewById(R.id.haze_view);
        cameraOverlay = findViewById(R.id.camera_overlay);
        View dragHandle = findViewById(R.id.drag_handle);
        dragHandle.setOnClickListener(view -> startARScanning());
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