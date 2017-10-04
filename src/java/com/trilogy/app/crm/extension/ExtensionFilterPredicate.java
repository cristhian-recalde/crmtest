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
package com.trilogy.app.crm.extension;

import java.util.List;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountCreationTemplate;
import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.bean.GroupTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberTypeAware;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SubscriptionTypeAware;
import com.trilogy.app.crm.bean.TypeAware;
import com.trilogy.app.crm.bean.account.SubscriptionTypeEnum;
import com.trilogy.app.crm.bean.core.SubscriptionType;
import com.trilogy.app.crm.extension.account.AccountExtension;
import com.trilogy.app.crm.extension.account.GroupPricePlanExtension;
import com.trilogy.app.crm.extension.account.PoolExtension;
import com.trilogy.app.crm.extension.service.AlcatelSSCServiceExtension;
import com.trilogy.app.crm.extension.spid.AlcatelSSCSpidExtension;
import com.trilogy.app.crm.extension.subscriber.AlcatelSSCSubscriberExtension;
import com.trilogy.app.crm.extension.subscriber.MultiSimSubExtension;
import com.trilogy.app.crm.extension.subscriber.OverdraftBalanceSubExtension;
import com.trilogy.app.crm.extension.subscriber.FixedStopPricePlanSubExtension;
import com.trilogy.app.crm.extension.usergroup.UserGroupExtension;
import com.trilogy.app.crm.license.LicenseAware;
import com.trilogy.app.crm.move.request.ConvertAccountGroupTypeRequest;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.app.crm.support.LicensingSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.entity.EntityInfo;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.xenum.AbstractEnum;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * This predicate is meant to filter out extensions that are not allowed to be
 * used with the parent bean for some business logic related reason.
 *
 * @author aaron.gourley@redknee.com
 * @since 8.2
 */
public class ExtensionFilterPredicate implements Predicate
{
    private static ExtensionFilterPredicate instance_ = null;
    
    public static ExtensionFilterPredicate instance()
    {
        if( instance_ == null )
        {
            instance_ = new ExtensionFilterPredicate();
        }
        return instance_;
    }

