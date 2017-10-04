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

import com.trilogy.app.crm.bean.OcgAdj2CrmAdj;
import com.trilogy.app.crm.bean.OcgAdj2CrmAdjSearch;
import com.trilogy.app.crm.bean.OcgAdj2CrmAdjSearchWebControl;
import com.trilogy.app.crm.bean.OcgAdj2CrmAdjXInfo;

/**
 * Provides search functionality for the OCG Adjustment Mapping.
 * 
 * @author candy
 */
public class OcgAdjMappingSearchBorder extends SearchBorder
{
    /**
     * Creates a new OcgAdjMappingSearchBorder.
     * 
     * @param context
     *            The operating context.
     */
    public OcgAdjMappingSearchBorder(Context context)
    {
        super(context, OcgAdj2CrmAdj.class, new OcgAdj2CrmAdjSearchWebControl());

        addAgent(new ContextAgentProxy()
        {
            public void execute(Context ctx) throws AgentException
            {
                OcgAdj2CrmAdjSearch criteria = (OcgAdj2CrmAdjSearch) getCriteria(ctx);

                if (criteria.getSpid() != -1)
                {
                    doSelect(ctx, new EQ(OcgAdj2CrmAdjXInfo.SPID, Integer.valueOf(criteria.getSpid())));
                }
                delegate(ctx);
            }
        });

        addAgent(new ContextAgentProxy()
        {
            public void execute(Context ctx) throws AgentException
            {
                OcgAdj2CrmAdjSearch criteria = (OcgAdj2CrmAdjSearch) getCriteria(ctx);

                if (!criteria.getOcgAdjId().equals(""))
                {

                    doSelect(ctx, new EQ(OcgAdj2CrmAdjXInfo.OCG_ADJ_ID, criteria.getOcgAdjId()));

                }

                delegate(ctx);
            }
        });

    }
} // class
