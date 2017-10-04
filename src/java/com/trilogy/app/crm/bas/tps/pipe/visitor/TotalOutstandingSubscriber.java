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
package com.trilogy.app.crm.bas.tps.pipe.visitor;


import java.util.ArrayList;
import java.util.List;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.numbermgn.MsisdnMgmtHistory;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * When visiting, it checks if the state is not available, postpaid and the total amount
 * outstanding is greater than zero.
 * @author arturo.medina@redknee.com
 *
 */
public class TotalOutstandingSubscriber implements Visitor
{

    /**
     * Creates the list of subscribers with outstanding amount owing.
     *
     */
    public TotalOutstandingSubscriber()
    {
        totalOutstandingSubscribers_ = new ArrayList<String>();
    }

    /**
     * {@inheritDoc}
     */
    public void visit(final Context context, final Object obj)
        throws AgentException
    {
        final MsisdnMgmtHistory number = (MsisdnMgmtHistory) obj;
        try
        {
            final Subscriber sub = SubscriberSupport.lookupSubscriberForSubId(context, number.getSubscriberId());

            if  (!totalOutstandingSubscribers_.contains(sub.getId()))
            {
                final long amountOwing = sub.getAmountOwing();
                if (amountOwing > 0)
                {
                    LogSupport.debug(context, this, "Subscriber with ID " + sub.getId()
                            + " has amount owing of "
                            + amountOwing
                            + " Adding it to the list");
    
                    totalOutstandingSubscribers_.add(sub.getId());
                }
            }
        }
        catch (HomeException e)
        {
            LogSupport.minor(context,
                    this,
                    "HomeException when trying to get the subscriber by ID" + e.getMessage(),
                    e);
        }
    }

    /**
     * Verifies if the number of subscribers with outstanding amount is greater than one.
     * @return true if it does, flase otherwise
     */
    public boolean hasTotalOutstandingSubscribers()
    {
        return totalOutstandingSubscribers_.size() > 1;
    }

    /**
     * Returns the array of outstanding subscriber in the MSISDN history.
     * @return the array of outstanding subscriber in the MSISDN history
     */
    public List<String> getOutstandingSubscribers()
    {
        return totalOutstandingSubscribers_;
    }

    /**
     * The number of outstanding subscribers in the visitor.
     */
    private List<String> totalOutstandingSubscribers_;

    /**
     * The serial version UID
     */
    private static final long serialVersionUID = 1697598602644268876L;

}
