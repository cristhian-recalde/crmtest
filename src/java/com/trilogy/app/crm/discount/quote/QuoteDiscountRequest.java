package com.trilogy.app.crm.discount.quote;

import java.util.List;

public class QuoteDiscountRequest 
{
	public QuoteDiscountRequest() {
		
	}
	
	private List<QuoteAccount> requestedAccounts ;
	private List<QuoteSubscriber> requestedSubscribers;
	
	public List<QuoteAccount> getRequestedAccounts() {
		return requestedAccounts;
	}
	public void setRequestedAccounts(List<QuoteAccount> requestedAccounts) {
		this.requestedAccounts = requestedAccounts;
	}
	public List<QuoteSubscriber> getRequestedSubscribers() {
		return requestedSubscribers;
	}
	public void setRequestedSubscribers(List<QuoteSubscriber> requestedSubscribers) {
		this.requestedSubscribers = requestedSubscribers;
	}
}
