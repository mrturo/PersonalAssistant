package com.fonax.android.view.adapter;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.fonax.android.R;

public class ExtensionsAdapter extends ArrayAdapter<String>{
	private static final int layoutResourceId = R.layout.itemlist_extension;
	private ArrayList<String> extensions;
	private ExtensionHolder holder;
	private static Context ctx;
	
	public ExtensionsAdapter(Context ctx, ArrayList<String> extensions){
		super(ctx, layoutResourceId, extensions );
		this.extensions = extensions;
		ExtensionsAdapter.ctx = ctx;
	}
	
	@Override
	public String getItem(int position) {
		return this.extensions.get(position);
	}

	@Override
	@SuppressLint("ViewHolder")
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = ((Activity) ExtensionsAdapter.ctx).getLayoutInflater();
		convertView = inflater.inflate(layoutResourceId, parent, false);
		this.holder = new ExtensionHolder( this.extensions.get(position), convertView );
		convertView.setTag(this.holder);
		this.holder.setupItem();
		return convertView;
	}
	
	public static class ExtensionHolder {
		private String extension;
		private TextView extensionView;
		
		public ExtensionHolder(String extension, View view){
			this.extension = extension;
			this.extensionView = (TextView) view.findViewById(R.id.extension);
		}
		
		private void setupItem() {
			this.extensionView.setText( ExtensionsAdapter.ctx.getResources().getString(
					R.string.text_abbreviation_extension ) + ": " + this.extension );
		}
		
	}
	
}
