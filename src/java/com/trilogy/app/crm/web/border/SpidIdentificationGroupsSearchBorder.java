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

import com.trilogy.app.crm.bean.SpidIdentificationGroups;
import com.trilogy.app.crm.bean.SpidIdentificationGroupsSearch;
import com.trilogy.app.crm.bean.SpidIdentificationGroupsSearchWebControl;
import com.trilogy.app.crm.bean.SpidIdentificationGroupsXInfo;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgentProxy;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.web.search.SearchBorder;

/**
 * Spid identification groups search border.
 * 
 * @author marcio.marques@redknee.com
 *
 */
public class SpidIdentificationGroupsSearchBorder extends SearchBorder
{
    public SpidIdentificationGroupsSearchBorder(Context ctx)
    {
       super(ctx, SpidIdentificationGroups.class, new SpidIdentificationGroupsSearchWebControl());


       addAgent(new ContextAgentProxy() {
           public void execute(Context ctx)
              throws AgentException
           {
               SpidIdentificationGroupsSearch criteria = (SpidIdentificationGroupsSearch)getCriteria(ctx);
       
             if (criteria.getSpid() != -1)
              {
                SearchBorder.doSelect(
                   ctx, 
                   new EQ(SpidIdentificationGroupsXInfo.SPID, Integer.valueOf(criteria.getSpid())));
              }
              delegate(ctx);
           }
        });
    }
}
