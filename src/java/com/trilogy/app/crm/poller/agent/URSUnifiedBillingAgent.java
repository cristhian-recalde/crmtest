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
package com.trilogy.app.crm.poller.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.PollerProcessor;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.account.SubscriptionTypeEnum;
import com.trilogy.app.crm.bean.calldetail.CallDetail;
import com.trilogy.app.crm.bean.calldetail.CallDetailHome;
import com.trilogy.app.crm.bean.calldetail.RerateCallDetail;
import com.trilogy.app.crm.bean.calldetail.RerateCallDetailHome;
import com.trilogy.app.crm.bean.calldetail.RerateCallDetailXInfo;
import com.trilogy.app.crm.bean.core.SubscriptionType;
import com.trilogy.app.crm.poller.CallDetailCreator;
import com.trilogy.app.crm.poller.Constants;
import com.trilogy.app.crm.poller.ProcessorInfo;
import com.trilogy.app.crm.poller.URSCallDetailCreator;
import com.trilogy.app.crm.poller.event.CRMProcessor;
import com.trilogy.app.crm.poller.event.URSUnifiedBillingProcessor;
import com.trilogy.app.crm.support.LicensingSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.PMLogMsg;

/**
 * Receives an ER 501 and stores it in the proper Call Detail Home.
 *
 * @author prasanna.kulkarni@redknee.com
 */
public class URSUnifiedBillingAgent implements ContextAgent, Constants
{
    public URSUnifiedBillingAgent(final CRMProcessor processor)
    {
		this(processor, false);
    }
    
    public URSUnifiedBillingAgent(final CRMProcessor processor, boolean isRoamingUrsProcessor)
    {
        super();
        processor_ = processor;
        creator_ = new URSCallDetailCreator(isRoamingUrsProcessor);
    }


