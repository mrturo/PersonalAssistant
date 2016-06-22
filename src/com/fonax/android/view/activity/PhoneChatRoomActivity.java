package com.fonax.android.view.activity;

import java.util.ArrayList;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnActionExpandListener;
import com.fonax.android.R;
import com.fonax.android.controller.Account;
import com.fonax.android.model.ActionBar;
import com.fonax.android.model.Conversation;
import com.fonax.android.view.adapter.MessageAdapter;
import com.fonax.android.view.fragment.ChatRoomFragment;

import android.app.SearchManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.SearchView;

public class PhoneChatRoomActivity extends SherlockFragmentActivity {
	private int mainLayout = R.layout.activity_chat_room_phone;
	private ChatRoomFragment chatRoom;
	private Conversation conversation;
	private AsyncTaskRunner runner;
	private boolean active;
	private ActionBar bar;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	protected void onResume() {
 		super.onResume();
		setContentView(this.mainLayout);
		
		Bundle extras = null;
		if( Account.getIt() != null ){
			if( Account.getIt().isLogged() ){
				if( Account.running ){
					extras = getIntent().getExtras();
					if( extras != null ){
						this.conversation = Account.getIt()
													.getConversationManager()
													.getConversation( extras.getString("user") );
						this.chatRoom = new ChatRoomFragment( this.conversation );
					}
					if( conversation == null || this.chatRoom == null ){
						this.conversation = null;
						this.chatRoom = null;
						finish();
					}
					else if( conversation.getOtherUsersList().isEmpty() ){
						this.conversation = null;
						this.chatRoom = null;
						finish();
					}
				}
			}
		}
		

		// Set bar
		this.bar = new ActionBar(this);
		this.bar.setConversationBar( conversation );
		
		getSupportFragmentManager()
				.beginTransaction()
				.add(R.id.fragment_chat_room, this.chatRoom, "frag1")
				.commit();
		
		this.startAsyncTask();
	}

	@Override
	protected void onPause() {
		this.stopAsyncTask();
		
		getSupportFragmentManager()
				.beginTransaction()
				.remove( getSupportFragmentManager().findFragmentByTag("frag1") );
		
		super.onPause();
	}
	
	private void startAsyncTask() {
		if (this.runner == null)
			this.runner = new AsyncTaskRunner();
	    this.runner.execute();
	    this.active = true;
	}

	private void stopAsyncTask() {
		if (this.runner != null) {
	        this.runner.cancel(true);
	        this.runner = null;
	    } this.active = false;
	}
	
	private class AsyncTaskRunner extends AsyncTask<Void, Void, MessageAdapter> {
		
		@Override
		protected MessageAdapter doInBackground(Void... arg0) {
			return chatRoom.getNewAdapter();
		}
		
		protected void onPostExecute(MessageAdapter newAdapter) {
			if( newAdapter != null ) chatRoom.setAdapter(newAdapter);
			if( active && !this.isCancelled() ){
				runner = new AsyncTaskRunner();
				runner.execute();
			}
		}
		
	}

	@Override
    public void onBackPressed(){
		if( chatRoom != null ) if( chatRoom.onBackPressed() )
			super.onBackPressed( );
    }
	
	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.chat_actions, menu);
		MenuItem callItem = menu.findItem(R.id.callContact);
		MenuItem extensionItem = menu.findItem(R.id.extensionsMenu);
		if( !this.conversation.isMultiUser() && this.conversation.getNumberOfOtherUsers() == 1 ){
			if( this.conversation.getOtherUsersList().get(0).getExtensions().isEmpty() ){
				callItem.setVisible(false);
				extensionItem.setVisible(false);
			} else if( this.conversation.getOtherUsersList().get(0).getExtensions().size() == 1 ){
				callItem.setVisible(true);
				extensionItem.setVisible(false);
			} else {
				Menu extensionsMenu = extensionItem.getSubMenu();
				callItem.setVisible(false);
				extensionItem.setVisible(true);
				this.chatRoom.extensionID = new ArrayList<Integer>();
				for(int i=0; i<this.conversation.getOtherUsersList().get(0).getExtensions().size(); i++){
					this.chatRoom.extensionID.add( Integer.parseInt(this.conversation.getOtherUsersList().get(0).getExtensions().get(i)) );
					extensionsMenu.add( extensionItem.getGroupId(), this.chatRoom.extensionID.get(i), i, 
							getResources().getString( R.string.text_abbreviation_extension ) +
							": " + this.conversation.getOtherUsersList().get(0).getExtensions().get(i) );
				}
			}
		}
		
		@SuppressWarnings("static-access")
		SearchManager searchManager = (SearchManager) this.getSystemService(
				this.getApplicationContext().SEARCH_SERVICE );
		this.chatRoom.searchMenuItem = menu.findItem( R.id.searchMessage );
		this.chatRoom.searchMenuItem.setOnActionExpandListener(new OnActionExpandListener() {
			
			@Override
			public boolean onMenuItemActionExpand(MenuItem item) {
				if( chatRoom.searchView != null) chatRoom.searchView.setQuery("", false);
				menu.removeItem(R.id.callContact);
				menu.removeItem(R.id.extensionsMenu);
				menu.removeItem(R.id.attachMedia);
				menu.removeItem(R.id.deleteConversation);
				chatRoom.showEmojiMenu(false);
				return true;
			}
			
			@Override
			public boolean onMenuItemActionCollapse(MenuItem item) {
				if( chatRoom.searchView != null){
					chatRoom.searchView.setQuery("", false);
					try{ chatRoom.searchView.onActionViewCollapsed(); }
					catch(Exception e){ }
					if( conversation.isActived() )
						invalidateOptionsMenu();
				}
				return true;
			}
		});
		
		this.chatRoom.searchView = (SearchView) this.chatRoom.searchMenuItem.getActionView();
		if( this.chatRoom.searchView != null ){
			this.chatRoom.searchView.setSearchableInfo(searchManager
					.getSearchableInfo(this.getComponentName()));
			this.chatRoom.searchView.setIconifiedByDefault(false);
			this.chatRoom.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
				
				@Override
				public boolean onQueryTextSubmit(String query) {
					chatRoom.resultMsg = chatRoom.resultMsg + 0.5;
					chatRoom.goToMsg = true;
					return true;
				}
	
				@Override
				public boolean onQueryTextChange(String newText) {
					chatRoom.resultMsg = 0.0;
					chatRoom.goToMsg = false;
					chatRoom.updateAdapter();
					return true;
				}
		    	
		    });
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case android.R.id.home:
	            this.finish();
	            return true;
	        case R.id.deleteConversation:
	        	this.conversation.clear();
	        	return true;
	        case R.id.callContact:
	        	if( !this.conversation.isMultiUser() && this.conversation.getNumberOfOtherUsers() == 1 ){
		        	@SuppressWarnings("unused")
					int ext = Integer.parseInt(this.conversation.getOtherUsersList().get(0).getExtensions().get(0));
	        	}
	        	return true;
	        default:
	        	int id = item.getItemId();
	        	for( int i=0; i<this.chatRoom.extensionID.size(); i++ ){
	        		if( this.chatRoom.extensionID.get(i) == id ){
	        			
	        			return true;
	        		}
	        	}
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	
}
