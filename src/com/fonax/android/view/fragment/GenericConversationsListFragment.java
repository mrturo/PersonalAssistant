package com.fonax.android.view.fragment;

import java.text.Normalizer;
import java.util.ArrayList;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.fonax.android.R;
import com.fonax.android.controller.Account;
import com.fonax.android.model.Contact;
import com.fonax.android.model.Conversation;
import com.fonax.android.view.activity.PhoneChatRoomActivity;
import com.fonax.android.view.activity.PhoneContactInfoActivity;
import com.fonax.android.view.activity.LoginActivity;
import com.fonax.android.view.adapter.ConversationsListAdapter;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;

public class GenericConversationsListFragment extends SherlockFragment {
	private	final int layout = R.layout.fragment_conversations_list;
	private ArrayList<String> selectedConversations;
	private RelativeLayout conversationsListLayout;
	private ArrayList<Conversation> itemsList;
	private ConversationsListAdapter adapter;
	private RelativeLayout emptyListLayout;
	private ListView conversationsListView;
	private ArrayList<Integer> extensionID;
	private FragmentActivity mainActivity;
	private ArrayList<String> extentions;
	protected boolean justConversations;
	private TextView infoNoMatchText;
	private MenuItem searchMenuItem;
	private ActionMode mActionMode;
	private SearchView searchView;
	private boolean firstTime;
	
	public GenericConversationsListFragment(){
		this.selectedConversations = new ArrayList<String>();
		this.itemsList = new ArrayList<Conversation>();
		this.extentions = new ArrayList<String>();
		this.justConversations = true;
		this.searchMenuItem = null;
		this.mainActivity = null;
		this.mActionMode = null;
		this.searchView = null;
		this.adapter = null;
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		setHasOptionsMenu(true);
	}
	
	@Override
	public void onResume() {
 		super.onResume();
	}
	
	@Override
	public void onPause() {
		super.onPause();
	}
	
	public boolean collapseSearchView() {
		if( this.searchMenuItem != null ){
			if( this.searchMenuItem.isActionViewExpanded() ){
				this.searchMenuItem.collapseActionView();
				return true;
			}
		}
		return false;
	}
	
	private void openOtherActivity(String username, Class<?> cls) {
		collapseSearchView();
		Intent intent = new Intent(getActivity().getApplicationContext(), cls);
		if( adapter != null ) intent.putExtra("user", username);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}
	
	private void selectContact(String username) {
		if( username != null ){
			if( username.length() > 0 ){
				if( selectedConversations.isEmpty() ){
					if( mActionMode != null ){
						mActionMode.finish();
						mActionMode = null;
					}
				}
				
				if( this.selectedConversations.contains( username ) ){
					this.selectedConversations.remove( username );
				}
				else
					this.selectedConversations.add( username );
				this.updateAdapter();
				this.setListView(true);
				
				if( mActionMode == null ){
					mActionMode = getActivity().startActionMode( 
							new ActionBarCallBack() );
				}
				if( mActionMode != null ){
					if( selectedConversations.isEmpty() ) {
						mActionMode.finish();
						mActionMode = null;
					} else {
						mActionMode.invalidate();
					}
				}
			}
		}
	}
	
