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
package com.trilogy.app.crm.transfer;

import java.io.IOException;

import javax.servlet.ServletException;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.OMLogMsg;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.support.TransferExceptionSupport;

/**
 * @author ling.tang@redknee.com
 */
final public class ReprocessTransferExceptionService
{
    private static ReprocessTransferExceptionService instance;

    private ReprocessTransferExceptionService()
    {
    }

    public static ReprocessTransferExceptionService instance()
    {
        if (instance == null)
        {
            instance = new ReprocessTransferExceptionService();
        }
        return instance;
    }

    public void service(final Context ctx, final TransferException exception) throws ServletException, IOException
    {
        final Context subCtx = ctx.createSubContext();
        try
        {
            TransferExceptionSupport.processTransferException(subCtx, exception);

            // Update Transfer Exception after successful transaction save
            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, this, "Creating Transfer Transaction was successful. "
                        + " Marking the Transfer Exception record=" + exception.getId() + " as CORRECTED");
            }

            TransferExceptionSupport.updateTransferExceptionState(ctx, exception, TransferFailureStateEnum.CORRECTED);
            TransferExceptionSupport.updateTransferExceptionCounters(ctx, exception.getId(), null);

            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, this, "Successfully marked the Transfer Exception record=" + exception.getId()
                        + " as CORRECTED");
            }

            //Log OMs for Transfer Exception Resolution.
            new OMLogMsg(Common.OM_MODULE, Common.OM_TRANSFER_EXCEPTION_RESOLVED, 1).log(ctx);
        }
        catch (ReprocessException re)
        {
            final ServletException se = new ServletException(re);
            throw se;
        }
        catch (HomeException e)
        {
            final ServletException se = new ServletException("Failed to mark the Transfer Exception record as "
                    + "CORRECTED after processing was successful.", e);
            throw se;
        }
    }
}
