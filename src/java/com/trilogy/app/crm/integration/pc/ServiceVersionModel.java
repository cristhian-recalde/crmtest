package com.trilogy.app.crm.integration.pc;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.trilogy.app.crm.bean.CallingGroupTypeEnum;
import com.trilogy.app.crm.bean.FixedIntervalTypeEnum;
import com.trilogy.app.crm.bean.OneTimeTypeEnum;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.ServiceSubTypeEnum;
import com.trilogy.app.crm.bean.ServiceTypeEnum;

public class ServiceVersionModel {

	private String serviceName;
	
	private String description;
	
	private Calendar effectiveFromDate;
	
	private String technicalServiceTemplateId;
	
	private String ServiceId;
	
	private NetworkTechnology networkTechnology_;
	
	private Map<String, Object> characteristics;

	/*
	 * Pricing is associated with ServiceVersion.
	 */
	private PriceModel price_;

	public ServiceVersionModel() {

	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Calendar getEffectiveFromDate() {
		return effectiveFromDate;
	}

	public void setEffectiveFromDate(Calendar effectiveFromDate) {
		this.effectiveFromDate = effectiveFromDate;
	}

	public String getTechnicalServiceTemplateId() {
		return technicalServiceTemplateId;
	}

	public void setTechnicalServiceTemplateId(String technicalServiceTemplateId) {
		this.technicalServiceTemplateId = technicalServiceTemplateId;
	}

	public String getServiceId() {
		return ServiceId;
	}

	public void setServiceId(String serviceId) {
		ServiceId = serviceId;
	}

	public PriceModel getPrice_() {
		return price_;
	}

	public void setPrice_(PriceModel price_) {
		this.price_ = price_;
	}

	public NetworkTechnology getNetworkTechnology_() {
		return networkTechnology_;
	}

	public void setNetworkTechnology_(NetworkTechnology networkTechnology_) {
		this.networkTechnology_ = networkTechnology_;
	}

	public Map<String, Object> getCharacteristics() {
		return characteristics;
	}

	public void setCharacteristics(Map<String, Object> characteristics) {
		this.characteristics = characteristics;
	}
	
}