    /**
     * {@inheritDoc}
     */
    public void execute(final Context ctx) throws AgentException
    {
        if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, this, "starting to execute");
        }
        final List params = new ArrayList();
        final ProcessorInfo info = (ProcessorInfo) ctx.get(ProcessorInfo.class);

        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "execute()");

        try
        {
            final Home tHome = (Home) ctx.get(CallDetailHome.class);
            final Home mtHistoryHome = (Home) ctx.get(Common.MT_CALL_DETAIL_HOME);

            if (tHome == null || mtHistoryHome == null)
            {
                //FIXED(traceability): severity should be major instead of minor
                // because functionality is crippled.
                // changed to major PS
                throw new AgentException("CallDetailHome not found in context");
            }

            final CallDetail t = creator_.createCallDetail(ctx, info, params);
            PollerProcessor cfg = (PollerProcessor) ctx.get(PollerProcessor.class);
            
            
            if (cfg!=null)
            {
                if (cfg.isChargeToOCG())
                {
                    t.setChargeOCG(true);
                }
            }
            
            if (t != null)
            {

                if (isPrepaidCallDetail(ctx,t))
                {
                    Home prepaidCallingCardHome = (Home) ctx.get(com.redknee.app.crm.Common.PREPAID_CALLING_CARD_CALL_DETAIL_HOME);
                    prepaidCallingCardHome.create(ctx,t);
                }
                else
                {
                    // Reratign should not be done for Prepaid calling card. confirmed with PLM
                    if (t.isRerated())
                    {
                        markReratingCDReceived(ctx, t);
                    }
                    
                    if (isMtCallDetail(ctx, t))
                    {
                        mtHistoryHome.create(ctx, t);
                    }
                    else
                    {
                        tHome.create(ctx, t);
                    }
                }
            }
        }
        catch (final Throwable t)
        {
            LogSupport.minor(ctx, this, "Failed to process ER 501 because of Exception " + t.getMessage(), t);
            processor_.saveErrorRecord(ctx, info.getRecord());
        }
        finally
        {
            pmLogMsg.log(ctx);
            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, this, "finished to execute");
            }
            CRMProcessor.playNice(ctx, CRMProcessor.HIGH_ER_THROTTLING);
        }
    }

    /**
     * Finds the corresponding Reraiting Call Detail and marks it as received.
     *
     * @param ctx The operating Context
     * @param cdr The Call Detail bean
     * @throws HomeException
     */
    private void markReratingCDReceived(final Context ctx, final CallDetail cdr)
        throws HomeException
    {
        // Match ER against extracted ER in rerate call detail table
        final And condition = new And();
        condition.add(new EQ(RerateCallDetailXInfo.SPID, Integer.valueOf(cdr.getSpid())));
        condition.add(new EQ(RerateCallDetailXInfo.CALL_TYPE, cdr.getCallType()));
        condition.add(new EQ(RerateCallDetailXInfo.TRAN_DATE, cdr.getTranDate()));
        condition.add(new EQ(RerateCallDetailXInfo.ORIG_MSISDN, cdr.getOrigMSISDN()));
        condition.add(new EQ(RerateCallDetailXInfo.DEST_MSISDN, cdr.getDestMSISDN()));

        final Home rcdHome = (Home) ctx.get(RerateCallDetailHome.class);

        final RerateCallDetail rcd = (RerateCallDetail) rcdHome.find(ctx, condition);

        if (rcd == null)
        {
            // If no match found do something
            // No behaviour was defined so I'm just logging it
            final String msg = "Unexpected error: Recieved rerated ER with no matching extracted call detail.";
            LogSupport.major(ctx, this, msg, new HomeException(msg));
        }
        else
        {
            rcd.setReceived(true);
            rcdHome.store(ctx, rcd);
        }
    }

    /**
     * Checks if hte call detail is  Prepaid Calling Card Subscription type.
     *
     * @param ctx The Operating Context
     * @param cdr Call Detail to check.
     * @return true if it is a MT Call Detail
     * @throws HomeException
     */
    public boolean isPrepaidCallDetail(final Context ctx, final CallDetail cdr) throws HomeException
    {
        if (LicensingSupportHelper.get(ctx).isLicensed(ctx, LicenseConstants.PREPAID_CALLING_CARD_LICENSE_KEY))
        {
            Subscriber sub = SubscriberSupport.getSubscriber(ctx, cdr.getSubscriberID());
            if(null == sub)
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(
                            this,
                            "CDR for MSISDN ["
                                    + cdr.getChargedMSISDN()
                                    + "] is determined not to be of a prepaid calling card because subsriber could not be determined from CDR.",
                            null).log(ctx);
                }
                return false;
            }
            long subTypeId = sub.getSubscriptionType();
            SubscriptionTypeEnum cdrSubscriptionType = SubscriptionType.getSubscriptionTypeEnum(ctx,subTypeId );
            if(cdrSubscriptionType == null)
            {
                throw new HomeException(
                        "Subscription ID could not determined for the CDR - MSISDN ["
                                + cdr.getChargedMSISDN()
                                + "], Subscriber ["
                                + cdr.getSubscriberID()
                                + "] and Subscription-Type-ID["
                                + subTypeId
                                + "]. Please check the compare the state of Subscriber and CDR with Subscription-Type-Configuration");
            }
            if (SubscriptionTypeEnum.PREPAID_CALLING_CARD.equals(cdrSubscriptionType))
            {
                return true;
            }
        }
        return false;
    }
    
    
    /**
     * Check if this CDR satisfies the conditions of the MT Call Detail.
     *
     * @param ctx The Operating Context
     * @param cdr Call Detail to check.
     * @return true if it is a MT Call Detail
     * @throws HomeException
     */
    public boolean isMtCallDetail(final Context ctx, final CallDetail cdr) throws HomeException
    {
        boolean result = false;
        if (LicensingSupportHelper.get(ctx).isLicensed(ctx, LicenseConstants.MT_CALL_HISTORY_LICENSE_KEY))
        {
            final Home spidHome = (Home) ctx.get(CRMSpidHome.class);
            if (spidHome == null)
            {
                throw new HomeException("System Error: SPID home not available!");
            }

            final CRMSpid spid = (CRMSpid) spidHome.find(ctx, Integer.valueOf(cdr.getSpid()));
            if (spid == null)
            {
                throw new HomeException("SPID " + cdr.getSpid() + " not found!");
            }

            if (cdr.getCallType() == spid.getMtCallType())
            {
                final StringTokenizer st = new StringTokenizer(spid.getMtBillOptionIgnore(), ",");
                boolean found = false;
                while (st.hasMoreTokens())
                {
                    if (cdr.getBillingOption().equals(st.nextToken()))
                    {
                        result = false;
                        found = true;
                        break;
                    }
                }

                if (!found)
                {
                    result = true;
                }
            }
        }

        return result;
    }

    private CRMProcessor processor_ = null;

    private CallDetailCreator creator_ = null;

    private static final String PM_MODULE = URSUnifiedBillingProcessor.class.getName();
}
