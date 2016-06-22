package com.fonax.android.view.fragment;


import com.fonax.android.controller.Account;

public class ConversationsListFragment extends GenericConversationsListFragment {

	public ConversationsListFragment(){
		
	}
	
    public void updateAdapter() {
		String filter = "";
		if( Account.getIt() != null) if( Account.getIt().getConversationsFragment() != null){
			filter = Account.getIt().getConversationsFragment().getSearchFilter();
		}
		super.setItemsList( super.getFilteredList(super.getActivedConversations(), filter) );
		super.updateAdapter();
	}
	
}
