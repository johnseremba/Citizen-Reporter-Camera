package com.code4africa.customcamera.customcameraapp;

import android.os.Build;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.InstrumentationRegistry.getTargetContext;
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
			new ActivityTestRule<>(CameraActivity.class);

	@Before
	public void grantCameraPermission() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			getInstrumentation().getUiAutomation().executeShellCommand(
					"pm grant " + getTargetContext().getPackageName()
							+ " android.permission.CAMERA"
			);
			getInstrumentation().getUiAutomation().executeShellCommand(
					"pm grant " + getTargetContext().getPackageName()
							+ " android.permission.WRITE_EXTERNAL_STORAGE"
			);
		}
	}

	@Test
	public void takePictureTest() {
		onView(withId(R.id.tv_camera)).perform(click());
		onView(withId(R.id.img_capture)).check(matches(isDisplayed()));
		onView(withId(R.id.img_capture)).perform(click());
	}
}
