package com.jwindustries.isitvegan.fragments;

/**
 * Detecting vertical planes does not work for moving objects (show HelloAR)
 * HelloAR allows you to tap spots to track, this seems to work reasonably well. Why does it not
 * work for recognized text?
 *
 * Use old technique?
 *
 * Currently:
 * - tracking middle of product with a single anchor
 * - only update position if distance is within expected bounds
 */

import android.app.Activity;
import android.graphics.Point;
import android.media.Image;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.ar.core.Anchor;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Config.InstantPlacementMode;
import com.google.ar.core.Coordinates2d;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingFailureReason;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.NotYetAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.jwindustries.isitvegan.R;
import com.jwindustries.isitvegan.graphics.BackgroundRenderer;
import com.jwindustries.isitvegan.graphics.Framebuffer;
import com.jwindustries.isitvegan.graphics.Mesh;
import com.jwindustries.isitvegan.graphics.Render;
import com.jwindustries.isitvegan.graphics.Shader;
import com.jwindustries.isitvegan.helpers.BoundingBoxHelper;
import com.jwindustries.isitvegan.helpers.CameraPermissionHelper;
import com.jwindustries.isitvegan.helpers.DisplayRotationHelper;
import com.jwindustries.isitvegan.helpers.SnackbarHelper;
import com.jwindustries.isitvegan.helpers.TapHelper;
import com.jwindustries.isitvegan.helpers.TrackingStateHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ARScanFragment extends Fragment implements Render.Renderer {
    private Activity hostActivity;

    private TextRecognizer textRecognizer;
    private boolean textRecognizerReady = true;

    private static final String MESSAGE_SEARCHING = "Searching for product...";

    private static final float Z_NEAR = .1f;
    private static final float Z_FAR = 100f;

    private GLSurfaceView surfaceView;

    private boolean installRequested;
    private boolean isInPreviewMode;

    private Session session;
    private SnackbarHelper messageSnackbarHelper;
    private DisplayRotationHelper displayRotationHelper;
    private TrackingStateHelper trackingStateHelper;
    private TapHelper tapHelper;
    private BoundingBoxHelper boundingBoxHelper;
    private Render render;

    private BackgroundRenderer backgroundRenderer;
    private Framebuffer virtualSceneFramebuffer;
    private boolean hasSetTextureNames = false;

    // Assumed distance from the device camera to the surface on which user will try to place objects.
    // This value affects the apparent scale of objects while the tracking method of the
    // Instant Placement point is SCREENSPACE_WITH_APPROXIMATE_DISTANCE.
    // Values in the [0.2, 2.0] meter range are a good choice for most AR experiences. Use lower
    // values for AR experiences where users are expected to place objects on surfaces close to the
    // camera. Use larger values for experiences where the user will likely be standing and trying to
    // place an object on the ground or floor in front of them.
    private static final float APPROXIMATE_DISTANCE_METERS = .15f;

    private Shader productShader;
    private Shader labelShader;

    private final List<Anchor> anchors = new ArrayList<>();
    private float[] previousTranslation;

    // Temporary matrix allocated here to reduce number of allocations for each frame.
    private final float[] modelMatrix = new float[16];
    private final float[] viewMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];
    private final float[] modelViewMatrix = new float[16]; // view x model
    private final float[] modelViewProjectionMatrix = new float[16]; // projection x view x model

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        hostActivity = getActivity();

        View rootView = inflater.inflate(R.layout.fragment_ar_scan, container, false);

        surfaceView = rootView.findViewById(R.id.surface_view);
        tapHelper = new TapHelper(/*context=*/ hostActivity);
        surfaceView.setOnTouchListener(tapHelper);
        render = new Render(surfaceView, this, hostActivity.getAssets());

        displayRotationHelper = new DisplayRotationHelper(/*context=*/ hostActivity);
        trackingStateHelper = new TrackingStateHelper(/*context=*/ hostActivity);
        messageSnackbarHelper = new SnackbarHelper();
        boundingBoxHelper = new BoundingBoxHelper();
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        installRequested = false;
        isInPreviewMode = true;

        // Listen to parent to determine whether the fragment is in preview mode
        getParentFragmentManager().setFragmentResultListener(
                getString(R.string.key_fragment_result),
                this,
                (requestKey, bundle) -> isInPreviewMode = bundle.getBoolean(getString(R.string.key_bundle_is_in_preview_mode))
        );

        return rootView;
    }

    @Override
    public void onDestroy() {
        if (session != null) {
            session.close();
            session = null;
        }
        textRecognizer.close();

        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (session == null) {
            String message = null;
            try {
                switch (ArCoreApk.getInstance().requestInstall(hostActivity, !installRequested)) {
                    case INSTALL_REQUESTED:
                        installRequested = true;
                        return;
                    case INSTALLED:
                        break;
                }

                // ARCore requires camera permissions to operate. If we did not yet obtain runtime
                // permission on Android M and above, now is a good time to ask the user for it.
                if (!CameraPermissionHelper.hasCameraPermission(hostActivity)) {
                    CameraPermissionHelper.requestCameraPermission(hostActivity);
                    return;
                }

                session = new Session(/* context= */ hostActivity);
            } catch (UnavailableArcoreNotInstalledException | UnavailableUserDeclinedInstallationException e) {
                message = "Please install ARCore";
            } catch (UnavailableApkTooOldException e) {
                message = "Please update ARCore";
            } catch (UnavailableSdkTooOldException e) {
                message = "Please update this app";
            } catch (UnavailableDeviceNotCompatibleException e) {
                message = "This device does not support AR";
            } catch (Exception e) {
                message = "Failed to create AR session";
            }

            if (message != null) {
                messageSnackbarHelper.showMessage(hostActivity, message);
                return;
            }
        }

        try {
            session.resume();
        } catch (CameraNotAvailableException e) {
            messageSnackbarHelper.showMessage(hostActivity, "Camera not available. Try restarting the app.");
            session = null;
            return;
        }

        surfaceView.onResume();
        displayRotationHelper.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (session != null) {
            // Note that the order matters - GLSurfaceView is paused first so that it does not try
            // to query the session. If Session is paused before GLSurfaceView, GLSurfaceView may
            // still call session.update() and get a SessionPausedException.
            displayRotationHelper.onPause();
            surfaceView.onPause();
            session.pause();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (!CameraPermissionHelper.hasCameraPermission(hostActivity)) {
            // Use toast instead of snackbar here since the activity will exit.
            Toast
                    .makeText(hostActivity, "Camera permission is needed to run this application", Toast.LENGTH_LONG)
                    .show();
            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(hostActivity)) {
                // Permission denied with checking "Do not ask again".
                CameraPermissionHelper.launchPermissionSettings(hostActivity);
            }
            hostActivity.finish();
        }
    }

    @Override
    public void onSurfaceCreated(Render render) {
        configureSession();

        // Prepare the rendering objects. This involves reading shaders and 3D model files, so may throw
        // an IOException.
        try {
            backgroundRenderer = new BackgroundRenderer(render);
            virtualSceneFramebuffer = new Framebuffer(/*width=*/ 1, /*height=*/ 1);

            productShader = Shader
                    .createFromAssets(render, "shaders/rectangle.vert", "shaders/rectangle.frag")
                    .setVec4("u_Color", new float[]{255.0f / 255.0f, 136.0f / 255.0f, 0.0f / 255.0f, 0.8f});
            labelShader = Shader
                    .createFromAssets(render, "shaders/rectangle.vert", "shaders/rectangle.frag")
                    .setVec4("u_Color", new float[]{0.0f / 255.0f, 170.0f / 255.0f, 86.0f / 255.0f, 0.8f});
        } catch (IOException e) {
            messageSnackbarHelper.showMessage(hostActivity, "Failed to read a required asset file: " + e);
        }
    }

    @Override
    public void onSurfaceChanged(Render render, int width, int height) {
        displayRotationHelper.onSurfaceChanged(width, height);
        virtualSceneFramebuffer.resize(width, height);
    }

    private float distance(float[] a, float[] b) {
        if (a == null || b == null) {
            return 0;
        }
        return (a[0] - b[0]) * (a[0] - b[0]) + (a[1] - b[1]) * (a[1] - b[1]) + (a[2] - b[2]) * (a[2] - b[2]);
    }

    @Override
    public void onDrawFrame(Render render) {
        // Texture names should only be set once on a GL thread unless they change. This is done during
        // onDrawFrame rather than onSurfaceCreated since the session is not guaranteed to have been
        // initialized during the execution of onSurfaceCreated.
        if (!hasSetTextureNames) {
            session.setCameraTextureNames(new int[]{backgroundRenderer.getCameraColorTexture().getTextureId()});
            hasSetTextureNames = true;
        }

        // -- Update per-frame state

        // Notify ARCore session that the view size changed so that the perspective matrix and
        // the video background can be properly adjusted.
        displayRotationHelper.updateSessionIfNeeded(session);

        // Obtain the current frame from ARSession. When the configuration is set to
        // UpdateMode.BLOCKING (it is by default), this will throttle the rendering to the
        // camera framerate.
        Frame frame;
        try {
            frame = session.update();
        } catch (CameraNotAvailableException e) {
            messageSnackbarHelper.showMessage(hostActivity, "Camera not available. Try restarting the app.");
            return;
        }

        Camera camera = frame.getCamera();

        // BackgroundRenderer.updateDisplayGeometry must be called every frame to update the coordinates
        // used to draw the background camera image.
        backgroundRenderer.updateDisplayGeometry(frame);

        // -- Draw background
        if (frame.getTimestamp() != 0) {
            // Suppress rendering if the camera did not produce the first frame yet. This is to avoid
            // drawing possible leftover data from previous sessions if the texture is reused.
            backgroundRenderer.drawBackground(render);
        }

        // If we are in preview mode, we do not need to do anything else
        if (isInPreviewMode) return;

        // Handle one tap per frame.
        handleTap();
        handleBoundingBox(frame);

        // Detect text
        if (textRecognizerReady) {
            try {
                Image image = frame.acquireCameraImage();
                recognizeText(image);
            } catch (NotYetAvailableException e) {
                // The camera is not ready yet
            }
        }

        // Keep the screen unlocked while tracking, but allow it to lock when tracking stops.
        trackingStateHelper.updateKeepScreenOnFlag(camera.getTrackingState());

        // Show a message based on whether tracking has failed, if planes are detected, and if the user
        // has placed any objects.
        String message = null;
        if (camera.getTrackingState() == TrackingState.PAUSED) {
            if (camera.getTrackingFailureReason() == TrackingFailureReason.NONE) {
                message = MESSAGE_SEARCHING;
            } else {
                message = TrackingStateHelper.getTrackingFailureReasonString(camera);
            }
        } else if (anchors.size() == 0) {
            message = MESSAGE_SEARCHING;
        }
        if (message == null) {
            messageSnackbarHelper.hide(hostActivity);
        } else {
            // TODO enable once main UI scrolling is implemented
//            messageSnackbarHelper.showMessage(hostActivity, message);
        }

        // If not tracking, don't draw 3D objects.
        if (camera.getTrackingState() == TrackingState.PAUSED) {
            return;
        }

        // -- Draw non-occluded virtual objects (planes, point cloud)

        // Get projection matrix.
        camera.getProjectionMatrix(projectionMatrix, 0, Z_NEAR, Z_FAR);

        // Get camera matrix and draw.
        camera.getViewMatrix(viewMatrix, 0);

        Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

        // -- Draw occluded virtual objects

        // Visualize anchors created by touch.
        render.clear(virtualSceneFramebuffer, 0f, 0f, 0f, 0f);

        if (anchors.size() >= 1) {
            if (camera.getTrackingState() == TrackingState.TRACKING) {
                float[] point = anchors.get(0).getPose().getTranslation();

                float distance = distance(previousTranslation, point);
                Log.d("TREST4", String.valueOf(distance));
                if (distance > 1e-6) {
                    point = previousTranslation;
                }
                previousTranslation = point;

                float[] pointA = new float[]{point[0] - .01f, point[1], point[2] - .01f};
                float[] pointB = new float[]{point[0] + .01f, point[1], point[2] - .01f};
                float[] pointC = new float[]{point[0] + .01f, point[1], point[2] + .01f};
                float[] pointD = new float[]{point[0] - .01f, point[1], point[2] + .01f};

                // 0: width
                // 1: depth
                // 2: height

                // Ingredient labels
                Mesh productMesh = Mesh.createRectangleFromPoints(pointA, pointB, pointC, pointD);
                productShader.setMat4("u_ModelViewProjection", modelViewProjectionMatrix);
                render.draw(productMesh, productShader, virtualSceneFramebuffer);
            }
        }

        // Compose the virtual scene with the background.
        backgroundRenderer.drawVirtualScene(render, virtualSceneFramebuffer);
    }

    private void handleBoundingBox(Frame frame) {
        Point[] points = boundingBoxHelper.poll();
        if (points == null) {
            return;
        }

        if (anchors.size() >= 1) {
            return;
        }

        float[] viewCoordinates = new float[2];
        List<Anchor> newAnchors = new ArrayList<>();
        for (Point point : points) {
            float[] cpuCoordinates = new float[]{point.y, point.x};
            frame.transformCoordinates2d(Coordinates2d.IMAGE_PIXELS, cpuCoordinates, Coordinates2d.VIEW, viewCoordinates);
            List<HitResult> hits = frame.hitTestInstantPlacement(viewCoordinates[0], viewCoordinates[1], APPROXIMATE_DISTANCE_METERS);
            if (hits.size() > 0) {
                Log.d("TREST", "Distance: " + hits.get(0).getDistance());
                Log.d("TREST", "X: " + hits.get(0).getHitPose().getTranslation()[0]);
                Log.d("TREST", "Y: " + hits.get(0).getHitPose().getTranslation()[1]);
                Log.d("TREST", "Z: " + hits.get(0).getHitPose().getTranslation()[2]);
                newAnchors.add(hits.get(0).createAnchor());
            }
        }

        if (newAnchors.size() == 1) {
            anchors.addAll(newAnchors);
        }
    }

    // Handle only one tap per frame, as taps are usually low frequency compared to frame rate.
    private void handleTap() {
        MotionEvent tap = tapHelper.poll();
        if (tap != null) {
            anchors.forEach(Anchor::detach);
            anchors.clear();
            previousTranslation = null;
        }
    }

    private void recognizeText(Image image) {
        textRecognizerReady = false;
        int rotation = displayRotationHelper.getCameraSensorToDisplayRotation(session.getCameraConfig().getCameraId());
        InputImage inputImage = InputImage.fromMediaImage(image, rotation);
        int imageHeight = inputImage.getHeight();
        ContextCompat.getMainExecutor(hostActivity).execute(() -> textRecognizer.process(inputImage)
                .addOnCompleteListener(result -> {
                    image.close();
                    textRecognizerReady = true;
                })
                .addOnSuccessListener(result -> {
                    List<Text.TextBlock> blocks = result.getTextBlocks();
                    if (blocks.size() > 0) {
                        int topLeftX = blocks.get(0).getCornerPoints()[0].x;
                        int topLeftY = blocks.get(0).getCornerPoints()[0].y;
                        int topRightX = blocks.get(0).getCornerPoints()[1].x;
                        int topRightY = blocks.get(0).getCornerPoints()[1].y;
                        int bottomRightX = blocks.get(0).getCornerPoints()[2].x;
                        int bottomRightY = blocks.get(0).getCornerPoints()[2].y;
                        int bottomLeftX = blocks.get(0).getCornerPoints()[3].x;
                        int bottomLeftY = blocks.get(0).getCornerPoints()[3].y;

                        for (int i = 0; i < blocks.size(); i++) {
                            Point[] points = blocks.get(i).getCornerPoints();
                            Point topLeft = points[0];
                            Point topRight = points[1];
                            Point bottomRight = points[2];
                            Point bottomLeft = points[3];
                            if (topLeft.x < topLeftX) topLeftX = topLeft.x;
                            if (topLeft.y < topLeftY) topLeftY = topLeft.y;
                            if (topRight.x > topRightX) topRightX = topRight.x;
                            if (topRight.y < topRightY) topRightY = topRight.y;
                            if (bottomRight.x > bottomRightX) bottomRightX = bottomRight.x;
                            if (bottomRight.y > bottomRightY) bottomRightY = bottomRight.y;
                            if (bottomLeft.x < bottomLeftX) bottomLeftX = bottomLeft.x;
                            if (bottomLeft.y > bottomLeftY) bottomLeftY = bottomLeft.y;

                            for (Text.Line line : blocks.get(i).getLines()) {
                                for (Text.Element element : line.getElements()) {
                                    if (element.getText().toLowerCase().equals("melk")) {
                                        boundingBoxHelper.addKeyword(element.getCornerPoints());
                                    }
                                }
                            }
                        }
                        Point topLeft = new Point(imageHeight - topLeftX, topLeftY);
                        Point topRight = new Point(imageHeight - topRightX, topRightY);
                        Point bottomRight = new Point(imageHeight - bottomRightX, bottomRightY);
                        Point bottomLeft = new Point(imageHeight - bottomLeftX, bottomLeftY);
                        Point midPoint = new Point((topLeft.x + topRight.x + bottomRight.x + bottomLeft.x) / 4, (topLeft.y + topRight.y + bottomRight.y + bottomLeft.y) / 4);
                        boundingBoxHelper.add(new Point[]{midPoint});
                    } else {
                        boundingBoxHelper.add(null);
                    }
                }));
    }

    private void configureSession() {
        Config config = session.getConfig();

        config.setCloudAnchorMode(Config.CloudAnchorMode.DISABLED);
        config.setDepthMode(Config.DepthMode.DISABLED);
        config.setFocusMode(Config.FocusMode.AUTO);
        config.setInstantPlacementMode(InstantPlacementMode.LOCAL_Y_UP);
        config.setLightEstimationMode(Config.LightEstimationMode.DISABLED);
        config.setPlaneFindingMode(Config.PlaneFindingMode.DISABLED);
        config.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE);

        session.configure(config);
    }
}