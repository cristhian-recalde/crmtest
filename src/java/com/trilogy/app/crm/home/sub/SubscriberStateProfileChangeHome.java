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
package com.trilogy.app.crm.home.sub;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.trilogy.app.crm.CommonTime;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bulkloader.AcctSubBulkLoadRequestServicer;
import com.trilogy.app.crm.poller.agent.VRAFraudERAgent;
import com.trilogy.app.crm.support.AuxiliaryServiceSupport;
import com.trilogy.app.crm.support.BillCycleSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * @author jchen
 *
 * Synchronize Subscriber profile with its state change
 */
public class SubscriberStateProfileChangeHome extends HomeProxy
{

    /**
     * @param delegate
     */
    public SubscriberStateProfileChangeHome(Home delegate)
    {
        super(delegate);
    }


    /**
     * @see com.redknee.framework.xhome.home.HomeSPI#create(com.redknee.framework.xhome.context.Context,
     *      java.lang.Object)
     */
    @Override
    public Object create(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
        Subscriber newSub = (Subscriber) obj;
        syncActivationDateForStateChange(ctx, null, newSub);
        return super.create(ctx, obj);
    }


    /**
     * @see com.redknee.framework.xhome.home.HomeSPI#store(com.redknee.framework.xhome.context.Context,
     *      java.lang.Object)
     */
    @Override
    public Object store(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
        Subscriber newSub = (Subscriber) obj;
        Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
        syncActivationDateForStateChange(ctx, oldSub, newSub);
        syncDeactivationDateForStateChange(ctx, oldSub, newSub);

        //If Subscriber is activated then set the sec.PPlan start and end date
        if (EnumStateSupportHelper.get(ctx).isTransition(oldSub, newSub, SubscriberStateEnum.AVAILABLE, SubscriberStateEnum.ACTIVE))
        {
            newSub.setSecondaryPricePlanStartDate(calculateSecondaryPPStartDateOnActivation(ctx,newSub));
            newSub.setSecondaryPricePlanEndDate(CalculateSecondaryPPEndDateOnActivation(ctx,newSub));
        }

        // Activate the subscriber state and billCycle on ABM if stateEntering = Active add special BillCycle check
        if (EnumStateSupportHelper.get(ctx).isTransition(oldSub, newSub, 
                Arrays.asList(SubscriberStateEnum.AVAILABLE, SubscriberStateEnum.INACTIVE), 
                SubscriberStateEnum.ACTIVE))
        {
            Account account = SubscriberSupport.lookupAccount(ctx,oldSub);
            if (account.getBillCycle(ctx).getDayOfMonth() == BillCycleSupport.SPECIAL_BILL_CYCLE_DAY)
            {
                SubscriberSupport.updateSubStateOnCrmAbmBM(ctx, account, newSub);   
            }
        }

        updateFraudProfile(ctx, oldSub, newSub);
        return super.store(ctx, obj);
    }

    /**
     * @see com.redknee.framework.xhome.home.HomeSPI#remove(com.redknee.framework.xhome.context.Context,
     *      java.lang.Object)
     */
    @Override
    public void remove(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
        super.remove(ctx, obj);
    }


    /**
     * @param oldSub
     * @param newSub
     */
    private void syncDeactivationDateForStateChange(Context ctx, Subscriber oldSub, Subscriber newSub)
    {
        // Adjust the End Date field if the sub is deactivated.
        // We have to do this here because the changeState method does
        // not modify the bean for some reason?
        // TT:408319744, endData must be set when state set to inactive
        // set end date
        boolean setEndDate = false;
        if (oldSub != null && newSub != null)
        {
            if (newSub.getEndDate() == null  && newSub.getState() == SubscriberStateEnum.INACTIVE )
            {	
                // Update the subscriber end date field to current time
                // so that the operator knows when the sub has been deactivated
                setEndDate = true;
            }
        }
        else
        {
            if (oldSub == null && newSub.getState().getIndex() == SubscriberStateEnum.INACTIVE_INDEX)
            {
                setEndDate = true;
            }
        }
        if (setEndDate)
        {
            newSub.setEndDate(new Date());
        }
        else
        {
            // check and see clean up end date
            if (oldSub != null && newSub != null)
            {
                if (EnumStateSupportHelper.get(ctx).isLeavingState(oldSub, newSub, SubscriberStateEnum.INACTIVE))
                {
                    // Clear the End date field
                    newSub.setEndDate(null);
                }
            }
        }
    }


