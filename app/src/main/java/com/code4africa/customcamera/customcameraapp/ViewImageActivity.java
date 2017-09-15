package com.code4africa.customcamera.customcameraapp;

import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;
import java.io.File;

public class ViewImageActivity extends AppCompatActivity {
	private static final String IMAGE_FILE_LOCATION = "image_file_location";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_view_image);

		DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		int width = displayMetrics.widthPixels;
		int height = displayMetrics.heightPixels;

		ImageView imageView = new ImageView(this);
		ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(width, height);
		imageView.setLayoutParams(params);
		setContentView(imageView);

		File imageFile = new File(
				getIntent().getStringExtra(IMAGE_FILE_LOCATION)
		);

		Log.d("ImgView", "Intent started : " + imageFile);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
	}
}