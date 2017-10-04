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
 * Copyright &copy; Redknee Inc. and its subsidiaries. All Rights Reserved.
 *
 */
package com.trilogy.app.crm.bean.webcontrol;

import java.io.PrintWriter;

import javax.servlet.ServletRequest;

import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.ui.BundleProfile;
import com.trilogy.app.crm.bean.ui.BundleProfileXInfo;
import com.trilogy.app.crm.bundle.BundleCategoryAssociation;
import com.trilogy.app.crm.bundle.BundleCategoryAssociationXInfo;
import com.trilogy.app.crm.bundle.DurationTypeEnum;
import com.trilogy.app.crm.bundle.ExpiryTypeEnum;
import com.trilogy.app.crm.bundle.FlexTypeEnum;
import com.trilogy.app.crm.bundle.RecurrenceTypeEnum;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.ViewModeEnum;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * @author cindy.wong@redknee.com
 * @since 2010-09-22
 */
public class BundleProfileViewCustomizationWebControl extends ProxyWebControl
{
    public BundleProfileViewCustomizationWebControl(WebControl delegate)
    {
        super(delegate);
    }

    @Override
	public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
	{
		com.redknee.app.crm.bean.ui.BundleProfile profile =
		    (com.redknee.app.crm.bean.ui.BundleProfile) obj;
		Context subCtx = ctx.createSubContext();

		setBundleIdViewMode(subCtx, profile);
		setGroupBundleIdViewMode(subCtx, profile);
		setAuxiliaryFieldsViewMode(subCtx, profile);
		setRecurringFieldsViewMode(subCtx, profile);
        setCrossUnitFieldsViewMode(subCtx, profile);
		setLimitedQuotaFieldsViewMode(subCtx, profile);
		setRollOverFieldsViewMode(subCtx, profile);
		setValidityFieldsViewMode(subCtx, profile);
		setRecurringValidityFieldsViewMode(subCtx, profile);
		setRelativeValidityFieldsViewMode(subCtx, profile);
		setOneOffDateRangeFieldsViewMode(subCtx, profile);
		setFlexFieldsViewMode(subCtx, profile);
		super.toWeb(subCtx, out, name, profile);
	}

	@Override
	public void
	    fromWeb(Context ctx, Object obj, ServletRequest req, String name)
	{
		com.redknee.app.crm.bean.ui.BundleProfile profile =
		    (com.redknee.app.crm.bean.ui.BundleProfile) obj;
		Context subCtx = ctx.createSubContext();

		setBundleIdViewMode(subCtx, profile);
		setGroupBundleIdViewMode(subCtx, profile);
		setAuxiliaryFieldsViewMode(subCtx, profile);
		setRecurringFieldsViewMode(subCtx, profile);
		setCrossUnitFieldsViewMode(subCtx, profile);
		setLimitedQuotaFieldsViewMode(subCtx, profile);
		setRollOverFieldsViewMode(subCtx, profile);
		setValidityFieldsViewMode(subCtx, profile);
		setRecurringValidityFieldsViewMode(subCtx, profile);
		setRelativeValidityFieldsViewMode(subCtx, profile);
		setOneOffDateRangeFieldsViewMode(subCtx, profile);
		setFlexFieldsViewMode(subCtx, profile);

		// copy existing field from DB if applicable
		com.redknee.app.crm.bean.ui.BundleProfile dbBean =
		    getStoredBean(subCtx, profile.getBundleId());
		if (dbBean != null)
		{
			XBeans.copy(ctx, dbBean, profile);
		}

		super.fromWeb(subCtx, profile, req, name);
	}

	protected com.redknee.app.crm.bean.ui.BundleProfile getStoredBean(
	    Context ctx, long id)
	{
		Home home =
		    (Home) ctx.get(com.redknee.app.crm.bean.ui.BundleProfileHome.class);
		com.redknee.app.crm.bean.ui.BundleProfile bean = null;
		try
		{
			bean = (BundleProfile) home.find(ctx, id);
		}
		catch (HomeException exception)
		{
			LogSupport.info(ctx, this, "Cannot find bundle profile " + id
			    + " from database", exception);
		}
		return bean;
	}

	protected void setBundleIdViewMode(Context ctx,
	    com.redknee.app.crm.bean.ui.BundleProfile profile)
	{
		boolean display = (ctx.getInt("MODE", CREATE_MODE) != CREATE_MODE);
		if (!display)
		{
			hideField(ctx, profile, BundleProfileXInfo.BUNDLE_ID);
		}
	}

