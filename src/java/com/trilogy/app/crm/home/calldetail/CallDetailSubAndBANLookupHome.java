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
package com.trilogy.app.crm.home.calldetail;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.calldetail.CallDetail;
import com.trilogy.app.crm.bean.calldetail.CallTypeEnum;
import com.trilogy.app.crm.poller.Constants;
import com.trilogy.app.crm.support.CallDetailSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;

/**
 * @author  
 *
 */
public class CallDetailSubAndBANLookupHome extends HomeProxy implements Constants
{
    /**
     * 
     */
    private static final long serialVersionUID = 944629720141220038L;

    /**
     * @param delegate
     */
    public CallDetailSubAndBANLookupHome(final Home delegate)
    {
        super(delegate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object create(final Context ctx, final Object obj) throws HomeException
    {
        final CallDetail cd = (CallDetail) obj;

        final Context subCtx = ctx.createSubContext();

        if (!fillSubAndBAN(subCtx, cd))
        {
            return null;
        }

        return super.create(subCtx, cd);
    }


    /**
     * @param ctx the operating context
     * @param cd Call Detail to fill in
     * @throws SubscriberNotFoundHomeException only if it is not an SMS call detail
     * @return true if values where set
     * @throws HomeException
     * @throws SubscriberNotFoundHomeException
     */
    private boolean fillSubAndBAN(final Context ctx, final CallDetail cd) throws HomeException
    {
    	
    	
        if (cd.getSubscriberID() == null || cd.getSubscriberID().trim().length() == 0 || cd.getBAN() == null
                || cd.getBAN().trim().length() == 0)
        {
            Subscriber sub = null;
            
            if(cd.getSubscriptionType() > 0)
        	{
            	sub = SubscriberSupport.lookupSubscriberForMSISDN(ctx, cd.getChargedMSISDN(), cd.getSubscriptionType(), cd.getTranDate());
        	}
            else
            {
            	sub = SubscriberSupport.lookupSubscriberForMSISDNLimited(ctx, cd.getChargedMSISDN(), cd.getTranDate());
            }
            if (sub == null)
            {
                if (cd.getCallType() == CallTypeEnum.SMS)
                {
                    if (cd.getChargedParty().equals(ER_MO_SMS) && cd.getOrigSvcGrade() == ER_PREPAID)
                    {
                        return false;
                    }
                    else if (cd.getChargedParty().equals(ER_MT_SMS) && cd.getTermSvcGrade() == ER_PREPAID)
                    {
                        return false;
                    }
                }
                throw new SubscriberNotFoundHomeException("Can't find subscriber [" + cd.getChargedMSISDN() + ","
                        + cd.getTranDate() + "]");
            }
            // TODO SubscriberSupport.lookupSubscriberForMSISDNLimited returns a LIMITED
            // subscriber with only
            // TODO 4 fields set. Putting it in the context is asking for trouble. Which
            // happened
            // caching the subscriber
            ctx.put(Subscriber.class, sub);
            cd.setSubscriberID(sub.getId());
            cd.setBAN(sub.getBAN());
        }
        CallDetailSupportHelper.get(ctx).debugMsg(CallDetailSubAndBANLookupHome.class, cd, "Fill in BAN: " + cd.getBAN() + " and subId" , ctx);
        return true;
    }
}
