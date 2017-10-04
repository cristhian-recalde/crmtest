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

import java.util.Date;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.InfoLogMsg;

import com.trilogy.app.crm.bean.BillCycleChangeStatusEnum;
import com.trilogy.app.crm.bean.BillCycleHistory;
import com.trilogy.app.crm.bean.SystemNoteSubTypeEnum;
import com.trilogy.app.crm.bean.SystemNoteTypeEnum;
import com.trilogy.app.crm.support.BillCycleHistorySupport;
import com.trilogy.app.crm.support.NoteSupportHelper;


/**
 * 
 *
 * @author aaron.gourley@redknee.com
 * @since 9.1
 */
public class AccountNoteBillCycleHistoryHome extends HomeProxy
{

    public AccountNoteBillCycleHistoryHome(Context ctx, Home delegate)
    {
        super(ctx, delegate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object create(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
        Object resultObj = super.create(ctx, obj);

        if (resultObj instanceof BillCycleHistory)
        {
            BillCycleHistory hist = (BillCycleHistory) resultObj;
            if (BillCycleChangeStatusEnum.PENDING.equals(hist.getStatus()))
            {
                String msg = "Pending bill cycle change submitted from [ID=" + hist.getOldBillCycleID() + ",Day=" + hist.getOldBillCycleDay() + "] to [ID=" + hist.getNewBillCycleID() + ",Day=" + hist.getNewBillCycleDay() + "]";
                NoteSupportHelper.get(ctx).addAccountNote(ctx, hist.getBAN(), msg, SystemNoteTypeEnum.EVENTS, SystemNoteSubTypeEnum.BILL_CYCLE_CHANGE);
                new InfoLogMsg(this, msg, null).log(ctx);
            }
            else if (BillCycleChangeStatusEnum.FAIL.equals(hist.getStatus()))
            {
                String msg = "Failed to change bill cycle change from [ID=" + hist.getOldBillCycleID() + ",Day=" + hist.getOldBillCycleDay() + "] to [ID=" + hist.getNewBillCycleID() + ",Day=" + hist.getNewBillCycleDay() + "].  Message=[" + hist.getFailureMessage() + "]";
                NoteSupportHelper.get(ctx).addAccountNote(ctx, hist.getBAN(), msg, SystemNoteTypeEnum.EVENTS, SystemNoteSubTypeEnum.BILL_CYCLE_CHANGE);
                new InfoLogMsg(this, msg, null).log(ctx);
            }
            else if (BillCycleChangeStatusEnum.COMPLETE.equals(hist.getStatus()))
            {
                String msg = "Bill cycle changed from [ID=" + hist.getOldBillCycleID() + ",Day=" + hist.getOldBillCycleDay() + "] to [ID=" + hist.getNewBillCycleID() + ",Day=" + hist.getNewBillCycleDay() + "]";
                NoteSupportHelper.get(ctx).addAccountNote(ctx, hist.getBAN(), msg, SystemNoteTypeEnum.EVENTS, SystemNoteSubTypeEnum.BILL_CYCLE_CHANGE);
                new InfoLogMsg(this, msg, null).log(ctx);
            }
        }
        
        return resultObj;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object store(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
        Object resultObj = super.store(ctx, obj);

        if (resultObj instanceof BillCycleHistory)
        {
            BillCycleHistory hist = (BillCycleHistory) resultObj;
            if (BillCycleChangeStatusEnum.PENDING.equals(hist.getStatus()))
            {
                String msg = "Altered pending bill cycle change.  New change is from [ID=" + hist.getOldBillCycleID() + ",Day=" + hist.getOldBillCycleDay() + "] to [ID=" + hist.getNewBillCycleID() + ",Day=" + hist.getNewBillCycleDay() + "]";
                NoteSupportHelper.get(ctx).addAccountNote(ctx, hist.getBAN(), msg, SystemNoteTypeEnum.EVENTS, SystemNoteSubTypeEnum.BILL_CYCLE_CHANGE);
                new InfoLogMsg(this, msg, null).log(ctx);
            }
            else if (BillCycleChangeStatusEnum.CANCELLED.equals(hist.getStatus()))
            {
                Date lastEventDate = BillCycleHistorySupport.getLastEventDate(ctx, hist.getBAN());
                if (lastEventDate == null
                        || lastEventDate.equals(hist.getBillCycleChangeDate()))
                {
                    String msg = "Cancelled pending bill cycle change from [ID=" + hist.getOldBillCycleID() + ",Day=" + hist.getOldBillCycleDay() + "] to [ID=" + hist.getNewBillCycleID() + ",Day=" + hist.getNewBillCycleDay() + "]";
                    NoteSupportHelper.get(ctx).addAccountNote(ctx, hist.getBAN(), msg, SystemNoteTypeEnum.EVENTS, SystemNoteSubTypeEnum.BILL_CYCLE_CHANGE);
                    new InfoLogMsg(this, msg, null).log(ctx);
                }
            }
        }
        
        return resultObj;
    }

}
