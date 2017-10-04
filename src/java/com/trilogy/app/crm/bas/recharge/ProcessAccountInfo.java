package com.trilogy.app.crm.bas.recharge;

import java.util.Date;

import com.trilogy.app.crm.bean.SubscriberTypeEnum;

/**
 * 
 * This class is used to hold recurring charge configuration for 
 * Account processing
 * 
 * @author jchen
 *
 */
public class ProcessAccountInfo  {

	public ProcessAccountInfo(Date billingDate, SubscriberTypeEnum applicalSubscriberType)
	{
		billingDate_ = billingDate;
		applicalSubscriberType_ = applicalSubscriberType;
	}
	
	/**
	 * 
	 * @return
	 */
	public SubscriberTypeEnum getApplicableSubscriberType() {
		return applicalSubscriberType_;
	}
	/**
	 * 
	 * @return
	 */
	public Date getBillingDate() {
		return billingDate_;
	}
	
	private Date billingDate_;
	private SubscriberTypeEnum applicalSubscriberType_;
	
}
