/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.transfer;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.support.TransferSupport;
import com.trilogy.app.crm.transfer.TransfersView;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.visitor.AbortVisitException;


public class TransferIsDisputablePredicate implements Predicate
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    public boolean f(Context ctx, Object obj) throws AbortVisitException
    {
        try
        {
            TransfersView transfer = (TransfersView) obj;
            Subscriber cont = null;
            Subscriber recp = null;
            boolean contOperator = false;
            boolean recpOperator = false;
            if (!TransferSupport.OPERATOR_ID.equals(transfer.getContSubId()))
            {
                contOperator = false;
                cont = SubscriberSupport.lookupSubscriberLimited(ctx, transfer.getContSubId());
                if (null == cont)
                {
                    // subscriber is external
                    return false;
                }
            }
            else
            {
                contOperator = true;
            }
            if (!TransferSupport.OPERATOR_ID.equals(transfer.getRecpSubId()))
            {
                recpOperator = false;
                recp = SubscriberSupport.lookupSubscriberLimited(ctx, transfer.getRecpSubId());
                if (null == recp)
                {
                    // subscriber is external
                    return false;
                }
            }
            else
            {
                recpOperator = true;
            }
            boolean failedTransfer = transfer.getFailedTransferId() != -1L;
            boolean currentDisputeTransfer = transfer.getCurrentDisputeId() != -1L;
            boolean contActive = contOperator || SubscriberStateEnum.ACTIVE.equals(cont.getState());
            boolean recpActive = recpOperator || SubscriberStateEnum.ACTIVE.equals(recp.getState());
            return !failedTransfer && !currentDisputeTransfer && contActive && recpActive;
        }
        catch (Exception e)
        {
            return false;
        }
    }
}