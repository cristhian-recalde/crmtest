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

package com.trilogy.app.crm.home.sub;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeOperationEnum;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.support.SubscriberSupport;

public class PackageHomeValidator implements Validator
{
    protected static final PackageHomeValidator INSTANCE = new PackageHomeValidator();

    private Validator createValidator_;
    private Validator storeValidator_;
    private Validator defaultValidator_;

    private PackageHomeValidator()
    {
        createValidator_ = new Validator()
        {
            public void validate(final Context ctx, final Object obj) throws IllegalStateException
            {
                final Subscriber sub = (Subscriber) obj;
                try
                {
                    Claim.validatePackageTypeAndAvailable(ctx, sub);
                }
                catch (ClaimHomeException chEx)
                {
                    final CompoundIllegalStateException el = new CompoundIllegalStateException();
                    final IllegalPropertyArgumentException ex = new IllegalPropertyArgumentException(
                            "Subscriber.packageId",
                            "Package not available: " + sub.getPackageId());
                    el.thrown(ex);
                    el.throwAll();
                }
                catch (Throwable t)
                {
                    final CompoundIllegalStateException el = new CompoundIllegalStateException();
                    final IllegalPropertyArgumentException ex = new IllegalPropertyArgumentException(
                            "Subscriber.packageId",
                            "Fail to validate package." + sub.getPackageId() + " [" + t.getMessage() + "]");
                    el.thrown(ex);
                    el.throwAll();
                }
            }
        };

        defaultValidator_ = createValidator_;

        storeValidator_ = new Validator()
        {
            public void validate(final Context ctx, final Object obj) throws IllegalStateException
            {
                final Subscriber newSub = (Subscriber) obj;
                Subscriber oldSub = null;
                try
                {
                    oldSub = SubscriberSupport.lookupSubscriberForSubId(ctx, newSub.getId());

                    if (!SafetyUtil.safeEquals(oldSub.getPackageId(), newSub.getPackageId()))
                    {
                        Claim.validatePackageTypeAndAvailable(ctx, newSub);
                    }
                }
                catch (ClaimHomeException chEx)
                {
                    final CompoundIllegalStateException el = new CompoundIllegalStateException();
                    final IllegalPropertyArgumentException ex = new IllegalPropertyArgumentException(
                            "Subscriber.packageId",
                            "Package not available." + newSub.getPackageId());
                    el.thrown(ex);
                    el.throwAll();
                }
                catch (Throwable t)
                {
                    final CompoundIllegalStateException el = new CompoundIllegalStateException();
                    final IllegalPropertyArgumentException ex = new IllegalPropertyArgumentException(
                            "Subscriber.packageId",
                            "Fail to validate package." + newSub.getPackageId() + " [" + t.getMessage() + "]");
                    el.thrown(ex);
                    el.throwAll();
                }
            }
        };
    }

    public static PackageHomeValidator instance()
    {
        return INSTANCE;
    }

    public Validator getCreateValidator()
    {
        return createValidator_;
    }

    public Validator getStoreValidator()
    {
        return storeValidator_;
    }

    public void validate(final Context ctx, final Object obj) throws IllegalStateException
    {
        final HomeOperationEnum op = (HomeOperationEnum) ctx.get(HomeOperationEnum.class);

        if (op == null)
        {
            defaultValidator_.validate(ctx, obj);
            return;
        }

        if (op == HomeOperationEnum.CREATE)
        {
            createValidator_.validate(ctx, obj);
        }
        else if (op == HomeOperationEnum.STORE)
        {
            storeValidator_.validate(ctx, obj);
        }
        else
        {
            defaultValidator_.validate(ctx, obj);
        }
    }
}
