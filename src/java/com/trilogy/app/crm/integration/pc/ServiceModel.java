package com.trilogy.app.crm.integration.pc;

import java.util.ArrayList;
import java.util.List;

import com.trilogy.app.crm.bean.ProductTypeEnum;

public class ServiceModel {

	private String serviceId;
	private ProductTypeEnum productType;
	private List<ServiceVersionModel> serviceVersion;
	
	

	public ServiceModel() {
		super();
		serviceVersion = new ArrayList<ServiceVersionModel>();
	}

	public String getServiceId() {
		return serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	public ProductTypeEnum getProductType() {
		return productType;
	}

	public void setProductType(ProductTypeEnum productType) {
		this.productType = productType;
	}

	public List<ServiceVersionModel> getServiceVersion() {
		return serviceVersion;
	}

	@Override
	public String toString() {
		return "ServiceId:" + serviceId + ",productType:" + productType;
	}
	
	
}
