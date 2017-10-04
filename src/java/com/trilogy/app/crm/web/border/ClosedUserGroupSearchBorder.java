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
import com.trilogy.framework.xhome.context.ContextAgents;
import com.trilogy.framework.xhome.context.ContextAgentProxy;
import com.trilogy.framework.xhome.web.search.SearchBorder;
import com.trilogy.framework.xhome.web.search.WildcardSelectSearchAgent;
import com.trilogy.framework.xhome.elang.EQ;

import com.trilogy.app.crm.bean.ClosedUserGroup;
import com.trilogy.app.crm.bean.ClosedUserGroupSearchXInfo;
import com.trilogy.app.crm.bean.ClosedUserGroupXInfo;
import com.trilogy.app.crm.bean.ClosedUserGroupSearch;
import com.trilogy.app.crm.bean.ClosedUserGroupSearchWebControl;


/**
 * Provides search functionality for the Closed User Groups.
 *
 * @author candy
 */
public class ClosedUserGroupSearchBorder
   extends SearchBorder
{
   /**
    * Creates a new ClosedUserGroupSearchBorder.
    *
    * @param context The operating context.
    */
   public ClosedUserGroupSearchBorder(Context context)
   {
      super(context, ClosedUserGroup.class, new ClosedUserGroupSearchWebControl());

      addAgent(new ContextAgentProxy()
            {
               public void execute(Context ctx)
                  throws AgentException
               {
                  ClosedUserGroupSearch criteria = (ClosedUserGroupSearch)getCriteria(ctx);
                  if (criteria.getID() != -1)
                  {
                      addSelect(ctx, new EQ(ClosedUserGroupXInfo.ID, Long.valueOf(criteria.getID())));
                  }
                  delegate(ctx);
               }
            }
      );

      addAgent(new WildcardSelectSearchAgent(ClosedUserGroupXInfo.NAME, ClosedUserGroupSearchXInfo.TEMPLATE_NAME, false));
      
      addAgent(new ContextAgentProxy()
      {

          public void execute(Context ctx) throws AgentException
          {
              ClosedUserGroupSearch criteria = (ClosedUserGroupSearch) getCriteria(ctx);
              if (criteria.getSpid() != -1)
              {
                  doSelect(ctx, new EQ(ClosedUserGroupXInfo.SPID, Integer.valueOf(criteria.getSpid())));
              }
              delegate(ctx);
          }
      });

   }
} // class
