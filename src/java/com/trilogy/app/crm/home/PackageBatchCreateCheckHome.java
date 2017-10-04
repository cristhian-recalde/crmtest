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
package com.trilogy.app.crm.home;

import java.util.regex.Pattern;

import com.trilogy.app.crm.bean.PackageBatchConfigruation;
import com.trilogy.app.crm.bean.PackageBulkTask;
import com.trilogy.app.crm.bean.PackageBulkTaskXInfo;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.MajorLogMsg;


/**
 * 
 * @author simar.singh@redknee.com
 * 
 */
public class PackageBatchCreateCheckHome extends HomeProxy
{

    public PackageBatchCreateCheckHome(Home delegate)
    {
        super(delegate);
    }

    private static final long serialVersionUID = 1L;


    @Override
    public Object create(Context ctx, Object bean) throws HomeException, HomeInternalException
    {
        try
        {
            if (null != bean && !(bean instanceof PackageBulkTask))
            {
                throw new IllegalArgumentException("Expects an instance of [" + PackageBulkTask.class.getName()
                        + "] but got [" + ((bean == null) ? "null" : bean.getClass()) + "]");
            }
            final PackageBulkTask task = (PackageBulkTask) bean;
            CompoundIllegalStateException exceptions = new CompoundIllegalStateException();
            fillInException(ctx, task, exceptions);
            if (exceptions.getSize() > 0)
            {
                throw new HomeException(exceptions);
            }
            return getDelegate(ctx).create(ctx, bean);
        }
        catch (Throwable t)
        {
            if (t instanceof HomeException)
            {
                throw (HomeException) t;
            }
            else
            {
                throw new HomeException("Entry could not be created", t);
            }
        }
    }


    private ExceptionListener fillInException(Context ctx, PackageBulkTask task, ExceptionListener exceptions)
            throws HomeException
    {
        final PackageBatchConfigruation config = (PackageBatchConfigruation) ctx.get(PackageBatchConfigruation.class);
        if (HomeSupportHelper.get(ctx).getBeanCount(ctx, PackageBulkTask.class,
                new EQ(PackageBulkTaskXInfo.BATCH_ID, task.getBatchId())) > 0)
        {
            throw new HomeException("An entry with Batch ID [" + task.getBatchId()
                    + "] already existis. Please chose a unique Batch ID.");
        }
        fillInExceptionsForBatchId(task.getBatchId(), config, exceptions);
        fillInExceptionsForBatchPin(task.getBatchPin(), config, exceptions);
        return exceptions;
    }


    private ExceptionListener fillInExceptionsForBatchId(String batchId, PackageBatchConfigruation config,
            ExceptionListener exceptions)
    {
        {
            final int maxLength = config.getBatchIdMaxLength();
            if (maxLength > 0 && batchId.length() > maxLength)
            {
                exceptions.thrown(new IllegalStateException("Batch ID [" + batchId.length()
                        + "] can not be longer than configured Maximum [" + maxLength + "]"));
            }
        }
        {
            final int minLength = config.getBatchIdMinLength();
            if (minLength > 0 && batchId.length() < minLength)
            {
                exceptions.thrown(new IllegalStateException("Batch ID [" + batchId.length()
                        + "] can not be shorter than configured Minimum [" + minLength + "]"));
            }
        }
        {
            String regexPattern = config.getBatchIdRegexPattern();
            if (!regexPattern.isEmpty())
            {
                if (!Pattern.matches(regexPattern, batchId))
                {
                    exceptions.thrown(new IllegalStateException("Batch ID [" + batchId
                            + "] does not match configured Reg-Ex Pattern [" + regexPattern + "]"));
                }
            }
        }
        return exceptions;
    }


    private ExceptionListener fillInExceptionsForBatchPin(String batchPin, PackageBatchConfigruation config,
            ExceptionListener exceptions)
    {
        {
            final int maxLength = config.getBatchPinMaxLength();
            if (maxLength > 0 && batchPin.length() > maxLength)
            {
                exceptions.thrown(new IllegalStateException("Batch PIN [" + batchPin.length()
                        + "] can not be longer than configured Maximum [" + maxLength + "]"));
            }
        }
        {
            final int minLength = config.getBatchPinMinLength();
            if (minLength > 0 && batchPin.length() < minLength)
            {
                exceptions.thrown(new IllegalStateException("Batch PIN [" + batchPin.length()
                        + "] can not be shorter than configured Minimum [" + minLength + "]"));
            }
        }
        {
            String regexPattern = config.getBatchPinRegexPattern();
            if (!regexPattern.isEmpty())
            {
                if (!Pattern.matches(regexPattern, batchPin))
                {
                    exceptions.thrown(new IllegalStateException("Batch PIN [" + batchPin
                            + "] does not match configured Reg-Ex Pattern [" + regexPattern + "]"));
                }
            }
        }
        return exceptions;
    }


    private void handleException(Context ctx, Throwable t)
    {
        ExceptionListener excl = (ExceptionListener) ctx.get(ExceptionListener.class);
        if (null != excl)
        {
            excl.thrown(t);
        }
        new MajorLogMsg(this, t.getMessage(), t).log(ctx);
    }


    private void handleException(Context ctx, String message, Throwable t)
    {
        handleException(ctx, new IllegalStateException(message, t));
    }
}
