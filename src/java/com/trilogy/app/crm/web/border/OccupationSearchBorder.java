/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee, no unauthorised use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the licence agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.web.border;

import com.trilogy.framework.xhome.web.search.SearchBorder;
import com.trilogy.framework.xhome.web.search.WildcardSelectSearchAgent;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgentProxy;
import com.trilogy.framework.xhome.context.ContextAgents;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.app.crm.bean.Occupation;
import com.trilogy.app.crm.bean.OccupationSearchXInfo;
import com.trilogy.app.crm.bean.OccupationXInfo;
import com.trilogy.app.crm.bean.OccupationSearch;
import com.trilogy.app.crm.bean.OccupationSearchWebControl;

/**
 * @author psperneac
 * @since May 1, 2005 10:59:03 PM
 */
public class OccupationSearchBorder extends SearchBorder
{
   public OccupationSearchBorder(Context ctx)
   {
      super(ctx,Occupation.class,new OccupationSearchWebControl());

      // id
      addAgent(new ContextAgentProxy()
            {
               public void execute(Context ctx)
                  throws AgentException
               {
                  OccupationSearch criteria = (OccupationSearch)getCriteria(ctx);

                  if (criteria.getId() != -1 &&
                      null != doFind(ctx, new EQ(OccupationXInfo.ID, Integer.valueOf(criteria.getId()))))
                  {
                     ContextAgents.doReturn(ctx);
                     return;
                  }
                  delegate(ctx);
               }
            }
      );

      // name
      addAgent(new WildcardSelectSearchAgent(OccupationXInfo.NAME, OccupationSearchXInfo.NAME, true));
   }
}
