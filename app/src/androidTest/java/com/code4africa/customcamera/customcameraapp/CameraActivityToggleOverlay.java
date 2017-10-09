package com.code4africa.customcamera.customcameraapp;

import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import java.util.ArrayList;
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
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class CameraActivityToggleOverlay {

	@Rule
	public ActivityTestRule<CameraActivity> mActivityTestRule =
			new ActivityTestRule<>(CameraActivity.class);

	@Test
	public void cameraActivityToggleAndOverlayExist() {
		// Tap once the camera screen to display full camera functions
		onView(withId(R.id.tv_camera)).perform(click());

		// Toggle to show overlay pictures
		onView(withId(R.id.img_overlay_toggle))
				.perform(click());

		ArrayList<Integer> visibleObjects = new ArrayList<Integer>(){
			{
				add(R.id.txt_swipe_caption);
				add(R.id.sw_swipe_1);
				add(R.id.sw_swipe_2);
				add(R.id.sw_swipe_3);
				add(R.id.sw_swipe_4);
				add(R.id.sw_swipe_5);
				add(R.id.img_capture);
				add(R.id.img_flash_btn);
				add(R.id.img_switch_camera);
				add(R.id.img_gallery);
				add(R.id.img_effects_btn);
				add(R.id.img_wb_btn);
				add(R.id.seekbar_light);
				add(R.id.img_overlay_toggle);
				add(R.id.img_btn_bg);
				add(R.id.img_top_bg);
				add(R.id.img_overlay);
			}
		};
		checkIsDisplayed(visibleObjects);
	}

	public void checkIsDisplayed(ArrayList<Integer> list) {
		for (Integer id : list) {
			onView(withId(id))
					.check(matches(isDisplayed()));
		}
	}
}
