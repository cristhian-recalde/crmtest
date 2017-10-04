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
package com.trilogy.app.crm.checking;

import java.util.Collection;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.PricePlanVersion;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServiceFee2ID;
import com.trilogy.app.crm.bean.ServiceHome;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.client.AppEcpClient;
import com.trilogy.app.crm.client.AppEcpClientSupport;
import com.trilogy.app.crm.config.AppEcpClientConfig;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.osa.ecp.provision.SubsProfile;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.OMLogMsg;


/**
 * ECP synchronization check.
 * For use by the DIVA tool.
 *
 * @author larry.xia@redknee.com
 */
public class ECPCheckFixer extends AbstractIntegrityValidation
{

    /**
     * {@inheritDoc}
     */
    public void printResults()
    {
        print("TOTAL: " + this.totalCount_);
        if (isRepairEnabled())
        {
            print("error:" + this.errorCount_);
            print("fixed: " + this.changeCount_);
            print("failed: " + this.failCount_);
            print("no voice Service or service stopped:" + this.noServiceCount_);
            print("No need fix:" + this.noChangeCount_);
        }
        else
        {
            print("Error: " + this.errorCount_);
            print("no Service or service stopped: " + this.noServiceCount_);
            // print("No Error: " + noChangeCount_);
        }
        if (this.notExistOnEcpCount_ > 0)
        {
            print("Subscriber not exist on ECP: " + this.notExistOnEcpCount_);
        }
        if (this.ghostCount_ > 0)
        {
            print("Ghost profile on ECP: " + this.ghostCount_);

        }
    }


    /**
     * {@inheritDoc}
     */
    public void process(final Context ctx, final Subscriber sub)
    {
        ++this.totalCount_;

        boolean ownsMsisdn = true;
        try
        {
            ownsMsisdn = SubscriberSupport.ownsMSISDN(ctx, sub);
        }
        catch (final HomeException e)
        {
            ownsMsisdn = false;
        }

        if (sub != null && ownsMsisdn)
        {
            final Home acctHome = (Home) ctx.get(AccountHome.class);

            final AppEcpClient ecpClient = (AppEcpClient) ctx.get(AppEcpClient.class);

            // get account
            Account account = null;
            if (!SubscriberStateEnum.INACTIVE.equals(sub.getState()))
            {
                try
                {
                    account = (Account) acctHome.find(ctx, sub.getBAN());
                }
                catch (final HomeException e)
                {
                    print(" Account " + sub.getBAN() + " not found");

                }
            }

            final SubsProfile ecpSub;
            try
            {
                ecpSub = ecpClient.getSubsProfile(sub.getMSISDN());
            }
            catch (final IllegalStateException exception)
            {
                final StringBuilder sb = new StringBuilder();
                sb.append(exception.getClass().getSimpleName());
                sb.append(" caught in ");
                sb.append("ECPCheckFixer.process(): ");
                if (exception.getMessage() != null)
                {
                    sb.append(exception.getMessage());
                }
                print(sb.toString());
                this.noServiceCount_++;
                return;
            }

            if (account != null && !sub.getState().equals(SubscriberStateEnum.INACTIVE) && hasVoice(ctx, sub))
            {

                boolean result = false;
                if (ecpSub != null)
                {
                    result = subCompareWithEcp(ctx, sub, ecpSub, account, getMessageHandler());
                    if (!result)
                    {
                        ++this.errorCount_;
                    }
                    else
                    {
                        ++this.noChangeCount_;
                    }
                }
                else
                {
                    final StringBuilder sb = new StringBuilder();
                    sb.append("Subscriber ");
                    sb.append(sub.getMSISDN());
                    sb.append(" doesn't exist on ECP subscriber state is ");
                    sb.append(sub.getState().getDescription());
                    sb.append(" account state is ");
                    sb.append(account.getState().getDescription());
                    print(sb.toString());
                    ++this.notExistOnEcpCount_;
                }
                if (!result)
                {
                    if (isRepairEnabled())
                    {
                        if (subSynchWithEcp(ctx, sub, ecpSub, account, ecpClient, getMessageHandler()))
                        {
                            ++this.changeCount_;
                        }
                        else
                        {
                            ++this.failCount_;
                        }
                    }
                }

            }
            else
            {
                if (ecpSub != null)
                {
                    ++this.ghostCount_;

                    if (account != null)
                    {
                        // basically if the account is null, don't print to file
                        final StringBuilder sb = new StringBuilder();
                        sb.append("Subscriber ");
                        sb.append(sub.getMSISDN());
                        sb.append(" is a ghost profile on ECP subscriber state is ");
                        sb.append(sub.getState().getDescription());
                        sb.append(" account state is ");
                        sb.append(account.getState().getDescription());
                        sb.append(" voice service ");
                        sb.append(hasVoice(ctx, sub));
                        print(sb.toString());
                    }

                }
                else
                {

                    if (account == null)
                    {
                        account = new Account();
                        account.setState(AccountStateEnum.INACTIVE);
                    }

                    final StringBuilder sb = new StringBuilder();
                    sb.append("Subscriber ");
                    sb.append(sub.getMSISDN());
                    sb.append(" doesn't exist on ECP subscriber state is ");
                    sb.append(sub.getState().getDescription());
                    sb.append(" account state is ");
                    sb.append(account.getState().getDescription());
                    sb.append(" voice service ");
                    sb.append(hasVoice(ctx, sub));
                    print(sb.toString());
                    ++this.noServiceCount_;
                }

            }

        }
        else if (sub != null)
        {
            // ignore if the subscriber no longer own this msisdn
            print("Subscriber " + sub.getMSISDN() + "no longer owns MSISDN" + sub.getMSISDN());
            ++this.noServiceCount_;
        }
        else
        {
            print("Subscriber is null");
            ++this.noServiceCount_;
        }
    }


