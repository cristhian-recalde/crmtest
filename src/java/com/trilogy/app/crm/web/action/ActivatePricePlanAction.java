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
package com.trilogy.app.crm.web.action;

import java.io.PrintWriter;
import java.util.Collection;

import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.support.IdentitySupport;
import com.trilogy.framework.xhome.web.action.SimpleWebAction;
import com.trilogy.framework.xhome.web.agent.WebAgents;
import com.trilogy.framework.xhome.web.util.Link;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.PricePlanHome;
import com.trilogy.app.crm.bean.PricePlanStateEnum;
import com.trilogy.app.crm.bean.PricePlanXInfo;

/**
 * Web action to activate a price plan.
 *
 * @author abajoria
 */
public class ActivatePricePlanAction extends SimpleWebAction 
{
	public ActivatePricePlanAction()
	{
		super("activatePricePlan", "Activate");
	}
	
    public ActivatePricePlanAction(String key, String label)
    {
        super(key, label);
        defaultHelpText_ = "Activates the current price plan.";
    }
    
    public void execute(Context ctx) throws AgentException 
    {
    	String action = WebAgents.getParameter(ctx, "action");
        String key = WebAgents.getParameter(ctx, "key");
        
        if (key != null && getKey().equals(action))
        {
        	Home pricePlanHome = (Home) ctx.get(PricePlanHome.class);
        	try 
    		{
	        	PricePlan bean = (PricePlan)pricePlanHome.find(ctx, getIdentitySupport(ctx).fromStringID(key));
	        	bean.setState(PricePlanStateEnum.ACTIVE);
	        	pricePlanHome.store(bean);
                // return to detail view
                Link link = new Link(ctx);
                link.add("cmd", WebAgents.getParameter(ctx, "cmd"));
                link.add("key", String.valueOf(bean.getId()));
                WebAgents.service(ctx.createSubContext(), link.write(), WebAgents.getWriter(ctx));
            }
        	catch (HomeException he) 
    		{
    			printError(WebAgents.getWriter(ctx), he.getMessage());
				new MajorLogMsg(this, "Exception while grandfathering priceplan", he).log(ctx);
			}
            catch (Exception ex)
            {
            	printError(WebAgents.getWriter(ctx), "Internal error occured." + ex.getMessage());
                new MinorLogMsg(this, "Exception hwile activating price plan [" + key + "]", ex).log(ctx);
            }
        }
        else
        {
        	super.execute(ctx);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isEnabled(Context ctx, Object bean)
    {
    	if(ctx.getBoolean("TABLE_MODE"))
    	{
    		return false;
    	}
    	PricePlan pricePlan = (PricePlan) bean;
    	if(PricePlanStateEnum.GRANDFATHERED.equals(pricePlan.getState()))
    	{
	    	Home pricePlanHome = (Home) ctx.get(PricePlanHome.class);
	    	try 
	    	{
				Collection<PricePlan> pricePlanColl = pricePlanHome.select(new EQ(PricePlanXInfo.GRANDFATHER_PPID, pricePlan.getId()));
				if(pricePlanColl != null && pricePlanColl.size() > 0)
				{
					return false;
				}
				else
				{
					return true;
				}
			} 
	    	catch (HomeException e) 
	    	{
				new MinorLogMsg(this, "Exception while checking if the price plan[" + pricePlan.getId() + "] " +
						"is grandafther of some price plan.", e).log(ctx);
			}
    	}
    	
    	return false;
    }
    
    public void printMessage(PrintWriter out, String msg)
    {
        out.println("<table class=\"center\"><tbody><tr><td><font color=\"green\">" + msg + "</font></td></tr></tbody></table>");
    }


    public void printError(PrintWriter out, String error)
    {
        out.println("<table class=\"center\"><tbody><tr><td><font color=\"red\">" + error + "</font></td></tr></tbody></table>");
    }
    
    private IdentitySupport getIdentitySupport(Context ctx)
    {
    	return XBeans.getInstanceOf(ctx, PricePlan.class, IdentitySupport.class);
    }
}
