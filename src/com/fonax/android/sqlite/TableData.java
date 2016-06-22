package com.fonax.android.sqlite;

import android.provider.BaseColumns;

public class TableData {
	
	public static final String DATABASE_NAME = "ap_android";
	
	public TableData(){
		
	}
	
	public static abstract class MessageSingleConversationTable implements BaseColumns{
		public static final String TABLE_NAME = "message_single_conversation";
		
		public static final String HOST = "host";
		public static final String ACCOUNT_USERNAME = "account_username";
		public static final String FROM = "user_from";
		public static final String TO = "user_to";
		public static final String DATETIME = "datetime";
		public static final String BODY = "body";
		public static final String READ = "read";
	}
	
	public static abstract class AccountsTable implements BaseColumns{
		public static final String TABLE_NAME = "accounts";
		
		public static final String HOST = "host";
		public static final String ACCOUNT_USERNAME = "account_username";
		public static final String ACCOUNT_PASSWORD = "account_password";
		public static final String REMEMBER_PASSWORD = "remember_password";
		public static final String LAST_LOGIN = "last_login";
	}
	
	public static abstract class ProfilePicturesTable implements BaseColumns{
		public static final String TABLE_NAME = "profile_pictures";
		
		public static final String HOST = "host";
		public static final String ACCOUNT_USERNAME = "account_username";
		public static final String ACCOUNT_PICTURE = "account_picture";
		public static final String LAST_UPDATE = "last_update";
	}
	
}
