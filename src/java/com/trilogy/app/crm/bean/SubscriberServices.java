package com.trilogy.app.crm.bean;

import java.util.Comparator;
import java.util.Date;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.CommonTime;
import com.trilogy.app.crm.bean.core.Service;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.bean.service.ServiceStateEnum;
import com.trilogy.app.crm.bean.service.SuspendReasonEnum;
import com.trilogy.app.crm.subscriber.provision.ProvisionResultCode;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.ServiceSupport;


public class SubscriberServices extends AbstractSubscriberServices implements Comparable
{
    public void setContext(Context ctx)
    {
        context_ = ctx;
    }
    
    public Context getContext()
    {
        return context_;
    }
    
    public void setService(Service svc)
    {
        this.service_ = svc;
    }
    
    public Service getService(Context ctx) 
    {
        if (ctx == null)
        {
            ctx = ContextLocator.locate();
        }
        
        if ( service_ == null )
        {
            try
            {
                setService(ServiceSupport.getService(ctx, this.getServiceId()));
            }
            catch (Exception ex)
            {
                new MinorLogMsg(this, ex.getMessage(), ex).log(ctx);
            }
        }
        
        if (service_ != null && service_.getContext()==null)
        {
            service_.setContext(ctx);
        }

        return this.service_;

    }
    
    public Service getService() 
    {
        return getService(getContext());
    }
    
    public boolean isEnableCLTC(final Context ctx)
    {
        return getService(ctx).isEnableCLTC();
    }
  
    public int getExecutionOrder()
    {
        return getService().getExecutionOrder();
    }

    /**
     * Reset default values to the given SubscriberServices record when moving to the given state
     * @param service_ the Subscriber Service record
     * @param newState the new state to be set.
     */
    public void assignSuspendReason(
            final Subscriber sub,  
            final ServiceStateEnum newState, 
            final SuspendReasonEnum suspendReason)
    {
        if (needsResetSuspendReason(sub, newState))
        {
            // Reset the Suspend Reason
            this.setSuspendReason(SuspendReasonEnum.NONE);
        }
        else
        {
            this.setSuspendReason(suspendReason);
        }
    }
   

    /**
     * Returns TRUE if the give subscriber needs to have the services status reset.
     * (If they don't have a reason to be suspended then their SuspendReason should be reset.
     * Otherwise returns FALSE.
     * @param subscriber
     * @param state
     * @return
     */
    private boolean needsResetSuspendReason(
            final Subscriber subscriber, 
            final ServiceStateEnum state)
    {
        boolean result = true; 
        if (state.equals(ServiceStateEnum.SUSPENDED) || state.equals(ServiceStateEnum.SUSPENDEDWITHERRORS)
        		|| state.equals(ServiceStateEnum.PROVISIONEDWITHERRORS))
        {
            result = false;
        }
        return result;
    }

    public boolean isPersonalizedFeeSet()
    {
        return getIsfeePersonalizationApplied();
    }

    /**
     * If the service is for one time charge, it will set the default start and end date
     * based on the service configuration.
     * @param ctx
     *              The operating context
     * @param subscriberService
     *              The subscriber service to set dates
     * @param defaultDate
     *              The default start date
     */
    public void setSubscriberServiceDates(final Context ctx, final Date defaultDate)
    {
        this.setStartDate(defaultDate);
        this.setEndDate(CalendarSupportHelper.get(ctx).findDateYearsAfter(CommonTime.YEARS_IN_FUTURE, defaultDate));
        final Service service = getService(ctx);
        if (service != null && ServicePeriodEnum.ONE_TIME.equals(service.getChargeScheme()))
        {
            if (service.getRecurrenceType() == OneTimeTypeEnum.ONE_OFF_FIXED_DATE_RANGE)
            {
                final Date today = new Date();
                this.setStartDate(service.getStartDate());
                if (service.getStartDate().before(today))
                {
                    this.setStartDate(today);
                }

                this.setEndDate(service.getEndDate());
            }
            else
            {
                this.setStartDate(defaultDate);
                if (service.getFixedInterval() == FixedIntervalTypeEnum.DAYS)
                {
                    this.setEndDate(
                            CalendarSupportHelper.get(ctx).findDateDaysAfter(service.getValidity(),
                                    defaultDate));
                }
                else
                {
                    this.setEndDate(
                            CalendarSupportHelper.get(ctx).findDateMonthsAfter(service.getValidity(),
                                    defaultDate));
                }
            }
        }
    }
        
    
    public int compareTo(Object obj) throws ClassCastException
    {
        if ( ! ( obj instanceof SubscriberServices))
        {
            throw new ClassCastException("A Service is not SubscriberService class");
        }

        SubscriberServices subService = (SubscriberServices) obj;
        
        final Long otherId = Long.valueOf(subService.getServiceId());
        final Long thisId = Long.valueOf(getServiceId());

        final String subId = subService.getSubscriberId();
        final String subId2 = getSubscriberId();
        
        if ( subId.equals(subId2))
        {
            return thisId.compareTo(otherId);
        }
        else
        {
            // Doesn't matter what order subscriberid is sorted
           return 1;
        }
    }
    
    
    /**
     * Comparator based on getExecutionOrder used for ordering of provisioning commands.
     */
    public static final Comparator<SubscriberServices> PROVISIONING_ORDER = new Comparator<SubscriberServices>()
    {

        /**
         * Compares the provisioning order of two services.
         *
         * @param s1
         *            First service to be compared.
         * @param s2
         *            Second service to be compared.
         * @return Returns a value greater than zero if s1 should be provisioned after s2,
         *         or a value less than zero if s1 should be provisioned before s2.
         */
        public int compare(final SubscriberServices s1, final SubscriberServices s2)
        {
            if (s1.getExecutionOrder() > s2.getExecutionOrder())
            {
                return 1;
            }

            if (s1.getExecutionOrder() < s2.getExecutionOrder())
            {
                return -1;
            }

            // Needed as a way to consistently break ties
            return s1.compareTo(s2);
        }
    };

    /**
     * Reverse of PROVISIONING_ORDER.
     *
     * @see #PROVISIONING_ORDER
     */
    public static final Comparator<SubscriberServices> UNPROVISIONING_ORDER = new Comparator<SubscriberServices>()
    {

        /**
         * Compares the unprovisioning order of two services.
         *
         * @param o1
         *            First service to be compared.
         * @param o2
         *            Second service to be compared.
         * @return Returns a value greater than zero if o1 should be unprovisioned after
         *         o2, or a value less than zero if o1 should be unprovisioned before o2.
         */
        public int compare(final SubscriberServices o1, final SubscriberServices o2)
        {
            return PROVISIONING_ORDER.compare(o2, o1);
        }
    };
    
	@Override
	public Object clone() throws CloneNotSupportedException {
		SubscriberServices clone = (SubscriberServices) super.clone();
		clone.setContext(null);
		if (clone.getService() != null) {
			clone.getService().setContext(null);
		}
		return clone;
	}

	public long getChangedServiceQuantity()
	{
	    
	    return changedServiceQuantity;
	}
	
	public void setChangedServiceQuantity(long newServiceQuantity)
	{
	    this.changedServiceQuantity = newServiceQuantity;
	}
	
	private long changedServiceQuantity = -1;

    private Service service_;
    // @TODO 
    private ServiceFee2 serviceFee2_;
    private transient Context context_;
    private static final int SUCCESS_UNPROVISION_RESULT_CODE = 0;
}
