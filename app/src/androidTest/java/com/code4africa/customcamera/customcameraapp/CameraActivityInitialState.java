package com.code4africa.customcamera.customcameraapp;

import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import java.util.ArrayList;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.not;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class CameraActivityInitialState {

	@Rule
	public ActivityTestRule<CameraActivity> mActivityTestRule =
			new ActivityTestRule<>(CameraActivity.class);

	@Test
	public void cameraActivityInitialState() {
		onView(withId(R.id.img_overlay_toggle))
				.check(matches(isDisplayed()));

		onView(withId(R.id.tv_camera))
				.check(matches(isDisplayed()));

		onView(withId(R.id.img_overlay))
				.check(matches(isDisplayed()));
	}

	@Test
	public void toggleOverlayVisibility() {
		onView(withId(R.id.img_overlay_toggle))
				.perform(click());

		onView(withId(R.id.img_overlay))
				.check(matches(not(isDisplayed())));
	}

	@Test
	public void absenceOfOtherIcons() {
		ArrayList<Integer> goneList = new ArrayList<Integer>() {
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
				add(R.id.txt_zoom_caption);
				add(R.id.txt_seekbar_progress);
			}
		};
		testIsGone(goneList);
	}

	public void testIsGone(ArrayList<Integer> list) {
		for (Integer id : list) {
			onView(withId(id))
					.check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
		}
	}

}
