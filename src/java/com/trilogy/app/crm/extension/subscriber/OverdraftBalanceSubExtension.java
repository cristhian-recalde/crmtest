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
package com.trilogy.app.crm.extension.subscriber;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.OverdraftBalanceLimit;
import com.trilogy.app.crm.bean.OverdraftBalanceLimitXInfo;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SystemNoteSubTypeEnum;
import com.trilogy.app.crm.bean.SystemNoteTypeEnum;
import com.trilogy.app.crm.client.bm.BalanceManagementSupport;
import com.trilogy.app.crm.client.bm.SubscriberProfileProvisionClient;
import com.trilogy.app.crm.client.bm.SubscriberProfileProvisionException;
import com.trilogy.app.crm.extension.DependencyValidatableExtension;
import com.trilogy.app.crm.extension.ExtensionInstallationException;
import com.trilogy.app.crm.extension.spid.OverdraftBalanceSpidExtension;
import com.trilogy.app.crm.extension.spid.OverdraftBalanceSpidExtensionXInfo;
import com.trilogy.app.crm.license.LicenseAware;
import com.trilogy.app.crm.subscriber.provision.SubscriberProvisionResultCode;
import com.trilogy.app.crm.support.CurrencyPrecisionSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.LicensingSupportHelper;
import com.trilogy.app.crm.support.NoteSupportHelper;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeOperationEnum;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.SeverityEnum;

/**
 * Overdraft Balance subscription extension.
 * @author Marcio Marques
 * @since 9.1.1
 *
 */
public class OverdraftBalanceSubExtension extends AbstractOverdraftBalanceSubExtension implements LicenseAware, BypassURCSAware, DependencyValidatableExtension
{

    
    private boolean bypassURCS_ = false;

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public void bypassURCS()
    {
        bypassURCS_ = true;
    }
    
    public void unBypassURCS()
    {
        bypassURCS_ = false;
    }

    public boolean isBypassURCS()
    {
        return bypassURCS_;
    }
    
    @Override
    public long getLimit()
    {
        return getLimit(ContextLocator.locate());
    }
    
    public long getLimit(Context context)
    {
        long limit = 0;
        
        if (super.getLimit()!=DEFAULT_LIMIT)
        {
            limit = super.getLimit();
        }
        else
        {
            int spid = -1;
            Subscriber subscriber = getSubscriber(context);
            if (subscriber!=null)
            {
                spid = subscriber.getSpid();
            }
            else if (context.has(Account.class))
            {
                spid = ((Account) context.get(Account.class)).getSpid();
            }
            
            if (spid!=-1)
            {
                try
                {
                    OverdraftBalanceSpidExtension extension = HomeSupportHelper.get(context).findBean(context,
                            OverdraftBalanceSpidExtension.class,
                            new EQ(OverdraftBalanceSpidExtensionXInfo.SPID, Integer.valueOf(spid)));
                    if (extension!=null)
                    {
                        limit = extension.getLimit();
                    }
                }
                catch (HomeException e)
                {
                    LogSupport.minor(context, this, "Unable to retrieve Overdraft Balance Limit SPID extension for SPID "
                            + getSubscriber(context).getSpid() + ": " + e.getMessage(), e);
                }
            }
        }
        
        return limit;
    }
    
    private long getNewLimit(Context ctx) throws HomeException
    {
        if (this.getOverdraftBalanceLimitId() != DEFAULT_OVERDRAFTBALANCELIMITID)
        {
            OverdraftBalanceLimit limit = (OverdraftBalanceLimit) HomeSupportHelper.get(ctx).findBean(ctx,
                    OverdraftBalanceLimit.class,
                    new EQ(OverdraftBalanceLimitXInfo.ID, this.getOverdraftBalanceLimitId()));
            if (limit!=null)
            {
                return limit.getLimit();
            }
            else
            {
                return 0;
            }
        }
        else if (this.getNewLimit() != DEFAULT_NEWLIMIT)
        {
            return this.getNewLimit();
        }
        else
        {
            return this.getLimit();
        }
                
    }
    
    @Override
    public void install(Context ctx) throws ExtensionInstallationException
    {
        try
        {
            updateOverdraftBalance(ctx, this.getNewLimit(ctx));
        }
        catch (HomeException exception)
        {
            Subscriber subscriber = this.getSubscriber(ctx);
            final String msg = "Unable to install overdraft balance limit for subscriber "
                    + subscriber.getId()
                    + ": Unable to retrieve Overdraft Balance Limit value";
            LogSupport.minor(ctx, this, msg, exception);
            throw new ExtensionInstallationException(msg, exception, false);
        }
    }

