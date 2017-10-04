/*
 * Created on Feb 25, 2005
 *
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home.cmd;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.framework.xhome.support.Command;

public class ValidateMsisdnCmd implements Command
{
	Subscriber subscriber;
	
	public ValidateMsisdnCmd(Subscriber sub)
	{
		setSubscriber(sub);
	}

	/**
	 * @return Returns the subscriber.
	 */
	public Subscriber getSubscriber()
	{
		return subscriber;
	}
	

	/**
	 * @param subscriber The subscriber to set.
	 */
	public void setSubscriber(Subscriber subscriber)
	{
		this.subscriber = subscriber;
	}
	
	
	
}
