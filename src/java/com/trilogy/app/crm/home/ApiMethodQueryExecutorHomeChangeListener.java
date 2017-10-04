/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily 
 * available. Additionally, source code is, by its very nature, confidential 
 * information and inextricably contains trade secrets and other information 
 * proprietary, valuable and sensitive to Redknee, no unauthorised use, 
 * disclosure, manipulation or otherwise is permitted, and may only be used 
 * in accordance with the terms of the licence agreement entered into with 
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved. 
 */
package com.trilogy.app.crm.home;

import com.trilogy.app.crm.api.queryexecutor.ApiMethodQueryExecutor;
import com.trilogy.app.crm.api.queryexecutor.QueryExecutorFactory;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xhome.home.HomeChangeEvent;
import com.trilogy.framework.xhome.home.HomeChangeListener;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.NotifyingHomeItem;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * @author Marcio Marques
 * @since 9.1.3
 */
public class ApiMethodQueryExecutorHomeChangeListener extends ContextAwareSupport implements HomeChangeListener
{
    public ApiMethodQueryExecutorHomeChangeListener(Context ctx)
    {
        super();
        setContext(ctx);
    }


    /**
     * @{inheritDoc
     */
    public void homeChange(HomeChangeEvent homechangeEvent)
    {
        if (homechangeEvent.getSource() instanceof ApiMethodQueryExecutor
                || (homechangeEvent.getSource() instanceof NotifyingHomeItem && ((NotifyingHomeItem) homechangeEvent
                        .getSource()).getNewObject() instanceof ApiMethodQueryExecutor))
        {
            Context ctx = getContext().createSubContext();

            ApiMethodQueryExecutor methodQueryExecutor = null;
            if (homechangeEvent.getSource() instanceof ApiMethodQueryExecutor)
            {
                methodQueryExecutor = (ApiMethodQueryExecutor) homechangeEvent.getSource();
            }
            else
            {
                methodQueryExecutor = (ApiMethodQueryExecutor) ((NotifyingHomeItem) homechangeEvent.getSource()).getNewObject();
            }

            try
            {
                QueryExecutorFactory.getInstance().reloadMethodQueryExecutor(ctx, methodQueryExecutor.getApiInterface(), methodQueryExecutor.getApiMethod());
            }
            catch (HomeException e)
            {
                LogSupport
                        .minor(ctx,
                                this,
                                "Unable to reload Method Executor for interface '" + methodQueryExecutor.getApiInterface() + "' and method '"
                                + methodQueryExecutor.getApiMethod() + "': "
                                        + e.getMessage(), e);
            }

        }
    }

}
