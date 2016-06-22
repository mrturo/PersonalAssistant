package com.fonax.android.controller;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;

import com.fonax.android.model.Contact;
import com.fonax.android.model.Note;
import com.fonax.android.sqlite.DatabaseOperations;
import com.fonax.android.view.fragment.ContactsListFragment;
import com.fonax.android.view.fragment.ConversationsListFragment;

import android.content.Context;
import android.util.Log;

public class Account implements Serializable {
	
	// JDBC driver name and database URL
	private static final String RDBMS = "mysql";
	private static final String DB_NAME_PBX = "fpbx";
	
	//  Database credentials
	private static final String DB_USER = "root";
	private static final String DB_PASS = "fpbx$1045%fx";
	
	public static final String HOST = "158.85.57.241";
	public static final String SERVICE = "158.85.57.241";
	public static final String RESOURCE = "AP-AndroidPhone";
	public static final int PORT = 5222;
	
	private static final long serialVersionUID = 1L;
	
	private transient ConnectionConfiguration connConfig;
	private ConversationsListFragment conversationsFrag;
	private	transient XMPPConnection connection;
	private NotificationsManager notifications;
	private ConversationManager conversations;
	private ContactsListFragment contactsFrag;
	private ChatManagerListener chatListener;
	private ProfilePicturesManager pictures;
	private DatabaseOperations cacheDB;
	private ContactsManager contacts;
	private ChatManager chatmanager;
	private boolean authenticated;
	public static boolean running;
	private GroupsManager groups;
	private Contact thisUser;
	private Connection conn;
	private Statement stmt;
	private int connecting;
	
	public Account(){
		
		this.cacheDB = new DatabaseOperations( Account.context );
		this.notifications = new NotificationsManager();
		this.pictures = new ProfilePicturesManager( this.cacheDB );
		this.conversations = new ConversationManager();
		this.contacts = new ContactsManager();
		this.thisUser = new Contact();
		
		this.authenticated = false;
		Account.running = false;
		this.connecting = 0;
		this.conn = null;
		this.stmt = null;
		
		this.conversationsFrag = new ConversationsListFragment();
		this.contactsFrag = new ContactsListFragment();
		
		this.connConfig = new ConnectionConfiguration( Account.HOST,
				Account.PORT, Account.SERVICE );
		this.connection = new XMPPConnection( this.connConfig );
		
		this.chatmanager = this.connection.getChatManager();
		this.chatListener = new ChatManagerListener(){
			@Override
			public void chatCreated(final Chat chat, final boolean createdLocally) {
				chat.addMessageListener(new MessageListener(){
					public void processMessage(Chat chat, Message message){
						conversations.add(message);
					}
				});
			}
		};
	}
	
	protected void finalize(){
		this.notifications = null;
		this.disconnectDBServer();
		this.logOut();
	}
	
	// Account Getters
	
	public DatabaseOperations getCacheDatabase(){
		return this.cacheDB;
	}
	
	public Contact getUser(){
		return this.thisUser;
	}
	
	public boolean isSettedData(){
		boolean result = false;
		if( this.thisUser.getUsername().length()>0 && this.thisUser.getPassword().length()>0 )
			result = true;
		return result;
	}
	
	public boolean isServerConnected(){
		return this.connection.isConnected() && this.conn!=null;
	}
	
	public boolean isLogged(){
		boolean result = false;
		if( this.isServerConnected() )
			result = this.authenticated;
		return result;
	}
	
	public ConversationManager getConversationManager(){
		return this.conversations;
	}
	
	public ContactsManager getContactsManager(){
		return this.contacts;
	}
	
	public NotificationsManager getNotifications(){
		return this.notifications;
	}
	
	public Roster getRoster(){
		return this.connection.getRoster();
	}
		
	public ChatManager getChatManager(){
		return this.connection.getChatManager();
	}
	
	public ProfilePicturesManager getProfilePicturesManager(){
		return this.pictures;
	}
	
 	public int getStausConnection(){
 		return this.connecting;
 	}
	
 	public Connection getDBConnection(){
 		return this.conn;
 	}
 	
 	public ConversationsListFragment getConversationsFragment(){
 		return this.conversationsFrag;
 	}
 	
 	public ContactsListFragment getContactsFragment(){
 		return this.contactsFrag;
 	}
 	
	// ---
	
	public boolean sendMessage(Note n, Chat chat){
		boolean result = false;
		if(n != null && chat != null){
			if( n.getBody().length()>0 ){
				Message newMessage = new Message();
				newMessage.setBody(n.getBody());
				try {
					chat.sendMessage(newMessage);
					result = true;
					String contact = n.getTo();
					int separator = contact.lastIndexOf('@');
					if(separator<0) contact = contact + "@" + Account.HOST;
					if( contact.equals(newMessage.getFrom()) || contact.equals(newMessage.getTo()) )
						this.conversations.add(newMessage);
				} catch (XMPPException e) { }
			}
		}
		return result;
	}
	
	public boolean setData(String username, String password){
		boolean result = false;
		if( !this.isLogged() ){
			result = this.thisUser.setAuthenticationDetails(username, password);
		}
		return result;
	}
	
	public void clearData(){
		this.thisUser = new Contact();
		this.contacts = new ContactsManager();
		this.conversations = new ConversationManager();
		this.contactsFrag = new ContactsListFragment();
		this.conversationsFrag = new ConversationsListFragment();
	}
	
	private void disconnectDBServer(){
		if( this.conn != null ){
			try {
				this.conn.close();
			} catch (SQLException e) { }
			this.conn = null;
		}
		if( this.stmt != null ){
			try {
				this.stmt.close();
			} catch (SQLException e) { }
			this.stmt = null;
		}
	}
	
