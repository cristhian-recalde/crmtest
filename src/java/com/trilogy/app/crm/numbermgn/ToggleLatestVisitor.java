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
package com.trilogy.app.crm.numbermgn;

import java.util.Date;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;

/**
 * For every MSISDNHistory visited it checks if it can be toggled and
 * put the end time stamp to today.
 * @author arturo.medina@redknee.com
 *
 */
public class ToggleLatestVisitor implements Visitor
{


    /**
     * Constructor that accepts all the parameters needed to modify such msisdn history
     * bean.
     * @param latest specifies if it's the ;atest or not
     * @param subscriberId the subscirber id to verify
     * @param endTimestamp the end timestamp to set
     * @param home the home to store in the DB
     */
    public ToggleLatestVisitor(final boolean latest,
            final String subscriberId,
            final Date endTimestamp,
            final Home home)
    {
        latest_ = latest;
        subscriberId_ = subscriberId;
        endTimestamp_ = endTimestamp;
        home_ = home;
        count_ = 0;
    }

    /**
     * {@inheritDoc}
     */
    public void visit(final Context ctx, final Object obj)
        throws AgentException
    {
        final SubscriberNumberMgmtHistory history = (SubscriberNumberMgmtHistory)obj;
        if ((! subscriberId_.isEmpty()) && subscriberId_.equals(history.getSubscriberId()))
        {
            // no need to do anything. This is not a subscriberId update
            setAddNewHistoryRequired(false);
            throw new AbortVisitException();
        }

        toggleHistory(ctx, history);
    }


    /**
     * Shared method to toggle withjoud checking the subscriber id
     * @param ctx the operating context
     * @param history the history bean to set
     * @throws AgentException exception thrown if something wrong occurs
     */
    protected void toggleHistory(final Context ctx,
            final SubscriberNumberMgmtHistory history)
        throws AgentException
    {

        try
        {
            SubscriberNumberMgmtHistory cloneHistory = (SubscriberNumberMgmtHistory) history.clone();

            if (cloneHistory.getLatest() != latest_)
            {
                cloneHistory.setLatest(latest_);
                cloneHistory.setEndTimestamp(endTimestamp_);
            }
            setHistory((SubscriberNumberMgmtHistory)home_.store(ctx, cloneHistory));
            // there should only be one entry with latest set to true
            setAddNewHistoryRequired(true);
            count_++;
            throw new AbortVisitException();
        }
        catch(CloneNotSupportedException cloneEx)
        {
            throw new AgentException(cloneEx);
        }
        catch (HomeException hEx)
        {
            throw new AgentException(hEx);
        }
    }

    /**
     * retrieves the history bean.
     * @return the history bean
     */
    public SubscriberNumberMgmtHistory getHistory()
    {
        return history_;
    }

    /**
     * adds the history bean.
     * @param history the MsisdnMgmtHistory bean
     */
    public void setHistory(final SubscriberNumberMgmtHistory history)
    {
        history_ = history;
    }

    /**
     * specifies if we need to add a new history to the home.
     * @return true if a new bean is required
     */
    public boolean isAddNewHistoryRequired()
    {
        return addNewHistoryRequired_;
    }

    /**
     * updates the boolean the specifies if we need to add a new history to the home.
     * @param addNewHistoryRequired true if a new bean is required
     */
    public void setAddNewHistoryRequired(final boolean addNewHistoryRequired)
    {
        addNewHistoryRequired_ = addNewHistoryRequired;
    }

    public int getCount()
    {
        return count_;
    }
    
    /**
     * specifies if it's the ;atest or not
     */
    private boolean latest_ = false;

    /**
     * the subscirber id to verify
     */
    private String subscriberId_;

    /**
     * the end timestamp to set
     */
    private Date endTimestamp_;
    
    /**
     * Number of history updated
     */
    private int count_;

    /**
     * the home to store in the DB
     */
    private Home home_;

    /**
     * the home to store in the DB
     */
    private SubscriberNumberMgmtHistory history_;

    /**
     * the flag to specify if a new bean needs to be inserted in the DB
     */
    private boolean addNewHistoryRequired_ = false;

    /**
     * The serial version UID
     */
    private static final long serialVersionUID = 9031224133776452200L;

}
