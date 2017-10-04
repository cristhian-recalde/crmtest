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
package com.trilogy.app.crm.subscriber.provision;

import java.util.Collection;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.support.ServiceSupport;

/**
 * To backup subscriber provisioning informaiton.
 * 
 * @author jchen
 */
public class SubscriberServicesBackupHome extends HomeProxy
{
	/**
     * 
     */
    private static final long serialVersionUID = 1L;

    public SubscriberServicesBackupHome(Home delegate)
	{
		super(delegate);
	}

	   /**
     * @see com.redknee.framework.xhome.home.HomeSPI#store(com.redknee.framework.xhome.context.Context, java.lang.Object)
     */
    @Override
    public Object store(Context ctx, Object obj) throws HomeException 
    {
    	LogSupport.debug(ctx, this, "SubscriberPipeline[store].....");
    	Object updateObj = populateBackup(ctx, obj);
        super.store(ctx, updateObj);
        return updateObj;
    }
    
    
    /**
     * remove from data store
    * @param ctx
     * @param obj
     * @exception HomeException
     * @exception HomeInternalException
     */
    @Override
    public void remove(Context ctx, Object obj)
        throws HomeException,  HomeInternalException
    {
    	LogSupport.debug(ctx, this, "SubscriberPipeline[remove].....");
    	Object updatedObj = populateBackup(ctx, obj);
        super.remove(ctx, updatedObj);   
    }


    private Object populateBackup(Context ctx, Object obj) throws HomeException
    {
        Subscriber sub = (Subscriber) obj;
        if (sub.getContextInternal() == null)
        {
            sub.setContext(ctx);
        }
        sub.resetBackupServices();
        sub.getServicesBackup(ctx);
        sub.setPricePlanBackup(sub.getPricePlan());
        sub.resetProvisionedAuxServiceIdsBackup();
        sub.resetProvisionedAuxServiceBackup();
        sub.getProvisionedAuxServiceIdsBackup(ctx);
        sub.getProvisionedAuxServiceBackup(ctx);
        
        Collection provisionedBundleIds = ServiceSupport.transformServiceObjectToIds(sub.getProvisionedBundles());
        sub.getProvisionedBundles().clear();
        sub.bundleProvisioned(provisionedBundleIds);
        sub.getProvisionedBundleIdsBackup().clear();
        sub.getProvisionedBundleIdsBackup().addAll(provisionedBundleIds);
        sub.getProvisionedPackageIdsBackup().clear();
        sub.getProvisionedPackageIdsBackup().addAll(sub.getServicePackageIds(ctx));
        return sub;
    }
}
