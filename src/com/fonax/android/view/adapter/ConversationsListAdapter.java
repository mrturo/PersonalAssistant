package com.fonax.android.view.adapter;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fonax.android.R;
import com.fonax.android.controller.Account;
import com.fonax.android.model.Conversation;
import com.rockerhieu.emojicon.EmojiconTextView;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConversationsListAdapter extends ArrayAdapter<Conversation>{
	private static final int layoutResourceId = R.layout.itemlist_conversations_list;
	private ArrayList<Conversation> conversationsList;
	private ArrayList<String> selectedConversations;
	private boolean conversationsDetails;
	private ConversationHolder holder;
	private static Context ctx;
	
	public ConversationsListAdapter(Context ctx, ArrayList<Conversation> conversationsList,
			Boolean conversationsDetails, ArrayList<String> selectedConversations){
		super(ctx, layoutResourceId, conversationsList );
		
		if( conversationsDetails != null )
			this.conversationsDetails = conversationsDetails;
		else this.conversationsDetails = false;
		
		if( selectedConversations != null )
			this.selectedConversations = selectedConversations;
		else this.selectedConversations = new ArrayList<String>();
		
		if( conversationsList != null )
			this.conversationsList = conversationsList;
		else this.conversationsList = new ArrayList<Conversation>();
		
		ConversationsListAdapter.ctx = ctx;
	}
	
	@Override
	public Conversation getItem(int position) {
		return this.conversationsList.get(position);
	}
	
	@Override
	@SuppressLint("ViewHolder")
	public View getView(int position, View convertView, ViewGroup parent) {
		this.holder = null;
		LayoutInflater inflater = ((Activity) ConversationsListAdapter.ctx).getLayoutInflater();
		convertView = inflater.inflate(layoutResourceId, parent, false);
		this.holder = new ConversationHolder( this.conversationsList.get(position), convertView);
		convertView.setTag(this.holder);
		this.holder.setupItem(convertView, this.selectedConversations, this.conversationsDetails );
		return convertView;
	}
	
	public static class ConversationHolder {
		private Conversation conversation;
		private TextView nameView;
		private TextView unreadMessagesView;
		private EmojiconTextView lastMessageBodyView;
		private TextView lastMessageTimeView;
		private ImageView availableIconView;
		private CircleImageView profilePictureView;
		
		public ConversationHolder(Conversation conversation, View view){ 
			this.conversation = conversation;
			this.nameView = (TextView) view.findViewById(R.id.name);
			this.lastMessageTimeView = (TextView) view.findViewById(R.id.dateTime);
			this.lastMessageBodyView = (EmojiconTextView) view.findViewById(R.id.lastMessage);
			this.profilePictureView = (CircleImageView) view.findViewById(R.id.profilePicture);
			this.unreadMessagesView = (TextView) view.findViewById(R.id.noUnreadMessages);
			this.availableIconView = (ImageView) view.findViewById(R.id.availableIcon);
		}
		
		@SuppressWarnings("deprecation")
		@SuppressLint("RtlHardcoded")
		private void setupItem(View view, ArrayList<String> selectedContacts, Boolean conversationsDetails ) {
			RelativeLayout.LayoutParams params;
			this.nameView.setText( this.conversation.getName() );
			Boolean canChatting = this.conversation.canChatting() && Account.getIt().getUser().canChatting();
			if( this.conversation.getPicture(ctx) != null )
				this.profilePictureView.setImageDrawable(this.conversation.getPicture(ctx));
			if( !this.conversation.isMultiUser() && canChatting ){
				if( this.conversation.getOtherUsersList().get(0).isAvaible(Account.getIt().getRoster()) )
					this.availableIconView.setImageResource(R.drawable.ic_chat_available);
			} else this.availableIconView.setVisibility(View.INVISIBLE);
			
			if( !this.conversation.isMultiUser() ){
				if( selectedContacts.contains(this.conversation.getOtherUsersList().get(0).getUsername()) )
					view.setBackgroundColor( ctx.getResources().getColor(R.color.lightSkyBlue) );
			}
			
			if( !conversationsDetails ){
				
				params = (RelativeLayout.LayoutParams) this.unreadMessagesView.getLayoutParams();
				params.width = 0;
				params = (RelativeLayout.LayoutParams) this.lastMessageBodyView.getLayoutParams();
				params.height = 0;
				params = (RelativeLayout.LayoutParams) this.lastMessageTimeView.getLayoutParams();
				params.height = 0;
				params = (RelativeLayout.LayoutParams) this.nameView.getLayoutParams();
				params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
				params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
				
			} else {
				
				String dateTime = "";
				Timestamp now = new Timestamp(new Date().getTime());
				Timestamp time = this.conversation.getLastMessageTime();
				Timestamp yesterday = new Timestamp(new Date().getTime() - 86400000);
				if(time != null){		
					if( yesterday.getYear()==time.getYear() && yesterday.getMonth()==time.getMonth()
							&& yesterday.getDate()==time.getDate() ){
						dateTime = ctx.getResources().getString(R.string.text_chat_yesterday);
					} else {
						SimpleDateFormat format = new SimpleDateFormat("hh:mm a");
						if( !(now.getYear()==time.getYear() && now.getMonth()==time.getMonth()
								&& now.getDate()==time.getDate()) )
							format = new SimpleDateFormat("dd/MM/yy");
						dateTime = format.format(time);
					}
				}
				
				if( canChatting && this.conversation.getMessages().size()>0 ){
					String writer = this.conversation.getLastMessageWriter();
					if( Account.getIt().getUser().getUsername().equals(writer) )
						writer = ctx.getResources().getString(R.string.text_chat_me);
					else{
						writer = Account.getIt().getContactsManager().getContact(writer).getName();
						int i = writer.indexOf(" ");
						if(i>=0)
							writer = writer.substring(0, i);
					}
					this.lastMessageTimeView.setText( dateTime );
					this.lastMessageBodyView.setText( writer + ": " + this.conversation.getLastMessageBody() );
				} else {
					this.lastMessageTimeView.setText( "" );
					this.lastMessageBodyView.setText( "" );
					
					params = (RelativeLayout.LayoutParams) this.lastMessageBodyView.getLayoutParams();
					params.height = 0;
					params = (RelativeLayout.LayoutParams) this.lastMessageTimeView.getLayoutParams();
					params.height = 0;
					params = (RelativeLayout.LayoutParams) this.nameView.getLayoutParams();
					params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
					
				}
				
				String unreadTag = "";
				int unread = this.conversation.getUnreadMessages();
				if( canChatting && unread > 0 ){
					if( unread < 10 )
						unreadTag = "0";
					unreadTag = " " + unreadTag + unread + " ";
					this.lastMessageBodyView.setTypeface(null, Typeface.BOLD);
					this.lastMessageBodyView.setTextColor(ctx.getResources().getColor(R.color.black));
					this.lastMessageTimeView.setTextColor(ctx.getResources().getColor(R.color.black));
				}
				
				this.lastMessageBodyView.setGravity(Gravity.LEFT);
				this.unreadMessagesView.setText( unreadTag );
				
			}
		}
		
	}
	
}
