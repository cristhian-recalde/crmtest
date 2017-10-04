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
package com.trilogy.app.crm.home.cmd;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.framework.xhome.home.HomeOperationEnum;
import com.trilogy.framework.xhome.support.Command;

/**
 * @author jchen
 *
 * In Subscriber pipe line, when creating, we need subscriber 
 * to persisted before doing provisioning, so we need to issue
 * this command to ask pipe line to save it first. 
 * Althought we can also can super.create(obj), 
 * but this would cause of pipe line order problem. 
 * 
 */
public class CreateSubscriberCmd implements Command
{
	protected Subscriber subscriber;
	
	public CreateSubscriberCmd(Subscriber subscriber)
	{
		setSubscriber(subscriber);
		setHomeOperation(HomeOperationEnum.CREATE);
	}
	
	public CreateSubscriberCmd(Subscriber subscriber, HomeOperationEnum  op)
	{
		setSubscriber(subscriber);
		setHomeOperation(op);
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
	public void setHomeOperation(HomeOperationEnum op)
	{
		homeOperation_ = op;
	}
	public HomeOperationEnum getHomeOperation()
	{
		return homeOperation_;
	}
	
	HomeOperationEnum  homeOperation_ = HomeOperationEnum.CREATE;
}
