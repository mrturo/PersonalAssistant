package com.fonax.android.model;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.TimeZone;

import org.jivesoftware.smack.Chat;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;

import com.fonax.android.R;
import com.fonax.android.controller.Account;
import com.fonax.android.sqlite.DatabaseOperations;

public class Conversation{
	private ArrayList<String> otherUsersList;
	private Comparator<Note> comparator;
	private Timestamp lastMessageTime;
	private ArrayList<Note> messages;
	private String lastMessageWriter;
	private String lastMessageBody;
	private int selectedMessages;
	private int unreadMessages;
	private Boolean multiUser;
	private Boolean active;
	private String name;
	private Chat chat;
	
	@SuppressWarnings("unused")
	public Conversation(Contact contact){
		String username = contact.getUsername() + "@" + Account.HOST;
		this.otherUsersList = new ArrayList<String>();
		this.otherUsersList.add( contact.getUsername() );
		this.name = "";
		this.lastMessageTime = null;
		this.lastMessageWriter = "";
		this.lastMessageBody = "";
		this.selectedMessages = 0;
		this.unreadMessages = 0;
		this.multiUser = false;
		this.active = false;
		this.comparator = new Comparator<Note>() {
		    public int compare(Note n1, Note n2) {
		    	if( n1 == null && n2 == null ){
		    		return -1;
		    	} else if( n1 == null ){
		    		return 1;
		    	} else if( n2 == null ){
		    		return -1;
		    	} else if( n1.getUTCTimestamp() == null && n2.getUTCTimestamp() == null ){
			    	return -1;
			    } else if( n1.getUTCTimestamp() == null ){
			    	return 1;
			    } else if( n2.getUTCTimestamp() == null ){
			    	return -1;
			    } else if( !n1.getUTCTimestamp().equals( n2.getUTCTimestamp() ) ) {
			    	if( n1.getUTCTimestamp().before( n2.getUTCTimestamp() ) )
			    		return -1;
			    	else return 1;
		    	} else return -1;
		    }
		};
		
		this.chat = Account.getIt().getChatManager().createChat(username, null);
		this.messages = new ArrayList<Note>();
		
		// Get history from local DB
		DatabaseOperations dop = Account.getIt().getCacheDatabase();
		if( dop != null ){
			Cursor cr = dop.getConversation(this);
			if( cr != null ){
				if( cr.getCount() > 0 ){
					cr.moveToFirst();
					do{
						//	0 HOST,		1 ACCOUNT_USERNAME,	2 FROM,	3 TO,
						//	4 DATETIME,	5 BODY,				6 READ
						if( Account.HOST.equals( cr.getString(0) ) ){
							if( Account.getIt().getUser().getUsername().equals( cr.getString(1) ) ){
								String from = cr.getString(2);
								String to = cr.getString(3);
								if( contact.getUsername().equals( from ) || contact.getUsername().equals( to ) ){
									Note n = new Note(from, to, cr.getString(5), cr.getLong(4), cr.getString(6));
									if( n != null ){
										if( !n.getBody().isEmpty() ){
											addMessages( n, false, false, false );
											if( !n.isRead() )
												this.unreadMessages++;
										}
									}
								}
							}
						}
					}while( cr.moveToNext() );
				}
			}
		}
		
		// Get history from web DB
		Boolean added = false;
		String json;
		/*try {
			String url = "http://" + Account.HOST + ":8080/chat/"
					+ Account.getIt().getUsername() + "/" + this.username;
			json = readJsonUrl(url);
			Gson gson = new Gson();
			jsonMessage[] webMessages = gson.fromJson(json, jsonMessage[].class);
			if( webMessages.length>0 ){
				Timestamp lastLocalMsg = null;
				if( this.lastMessageTime != null ) lastLocalMsg = new Timestamp( this.lastMessageTime.getTime() );
				for (jsonMessage m : webMessages){
					if( m.us.equals(Account.getIt().getUsername() + "@" + Account.HOST) &&
							m.with_user.equals(this.username) && !m.body.isEmpty() ){
						Note n = new Note(m);
						if(!n.getBody().isEmpty()){
							if( lastLocalMsg == null ){
								addMessages( n, true, false );
								added = true;
							} else if( existMessage(n) ){
								if( n.getUTCTimestamp().after( lastLocalMsg ) ){
									addMessages( n, true, false );
									added = true;
								}
							} else {
								addMessages( n, true, false );
								added = true;
							}
						}
					}
				}
			}
		} catch (Exception e) { }*/
		
		if( added ) {
			readAll();
			Collections.sort(this.messages, this.comparator);
			
				if(this.messages.size()>1){
				Note n1, n2;
				for(int i=1; i<this.messages.size(); i++){
					n1 = this.messages.get(i-1); n2 = this.messages.get(i);
					if( n1.getFrom().equals(n2.getFrom()) && 
							n1.getTo().equals(n2.getTo()) && 
							n1.getBody().equals(n2.getBody()) ){
						this.messages.remove(i);
						i--;
					}
				}
			}
		}
	}
	
