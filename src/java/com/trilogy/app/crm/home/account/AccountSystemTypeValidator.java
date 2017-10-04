package com.trilogy.app.crm.home.account;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.account.AccountConstants;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.LicensingSupportHelper;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.NEQ;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

public class AccountSystemTypeValidator implements Validator
{
    public AccountSystemTypeValidator()
    {
    }

    public void invalidSystemType(ExceptionListener el, SubscriberTypeEnum type)
    {
        el.thrown(new IllegalPropertyArgumentException("Account.systemType", "Unsupported type "+type.getDescription()));
    }

    @Override
    public void validate(Context ctx, Object obj)
        throws IllegalStateException
    {
        Account oldAccount = (Account) ctx.get(AccountConstants.OLD_ACCOUNT);
        CompoundIllegalStateException el = new CompoundIllegalStateException();
        Account account = (Account)obj;

        if (SubscriberTypeEnum.PREPAID.equals(account.getSystemType()))
        {
    	    if (LicensingSupportHelper.get(ctx).isLicensed(ctx, LicenseConstants.PREPAID_LICENSE_KEY))
            {
    	        if (oldAccount!=null && oldAccount.getSystemType() != account.getSystemType())
    	        {
    	            validatePrepaid(ctx, account, el);
    	        }
            }
            else
            {
                invalidSystemType(el, account.getSystemType());
            }
        }
        else if (SubscriberTypeEnum.POSTPAID.equals(account.getSystemType()))
        {
    	    if (LicensingSupportHelper.get(ctx).isLicensed(ctx, LicenseConstants.POSTPAID_LICENSE_KEY))
            {
                if (oldAccount!=null && oldAccount.getSystemType() != account.getSystemType())
                {
                    validatePostpaid(ctx, account, el);
                }
            }
            else
            {
                invalidSystemType(el, account.getSystemType());
            }
        }
        else if (SubscriberTypeEnum.HYBRID.equals(account.getSystemType()))
        {
    	    if (!LicensingSupportHelper.get(ctx).isLicensed(ctx, LicenseConstants.HYBRID_LICENSE_KEY))
            {
                invalidSystemType(el, account.getSystemType());
            }
        }
        else
        {
            // should not happend. nothing to validate
            invalidSystemType(el, account.getSystemType());
        }
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, "after validation el has size "+el.getSize(), null).log(ctx);
        }
        el.throwAll();
    }


    protected void validatePrepaid(Context ctx, Account account, ExceptionListener el)
        throws IllegalStateException
    {
        if (containsSubscriptions(ctx, account, SubscriberTypeEnum.POSTPAID))
        {
            el.thrown(new IllegalPropertyArgumentException("Account.systemType", "Account cannot be prepaid since it contains postpaid subscriptions"));
        }

        Account oldAccount = getOldAccount(ctx);
        if(null != oldAccount && !SafetyUtil.safeEquals(oldAccount.getSystemType(), account.getSystemType()))
        {
            if(containsSubscribers(ctx, account, SubscriberTypeEnum.POSTPAID))
            {
                el.thrown(new IllegalPropertyArgumentException(AccountXInfo.SYSTEM_TYPE, "Account cannot be changed to prepaid since it contains postpaid subscriber accounts."));
            }
            else if(containsSubscribers(ctx, account, SubscriberTypeEnum.HYBRID))
            {
                el.thrown(new IllegalPropertyArgumentException(AccountXInfo.SYSTEM_TYPE, "Account cannot be changed to prepaid since it contains hybrid subscriber accounts."));
            }
        }
    }

    protected void validatePostpaid(Context ctx, Account account, ExceptionListener el)
        throws IllegalStateException
    {
        if (containsSubscriptions(ctx, account, SubscriberTypeEnum.PREPAID))
        {
            el.thrown(new IllegalPropertyArgumentException(AccountXInfo.SYSTEM_TYPE, "Account cannot be postpaid since it contains prepaid subscriptions."));
        }

        Account oldAccount = getOldAccount(ctx);
        if(null != oldAccount && !SafetyUtil.safeEquals(oldAccount.getSystemType(), account.getSystemType()))
        {
            if(containsSubscribers(ctx, account, SubscriberTypeEnum.PREPAID))
            {
                el.thrown(new IllegalPropertyArgumentException(AccountXInfo.SYSTEM_TYPE, "Account cannot be changed to postpaid since it contains prepaid subscriber accounts."));
            }
            else if(containsSubscribers(ctx, account, SubscriberTypeEnum.HYBRID))
            {
                el.thrown(new IllegalPropertyArgumentException(AccountXInfo.SYSTEM_TYPE, "Account cannot be changed to postpaid since it contains hybrid subscriber accounts."));
            }
        }
    }

    protected boolean containsSubscribers(Context ctx, Account account, SubscriberTypeEnum subType)
    {
        boolean result = false;
        
        And filter = new And();
        filter.add(new EQ(AccountXInfo.PARENT_BAN, account.getBAN()));
        filter.add(new EQ(AccountXInfo.SYSTEM_TYPE, subType));
        filter.add(new NEQ(AccountXInfo.STATE, AccountStateEnum.INACTIVE));

        try
        {
             result = HomeSupportHelper.get(ctx).hasBeans(ctx, Account.class, filter);
        }
        catch (HomeException e)
        {
            new MinorLogMsg(this, 
                    "Error determining whether or not account " + account.getBAN()
                    + " contains any " + subType + " subscriptions.", e).log(ctx);
        }
        
        return result;
    }

    protected boolean containsSubscriptions(Context ctx, Account account, SubscriberTypeEnum subType)
    {
        try
        {
            And filter = new And();
            filter.add(new EQ(SubscriberXInfo.BAN, account.getBAN()));
            filter.add(new EQ(SubscriberXInfo.SUBSCRIBER_TYPE, subType));
            filter.add(new NEQ(SubscriberXInfo.STATE, SubscriberStateEnum.INACTIVE));
            
            return HomeSupportHelper.get(ctx).hasBeans(ctx, Subscriber.class, filter);
        }
        catch (HomeException hEx)
        {
            new MinorLogMsg(this, "Unable to determine if account contains "+subType.getDescription()+" subscribers", hEx).log(ctx);
            return false;
        }
    }

    private Account getOldAccount(Context ctx)
    {
        Account oldAccount = (Account) ctx.get(AccountConstants.OLD_ACCOUNT);
        return oldAccount;
    }
}