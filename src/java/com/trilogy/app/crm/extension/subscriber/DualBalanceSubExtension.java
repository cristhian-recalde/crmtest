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
package com.trilogy.app.crm.extension.subscriber;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.client.AbmResultCode;
import com.trilogy.app.crm.client.bm.BalanceManagementSupport;
import com.trilogy.app.crm.client.bm.SubscriberProfileProvisionClient;
import com.trilogy.app.crm.client.bm.SubscriberProfileProvisionException;
import com.trilogy.app.crm.extension.ExtensionInstallationException;
import com.trilogy.app.crm.subscriber.provision.SubscriberProvisionResultCode;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;


/**
 * 
 *
 * @author kumaran.sivasubramaniam@redknee.com
 * @since 8.5
 */
public class DualBalanceSubExtension extends AbstractDualBalanceSubExtension
{

    @Override
    public void install(Context ctx) throws ExtensionInstallationException
    {        
        updateDualBalance(ctx, true);        
    }

    @Override
    public void uninstall(Context ctx) throws ExtensionInstallationException
    {
        updateDualBalance(ctx, false);
    }

    @Override
    public void update(Context ctx) throws ExtensionInstallationException
    {
        
    }
    
    public void updateDualBalance(Context ctx, boolean enabled) throws ExtensionInstallationException
    {

        Subscriber subscriber = getSubscriber(ctx);
        new InfoLogMsg(this, "Updating dual balance for sub[" +subscriber.getId() + "] to " + enabled, null).log(ctx);

        try
        {
            final SubscriberProfileProvisionClient client;
            client = BalanceManagementSupport.getSubscriberProfileProvisionClient(ctx);
            client.updateDualBalance(ctx, subscriber, enabled);
        }
        catch (final SubscriberProfileProvisionException exception)
        {
            final short resultBM = exception.getErrorCode();
            SubscriberProvisionResultCode.setProvisionUpsErrorCode(ctx, resultBM);
            final String err = "Failed to update Dual Balance status for subscription " + subscriber.getId() + "  due to error on URCS (" + resultBM + ")";
            new MinorLogMsg(this, err, exception).log(ctx);
            throw new ExtensionInstallationException(err , exception, false);
            
        }
    }


    
    @Override
    public void deactivate(Context ctx) throws ExtensionInstallationException
    {
        uninstall(ctx);
        Home home = (Home) ctx.get(DualBalanceSubExtensionHome.class);
        try
        {
            home.remove(ctx, this);
        }
        catch (HomeException e)
        {
            throw new ExtensionInstallationException("Unable to remove Dual Balance for deactivated subscription " + this.getSubId() + ": " + e.getMessage(), e, false);
        }
    }

    public boolean isValidForSubscriberType(SubscriberTypeEnum subscriberType)
    {
        return SubscriberTypeEnum.POSTPAID.equals(subscriberType);
    }

}
