package com.fonax.android.controller;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jivesoftware.smack.RosterEntry;

import android.os.AsyncTask;

import com.fonax.android.model.Contact;

public class ContactsManager {
	private ArrayList<Contact> contacts;
	private Comparator<Contact> comparator;
	
 	public ContactsManager(){
		this.contacts = new ArrayList<Contact>();
		this.comparator = new Comparator<Contact>() {
		    public int compare(Contact c1, Contact c2) {
		    	return (c1.getName().toLowerCase()).compareTo(c2.getName().toLowerCase());
		    }
		};
		new UpdateContactsRunner().execute();
	}
	
 	public void updateList(){
 		ArrayList<Contact> tempContactsList = new ArrayList<Contact>();
		// Get contacts from PBX Database
		if( Account.getIt().getDBConnection()!=null ){
			try {
				String sql = "SELECT * FROM users";
				PreparedStatement statement = Account.getIt().getDBConnection()
						.prepareStatement(sql);
				ResultSet rs = statement.executeQuery();
				while(rs.next()){
					String username = rs.getString("login");
					if(username!=null){
						if(username.length()>0){
							String levels, extensions, email, typeuser;
							String name = rs.getString("name");
							levels = rs.getString("levels");
							extensions = rs.getString("extensions");
							email = rs.getString("email");
							typeuser = rs.getString("typeuser");
							String temp = name;
							if( name == null )
								temp = username;
							else if( name.length() <= 0 )
							temp = username;
						if( username.equals(Account.getIt().getUser().getUsername()) ){
								Account.getIt().getUser().setName(name);
								Account.getIt().getUser().setEmail(email);
								Account.getIt().getUser().setTypeUser(typeuser);
								
								temp = rs.getString("levels");
								ArrayList<Integer> tempList1 = new ArrayList<Integer>(); 
								if( temp!=null ){
									if( !temp.isEmpty() ){
										int start = 0, end;
										while( start < temp.length() ){
											end = temp.indexOf(",", start);
											if(end<0) end = temp.length();
											tempList1.add( Integer.parseInt(temp.substring(start, end)) );
											start = end + 1;
										}
										Collections.sort(tempList1);
										Account.getIt().getUser().setLevels(tempList1);
									}
								}
								temp = rs.getString("extensions");
								ArrayList<String> tempList2 = new ArrayList<String>(); 
								if( temp!=null ){
									if( !temp.isEmpty() ){
										int start = 0, end;
										while( start < temp.length() ){
											end = temp.indexOf(",", start);
											if(end<0) end = temp.length();
											tempList2.add( temp.substring(start, end) );
											start = end + 1;
										}
										Collections.sort(tempList2);
										Account.getIt().getUser().setExtensions(tempList2);
									}
								}
								
							} else {
								Contact newContact = new Contact(username, name, levels, extensions, email, typeuser);
								if(newContact != null) tempContactsList.add( newContact );
							}
						}
					}
				}
				rs.close();
			} catch (SQLException e) { }
		}
				
		// Get contacts from XMPP Server
		if(Account.getIt().getUser().canChatting()){
			List<RosterEntry> entries = new ArrayList<RosterEntry>(
					Account.getIt().getRoster().getEntries());
			if( !entries.isEmpty() ){
				ArrayList<String> tempEntries = new ArrayList<String>();
				for( RosterEntry entry : entries ){
					String user = entry.getUser();
					tempEntries.add(user.substring(0, user.indexOf("@")));
				}
				for(int i=0; i<tempContactsList.size(); i++){
					if( tempEntries.contains( tempContactsList.get(i).getUsername() ) )
						tempContactsList.get(i).setChatting(true);
				}
			}
		}
		
		Collections.sort(tempContactsList, comparator);
		contacts.clear();
		contacts.addAll(tempContactsList);
 	}
 	
 	public ArrayList<Contact> getContactList(){
		return this.contacts;
	}
	
	public Contact getContact(String username){
		Contact result = null;
		if( !this.contacts.isEmpty() ) if( username != null ) if( username.length() > 0 ){
			for(int i=0; i<this.contacts.size(); i++){
				if(this.contacts.get(i).getUsername().equals(username))
					result = this.contacts.get(i);
			}
		}
		return result;
	}
	
	private class UpdateContactsRunner extends AsyncTask<Void, Void, String> {
		
		@Override
		protected String doInBackground(Void... arg0) {
			if( Account.getIt() != null ){
				if( Account.getIt().isLogged() ){
					updateList();
				}
			}
			return "";
		}
		
		@Override
		protected void onPostExecute(String arg) {
			new UpdateContactsRunner().execute();
		}
	}
	
}
