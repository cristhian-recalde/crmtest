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

import com.trilogy.app.crm.bas.tps.ServiceActivationTemplate;
import com.trilogy.app.crm.bean.ServiceActivationTemplateSearch;
import com.trilogy.app.crm.bean.ServiceActivationTemplateSearchWebControl;
import com.trilogy.app.crm.bean.ServiceActivationTemplateSearchXInfo;
import com.trilogy.app.crm.bas.tps.ServiceActivationTemplateXInfo;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgentProxy;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.web.search.SearchBorder;
import com.trilogy.framework.xhome.web.search.WildcardSelectSearchAgent;


/**
 * @author jke
 */
public class SubscriberCreationTemplateSearchBorder extends SearchBorder
{
        public SubscriberCreationTemplateSearchBorder(Context ctx)
        {
           super(ctx, ServiceActivationTemplate.class, new ServiceActivationTemplateSearchWebControl());


             // identifier
             addAgent(new ContextAgentProxy()
                   {
                      public void execute(Context ctx)
                         throws AgentException
                      {
                          ServiceActivationTemplateSearch criteria = (ServiceActivationTemplateSearch)getCriteria(ctx);

                         if (criteria.getIdentifier() > -1)
                         {
                            doSelect(
                               ctx,
                               new EQ(ServiceActivationTemplateXInfo.IDENTIFIER, Long.valueOf(criteria.getIdentifier())));
                         }
                         delegate(ctx);
                      }
                   }
             );

             // spid
             addAgent(new ContextAgentProxy()
                   {
                      public void execute(Context ctx)
                         throws AgentException
                      {
                          ServiceActivationTemplateSearch criteria = (ServiceActivationTemplateSearch)getCriteria(ctx);

                         if (criteria.getSpid() > -1)
                         {
                            doSelect(
                               ctx,
                               new EQ(ServiceActivationTemplateXInfo.SPID, Integer.valueOf(criteria.getSpid())));
                         }
                         delegate(ctx);
                      }
                   }
             );
        // name
        addAgent(new WildcardSelectSearchAgent(ServiceActivationTemplateXInfo.NAME,
                ServiceActivationTemplateSearchXInfo.NAME, true));
        }

}
