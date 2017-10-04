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

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgentProxy;
import com.trilogy.framework.xhome.context.ContextAgents;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.web.search.SearchBorder;
import com.trilogy.framework.xhome.web.search.WildcardSelectSearchAgent;

import com.trilogy.app.crm.bean.BearerType;
import com.trilogy.app.crm.bean.BearerTypeXInfo;
import com.trilogy.app.crm.bean.BearerTypeSearch;
import com.trilogy.app.crm.bean.BearerTypeSearchWebControl;
import com.trilogy.app.crm.bean.BearerTypeSearchXInfo;

/**
 * @author psperneac
 * @since May 1, 2005 9:25:02 PM
 */
public class BearerTypeSearchBorder extends SearchBorder
{
   public BearerTypeSearchBorder(Context ctx)
   {
      super(ctx,BearerType.class,new BearerTypeSearchWebControl());

      addAgent(new WildcardSelectSearchAgent(BearerTypeXInfo.ID, BearerTypeSearchXInfo.ID, true));

      addAgent(new WildcardSelectSearchAgent(BearerTypeXInfo.DESCRIPTION, BearerTypeSearchXInfo.DESCRIPTION, true));
   }
}