    @Override
    public void update(Context ctx) throws ExtensionInstallationException
    {
        try
        {
            if (this.getOverdraftBalanceLimitId() != DEFAULT_OVERDRAFTBALANCELIMITID || this.getNewLimit() != DEFAULT_NEWLIMIT)
            {
                updateOverdraftBalance(ctx, this.getNewLimit(ctx));
            }
        }
        catch (HomeException exception)
        {
            Subscriber subscriber = this.getSubscriber(ctx);
            final String msg = "Unable to update overdraft balance limit for subscriber "
                    + subscriber.getId()
                    + ": Unable to retrieve Overdraft Balance Limit value";
            LogSupport.minor(ctx, this, msg, exception);
            throw new ExtensionInstallationException(msg, exception, false);
        }
    }

    @Override
    public void uninstall(Context ctx) throws ExtensionInstallationException
    {
        long overdraftBalanceLimit = 0;
        Subscriber subscriber = getSubscriber(ctx);
        int spidId = subscriber.getSpid();
        try
        {
            OverdraftBalanceSpidExtension extension = HomeSupportHelper.get(ctx).findBean(ctx,
                    OverdraftBalanceSpidExtension.class,
                    new EQ(OverdraftBalanceSpidExtensionXInfo.SPID, Integer.valueOf(getSubscriber(ctx).getSpid())));
            if (extension!=null)
            {
                overdraftBalanceLimit = extension.getLimit();
            }
        }
        catch (HomeException e)
        {
            LogSupport.minor(ctx, this, "Unable to find SPID " + spidId + ". Setting overdraft balance limit to 0 upon removal: " + e.getMessage(), e);
        }
        
        try
        {
            validateLimit(ctx, subscriber, overdraftBalanceLimit);
        }
        catch (IllegalPropertyArgumentException e)
        {
            throw new ExtensionInstallationException("Unable to remove Overdraft Balance Limit extension: " + e.getMessageText(), e, false, true);
        }

        updateOverdraftBalance(ctx, overdraftBalanceLimit);
    }

    public void updateOverdraftBalance(Context ctx, long limit) throws ExtensionInstallationException
    {
        if (!isBypassURCS())
        {
            Subscriber subscriber = getSubscriber(ctx);
    
            if (LogSupport.isEnabled(ctx, SeverityEnum.INFO))
            {
                LogSupport.info(
                        ctx,
                        this,
                        "Updating overdraft balance for subscriber '"
                                + subscriber.getId()
                                + "' to "
                                + CurrencyPrecisionSupportHelper.get(ctx).formatDisplayCurrencyValue(ctx,
                                        subscriber.getCurrency(ctx), limit));
            }
    
            try
            {
                final SubscriberProfileProvisionClient client = BalanceManagementSupport.getSubscriberProfileProvisionClient(ctx);
                if (client!=null)
                {
                    client.updateOverdraftBalanceLimit(ctx, subscriber, limit);
                    createModificationNote(ctx, subscriber, limit);
                }
                else
                {
                    final String msg = "Unable to update overdraft balance limit for subscriber "
                            + subscriber.getId()
                            + " to "
                            + CurrencyPrecisionSupportHelper.get(ctx).formatDisplayCurrencyValue(ctx,
                                    subscriber.getCurrency(ctx), limit) + ": SubscriberProfileProvisionClient not installed";
                    LogSupport.minor(ctx, this, msg);
                    throw new ExtensionInstallationException(msg , null, false);
                }
            }
            catch (final SubscriberProfileProvisionException exception)
            {
                final short result = exception.getErrorCode();
                SubscriberProvisionResultCode.setProvisionUpsErrorCode(ctx, result);
                final String msg = "Unable to update overdraft balance limit for subscription "
                        + subscriber.getId()
                        + " to "
                        + CurrencyPrecisionSupportHelper.get(ctx).formatDisplayCurrencyValue(ctx,
                                subscriber.getCurrency(ctx), limit) + " due to an error on CPS (" + result + ")";
                LogSupport.minor(ctx, this, msg, exception);
                throw new ExtensionInstallationException(msg , exception, false);
            }
        }
        this.setLimit(limit);
    }
    
    public void validateLimit(Context ctx, Subscriber subscriber, long limit) throws IllegalPropertyArgumentException
    {
        long overdraftBalance = subscriber.getOverdraftBalance(ctx);
        if (overdraftBalance>limit)
        {
            throw new IllegalPropertyArgumentException(OverdraftBalanceSubExtensionXInfo.OVERDRAFT_BALANCE_LIMIT_ID,
                "Overdraft Balance Limit cannot be lower than the current used overdraft balance of "
                        + CurrencyPrecisionSupportHelper.get(ctx).formatDisplayCurrencyValue(ctx,
                                subscriber.getCurrency(ctx), overdraftBalance));
        }
    }

