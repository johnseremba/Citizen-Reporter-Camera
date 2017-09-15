package com.code4africa.customcamera.customcameraapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
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
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.hardware.camera2.CameraDevice;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;
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

public class CameraActivity extends AppCompatActivity {
	private static final String TAG = CameraActivity.class.getSimpleName();
	private static final int REQUEST_CAMERA_PERMISSION = 1;
	private static final int REQUEST_STORAGE_PERMISSION = 2;
	private static final int STATE_PREVIEW = 0;
	private static final int STATE_WAIT_LOCK = 1;
	private int captureState = STATE_PREVIEW;
	private TextureView textureView;
	private ImageView capturePictureBtn;
	private ImageView openGalleryBtn;
	private ImageView swapCameraBtn;
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
	int camLensFacing = CameraCharacteristics.LENS_FACING_BACK;
	private boolean isRecording = false;

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
			process(result);
		}

		private void process(CaptureResult captureResult) {
			switch (captureState) {
				case STATE_PREVIEW:
					//
					break;
				case STATE_WAIT_LOCK:
					captureState = STATE_PREVIEW;
					Integer afState = captureResult.get(CaptureResult.CONTROL_AF_STATE);
					if (afState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED ||
								afState == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED) {
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
			if(isRecording) {
				try {
					createVideoFileName();
				} catch (IOException e) {
					e.printStackTrace();
				}
				startRecord();
				mediaRecorder.start();
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
						camLensFacing) {
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
					videoSize = chooseOptimalSize(map.getOutputSizes(MediaRecorder.class), rotatedWidth, rotatedHeight);
					imageSize = chooseOptimalSize(map.getOutputSizes(ImageFormat.JPEG), rotatedWidth, rotatedHeight);
					imageReader = ImageReader.newInstance(imageSize.getWidth(), imageSize.getHeight(), ImageFormat.JPEG, 1);
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
									cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, null);
								} catch (CameraAccessException e) {
									e.printStackTrace();
								}
							}

							@Override
							public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

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

	public void createImageFolder() {
		File imageFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
		imageFolder = new File(imageFile, "Code4Africa");
		if(!imageFolder.exists()) {
			boolean result = imageFolder.mkdirs();
			if(result) {
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
		if(!videoFolder.exists()) {
			boolean result = videoFolder.mkdir();
			if(result){
				Log.d(TAG, "C4A video folder created successfully!");
			} else {
				Log.d(TAG, "C4A video directory already exists");
			}
		}
	}

	public File createVideoFileName() throws IOException {
		String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String prepend = "VID_" + timestamp;
		File videoFile = File.createTempFile(prepend, ".mp4", imageFolder);
		videoFileName = videoFile.getAbsolutePath();
		Log.d(TAG, "Video Name: " + videoFileName);
		Log.d(TAG, "Video folder: " + videoFolder);
		return videoFile;
	}

	public File createImageFileName() throws IOException {
		String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String prepend = "IMG_" + timestamp;
		File imageFile = File.createTempFile(prepend, ".jpg", imageFolder);
		imageFileName = imageFile.getAbsolutePath();
		Log.d(TAG, "Image Name: " + imageFileName);
		Log.d(TAG, "Image folder: " + imageFolder);
		return imageFile;
	}

	private void checkWriteStoragePermission() {
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
			if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
					== PackageManager.PERMISSION_GRANTED){
				Log.d(TAG, "External storage permissions granted");
				if(!isRecording) {
					lockFocus();
				} else {
					startRecord();
					mediaRecorder.start();
				}
			} else {
					if(shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
						Toast.makeText(this, "App needs to store pictures & videos", Toast.LENGTH_SHORT);
					}
					Log.d(TAG, "No external storage permissions");
					requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
			}
		} else {
			if(!isRecording) {
				lockFocus();
			} else {
				startRecord();
				mediaRecorder.start();
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
						createImageFileName();
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
		mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
		mediaRecorder.setOutputFile(videoFileName);
		mediaRecorder.setVideoEncodingBitRate(1000000);
		mediaRecorder.setVideoFrameRate(30);
		mediaRecorder.setVideoSize(videoSize.getWidth(), videoSize.getHeight());
		mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
		mediaRecorder.setOrientationHint(totalRotation);
		mediaRecorder.prepare();
	}

	private void swapCamID(){
		if(camLensFacing == CameraCharacteristics.LENS_FACING_BACK) {
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
				return  imageView;
			}
		});

		switcher2.setFactory(new ViewSwitcher.ViewFactory() {
			@Override public View makeView() {
				ImageView imageView = new ImageView(getApplicationContext());
				imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
				return  imageView;
			}
		});

		switcher3.setFactory(new ViewSwitcher.ViewFactory() {
			@Override public View makeView() {
				ImageView imageView = new ImageView(getApplicationContext());
				imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
				return  imageView;
			}
		});

		switcher4.setFactory(new ViewSwitcher.ViewFactory() {
			@Override public View makeView() {
				ImageView imageView = new ImageView(getApplicationContext());
				imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
				return  imageView;
			}
		});

		switcher5.setFactory(new ViewSwitcher.ViewFactory() {
			@Override public View makeView() {
				ImageView imageView = new ImageView(getApplicationContext());
				imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
				return  imageView;
			}
		});

		switcher1.setImageResource(R.drawable.ic_circular);
		switcher2.setImageResource(R.drawable.ic_circular);
		switcher3.setImageResource(R.drawable.ic_selected_circular);
		switcher4.setImageResource(R.drawable.ic_circular);
		switcher5.setImageResource(R.drawable.ic_circular);
		imgOverlay.setImageResource(R.drawable.interaction_001);

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

	@Override public boolean onTouchEvent(MotionEvent event) {
		this.gestureObject.onTouchEvent(event);
		return super.onTouchEvent(event);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera);

		gestureObject = new GestureDetectorCompat(this, new LearnGesture());

		createImageFolder();
		createVideoFolder();

		mediaRecorder = new MediaRecorder();

		textureView = (TextureView) findViewById(R.id.tv_camera);
		capturePictureBtn = (ImageView) findViewById(R.id.img_capture);
		openGalleryBtn = (ImageView) findViewById(R.id.img_gallery);
		swapCameraBtn = (ImageView) findViewById(R.id.img_switch_camera);
		swipeText = (TextView) findViewById(R.id.txt_swipe_caption);

		switcher1 = (ImageSwitcher) findViewById(R.id.sw_swipe_1);
		switcher2 = (ImageSwitcher) findViewById(R.id.sw_swipe_2);
		switcher3 = (ImageSwitcher) findViewById(R.id.sw_swipe_3);
		switcher4 = (ImageSwitcher) findViewById(R.id.sw_swipe_4);
		switcher5 = (ImageSwitcher) findViewById(R.id.sw_swipe_5);
		imgOverlay = (ImageView) findViewById(R.id.img_overlay);

		// Initializes the scenes with the relevant scene images
		initializeScenes();

		Toast.makeText(getApplicationContext(), "Interaction Scene", Toast.LENGTH_SHORT).show();

		// Creates the swipe buttons and initializes the initial overlay image
		initializeCameraInterface();

		capturePictureBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				checkWriteStoragePermission();
			}
		});

		capturePictureBtn.setOnTouchListener(new View.OnTouchListener() {
			Float x1, x2, y1, y2;
			Long t1, t2;
			private long CLICK_DURATION = 400;

			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				switch (motionEvent.getAction()){
					case MotionEvent.ACTION_DOWN:
						x1 = motionEvent.getX();
						y1 = motionEvent.getY();
						t1 = System.currentTimeMillis();
						return true;
					case MotionEvent.ACTION_UP:
						x2 = motionEvent.getX();
						y2 = motionEvent.getY();
						t2 = System.currentTimeMillis();

						if(!isRecording){
							if((t2 - t1) >= CLICK_DURATION) {
								// Record a video for long press
								capturePictureBtn.setImageResource(R.drawable.ic_video_record);
								isRecording = true;
							} else {
								// Take a picture if the user just clicks
								checkWriteStoragePermission();
							}
						} else {
							// Stop video recording, set back the capture icon
							isRecording = false;
							capturePictureBtn.setImageResource(R.drawable.camera_capture);
							mediaRecorder.stop();
							mediaRecorder.reset();
							startPreview();
						}
						return true;
				}
				return false;
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

	}


	private void initializeScenes() {
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

		overlayScenes = new HashMap<String, ArrayList<Integer>>(){
			{
				put("Portrait", portrait);
				put("Signature", signature);
				put("Interaction", interaction);
				put("Candid", candid);
				put("Environment", environment);
			}
		};
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

		public class LearnGesture extends GestureDetector.SimpleOnGestureListener {

			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
				prevScene = selectedScene;
				if (e2.getX() > e1.getX()) {
					// Left to Right swipe
					selectedScene += 1;
					swipeScenes(selectedScene, prevScene);
				} else if (e2.getX() < e1.getX()) {
					// Right to Left swipe
					selectedScene -= 1;
					if(selectedScene < 0) {
						selectedScene = 4;
					}
					swipeScenes(selectedScene, prevScene);
				}
				return super.onFling(e1, e2, velocityX, velocityY);
			}
		}

		public void swipeScenes(Integer nextScene, Integer prevScene) {
			switch (prevScene) {
				case 0:
					switcher1.setImageResource(R.drawable.ic_circular);
					break;
				case 1:
					switcher2.setImageResource(R.drawable.ic_circular);
					break;
				case 2:
					switcher3.setImageResource(R.drawable.ic_circular);
					break;
				case 3:
					switcher4.setImageResource(R.drawable.ic_circular);
					break;
				case 4:
					switcher5.setImageResource(R.drawable.ic_circular);
					break;
				default:
					selectedScene = 0;
					switcher1.setImageResource(R.drawable.ic_circular);
					break;
			}

			switch(nextScene) {
				case 0:
					switcher1.setImageResource(R.drawable.ic_selected_circular);
					swipeText.setText("Portrait");
					imgOverlay.setImageResource(overlayScenes.get("Portrait").get(0));
					break;
				case 1:
					switcher2.setImageResource(R.drawable.ic_selected_circular);
					swipeText.setText("Signature");
					imgOverlay.setImageResource(overlayScenes.get("Signature").get(0));
					break;
				case 2:
					switcher3.setImageResource(R.drawable.ic_selected_circular);
					swipeText.setText("Interaction");
					imgOverlay.setImageResource(overlayScenes.get("Interaction").get(0));
					break;
				case 3:
					switcher4.setImageResource(R.drawable.ic_selected_circular);
					swipeText.setText("Candid");
					imgOverlay.setImageResource(overlayScenes.get("Candid").get(0));
					break;
				case 4:
					switcher5.setImageResource(R.drawable.ic_selected_circular);
					swipeText.setText("Environment");
					imgOverlay.setImageResource(overlayScenes.get("Environment").get(0));
					break;
				default:
					selectedScene = 0;
					switcher1.setImageResource(R.drawable.ic_selected_circular);
					swipeText.setText("Portrait");
					imgOverlay.setImageResource(overlayScenes.get("Portrait").get(0));
					break;
			}
		}
}
