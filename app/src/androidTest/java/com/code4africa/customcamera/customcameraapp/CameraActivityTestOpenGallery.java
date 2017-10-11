package com.code4africa.customcamera.customcameraapp;

import android.content.Intent;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.intent.matcher.IntentMatchers.toPackage;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by johnpaulseremba on 11/10/2017.
 */

@LargeTest
@RunWith(AndroidJUnit4.class)
public class CameraActivityTestOpenGallery {
	@Rule
	public IntentsTestRule<CameraActivity> mCameraActivity =
			new IntentsTestRule<>(CameraActivity.class);

	@Test
	public void openGalleryIntentTest() {
		// initialize Camera
		onView(withId(R.id.tv_camera)).check(matches(isDisplayed())).perform(click());
		onView(withId(R.id.img_gallery)).check(matches(isDisplayed())).perform(click());
		intending(toPackage("com.android.gallery"));
	}
}
