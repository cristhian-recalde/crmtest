/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee, no
 * unauthorised use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the licence agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 *
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.vpn;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.DebugLogMsg;

import com.trilogy.app.crm.bean.AbstractAccount;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryServiceHome;
import com.trilogy.app.crm.bean.VpnAuxiliarySubscriber;
import com.trilogy.app.crm.bean.core.custom.AuxiliaryService;
import com.trilogy.app.crm.home.sub.StateChangeAuxiliaryServiceSupport;
import com.trilogy.app.crm.subscriber.charge.CrmCharger;
import com.trilogy.app.crm.subscriber.charge.SubscriberAuxiliaryServiceCharger;
import com.trilogy.app.crm.subscriber.charge.handler.LoggingHandler;
import com.trilogy.app.crm.support.AuxiliaryServiceSupport;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
import com.trilogy.app.crm.support.SubscriberSupport;


/**
 * Unprovision the VPN services and from VPN
 *
 * @author arturo.medina@redknee.com
 */
public class UnprovisionVPNVisitor implements Visitor
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;


    /**
     * When instantiating a ProvisionVPNServiceVisitor by default the system assumes it
     * souldn't delete the subscriber service
     *
     * @param account
     *            Account with the VPN service.
     */
    public UnprovisionVPNVisitor(final Account account)
    {
        setAccount(account);
    }


    /**
     * {@inheritDoc}
     */
    public void visit(final Context ctx, final Object obj) throws AgentException, AbortVisitException
    {
        final VpnAuxiliarySubscriber vpnSub = (VpnAuxiliarySubscriber) obj;
        final Home subAuxSvcHome = (Home) ctx.get(SubscriberAuxiliaryServiceHome.class);
        final String subId = vpnSub.getSubscriberId();
        try
        {
            final Subscriber sub = SubscriberSupport.lookupSubscriberForSubId(ctx, subId);
            final AuxiliaryService auxSvc = AuxiliaryServiceSupport.getAuxiliaryService(ctx,
                    vpnSub.getAuxiliaryServiceId());
            final SubscriberAuxiliaryService subAuxSvc = SubscriberAuxiliaryServiceSupport
                    .getSubscriberAuxiliaryService(ctx, vpnSub.getSubcriberAuxiliaryId());

            if (subAuxSvc != null)
            {
                StateChangeAuxiliaryServiceSupport.unProvisionHlr(ctx, subAuxSvc, auxSvc, sub,this);
                CrmCharger  charger = new SubscriberAuxiliaryServiceCharger(sub, subAuxSvc);

                // Manda - Handling update SMSB with VPN Service Grade
                SubscriberAuxiliaryServiceSupport.updateSmsbSvcGradeforVPNService(ctx, sub, false);

                charger.refund(ctx, new LoggingHandler()); 

                subAuxSvcHome.remove(ctx, subAuxSvc);
            }
        }
        catch (final Exception e)
        {
            new DebugLogMsg(this,
                "CaughtException in the visitor to unProvision responsible subscriber with vpn service  under account"
                    + account_.getBAN(), e).log(ctx);
        }
    }


    /**
     * @return Returns the account.
     */
    public AbstractAccount getAccount()
    {
        return account_;
    }


    /**
     * @param account
     *            The account to set.
     */
    public void setAccount(final AbstractAccount account)
    {
        account_ = account;
    }

    private AbstractAccount account_;
}
