package com.trilogy.app.crm.home;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.BillCycleChangeStatusEnum;
import com.trilogy.app.crm.bean.BillCycleHistory;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.BillCycleHistorySupport;
import com.trilogy.app.crm.support.HomeSupportHelper;

/**
 * 
 *
 * @author aaron.gourley@redknee.com
 * @since 9.1
 */
public class AutoAccountUpdatingBillCycleHistoryHome extends HomeProxy
{
    public AutoAccountUpdatingBillCycleHistoryHome(Context ctx, Home delegate)
    {
        super(ctx, delegate);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object create(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
        Object resultObj = null;
        
        if (obj instanceof BillCycleHistory)
        {
            BillCycleHistory hist = (BillCycleHistory) obj;
            if (BillCycleChangeStatusEnum.PENDING.equals(hist.getStatus())
                    && hist.getOldBillCycleDay() == hist.getNewBillCycleDay())
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, "Pending bill cycle change has the same old & new bill cycle day.  Attempting to update account " + hist.getBAN() + " directly...", null).log(ctx);
                }
                boolean autoProcessed = false;
                try
                {
                    Account account = AccountSupport.getAccount(ctx, hist.getBAN());
                    account.setBillCycleID(hist.getNewBillCycleID());
                    Account resultAccount = HomeSupportHelper.get(ctx).storeBean(ctx, account);
                    autoProcessed = (resultAccount != null && resultAccount.getBillCycleID() == hist.getNewBillCycleID());
                }
                catch (Exception e)
                {
                    new MinorLogMsg(this, "Failed to update bill cycle to " + hist.getNewBillCycleID()
                            + " for account " + hist.getBAN()
                            + ".  Pending change request will be created for retry later.", e).log(ctx);
                }

                if (autoProcessed)
                {
                    new InfoLogMsg(this, "Account " + hist.getBAN() + " updated directly for bill cycle change request.  Old & new bill cycle dates were the same.", null).log(ctx);
                    resultObj = BillCycleHistorySupport.getLastEvent(ctx, ((BillCycleHistory)obj).getBAN());
                }
            }
        }
        
        if (resultObj == null)
        {
            resultObj = super.create(ctx, obj);
        }
        
        return resultObj;
    }
}