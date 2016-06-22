package com.fonax.android.sqlite;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import com.fonax.android.controller.Account;
import com.fonax.android.model.Conversation;
import com.fonax.android.model.Note;
import com.fonax.android.model.ProfilePicture;
import com.fonax.android.sqlite.TableData.AccountsTable;
import com.fonax.android.sqlite.TableData.MessageSingleConversationTable;
import com.fonax.android.sqlite.TableData.ProfilePicturesTable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class DatabaseOperations extends SQLiteOpenHelper {

	private SQLiteDatabase reader;
	private SQLiteDatabase writer;
	public static final int database_version = 1;
	public String CREATETABLE_MESSAGE_SINGLE_CONVERSATION = "CREATE TABLE IF NOT EXISTS " +
		MessageSingleConversationTable.TABLE_NAME + "( " +
			MessageSingleConversationTable.HOST + " VARCHAR(50), " +
			MessageSingleConversationTable.ACCOUNT_USERNAME + " VARCHAR(50), " +
			MessageSingleConversationTable.FROM + " VARCHAR(50), " +
			MessageSingleConversationTable.TO + " VARCHAR(50), " +
			MessageSingleConversationTable.DATETIME + " LONG, " +
			MessageSingleConversationTable.BODY + " TEXT, " +
			MessageSingleConversationTable.READ + " VARCHAR(3)" +
		");";
	public String CREATETABLE_ACCOUNTS = "CREATE TABLE IF NOT EXISTS " +
		AccountsTable.TABLE_NAME + "( " +
			AccountsTable.HOST + " VARCHAR(50), " +
			AccountsTable.ACCOUNT_USERNAME + " VARCHAR(50), " +
			AccountsTable.ACCOUNT_PASSWORD + " VARCHAR(50), " +
			AccountsTable.REMEMBER_PASSWORD + " VARCHAR(3), " +
			AccountsTable.LAST_LOGIN + " VARCHAR(3)" +
		");";
	public String CREATETABLE_PROFILE_PICTURES = "CREATE TABLE IF NOT EXISTS " +
		ProfilePicturesTable.TABLE_NAME + "( " +
			ProfilePicturesTable.HOST + " VARCHAR(50), " +
			ProfilePicturesTable.ACCOUNT_USERNAME + " VARCHAR(50), " +
			ProfilePicturesTable.ACCOUNT_PICTURE + " BLOB, " +
			ProfilePicturesTable.LAST_UPDATE + " LONG" +
		");";
	
	public DatabaseOperations(Context context) {
		super(context, TableData.DATABASE_NAME, null, database_version);
		this.reader = this.getReadableDatabase();
		this.writer = this.getWritableDatabase();
		Log.i("CACHE DB", "DB Created");
	}

	@Override
	public void onCreate(SQLiteDatabase sdb) {
		sdb.execSQL(this.CREATETABLE_MESSAGE_SINGLE_CONVERSATION);
		Log.i("CACHE DB", "DB Table Created: Single Conversation Messages");
		
		sdb.execSQL(this.CREATETABLE_ACCOUNTS);
		Log.i("CACHE DB", "DB Table Created: Accounts");
		
		sdb.execSQL(this.CREATETABLE_PROFILE_PICTURES);
		Log.i("CACHE DB", "DB Table Created: Profile Pictures");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onUpgrade(db, oldVersion, newVersion);
	}
	
	public long addMessage(Note message){
		ContentValues cv = new ContentValues();
		cv.put(MessageSingleConversationTable.HOST, Account.HOST );
		cv.put(MessageSingleConversationTable.ACCOUNT_USERNAME, Account.getIt().getUser().getUsername() );
		cv.put(MessageSingleConversationTable.FROM, message.getFrom() );
		cv.put(MessageSingleConversationTable.TO, message.getTo() );
		cv.put(MessageSingleConversationTable.DATETIME, message.getUTCTimestamp().getTime() );
		cv.put(MessageSingleConversationTable.BODY, message.getBody() );
		cv.put(MessageSingleConversationTable.READ, ( message.isRead() ? "R" : "U" ) );
		long id = writer.insert(MessageSingleConversationTable.TABLE_NAME, null, cv);
		Log.i("CACHE DB", "One message added");
		return id;
	}
	
	public void deleteMessage(Note message){
		String selection = MessageSingleConversationTable.HOST + " LIKE ? AND " + MessageSingleConversationTable.ACCOUNT_USERNAME + " LIKE ?"
				+ " AND " + MessageSingleConversationTable.FROM + " LIKE ? AND " + MessageSingleConversationTable.TO + " LIKE ? "
				+ " AND " + MessageSingleConversationTable.DATETIME + " LIKE ? AND " + MessageSingleConversationTable.BODY + " LIKE ? ";
		String args[] = { Account.HOST, Account.getIt().getUser().getUsername(),
				message.getFrom(), message.getTo(),
				message.getUTCTimestamp().getTime() + "", message.getBody() };
		writer.delete(MessageSingleConversationTable.TABLE_NAME, selection, args);
	}
	
	public Cursor getConversation(Conversation c){
		String[] columns = {MessageSingleConversationTable.HOST, MessageSingleConversationTable.ACCOUNT_USERNAME, MessageSingleConversationTable.FROM, MessageSingleConversationTable.TO,
				MessageSingleConversationTable.DATETIME, MessageSingleConversationTable.BODY, MessageSingleConversationTable.READ};
		String selection = MessageSingleConversationTable.HOST + " LIKE ? AND " + MessageSingleConversationTable.ACCOUNT_USERNAME + " LIKE ?"
				+ " AND ( (" + MessageSingleConversationTable.FROM + " LIKE ? AND " + MessageSingleConversationTable.TO + " LIKE ? )"
				+ " OR (" + MessageSingleConversationTable.FROM + " LIKE ? AND " + MessageSingleConversationTable.TO + " LIKE ? ) )";
		String args[] = { Account.HOST, Account.getIt().getUser().getUsername(),  
				c.getOtherUsersList().get(0).getUsername(), Account.getIt().getUser().getUsername(),
				Account.getIt().getUser().getUsername(), c.getOtherUsersList().get(0).getUsername() };
		return reader.query(MessageSingleConversationTable.TABLE_NAME, columns, selection, args, null, null, MessageSingleConversationTable.DATETIME);
	}
	
	public void deleteConversation(Conversation c){
		String selection = MessageSingleConversationTable.HOST + " LIKE ? AND " + MessageSingleConversationTable.ACCOUNT_USERNAME + " LIKE ?"
				+ " AND (" + MessageSingleConversationTable.FROM + " LIKE ? AND " + MessageSingleConversationTable.TO + " LIKE ? "
				+ " OR " + MessageSingleConversationTable.FROM + " LIKE ? AND " + MessageSingleConversationTable.TO + " LIKE ? )";
		String args[] = { Account.HOST, Account.getIt().getUser().getUsername(),
				c.getOtherUsersList().get(0).getUsername(), Account.getIt().getUser().getUsername(),
				Account.getIt().getUser().getUsername(), c.getOtherUsersList().get(0).getUsername() };
		writer.delete(MessageSingleConversationTable.TABLE_NAME, selection, args);
	}
	
	public void deleteAllConversations(){
		String selection = MessageSingleConversationTable.HOST + " LIKE ? AND " + MessageSingleConversationTable.ACCOUNT_USERNAME + " LIKE ?";
		String args[] = { Account.HOST, Account.getIt().getUser().getUsername() };
		writer.delete(MessageSingleConversationTable.TABLE_NAME, selection, args);
	}
	
	public void updateReadStatus(Conversation c){
		String selection = "(" + MessageSingleConversationTable.FROM + " LIKE ? AND " + MessageSingleConversationTable.TO + " LIKE ? )"
				+ " OR (" + MessageSingleConversationTable.FROM + " LIKE ? AND " + MessageSingleConversationTable.TO + " LIKE ? )";
		String args[] = { c.getOtherUsersList().get(0).getUsername(), Account.getIt().getUser().getUsername(),
				Account.getIt().getUser().getUsername(), c.getOtherUsersList().get(0).getUsername() };
		ContentValues values = new ContentValues();
		values.put( MessageSingleConversationTable.READ, "R" );
		writer.update( MessageSingleConversationTable.TABLE_NAME , values, selection, args );
	}
	
	public boolean addAccount(int remember){
		boolean result = false;
		ContentValues cv = new ContentValues();
		String[] columns = { AccountsTable.HOST, AccountsTable.ACCOUNT_USERNAME, AccountsTable.ACCOUNT_PASSWORD };
		String selection = AccountsTable.HOST + " LIKE ? AND "
				+ AccountsTable.ACCOUNT_USERNAME + " LIKE ?";
		String args[] = { Account.HOST, Account.getIt().getUser().getUsername()  };
		Cursor c = reader.query(AccountsTable.TABLE_NAME, columns, selection, args, null, null, null);
		if( c.getCount() > 1 ){
			writer.delete(AccountsTable.TABLE_NAME, selection, args);
			c = reader.query(AccountsTable.TABLE_NAME, columns, selection, args, null, null, null);
		}
		if ( c.getCount() == 0 ){
			cv.put( AccountsTable.HOST, Account.HOST );
			cv.put( AccountsTable.ACCOUNT_USERNAME, Account.getIt().getUser().getUsername() );
			cv.put( AccountsTable.ACCOUNT_PASSWORD, Account.getIt().getUser().getPassword() );
			if( remember != 0 )
				cv.put( AccountsTable.REMEMBER_PASSWORD, ( ( remember > 0 ) ? "Y" : "N" ) );
			cv.put( AccountsTable.LAST_LOGIN, "Y" );
			long id = writer.insert(AccountsTable.TABLE_NAME, null, cv);
			if( id > 0 ){
				Log.i("CACHE DB", "Account added");
				result = true;
			}
		} else {
			cv.put( AccountsTable.ACCOUNT_PASSWORD, Account.getIt().getUser().getPassword() );
			if( remember != 0 )
				cv.put( AccountsTable.REMEMBER_PASSWORD, ( ( remember > 0 ) ? "Y" : "N" ) );
			cv.put( AccountsTable.LAST_LOGIN, "Y" );
			int n = writer.update( AccountsTable.TABLE_NAME , cv, selection, args );
			if(n>0){
				Log.i("CACHE DB", "Account updated");
				result = true;
			}
		}
		return result;
	}

	public boolean clearPasswordAccount(){
		boolean result = false;
		ContentValues cv = new ContentValues();
		SQLiteDatabase SQ2 = this.getWritableDatabase();
		String selection = AccountsTable.HOST + " LIKE ? AND "
				+ AccountsTable.ACCOUNT_USERNAME + " LIKE ?";
		String args[] = { Account.HOST, Account.getIt().getUser().getUsername()  };
		cv.put( AccountsTable.ACCOUNT_PASSWORD, "" );
		cv.put( AccountsTable.LAST_LOGIN, "N" );
		int n = SQ2.update( AccountsTable.TABLE_NAME , cv, selection, args );
		if( n > 0 ){
			Log.i("CACHE DB", "Cleared Account");
			result = true;
		}
		return result;
	}
	
	public boolean logOutAccount(){
		boolean result = false;
		ContentValues cv = new ContentValues();
		String selection = AccountsTable.HOST + " LIKE ? AND "
				+ AccountsTable.ACCOUNT_USERNAME + " LIKE ? AND "
				+ AccountsTable.LAST_LOGIN + " LIKE ?";
		String args[] = { Account.HOST, Account.getIt().getUser().getUsername(), "Y" };
		cv.put( AccountsTable.LAST_LOGIN, "N" );
		int n = writer.update( AccountsTable.TABLE_NAME , cv, selection, args );
		if( n > 0 ){
			selection = AccountsTable.HOST + " LIKE ? AND "
					+ AccountsTable.ACCOUNT_USERNAME + " LIKE ? AND "
					+ AccountsTable.REMEMBER_PASSWORD + " LIKE ?";
			String args2[] = { Account.HOST, Account.getIt().getUser().getUsername(), "N" };
			cv.put( AccountsTable.ACCOUNT_PASSWORD, "" );
			int i = writer.update( AccountsTable.TABLE_NAME , cv, selection, args2 );
			i = i + 0;
			Log.i("CACHE DB", "Logout Account");
			result = true;
		}
		return result;
	}

	public Account getAccount(){
		Account a = null;
		String[] columns = { AccountsTable.ACCOUNT_USERNAME, AccountsTable.ACCOUNT_PASSWORD };
		String selection = AccountsTable.HOST + " LIKE ? AND "
				+ AccountsTable.ACCOUNT_PASSWORD + " NOT LIKE ? AND "
						+ AccountsTable.LAST_LOGIN + " LIKE ?";
		String args[] = { Account.HOST, "", "Y" };
		Cursor c = reader.query(AccountsTable.TABLE_NAME, columns, selection, args, null, null, null);
		if( c.getCount() > 1 ){
			writer.delete(AccountsTable.TABLE_NAME, selection, args);
		} else if( c.getCount() == 1 ){
			c.moveToFirst();
			a = new Account();
			a.setData( c.getString(0) , c.getString(1) );
		}
		return a;
	}
	
	public ArrayList<String> getAccountsList(){
		ArrayList<String> accounts = new ArrayList<String>();
		String[] columns = { AccountsTable.ACCOUNT_USERNAME };
		String selection = AccountsTable.HOST + " LIKE ?";
		String args[] = { Account.HOST };
		Cursor c = reader.query(AccountsTable.TABLE_NAME, columns, selection, args, null, null, null);
		if( c != null ){
			if( c.getCount() > 0 ){
				String username;
				c.moveToFirst();
				do{
					username = c.getString(0);
					if( !accounts.contains(username) ) accounts.add(username);
				}while( c.moveToNext() );
			}
		}
		return accounts;
	}
	
	public String getPasswordAccount(String username){
		String password = "";
		String[] columns = { AccountsTable.ACCOUNT_PASSWORD, AccountsTable.REMEMBER_PASSWORD };
		String selection = AccountsTable.HOST + " LIKE ? AND "
				+ AccountsTable.ACCOUNT_USERNAME + " LIKE ?";
		String args[] = { Account.HOST, username };
		Cursor c = reader.query(AccountsTable.TABLE_NAME, columns, selection, args, null, null, null);
		if( c.getCount() > 1 ){
			writer.delete(AccountsTable.TABLE_NAME, selection, args);
		} else if( c.getCount() == 1 ){
			c.moveToFirst();
			String rememberValue = c.getString(1);
			if( rememberValue.equals("Y") )
				password = c.getString(0); 
		}
		return password;
	}

	public Cursor getProfilePictures(){
		String[] columns = { ProfilePicturesTable.ACCOUNT_USERNAME, 
		ProfilePicturesTable.ACCOUNT_PICTURE, ProfilePicturesTable.LAST_UPDATE };
		String selection = ProfilePicturesTable.HOST + " LIKE ?";
		String args[] = { Account.HOST };
		return reader.query(ProfilePicturesTable.TABLE_NAME, columns, selection, args, null, null, null);
	}
	
	public byte[] updatePicture(ProfilePicture pp){
		byte[] result = null;
		ContentValues cv = new ContentValues();
		String[] columns = { ProfilePicturesTable.ACCOUNT_USERNAME, 
		ProfilePicturesTable.ACCOUNT_PICTURE, ProfilePicturesTable.LAST_UPDATE };
		String selection = ProfilePicturesTable.HOST + " LIKE ? AND "
				+ ProfilePicturesTable.ACCOUNT_USERNAME + " LIKE ?";
		String args[] = { Account.HOST, pp.getUsername() };
		Cursor c = reader.query(ProfilePicturesTable.TABLE_NAME, columns, selection, args, null, null, null);
		if( c.getCount() > 1 ){
			writer.delete(ProfilePicturesTable.TABLE_NAME, selection, args);
			c = reader.query(ProfilePicturesTable.TABLE_NAME, columns, selection, args, null, null, null);
		}
		
		Bitmap bitmap = ((BitmapDrawable) pp.getPicture()).getBitmap();
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
		result = stream.toByteArray();
		cv.put( ProfilePicturesTable.LAST_UPDATE, pp.getLastUpdated().getTime() );
		cv.put( ProfilePicturesTable.ACCOUNT_PICTURE, result );
		if( c.getCount() == 0 ){
			cv.put( ProfilePicturesTable.HOST, Account.HOST );
			cv.put( ProfilePicturesTable.ACCOUNT_USERNAME, pp.getUsername() );
			long id = writer.insert(ProfilePicturesTable.TABLE_NAME, null, cv);
			if( id > 0 ){
				Log.i("CACHE DB", "Update a profile picture");
			}
		} else {
			int n = writer.update( ProfilePicturesTable.TABLE_NAME , cv, selection, args );
			if(n>0){
				Log.i("CACHE DB", "Update a profile picture");
			}
		}
		return result;
	}
	
	public void deleteProfilePicture(String username){
		String selection = ProfilePicturesTable.HOST + " LIKE ? AND "
				+ ProfilePicturesTable.ACCOUNT_USERNAME + " LIKE ?";
		String args[] = { Account.HOST, username };
		writer.delete(ProfilePicturesTable.TABLE_NAME, selection, args);
	}
	
	public Drawable getPictureAccount(String username){
		Drawable result = null;
		String[] columns = { ProfilePicturesTable.ACCOUNT_PICTURE };
		String selection = ProfilePicturesTable.HOST + " LIKE ? AND "
				+ ProfilePicturesTable.ACCOUNT_USERNAME + " LIKE ?";
		String args[] = { Account.HOST, username };
		Cursor c = reader.query(ProfilePicturesTable.TABLE_NAME, columns, selection, args, null, null, null);
		if( c.getCount() == 1 ){
			c.moveToFirst();
			byte[] imageBytes = c.getBlob(0);
			Bitmap bitMapImage = BitmapFactory.decodeByteArray( imageBytes, 0,
					imageBytes.length);
			result = new BitmapDrawable(Account.getContext().getResources(),
					bitMapImage);	
		}
		return result;
	}
	
}
