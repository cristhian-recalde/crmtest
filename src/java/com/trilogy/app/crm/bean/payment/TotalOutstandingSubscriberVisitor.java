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

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.numbermgn.MsisdnMgmtHistory;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * When visiting, it checks if the state is not available, postpaid and the total amount
 * outstanding is greater than zero.
 * @author arturo.medina@redknee.com
 *
 */
public class TotalOutstandingSubscriberVisitor implements Visitor
{

    /**
     * Creates the list of subscribers with outstanding amount owing.
     *
     */
    public TotalOutstandingSubscriberVisitor()
    {
        totalOutstandingSubscribers_ = new HashSet<Subscriber>();
        totalPostpaidSubscribersIDs_ = new HashSet<String>(); 
        activeSubscribers = new HashSet<Subscriber>(); 
    }

    
    /**
     * {@inheritDoc}
     */
    public void visit(final Context context, final Object obj)
        throws AgentException
    {
        Subscriber sub = (Subscriber) obj;
        if (sub != null && sub.getSubscriberType().equals(SubscriberTypeEnum.POSTPAID))
        {

            if (totalPostpaidSubscribersIDs_.add(sub.getId()))
            {

                // interface changed but there is no documentation. need confirm.
                final long amountOwing = sub.getMonthToDateBalance(context) + sub.getLastInvoiceAmount(context);

                if (amountOwing > 0)
                {
                    if (LogSupport.isDebugEnabled(context))
                    {
                        LogSupport.debug(context, this, "Subscriber with ID " + sub.getId()
                                + " has amount owing of " + amountOwing + " Adding it to the list");
                    }
                    totalOutstandingSubscribers_.add(sub);
                }
            }
            if (!(sub.getState().equals(SubscriberStateEnum.INACTIVE) || sub.getState().equals(
                    SubscriberStateEnum.PENDING)))
            {
                this.activeSubscribers.add(sub);
            }
        }
    }
    

    /**
     * Returns the array of outstanding subscriber in the MSISDN history.
     * @return the array of outstanding subscriber in the MSISDN history
     */
    public Set<Subscriber> getOutstandingSubscribers()
    {
        return totalOutstandingSubscribers_;
    }


    public Set<String> getPostpaidSubscribersIDs()
    {
        return totalPostpaidSubscribersIDs_;
    }

    public Set<Subscriber> getActiveSubscriber()
    {
    	return this.activeSubscribers;
    }
	
    /**
     * The number of outstanding subscribers in the visitor.
     */
    private Set<Subscriber> totalOutstandingSubscribers_;
    private Set<String> totalPostpaidSubscribersIDs_;
    private Set<Subscriber> activeSubscribers; 
    

	/**
     * The serial version UID
     */
    private static final long serialVersionUID = 1697598602644268876L;

}
