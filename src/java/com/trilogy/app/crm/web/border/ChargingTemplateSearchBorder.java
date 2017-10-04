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
 */package com.trilogy.app.crm.web.border;

import com.trilogy.app.crm.bean.ChargingTemplate;
import com.trilogy.app.crm.bean.ChargingTemplateSearch;
import com.trilogy.app.crm.bean.ChargingTemplateSearchWebControl;
import com.trilogy.app.crm.bean.ChargingTemplateSearchXInfo;
import com.trilogy.app.crm.bean.ChargingTemplateXInfo;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgentProxy;
import com.trilogy.framework.xhome.context.ContextAgents;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.web.search.SearchBorder;
import com.trilogy.framework.xhome.web.search.WildcardSelectSearchAgent;

/**
 * Search border used in the charging template menu.
 * @author Marcio Marques
 * @since 8.5
 *
 */
public class ChargingTemplateSearchBorder extends SearchBorder
{
    public ChargingTemplateSearchBorder(Context context)
    {
       super(context,ChargingTemplate.class,new ChargingTemplateSearchWebControl());

       addAgent(new ContextAgentProxy()
             {
                @Override
             public void execute(Context ctx)
                   throws AgentException
                {
                    ChargingTemplateSearch criteria = (ChargingTemplateSearch)getCriteria(ctx);

                   if (criteria.getIdentifier() != -1 &&
                       null != doFind(ctx, new EQ(ChargingTemplateXInfo.IDENTIFIER, Integer.valueOf(criteria.getIdentifier()))))
                   {
                      ContextAgents.doReturn(ctx);
                   }
                   delegate(ctx);
                }
             }
       );

       addAgent(new ContextAgentProxy()
             {
                @Override
             public void execute(Context ctx)
                   throws AgentException
                {
                    ChargingTemplateSearch criteria = (ChargingTemplateSearch)getCriteria(ctx);

                   if (criteria.getSpid() != -1)
                   {
                      doSelect(
                         ctx, 
                         new EQ(ChargingTemplateXInfo.SPID, Integer.valueOf(criteria.getSpid())));
                   }
                   delegate(ctx);
                }
             }
       );

       addAgent(new ContextAgentProxy()
             {
                @Override
             public void execute(Context ctx)
                   throws AgentException
                {
                    ChargingTemplateSearch criteria = (ChargingTemplateSearch)getCriteria(ctx);

                   if (criteria.getSubscriberType() >= 0)
                   {
                      doSelect(
                         ctx, 
                         new EQ(ChargingTemplateXInfo.SUBSCRIBER_TYPE, criteria.getSubscriberType()));
                   }
                   delegate(ctx);
                }
             }
       );

       addAgent(new ContextAgentProxy()
       {
          @Override
       public void execute(Context ctx)
             throws AgentException
             
          {
              
              ChargingTemplateSearch criteria = (ChargingTemplateSearch)getCriteria(ctx);

             if (criteria.getSubscriptionType() > 0)
             {
                doSelect(
                   ctx, 
                   new EQ(ChargingTemplateXInfo.SUBSCRIPTION_TYPE, criteria.getSubscriptionType()));
             }
             delegate(ctx);
          }
       }
       );

       addAgent(new WildcardSelectSearchAgent(ChargingTemplateXInfo.NAME, ChargingTemplateSearchXInfo.NAME, true));
    }
 }
