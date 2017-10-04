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

package com.trilogy.app.crm.state;

import com.trilogy.app.crm.bas.recharge.SuspensionSupport;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * @author bdhavalshankh
 * @since 9.6
 */
public class ResumeExternalPriceplanServiceUpdateAgent implements ServiceStateUpdateAgent
{
    @Override
    public void update(Context ctx, Subscriber subscriber, Service service) throws HomeException
    {
        int result = SuspensionSupport.suspendGenericService(ctx, subscriber, service, false, true);
        if(result == SuspensionSupport.EXTERNAL_FAIL)
        {
            String msg =  " Failure result code returned: " +result+ ". Can not update Subscriber. "+ 
                          " Error occurred while executing resume commands for external" +
                          " priceplan service.  ";
            
            LogSupport.minor(ctx, this, msg);
            throw new HomeException(msg);
        }
    }
}
