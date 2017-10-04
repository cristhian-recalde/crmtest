/*
 * Copyright (c) 1999-2003, REDKNEE. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * REDKNEE. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with REDKNEE.
 *
 * REDKNEE MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE
 * SUITABILITY OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR
 * A PARTICULAR PURPOSE, OR NON-INFRINGEMENT. REDKNEE SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING,
 * MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */
package com.trilogy.app.crm.poller;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.service.poller.nbio.PollerStatusNotifier;

/**
 * This is a simple implementation of the poller status notifier. This will get all
 * the notifications about the mount, also will get info about the place in the file
 * where the polling is happening. This class could potentially update the ui with
 * the polling position. This is given as a parameter in the PollerStatus class.
 * 
 * @author psperneac
 */
public class CRMPollerStatusNotifier extends ContextAwareSupport implements PollerStatusNotifier
{
	private String name;

	/**
    * Constructor 
    * 
	 * @param ctx context that the notifier works in
	 * @param name the name of the poller
	 */
	public CRMPollerStatusNotifier(Context ctx,String name)
	{
		setContext(ctx);
		setName(name);
	}

	/**
    * Sets the name of the poller
	 * @param name
	 */
	public void setName(String name)
	{
		this.name=name;
	}
	
	/**
	 * @return the name of the poller
	 */
	public String getName()
	{
		return this.name;
	}

	/**
	 * @see com.redknee.service.poller.nbio.PollerStatusNotifier#nfsMountUp(java.lang.String)
	 */
	public void nfsMountUp(String pollerName)
	{
		new MinorLogMsg(this,"NFS Mount up",null).log(getContext());
	}

	/**
	 * @see com.redknee.service.poller.nbio.PollerStatusNotifier#nfsMountDown(java.lang.String)
	 */
	public void nfsMountDown(String pollerName)
	{
		new MinorLogMsg(this,"NFS Mount down",null).log(getContext());
	}

	/**
	 * @see com.redknee.service.poller.nbio.PollerStatusNotifier#currentReadPos(java.lang.String, long, int, long)
	 */
	public void currentReadPos(String pollerName, long seqNo, int date, long bytePointer)
	{
		// TODO do something on current read position (update UI)
	}
}
