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
package com.trilogy.app.crm.api.rmi;

import java.util.Map;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.util.crmapi.wsdl.v2_0.types.PoolLimitStrategyEnum;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.BaseReadOnlyAccountExtension;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.BaseReadOnlyAccountExtensionSequence_type0;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.BaseReadOnlyAccountExtensionSequence_type1;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.BaseReadOnlyAccountExtensionSequence_type2;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.BaseReadOnlyAccountExtensionSequence_type3;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.extensions.ReadOnlyAccountExtension;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.extensions.ReadOnlyFriendsAndFamilyAccountExtension;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.extensions.ReadOnlyFriendsAndFamilyExtn;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.extensions.ReadOnlyGroupPricePlanAccountExtension;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.extensions.ReadOnlyGroupPricePlanExtn;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.extensions.ReadOnlyPoolAccountExtension;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.extensions.ReadOnlySubscriberLimitAccountExtension;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.extensions.ReadOnlySubscriberLimitExtn;
import com.trilogy.app.crm.extension.account.AbstractGroupPricePlanExtension;
import com.trilogy.app.crm.extension.account.AccountExtension;
import com.trilogy.app.crm.extension.account.FriendsAndFamilyExtension;
import com.trilogy.app.crm.extension.account.GroupPricePlanExtension;
import com.trilogy.app.crm.extension.account.PoolExtension;
import com.trilogy.app.crm.extension.account.SubscriberLimitExtension;
import com.trilogy.app.crm.extension.account.SubscriptionPoolProperty;
import com.trilogy.app.crm.support.CollectionSupportHelper;

/**
 * Adapts AccountExtension object to API objects.
 *
 * @author aaron.gourley@redknee.com
 * @since 8.2
 */
public class AccountExtensionToApiAdapter implements Adapter
{   
    @Override
    public Object adapt(final Context ctx, final Object obj) throws HomeException
    {
        AccountExtension extension = (AccountExtension) obj;

        BaseReadOnlyAccountExtension apiExtension = null; 
        
        if (extension instanceof PoolExtension)
        {
        	apiExtension = new ReadOnlyAccountExtension();
            PoolExtension poolExtension = (PoolExtension) extension;
            
            ReadOnlyPoolAccountExtension apiPoolExtension = new ReadOnlyPoolAccountExtension();   
            
            apiPoolExtension.setGroupMobileNumber(poolExtension.getPoolMSISDN());

            Map<Long, SubscriptionPoolProperty> crmPoolProperties = poolExtension.getSubscriptionPoolProperties();

            com.redknee.util.crmapi.wsdl.v2_2.types.account.extensions.SubscriptionPoolProperty[] poolProperties = CollectionSupportHelper.get(ctx).adaptCollection(
                    ctx, 
                    crmPoolProperties.values(), 
                    new PoolPropertyToApiAdapter(), 
                    new com.redknee.util.crmapi.wsdl.v2_2.types.account.extensions.SubscriptionPoolProperty[] {});

            apiPoolExtension.setSubscriptionPoolProperty(poolProperties);
            
			apiPoolExtension.setPoolLimitStrategy(PoolLimitStrategyEnum
			    .valueOf(poolExtension.getQuotaType().getIndex()));
			apiPoolExtension.setPoolLimit(Long.valueOf(poolExtension
			    .getQuotaLimit()));
			Map bundles = poolExtension.getPoolBundles(ctx);
			Long[] bundleIDs = new Long[bundles.size()];
			int i = 0;
			for (Object key : bundles.keySet())
			{
				bundleIDs[i++] = (Long) key;
			}
			apiPoolExtension.setBundleIDs(bundleIDs);

            BaseReadOnlyAccountExtensionSequence_type3 choice = new BaseReadOnlyAccountExtensionSequence_type3();
            choice.setPool(apiPoolExtension);
            ((ReadOnlyAccountExtension) apiExtension).setBaseReadOnlyAccountExtensionSequence_type3(choice);
        }
        else if(extension instanceof GroupPricePlanExtension)
        {
        	apiExtension = new ReadOnlyGroupPricePlanExtn();
        	GroupPricePlanExtension crmgroupPricePlanExtension = (GroupPricePlanExtension) extension;
        	ReadOnlyGroupPricePlanAccountExtension apiGroupPricePlanAccountExtension = new ReadOnlyGroupPricePlanAccountExtension();
        	
        	if(crmgroupPricePlanExtension.getPostpaidPricePlanID() != AbstractGroupPricePlanExtension.DEFAULT_POSTPAIDPRICEPLANID)
        	{
        		apiGroupPricePlanAccountExtension.setPostpaidPricePlanID(crmgroupPricePlanExtension.getPostpaidPricePlanID());
        	}
        	if(crmgroupPricePlanExtension.getPrepaidPricePlanID() != AbstractGroupPricePlanExtension.DEFAULT_PREPAIDPRICEPLANID)
        	{
        		apiGroupPricePlanAccountExtension.setPrepaidPricePlanID(crmgroupPricePlanExtension.getPrepaidPricePlanID());
        	}
        	
        	BaseReadOnlyAccountExtensionSequence_type1 addToApi = new BaseReadOnlyAccountExtensionSequence_type1();
        	addToApi.setGroupPricePlan(apiGroupPricePlanAccountExtension);
        	((ReadOnlyGroupPricePlanExtn) apiExtension).setBaseReadOnlyAccountExtensionSequence_type1(addToApi);
        }
        else if(extension instanceof SubscriberLimitExtension)
        {
        	apiExtension = new ReadOnlySubscriberLimitExtn();
        	SubscriberLimitExtension crmSubscriberLimitExtension = (SubscriberLimitExtension) extension;
        	ReadOnlySubscriberLimitAccountExtension apiSubscriberLimitExtension = new ReadOnlySubscriberLimitAccountExtension();
        	
        	apiSubscriberLimitExtension.setMaxSubscribers(crmSubscriberLimitExtension.getMaxSubscribers());

        	BaseReadOnlyAccountExtensionSequence_type2 addToApi = new BaseReadOnlyAccountExtensionSequence_type2();
        	addToApi.setSubscriberLimit(apiSubscriberLimitExtension);
        	((ReadOnlySubscriberLimitExtn) apiExtension).setBaseReadOnlyAccountExtensionSequence_type2(addToApi);
        }
        else if(extension instanceof FriendsAndFamilyExtension)
        {
        	apiExtension = new ReadOnlyFriendsAndFamilyExtn();
        	FriendsAndFamilyExtension crmFnFExtension = (FriendsAndFamilyExtension) extension;
        	ReadOnlyFriendsAndFamilyAccountExtension apiFnF = new ReadOnlyFriendsAndFamilyAccountExtension();
        	
        	apiFnF.setCugOwnerMSISDN(crmFnFExtension.getCugOwnerMsisdn());
        	apiFnF.setCugTemplateID(crmFnFExtension.getCugTemplateID());
        	apiFnF.setSmsNotificationMSISDN(crmFnFExtension.getSmsNotificationMSISDN());
        	
        	BaseReadOnlyAccountExtensionSequence_type0 addToApi = new BaseReadOnlyAccountExtensionSequence_type0();
        	addToApi.setFriendsAndFamily(apiFnF);
        	
        	((ReadOnlyFriendsAndFamilyExtn)apiExtension).setBaseReadOnlyAccountExtensionSequence_type0(addToApi);
        }
        else
        {
        	throw new HomeException("Extension type unsupported");
        }
        
        return apiExtension;
    }

    @Override
    public Object unAdapt(final Context ctx, final Object obj) throws HomeException
    {
        throw new UnsupportedOperationException();
    }
}
