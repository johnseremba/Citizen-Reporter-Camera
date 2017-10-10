package com.code4africa.customcamera.customcameraapp;

import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import java.util.ArrayList;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.not;

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
		onView(withId(R.id.img_overlay_toggle)).perform(click());

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

	@Test
	public void testOverlayFling() {
		// initialize Camera
		onView(withId(R.id.tv_camera)).perform(click());
		onView(withId(R.id.img_overlay_toggle)).perform(click());

		// Fling to change overlays
		ArrayList<String> scenes = new ArrayList<String>(){
			{
				add("Signature");
				add("Portrait");
				add("Environment");
				add("Candid");
				add("Interaction");
			}
		};
		swipeToChangeScenes(scenes);
	}

	@Test
	public void testDefaultScene() {
		// initialize Camera
		onView(withId(R.id.tv_camera)).perform(click());
		onView(withId(R.id.img_overlay_toggle)).perform(click());
		onView(withId(R.id.txt_swipe_caption)).check(matches(withText("Interaction")));
		onView(withId(R.id.img_overlay)).check(matches(withDrawable(R.drawable.interaction_001)));

		onView(withId(R.id.tv_camera)).perform(longClick());
		onView(withId(R.id.scene_recylcer_view)).check(matches(isDisplayed()));
		onView(withId(R.id.scene_recylcer_view))
				.perform(RecyclerViewActions.actionOnItemAtPosition(1, click()));
		onView(withId(R.id.scene_recylcer_view))
				.check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
		onView(withId(R.id.img_overlay))
				.check(matches(withDrawable(R.drawable.interaction_002)));
	}

	@Test
	public void toggleOutOverlays() {
		// Tap once the camera screen to display full camera functions
		onView(withId(R.id.tv_camera)).perform(click());

		// Toggle to show overlay pictures
		onView(withId(R.id.img_overlay_toggle)).perform(click());

		// Hide overlays
		onView(withId(R.id.img_overlay_toggle)).perform(click());

		ArrayList<Integer> displayedList = new ArrayList<Integer>() {
			{
				add(R.id.img_capture);
				add(R.id.img_flash_btn);
				add(R.id.img_overlay_toggle);
				add(R.id.img_switch_camera);
				add(R.id.img_gallery);
				add(R.id.img_effects_btn);
				add(R.id.img_wb_btn);
				add(R.id.tv_camera);
				add(R.id.seekbar_light);
				add(R.id.img_btn_bg);
				add(R.id.img_top_bg);
			}
		};
		ArrayList<Integer> goneList = new ArrayList<Integer>() {
			{
				add(R.id.txt_swipe_caption);
				add(R.id.sw_swipe_1);
				add(R.id.sw_swipe_2);
				add(R.id.sw_swipe_3);
				add(R.id.sw_swipe_4);
				add(R.id.sw_swipe_5);
				add(R.id.txt_zoom_caption);
				add(R.id.txt_seekbar_progress);
				add(R.id.img_overlay);
			}
		};
		checkIsDisplayed(displayedList);
		checkIsGone(goneList);
	}

	public void checkIsDisplayed(ArrayList<Integer> list) {
		for (Integer id : list) {
			onView(withId(id))
					.check(matches(isDisplayed()));
		}
	}

	public void checkIsGone(ArrayList<Integer> list) {
		for (Integer id : list) {
			onView(withId(id))
					.check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
		}
	}

	public void swipeToChangeScenes(ArrayList<String> captions) {
		for (String sceneCaption : captions) {
			onView(withId(R.id.tv_camera))
					.perform(swipeLeft());
			onView(withId(R.id.txt_swipe_caption))
					.check(matches(withText(sceneCaption)));
			onView(withId(R.id.img_overlay))
					.check(matches(isDisplayed()));
			onView(withId(R.id.img_overlay))
					.check(matches(not(noDrawable())));
			checkChildViews();
		}
	}

	public void checkChildViews() {
		// Show child scenes
		onView(withId(R.id.tv_camera)).perform(longClick());
		onView(withId(R.id.scene_recylcer_view)).check(matches(isDisplayed()));
		onView(withId(R.id.scene_recylcer_view))
				.perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
		onView(withId(R.id.scene_recylcer_view))
				.check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
		onView(withId(R.id.img_overlay))
				.check(matches(not(noDrawable())));
	}

	public static Matcher<View> withDrawable(final int resourceID) {
		return new DrawableMatcher(resourceID);
	}

	public static Matcher<View> noDrawable() {
		return  new DrawableMatcher(-1);
	}
}
