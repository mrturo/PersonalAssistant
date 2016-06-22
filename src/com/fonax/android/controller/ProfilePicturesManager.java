package com.fonax.android.controller;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Timestamp;
import java.util.ArrayList;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

import com.fonax.android.model.Contact;
import com.fonax.android.model.ProfilePicture;
import com.fonax.android.sqlite.DatabaseOperations;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;

public class ProfilePicturesManager {
	@SuppressWarnings("static-access")
	private static final String SFTP_HOST = Account.getIt().HOST;
	private static final String SFTP_USER = "root";
	private static final String SFTP_PASS = "fx$24%viv0";
	// private static final String SERVER_PATH = "/root/public/app/images/userProfilePictures/";
	private static final String SERVER_PATH = "/home/node/dev/ap/public/images/userProfilePictures/";
	private static final int SFTP_PORT = 22;
	
	private static final boolean downloadPictures = true;
	
	private AsyncTaskRunner runner;
	private Timestamp resultTime;
	private boolean usingServer;
	private String resultPath;
	private Session session;
	private Channel channel;
	private JSch jsch;
	
	private ArrayList<ProfilePicture> pictures;
	
	private ChannelSftp getChannelSftp() throws JSchException{
		int times = 0;
		ChannelSftp channelSftp = null;
		while( times < 2 ){
			if( this.jsch == null ){
				this.jsch = new JSch();
				this.session = null;
			}
			if( this.session == null ){
				this.session = this.jsch.getSession(SFTP_USER, SFTP_HOST, SFTP_PORT);
				this.session.setPassword(SFTP_PASS);
				java.util.Properties config = new java.util.Properties();
				config.put("StrictHostKeyChecking", "no");
				this.session.setConfig(config);
			}
			if( !this.session.isConnected() ){
				this.session.connect();
				this.channel = null;
			}
			if( this.channel == null ){
				this.channel = this.session.openChannel("sftp");
			}
			if( !this.channel.isConnected() ){
				this.channel.connect();
			}
			if( this.channel.isConnected() ){
				channelSftp = (ChannelSftp) this.channel;
	            try {
	            	channelSftp.getHome();
					channelSftp.cd(SERVER_PATH);
					times = 2;
				} catch (SftpException e) {
					this.channel = null;
					times++;
				}
			}
		}
		return channelSftp;
	}
	
	public ProfilePicturesManager( DatabaseOperations dop ){
		this.pictures = new ArrayList<ProfilePicture>();
		this.jsch = new JSch();
		this.session = null;
		this.channel = null;
		if( dop != null ){
			Cursor c = dop.getProfilePictures();
			if( c.getCount() > 0 ){
				c.moveToFirst();
				do{
					String username = c.getString(0);
					byte[] imageBytes = c.getBlob(1);
					Timestamp lastUpdated = new Timestamp(c.getLong(2));
					
					Bitmap bitMapImage = BitmapFactory.decodeByteArray( imageBytes, 0,
							imageBytes.length);
					Drawable picture = new BitmapDrawable(Account.getContext().getResources(),
							bitMapImage);
					
					ProfilePicture tempPP = new ProfilePicture(username, picture, lastUpdated);
					pictures.add(tempPP);
				}while( c.moveToNext() );
			}
		}

		this.runner = new AsyncTaskRunner();
		if(downloadPictures) this.runner.execute();
	}
	
	private Timestamp getLastUpdatedFromServer( final String username ){
		this.resultTime = null;
		try {
			ChannelSftp channelSftp = getChannelSftp();
			if( channelSftp != null ){
				SftpATTRS fileAttributes = channelSftp.lstat(SERVER_PATH + username + ".png");
				resultTime = new Timestamp( ((long)fileAttributes.getMTime()) * 1000 );
			}
		} catch (Exception e) { }
		return resultTime;
	}
	
	private String getExtension(String sourceFilePath){
		String result = "";
		if( sourceFilePath != null ){
			if( sourceFilePath.length() > 0 ){
				int i = sourceFilePath.lastIndexOf('/');
				String fileName = sourceFilePath.substring(i+1);
				if( fileName != null ){
					if( fileName.length() > 0 ){
						i = fileName.indexOf('.');
						result = fileName.substring(i+1);
						if( result == null ) result = "";
					}
				}
			}
		}
		return result.toLowerCase();
	}
	
	private String uploadToServer(final String username, final String sourceFilePath){
		resultPath = "";
		Thread t = new Thread(new Runnable() {
			public void run() {
				try {
					ChannelSftp channelSftp = getChannelSftp();
					if( channelSftp != null ){
			            FileInputStream fis = null;
						File sourceF = new File(sourceFilePath);
						String ext = getExtension(sourceFilePath); 
						if( ext.equals("png") ){
							fis = new FileInputStream(sourceF);
						} else {
							int i = sourceFilePath.indexOf( "." + ext );
							String tempPath = sourceFilePath.substring(0, i) + ".png";
							File tempFile = new File(tempPath);
							// ImageIO.write( ImageIO.read( sourceF ), "png", tempFile );
							fis = new FileInputStream(tempFile);
							tempFile.delete();
						}
						if( fis != null ){
							resultPath = SERVER_PATH + username + ".png";
							channelSftp.put(fis, resultPath );
						}
					}
				} catch (Exception e) {
					resultPath = "";
				}
				usingServer = false;
			}
		});
		
		usingServer = true;
		t.start();
		while(usingServer){}
		return resultPath;
	}
	
