package com.code4africa.customcamera.customcameraapp;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by johnpaulseremba on 17/09/2017.
 */

public class SceneSelectorAdapter extends RecyclerView.Adapter<SceneSelectorAdapter.ViewHolder> {

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.scene_selector_view, parent, false);
		return new ViewHolder(view);
	}

	@Override public void onBindViewHolder(ViewHolder holder, int position) {

	}

	@Override public int getItemCount() {
		return 0;
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		private final ImageView imageView;

		public ViewHolder(View itemView) {
			super(itemView);
			imageView = (ImageView) itemView.findViewById(R.id.scene_image_view);
		}

		public ImageView getImageView() {
			return imageView;
		}
	}

}
