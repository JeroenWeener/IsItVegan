package com.jwindustries.isitvegan.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.lifecycle.ViewModelProvider;

import com.jwindustries.isitvegan.R;
import com.jwindustries.isitvegan.CameraXViewModel;

public class MainActivity extends AppCompatActivity {
    PreviewView previewView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        previewView = findViewById(R.id.preview_view);
        previewView.setOnClickListener(view -> {
            startActivity(new Intent(this, ARScanActivity.class));
        });

        new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()))
                .get(CameraXViewModel.class)
                .getProcessCameraProvider()
                .observe(this, this::bindPreviewUseCase);
    }

    private void bindPreviewUseCase(ProcessCameraProvider provider) {
        Preview previewUseCase = new Preview.Builder().build();
        previewUseCase.setSurfaceProvider(previewView.getSurfaceProvider());
        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
        provider.bindToLifecycle(this, cameraSelector, previewUseCase);
    }
}