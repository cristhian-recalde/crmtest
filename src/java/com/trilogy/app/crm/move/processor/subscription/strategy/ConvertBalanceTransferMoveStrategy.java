package com.trilogy.app.crm.move.processor.subscription.strategy;

import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategy;
import com.trilogy.app.crm.move.request.SubscriptionMoveRequest;
import com.trilogy.app.crm.move.support.SubscriptionMoveValidationSupport;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;


public class ConvertBalanceTransferMoveStrategy<SMR extends SubscriptionMoveRequest> extends BalanceTransferCopyMoveStrategy<SMR>
{
    public ConvertBalanceTransferMoveStrategy(CopyMoveStrategy<SMR> delegate,
            AdjustmentTypeEnum creditAdjustmentType,
            AdjustmentTypeEnum debitAdjustmentType)
    {
        super(delegate, 
                creditAdjustmentType, 
                debitAdjustmentType);
    }
    
    public ConvertBalanceTransferMoveStrategy(CopyMoveStrategy<SMR> delegate)
    {
        super(delegate, 
                AdjustmentTypeEnum.SubscriberTransferCredit, 
                AdjustmentTypeEnum.SubscriberTransferDebit);
    }


    public void validate(Context ctx, SMR request) throws IllegalStateException
    {
        CompoundIllegalStateException cise = new CompoundIllegalStateException();

        SubscriptionMoveValidationSupport.validateOldAccountExists(ctx, request, cise);
        
        SubscriptionMoveValidationSupport.validateNewSubscriptionExists(ctx, request, cise);

        SubscriptionMoveValidationSupport.validateNewAccountExists(ctx, request, cise);

        if (bmClient_ == null)
        {
            cise.thrown(new IllegalStateException("No Balance Management provisioning client installed."));
        }
        
        cise.throwAll();
        
        super.validate(ctx, request);

    }


    /**
     * @{inheritDoc
     */
    @Override
    public void createNewEntity(Context ctx, SMR request) throws MoveException
    {
        Subscriber oldSubscription = request.getOldSubscription(ctx);
        long oldBalance = getBalance(ctx, request, oldSubscription);
        
        if (oldBalance > 0)
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Convert:: Debiting balance of " + oldBalance + " from subscription "
                        + oldSubscription.getId() + "...", null).log(ctx);
            }                    
            createAdjustmentTransaction(ctx, request, oldBalance, oldSubscription);
            new InfoLogMsg(this, "Convert:: Successfully debited balance of " + oldBalance + " from subscription "
                    + oldSubscription.getId(), null).log(ctx);
        }
        
        super.createNewEntity(ctx, request);
        
        Subscriber newSubscription = request.getNewSubscription(ctx);
        if (oldBalance > 0)
        {
            CRMSpid spid = null;
            try
            {
                spid = SpidSupport.getCRMSpid(ctx, oldSubscription.getSpid());
            }
            catch (HomeException e)
            {
                new MinorLogMsg(this, "Error retrieving service provider " + oldSubscription.getSpid()
                        + " for subscription " + request.getOldSubscriptionId(), e);
            }
            
            long newBalance = oldBalance;
            if (spid != null)
            {
                StringBuilder msg = new StringBuilder();
                if (oldSubscription.getSubscriberType() == SubscriberTypeEnum.POSTPAID && newSubscription.getSubscriberType() == SubscriberTypeEnum.PREPAID)
                {
                    newBalance = Math.round(oldBalance * spid.getBalanceAdjustmentFactor());
                    msg.append("Postpaid to Prepaid Conversion: newBalance = Math.round(oldBalance * spid.getBalanceAdjustmentFactor())");
                }
                else if (oldSubscription.getSubscriberType() == SubscriberTypeEnum.PREPAID && newSubscription.getSubscriberType() == SubscriberTypeEnum.POSTPAID)
                {
                    newBalance = Math.round(oldBalance / spid.getBalanceAdjustmentFactor());
                    msg.append("Prepaid to Postpaid Conversion: newBalance = Math.round(oldBalance / spid.getBalanceAdjustmentFactor())");
                }
                
                if (LogSupport.isDebugEnabled(ctx))
                {
                    msg.append(", Old Balance = ");
                    msg.append(oldBalance);
                    msg.append(", Balance Adjustment Factor = ");
                    msg.append(spid.getBalanceAdjustmentFactor());
                    msg.append(", New Balance = ");
                    msg.append(newBalance);
                    msg.append(" ...");
                    LogSupport.debug(ctx, this, msg.toString());
                }
            }
            
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Convert:: Crediting balance of " + newBalance + " to subscription "
                        + newSubscription.getId() + "...", null).log(ctx);
            }
            createAdjustmentTransaction(ctx, request, -newBalance, newSubscription);
            new InfoLogMsg(this, "Convert:: Successfully credited balance of " + newBalance + " to subscription "
                    + newSubscription.getId(), null).log(ctx);
        }
    }
}
    
