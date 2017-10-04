/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily 
 * available. Additionally, source code is, by its very nature, confidential 
 * information and inextricably contains trade secrets and other information 
 * proprietary, valuable and sensitive to Redknee, no unauthorised use, 
 * disclosure, manipulation or otherwise is permitted, and may only be used 
 * in accordance with the terms of the licence agreement entered into with 
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved. 
 */
package com.trilogy.app.crm.clean;


/**
 * Constants that are used by the cron agents which might be helpful to expose to the rest of the application.
 * 
 * @author Aaron Gourley
 * @since 7.5
 */
public class CronConstants
{
    public static final String FROM_CRON_AGENT_CTX_KEY = "fromCronAgent";
    public static final String FROM_UNPROV_CRON_AGENT_CTX_KEY = "fromUnProvisionCronAgent";
    
    public static final String BALANCE_THRESHOLD_CTX_KEY = "BALANCE_THRESHOLD";
    public static final String SUBSCRIBER_CLEANUP_TASK = "SUBSCRIBER_CLEANUP_TASK";
}
