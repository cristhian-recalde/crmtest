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
package com.trilogy.app.crm.web.border;

import com.trilogy.app.crm.bean.OverdraftBalanceLimit;
import com.trilogy.app.crm.bean.OverdraftBalanceLimitSearch;
import com.trilogy.app.crm.bean.OverdraftBalanceLimitSearchWebControl;
import com.trilogy.app.crm.bean.OverdraftBalanceLimitSearchXInfo;
import com.trilogy.app.crm.bean.OverdraftBalanceLimitXInfo;
import com.trilogy.app.crm.web.search.SelectNumberSearchAgent;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgentProxy;
import com.trilogy.framework.xhome.context.ContextAgents;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.web.search.SearchBorder;

/**
 * Search border used in the overdraft balance
 * @author Marcio Marques
 * @since 9.1.1
 *
 */
public class OverdraftBalanceLimitSearchBorder extends SearchBorder
{
    public OverdraftBalanceLimitSearchBorder(Context context)
    {
       super(context,OverdraftBalanceLimit.class,new OverdraftBalanceLimitSearchWebControl());

        addAgent(new ContextAgentProxy()
        {
            @Override
            public void execute(Context ctx) throws AgentException
            {
                OverdraftBalanceLimitSearch criteria = (OverdraftBalanceLimitSearch) getCriteria(ctx);

                if (criteria.getId() > 0
                        && null != doFind(ctx, new EQ(OverdraftBalanceLimitXInfo.ID, Long.valueOf(criteria.getId()))))
                {
                    ContextAgents.doReturn(ctx);
                }
                delegate(ctx);
            }
        });
       
       addAgent((new SelectNumberSearchAgent(OverdraftBalanceLimitXInfo.SPID, OverdraftBalanceLimitSearchXInfo.SPID, Integer.valueOf(-1))));

       addAgent((new SelectNumberSearchAgent(OverdraftBalanceLimitXInfo.LIMIT, OverdraftBalanceLimitSearchXInfo.LIMIT, Long.valueOf(-1))));

    }
 }
