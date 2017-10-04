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
package com.trilogy.app.crm.bundle.web;

import java.io.PrintWriter;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.ui.BundleProfile;
import com.trilogy.app.crm.bundle.ActivationFeeCalculationEnum;
import com.trilogy.app.crm.bundle.ActivationTypeEnum;
import com.trilogy.app.crm.bundle.BundleSegmentEnum;
import com.trilogy.app.crm.bundle.BundleTypeEnum;
import com.trilogy.app.crm.bundle.ExpiryTypeEnum;
import com.trilogy.app.crm.bundle.QuotaTypeEnum;
import com.trilogy.app.crm.bundle.RecurrenceTypeEnum;
import com.trilogy.app.crm.defaultvalue.BooleanValue;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.framework.core.locale.Currency;
import com.trilogy.framework.core.locale.CurrencyHome;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.webcontrol.OutputWebControl;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;


/**
 * This class sets the Currency in the context based on the SPID of the Bundle.
 *
 * @author larry.xia@redknee.com
 * @author victor.stratan@redknee.com
 * @author karen.lin@redknee.com
 */
public class BundleProfileWebControlProxy extends ProxyWebControl
{

    /**
     * Constructor.
     * @param delegate The control delegate.
     */
    public BundleProfileWebControlProxy(final WebControl delegate)
    {
        super(delegate);
    }

    /**
     * Put the correct Currency in the context, based on the Service Provider configuration, before rendering property.
     *
     * {@inheritDoc}
     */
    @Override
    public void toWeb(final Context ctx, final PrintWriter out, final String name, final Object obj)
    {
        final Context subCtx = ctx.createSubContext();

        final BundleProfile profile = (BundleProfile) obj;
        try
        {
            final CRMSpid crmSpid = SpidSupport.getCRMSpid(ctx, profile.getSpid());
            if (crmSpid != null)
            {
                final Home home = (Home) ctx.get(CurrencyHome.class);
                final Object cur = home.find(crmSpid.getCurrency());
                subCtx.put(Currency.class, cur);
                
                subCtx.put("useSIunit", !crmSpid.isUseIECunits()); 
            }
            // there is no else condition since obviously the spid is not selected yet,
            // so we don't care what Currency will be shown. When a SPID will be selected
            // the correct currency will be used.
        }
        catch (HomeException e)
        {
            new MinorLogMsg(this, "CRMSpid home error", e).log(ctx);
        }
        
        if ( ctx.getInt("MODE") == OutputWebControl.EDIT_MODE || ctx.getInt("MODE") == OutputWebControl.CREATE_MODE)
        {
            out.print("<input type=\"hidden\" name=\"" + "crm.bundleprofile.spid" +"\" value=\""+(String.valueOf(profile.getSpid()).replaceAll("\"", "&quot;"))+"\" />");
        }
        
        super.toWeb(subCtx, out, name, obj);
    }
    
    
    /**
     * This method grabs the BundleProfileApi returned from the web
     * and set must set the hidden Activation Scheme and Expiry Scheme,
     * and Activation Calculation Fee  approriately
     * for one time bundle only
     */
       @Override
    public Object fromWeb(Context ctx, javax.servlet.ServletRequest req, String name)
    {
		String profileSpid = req.getParameter("crm.bundleprofile.spid");
		if (profileSpid != null) 
		{
			try 
			{
				final int spid = Integer.parseInt(profileSpid);
				final CRMSpid crmSpid = SpidSupport.getCRMSpid(ctx, spid);
				if (crmSpid != null) 
				{
					ctx.put("useSIunit", !crmSpid.isUseIECunits());
				}
			} 
			catch (Throwable e) 
			{
				new MinorLogMsg(this, "CRMSpid home error", e).log(ctx);
			}
		}

		BundleProfile bean = (BundleProfile) super.fromWeb(ctx, req, name);

		bean = (BundleProfile) super.fromWeb(ctx, req, name);

		if (bean.getType() == BundleTypeEnum.POINTS_INDEX) 
		{
			bean.setQuotaScheme(QuotaTypeEnum.MOVING_QUOTA);
			bean.setSegment(BundleSegmentEnum.HYBRID);
			bean.setRecurrenceScheme(RecurrenceTypeEnum.ONE_OFF_FIXED_INTERVAL);
		} 
		else 
		{
			// if values of these fields hold Point Bundles values that normaly
			// cannot be selected
			// set other values that make sence
			if (bean.getSegment() == BundleSegmentEnum.HYBRID) 
			{
				bean.setSegment(BundleSegmentEnum.PREPAID);
			}
		}

		if (bean.getRecurrenceScheme().isOneTime()) 
		{
			/**
			 * if this is a one time bundle (fixed interval+fixed date range)
			 * then, the Activiation Calculation Fee needs to be set to full,
			 * NOT prorated!!!!
			 */
			if (bean.getRecurrenceScheme().isOneTime()) 
			{
				bean.setActivationFeeCalculation(ActivationFeeCalculationEnum.FULL);
				if (bean.getRecurrenceScheme().equals(RecurrenceTypeEnum.ONE_OFF_FIXED_DATE_RANGE)) 
				{
					bean.setActivationScheme(ActivationTypeEnum.SCHEDULED_ACTIVATION);
				}
			} 
			else 
			{
				// reset the fields
				bean.setActivationFeeCalculation(BundleProfile.DEFAULT_ACTIVATIONFEECALCULATION);
				bean.setActivationScheme(BundleProfile.DEFAULT_ACTIVATIONSCHEME);
			}
			
			boolean secondaryBalance = BooleanValue.FALSE;
			try
			{
				secondaryBalance = bean.isSecondaryBalance(ctx);
				
			}
			catch(HomeException e)
			{
				LogSupport.major(ctx, this, "Error finding if bundle profile is secondary balance or not : ", e);
				secondaryBalance = BooleanValue.FALSE;
			}
			
			if (!secondaryBalance && bean.getEndDate() != null || (bean.getRecurrenceScheme().isInterval() && bean.getValidity() > 0)) 
			{
				bean.setExpiryScheme(ExpiryTypeEnum.EXPIRE_AND_DELAY_PURGE);
			}
			else 
			{
				// reset the fields
				bean.setActivationScheme(BundleProfile.DEFAULT_ACTIVATIONSCHEME);
				bean.setExpiryScheme(BundleProfile.DEFAULT_EXPIRYSCHEME);
				bean.setActivationFeeCalculation(BundleProfile.DEFAULT_ACTIVATIONFEECALCULATION);
			}
		}

		return bean;
	}
       

	public void fromWeb(Context ctx, Object bean, javax.servlet.ServletRequest req, String name) 
	{
		String profileSpid = req.getParameter("crm.bundleprofile.spid");
		if (profileSpid != null) 
		{
			try 
			{
				final int spid = Integer.parseInt(profileSpid);
				final CRMSpid crmSpid = SpidSupport.getCRMSpid(ctx, spid);
				if (crmSpid != null) 
				{
					ctx.put("useSIunit", !crmSpid.isUseIECunits());
				}
			} 
			catch (Throwable e) 
			{
				new MinorLogMsg(this, "CRMSpid home error", e).log(ctx);
			}
		}
		
		super.fromWeb(ctx, bean, req, name);
	}
}
