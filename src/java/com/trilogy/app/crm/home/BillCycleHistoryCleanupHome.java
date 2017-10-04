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
package com.trilogy.app.crm.home;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.BillCycleChangeStatusEnum;
import com.trilogy.app.crm.bean.BillCycleHistory;
import com.trilogy.app.crm.support.BillCycleHistorySupport;
import com.trilogy.app.crm.support.HomeSupportHelper;


/**
 * 
 *
 * @author aaron.gourley@redknee.com
 * @since 9.1
 */
public class BillCycleHistoryCleanupHome extends HomeProxy
{
    public BillCycleHistoryCleanupHome(Context ctx, Home delegate)
    {
        super(ctx, delegate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object create(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
        BillCycleHistory lastEvent = null;
        
        BillCycleChangeStatusEnum newStateForLastEvent = null;
        if (obj instanceof BillCycleHistory)
        {
            BillCycleHistory hist = (BillCycleHistory) obj;
            
            if (BillCycleChangeStatusEnum.PENDING.equals(hist.getStatus()))
            {
                lastEvent = BillCycleHistorySupport.getLastEvent(ctx, hist.getBAN());
                if (lastEvent == null
                        || (BillCycleChangeStatusEnum.PENDING.equals(lastEvent.getStatus())
                                && lastEvent.getBillCycleChangeDate().before(hist.getBillCycleChangeDate())))
                {
                    newStateForLastEvent = BillCycleChangeStatusEnum.CANCELLED;
                }
            }
            else if (BillCycleChangeStatusEnum.COMPLETE.equals(hist.getStatus()))
            {
                lastEvent = BillCycleHistorySupport.getLastEvent(ctx, hist.getBAN());
                if (lastEvent == null
                        || BillCycleChangeStatusEnum.PENDING.equals(lastEvent.getStatus()))
                {
                    newStateForLastEvent = BillCycleChangeStatusEnum.PROCESSED;
                }
            }
        }
        
        Object resultObj = super.create(ctx, obj);
        
        if (resultObj instanceof BillCycleHistory 
                && lastEvent != null && newStateForLastEvent != null)
        {
            BillCycleHistory hist = (BillCycleHistory) resultObj;
            
            lastEvent.setStatus(newStateForLastEvent);
            if (BillCycleChangeStatusEnum.CANCELLED.equals(newStateForLastEvent))
            {
                lastEvent.setFailureMessage("New bill cycle change scheduled to replace this one.");
            }
            
            try
            {
                lastEvent = HomeSupportHelper.get(ctx).storeBean(ctx, lastEvent);
                new InfoLogMsg(this, "Changed state of pending bill cycle change request [" + lastEvent.ID()
                        + "] to " + newStateForLastEvent + " following creation of " + hist.getStatus()
                        + " request [" + hist.ID() + "]", null).log(ctx);
            }
            catch (HomeException e)
            {
                new MinorLogMsg(this, "Error changing state of pending bill cycle change request [" + lastEvent.ID()
                        + "] to " + newStateForLastEvent + " following creation of " + hist.getStatus()
                        + " request [" + hist.ID() + "]", e).log(ctx);
            }
        }
        
        return resultObj;
    }
}
