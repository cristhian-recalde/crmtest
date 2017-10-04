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
 * @author jchen
 */
public class SubscriberFaxMsisdnHome 
   extends AbstractSubscriberMsisdnHome
{
	/**
	 * @param delegate
	 */
	public SubscriberFaxMsisdnHome(Home delegate) 
	{
		super(delegate);
	}
	
	
	/**
	 * @see com.redknee.app.crm.home.sub.AbstractSubscriberMsisdnHome#getResourceId(com.redknee.app.crm.bean.Subscriber)
	 */
	protected String getResourceId(Subscriber sub) 
	{
		return sub.getFaxMSISDN();
	}
	
	/**
	 * @see com.redknee.app.crm.home.sub.AbstractSubscriberMsisdnHome#getResourceRef()
	 */
	protected String getResourceRef() 
	{
		return "faxMsisdn";
	}
	
	
	/**
	 * @see com.redknee.app.crm.home.sub.AbstractSubscriberMsisdnHome#isResourceOptional()
	 */
	protected boolean isResourceOptional() 
	{
		return true;
	}


	@Override
	protected boolean isResourceExternal(Subscriber sub)
	{
		return sub.getFaxMsisdnEntryType() == MsisdnEntryTypeEnum.EXTERNAL_INDEX;
	}
}
