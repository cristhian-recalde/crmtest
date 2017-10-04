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
package com.trilogy.app.crm.bean.ui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.webcontrol.WebController;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;

import com.trilogy.app.crm.bean.SysFeatureCfg;
import com.trilogy.app.crm.bean.SysFeatureCfgXInfo;
import com.trilogy.app.crm.bean.core.BundleCategoryAssociation;
import com.trilogy.app.crm.bundle.BMBundleCategoryAssociation;
import com.trilogy.app.crm.bundle.BMBundleCategoryAssociationHome;
import com.trilogy.app.crm.bundle.BMBundleCategoryAssociationXInfo;
import com.trilogy.app.crm.bundle.BundleCategory;
import com.trilogy.app.crm.bundle.BundleCategoryXInfo;
import com.trilogy.app.crm.bundle.BundleTypeEnum;
import com.trilogy.app.crm.bundle.GroupChargingTypeEnum;
import com.trilogy.app.crm.bundle.QuotaTypeEnum;
import com.trilogy.app.crm.bundle.UnitTypeEnum;
import com.trilogy.app.crm.bundle.YearDateTime;
import com.trilogy.app.crm.bundle.exception.BundleManagerException;
import com.trilogy.app.crm.bundle.exception.CategoryNotExistException;
import com.trilogy.app.crm.bundle.service.CRMBundleCategory;
import com.trilogy.app.crm.bundle.CategoryAssociationTypeEnum;
import com.trilogy.app.crm.defaultvalue.BooleanValue;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;

/**
 * @author cindy.wong@redknee.com
 * @since 8.5
 */