	@SuppressWarnings("unused")
	private static String readJsonUrl(String urlString) throws Exception {
	    BufferedReader reader = null;
	    try {
	        URL url = new URL(urlString);
	        reader = new BufferedReader(new InputStreamReader(url.openStream()));
	        StringBuffer buffer = new StringBuffer();
	        int read;
	        char[] chars = new char[1024];
	        while ((read = reader.read(chars)) != -1)
	            buffer.append(chars, 0, read); 

	        return buffer.toString();
	    } finally {
	        if (reader != null)
	            reader.close();
	    }
	}
	
	@SuppressWarnings("unused")
	private boolean existMessage(Note n){
		Boolean result = false;
		for(int i = 0; i < messages.size() && !result; i++){
			Note m = messages.get(i);
			if( n.getTo().equals( m.getTo() ) && n.getFrom().equals( m.getFrom() )
					&& n.getBody().equals( m.getBody() ) ){
				result = true;
			}
		}
		return result;
	}
	
	public boolean isMultiUser(){
		return this.multiUser;
	}
	
	public String getName(){
		if( !this.otherUsersList.isEmpty() && !this.multiUser ){
			return Account.getIt().getContactsManager().getContact( this.otherUsersList.get(0) ).getName();
		}
		return this.name;
	}
	
	public Drawable getPicture(Context ctx){
		Drawable result = null;
		if( multiUser ){
			result = ctx.getResources().getDrawable( R.drawable.ic_generic_profile_picture );
		}
		else {
			result = Account.getIt().getContactsManager().getContact( this.otherUsersList.get(0) ).getProfilePicture();
			if( result == null )
				result = ctx.getResources().getDrawable( R.drawable.ic_generic_profile_picture );
		}
		return result;
	}
	
	public ArrayList<Contact> getOtherUsersList(){
		ArrayList<Contact> result = new ArrayList<Contact>();
		for(int i = 0; i<this.otherUsersList.size(); i++)
			result.add( Account.getIt().getContactsManager().getContact( this.otherUsersList.get(i) ) );
		return result;
	}
	
	public int getNumberOfOtherUsers(){
		return this.otherUsersList.size();
	}
	
	public boolean canChatting(){
		boolean result = false;
		if( Account.getIt().getUser().canChatting() ){
			if( !this.otherUsersList.isEmpty() ){
				for(int i=0; i<this.otherUsersList.size(); i++){
					result = result || Account.getIt().getContactsManager().getContact( this.otherUsersList.get(i) ).canChatting();
				}
			}
		}
		return result;
	}
	
	public Chat getChat(){
		return this.chat;
	}
	
