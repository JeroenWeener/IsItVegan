package com.jwindustries.isitvegan.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProvider;

import com.jwindustries.isitvegan.CameraXViewModel;
import com.jwindustries.isitvegan.R;

public class MainActivity extends BaseActivity {
    private ViewGroup layoutContainerView;
    private CardView previewContainerView;
    private PreviewView previewView;
    private View hazeView;

    private float originalAlpha = -1f;
    private int originalHeight = -1;

    // Animation duration in ms
    private static final int ANIMATION_DURATION = 250;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        layoutContainerView = findViewById(R.id.layout_container);

        previewContainerView = findViewById(R.id.preview_container);
        previewContainerView.setOnClickListener(view -> startScanActivity());

        previewView = findViewById(R.id.preview_view);

        hazeView = findViewById(R.id.haze_view);

        new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()))
                .get(CameraXViewModel.class)
                .getProcessCameraProvider()
                .observe(this, this::bindPreviewUseCase);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Restore initial alpha that has been changed by transition
        if (originalAlpha != -1f) {
            hazeView.setAlpha(originalAlpha);
        }

        // Restore initial preview view size that has been changed by transition
        if (originalHeight != -1) {
            ViewGroup.LayoutParams layoutParams = previewContainerView.getLayoutParams();
            layoutParams.height = originalHeight;
            previewContainerView.setLayoutParams(layoutParams);
        }
    }

    /**
     * Start scanning activity. As a nice animation, remove the haze from the preview view and
     * increase the dimensions to fill the screen, as these will be the dimensions of the surface
     * view in the scanning activity.
     */
    private void startScanActivity() {
        originalHeight = previewContainerView.getMeasuredHeight();
        int verticalOffset = -((ViewGroup.MarginLayoutParams) previewContainerView.getLayoutParams()).topMargin;
        ValueAnimator previewHeightAnimator = ValueAnimator.ofInt(originalHeight, layoutContainerView.getMeasuredHeight() + 2 * verticalOffset);
        previewHeightAnimator.addUpdateListener(valueAnimator -> {
            int intermediateHeight = (Integer) valueAnimator.getAnimatedValue();
            ViewGroup.LayoutParams layoutParams = previewContainerView.getLayoutParams();
            layoutParams.height = intermediateHeight;
            previewContainerView.setLayoutParams(layoutParams);
        });

        originalAlpha = hazeView.getAlpha();
        ValueAnimator hazeAnimator = ValueAnimator.ofFloat(originalAlpha, 0f);
        hazeAnimator.addUpdateListener(valueAnimator -> {
            float intermediateAlpha = (Float) valueAnimator.getAnimatedValue();
            hazeView.setAlpha(intermediateAlpha);
        });

        // Start the scanning activity after the animation has finished
        Intent intent = new Intent(this, ARScanActivity.class);
        previewHeightAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                startActivity(intent);
            }
        });

        previewHeightAnimator.setDuration(ANIMATION_DURATION);
        hazeAnimator.setDuration(ANIMATION_DURATION);
        previewHeightAnimator.start();
        hazeAnimator.start();
    }

    private void bindPreviewUseCase(ProcessCameraProvider provider) {
        Preview previewUseCase = new Preview.Builder().build();
        previewUseCase.setSurfaceProvider(previewView.getSurfaceProvider());
        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
        provider.bindToLifecycle(this, cameraSelector, previewUseCase);
    }
}