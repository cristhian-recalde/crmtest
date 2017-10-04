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
package com.trilogy.app.crm.home.account.extension;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.NEQ;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xlog.log.MajorLogMsg;

import com.trilogy.app.crm.account.AccountConstants;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.TransactionXInfo;
import com.trilogy.app.crm.bean.account.SubscriptionTypeEnum;
import com.trilogy.app.crm.bean.core.SubscriptionType;
import com.trilogy.app.crm.extension.account.PoolExtension;
import com.trilogy.app.crm.extension.account.PoolExtensionXInfo;
import com.trilogy.app.crm.extension.account.SubscriptionPoolProperty;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.SubscriberSupport;

/**
 * Validates the Pooled account definition.
 *
 * @author victor.stratan@redknee.com
 */
public final class PooledAccountValidator implements Validator
{
    /**
     * {@inheritDoc}
     */
    public void validate(final Context ctx, final Object obj) throws IllegalStateException
    {
        final Account account = (Account) obj;
        if(EnumStateSupportHelper.get(ctx).isOneOfStates(account,ACCOUNT_EXTENSION_DEACTIVATED_STATES))
        {
            return;
        }

        if (account.isPooled(ctx))
        {
            if (SpidSupport.getPooledSubscriptionLevel(ctx, account.getSpid()) == CRMSpid.DEFAULT_POOLSUBSCRIPTIONLEVEL)
            {
                throwError(ctx, AccountXInfo.GROUP_TYPE,
                        "Account of type Pooled cannot be used because Service Provider does not have the "
                                + "Pooled Group Subscription Level specified", null);
            }

            final PoolExtension extension = (PoolExtension) account.getFirstAccountExtensionOfType(PoolExtension.class);
            if (extension == null)
            {
                throwError(ctx, AccountXInfo.GROUP_TYPE,
                        "Account of type Pooled has to define a Pool Extension", null);
            }
            else
            {
                if (extension.getPoolMSISDN().length() == 0)
                {
                    throwError(ctx, PoolExtensionXInfo.POOL_MSISDN,
                            "Pool Extension requires the MSISDN value set.", null);
                }

                final Map<Long, SubscriptionPoolProperty> properties = extension.getSubscriptionPoolProperties();
                if (SubscriberTypeEnum.POSTPAID.equals(account.getSystemType()))
                {
                    for (SubscriptionPoolProperty poolProperty : properties.values())
                    {
                        final SubscriptionType subType = poolProperty.getSubscriptionType(ctx);
                        if (!subType.isOfType(SubscriptionTypeEnum.AIRTIME))
                        {
                            throwError(ctx, PoolExtensionXInfo.SUBSCRIPTION_POOL_PROPERTIES,
                                    "Non Airtime Pool in Postpaid account is not supported.", null);
                        }
                    }
                }

                final Account oldAccount = (Account) ctx.get(AccountConstants.OLD_ACCOUNT);
                if (oldAccount != null && oldAccount.isPooled(ctx))
                {
                    // Restrict billing type conversion for group pooled accounts
                    final SubscriberTypeEnum newType = account.getSystemType();
                    final SubscriberTypeEnum oldType = oldAccount.getSystemType();
                    if (!newType.equals(oldType))
                    {
                        //Do not allow conversion of Prepaid accounts, since leader of hybrid and postpaid must be a postpaid subscription
                        if (SubscriberTypeEnum.PREPAID.equals(oldType)) 
                        {
                            throwError(ctx, AccountXInfo.SYSTEM_TYPE, 
                                    "Billing type conversion from prepaid is not supported for pooled accounts.", null);
                        }
                        //Do not allow conversion from Postpaid to Prepaid
                        if (SubscriberTypeEnum.POSTPAID.equals(oldType) && SubscriberTypeEnum.PREPAID.equals(newType)) 
                        {
                            throwError(ctx, AccountXInfo.SYSTEM_TYPE, 
                                    "Billing type conversion from postpaid to prepaid is not supported for pooled accounts.", null);
                        }
                        //Do not allow conversion from Hybrid to Prepaid
                        if (SubscriberTypeEnum.HYBRID.equals(oldType) && SubscriberTypeEnum.PREPAID.equals(newType)) 
                        {
                            throwError(ctx, AccountXInfo.SYSTEM_TYPE, 
                                    "Billing type conversion from hybrid to prepaid is not supported for pooled accounts.", null);
                        }                        
                        //If converting from Hybrid to Postpaid, only allow if no active Prepaid subscribers exist
                        if (SubscriberTypeEnum.HYBRID.equals(oldType) && SubscriberTypeEnum.POSTPAID.equals(newType))
                        {
                            
                            final And filter = new And();
                            filter.add(new EQ(SubscriberXInfo.POOL_ID, account.getBAN()));
                            filter.add(new EQ(SubscriberXInfo.SUBSCRIBER_TYPE, SubscriberTypeEnum.PREPAID));
                            filter.add(new NEQ(SubscriberXInfo.STATE, SubscriberStateEnum.INACTIVE));
                            
                            boolean hasPrepaid = false;
                            try
                            {
                                hasPrepaid = HomeSupportHelper.get(ctx).hasBeans(ctx, Subscriber.class, filter);
                            }
                            catch (HomeInternalException e)
                            {
                                final String message = "Unable to determine if account[BAN=%s] has prepaid member subscriptions.";
                                new MajorLogMsg(this, String.format(message, account.getBAN()), e).log(ctx);
                            }
                            catch (HomeException e)
                            {
                                final String message = "Unable to determine if account[BAN=%s] has prepaid member subscriptions.";
                                new MajorLogMsg(this, String.format(message, account.getBAN()), e).log(ctx);
                            }                            
                            
                            if (hasPrepaid)
                            {
                                throwError(ctx, AccountXInfo.SYSTEM_TYPE, 
                                        "Cannot convert from hybrid to postpaid if prepaid subscriptions exist for pooled accounts..", null);
                            }
                        }
                    }
                    
                    final PoolExtension oldExtention = (PoolExtension) oldAccount.getFirstAccountExtensionOfType(PoolExtension.class);
                    if (oldExtention != null)
                    {
                        final Map oldProperties = oldExtention.getSubscriptionPoolProperties();

                        if (oldProperties.keySet().equals(properties.keySet()))
                        {
                            // nothing changed - let through. This may be a next sub id update
                            return;
                        }

                        final Set oldPropClone = new HashSet(oldProperties.keySet());
                        oldPropClone.removeAll(properties.keySet());
                        if (oldPropClone.size() > 0)
                        {
                            throwError(ctx, AccountXInfo.ACCOUNT_EXTENSIONS,
                                    "Removing Subscription Type property from Pool Extension is not supported.", null);
                        }
                    }
                                        
                }

                if (properties.size() == 0)
                {
                    throwError(ctx, AccountXInfo.ACCOUNT_EXTENSIONS,
                            "Pool Extension should have at least one Subscription Type property.", null);
                }

                // TODO victor: check LicensingUtil.isLicensed(ctx, LicenseConstants.PREPAID_GROUP_POOLED_LICENSE_KEY)
            }
        }
    }

    private void throwError(final Context ctx, final PropertyInfo property, final String msg, final Exception cause)
        throws IllegalStateException
    {
        final CompoundIllegalStateException compoundException = new CompoundIllegalStateException();
        final IllegalPropertyArgumentException propertyException = new IllegalPropertyArgumentException(property, msg);
        if (cause != null)
        {
            propertyException.initCause(cause);
        }
        compoundException.thrown(propertyException);
        compoundException.throwAll();
    }
    
    private static final Collection<AccountStateEnum> ACCOUNT_EXTENSION_DEACTIVATED_STATES = Collections
    .unmodifiableSet(new HashSet<AccountStateEnum>(Arrays.asList(AccountStateEnum.INACTIVE)));

}