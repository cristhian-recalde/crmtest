package com.trilogy.app.crm.integration.pc;

public class PriceModel {
	
    public enum PriceType {
    	ONE_TIME, RECCURING
	}
    
	private String priceTemplateBusinesskey;
	private String priceName;
	private Float amount;
	private String taxIncluded;
	private PriceType priceType;
	private long discountAmount;
	private String discountIsRelative;
	private String name;
	private String frequency;
	private String currency;
	private boolean isProRated;
	private boolean isAdvanceApplicable;
	private String businessKey; // Need to map service with Price

	public String getPriceTemplateBusinesskey()
	{
		return priceTemplateBusinesskey;
	}

	public void setPriceTemplateBusinesskey(String priceTemplateBusinesskey) {
		this.priceTemplateBusinesskey = priceTemplateBusinesskey;
	}

	public String getPriceName() {
		return priceName;
	}

	public void setPriceName(String priceName) {
		this.priceName = priceName;
	}

	public Float getAmount() {
		return amount;
	}

	public void setAmount(Float float1) {
		this.amount = float1;
	}

	public String getTaxIncluded() {
		return taxIncluded;
	}

	public void setTaxIncluded(String taxIncluded) {
		this.taxIncluded = taxIncluded;
	}

	public PriceType getPriceType() {
		return priceType;
	}

	public void setPriceType(PriceType priceType) {
		this.priceType = priceType;
	}

	public long getDiscountAmount() {
		return discountAmount;
	}

	public void setDiscountAmount(long discountAmount) {
		this.discountAmount = discountAmount;
	}

	public String getDiscountIsRelative() {
		return discountIsRelative;
	}

	public void setDiscountIsRelative(String discountIsRelative) {
		this.discountIsRelative = discountIsRelative;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFrequency() {
		return frequency;
	}

	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public boolean isProRated() {
		return isProRated;
	}

	public void setProRated(boolean isProRated) {
		this.isProRated = isProRated;
	}

	public boolean isAdvanceApplicable() {
		return isAdvanceApplicable;
	}

	public void setAdvanceApplicable(boolean isAdvanceApplicable) {
		this.isAdvanceApplicable = isAdvanceApplicable;
	}

	@Override
	public String toString() {
		return "PriceModel [priceTemplateBusinesskey="
				+ priceTemplateBusinesskey + ", priceName=" + priceName
				+ ", amount=" + amount + ", taxIncluded=" + taxIncluded
				+ ", priceType=" + priceType + ", discountAmount="
				+ discountAmount + ", discountIsRelative=" + discountIsRelative
				+ ", name=" + name + ", frequency=" + frequency + ", currency="
				+ currency + ", isProRated=" + isProRated
				+ ", isAdvanceApplicable=" + isAdvanceApplicable
				+ ", businessKey=" + businessKey + "]";
	}
	
	
	
}