    /**
     * Verifies if the subscriber in CRM is synchronized with subscriber in ECP.
     *
     * @param ctx
     *            The operating context.
     * @param crmSub
     *            CRM subscriber.
     * @param ecpSub
     *            ECP subscriber.
     * @param account
     *            Account owning the subscriber.
     * @param messager
     *            Message handler.
     * @return Returns <code>true</code> if the subscriber is synchronized,
     *         <code>false</code> otherwise.
     */
    public boolean subCompareWithEcp(final Context ctx, final Subscriber crmSub, final SubsProfile ecpSub,
        final Account account, final MessageHandler messager)
    {

        final String defaultMsg = "subscriber MSISDN = " + crmSub.getMSISDN();
        String msg = defaultMsg;
        boolean ret = true;

        //As of CRM 8.2, rate plan information lives in Price Plan and doesn't have to be checked per Subscriber.
        AppEcpClientConfig config;

        config = (AppEcpClientConfig) ctx.get(AppEcpClientConfig.class);
        if (config == null)
        {
            messager.print("System error: AppEcpClientConfig not found in context");
            return false;
        }

        // Determine the ECP Class of Service for this subscriber.
        int classOfService = 0;
        try
        {
            classOfService = config.getClassOfService(ctx, account.getSpid(), account.getType(), crmSub
                .getSubscriberType());
        }
        catch (final HomeException e)
        {
            messager.print("System error: can not find class of service in ECP configuration");
            return false;
        }

        if (account.getSpid() != ecpSub.spid)
        {
            msg = msg.concat(" spid (CRM) " + account.getSpid() + " (ECP) " + ecpSub.spid);
            ret = false;
        }
        if (!account.getCurrency().equals(ecpSub.currencyType))
        {
            msg = msg.concat(" currency (CRM) " + account.getCurrency() + " (ECP) " + ecpSub.currencyType);
            ret = false;
        }
        if (config.getExpiry() != ecpSub.expiry)
        {
            msg = msg.concat(" expiry (CRM) " + config.getExpiry() + "  (ECP) " + ecpSub.spid);
            ret = false;
        }
        if (classOfService != ecpSub.classOfService)
        {
            msg = msg.concat(" class_of_service (CRM) " + classOfService + "  (ECP) " + ecpSub.classOfService);
            ret = false;
        }

        if (AppEcpClientSupport.mapToEcpState(ctx, crmSub) != ecpSub.state)
        {
            msg = msg.concat(" state (CRM) " + crmSub.getState().getIndex() + "  (ECP) " + ecpSub.state);
            ret = false;
        }

        if (!config.getPin().equals(ecpSub.pin))
        {
            msg = msg.concat(" pin (CRM) " + config.getPin() + " (ECP) " + ecpSub.pin);
            ret = false;
        }
        if (config.getLanguage() != ecpSub.language)
        {
            msg = msg.concat(" Language (CRM) " + config.getLanguage() + " (ECP) " + ecpSub.language);
            ret = false;
        }
        if (!config.getTimeRegionId().equals(ecpSub.timeRegionID))
        {
            msg = msg.concat(" TImeRegionID (CRM) " + config.getTimeRegionId() + " (ECP) " + ecpSub.timeRegionID);
            ret = false;
        }
        if (!crmSub.getGroupMSISDN(ctx).equals(ecpSub.groupAccount)
                || ("".equals(crmSub.getGroupMSISDN(ctx))
                        && crmSub.getMSISDN().equals(ecpSub.groupAccount)))
        {
            msg = msg.concat(" Group MSISDN (CRM) " + crmSub.getGroupMSISDN(ctx) + "(ECP) " + ecpSub.groupAccount);
            ret = false;
        }
        if (msg.equals(defaultMsg))
        {
            msg = msg.concat(" ECP profile is OK");
        }
        messager.print(msg);
        return ret;
    }