	private boolean connectToDBServer(){
		boolean result = false;
		if( this.conn != null && this.stmt != null ) result = true;
		else {
			this.conn = null;
			this.stmt = null;
			try {
				Class.forName("com." + Account.RDBMS + ".jdbc.Driver");
				String url = "jdbc:" + Account.RDBMS + "://" + Account.HOST + "/" + Account.DB_NAME_PBX;
				this.conn = DriverManager.getConnection(url, Account.DB_USER, Account.DB_PASS);
				this.stmt = conn.createStatement();
				result = true;
			} catch (ClassNotFoundException e) {
				Log.e("xmppServer", "Failed to check database " + Account.HOST);
			} catch (SQLException e) {
				Log.e("xmppServer", "Failed to check database " + Account.HOST);
			}
			if(!result) this.disconnectDBServer();
		}
		return result;
	}
	
 	public void logIn(){
 		this.connecting = 1;
 		if( !this.connection.isAuthenticated() && this.isSettedData() ){
			Thread t = new Thread(new Runnable() {
				public void run() {
					
					// Check in PBX Database
					if( connectToDBServer() ){
						try {
							
							// Make query
							String sql = "SELECT * FROM users WHERE"
									   + " login = ? AND  password = ?";
							PreparedStatement statement = conn.prepareStatement(sql);
							statement.setString( 1, thisUser.getUsername() );
							statement.setString( 2, thisUser.getPassword() );
							
							// Check user table
							int i = 0;
							ResultSet rs = statement.executeQuery();
							while(rs.next()){
								String temp;
								thisUser.setTypeUser( rs.getString("typeuser") );
								thisUser.setEmail( rs.getString("email") );
								thisUser.setName( rs.getString("name") );
								thisUser.removeAllExtensions();
								thisUser.removeAllLevels();
								
								temp = rs.getString("levels");
								if( temp!=null ){
									if( !temp.isEmpty() ){
										int start = 0, end;
										while( start < temp.length() ){
											end = temp.indexOf(",", start);
											if(end<0) end = temp.length();
											thisUser.addLevel( Integer.parseInt(temp.substring(start, end)) );
											start = end + 1;
										}
									}
								}
								
								temp = rs.getString("extensions");
								if( temp!=null ){
									if( !temp.isEmpty() ){
										int start = 0, end;
										while( start < temp.length() ){
											end = temp.indexOf(",", start);
											if(end<0) end = temp.length();
											thisUser.addExtension( temp.substring(start, end) );
											start = end + 1;
										}
									}
								}
								
								i++;
							} // Username or password rigth
							if(i!=1){ // Username or password wrong
								Log.e("xmppServer", "Failed to log in as " + thisUser.getUsername() );
								connecting = -2;
								if(i>1){
									thisUser = new Contact();
								}
							} else{
								authenticated = true;
							}
							rs.close();
							
						} catch (SQLException e) {
							Log.e("xmppServer", "Failed to log in as " + thisUser.getUsername() );
							connecting = -2;
						}
					} else connecting = -1;
					
					// Check and Connect to XMPP SERVER
					if(connecting > 0){
						try {
							connection.connect();
							Log.i("xmppServer", "Connected to " + connection.getHost());
						} catch ( XMPPException e ) {
							Log.e("xmppServer", "Failed to connect to " + Account.HOST);
							Log.e("xmppServer", e.toString());
						}
						if( connection.isConnected() ){
							try {
								connection.login(thisUser.getUsername(), thisUser.getPassword(), Account.RESOURCE);
								Log.i("xmppServer", "Logged in as " + connection.getUser());
								thisUser.setChatting(true);
								connection.sendPacket(new Presence(Presence.Type.available));
								chatmanager.addChatListener(chatListener);
							} catch (XMPPException e) { }
						}
					}
					
					if(authenticated){
						contacts.updateList();
						
						conversationsFrag.updateAdapter();
						conversationsFrag.setListView(false);
						
						contactsFrag.updateAdapter();
						contactsFrag.setListView(false);
						
						groups = new GroupsManager(connection);
						groups.toString();
						
						connecting = 2;
					}
				}
			});
			t.start();
		} else this.connecting = 0;
	}
	
 	public void resetStausConnection(){
 		this.connecting=0;
 	}
 	
	public boolean logOut() {
		boolean result = true;
		if( this.connection != null ){
			if( this.connection.isConnected() ){
				try {
					this.connection.sendPacket( new Presence(Presence.Type.unavailable) );
					this.connection.disconnect();
					this.authenticated = false;
					this.connecting = 0;
					this.clearData();
				} catch(Exception e){
					result = false;
				}
			}
		}
		return result;
	}
	
	// Static Account
	
	private static Account account;
	private static Context context;
	
	static{
		Account.account = null;
		Account.context = null;
	}
	
	public static void getFromCache(Context ctx) {
		String username = "";
		String password = "";
		Account.context = ctx;
		DatabaseOperations dop = new DatabaseOperations(ctx);
		Account a = dop.getAccount();
		if( a != null ) {
			username = a.getUser().getUsername();
			password = a.getUser().getPassword();
		}
		Account.newOne(ctx);
		Account.account.setData(username, password);
	}
	
	public static Account getIt() {
		return Account.account;
	}
	
	public static Context getContext() {
		return Account.context;
	}
	
	public static void clear(){
		if( Account.account != null ){
			Account.account.logOut();
			Account.account = null;
		}
	}
	
	public static void newOne(Context ctx) {
		Account.clear();
		Account.context = ctx;
		Account.account = new Account();
	}
	
}