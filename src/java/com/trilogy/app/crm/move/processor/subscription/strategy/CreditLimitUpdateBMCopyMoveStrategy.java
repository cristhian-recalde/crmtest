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

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.client.bm.BalanceManagementSupport;
import com.trilogy.app.crm.client.bm.SubscriberProfileProvisionClient;
import com.trilogy.app.crm.client.bm.SubscriberProfileProvisionException;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategy;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategyProxy;
import com.trilogy.app.crm.move.request.ConvertSubscriptionBillingTypeRequest;
import com.trilogy.app.crm.move.request.SubscriptionMoveRequest;
import com.trilogy.app.crm.move.support.SubscriptionMoveValidationSupport;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.InfoLogMsg;


/**
 * This copy strategy is for updating BM's credit limit directly when credit limit get changed
 * through other CRM processes.
 * 
 * It performs validation required to complete its task successfully.
 *  
 * @author Kumaran Sivasubramaniam
 * @since 8.2
 */
public class CreditLimitUpdateBMCopyMoveStrategy<SMR extends SubscriptionMoveRequest> extends CopyMoveStrategyProxy<SMR>
{
    public CreditLimitUpdateBMCopyMoveStrategy(CopyMoveStrategy<SMR> delegate)
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
        Subscriber newSubscription = request.getNewSubscription(ctx);
        long oldCreditLimit = newSubscription.getCreditLimit(ctx);
        super.createNewEntity(ctx, request);
        long newCreditLimit = newSubscription.getCreditLimit(ctx);
      
        if (newCreditLimit != oldCreditLimit )
        {
            final SubscriberProfileProvisionClient client;
            client = BalanceManagementSupport.getSubscriberProfileProvisionClient(ctx);
            try
            {
                client.adjustCreditLimit(ctx, newSubscription, newCreditLimit, oldCreditLimit, "");
                new InfoLogMsg(this, "Credit Limit has been updated to BM" + newSubscription.getId() + "  new credit Limit "+ newCreditLimit,
                        null).log(ctx);
            }
            catch (HomeException ex)
            {
                throw new MoveException(request, "Unable to update the credit limit", ex);
            }
            catch (SubscriberProfileProvisionException ex)
            {
                throw new MoveException(request, "Unable to update the credit limit", ex);

            }
        }

        
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void removeOldEntity(Context ctx, SMR request) throws MoveException
    {
        super.removeOldEntity(ctx, request);
    }

}
