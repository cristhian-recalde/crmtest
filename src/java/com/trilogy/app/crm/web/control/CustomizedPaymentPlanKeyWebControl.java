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
package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.KeyWebControlOptionalValue;
import com.trilogy.framework.xhome.webcontrol.ViewModeEnum;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.payment.PaymentPlanHome;
import com.trilogy.app.crm.bean.payment.PaymentPlanKeyWebControl;
import com.trilogy.app.crm.bean.payment.PaymentPlanStateEnum;
import com.trilogy.app.crm.bean.payment.PaymentPlanXInfo;
import com.trilogy.app.crm.support.PaymentPlanSupport;

/**
 * @author Angie Li
 *
 * Customized Web Control for Payment Plan drop down.
 */
public class CustomizedPaymentPlanKeyWebControl extends
        PaymentPlanKeyWebControl {

    public static final KeyWebControlOptionalValue DEFAULT=new KeyWebControlOptionalValue("---", Long.valueOf(PaymentPlanSupport.INVALID_PAYMENT_PLAN_ID));
    
    public CustomizedPaymentPlanKeyWebControl()
    {
        super();
    }

    public CustomizedPaymentPlanKeyWebControl(boolean autoPreview)
    {
        super(autoPreview);
    }

    public CustomizedPaymentPlanKeyWebControl(int listSize)
    {
        super(listSize);
    }

    public CustomizedPaymentPlanKeyWebControl(int listSize, boolean autoPreview)
    {
        super(listSize, autoPreview);
    }

    public CustomizedPaymentPlanKeyWebControl(int listSize, boolean autoPreview, boolean isOptional)
    {
        super(listSize, autoPreview, isOptional);
    }

    public CustomizedPaymentPlanKeyWebControl(int listSize, boolean autoPreview, boolean isOptional, boolean allowCustom)
    {
        super(listSize, autoPreview, isOptional, allowCustom);
    }

    public CustomizedPaymentPlanKeyWebControl(int listSize, boolean autoPreview, Object optionalValue)
    {
        super(listSize, autoPreview, optionalValue);
    }

    public CustomizedPaymentPlanKeyWebControl(int listSize, boolean autoPreview, Object optionalValue, boolean allowCustom)
    {
        super(listSize, autoPreview, optionalValue, allowCustom);
    }


    /**
     * Cannot use the super class's (AbstractKeyWebControl) toWeb method 
     * because it does a PaymentPlanHome.selectAll().  This will include even
     * the DEPRECATED payment plans. 
     * We want it to be dependent on the view mode for this field.  If the 
     * field is WRITE-ABLE, we have to filter the selections available to only 
     * include the Active payment plans.  Otherwise, we retrieve all the 
     * payment plans no matter the state. TT 5111226624. 
     * Angie Li
     */
    @Override
    public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
    {
        ViewModeEnum mode = (ViewModeEnum) ctx.get("Account.paymentPlan.mode", ViewModeEnum.READ_ONLY);
        
        //the persistent data
        Account realAccount = null;
        //the data going through the pipeline
        String ban = null;
        Object bean = ctx.get(AbstractWebControl.BEAN);
        if (bean instanceof Account)
        {
            ban = ((Account) bean).getBAN();
        }
        else if (bean instanceof Subscriber)
        {
            ban = ((Subscriber) bean).getBAN();
        }

        if ( ban != null && ban.length() != 0 )  // not creating a new bean
        {
            HomeException exception = null;
            try
            {
                realAccount = (Account)((Home)ctx.get(AccountHome.class)).find(ctx, ban);
            }
            catch(HomeException e)
            {
                exception = e;
            }
            finally
            {
                if (realAccount == null || exception != null)
                {
                   new MinorLogMsg(this, "No Account [BAN=" + ban + "] found in Database", exception).log(ctx);
                }
            }
        }
        
        if (realAccount != null && mode.equals(ViewModeEnum.READ_WRITE))
        {
            super.toWeb(wrapContext(ctx, realAccount.getPaymentPlan()), out, name, obj);
        }
        else
        {
            super.toWeb(ctx, out, name, obj);
        }
    }
    
    /**
     * Return a subscontext with the Payment Plan Home filtered 
     * for only Active payment plans and also the currently selected
     * payment plan (in case it is deprecated).
     */
    public Context wrapContext(final Context originalContext, long paymentPlanID)
    {
        final Context newContext = originalContext.createSubContext();

        final Predicate predicate = new Or().add(new EQ(PaymentPlanXInfo.STATE, PaymentPlanStateEnum.ACTIVE))
                                            .add(new EQ(PaymentPlanXInfo.ID, Long.valueOf(paymentPlanID)));
        
        final Home originalHome = (Home)newContext.get(PaymentPlanHome.class);
        final Home newHome = new HomeProxy(newContext, originalHome).where(newContext, predicate);

        newContext.put(PaymentPlanHome.class, newHome);

        return newContext;
    }
    
}
