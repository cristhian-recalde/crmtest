/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.move.processor.generic.strategy;

import java.util.Collection;

import com.trilogy.app.crm.bean.SupplementaryData;
import com.trilogy.app.crm.bean.SupplementaryDataEntityEnum;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.MoveRequest;
import com.trilogy.app.crm.move.MoveWarningException;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategy;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategyProxy;
import com.trilogy.app.crm.move.request.AccountMoveRequest;
import com.trilogy.app.crm.move.request.SubscriptionMoveRequest;
import com.trilogy.app.crm.support.SupplementaryDataSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;


/**
 * 
 * @author Marcio Marques
 *
 * @since 9.1.3
 */
public class SupplementaryDataCopyMoveStrategy<MR extends MoveRequest> extends CopyMoveStrategyProxy<MR>
{
    public SupplementaryDataCopyMoveStrategy(CopyMoveStrategy<MR> delegate, MR moveRequest)
    {
        super(delegate);
        if (moveRequest instanceof AccountMoveRequest)
        {
            entity_ = SupplementaryDataEntityEnum.ACCOUNT;
        }
        else if (moveRequest instanceof SubscriptionMoveRequest)
        {
            entity_ = SupplementaryDataEntityEnum.SUBSCRIPTION;
        }
        else
        {
            throw new UnsupportedOperationException("Invalid move request");
        }
            
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void initialize(Context ctx, MR request)
    {
        super.initialize(ctx, request);
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void validate(Context ctx, MR request) throws IllegalStateException
    {
        super.validate(ctx, request);
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void createNewEntity(Context ctx, MR request) throws MoveException
    {
        super.createNewEntity(ctx, request);
        
        final String oldIdentifier;
        final String newIdentifier;
        if (SupplementaryDataEntityEnum.ACCOUNT.equals(entity_))
        {
            oldIdentifier = ((AccountMoveRequest) request).getOldAccount(ctx).getBAN();
            newIdentifier = ((AccountMoveRequest) request).getNewAccount(ctx).getBAN();
        }
        else if (SupplementaryDataEntityEnum.SUBSCRIPTION.equals(entity_))
        {
            oldIdentifier = ((SubscriptionMoveRequest) request).getOldSubscription(ctx).getId();
            newIdentifier = ((SubscriptionMoveRequest) request).getNewSubscription(ctx).getId();
        }
        else
        {
            oldIdentifier = null;
            newIdentifier = null;
        }

        try
        {
            Collection<SupplementaryData> data = SupplementaryDataSupportHelper.get(ctx).getSupplementaryData(ctx,
                    entity_, oldIdentifier);
            
            for (SupplementaryData individualData : data)
            {
                individualData.setIdentifier(newIdentifier);
                try
                {
                    SupplementaryDataSupportHelper.get(ctx).addOrUpdateSupplementaryData(ctx, individualData);
                }
                catch (HomeException e)
                {
                    request.getWarnings(ctx).add(
                            new MoveWarningException(request, "Error occured while trying to copy "
                                    + entity_.getDescription() + " '" + oldIdentifier
                                    + "' supplementary data with key '" + individualData.getKey() + "' and value '"
                                    + individualData.getValue() + " to new  " + entity_.getDescription() + " '"
                                    + newIdentifier + "'", e));
                }
            }
    
        }
        catch (HomeException e)
        {
            request.getWarnings(ctx).add(
                    new MoveWarningException(request, "Error occured while trying to copy " + entity_.getDescription()
                            + " '" + oldIdentifier + "' supplementary data to new  " + entity_.getDescription() + " '"
                            + newIdentifier + "'", e));
        }
        

    }
    
    /**
     * @{inheritDoc}
     */
    @Override
    public void removeOldEntity(Context ctx, MR request) throws MoveException
    {
        super.removeOldEntity(ctx, request);
    }
    
    private SupplementaryDataEntityEnum entity_;
}
