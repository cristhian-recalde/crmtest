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
package com.trilogy.app.crm.home.pipelineFactory;

import java.io.IOException;
import java.util.Comparator;

import com.trilogy.app.crm.bean.ChargingTemplateAdjType;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.support.StorageSupportHelper;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.AuditJournalHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.SortingHome;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * 
 * @author Marcio Marques
 * @since 8.5
 *
 */
public class ChargingTemplateAdjTypeHomePipelineFactory implements PipelineFactory
{

    /**
     * {@inheritDoc}
     */
    public Home createPipeline(final Context ctx, final Context serverCtx) throws HomeException, IOException,
            AgentException
    {
        LogSupport.info(ctx, this, "Installing the Charging template adjustment type mapping home ");
        Home home = StorageSupportHelper.get(ctx).createHome(ctx, ChargingTemplateAdjType.class, "CHARGINGTEMPLATEADJTYPE");
        home = new AuditJournalHome(ctx, home);
        home = new SortingHome(ctx, home, new Comparator()
        {
            public int compare(Object o1, Object o2)
            {
              if ( o1 instanceof ChargingTemplateAdjType )
              {
                  if (((ChargingTemplateAdjType) o1).getIdentifier() > ((ChargingTemplateAdjType) o2).getIdentifier())
                  {
                      return 1;
                  }
                  else if (((ChargingTemplateAdjType) o1).getIdentifier() < ((ChargingTemplateAdjType) o2).getIdentifier())
                  {
                      return -1;
                  }
                  else if (((ChargingTemplateAdjType) o1).getAdjustmentTypeId() > ((ChargingTemplateAdjType) o2).getAdjustmentTypeId())
                  {
                      return 1;
                  }
                  else if (((ChargingTemplateAdjType) o1).getAdjustmentTypeId() < ((ChargingTemplateAdjType) o2).getAdjustmentTypeId())
                  {
                      return -1;
                  }
                  else 
                  {
                      return 0;
                  }

              }

              return ((Comparable) XBeans.getIdentifier(o1)).compareTo(XBeans.getIdentifier(o2));
            }
        
        });
        LogSupport.info(ctx, this, "Charging Template Adjustment Type Mapping Home installed succesfully");
        return home;
    }
        
}
