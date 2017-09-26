package com.code4africa.customcamera.customcameraapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.MeteringRectangle;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaActionSound;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.hardware.camera2.CameraDevice;
import android.widget.Chronometer;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;
import com.bumptech.glide.Glide;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class CameraActivity extends AppCompatActivity
		implements SceneSelectorAdapter.OnClickThumbListener {
	private static final String TAG = CameraActivity.class.getSimpleName();
	private static final String IMAGE_FILE_LOCATION = "image_file_location";
	private static final String IMAGE_SAVED_PATH = "imagePath";
	private static final String VIDEO_SAVED_PATH = "videoPath";
	private static final int REQUEST_CAMERA_PERMISSION = 1;
	private static final int REQUEST_STORAGE_PERMISSION = 2;
	private static final int REQUEST_AUDIO_PERMISSION = 4;
	private static final int STATE_PREVIEW = 0;
	private static final int STATE_WAIT_LOCK = 1;
	private static final int PREVIEW_IMAGE_RESULT = 3;
	private static final int PROGRESS_MIN = 50;
	private static final int unSelected = R.drawable.ic_circular;
	private static final int selected = R.drawable.ic_selected_circular;
	private static String PORTRAIT_SCENE = "Portrait";
	private static String CANDID_SCENE = "Candid";
	private static String INTERACTION_SCENE = "Interaction";
	private static String ENVIRONMENT_SCENE = "Environment";
	private static String SIGNATURE_SCENE = "Signature";
	private int captureState = STATE_PREVIEW;
	private TextureView textureView;
	private ImageView capturePictureBtn;
	private ImageView openGalleryBtn;
	private ImageView swapCameraBtn;
	private ImageView flashModeBtn;
	private TextView swipeText;
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

	private CaptureRequest.Builder captureRequestBuilder;
	private File imageFolder;
	private File videoFolder;
	private String imageFileName;
	private String videoFileName;
	private Size imageSize;
	private Size videoSize;
	private Size previewSize;
	private ImageReader imageReader;
	private MediaRecorder mediaRecorder;
	private CameraCaptureSession previewCaptureSession;
	private int totalRotation;

	private ImageSwitcher switcher1, switcher2, switcher3, switcher4, switcher5;
	private ImageView imgOverlay;
	private HashMap<String, ArrayList<Integer>> overlayScenes;
	private ArrayList<Integer> portrait, signature, interaction, candid, environment;
	private GestureDetectorCompat gestureObject;
	private Integer selectedScene = 2;
	private Integer prevScene = 2;
	private int camLensFacing = CameraCharacteristics.LENS_FACING_BACK;
	private boolean isRecording = false;
	private Chronometer chronometer;
	private String cameraPreviewResult;
	private Integer flashStatus = 0;

	private RecyclerView sceneRecyclerView;
	private LinearLayoutManager layoutManager;
	private SceneSelectorAdapter sceneSelectorAdapter;
	private boolean moreScenes = false;
	private String currentScene;

	private double progressValue;
	private SeekBar lightSeekBar;
	private TextView seekBarProgressText;
	private int aeRange;
	private CameraManager cameraManager;
	private Float maxDigitalZoom;
	private ScaleGestureDetector scaleGestureDetector;
	private float scale = 10f;
	private TextView zoomCaption;
	private Rect activePixesAfter;
	private Rect zoom;

	private final ImageReader.OnImageAvailableListener onImageAvailableListener =
			new ImageReader.OnImageAvailableListener() {
				@Override
				public void onImageAvailable(ImageReader reader) {
					backgroundHandler.post(new ImageSaver(reader.acquireLatestImage()));
				}
			};
	private boolean manualFocusEngaged = false;
	private Rect sensorArraySize;
	private boolean isMeteringAFAreaSupported;

	@Override public void OnClickScene(String sceneKey, Integer position) {
		imgOverlay.setImageResource(overlayScenes.get(sceneKey).get(position));
		hideSceneSwitcher();
	}

	private class ImageSaver implements Runnable {
		private final Image image;

		private ImageSaver(Image image) {
			this.image = image;
		}

		@Override public void run() {
			ByteBuffer byteBuffer = image.getPlanes()[0].getBuffer();
			byte[] bytes = new byte[byteBuffer.remaining()];
			byteBuffer.get(bytes);

			FileOutputStream fileOutputStream = null;
			try {
				fileOutputStream = new FileOutputStream(imageFileName);
				fileOutputStream.write(bytes);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				image.close();

				Intent mediaStoreUpdateIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
				mediaStoreUpdateIntent.setData(Uri.fromFile(new File(imageFileName)));
				sendBroadcast(mediaStoreUpdateIntent);

				if (fileOutputStream != null) {
					try {
						fileOutputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				openImage();
			}
		}
	}

	private CameraCaptureSession.CaptureCallback previewCaptureCallback =
			new CameraCaptureSession.CaptureCallback() {
				@Override
				public void onCaptureCompleted(@NonNull CameraCaptureSession session,
						@NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
					super.onCaptureCompleted(session, request, result);
					manualFocusEngaged = false;

					if (request.getTag() == "FOCUS_TAG") {
						captureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, null);
						applySettings();
					}

					process(result);
				}

				private void process(CaptureResult captureResult) {
					switch (captureState) {
						case STATE_WAIT_LOCK:
							captureState = STATE_PREVIEW;
							MediaActionSound sound = new MediaActionSound();
							sound.play(MediaActionSound.SHUTTER_CLICK);
							startStillCapture();
							break;
					}
				}
			};

	private TextureView.SurfaceTextureListener surfaceTextureListener =
			new TextureView.SurfaceTextureListener() {
				@Override public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width,
						int height) {
					setUpCamera(width, height);
					transformImage(width, height);
					connectCamera();
				}

				@Override
				public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width,
						int height) {

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
			mediaRecorder = new MediaRecorder();

			if (isRecording) {
				try {
					createVideoFileName();
				} catch (IOException e) {
					e.printStackTrace();
				}
				startRecord();
				mediaRecorder.start();

				runOnUiThread(new Runnable() {
					@Override public void run() {
						chronometer.setBase(SystemClock.elapsedRealtime());
						chronometer.setVisibility(View.VISIBLE);
						chronometer.start();
					}
				});
			} else {
				startPreview();
			}
		}

		@Override public void onDisconnected(@NonNull CameraDevice camera) {
			camera.close();
			cameraDevice = null;
		}

		@Override public void onError(@NonNull CameraDevice camera, int i) {
			camera.close();
			cameraDevice = null;
			Log.d(TAG, "Error opening camera: ");
		}
	};

	private void closeCamera() {
		if (cameraDevice != null) {
			cameraDevice.close();
			cameraDevice = null;
		}
		if (mediaRecorder != null) {
			mediaRecorder.release();
			mediaRecorder = null;
		}
	}

	private void setUpCamera(int width, int height) {
		cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
		try {
			for (String camID : cameraManager.getCameraIdList()) {
				CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(camID);
				if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) ==
						camLensFacing) {
					int deviceOrientation = getWindowManager().getDefaultDisplay().getRotation();
					totalRotation = sensorToDeviceOrientation(cameraCharacteristics, deviceOrientation);
					int rotatedWidth = width;
					int rotatedHeight = height;
					boolean swapRotation = totalRotation == 90 || totalRotation == 270;

					if (swapRotation) {
						rotatedWidth = height;
						rotatedHeight = width;
					}

					StreamConfigurationMap map =
							cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
					previewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), rotatedWidth,
							rotatedHeight);
					videoSize = chooseOptimalSize(map.getOutputSizes(MediaRecorder.class), rotatedWidth,
							rotatedHeight);
					imageSize =
							chooseOptimalSize(map.getOutputSizes(ImageFormat.JPEG), rotatedWidth, rotatedHeight);
					imageReader =
							ImageReader.newInstance(imageSize.getWidth(), imageSize.getHeight(), ImageFormat.JPEG,
									1);
					imageReader.setOnImageAvailableListener(onImageAvailableListener, backgroundHandler);
					cameraID = camID;
					aeRange = cameraCharacteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE)
							.getUpper();
					sensorArraySize =
							cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
					activePixesAfter =
							cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
					maxDigitalZoom =
							cameraCharacteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM);
					isMeteringAFAreaSupported =
							cameraCharacteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AF) >= 1;
					maxDigitalZoom *= 10;
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

	private static int sensorToDeviceOrientation(CameraCharacteristics cameraCharacteristics,
			int deviceOrientation) {
		int sensorOrientation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
		deviceOrientation = ORIENTATIONS.get(deviceOrientation);
		return (sensorOrientation + deviceOrientation + 360) % 360;
	}

	public static class CompareSizeByArea implements Comparator<Size> {
		@Override public int compare(Size lhs, Size rhs) {
			return Long.signum(
					(long) lhs.getWidth() * lhs.getHeight() / (long) rhs.getWidth() * rhs.getHeight());
		}
	}

	public static Size chooseOptimalSize(Size[] choices, int width, int height) {
		List<Size> optimal = new ArrayList<Size>();
		for (Size option : choices) {
			if (option.getHeight() == option.getWidth() * height / width
					&& option.getWidth() >= width && option.getHeight() >= height) {
				optimal.add(option);
			}
		}

		if (optimal.size() > 0) {
			return Collections.min(optimal, new CompareSizeByArea());
		} else {
			return choices[0];
		}
	}

	private void swapFlashMode() {
		flashStatus += 1;

		if (flashStatus > 2) {
			flashStatus = 0;
		}

		switch (flashStatus) {
			case 0:
				flashModeBtn.setImageResource(R.drawable.ic_flash_off);
				break;
			case 1:
				flashModeBtn.setImageResource(R.drawable.ic_flash_on);
				break;
			case 2:
				flashModeBtn.setImageResource(R.drawable.ic_flash_auto);
				break;
		}
	}

	private void connectCamera() {
		CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
		try {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
						== PackageManager.PERMISSION_GRANTED) {
					cameraManager.openCamera(cameraID, cameraDeviceStateCallback, backgroundHandler);
				} else {
					if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
						Toast.makeText(this, "Code4Africa custom camera required access to the camera.",
								Toast.LENGTH_SHORT).show();
					}
					requestPermissions(new String[] { Manifest.permission.CAMERA },
							REQUEST_CAMERA_PERMISSION);
				}
			} else {
				cameraManager.openCamera(cameraID, cameraDeviceStateCallback, backgroundHandler);
			}
		} catch (CameraAccessException e) {
			e.printStackTrace();
		}
	}

	private void startRecord() {
		try {
			setUpMediaRecorder();
			SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
			surfaceTexture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
			Surface previewSurface = new Surface(surfaceTexture);
			Surface recordSurface = mediaRecorder.getSurface();
			try {
				captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
				captureRequestBuilder.addTarget(previewSurface);
				captureRequestBuilder.addTarget(recordSurface);

				cameraDevice.createCaptureSession(Arrays.asList(previewSurface, recordSurface),
						new CameraCaptureSession.StateCallback() {
							@Override
							public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
								try {
									cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null,
											null);
								} catch (CameraAccessException e) {
									e.printStackTrace();
								}
							}

							@Override
							public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
								Log.d(TAG, "onConfigureFailed: startRecord");
							}
						}, null);
			} catch (CameraAccessException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
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

			cameraDevice.createCaptureSession(Arrays.asList(previewSurface, imageReader.getSurface()),
					new CameraCaptureSession.StateCallback() {
						@Override
						public void onConfigured(@NonNull CameraCaptureSession session) {
							previewCaptureSession = session;
							try {
								previewCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null,
										backgroundHandler);
							} catch (CameraAccessException e) {
								e.printStackTrace();
							}
						}

						@Override
						public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
							Log.d(TAG, "Unable to setup camera preview!");
						}
					}, null);
		} catch (CameraAccessException e) {
			e.printStackTrace();
		}
	}

	public void createImageFolder() {
		File imageFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
		imageFolder = new File(imageFile, "Code4Africa");
		if (!imageFolder.exists()) {
			boolean result = imageFolder.mkdirs();
			if (result) {
				Log.d(TAG, "C4A images folder created successfully!");
			} else {
				Log.d(TAG, "Oops, C4A images folder not created!!");
			}
		} else {
			Log.d(TAG, "Image directory already exists!");
		}
	}

	public void createVideoFolder() {
		File videoFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
		videoFolder = new File(videoFile, "Code4Africa");
		if (!videoFolder.exists()) {
			boolean result = videoFolder.mkdir();
			if (result) {
				Log.d(TAG, "C4A video folder created successfully!");
			} else {
				Log.d(TAG, "C4A video directory already exists");
			}
		}
	}

	public void createVideoFileName() throws IOException {
		String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String prepend = "VID_" + timestamp;
		File videoFile = File.createTempFile(prepend, ".mp4", videoFolder);
		videoFileName = videoFile.getAbsolutePath();
	}

	public void createImageFileName() throws IOException {
		String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String prepend = "IMG_" + timestamp;
		File imageFile = File.createTempFile(prepend, ".jpg", imageFolder);
		imageFileName = imageFile.getAbsolutePath();
	}

	private void checkWriteStoragePermission() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
					== PackageManager.PERMISSION_GRANTED) {
				createMediaFolders();
				if (isRecording) {
					if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
							== PackageManager.PERMISSION_GRANTED) {
						try {
							createVideoFileName();
						} catch (IOException e) {
							e.printStackTrace();
						}
						startRecord();
						mediaRecorder.start();
						chronometer.setBase(SystemClock.elapsedRealtime());
						chronometer.setVisibility(View.VISIBLE);
						chronometer.start();
					} else {
						if (shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
							Toast.makeText(this.getApplicationContext(), "App needs to record audio",
									Toast.LENGTH_SHORT).show();
						}
						Log.d(TAG, "No audio permissions.");
						requestPermissions(new String[] { Manifest.permission.RECORD_AUDIO },
								REQUEST_AUDIO_PERMISSION);
					}
				}
			} else {
				if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
					Toast.makeText(this, "App needs to store pictures & videos", Toast.LENGTH_SHORT).show();
				}
				Log.d(TAG, "No external storage permissions");
				requestPermissions(new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE },
						REQUEST_STORAGE_PERMISSION);
			}
		} else {
			createMediaFolders();
			if (isRecording) {
				try {
					createVideoFileName();
				} catch (IOException e) {
					e.printStackTrace();
				}
				startRecord();
				mediaRecorder.start();
				chronometer.setBase(SystemClock.elapsedRealtime());
				chronometer.setVisibility(View.VISIBLE);
				chronometer.start();
			}
		}
	}

	private void createMediaFolders() {
		createImageFolder();
		createVideoFolder();
	}

	private void lockFocus() {
		captureState = STATE_WAIT_LOCK;
		captureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
				CaptureRequest.CONTROL_AF_TRIGGER_START);
		try {
			previewCaptureSession.capture(captureRequestBuilder.build(), previewCaptureCallback,
					backgroundHandler);
		} catch (CameraAccessException e) {
			e.printStackTrace();
		}
	}

	private void unLockFocus() {
		captureState = STATE_PREVIEW;
		captureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
				CaptureRequest.CONTROL_AF_TRIGGER_CANCEL);
		try {
			previewCaptureSession.capture(captureRequestBuilder.build(), previewCaptureCallback,
					backgroundHandler);
		} catch (CameraAccessException e) {
			e.printStackTrace();
		}
	}

	@Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case PREVIEW_IMAGE_RESULT:
				// Get result from the preview screen
				// Set the absolute image path as the return value for the camera intent
				if (resultCode == Activity.RESULT_OK) {
					cameraPreviewResult = data.getStringExtra(IMAGE_SAVED_PATH);
					Intent resultIntent = new Intent();
					resultIntent.putExtra(IMAGE_SAVED_PATH, cameraPreviewResult);
					setResult(Activity.RESULT_OK, resultIntent);
					Log.d(TAG, "Success: " + cameraPreviewResult);
				} else if (resultCode == Activity.RESULT_CANCELED) {
					// Set the response of the camera intent to result canceled.
					setResult(Activity.RESULT_CANCELED);
					Log.d(TAG, "Image deleted by user!");
				}
				break;
		}
	}

	@Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
			@NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		switch (requestCode) {
			case REQUEST_CAMERA_PERMISSION:
				if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
					Toast.makeText(getApplicationContext(), "App can't run without camera permissions.",
							Toast.LENGTH_SHORT).show();
				} else {
					Log.d(TAG, "Camera permission granted successfully");
				}
				break;
			case REQUEST_STORAGE_PERMISSION:
				if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					try {
						if (isRecording) {
							createVideoFileName();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
					Log.d(TAG, "Storage permission granted successfully");
				} else {
					Toast.makeText(getApplicationContext(), "App can't run without storage permissions.",
							Toast.LENGTH_SHORT).show();
					Log.d(TAG, "Storage permissions denied.");
				}
				break;
			case REQUEST_AUDIO_PERMISSION:
				if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					Log.d(TAG, "Audio permission granted successfully");
				} else {
					Toast.makeText(getApplicationContext(), "App needs to record audio.", Toast.LENGTH_SHORT)
							.show();
					Log.d(TAG, "Audio permissions denied.");
				}
				break;
		}
	}

	private void startStillCapture() {
		try {
			captureRequestBuilder =
					cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
			captureRequestBuilder.addTarget(imageReader.getSurface());
			captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION,
					totalRotation); // Fix orientation skews

			switch (flashStatus) {
				case 0:
					captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
							CameraMetadata.CONTROL_AE_MODE_ON);
					captureRequestBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
					break;
				case 1:
					captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
							CameraMetadata.CONTROL_AE_MODE_ON_ALWAYS_FLASH);
					captureRequestBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_SINGLE);
					break;
				case 2:
					captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
							CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
					captureRequestBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
					break;
			}

			captureRequestBuilder.set(CaptureRequest.SCALER_CROP_REGION, zoom);
			captureRequestBuilder.set(CaptureRequest.CONTROL_AE_LOCK, false);
			captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON);
			captureRequestBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION,
					(int) progressValue);
			captureRequestBuilder.set(CaptureRequest.CONTROL_AE_LOCK, true);

			CameraCaptureSession.CaptureCallback stillCaptureCallback =
					new CameraCaptureSession.CaptureCallback() {
						@Override
						public void onCaptureStarted(@NonNull CameraCaptureSession session,
								@NonNull CaptureRequest request, long timestamp, long frameNumber) {
							super.onCaptureStarted(session, request, timestamp, frameNumber);
							try {
								createImageFileName();
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

	private void setUpMediaRecorder() throws IOException {
		mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
		mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
		mediaRecorder.setOutputFile(videoFileName);
		mediaRecorder.setVideoEncodingBitRate(2500000);
		mediaRecorder.setVideoFrameRate(30);
		mediaRecorder.setVideoSize(videoSize.getWidth(), videoSize.getHeight());
		mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
		mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
		mediaRecorder.setOrientationHint(totalRotation);
		mediaRecorder.prepare();
	}

	private void swapCamID() {
		if (camLensFacing == CameraCharacteristics.LENS_FACING_BACK) {
			camLensFacing = CameraCharacteristics.LENS_FACING_FRONT;
		} else {
			camLensFacing = CameraCharacteristics.LENS_FACING_BACK;
		}
		closeCamera();
		setUpCamera(textureView.getWidth(), textureView.getHeight());
		connectCamera();
	}

	private void initializeCameraInterface() {
		switcher1.setFactory(new ViewSwitcher.ViewFactory() {
			@Override public View makeView() {
				ImageView imageView = new ImageView(getApplicationContext());
				imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
				return imageView;
			}
		});

		switcher2.setFactory(new ViewSwitcher.ViewFactory() {
			@Override public View makeView() {
				ImageView imageView = new ImageView(getApplicationContext());
				imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
				return imageView;
			}
		});

		switcher3.setFactory(new ViewSwitcher.ViewFactory() {
			@Override public View makeView() {
				ImageView imageView = new ImageView(getApplicationContext());
				imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
				return imageView;
			}
		});

		switcher4.setFactory(new ViewSwitcher.ViewFactory() {
			@Override public View makeView() {
				ImageView imageView = new ImageView(getApplicationContext());
				imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
				return imageView;
			}
		});

		switcher5.setFactory(new ViewSwitcher.ViewFactory() {
			@Override public View makeView() {
				ImageView imageView = new ImageView(getApplicationContext());
				imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
				return imageView;
			}
		});

		switcher1.setImageResource(R.drawable.ic_circular);
		switcher2.setImageResource(R.drawable.ic_circular);
		switcher3.setImageResource(R.drawable.ic_selected_circular);
		switcher4.setImageResource(R.drawable.ic_circular);
		switcher5.setImageResource(R.drawable.ic_circular);
		swipeScenes(selectedScene, prevScene);
	}

	private void transformImage(int width, int height) {
		if (prevScene == null || textureView == null) {
			return;
		}

		Matrix matrix = new Matrix();
		int rotation = getWindowManager().getDefaultDisplay().getRotation();
		RectF textureRectF = new RectF(0, 0, width, height);
		RectF previewRectF = new RectF(0, 0, previewSize.getHeight(), previewSize.getWidth());
		float centerX = textureRectF.centerX();
		float centerY = textureRectF.centerY();

		if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) {
			previewRectF.offset(centerX - previewRectF.centerX(), centerY - previewRectF.centerY());
			matrix.setRectToRect(textureRectF, previewRectF, Matrix.ScaleToFit.FILL);
			float scale = Math.max((float) width / previewSize.getWidth(),
					(float) height / previewSize.getHeight());
			matrix.postScale(scale, scale, centerX, centerY);
			matrix.postRotate(90 * (rotation - 2), centerX, centerY);
		}
		textureView.setTransform(matrix);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		View decorView = getWindow().getDecorView();
		if (hasFocus) {
			decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
					| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
					| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
					| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_FULLSCREEN
					| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
		}
	}

	@Override public boolean onTouchEvent(MotionEvent event) {
		this.gestureObject.onTouchEvent(event);
		if (!isRecording) {
			this.scaleGestureDetector.onTouchEvent(event);
		}
		return super.onTouchEvent(event);
	}

	public void openImage() {
		Intent sendFileAddressIntent = new Intent(this, ViewImageActivity.class);
		sendFileAddressIntent.putExtra(IMAGE_FILE_LOCATION, imageFileName);
		startActivityForResult(sendFileAddressIntent, PREVIEW_IMAGE_RESULT);
	}

	private void zoomIn(int zoomLevel) {
		int minWidth = (int) (activePixesAfter.width() / maxDigitalZoom);
		int minHeight = (int) (activePixesAfter.height() / maxDigitalZoom);
		int widthDiff = activePixesAfter.width() - minWidth;
		int heightDiff = activePixesAfter.height() - minHeight;
		int cropWidth = widthDiff / 100 * (int) zoomLevel;
		int cropHeight = heightDiff / 100 * (int) zoomLevel;

		cropWidth -= cropHeight & 3;
		cropHeight -= cropHeight & 3;
		zoom = new Rect(cropWidth, cropHeight, activePixesAfter.width() - cropWidth,
				activePixesAfter.height() - cropHeight);
		captureRequestBuilder.set(CaptureRequest.SCALER_CROP_REGION, zoom);
		applySettings();
	}

	private class ScaleListener implements ScaleGestureDetector.OnScaleGestureListener {
		float onScaleBegin = 0;
		float onScaleEnd = 0;

		@Override public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
			if (scale >= maxDigitalZoom) {
				scale = maxDigitalZoom;
				onScaleBegin = 0;
				onScaleEnd = 0;
			} else if (scale < 1) {
				scale = 1f;
				onScaleBegin = 0;
				onScaleEnd = 0;
			}
			zoomCaption.setVisibility(View.VISIBLE);
			scale *= scaleGestureDetector.getScaleFactor();
			zoomCaption.setText(String.format("Zoom: %s", String.valueOf((int) scale)));
			return true;
		}

		@Override public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
			onScaleBegin = scale;
			return true;
		}

		@Override public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
			onScaleEnd = scale;
			if (onScaleEnd > onScaleBegin) {
				Log.d(TAG, "Scaled up by: " + String.valueOf(onScaleEnd / onScaleBegin));
				Log.d(TAG, "Max Zoom: " + String.valueOf(maxDigitalZoom));
			} else {
				Log.d(TAG, "Scaled down by: " + String.valueOf(onScaleBegin / onScaleEnd));
			}
			zoomIn((int) onScaleEnd);
			zoomCaption.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera);

		gestureObject = new GestureDetectorCompat(this, new LearnGesture());
		scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());

		initializeObjects();
		// Initializes the scenes with the relevant scene images
		initializeScenes();

		Toast.makeText(getApplicationContext(), R.string.interaction_scene, Toast.LENGTH_SHORT).show();
		currentScene = INTERACTION_SCENE;

		// Creates the swipe buttons and initializes the initial overlay image
		initializeCameraInterface();

		lightSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				progressValue = round(((double) (progress - PROGRESS_MIN) / PROGRESS_MIN) * aeRange, 2);
				seekBarProgressText.setText(String.format("%s", Double.toString(progressValue)));
			}

			@Override public void onStartTrackingTouch(SeekBar seekBar) {
				seekBarProgressText.setVisibility(View.VISIBLE);
			}

			@Override public void onStopTrackingTouch(SeekBar seekBar) {
				increaseBrightness(progressValue);
				seekBarProgressText.setVisibility(View.INVISIBLE);
			}
		});

		switcher1.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View view) {
				prevScene = selectedScene;
				selectedScene = 0;
				setSceneAdapter(PORTRAIT_SCENE);
				prevScene = 0;
			}
		});

		switcher2.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View view) {
				prevScene = selectedScene;
				selectedScene = 1;
				setSceneAdapter(SIGNATURE_SCENE);
				prevScene = 1;
			}
		});

		switcher3.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View view) {
				prevScene = selectedScene;
				selectedScene = 2;
				setSceneAdapter(INTERACTION_SCENE);
				prevScene = 2;
			}
		});

		switcher4.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View view) {
				prevScene = selectedScene;
				selectedScene = 3;
				setSceneAdapter(CANDID_SCENE);
				prevScene = 3;
			}
		});

		switcher5.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View view) {
				prevScene = selectedScene;
				selectedScene = 4;
				setSceneAdapter(ENVIRONMENT_SCENE);
				prevScene = 4;
			}
		});

		capturePictureBtn.setOnLongClickListener(new View.OnLongClickListener() {
			@Override public boolean onLongClick(View view) {
				// Record a video for long press
				hideSceneIcons();

				if (moreScenes) {
					hideSceneSwitcher();
				}

				MediaActionSound sound = new MediaActionSound();
				sound.play(MediaActionSound.START_VIDEO_RECORDING);

				isRecording = true;
				imgOverlay.setImageDrawable(null);
				swipeText.setText(R.string.recording_status);

				capturePictureBtn.setImageResource(R.drawable.ic_video_record);
				checkWriteStoragePermission();
				return true;
			}
		});

		capturePictureBtn.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View view) {
				if (!isRecording) {
					// Take a picture if the user just clicks
					checkWriteStoragePermission();
					lockFocus();
				} else {
					// Stop video recording, set back the capture icon
					showSceneIcons();
					MediaActionSound sound = new MediaActionSound();
					sound.play(MediaActionSound.STOP_VIDEO_RECORDING);
					swipeText.setText(R.string.scene_changer_text);

					chronometer.stop();
					chronometer.setVisibility(View.INVISIBLE);

					isRecording = false;
					capturePictureBtn.setImageResource(R.drawable.camera_capture);
					swipeScenes(selectedScene, prevScene);

					startPreview();
					mediaRecorder.stop();
					mediaRecorder.reset();

					Intent mediaStoreUpdateIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
					mediaStoreUpdateIntent.setData(Uri.fromFile(new File(videoFileName)));
					sendBroadcast(mediaStoreUpdateIntent);
					createVideoReturnIntent();
				}
			}
		});

		openGalleryBtn.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View view) {
				Intent galleryIntent =
						new Intent(Intent.ACTION_VIEW, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				startActivity(galleryIntent);
			}
		});

		swapCameraBtn.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View view) {
				swapCamID();
			}
		});

		flashModeBtn.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View view) {
				swapFlashMode();
			}
		});
	}

	private void initializeObjects() {
		mediaRecorder = new MediaRecorder();
		sceneRecyclerView = (RecyclerView) findViewById(R.id.scene_recylcer_view);
		layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
		sceneRecyclerView.setLayoutManager(layoutManager);

		chronometer = (Chronometer) findViewById(R.id.chronometer2);
		textureView = (TextureView) findViewById(R.id.tv_camera);
		capturePictureBtn = (ImageView) findViewById(R.id.img_capture);
		openGalleryBtn = (ImageView) findViewById(R.id.img_gallery);
		swapCameraBtn = (ImageView) findViewById(R.id.img_switch_camera);
		flashModeBtn = (ImageView) findViewById(R.id.img_flash_btn);
		swipeText = (TextView) findViewById(R.id.txt_swipe_caption);

		switcher1 = (ImageSwitcher) findViewById(R.id.sw_swipe_1);
		switcher2 = (ImageSwitcher) findViewById(R.id.sw_swipe_2);
		switcher3 = (ImageSwitcher) findViewById(R.id.sw_swipe_3);
		switcher4 = (ImageSwitcher) findViewById(R.id.sw_swipe_4);
		switcher5 = (ImageSwitcher) findViewById(R.id.sw_swipe_5);
		imgOverlay = (ImageView) findViewById(R.id.img_overlay);

		seekBarProgressText = (TextView) findViewById(R.id.txt_seekbar_progress);
		lightSeekBar = (SeekBar) findViewById(R.id.seekbar_light);
		zoomCaption = (TextView) findViewById(R.id.txt_zoom_caption);
	}

	private void increaseBrightness(double progressValue) {
		captureRequestBuilder.set(CaptureRequest.CONTROL_AE_LOCK, false);
		captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON);
		captureRequestBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, (int) progressValue);
		captureRequestBuilder.set(CaptureRequest.CONTROL_AE_LOCK, true);
		applySettings();
	}

	private void applySettings() {
		try {
			previewCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, null);
		} catch (CameraAccessException e) {
			e.printStackTrace();
		}
	}

	private double round(double value, int places) {
		if (places < 0) throw new IllegalArgumentException();

		long factor = (long) Math.pow(10, places);
		value = value * factor;
		long tmp = Math.round(value);
		return (double) tmp / factor;
	}

	private void setSceneAdapter(String scene) {
		swipeScenes(selectedScene, prevScene);
		showSceneSwitcher();
		sceneSelectorAdapter =
				new SceneSelectorAdapter(CameraActivity.this, scene, overlayScenes.get(scene));
		sceneRecyclerView.setAdapter(sceneSelectorAdapter);
	}

	private void createVideoReturnIntent() {
		Intent resultIntent = new Intent();
		resultIntent.putExtra(VIDEO_SAVED_PATH, videoFileName);
		setResult(Activity.RESULT_OK, resultIntent);
		Log.d(TAG, "Success: " + videoFileName);
	}

	private void hideSceneIcons() {
		switcher1.setVisibility(View.INVISIBLE);
		switcher2.setVisibility(View.INVISIBLE);
		switcher3.setVisibility(View.INVISIBLE);
		switcher4.setVisibility(View.INVISIBLE);
		switcher5.setVisibility(View.INVISIBLE);
		flashModeBtn.setVisibility(View.INVISIBLE);
		lightSeekBar.setVisibility(View.INVISIBLE);
	}

	private void showSceneIcons() {
		switcher1.setVisibility(View.VISIBLE);
		switcher2.setVisibility(View.VISIBLE);
		switcher3.setVisibility(View.VISIBLE);
		switcher4.setVisibility(View.VISIBLE);
		switcher5.setVisibility(View.VISIBLE);
		flashModeBtn.setVisibility(View.VISIBLE);
		lightSeekBar.setVisibility(View.VISIBLE);
	}

	private void initializeScenes() {
		seekBarProgressText.setVisibility(View.INVISIBLE);
		zoomCaption.setVisibility(View.INVISIBLE);
		hideSceneSwitcher();
		portrait = new ArrayList<Integer>() {
			{
				add(R.drawable.portrait_001);
				add(R.drawable.portrait_002);
				add(R.drawable.portrait_003);
				add(R.drawable.portrait_004);
				add(R.drawable.portrait_005);
				add(R.drawable.portrait_006);
				add(R.drawable.portrait_007);
			}
		};

		signature = new ArrayList<Integer>() {
			{
				add(R.drawable.signature_001);
				add(R.drawable.signature_002);
				add(R.drawable.signature_003);
				add(R.drawable.signature_004);
				add(R.drawable.signature_005);
			}
		};

		interaction = new ArrayList<Integer>() {
			{
				add(R.drawable.interaction_001);
				add(R.drawable.interaction_002);
				add(R.drawable.interaction_003);
			}
		};

		candid = new ArrayList<Integer>() {
			{
				add(R.drawable.candid_001);
				add(R.drawable.candid_002);
				add(R.drawable.candid_003);
			}
		};

		environment = new ArrayList<Integer>() {
			{
				add(R.drawable.environment_001);
				add(R.drawable.environment_002);
				add(R.drawable.environment_003);
			}
		};

		overlayScenes = new HashMap<String, ArrayList<Integer>>() {
			{
				put(PORTRAIT_SCENE, portrait);
				put(SIGNATURE_SCENE, signature);
				put(INTERACTION_SCENE, interaction);
				put(CANDID_SCENE, candid);
				put(ENVIRONMENT_SCENE, environment);
			}
		};
	}

	private void hideSceneSwitcher() {
		sceneRecyclerView.setVisibility(View.GONE);
		moreScenes = false;
	}

	private void showSceneSwitcher() {
		sceneRecyclerView.setVisibility(View.VISIBLE);
		moreScenes = true;
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
	}

	@Override protected void onResume() {
		super.onResume();

		startBackgroundThread();

		if (textureView.isAvailable()) {
			setUpCamera(textureView.getWidth(), textureView.getHeight());
			transformImage(textureView.getWidth(), textureView.getHeight());
			connectCamera();
		} else {
			textureView.setSurfaceTextureListener(surfaceTextureListener);
		}
	}

	public class LearnGesture extends GestureDetector.SimpleOnGestureListener {

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			if (moreScenes) {
				hideSceneSwitcher();
			}

			if (!isRecording) {
				prevScene = selectedScene;
				if (e2.getX() > e1.getX()) {
					// Left to Right swipe
					selectedScene += 1;
					swipeScenes(selectedScene, prevScene);
				} else if (e2.getX() < e1.getX()) {
					// Right to Left swipe
					selectedScene -= 1;
					if (selectedScene < 0) {
						selectedScene = 4;
					}
					swipeScenes(selectedScene, prevScene);
				}
			}
			return super.onFling(e1, e2, velocityX, velocityY);
		}

		@Override public void onLongPress(MotionEvent e) {
			// Show child scenes on long pressing the screen.;
			super.onLongPress(e);
			showSceneSwitcher();
			sceneSelectorAdapter = new SceneSelectorAdapter(CameraActivity.this, currentScene,
					overlayScenes.get(currentScene));
			sceneRecyclerView.setAdapter(sceneSelectorAdapter);
		}

		@Override public boolean onSingleTapUp(MotionEvent e) {
			if (manualFocusEngaged) {
				Log.d(TAG, "Manual focus already engaged!");
				return true;
			}

			final int y =
					(int) ((e.getX() / (float) textureView.getWidth()) * (float) sensorArraySize.height());
			final int x =
					(int) ((e.getY() / (float) textureView.getHeight()) * (float) sensorArraySize.width());
			final int halfTouchWidth = (int) e.getTouchMajor(); // 150;
			final int halfTouchHeight = (int) e.getTouchMinor(); //150;
			MeteringRectangle focusArea = new MeteringRectangle(
					Math.max(x - halfTouchWidth, 0),
					Math.max(y - halfTouchHeight, 0),
					halfTouchWidth * 2,
					halfTouchHeight * 2,
					MeteringRectangle.METERING_WEIGHT_MAX - 1
			);

			try {
				previewCaptureSession.stopRepeating();
			} catch (CameraAccessException e1) {
				e1.printStackTrace();
			}

			captureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
					CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
			captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CameraMetadata.CONTROL_AF_MODE_OFF);

			try {
				previewCaptureSession.capture(captureRequestBuilder.build(), previewCaptureCallback,
						backgroundHandler);
			} catch (CameraAccessException e1) {
				e1.printStackTrace();
			}

			if (isMeteringAFAreaSupported) {
				captureRequestBuilder.set(CaptureRequest.CONTROL_AF_REGIONS,
						new MeteringRectangle[] { focusArea });
			}

			captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
			captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
					CameraMetadata.CONTROL_AF_MODE_AUTO);
			captureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
					CameraMetadata.CONTROL_AF_TRIGGER_START);
			captureRequestBuilder.setTag("FOCUS_TAG");

			try {
				previewCaptureSession.capture(captureRequestBuilder.build(), previewCaptureCallback,
						backgroundHandler);
			} catch (CameraAccessException e1) {
				e1.printStackTrace();
			}
			manualFocusEngaged = true;
			return true;
		}
	}

	public void swipeScenes(Integer nextScene, Integer prevScene) {
		switch (prevScene) {
			case 0:
				switcher1.setImageResource(unSelected);
				break;
			case 1:
				switcher2.setImageResource(unSelected);
				break;
			case 2:
				switcher3.setImageResource(unSelected);
				break;
			case 3:
				switcher4.setImageResource(unSelected);
				break;
			case 4:
				switcher5.setImageResource(unSelected);
				break;
			default:
				selectedScene = 0;
				switcher1.setImageResource(unSelected);
				break;
		}
		switch (nextScene) {
			case 0:
				switcher1.setImageResource(selected);
				loadOverlayImage(PORTRAIT_SCENE);
				break;
			case 1:
				switcher2.setImageResource(selected);
				loadOverlayImage(SIGNATURE_SCENE);
				break;
			case 2:
				switcher3.setImageResource(selected);
				loadOverlayImage(INTERACTION_SCENE);
				break;
			case 3:
				switcher4.setImageResource(selected);
				loadOverlayImage(CANDID_SCENE);
				break;
			case 4:
				switcher5.setImageResource(selected);
				loadOverlayImage(ENVIRONMENT_SCENE);
				break;
			default:
				selectedScene = 0;
				switcher1.setImageResource(selected);
				loadOverlayImage(PORTRAIT_SCENE);
				break;
		}
	}

	private void loadOverlayImage(String scene) {
		int imgID = overlayScenes.get(scene).get(0);
		currentScene = scene;
		swipeText.setText(scene);
		GlideApp.with(this).load(imgID).placeholder(imgID).centerCrop().into(imgOverlay);
	}
}
