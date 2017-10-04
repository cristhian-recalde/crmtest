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
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.web.border;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.bean.*;
import com.trilogy.app.crm.support.LicensingSupportHelper;

import com.trilogy.framework.xhome.context.*;
import com.trilogy.framework.xhome.web.search.*;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;
import com.trilogy.framework.xhome.webcontrol.ViewModeEnum;
import com.trilogy.framework.xhome.elang.EQ;


/**
 * This class creates a Border for searching Charging Type.
 *
 * Add this Border before the WebController, not as one of either its
 * Summary or Detail borders.
 *
 * @author candy
 */
public class ChargingTypeSearchBorder
   extends SearchBorder
{

   public ChargingTypeSearchBorder(Context context)
   {
      super(context, ChargingType.class, new ChargingTypeSearchWebControl());   
            
      addAgent(new ContextAgentProxy()
            {
               public void execute(Context ctx)
                  throws AgentException
               {
                  ChargingTypeSearch criteria = (ChargingTypeSearch)getCriteria(ctx);
                  if (criteria.getChargingTypeID() != -1 &&
                      null != doFind(ctx, new EQ(ChargingTypeXInfo.CHARGING_TYPE_ID, Long.valueOf(criteria.getChargingTypeID()))))
                  {
                     ContextAgents.doReturn(ctx);
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
                  ChargingTypeSearch criteria = (ChargingTypeSearch)getCriteria(ctx);
         	     if ( LicensingSupportHelper.get(ctx).isLicensed(ctx, LicenseConstants.PREPAID_LICENSE_KEY  ) && LicensingSupportHelper.get(ctx).isLicensed(ctx, LicenseConstants.POSTPAID_LICENSE_KEY )){    

         	    	 if (criteria.getSubscriberType() != null)
                  	{
                	  doSelect(
                        ctx,
                        new EQ(ChargingTypeXInfo.SUBSCRIBER_TYPE, criteria.getSubscriberType()));
                  	}            
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
                  ChargingTypeSearch criteria = (ChargingTypeSearch)getCriteria(ctx);
                  if (criteria.getSpid() != -1)
                  {
                     doSelect(
                        ctx,
                        new EQ(ChargingTypeXInfo.SPID, Integer.valueOf(criteria.getSpid())));
                  }
                  delegate(ctx);
               }
            }
      );
   }
   
   public void service(Context ctx, HttpServletRequest req, HttpServletResponse res, final RequestServicer delegate)
   throws ServletException, IOException
   {
	     if ( !LicensingSupportHelper.get(ctx).isLicensed(ctx, LicenseConstants.PREPAID_LICENSE_KEY  ) || !LicensingSupportHelper.get(ctx).isLicensed(ctx, LicenseConstants.POSTPAID_LICENSE_KEY )){    

	    	  AbstractWebControl.setMode(ctx, ChargingTypeSearchXInfo.SUBSCRIBER_TYPE, ViewModeEnum.NONE);
	      } 
	     super.service(ctx, req, res, delegate); 
  }
   
   
}
