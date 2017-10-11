package com.code4africa.customcamera.customcameraapp;

import android.content.res.Resources;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class CameraActivityWhiteBalanceTest {

	@Rule
	public ActivityTestRule<CameraActivity> mCameraActivity =
			new ActivityTestRule<>(CameraActivity.class);

	@Test
	public void whiteBalanceTest() {
		final Resources resources = mCameraActivity.getActivity().getResources();
		onView(withId(R.id.tv_camera)).perform(click());
		onView(withId(R.id.img_wb_btn))
				.check(matches(isDisplayed()))
				.perform(click());
		onView(withText(resources.getString(R.string.wb_title))).check(matches(isDisplayed()));
		onData(allOf(is(instanceOf(String.class)), is(resources.getString(R.string.wb_auto)))).perform(
				click());
		CameraActivity cameraActivity = mCameraActivity.getActivity();
		assertTrue(cameraActivity.checkWhiteBalanceStatus(resources.getString(R.string.wb_auto)));
	}

	@Test
	public void colorEffectsTest() {
		final Resources resources = mCameraActivity.getActivity().getResources();
		onView(withId(R.id.tv_camera)).perform(click());
		onView(withId(R.id.img_effects_btn))
				.check(matches(isDisplayed()))
				.perform(click());
		onView(withText(resources.getString(R.string.color_effects_title))).check(
				matches(isDisplayed()));
		onData(allOf(is(instanceOf(String.class)), is(resources.getString(R.string.color_effects_off))))
				.perform(click());
		CameraActivity cameraActivity = mCameraActivity.getActivity();
		assertTrue(
				cameraActivity.checkCurrentColorEffect(resources.getString(R.string.color_effects_off)));
	}
}
