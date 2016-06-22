package com.fonax.android.model;

import java.util.ArrayList;
import java.util.Random;

import com.fonax.android.controller.Account;

public class Group {
	private static final boolean multipleAdmins = false;
	private ArrayList<String> members;
	private ArrayList<String> admins;
	private String name;
	
	public Group(String name, boolean addMainUser){
		this.name = name + "";
		this.admins = new ArrayList<String>();
		this.members = new ArrayList<String>();
		if( addMainUser ) this.addMember( Account.getIt().getUser().getUsername() );
	}
	
	public Group(String name, ArrayList<String> members, boolean addMainUser){
		String mainUser = Account.getIt().getUser().getUsername();
		this.name = name + "";
		this.admins = new ArrayList<String>();
		this.members = new ArrayList<String>();
		if( members.contains(mainUser) || addMainUser ) this.addMember( mainUser );
		else{
			int randomI = new Random().nextInt( members.size() );
			this.addMember( members.get( randomI ) );
		}
		this.addMembers( members );
	}
	
	public Group(String name, String admin, ArrayList<String> members){
		String mainUser = Account.getIt().getUser().getUsername();
		this.name = name + "";
		this.admins = new ArrayList<String>();
		this.members = new ArrayList<String>();
		if( members.contains(admin) ) this.addMember(admin);
		else if( members.contains(mainUser) ) this.addMember( mainUser );
		else{
			int randomI = new Random().nextInt( members.size() );
			this.addMember( members.get( randomI ) );
		}
		this.addMembers( members );
	}

	public Group(String name, ArrayList<String> admins, ArrayList<String> members,
			boolean addMainUser){
		String mainUser = Account.getIt().getUser().getUsername();
		this.name = name + "";
		this.admins = new ArrayList<String>();
		this.members = new ArrayList<String>();
		for(int i=0; i<admins.size(); i++){
			if( members.contains(admins.get(0)) ){
				this.addMember(admins.get(0));
				this.addAdmin(admins.get(0));
			}
		}
		if( this.admins.isEmpty() ){
			if( members.contains(mainUser) || addMainUser ) this.addMember( mainUser );
			else{
				int randomI = new Random().nextInt( members.size() );
				this.addMember( members.get( randomI ) );
			}
		}
		this.addMembers( members );
		if( addMainUser && !this.members.contains(mainUser) ) this.addMember( mainUser );
	}
	
	public String getName(){
		return this.name;
	}
	
	public void setName(String newName){
		if( newName == null ) this.name = "";
		else this.name = newName + "";
	}
	
	public boolean isEmpty(){
		return this.members.isEmpty();
	}
	
	public int size(){
		return this.members.size();
	}

	public void clear(){
		this.members.clear();
		this.admins.clear();
	}
	
	public void addMember(String username){
		if( username != null ) if( !username.isEmpty() ){
			if( this.admins.isEmpty() )
				this.admins.add( username );
			if( !members.contains( username ) )
				this.members.add( username );
		}
	}
	
	public void addMembers(ArrayList<String> newMembers){
		if( newMembers != null ) if( !newMembers.isEmpty() ){
			for(int i=0; i<newMembers.size(); i++){
				this.addMember( newMembers.get(i) );
			}
		}
	}
	
	public void addMember(Contact c){
		if( c != null )
			this.addMember( c.getUsername() );
	}
	
	public void addNewMembers(ArrayList<Contact> newMembers){
		if( newMembers != null ) if( !newMembers.isEmpty() ){
			for(int i=0; i<newMembers.size(); i++){
				this.addMember( newMembers.get(i) );
			}
		}
	}
	
	public void removeMember(String username){
		if( username != null ) if( !username.isEmpty() ){
			this.members.remove( username );
			this.removeAdmin( username );
		}
	}
	
	public void removeMember(Contact c){
		if( c != null )
			this.removeMember( c.getUsername() );
	}
	
	public void leave(){
		this.removeMember( Account.getIt().getUser().getUsername() );
	}
	
	public ArrayList<String> getMemberUsernames(boolean mainUser){
		ArrayList<String> result = new ArrayList<String>();
		if( mainUser ) result.addAll( this.members );
		else{
			for(int i=0; i<this.members.size(); i++){
				if( !this.members.get(i).equals(
						Account.getIt().getUser().getUsername() ) ){
					result.add( this.members.get(i) );
				}
			}
		}
		return result;
	}
	
	public ArrayList<String> getMemberUsernames(){
		return getMemberUsernames(true);
	}

	public ArrayList<Contact> getMembers(boolean mainUser){
		String mainUsername = Account.getIt().getUser().getUsername();
		ArrayList<Contact> result = new ArrayList<Contact>();
		for(int i=0; i<this.members.size(); i++){
			String username = this.members.get(i);
			if( mainUser || !username.equals( mainUsername ) )
				result.add( Account.getIt().getContactsManager().getContact(username) );
		}
		return result;
	}
	
	public ArrayList<Contact> getMembers(){
		return getMembers(true);
	}
	
	public boolean isJoined(String username){
		return this.members.contains(username);
	}

	public boolean isJoined(Contact c){
		if( c != null )
			return this.members.contains(c.getUsername());
		return false;
	}

	public boolean isJoined(){
		return this.members.contains( Account.getIt().getUser().getUsername() );
	}
	
	public void addAdmin(String username){
		if( username != null && ( Group.multipleAdmins || this.members.isEmpty() ) ){
			if( this.members.contains( username ) && !this.admins.contains( username ) )
				this.admins.add( username );
		}
	}
	
	public void addAdmin(Contact c){
		if( c != null )
			this.addAdmin( c.getUsername() );
	}
	
	public void removeAdmin(String username){
		if(username!=null) if(!username.isEmpty() && this.admins.contains(username)){
			if( this.members.isEmpty() ) this.admins.clear();
			else if( !this.members.contains(username) ) this.admins.remove(username);
			else if( this.members.size()>1 ) this.admins.remove(username);
			if( !this.members.isEmpty() && this.admins.isEmpty() ){
				if( this.members.size() == 1 )
					this.admins.add( this.members.get(0) );
				else while( this.admins.isEmpty() ){
					int randomI = new Random().nextInt( this.members.size() );
					String newAdmin = this.members.get( randomI );
					if( !newAdmin.equals(username) ) this.admins.add(newAdmin);
				}
			}
		}
	}
	
	public void removeAdmin(Contact c){
		if( c != null )
			this.removeAdmin( c.getUsername() );
	}

	public ArrayList<String> getAdminsUsernames(){
		ArrayList<String> result = new ArrayList<String>();
		result.addAll( this.admins );
		return result;
	}

	public ArrayList<Contact> getAdmins(){
		ArrayList<Contact> result = new ArrayList<Contact>();
		for(int i=0; i<this.admins.size(); i++){
			String username = this.admins.get(i);
			result.add( Account.getIt().getContactsManager().getContact(username) );
		}
		return result;
	}

	public boolean isAdmin(String username){
		return this.admins.contains( username );
	}

	public boolean isAdmin(Contact c){
		if( c != null )
			return this.admins.contains(c.getUsername());
		return false;
	}

	public boolean isAdmin(){
		return this.admins.contains( Account.getIt().getUser().getUsername() );
	}
	
}