    @Override
    public void validate(Context ctx) throws IllegalStateException
    {
        if (!isBypassURCS())
        {
            Subscriber subscriber = getSubscriber(ctx);
            CompoundIllegalStateException cise = new CompoundIllegalStateException();
            
            long limit;
            try
            {
                    limit = getNewLimit(ctx);
            }
            catch (HomeException exception)
            {
                final String msg = "Unable to update retrieve overdraft balance limit for subscriber "
                        + subscriber.getId() + ". Using 0 instead.";
                LogSupport.minor(ctx, this, msg, exception);
                limit = 0;
            }
            
            try
            {
                validateLimit(ctx, subscriber, limit);
            }
            catch (IllegalPropertyArgumentException t)
            {
                cise.thrown(t);
            }


            cise.throwAll();
        }
    }

    public void deactivate(Context ctx) throws ExtensionInstallationException
    {
        Home home = (Home) ctx.get(OverdraftBalanceSubExtensionHome.class);
        try
        {
            home.remove(ctx, this);
        }
        catch (HomeException e)
        {
            StringBuilder errorMessage = new StringBuilder();
            errorMessage.append("Unable to deactivate Overdraft Balance Subscription Extension for subscription ");
            errorMessage.append(this.getSubId());
            errorMessage.append(" due to an error on URCS.");

            LogSupport.minor(ctx, this, errorMessage.toString() + " Exception: " + e.getMessage(), e);
            throw new ExtensionInstallationException(errorMessage.toString(), e, true, false);
        }
    }

    @Override
    public boolean isValidForSubscriberType(SubscriberTypeEnum subscriberType)
    {
        return SubscriberTypeEnum.PREPAID.equals(subscriberType);
    }
    
    private void createModificationNote(Context ctx, Subscriber subscriber, long limit)
    {
        try
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Subscription overdraft balance limit updated to ");
            sb.append(CurrencyPrecisionSupportHelper.get(ctx).formatDisplayCurrencyValue(ctx,
                    subscriber.getCurrency(ctx), limit));
            sb.append(".");
            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, this, sb.toString());
            }
            NoteSupportHelper.get(ctx).addSubscriberNote(ctx, subscriber.getId(), sb.toString(), SystemNoteTypeEnum.EVENTS , SystemNoteSubTypeEnum.OVERDRAFT_BALANCE_LIMIT);
        }
        catch (HomeException e)
        {
            LogSupport.minor(ctx, this, "Unable to add subscriber note regarding overdraft balance limit update: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isLicensed(Context ctx)
    {
        return LicensingSupportHelper.get(ctx).isLicensed(ctx, LicenseConstants.OVERDRAFT_BALANCE_LICENSE);
    }

    public boolean transientEquals(Object o)
    {
        boolean result = super.transientEquals(o);
        OverdraftBalanceSubExtension extension = (OverdraftBalanceSubExtension) o;
        
        if (extension!=null)
        {
            result = result && bypassURCS_ == extension.isBypassURCS();
        }
        
        return result;
    }

    @Override
    public void validateDependency(Context ctx) throws IllegalStateException
    {
        HomeOperationEnum operation = (HomeOperationEnum) ctx.get(HomeOperationEnum.class);

        if (HomeOperationEnum.REMOVE.equals(operation))
        {
            CompoundIllegalStateException cise = new CompoundIllegalStateException();
            long overdraftBalanceLimit = 0;
            Subscriber subscriber = getSubscriber(ctx);
            int spidId = subscriber.getSpid();
            
            try
            {
                OverdraftBalanceSpidExtension extension = HomeSupportHelper.get(ctx).findBean(ctx,
                        OverdraftBalanceSpidExtension.class,
                        new EQ(OverdraftBalanceSpidExtensionXInfo.SPID, Integer.valueOf(getSubscriber(ctx).getSpid())));
                if (extension!=null)
                {
                    overdraftBalanceLimit = extension.getLimit();
                }
            }
            catch (HomeException e)
            {
                LogSupport.minor(ctx, this, "Unable to find SPID " + spidId + ". Setting overdraft balance limit to 0 upon removal: " + e.getMessage(), e);
            }
            try
            {
                validateLimit(ctx, subscriber, overdraftBalanceLimit);
            }
            catch (IllegalPropertyArgumentException t)
            {
                cise.thrown(t);
            }
            cise.throwAll();
        }

    }
}
