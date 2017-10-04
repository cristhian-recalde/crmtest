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
package com.trilogy.app.crm.dunning;

import java.util.Collections;
import java.util.List;

import com.trilogy.framework.xhome.context.Context;

/**
 * Defining constants for dunning feature.
 * @author gary.anderson@redknee.com
 */
public class DunningConstants
{
    public static final int ALL_SUCCESS_ER = 0;
    public static final int ECP_FAILURE_ER = 3006;
    public static final int SOME_FAILURE_ER = 3011;
    public static final String CONTEXT_KEY_IS_IN_DUNNING = "com.redknee.app.crm.dunning.isInDunning";
    public static final String SUBSCRIPTION_DUNNING_LEVEL_CHANGE = "com.redknee.app.crm.dunning.isSubscriberDunningLevelChange";
    public static final String DUNNING_ACTION_KEY = "dunning action key";
    public static final String DUNNING_SUSPENSION_REASON_UNPAID = "Unpaid";
    public static final String DUNNING_SUSPENSION_REASON_OTHER = "Other";
    public static final int DUNNING_DEFAULT_KEY =0;
    public static final int DUNNING_SPID_KEY =1;
    public static final int DUNNING_BILLCYCLE_KEY =2;
    public static final int DUNNING_CREDITCATEGORY_KEY =3;
    public static final int DUNNINGPOLICY_DEFAULT_KEY =4;
    public static final String DUNNING_IS_OTG_APPLIED = "is_OTG_Applied";
    public static final String DUNNINGAGENT_OBJECT_TOPROCESS = "OBJECT_TOPROCESS";
    @SuppressWarnings("serial")
	public static final DunningPolicy DUMMY_DUNNING_POLICY = new DunningPolicy(){
    	@Override
    	public List<DunningLevel> getAllLevels(final Context ctx)
    	{
    		return Collections.emptyList();
    	}
    	
    	@Override
    	public long getDunningPolicyId()
	   {
	      return 0;
	   }
    };
    
}
