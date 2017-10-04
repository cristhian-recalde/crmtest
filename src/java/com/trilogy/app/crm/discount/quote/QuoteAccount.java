package com.trilogy.app.crm.discount.quote;

public class QuoteAccount 
{
	private String ban;
	private int spid;
	private long accountType;
	private long creditCategory;
	private String rootBan;
	private String discountGrade;
	
	public String getBan() {
		return ban;
	}
	public void setBan(String ban) {
		this.ban = ban;
	}
	public int getSpid() {
		return spid;
	}
	public void setSpid(int spid) {
		this.spid = spid;
	}
	public long getAccountType() {
		return accountType;
	}
	public void setAccountType(long accountType) {
		this.accountType = accountType;
	}
	public long getCreditCategory() {
		return creditCategory;
	}
	public void setCreditCategory(long creditCategory) {
		this.creditCategory = creditCategory;
	}
	public String getRootBan() {
		return rootBan;
	}
	public void setRootBan(String rootBan) {
		this.rootBan = rootBan;
	}
	public String getDiscountGrade() {
		return discountGrade;
	}
	public void setDiscountGrade(String discountGrade) {
		this.discountGrade = discountGrade;
	}
	
}
