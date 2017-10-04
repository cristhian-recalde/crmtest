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

package com.trilogy.app.crm.transfer;

import com.trilogy.app.crm.support.TransferSupport;
import com.trilogy.app.troubleticket.bean.StatusEnum;

public class TransferDispute
    extends AbstractTransferDispute
{
    public TransferDispute()
    {
        super();
    }

    public boolean canBeCreatedFromTT()
    {
        return false;
    }

    public boolean statusTransitionAllowed(StatusEnum status)
    {
        switch(status.getIndex())
        {
        case StatusEnum.INITIATED_INDEX:
        case StatusEnum.ASSIGNED_INDEX:
        case StatusEnum.FEEDBACK_INDEX:
        case StatusEnum.MONITORING_INDEX:
        case StatusEnum.OPEN_INDEX:
        case StatusEnum.SOLUTION_AVAILABLE_INDEX:
            return true;

        case StatusEnum.RESOLVED_INDEX:
        case StatusEnum.CLOSED_INDEX:
            return TransferDisputeStatusEnum.ACCEPTED.equals(getState()) || TransferDisputeStatusEnum.REJECTED.equals(getState());

        default:
            return false;
        }
    }

    public void setStatus(StatusEnum status)
    {
        switch(status.getIndex())
        {
        case StatusEnum.INITIATED_INDEX:
        case StatusEnum.FEEDBACK_INDEX:
        case StatusEnum.MONITORING_INDEX:
        case StatusEnum.OPEN_INDEX:
        case StatusEnum.SOLUTION_AVAILABLE_INDEX:
        case StatusEnum.RESOLVED_INDEX:
        case StatusEnum.CLOSED_INDEX:
            break;

        case StatusEnum.ASSIGNED_INDEX:
            if(TransferDisputeStatusEnum.INITIATED.equals(getState()))
            {
                setState(TransferDisputeStatusEnum.ASSIGNED);
            }
            break;

        default:
        }
    }

    public String getSubscriptionOrBAN()
    {
        return !TransferSupport.OPERATOR_ID.equals(getContSubId()) ? getContSubId() : getRecpSubId();
    }

    public Object getHomeKey()
    {
        return TransferDisputeHome.class;
    }
    public Long getID()
    {
        return getDisputeId();
    }
    
    public boolean isMakeAdjustmentAllowed()
    {
        return false;
    }    
}