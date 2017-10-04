package com.trilogy.app.crm.home.account;

import java.util.Collection;

import com.trilogy.app.crm.account.AccountConstants;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.core.AuxiliaryService;
import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.bean.CallingGroupTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.account.SubscriptionTypeEnum;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;


public class AccountBirthdayPlanValidator implements Validator
{
    private static AccountBirthdayPlanValidator INSTANCE = new AccountBirthdayPlanValidator();
    
    public static AccountBirthdayPlanValidator instance()
    {
        return INSTANCE;
    }

    private AccountBirthdayPlanValidator()
    {
        
    }

    /* (non-Javadoc)
     * @see com.redknee.framework.xhome.beans.Validator#validate(com.redknee.framework.xhome.context.Context, java.lang.Object)
     */
    public void validate(Context ctx, Object obj) throws IllegalStateException
    {
        Account account = (Account) obj;
        Account oldAccount = (Account) ctx.get(AccountConstants.OLD_ACCOUNT);
        
        if (account.getDateOfBirth() == null && oldAccount!=null && oldAccount.getDateOfBirth() != null)
        {
            try
            {
                final Collection<Subscriber> subscribers = account.getSubscribers(ctx);
                if (subscribers.size()>0)
                {
                    for (Subscriber subscriber : subscribers)
                    {
                        if (SubscriptionTypeEnum.AIRTIME_INDEX == subscriber.getSubscriptionType(ctx).getType())
                        {
                            validateSubscriberBirthdayPlan(ctx, subscriber, account);
                            break;
                        }
                    }
                }
            }
            catch (HomeException e)
            {
                StringBuffer sb = new StringBuffer();
                sb.append("Failed to retrieve subscriptions. BAN='");
                sb.append(account.getBAN());
                sb.append("': ");
                sb.append(e.getMessage());
                LogSupport.minor(ctx, this, sb.toString(), e);
                throw new IllegalStateException("Failed to retrieve account's subscriptions. Birthday plan validation cannot be performed.");
            }
        }
    }
    
    private void validateSubscriberBirthdayPlan(final Context ctx, final Subscriber subscriber, final Account account) throws IllegalStateException
    {
        CompoundIllegalStateException exception = new CompoundIllegalStateException();
        
        try
        {
            final Collection<AuxiliaryService> services = SubscriberAuxiliaryServiceSupport.getAuxiliaryServiceCollection(ctx, subscriber.getAuxiliaryServices(ctx), null);
            for (final AuxiliaryService service : services)
            {
                if (service.isBirthdayPlan(ctx))
                {
                    if (account.getDateOfBirth()==null)
                    {
                        exception.thrown(new IllegalPropertyArgumentException(AccountXInfo.DATE_OF_BIRTH, "Date of Birth cannot be empty as subscription '" + subscriber.getId() + "' has a Birthday Plan auxiliary service selected."));
                    }
                    break;
                }
            }
        }
        catch (HomeException e)
        {
            StringBuffer sb = new StringBuffer();
            sb.append("Failed to retrieve subscriber auxiliary services. SubscriberID='");
            sb.append(subscriber.getId());
            sb.append("', MSISDN='");
            sb.append(subscriber.getMSISDN());
            sb.append("': ");
            sb.append(e.getMessage());
            LogSupport.minor(ctx, this, sb.toString(), e);
            throw new IllegalStateException("Failed to retrieve subscriber auxiliary services. Birthday plan validation cannot be performed.");
        }

        exception.throwAll();            
    }
}