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

import com.trilogy.app.crm.bean.OICKMapping;
import com.trilogy.app.crm.bean.OICKMappingXInfo;
import com.trilogy.app.crm.bean.OickMappingSearch;
import com.trilogy.app.crm.bean.OickMappingSearchWebControl;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgentProxy;
import com.trilogy.framework.xhome.web.search.SearchBorder;
import com.trilogy.framework.xhome.elang.EQ;

/**
 * @author jchen
 */
public class OickMappingSearchBorder 
   extends SearchBorder
{
   public  OickMappingSearchBorder(final Context context)
   {
      super(context, OICKMapping.class, new OickMappingSearchWebControl());

      // Account BAN
      addAgent(new ContextAgentProxy()
            {
               public void execute(Context ctx)
                  throws AgentException
               {
                  OickMappingSearch criteria = (OickMappingSearch)getCriteria(ctx);
                  if (criteria.getIdentifier() != -1)
                  {
                     doSelect(
                        ctx,
                        new EQ(OICKMappingXInfo.OICKMAPPING_ID, Long.valueOf(criteria.getIdentifier())));
                  }
                  delegate(ctx);
               }
            }
      );
      addAgent(new ContextAgentProxy()
            {
               public void execute(Context ctx)
                  throws AgentException
               {
                  OickMappingSearch criteria = (OickMappingSearch)getCriteria(ctx);

                  if (criteria.getSpid() != -1)
                  {
                     doSelect(
                        ctx,
                        new EQ(OICKMappingXInfo.SPID, Integer.valueOf(criteria.getSpid())));
                  }
                  delegate(ctx);
               }
            }
      );
   }
}

