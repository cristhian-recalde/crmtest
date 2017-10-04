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
package com.trilogy.app.crm.bundle.validator;

import java.util.Collection;
import java.util.Map;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bundle.GroupChargingTypeEnum;
import com.trilogy.app.crm.bundle.SubscriberBundleSupport;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MinorLogMsg;


/**
 * Provides bean-level validation for the Subscriber's calling group (PLP/CUG)
 * information.
 * 
 * @author victor.stratan@redknee.com
 */
public class MemberGroupBundleValidator implements Validator
{

    /**
     * Creates a new CallingGroupValidator.
     */
    private MemberGroupBundleValidator()
    {
    }


    /**
     * Gets the singleton instance of this class.
     * 
     * @return The singleton instance of this class.
     */
    public static MemberGroupBundleValidator instance()
    {
        return instance_;
    }


    /**
     * {@inheritDoc}
     */
    public void validate(final Context ctx, final Object obj)
    {
        try
        {
            final Subscriber subscriber = (Subscriber) obj;
            validateGroupBundles(ctx, subscriber);
            validateMemberBundles(ctx, subscriber);
        }
        catch (Throwable t)
        {
            new MinorLogMsg(this, "Failed at balidation of Member - Group Bundle", t).log(ctx);
            throw new IllegalStateException(t.getMessage(), t);
        }
    }


    /**
     * Validate Group Bundles - They are allowed only on the pooled leader
     * 
     * @param ctx
     * @param newSub
     * @throws HomeException
     */
    private void validateGroupBundles(final Context ctx, final Subscriber newSub) throws HomeException
    {
        if (newSub.isPooledGroupLeader(ctx))
        {
            final Map newBundles = newSub.getBundles();
            if (newBundles != null && newBundles.size() > 0)
            {
                final Collection groupBundles = SubscriberBundleSupport.selectBundlesFromListOfType(ctx,
                        newBundles.keySet(), GroupChargingTypeEnum.GROUP_BUNDLE);
                if (groupBundles == null || groupBundles.size() != newBundles.size())
                {
                    throw new IllegalStateException("Invalid Bundle Assignment. A Pool can only have Group Bundles");
                }
            }
        }
    }


    /**
     * Validate Member Bundles - On Pool Members
     * 
     * @param ctx
     *            The operating context.
     * @param subscriber
     *            The subscriber to validate.
     * @throws HomeException
     * 
     * @exception IllegalArgumentException
     *                Thrown if the subscriber has selected member bundles of a bundle
     *                group that is not assigned to the group leader or there is no group
     *                leader.
     */
    private void validateMemberBundles(final Context ctx, final Subscriber subscriber) throws HomeException
    {
        if (subscriber.isPooledMemberSubscriber(ctx))
        {
            final Map newBundles = subscriber.getBundles();
            if (newBundles != null && newBundles.size() > 0)
            {
                final Collection groupBundles = SubscriberBundleSupport.selectBundlesFromListOfType(ctx,
                        newBundles.keySet(), GroupChargingTypeEnum.GROUP_BUNDLE);
                if (groupBundles != null & groupBundles.size() > 0)
                {
                    throw new IllegalStateException(
                            "Invalid Bundle Assignment. A Pool Member Subscriber can not have Group Bundles");
                }
            }
        }
    }

    /**
     * The singleton instance of this class.
     */
    private static final MemberGroupBundleValidator instance_ = new MemberGroupBundleValidator();
    private static final String BUNDLES_PROPERTY_NAME = "Bundles";
} // class