package com.fonax.android.view.activity;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.fonax.android.R;
import com.fonax.android.controller.Account;
import com.fonax.android.view.fragment.GenericConversationsListFragment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

public class PhoneManagerActivity extends SherlockFragmentActivity {
	private com.fonax.android.model.ActionBar actionBar;
	private int mainLayout = R.layout.activity_manager;
	private FragmentsUpdaterAsyncTask fragmentsUpdater;
	private Tab conversationsTab, contactsTab;
	private Boolean runningTasks;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.runningTasks = false;
		
		if ( !NoInternetActivity.isOnline( getApplicationContext() ) ) {
			Intent intent = new Intent(getApplicationContext(),
					NoInternetActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			overridePendingTransition(0, 0);
			finish();
		} else if ( Account.getIt() == null ) {
			Intent intent = new Intent(getApplicationContext(),
					LoginActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			overridePendingTransition(0, 0);
			finish();
		} else if ( !Account.getIt().isLogged() ) {
			Intent intent = new Intent(getApplicationContext(),
					LoginActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			overridePendingTransition(0, 0);
			finish();
		} else {
			setContentView(this.mainLayout);
			
			// Set Action Bar
			this.actionBar = new com.fonax.android.model.ActionBar(this);
			this.actionBar.setGenericBar(false);
			
			if( Account.getIt().getUser().canChatting() ||
					!Account.getIt().getConversationManager().getActivedConversations().isEmpty() ){
				this.actionBar.getBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
				
				// Set Tab Icon and Titles
				this.conversationsTab = this.actionBar.getBar().newTab()
						.setIcon( R.drawable.ic_action_chat )
						.setText( getString( R.string.title_tab_chats_list ) );
				this.contactsTab = this.actionBar.getBar().newTab()
						.setIcon( R.drawable.ic_action_person )
						.setText( getString( R.string.title_tab_contacts_list ) );
				
				// Set Tab Listeners
				this.conversationsTab.setTabListener( new ManagerTabsListener( Account.getIt().getConversationsFragment() ) );
				this.contactsTab.setTabListener( new ManagerTabsListener( Account.getIt().getContactsFragment() ) );
				
				// Add tabs in Action Bar
				this.actionBar.getBar().addTab( this.conversationsTab );
				this.actionBar.getBar().addTab( this.contactsTab );
			} else {
				GenericConversationsListFragment clf = Account.getIt().getContactsFragment();
				FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
				transaction.add(R.id.fragment_container, clf);
				transaction.commit();	
			}
			
			Account.running = true;
		}
		Account.getIt().resetStausConnection();
		
	}
	
	private void startAsyncTask() {
		if( !this.runningTasks ){
			if( this.fragmentsUpdater == null ){
				this.fragmentsUpdater = new FragmentsUpdaterAsyncTask();
			}
			this.runningTasks = true;
			this.fragmentsUpdater.execute();
		}
	}
	
	private void stopAsyncTask() {
		if( this.runningTasks ){
			this.runningTasks = false;
			if( this.fragmentsUpdater != null ){
				this.fragmentsUpdater.cancel(false);
				this.fragmentsUpdater = null;
			}
		}
	}
	
	@Override
	protected void onResume() {
 		super.onResume();
		this.startAsyncTask();
	}
	
	@Override
	protected void onStop() {
 		super.onStop();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		this.stopAsyncTask();
	}
	
	private class FragmentsUpdaterAsyncTask extends AsyncTask<Void, Void, Boolean> {
		
		@Override
		protected Boolean doInBackground(Void... arg0) {
			Boolean result = false;
			if( !this.isCancelled() && runningTasks ){
				// Set Contacts Adapter
				if( Account.getIt().getContactsFragment().isVisible() )
					Account.getIt().getContactsFragment().updateAdapter();
				
				// Set Conversations Adapter
				if( Account.getIt().getConversationsFragment().isVisible() )
					Account.getIt().getConversationsFragment().updateAdapter();
				
				result = true;
			}
			return result;
		}
		
		@Override
		protected void onPostExecute(Boolean arg) {
			super.onPostExecute( arg );
			if( !this.isCancelled() && runningTasks  ) {
				if( arg ){
					if( Account.getIt().getContactsFragment().isVisible() )
						Account.getIt().getContactsFragment().setListView(true);
					
					if( Account.getIt().getConversationsFragment().isVisible() )
						Account.getIt().getConversationsFragment().setListView(true);
				}
				new FragmentsUpdaterAsyncTask( ).execute();
			}
		}
		
	}
	
	public class ManagerTabsListener implements TabListener {
		GenericConversationsListFragment fragment;
	 
		public ManagerTabsListener(GenericConversationsListFragment fragment) {
			this.fragment = fragment;
		}
	 
		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			ft.replace(R.id.fragment_container, fragment);
		}
	 
		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			ft.remove(fragment);
		}
	 
		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {
			
		}
	}
	
	@Override
    public void onBackPressed(){
		boolean someAction = false;
		if( Account.getIt().getContactsFragment().isVisible() ){
			someAction = Account.getIt().getContactsFragment().unselectContacts() ||
					Account.getIt().getContactsFragment().collapseSearchView();
		} else if( Account.getIt().getConversationsFragment().isVisible() ){
			someAction = Account.getIt().getConversationsFragment().unselectContacts() ||
					Account.getIt().getConversationsFragment().collapseSearchView();
		}
		
		if( !someAction ) super.onBackPressed( );
    }
	
}