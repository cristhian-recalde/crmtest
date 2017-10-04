// INSPECTED: 24/09/2003 GEA
// REVIEW(codestyle): Convert tabs to spaces.
package com.trilogy.app.crm.provision;

import java.text.SimpleDateFormat;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.BillCycleHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.core.BillCycle;
import com.trilogy.app.crm.bean.core.Service;
import com.trilogy.app.crm.bean.externalapp.ExternalAppEnum;
import com.trilogy.app.crm.client.smsb.AppSmsbClient;
import com.trilogy.app.crm.client.smsb.AppSmsbClientSupport;
import com.trilogy.app.crm.config.AppSmsbClientConfig;
import com.trilogy.app.crm.support.ExternalAppSupportHelper;
import com.trilogy.app.crm.technology.TechnologyEnum;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.EntryLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.OMLogMsg;

public class SmsProvisionAgent extends CommonProvisionAgent
{
    public final static short DEFAULT_BILLCYCLEDATE = 1;
    public final static String DEFAULT_EQTYPE = "G";
    public final static String DEFAULT_LANGUAGE = "E";

    /**
     * Installs SMS services to HLR and AppSmsb
     * Context must contain the subscriber to be installed keyed by Subscriber.class
     * Context must contain Service to retrieve additional params needed associated with this service
     * Context must contain AppSmsbClient to provision AppSmsb using CORBA
     * Context must contain Account of the subscriber
     *
     * @param ctx
     */
    public void execute(Context ctx)
            throws AgentException
    {
        AppSmsbClient appSmsbClient;
        Subscriber subscriber;
        Service service;
        Account account;
        String hlrCmds = null;
        AppSmsbClientConfig config;
        int result;

        appSmsbClient = (AppSmsbClient) ctx.get(AppSmsbClient.class);
        if (appSmsbClient == null)
        {
            throw new AgentException("System error: AppSmsbClient not found in context");
        }

        subscriber = (Subscriber) ctx.get(Subscriber.class);
        if (subscriber == null)
        {
            throw new AgentException("System error: No subscriber to provision");
        }
        
        
        account = (Account) ctx.get(Account.class);
        if (account == null)
        {
            throw new AgentException("System error: subscriber's account not found");
        }

        service = (Service) ctx.get(Service.class);
        if (service == null)
        {
            throw new AgentException("System error: Service for SMS provisioning not found in context");
        }

        config = (AppSmsbClientConfig) ctx.get(AppSmsbClientConfig.class);
        if (config == null)
        {
            throw new AgentException("System error: AppSmsbClientConfig not found in context");
        }

        // look for the SogClient for the subscriber's HLR ID
        short hlrId = subscriber.getHlrId();

        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, "Provisioning SMS Services for " + subscriber.getMSISDN(), null).log(ctx);
        }

        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, "Provisioning service " + service.getName(), null).log(ctx);
        }

        // Set BillCycleDate
        short billCycleDate = DEFAULT_BILLCYCLEDATE;
        {
            Home billCycleHome = (Home) ctx.get(BillCycleHome.class);

            try
            {
                BillCycle billCycle = (BillCycle) billCycleHome.find(ctx, Integer.valueOf(account.getBillCycleID()));

                if (billCycle != null)
                {
                    billCycleDate = (short) billCycle.getDayOfMonth();
                }
            }
            catch (HomeException e)
            {
                // ignore, just use default billcycledate then
            }
        }

        // Set Equiment Type
        String eqType = DEFAULT_EQTYPE;
        eqType = getSmsbEqType(subscriber);

        // add subscriber to SMSB
        Boolean State = AppSmsbClientSupport.mapSmsbState(ctx,subscriber);
        boolean smsbState = (State == null ? false : State.booleanValue());
        short servId = (short) service.getIdentifier();
        String groupMSISDN = subscriber.getGroupMSISDN(ctx);
        if( "".equals(groupMSISDN) )
        {
            // This check is required because as of CRM 7.5, the group MSISDN field in CRM and ABM is blank
            // for non-pooled subscribers.  Previous versions provisioned SMSB with group MSISDN = MSISDN.
            groupMSISDN = subscriber.getMSISDN();
        }
        result = appSmsbClient.addSubscriber(
                subscriber.getMSISDN(),
                groupMSISDN,
                subscriber.getIMSI(),
                (short) account.getSpid(),
                servId, // Fxix for TT # 6102840817 
                //config.getSvcId(),
                //config.getSvcGrade(subscriber.getSubscriberType()),
                config.getSvcGradeWithVpnCheck(ctx, account, subscriber),
                //Manda - added this new method call to check for vpn
                subscriber.getBAN(),

                // TODO 2008-08-22 date of birthday no longer part of Subscriber
                (account.getDateOfBirth() != null) ? new SimpleDateFormat(
                        AppSmsbClient.SIMPLE_DATE_FORMAT_STRING).format(account.getDateOfBirth()) : "", // Birthdate
               // "", // Birthdate
                "", // Gender
                DEFAULT_LANGUAGE, // Language
                "", // Location
                billCycleDate, // BillCycleDate
                eqType, // EqType
                config.getTzOffset(),
                (short) 0, //As of CRM 8.2, Rate Plan information is in Price Plan and it is ignored when pushed to the subscriber through this interface
                config.getRecurDate(),
                config.getScpId(),
                subscriber.getHlrId(),
                smsbState,
                config.getBarringPlan(),
                "0",
                ""); // incomingSmsCount

        if (result != 0)
        {
            new OMLogMsg(Common.OM_MODULE, Common.OM_SMSB_ERROR).log(ctx);
            new EntryLogMsg(10360, this, "", subscriber.toString(), new java.lang.String[]{String.valueOf(result)},
                    null).log(ctx);
            throw new ProvisionAgentException(ctx, ExternalAppSupportHelper.get(ctx).getProvisionErrorMessage(ctx,
                    ExternalAppEnum.SMS, result, service), result, ExternalAppEnum.SMS);
        }
//        state
//        AppSmsbClientSupport.updateSmsbSubscriberState(ctx, subscriber);
//        if (result != 0)
//        {
//            new OMLogMsg(Common.OM_MODULE, Common.OM_SMSB_ERROR).log(ctx);
//            new EntryLogMsg(10360, this, "", subscriber.toString(), new java.lang.String[]{String.valueOf(result)},
//                    null).log(ctx);
//            throw new ProvisionAgentException("SMSB provisioning update state failed", result, 3007,
//                    ProvisionAgentException.SMSB);
//        }

        //hlr
        if (subscriber.getSubscriberType() == SubscriberTypeEnum.POSTPAID)
        {

            hlrCmds = service.getProvisionConfigs();
        }
        else if (subscriber.getSubscriberType() == SubscriberTypeEnum.PREPAID)
        {
            hlrCmds = service.getPrepaidProvisionConfigs();
        }

        if ((hlrCmds == null) || (hlrCmds.length() == 0))
        {
            // no HLR commands configured means nothing to do
            return;
        }

        // execute HLR commands using HLR client
        callHlr(ctx, true,subscriber,service,null,null);
    }

    /**
     * @param sub
     * @return
     */
    private String getSmsbEqType(Subscriber sub)
    {
        String eqType;
        if (sub.getTechnology().equals(TechnologyEnum.GSM))
        {
            eqType = "G";
        }
        else if (sub.getTechnology().equals(TechnologyEnum.CDMA))
        {
            eqType = "C";
        }
        else
        {
            eqType = "T";
        }
        return eqType;
    }
}
