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
package com.trilogy.app.crm.web.border.search;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.ConvergedAccountSubscriberHome;
import com.trilogy.app.crm.bean.ConvergedAccountSubscriberTransientHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.ConvergedAccountSubscriberSearch;
import com.trilogy.app.crm.support.ConvergedAccountSubscriberSupport;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.context.ContextAgentProxy;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.session.Session;
import com.trilogy.framework.xhome.web.search.SearchBorder;
import com.trilogy.framework.xlog.log.LogSupport;

import java.util.Collection;
import java.util.Iterator;

/**
 * 
 * @author daniel.lee@redknee.com
 *
 */

public class AccountSubscriberSearchByParentBANAgent
    extends ContextAgentProxy
    implements SQLJoinCreator
{
    public AccountSubscriberSearchByParentBANAgent()
    {
        super();
    }

    public AccountSubscriberSearchByParentBANAgent(ContextAgent agent)
    {
        super(agent);
    }

    public void execute(final Context ctx)
        throws AgentException
    {
        ConvergedAccountSubscriberSearch criteria  = (ConvergedAccountSubscriberSearch) SearchBorder.getCriteria(ctx);
        Account parentAccount = (Account)Session.getSession(ctx).get(Account.class);

        if(null != parentAccount)
        {
            Home subHome = (Home)ctx.get(AccountHome.class);
            try
            {
                Home conAcctSub = null;
                Collection c = subHome.select(new EQ(AccountXInfo.PARENT_BAN, parentAccount.getBAN()));
                Iterator i = c.iterator();
                while(i.hasNext())
                {
                    Account account = (Account)i.next();
                    conAcctSub = ConvergedAccountSubscriberSupport.mergeAcctSubHome(ctx, account, (Subscriber)null, conAcctSub, criteria.getType().getIndex());
                }

                if( conAcctSub != null)
                {
                    ctx.put(ConvergedAccountSubscriberHome.class, conAcctSub);
                }
                else
                {
                    ctx.put(ConvergedAccountSubscriberHome.class, new ConvergedAccountSubscriberTransientHome(ctx));
                }
            }
            catch(Exception e)
            {
                LogSupport.major(ctx, this, "Problem searching account by Parent BAN.", e);
            }
        }

        delegate(ctx);
    }

    public String getSqlJoinClause(Context ctx, int searchTypeIndex)
    {
        Account parentAccount = (Account)Session.getSession(ctx).get(Account.class);
        StringBuilder sqlClause = new StringBuilder();

        if(null != parentAccount)
        {
            if (searchTypeIndex == 0)
            {
                    sqlClause.append("PARENTBAN = '").append(parentAccount.getBAN()).append("'"); 
            }
            else
            {
                sqlClause.append("BAN IN (SELECT BAN FROM ACCOUNT WHERE PARENTBAN = '").append(parentAccount.getBAN()).append("')"); 
            }
        }

        if(LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, this, "Converged Search SQL = " + sqlClause.toString());
        }
        
        return sqlClause.toString();
    }

    private static final long serialVersionUID = 8943109859049151595L;
}