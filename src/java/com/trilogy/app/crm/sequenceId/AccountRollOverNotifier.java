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
package com.trilogy.app.crm.sequenceId;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.IdentifierSequence;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xlog.log.EntryLogMsg;

/**
 * @author amedina
 */
public class AccountRollOverNotifier extends ContextAwareSupport implements
		RollOverNotofiable 
{
	public AccountRollOverNotifier(Context ctx, CRMSpid spid)
	{
		setContext(ctx);
		
		spid_ = spid;
	}

	
	/* (non-Javadoc)
	 * @see com.redknee.app.crm.sequenceId.RollOverNotofiable#notify(com.redknee.app.crm.bean.IdentifierSequence)
	 */
	public void notify(IdentifierSequence sequence) 
	{
		new EntryLogMsg(10387, this, "", "", new String[]
														{ String.valueOf(spid_) }, null).log(getContext());
	}
	
	CRMSpid spid_ = null;

}
