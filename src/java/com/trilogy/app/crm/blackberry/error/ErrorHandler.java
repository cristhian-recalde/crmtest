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
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */

package com.trilogy.app.crm.blackberry.error;

import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.service.blackberry.model.ResultEnum;

/**
 * Handles any error occurred in the BlackBerry connection 
 * @author arturo.medina@redknee.com
 *
 */
public interface ErrorHandler
{
    /**
     * Handles any error occurred in the Blackberry service
     * @param ctx
     * @param subscriber
     * @param service 
     * @param resultStatus 
     * @param errorCode 
     * @param description 
     */
    public void handleError(Context ctx, 
            Subscriber subscriber,
            Service service,
            ResultEnum resultStatus,
            String errorCode,
            String description);
}
