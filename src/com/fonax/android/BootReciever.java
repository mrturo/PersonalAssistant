package com.fonax.android;

import com.fonax.android.controller.Account;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReciever extends BroadcastReceiver{
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Thread t = new Thread( new Runnable() {
			public void run() {
				while( true ){
					if( Account.getIt() != null ){
						if( Account.getIt().isSettedData() ){
							if( !Account.getIt().isLogged() && 
									Account.getIt().getStausConnection() == 0 ){
								Account.getIt().logIn();
								while(Account.getIt().getStausConnection()==1){ }
							}
						}
					}
				}
			}
		} );
		
		if( Account.getIt() == null && context != null )
			Account.getFromCache( context );
		t.start();
	}
	
}