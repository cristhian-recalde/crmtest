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
package com.trilogy.app.crm.blackberry.error;

/**
 * Interface itemizing error codes that require 
 * special handling in CRM.
 * 
 * @author angie.li
 *
 */
public interface RIMBlackBerryErrorCodes 
{
    /**
     * Denotes the specified service mapping existed on the RIM Provisioning System as active.
     */
    public static final int SERVICE_ALREADY_ACTIVE = 21020;
    
    /**
     * Denotes the specified service mapping was not active on the RIM Provisioning System.
     */
    public static final int SERVICE_DEACTIVATED = 21040;
    
    /**
     * Denotes the specified old billing identifier was not found on the RIM Provisioning System.
     */
    public static final int OLD_BILLING_NOT_FOUND = 61040;
    
    /**
     * Denotes the specified new billing identifier is deactivated on the RIM Provisioning System.
     */
    public static final int  NEW_BILLING_DEACTIVATED = 61180;
    
    /**
     * Denotes the specified new billing identifier is suspended on the RIM Provisioning System.
     */
    public static final int  NEW_BILLING_SUSPENDED = 61200;
    
    /**
     * Denotes the specified IMSI is invalid on the RIM Provisioning System.
     */
    public static final int  INVALID_IMSI = 61010;
    
    /**
     * Denotes the specified IMSI or MSISDN format or lenght is invalid on the RIM Provisioning System.
     */
    public static final int  INVALID_FORMAT_OR_LENGTH = 61120;
    

}
