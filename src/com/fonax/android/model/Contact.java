package com.fonax.android.model;

import java.util.ArrayList;
import java.util.Collections;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.packet.Presence;

import com.fonax.android.controller.Account;

import android.graphics.drawable.Drawable;

public class Contact {
	private String username, password, name, email, typeuser;
	private ArrayList<String> extensions;
	private ArrayList<Integer> levels;
	private Boolean canChatting;
	
	public Contact(){
		this.extensions = new ArrayList<String>();
		this.levels = new ArrayList<Integer>();
		this.canChatting = false;
		this.username = "";
		this.password = "";
		this.typeuser = "";
		this.email = "";
		this.name = "";
	}
	
	public Contact(RosterEntry e){
		this.extensions = new ArrayList<String>();
		this.levels = new ArrayList<Integer>();
		this.canChatting = false;
		this.username = "";
		this.typeuser = "";
		this.password = "";
		this.email = "";
		this.name = "";
		
		if( e != null ){
			String tempUsername = e.getUser();
			if( tempUsername != null ){
				if( tempUsername.length()>0 ){
					this.username = tempUsername;
					this.name = e.getName();
					this.canChatting = true;
					if( this.name == null )
						this.name = this.username;
					else if( this.name.length() == 0 )
						this.name = this.username;
				}
			}
		}
	}
	
	public Contact(String username, String name, String levels, String extensions, String email,
			String typeuser){
		this.extensions = new ArrayList<String>();
		this.levels = new ArrayList<Integer>();
		this.username = username;
		this.typeuser = typeuser;
		this.canChatting = false;
		this.password = "";
		this.email = email;
		this.name = name;
		if( this.name.length() == 0 )
			this.name = this.username;
		
		if( extensions.length() > 0 ){
			int start = 0, end;
			while( start < extensions.length() ){
				end = extensions.indexOf(",", start);
				if(end<0) end = extensions.length();
				this.extensions.add( extensions.substring(start, end) );
				// this.extensions.add( Integer.parseInt(extensions.substring(start, end)) );
				start = end + 1;
			}
			Collections.sort(this.extensions);
		}
		
		if( levels.length() > 0 ){
			int start = 0, end;
			while( start < levels.length() ){
				end = levels.indexOf(",", start);
				if(end<0) end = levels.length();
				this.levels.add( Integer.parseInt(levels.substring(start, end)) );
				start = end + 1;
			}
			Collections.sort(this.levels);
		}
	}
	
	public boolean equals(Contact c){
		if( c != null && this.username != null )
			if( c.getUsername() != null )
				return c.getUsername().equals(this.username);
		return false;
	}
	
	// Getter Methods
	
	public String getUsername(){
		return this.username;
	}

	public String getPassword(){
		return this.password;
	}
	
	public String getName(){
		return this.name;
	}
	
	public String getEmail(){
		return this.email;
	}
	
	public String getTypeUser(){
		return this.typeuser;
	}
	
	public ArrayList<Integer> getLevels(){
		return this.levels;
	}
	
	public ArrayList<String> getExtensions(){
		return this.extensions;
	}
	
	public Drawable getProfilePicture(){
		return Account.getIt().getProfilePicturesManager().getPicture( this.username );
	}
	
	public Boolean canChatting(){
		return this.canChatting;
	}
		
	public Boolean isAvaible(Roster roster){
		if( this.canChatting ){
			if( (roster != null) && (this.username != null) ){
				String username = this.username + "@" + Account.HOST;
				Presence presence = roster.getPresence(username);
				if(presence!=null) return ( presence.getType() != Presence.Type.unavailable );
			}
		}
		return false;
	}
	
	// Setter Methods
	
	public void setPicture(String filePath){
		Account.getIt().getProfilePicturesManager().updatedPicture( this.username, filePath );
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public void setEmail(String email){
		this.email = email;
	}
	
	public void setTypeUser(String typeuser){
		this.typeuser = typeuser;
	}
	
	public void setChatting(Boolean canChatting){
		this.canChatting = canChatting;
	}
	
	public void setExtensions(ArrayList<String> extensions){
		this.extensions = extensions;
	}
	
	public void setLevels(ArrayList<Integer> levels){
		this.levels = levels;
	}
	
	public void removeAllExtensions(){
		this.extensions.clear();
	}
	
	public void removeAllLevels(){
		this.levels.clear();
	}
	
	public void addExtension(String newExt){
		if( !this.extensions.contains(newExt) ){
			this.extensions.add(newExt);
			Collections.sort(this.extensions);
		}
	}
	
	public void addLevel(Integer newLevel){
		if( !this.levels.contains(newLevel) ){
			this.levels.add(newLevel);
			Collections.sort(this.levels);
		}
	}
	
 	public boolean setAuthenticationDetails(String username, String password){
		boolean result = false;
		if( username != null && password != null ){
			if( username.length()>0 && password.length()>0 ){
				this.username = username;
				this.password = password;
				result = true;
			}
		}
		if( !result && this.username!=null && this.password!=null )
			result = true;
		return result;
	}
	
}
