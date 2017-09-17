package com.code4africa.customcamera.customcameraapp;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import java.util.ArrayList;

public class SceneSelectorAdapter extends RecyclerView.Adapter<SceneSelectorAdapter.ViewHolder> {
	private String sceneKey;
	private ArrayList<Integer> scenesList;
	private static final String TAG = SceneSelectorAdapter.class.getSimpleName();
	private OnClickThumbListener onClickThumbListener;

	public interface OnClickThumbListener {
		void OnClickScene(String sceneKey, Integer position);
	}

	public SceneSelectorAdapter (Activity activity, String sceneKey, ArrayList<Integer> scenesList) {
		this.sceneKey = sceneKey;
		this.scenesList = scenesList;
		this.onClickThumbListener = (OnClickThumbListener) activity;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.scene_selector_view, parent, false);
		return new ViewHolder(view);
	}

	@Override public void onBindViewHolder(ViewHolder holder, int position) {
		holder.position = position;
		holder.bindScene(scenesList.get(position));
	}

	@Override public int getItemCount() {
		return this.scenesList.size();
	}

	public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
		private final ImageView imageView;
		private ImageView imageOverlay;
		private ArrayList<Integer> scenesList;
		private Integer position;

		public ViewHolder(final View itemView) {
			super(itemView);
			imageView = (ImageView) itemView.findViewById(R.id.scene_image_view);
			this.scenesList = scenesList;
			imageView.setOnClickListener(this);
		}

		public ImageView getImageView() {
			return imageView;
		}

		public void bindScene(Integer sceneID) {
			imageView.setImageResource(sceneID);
		}

		@Override public void onClick(View view) {
			onClickThumbListener.OnClickScene(sceneKey, position);
		}
	}

}
