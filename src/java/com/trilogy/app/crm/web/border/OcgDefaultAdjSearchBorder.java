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

import com.trilogy.app.crm.bean.Spid2DefaultAdj;
import com.trilogy.app.crm.bean.Spid2DefaultAdjSearch;
import com.trilogy.app.crm.bean.Spid2DefaultAdjSearchWebControl;
import com.trilogy.app.crm.bean.Spid2DefaultAdjXInfo;

/**
 * Provides search functionality for the OCG Adjustment Mapping.
 * 
 * @author candy
 */
public class OcgDefaultAdjSearchBorder extends SearchBorder
{
    /**
     * Creates a new OcgDefaultAdjSearchBorder.
     * 
     * @param context
     *            The operating context.
     */
    public OcgDefaultAdjSearchBorder(Context context)
    {
        super(context, Spid2DefaultAdj.class, new Spid2DefaultAdjSearchWebControl());

        addAgent(new ContextAgentProxy()
        {
            public void execute(Context ctx) throws AgentException
            {
                Spid2DefaultAdjSearch criteria = (Spid2DefaultAdjSearch) getCriteria(ctx);

                if (criteria.getSpid() != -1)
                {
                    // ContextAgents.doReturn(ctx);
                    doSelect(ctx, new EQ(Spid2DefaultAdjXInfo.SPID, Integer.valueOf(criteria.getSpid())));
                }
                delegate(ctx);
            }
        });

    }
} // class
