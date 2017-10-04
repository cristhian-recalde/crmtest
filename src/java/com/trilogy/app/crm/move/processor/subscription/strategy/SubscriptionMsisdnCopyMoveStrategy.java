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

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.MsisdnStateEnum;
import com.trilogy.app.crm.bean.PortingTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.Msisdn;
import com.trilogy.app.crm.home.MsisdnPortHandlingHome;
import com.trilogy.app.crm.move.MoveConstants;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategy;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategyProxy;
import com.trilogy.app.crm.move.request.SubscriptionMoveRequest;
import com.trilogy.app.crm.move.support.SubscriptionMoveValidationSupport;
import com.trilogy.app.crm.numbermgn.MsisdnAlreadyAcquiredException;
import com.trilogy.app.crm.numbermgn.MsisdnManagement;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.MsisdnSupport;


/**
 * Handles MSISDN Management related processing required to move subscriptions.
 * 
 * It must deassociate its MSISDN from the old subscription prior to associating
 * it with the new subscription, which must happen in the createNewEntity method.
 * 
 * It performs validation required to complete its task successfully.
 *
 * @author Aaron Gourley
 * @since 8.1
 */
public class SubscriptionMsisdnCopyMoveStrategy<SMR extends SubscriptionMoveRequest> extends CopyMoveStrategyProxy<SMR>
{
    public SubscriptionMsisdnCopyMoveStrategy(CopyMoveStrategy<SMR> delegate)
    {
        super(delegate);
    }
    
    /**
     * @{inheritDoc}
     */
    @Override
    public void validate(Context ctx, SMR request) throws IllegalStateException
    {
        CompoundIllegalStateException cise = new CompoundIllegalStateException();

        SubscriptionMoveValidationSupport.validateOldSubscriptionExists(ctx, request, cise);
        
        SubscriptionMoveValidationSupport.validateNewSubscriptionExists(ctx, request, cise);
        
        cise.throwAll();

        // Don't bother letting the delegate validate unless this processor has passed.
        super.validate(ctx, request);
    }


    /**
     * @{inheritDoc
     */
    @Override
    public void createNewEntity(Context ctx, SMR request) throws MoveException
    {
        super.createNewEntity(ctx, request);
        Subscriber oldSubscription = request.getOldSubscription(ctx);
        if (ctx.getBoolean(MoveConstants.NO_BILLCYCLE_CHANGE, false))
        {
            
            Subscriber newSubscription = request.getNewSubscription(ctx);
            try
            {
                new DebugLogMsg(this, "Converting the subscriber type for the msisdn " + newSubscription.getMSISDN() + " with MSISDN "
                        + oldSubscription.getMSISDN() + "...", null).log(ctx);
                MsisdnManagement.convertMsisdn(ctx, newSubscription.getMSISDN(), newSubscription.getBAN(), newSubscription.getSubscriberType(), newSubscription,"");
                new InfoLogMsg(this, "Successfully converted the subscriber type for the  MSISDN " + oldSubscription.getMSISDN(), null).log(ctx);
            }
            catch (HomeException e)
            {
                String msg = "Error occurred associating mobile number " + newSubscription.getMSISDN()
                        + " with subscription " + newSubscription.getId();
                new MajorLogMsg(this, msg + ": " + e.getMessage(), null).log(ctx);
                throw new MoveException(request, msg, e);
            }
        }
        //else // TT # 12062831010 - MSISDn history table should be updated during pre-paid to post-paid ( or vice versa ) conversion.
        if(!oldSubscription.getId().equals(request.getNewSubscription(ctx).getId()))
        {
            try
            {
                ensureOrMakeMsisdnAvaialble(ctx, oldSubscription);
                new InfoLogMsg(this, "MSISDN " + oldSubscription.getMSISDN()
                        + " is disassociated from subscription and avaialble" + request.getOldSubscriptionId(), null)
                        .log(ctx);
            }
            catch (HomeException e)
            {
                new InfoLogMsg(this, "Error occurred ensuring mobile number is available "
                        + oldSubscription.getMSISDN() + " after being disassociated old subscription "
                        + oldSubscription.getId() + ": " + e.getMessage(), null).log(ctx);
            }
            Subscriber newSubscription = request.getNewSubscription(ctx);
            try
            {
                ensureMsisdnIsClaimedAndAssociateitWithNewSub(ctx, newSubscription);
            }
            catch (Throwable e)
            {
                String msg = "Error occurred associating mobile number " + newSubscription.getMSISDN()
                        + " with subscription " + newSubscription.getId();
                new MajorLogMsg(this, msg + ": " + e.getMessage(), null).log(ctx);
                throw new MoveException(request, msg, e);
            }
        }
    }
    
    private void ensureOrMakeMsisdnAvaialble(Context ctx, Subscriber oldSubscription) throws HomeException
    {
        try
        {
            String msisdn = oldSubscription.getMsisdn();
            MsisdnManagement.deassociateMsisdnWithSubscription(ctx, msisdn, oldSubscription, "");
            MsisdnManagement.releaseMsisdn(ctx, msisdn, oldSubscription.getBAN(), "");
        }
        catch (Throwable t)
        {
            new DebugLogMsg(
                    this,
                    "MSISDNs disassciation with old Subscription and release from old account failed. It could be that the MSISDN is already avaialble",
                    t).log(ctx);
        }
        Msisdn msisdnObj = MsisdnSupport.getMsisdn(ctx, oldSubscription.getMSISDN());
        MsisdnStateEnum msisdnState = msisdnObj.getState();
        if (MsisdnStateEnum.AVAILABLE != msisdnState)
        {
            if (MsisdnStateEnum.HELD == msisdnState)
            {
                ctx.put(MsisdnPortHandlingHome.MSISDN_PORT_KEY, msisdnObj.getMsisdn());
                msisdnObj.setState(MsisdnStateEnum.AVAILABLE);
                HomeSupportHelper.get(ctx).storeBean(ctx, msisdnObj);
            }
        } else
        {
            throw new RuntimeException("MSISDN not avaiable for claiming and assocating it with new Sibscription");
        }
    }
    
    public void ensureMsisdnIsClaimedAndAssociateitWithNewSub(Context ctx, Subscriber newSubscription) throws HomeException
    {
        try
        {
        	Msisdn msisdnObj = MsisdnSupport.getMsisdn(ctx, newSubscription.getMSISDN());
            MsisdnManagement.claimMsisdn(ctx, newSubscription.getMSISDN(), newSubscription.getBAN(), msisdnObj.isExternal(), "");
        }
        catch (MsisdnAlreadyAcquiredException e)
        {
            new DebugLogMsg(this, "MSISDN is already acquired as expected, we can ignore", null).log(ctx);
        }
        MsisdnManagement.associateMsisdnWithSubscription(ctx, newSubscription.getMSISDN(), newSubscription, "");
        new InfoLogMsg(this, "Successfully associated MSISDN " + newSubscription.getMSISDN() + " with subscription ",
                null).log(ctx);
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void removeOldEntity(Context ctx, SMR request) throws MoveException
    {
        // Nothing to do, because we had to deassociate the MSISDN before associating it in create.
        super.removeOldEntity(ctx, request);
    }
}
