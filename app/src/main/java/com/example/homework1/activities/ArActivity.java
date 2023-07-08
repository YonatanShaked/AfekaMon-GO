package com.example.homework1.activities;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.media.Image;
import android.media.MediaPlayer;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.homework1.R;
import com.example.homework1.interfaces.Constants;
import com.example.homework1.models.WrappedAnchor;
import com.google.ar.core.Anchor;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Config.InstantPlacementMode;
import com.google.ar.core.DepthPoint;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.InstantPlacementPoint;
import com.google.ar.core.LightEstimate;
import com.google.ar.core.Plane;
import com.google.ar.core.Point;
import com.google.ar.core.Point.OrientationMode;
import com.google.ar.core.PointCloud;
import com.google.ar.core.Session;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingFailureReason;
import com.google.ar.core.TrackingState;
import com.google.ar.core.examples.java.common.helpers.CameraPermissionHelper;
import com.google.ar.core.examples.java.common.helpers.DepthSettings;
import com.google.ar.core.examples.java.common.helpers.DisplayRotationHelper;
import com.google.ar.core.examples.java.common.helpers.FullScreenHelper;
import com.google.ar.core.examples.java.common.helpers.InstantPlacementSettings;
import com.google.ar.core.examples.java.common.helpers.SnackbarHelper;
import com.google.ar.core.examples.java.common.helpers.TapHelper;
import com.google.ar.core.examples.java.common.helpers.TrackingStateHelper;
import com.google.ar.core.examples.java.common.samplerender.Framebuffer;
import com.google.ar.core.examples.java.common.samplerender.GLError;
import com.google.ar.core.examples.java.common.samplerender.Mesh;
import com.google.ar.core.examples.java.common.samplerender.SampleRender;
import com.google.ar.core.examples.java.common.samplerender.Shader;
import com.google.ar.core.examples.java.common.samplerender.Texture;
import com.google.ar.core.examples.java.common.samplerender.VertexBuffer;
import com.google.ar.core.examples.java.common.samplerender.arcore.BackgroundRenderer;
import com.google.ar.core.examples.java.common.samplerender.arcore.PlaneRenderer;
import com.google.ar.core.examples.java.common.samplerender.arcore.SpecularCubemapFilter;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.NotYetAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class ArActivity extends AppCompatActivity implements SampleRender.Renderer {
    private static final float[] sphericalHarmonicFactors = {0.282095f, -0.325735f, 0.325735f, -0.325735f, 0.273137f, -0.273137f, 0.078848f, -0.273137f, 0.136569f};
    private static final String TAG = ArActivity.class.getSimpleName();
    private static final String SEARCHING_PLANE_MESSAGE = "Searching for surfaces...";
    private static final String WAITING_FOR_TAP_MESSAGE = "Tap on a surface to place an object.";
    private static final float Z_NEAR = 0.1f;
    private static final float Z_FAR = 100f;
    private static final int CUBEMAP_RESOLUTION = 16;
    private static final int CUBEMAP_NUMBER_OF_IMPORTANCE_SAMPLES = 32;
    private GLSurfaceView surfaceView;
    private boolean installRequested;
    private Session session;
    private final SnackbarHelper messageSnackbarHelper = new SnackbarHelper();
    private DisplayRotationHelper displayRotationHelper;
    private final TrackingStateHelper trackingStateHelper = new TrackingStateHelper(this);
    private TapHelper tapHelper;
    private PlaneRenderer planeRenderer;
    private BackgroundRenderer backgroundRenderer;
    private Framebuffer virtualSceneFramebuffer;
    private boolean hasSetTextureNames = false;
    private final DepthSettings depthSettings = new DepthSettings();
    private final boolean[] depthSettingsMenuDialogCheckboxes = new boolean[2];
    private final InstantPlacementSettings instantPlacementSettings = new InstantPlacementSettings();
    private final boolean[] instantPlacementSettingsMenuDialogCheckboxes = new boolean[1];
    private static final float APPROXIMATE_DISTANCE_METERS = 2.0f;
    private VertexBuffer pointCloudVertexBuffer;
    private Mesh pointCloudMesh;
    private Shader pointCloudShader;
    private long lastPointCloudTimestamp = 0;
    private Mesh virtualObjectMesh;
    private Mesh virtualAfekaBallMesh;
    private Shader virtualObjectShader;
    private Shader virtualObjectAfekaBallShader;
    private Texture virtualObjectAlbedoTexture;
    private Texture virtualObjectAlbedoAfekaBallTexture;
    private Texture virtualObjectAlbedoInstantPlacementTexture;
    private Texture virtualObjectAlbedoInstantPlacementAfekaBallTexture;
    private final List<WrappedAnchor> wrappedAnchors = new ArrayList<>();
    private SpecularCubemapFilter cubemapFilter;
    private final float[] modelMatrix = new float[16];
    private final float[] viewMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];
    private final float[] modelViewMatrix = new float[16]; // view x model
    private final float[] modelViewProjectionMatrix = new float[16]; // projection x view x model
    private final float[] sphericalHarmonicsCoefficients = new float[9 * 3];
    private final float[] viewInverseMatrix = new float[16];
    private final float[] worldLightDirection = {0.0f, 0.0f, 0.0f, 0.0f};
    private final float[] viewLightDirection = new float[4]; // view x world light direction
    private MediaPlayer mediaPlayer;
    private MediaPlayer mediaPlayerBall;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);
        surfaceView = findViewById(R.id.surfaceview);
        displayRotationHelper = new DisplayRotationHelper(this);

        tapHelper = new TapHelper(this);
        surfaceView.setOnTouchListener(tapHelper);

        SampleRender render = new SampleRender(surfaceView, this, getAssets());
        Log.d(TAG, "onCreate - render: " + render);

        installRequested = false;

        depthSettings.onCreate(this);
        instantPlacementSettings.onCreate(this);
        ImageButton settingsButton = findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(ArActivity.this, v);
            popup.setOnMenuItemClickListener(ArActivity.this::settingsMenuClick);
            popup.inflate(R.menu.settings_menu);
            popup.show();
        });

        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.battle);
        mediaPlayer.setLooping(true);
        mediaPlayer.setVolume(50, 50);
        mediaPlayer.start();

        mediaPlayerBall = MediaPlayer.create(getApplicationContext(), R.raw.ball);
        mediaPlayerBall.setVolume(100, 100);
        mediaPlayerBall.setOnCompletionListener(mediaPlayer -> {
            final int random = new Random().nextInt(Constants.CATCH_CHANCE);
            if (random == 0) {
                Intent intent = new Intent(this, GameMenuActivity.class);
                intent.putExtra("caught", true);
                startActivity(intent);
                finish();
            }
        });
    }

    protected boolean settingsMenuClick(MenuItem item) {
        if (item.getItemId() == R.id.depth_settings) {
            launchDepthSettingsMenuDialog();
            return true;
        } else if (item.getItemId() == R.id.instant_placement_settings) {
            launchInstantPlacementSettingsMenuDialog();
            return true;
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        if (session != null) {
            session.close();
            session = null;
        }

        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayerBall.stop();
        mediaPlayerBall.release();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mediaPlayer.start();

        if (session == null) {
            Exception exception = null;
            String message = null;
            try {
                switch (ArCoreApk.getInstance().requestInstall(this, !installRequested)) {
                    case INSTALL_REQUESTED:
                        installRequested = true;
                        return;
                    case INSTALLED:
                        break;
                }

                if (!CameraPermissionHelper.hasCameraPermission(this)) {
                    CameraPermissionHelper.requestCameraPermission(this);
                    return;
                }

                session = new Session(this);
            } catch (UnavailableArcoreNotInstalledException | UnavailableUserDeclinedInstallationException e) {
                message = "Please install ARCore";
                exception = e;
            } catch (UnavailableApkTooOldException e) {
                message = "Please update ARCore";
                exception = e;
            } catch (UnavailableSdkTooOldException e) {
                message = "Please update this app";
                exception = e;
            } catch (UnavailableDeviceNotCompatibleException e) {
                message = "This device does not support AR";
                exception = e;
            } catch (Exception e) {
                message = "Failed to create AR session";
                exception = e;
            }

            if (message != null) {
                messageSnackbarHelper.showError(this, message);
                Log.e(TAG, "Exception creating session", exception);
                return;
            }
        }

        try {
            configureSession();
            session.resume();
        } catch (CameraNotAvailableException e) {
            messageSnackbarHelper.showError(this, "Camera not available. Try restarting the app.");
            session = null;
            return;
        }

        surfaceView.onResume();
        displayRotationHelper.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mediaPlayer.pause();
        if (session != null) {
            displayRotationHelper.onPause();
            surfaceView.onPause();
            session.pause();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            Toast.makeText(this, "Camera permission is needed to run this application", Toast.LENGTH_LONG).show();
            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this)) {
                CameraPermissionHelper.launchPermissionSettings(this);
            }
            finish();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        FullScreenHelper.setFullScreenOnWindowFocusChanged(this, hasFocus);
    }

    @Override
    public void onSurfaceCreated(SampleRender render) {
        try {
            planeRenderer = new PlaneRenderer(render);
            backgroundRenderer = new BackgroundRenderer(render);
            virtualSceneFramebuffer = new Framebuffer(render, 1, 1);
            cubemapFilter = new SpecularCubemapFilter(render, CUBEMAP_RESOLUTION, CUBEMAP_NUMBER_OF_IMPORTANCE_SAMPLES);
            Texture dfgTexture = new Texture(render, Texture.Target.TEXTURE_2D, Texture.WrapMode.CLAMP_TO_EDGE, false);
            final int dfgResolution = 64;
            final int dfgChannels = 2;
            final int halfFloatSize = 2;

            ByteBuffer buffer = ByteBuffer.allocateDirect(dfgResolution * dfgResolution * dfgChannels * halfFloatSize);
            try (InputStream is = getAssets().open("models/dfg.raw")) {
                int res = is.read(buffer.array());
                Log.d(TAG, "onSurfaceCreated: " + res);
            }

            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, dfgTexture.getTextureId());
            GLError.maybeThrowGLException("Failed to bind DFG texture", "glBindTexture");
            GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RG16F, dfgResolution, dfgResolution, 0, GLES30.GL_RG, GLES30.GL_HALF_FLOAT, buffer);
            GLError.maybeThrowGLException("Failed to populate DFG texture", "glTexImage2D");

            pointCloudShader = Shader.createFromAssets(render, "shaders/point_cloud.vert", "shaders/point_cloud.frag", /*=*/ null).setVec4("u_Color", new float[]{31.0f / 255.0f, 188.0f / 255.0f, 210.0f / 255.0f, 1.0f}).setFloat("u_PointSize", 5.0f);
            pointCloudVertexBuffer = new VertexBuffer(render, 4, null);
            final VertexBuffer[] pointCloudVertexBuffers = {pointCloudVertexBuffer};
            pointCloudMesh = new Mesh(render, Mesh.PrimitiveMode.POINTS, null, pointCloudVertexBuffers);

            virtualObjectAlbedoTexture = Texture.createFromAsset(render, "models/monke.png", Texture.WrapMode.CLAMP_TO_EDGE, Texture.ColorFormat.SRGB);
            virtualObjectAlbedoAfekaBallTexture = Texture.createFromAsset(render, "models/monke.png", Texture.WrapMode.CLAMP_TO_EDGE, Texture.ColorFormat.SRGB);

            virtualObjectAlbedoInstantPlacementTexture = Texture.createFromAsset(render, "models/afekaBall.png", Texture.WrapMode.CLAMP_TO_EDGE, Texture.ColorFormat.SRGB);
            virtualObjectAlbedoInstantPlacementAfekaBallTexture = Texture.createFromAsset(render, "models/afekaBall.png", Texture.WrapMode.CLAMP_TO_EDGE, Texture.ColorFormat.SRGB);

            Texture virtualObjectPbrTexture = Texture.createFromAsset(render, "models/monke.png", Texture.WrapMode.CLAMP_TO_EDGE, Texture.ColorFormat.LINEAR);
            Texture virtualObjectPbrAfekaBallTexture = Texture.createFromAsset(render, "models/afekaBall.png", Texture.WrapMode.CLAMP_TO_EDGE, Texture.ColorFormat.LINEAR);

            virtualObjectMesh = Mesh.createFromAsset(render, "models/monke.obj");
            virtualAfekaBallMesh = Mesh.createFromAsset(render, "models/afekaBall.obj");

            virtualObjectShader = Shader.createFromAssets(render, "shaders/environmental_hdr.vert", "shaders/environmental_hdr.frag",
                            new HashMap<String, String>() {
                                {
                                    put("NUMBER_OF_MIPMAP_LEVELS", Integer.toString(cubemapFilter.getNumberOfMipmapLevels()));
                                }
                            }).setTexture("u_AlbedoTexture", virtualObjectAlbedoTexture)
                    .setTexture("u_RoughnessMetallicAmbientOcclusionTexture", virtualObjectPbrTexture)
                    .setTexture("u_Cubemap", cubemapFilter.getFilteredCubemapTexture()).setTexture("u_DfgTexture", dfgTexture);

            virtualObjectAfekaBallShader = Shader.createFromAssets(render, "shaders/environmental_hdr.vert", "shaders/environmental_hdr.frag",
                            new HashMap<String, String>() {
                                {
                                    put("NUMBER_OF_MIPMAP_LEVELS", Integer.toString(cubemapFilter.getNumberOfMipmapLevels()));
                                }
                            }).setTexture("u_AlbedoTexture", virtualObjectAlbedoAfekaBallTexture)
                    .setTexture("u_RoughnessMetallicAmbientOcclusionTexture", virtualObjectPbrAfekaBallTexture)
                    .setTexture("u_Cubemap", cubemapFilter.getFilteredCubemapTexture()).setTexture("u_DfgTexture", dfgTexture);

        } catch (IOException e) {
            Log.e(TAG, "Failed to read a required asset file", e);
            messageSnackbarHelper.showError(this, "Failed to read a required asset file: " + e);
        }
    }

    @Override
    public void onSurfaceChanged(SampleRender render, int width, int height) {
        displayRotationHelper.onSurfaceChanged(width, height);
        virtualSceneFramebuffer.resize(width, height);
    }

    @Override
    public void onDrawFrame(SampleRender render) {
        if (session == null) {
            return;
        }

        if (!hasSetTextureNames) {
            session.setCameraTextureNames(new int[]{backgroundRenderer.getCameraColorTexture().getTextureId()});
            hasSetTextureNames = true;
        }

        displayRotationHelper.updateSessionIfNeeded(session);

        Frame frame;
        try {
            frame = session.update();
        } catch (CameraNotAvailableException e) {
            Log.e(TAG, "Camera not available during onDrawFrame", e);
            messageSnackbarHelper.showError(this, "Camera not available. Try restarting the app.");
            return;
        }
        Camera camera = frame.getCamera();

        try {
            backgroundRenderer.setUseDepthVisualization(render, depthSettings.depthColorVisualizationEnabled());
            backgroundRenderer.setUseOcclusion(render, depthSettings.useDepthForOcclusion());
        } catch (IOException e) {
            Log.e(TAG, "Failed to read a required asset file", e);
            messageSnackbarHelper.showError(this, "Failed to read a required asset file: " + e);
            return;
        }

        backgroundRenderer.updateDisplayGeometry(frame);

        if (camera.getTrackingState() == TrackingState.TRACKING && (depthSettings.useDepthForOcclusion() || depthSettings.depthColorVisualizationEnabled())) {
            try (Image depthImage = frame.acquireDepthImage16Bits()) {
                backgroundRenderer.updateCameraDepthTexture(depthImage);
            } catch (NotYetAvailableException e) {
                // This normally means that depth data is not available yet. This is normal so we will not
                // spam the logcat with this.
            }
        }

        handleTap(frame, camera);

        trackingStateHelper.updateKeepScreenOnFlag(camera.getTrackingState());

        String message = null;
        if (camera.getTrackingState() == TrackingState.PAUSED) {
            if (camera.getTrackingFailureReason() == TrackingFailureReason.NONE) {
                message = SEARCHING_PLANE_MESSAGE;
            } else {
                message = TrackingStateHelper.getTrackingFailureReasonString(camera);
            }
        } else if (hasTrackingPlane()) {
            if (wrappedAnchors.isEmpty()) {
                message = WAITING_FOR_TAP_MESSAGE;
            }
        } else {
            message = SEARCHING_PLANE_MESSAGE;
        }
        if (message == null) {
            messageSnackbarHelper.hide(this);
        } else {
            messageSnackbarHelper.showMessage(this, message);
        }

        if (frame.getTimestamp() != 0) {
            backgroundRenderer.drawBackground(render);
        }

        if (camera.getTrackingState() == TrackingState.PAUSED) {
            return;
        }

        camera.getProjectionMatrix(projectionMatrix, 0, Z_NEAR, Z_FAR);

        camera.getViewMatrix(viewMatrix, 0);

        try (PointCloud pointCloud = frame.acquirePointCloud()) {
            if (pointCloud.getTimestamp() > lastPointCloudTimestamp) {
                pointCloudVertexBuffer.set(pointCloud.getPoints());
                lastPointCloudTimestamp = pointCloud.getTimestamp();
            }
            Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
            pointCloudShader.setMat4("u_ModelViewProjection", modelViewProjectionMatrix);
            render.draw(pointCloudMesh, pointCloudShader);
        }

        planeRenderer.drawPlanes(render, session.getAllTrackables(Plane.class), camera.getDisplayOrientedPose(), projectionMatrix);

        updateLightEstimation(frame.getLightEstimate(), viewMatrix);

        render.clear(virtualSceneFramebuffer, 0f, 0f, 0f, 0f);
        int count = 0;
        for (WrappedAnchor wrappedAnchor : wrappedAnchors) {
            Anchor anchor = wrappedAnchor.getAnchor();
            Trackable trackable = wrappedAnchor.getTrackable();
            if (anchor.getTrackingState() != TrackingState.TRACKING) {
                continue;
            }

            anchor.getPose().toMatrix(modelMatrix, 0);

            Matrix.multiplyMM(modelViewMatrix, 0, viewMatrix, 0, modelMatrix, 0);
            Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, modelViewMatrix, 0);

            if (count == 0) // AfekaMon
            {
                virtualObjectShader.setMat4("u_ModelView", modelViewMatrix);
                virtualObjectShader.setMat4("u_ModelViewProjection", modelViewProjectionMatrix);

                if (trackable instanceof InstantPlacementPoint && ((InstantPlacementPoint) trackable).getTrackingMethod() == InstantPlacementPoint.TrackingMethod.SCREENSPACE_WITH_APPROXIMATE_DISTANCE) {
                    virtualObjectShader.setTexture("u_AlbedoTexture", virtualObjectAlbedoInstantPlacementTexture);
                } else {
                    virtualObjectShader.setTexture("u_AlbedoTexture", virtualObjectAlbedoTexture);
                }

                render.draw(virtualObjectMesh, virtualObjectShader, virtualSceneFramebuffer);
            } else // AfekaBall
            {
                virtualObjectAfekaBallShader.setMat4("u_ModelView", modelViewMatrix);
                virtualObjectAfekaBallShader.setMat4("u_ModelViewProjection", modelViewProjectionMatrix);

                if (trackable instanceof InstantPlacementPoint && ((InstantPlacementPoint) trackable).getTrackingMethod() == InstantPlacementPoint.TrackingMethod.SCREENSPACE_WITH_APPROXIMATE_DISTANCE) {
                    virtualObjectAfekaBallShader.setTexture("u_AlbedoTexture", virtualObjectAlbedoInstantPlacementAfekaBallTexture);
                } else {
                    virtualObjectAfekaBallShader.setTexture("u_AlbedoTexture", virtualObjectAlbedoAfekaBallTexture);
                }

                render.draw(virtualAfekaBallMesh, virtualObjectAfekaBallShader, virtualSceneFramebuffer);
            }
            count++;
        }

        backgroundRenderer.drawVirtualScene(render, virtualSceneFramebuffer, Z_NEAR, Z_FAR);
    }

    private void handleTap(Frame frame, Camera camera) {
        MotionEvent tap = tapHelper.poll();
        if (tap != null && camera.getTrackingState() == TrackingState.TRACKING) {
            List<HitResult> hitResultList;
            if (instantPlacementSettings.isInstantPlacementEnabled()) {
                hitResultList = frame.hitTestInstantPlacement(tap.getX(), tap.getY(), APPROXIMATE_DISTANCE_METERS);
            } else {
                hitResultList = frame.hitTest(tap);
            }
            for (HitResult hit : hitResultList) {
                Trackable trackable = hit.getTrackable();
                if ((trackable instanceof Plane && ((Plane) trackable).isPoseInPolygon(hit.getHitPose()) && (PlaneRenderer.calculateDistanceToPlane(hit.getHitPose(), camera.getPose()) > 0)) || (trackable instanceof Point && ((Point) trackable).getOrientationMode() == OrientationMode.ESTIMATED_SURFACE_NORMAL) || (trackable instanceof InstantPlacementPoint) || (trackable instanceof DepthPoint)) {
                    if (wrappedAnchors.size() >= 20) {
                        wrappedAnchors.get(0).getAnchor().detach();
                        wrappedAnchors.remove(0);
                    }

                    if (wrappedAnchors.size() > 0 && !mediaPlayerBall.isPlaying()) {
                        wrappedAnchors.add(new WrappedAnchor(hit.createAnchor(), trackable));
                        mediaPlayerBall.start();
                    }
                    else {
                        wrappedAnchors.add(new WrappedAnchor(hit.createAnchor(), trackable));
                    }

                    this.runOnUiThread(this::showOcclusionDialogIfNeeded);
                    break;
                }
            }
        }
    }

    private void showOcclusionDialogIfNeeded() {
        boolean isDepthSupported = session.isDepthModeSupported(Config.DepthMode.AUTOMATIC);
        if (!depthSettings.shouldShowDepthEnableDialog() || !isDepthSupported) {
            return;
        }

        new AlertDialog.Builder(this).setTitle(R.string.options_title_with_depth).setMessage(R.string.depth_use_explanation).setPositiveButton(R.string.button_text_enable_depth, (DialogInterface dialog, int which) -> depthSettings.setUseDepthForOcclusion(true)).setNegativeButton(R.string.button_text_disable_depth, (DialogInterface dialog, int which) -> depthSettings.setUseDepthForOcclusion(false)).show();
    }

    private void launchInstantPlacementSettingsMenuDialog() {
        resetSettingsMenuDialogCheckboxes();
        Resources resources = getResources();
        new AlertDialog.Builder(this).setTitle(R.string.options_title_instant_placement).setMultiChoiceItems(resources.getStringArray(R.array.instant_placement_options_array), instantPlacementSettingsMenuDialogCheckboxes, (DialogInterface dialog, int which, boolean isChecked) -> instantPlacementSettingsMenuDialogCheckboxes[which] = isChecked).setPositiveButton(R.string.done, (DialogInterface dialogInterface, int which) -> applySettingsMenuDialogCheckboxes()).setNegativeButton(android.R.string.cancel, (DialogInterface dialog, int which) -> resetSettingsMenuDialogCheckboxes()).show();
    }

    private void launchDepthSettingsMenuDialog() {
        resetSettingsMenuDialogCheckboxes();
        Resources resources = getResources();
        if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
            new AlertDialog.Builder(this).setTitle(R.string.options_title_with_depth).setMultiChoiceItems(resources.getStringArray(R.array.depth_options_array), depthSettingsMenuDialogCheckboxes, (DialogInterface dialog, int which, boolean isChecked) -> depthSettingsMenuDialogCheckboxes[which] = isChecked).setPositiveButton(R.string.done, (DialogInterface dialogInterface, int which) -> applySettingsMenuDialogCheckboxes()).setNegativeButton(android.R.string.cancel, (DialogInterface dialog, int which) -> resetSettingsMenuDialogCheckboxes()).show();
        } else {
            new AlertDialog.Builder(this).setTitle(R.string.options_title_without_depth).setPositiveButton(R.string.done, (DialogInterface dialogInterface, int which) -> applySettingsMenuDialogCheckboxes()).show();
        }
    }

    private void applySettingsMenuDialogCheckboxes() {
        depthSettings.setUseDepthForOcclusion(depthSettingsMenuDialogCheckboxes[0]);
        depthSettings.setDepthColorVisualizationEnabled(depthSettingsMenuDialogCheckboxes[1]);
        instantPlacementSettings.setInstantPlacementEnabled(instantPlacementSettingsMenuDialogCheckboxes[0]);
        configureSession();
    }

    private void resetSettingsMenuDialogCheckboxes() {
        depthSettingsMenuDialogCheckboxes[0] = depthSettings.useDepthForOcclusion();
        depthSettingsMenuDialogCheckboxes[1] = depthSettings.depthColorVisualizationEnabled();
        instantPlacementSettingsMenuDialogCheckboxes[0] = instantPlacementSettings.isInstantPlacementEnabled();
    }

    private boolean hasTrackingPlane() {
        for (Plane plane : session.getAllTrackables(Plane.class)) {
            if (plane.getTrackingState() == TrackingState.TRACKING) {
                return true;
            }
        }
        return false;
    }

    private void updateLightEstimation(LightEstimate lightEstimate, float[] viewMatrix) {
        if (lightEstimate.getState() != LightEstimate.State.VALID) {
            virtualObjectShader.setBool("u_LightEstimateIsValid", false);
            virtualObjectAfekaBallShader.setBool("u_LightEstimateIsValid", false);
            return;
        }
        virtualObjectShader.setBool("u_LightEstimateIsValid", true);
        virtualObjectAfekaBallShader.setBool("u_LightEstimateIsValid", true);

        Matrix.invertM(viewInverseMatrix, 0, viewMatrix, 0);
        virtualObjectShader.setMat4("u_ViewInverse", viewInverseMatrix);
        virtualObjectAfekaBallShader.setMat4("u_ViewInverse", viewInverseMatrix);

        updateMainLight(lightEstimate.getEnvironmentalHdrMainLightDirection(), lightEstimate.getEnvironmentalHdrMainLightIntensity(), viewMatrix);
        updateSphericalHarmonicsCoefficients(lightEstimate.getEnvironmentalHdrAmbientSphericalHarmonics());
        cubemapFilter.update(lightEstimate.acquireEnvironmentalHdrCubeMap());
    }

    private void updateMainLight(float[] direction, float[] intensity, float[] viewMatrix) {
        worldLightDirection[0] = direction[0];
        worldLightDirection[1] = direction[1];
        worldLightDirection[2] = direction[2];
        Matrix.multiplyMV(viewLightDirection, 0, viewMatrix, 0, worldLightDirection, 0);
        virtualObjectShader.setVec4("u_ViewLightDirection", viewLightDirection);
        virtualObjectAfekaBallShader.setVec4("u_ViewLightDirection", viewLightDirection);
        virtualObjectShader.setVec3("u_LightIntensity", intensity);
        virtualObjectAfekaBallShader.setVec3("u_LightIntensity", intensity);
    }

    private void updateSphericalHarmonicsCoefficients(float[] coefficients) {
        if (coefficients.length != 9 * 3) {
            throw new IllegalArgumentException("The given coefficients array must be of length 27 (3 components per 9 coefficients");
        }

        for (int i = 0; i < 9 * 3; ++i) {
            sphericalHarmonicsCoefficients[i] = coefficients[i] * sphericalHarmonicFactors[i / 3];
        }
        virtualObjectShader.setVec3Array("u_SphericalHarmonicsCoefficients", sphericalHarmonicsCoefficients);
        virtualObjectAfekaBallShader.setVec3Array("u_SphericalHarmonicsCoefficients", sphericalHarmonicsCoefficients);
    }

    private void configureSession() {
        Config config = session.getConfig();
        config.setLightEstimationMode(Config.LightEstimationMode.ENVIRONMENTAL_HDR);
        if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
            config.setDepthMode(Config.DepthMode.AUTOMATIC);
        } else {
            config.setDepthMode(Config.DepthMode.DISABLED);
        }
        if (instantPlacementSettings.isInstantPlacementEnabled()) {
            config.setInstantPlacementMode(InstantPlacementMode.LOCAL_Y_UP);
        } else {
            config.setInstantPlacementMode(InstantPlacementMode.DISABLED);
        }
        session.configure(config);
    }
}
