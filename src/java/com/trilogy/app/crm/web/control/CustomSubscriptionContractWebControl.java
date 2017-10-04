/*
 * Created on Apr 7, 2004
 */
package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;
import java.util.Date;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.contract.SubscriptionContractTerm;
import com.trilogy.app.crm.contract.SubscriptionContractTermXInfo;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.core.web.XCurrencyWebControl;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;


/**
 * @author dzhang
 * 
 *         Customized WebControl for Subscriber's Credit Limit
 */
public class CustomSubscriptionContractWebControl extends ProxyWebControl
{

    public CustomSubscriptionContractWebControl()
    {
        super(XCurrencyWebControl.instance());
    }


    public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
    {
        try
        {
            int mode = ctx.getInt("MODE", DISPLAY_MODE);
            Object value = obj;
            Subscriber sub = (Subscriber) ctx.get(AbstractWebControl.BEAN);

            long contractId = sub.getSubscriptionContract(ctx);
            boolean changed = false;
            if ((Long)value != contractId)
            {
                SubscriptionContractTerm term = HomeSupportHelper.get(ctx).findBean(ctx, SubscriptionContractTerm.class, new EQ(SubscriptionContractTermXInfo.ID, contractId));
                if (term != null)
                {
                    Date curDate = new Date();
                    Date endDate = CalendarSupportHelper.get(ctx).findDateMonthsAfter(term.getContractLength(), curDate);
                    sub.setSubscriptionContractStartDate(curDate);
                    sub.setSubscriptionContractEndDate(endDate);
                    changed = true;
                }
            
            }
            
            if (mode == EDIT_MODE && changed )
            {
                out.print("<font size=\"1\" color=\"red\">Warning: Terminating contract has penalty.\n Please see the contract for details</font>");
                
            }
            getDelegate().toWeb(ctx, out, name, value);
        }
        catch (HomeException e)
        {
            // not important
        }
    }
}