package com.fonax.android.view.fragment;

import com.fonax.android.controller.Account;

public class ContactsListFragment extends GenericConversationsListFragment {
	
	public ContactsListFragment(){
		super.justConversations = false;
	}
	
    public void updateAdapter() {
		String filter = "";
		if( Account.getIt() != null) if( Account.getIt().getContactsFragment() != null){
			filter = Account.getIt().getContactsFragment().getSearchFilter();
		}

		super.setItemsList( super.getFilteredList(super.getContactList(), filter) );
		super.updateAdapter();
	}
	
}