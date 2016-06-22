package com.fonax.android;

import com.actionbarsherlock.app.SherlockActivity;
import com.fonax.android.controller.Account;
import com.fonax.android.model.ActionBar;
import com.fonax.android.view.activity.ConnectingActivity;
import com.fonax.android.view.activity.LoginActivity;
import com.fonax.android.view.activity.PhoneManagerActivity;

import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends SherlockActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		new ActionBar(this).setGenericBar(false);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Intent intent;
		if( Account.getIt() == null )
			Account.getFromCache(getApplicationContext());
		
		if( Account.getIt().isLogged() )
			intent = new Intent(getApplicationContext(), PhoneManagerActivity.class);
		else if( Account.getIt().isSettedData() )
			intent = new Intent(getApplicationContext(), ConnectingActivity.class);
		else
			intent = new Intent(getApplicationContext(), LoginActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		overridePendingTransition(0, 0);
		finish();
	}
}