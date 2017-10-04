/* 
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily 
 * available. Additionally, source code is, by its very nature, confidential 
 * information and inextricably contains trade secrets and other information 
 * proprietary, valuable and sensitive to Redknee, no unauthorised use, 
 * disclosure, manipulation or otherwise is permitted, and may only be used 
 * in accordance with the terms of the licence agreement entered into with 
 * Redknee Inc. and/or its subsidiaries. 
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.BillCycle;
import com.trilogy.app.crm.bean.BillCycleKeyWebControl;
import com.trilogy.app.crm.bean.BillCycleXInfo;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.move.request.ConvertAccountBillingTypeRequest;
import com.trilogy.app.crm.support.BillCycleSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.LT;
import com.trilogy.framework.xhome.elang.NEQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.KeyWebControlOptionalValue;

/**
 * This class adds customized behaviour to the BillCycleKeyWebControl.
 *
 * @author Jimmy Ng
 */
public class CustomizedBillCycleKeyWebControl extends BillCycleKeyWebControl
{
	public static final KeyWebControlOptionalValue DEFAULT=new KeyWebControlOptionalValue("--", "-1");

	public CustomizedBillCycleKeyWebControl()
	{
		super();
	}

	public CustomizedBillCycleKeyWebControl(boolean autoPreview)
	{
		super(autoPreview);
	}

	public CustomizedBillCycleKeyWebControl(int listSize)
	{
		super(listSize);
	}

	public CustomizedBillCycleKeyWebControl(int listSize, boolean autoPreview)
	{
		super(listSize, autoPreview);
	}

	public CustomizedBillCycleKeyWebControl(int listSize, boolean autoPreview, boolean isOptional)
	{
		super(listSize, autoPreview, isOptional);
	}

	public CustomizedBillCycleKeyWebControl(int listSize, boolean autoPreview, boolean isOptional, boolean allowCustom)
	{
		super(listSize, autoPreview, isOptional, allowCustom);
	}

	public CustomizedBillCycleKeyWebControl(int listSize, boolean autoPreview, Object optionalValue)
	{
		super(listSize, autoPreview, optionalValue);
	}

	public CustomizedBillCycleKeyWebControl(int listSize, boolean autoPreview, Object optionalValue, boolean allowCustom)
	{
		super(listSize, autoPreview, optionalValue, allowCustom);
	}

	/**
	 * INHERIT
	 */
	@Override
    public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
	{
        Context subCtx = ctx.createSubContext();
        
        HttpServletRequest req = (HttpServletRequest) subCtx.get(HttpServletRequest.class);
        HttpSession session = req.getSession();
        
		final int mode = subCtx.getInt("MODE", DISPLAY_MODE);
		
		Object beanObj = subCtx.get(AbstractWebControl.BEAN);
        
        Account account = null;
		if (beanObj instanceof Account)
		{
			account = (Account) beanObj;
		}
		
		ConvertAccountBillingTypeRequest request = null;
		
		if ( beanObj instanceof ConvertAccountBillingTypeRequest)
		{
		    request = (ConvertAccountBillingTypeRequest) beanObj;
		}
		
        Home billCycleHome = (Home) subCtx.get(getHomeKey());
		if( beanObj instanceof CRMSpid || beanObj instanceof ConvertAccountBillingTypeRequest ||
		        (/* TT8051700004 - Filter out the auto-bill cycles for postpaid accounts */
		        account != null && account.isPostpaid())
		    || (/* This is for conversion request web control */
		            request != null && request.getSystemType().equals(SubscriberTypeEnum.POSTPAID)))
		{
		    // TT8051700004 - Filter out the auto-bill cycles for postpaid accounts
		    billCycleHome = billCycleHome.where(subCtx, new LT(BillCycleXInfo.BILL_CYCLE_ID, BillCycleSupport.AUTO_BILL_CYCLE_START_ID));
		}

        //
        // Add same-billing-day checking when we are in EDIT mode.
        //
		if (!(beanObj instanceof CRMSpid) && mode == EDIT_MODE && ( request == null))
		{
			session.removeAttribute("OldSatId");
			if (obj instanceof Integer)
			{
				try
				{
				    BillCycle bean = (BillCycle) billCycleHome.find(subCtx, obj);
	                if (bean != null)
	                {
	                    // Filter out those Bill Cycles with different billing day
	                    billCycleHome = billCycleHome.where(subCtx, new EQ(BillCycleXInfo.DAY_OF_MONTH, bean.getDayOfMonth()));
	                }
				}
				catch (HomeException e)
				{
				}
			}
		}
		
        if (account != null)
        {
            // This code filters the special Bill Cycle Id for Postpaid Subs/accounts which are not individual.
            Subscriber sub = account.getSubscriber();
            if ((!account.isIndividual(subCtx) || account.isBusiness(subCtx))
                    || (sub != null && sub.getSubscriberType().equals(SubscriberTypeEnum.POSTPAID)))
            {
                if (billCycleHome != null)
                {
                    billCycleHome = billCycleHome.where(subCtx, new NEQ(BillCycleXInfo.DAY_OF_MONTH, Integer.valueOf(-1)));
                }
            }
        }

        subCtx.put(getHomeKey(), billCycleHome);
		super.toWeb(subCtx, out, name, obj);
	}
}
