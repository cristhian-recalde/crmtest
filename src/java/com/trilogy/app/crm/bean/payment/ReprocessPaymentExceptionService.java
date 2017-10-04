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
package com.trilogy.app.crm.bean.payment;

import java.io.IOException;

import javax.servlet.ServletException;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.support.PaymentExceptionSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.OMLogMsg;

/**
 * Service accepts parameters from ReprocessPaymentExceptionWebAgent
 * in order to create a new Payment Transaction for the requested
 * Payment Exception record.
 * 
 * @author Angie Li
 *
 */
public class ReprocessPaymentExceptionService  
{

    private ReprocessPaymentExceptionService() {}
    
    public static ReprocessPaymentExceptionService instance()
    {
        if (instance_ == null)
        {
            instance_ = new ReprocessPaymentExceptionService();
        }
        return instance_;
    }
    
    public void service(Context ctx, PaymentException exception) throws ServletException, IOException 
    {
        Context subCtx = ctx.createSubContext();
        try
        {
            PaymentExceptionSupport.createTransactionRecord(subCtx, exception);
            
            // Remove Payment Exception after successful transaction save
            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, this, "Creating Payment Transaction was successful.  Begin deleting the Payment Exception record=" + exception.getId());
            }
            
            Home paymentExceptionHome = (Home) ctx.get(PaymentExceptionHome.class);
            paymentExceptionHome.remove(exception);

            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, this, "Successfully deleted the Payment Exception record=" + exception.getId());
            }
            
            //Log OMs for Payment Exception Resolution.
            new OMLogMsg(Common.OM_MODULE, Common.OM_PAYMENT_EXCEPTION_RESOLVED, 1).log(ctx);
        }
        catch (ReprocessException re)
        {
            ServletException se = new ServletException(re);
            throw se;
        }
        catch (HomeException e)
        {
            ServletException se = new ServletException("Failed to delete the Payment Exception record after processing was successful.", e);
            throw se;
        }
    }

    private static ReprocessPaymentExceptionService instance_ = null; 
}
