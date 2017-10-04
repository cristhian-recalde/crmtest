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
package com.trilogy.app.crm.extension.auxiliaryservice.core.custom;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.externalapp.ExternalAppEnum;
import com.trilogy.app.crm.extension.ExtendedAssociableExtension;
import com.trilogy.app.crm.extension.ExtensionAssociationException;
import com.trilogy.app.crm.support.ExternalAppSupport;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.vpn.SubscriberAuxiliaryVpnServiceSupport;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * 
 * @author Marcio Marques
 * @since 9.1.2
 *
 */
public class VPNAuxSvcExtension extends
        com.redknee.app.crm.extension.auxiliaryservice.core.VPNAuxSvcExtension implements ExtendedAssociableExtension<SubscriberAuxiliaryService>
{

    @Override
    public void associate(Context ctx, SubscriberAuxiliaryService association) throws ExtensionAssociationException
    {
        final Subscriber subscriber = getSubscriber(ctx, association);
        final Account account = SubscriberSupport.lookupAccount(ctx, subscriber.getBAN());

        if (account == null)
        {
            association.setProvisionActionState(false);
            throw new ExtensionAssociationException(ExternalAppEnum.BSS, "Account not found for the given BAN = " + subscriber.getBAN(), ExternalAppSupport.BSS_DATABASE_FAILURE_ACCOUNT_RETRIEVAL);
        }
        
        setAccountVPNMsisdn(ctx, subscriber, account, association);
        association.setProvisionActionState(true);
    }


    @Override
    public void postExternalBeanCreation(Context ctx, SubscriberAuxiliaryService association, boolean success)
            throws ExtensionAssociationException
    {
        if (!success)
        {
            resetAccountVPNMsisdn(ctx, association);
        }
    }

    @Override
    public void updateAssociation(Context ctx, SubscriberAuxiliaryService association) throws ExtensionAssociationException
    {
        final Subscriber subscriber = getSubscriber(ctx, association);
        final Account account = SubscriberSupport.lookupAccount(ctx, subscriber.getBAN());

        if (account == null)
        {
            association.setProvisionActionState(false);
            throw new ExtensionAssociationException(ExternalAppEnum.BSS, "Account not found for the given BAN = " + subscriber.getBAN(), ExternalAppSupport.BSS_DATABASE_FAILURE_ACCOUNT_RETRIEVAL);
        }
        
        setAccountVPNMsisdn(ctx, subscriber, account, association);
        association.setProvisionActionState(true);
    }


    @Override
    public void postExternalBeanUpdate(Context ctx, SubscriberAuxiliaryService association, boolean success)
            throws ExtensionAssociationException
    {
        if (!success)
        {
            resetAccountVPNMsisdn(ctx, association);
        }
    }

    @Override
    public void dissociate(Context ctx, SubscriberAuxiliaryService subAuxSvc) throws ExtensionAssociationException
    {
    }

    @Override
    public void postExternalBeanRemoval(Context ctx, SubscriberAuxiliaryService association, boolean success)
            throws ExtensionAssociationException
    {
        if (success)
        {
            final Subscriber subscriber = getSubscriber(ctx, association);
            final Account account = SubscriberSupport.lookupAccount(ctx, subscriber.getBAN());

            if (account == null)
            {
                throw new ExtensionAssociationException(ExternalAppEnum.BSS, "Account not found for the given BAN = " + subscriber.getBAN(), ExternalAppSupport.BSS_DATABASE_FAILURE_ACCOUNT_RETRIEVAL);
            }

            cleanAccountVPN(ctx, subscriber, account);
        }
    }
    
    
    private void cleanAccountVPN(Context ctx, Subscriber subscriber, Account account) throws ExtensionAssociationException
    {
        try
        {
            // If this is the VPN lead subscriber, then clear the account's VPN MSISDN
            final Account rootAccount = account.getRootAccount(ctx);
            if (SafetyUtil.safeEquals(rootAccount.getVpnMSISDN(), subscriber.getMSISDN()))
            {
                final Home accountHome = (Home) ctx.get(AccountHome.class);
                rootAccount.setVpnMSISDN("");
                accountHome.store(ctx, rootAccount);
            }
        }
        catch (HomeException he)
        {
            new MinorLogMsg(SubscriberAuxiliaryVpnServiceSupport.class,
                    "An error occurred disassociating the subscriber's MSISDN with the account's VPN MSISDN.", he)
                    .log(ctx);
        }
    }
    
    private void resetAccountVPNMsisdn(Context ctx, SubscriberAuxiliaryService association) throws ExtensionAssociationException
    {
        try
        {
            Account account = getVPNAccount();
            if (account!=null)
            {
                account.setVpnMSISDN("");
                HomeSupportHelper.get(ctx).storeBean(ctx, account);
            }
        }
        catch (Exception e)
        {
            new MinorLogMsg(this, "Error resetting VPN MSISDN for subscriber "
                    + association.getSubscriberIdentifier() + "'s account.", null).log(ctx);
        }
    }
    
    private void setAccountVPNMsisdn(Context ctx, Subscriber subscriber, Account subscriberAccount, SubscriberAuxiliaryService association) throws ExtensionAssociationException
    {
        try
        {
            Account account = subscriberAccount.getRootAccount(ctx);
            if (account != null && account.isMom(ctx))
            {
                // if this subscriber was the first subscriber
                if (account.getVpnMSISDN() == null || account.getVpnMSISDN().length() == 0)
                {
                    vpnAccount_ = account;
                    final Home actHome = (Home) ctx.get(AccountHome.class);
                    account.setVpnMSISDN(subscriber.getMSISDN());
                    actHome.store(ctx, account);
                }
            }
        }
        catch (Exception e)
        {
            association.setProvisionActionState(false);
            String msg = "Error setting VPN MSISDN of root account for account" + subscriberAccount.getBAN() + " to "
                    + subscriber.getMSISDN();
            throw new ExtensionAssociationException(ExternalAppEnum.BSS, msg + e.getMessage(), msg, ExternalAppSupport.BSS_DATABASE_FAILURE_ACCOUNT_UPDATE, e);
        }
    }

    private Subscriber getSubscriber(Context ctx, SubscriberAuxiliaryService subAuxSvc) throws ExtensionAssociationException
    {
        try
        {
            Subscriber subscriber = SubscriberSupport.getSubscriberForAuxiliaryService(ctx, subAuxSvc);
            if (subscriber == null)
            {
                throw new ExtensionAssociationException(ExternalAppEnum.BSS, "Subscription not found. Identifier = " + subAuxSvc.getSubscriberIdentifier(), ExternalAppSupport.BSS_DATABASE_FAILURE_SUBSCRIPTION_RETRIEVAL);
            }
            return subscriber;
        }
        catch (HomeException e)
        {
            throw new ExtensionAssociationException(ExternalAppEnum.BSS, "Unable to retrieve subscription "
                    + subAuxSvc.getSubscriberIdentifier() + ": " + e.getMessage(), ExternalAppSupport.BSS_DATABASE_FAILURE_SUBSCRIPTION_RETRIEVAL, e, false);
        }
    }
    
    private Account getVPNAccount()
    {
        return vpnAccount_;
    }

    /**
     * Result code for error.
     */
    private static final int ERROR_RESULT = -2;
    
    private Account vpnAccount_ = null;
}
