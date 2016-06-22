package com.fonax.android.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.jivesoftware.smack.packet.Message;

import android.os.AsyncTask;

import com.fonax.android.R;
import com.fonax.android.model.Contact;
import com.fonax.android.model.Conversation;
import com.fonax.android.model.Note;

public class ConversationManager {
	private boolean newMessagesRunning, messagesHistoryRunning;
	private boolean startedNewMessages, startedMessagesHistory;
	private ArrayList<Integer> lastSize, lastUnread;
	private ArrayList<Conversation> conversations;
	private ArrayList<Note> noProcessedMessages;
	private Comparator<Conversation> comparator;
	private int unreadMessages;
	
	public ConversationManager(){
		this.unreadMessages = 0;
		this.newMessagesRunning = true;
		this.startedNewMessages = true;
		this.messagesHistoryRunning = true;
		this.startedMessagesHistory = true;
		this.lastSize = new ArrayList<Integer>();
		this.lastUnread = new ArrayList<Integer>();
		this.noProcessedMessages = new ArrayList<Note>();
		this.conversations = new ArrayList<Conversation>();
		this.comparator = new Comparator<Conversation>() {
		    public int compare(Conversation c1, Conversation c2) {
		    	if( c1 == null && c2 == null )
		    		return 0;
		    	else if( c1 == null )
		    		return 1;
		    	else if( c2 == null )
		    		return -1;
		    	else if( c1.getMessages().isEmpty() && c2.getMessages().isEmpty() )
		    		return (c1.getName().toLowerCase()).compareTo(c2.getName().toLowerCase());
		    	else if( c1.getMessages().isEmpty() )
		    		return 1;
		    	else if( c2.getMessages().isEmpty() )
		    		return -1;
		    	else
		    		return c2.getLastMessageTime().compareTo( c1.getLastMessageTime() );
		    }
		};
		new NewMessagesRunner().execute();
		new MessagesHistoryRunner().execute();
	}
	
	protected void finalize(){
		this.startedNewMessages = false;
		this.startedMessagesHistory = false;
		while( this.newMessagesRunning || this.messagesHistoryRunning ){ }
	}
	
	public void add(Message message){
		if(message != null)
			if(message.getBody() != null)
				if(message.getBody().length() > 0){
					Note n = new Note(message);
					n.deleteHost();
					this.noProcessedMessages.add(n);
				}
	}
	
	public Conversation getConversation(String username){
		Conversation result = null;
		if( username != null ){
			if( username.length() > 0 ){
				for(int i=0; i<this.conversations.size(); i++){
					ArrayList<Contact> tempList = this.conversations.get(i).getOtherUsersList();
					if( !tempList.isEmpty() ){
						if( !this.conversations.get(i).isMultiUser() ){
							if( tempList.size() == 1 && tempList.get(0).getUsername().equals(username) ){
								result = this.conversations.get(i);
							}
						}
					}
				}
			}
			if(result == null){
				Contact c = Account.getIt().getContactsManager().getContact(username);
				if( c != null ){
					result = new Conversation( c );
					this.conversations.add(result);
					this.lastSize.add(0);
					this.lastUnread.add(0);
				}
			}
		}
		return result;
	}
	
	public ArrayList<Conversation> getActivedConversations(){
		ArrayList<Conversation> result = new ArrayList<Conversation>();
		for( int i=0; i<conversations.size(); i++ ){
			Conversation c = conversations.get(i) ;
			if( c != null ) if( !c.getMessages().isEmpty() ) result.add( c );
		}
		Collections.sort(result, comparator);
		return result;
	}
	
	public void clearAll(){
		Account.getIt().getCacheDatabase().deleteAllConversations();
		for(int i=0; i<this.conversations.size(); i++){
			this.conversations.get(i).clear();
		}
	}
	
	public String[] getNotification(){
		int contacts = 0, messages = 0;
		String message = "", contact = "";
		for( int i=0; i<this.conversations.size(); i++ ){
			Conversation c = this.conversations.get(i);
			ArrayList<Contact> con = c.getOtherUsersList();
			Boolean canChatting = true;
			if( con.size() == 1 ) canChatting = con.get(0).canChatting();
			if( c.getUnreadMessages()>0 && !c.isActived() && canChatting ){
				messages = messages + c.getUnreadMessages();
				contacts++;
				if( contacts == 1 ){
					contact = c.getMessages().get(0).getFrom();
					if( messages == 1 ){
						int lastIndex = c.getMessages().size() - 1;
						message = c.getMessages().get(lastIndex).getBody();
					}
				}
			}
		}
		if( messages > 0 ){
			String notification_title = "", notification_subject = "";
			if( contacts == 1 ){
				notification_title = contact;
				if( messages == 1 ) notification_subject = message;
				else{
					notification_subject = "";
					if( messages < 10 ) notification_subject = "0";
					notification_subject = notification_subject + messages
							+ " " + Account.getContext().getResources().getString(
							R.string.text_notification_new_messages );
				}
			}
			else if( contacts > 1 ){
				notification_subject = "";
				if( messages < 10 ) notification_subject = "0";
				notification_subject = notification_subject + messages
						+ " " + Account.getContext().getResources().getString(
						R.string.text_notification_messages_from ) + " ";
				if( contacts < 10 ) notification_subject = notification_subject + "0";
				notification_subject = notification_subject + contacts
						+ " " + Account.getContext().getResources().getString(
						R.string.text_notification_conversations ) + " ";
			}
			String alert = "";
			if( this.unreadMessages < messages ) alert = "alert";
			this.unreadMessages = messages;
			
			String[] result = {notification_title,notification_subject,alert};
			if( result[0]=="" && result[1]=="" ) result = null;
			return result;
		} else this.unreadMessages = 0;
		return null;
	}
	
	private class NewMessagesRunner extends AsyncTask<Void, Void, String> {
		
		@Override
		protected String doInBackground(Void... arg0) {
			Note message;
			Conversation contact;
			String contactUsername;
			while(noProcessedMessages.size()>0 && startedNewMessages){
				message = noProcessedMessages.get(0);
				noProcessedMessages.remove(0);
				contactUsername = message.getFrom();
				if(contactUsername.equals(Account.getIt().getUser().getUsername()))
					contactUsername = message.getTo();
				contact = getConversation(contactUsername);
				contact.addMessages(message, true, true, true);
			}
			return "";
		}
		
		@Override
		protected void onPostExecute(String arg) {
			if( startedNewMessages )
				new NewMessagesRunner().execute();
			else newMessagesRunning = false;
		}
	}
	
	private class MessagesHistoryRunner extends AsyncTask<Void, Void, String> {
				
		@Override
		protected String doInBackground(Void... arg0) {
			if( startedMessagesHistory ){
				for(int i=0; i<conversations.size(); i++){
					Conversation c = conversations.get(i);
					if( c.getMessages().size() != lastSize.get(i)
							|| c.getUnreadMessages() != lastUnread.get(i) ){
						lastSize.set(i, c.getMessages().size());
						lastUnread.set(i, c.getUnreadMessages());
					}
				}
			}
			return "";
		}
		
		@Override
		protected void onPostExecute(String arg) {
			if( startedMessagesHistory )
				new MessagesHistoryRunner().execute();
			else messagesHistoryRunning = false;
		}
	}
	
}
