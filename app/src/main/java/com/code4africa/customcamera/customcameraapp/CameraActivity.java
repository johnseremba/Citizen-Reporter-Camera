package com.code4africa.customcamera.customcameraapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.hardware.camera2.CameraDevice;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class CameraActivity extends AppCompatActivity {
	private static final String TAG = CameraActivity.class.getSimpleName();
	private static final int REQUEST_CAMERA_PERMISSION = 1;
	private static final int REQUEST_STORAGE_PERMISSION = 2;
	private static final int STATE_PREVIEW = 0;
	private static final int STATE_WAIT_LOCK = 1;
	private int captureState = STATE_PREVIEW;
	private TextureView textureView;
	private ImageView capturePictureBtn;
	private CameraDevice cameraDevice;
	private String cameraID;
	private HandlerThread backgroundHandlerThread;
	private Handler backgroundHandler;
	private static SparseIntArray ORIENTATIONS = new SparseIntArray();
	static {
		ORIENTATIONS.append(Surface.ROTATION_0, 0);
		ORIENTATIONS.append(Surface.ROTATION_90, 90);
		ORIENTATIONS.append(Surface.ROTATION_180, 180);
		ORIENTATIONS.append(Surface.ROTATION_270, 270);
	}
	private Size previewSize;
	private CaptureRequest.Builder captureRequestBuilder;

	private File picturesFolder;
	private String pictureName;
	private Size pictureSize;
	private ImageReader imageReader;
	private CameraCaptureSession previewCaptureSession;
	int totalRotation;

	private final ImageReader.OnImageAvailableListener onImageAvailableListener = new ImageReader.OnImageAvailableListener() {
		@Override
		public void onImageAvailable(ImageReader reader) {
			backgroundHandler.post(new ImageSaver(reader.acquireLatestImage()));
		}
	};

	private class ImageSaver implements Runnable {
		private final Image image;

		public ImageSaver(Image image) {
			this.image = image;
		}

		@Override public void run() {
			ByteBuffer byteBuffer = this.image.getPlanes()[0].getBuffer();
			byte[] bytes = new byte[byteBuffer.remaining()];
			byteBuffer.get(bytes);

			FileOutputStream fileOutputStream = null;
			try {
				fileOutputStream = new FileOutputStream(pictureName);
				fileOutputStream.write(bytes);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				this.image.close();

				if(fileOutputStream != null) {
					try {
						fileOutputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private CameraCaptureSession.CaptureCallback previewCaptureCallback = new CameraCaptureSession.CaptureCallback() {
		@Override
		public void onCaptureCompleted(@NonNull CameraCaptureSession session,
				@NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
			super.onCaptureCompleted(session, request, result);

		}

		private void process(CaptureResult captureResult) {
			switch (captureState) {
				case STATE_PREVIEW:
					//
					break;
				case STATE_WAIT_LOCK:
					captureState = STATE_PREVIEW;
					Integer afState = captureResult.get(CaptureResult.CONTROL_AF_STATE);
					if (afState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED
							|| afState == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED) {
						Toast.makeText(getApplicationContext(), "AF Locked", Toast.LENGTH_SHORT).show();
						startStillCapture();
					}
					break;

			}
		}
	};

	private TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
		@Override public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
			setUpCamera(width, height);
			connectCamera();
		}

		@Override
		public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {

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
			startPreview();
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

	private void setUpCamera(int width, int height) {
		CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

		try{
			for(String camID: cameraManager.getCameraIdList()){
				CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(camID);
				if(cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) ==
						CameraCharacteristics.LENS_FACING_BACK) {
					int deviceOrientation = getWindowManager().getDefaultDisplay().getRotation();
					totalRotation = sensorToDeviceOrientation(cameraCharacteristics, deviceOrientation);
					int rotatedWidth = width;
					int rotatedHeight = height;
					boolean swapRotation = totalRotation == 90 || totalRotation == 270;

					if(swapRotation) {
						rotatedWidth = height;
						rotatedHeight = width;
					}

					StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
					previewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), rotatedWidth, rotatedHeight);
					pictureSize = chooseOptimalSize(map.getOutputSizes(ImageFormat.JPEG), rotatedWidth, rotatedHeight);
					imageReader = ImageReader.newInstance(pictureSize.getWidth(), pictureSize.getHeight(), ImageFormat.JPEG, 1);
					imageReader.setOnImageAvailableListener(onImageAvailableListener, backgroundHandler);

					cameraID = camID;
					return;
				}
			}
		} catch (CameraAccessException e) {
			e.printStackTrace();
		}

	}

	private void startBackgroundThread() {
		backgroundHandlerThread = new HandlerThread("Code4AfricaCustomCamera");
		backgroundHandlerThread.start();
		backgroundHandler = new Handler(backgroundHandlerThread.getLooper());
	}

	private void stopBackgroundThread() {
		backgroundHandlerThread.quitSafely();

		try {
			backgroundHandlerThread.join();
			backgroundHandler = null;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private static int sensorToDeviceOrientation(CameraCharacteristics cameraCharacteristics,int deviceOrientation) {
		int sensorOrientation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
		deviceOrientation = ORIENTATIONS.get(deviceOrientation);
		return (sensorOrientation + deviceOrientation + 360) % 360;
	}

	public static class CompareSizeByArea implements Comparator<Size> {
		@Override public int compare(Size lhs, Size rhs) {
			return Long.signum((long) lhs.getWidth() * lhs.getHeight() / (long) rhs.getWidth() * rhs.getHeight());
		}
	}

	public static Size chooseOptimalSize(Size[] choices, int width, int height) {
		List<Size> optimal = new ArrayList<Size>();
		for(Size option : choices) {
			if(option.getHeight() == option.getWidth() * height / width
					&& option.getWidth() >= width && option.getHeight() >= height) {
				optimal.add(option);
			}
		}

		if(optimal.size() > 0) {
			return Collections.min(optimal, new CompareSizeByArea());
		} else {
			return choices[0];
		}

	}

	private void connectCamera() {
		CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
		try {
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
					cameraManager.openCamera(cameraID, cameraDeviceStateCallback, backgroundHandler);
				} else {
					if(shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
						Toast.makeText(this, "Code4Africa custom camera required access to the camera.", Toast.LENGTH_SHORT).show();
					}
					requestPermissions(new String[] {Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
				}
			} else {
				cameraManager.openCamera(cameraID, cameraDeviceStateCallback, backgroundHandler);
			}
		} catch (CameraAccessException e) {
			e.printStackTrace();
		}
	}

	private void startPreview() {
		SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
		surfaceTexture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
		Surface previewSurface = new Surface(surfaceTexture);

		try {
			captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
			captureRequestBuilder.addTarget(previewSurface);

			cameraDevice.createCaptureSession(Arrays.asList(previewSurface),
					new CameraCaptureSession.StateCallback() {
						@Override public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
							previewCaptureSession = cameraCaptureSession;
							try {
								previewCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, backgroundHandler);
							} catch (CameraAccessException e) {
								e.printStackTrace();
							}
						}

						@Override
						public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
							Toast.makeText(getApplicationContext(), "Unable to setup camera preview!", Toast.LENGTH_SHORT).show();
						}
					}, null);
		} catch (CameraAccessException e) {
			e.printStackTrace();
		}
	}

	public void createPicturesFolder() {
		File pictureFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		picturesFolder = new File(pictureFile, "Code4Africa");
		if(!picturesFolder.exists()) {
			picturesFolder.mkdirs();
		}
	}

	public File createPictureName() throws IOException{
		String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String prepend = "IMG_" + timestamp + "_";
		File pictureFile = File.createTempFile(prepend, ".jpg", picturesFolder);
		pictureName = pictureFile.getAbsolutePath();
		return pictureFile;
	}

	private void checkWriteStoragePermission(){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
			if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
					== PackageManager.PERMISSION_GRANTED){
				try {
					createPictureName();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
					if(shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
						Toast.makeText(this, "App needs to store pictures", Toast.LENGTH_SHORT);
					}
					requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
			}
		} else {
			try {
				createPictureName();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void lockFocus() {
		captureState = STATE_WAIT_LOCK;
		captureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_START);
		try {
			previewCaptureSession.capture(captureRequestBuilder.build(), previewCaptureCallback, backgroundHandler);
		} catch (CameraAccessException e) {
			e.printStackTrace();
		}
	}

	@Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
			@NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		switch (requestCode) {
			case REQUEST_CAMERA_PERMISSION:
				if(grantResults[0] != PackageManager.PERMISSION_GRANTED){
					Toast.makeText(getApplicationContext(), "App can't run without camera permissions.", Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getApplicationContext(), "Permission granted successfully", Toast.LENGTH_SHORT).show();
				}
				break;
			case REQUEST_STORAGE_PERMISSION:
				if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					try {
						createPictureName();
					} catch (IOException e) {
						e.printStackTrace();
					}
					Toast.makeText(getApplicationContext(), "Permission granted successfully", Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getApplicationContext(), "App can't run without storage permissions.", Toast.LENGTH_SHORT).show();
				}
				break;
			default:
				break;
		}
	}

	private void startStillCapture(){
		try {
			captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
			captureRequestBuilder.addTarget(imageReader.getSurface());
			captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, totalRotation); // Fix orientation skews

			CameraCaptureSession.CaptureCallback stillCaptureCallback = new CameraCaptureSession.CaptureCallback() {
				@Override
				public void onCaptureStarted(@NonNull CameraCaptureSession session,
						@NonNull CaptureRequest request, long timestamp, long frameNumber) {
					super.onCaptureStarted(session, request, timestamp, frameNumber);
					try {
						createPictureName();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			};
			previewCaptureSession.capture(captureRequestBuilder.build(), stillCaptureCallback, null);
		} catch (CameraAccessException e) {
			e.printStackTrace();
		}
	}

	@Override protected void onPause() {
		closeCamera();
		stopBackgroundThread();
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

		createPicturesFolder();

		createPicturesFolder();
		textureView = (TextureView) findViewById(R.id.tv_camera);
		capturePictureBtn = (ImageView) findViewById(R.id.img_capture);

		capturePictureBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				lockFocus();
			}
		});

	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
	}

	@Override protected void onResume() {
		super.onResume();

		startBackgroundThread();

		if(textureView.isAvailable()) {
			setUpCamera(textureView.getWidth(), textureView.getHeight());
			connectCamera();
		} else {
			textureView.setSurfaceTextureListener(surfaceTextureListener);
		}
	}

}
