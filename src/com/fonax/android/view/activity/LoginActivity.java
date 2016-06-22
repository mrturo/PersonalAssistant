package com.fonax.android.view.activity;

import java.util.ArrayList;

import com.actionbarsherlock.app.SherlockActivity;
import com.fonax.android.R;
import com.fonax.android.controller.Account;
import com.fonax.android.controller.ScreenManager;
import com.fonax.android.model.ActionBar;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class LoginActivity extends SherlockActivity implements View.OnClickListener {
	boolean settedSavedPassword, settedSavedPicture;
	CheckBox rememberPasswordView;
	AutoCompleteTextView userView;
	EditText passwordView;
	String lastUser;
	
	private void errorAlert(String msg){
		String errorTitle = getString(R.string.alert_error);
		String okButton = getString(R.string.action_ok);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(errorTitle)
			.setMessage(msg)
			.setCancelable(true)
			.setNegativeButton(okButton, new DialogInterface.OnClickListener() {
		 		public void onClick(DialogInterface dialog, int id) {
		 			dialog.cancel();
		 		}
		 	});
		builder.create().show();
	}
	
	private void connect(){
		String user = (String) userView.getText().toString();
		String password = (String) passwordView.getText().toString();
		if( Account.getIt().setData(user, password) ){
			Intent intent = new Intent(getApplicationContext(),
					ConnectingActivity.class);
			intent.putExtra("remember", rememberPasswordView.isChecked());
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			overridePendingTransition(0, 0);
			finish();
		} else {
			this.errorAlert( getString( R.string.alert_login_no_completed_data ) );
		}
	}
	
	private boolean setSavedPassword(String user){
		String password = Account.getIt().getCacheDatabase().getPasswordAccount(user);
		if( !password.isEmpty() ){
			userView.dismissDropDown();
			settedSavedPassword = true;
			passwordView.setText(password);
			rememberPasswordView.setChecked(true);
			return true;
		} else if(settedSavedPassword){
			settedSavedPassword = false;
			passwordView.setText("");
			rememberPasswordView.setChecked(false);
			return true;
		}
		return false;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if ( !NoInternetActivity.isOnline( getApplicationContext() ) ) {
			Intent intent = new Intent(getApplicationContext(), NoInternetActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			overridePendingTransition(0, 0);
			finish();
		} else {
			new ActionBar(this).setGenericBar(false);
			int localLayout = R.layout.activity_phone_login;
			if( ScreenManager.isTablet(getApplicationContext()) )
				localLayout = R.layout.activity_tablet_login;
			setContentView( localLayout );
			Account.getIt().clearData();
			this.rememberPasswordView = (CheckBox) findViewById(R.id.rememberPasswordBox);
			this.passwordView = (EditText) findViewById(R.id.accountPassword);
			this.passwordView.setOnKeyListener(new OnKeyListener() {
			   @Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					 if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
					            (keyCode == KeyEvent.KEYCODE_ENTER)) {
						 connect();
						 return true;
					 }
					return false;
				}
			});
			
			this.lastUser = "";
			this.settedSavedPassword = false;
			this.settedSavedPicture = false;
			this.userView = (AutoCompleteTextView) findViewById(R.id.accountUsername);
			// this.userView.setText( R.string.screen_type );
			
			ArrayList<String> preList = Account.getIt().getCacheDatabase().getAccountsList();
			String[] accountsList = new String[preList.size()];
			accountsList = preList.toArray(accountsList);
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, accountsList);
			this.userView.setAdapter(adapter);
			this.userView.setTextColor(getResources().getColor(R.color.black));
			this.userView.setOnKeyListener(new OnKeyListener() {
				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					String user = (String) userView.getText().toString();
					if( !user.equals(lastUser) ){
						lastUser = user + "";
						setSavedPassword(user);
					}
					return true;
				}
			});
			this.userView.setOnItemClickListener( new OnItemClickListener(){

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					String user = (String) userView.getText().toString();
					if( !user.equals(lastUser) ){
						lastUser = user + "";
						setSavedPassword(user);
					}
				}
				
			});
			
			((Button) findViewById(R.id.ok)).setOnClickListener(this);
			InputMethodManager inputManager = (InputMethodManager) getSystemService(
					Context.INPUT_METHOD_SERVICE);
			inputManager.hideSoftInputFromWindow(findViewById(R.id.ok)
					.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			
			int stausConnection = Account.getIt().getStausConnection();
			if( stausConnection != 0 ){
				if( stausConnection < 0 ){
					String msg = "";
					if( stausConnection == -1 )
						msg = getString(R.string.alert_login_no_server);
					else if( stausConnection == -2 )
						msg = getString(R.string.alert_login_no_account);
					this.errorAlert( msg );
				}
				Account.getIt().resetStausConnection();
			}
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.ok:
				this.connect();
				break;
			default:
				break;
		}
	}
}
