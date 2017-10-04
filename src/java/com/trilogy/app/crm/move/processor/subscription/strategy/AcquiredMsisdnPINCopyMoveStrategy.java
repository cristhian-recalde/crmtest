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
package com.trilogy.app.crm.move.processor.subscription.strategy;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.bean.AcquiredMsisdnPINManagement;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategy;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategyProxy;
import com.trilogy.app.crm.move.request.SubscriptionMoveRequest;
import com.trilogy.app.crm.move.support.SubscriptionMoveValidationSupport;
import com.trilogy.app.crm.support.LicensingSupportHelper;
import com.trilogy.app.crm.support.PinManagerSupport;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;


/**
 * Responsible to pcoy Aquired MSISDN PIN during move/conversion
 *
 * @author Marcio Marques
 * @since 9.1.2
 */
public class AcquiredMsisdnPINCopyMoveStrategy<SMR extends SubscriptionMoveRequest> extends CopyMoveStrategyProxy<SMR>
{

    public AcquiredMsisdnPINCopyMoveStrategy(CopyMoveStrategy<SMR> delegate)
    {
        super(delegate);
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void initialize(Context ctx, SMR request)
    {
        super.initialize(ctx, request);
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void validate(Context ctx, SMR request) throws IllegalStateException
    {
        CompoundIllegalStateException cise = new CompoundIllegalStateException();

        SubscriptionMoveValidationSupport.validateNewSubscriptionExists(ctx, request, cise);

        SubscriptionMoveValidationSupport.validateOldSubscriptionExists(ctx, request, cise);
        
        if (LicensingSupportHelper.get(ctx).isLicensed(ctx, LicenseConstants.PIN_MANAGER_LICENSE_KEY))
        {
            try
            {
                PinManagerSupport.getClient(ctx);
            }
            catch (HomeException e)
            {
                cise.thrown(new IllegalStateException("No PIN Manager client installed."));
            }
        }
        
        cise.throwAll();

        // Don't bother letting the delegate validate unless this processor has passed.
        super.validate(ctx, request);
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void createNewEntity(Context ctx, SMR request) throws MoveException
    {
        super.createNewEntity(ctx, request);
        
        Subscriber oldSubscription = request.getOldSubscription(ctx);
        
        Subscriber newSubscription = request.getNewSubscription(ctx);

        if (LicensingSupportHelper.get(ctx).isLicensed(ctx, LicenseConstants.PIN_MANAGER_LICENSE_KEY) &&
                !oldSubscription.getBAN().equals(newSubscription.getBAN()))
        {
            try
            {
                AcquiredMsisdnPINManagement aquiredMsisdnPIN = PinManagerSupport.getPinProvisoningRecord(ctx, newSubscription.getMSISDN());
                if (aquiredMsisdnPIN!=null)
                {
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        new DebugLogMsg(this, "Copying Acquired Msisdn PIN Management of subscription " + oldSubscription.getId() + "...", null).log(ctx);
                    }
                    PinManagerSupport.createPinProvisoningRecord(ctx, newSubscription.getBAN(), newSubscription.getMSISDN(), aquiredMsisdnPIN.getState());
                    new InfoLogMsg(this, "Successfully copied Acquired Msisdn PIN Management of subscription " + oldSubscription.getId(), null).log(ctx);
                }
            }
            catch (Exception e)
            {
                new MinorLogMsg(this, "Failed to copy Acquired Msisdn PIN Management from subscription "
                        + "(ID=" + oldSubscription.getId() + "/MSISDN=" + oldSubscription.getMSISDN() + ").", e).log(ctx);
            }
        }

    }
}
