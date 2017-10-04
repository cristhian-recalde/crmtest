package com.trilogy.app.crm.discount.quote;

import java.util.List;

public class QuoteSubscriber 
{
	private String correlationId;
	private String subscriberId;
	private long subscriptionType;
	private long priceplanId;
	private List<QuoteSubscriberServiceDetail> items;
	private int actionType; //ADD or REMOVE (1/2)
	public String getCorrelationId() {
		return correlationId;
	}
	public void setCorrelationId(String correlationId) {
		this.correlationId = correlationId;
	}
	public String getSubscriberId() {
		return subscriberId;
	}
	public void setSubscriberId(String subscriberId) {
		this.subscriberId = subscriberId;
	}
	public long getSubscriptionType() {
		return subscriptionType;
	}
	public void setSubscriptionType(long subscriptionType) {
		this.subscriptionType = subscriptionType;
	}
	public long getPriceplanId() {
		return priceplanId;
	}
	public void setPriceplanId(long priceplanId) {
		this.priceplanId = priceplanId;
	}
	public List<QuoteSubscriberServiceDetail> getItems() {
		return items;
	}
	public void setItems(List<QuoteSubscriberServiceDetail> items) {
		this.items = items;
	}
	public int getActionType() {
		return actionType;
	}
	public void setActionType(int actionType) {
		this.actionType = actionType;
	}


}
