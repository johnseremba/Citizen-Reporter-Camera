package com.code4africa.customcamera.customcameraapp;

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by johnpaulseremba on 05/10/2017.
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class CapturePictureTest {
	@Rule
	public ActivityTestRule<CameraActivity> mCameraActivity =
			new ActivityTestRule<CameraActivity>(CameraActivity.class);

	@Test
	public void openCamera() {
		onView(withId(R.id.tv_camera)).check(matches(isDisplayed()));
		onView(withId(R.id.img_capture)).perform(click());
		//intended()
		//onView(withId(R.id.img_view)).check(matches(isDisplayed()));
	}

}