	protected void setGroupBundleIdViewMode(Context ctx,
	    com.redknee.app.crm.bean.ui.BundleProfile profile)
	{
		boolean display = profile.isMemberBundle() && profile.isLimitedQuota();
		if (!display)
		{
			hideField(ctx, profile, BundleProfileXInfo.GROUP_BUNDLE_ID);
		}
	}

	protected void setAuxiliaryFieldsViewMode(Context ctx,
	    com.redknee.app.crm.bean.ui.BundleProfile profile)
	{
		boolean display = profile.isAuxiliary() || profile.isFlex();
		if (!display)
		{
			hideField(ctx, profile, BundleProfileXInfo.AUXILIARY_SERVICE_CHARGE);			
		}
        if (profile.isFlex())
        {
            makeFieldReadOnly(ctx, profile, BundleProfileXInfo.AUXILIARY);
        }
	}

	protected void setRecurringFieldsViewMode(Context ctx,
	    com.redknee.app.crm.bean.ui.BundleProfile profile)
	{
		boolean recurring = !profile.isOneTime();
		if (!recurring)
		{
			hideField(ctx, profile, BundleProfileXInfo.SMART_SUSPENSION_ENABLED);
			hideField(ctx, profile,
			    BundleProfileXInfo.ACTIVATION_FEE_CALCULATION);
		}
	}

	protected void setCrossUnitFieldsViewMode(Context ctx,
	    com.redknee.app.crm.bean.ui.BundleProfile profile)
	{
        boolean isSingleService = profile.isSingleService();
		if (isSingleService)
		{
			hideField(ctx, profile, BundleProfileXInfo.BUNDLE_CATEGORY_IDS);
		}
		else
		{
			hideField(ctx, profile, BundleProfileXInfo.BUNDLE_CATEGORY_ID);
			boolean isCurrency = profile.isCurrency();
			if (isCurrency)
			{
	            hideField(ctx, BundleCategoryAssociation.class, BundleCategoryAssociationXInfo.RATE);
			}
		}
	}

	protected void setLimitedQuotaFieldsViewMode(Context ctx,
	    com.redknee.app.crm.bean.ui.BundleProfile profile)
	{
		boolean limitedQuota = profile.isLimitedQuota();
		if (!limitedQuota)
		{
			hideField(ctx, profile, BundleProfileXInfo.INITIAL_BALANCE_LIMIT);
			hideField(ctx, profile, BundleProfileXInfo.GROUP_CHARGING_SCHEME);

		}
	}

	protected void setRollOverFieldsViewMode(Context ctx,
	    com.redknee.app.crm.bean.ui.BundleProfile profile)
	{
		boolean display = profile.isLimitedQuota() && !profile.isOneTime();
		if (!display)
		{
			hideField(ctx, profile, BundleProfileXInfo.ROLLOVER_PERCENT);
			hideField(ctx, profile, BundleProfileXInfo.ROLLOVER_MAX);
			hideField(ctx, profile, BundleProfileXInfo.EXPIRY_PERCENT);
		}
	}

	protected void setValidityFieldsViewMode(Context ctx,
	    com.redknee.app.crm.bean.ui.BundleProfile profile)
	{
		boolean oneOffFixedInterval =
		    SafetyUtil.safeEquals(profile.getRecurrenceScheme(),
		        RecurrenceTypeEnum.ONE_OFF_FIXED_INTERVAL);

		boolean expireAndDelayPurge =
		    SafetyUtil.safeEquals(profile.getExpiryScheme(),
		        ExpiryTypeEnum.EXPIRE_AND_DELAY_PURGE);

		boolean display =
		    profile.isInterval()
		        && (oneOffFixedInterval || expireAndDelayPurge);

		if (!display)
		{
			hideField(ctx, profile, BundleProfileXInfo.VALIDITY);
			hideField(ctx, profile, BundleProfileXInfo.INTERVAL);
		}
        if (profile.getInterval() == DurationTypeEnum.BCD_INDEX)
        {
            hideField(ctx, profile, BundleProfileXInfo.VALIDITY);
        }
	}

	protected void setRecurringValidityFieldsViewMode(Context ctx,
	    com.redknee.app.crm.bean.ui.BundleProfile profile)
	{
		boolean display =
		    profile.isInterval()
		        && SafetyUtil.safeEquals(profile.getRecurrenceScheme(),
		            RecurrenceTypeEnum.RECUR_CYCLE_FIXED_INTERVAL)
		        && !SafetyUtil.safeEquals(profile.getChargingRecurrenceScheme(),
		                ServicePeriodEnum.DAILY);

		if (!display)
		{
			hideField(ctx, profile, BundleProfileXInfo.RECURRING_START_VALIDITY);
			hideField(ctx, profile, BundleProfileXInfo.RECURRING_START_INTERVAL);
			hideField(ctx, profile, BundleProfileXInfo.RECURRING_START_HOUR);
			hideField(ctx, profile, BundleProfileXInfo.RECURRING_START_MINUTES);
		}
	}

