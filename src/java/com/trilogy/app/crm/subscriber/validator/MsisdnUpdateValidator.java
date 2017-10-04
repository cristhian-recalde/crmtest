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
package com.trilogy.app.crm.subscriber.validator;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;


/**
 * @author skushwaha
 * 
 * If Subscriber State is Pending or Inactive then cannot change Msisdn and Group Msisdn
 */
public class MsisdnUpdateValidator implements Validator
{

    /**
     * Class used to validate that the Subscriber MSISDN and MSISDN Group of deactivated
     * user should not be allowed to changed
     */
    protected static MsisdnUpdateValidator instance__;


    public static MsisdnUpdateValidator instance()
    {
        if (instance__ == null)
        {
            instance__ = new MsisdnUpdateValidator();
        }
        return instance__;
    }


    /** Prevents initialization. Use singleton. */
    private MsisdnUpdateValidator()
    {
    }


    /*
     * (non-Javadoc)
     * 
     * @see com.redknee.framework.xhome.beans.Validator#validate(com.redknee.framework.xhome.context.Context,
     *      java.lang.Object)
     */
    public void validate(Context ctx, Object obj) throws IllegalStateException
    {
        if (obj == null || !ctx.has(Lookup.OLDSUBSCRIBER))
        {
            return;
        }
        Subscriber sub = (Subscriber) obj;
        if (sub.getState() == SubscriberStateEnum.PENDING || sub.getState() == SubscriberStateEnum.INACTIVE)
        {
            Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
            if (isMSISDNChanged(oldSub.getMSISDN(), sub.getMSISDN()))
            {
                throw new IllegalStateException("Cannot Change the MSISDN of Subscriber as state is : "
                        + sub.getState());
            }
            /*if (isGroupMSISDNChanged(oldSub.getMSISDNGroup(), sub.getMSISDNGroup()))
            {
                throw new IllegalStateException("Cannot Change the Group MSISDN of Subscriber as state is : "
                        + sub.getState());
            }*/
        }
    }


    private boolean isMSISDNChanged(String oldMSISDN, String MSISDN)
    {
        if (oldMSISDN.equals(MSISDN))
            return false;
        else
            return true;
    }


    /*private boolean isGroupMSISDNChanged(int oldMSISDNGroup, int MSISDNGroup)
    {
        if (oldMSISDNGroup == MSISDNGroup)
            return false;
        else
            return true;
    }*/
}
