/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.provision.soap;

import java.util.ArrayList;
import java.util.Date;

import electric.util.holder.intOut;

/**
 * @author skushwaha
 * Code ported by amit.baid@redknee.com
 */
public interface SubscriberInterface {

	public static final int SUCCESSFUL = 0;

	public static final int INVALID_MSISDN = 1;

	public static final int INVALID_PRICE_PLAN = 2;

	public static final int STARTDATE_LESS_THAN_CURRENTDATE = 3;

	public static final int ENDDATE_LESS_THAN_STARTDATE = 4;
	
	public static final int STARTDATE_OR_ENDDATE_NULL = 5; 

	public static final int INTERNAL_ERROR = 6;

	public static final int INVALID_PARAMETERS = 7;

	public static final int FAILED_PROVISIONING = 8;

	public static final int INVALID_SUBSCRIBER_STATE = 9; // If Subscriber is deactivated

	public static final int LOGIN_FAILED = 10;
	
	public static final int INVALID_SPID = 11;
	
	public static final int INVALID_IMSI = 12;
	
	public static final int INVALID_AUX_SERVICE_ID = 13;
	
	public static final int INVALID_AUX_MSISDN_ASSOCIATION = 14;
	
	public static final int INVALID_PAYMENT_NUMBER = 15;
	
	public ArrayList getSubAuxSvcList(final String userName,
			final String password, long Spid, String msisdn, String Imsi,
			intOut retCode) throws Exception;

	public void provSubAuxSvcList(final String userName, final String password,
			long spid, String msisdn, String imsi, long auxServiceId,
			Date startDate, Date endDate, long numPayments,intOut retCode) throws Exception;

	public void deProvSubAuxSvcList(final String userName, final String password,
			long spid, String msisdn, String imsi, long auxSvcId,intOut retCode)
			throws Exception;

	public ArrayList getPricePlanList(final String userName,
			final String password, long spid, intOut retCode) throws Exception;

	public long getSubPricePlan(final String userName, final String password, long spid, String msisdn,
			String imsi, intOut retCode) throws Exception;

	public void switchSubPricePlan(final String userName, final String password,
			long spid, String msisdn, String imsi, long newPricePlanId,intOut retCode)
			throws Exception;
}
