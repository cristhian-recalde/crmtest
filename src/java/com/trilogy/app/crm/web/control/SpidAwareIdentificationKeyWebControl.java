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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.msp.SpidAware;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.IdentificationGroup;
import com.trilogy.app.crm.bean.IdentificationHome;
import com.trilogy.app.crm.bean.IdentificationXInfo;
import com.trilogy.app.crm.bean.SpidIdentificationGroups;
import com.trilogy.app.crm.bean.SpidIdentificationGroupsHome;
import com.trilogy.app.crm.bean.SpidIdentificationGroupsXInfo;
import com.trilogy.app.crm.bean.account.AccountIdentification;
import com.trilogy.app.crm.bean.account.AccountIdentificationGroup;

/**
 * This web control filters the identification key web control based on the spid
 * identification groups configuration.
 * 
 * @author marcio.marques@redknee.com
 * 
 */
public class SpidAwareIdentificationKeyWebControl extends ProxyWebControl 
{
    public SpidAwareIdentificationKeyWebControl(WebControl delegate)
    {
        setDelegate(delegate);
    }
    
    @Override
    public void toWeb(Context ctx, PrintWriter out, String name, Object obj) 
    {   
        Context subCtx = ctx.createSubContext();

        subCtx.put(IdentificationHome.class, filterIdentificationHome(ctx));
        
        delegate_.toWeb(subCtx, out, name, obj);
    }

    private Home filterIdentificationHome(final Context ctx)
    {
        final Home originalHome = (Home) ctx.get(IdentificationHome.class);
        final Object bean = ctx.get(AbstractWebControl.BEAN);
        final AccountIdentificationGroup actIdGroup = (AccountIdentificationGroup) ctx.get(AccountIdentificationGroup.class);
		final SpidAware account = (SpidAware) ctx.get(Account.class);
        int spid = -1;
        
        if (bean instanceof AccountIdentification && account!=null && actIdGroup!=null)
        {
            final AccountIdentification ai = (AccountIdentification) bean;
            spid = account.getSpid();
            Or filter = new Or();
            And and = new And();
            and.add(new EQ(IdentificationXInfo.SPID, Integer.valueOf(spid)));
            if (actIdGroup.getIdGroup()!=AccountIdentificationGroup.DEFAULT_IDGROUP)
            {
                try
                {
                Home spidIdHome = (Home) ctx.get(SpidIdentificationGroupsHome.class);
                SpidIdentificationGroups spidIdGroups = (SpidIdentificationGroups) spidIdHome.find(new EQ(SpidIdentificationGroupsXInfo.SPID, Integer.valueOf(spid)));
                if (spidIdGroups!=null)
                {
                    Iterator<IdentificationGroup> iter = spidIdGroups.getGroups().iterator();
                    while (iter.hasNext())
                    {
                        IdentificationGroup idGroup = iter.next();
                        if (idGroup.getIdGroup()==actIdGroup.getIdGroup())
                        {
                            if (!idGroup.isAcceptAny())
                            {
                                Set<Integer> set = new HashSet<Integer>();
                                StringTokenizer st = new StringTokenizer(idGroup.getIdentificationIdList(),",");
                                while (st.hasMoreTokens()) 
                                {
                                    set.add(Integer.valueOf(st.nextToken()));
                                }
                                and.add(new In(IdentificationXInfo.CODE, set));
                            }
                            break;
                        }
                    }
                }
                } catch (Exception e)
                {
                    if(LogSupport.isDebugEnabled(ctx))
                    {
                      new DebugLogMsg(this,e.getMessage(),e).log(ctx);
                    }
                }
            }
            else
            {
                and.add(new EQ(IdentificationXInfo.CODE, Integer.valueOf(ai.getIdType())));
            }
            filter.add(and);
            if (ai.getIdType()!=AccountIdentification.DEFAULT_IDTYPE)
            {
                filter.add(new EQ(IdentificationXInfo.CODE, Integer.valueOf(ai.getIdType())));
            }

            final Home newHome = new HomeProxy(ctx, originalHome).where(ctx, filter);
            return newHome;
        }
        return originalHome;
    }

}
