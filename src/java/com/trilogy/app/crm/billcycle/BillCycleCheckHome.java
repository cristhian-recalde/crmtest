package com.trilogy.app.crm.billcycle;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;

import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.BillCycle;
import com.trilogy.app.crm.bean.BillCycleHome;

/**
 * @author rattapattu
 *  
 */
public class BillCycleCheckHome extends HomeProxy
{
    public BillCycleCheckHome(Context ctx, Home delegate)
    {
        super(ctx, delegate);
    }

    public Object store(Context ctx, Object obj) throws HomeException
    {
        checkForDayOfMonth(ctx,obj);
        return super.store(ctx, obj);
    }

    public void remove(Context ctx, Object obj) throws HomeException
    {      
        if (isUsedByAccounts(ctx,((BillCycle)obj).getBillCycleID()))
        {
            throw new HomeException("Cannot delete, this bill cycle is being used by accounts");
        }
        super.remove(ctx, obj);
    }
    
    /**
     * @param ctx
     * @param obj
     */
    private void checkForDayOfMonth(Context ctx, Object obj) throws HomeException
    {
        if(obj != null)
        {
	        BillCycle newObj = (BillCycle)obj;
	        BillCycle oldObj = getOldBillCycle(ctx,newObj.getBillCycleID());
	        if(isUsedByAccounts(ctx,newObj.getBillCycleID()))
	        {
	            if (newObj.getDayOfMonth() != oldObj.getDayOfMonth())
	            {
	                throw new HomeException("Cannot modify day of month as this bill cycle is being used by accounts");	                
	            }
	        }
        }    
    }

    /**
     * @param ctx
     * @param billCycleID
     */
    private BillCycle getOldBillCycle(Context ctx, int billCycleId)
    {
        Home home = (Home)ctx.get(BillCycleHome.class);
        Object obj;
        try
        {
            obj = home.find(ctx, Integer.valueOf(billCycleId));
        }
        catch (Exception e)
        {
            obj = null;
        }
        return (obj != null ? (BillCycle)obj : null );
    }
    
    private boolean isUsedByAccounts(Context ctx, int billCycleId)
    {
        Home home = (Home)ctx.get(AccountHome.class);
        Object account = null;
        try
        {
           account = home.find(ctx, new EQ(AccountXInfo.BILL_CYCLE_ID, Integer.valueOf(billCycleId)));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (account != null);
    }
}
