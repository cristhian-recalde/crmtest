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
import com.trilogy.framework.xhome.web.search.WildcardSelectSearchAgent;

import com.trilogy.app.crm.bean.ClosedUserGroupTemplate;
import com.trilogy.app.crm.bean.ClosedUserGroupTemplateSearch;
import com.trilogy.app.crm.bean.ClosedUserGroupTemplateSearchWebControl;
import com.trilogy.app.crm.bean.ClosedUserGroupTemplateSearchXInfo;
import com.trilogy.app.crm.bean.ClosedUserGroupTemplateXInfo;


/**
 * Provides search functionality for the Closed User Groups.
 *
 * @author candy
 */
public class ClosedUserGroupTemplateSearchBorder
   extends SearchBorder
{
   /**
    * Creates a new ClosedUserGroupSearchTemplateBorder.
    *
    * @param context The operating context.
    */
   public ClosedUserGroupTemplateSearchBorder(Context context)
   {
      super(context, ClosedUserGroupTemplate.class, new ClosedUserGroupTemplateSearchWebControl());

      addAgent(new ContextAgentProxy()
            {
               public void execute(Context ctx)
                  throws AgentException
               {
                   ClosedUserGroupTemplateSearch criteria = (ClosedUserGroupTemplateSearch)getCriteria(ctx);
                  if (criteria.getID() != -1)
                  {
                      addSelect(ctx, new EQ(ClosedUserGroupTemplateXInfo.ID, Long.valueOf(criteria.getID())));
                  }
                  delegate(ctx);
               }
            }
      );

      addAgent(new WildcardSelectSearchAgent(ClosedUserGroupTemplateXInfo.NAME, ClosedUserGroupTemplateSearchXInfo.NAME, false));

      SelectSearchAgent spidAgent = new SelectSearchAgent(ClosedUserGroupTemplateXInfo.SPID,ClosedUserGroupTemplateSearchXInfo.SPID);
      addAgent(spidAgent.addIgnore(Integer.valueOf(-1)));

   }
} // class
