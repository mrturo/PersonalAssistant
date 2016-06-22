package com.fonax.android.model;

import org.jivesoftware.smack.packet.Message;

import com.fonax.android.controller.Account;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class Note {
	private long db_id;
	private String body, from, to;
	private Timestamp timestamp;
	private boolean read, selected, searchResult;
	private boolean received_server;
	private boolean bottom, top;
	private int size;
	
	public Note(Message message) {
		this.db_id = -1;
		this.timestamp = new Timestamp(currentUTC());
		this.selected = false;
		this.searchResult = false;
		this.read = true;
		this.received_server = true;
		
		this.body = Note.clearString( message.getBody() );
		
		this.from = message.getFrom();
		int separator = this.from.lastIndexOf('/');
		if(separator>0) this.from = this.from.substring(0, separator);
		
		this.to = message.getTo();
		separator = this.to.lastIndexOf('/');
		if(separator>0) this.to = this.to.substring(0, separator);
		
		this.bottom = true;
		this.top = true;
		this.size = this.body.length();
	}
	
	public Note(String from, String to, String body, long milliseconds, String read){
		this.timestamp = new Timestamp(milliseconds);
		this.selected = false;
		this.searchResult = false;
		this.read = ( read.equals("R") );
		this.received_server = true;
		
		this.body = Note.clearString( body );
		
		this.from = from;
		int separator = this.from.lastIndexOf('/');
		if(separator>0) this.from = this.from.substring(0, separator);
		
		this.to = to;
		separator = this.to.lastIndexOf('/');
		if(separator>0) this.to = this.to.substring(0, separator);
		
		this.bottom = true;
		this.top = true;
		this.size = this.body.length();
	}
	
	public Note(String to, String body){
		this.timestamp = new Timestamp(currentUTC());
		this.selected = false;
		this.searchResult = false;
		this.read = true;
		this.received_server = false;
		
		this.body = Note.clearString( body );
		
		this.from = Account.getIt().getUser().getUsername();
		
		this.to = to;
		int separator = this.to.lastIndexOf('/');
		if(separator>0) this.to = this.to.substring(0, separator);
		
		this.bottom = true;
		this.top = true;
		this.size = this.body.length();
	}
	
	public Note(jsonMessage jMessage) throws Exception{
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		Date d = formatter.parse(jMessage.utc);
		((SimpleDateFormat) formatter).applyPattern("yyyy-MM-dd HH:mm:ss.SSS");
		this.timestamp = Timestamp.valueOf(formatter.format(d));
		this.received_server = true;
		
		this.searchResult = false;
		this.body = Note.clearString( jMessage.body );
		this.selected = false;
		this.read = false;
		
		String us = jMessage.us;
		int separator = us.lastIndexOf('@');
		if(separator>0) us = us.substring(0, separator);
		
		String wu = jMessage.with_user;
		separator = us.lastIndexOf('@');
		if(separator>0) us = us.substring(0, separator);
		
		if( jMessage.dir == 0 ){
			this.to = us;
			this.from = wu;	
		} else if( jMessage.dir == 1 ){
			this.from = us;
			this.to = wu;	
		}
		
		this.bottom = true;
		this.top = true;
		this.size = this.body.length();
	}
	
	public boolean isBottom(){
		return this.bottom;
	}
	
	public void setBottom(Boolean status){
		this.bottom = status;
	}
	
	public boolean isTop(){
		return this.top;
	}
	
	public void setTop(Boolean status){
		this.top = status;
	}
	
	public int getSize(){
		return this.size;
	}
	
	public void setSize(int newSize){
		this.size = newSize;
	}
	
	private long currentUTC(){
		Calendar c = Calendar.getInstance();
		TimeZone z = c.getTimeZone();
	    int offset = z.getRawOffset();
	    if(z.inDaylightTime(new Date())){
	        offset = offset + z.getDSTSavings();
	    }
	    int offsetHrs = offset / 1000 / 60 / 60;
	    int offsetMins = offset / 1000 / 60 % 60;

	    c.add(Calendar.HOUR_OF_DAY, (-offsetHrs));
	    c.add(Calendar.MINUTE, (-offsetMins));
	    
		return c.getTime().getTime();
	}
	
	public long getDB_ID(){
		return this.db_id;
	}
	
	public void setDB_ID(long db_id){
		this.db_id = db_id;
	}
	
	public String getBody(){
		return this.body;
	}
	
	public Timestamp getTimestamp(){
		long dif = this.timestamp.getTime() - ( currentUTC() - (new Date().getTime()) );
		return new Timestamp(dif);
	}
	
	public Timestamp getUTCTimestamp(){
		return this.timestamp;
	}
	
	public String getFrom(){
		return this.from;
	}
	
	public String getTo(){
		return this.to;
	}
	
	public Boolean isRead(){
		return this.read;
	}
	
	public void read(){
		this.read = true;
	}
	
	public void unread(){
		this.read = false;
	}
	
	public boolean isReceivedByServer(){
		return this.received_server;
	}
	
	public void receivedByServer(){
		received_server = true;
	}
	
	public boolean isSelected(){
		return this.selected;
	}
	
	public void select(){
		this.selected = true;
	}

	public void unselect(){
		this.selected = false;
	}
	
	public boolean getSearchResult(){
		return this.searchResult;
	}
	
	public void setSearchResult(boolean searchResult){
		this.searchResult = searchResult;
	}
	
	public void deleteHost(){
		int separator = this.from.lastIndexOf('@');
		if(separator>0) this.from = this.from.substring(0, separator);
		
		separator = this.to.lastIndexOf('@');
		if(separator>0) this.to = this.to.substring(0, separator);
	}
	
	public void addHost(){
		int separator = this.from.lastIndexOf('@');
		if(separator<0) this.from = this.from + "@" + Account.HOST;
		
		separator = this.to.lastIndexOf('@');
		if(separator<0) this.to = this.to + "@" + Account.HOST;
	}
	
	private static String clearString(String arg){
		String result = "";
		if( arg != null ){
			if( arg.length() > 0 ){
				int start, end;
				for(start = 0; start<arg.length() && arg.charAt(start) == ' '; start++){ }
				for(end = arg.length()-1; end>=0 && arg.charAt(end) == ' '; end--){ }
				end = end + 1;
				if( start>0 || end<arg.length() ){
					if( start <= end )
						result = arg.subSequence(start, end) + "";
				} else result = arg;
			}
		}
		return result;
	}
	
	public static class jsonMessage {
		int id;
		int prev_id;
		int next_id;
		String us;
		String with_user;
		String with_server;
		String with_resource;
		String utc;
		String change_by;
		String change_utc;
		int deleted;
		String subject;
		String thread;
		int crypt;
		String extra;
		int coll_id;
		int dir;
		String body;
		String name;
	}
	
}