public class BundleProfile extends AbstractBundleProfile implements
    ContextAware
{
	private static final long serialVersionUID = 1L;

	public boolean isLimitedQuota()
	{
		return SafetyUtil.safeEquals(QuotaTypeEnum.FIXED_QUOTA,
		    getQuotaScheme())
		    || SafetyUtil.safeEquals(QuotaTypeEnum.MOVING_QUOTA,
		        getQuotaScheme());
	}

    public boolean isSingleService()
    {
     return CategoryAssociationTypeEnum.SINGLE_UNIT.equals(this.getAssociationType());
    }

    public boolean isCrossService()
    {
     return CategoryAssociationTypeEnum.CROSS_UNIT.equals(this.getAssociationType());
    }

    public boolean isCurrency()
    {
     return CategoryAssociationTypeEnum.CURRENCY.equals(this.getAssociationType());
    }

    public boolean isOneTime()
	{
		boolean result = false;
		if (getRecurrenceScheme() != null)
		{
			result = getRecurrenceScheme().isOneTime();
		}
		return result;
	}
    
    
    @Override
    public Date getEndDate()
    {
 	   boolean secondaryBalance;
 	   int numberOfYears = SysFeatureCfg.DEFAULT_SECONDARYBALANCEBUNDLETOENDAFTER;
 	   try
 	   {
 		   SysFeatureCfg sysConfig = (SysFeatureCfg) getContext().get(SysFeatureCfg.class);
 		   if(sysConfig != null)
 		   {
 			   numberOfYears = sysConfig.getSecondaryBalanceBundleToEndAfter();
 		   }
 		   secondaryBalance = isSecondaryBalance(getContext());
 	   }
 	   catch(HomeException e)
 	   {
 		   LogSupport.major(getContext(), this, "Error finding if this bundle is a secondary balance:", e);
 		   secondaryBalance = BooleanValue.FALSE;
 	   }
 	   
 	   if(secondaryBalance)
 	   {
 		   Date end = super.getEndDate();
 		   
 		   if(end == null)
 		   {
 			   return CalendarSupportHelper.get(getContext()).findDateYearsAfter(numberOfYears, new Date());
 		   }
 		   
 		   return end;
 	   }
 	   else
 	   {
 		   return super.getEndDate();
 	   }
    }
    
    public boolean isSecondaryBalance(Context ctx)
    	throws HomeException
    {
	   boolean isSecondaryBalance = BooleanValue.FALSE;
	   
       List<BundleCategory> associatedBundleCategoryList = getBundleCategories(ctx);
	   
       for(BundleCategory associatedBundleCategory : associatedBundleCategoryList)
       {
    	   if(UnitTypeEnum.SECONDARY_BALANCE.equals(associatedBundleCategory.getUnitType()))
    	   {
    		   isSecondaryBalance = BooleanValue.TRUE;
    		   break;
    	   }
       }
	
	   return isSecondaryBalance;
	}

	public boolean isInterval()
	{
		boolean result = false;
		if (getRecurrenceScheme() != null)
		{
			result = getRecurrenceScheme().isInterval();
		}
		return result;
	}

	public boolean isMemberBundle()
	{
		return SafetyUtil.safeEquals(GroupChargingTypeEnum.MEMBER_BUNDLE,
		    getGroupChargingScheme());
	}

	/*
	 * [Cindy Wong] The methods below are copied and pasted from bean.core.
	 */
	protected YearDateTime populateYearDateTime(Date date)
	{
		YearDateTime obj = new YearDateTime();

		if (date == null)
		{
			obj.setPresent(false);
		}
		else
		{
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			obj.setPresent(true);
			obj.setYear(calendar.get(Calendar.YEAR));
			obj.setMonth(calendar.get(Calendar.MONTH) + 1);
			obj.setDay(calendar.get(Calendar.DAY_OF_MONTH));
			obj.setHour(calendar.get(Calendar.HOUR_OF_DAY));
			obj.setMinute(calendar.get(Calendar.MINUTE));
		}

		return obj;
	}

	/**
	 * convenience method to stay compatible with ProductBundleManager
	 */
	public void setYearStartDateTime(YearDateTime obj)
	{
		if (obj.getPresent())
		{
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.YEAR, obj.getYear());
			calendar.set(Calendar.MONTH, obj.getMonth() - 1);
			calendar.set(Calendar.DAY_OF_MONTH, obj.getDay());
			calendar.set(Calendar.HOUR_OF_DAY, obj.getHour());
			calendar.set(Calendar.MINUTE, obj.getMinute());
			calendar.set(Calendar.SECOND, 0);

			setStartDate(calendar.getTime());
		}
		else
		{
			setStartDate(null);
		}
	}

	/**
	 * convenience method to stay compatible with ProductBundleManager
	 */
	public YearDateTime getYearStartDateTime()
	{
		return populateYearDateTime(getStartDate());
	}

	/**
	 * convenience method to stay compatible with ProductBundleManager
	 */
	public void setYearExpiryDateTime(YearDateTime obj)
	{
		if (obj.getPresent())
		{
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.YEAR, obj.getYear());
			calendar.set(Calendar.MONTH, obj.getMonth() - 1);
			calendar.set(Calendar.DAY_OF_MONTH, obj.getDay());
			calendar.set(Calendar.HOUR_OF_DAY, obj.getHour());
			calendar.set(Calendar.MINUTE, obj.getMinute());
			calendar.set(Calendar.SECOND, 0);

			setEndDate(calendar.getTime());
		}
		else
		{
			setEndDate(null);
		}
	}

	/**
	 * convenience method to stay compatible with ProductBundleManager
	 */
	public YearDateTime getYearExpiryDateTime()
	{
		return populateYearDateTime(getEndDate());
	}

	/**
	 * convenience method to determine whether bundle has a validity period.
	 */
	public boolean isIsValidity()
	{
		return getRecurrenceScheme().isInterval();
	}

	@Override
	public int getBundleCategoryId()
	{
		if (getBundleCategoryIds().size() > 0 && bundleCategoryId_ <= 0)
		{
			Iterator<Map.Entry<?, BundleCategoryAssociation>> iter =
			    getBundleCategoryIds().entrySet().iterator();
			bundleCategoryId_ = iter.next().getValue().getCategoryId();
		}

		return super.getBundleCategoryId();
	}

	@Override
	public void setBundleCategoryId(int categoryId)
	{
		if (categoryId >= 0)
		{
			bundleCategoryIds_ = new HashMap();
			BundleCategoryAssociation assoc = new BundleCategoryAssociation();
			assoc.setCategoryId(categoryId);
			assoc.setContext(getContext());
			bundleCategoryIds_.put(Integer.valueOf(1), assoc);
			super.setBundleCategoryId(categoryId);
		}
	}

	@Override
	public void setBundleCategoryIds(Map categoryIds)
	{
		super.setBundleCategoryIds(categoryIds);
		bundleCategoryIds_ = categoryIds;
		if (categoryIds.size() > 0)
		{
			Iterator<Map.Entry<?, BundleCategoryAssociation>> iter =
			    getBundleCategoryIds().entrySet().iterator();
			bundleCategoryId_ = iter.next().getValue().getCategoryId();
		}
	}

	@Override
	public Map getBundleCategoryIds()
	{
		if ((bundleCategoryIds_ == null || bundleCategoryIds_.size() == 0)
		    && getBundleId() > 0)
		{
			Collection coll = null;
			bundleCategoryIds_ = new HashMap();

			Home assocHome =
			    (Home) getContext().get(BMBundleCategoryAssociationHome.class);
			try
			{
				coll =
				    assocHome.select(
				        getContext(),
				        new EQ(BMBundleCategoryAssociationXInfo.BUNDLE_ID, Long
				            .valueOf(this.getBundleId())));
			}
			catch (final HomeException e)
			{
				String msg =
				    "Error trying to get the bundle category associations for bundle profile "
				        + getBundleId();
				new MajorLogMsg(this, msg, e).log(getContext());
			}

			if (coll != null)
			{
				final Iterator<BMBundleCategoryAssociation> iter =
				    coll.iterator();
				int i = 1;
				BMBundleCategoryAssociation bundleAssoc = null;
				while (iter.hasNext())
				{
					bundleAssoc = iter.next();
					BundleCategoryAssociation assoc;
					try
					{
						assoc =
						    (BundleCategoryAssociation) XBeans.instantiate(
						        BundleCategoryAssociation.class, getContext());
					}
					catch (Exception e)
					{
						assoc = new BundleCategoryAssociation();
					}
					assoc.setCategoryId(bundleAssoc.getCategoryId());
					assoc.setType(bundleAssoc.getUnitType().getIndex());
					assoc.setRate(bundleAssoc.getRate());
					bundleCategoryIds_.put(Integer.valueOf(i), assoc);
					i++;
				}
			}
		}

		return bundleCategoryIds_;

	}

	public List<BundleCategory> getBundleCategories(Context ctx)
	{
		CRMBundleCategory service =
		    (CRMBundleCategory) ctx.get(CRMBundleCategory.class);
		List<BundleCategory> categories = new ArrayList<BundleCategory>();
		BundleCategory cat = null;

		if (service != null)
		{
			Iterator<Map.Entry<?, BundleCategoryAssociation>> iter =
			    getBundleCategoryIds().entrySet().iterator();
			while (iter.hasNext())
			{
				BundleCategoryAssociation association = iter.next().getValue();
				try
				{
					cat = service.getCategory(ctx, association.getCategoryId());
					categories.add(cat);
				}
				catch (CategoryNotExistException e)
				{
					if (LogSupport.isDebugEnabled(ctx))
					{
						String message =
						    "CategoryNotExistException while getting the category "
						        + association.getCategoryId();
						LogSupport.debug(ctx, this, message, e);
					}
				}
				catch (BundleManagerException e)
				{
					if (LogSupport.isDebugEnabled(ctx))
					{
						String message =
						    "BundleManagerException while getting the category "
						        + association.getCategoryId();
						LogSupport.debug(ctx, this, message, e);
					}
				}
			}
		}
		else
		{
			String message =
			    "Bundle Category Service doesn't exists in the Context.";
			LogSupport.crit(ctx, this, message);
		}

		return categories;
	}

	@Override
	public int getType()
	{

		// If bundle exists but categories have not been retrieved yet, just use
		// bundle type.
		if ((getBundleId() > 0 && (bundleCategoryIds_ == null || bundleCategoryIds_
		    .size() == 0)))
		{
			return super.getType();
		}
		else if (isCurrency())
		{
			return BundleTypeEnum.MONETARY_INDEX;
		}
        else if (isCrossService())
        {
            return BundleTypeEnum.CROSS_SERVICE_INDEX;
        }
		else if (getBundleCategoryIds().size() == 0)
		{
			return super.getType();
		}
		else
		{
			Iterator<Map.Entry<?, BundleCategoryAssociation>> iter =
			    getBundleCategoryIds().entrySet().iterator();
			return iter.next().getValue().getType();
		}
	}

	/**
	 * override the getType method so that we can do the lookup based on
	 * the category
	 */
	public List<BundleTypeEnum> getTypes()
	{
		List<BundleTypeEnum> types = new ArrayList<BundleTypeEnum>();

		Iterator<Map.Entry<?, BundleCategoryAssociation>> iter =
		    getBundleCategoryIds().entrySet().iterator();
		while (iter.hasNext())
		{
			BundleCategoryAssociation association = iter.next().getValue();
			if (!types.contains(BundleTypeEnum.get((short) association
			    .getType())))
			{
				types.add(BundleTypeEnum.get((short) association.getType()));
			}
		}

		return types;
	}

	/**
	 * This method returns true if the bundle of indicated type Since we store
	 * BundleTypeEnup property of BundleProfile with EnumIndexInteger
	 * WebControl, if
	 * enum.equals() we will end up comparing integer equivalents of the enum's
	 * index
	 * short value
	 * 
	 * @param bundleTypeEnum
	 * @return
	 */
	public boolean isOfType(BundleTypeEnum bundleTypeEnum)
	{
		if (null == bundleTypeEnum)
		{
			return false;
		}
		return (bundleTypeEnum.getIndex()) == this.getType();
	}
	
	public static boolean isFromWebNewOrPreviewOnFlexType(final Context ctx)
    {
        if (ctx != null)
        {
            final HttpServletRequest req = (HttpServletRequest) ctx.get(HttpServletRequest.class);
            // If a preview is occurring as a result of a price plan selection
            // set the deposit and credit limit to that of the price plan
            if (req != null
                    && (WebController.isCmd("New", req) || WebController.isCmd("Preview", req)
                            && req.getParameter("PreviewButtonSrc") != null
                            && (req.getParameter("PreviewButtonSrc").indexOf(".flexType") != -1)))
            {
                return true;
            }
        }
        return false;
    }

	public BundleCategory getCategory(Context ctx) throws HomeException
	{
		CRMBundleCategory service =
		    (CRMBundleCategory) ctx.get(CRMBundleCategory.class);
		BundleCategory cat = null;

		if (service != null)
		{
			try
			{
				if (getBundleCategoryId() != -1)
				{
					cat = service.getCategory(ctx, getBundleCategoryId());
				}
			}
			catch (CategoryNotExistException e)
			{
				if (LogSupport.isDebugEnabled(ctx))
				{
					String message =
					    "CategoryNotExistException while getting the category "
					        + getBundleCategoryId();
					LogSupport.debug(ctx, this, message, e);
				}
			}
			catch (BundleManagerException e)
			{
				if (LogSupport.isDebugEnabled(ctx))
				{
					String message =
					    "BundleManagerException while getting the category "
					        + getBundleCategoryId();
					LogSupport.debug(ctx, this, message, e);
				}
			}
		}
		else
		{
			String message =
			    "Bundle Category Service doesn't exists in the Context "
			        + getBundleCategoryId();
			LogSupport.crit(ctx, this, message);
		}

		return cat;
	}

	@Override
	public Context getContext()
	{
		return ctx_;
	}

	@Override
	public void setContext(Context ctx)
	{
		ctx_ = ctx;
	}

	private transient Context ctx_;
}
