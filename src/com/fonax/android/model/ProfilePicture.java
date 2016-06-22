package com.fonax.android.model;

import java.sql.Timestamp;
import java.util.Date;

import android.graphics.drawable.Drawable;

public class ProfilePicture {
	private String username;
	private Drawable picture;
	private Timestamp lastUpdated;
	
	public ProfilePicture(String username){
		if( username == null )
			this.username = "";
		else
			this.username = username + "";
		this.picture = null;
		this.lastUpdated = new Timestamp(new Date().getTime());
	}
	
	public ProfilePicture(String username, Drawable picture){
		if( username == null )
			this.username = "";
		else
			this.username = username + "";
		this.picture = picture;
		this.lastUpdated = new Timestamp(new Date().getTime());
	}
	
	public ProfilePicture(String username, Drawable picture, Timestamp lastUpdated){
		if( username == null )
			this.username = "";
		else
			this.username = username + "";
		this.picture = picture;
		if(lastUpdated == null)
			this.lastUpdated = new Timestamp(new Date().getTime());
		else
			this.lastUpdated = lastUpdated;
	}
	
	public String getUsername(){
		return this.username;
	}
	
	public Drawable getPicture(){
		return this.picture;
	}
	
	public Timestamp getLastUpdated(){
		return this.lastUpdated;
	}
	
	public void updatePicture(Drawable picture){
		this.picture = picture;
		this.lastUpdated = new Timestamp(new Date().getTime());
	}
	
	public void updatePicture(Drawable picture, Timestamp lastUpdated){
		this.picture = picture;
		if(lastUpdated == null)
			this.lastUpdated = new Timestamp(new Date().getTime());
		else
			this.lastUpdated = lastUpdated;
	}
	
	public void setLastUpdated(Timestamp lastUpdated){
		if(lastUpdated == null)
			this.lastUpdated = new Timestamp(new Date().getTime());
		else
			this.lastUpdated = lastUpdated;
	}
	
	public void setLastUpdated(){
		this.setLastUpdated(null);
	}
	
}