	public ArrayList<Note> getMessages(){
		if( messages.size() >1 ){
			int max, proxMsg;
			for(int currentMsg=0; currentMsg<messages.size(); currentMsg++){
				max = messages.get(currentMsg).getBody().length();
				messages.get(currentMsg).setTop(true);
				messages.get(currentMsg).setBottom(true);
				if( currentMsg > 0 ) messages.get(currentMsg-1).setBottom(true);
				if( currentMsg < messages.size()-1 ) messages.get(currentMsg+1).setTop(true);
				proxMsg = currentMsg + 1;
				while( proxMsg<messages.size() &&
						messages.get(currentMsg).getFrom().equals(
								messages.get(proxMsg).getFrom()) &&
						( ( messages.get(currentMsg).getTimestamp().getTime() + 60000 )
								>= messages.get(proxMsg).getTimestamp().getTime() ) ){
					if( max < messages.get(proxMsg).getBody().length() )
						max = messages.get(proxMsg).getBody().length();
					messages.get( proxMsg ).setTop(false);
					messages.get( proxMsg-1 ).setBottom(false);
					
					messages.get( proxMsg ).setBottom(true);
					if(proxMsg<messages.size()-1) messages.get( proxMsg+1 ).setTop(true);
					proxMsg++;
				} proxMsg--;
				if( currentMsg < proxMsg ){
					while( currentMsg <= proxMsg && currentMsg < messages.size() ){
						messages.get(currentMsg).setSize(max);
						currentMsg++;
					} currentMsg--;
				}
			}
		}
		return this.messages;
	}
	
	public int getUnreadMessages(){
		return this.unreadMessages;
	}
	
	public String getLastMessageBody(){
		return this.lastMessageBody;
	}
	
	public Timestamp getLastMessageTime(){
		if( this.lastMessageTime != null ){
			Calendar c = Calendar.getInstance();
			TimeZone z = c.getTimeZone();
		    int offset = z.getRawOffset();
		    if(z.inDaylightTime(new Date())){
		        offset = offset + z.getDSTSavings();
		    }
		    int offsetHrs = offset / 1000 / 60 / 60;
		    int offsetMins = offset / 1000 / 60 % 60;

		    c.add(Calendar.HOUR_OF_DAY, (-offsetHrs));
		    c.add(Calendar.MINUTE, (-offsetMins));
			
			return new Timestamp( this.lastMessageTime.getTime() -
					( c.getTime().getTime() - (new Date().getTime()) ) );
		}
		return null;
	}
	
	public String getLastMessageWriter(){
		return this.lastMessageWriter;
	}
	
	public boolean isActived(){
		return this.active;
	}
	
	public void active(){
		this.active = true;
	}
	
	public void unactive(){
		this.active = false;
	}
	
	public void addMessages(Note message, Boolean addToDB,  Boolean setUnread, Boolean checkExist){
		if(message != null){
			if( !message.getBody().isEmpty() ){
				if( !message.getFrom().equals(Account.getIt().getUser().getUsername()) && setUnread ){
					message.unread();
					this.unreadMessages++;
				}
				
				boolean canAdd = true;
				if( message.getFrom().equals(Account.getIt().getUser().getUsername()) && checkExist ){
					for(int i=this.messages.size()-1; i>=0 && canAdd; i++){
						Note temp = this.messages.get(i);
						if( !temp.isReceivedByServer() ){
							if( message.getFrom().equals(temp.getFrom()) ){
								if( message.getTo().equals(temp.getTo()) ){
									if( message.getBody().equals(temp.getBody()) ){
										this.messages.get(i).receivedByServer();
										canAdd = false;
									}
								}
							}
						}
					}
				}
				
				if(canAdd){
					this.messages.add( message );
					if( addToDB )
						message.setDB_ID(Account.getIt().getCacheDatabase().addMessage(message));
					if( this.lastMessageTime == null ){
						this.lastMessageTime = message.getUTCTimestamp();
						this.lastMessageWriter = message.getFrom();
						this.lastMessageBody = message.getBody();
					} else if( this.lastMessageTime.before(message.getUTCTimestamp()) ){
						this.lastMessageTime = message.getUTCTimestamp();
						this.lastMessageWriter = message.getFrom();
						this.lastMessageBody = message.getBody();
					} else if( this.lastMessageTime.equals(message.getUTCTimestamp()) ){
						this.lastMessageWriter = message.getFrom();
						this.lastMessageBody = message.getBody();
					}
				}
			}
		}
	}
	
