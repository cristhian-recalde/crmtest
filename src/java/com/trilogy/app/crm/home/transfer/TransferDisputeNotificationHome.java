package com.trilogy.app.crm.home.transfer;

import com.trilogy.app.crm.support.SubscriptionNotificationSupport;
import com.trilogy.app.crm.transfer.TransferDispute;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;

public class TransferDisputeNotificationHome extends HomeProxy
{
    public TransferDisputeNotificationHome(Context ctx, Home delegate)
    {
        super(ctx, delegate);
    }

    public Object create(Context ctx, Object bean) throws HomeException,
            HomeInternalException
    {
        TransferDispute dispute = (TransferDispute) super.create(ctx, bean);

        SubscriptionNotificationSupport.sendTransferDisputeNotification(ctx, dispute);

        return dispute;
    }
}
