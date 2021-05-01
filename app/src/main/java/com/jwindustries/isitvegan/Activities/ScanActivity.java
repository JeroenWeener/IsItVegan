package com.jwindustries.isitvegan.Activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraInfo;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.core.TorchState;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
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
    private Camera camera;

    private AdditiveIngredientAdapter adapter;
    private RecyclerView recyclerView;
    private ViewSwitcher scanListContainer;
    private LinearLayoutManager layoutManager;

    private Menu menu;

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
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.scan_actionbar_menu, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finishAfterTransition();
            return true;
        } else if (item.getItemId() == R.id.action_toggle_torch) {
            this.toggleTorch();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Turn torch on/off and update flash icon in action bar
     * Show a toast to the user if there is no flash on the device
     */
    private void toggleTorch() {
        CameraInfo cameraInfo = this.camera.getCameraInfo();
        CameraControl cameraControl = this.camera.getCameraControl();
        if (cameraInfo.hasFlashUnit()) {
            LiveData<Integer> torchState = cameraInfo.getTorchState();
            if (torchState.getValue() != null) {
                Drawable flashIcon;
                if (torchState.getValue() == TorchState.OFF) {
                    cameraControl.enableTorch(true);
                    flashIcon = ContextCompat.getDrawable(this, R.drawable.ic_baseline_flash_on_24);
                } else {
                    cameraControl.enableTorch(false);
                    flashIcon = ContextCompat.getDrawable(this, R.drawable.ic_baseline_flash_off_24);
                }
                if (flashIcon != null) {
                    flashIcon.setColorFilter(this.getResources().getColor(android.R.color.white, this.getTheme()), PorterDuff.Mode.SRC_IN);
                    this.menu.getItem(0).setIcon(flashIcon);
                }
            }
        } else {
            Toast.makeText(this, R.string.error_no_flash_on_device, Toast.LENGTH_SHORT).show();
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
                        this.camera = cameraProviderFuture.get().bindToLifecycle(
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
                Toast.makeText(this, R.string.error_no_camera_permission_granted, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}