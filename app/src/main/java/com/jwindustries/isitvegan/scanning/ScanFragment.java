package com.jwindustries.isitvegan.scanning;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraInfo;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.util.concurrent.ListenableFuture;
import com.jwindustries.isitvegan.AdditiveIngredientAdapter;
import com.jwindustries.isitvegan.Ingredient;
import com.jwindustries.isitvegan.IngredientList;
import com.jwindustries.isitvegan.R;
import com.jwindustries.isitvegan.Utils;

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

public class ScanFragment extends Fragment implements BarcodeFoundListener, TextFoundListener {
    private Activity hostActivity;

    /*
     * Camera
     */
    private final ExecutorService cameraExecutor = Executors.newSingleThreadExecutor();
    private final int PERMISSION_REQUEST_CODE = 10;
    private final String[] REQUIRED_PERMISSIONS = { Manifest.permission.CAMERA };
    private ImageAnalyzer imageAnalyzer;
    private ImageAnalysis imageAnalysis;
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
    private View rootView;
    private AdditiveIngredientAdapter adapter;
    private RecyclerView recyclerView;
    private ViewSwitcher scanListContainer;
    private LinearLayoutManager layoutManager;

    private List<Ingredient> ingredientList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.hostActivity = getActivity();

        this.rootView = inflater.inflate(R.layout.fragment_scan, container, false);

        this.adapter = new AdditiveIngredientAdapter(hostActivity);

        this.layoutManager = new LinearLayoutManager(hostActivity, LinearLayoutManager.VERTICAL, false);
        this.recyclerView = rootView.findViewById(R.id.ingredient_view_2);
        this.recyclerView.setLayoutManager(this.layoutManager);
        this.recyclerView.setAdapter(this.adapter);
        this.scanListContainer = rootView.findViewById(R.id.outer_scan_list_container);
        Button clearButton = rootView.findViewById(R.id.clear_button);
        clearButton.setOnClickListener((v) -> clearList());

        this.imageAnalysis = new ImageAnalysis.Builder().setTargetAspectRatio(AspectRatio.RATIO_16_9).build();
        this.imageAnalyzer = new ImageAnalyzer(this, this);
        this.imageAnalysis.setAnalyzer(this.cameraExecutor, imageAnalyzer);

        this.ingredientList = IngredientList.getIngredientList(hostActivity);

        this.barcodeRequestQueue = Volley.newRequestQueue(hostActivity);
        this.requestedBarcodes = new ArrayList<>();
        this.handledBarcodes = new ArrayList<>();
        this.queuedMessages = new ArrayList<>();

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            requestPermission();
        }

        // Listen to parent to determine whether the fragment is in preview mode
        getParentFragmentManager().setFragmentResultListener(
                getString(R.string.key_fragment_result),
                this,
                (requestKey, bundle) -> {
                    this.setTorchEnabled(bundle.getBoolean(getString(R.string.key_bundle_is_torch_on)));
                    this.handlePreviewMode(bundle.getBoolean(getString(R.string.key_bundle_is_in_preview_mode)));
                }
        );

        return rootView;
    }

    private void handlePreviewMode(boolean isInPreviewMode) {
        this.imageAnalyzer.setEnabled(!isInPreviewMode);
        if (isInPreviewMode) {
            setTorchEnabled(false);
            clearList();
            if (scanListContainer.getCurrentView() == this.rootView.findViewById(R.id.inner_scan_list_container)) {
                scanListContainer.showNext();
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        this.barcodeRequestQueue.cancelAll(BARCODE_REQUEST_TAG);
    }

    /**
     * Turn torch on/off
     * Show a toast to the user if there is no flash on the device
     */
    private void setTorchEnabled(boolean enabled) {
        CameraInfo cameraInfo = this.camera.getCameraInfo();
        CameraControl cameraControl = this.camera.getCameraControl();
        if (cameraInfo.hasFlashUnit()) {
            cameraControl.enableTorch(enabled);
        } else {
            Toast.makeText(hostActivity, R.string.error_no_flash_on_device, Toast.LENGTH_SHORT).show();
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
                    response -> {
                        try {
                            boolean productExists = response.get("status").toString().equals("1");
                            if (productExists) {
                                JSONArray ingredientsJson = (JSONArray) ((JSONObject) response.get("product")).get("ingredients");
                                boolean areIngredientsFound = false;
                                for (int ingredientIndex = 0; ingredientIndex < ingredientsJson.length(); ingredientIndex++) {
                                    String ingredientText = ((JSONObject) ingredientsJson.get(ingredientIndex)).getString("text");
                                    List<Ingredient> foundIngredients = this.ingredientList
                                            .stream()
                                            .filter(ingredient -> ingredient.isContainedIn(ingredientText))
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
                    error -> {
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
                .filter(ingredient -> ingredient.isContainedIn(Utils.normalizeString(foundText, false)))
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
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(hostActivity);
        cameraProviderFuture.addListener(
                () -> {
                    Preview preview = new Preview.Builder().build();
                    preview.setSurfaceProvider(((PreviewView) hostActivity.findViewById(R.id.camera_preview_view)).getSurfaceProvider());
                    try {
                        cameraProviderFuture.get().unbindAll();
                        this.camera = cameraProviderFuture.get().bindToLifecycle(
                                this,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                imageAnalysis
                        );
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                },
                ContextCompat.getMainExecutor(hostActivity)
        );
    }

    public void clearList() {
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
        return ContextCompat.checkSelfPermission(hostActivity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(
                hostActivity,
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
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(hostActivity, R.string.error_no_camera_permission_granted, Toast.LENGTH_SHORT).show();
                // TODO
//                finish();
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

            Snackbar snackbar = Snackbar.make(hostActivity.findViewById(R.id.scan_root), message, Snackbar.LENGTH_SHORT);

            // Compensate for translucent navigation bar, as otherwise the snackbar will be shown behind it
            final FrameLayout snackBarView = (FrameLayout) snackbar.getView();
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) snackBarView.getChildAt(0).getLayoutParams();
            params.setMargins(params.leftMargin,
                    params.topMargin,
                    params.rightMargin,
                    params.bottomMargin + 80);
            snackBarView.getChildAt(0).setLayoutParams(params);

            snackbar
                    .setAction(R.string.snackbar_action_dismiss, view -> {
                        snackbar.dismiss();
                        this.queuedMessages.remove(tag);
                    });

            snackbar.show();
        }
    }
}