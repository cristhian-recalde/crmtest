/**
 * 
 */
package com.trilogy.app.crm.integration.pc;

import java.util.ArrayList;
import java.util.List;

/**
 * @author abhay.parashar
 * Model Class containing BSS price plan fields
 */
public class PublishOfferPricePlanModel {
	
	private long pricePlanId;
	private String pricePlanName;
	private int serviceProviderID;
	private long pricePlanType;
	private String unifiedOfferId;
	private long subscriptionType;
	// List of all the versions.
	private List<PublishedVersionModel> versionModel;

	/**
	 * 
	 */
	public PublishOfferPricePlanModel() {
		versionModel = new ArrayList<PublishedVersionModel>();
	}

	public long getPricePlanId() {
		return pricePlanId;
	}

	public void setPricePlanId(long pricePlanId) {
		this.pricePlanId = pricePlanId;
	}

	public String getPricePlanName() {
		return pricePlanName;
	}

	public void setPricePlanName(String pricePlanName) {
		this.pricePlanName = pricePlanName;
	}

	public int getServiceProviderID() {
		return serviceProviderID;
	}

	public void setServiceProviderID(int serviceProviderID) {
		this.serviceProviderID = serviceProviderID;
	}

	public long getPricePlanType() {
		return pricePlanType;
	}

	public void setPricePlanType(long subcriptionType) {
		this.pricePlanType = subcriptionType;
	}
	
	public String getUnifiedOfferId() {
		return unifiedOfferId;
	}

	public void setUnifiedOfferId(String unifiedOfferId) {
		this.unifiedOfferId = unifiedOfferId;
	}

	public List<PublishedVersionModel> getVersionModel() {
		return versionModel;
	}

	public void setVersionModel(List<PublishedVersionModel> versionModel) {
		this.versionModel = versionModel;
	}

	public long getSubscriptionType() {
		return subscriptionType;
	}

	public void setSubscriptionType(long subscriptionType) {
		this.subscriptionType = subscriptionType;
	}

	@Override
	public String toString() {
		return "PublishOfferPricePlanModel [pricePlanId=" + pricePlanId
				+ ", pricePlanName=" + pricePlanName + ", serviceProviderID="
				+ serviceProviderID + ", pricePlanType=" + pricePlanType
				/*+ ", unifiedOfferId=" +*/+ ", subscriptionType="
				+ subscriptionType + ", versionModel=" + versionModel + "]";
	}
	
	
}
