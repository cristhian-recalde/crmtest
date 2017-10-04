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
package com.trilogy.app.crm.move.request;

import java.util.Collection;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.web.util.Link;
import com.trilogy.framework.xlog.log.MajorLogMsg;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountsDiscount;
import com.trilogy.app.crm.bean.AccountsDiscountXInfo;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.core.SubscriptionClass;
import com.trilogy.app.crm.support.HomeSupportHelper;


/**
 * Concrete class providing customization of move request related code.  Note that this move
 * request is used for account conversion (i.e. moving from prepaid to postpaid or visa versa).
 *
 * @author aaron.gourley@redknee.com
 * @since 8.2
 */
public class ConvertAccountBillingTypeRequest extends AbstractConvertAccountBillingTypeRequest
{
    public static final String CONVERT_SUCCESS_MSG_KEY = ConvertAccountBillingTypeRequest.class.getSimpleName() + ".success";

    /**
     * @{inheritDoc}
     */
    @Override
    public String getSuccessMessage(Context ctx)
    {
        String msg = null;
        
        MessageMgr mmgr = new MessageMgr(ctx, this);
        
        String newType = (getSystemType() == SubscriberTypeEnum.POSTPAID ? "Postpaid" : "Prepaid");
        String oldType = (getSystemType() == SubscriberTypeEnum.POSTPAID ? "Prepaid" : "Postpaid");
        
        final Link link = new Link(ctx);
        link.remove("cmd");
        link.add("cmd","SubMenuAccountEdit");
        link.remove("key");
        link.add("key",this.getNewBAN());

        msg = mmgr.get(CONVERT_SUCCESS_MSG_KEY, 
                        "New {0} account created with BAN: <a href=\"{1}\">{2}</a>.  Old {3} account with BAN {4} has been deactivated.",
                        new String[] {
                                newType,
                                link.write(), 
                                this.getNewBAN(),
                                oldType,
                                this.getExistingBAN()
                            }); 
        
        if(getOldAccount(ctx).getSystemType() == SubscriberTypeEnum.POSTPAID && getNewAccount(ctx).getSystemType() == SubscriberTypeEnum.PREPAID)
        {
        	try {
        	And and = new And ();
    		and.add(new EQ(AccountsDiscountXInfo.BAN, getOldAccount(ctx).getBAN()));
    		and.add(new EQ(AccountsDiscountXInfo.SPID, getOldAccount(ctx).getSpid()));
    		Collection<AccountsDiscount> coll = HomeSupportHelper.get(ctx).getBeans(ctx, AccountsDiscount.class, and);
    		
    		for (AccountsDiscount acc : coll){
					HomeSupportHelper.get(ctx).removeBean(ctx, acc);
				}  
        	}catch (HomeException e) {
        		new MajorLogMsg(Account.class, "Unable to find/remove discount class for ban " + getOldAccount(ctx).getBAN(), e).log(ctx);
			}
        }
        return msg;
    }

    /**
     * {@inheritDoc}
     */
    public SubscriptionClass getSubscriptionClass(Context ctx)
    {
        return SubscriptionClass.getSubscriptionClass(ctx, getSubscriptionClass());
    }
}
