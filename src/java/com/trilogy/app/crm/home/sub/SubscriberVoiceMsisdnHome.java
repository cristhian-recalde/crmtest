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
package com.trilogy.app.crm.home.sub;

import com.trilogy.app.crm.bean.MsisdnEntryTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.framework.xhome.home.Home;

/**
 * msisdn table in responsible to subscriber entiry events
 * 
 * @author jchen
 */
public class SubscriberVoiceMsisdnHome 
   extends AbstractSubscriberMsisdnHome
{
	/**
	 * @param delegate
	 */
	public SubscriberVoiceMsisdnHome(Home delegate) 
	{
		super(delegate);
	}
	
	
	/**
	 * @see com.redknee.app.crm.home.sub.AbstractSubscriberMsisdnHome#getResourceId(com.redknee.app.crm.bean.Subscriber)
	 */
	protected String getResourceId(Subscriber sub) 
	{
		return sub.getMSISDN();
	}

	/**
	 * @see com.redknee.app.crm.home.sub.AbstractSubscriberMsisdnHome#getResourceRef()
	 */
	protected String getResourceRef() 
	{
		return "voiceMsisdn";
	}
	
	/**
	 * @see com.redknee.app.crm.home.sub.AbstractSubscriberMsisdnHome#isResourceOptional()
	 */
	protected boolean isResourceOptional() 
	{
		return false;
	}


	@Override
	protected boolean isResourceExternal(Subscriber sub)
	{
		return sub.getMsisdnEntryType() == MsisdnEntryTypeEnum.EXTERNAL_INDEX;
	}
}
