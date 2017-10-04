/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee, no
 * unauthorised use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the licence agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.move.processor.subscription.strategy;

import java.util.Date;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.contract.SubscriptionContract;
import com.trilogy.app.crm.contract.SubscriptionContractTerm;
import com.trilogy.app.crm.contract.SubscriptionContractTermXInfo;
import com.trilogy.app.crm.contract.SubscriptionContractXInfo;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategy;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategyProxy;
import com.trilogy.app.crm.move.request.SubscriptionMoveRequest;
import com.trilogy.app.crm.move.support.SubscriptionMoveValidationSupport;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * Responsible for canceling the existing contract
 * 
 * It performs validation required to complete its task successfully.
 * 
 * @author Kumaran Sivasubramaniam
 * @since 9.0
 */
public class SubscriptionContractCopyMoveStrategy<SMR extends SubscriptionMoveRequest>
        extends
            CopyMoveStrategyProxy<SMR>
{

    public SubscriptionContractCopyMoveStrategy(CopyMoveStrategy<SMR> delegate)
    {
        super(delegate);
    }


    /**
     * @{inheritDoc
     */
    @Override
    public void initialize(Context ctx, SMR request)
    {
        super.initialize(ctx, request);
    }


    /**
     * @{inheritDoc
     */
    @Override
    public void validate(Context ctx, SMR request) throws IllegalStateException
    {
        CompoundIllegalStateException cise = new CompoundIllegalStateException();
        Subscriber oldSubscription = SubscriptionMoveValidationSupport
                .validateOldSubscriptionExists(ctx, request, cise);
        if (oldSubscription.isPostpaid() && oldSubscription != null
                && oldSubscription.getSubscriptionContract(ctx) != -1)
        {
            long contractId = oldSubscription.getSubscriptionContract(ctx);
            // SubscriptionContractSupport.getSubscriptionContractTerm(ctx, contract)
            hasContract_ = true;
        }
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
        Subscriber newSubscriber = request.getNewSubscription(ctx);
        try
        {
            if (newSubscriber.isPostpaid() && newSubscriber.getSubscriptionContract() != -1)
            {
                Date startDate = new Date();
                SubscriptionContractTerm term = HomeSupportHelper.get(ctx).findBean(ctx,
                        SubscriptionContractTerm.class,
                        new EQ(SubscriptionContractTermXInfo.ID, newSubscriber.getSubscriptionContract(ctx)));
                if (term != null)
                {
                    SubscriptionContract newContract = new com.redknee.app.crm.contract.core.SubscriptionContract(newSubscriber.getId(), term, startDate);
                    newContract = HomeSupportHelper.get(ctx).createBean(ctx, newContract);
                    newSubscriber.setSubscriptionContractEndDate(newContract.getContractEndDate());
                    newSubscriber.setSubscriptionContractStartDate(newContract.getContractStartDate());
                    newSubscriber.setSubscriptionContract(newContract.getContractId());
                    if(LogSupport.isDebugEnabled(ctx))
                    {
                        LogSupport.debug(ctx, this, "Contract created for subscriber " + newSubscriber.getId() + "with contract Id : " + newContract.getContractId());
                    }
                }
                else
                {
                    if(LogSupport.isDebugEnabled(ctx))
                    {
                        LogSupport.debug(ctx, this, "Unable to find contract for subscriber " + newSubscriber.getId());
                    }
                    throw new MoveException(request, "Unable to find contract Id " + newSubscriber.getId());
                }
            }
        }
        catch (HomeException homeEx)
        {
            throw new MoveException(request, "Unable to create contract for sub " + newSubscriber.getId());
        }
    }


    /**
     * @{inheritDoc
     */
    @Override
    public void removeOldEntity(Context ctx, SMR request) throws MoveException
    {
        if (hasContract_)
        {
            Subscriber oldSubscription = request.getOldSubscription(ctx);
            try
            {
                SubscriptionContract contract = HomeSupportHelper.get(ctx).findBean(ctx, SubscriptionContract.class,
                        new EQ(SubscriptionContractXInfo.SUBSCRIPTION_ID, oldSubscription.getId()));
                if (contract != null)
                {
                    HomeSupportHelper.get(ctx).removeBean(ctx, contract);
                }
            }
            catch (HomeException homeEx)
            {
                throw new MoveException(request, "Unable to remove contract Id " + oldSubscription.getId());
            }
        }
        super.removeOldEntity(ctx, request);
    }

    private boolean hasContract_ = false;
}