	public void updatedPicture(String username, String sourceFilePath){
		String imgPath = uploadToServer(username, sourceFilePath);
		if( imgPath.length() > 0 ){
			boolean updated = false;
			Timestamp lastUpdate = getLastUpdatedFromServer(username);
			Drawable picture = Drawable.createFromPath(imgPath);
			ProfilePicture pp = new ProfilePicture(username, picture, lastUpdate);
			for(int i=0; i<this.pictures.size() && !updated; i++){
				if( this.pictures.get(i).getUsername().equals(username) ){
					this.pictures.get(i).updatePicture(picture, lastUpdate);
					updated = true;
				}
			}
			if(!updated) this.pictures.add(pp);
			Account.getIt().getCacheDatabase().updatePicture(pp);
		}
	}
	
	private ProfilePicture downloadPictureFromServer(String username){
		ProfilePicture ppResult = null;
		try {
			ChannelSftp channelSftp = getChannelSftp();
			if( channelSftp != null ){
				String filePath = SERVER_PATH + username + ".png";
				SftpATTRS fileAttributes = channelSftp.lstat(filePath);
				Timestamp lastUpdated = new Timestamp( ((long)fileAttributes.getMTime()) * 1000 );
				Drawable picture = Drawable.createFromStream(channelSftp.get(filePath), username + ".png");
				ppResult = new ProfilePicture(username, picture, lastUpdated);
				Account.getIt().getCacheDatabase().updatePicture(ppResult);
			}
		} catch (Exception e) { }
		return ppResult;
	}
	
	private ProfilePicture getProfilePicture(String username){
		boolean find = false;
		ProfilePicture picture = null;
		for(int i=0; i<this.pictures.size() && !find; i++){
			if( this.pictures.get(i).getUsername().equals(username) ){
				picture = this.pictures.get(i);
				find = true;
			}
		}
		return picture;
	}
	
	public Drawable getPicture(String username){
		Drawable picture = null;
		ProfilePicture temp = getProfilePicture(username);
		if( temp != null){
			picture = temp.getPicture();
		}
		return picture;
	}
	
	private class AsyncTaskRunner extends AsyncTask<Void, Void, ArrayList<ProfilePicture>> {
		
		@Override
		protected ArrayList<ProfilePicture> doInBackground(Void... params) {
			ArrayList<ProfilePicture> picturesTempList = null;
			if( Account.getIt() != null ){
				String username = Account.getIt().getUser().getUsername();
				ProfilePicture pp;
				Timestamp t;
				if( username != null ){
					if( !username.isEmpty() ){
						t = getLastUpdatedFromServer( username );
						if( t != null ){
							pp = getProfilePicture( username );
							if( pp == null ){
								pp = downloadPictureFromServer( username );
							} else if( pp.getPicture() == null ){
								pp = downloadPictureFromServer( username );
							} else if( t.after( pp.getLastUpdated() ) ){
								pp = downloadPictureFromServer( username );
							}
							if( pp != null ){
								if( pp.getPicture() != null ){
									if( picturesTempList == null )
										picturesTempList = new ArrayList<ProfilePicture>();
									picturesTempList.add(pp);
								}
							}
						} else {
							 Account.getIt().getCacheDatabase().deleteProfilePicture(username);
						}
					}
				}
				
				ArrayList<Contact> contactsList = Account.getIt().getContactsManager().getContactList();
				if( contactsList == null )
					picturesTempList = null;
				else if( contactsList.isEmpty() )
					picturesTempList = null;
				else {
					for(int i=0; i<contactsList.size(); i++){
						if( contactsList.get(i) != null ){
							username = contactsList.get(i).getUsername();
							if( username != null ){
								if( !username.isEmpty() ){
									t = getLastUpdatedFromServer( username );
									if( t != null ){
										pp = getProfilePicture( username );
										if( pp == null ){
											pp = downloadPictureFromServer( username );
										} else if( pp.getPicture() == null ){
											pp = downloadPictureFromServer( username );
										} else if( t.after( pp.getLastUpdated() ) ){
											pp = downloadPictureFromServer( username );
										}
										if( pp != null ){
											if( pp.getPicture() != null ){
												if( picturesTempList == null )
													picturesTempList = new ArrayList<ProfilePicture>();
												picturesTempList.add(pp);
											}
										}
									} else {
										Account.getIt().getCacheDatabase().deleteProfilePicture(username);
									}
								}
							}
						}
					}
				}
			}
			return picturesTempList;
		}
		
		@Override
		protected void onPostExecute(ArrayList<ProfilePicture> picturesTempList) {
			super.onPostExecute(picturesTempList);
			if( picturesTempList != null ){
				if( !picturesTempList.isEmpty() ){
					pictures.clear();
					pictures = null;
					pictures = picturesTempList;
				}
			}
			new AsyncTaskRunner().execute();
		}
		
	}
	
	
}
