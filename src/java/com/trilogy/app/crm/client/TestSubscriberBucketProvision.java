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
package com.trilogy.app.crm.client;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.omg.CORBA.Context;
import org.omg.CORBA.ContextList;
import org.omg.CORBA.DomainManager;
import org.omg.CORBA.ExceptionList;
import org.omg.CORBA.NVList;
import org.omg.CORBA.NamedValue;
import org.omg.CORBA.Object;
import org.omg.CORBA.Policy;
import org.omg.CORBA.Request;
import org.omg.CORBA.SetOverrideType;

import com.trilogy.product.bundle.manager.provision.v5_0.bucket.BucketHistoryReturnParam;
import com.trilogy.product.bundle.manager.provision.v5_0.bucket.BucketRetrievalReturnParam;
import com.trilogy.product.bundle.manager.provision.v5_0.bucket.BucketRetrievalReturnParamV2;
import com.trilogy.product.bundle.manager.provision.v5_0.bucket.BucketRetrievalWithSummaryReturnParam;
import com.trilogy.product.bundle.manager.provision.v5_0.bucket.BucketRetrievalWithSummaryReturnParamV2;
import com.trilogy.product.bundle.manager.provision.v5_0.bucket.BucketReturnParam;
import com.trilogy.product.bundle.manager.provision.v5_0.bucket.BucketServiceReturnParam;
import com.trilogy.product.bundle.manager.provision.v5_0.bucket.SubscriptionBalanceReturnParam;
import com.trilogy.product.bundle.manager.provision.v5_0.bucket.QueryType;
import com.trilogy.product.bundle.manager.provision.v5_0.bucket.StatusType;
import com.trilogy.product.bundle.manager.provision.v5_0.bucket.SubscriberBucket;
import com.trilogy.product.bundle.manager.provision.v5_0.bucket.SubscriberBucketProvision;
import com.trilogy.product.bundle.manager.provision.v5_0.bucket.SubscriberBucketRetrieval;
import com.trilogy.product.bundle.manager.provision.v5_0.bucket.SubscriptionBalanceReturnParamV2;
import com.trilogy.product.bundle.manager.provision.common.param.Parameter;
import com.trilogy.product.bundle.manager.provision.common.type.YearDateTime;
import com.trilogy.product.bundle.manager.provision.v5_0.bucket.AuthHeader;
import com.trilogy.product.bundle.manager.provision.v5_0.bucket.EntityBundleDetails;
import com.trilogy.product.bundle.manager.provision.v5_0.bucket.EntityReference;
import com.trilogy.product.bundle.manager.provision.common.param.Parameter;


public class TestSubscriberBucketProvision implements SubscriberBucketProvision
{
    private static final long serialVersionUID = 1L;
    
    Map<Long, SubscriberBucket> buckets_ = new HashMap<Long, SubscriberBucket>();
    
    public BucketReturnParam[] changeBCD(String msisdn, long bundleId, int subscriptionType, int newBCD, boolean flag, Parameter[] aparameter)
    {
        BucketReturnParam[] ret = new BucketReturnParam[1];
        ret[0] = new BucketReturnParam();
        
        return ret;
    }


    public BucketReturnParam createBucket(String msisdn, long bundleId, int subscriptionType, Parameter[] parameter)
    {
        BucketReturnParam ret = new BucketReturnParam();
        
        if (!buckets_.containsKey(Long.valueOf(bundleId)))
        {
            SubscriberBucket bucket = new SubscriberBucket();
            bucket.msisdn = msisdn;
            bucket.bundleId = bundleId;
            buckets_.put(Long.valueOf(bundleId), bucket);
            ret.resultCode = 0;
        }
        else
        {
            ret.resultCode = -1;
        }
        return ret;
    }


    public short[] createBulkBuckets(String[] msisdn, long[] bundleId, int[] subscriptionTypes, Parameter[][] parameter)
    {
        short [] ret = new short[bundleId.length];
        for (int i = 0; i < bundleId.length; i ++)
        {
            if (!buckets_.containsKey(Long.valueOf(bundleId[i])))
            {
                SubscriberBucket bucket = new SubscriberBucket();
                bucket.msisdn = msisdn[i];
                bucket.bundleId = bundleId[i];
                buckets_.put(Long.valueOf(bundleId[i]), bucket);
                ret[i] = 0;
            }
            else
            {
                ret[i] = -1;
            }
        }    

        return ret;
    }


