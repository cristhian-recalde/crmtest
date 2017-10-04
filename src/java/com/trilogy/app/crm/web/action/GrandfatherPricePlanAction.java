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
import java.util.Date;
import java.util.Map;

import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.support.IdentitySupport;
import com.trilogy.framework.xhome.web.action.SimpleWebAction;
import com.trilogy.framework.xhome.web.agent.WebAgents;
import com.trilogy.framework.xhome.web.renderer.ButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultButtonRenderer;
import com.trilogy.framework.xhome.web.util.Link;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.PricePlanHome;
import com.trilogy.app.crm.bean.PricePlanStateEnum;
import com.trilogy.app.crm.bean.PricePlanVersionHome;
import com.trilogy.app.crm.bean.PricePlanVersionIdentitySupport;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.PricePlan;
import com.trilogy.app.crm.bean.core.PricePlanVersion;
import com.trilogy.app.crm.bean.core.ServicePackageVersion;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.PricePlanSupport;

/**
 * Web action to trigger grandfather process. 
 *
 * @author abajoria
 */
public class GrandfatherPricePlanAction extends SimpleWebAction
{
	public GrandfatherPricePlanAction()
	{
		super("grandfatherPricePlan", "Grandfather");
	}
	
    public GrandfatherPricePlanAction(String key, String label)
    {
        super(key, label);
        defaultHelpText_ = "Grandfather the current price plan.";
    }
    
    /**
     * {@inheritDoc}
     */
    public void execute(Context ctx) throws AgentException 
    {
    	String action = WebAgents.getParameter(ctx, "action");
        String key = WebAgents.getParameter(ctx, "key");
        boolean isClone = Boolean.valueOf(WebAgents.getParameter(ctx, "clone"));
        
        if (key != null && getKey().equals(action))
        {
        	Home pricePlanHome = (Home) ctx.get(PricePlanHome.class);
        	try 
    		{
	        	PricePlan bean = (PricePlan)pricePlanHome.find(ctx, getIdentitySupport(ctx).fromStringID(key));
	        	if(!isClone)
	        	{
	        		grandfatherWithoutClone(ctx, pricePlanHome, bean);
	        	}
	        	else
	        	{
	        		grandfatherWithClone(ctx, pricePlanHome, bean);
	        	}
    		} 
    		catch (HomeException he) 
    		{
    			printError(WebAgents.getWriter(ctx), he.getMessage());
				new MajorLogMsg(this, "Exception while grandfathering priceplan", he).log(ctx);
			}
        
        }
        else
        {
        	super.execute(ctx);
        }
    }
    