	public void readAll(){
		if( this.unreadMessages > 0 ){
			this.unreadMessages = 0;
			boolean keepChecking = true;
			Account.getIt().getCacheDatabase().updateReadStatus(this);			
			for(int i=this.messages.size()-1; i>=0 && keepChecking; i--){
				if(!this.messages.get(i).getFrom().equals(Account.getIt().getUser().getUsername())){
					if( !this.messages.get(i).isRead() ) this.messages.get(i).read();
					else keepChecking = false;
				}
			}
		}
	}
	
	public void sendMessage(String body){
		if( !this.otherUsersList.isEmpty() && body != null ){
			if( !body.isEmpty() ){
				if( !multiUser ){
					if( this.otherUsersList.size() == 1 ){
						Note n = new Note(Account.getIt().getContactsManager().getContact( this.otherUsersList.get(0) ).getUsername(), body);
						if( !n.getBody().isEmpty() ){
							addMessages(n, true, false, false);
							Account.getIt().sendMessage(n, this.chat);
						}
					}
				}
			}
		}
	}
	
	public void unselectAllMessages(){
		this.selectedMessages = 0;
		for(int i=0; i<this.messages.size(); i++){
			if(this.messages.get(i).isSelected())
				this.messages.get(i).unselect();
		}
	}
	
	public void deleteSelectedMessages(){
		if( this.selectedMessages>0 ){
			this.selectedMessages = 0;
			for(int i=0; i<this.messages.size(); i++){
				if(this.messages.get( i ).isSelected()){
					Account.getIt().getCacheDatabase().deleteMessage( this.messages.get( i ) );
					this.messages.remove( i-- );
				}
			}
			if( !this.messages.isEmpty() ){
				Note n = this.messages.get(this.messages.size()-1);
				this.lastMessageTime = n.getUTCTimestamp();
				this.lastMessageWriter = n.getFrom();
				this.lastMessageBody = n.getBody();
			} else {
				this.lastMessageWriter = "";
				this.lastMessageTime = null;
				this.lastMessageBody = "";
			}
		}
	}
	
 	public void selectedMessagesToClipboard(Context context){
		if( this.selectedMessages>0 ){
			ArrayList<Note> selectedItems = new ArrayList<Note>();
			for(int i=0; i<this.messages.size(); i++){
				if(this.messages.get(i).isSelected())
					selectedItems.add(this.messages.get(i));
			}
			if( !selectedItems.isEmpty() ){
				ClipboardManager clipboard = (ClipboardManager) context.getSystemService(
						Context.CLIPBOARD_SERVICE );
				String text = "";
				if( selectedItems.size()==1 ){
					text = selectedItems.get(0).getBody();
				} else if( selectedItems.size()>1 ){
					SimpleDateFormat format = new SimpleDateFormat("[hh:mm a, dd/MMM/yy]");
					for(int i=0; i<selectedItems.size(); i++){
						String name = selectedItems.get(i).getFrom();
						if(name.equals(Account.getIt().getUser().getUsername()))
							name = context.getResources().getString(R.string.text_chat_me);
						else{
							Contact contact = Account.getIt().getContactsManager().getContact(name);
							name = contact.getName();
						}
						
						if(i>0) text = text + "\n";
						text = text	+ format.format(selectedItems.get(i).getUTCTimestamp())
									+ " " + name + ": " + selectedItems.get(i).getBody();
					}
				}
				clipboard.setPrimaryClip(ClipData.newPlainText("simple text",text));
			}
		}
	}
	
	public void changeStatusSelecctionOfMessage(int index){
		if( index>=0 && index<this.messages.size() ){
			if( this.messages.get(index).isSelected() ){
				this.messages.get(index).unselect();
				this.selectedMessages--;
			} else {
				this.messages.get(index).select();
				selectedMessages++;
			}
		}
	}
	
	public int numberSelectedMessage(){
		return this.selectedMessages;
	}
	
	public void clear(){
		this.messages.clear();
		this.lastMessageTime = null;
		this.lastMessageWriter = "";
		this.lastMessageBody = "";
		this.unreadMessages = 0;
		Account.getIt().getCacheDatabase().deleteConversation( this );
	}
	
}