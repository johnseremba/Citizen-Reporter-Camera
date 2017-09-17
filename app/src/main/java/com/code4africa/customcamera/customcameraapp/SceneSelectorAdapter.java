package com.code4africa.customcamera.customcameraapp;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import java.util.ArrayList;
import java.util.HashMap;

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
		private HashMap<String, ArrayList<Integer>> scenesList;

		public ViewHolder(View itemView, HashMap<String, ArrayList<Integer>> scenesList) {
			super(itemView);
			imageView = (ImageView) itemView.findViewById(R.id.scene_image_view);
			this.scenesList = scenesList;
		}

		public ImageView getImageView() {
			return imageView;
		}

		public HashMap<String, ArrayList<Integer>> getData() {

		}
	}

}