    private void syncActivationDateForStateChange(Context ctx, Subscriber oldSub, Subscriber newSub) throws HomeException
    {
        if(EnumStateSupportHelper.get(ctx).isEnteringState(oldSub, newSub, SubscriberStateEnum.ACTIVE)
                && ((oldSub == null && newSub.getStartDate() == null) || 
                        (EnumStateSupportHelper.get(ctx).stateEquals(oldSub, SubscriberStateEnum.AVAILABLE) && !ctx.getBoolean(AcctSubBulkLoadRequestServicer.BULKLOAD_RUNNING, Boolean.FALSE))))
        {
            /*
             * TT : 7042647870 Removing Expired state so that startDate would not get
             * changed it is activated again
             */
            newSub.setStartDate(new Date());
        }
        else if (oldSub == null && newSub.getStartDate() != null && newSub.isPostpaid())
        {
            Set<SubscriberServices> services = newSub.getIntentToProvisionServices(ctx);
            for (SubscriberServices service : services)
            {
                service.setStartDate(newSub.getStartDate());
            }
            
            List<SubscriberAuxiliaryService> auxServices = newSub.getAuxiliaryServices(ctx);
            for (SubscriberAuxiliaryService auxService : auxServices)
            {
                if (AuxiliaryServiceSupport.supportsPreDating(ctx, auxService.getAuxiliaryService(ctx)))
                {
                    auxService.setStartDate(newSub.getStartDate());
                }
            }
        }
    }





    /**
     * For Prepaid Subscribers - * If the state transistioning is from any state to Active &
     * the Auto-Block is enabled for the subscriber's Spid, then change the state of the
     * subscriber to LOCKED/BARRED
     *
     * For Pospaid Subscribers - * If the state transistioning is from any state to Active &
     * the Auto-Block is enabled for the subscriber's Spid, then change the state of the
     * subscriber to SUSPENDED
     *
     * @param oldSub
     * @param newSub
     * @throws HomeException
     */
    private void updateFraudProfile(Context ctx, Subscriber oldSub, Subscriber newSub) throws HomeException
    {
      boolean autoBlockRequired = false;
      CRMSpid sp = SpidSupport.getCRMSpid(getContext(), newSub.getSpid());
      if (sp != null)
      {
        autoBlockRequired = sp.getAutoBlockOnVoucherFraud();
      }
      else
      {
        if (LogSupport.isDebugEnabled(getContext()))
        {
          new DebugLogMsg(this, "Voucher Fraud Profile Update check for " + newSub.getMSISDN()
              + " will not be made." + "Reason: Spid with ID " + newSub.getSpid() + "doesn't exist.", null)
              .log(getContext());
        }
        return;
      }


      if (newSub != null)
      {
        // subscriber's (prepaid/postpaid) Fraud profile should be reset when subscriber is deactivated.
        if (EnumStateSupportHelper.get(ctx).stateEquals(newSub, SubscriberStateEnum.INACTIVE))
        {
              newSub.setVraFraudProfile(false);
              LogSupport.debug(ctx , this , "Resetting the Fraud profile of the subscriber " +newSub.getMSISDN());
        }

        // If Auto-Blocking enabled and Vra Fraud state is true.
        if (autoBlockRequired && newSub.getVraFraudProfile())
        {
          // Available -> Locked/Barred state transition is not supported.To achieve this we use, Available -> Active followed by Active -> Locked/Barred
          if (newSub.getSubscriberType().equals(SubscriberTypeEnum.PREPAID)
                  && EnumStateSupportHelper.get(ctx).isTransition(oldSub, newSub, SubscriberStateEnum.AVAILABLE, SubscriberStateEnum.ACTIVE))
          {
            //This would activate the subscriber.
            super.store(ctx, newSub);
          }
          // if subscriber is prepaid and state change required to be propogated then change subscriber state to barred
          if (newSub.getSubscriberType().equals(SubscriberTypeEnum.PREPAID)
              && EnumStateSupportHelper.get(ctx).isTransition(oldSub, newSub, 
                      Arrays.asList(
                              SubscriberStateEnum.LOCKED, 
                              SubscriberStateEnum.EXPIRED, 
                              SubscriberStateEnum.SUSPENDED,
                              SubscriberStateEnum.AVAILABLE), 
                      SubscriberStateEnum.ACTIVE))
          {
            if (newSub.getState().getIndex() != SubscriberStateEnum.LOCKED_INDEX)
            {
              newSub.setState(SubscriberStateEnum.LOCKED);
            }
            if (!ctx.getBoolean(VRAFraudERAgent.NOTE_HANDLED_BY_VRAFRAUDERAGENT))
            {
              ctx.put(VRAFraudERAgent.SUSPEND_DUE_TO_FRAUD_PROFILE, true);
            }
          }
          else if (newSub.getSubscriberType().equals(SubscriberTypeEnum.POSTPAID) &&
              EnumStateSupportHelper.get(ctx).isTransition(oldSub, newSub, SubscriberStateEnum.SUSPENDED, SubscriberStateEnum.ACTIVE))
          {
            newSub.setState(SubscriberStateEnum.SUSPENDED);
            ctx.put(VRAFraudERAgent.SUSPEND_DUE_TO_FRAUD_PROFILE, true);
          }
        }
      }
    }


