package com.fonax.android.view.activity;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.fonax.android.R;
import com.fonax.android.controller.Account;
import com.fonax.android.model.ActionBar;
import com.fonax.android.model.Contact;
import com.fonax.android.view.adapter.ExtensionsAdapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PhoneContactInfoActivity extends SherlockActivity {
	private boolean showingExpandedImage;
	private int mShortAnimationDuration;
	private ImageView expandedImageView;
	private Animator mCurrentAnimator;
	private ImageView backgroundView;
	private float startScaleFinal;
	private Point globalOffset;
	private Rect startBounds;
	private Rect finalBounds;
	private Contact contact;
	
	private void zoomOutFromThumb(final ImageView expandedImageView, Rect startBounds, float startScaleFinal,
			final ImageView backgroundView) {
		if (mCurrentAnimator != null) {
			mCurrentAnimator.cancel();
		}
		
		this.showingExpandedImage = false;
		AnimatorSet set = new AnimatorSet();
		set.play(ObjectAnimator
				.ofFloat(expandedImageView, "x", startBounds.left))
				.with(ObjectAnimator
						.ofFloat(expandedImageView,
								"y",startBounds.top))
				.with(ObjectAnimator
						.ofFloat(expandedImageView,
								"scaleX", startScaleFinal))
				.with(ObjectAnimator
						.ofFloat(expandedImageView,
								"scaleY", startScaleFinal));
		set.setDuration(mShortAnimationDuration);
		set.setInterpolator(new DecelerateInterpolator());
		set.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				// thumbView.setAlpha(1f);
				backgroundView.setVisibility(View.GONE);
				expandedImageView.setVisibility(View.GONE);
				mCurrentAnimator = null;
			}
			
			@Override
			public void onAnimationCancel(Animator animation) {
				// thumbView.setAlpha(1f);
				backgroundView.setVisibility(View.GONE);
				expandedImageView.setVisibility(View.GONE);
				mCurrentAnimator = null;
			}
		});
		set.start();
		mCurrentAnimator = set;
	}
	
	private void zoomInFromThumb(Drawable image) {
		if (mCurrentAnimator != null) {
			mCurrentAnimator.cancel();
		}
		
		this.showingExpandedImage = true;
		backgroundView = (ImageView) findViewById(
				R.id.backPicture );
		expandedImageView = (ImageView) findViewById(
				R.id.expandedProfilePicture );
		expandedImageView.setImageDrawable(image);
		
		startBounds = new Rect();
		finalBounds = new Rect();
		globalOffset = new Point();
		
		// thumbView.getGlobalVisibleRect(startBounds);
		findViewById(R.id.container_contactInfo)
				.getGlobalVisibleRect(finalBounds, globalOffset);
		startBounds.offset(-globalOffset.x, -globalOffset.y);
		finalBounds.offset(-globalOffset.x, -globalOffset.y);
		
		float startScale;
		if ((float) finalBounds.width() / finalBounds.height()
				> (float) startBounds.width() / startBounds.height()) {
			startScale = (float) startBounds.height() / finalBounds.height();
			float startWidth = startScale * finalBounds.width();
			float deltaWidth = (startWidth - startBounds.width()) / 2;
			startBounds.left -= deltaWidth;
			startBounds.right += deltaWidth;
		} else {
			startScale = (float) startBounds.width() / finalBounds.width();
			float startHeight = startScale * finalBounds.height();
			float deltaHeight = (startHeight - startBounds.height()) / 2;
			startBounds.top -= deltaHeight;
			startBounds.bottom += deltaHeight;
		}
		
		// thumbView.setAlpha(0f);
		expandedImageView.setVisibility(View.VISIBLE);
		backgroundView.setVisibility(View.VISIBLE);
		
		expandedImageView.setPivotX(0f);
		expandedImageView.setPivotY(0f);
		
		AnimatorSet set = new AnimatorSet();
		set
			.play(ObjectAnimator.ofFloat(expandedImageView, "x",
					startBounds.left, finalBounds.left))
			.with(ObjectAnimator.ofFloat(expandedImageView, "y",
					startBounds.top, finalBounds.top))
			.with(ObjectAnimator.ofFloat(expandedImageView, "scaleX",
					startScale, 1f)).with(ObjectAnimator.ofFloat(expandedImageView,
							"scaleY", startScale, 1f));
		set.setDuration(mShortAnimationDuration);
		set.setInterpolator(new DecelerateInterpolator());
		set.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				mCurrentAnimator = null;
			}
			
			@Override
			public void onAnimationCancel(Animator animation) {
				mCurrentAnimator = null;
			}
		});
		set.start();
		mCurrentAnimator = set;
		
		startScaleFinal = startScale;
		expandedImageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				zoomOutFromThumb(expandedImageView, startBounds, startScaleFinal, backgroundView);
			}
		});
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contact_info);
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			new ActionBar(this).setGenericBar( true, getApplicationContext()
					.getString(R.string.title_activity_contact_info) );
			this.contact = Account.getIt().getContactsManager().getContact(extras.getString("user"));
			TextView nameView = (TextView) findViewById(R.id.name);
			TextView emailView = (TextView) findViewById(R.id.email);
			TextView extTitleView = (TextView) findViewById(R.id.extensionTitle);
			ListView extListView = (ListView) findViewById(R.id.list);
			
			this.showingExpandedImage = false;
			final ImageView profilePictureView = (ImageView) findViewById(R.id.profilePicture);
			final Drawable tempPic = this.contact.getProfilePicture();
			if( tempPic != null ){
				profilePictureView.setImageDrawable( tempPic );
				profilePictureView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						zoomInFromThumb(tempPic);
					}
				});
			} else {
				RelativeLayout.LayoutParams params;
				params = (RelativeLayout.LayoutParams) profilePictureView.getLayoutParams();
				params.width = 0;
			}
			mShortAnimationDuration = getResources().getInteger(
					android.R.integer.config_shortAnimTime );
			
			nameView.setText( this.contact.getName() );
			
			if( this.contact.getEmail()!=null ){
				if( !this.contact.getEmail().isEmpty() ){
					emailView.setText( this.contact.getEmail() );
					emailView.setOnClickListener(new View.OnClickListener() {
					    @Override
					    public void onClick(View v) {
					    	Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
					                "mailto",contact.getEmail(), null));
						    startActivity(Intent.createChooser(emailIntent, "Send email..."));
					    }
					});
				} else emailView.setVisibility( View.INVISIBLE );
			} else emailView.setVisibility( View.INVISIBLE );
			
			if( this.contact.getExtensions() == null ){
				extTitleView.setVisibility( View.INVISIBLE );
				extListView.setVisibility( View.INVISIBLE );
			} else if( this.contact.getExtensions().isEmpty() ){
				extTitleView.setVisibility( View.INVISIBLE );
				extListView.setVisibility( View.INVISIBLE );
			} else {
				if( this.contact.getExtensions().size() == 1 ){
					extTitleView.setText( R.string.text_extension );
				}
				ExtensionsAdapter adapter = new ExtensionsAdapter( PhoneContactInfoActivity.this,
						this.contact.getExtensions() );
				extListView.setAdapter( adapter );
			}
			
			extListView.setOnItemClickListener(new OnItemClickListener() {
				@SuppressWarnings("unused")
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					int ext = Integer.parseInt( contact.getExtensions().get(position) );
				}
			});
			
			extListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
				@SuppressWarnings("unused")
				@Override
				public boolean onItemLongClick(AdapterView<?> parent,
						View view, int position, long id) {
					int ext = Integer.parseInt( contact.getExtensions().get(position) );
					return true;
				}
			});
			
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
		    case android.R.id.home:
		    	if( this.showingExpandedImage ) 
					zoomOutFromThumb(expandedImageView, startBounds, startScaleFinal, backgroundView);
				else
					finish();
				break;
	    }
	    return super.onOptionsItemSelected(item);
	}
	
	@Override
    public void onBackPressed(){
		if( this.showingExpandedImage ) 
			zoomOutFromThumb( expandedImageView, startBounds, startScaleFinal, backgroundView );
		else
			super.onBackPressed( );
    }
	
}
