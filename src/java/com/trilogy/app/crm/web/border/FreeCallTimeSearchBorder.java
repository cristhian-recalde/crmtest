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
import com.trilogy.framework.xhome.web.search.WildcardSelectSearchAgent;

import com.trilogy.app.crm.bean.FreeCallTime;
import com.trilogy.app.crm.bean.FreeCallTimeSearchXInfo;
import com.trilogy.app.crm.bean.FreeCallTimeXInfo;
import com.trilogy.app.crm.bean.FreeCallTimeSearch;
import com.trilogy.app.crm.bean.FreeCallTimeSearchWebControl;


/**
 * Provides a custom search border for the FreeCallTimeScreen that allows
 * templates to be searched by identifier, name, and SPID.
 *
 * @author gary.anderson@redknee.com
 */
public
class FreeCallTimeSearchBorder
    extends SearchBorder
{
    /**
     * Creates a new search border.
     *
     * @param context The operating context.
     */
    public FreeCallTimeSearchBorder(Context context)
    {
        super(context, FreeCallTime.class, new FreeCallTimeSearchWebControl());

        // Search agent for FreeCallTime.identifier
        addAgent(
            new ContextAgentProxy()
            {
                public void execute(Context context1)
                    throws AgentException
                {
                    FreeCallTimeSearch search = (FreeCallTimeSearch)getCriteria(context1);
                    if (search.getIdentifier() != -1)
                    {
                        addSelect(context1, new EQ(FreeCallTimeXInfo.IDENTIFIER, Long.valueOf(search.getIdentifier())));
                    }

                    delegate(context1);
                }
            });

        // Search agent for FreeCallTime.name
        addAgent(new WildcardSelectSearchAgent(FreeCallTimeXInfo.NAME, FreeCallTimeSearchXInfo.NAME, true));
        // Search agent for FreeCallTime.spid
        addAgent(
            new ContextAgentProxy()
            {
                public void execute(Context context1)
                    throws AgentException
                {
                    FreeCallTimeSearch search = (FreeCallTimeSearch)getCriteria(context1);

                    if (search.getSpid() != -1)
                    {
                       addSelect(context1, new EQ(FreeCallTimeXInfo.SPID, Integer.valueOf(search.getSpid())));
                    }
                    delegate(context1);
                }
            });
    }
} // class
