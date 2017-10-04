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
package com.trilogy.app.crm.extension.subscriber;

import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.core.GSMPackage;
import com.trilogy.app.crm.support.HomeSupportHelper;



/**
 * 
 *
 * @author aaron.gourley@redknee.com
 * @since 
 */
public class MultiSimRecordHolder extends AbstractMultiSimRecordHolder
{
    /**
     * {@inheritDoc}
     */
    @Override
    public String getImsi()
    {
        synchronized (IMSI_LOCK)
        {
            if (imsi_ == null || imsi_.isEmpty())
            {
                GSMPackage gsmPackage = null;
                try
                {
                    gsmPackage = HomeSupportHelper.get().findBean(ContextLocator.locate(), GSMPackage.class, this.getPackageID());
                }
                catch (HomeException e)
                {
                    if (LogSupport.isDebugEnabled(ContextLocator.locate()))
                    {
                        new DebugLogMsg(this, "Error retrieving GSM package " + this.getPackageID(), e).log(ContextLocator.locate());
                    }
                }
                if (gsmPackage != null)
                {
                    super.setImsi(gsmPackage.getIMSI());
                }
            }
        }
        return super.getImsi();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setImsi(String imsi) throws IllegalArgumentException
    {
        synchronized (IMSI_LOCK)
        {
            super.setImsi(imsi);
        }
    }

    public MultiSimRecordHolder getNewSimAfterSwap() {
		return newSimAfterSwap;
	}

	public void setNewSimAfterSwap(MultiSimRecordHolder swappedSimRecord) {
		this.newSimAfterSwap = swappedSimRecord;
	}
	
	public boolean isSimSwapped()
	{
		return newSimAfterSwap!=null;
	}

	private transient Object IMSI_LOCK = new Object();
    private MultiSimRecordHolder newSimAfterSwap = null;
}
