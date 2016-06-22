package com.fonax.android.view.adapter;

import java.sql.Timestamp;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.support.v4.app.FragmentActivity;
import android.text.Spannable;
import android.text.style.BackgroundColorSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.fonax.android.R;
import com.fonax.android.controller.Account;
import com.fonax.android.model.Contact;
import com.fonax.android.model.Conversation;
import com.fonax.android.model.Note;
import com.rockerhieu.emojicon.EmojiconTextView;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends ArrayAdapter<Note>{
	private static final int layoutResourceId = R.layout.itemlist_message_chat;
	private static int maxWidthMessages, minWidthMessages;
	private ArrayList<Note> messageList;
	private static String searchFilter;
	private static FragmentActivity activity;
	private MessageHolder holder;
	private Boolean showPicture;
	private Boolean conference;
	
	public MessageAdapter( SherlockFragmentActivity fActivity, ArrayList<Note> messageList, Boolean conference,
			Boolean showPicture, String searchFilter, int widthMessages ){
		super(activity.getApplicationContext(), layoutResourceId, messageList );
		MessageAdapter.searchFilter = searchFilter.toLowerCase();
		MessageAdapter.maxWidthMessages = widthMessages;
		MessageAdapter.activity = (FragmentActivity) fActivity;
		this.messageList = messageList;
		this.showPicture = showPicture;
		this.conference = conference;
		
		if( MessageAdapter.searchFilter == null )
			MessageAdapter.searchFilter = "";
		else{
			MessageAdapter.searchFilter = MessageAdapter.searchFilter.toLowerCase();
			MessageAdapter.searchFilter = Normalizer.normalize(MessageAdapter.searchFilter, Normalizer.Form.NFD);
			MessageAdapter.searchFilter = MessageAdapter.searchFilter.replaceAll("[^\\p{ASCII}]", "");
		}
	}
	
	public MessageAdapter( SherlockFragmentActivity fActivity, Conversation conversation, Boolean showPicture, String searchFilter){
		super(fActivity.getApplicationContext(), layoutResourceId, conversation.getMessages() );
		MessageAdapter.activity = (FragmentActivity) fActivity;
		this.messageList = conversation.getMessages();
		this.conference = conversation.isMultiUser();
		MessageAdapter.searchFilter = searchFilter;
		MessageAdapter.maxWidthMessages = -1;
		this.showPicture = showPicture;
		
		if( MessageAdapter.searchFilter == null )
			MessageAdapter.searchFilter = "";
		else{
			MessageAdapter.searchFilter = MessageAdapter.searchFilter.toLowerCase();
			MessageAdapter.searchFilter = Normalizer.normalize(MessageAdapter.searchFilter, Normalizer.Form.NFD);
			MessageAdapter.searchFilter = MessageAdapter.searchFilter.replaceAll("[^\\p{ASCII}]", "");
		}
	}
	
	@Override
	public Note getItem(int position) {
		return this.messageList.get(position);
	}
	
	@Override
	public int getItemViewType(int position) {
	    return (this.messageList.get(position).getFrom().equals(Account.getIt().getUser().getUsername())) ? 0 : 1;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	@SuppressLint("ViewHolder")
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater lInf = MessageAdapter.activity.getLayoutInflater();
		convertView = lInf.inflate(layoutResourceId, parent, false);
		
		Drawable profilePicture = null;
		Note c = this.messageList.get(position);
		int type = getItemViewType(position);
		Timestamp msgTime = c.getTimestamp();
		
		// Set date-time
		String sender = "";
		Timestamp now = new Timestamp(new Date().getTime());
		SimpleDateFormat format = new SimpleDateFormat("hh:mm a");
		Timestamp yesterday = new Timestamp(new Date().getTime() - 86400000);
		if( yesterday.getYear()==msgTime.getYear() && yesterday.getMonth()==msgTime.getMonth()
				&& yesterday.getDate()==msgTime.getDate() ){
			sender = MessageAdapter.activity.getApplicationContext().getResources().getString(R.string.text_chat_yesterday)
					+ ", " + format.format(msgTime);
		} else {
			if( !(now.getYear()==msgTime.getYear() && now.getMonth()==msgTime.getMonth()
					&& now.getDate()==msgTime.getDate()) )
				format = new SimpleDateFormat("EEE, MMM dd yyyy. hh:mm a");
			sender = format.format(msgTime);
		}
		
		// Set user details
		if (type == 0)
			profilePicture = Account.getIt().getUser().getProfilePicture();
		else{
			Contact contact = Account.getIt().getContactsManager().getContact(c.getFrom());
			profilePicture = contact.getProfilePicture();
			if(this.conference)
				sender = contact.getName() + " • " + sender;
		}
		
		// Set holder
		this.holder = null;
		this.holder = new MessageHolder( this.getItem(position), convertView, this.showPicture);
		convertView.setTag(this.holder);
		this.holder.setupItem(type, profilePicture, sender, c.isBottom(), convertView);
		if( position == 0 ) convertView.setPadding(0, 4, 0, 0);
		
		return convertView;
	}
	
	public static class MessageHolder {
		private static final int pictureId = R.id.imagePicture;
		private static final int statusId = R.id.statusMessage;
		private static final int senderId = R.id.sender;
		private static final int bodyId = R.id.body;
		private Boolean showPicture;
		private float sizeText;
		
		private Note message;
		public EmojiconTextView bodyView;
		public TextView senderView;
		public CircleImageView pictureView;
		public ImageView statusView;
		
		public MessageHolder(Note message, View view, Boolean showPicture){
			this.message = message;
		    this.showPicture = showPicture;
			this.pictureView = (CircleImageView) view.findViewById(pictureId);
			this.senderView = (TextView) view.findViewById(senderId);
			this.bodyView = (EmojiconTextView) view.findViewById(bodyId);
			this.statusView = (ImageView) view.findViewById(statusId);
			
			float density = activity.getApplicationContext().getResources().getDisplayMetrics().density;
			this.sizeText = (int)(this.bodyView.getTextSize() * density * 1.25);
			minWidthMessages = (int)(this.sizeText * 2.0);
		}
		
		@SuppressLint("RtlHardcoded")
		private void setupItem(int type, Drawable profilePicture, String sender, Boolean details, View view) {
			this.bodyView.setText( this.message.getBody(), BufferType.SPANNABLE );
			RelativeLayout.LayoutParams params;
			this.senderView.setText( sender );
			if(profilePicture != null )
				this.pictureView.setImageDrawable(profilePicture);
			
			if( this.message.isSelected() )
				view.setBackgroundColor( activity.getApplicationContext().getResources().getColor(
						R.color.lightSkyBlue) );
			
			String searchBody = this.message.getBody().toLowerCase();
			if( searchBody == null )
				searchBody = "";
			else if( !searchBody.isEmpty() ){
				searchBody = searchBody.toLowerCase();
				searchBody = Normalizer.normalize(searchBody, Normalizer.Form.NFD);
				searchBody = searchBody.replaceAll("[^\\p{ASCII}]", "");
			}
			
			int lengthSearch = searchFilter.length();
			if( lengthSearch>0 && searchBody.contains(searchFilter) ){
				Spannable s = (Spannable)this.bodyView.getText();
				for(int start=0; start<lengthSearch; start++){
					start = searchBody.indexOf(searchFilter, start);
					if(start<0) start = lengthSearch;
					else{
						s.setSpan(new BackgroundColorSpan(activity.getApplicationContext().getResources()
								.getColor(R.color.yelow)), start, start+lengthSearch,
								Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					}
				}
				this.message.setSearchResult(true);
			} else this.message.setSearchResult(false);
			
			
			if (type == 0){ // Set message to right
				
				// Picture
				params = (RelativeLayout.LayoutParams) this.pictureView.getLayoutParams();
				params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
				params.leftMargin = 6;
				
				// Sender
				params = (RelativeLayout.LayoutParams) this.senderView.getLayoutParams();
				params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
				params.addRule(RelativeLayout.LEFT_OF, pictureId);
				this.senderView.setGravity(Gravity.RIGHT);
				
				// Body
				params = (RelativeLayout.LayoutParams) this.bodyView.getLayoutParams();
				params.addRule(RelativeLayout.LEFT_OF, pictureId);
				this.bodyView.setGravity(Gravity.RIGHT);
				if( this.message.isTop() && this.message.isBottom() )
					this.bodyView.setBackgroundResource(R.drawable.chat_bubble_right_all);
				else if( this.message.isTop() && !this.message.isBottom() )
					this.bodyView.setBackgroundResource(R.drawable.chat_bubble_right_top);
				else if( !this.message.isTop() && this.message.isBottom() )
					this.bodyView.setBackgroundResource(R.drawable.chat_bubble_right_bottom);
				else
					this.bodyView.setBackgroundResource(R.drawable.chat_bubble_right_any);
				
				// Status
				if( !details ){
					params = (RelativeLayout.LayoutParams) this.statusView.getLayoutParams();
					params.width = 0;
				} else if( message.isReceivedByServer() ){
						this.statusView.setImageResource(R.drawable.ic_check);
				}
			
			} else { // Set message to left
				
				// Picture
				params = (RelativeLayout.LayoutParams) this.pictureView.getLayoutParams();
				params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
				params.rightMargin = 6;
				
				// Sender
				params = (RelativeLayout.LayoutParams) this.senderView.getLayoutParams();
				params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
				params.addRule(RelativeLayout.RIGHT_OF, pictureId);
				
				// Body
				params = (RelativeLayout.LayoutParams) this.bodyView.getLayoutParams();
				params.addRule(RelativeLayout.RIGHT_OF, pictureId);
				if( this.message.isTop() && this.message.isBottom() )
					this.bodyView.setBackgroundResource(R.drawable.chat_bubble_left_all);
				else if( this.message.isTop() && !this.message.isBottom() )
					this.bodyView.setBackgroundResource(R.drawable.chat_bubble_left_top);
				else if( !this.message.isTop() && this.message.isBottom() )
					this.bodyView.setBackgroundResource(R.drawable.chat_bubble_left_bottom);
				else
					this.bodyView.setBackgroundResource(R.drawable.chat_bubble_left_any);
				
				// Status
				params = (RelativeLayout.LayoutParams) this.statusView.getLayoutParams();
				params.width = 0;
				
			}
			
			if(!this.showPicture){
				params = (RelativeLayout.LayoutParams) this.pictureView.getLayoutParams();
				params.height = 1;
				params.width = 1;
			}
			
			if(!details){
				params = (RelativeLayout.LayoutParams) this.senderView.getLayoutParams();
				params.height = 0;
				
				params = (RelativeLayout.LayoutParams) this.pictureView.getLayoutParams();
				params.height = 0;
			}
			
			if( maxWidthMessages>=0 ){
				int sizeBubble = (int)((this.message.getSize()) * sizeText);
				if( sizeBubble < minWidthMessages ) sizeBubble = minWidthMessages;
				else if( sizeBubble > maxWidthMessages ) sizeBubble = maxWidthMessages;
				
				params = (RelativeLayout.LayoutParams) this.bodyView.getLayoutParams();
				params.width = sizeBubble;
				this.bodyView.setLayoutParams(params);
			}
			
			int paddingTop = 7, paddingBottom = 7;
			if( !this.message.isTop() ) paddingTop = 1;
			if( !this.message.isBottom() ) paddingBottom = 1;
			
			this.bodyView.setPadding(7, paddingTop, 7, paddingBottom);
			
		}
	}
	
}
