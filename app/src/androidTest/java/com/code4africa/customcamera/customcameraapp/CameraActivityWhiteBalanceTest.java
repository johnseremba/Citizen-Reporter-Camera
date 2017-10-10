package com.code4africa.customcamera.customcameraapp;

import android.support.test.espresso.DataInteraction;
import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class CameraActivityWhiteBalanceTest {

	@Rule
	public ActivityTestRule<CameraActivity> mActivityTestRule =
			new ActivityTestRule<>(CameraActivity.class);

	@Test
	public void whiteBalanceTest() {
		onView(withId(R.id.tv_camera)).perform(click());
		onView(withId(R.id.img_wb_btn))
				.check(matches(isDisplayed()))
				.perform(click());
		onView(withText("White Balance")).check(matches(isDisplayed()));
		onData(allOf(is(instanceOf(String.class)), is("Auto"))).perform(click());
		CameraActivity cameraActivity = mActivityTestRule.getActivity();
		assertTrue(cameraActivity.checkWhiteBalanceStatus("Auto"));
	}

	@Test
	public void colorEffectsTest() {
		onView(withId(R.id.tv_camera)).perform(click());
		onView(withId(R.id.img_effects_btn))
				.check(matches(isDisplayed()))
				.perform(click());
		onView(withText("Color Filters")).check(matches(isDisplayed()));
		onData(allOf(is(instanceOf(String.class)), is("Off"))).perform(click());
		CameraActivity cameraActivity = mActivityTestRule.getActivity();
		assertTrue(cameraActivity.checkCurrentColorEffect("Off"));
	}
}
