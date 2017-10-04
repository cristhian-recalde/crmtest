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
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.DebugLogMsg;

import com.trilogy.app.crm.bean.AbstractAccount;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.VpnAuxiliarySubscriber;
import com.trilogy.app.crm.bean.core.custom.AuxiliaryService;
import com.trilogy.app.crm.home.sub.StateChangeAuxiliaryServiceSupport;
import com.trilogy.app.crm.subscriber.charge.CrmCharger;
import com.trilogy.app.crm.subscriber.charge.SubscriberAuxiliaryServiceCharger;
import com.trilogy.app.crm.subscriber.charge.handler.LoggingHandler;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.AuxiliaryServiceSupport;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
import com.trilogy.app.crm.support.SubscriberSupport;


/**
 * Unprovisions the auxiliary service for every non-responsible subscriber on the visitor
 *
 * @author arturo.medina@redknee.com
 */
public class ProvisionVPNServiceVisitor implements Visitor
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
     *            Account of the VPN service.
     * @param provision
     *            Whether to provision or unprovision.
     */
    public ProvisionVPNServiceVisitor(final Account account, final boolean provision)
    {
        setAccount(account);
        setProvision(provision);
    }


    /**
     * {@inheritDoc}
     */
    public void visit(final Context ctx, final Object obj) throws AgentException, AbortVisitException
    {
        final VpnAuxiliarySubscriber vpnSub = (VpnAuxiliarySubscriber) obj;
        final String subId = vpnSub.getSubscriberId();
        try
        {
            final Subscriber sub = SubscriberSupport.lookupSubscriberForSubId(ctx, subId);
            final Account acct = AccountSupport.getAccount(ctx, sub.getBAN());
            if (acct.getBAN().equals(account_.getBAN()))
            {
                if (acct.isResponsible())
                {
                    final AuxiliaryService auxSvc = AuxiliaryServiceSupport.getAuxiliaryService(ctx,
                            vpnSub.getAuxiliaryServiceId());
                    final SubscriberAuxiliaryService subAuxSvc = SubscriberAuxiliaryServiceSupport
                        .getSubscriberAuxiliaryService(ctx, vpnSub.getSubcriberAuxiliaryId());

	                CrmCharger  charger = new SubscriberAuxiliaryServiceCharger(sub,  subAuxSvc);

                    if (provision())
                    {
                        StateChangeAuxiliaryServiceSupport.provisionHlr(ctx, subAuxSvc, auxSvc, sub, this);

                        // Manda - Handling update SMSB with VPN Service Grade
                        SubscriberAuxiliaryServiceSupport.updateSmsbSvcGradeforVPNService(ctx, sub, true);

	                	charger.charge(ctx, new LoggingHandler()); 
                    }
                    else
                    {
                        StateChangeAuxiliaryServiceSupport.unProvisionHlr(ctx, subAuxSvc, auxSvc, sub,this);

                        // Manda - Handling update SMSB with VPN Service Grade
                        SubscriberAuxiliaryServiceSupport.updateSmsbSvcGradeforVPNService(ctx, sub, false);

                        // need figure why charge is called here, fix me
	                	charger.refund(ctx, new LoggingHandler());
                    }

                }

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


    /**
     * @return Returns the account.
     */
    public boolean provision()
    {
        return provision_;
    }


    /**
     * @param account
     *            The account to set.
     */
    public void setProvision(final boolean provision)
    {
        provision_ = provision;
    }

    private AbstractAccount account_;
    private boolean provision_;
}