	protected void setRelativeValidityFieldsViewMode(Context ctx,
	    com.redknee.app.crm.bean.ui.BundleProfile profile)
	{
		boolean display = profile.isInterval();

		if (!display)
		{
			hideField(ctx, profile, BundleProfileXInfo.RELATIVE_START_HOUR);
			hideField(ctx, profile, BundleProfileXInfo.RELATIVE_START_MINUTES);
			hideField(ctx, profile, BundleProfileXInfo.RELATIVE_START_VALIDITY);
			hideField(ctx, profile, BundleProfileXInfo.RELATIVE_START_INTERVAL);
		}
	}


    protected void setOneOffDateRangeFieldsViewMode(Context ctx, com.redknee.app.crm.bean.ui.BundleProfile profile)
    {
        boolean display = SafetyUtil.safeEquals(RecurrenceTypeEnum.ONE_OFF_FIXED_DATE_RANGE,
                profile.getRecurrenceScheme());
        if (!display)
        {
            hideField(ctx, profile, BundleProfileXInfo.START_DATE);
        }
        boolean displayEndDate = display
                || ( SafetyUtil.safeEquals(ExpiryTypeEnum.EXPIRE_AND_DELAY_PURGE, profile.getExpiryScheme()) && 
                        SafetyUtil.safeEquals(RecurrenceTypeEnum.RECUR_CYCLE_FIXED_DATETIME, profile.getRecurrenceScheme()));
        if (!displayEndDate)
        {
            hideField(ctx, profile, BundleProfileXInfo.END_DATE);
        }
    }


    protected void setFlexFieldsViewMode(Context ctx, com.redknee.app.crm.bean.ui.BundleProfile profile)
    {
        boolean hideSecondaryFlexFields = !profile.isFlex() || FlexTypeEnum.ROOT.equals(profile.getFlexType());
        if (hideSecondaryFlexFields)
        {
            hideField(ctx, profile, BundleProfileXInfo.ROOT);
            // secondary flex charge
            if (!profile.isAuxiliary())
            {
                hideField(ctx, profile, BundleProfileXInfo.AUXILIARY_SERVICE_CHARGE);
            }
        }
        if (profile.isFlex()) 
        {
            makeFieldReadOnly(ctx, profile, BundleProfileXInfo.ADJUSTMENT_TYPE);
        }
        else
        {
            hideField(ctx, profile, BundleProfileXInfo.ADJUSTMENT_TYPE);            
        }
    }


	public static ViewModeEnum
	    getViewMode(Context ctx,
	        com.redknee.app.crm.bean.ui.BundleProfile profile,
	        PropertyInfo property)
	{
		ViewModeEnum viewMode =
		    (ViewModeEnum) ctx.get(getFieldViewModeKey(profile, property),
		        ViewModeEnum.READ_WRITE);
		return viewMode;
	}

	public static void
	    hideField(Context ctx,
	        com.redknee.app.crm.bean.ui.BundleProfile profile,
	        PropertyInfo property)
	{
		ctx.put(getFieldViewModeKey(profile, property), ViewModeEnum.NONE);
	}
	

    public static void makeFieldReadOnly(Context ctx, com.redknee.app.crm.bean.ui.BundleProfile profile,
            PropertyInfo property)
    {
        ctx.put(getFieldViewModeKey(profile, property), ViewModeEnum.READ_ONLY);
    }

    public static void
    hideField(Context ctx,
        Class classObject,
        PropertyInfo property)
{
    ctx.put(getFieldViewModeKey(classObject, property), ViewModeEnum.NONE);
}

    public static String
	    getFieldViewModeKey(com.redknee.app.crm.bean.ui.BundleProfile profile,
	        PropertyInfo property)
	{
		StringBuilder fieldName = new StringBuilder();
		fieldName.append(profile.getClass().getSimpleName());
		fieldName.append(".");
		fieldName.append(property.getName());
		fieldName.append(".mode");
		return fieldName.toString();
	}

    public static String
    getFieldViewModeKey(Class classObject,
        PropertyInfo property)
{
    StringBuilder fieldName = new StringBuilder();
    fieldName.append(classObject.getSimpleName());
    fieldName.append(".");
    fieldName.append(property.getName());
    fieldName.append(".mode");
    return fieldName.toString();
}
}
