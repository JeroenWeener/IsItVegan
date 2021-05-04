package com.jwindustries.isitvegan.scanning;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.util.concurrent.ListenableFuture;
import com.jwindustries.isitvegan.AdditiveIngredientAdapter;
import com.jwindustries.isitvegan.Ingredient;
import com.jwindustries.isitvegan.IngredientList;
import com.jwindustries.isitvegan.R;
import com.jwindustries.isitvegan.Utils;
import com.jwindustries.isitvegan.activities.BaseActivity;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class ScanActivity extends BaseActivity implements BarcodeFoundListener, TextFoundListener {

    /*
     * Camera
     */
    private final ExecutorService cameraExecutor = Executors.newSingleThreadExecutor();
    private final int PERMISSION_REQUEST_CODE = 10;
    private final String[] REQUIRED_PERMISSIONS = { Manifest.permission.CAMERA };
    private ImageAnalysis imageAnalyzer;
    private Camera camera;

    /*
     * Barcode requests
     */
    private static final String OPEN_FOOD_FACTS_API_URL = "https://en.openfoodfacts.org/api/v0/product/";
    private static final String BARCODE_REQUEST_TAG = "BarcodeRequestTag";
    private RequestQueue barcodeRequestQueue;
    private List<String> requestedBarcodes;
    private List<String> handledBarcodes;
    private List<String> queuedMessages;

    /*
     * UI
     */
    private AdditiveIngredientAdapter adapter;
    private RecyclerView recyclerView;
    private ViewSwitcher scanListContainer;
    private LinearLayoutManager layoutManager;
    private Menu optionsMenu;

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
        this.imageAnalyzer.setAnalyzer(this.cameraExecutor, new ImageAnalyzer(this, this));

        this.ingredientList = IngredientList.getIngredientList(this);

        this.barcodeRequestQueue = Volley.newRequestQueue(this);
        this.requestedBarcodes = new ArrayList<>();
        this.handledBarcodes = new ArrayList<>();
        this.queuedMessages = new ArrayList<>();

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            requestPermission();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        this.barcodeRequestQueue.cancelAll(BARCODE_REQUEST_TAG);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.scan_actionbar_menu, menu);
        this.optionsMenu = menu;
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
                if (torchState.getValue() == TorchState.OFF) {
                    cameraControl.enableTorch(true);
                    this.optionsMenu.getItem(0).setIcon(ContextCompat.getDrawable(this, R.drawable.flash_on_white));
                } else {
                    cameraControl.enableTorch(false);
                    this.optionsMenu.getItem(0).setIcon(ContextCompat.getDrawable(this, R.drawable.flash_off_white));
                }
            }
        } else {
            Toast.makeText(this, R.string.error_no_flash_on_device, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBarcodeFound(String barcode) {
        if (!this.handledBarcodes.contains(barcode) && !this.requestedBarcodes.contains(barcode)) {
            this.requestedBarcodes.add(barcode);

            JsonObjectRequest barcodeRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    OPEN_FOOD_FACTS_API_URL + barcode + ".json",
                    null,
                    (Response.Listener<JSONObject>) response -> {
                        try {
                            boolean productExists = response.get("status").toString().equals("1");
                            if (productExists) {
                                JSONArray ingredientsJson = (JSONArray) ((JSONObject) response.get("product")).get("ingredients");
                                boolean areIngredientsFound = false;
                                for (int ingredientIndex = 0; ingredientIndex < ingredientsJson.length(); ingredientIndex++) {
                                    String ingredientText = ((JSONObject) ingredientsJson.get(ingredientIndex)).getString("text");
                                    List<Ingredient> foundIngredients = this.ingredientList
                                            .stream()
                                            .filter(ingredient -> Utils.isIngredientInText(ingredient, ingredientText))
                                            .collect(Collectors.toList());
                                    this.addIngredients(foundIngredients);
                                    areIngredientsFound = areIngredientsFound || foundIngredients.size() > 0;
                                }

                                if (areIngredientsFound) {
                                    this.createSnackbar(this.getString(R.string.message_scan_barcode_success) + ' ' + barcode, barcode);
                                } else {
                                    this.createSnackbar(this.getString(R.string.message_scan_barcode_success_no_ingredients) + ' ' + barcode, barcode);
                                }
                            } else {
                                this.createSnackbar(this.getString(R.string.message_scan_barcode_no_info) + ' ' + barcode, barcode);
                            }

                            this.requestedBarcodes.remove(barcode);
                            this.handledBarcodes.add(barcode);

                        } catch (JSONException e) {
                            this.requestedBarcodes.remove(barcode);
                            this.createSnackbar(this.getString(R.string.message_scan_barcode_error) + ' ' + barcode, barcode);
                            e.printStackTrace();
                        }
                    },
                    (Response.ErrorListener) error -> {
                        this.requestedBarcodes.remove(barcode);
                        this.createSnackbar(this.getString(R.string.message_scan_barcode_error) + ' ' + barcode, barcode);
                        error.printStackTrace();
                    });

            barcodeRequest.setTag(BARCODE_REQUEST_TAG);
            this.barcodeRequestQueue.add(barcodeRequest);

        } else if (handledBarcodes.contains(barcode)) {
            this.createSnackbar(barcode + ' ' + this.getString(R.string.message_scan_barcode_already_scanned), barcode);
        }
    }

    @Override
    public void onTextFound(String foundText) {
        List<Ingredient> foundIngredients = this.ingredientList
                .stream()
                .filter(ingredient -> Utils.isIngredientInText(ingredient, Utils.normalizeString(foundText, false)))
                .collect(Collectors.toList());
        this.addIngredients(foundIngredients);
    }

    private void addIngredients(List<Ingredient> ingredients) {
        int numberAdded = this.adapter.addIngredients(ingredients);
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
        this.requestedBarcodes.clear();
        this.handledBarcodes.clear();
        this.queuedMessages.clear();
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

    /**
     * Show a snackbar with specified message
     *
     * If a snackbar with given tag is already displayed, do not show a new one
     * Tags are considered again after a 2 second countdown
     *
     * @param message the message displayed in the snackbar
     * @param tag tag used for distinguishing between snackbars
     */
    private void createSnackbar(String message, String tag) {
        if (!this.queuedMessages.contains(tag)) {
            this.queuedMessages.add(tag);
            new Handler(Looper.getMainLooper()).postDelayed(() -> this.queuedMessages.remove(tag), 2000);

            Snackbar snackbar = Snackbar.make(this.findViewById(R.id.scan_root), message, Snackbar.LENGTH_SHORT);
            snackbar
                    .setAction(R.string.snackbar_action_dismiss, view -> {
                        snackbar.dismiss();
                        this.queuedMessages.remove(tag);
                    })
                    .show();
        }
    }
}