    private boolean filterBasedOnInterfaces(Context ctx, Class extensionClass, Object parentBean)
    {
        boolean result = true;
        AbstractExtension extension = null;
        try
        {
            extension = (AbstractExtension) extensionClass.newInstance();
        }
        catch (Exception e)
        {
            LogSupport.debug(ctx, this, "Unable to verify extension based on filters "
                    + extensionClass.getName() + ": " + e.getMessage(), e);
        }
        
        if (extension!=null && parentBean!=null)
        {

            if (result && SubscriberTypeDependentExtension.class.isAssignableFrom(extensionClass) 
                && (parentBean instanceof SubscriberTypeAware))
            {
                SubscriberTypeEnum subscriberType = ((SubscriberTypeAware) parentBean).getSubscriberType();
                result = result && ((SubscriberTypeDependentExtension) extension).isValidForSubscriberType(subscriberType);
            }
            
            if (result && TypeDependentExtension.class.isAssignableFrom(extensionClass)
                    && (parentBean instanceof TypeAware))
            {
                AbstractEnum enumType = ((TypeAware) parentBean).getEnumType();
                result = result && ((TypeDependentExtension) extension).isValidForType(enumType);
            }
            
            if (result && (LicenseAware.class.isAssignableFrom(extensionClass)))
            {
                result = result && ((LicenseAware) extension).isLicensed(ctx);
            }
        }
        return result;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean f(Context ctx, Object obj) throws AbortVisitException
    {
        Class extensionClass = getExtensionClass(obj);

        boolean visible = extensionClass != null;
        
        Object parentBean = ExtensionSupportHelper.get(ctx).getParentBean(ctx);

        if (visible)
        {
            if (AccountExtension.class.isAssignableFrom(extensionClass))
            {
                visible = filterAccountExtensions(ctx, extensionClass, parentBean);
            }
            else if (UserGroupExtension.class.isAssignableFrom(extensionClass))
            {
                visible = true;
            }
            else if (MultiSimSubExtension.class.isAssignableFrom(extensionClass))
            {
                visible = LicensingSupportHelper.get(ctx).isLicensed(ctx, LicenseConstants.MULTI_SIM_LICENSE);
                if (visible)
                {
                    boolean found = false;
                    Subscriber sub = null;
                    if (parentBean instanceof Subscriber)
                    {
                        sub = (Subscriber) parentBean;
                    }
                    if (sub != null)
                    {
                        // Only show the Multi-SIM extension if the subscription has a multi-SIM service
                        List<SubscriberAuxiliaryService> associations = sub.getAuxiliaryServices(ctx);
                        if (associations != null)
                        {
                            for (SubscriberAuxiliaryService association : associations)
                            {
                                if (association != null
                                        && AuxiliaryServiceTypeEnum.MultiSIM.equals(association.getType(ctx)))
                                {
                                    found = true;
                                    break;
                                }
                            }
                        }
                    }
                    visible = found;
                }
            }
            else if ((AlcatelSSCServiceExtension.class.isAssignableFrom(extensionClass)
                    || AlcatelSSCSubscriberExtension.class.isAssignableFrom(extensionClass)
                    || AlcatelSSCSpidExtension.class.isAssignableFrom(extensionClass)))
            {
                // Disable all alcatel extensions if the main feature licence is disabled
                visible = LicensingSupportHelper.get(ctx).isLicensed(ctx, LicenseConstants.ALCATEL_LICENSE);
                if (visible)
                {
                    SubscriptionType subscriptionType = null;
                    if (parentBean instanceof SubscriptionType)
                    {
                        subscriptionType = (SubscriptionType) parentBean;
                    }
                    if (parentBean instanceof SubscriptionTypeAware)
                    {
                        subscriptionType = ((SubscriptionTypeAware) parentBean).getSubscriptionType(ctx);
                    }
                    
                    if (subscriptionType != null)
                    {                    
                        if (!subscriptionType.isOfType(SubscriptionTypeEnum.BROADBAND))
                        {
                            // Alcatel SSC extensions are only applicable to broadband subscription types
                            visible = false;
                        }
                    }
                }
            }
            else if (OverdraftBalanceSubExtension.class.isAssignableFrom(extensionClass))
            {
                if (parentBean instanceof Subscriber)
                {
                    Subscriber subscriber = (Subscriber) parentBean;
                    visible = !subscriber.isPooled(ctx)
                            && (subscriber.getSubscriptionType(ctx) == null || subscriber.getSubscriptionType(ctx).getType() == SubscriptionTypeEnum.AIRTIME_INDEX);
                }
            }
            else if (FixedStopPricePlanSubExtension.class.isAssignableFrom(extensionClass))
            {
                if (parentBean instanceof Subscriber)
                {
                    Subscriber subscriber = (Subscriber) parentBean;
                    visible = LicensingSupportHelper.get(ctx).isLicensed(ctx, LicenseConstants.SUBSCRIBER_FIXED_STOP_PRICEPLAN_LICENSE);
                    visible = visible && !subscriber.isPooled(ctx)
                            && (subscriber.getSubscriptionType(ctx) == null || subscriber.getSubscriptionType(ctx).getType() == SubscriptionTypeEnum.AIRTIME_INDEX);
                    visible = visible && subscriber.isPrepaid();
                }
            }            
        }
        
        if (visible)
        {
            visible = filterBasedOnInterfaces(ctx, extensionClass, parentBean);
        }

        return visible;
    }

    private boolean filterAccountExtensions(Context ctx, Class<AccountExtension> extensionClass, Object parentBean)
    {
        boolean visible = true;
		boolean isPooled = false;
		boolean isIndividual = false;
		boolean onlyPooled = false;

        if (parentBean instanceof ConvertAccountGroupTypeRequest)
        {
            ConvertAccountGroupTypeRequest request = (ConvertAccountGroupTypeRequest) parentBean;
            isPooled = GroupTypeEnum.GROUP_POOLED.equals(request.getGroupType());
            onlyPooled = true;
        }
        else if (parentBean instanceof Account)
        {
            Account account = (Account) parentBean;
			isPooled = account.isPooled(ctx);
			isIndividual = account.isIndividual(ctx);
        }
        else if (parentBean instanceof AccountCreationTemplate)
        {
            AccountCreationTemplate act = (AccountCreationTemplate) parentBean;
			isPooled = act.isPooled(ctx);
			isIndividual = act.isIndividual(ctx);
        }
        
        if (!isPooled && PoolExtension.class.isAssignableFrom(extensionClass))
		{
			// Pooled extensions are only applicable to pooled account types
			visible = false;
		}
		else if (isIndividual
		    && GroupPricePlanExtension.class.isAssignableFrom(extensionClass))
		{
			// Group Price Plan extensions are only applicable to non-individual
			// account types
			visible = false;
		}
		else if (onlyPooled && !(PoolExtension.class.isAssignableFrom(extensionClass)))
		{
		    visible = false;
		}
        
        return visible;
    }

    private Class getExtensionClass(Object obj)
    {
        Class extensionClass = null;
        if (obj instanceof EntityInfo)
        {
            try
            {
                extensionClass = Class.forName(((EntityInfo)obj).getClassName());
            }
            catch (ClassNotFoundException e)
            {
                // Ignore.  Leave extension class null and filter it out.
            }
        }
        else if (obj instanceof Class)
        {
            extensionClass = (Class) obj;
        }
        else if (obj != null)
        {
            extensionClass = obj.getClass();
        }
        return extensionClass;
    }

}
