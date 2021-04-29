package com.jwindustries.isitvegan.Activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ViewSwitcher;

import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.util.concurrent.ListenableFuture;
import com.jwindustries.isitvegan.AdditiveIngredientAdapter;
import com.jwindustries.isitvegan.Ingredient;
import com.jwindustries.isitvegan.IngredientList;
import com.jwindustries.isitvegan.R;
import com.jwindustries.isitvegan.TextFoundListener;
import com.jwindustries.isitvegan.TextReadAnalyzer;
import com.jwindustries.isitvegan.Utils;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class ScanActivity extends BaseActivity implements TextFoundListener {
    private final ExecutorService cameraExecutor = Executors.newSingleThreadExecutor();
    private final int PERMISSION_REQUEST_CODE = 10;
    private final String[] REQUIRED_PERMISSIONS = { Manifest.permission.CAMERA };
    private ImageAnalysis imageAnalyzer;

    private AdditiveIngredientAdapter adapter;
    private RecyclerView recyclerView;
    private ViewSwitcher scanListContainer;
    private LinearLayoutManager layoutManager;

    private List<Ingredient> ingredientList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        this.adapter = new AdditiveIngredientAdapter(this);

        this.layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        this.recyclerView = this.findViewById(R.id.ingredient_view);
        this.recyclerView.setLayoutManager(this.layoutManager);
        this.recyclerView.setAdapter(this.adapter);

        this.scanListContainer = this.findViewById(R.id.outer_scan_list_container);

        this.imageAnalyzer = new ImageAnalysis.Builder().setTargetAspectRatio(AspectRatio.RATIO_16_9).build();
        this.imageAnalyzer.setAnalyzer(this.cameraExecutor, new TextReadAnalyzer(this));

        this.ingredientList = IngredientList.getIngredientList(this);

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            requestPermission();
        }
    }

    @Override
    public void onTextFound(String foundText) {
        List<Ingredient> foundIngredients = this.ingredientList
                .stream()
                .filter(ingredient -> Utils.isIngredientInText(ingredient, Utils.normalizeString(foundText, false)))
                .collect(Collectors.toList());
        int numberAdded = this.adapter.addIngredients(foundIngredients);
        if (numberAdded > 0) {
            // Switch empty label for list view
            if (this.scanListContainer.getCurrentView().getId() != R.id.inner_scan_list_container) {
                this.scanListContainer.showNext();
            }

            // Maintain location at top of the list but do not jump there if the user is scrolling
            int scrollPosition = this.layoutManager.findFirstVisibleItemPosition();
            if (scrollPosition == 0) {
                this.recyclerView.scrollToPosition(0);
            }
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(
                () -> {
                    Preview preview = new Preview.Builder().build();
                    preview.setSurfaceProvider(((PreviewView) this.findViewById(R.id.camera_preview_view)).getSurfaceProvider());
                    try {
                        cameraProviderFuture.get().unbindAll();
                        cameraProviderFuture.get().bindToLifecycle(
                                this,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                imageAnalyzer
                        );
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                },
                ContextCompat.getMainExecutor(this)
        );
    }

    public void clearList(View view) {
        this.adapter.clearList();
        this.scanListContainer.showNext();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.cameraExecutor.shutdown();
    }

    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                PERMISSION_REQUEST_CODE
        );
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NotNull String[] permissions,
            @NotNull int[] grantResults
    ) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                finish();
            }
        }
    }
}