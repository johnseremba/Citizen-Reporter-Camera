package com.code4africa.customcamera.customcameraapp;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.widget.TextView;

public class CameraActivity extends AppCompatActivity {
	private static final String TAG = CameraActivity.class.getSimpleName();
	private TextureView textureView;

	private TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
		@Override public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {

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

	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
	}

	@Override protected void onResume() {
		super.onResume();
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
