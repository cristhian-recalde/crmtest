/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home.pipelineFactory;

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.framework.core.platform.CoreSupport;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.cluster.RMIClusteredHome;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.NEQ;
import com.trilogy.framework.xhome.home.AdapterHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeValidator;
import com.trilogy.framework.xhome.home.NotifyingHome;
import com.trilogy.framework.xhome.home.ValidatingHome;
import com.trilogy.framework.xhome.relationship.NoRelationshipRemoveHome;

import com.trilogy.app.crm.bean.account.SubscriptionClassHome;
import com.trilogy.app.crm.bean.account.SubscriptionClassXInfo;
import com.trilogy.app.crm.bean.account.SubscriptionTypeHome;
import com.trilogy.app.crm.bean.account.SubscriptionTypeXInfo;
import com.trilogy.app.crm.bean.core.SubscriptionType;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.support.ConfigChangeRequestSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.xhome.adapter.ExtendedBeanAdapter;


public class SubscriptionTypeHomePipelineFactory implements PipelineFactory
{
    /**
     * Singleton instance.
     */
    private static SubscriptionTypeHomePipelineFactory instance_;


    /**
     * Create a new instance of <code>SubscriptionTypeHomePipelineFactory</code>.
     */
    protected SubscriptionTypeHomePipelineFactory()
    {
        // empty
    }


    /**
     * Returns an instance of <code>SubscriptionTypeHomePipelineFactory</code>.
     * 
     * @return An instance of <code>SubscriptionTypeHomePipelineFactory</code>.
     */
    public static SubscriptionTypeHomePipelineFactory instance()
    {
        if (instance_ == null)
        {
            instance_ = new SubscriptionTypeHomePipelineFactory();
        }
        return instance_;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Home createPipeline(Context ctx, Context serverCtx) throws RemoteException, HomeException, IOException,
            AgentException
    {
        Home home = CoreSupport.bindHome(ctx, SubscriptionType.class);
        
        home = new NotifyingHome(home);

        home = new RMIClusteredHome(ctx, SubscriptionTypeHome.class.getName(), home);
        
       // home = new ValidatingHome(new SubscriptionTypeUniquenessHomeValidator(), home);
        
        home = new NoRelationshipRemoveHome(ctx, SubscriptionTypeXInfo.ID, SubscriptionClassXInfo.SUBSCRIPTION_TYPE,
                SubscriptionClassHome.class,
                "This Subscription Type is in use.  Cannot delete this Subscription Type.", home);

        // Install a home to adapt between core business logic bean and CRM specific bean
        home = new AdapterHome(
                ctx, 
                home, 
                new ExtendedBeanAdapter<com.redknee.app.crm.bean.core.SubscriptionType, com.redknee.app.crm.bean.account.CRMSubscriptionType>(
                        com.redknee.app.crm.bean.core.SubscriptionType.class, 
                        com.redknee.app.crm.bean.account.CRMSubscriptionType.class));
        
		home =
		    ConfigChangeRequestSupportHelper
		        .get(ctx)
		        .registerHomeForConfigSharing(ctx, home, SubscriptionType.class);
        return home;
    }

    /**
     * 
     * @author ssimar this class provides a home-validator that ensure's uniqueness of
     *         Type attribute in SubscriptionType home
     */
    private class SubscriptionTypeUniquenessHomeValidator implements HomeValidator
    {

        @Override
        public Validator getCreateValidator()
        {
            // TODO Auto-generated method stub
            return new Validator()
            {

                @Override
                public void validate(Context ctx, Object obj) throws IllegalStateException
                {
                    final CompoundIllegalStateException el = new CompoundIllegalStateException();
                    try
                    {
                        SubscriptionType existingEntry = SubscriptionType.getSubscriptionType(ctx, ((SubscriptionType) obj).getTypeEnum());
                        if (existingEntry != null)
                        {
                            el.thrown(new IllegalPropertyArgumentException(SubscriptionTypeXInfo.TYPE,
                                    "An entry already exists for  the Subscription-Type. Please remove the enty ["
                                            + existingEntry.getId() + "] - [" + existingEntry.getName()
                                            + "] first."));
                        }
                    }
                    catch (HomeException e)
                    {
                        el.thrown(new IllegalStateException(
                                "Operation Failed! Uniqueness check on Subscription-Type could not be performed because of ["
                                        + e.getMessage() + "]", e));
                    }
                    el.throwAll();
                }
            };
        }


        @Override
        public Validator getStoreValidator()
        {
            final CompoundIllegalStateException el = new CompoundIllegalStateException();
            // TODO Auto-generated method stub
            return new Validator()
            {

                @Override
                public void validate(Context ctx, Object obj) throws IllegalStateException
                {
                    try
                    {
                        SubscriptionType newEntry = (SubscriptionType) obj;
                        
                        And existingFilter = new And();
                        existingFilter.add(new EQ(SubscriptionTypeXInfo.TYPE, newEntry.getType()));
                        existingFilter.add(new NEQ(SubscriptionTypeXInfo.ID, newEntry.getId()));
                        
                        SubscriptionType existingEntry = HomeSupportHelper.get(ctx).findBean(ctx, SubscriptionType.class, existingFilter);
                        
                        // ensure that self-updates do not fail
                        if (null != existingEntry)
                        {
                            el.thrown(new IllegalPropertyArgumentException(SubscriptionTypeXInfo.TYPE,
                                    "An entry already exists for the Subscription-Type. Please remove the enty ["
                                            + existingEntry.getId() + "] - [" + existingEntry.getName() + "] first."));
                        }
                    }
                    catch (HomeException e)
                    {
                        el.thrown(new IllegalStateException(
                                "Operation Failed! Uniqueness check on Subscription-Type could not be performed because of ["
                                        + e.getMessage() + "]", e));
                    }
                    el.throwAll();
                }
            };
        }
    }
}
