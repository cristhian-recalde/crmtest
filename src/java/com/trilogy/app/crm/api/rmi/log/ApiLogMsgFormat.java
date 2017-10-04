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
package com.trilogy.app.crm.api.rmi.log;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xlog.format.FastLogMsgFormat;
import com.trilogy.framework.xlog.log.LogMsg;
import com.trilogy.framework.xlog.logger.LoggerSupport;

import com.trilogy.app.crm.api.Constants;
import com.trilogy.util.crmapi.wsdl.v2_0.types.CRMRequestHeader;


/**
 * Custom formatter for API transactions
 * 
 * date time [thread] [TXID] [API.methodName] severity class - message
 * 2002-10-29 00:23:32,234 [main] [TXID 123A] [API.createAccount()] INFO com.redknee.framework.xlog.format - formatting message
 *
 * @author aaron.gourley@redknee.com
 * @since 8.2
 */
public class ApiLogMsgFormat extends FastLogMsgFormat implements ContextAware
{   
    /**
     * @param ctx
     */
    public ApiLogMsgFormat(Context ctx)
    {
        super(ctx);
        setContext(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String format(LogMsg logMsg)
    {
        sb_.setLength(0);
        
        if ( logMsg.getTimestamp() != lastTime_ )
        {
          lastTime_ = logMsg.getTimestamp();
          date_.setTime(lastTime_);
          lastDate_ = dateFormat_.format(date_);
        }

        sb_.append(lastDate_);
        sb_.append(" [");
        sb_.append(logMsg.getUserId());
        sb_.append(":");
        sb_.append(logMsg.getThreadName());
        sb_.append("] ");

        final CRMRequestHeader header = (CRMRequestHeader) getContext().get(CRMRequestHeader.class);
        if (header != null)
        {
            sb_.append("[");
            sb_.append("TXID ");
            sb_.append(header.getTransactionID());
            sb_.append("] ");
        }
        
        final String method = (String) getContext().get(Constants.METHOD_NAME_CTX_KEY);
        if (method != null)
        {
            sb_.append("[");
            sb_.append("API.");
            sb_.append(method);
            sb_.append("()] ");
        }
        
        sb_.append(logMsg.getSeverity().getDescription());
        sb_.append(' ');
        
        sb_.append(logMsg.getSource());
        
        sb_.append(" - ");
        
        sb_.append(logMsg.getMessage());
        sb_.append(LoggerSupport.newline());
      
        return sb_.toString();
    }

    /**
     * {@inheritDoc}
     */
    public Context getContext()
    {
        return ctx_;
    }

    /**
     * {@inheritDoc}
     */
    public void setContext(Context ctx)
    {
        ctx_ = ctx;
    }

    protected Context ctx_ = null;
}
