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
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved. 
 */
package com.trilogy.app.crm.api.rmi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bundle.Balance;
import com.trilogy.app.crm.bundle.BundleProfile;
import com.trilogy.app.crm.bundle.InvalidBundleApiException;
import com.trilogy.app.crm.bundle.SubscriberBucket;
import com.trilogy.app.crm.support.BundleSupportHelper;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.util.crmapi.wsdl.v2_1.types.subscription.SubscriptionBundleBalance;
import com.trilogy.util.crmapi.wsdl.v2_2.types.subscription.BundleStatusEnum;
import com.trilogy.util.crmapi.wsdl.v2_2.types.subscription.ReadOnlySubscriptionBundle;
import com.trilogy.util.crmapi.wsdl.v2_2.types.subscription.SubscriptionBundleUnitTypeEnum;


/**
 * This class adapts from CRM's SubscriberBucket bean to the API's ReadOnlySubscriberBundle bean.
 * The reverse adaptation is not supported because we can't do a reverse lookup of the application
 * ID from the balance type and unit type parameters.
 *
 * @author Aaron Gourley
 * @since 7.3
 */
public class SubscriberBucketToApiAdapter implements Adapter
{
    private static SubscriberBucketToApiAdapter instance_ = null;
    
    public static SubscriberBucketToApiAdapter instance()
    {
        if( instance_ == null )
        {
            instance_ = new SubscriberBucketToApiAdapter();
        }
        return instance_;
    }
    
    protected SubscriberBucketToApiAdapter()
    {
        // Derived classes can use this ctor, but users should use the singleton method
    }
    
    /**
     * @{inheritDoc}
     */
    public Object adapt(Context ctx, final Object bean) throws HomeException
    {
        final SubscriberBucket bucket = (SubscriberBucket)bean;
        
        final ReadOnlySubscriptionBundle apiBundle = new ReadOnlySubscriptionBundle();
        
        if( bucket != null )
        {
            apiBundle.setBucketID(bucket.getBucketId());
            apiBundle.setBundleID(bucket.getBundleId());
            apiBundle.setMsisdn(bucket.getMsisdn());
            apiBundle.setSpid(bucket.getSpid());
            apiBundle.setExpiryTime(CalendarSupportHelper.get(ctx).dateToCalendar(new Date(bucket.getExpiryTime())));
            apiBundle.setUnitType(SubscriptionBundleUnitTypeEnum.valueOf(bucket.getUnitType().getIndex()));
            apiBundle.setProvisionTime(CalendarSupportHelper.get(ctx).dateToCalendar(new Date(bucket.getProvisionTime())));
            apiBundle.setActivationTime(CalendarSupportHelper.get(ctx).dateToCalendar(new Date(bucket.getActivationTime())));
            apiBundle.setStatus(BundleStatusEnum.valueOf(bucket.getStatus().getIndex()));
            try
            {
                BundleProfile profile = BundleSupportHelper.get(ctx).getBundleProfile(ctx, bucket.getBundleId());
                apiBundle.setBundleName(profile.getName());
            }
            catch (InvalidBundleApiException invalidEx)
            {
                throw new HomeException("Unable to find the bundle profile for id" + bucket.getBundleId());
            }
            
            List balances = new ArrayList();
            
            Balance balance = bucket.getRegularBal();
            
            
            
            if( balance != null )
            {
                balances.add(adaptBundleBalance(balance));
            }
            
            // TODO: Add cross service balances when CRM supports them
            
            if( balances.size() > 0 )
            {
                Object[] balObjArray = balances.toArray();
                SubscriptionBundleBalance[] balArray = new SubscriptionBundleBalance[balObjArray.length];
                for( int i=0; i<balObjArray.length; i++ )
                {
                    balArray[i] = (SubscriptionBundleBalance)balObjArray[i];
                }
                apiBundle.setBalances(balArray);  
            }
        }
                
        return apiBundle;
    }


    /**
     * @{inheritDoc}
     */
    public Object unAdapt(Context ctx, final Object bean) throws HomeException
    {
        throw new UnsupportedOperationException();
    }


    protected SubscriptionBundleBalance adaptBundleBalance(Balance balance)
    {
        final SubscriptionBundleBalance apiBalance = new SubscriptionBundleBalance();
        
        apiBalance.setBundleCategoryId(balance.getApplicationId());
        apiBalance.setPersonalLimit(balance.getPersonalLimit());
        apiBalance.setPersonalUsed(balance.getPersonalUsed());
        apiBalance.setPersonalBalance(balance.getPersonalBalance());
        
        apiBalance.setGroupLimit(balance.getGroupLimit());
        apiBalance.setGroupUsed(balance.getGroupUsed());
        apiBalance.setGroupBalance(balance.getGroupBalance());
        
        apiBalance.setRolloverLimit(balance.getRolloverLimit());
        apiBalance.setRolloverUsed(balance.getRolloverUsed());
        apiBalance.setRolloverBalance(balance.getRolloverBalance());
        
        return apiBalance;
    }
}
