/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */

package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;
import java.util.Collection;

import com.trilogy.app.crm.bean.AccntGrpScreeningTemp;
import com.trilogy.app.crm.bean.AccntGrpScreeningTempXInfo;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.GroupScreeningTemplateHome;
import com.trilogy.app.crm.bean.GroupScreeningTemplateXInfo;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.webcontrol.AbstractKeyWebControl;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * A Proxy-Key control that selects keys based on property match (equality join)
 * This class filters the group screening template on the basis of template
 * assigned to the parent account
 * 
 * @author ankit.nagpal@redknee.com
 * since 9_9
 * 
 */
public class CustomGroupScreeningTemplateKeyWebControl extends ProxyWebControl
{
    public CustomGroupScreeningTemplateKeyWebControl(AbstractKeyWebControl keyWC) {
		super(keyWC);
	}

    
    @Override
    public Context wrapContext(Context ctx)
    {
    	Context subCtx = ctx.createSubContext();
		Home h = (Home) subCtx.get(GroupScreeningTemplateHome.class);
		final Object sub = subCtx.get(AbstractWebControl.BEAN);

		if (sub instanceof Subscriber) 
		{
			final Subscriber s = (Subscriber) sub;
			Account parentAccount = null;
			final Account account = SubscriberSupport.lookupAccount(subCtx, s);

			try 
			{
				if (account != null) 
				{
					parentAccount = account.getParentAccount(subCtx);
				}
			} 
			catch (HomeException e) 
			{
				LogSupport.minor(subCtx, this, "Unable to find parent account "	+ e.getMessage(), e);
			}
			if (parentAccount != null) 
			{
				And and = new And();
				Or or = new Or();
				and.add(new EQ(AccntGrpScreeningTempXInfo.BAN, parentAccount.getBAN()));
				and.add(new EQ(AccntGrpScreeningTempXInfo.SPID, parentAccount.getSpid()));
				Collection<AccntGrpScreeningTemp> coll = null;
				try
				{
					coll = HomeSupportHelper.get(subCtx).getBeans(subCtx, AccntGrpScreeningTemp.class, and);
		        
			        for (AccntGrpScreeningTemp accountsGroupScreeningTemplate : coll)
			        {
			        	or.add(new EQ(GroupScreeningTemplateXInfo.IDENTIFIER, accountsGroupScreeningTemplate.getScreeningTemplate()));
			        }
			    }
				catch (HomeException e) {
					new MinorLogMsg(this, "Error during output of group screening to subscriber. ", e).log(subCtx); 
				}
				subCtx.put(GroupScreeningTemplateHome.class, h.where(subCtx, or));
			}
			else
			{
				subCtx.put(GroupScreeningTemplateHome.class, h.where(ctx, new EQ(GroupScreeningTemplateXInfo.IDENTIFIER, new Long(-1))));
			}
		}
		return subCtx;
	}
    
    
    public void toWeb(Context ctx, PrintWriter out, String p2, Object p3)
    {
    	Context subCtx = ctx.createSubContext();
		final Object sub = subCtx.get(AbstractWebControl.BEAN);
    	if (sub instanceof Subscriber && subCtx.getInt("MODE", DISPLAY_MODE) == DISPLAY_MODE  && ((Subscriber) sub).getState() != SubscriberStateEnum.INACTIVE)
        {
    		final Subscriber s = (Subscriber) sub;
    		if(s.getGroupScreeningTemplateId() != -1)
    		{
    			getDelegate(ctx).toWeb(ctx, out, p2, new EQ(GroupScreeningTemplateXInfo.IDENTIFIER, s.getGroupScreeningTemplateId()));
    		}
			return;
        }

        getDelegate(ctx).toWeb(wrapContext(ctx), out, p2, p3);
    }
}
