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
package com.trilogy.app.crm.web.border;

import com.trilogy.app.crm.bean.ConvergedAccountSubscriber;
import com.trilogy.app.crm.bean.ConvergedAccountSubscriberSearchWebControl;
import com.trilogy.app.crm.web.border.search.AccountSubscriberFirstNameLastNameSearchAgent;
import com.trilogy.app.crm.web.border.search.AccountSubscriberSearchComposite;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.web.search.SearchBorder;

/**
 * @author sthakur
 *
 * 
 */
public class ConvergedAccountSubscriberSearchBorder extends SearchBorder 
{
	public ConvergedAccountSubscriberSearchBorder(final Context ctx, boolean searchSubAccounts)
	{
		super(ctx, ConvergedAccountSubscriber.class, new ConvergedAccountSubscriberSearchWebControl());

		//Contact Info search
		addAgent(new AccountSubscriberFirstNameLastNameSearchAgent(ctx, new AccountSubscriberSearchComposite(searchSubAccounts)));
	}
}
