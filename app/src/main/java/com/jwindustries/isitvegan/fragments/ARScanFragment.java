package com.jwindustries.isitvegan.fragments;

import android.graphics.Rect;
import android.graphics.RectF;
import android.media.Image;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
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
import com.google.ar.core.InstantPlacementPoint;
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
    private TextRecognizer textRecognizer;
    private boolean textRecognizerReady = true;

    private static final String MESSAGE_SEARCHING = "Searching for product...";

    private static final float Z_NEAR = .1f;
    private static final float Z_FAR = 100f;

    private GLSurfaceView surfaceView;

    private boolean installRequested;
    private boolean isInPreviewMode;

    private Session session;
    private final SnackbarHelper messageSnackbarHelper = new SnackbarHelper();
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
    private static final float APPROXIMATE_DISTANCE_METERS = .2f;

    private Shader labelShader;

    private final List<Anchor> anchors = new ArrayList<>();

    // Temporary matrix allocated here to reduce number of allocations for each frame.
    private final float[] modelMatrix = new float[16];
    private final float[] viewMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];
    private final float[] modelViewMatrix = new float[16]; // view x model
    private final float[] modelViewProjectionMatrix = new float[16]; // projection x view x model

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_ar_scan, container, false);

        surfaceView = rootView.findViewById(R.id.surface_view);
        tapHelper = new TapHelper(/*context=*/ this.getContext());
        surfaceView.setOnTouchListener(tapHelper);
        render = new Render(surfaceView, this, getActivity().getAssets());

        displayRotationHelper = new DisplayRotationHelper(/*context=*/ getContext());
        trackingStateHelper = new TrackingStateHelper(/*context=*/ getActivity());
        boundingBoxHelper = new BoundingBoxHelper();
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        installRequested = false;
        isInPreviewMode = true;

        // Listen to parent to determine whether the fragment is in preview mode
        getParentFragmentManager().setFragmentResultListener(
                "requestKey",
                this,
                (requestKey, bundle) -> isInPreviewMode = bundle.getBoolean("isInPreviewMode")
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
                switch (ArCoreApk.getInstance().requestInstall(getActivity(), !installRequested)) {
                    case INSTALL_REQUESTED:
                        installRequested = true;
                        return;
                    case INSTALLED:
                        break;
                }

                // ARCore requires camera permissions to operate. If we did not yet obtain runtime
                // permission on Android M and above, now is a good time to ask the user for it.
                if (!CameraPermissionHelper.hasCameraPermission(getActivity())) {
                    CameraPermissionHelper.requestCameraPermission(getActivity());
                    return;
                }

                session = new Session(/* context= */ getContext());
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
                messageSnackbarHelper.showError(getActivity(), message);
                return;
            }
        }

        try {
            session.resume();
        } catch (CameraNotAvailableException e) {
            messageSnackbarHelper.showError(getActivity(), "Camera not available. Try restarting the app.");
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
        if (!CameraPermissionHelper.hasCameraPermission(getActivity())) {
            // Use toast instead of snackbar here since the activity will exit.
            Toast
                    .makeText(getContext(), "Camera permission is needed to run this application", Toast.LENGTH_LONG)
                    .show();
            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(getActivity())) {
                // Permission denied with checking "Do not ask again".
                CameraPermissionHelper.launchPermissionSettings(getActivity());
            }
            getActivity().finish();
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

            labelShader = Shader
                    .createFromAssets(render, "shaders/rectangle.vert", "shaders/rectangle.frag")
                    .setVec4("u_Color", new float[]{255.0f / 255.0f, 136.0f / 255.0f, 0.0f / 255.0f, 0.8f});
        } catch (IOException e) {
            messageSnackbarHelper.showError(getActivity(), "Failed to read a required asset file: " + e);
        }
    }

    @Override
    public void onSurfaceChanged(Render render, int width, int height) {
        displayRotationHelper.onSurfaceChanged(width, height);
        virtualSceneFramebuffer.resize(width, height);
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
            messageSnackbarHelper.showError(getActivity(), "Camera not available. Try restarting the app.");
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
            messageSnackbarHelper.hide(getActivity());
        } else {
            messageSnackbarHelper.showMessage(getActivity(), message);
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

        if (anchors.size() == 4) {
            Anchor anchorA = anchors.get(0);
            Anchor anchorB = anchors.get(1);
            Anchor anchorC = anchors.get(2);
            Anchor anchorD = anchors.get(3);

            if (anchorA.getTrackingState() == TrackingState.TRACKING) {
                float[] pointA = anchorA.getPose().getTranslation();
//                float[] pointB = new float[]{anchorA.getPose().getTranslation()[0] + .01f, anchorA.getPose().getTranslation()[1], anchorA.getPose().getTranslation()[2] + .005f};
//                float[] pointC = new float[]{anchorD.getPose().getTranslation()[0] + .01f, anchorD.getPose().getTranslation()[1], anchorD.getPose().getTranslation()[2] - .005f};
                float[] pointD = anchorD.getPose().getTranslation();

//                float[] pointA = new float[]{anchorB.getPose().getTranslation()[0] - .01f, anchorB.getPose().getTranslation()[1], anchorB.getPose().getTranslation()[2] + .005f};
                float[] pointB = anchorB.getPose().getTranslation();
                float[] pointC = anchorC.getPose().getTranslation();
//                float[] pointD = new float[]{anchorC.getPose().getTranslation()[0] - .01f, anchorC.getPose().getTranslation()[1], anchorC.getPose().getTranslation()[2] - .005f};

                // 0: width
                // 1: depth
                // 2: height

                // Ingredient labels
                Mesh labelMesh = Mesh.createRectangleFromPoints(pointA, pointB, pointC, pointD);
                labelShader.setMat4("u_ModelViewProjection", modelViewProjectionMatrix);
                render.draw(labelMesh, labelShader, virtualSceneFramebuffer);
            }
        }

        // Compose the virtual scene with the background.
        backgroundRenderer.drawVirtualScene(render, virtualSceneFramebuffer);
    }

    private void handleBoundingBox(Frame frame) {
        RectF boundingBox = boundingBoxHelper.poll();
        if (boundingBox == null) {
            return;
        }

        if (anchors.size() >= 4) {
            return;
        }

        float[][] cpuCoordinateLists = new float[][]{
                new float[]{boundingBox.top, boundingBox.left},
                new float[]{boundingBox.top, boundingBox.right},
                new float[]{boundingBox.bottom, boundingBox.right},
                new float[]{boundingBox.bottom, boundingBox.left},
        };
        float[] viewCoordinates = new float[2];
        List<Anchor> newAnchors = new ArrayList<>();
        for (float[] cpuCoordinates : cpuCoordinateLists) {
            frame.transformCoordinates2d(Coordinates2d.IMAGE_PIXELS, cpuCoordinates, Coordinates2d.VIEW, viewCoordinates);
            List<HitResult> hits = frame.hitTestInstantPlacement(viewCoordinates[0], viewCoordinates[1], APPROXIMATE_DISTANCE_METERS);
            for (HitResult hit : hits) {
                if (hit.getTrackable() instanceof InstantPlacementPoint) {
                    newAnchors.add(hit.createAnchor());
                    break;
                }
            }
        }

        if (newAnchors.size() == 4) {
            anchors.addAll(newAnchors);
        }
    }

    // Handle only one tap per frame, as taps are usually low frequency compared to frame rate.
    private void handleTap() {
        MotionEvent tap = tapHelper.poll();
        if (tap != null) {
            anchors.clear();
        }
    }

    private void recognizeText(Image image) {
        textRecognizerReady = false;
        int rotation = displayRotationHelper.getCameraSensorToDisplayRotation(session.getCameraConfig().getCameraId());
        InputImage inputImage = InputImage.fromMediaImage(image, rotation);
        ContextCompat.getMainExecutor(getContext()).execute(() -> textRecognizer.process(inputImage)
                .addOnCompleteListener(result -> {
                    image.close();
                    textRecognizerReady = true;
                })
                .addOnSuccessListener(result -> {
                    List<Text.TextBlock> blocks = result.getTextBlocks();

                    if (blocks.size() > 0) {
                        RectF resultRect = new RectF(blocks
                                .get(0)
                                .getLines()
                                .get(0)
                                .getElements()
                                .get(0)
                                .getBoundingBox()
                        );
                        for (Text.TextBlock block : blocks) {
                            List<Text.Line> lines = block.getLines();
                            for (Text.Line line : lines) {
                                List<Text.Element> elements = line.getElements();
                                for (Text.Element element : elements) {
                                    Rect boundingBox = element.getBoundingBox();
                                    if (boundingBox == null) return;
                                    if (boundingBox.left < resultRect.left)
                                        resultRect.left = boundingBox.left;
                                    if (boundingBox.right > resultRect.right)
                                        resultRect.right = boundingBox.right;
                                    if (boundingBox.top < resultRect.top)
                                        resultRect.top = boundingBox.top;
                                    if (boundingBox.bottom > resultRect.bottom)
                                        resultRect.bottom = boundingBox.bottom;
                                }
                            }
                        }
                        boundingBoxHelper.add(resultRect);
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