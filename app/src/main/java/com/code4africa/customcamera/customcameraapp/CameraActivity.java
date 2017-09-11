package com.code4africa.customcamera.customcameraapp;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.hardware.camera2.CameraDevice;
import android.widget.Toast;


public class CameraActivity extends AppCompatActivity {
	private static final String TAG = CameraActivity.class.getSimpleName();
	private TextureView textureView;
	private CameraDevice cameraDevice;

	private TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
		@Override public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
			Toast.makeText(getApplicationContext(), "Surface texture is available", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

		}

		@Override public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
			return false;
		}

		@Override public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

		}
	};

	private CameraDevice.StateCallback cameraDeviceStateCallback = new CameraDevice.StateCallback() {
		@Override public void onOpened(@NonNull CameraDevice camera) {
			cameraDevice = camera;
		}

		@Override public void onDisconnected(@NonNull CameraDevice camera) {
			camera.close();
			cameraDevice = null;
		}

		@Override public void onError(@NonNull CameraDevice camera, int i) {
			camera.close();
			cameraDevice = null;
			Log.w(TAG, "Error opening camera: ");
		}
	};

	private void closeCamera() {
		if(cameraDevice != null) {
			cameraDevice.close();
			cameraDevice = null;
		}
	}

	@Override protected void onPause() {
		closeCamera();
		super.onPause();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		View decorView = getWindow().getDecorView();
		if(hasFocus){
			decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
			| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
			| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
			| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
			| View.SYSTEM_UI_FLAG_FULLSCREEN
			| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera);

		textureView = (TextureView) findViewById(R.id.tv_camera);

	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
	}

	@Override protected void onResume() {
		super.onResume();

		if(textureView.isAvailable()) {

		} else {
			textureView.setSurfaceTextureListener(surfaceTextureListener);
		}
	}

	private boolean checkCameraHardware(Context context) {
		return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
	}

	public static CameraDevice getCameraInstance() {
		CameraDevice camera = null;
		try {
			//camera = CameraDevice;
		} catch (Exception e) {
			Log.d(TAG, "Camera not available: " + e);
		}
		return camera;
	}
}