    private void grandfatherWithoutClone(Context ctx, Home pricePlanHome, PricePlan pricePlan) throws HomeException
    {
    	pricePlan.setState(PricePlanStateEnum.GRANDFATHERED);
		pricePlanHome.store(pricePlan);
		printMessage(WebAgents.getWriter(ctx), "Price Plan [" + pricePlan.getId() + "] has been marked as Grandfathered.");
		try
        {
            // return to detail view
            Link link = new Link(ctx);
            link.add("cmd", WebAgents.getParameter(ctx, "cmd"));
            link.add("key", String.valueOf(pricePlan.getId()));
            WebAgents.service(ctx.createSubContext(), link.write(), WebAgents.getWriter(ctx));
        }
        catch (Exception ex)
        {
        	printError(WebAgents.getWriter(ctx), "Grandfathering is done. But, error occured while routing GUI to correct page." + ex.getMessage());
            new MinorLogMsg(this, "Exception while routing GUI to correct page.", ex).log(ctx);
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
    	return PricePlanStateEnum.ACTIVE.equals(pricePlan.getState());
    }
    
    private void grandfatherWithClone(Context ctx, Home pricePlanHome, PricePlan oldPricePlan) throws HomeException
    {
    	try 
		{
			PricePlan clonedPricePlan = (PricePlan) oldPricePlan.deepClone();
			setDefaultValuesForClonedPricePlan(clonedPricePlan);
			clonedPricePlan.setGrandfatherPPId(oldPricePlan.getId());
			clonedPricePlan.setState(PricePlanStateEnum.PENDING_ACTIAVTION);
			clonedPricePlan = (PricePlan) pricePlanHome.create(clonedPricePlan);
			PricePlanVersion ppVersion = PricePlanSupport.getCurrentVersion(ctx, oldPricePlan.getId());
			if(ppVersion == null)
			{
				ppVersion = PricePlanSupport.findHighestVersion(ctx, oldPricePlan);
			}
			if(ppVersion != null)
			{
				ppVersion = (PricePlanVersion) ppVersion.deepClone();
				ppVersion.setId(clonedPricePlan.getId());
				setDefaultValuesForClonedPricePlanVersion(ctx, ppVersion);
				Home ppVersionHome = (Home) ctx.get(PricePlanVersionHome.class);
				ppVersionHome.create(ppVersion);
			}
			else
			{
				new InfoLogMsg(this, "Grandfathered price plan does not have any version").log(ctx);
			}
			printMessage(WebAgents.getWriter(ctx), 
					"New price plan [" + clonedPricePlan.getId() + "] is created successfully." +
							" Older price plan [" + oldPricePlan.getId() +"] will be marked Grandfathered on new Price Plan version's activation.");
			printCaution(WebAgents.getWriter(ctx), "<b>Caution</b>: Please make sure to change the version activation date <b>as required</b>. By default, it is set to system's current date.");
			try
            {
                // return to detail view
                Link link = new Link(ctx);
                link.add("cmd", WebAgents.getParameter(ctx, "cmd"));
                link.add("key", String.valueOf(clonedPricePlan.getId()));
                if(ppVersion != null)
                {
                	//FIXME: Here 'versions' name should come from propertyInfo. But, current framework does not provide the ability for relationship properties.
                    link.add(".versionsaction", "edit");
                    link.add(".versionskey", PricePlanVersionIdentitySupport.instance().toStringID(ppVersion.ID()));
                }
                WebAgents.service(ctx.createSubContext(), link.write(), WebAgents.getWriter(ctx));
            }
            catch (Exception ex)
            {
            	printError(WebAgents.getWriter(ctx), "Grandfathering is done. But, error occured while routing GUI to correct page." + ex.getMessage());
                new MinorLogMsg(this, "Exception while routing GUI to correct page.", ex).log(ctx);
            }
			
		} 
		catch (CloneNotSupportedException cse) 
		{
			printError(WebAgents.getWriter(ctx), cse.getMessage());
			new MajorLogMsg(this, "Exception while grandfathering priceplan", cse).log(ctx);
		}
    }
    
    private void setDefaultValuesForClonedPricePlanVersion(Context ctx, PricePlanVersion ppVersion)
    {
    	ppVersion.setVersion(PricePlanVersion.DEFAULT_VERSION);
    	ppVersion.setCreatedDate(new Date());
		//this is set to null because BSS sets it on activation of a version.
		ppVersion.setActivation(null);
		//this is set to today's date by default. This can be modified by the Admin on BSS GUI.
		ppVersion.setActivateDate(CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(new Date()));
		ppVersion.setCharge(PricePlanVersion.DEFAULT_CHARGE);
		//set BundleFee to default start and end date. 
		ServicePackageVersion servicePackageVersion = ppVersion.getServicePackageVersion(ctx);
		if(servicePackageVersion != null)
		{
			servicePackageVersion.setCreatedDate(new Date());
			Map<Long, BundleFee> bundleFeeMap = servicePackageVersion.getBundleFees();
			if(bundleFeeMap != null && bundleFeeMap.size() > 0)
			{
				Collection<BundleFee> bundleFeeColl = bundleFeeMap.values();
				BundleFee defaultBundleFee = new BundleFee();
				for(BundleFee bundleeFee : bundleFeeColl)
				{
					bundleeFee.setStartDate(defaultBundleFee.getStartDate());
					bundleeFee.setEndDate(defaultBundleFee.getEndDate());
				}
			}
		}
    }
    
    private void setDefaultValuesForClonedPricePlan(PricePlan pricePlan)
    {
    	pricePlan.setId(PricePlan.DEFAULT_ID);
    	pricePlan.setCurrentVersion(PricePlan.DEFAULT_CURRENTVERSION);
    	pricePlan.setNextVersion(PricePlan.DEFAULT_NEXTVERSION);
    	pricePlan.setCurrentVersionCharge(PricePlan.DEFAULT_CURRENTVERSIONCHARGE);
    	pricePlan.setCurrentVersionChargeCycle(PricePlan.DEFAULT_CURRENTVERSIONCHARGECYCLE);
    }
    
    private IdentitySupport getIdentitySupport(Context ctx)
    {
    	return XBeans.getInstanceOf(ctx, PricePlan.class, IdentitySupport.class);
    }
    
    public void printMessage(PrintWriter out, String msg)
    {
        out.println("<table class=\"center\"><tbody><tr><td><font color=\"green\">" + msg + "</font></td></tr></tbody></table>");
    }
    
    public void printCaution(PrintWriter out, String msg)
    {
        out.println("<table class=\"center\"><tbody><tr><td><font color=\"#FE642E\">" + msg + "</font></td></tr></tbody></table>");
    }


    public void printError(PrintWriter out, String error)
    {
        out.println("<table class=\"center\"><tbody><tr><td><font color=\"red\">" + error + "</font></td></tr></tbody></table>");
    }

    public void writeLinkDetail(Context ctx, PrintWriter out, Object bean, Link link)
    {
        link = modifyLink(ctx, bean, link);
        //add JS and CSS for Grandfather to show dialog-box.
    	out.println("<script language=\"javascript\" src=\"javascript/grandfather/grandfather.js\" type=\"text/javascript\"></script>");
    	out.println("<link rel=\"stylesheet\" href=\"javascript/jquery/jquery-ui-1.7.2.custom.css\" media=\"screen\" type=\"text/css\">");
    	out.println("<link rel=\"stylesheet\" href=\"javascript/grandfather/grandfather.css\" media=\"screen\" type=\"text/css\">");
    	
        MessageMgr mmgr = new MessageMgr(ctx, this);
        ButtonRenderer br = (ButtonRenderer) ctx.get(ButtonRenderer.class, DefaultButtonRenderer.instance());

        out.print("<a href=\"");
        link.write(out);
        out.print("\" onclick=\"return confirmGrandfatherPPCloning(jQuery(this).attr('href'));\">");
        out.print(mmgr.get("WebAction." + getKey() + ".DetailLabel", br.getButton(ctx, getKey(), mmgr.get("WebAction." + getKey() + ".Label", getLabel()))));
        out.print("</a>");
    }
    
}
