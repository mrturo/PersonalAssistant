package com.fonax.android.view.activity;

import com.actionbarsherlock.app.SherlockActivity;
import com.fonax.android.MainActivity;
import com.fonax.android.R;
import com.fonax.android.model.ActionBar;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

public class NoInternetActivity extends SherlockActivity {
	
	public static boolean isOnline(Context ctx) {
		boolean result = false;
		ConnectivityManager cm =
			(ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if( netInfo != null )
			result = netInfo.isConnected();
		return result;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if ( isOnline( getApplicationContext() ) ) {
			Intent intent = new Intent(getApplicationContext(), MainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			overridePendingTransition(0, 0);
			finish();
		} else {
			setContentView(R.layout.activity_no_internet);
			new ActionBar(this).setGenericBar(false);
		}
	}
}
