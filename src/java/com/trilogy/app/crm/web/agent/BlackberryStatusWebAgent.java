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
package com.trilogy.app.crm.web.agent;

import java.io.PrintWriter;
import java.util.List;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.blackberry.BlackberrySupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.session.Session;
import com.trilogy.framework.xhome.web.agent.WebAgent;
import com.trilogy.framework.xhome.web.agent.WebAgents;
import com.trilogy.framework.xhome.web.renderer.DefaultTableRenderer;
import com.trilogy.framework.xhome.web.renderer.DetailRenderer;
import com.trilogy.framework.xhome.web.renderer.TableRenderer;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.service.blackberry.IServiceBlackberry;
import com.trilogy.service.blackberry.ServiceBlackberryException;
import com.trilogy.service.blackberry.ServiceBlackberryFactory;
import com.trilogy.service.blackberry.model.Attributes;
import com.trilogy.service.blackberry.model.ResultEnum;
import com.trilogy.service.blackberry.model.ServiceMapping;
import com.trilogy.service.blackberry.model.ServiceStatus;
import com.trilogy.service.blackberry.model.Status;


/**
 * Blackberry Status WebAgent Report.
 * @author marcio.marques@redknee.com
 *
 */
public class BlackberryStatusWebAgent extends WebAgents implements WebAgent
{
 
    public BlackberryStatusWebAgent()
    {
    }
    
    /**
     * {@inheritDoc}
     */
    public void execute(Context ctx)
    {
       final PrintWriter   out = (PrintWriter) ctx.get(PrintWriter.class);
       final TableRenderer tableRenderer   = (TableRenderer) ctx.get(TableRenderer.class, DefaultTableRenderer.instance());
       DetailRenderer renderer = (DetailRenderer) ctx.get(DetailRenderer.class);
       final Context       ses = Session.getSession(ctx);
       final Subscriber    sub = (Subscriber) ses.get(Subscriber.class);

       try
       {
           if ( sub == null )
           {
              out.println("Please selected subscriber first.");
              return;
           }
           
    	   // Only writes to the screen if subscriber has Blackberry service.
           if ((BlackberrySupport.subscriberHasBlackberryService(ctx, sub.getId())))
           {
    	   
        	   IServiceBlackberry serviceBlackberry = ServiceBlackberryFactory.getServiceBlackberry(ctx, sub.getSpid());
             
               Home serviceMappingHome = ServiceBlackberryFactory.getServiceMapping(ctx);

               if (serviceBlackberry == null)
               {
                   throw new HomeException("Developer error: no IServiceBlackberry found in context.");
               }
               
               if (serviceMappingHome == null)
               {
                   throw new HomeException("Developer error: no ServiceMappingHome found in context.");
               }
               
               renderer.Table(ctx, out, "BlackBerry Status");

               renderer.TR(ctx,out, "Billing ID / IMSI");
               out.print(sub.getIMSI());
               renderer.TREnd(ctx,out);
               
               if (!BlackberrySupport.subscriberHasBeenActivated(sub))
               {
                   renderer.TR(ctx,out, "SIM Status");
                   out.print("Pending activation.");
                   renderer.TREnd(ctx,out);
                   renderer.TableEnd(ctx, out);
                   return;
               }
               
               try
               {
                   Attributes attributes = BlackberrySupport.getBlackberryAttributesBasicAttributes(ctx, sub);
                   Status status = serviceBlackberry.status(ctx, sub.getSpid(), attributes);
                   
                   if (status!=null)
                   {
                	   if (BlackberrySupport.isParamTrackingEnabled(ctx))
                       {
		                   renderer.TR(ctx,out, "Mobile Number");
		                   out.print(status.getAttributes().getMsisdn());
		                   renderer.TREnd(ctx,out);
                       }
	    
	                   renderer.TR(ctx,out, "SIM Status");
	                   out.print(status.getSubscriberStatus());
	                   renderer.TREnd(ctx,out);
	    
	                   renderer.TR(ctx,out, "Service Status");
	    
	                   tableRenderer.Table(ctx, out, "");
	                   
	                   List<ServiceStatus> servicesStatus = status.getServices();

	                   if (servicesStatus.size()==0)
	                   {
	                       tableRenderer.TR(ctx,out, status, 0);
	                       tableRenderer.TD(ctx,out);
	                       out.print("No services");
	                       tableRenderer.TDEnd(ctx,out);
	                       tableRenderer.TREnd(ctx,out);
	                   } else
	                   {
		                   for (int count = 0; count<servicesStatus.size(); count++)
		                   {
		                	   ServiceMapping serviceMapping = (ServiceMapping) serviceMappingHome.find(ctx, new Long (servicesStatus.get(count).getServiceId()));
		                	   tableRenderer.TR(ctx,out, status, count);
			                   tableRenderer.TD(ctx,out);
			                   out.print(serviceMapping.getDescription());
			                   tableRenderer.TDEnd(ctx,out);
			                   tableRenderer.TD(ctx,out);
			                   out.print(servicesStatus.get(count).getStatus());
			                   tableRenderer.TDEnd(ctx,out);
			                   tableRenderer.TREnd(ctx,out);
		                   }
	                   }

	                   renderer.TableEnd(ctx, out);
	                   
	                   renderer.TREnd(ctx,out);
                   }
                   
               } catch (ServiceBlackberryException e)
               {
            	   String description;
            	   if (e.getResultStatus().equals(ResultEnum.COMM_FAILURE))
            	   {
            		   description = "Status unavailable due to communication failure";
            	   } else if (e.getResultStatus().equals(ResultEnum.RIM_COMM_FAILURE) || 
            			   e.getResultStatus().equals(ResultEnum.RIM_PROVISION_FAILURE))
            	   {
            		   description = "Status unavailable: " + e.getDescription();
            	   } else
            	   {
            		   description = "Status unavailable due to unknown error";
            	   }
            	   new MinorLogMsg(this, "Internal Error in Blackberry status - " + description + ": " + e.getMessage(), e).log(ctx);
                   
                   renderer.TR(ctx,out, "SIM Status");
                   out.print(description);
                   renderer.TREnd(ctx,out);
               }
               catch (HomeException homeEx)
               {
                   new MinorLogMsg(this, "Internal Error in Blackberry status - : " + homeEx.getMessage(), homeEx).log(ctx);
                   
               }
               renderer.TableEnd(ctx,out);
           }
       }
       catch (HomeException e)
       {
           new MinorLogMsg(this, "Internal Error while verifying if subscriber has Blackberry service: " + e.getMessage(), e).log(ctx);
       }
    }
}