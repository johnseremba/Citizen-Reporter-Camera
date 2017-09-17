package com.code4africa.customcamera.customcameraapp;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import java.util.ArrayList;
import java.util.HashMap;

public class SceneSelectorAdapter extends RecyclerView.Adapter<SceneSelectorAdapter.ViewHolder> {
	private String sceneKey;
	private HashMap<String, ArrayList<Integer>> scenesList;
	private static final String TAG = SceneSelectorAdapter.class.getSimpleName();

	public SceneSelectorAdapter (String sceneKey, HashMap<String, ArrayList<Integer>> scenesList) {
		this.sceneKey = sceneKey;
		this.scenesList = scenesList;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.scene_selector_view, parent, false);
		return new ViewHolder(view);
	}

	@Override public void onBindViewHolder(ViewHolder holder, int position) {
		Log.d(TAG, "Bind position: " + position);
		holder.bindScene(scenesList.get(this.sceneKey).get(position));
	}

	@Override public int getItemCount() {
		return this.scenesList.size();
	}

	public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
		private final ImageView imageView;
		private HashMap<String, ArrayList<Integer>> scenesList;

		public ViewHolder(View itemView) {
			super(itemView);
			imageView = (ImageView) itemView.findViewById(R.id.scene_image_view);
			this.scenesList = scenesList;
		}

		public ImageView getImageView() {
			return imageView;
		}

		public void bindScene(Integer sceneID) {
			Log.d(TAG, "Bound Scene ID: " + sceneID);
			imageView.setImageResource(sceneID);
		}

		@Override public void onClick(View view) {
			Log.d(TAG, "Clicked: " + view);
		}
	}

}
