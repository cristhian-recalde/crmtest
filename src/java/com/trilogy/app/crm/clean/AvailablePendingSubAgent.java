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
package com.trilogy.app.crm.clean;

import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;

/**
 * ContextAgent to make available subscriber in Pending state, since its using the same functionality as the 
 * Pending to active state, we are reusig the code with only the state changed
 *  
 * @author amedina
 */
public class AvailablePendingSubAgent extends ActivePendingSubAgent
{

	public AvailablePendingSubAgent()
	{
		state = SubscriberStateEnum.AVAILABLE;
		type = SubscriberTypeEnum.PREPAID;
	}
}
