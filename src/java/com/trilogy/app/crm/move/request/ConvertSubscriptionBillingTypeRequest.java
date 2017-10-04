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

import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.core.SubscriptionClass;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.msp.SpidAware;
import com.trilogy.framework.xhome.web.util.Link;


/**
 * 
 *
 * @author aaron.gourley@redknee.com
 * @since 8.2
 */
public class ConvertSubscriptionBillingTypeRequest extends AbstractConvertSubscriptionBillingTypeRequest implements SpidAware
{
	private int spid = 1;
	
    public static final String CONVERT_SUCCESS_MSG_KEY = ConvertSubscriptionBillingTypeRequest.class.getSimpleName() + ".success";

    /**
     * {@inheritDoc}
     */
    public SubscriptionClass getSubscriptionClass(Context ctx)
    {
        return SubscriptionClass.getSubscriptionClass(ctx, getSubscriptionClass());
    }

    
    /**
     * @{inheritDoc}
     */
    @Override
    public String getSuccessMessage(Context ctx)
    {
        String msg = null;
        
        MessageMgr mmgr = new MessageMgr(ctx, this);
        
        String newType = (getSubscriberType() == SubscriberTypeEnum.POSTPAID ? "Postpaid" : "Prepaid");
        String oldType = (getSubscriberType() == SubscriberTypeEnum.POSTPAID ? "Prepaid" : "Postpaid");
        
        final Link link = new Link(ctx);
        link.remove("cmd");
        link.add("cmd","SubMenuSubProfileEdit");
        link.remove("key");
        link.add("key",this.getNewSubscriptionId());

        msg = mmgr.get(CONVERT_SUCCESS_MSG_KEY, 
                        "New {0} subscription has been created with ID: <a href=\"{1}\">{2}</a>.  Old {3} subscription with ID {4} has been deactivated.",
                        new String[] {
                                newType,
                                link.write(), 
                                this.getNewSubscriptionId(),
                                oldType,
                                this.getOldSubscriptionId()
                            }); 
        return msg;
    }


	@Override
	public int getSpid() 
	{
		return spid;
	}


	@Override
	public void setSpid(int spid) 
	{
		this.spid = spid;
	}
    
}
