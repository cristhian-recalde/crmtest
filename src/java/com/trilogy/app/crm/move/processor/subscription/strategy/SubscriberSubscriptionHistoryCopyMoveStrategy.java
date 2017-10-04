package com.trilogy.app.crm.move.processor.subscription.strategy;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.trilogy.app.crm.bean.ChargedItemTypeEnum;
import com.trilogy.app.crm.bean.ServiceFee2ID;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.Service;
import com.trilogy.app.crm.bean.service.SubscriberSubscriptionHistory;
import com.trilogy.app.crm.bean.service.SubscriberSubscriptionHistoryHome;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.MoveWarningException;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategy;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategyProxy;
import com.trilogy.app.crm.move.request.SubscriptionMoveRequest;
import com.trilogy.app.crm.move.support.SubscriptionMoveValidationSupport;
import com.trilogy.app.crm.service.ServicePeriodHandler;
import com.trilogy.app.crm.subscriber.subscription.history.SubscriberSubscriptionHistorySupport;
import com.trilogy.app.crm.support.ServicePeriodSupportHelper;
import com.trilogy.app.crm.support.ServiceSupport;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;


public class SubscriberSubscriptionHistoryCopyMoveStrategy<SMR extends SubscriptionMoveRequest> extends CopyMoveStrategyProxy<SMR>
{
    public SubscriberSubscriptionHistoryCopyMoveStrategy(CopyMoveStrategy<SMR> delegate)
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

        final Subscriber oldSubscription = request.getOldSubscription(ctx);
        final Subscriber newSubscription = request.getNewSubscription(ctx);

        Set<ServiceFee2ID> services = oldSubscription.getServices(ctx);
        List<SubscriberAuxiliaryService> auxServices = oldSubscription.getAuxiliaryServices(ctx);
        Map<Long, BundleFee> bundles = oldSubscription.getBundles();
        
        try
        {
    
            final int billCycleDay = oldSubscription.getAccount(ctx).getBillCycleDay(ctx);
            
            for (ServiceFee2ID serviceFee2ID : services)
            {
                Service service = ServiceSupport.getService(ctx, serviceFee2ID.getServiceId());
                copyItem(ctx, request, serviceFee2ID.getServiceId(), newSubscription.getId(), oldSubscription.getId(), ChargedItemTypeEnum.SERVICE, service,
                        service.getChargeScheme(), billCycleDay, oldSubscription.getSpid());
            }        

            for (Iterator<Map.Entry<Long, BundleFee>> i = bundles.entrySet().iterator(); i.hasNext();)
            {
                final Map.Entry<Long, BundleFee> entry = i.next();
                final Long key = entry.getKey();
                final BundleFee fee = entry.getValue();
                copyItem(ctx, request, fee.getId(), newSubscription.getId(), oldSubscription.getId(), fee.isAuxiliarySource()
                        ? ChargedItemTypeEnum.AUXBUNDLE
                        : ChargedItemTypeEnum.BUNDLE, fee, fee.getServicePeriod(), billCycleDay, oldSubscription
                        .getSpid());
            }
            
            for (SubscriberAuxiliaryService auxService : auxServices)
            {
                copyItem(ctx, request, auxService.getAuxiliaryServiceIdentifier(), newSubscription.getId(), oldSubscription.getId(), ChargedItemTypeEnum.AUXSERVICE, auxService,
                        auxService.getAuxiliaryService(ctx).getChargingModeType(), billCycleDay, oldSubscription.getSpid());
            }        
        }
        catch (HomeException e)
        {
            request.getWarnings(ctx).add(
                    new MoveWarningException(request,
                            "Error occured while trying to copy subscriber subscription history entries from subscriber "
                                    + oldSubscription.getId() + " to subscriber " + newSubscription.getId(), e));
        }
        

    }
    
    private void copyItem(Context ctx, SMR request, long itemId, String newSubscriptionId, String subscriptionId, ChargedItemTypeEnum itemType, final Object item, ServicePeriodEnum servicePeriod, int billCycleDay, int spid)
    {
        final Home home = (Home) ctx.get(SubscriberSubscriptionHistoryHome.class);
        ServicePeriodHandler handler = ServicePeriodSupportHelper.get(ctx).getHandler(servicePeriod);
        try
        {
            Date startDate = handler.calculateCycleStartDate(ctx, new Date(), billCycleDay, spid, subscriptionId, item);
    
            SubscriberSubscriptionHistory history = SubscriberSubscriptionHistorySupport.getLastChargingEventSince(ctx, subscriptionId, itemType, item, startDate);
            if(null != history)
            {
                history.setSubscriberId(newSubscriptionId);
                SubscriberSubscriptionHistorySupport.createRecord(ctx,history);
            } else
            {
                new DebugLogMsg(this,"No last charging event found for Subscriotion ["+ subscriptionId + "] in moving to new Subscription ["+ newSubscriptionId + "]" +
                		"; Item-Type [" + itemType +"], Service Period [" + servicePeriod +"] Bill-Cycle-Day [" +billCycleDay + "]",null).log(ctx);
            }
            
        }
        catch (HomeException e)
        {
            request.getWarnings(ctx).add(
                    new MoveWarningException(request,
                            "Error occured while trying to copy subscriber subscription history entry for item "
                                    + itemId + " (" + itemType + ") from subscriber "
                                    + subscriptionId + " to subscriber " + newSubscriptionId, e));
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
