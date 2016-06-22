package com.fonax.android.view.activity;

import com.actionbarsherlock.app.SherlockActivity;
import com.fonax.android.controller.Account;
import com.fonax.android.model.ActionBar;
import com.fonax.android.R;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.ImageView;

public class ConnectingActivity extends SherlockActivity{
	private AsyncTaskConnecting runner;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_connect );
		new ActionBar(this).setGenericBar(false);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		this.runner = new AsyncTaskConnecting();
		this.runner.execute();
	}
	
	private class AsyncTaskConnecting extends AsyncTask<Void, Void, Intent> {
		private int mMediumAnimationDuration, mLongAnimationDuration;
		private ImageView expandedIcon_color, expandedIcon_bw;
		private boolean connecting, isColor, error;
		private Thread t;
		long waitTime;
		
		private void animationView(ImageView view, boolean show, long duration){
			if(view != null){
				float alpha = 0;
				if(show) alpha = 1;
				view.animate()
	            		.alpha(alpha)
	            		.setDuration(duration)
	            		.setListener(null);
			}
			SystemClock.sleep(duration + waitTime);
		}
		
		public AsyncTaskConnecting(){
			this.expandedIcon_color = (ImageView) findViewById( R.id.expandedConnectingIcon_color );
			this.expandedIcon_bw = (ImageView) findViewById( R.id.expandedConnectingIcon_bw );
			this.connecting = true;
			this.isColor = false;
			this.waitTime = 1500;
			this.error = false;
			this.t = new Thread( new Runnable() {
				public void run() {
					Log.i("Login Animation", "Started");
					SystemClock.sleep(1500);
					animationView(expandedIcon_bw, true, mLongAnimationDuration);
					while( connecting ){
						isColor = !isColor;
						animationView(expandedIcon_color, isColor, mMediumAnimationDuration);
					}
					if(!error && !isColor){
						animationView(expandedIcon_color, true, mMediumAnimationDuration);
					} else if(error && isColor){
						animationView(expandedIcon_color, false, mMediumAnimationDuration);
					}
					Log.i("Login Animation", "Finish");
				}
			} );
			this.mMediumAnimationDuration = getResources().getInteger(
	                android.R.integer.config_mediumAnimTime);
			this.mLongAnimationDuration = getResources().getInteger(
	                android.R.integer.config_longAnimTime);
			t.start();
		}
		
		@Override
		protected Intent doInBackground(Void... arg0) {
			Intent intent = null;
			Account.getIt().logIn();
			SystemClock.sleep(1000);
			while( Account.getIt().getStausConnection()==1 ){ }
			if( Account.getIt().getStausConnection()>1 ){
				int remember = 0;
				Bundle extras = getIntent().getExtras();
				if (extras != null) remember = ( extras.getBoolean("remember") ? 1 : -1 );
				Account.getIt().getCacheDatabase().addAccount(remember);
				intent = new Intent(getApplicationContext(), PhoneManagerActivity.class);
			} else {
				Account.getIt().getCacheDatabase().clearPasswordAccount();
				intent = new Intent(getApplicationContext(), LoginActivity.class);
				this.error = true;
			}
			this.connecting = false;
			SystemClock.sleep(mLongAnimationDuration);
			return intent;
		}
		
		@Override
		protected void onPostExecute(Intent intent) {
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			overridePendingTransition(0, 0);
			finish();
		}
	}
	
}
