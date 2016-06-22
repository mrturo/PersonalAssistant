package com.fonax.android.model;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.fonax.android.R;
import com.fonax.android.controller.Account;
import com.fonax.android.view.activity.PhoneContactInfoActivity;

import de.hdodenhof.circleimageview.CircleImageView;

public class ActionBar {
	private int conversationBarLayout = R.layout.actionbar_conversation;
	private int genericBarLayout = R.layout.actionbar_generic;
	private SherlockFragmentActivity sFAct;
	private LayoutInflater mInflater;
	private SherlockActivity sAct;
	private boolean firstTime;
	private View barView;
	private Context ctx;
	
	public ActionBar(SherlockActivity sAct){
		this.sFAct = null;
		this.firstTime = true;
		if( sAct != null ){
			this.ctx = sAct.getApplicationContext();
			this.sAct = sAct;
		} else {
			this.ctx = null;
			this.sAct = null;
		}
	}
	
	public ActionBar(SherlockFragmentActivity sFAct){
		this.sAct = null;
		this.firstTime = true;
		if( sFAct != null ){
			this.ctx = sFAct.getApplicationContext();
			this.sFAct = sFAct;
		} else {
			this.ctx = null;
			this.sFAct = null;
		}
	}

	public ActionBar(SherlockFragment sFrag){
		this.sAct = null;
		this.firstTime = true;
		if( sFrag != null ){
			this.sFAct = sFrag.getSherlockActivity();
			this.ctx = sFAct.getApplicationContext();
		} else {
			this.ctx = null;
			this.sFAct = null;
		}
		
	}
	
	private void setHomeAsUp(Boolean HomeAsUp){
		ImageView backView = (ImageView) this.barView.findViewById(R.id.backButtom);
		if( HomeAsUp ){
			backView.setVisibility(View.VISIBLE);
			backView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if( sFAct != null ) sFAct.finish();
					else if( sAct != null ) sAct.finish();
				}
			});
		} else {
			backView.setVisibility(View.INVISIBLE);
			backView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) { }
			});
		}
	}
	
	public com.actionbarsherlock.app.ActionBar getBar(){
		com.actionbarsherlock.app.ActionBar bar = null;
		if( this.sAct != null ){
			bar = this.sAct.getSupportActionBar();
			if( bar != null ) this.mInflater = LayoutInflater.from(this.sAct);
			else this.mInflater = null;
		}
		else if( this.sFAct != null ){
			bar = this.sFAct.getSupportActionBar();
			if( bar != null ) this.mInflater = LayoutInflater.from(this.sFAct);
			else this.mInflater = null;
		}
		if( bar != null && this.firstTime ){
			bar.setDisplayShowCustomEnabled(false);
			bar.setDisplayShowTitleEnabled(false);
			bar.setDisplayHomeAsUpEnabled(false);
			bar.setDisplayShowHomeEnabled(false);
			bar.setIcon(android.R.color.transparent);
			this.firstTime = false;
		}
		return bar;
	}
	
	private void setBarView(int layout){
		if( this.mInflater != null )
			this.barView = this.mInflater.inflate(layout, null);
		else this.barView = null;
	}
	
	public void setTitle(String title){
		com.actionbarsherlock.app.ActionBar bar = getBar();
		if( bar != null && this.barView != null ){
			if( title == null ) title = ""; 
			TextView titleView = (TextView) this.barView.findViewById(R.id.titleView);
			titleView.setText( title );
			bar.setCustomView( this.barView );
			bar.setDisplayShowCustomEnabled(true);
		}
	}
	
	private void goToContactInfo(String username){
		Intent intent = new Intent(ctx, PhoneContactInfoActivity.class);
		intent.putExtra("user", username);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		if( sFAct != null ) sFAct.startActivity(intent);
		else if( sAct != null ) sAct.startActivity(intent);
	}
	
	public boolean setGenericBar(Boolean HomeAsUp, String title){
		boolean result = false;
		com.actionbarsherlock.app.ActionBar bar = getBar();
		if( bar != null ){
			this.setBarView( genericBarLayout );
			if( this.barView != null ){
				this.setHomeAsUp( HomeAsUp );
				bar.setCustomView( this.barView );
			}
			setTitle(title);
			result = true;
		}
		return result;
	}
	
	public boolean setGenericBar(Boolean HomeAsUp){
		return setGenericBar( HomeAsUp,  ctx.getString(R.string.app_name) );
	}
	
	public boolean setConversationBar(final Conversation conversation){
		boolean result = false;
		if( conversation != null ){	
			com.actionbarsherlock.app.ActionBar bar = getBar();
			if( bar != null ){
				this.setBarView(conversationBarLayout);
				if( this.barView != null ){
					this.setHomeAsUp(true);
					CircleImageView circleImageView = (CircleImageView) this.barView.findViewById(R.id.circleIconView);
					TextView titleView = (TextView) this.barView.findViewById(R.id.titleView);
					TextView subtitleView = (TextView) this.barView.findViewById(R.id.subtitleView);
					String title = conversation.getName(); 
					Drawable picture = conversation.getPicture( this.ctx );
					if( picture != null ){
						circleImageView.setImageDrawable( picture );
						circleImageView.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								if( sFAct != null ) sFAct.finish();
								else if( sAct != null ) sAct.finish();
							}
						});
					}
					if( title != null ) if( !title.isEmpty() ) titleView.setText( title );
					titleView.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							if( conversation.getNumberOfOtherUsers() > 0 ){
								if( !conversation.isMultiUser() ) goToContactInfo( conversation.getOtherUsersList().get(0).getUsername() );
							}
						}
					});
					if( !conversation.isMultiUser() && conversation.getNumberOfOtherUsers() > 0 ){
						if(conversation.getOtherUsersList().get(0).isAvaible( Account.getIt().getRoster() ))
							subtitleView.setText(R.string.text_chat_available);
						else subtitleView.setText(R.string.text_chat_unavailable);
						subtitleView.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								if( conversation.getNumberOfOtherUsers() > 0 ){
									if( !conversation.isMultiUser() ) goToContactInfo( conversation.getOtherUsersList().get(0).getUsername() );
								}
							}
						});
					} else {
						
						
					}
					bar.setCustomView( this.barView );
					bar.setDisplayShowCustomEnabled(true);
				}
				result = true;
			}
		} else result = setGenericBar(true);
		return result;
	}
	
}
