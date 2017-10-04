package com.trilogy.app.crm.integration.pc;

public class PricePlanModel {

	private int pricePlanId;
	private String pricePlanName;
	private int serviceProviderID;
	private int subcriptionType;
	
	private VersionModel _versionModel;

	public int getPricePlanId() {
		return pricePlanId;
	}

	public void setPricePlanId(int pricePlanId) {
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

	public int getSubcriptionType() {
		return subcriptionType;
	}

	public void setSubcriptionType(int subcriptionType) {
		this.subcriptionType = subcriptionType;
	}

	public VersionModel get_versionModel() {
		return _versionModel;
	}

	public void set_versionModel(VersionModel _versionModel) {
		this._versionModel = _versionModel;
	}

	@Override
	public String toString() {
		return "PricePlanId:" + pricePlanId + ",PricePlanName:" + pricePlanName
				+ ",ServiceProviderID:" + serviceProviderID
				+ ",SubcriptionType:" + subcriptionType + _versionModel;
	}
}
