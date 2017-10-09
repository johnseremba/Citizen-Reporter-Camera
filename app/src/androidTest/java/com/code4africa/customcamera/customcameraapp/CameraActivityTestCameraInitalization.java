package com.code4africa.customcamera.customcameraapp;

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

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class CameraActivityTestCameraInitalization {

	@Rule
	public ActivityTestRule<CameraActivity> mActivityTestRule =
			new ActivityTestRule<>(CameraActivity.class);

	@Test
	public void cameraActivityTestCameraInitalization() {
		ViewInteraction appCompatImageView = onView(
				allOf(withId(R.id.img_flash_btn),
						childAtPosition(
								allOf(withId(R.id.root_container),
										childAtPosition(
												withId(android.R.id.content),
												0)),
								14),
						isDisplayed()));
		appCompatImageView.perform(click());

		ViewInteraction imageView = onView(
				allOf(withId(R.id.img_flash_btn),
						childAtPosition(
								allOf(withId(R.id.root_container),
										childAtPosition(
												withId(android.R.id.content),
												0)),
								2),
						isDisplayed()));
		imageView.check(matches(isDisplayed()));

		ViewInteraction imageView2 = onView(
				allOf(withId(R.id.img_overlay_toggle),
						childAtPosition(
								allOf(withId(R.id.root_container),
										childAtPosition(
												withId(android.R.id.content),
												0)),
								3),
						isDisplayed()));
		imageView2.check(matches(isDisplayed()));

		ViewInteraction imageView3 = onView(
				allOf(withId(R.id.img_top_bg),
						childAtPosition(
								allOf(withId(R.id.root_container),
										childAtPosition(
												withId(android.R.id.content),
												0)),
								1),
						isDisplayed()));
		imageView3.check(matches(isDisplayed()));

		ViewInteraction view = onView(
				allOf(withId(R.id.tv_camera),
						childAtPosition(
								allOf(withId(R.id.root_container),
										childAtPosition(
												withId(android.R.id.content),
												0)),
								0),
						isDisplayed()));
		view.check(matches(isDisplayed()));

		ViewInteraction imageView4 = onView(
				allOf(withId(R.id.img_gallery),
						childAtPosition(
								allOf(withId(R.id.root_container),
										childAtPosition(
												withId(android.R.id.content),
												0)),
								5),
						isDisplayed()));
		imageView4.check(matches(isDisplayed()));

		ViewInteraction imageView5 = onView(
				allOf(withId(R.id.img_wb_btn),
						childAtPosition(
								allOf(withId(R.id.root_container),
										childAtPosition(
												withId(android.R.id.content),
												0)),
								6),
						isDisplayed()));
		imageView5.check(matches(isDisplayed()));

		ViewInteraction imageView6 = onView(
				allOf(withId(R.id.img_capture), withContentDescription("Button to take a picture"),
						childAtPosition(
								allOf(withId(R.id.root_container),
										childAtPosition(
												withId(android.R.id.content),
												0)),
								7),
						isDisplayed()));
		imageView6.check(matches(isDisplayed()));

		ViewInteraction imageView7 = onView(
				allOf(withId(R.id.img_effects_btn),
						childAtPosition(
								allOf(withId(R.id.root_container),
										childAtPosition(
												withId(android.R.id.content),
												0)),
								8),
						isDisplayed()));
		imageView7.check(matches(isDisplayed()));

		ViewInteraction imageView8 = onView(
				allOf(withId(R.id.img_switch_camera),
						childAtPosition(
								allOf(withId(R.id.root_container),
										childAtPosition(
												withId(android.R.id.content),
												0)),
								9),
						isDisplayed()));
		imageView8.check(matches(isDisplayed()));

		ViewInteraction imageView9 = onView(
				allOf(withId(R.id.img_btn_bg),
						childAtPosition(
								allOf(withId(R.id.root_container),
										childAtPosition(
												withId(android.R.id.content),
												0)),
								4),
						isDisplayed()));
		imageView9.check(matches(isDisplayed()));

		ViewInteraction imageView10 = onView(
				allOf(withId(R.id.img_btn_bg),
						childAtPosition(
								allOf(withId(R.id.root_container),
										childAtPosition(
												withId(android.R.id.content),
												0)),
								4),
						isDisplayed()));
		imageView10.check(matches(isDisplayed()));
	}

	private static Matcher<View> childAtPosition(
			final Matcher<View> parentMatcher, final int position) {

		return new TypeSafeMatcher<View>() {
			@Override
			public void describeTo(Description description) {
				description.appendText("Child at position " + position + " in parent ");
				parentMatcher.describeTo(description);
			}

			@Override
			public boolean matchesSafely(View view) {
				ViewParent parent = view.getParent();
				return parent instanceof ViewGroup && parentMatcher.matches(parent)
						&& view.equals(((ViewGroup) parent).getChildAt(position));
			}
		};
	}
}
