package com.trilogy.app.crm.integration.pc;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class VersionModel {

	
	private long versionId;
	private Calendar activationDate;
	private String description;

	private List<ServiceModel> serviceList;
	
	public VersionModel() {
		serviceList = new ArrayList<ServiceModel>();
	}

	public long getVersionId() {
		return versionId;
	}

	public void setVersionId(long long1) {
		this.versionId = long1;
	}

	public Calendar getActivationDate() {
		return activationDate;
	}

	public void setActivationDate(Calendar calendar) {
		this.activationDate = calendar;
	}

	public List<ServiceModel> getServiceList() {
		return serviceList;
	}

	public void setServiceList(List<ServiceModel> serviceList) {
		this.serviceList = serviceList;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return "VersionId:" + versionId + ",ActivationDate:" + activationDate
				+ ",Description:" + description + "\t" + serviceList;
	}

}