    public BucketReturnParam decreaseBalanceLimit(String msisdn, int spid, long bundleId, int subscriptionType,
            long balance, Parameter[] parameter)
    {
        BucketReturnParam ret = new BucketReturnParam();

        if (!buckets_.containsKey(Long.valueOf(bundleId)))
        {
            SubscriberBucket bucket = (SubscriberBucket) buckets_.get(Long.valueOf(bundleId));
            bucket.msisdn = msisdn;
            bucket.spid = spid;
            bucket.bundleId = bundleId;
            bucket.balanceLimit -= balance;
            ret.resultCode = 0;
        }
        else
        {
            ret.resultCode = -1;
        }
        return ret;
    }


    public BucketRetrievalReturnParam getAllSubscriberBalances(String msisdn, int subscriptionType)
    {
        BucketRetrievalReturnParam ret = new BucketRetrievalReturnParam();
        
        Iterator<Long> iter = buckets_.keySet().iterator();
        int index = 0;
        while (iter.hasNext())
        {
            SubscriberBucket bucket = buckets_.get(iter.next());
            if (bucket.msisdn.equals(msisdn))
            {
                SubscriberBucketRetrieval subscriberBucket = new SubscriberBucketRetrieval();
                subscriberBucket.regularBalance.personalLimit = bucket.balanceLimit;
                subscriberBucket.regularBalance.personalUsed = bucket.balanceUsed;
                subscriberBucket.regularBalance.rolloverLimit = bucket.rolloverBalanceLimit;
                subscriberBucket.regularBalance.rolloverUsed = bucket.rolloverBalanceUsed;

                if (ret.bucketRetrievalCollection == null)
                {
                    ret.bucketRetrievalCollection = new SubscriberBucketRetrieval[buckets_.keySet().size()];
                }
                ret.bucketRetrievalCollection[index] = subscriberBucket;
                index += 1;
            }
        }
        
        ret.resultCode = 0;
        
        return ret;
    }


    public BucketReturnParam getBucket(String msisdn, long bundleId, int subscriptionType, Parameter[] aparameter)
    {
        BucketReturnParam ret = new BucketReturnParam();
        
        if (!buckets_.containsKey(Long.valueOf(bundleId)))
        {
            SubscriberBucket bucket = (SubscriberBucket) buckets_.get(Long.valueOf(bundleId));
            ret.outSubscriberBucket = bucket;
            ret.resultCode = 0;
        }
        else
        {
            ret.resultCode = -1;
        }
        return ret;
    }


    public BucketReturnParam increaseBalanceLimit(String msisdn, int spid, long bundleId, int subscriptionType, long balance, Parameter[] parameter)
    {
        BucketReturnParam ret = new BucketReturnParam();
        
        if (!buckets_.containsKey(Long.valueOf(bundleId)))
        {
            SubscriberBucket bucket = (SubscriberBucket) buckets_.get(Long.valueOf(bundleId));
            bucket.msisdn = msisdn;
            bucket.spid = spid;
            bucket.bundleId = bundleId;
            bucket.balanceLimit += balance;
            ret.resultCode = 0;
        }
        else
        {
            ret.resultCode = -1;
        }
        return ret;
    }


    public BucketReturnParam removeBucket(String msisdn, long bundleId, int subscriptionType, Parameter[] aparameter)
    {
        BucketReturnParam ret = new BucketReturnParam();
        
        if (!buckets_.containsKey(Long.valueOf(bundleId)))
        {
            buckets_.remove(Long.valueOf(bundleId));
            ret.resultCode = 0;
        }
        else
        {
            ret.resultCode = -1;
        }
        return ret;
    }


    public short[] removeBulkBuckets(String[] msisdn, long[] bundleId, int[] subscriptionTypes, Parameter[][] parameter)
    {
        short [] ret = new short[bundleId.length];
        for (int i = 0; i < bundleId.length; i ++)
        {
            if (!buckets_.containsKey(Long.valueOf(bundleId[i])))
            {
                buckets_.remove(Long.valueOf(bundleId[i]));
                ret[i] = 0;
            }
            else
            {
                ret[i] = -1;
            }
        }    

        return ret;
    }


    public BucketServiceReturnParam removeSubscriberBucket(String msisdn, int spid, int subscriptionType, long bundleId)
    {
        BucketServiceReturnParam ret = new BucketServiceReturnParam();
        
        if (!buckets_.containsKey(Long.valueOf(bundleId)))
        {
            SubscriberBucket bucket = (SubscriberBucket) buckets_.get(Long.valueOf(bundleId));
            buckets_.remove(Long.valueOf(bundleId));
            ret.resultCode = 0;
        }
        else
        {
            ret.resultCode = -1;
        }
        return ret;
    }


