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

import java.util.Date;

import electric.util.holder.intOut;


/**
 * @author msubramanyam
 * Code ported by amit.baid@redknee.com
 */
public interface PricePlanProvisionInterface {
    
    
    public static final int SUCCESSFUL 							= 0;
    public static final int INVALID_MSISDN 						= 1;
    public static final int INVALID_PRICE_PLAN					= 2;
    public static final int STARTDATE_LESS_THAN_CURRENTDATE		= 3;
    public static final int ENDDATE_LESS_THAN_STARTDATE			= 4;
    public static final int INTERNAL_ERROR						= 5;
    public static final int INVALID_PARAMETERS					= 6;
    public static final int FAILED_PROVISIONING					= 7;
    public static final int INVALID_SUBSCRIBER_STATE			= 8; // If Subscriber is deactivated
    
    /**
     * This method sets the Secondary PricePlan, its start and end dates for the 
     * given Subscriber Msisdn
     * @param msisdn String MSISDN of the Subscriber
     * @param pricePlanId int Secondary Price Plan Id
     * @param startDate java.util.Date Secondary Price Plan Start Date
     * @param endDate java.util.Date Secondary Price Plan End Date
     * @param retCode intOut holder object
     * @throws SoapServiceException
     */
    public void provisionSecondaryPricePlan(final String msisdn,final int pricePlanId, final Date startDate, final Date endDate,intOut retCode ) throws SoapServiceException;

}
