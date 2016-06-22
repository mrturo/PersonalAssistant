package com.fonax.android.controller;

import android.content.Context;

import com.fonax.android.R;

public class ScreenManager {
	
	public static boolean isPhone(Context ctx){
		if(ctx == null) return false;
		else return ctx.getResources().getString(R.string.screen_type)
				.equals(ctx.getResources().getString(R.string.screen_phone));
	}
	
	public static boolean isTablet(Context ctx){
		if(ctx == null) return false;
		else return !(ctx.getResources().getString(R.string.screen_type)
				.equals(ctx.getResources().getString( R.string.screen_phone)));
	}
	
	public static int getTabletSize(Context ctx){
		if( ctx == null ) return 0;
		else if( ctx.getResources().getString(R.string.screen_type)
				.equals(ctx.getResources().getString(R.string.screen_7tablet)) )
			return 7;
		else if( ctx.getResources().getString(R.string.screen_type)
				.equals(ctx.getResources().getString(R.string.screen_10tablet)) )
			return 10;
		else return 0;
	}
	
}
