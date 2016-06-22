package com.fonax.android.view.fragment;

import java.util.ArrayList;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentTransaction;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.MenuItem;
import com.fonax.android.R;
import com.fonax.android.controller.Account;
import com.fonax.android.model.Conversation;
import com.fonax.android.view.adapter.MessageAdapter;
import com.rockerhieu.emojicon.EmojiconEditText;
import com.rockerhieu.emojicon.EmojiconGridFragment;
import com.rockerhieu.emojicon.EmojiconsFragment;
import com.rockerhieu.emojicon.emoji.Emojicon;

public class ChatRoomFragment extends SherlockFragment implements
		View.OnClickListener,
		EmojiconGridFragment.OnEmojiconClickedListener,
		EmojiconsFragment.OnEmojiconBackspaceClickedListener {
	
	private static int layout = R.layout.fragment_chat_room;
	private static boolean showPictures = true;

	private RelativeLayout.LayoutParams params;
	public ArrayList<Integer> extensionID;
	private InputMethodManager keyboard;
	protected Conversation conversation;
	private EmojiconEditText chatInput;
	private ListView messagesListView;
	private FrameLayout emojiMenuView;
	private ImageView emojiBottomView;
	private LinearLayout inputBarView;
	public MenuItem searchMenuItem;
	private ActionMode mActionMode;
	private MessageAdapter adapter;
	public SearchView searchView;
	private TextView statusView;
	public double resultMsg;
	public boolean goToMsg;
	protected String title;
	private View rootView;
	
	private boolean goToLastPosition;
	private int nMsg, lastResultMsg;
	private String lastSearch;
	
	public ChatRoomFragment(Conversation conversation){
		this.conversation = conversation;
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onResume() {
 		super.onResume();
 		FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
 		transaction.add(R.id.fragment_emojicons_menu, EmojiconsFragment.newInstance(false));
 		transaction.commit();
	}
	
	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		this.rootView = inflater.inflate(layout, container, false);
		this.showKeyboard(false);
		this.mActionMode = null;
		this.searchView = null;
		this.searchMenuItem = null;
		this.resultMsg = 0.0;
		this.goToMsg = false;
		
		// Set Conversation
		this.conversation.active();
		this.conversation.readAll();
		
		// Get views
		this.statusView = (TextView) this.rootView.findViewById( R.id.statusMessage	);
		this.inputBarView = (LinearLayout) this.rootView.findViewById( R.id.inputBar );
		this.emojiBottomView = (ImageView) this.rootView.findViewById( R.id.emojiIcon );
		this.chatInput = (EmojiconEditText) this.rootView.findViewById( R.id.chatInput );
		this.messagesListView = (ListView) this.rootView.findViewById( R.id.messagesList );
		this.emojiMenuView = (FrameLayout) this.rootView.findViewById( R.id.fragment_emojicons_menu );
		
		if( this.emojiMenuView == null ){
			this.emojiBottomView.setVisibility(View.INVISIBLE);
		} else {
			this.emojiBottomView.setOnClickListener(this);
			this.emojiMenuView.setOnKeyListener(new OnKeyListener() {
				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
							(keyCode == KeyEvent.KEYCODE_ENTER)) {
						sendMessage();
					} else if ((event.getAction() == KeyEvent.KEYCODE_DEL)) {
						EmojiconsFragment.backspace(chatInput);
					} else showEmojiMenu(false);
					return true;
				}
			});
		}
		
		this.chatInput.requestFocus();
		// getChildFragmentManager()
		//		.beginTransaction()
		//		.replace(this.idEmoticonsMenu, EmojiconsFragment.newInstance(false) );
		
		// Set listener views
		((ImageView) this.rootView.findViewById(R.id.chatSend)).setOnClickListener(this);
	    this.chatInput.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
						(keyCode == KeyEvent.KEYCODE_ENTER)) {
					sendMessage();
					return true;
				}
				return false;
			}
		});
		this.chatInput.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(isShowingEmojiMenu())
					showKeyboard(false);
			}
		});
		// this.chatInput.setOnFocusChangeListener(new OnFocusChangeListener() {
        //    @Override
        //    public void onFocusChange(View v, boolean hasFocus) {
        //    	chatInput.post(new Runnable() {
        //            @SuppressWarnings("static-access")
		//			@Override
        //            public void run() {
        //            	keyboard = (InputMethodManager) getSherlockActivity().getSystemService(getSherlockActivity().getApplicationContext().INPUT_METHOD_SERVICE);
        //            	keyboard.showSoftInput(chatInput, InputMethodManager.SHOW_IMPLICIT);
        //            }
        //        });
        //    }
        //});
		this.messagesListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if( conversation.numberSelectedMessage()>0 ){
					int pre = conversation.numberSelectedMessage();
					conversation.changeStatusSelecctionOfMessage(position);
					
					MessageAdapter newAdapter = new MessageAdapter( getSherlockActivity(),
							conversation, ChatRoomFragment.showPictures, "" );
					Parcelable state = messagesListView.onSaveInstanceState();
					messagesListView.setAdapter(newAdapter);
					messagesListView.onRestoreInstanceState(state);
					
					if(pre>0 && conversation.numberSelectedMessage()==0){
						mActionMode.finish();
						mActionMode = null;
					} else {
						mActionMode.invalidate();
					}
				}
			}
		});
		this.messagesListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent,
					View view, int position, long id) {
				int pre = conversation.numberSelectedMessage();
				conversation.changeStatusSelecctionOfMessage(position);
				
				MessageAdapter newAdapter = new MessageAdapter( getSherlockActivity(),
						conversation, ChatRoomFragment.showPictures, "" );
				Parcelable state = messagesListView.onSaveInstanceState();
				messagesListView.setAdapter(newAdapter);
				messagesListView.onRestoreInstanceState(state);
				
				if( conversation.numberSelectedMessage()==0 ){
					mActionMode.finish();
					mActionMode = null;
				} else {
					if( conversation.numberSelectedMessage() == 1 && pre == 0 ){
						mActionMode = getSherlockActivity().startActionMode(new ActionBarCallBack());
					}
					mActionMode.invalidate();
				}
				return true;
			}
		});
		
		setRetainInstance(true);
		setHasOptionsMenu(true);

	    this.nMsg = 0;
	    this.lastSearch = "";
	    this.lastResultMsg = 0;
	    this.goToLastPosition = true;
	    this.updateAdapter();
		
		// Updating messages in real-time
		if( this.conversation.canChatting() ){
			this.params = (RelativeLayout.LayoutParams) this.statusView.getLayoutParams();
			this.params.height = 0;
		}
		else{
			showKeyboard(false);
			this.params = (RelativeLayout.LayoutParams) this.inputBarView.getLayoutParams();
			this.params.height = 0;
		}
		
		return this.rootView;
	}
	
	@Override
	public void onDestroyView(){
		if( this.conversation != null ) this.conversation.unactive();
	    this.collapseSearchView();
		super.onDestroyView();
	}
	
	private void collapseSearchView(){
		if( this.searchMenuItem != null ){
			this.searchMenuItem.collapseActionView();
		}
	}
	
	@Override
	public void onEmojiconBackspaceClicked(View v) {
        EmojiconsFragment.backspace(this.chatInput);
	}

	@Override
	public void onEmojiconClicked(Emojicon emojicon) {
		EmojiconsFragment.input(this.chatInput, emojicon);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.chatSend:
			sendMessage();
			break;
		case R.id.emojiIcon:
			showEmojiMenu();
			if( !isShowingEmojiMenu() ) showKeyboard(true);
			break;
		default:
			break;
	}
	}
	
	public MessageAdapter getNewAdapter(){
		String searchFilter = "";
		MessageAdapter newAdapter = null;
		int size = conversation.getMessages().size();
		if( searchMenuItem != null && searchView != null ){
			if( searchMenuItem.isActionViewExpanded() ){
				CharSequence cs = searchView.getQuery();
				searchFilter = (String) cs.toString();
				if( searchFilter == null ) searchFilter = "";
			}
		}
		
		if( this.nMsg != size || !this.lastSearch.equals(searchFilter) || goToMsg ){
			conversation.readAll();
			newAdapter = new MessageAdapter( getSherlockActivity(),
					conversation, ChatRoomFragment.showPictures, searchFilter );
			if(size == 0){
				this.goToLastPosition = true;
			} else {
				int lastPosition = messagesListView.getLastVisiblePosition();
				if( ( conversation.getMessages().get(size-1).getFrom().equals( Account.getIt().getUser().getUsername() ) )
						|| ( lastPosition == size-2 ) )
					this.goToLastPosition = true;
			}
			this.lastSearch = searchFilter + "";
			this.nMsg = size;
		}
		return newAdapter;
	}
	
	public void setAdapter(MessageAdapter newAdapter) {
		int rMsg = (int) resultMsg;
		if( rMsg > resultMsg ) rMsg--;
		if( goToMsg && rMsg>0 && lastResultMsg!=rMsg ){
			lastResultMsg = rMsg;
			goToMsg = false;
			int index = -1;
			for(int i=0; i<conversation.getMessages().size(); i++){
				@SuppressWarnings("unused")
				View view = adapter.getView(i, null, messagesListView);
			}
			
			int j = 0;
			boolean find = false;
			for(int i=0; i<conversation.getMessages().size() && !find; i++){
				if( conversation.getMessages().get(i).getSearchResult() ){
					j++;
					if( j == rMsg ){
						find = true;
						index = i;
					}
				}
			}
			if( !find ){
				rMsg = 1;
				for(int i=0; i<conversation.getMessages().size() && !find; i++){
					if( conversation.getMessages().get(i).getSearchResult() ){
						j++;
						if( j == rMsg ){
							find = true;
							index = i;
						}
					}
				}
				if (find) resultMsg = 1.0;
				else resultMsg = 0.0;
			}
			if( find ){
				View v = messagesListView.getChildAt(0);
				int top = (v == null) ? 0 : (v.getTop() - messagesListView.getPaddingTop());
				messagesListView.setSelectionFromTop(index-1, top);
			}
		} else if( newAdapter != null ) {
			if( goToLastPosition ) {
				messagesListView.setAdapter(newAdapter);
				goToLastPosition = false;
			} else {
				Parcelable state = messagesListView.onSaveInstanceState();
				messagesListView.setAdapter(newAdapter);
				messagesListView.onRestoreInstanceState(state);
			}
			this.adapter = newAdapter;
		}
	}
	
	public void updateAdapter(){
		MessageAdapter adapter = getNewAdapter(); 
		setAdapter( adapter );
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
			if( conversation.numberSelectedMessage() > 0 ){
				String postTitle = getResources().getString(R.string.text_selected_message);
				if( conversation.numberSelectedMessage() > 1 )
					postTitle = getResources().getString(R.string.text_selected_messages);
				String title = conversation.numberSelectedMessage() + " " + postTitle;
				if( conversation.numberSelectedMessage() < 10 )
					title = "0" + title;
				mode.setTitle( title );
				
				int nButtoms = 0;
				menu.add(0, R.id.deleteMessages, nButtoms++, getResources()
						.getString(R.string.action_chat_delete_messages))
						.setIcon(R.drawable.ic_action_discard);
				menu.add(0, R.id.copyMessages, nButtoms++, getResources()
						.getString(R.string.action_copy))
						.setIcon(R.drawable.ic_action_copy);
			}
			return true;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode,
				android.view.MenuItem item) {
			switch (item.getItemId()) {
				case R.id.deleteMessages:
					conversation.deleteSelectedMessages();
					break;
				case R.id.copyMessages:
					conversation.selectedMessagesToClipboard(getSherlockActivity().getApplicationContext());
					break;
				default:
					break;
			}
			mActionMode.finish();
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			conversation.unselectAllMessages();
			MessageAdapter newAdapter = new MessageAdapter( getSherlockActivity(),
					conversation, ChatRoomFragment.showPictures, "" );
			Parcelable state = messagesListView.onSaveInstanceState();
			messagesListView.setAdapter(newAdapter);
			messagesListView.onRestoreInstanceState(state);
		}
		
	}
	
	@SuppressWarnings("static-access")
	private void showKeyboard(Boolean show){
		if( show != null && chatInput != null ){
			if( keyboard == null )
				this.keyboard = (InputMethodManager) getSherlockActivity().getSystemService(getSherlockActivity().getApplicationContext().INPUT_METHOD_SERVICE);
			if( keyboard != null ){
				if( show && !isShowingEmojiMenu() )
					keyboard.showSoftInput(chatInput, InputMethodManager.SHOW_IMPLICIT);
			    else
			    	keyboard.hideSoftInputFromWindow(chatInput.getWindowToken(), 0);
			}
		}
	}
	
	public void showEmojiMenu(Boolean show){
		if( show != null && this.emojiMenuView != null ){
			this.params = (RelativeLayout.LayoutParams) this.emojiMenuView.getLayoutParams();
			if( this.params != null && this.searchMenuItem != null && this.emojiBottomView != null ){
				if( show && !this.searchMenuItem.isActionViewExpanded() ){
					this.params.height = 350;
					showKeyboard(false);
					this.emojiBottomView.setImageResource( R.drawable.ic_action_keyboard );
				}  else {
					this.params.height = 0;
					this.emojiBottomView.setImageResource( R.drawable.orca_emoji_category_people );
				}
				this.emojiMenuView.setLayoutParams(this.params);
			}
		}
	}
	
	private boolean isShowingEmojiMenu(){
		if( this.emojiMenuView != null )
			return ( this.emojiMenuView.getLayoutParams().height != 0 );
		else
			return false;
	}
	
	private void showEmojiMenu(){
		if( this.emojiMenuView != null ){
			this.params = (RelativeLayout.LayoutParams) this.emojiMenuView.getLayoutParams();
			if( this.params != null ) showEmojiMenu( this.params.height == 0 );
		}
	}
		
	public boolean onBackPressed(){
		boolean otherAction = false;
		if( this.isShowingEmojiMenu() ){
			showEmojiMenu(false);
			otherAction = true;
		}
		if(  this.searchMenuItem != null ){
			if( this.searchMenuItem.isActionViewExpanded() ){
				this.searchMenuItem.collapseActionView();
				this.updateAdapter();
				otherAction = true;
			}
		}
		if(  this.conversation != null ){
			if( this.conversation.numberSelectedMessage() > 0 ){
				this.conversation.unselectAllMessages();
				mActionMode.finish();
				mActionMode = null;
				otherAction = true;
			}
		}
		return !otherAction;
    }
	
	private void sendMessage(){
		this.conversation.sendMessage( this.chatInput.getText().toString() );
		this.chatInput.setText("");
		showEmojiMenu(false);
	}
	
}