    /**
     * Synchronize CRM and ECP subscriber.
     *
     * @param ctx
     *            The operating context.
     * @param crmSub
     *            CRM subscriber.
     * @param ecpSub
     *            ECP subscriber.
     * @param account
     *            account owning the subscriber.
     * @param ecpClient
     *            ECP client.
     * @param messager
     *            Message handler.
     * @return Whether the sychronization was successful.
     */
    public boolean subSynchWithEcp(final Context ctx, final Subscriber crmSub, final SubsProfile ecpSub,
        final Account account, final AppEcpClient ecpClient, final MessageHandler messager)
    {

        AppEcpClientConfig config = null;
        CRMSpid spid = null;
        int result;

        config = (AppEcpClientConfig) ctx.get(AppEcpClientConfig.class);
        if (config == null)
        {
            print("System error: AppEcpClientConfig not found in context");
            return false;
        }

        // Determine the ECP Class of Service for this subscriber.
        int classOfService = 0;
        try
        {
            classOfService = config.getClassOfService(ctx, account.getSpid(), account.getType(), crmSub
                .getSubscriberType());
            spid = SpidSupport.getCRMSpid(ctx, crmSub.getSpid());
        }
        catch (final HomeException e)
        {
            print("System error: can not find class of service in ECP configuration");
            return false;
        }
        
        String groupMSISDN = crmSub.getGroupMSISDN(ctx);
        if( "".equals(groupMSISDN) )
        {
            // This check is required because as of CRM 7.5, the group MSISDN field in CRM and ABM is blank
            // for non-pooled subscribers.  Previous versions provisioned ECP with group MSISDN = MSISDN.
            groupMSISDN = crmSub.getMSISDN();
        }

        if (ecpSub == null)
        {
            final int ecpState;
            if (AppEcpClientSupport.mapToEcpState(ctx, crmSub) != -1)
            {
                ecpState = AppEcpClientSupport.mapToEcpState(ctx, crmSub);
            }
            else
            {
                ecpState = AppEcpClient.AVAILABLE;
            }
            result = ecpClient.addSubscriber(
                crmSub.getMSISDN(),
                account.getSpid(),
                crmSub.getIMSI(),
                account.getCurrency(),
                0, //As of CRM 8.2, Rate Plan information is in Price Plan and it is ignored when pushed to the subscriber through this interface
                config.getExpiry(),
                classOfService,
                ecpState,
                config.getPin(),
                config.getLanguage(),
                spid.getTimezone(),
                groupMSISDN);
        }
        else
        {
            final int ecpState;
            if (AppEcpClientSupport.mapToEcpState(ctx, crmSub) != -1)
            {
                ecpState = AppEcpClientSupport.mapToEcpState(ctx, crmSub);
            }
            else
            {
                ecpState = AppEcpClient.AVAILABLE;
            }
            result = ecpClient.updateSubscriber(
                crmSub.getMSISDN(),
                account.getSpid(),
                crmSub.getIMSI(),
                account.getCurrency(),
                0, //As of CRM 8.2, Rate Plan information is in Price Plan and it is ignored when pushed to the subscriber through this interface
                config.getExpiry(),
                classOfService,
                ecpState,
                config.getPin(),
                config.getLanguage(),
                spid.getTimezone(),
                groupMSISDN);
        }

        if (result != 0)
        {
            new OMLogMsg(Common.OM_MODULE, Common.OM_ECP_ERROR).log(ctx);
            print("ECP synchronization failed: " + result);
            return false;
        }

        return true;
    }


    /**
     * Checks if a subscriber has the voice service enabled.
     *
     * @param ctx
     *            The operating context.
     * @param sub
     *            The subscriber being verified.
     * @return Returns <code>true</code> if the subscriber has voice service,
     *         <code>false</code> otherwise.
     */
    public boolean hasVoice(final Context ctx, final Subscriber sub)
    {
        final Home serviceHome = (Home) ctx.get(ServiceHome.class);
        final Collection<ServiceFee2ID> services = sub.getServices();
        if (services != null)
        {
            for (final ServiceFee2ID serviceFee2ID : services)
            {
                try
                {
                    if (serviceFee2ID != null)
                    {
                        final Service service = (Service) serviceHome.find(ctx, serviceFee2ID.getServiceId());
                        if (service != null && service.getType() == ServiceTypeEnum.VOICE)
                        {
                            return true;
                        }
                    }
                }
                catch (final HomeException e)
                {
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        new DebugLogMsg(this, e.getMessage(), e).log(ctx);
                    }
                }

            }
        }

        return false;
    }

    private int totalCount_ = 0;
    private int errorCount_ = 0;
    private int noChangeCount_ = 0;
    private int notExistOnEcpCount_ = 0;
    private int changeCount_ = 0;
    private int failCount_ = 0;
    private int ghostCount_ = 0;
    private int noServiceCount_ = 0;
}