    public BucketServiceReturnParam switchBundles(String msisdn, int spid, int subscriptionType, long[] bundleIds,
            long[] newBundleIds, Parameter[][] aparameter)
    {
        // switchBundles(msisdn, spid, oldBundleIds, newBundleIds,
        BucketServiceReturnParam ret = new BucketServiceReturnParam();

        for (int index = 0; index < bundleIds.length; index ++)
        {
            if (buckets_.containsKey(Long.valueOf(bundleIds[index])))
            {
                SubscriberBucket bucket = (SubscriberBucket) buckets_.get(Long.valueOf(bundleIds[index]));
                bucket.bundleId = newBundleIds[index];
                buckets_.remove(Long.valueOf(newBundleIds[index]));
                buckets_.put(Long.valueOf(newBundleIds[index]), bucket);
                ret.resultCode = 0;
            }
            else
            {
                ret.resultCode = -1;  
            }
        }    

        return ret;
    }


    public BucketReturnParam updateBucketStatus(String msisdn, StatusType statustype, int subscriptionType,
            Parameter[] aparameter)
    {
        BucketReturnParam ret = new BucketReturnParam();
        ret.resultCode = -1;
        Iterator<Long> iter = buckets_.keySet().iterator();
        int index = 0;
        while (iter.hasNext())
        {
            Long bundleId = iter.next();
            SubscriberBucket bucket = (SubscriberBucket) buckets_.get(bundleId);
            if (bucket.msisdn.equals(msisdn))
            {
                bucket.status = statustype;
                buckets_.put(bundleId, bucket);
                ret.resultCode = 0;
            }
        }
        return ret;
    }

    public BucketHistoryReturnParam getBucketUsageHistory(String msisdn, int subscriptionType, YearDateTime startTime, YearDateTime endTime, Parameter[] inParamSet)
    {
        return null;
    }
    
    public SubscriptionBalanceReturnParam listSubscriptionBalances(String msisdn, QueryType queryType, int subscriptionTypeId, Parameter[] inParamSet)
    {
    	return null;
    }
    
    public Request _create_request(Context context, String s, NVList nvlist, NamedValue namedvalue)
    {
        return null;
    }


    public Request _create_request(Context context, String s, NVList nvlist, NamedValue namedvalue,
            ExceptionList exceptionlist, ContextList contextlist)
    {
        return null;
    }


    public Object _duplicate()
    {
        return null;
    }


    public DomainManager[] _get_domain_managers()
    {
        return null;
    }


    public Object _get_interface_def()
    {
        return null;
    }


    public Policy _get_policy(int i)
    {
        return null;
    }


    public int _hash(int i)
    {
        return 0;
    }


    public boolean _is_a(String s)
    {
        return false;
    }


    public boolean _is_equivalent(Object obj)
    {
        return false;
    }


    public boolean _non_existent()
    {
        return false;
    }


    public void _release()
    {

    }


    public Request _request(String s)
    {
        return null;
    }


    public Object _set_policy_override(Policy[] apolicy, SetOverrideType setoverridetype)
    {
        return null;
    }


    @Override
    public BucketRetrievalWithSummaryReturnParam getAllSubscriberBalancesWithSummary(
            String msisdn)
    {
        return new BucketRetrievalWithSummaryReturnParam();
    }
    

    /* (non-Javadoc)
     * @see com.redknee.product.bundle.manager.provision.v5_0.bucket.SubscriberBucketProvisionOperations#getAllSubscriberBalancesV2(java.lang.String, int)
     */
    @Override
    public BucketRetrievalReturnParamV2 getAllSubscriberBalancesV2(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    /* (non-Javadoc)
     * @see com.redknee.product.bundle.manager.provision.v5_0.bucket.SubscriberBucketProvisionOperations#getAllSubscriberBalancesWithSummaryV2(java.lang.String)
     */
    @Override
    public BucketRetrievalWithSummaryReturnParamV2 getAllSubscriberBalancesWithSummaryV2(String arg0)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    /* (non-Javadoc)
     * @see com.redknee.product.bundle.manager.provision.v5_0.bucket.SubscriberBucketProvisionOperations#listSubscriptionBalancesV2(java.lang.String, com.redknee.product.bundle.manager.provision.v5_0.bucket.QueryType, int, com.redknee.product.bundle.manager.provision.common.param.Parameter[])
     */
    @Override
    public SubscriptionBalanceReturnParamV2 listSubscriptionBalancesV2(String arg0, QueryType arg1, int arg2,
            Parameter[] arg3)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public BucketReturnParam truncateBucket (AuthHeader authH, String msisdn, 
    		String bundleID, int subscriptionType, Parameter[] arg3)
    {
    	return null;
    }
    
    @Override
    public BucketReturnParam updateBucketStatus_Auth (AuthHeader authH, String msisdn, StatusType status, int subscriptionType, Parameter[] arg3)
    {
    	return null;
    }
    
    @Override
    public EntityBundleDetails getAccountsBalancesWithSummary(EntityReference[] entityReference,Parameter[] inParamSet)
    {
        return new EntityBundleDetails();
    }
    
    
}
