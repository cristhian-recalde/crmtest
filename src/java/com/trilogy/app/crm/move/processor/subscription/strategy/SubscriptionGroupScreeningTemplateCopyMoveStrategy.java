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

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.AbstractSubscriber;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategy;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategyProxy;
import com.trilogy.app.crm.move.request.SubscriptionMoveRequest;

/**
 * @param <SMR>
 * @author chandrachud.ingale
 * @since This class is added to reset the GroupScreeningTemplateId of subscriber when account move happens.
 */
public class SubscriptionGroupScreeningTemplateCopyMoveStrategy<SMR extends SubscriptionMoveRequest> extends
        CopyMoveStrategyProxy<SMR>
{

    public SubscriptionGroupScreeningTemplateCopyMoveStrategy(CopyMoveStrategy<SMR> delegate)
    {
        super(delegate);
    }


    /**
     * @{inheritDoc
     */
    public void createNewEntity(Context ctx, SMR request) throws MoveException
    {
        Subscriber newSubscription = request.getNewSubscription(ctx);
        if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, this, "Setting GroupScreeningTemplateId as -1 for SubscriptionMove");
        }
        newSubscription.setGroupScreeningTemplateId(AbstractSubscriber.DEFAULT_GROUPSCREENINGTEMPLATEID);

        super.createNewEntity(ctx, request);
    }

}