    /**
     * Utility method to calculate the Secondary Price Plan start Date based on
     * the subscriber secondary pp start date from SCT.
     * @param ctx Context object
     * @param sub Subscriber object
     * @return java.util.Date object
     */
    private static Date calculateSecondaryPPStartDateOnActivation(final Context ctx, final Subscriber sub)
    {
        //setting the default sec PP start date to 5 yrs from current date
        if (ctx == null || sub == null)
        {
            return CalendarSupportHelper.get(ctx).findDateYearsAfter(CommonTime.YEARS_IN_FUTURE, new Date());
        }
        Date secPPStartDt = sub.getSecondaryPricePlanStartDate();
        Date subCreateDt = sub.getDateCreated();
        Date subActivateDt = sub.getStartDate();

        int diffDays =(int) CalendarSupportHelper.get(ctx).getNumberOfDaysBetween(subCreateDt,subActivateDt);

        if (secPPStartDt != null)
        {
            secPPStartDt = CalendarSupportHelper.get(ctx).findDateDaysAfter(diffDays,secPPStartDt);
        }
       else if (subActivateDt != null)
        {
            // Setting the sec PP Start date to 5 yrs from Activation date.
            secPPStartDt = CalendarSupportHelper.get(ctx).findDateYearsAfter(CommonTime.YEARS_IN_FUTURE, subActivateDt);
        }

        return secPPStartDt;
    }


    /**
     * Utility method to calculate the Seconday Priceplan End date based on the Sec
     * PP end date coming from the SCT.
     * @param ctx Context object
     * @param sub Subscriber object
     * @return java.util.Date object
     */
    private static Date CalculateSecondaryPPEndDateOnActivation(final Context ctx, final Subscriber sub)
    {
        //setting the default sec PP start date to 5 yrs from current date
        if (ctx == null || sub == null)
        {
            return CalendarSupportHelper.get(ctx).findDateYearsAfter(CommonTime.YEARS_IN_FUTURE, new Date());
        }

        Date secPPEndDt = sub.getSecondaryPricePlanEndDate();
        Date subCreateDt = sub.getDateCreated();
        Date subActivateDt = sub.getStartDate();
        Date secPPStartDt = sub.getSecondaryPricePlanStartDate();

       int diffDays = (int)CalendarSupportHelper.get(ctx).getNumberOfDaysBetween(subCreateDt,subActivateDt);

        if (secPPEndDt != null)
        {
            secPPEndDt = CalendarSupportHelper.get(ctx).findDateDaysAfter(diffDays,secPPEndDt);
        }
        else if (secPPStartDt != null)
        {
            //Setting the Sec PP end date to 5 yrs from Sec PP Start Date
            secPPEndDt = CalendarSupportHelper.get(ctx).findDateYearsAfter(CommonTime.YEARS_IN_FUTURE, secPPStartDt);
        }

        return secPPEndDt;
    }

}
