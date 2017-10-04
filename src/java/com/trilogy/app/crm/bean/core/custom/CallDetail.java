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
package com.trilogy.app.crm.bean.core.custom;

import java.util.ArrayList;
import java.util.List;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.ChargedBundleInfo;
import com.trilogy.app.crm.bean.ChargedBundleInfoHome;
import com.trilogy.app.crm.bean.ChargedBundleInfoXInfo;
import com.trilogy.app.crm.bean.calldetail.AbstractCallDetail;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.SpidSupport;


/**
 * 
 *
 * @author aaron.gourley@redknee.com
 * @since 
 */
public class CallDetail extends com.redknee.app.crm.bean.calldetail.CallDetail
{
    @Override
    public String getComponentName1()
    {
        String componentName = DEFAULT_COMPONENTNAME1;
        Context ctx = ContextLocator.locate();
        if (null != ctx)
        {
            final int spid = this.getSpid();
            try
            {
                CRMSpid crmSpid = SpidSupport.getCRMSpid(ctx, spid);
                if (null != crmSpid)
                {
                    componentName = crmSpid.getChargingComponentsConfig().getComponentFirst().getName();
                }
            }
            catch (Throwable t)
            {
                new MinorLogMsg(this, "Error in fetching CRM SPID [" + spid + "]", t);
            }
        }
        return componentName;
    }

    @Override
    public String getComponentName2()
    {
        String componentName = DEFAULT_COMPONENTNAME2;
        Context ctx = ContextLocator.locate();
        if (null != ctx)
        {
            final int spid = this.getSpid();
            try
            {
                CRMSpid crmSpid = SpidSupport.getCRMSpid(ctx, spid);
                if (null != crmSpid)
                {
                    componentName = crmSpid.getChargingComponentsConfig().getComponentSecond().getName();
                }
            }
            catch (Throwable t)
            {
                new MinorLogMsg(this, "Error in fetching CRM SPID [" + spid + "]", t);
            }
        }
        return componentName;
    }

    @Override
    public String getComponentName3()
    {
        String componentName = DEFAULT_COMPONENTNAME3;
        Context ctx = ContextLocator.locate();
        if (null != ctx)
        {
            final int spid = this.getSpid();
            try
            {
                CRMSpid crmSpid = SpidSupport.getCRMSpid(ctx, spid);
                if (null != crmSpid)
                {
                    componentName = crmSpid.getChargingComponentsConfig().getComponentThird().getName();
                }
            }
            catch (Throwable t)
            {
                new MinorLogMsg(this, "Error in fetching CRM SPID [" + spid + "]", t);
            }
        }
        return componentName;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<ChargedBundleInfo> getChargedBundleInfoList() 
    {
    	synchronized (this)
		{
			if (!getChargedBundleInfoLoaded())
			{
				lazyLoadChargedBundleInfo();
			}
		}
		return super.getChargedBundleInfoList();
    }
    
    protected synchronized void lazyLoadChargedBundleInfo()
	{
    	Context ctx = ContextLocator.locate();
        if (null != ctx)
        {
	    	if(this.getId() == AbstractCallDetail.DEFAULT_ID)
	    	{
	    		LogSupport.info(ctx, this, "Call Detail ID has not been assigned yet. Charged Bundle Info can not be loaded now.");
	    		setChargedBundleInfoLoaded(Boolean.TRUE);
	    	}
	    	else if(ctx.get(ChargedBundleInfoHome.class) == null)
	    	{
	    		LogSupport.minor(ctx, this, "ChargedBundleInfoHome not yet available in context. " +
	    				"Charged Bundle Info for the call detail can not be loaded.");
	    		setChargedBundleInfoLoaded(Boolean.TRUE);
	    	}
	    	else
	    	{
	    		List <ChargedBundleInfo> chargedBundleInfoList = new ArrayList<ChargedBundleInfo>();
	    		try 
	        	{
	    			And and = new And();
	    			and.add(new EQ(ChargedBundleInfoXInfo.CALL_DETAIL_ID, this.getId()));
	    			and.add(new EQ(ChargedBundleInfoXInfo.TRANS_DATE, this.getTranDate()));
	    			
	        		chargedBundleInfoList = (List<ChargedBundleInfo>) HomeSupportHelper.get(ctx).getBeans(ctx, ChargedBundleInfo.class, and);
	        		this.setChargedBundleInfoList(chargedBundleInfoList);
	        		setChargedBundleInfoLoaded(Boolean.TRUE);
				}
	        	catch (HomeException e) 
	        	{
	        		LogSupport.minor(ctx, this, "Error in fetching Charged Bundle Information for"
	        				+ " this call detail with ID "+this.getId()+ " for Subscriber "+this.getSubscriberID());
				}
	    	}
        }
	}
    
}
