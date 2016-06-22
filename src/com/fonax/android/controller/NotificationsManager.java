package com.fonax.android.controller;

import com.fonax.android.MainActivity;
import com.fonax.android.R;
import com.fonax.android.model.Contact;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

public class NotificationsManager {
	android.app.NotificationManager mNotificationManager;
	private static final int ID_UNREAD_MESSAGES = 0;
	private static final String DEFAULT_TEXT = Account.getContext().getResources().getString(
			R.string.app_name );
	private boolean show_unreadMsg, firstTime;
	private boolean startAsyncTask, runningAsyncTask;
	
	@SuppressWarnings("static-access")
	public NotificationsManager(){
		this.mNotificationManager = (NotificationManager)
				Account.getContext().getSystemService(Account.getContext().NOTIFICATION_SERVICE);
		
		this.firstTime = true;
		this.startAsyncTask = true;
		this.show_unreadMsg = false;
		this.runningAsyncTask = true;
		AsyncTaskRunner runner = new AsyncTaskRunner();
	    runner.execute();
	}
	
	protected void finalize(){
		this.startAsyncTask = false;
		while( this.runningAsyncTask ){ }
		this.mNotificationManager.cancelAll();
		this.show_unreadMsg = false;
	}
	
	private String[] setText(String[] text){
		if( text == null ){
			return null;
		} else {
			String title = text[0];
			String subject = text[1];
			String alert = text[2];
			
			if( title == null )
				title = NotificationsManager.DEFAULT_TEXT;
			else if( title.length() < 1 )
				title = NotificationsManager.DEFAULT_TEXT;
			if( subject == null )
				subject = NotificationsManager.DEFAULT_TEXT;
			else if( subject.length() < 1 )
				subject = NotificationsManager.DEFAULT_TEXT;
			if( alert == null )
				alert = "";
			
			String[] result = {title, subject, alert};
			return result;
		}
	}
	
	private void showNotification(int id, String title, String Subject, Intent intent,
			Bitmap icon, Boolean alert) {
		
		if( intent == null ) intent = new Intent(Account.getContext(), MainActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(Account.getContext(), 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		if(icon == null){
			icon = BitmapFactory.decodeResource(Account.getContext().getResources(),
					R.drawable.ic_launcher);
		}
		
		if ( android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN ){
			
			Notification n = new Notification.Builder(Account.getContext())
					.setSmallIcon(R.drawable.ic_launcher)
					.setContentIntent(contentIntent)
					.setContentText(Subject)
					.setContentTitle(title)
					.setLargeIcon(icon)
					.build();
			if(alert){
				n.defaults |= Notification.DEFAULT_SOUND;
				n.defaults |= Notification.DEFAULT_VIBRATE;
			}
			this.mNotificationManager.notify(id, n);
			
		} else{
			
			@SuppressWarnings("deprecation")
			Notification n = new Notification.Builder(Account.getContext())
					.setSmallIcon(R.drawable.ic_launcher)
					.setContentIntent(contentIntent)
					.setContentText(Subject)
					.setContentTitle(title)
					.setLargeIcon(icon)
					.getNotification();
			if(alert){
				n.defaults |= Notification.DEFAULT_SOUND;
				n.defaults |= Notification.DEFAULT_VIBRATE;
			}
			this.mNotificationManager.notify(id, n);
			
		}
		
	}
	
	private class AsyncTaskRunner extends AsyncTask<Void, Void, String> {
		
		@Override
		protected String doInBackground(Void... arg0) {
			if( startAsyncTask ){
				if( firstTime ) mNotificationManager.cancelAll();
				
				boolean logged = false, alert = false;
				if(Account.getIt() != null) logged = Account.getIt().isLogged();
				if(logged){
					
					// Set unread messages notification
					if( Account.getIt().getConversationManager() != null ){
						String[] unreadMessages = setText( Account.getIt().getConversationManager().getNotification() );
						if( unreadMessages != null ){
							if( !show_unreadMsg ) show_unreadMsg = true;
							
							String title = unreadMessages[0];
							Contact c = Account.getIt().getContactsManager().getContact(title);
							Bitmap icon = null;
							if( c != null ){
								Drawable picture = c.getProfilePicture();
								if(picture == null){
									picture = Account.getContext().getResources().getDrawable(
											R.drawable.ic_generic_profile_picture );
								}
								
								icon = ((BitmapDrawable)picture).getBitmap();
								title = c.getName();
							}
							
							if(unreadMessages[2].length()>0) alert = true;
							showNotification(ID_UNREAD_MESSAGES, title, unreadMessages[1], null, icon, (alert&&startAsyncTask));
						}
						else if( show_unreadMsg || firstTime ){
							mNotificationManager.cancel(ID_UNREAD_MESSAGES);
							show_unreadMsg = false;
						}
					}
					
				} else {
					
				}
				
				if( alert && startAsyncTask ){
					alert = true;
				}
				
			}
			return "";
		}
		
		@Override
		protected void onPostExecute(String arg) {
			if( startAsyncTask ){
				if( firstTime ) firstTime = false;
				new AsyncTaskRunner().execute();
			}
			else
				runningAsyncTask = false;
		}
	}
	
}
