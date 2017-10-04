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

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgentProxy;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.web.search.SearchBorder;
import com.trilogy.framework.xhome.web.search.SelectSearchAgent;

import com.trilogy.app.crm.bean.BirthdayPlan;
import com.trilogy.app.crm.bean.BirthdayPlanSearch;
import com.trilogy.app.crm.bean.BirthdayPlanSearchWebControl;
import com.trilogy.app.crm.bean.BirthdayPlanSearchXInfo;
import com.trilogy.app.crm.bean.BirthdayPlanXInfo;

/**
 * @author victor.stratan@redknee.com
 */
public class BirthdayPlanSearchBorder extends SearchBorder
{
    public BirthdayPlanSearchBorder(final Context context)
    {
        super(context, BirthdayPlan.class, new BirthdayPlanSearchWebControl());

        // id
        addAgent(new ContextAgentProxy()
        {
            public void execute(final Context ctx) throws AgentException
            {
                final BirthdayPlanSearch criteria = (BirthdayPlanSearch) getCriteria(ctx);

                if (criteria.getID() != -1)
                {
                    doSelect(ctx, new EQ(BirthdayPlanXInfo.ID, Long.valueOf(criteria.getID())));
                }
                delegate(ctx);
            }
        }
        );

        // name
        // TODO 2008-02-04 can this be reimplemented without using deprecated methods?
        addAgent(new SelectSearchAgent(BirthdayPlanXInfo.NAME,BirthdayPlanSearchXInfo.NAME));

        // spid
        addAgent(new ContextAgentProxy()
        {
            public void execute(final Context ctx) throws AgentException
            {
                final BirthdayPlanSearch criteria = (BirthdayPlanSearch) getCriteria(ctx);

                if (criteria.getSPID() != -1)
                {
                    doSelect(ctx, new EQ(BirthdayPlanXInfo.SPID, Integer.valueOf(criteria.getSPID())));
                }
                delegate(ctx);
            }
        }
        );

    }
}
