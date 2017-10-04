package com.trilogy.app.crm.subscriber.charge;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import com.trilogy.app.crm.bean.AbstractSubscriber;
import com.trilogy.app.crm.bean.AdjustmentTypeXInfo;
import com.trilogy.app.crm.bean.MsisdnGroup;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SystemNoteSubTypeEnum;
import com.trilogy.app.crm.bean.SystemNoteTypeEnum;
import com.trilogy.app.crm.bean.core.AdjustmentType;
import com.trilogy.app.crm.bean.core.Msisdn;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.bean.ui.MsisdnXInfo;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.NoteSupportHelper;
import com.trilogy.app.crm.support.SystemSupport;
import com.trilogy.app.crm.support.TransactionSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;


public class MsisdnGroupChargingHome extends HomeProxy
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    public MsisdnGroupChargingHome(final Context ctx, final Home delegate)
    {
        super(ctx, delegate);
    }


    /**
     * in future we should support both precharge and postcharge, but for new we support
     * postcharge only
     * 
     * @param ctx
     * @param obj
     * @return
     * @throws HomeException
     */
    public Object create(final Context ctx, final Object obj) throws HomeException
    {
        final Object newObj = super.create(ctx, obj);
        Subscriber sub = (Subscriber) obj;

        new DebugLogMsg(this, "Create : checking if skipCharging for subscriber state : " + sub.getState().getDescription());
        if (!isSkipCharging(ctx, sub))
        {
            MsisdnGroup group = getMsisdnGroup(ctx, sub);
            if (isChargeable(ctx, group))
            {
                chargeSubscription(ctx, sub, group);
            }
        }
        return newObj;
    }


    public Object store(final Context ctx, final Object obj) throws HomeException
    {
        Subscriber newSub = (Subscriber) obj;
        
        final Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
        new DebugLogMsg(this, "Store : checking if skipCharging for subscriber new state : " + newSub.getState().getDescription() + " : old state : " + oldSub.getState().getDescription());
        
        //incase the old subscriber was not charged then only charge the new subscriber. 
        //Requirement says : It is expected that the refund for the over charges are resolved using a manual adjustment. 
        if (isMsisdnSwap(ctx, newSub, oldSub) || 
                ( isSkipCharging(ctx, oldSub) && !isSkipCharging(ctx, newSub)))
        {
            MsisdnGroup group = getMsisdnGroup(ctx, newSub);
            if (isChargeable(ctx, group))
            {
                chargeSubscription(ctx, oldSub, group);
            }
        }
        final Object newObj = super.store(ctx, obj);
        return newObj;
    }


    private boolean isMsisdnSwap(final Context ctx, final Subscriber newSub, final Subscriber oldSub)
    {
        boolean result = false;
        if (oldSub != null && !oldSub.getMsisdn().equals(newSub.getMsisdn()))
        {
            result = true;
        }
        return result;
    }


    private void chargeSubscription(final Context ctx, final Subscriber sub, final MsisdnGroup group)
            throws HomeException
    {
        AdjustmentType adjustmentType = HomeSupportHelper.get(ctx).findBean(ctx,
                com.redknee.app.crm.bean.core.AdjustmentType.class,
                new EQ(AdjustmentTypeXInfo.CODE, (int) group.getAdjustmentId()));
        if (adjustmentType != null)
        {
            long balance = sub.getBalanceRemaining(ctx);
            Transaction trans = TransactionSupport.createTransaction(ctx, sub, group.getFee(), balance, adjustmentType,
                    false, false, "", new Date(), new Date(), "");
            if (trans == null)
            {
                new MinorLogMsg(this, "Unable to do charging for msisdn group " + group.getId() + " for amount "
                        + group.getFee(), null).log(ctx);
                NoteSupportHelper.get(ctx).addSubscriberNote(ctx, sub.getId(), "Failed to charged msisdn group [" + group.getName() +"] fee",  SystemNoteTypeEnum.EVENTS, SystemNoteSubTypeEnum.SUBUPDATE);
            }
        }
        else
        {
            throw new HomeException(" Unable to find adjustmentId " + group.getAdjustmentId() + " ");
        }
    }


    private Msisdn getMsisdn(final Context ctx, final Subscriber sub) throws HomeException
    {
        Msisdn msisdn = HomeSupportHelper.get(ctx).findBean(ctx, Msisdn.class,sub.getMsisdn());
        return msisdn;
    }


    private MsisdnGroup getMsisdnGroup(final Context ctx, final Subscriber sub) throws HomeException
    {
        int msisdnGroup = sub.getMSISDNGroup();
        if (msisdnGroup == AbstractSubscriber.DEFAULT_MSISDNGROUP)
        {
            Msisdn msisdn = getMsisdn(ctx, sub);
            if(msisdn !=null) //For external numbers msisdn comes null.
            	msisdnGroup = msisdn.getGroup();
        }
        MsisdnGroup group = HomeSupportHelper.get(ctx).findBean(ctx, com.redknee.app.crm.bean.MsisdnGroup.class,
                msisdnGroup);
        return group;
    }


    private boolean isChargeable(final Context ctx, final MsisdnGroup group)
    {
        return (group != null) && (group.getFee() > 0) ? true : false;
    }
    
    private boolean isSkipCharging(Context ctx, Subscriber subscriber)
    {
        boolean result = false;
        
        if ((subscriber.isPrepaid() && EnumStateSupportHelper.get(ctx).isOneOfStates(subscriber.getState(), PREPAID_NON_CHARGEABLE_STATES) && !SystemSupport.supportsPrepaidCreationInActiveState(ctx))
                || (subscriber.isPostpaid() && EnumStateSupportHelper.get(ctx).isOneOfStates(subscriber.getState(), POSTPAID_NON_CHARGEABLE_STATES)))
        {
                result = true;
        }
        
        new DebugLogMsg(this, "SkipCharging result for subscriber : " + result);
        return result;
    }
    
    private static final Collection<SubscriberStateEnum> POSTPAID_NON_CHARGEABLE_STATES = 
            Arrays.asList(
                SubscriberStateEnum.AVAILABLE,
                SubscriberStateEnum.IN_ARREARS,
                SubscriberStateEnum.IN_COLLECTION,
                SubscriberStateEnum.INACTIVE,
                SubscriberStateEnum.PENDING
                );
    
    private static final Collection<SubscriberStateEnum> PREPAID_NON_CHARGEABLE_STATES = 
            Arrays.asList(
                SubscriberStateEnum.AVAILABLE,
                SubscriberStateEnum.INACTIVE,
                SubscriberStateEnum.PENDING
                );
    
}