	public boolean unselectContacts(){
		boolean result = false;
		if( !selectedConversations.isEmpty() ){
			selectedConversations.clear();
			this.updateAdapter();
			this.setListView(true);
			result = true;
		}
		return result;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View rootView = inflater.inflate(layout, container, false);
		
		this.conversationsListLayout = (RelativeLayout) rootView.findViewById(R.id.conversationsList);
		this.emptyListLayout = (RelativeLayout) rootView.findViewById(R.id.emptyList);
		this.infoNoMatchText = (TextView) rootView.findViewById(R.id.infoNoMatch);
		if( this.justConversations ) this.infoNoMatchText.setText(R.string.text_conversations_list_searh_no_match);
		else this.infoNoMatchText.setText(R.string.text_contacts_list_searh_no_match);
		this.emptyListLayout.setVisibility(View.INVISIBLE);
		this.firstTime = true;
		
		this.conversationsListView = (ListView) rootView.findViewById(R.id.list);
		this.conversationsListView.setOnItemClickListener(new OnItemClickListener() {
			
        	@Override
			public void onItemClick(AdapterView<?> listC, View view, int position,
					long id) {
				
        		Conversation c = adapter.getItem(position);
        		if( c != null ){
	        		String username = c.getOtherUsersList().get(0).getUsername();
	        		if( username != null ){
		        		if( username.length() > 0 ){
							if( selectedConversations.isEmpty() ){
								if( !Account.getIt().getUser().canChatting() ){
									openOtherActivity(username,
											PhoneContactInfoActivity.class);
								} else if( !Account.getIt().getContactsManager().getContact(username).
										canChatting() ) {
									openOtherActivity(username,
											PhoneContactInfoActivity.class);
								} else {
									openOtherActivity(username,
											PhoneChatRoomActivity.class);
								}
							}
							else{
								selectContact( username );
							}
		        		}
	        		}
        		}
				
			}
        	
		});
        this.conversationsListView.setOnItemLongClickListener(new AdapterView.
        		OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				String username = adapter.getItem(position).getOtherUsersList().get(0).getUsername();
				selectContact( username );
				return true;
			}
        	
        });
        this.extensionID = new ArrayList<Integer>();
		this.selectedConversations.clear();
		
		if( Account.getIt().getUser().canChatting() ){
			RelativeLayout.LayoutParams params;
			TextView status = (TextView) rootView.findViewById(R.id.statusAccount);
			params = (RelativeLayout.LayoutParams) status.getLayoutParams();
			params.height = 0;
		}
		
		updateAdapter();
		this.setListView(false);
		
		return rootView;
	}
	
	@Override
	public void onDestroyView(){
		super.onDestroyView();
		selectedConversations.clear();
		if( mActionMode != null )
			mActionMode.finish();
		this.collapseSearchView();
	}
	
	@SuppressWarnings("static-access")
	public void onCreateOptionsMenu(final Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.contacts_list_actions, menu);
		
		SearchManager searchManager = (SearchManager) getActivity().
				getSystemService( getActivity().getApplicationContext().
						SEARCH_SERVICE );
		this.searchMenuItem = menu.findItem( R.id.searchContact );
		this.searchView = (SearchView) searchMenuItem.getActionView();
	    if ( this.searchView != null ) {
	    	this.searchView.setSearchableInfo( searchManager
	                .getSearchableInfo( getActivity().getComponentName() ) );
	    	this.searchView.setIconifiedByDefault( false );
	    }
	    this.searchView.setOnQueryTextListener( new SearchView.OnQueryTextListener() {

			@Override
			public boolean onQueryTextSubmit( String query ) {
				updateAdapter();
				setListView(true);
				return true;
			}

			@Override
			public boolean onQueryTextChange( String newText ) {
				updateAdapter();
				setListView(true);
				return true;
			}
	    	
	    });
	    this.searchMenuItem.setOnActionExpandListener( new MenuItem.
	    		OnActionExpandListener() {

			@Override
			public boolean onMenuItemActionExpand(MenuItem item) {
				if( searchView != null ) searchView.setQuery("", false);
				menu.removeItem(R.id.deleteConversations);
				menu.removeItem(R.id.logoutAccount);
				return true;
			}

			@Override
			public boolean onMenuItemActionCollapse(MenuItem item) {
		        if( searchView != null){
					searchView.setQuery("", false);
					try{ searchView.onActionViewCollapsed(); }
					catch(Exception e){ }
				}
		        updateAdapter();
				setListView(true);
				getActivity().invalidateOptionsMenu();
				return true;
			}
	    	
	    });
	    int nButtoms = 1;
	    if( !this.itemsList.isEmpty() ){
	    	if( !searchMenuItem.isVisible() ) searchMenuItem.setVisible(true);
		    if( this.justConversations ){
			    menu.add(1, R.id.deleteConversations, nButtoms++, getResources()
					.getString(R.string.action_delete_conversation))
					.setIcon(R.drawable.ic_action_discard);
		    }
	    } else if( getSearchFilter().isEmpty() && searchMenuItem.isVisible() ) searchMenuItem.setVisible(false);
	    menu.add(1, R.id.logoutAccount, nButtoms++, getResources()
			.getString(R.string.action_logout))
			.setIcon(R.drawable.ic_action_cancel);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.logoutAccount:
				// Logout account
				Account.getIt().getCacheDatabase().logOutAccount();
				Account.newOne( getActivity().getApplicationContext() );
				
				// Go to Login Activity
				Intent intent = new Intent( getActivity().getApplicationContext(),
						LoginActivity.class );
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				
				getActivity().overridePendingTransition(0, 0);
				getActivity().finish();
				break;
				
			case R.id.deleteConversations:
				Account.getIt().getConversationManager().clearAll();
				updateAdapter();
				setListView(false);
				break;
				
			default:
				break;
				
		}
		return super.onOptionsItemSelected(item);
	}
	
	public String getSearchFilter() {
		String searchFilter = "";
		if( searchMenuItem != null && searchView != null ) {
			if( searchMenuItem.isActionViewExpanded() ){
				CharSequence cs = searchView.getQuery();
				searchFilter = (String) cs.toString();
				if( searchFilter == null ) searchFilter = "";
			}
		}
		return searchFilter;
	}
	
	public static ArrayList<Conversation> getContactList(){
		ArrayList<Conversation> result = new ArrayList<Conversation>();
		ArrayList<Contact> contactList = Account.getIt().getContactsManager().getContactList();
		if( contactList != null ){
			for( int i=0; i<contactList.size(); i++ ){
				Conversation c = Account.getIt().getConversationManager().getConversation( contactList.get(i).getUsername() );
				if( c != null ) result.add( c );
			}
		}
		return result;
	}
	
	protected ArrayList<Conversation> getActivedConversations(){
		return Account.getIt().getConversationManager().getActivedConversations();
	}
	
	private String normalizeText(String text){
		String result = "";
		if( text != null ){
			if( !text.isEmpty() ){
				result = Normalizer.normalize(text.toLowerCase(), Normalizer.Form.NFD);
				result = result.replaceAll("[^\\p{ASCII}]", "");
			}
		}
		return result;
	}
	
	protected ArrayList<Conversation> getFilteredList(ArrayList<Conversation> list, String originalFilter){
		String filter = normalizeText( originalFilter );
		if( filter.length() == 0 ){
			return list;
		}
		else{
			ArrayList<Conversation> tempList = new ArrayList<Conversation>();
			for(int i=0; i<list.size(); i++){
				Conversation tempItem = list.get(i);
				String temp = normalizeText( tempItem.getName() );
				if( temp.contains(filter) )
					tempList.add( tempItem );
				else{
					Boolean added = false;
					if( tempItem.getNumberOfOtherUsers() == 1 ){
						for(int j=0; j<tempItem.getOtherUsersList().get(0).getExtensions().size() && !added; j++){
							if( tempItem.getOtherUsersList().get(0).getExtensions().get(j).contains(filter) ){
								added = true;
								tempList.add( tempItem );
							}
						}
					}
				}
			}
			return tempList;
		}
	}
	
	protected void setItemsList(ArrayList<Conversation> newItemsList){
		int before = 0, now = 0;
		if( this.itemsList != null ){
			before = this.itemsList.size();
			this.itemsList.clear();
		} else this.itemsList = new ArrayList<Conversation>();
		
		if( newItemsList != null ){
			this.itemsList.addAll(newItemsList);
			now = this.itemsList.size();
		}
		
		if( getActivity() != null ){
			if( this.firstTime ){
				this.firstTime = false;
				if( now == 0 ) before = -1;
			}
			if( ( before==0 || now==0 ) && ( before!=now ) ){
				if( getSearchFilter().isEmpty() ){
					if( now == 0 ){
						this.conversationsListLayout.setVisibility(View.INVISIBLE);
						this.emptyListLayout.setVisibility(View.VISIBLE);
						this.infoNoMatchText.setVisibility(View.INVISIBLE);
					} else {
						this.conversationsListLayout.setVisibility(View.VISIBLE);
						this.emptyListLayout.setVisibility(View.INVISIBLE);
						getActivity().invalidateOptionsMenu();
					}
				} else if( now==0 ){
					this.conversationsListLayout.setVisibility(View.INVISIBLE);
					this.emptyListLayout.setVisibility(View.VISIBLE);
					this.infoNoMatchText.setVisibility(View.VISIBLE);
				} else {
					this.conversationsListLayout.setVisibility(View.VISIBLE);
					this.emptyListLayout.setVisibility(View.INVISIBLE);
				}
			}
		}
	}
	
	protected void updateAdapter() {
		ConversationsListAdapter adapList = null;
		if( this.mainActivity == null ){
			this.mainActivity = this.getActivity();
		}
		if( this.mainActivity != null ) {
			adapList = new ConversationsListAdapter( mainActivity, this.itemsList,
					justConversations, selectedConversations );
		}
		this.adapter = adapList;
	}
	
	public void setListView(boolean keepPosition){
		if( this.adapter != null && this.conversationsListView != null ){
			if(keepPosition){
				int index = this.conversationsListView.getFirstVisiblePosition();
				View v = this.conversationsListView.getChildAt( 0 );
				int top = ( v == null ) ? 0 : ( v.getTop() - this.conversationsListView.
						getPaddingTop() );
				this.conversationsListView.setAdapter( this.adapter );
				this.conversationsListView.setSelectionFromTop( index, top );
			} else this.conversationsListView.setAdapter( this.adapter );
		}
	}
	
	private class ActionBarCallBack implements ActionMode.Callback {
		
		@Override
		public boolean onCreateActionMode(ActionMode mode,
				android.view.Menu menu) {
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode,
				android.view.Menu menu) {
			menu.clear();
			if( selectedConversations.size() > 0 ) {
				String postTitle = getResources().getString( R.string.
						text_selected_contact );
				if( selectedConversations.size() > 1 ) {
					postTitle = getResources().getString(R.string.
							text_selected_contacts );
				}
				
				String title = selectedConversations.size() + " " + postTitle;
				if( selectedConversations.size() < 10 ) title = "0" + title;
				mode.setTitle( title );
				
				int nButtoms = 0;
				if( justConversations ){
					menu.add(0, R.id.deleteConversation, nButtoms++, getResources()
						.getString(R.string.action_delete_conversation))
						.setIcon(R.drawable.ic_action_discard);
				}
				if( selectedConversations.size() > 1) {
					menu.add(0, R.id.addGroup, nButtoms++, getResources()
						.getString(R.string.action_add_group))
						.setIcon(R.drawable.ic_action_add_group);
				}
				if( selectedConversations.size() == 1 ) {
					Conversation c = Account.getIt().getConversationManager().
							getConversation( selectedConversations.get( 0 ) );
					if(c!=null){
						if(!c.isMultiUser()){
							if( c.getNumberOfOtherUsers() == 1 )
								menu.add(0, R.id.contactInfo, nButtoms++, getResources()
										.getString(R.string.action_contact_info))
										.setIcon(R.drawable.ic_action_about);
						}
					}
				}
				if( selectedConversations.size() == 1 ) {
					extentions = Account.getIt().getContactsManager().getContact( selectedConversations
							.get(0) ).getExtensions();
					if( !extentions.isEmpty() ){
						if( extentions.size() == 1 ){
							menu.add(0, R.id.callContact, nButtoms++, getResources()
								.getString(R.string.action_call_contact))
								.setIcon(R.drawable.ic_action_call);
						} else {
							SubMenu extensionsMenu = menu.addSubMenu( 0,
									R.id.extensionsMenu, nButtoms++,
									getResources().getString( 
											R.string.action_call_contact) )
									.setIcon(R.drawable.ic_action_call);

							extensionID = new ArrayList<Integer>();
							for(int i=0; i<extentions.size(); i++){
								extensionID.add( Integer.parseInt(extentions.get(i)) );
								extensionsMenu.add( 0, extensionID.get(i), i, 
										getResources().getString( R.string.
												text_abbreviation_extension )
										+ ": " + extentions.get(i) );
							}
						}
					}
				}
			}
			return true;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode,
				android.view.MenuItem item) {
			switch (item.getItemId()) {
				
				case R.id.deleteConversation:
					for(int i=0; i<selectedConversations.size(); i++){
						Conversation c = Account.getIt().getConversationManager().
								getConversation( selectedConversations.get( i ) );
						if( c != null ) if( c.getMessages().size() > 0 ) c.clear();
					}
					selectedConversations.clear();
					updateAdapter();
					setListView(false);
					if( mActionMode != null )
						mActionMode.finish();
					break;
					
				case R.id.contactInfo:
					if( selectedConversations.size() == 1 ){
						Conversation c = Account.getIt().getConversationManager().
								getConversation( selectedConversations.get( 0 ) );
						if(c!=null) if(!c.isMultiUser()){
							if( c.getNumberOfOtherUsers() == 1 )
								openOtherActivity( selectedConversations.get(0),
										PhoneContactInfoActivity.class );
						}
					}
					selectedConversations.clear();
					updateAdapter();
					setListView(false);
					if( mActionMode != null )
						mActionMode.finish();
					break;
					
				case R.id.callContact:
					if( selectedConversations.size() == 1 ){
						@SuppressWarnings("unused")
						int ext = Integer.parseInt(extentions.get(0));
					}
					selectedConversations.clear();
					updateAdapter();
					setListView(false);
					if( mActionMode != null )
						mActionMode.finish();
					break;
					
				case R.id.extensionsMenu:
					break;
					
				default:
					if( selectedConversations.size() == 1 ){
						int id = item.getItemId();
			        	for( int i=0; i<extensionID.size(); i++ ){
			        		if( extensionID.get(i) == id ){
			        			
			        		}
			        	}
					}
					selectedConversations.clear();
					updateAdapter();
					setListView(false);
					if( mActionMode != null )
						mActionMode.finish();
					break;
				
			}
			
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			selectedConversations.clear();
			updateAdapter();
			setListView(true);
		}
		
	}
	
}
