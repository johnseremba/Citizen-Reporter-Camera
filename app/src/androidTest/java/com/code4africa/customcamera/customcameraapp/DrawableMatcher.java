package com.code4africa.customcamera.customcameraapp;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * Created by johnpaulseremba on 10/10/2017.
 */

public class DrawableMatcher extends TypeSafeMatcher {
	private final int EXPECTED_ID;
	private final int EMPTY = -1;
	private final int ANY = -2;
	private String resourceName;

	public DrawableMatcher(int expectedID) {
		super(View.class);
		EXPECTED_ID = expectedID;
	}

	@Override protected boolean matchesSafely(Object item) {
		if (!(item instanceof ImageView)) {
			return false;
		}
		ImageView imageView = (ImageView) item;
		if (EXPECTED_ID == EMPTY) {
			return imageView.getDrawable() == null;
		}
		if (EXPECTED_ID == ANY) {
			return imageView.getDrawable() != null;
		}
		Resources resources = ((ImageView) item).getContext().getResources();
		Drawable expectedDrawable = resources.getDrawable(EXPECTED_ID);
		resourceName = resources.getResourceName(EXPECTED_ID);

		if (expectedDrawable == null) {
			return false;
		}

		Bitmap bitmap = getBitmap(imageView.getDrawable());
		Bitmap otherBitmap = getBitmap(expectedDrawable);
		return bitmap.sameAs(otherBitmap);
	}

	private Bitmap getBitmap(Drawable drawable) {
		Bitmap bitmap = Bitmap.createBitmap(
				drawable.getIntrinsicWidth(),
				drawable.getIntrinsicHeight(),
				Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);
		return bitmap;
	}

	@Override public void describeTo(Description description) {
		description.appendText("Drawable resource Id; ");
		description.appendValue(EXPECTED_ID);
		if (resourceName != null) {
			description.appendText("[" + resourceName + "]");
		}
	}
}
