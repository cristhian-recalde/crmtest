package com.trilogy.app.crm.discount.quote;


public class QuoteDiscountResponse 
{
	public QuoteDiscountResponse() {
		
	}

	private String discountType;
	private long serviceId;
	private String path;
	private String discountRule;
	private long discountAmount;
	private String subscriberId;
	
	public String getDiscountType() {
		return discountType;
	}
	public void setDiscountType(String discountType) {
		this.discountType = discountType;
	}
	public long getServiceId() {
		return serviceId;
	}
	public void setServiceId(long serviceId) {
		this.serviceId = serviceId;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getDiscountRule() {
		return discountRule;
	}
	public void setDiscountRule(String discountRule) {
		this.discountRule = discountRule;
	}
	public long getDiscountAmount() {
		return discountAmount;
	}
	public void setDiscountAmount(long discountAmount) {
		this.discountAmount = discountAmount;
	}
	public String getSubscriberId() {
		return subscriberId;
	}
	public void setSubscriberId(String subscriberId) {
		this.subscriberId = subscriberId;
	}

	
	
	
}
