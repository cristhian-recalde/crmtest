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
package com.trilogy.app.crm.bean.payment;

import java.util.HashSet;
import java.util.Set;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.numbermgn.MsisdnMgmtHistory;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.LogSupport;

public class PostpaidSubscribersVisitor implements Visitor
{
    
    public PostpaidSubscribersVisitor(String ban)
    {
        this.postpaidSubscribers_ = new HashSet<Subscriber>();
        if ( ban == null || ban.length() > 0)
        {
            this.ban = ban; 
        }
    }
    
    public void visit(final Context context, final Object obj)
            throws AgentException
    {
        final MsisdnMgmtHistory number = (MsisdnMgmtHistory) obj;
        try
        {
            final Subscriber sub = SubscriberSupport.lookupSubscriberForSubId(context, number.getSubscriberId());

            if (sub != null && sub.getSubscriberType().equals(SubscriberTypeEnum.POSTPAID))
            {

                boolean isUnderAccount = isSubscriberUnderAccount(context, sub, ban);

                if (isUnderAccount)
                {
                    postpaidSubscribers_.add(sub);
                }

            }

        }
        catch (HomeException e)
        {
            LogSupport
                    .minor(context, this, "HomeException when trying to get the subscriber by ID" + e.getMessage(), e);
        }
    }


    public boolean isSubscriberUnderAccount(Context ctx, final Subscriber sub, final String ban) throws HomeException
    {
        boolean ret = false;

        // the ban in TPS must be immediate responsible account or immediate account.
        if (ban == null || ban.equals(sub.getResponsibleParentAccount(ctx).getBAN()) || ban.equals(sub.getBAN()))
        {

            ret = true;
        }

        return ret;
    }
    
    public Set<Subscriber> getPostpaidSubscribers()
    {
        return postpaidSubscribers_;
    }

    private Set<Subscriber> postpaidSubscribers_;
    private String          ban;
}
