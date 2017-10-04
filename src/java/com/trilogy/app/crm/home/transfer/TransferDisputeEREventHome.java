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

package com.trilogy.app.crm.home.transfer;

import com.trilogy.app.crm.log.ERLogger;
import com.trilogy.app.crm.transfer.TransferDispute;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;

/**
 * 
 * @author ssimar
 *
 */

public class TransferDisputeEREventHome extends HomeProxy
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    public TransferDisputeEREventHome(Context ctx, Home delegate)
    {
        super(ctx, delegate);
    }


    /**
     * 
     */
    public Object create(Context ctx, Object bean) throws HomeException, HomeInternalException
    {
        TransferDispute dispute = null;
        try
        {
            dispute = (TransferDispute) super.create(ctx, bean);
            return dispute;
        }
        catch (HomeException e)
        {
            throw e;
        }
        finally
        {
            if (dispute != null)
            {
                ERLogger.generateTransferDisputEr(ctx, dispute, EVENT_CREATE, RESULT_CODE_SUCCESS);
            }
            else
            {
                ERLogger.generateTransferDisputEr(ctx, (TransferDispute) bean, EVENT_CREATE, RESULT_CODE_FAIL);
            }
        }
    }


    /*
     * @Override(non-Javadoc)
     * 
     * @see
     * com.redknee.framework.xhome.home.HomeProxy#store(com.redknee.framework.xhome.context
     * .Context, java.lang.Object)
     */
    public Object store(Context ctx, Object bean) throws HomeException, HomeInternalException
    {
        TransferDispute dispute = null;
        try
        {
            dispute = (TransferDispute) super.store(ctx, bean);
            return dispute;
        }
        catch (HomeException e)
        {
            throw e;
        }
        finally
        {
            if (dispute != null)
            {
                ERLogger.generateTransferDisputEr(ctx, dispute, EVENT_UPDATE, RESULT_CODE_SUCCESS);
            }
            else
            {
                ERLogger.generateTransferDisputEr(ctx, (TransferDispute) bean, EVENT_UPDATE, RESULT_CODE_FAIL);
            }
        }
    }


    /*
     * \@Override(non-Javadoc)
     * 
     * @see
     * com.redknee.framework.xhome.home.HomeProxy#remove(com.redknee.framework.xhome.context
     * .Context, java.lang.Object)
     */
    public void remove(Context ctx, Object bean) throws HomeException, HomeInternalException
    {
        TransferDispute dispute = null;
        try
        {
            super.remove(ctx, bean);
            dispute = (TransferDispute) bean;
        }
        catch (HomeException e)
        {
            throw e;
        }
        finally
        {
            if (dispute != null)
            {
                ERLogger.generateTransferDisputEr(ctx, dispute, EVENT_REMOVE, RESULT_CODE_SUCCESS);
            }
            else
            {
                ERLogger.generateTransferDisputEr(ctx, (TransferDispute) bean, EVENT_REMOVE, RESULT_CODE_FAIL);
            }
        }
    }

    public static final String EVENT_CREATE = "C";
    public static final String EVENT_UPDATE = "U";
    public static final String EVENT_REMOVE = "R";
    public static final int RESULT_CODE_SUCCESS = 0;
    public static final int RESULT_CODE_FAIL = 1;
}
