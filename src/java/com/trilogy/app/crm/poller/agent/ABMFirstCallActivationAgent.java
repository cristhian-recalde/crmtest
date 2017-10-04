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

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.home.sub.SubscriberHomeFactory;
import com.trilogy.app.crm.poller.FilterOutException;
import com.trilogy.app.crm.poller.ProcessorInfo;
import com.trilogy.app.crm.poller.event.ABMProcessor;
import com.trilogy.app.crm.poller.event.CRMProcessor;
import com.trilogy.app.crm.poller.event.CRMProcessorSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.util.SimpleLocks;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.PMLogMsg;
import com.trilogy.product.s2100.oasis.PrepaidAccountService;
import com.trilogy.service.corba.CorbaClientProperty;
import com.trilogy.service.proxy.ProxyInfo;
import com.trilogy.service.proxy.ProxyInfoHome;
import com.trilogy.service.proxy.share.MultiplexerConfiguration;
import com.trilogy.util.snippet.log.Logger;

/**
 * Agent that polls the ER453 Activation ER from ABM which activates the subscribers.
 *
 * @author ravi.patel@redknee.com
 */
public class ABMFirstCallActivationAgent extends AbstractActivationAgent implements ContextAgent
{
    private CRMProcessor processor_ = null;

    protected static final int INDEX_USERID = 3;
    protected static final int INDEX_MSISDN = 4;
    protected static final int INDEX_ER_REFERENCE = 6;
    protected static final int INDEX_ACTIVATION_DATE = 7;
    protected static final int INDEX_EXPIRY_DATE = 8;
    protected static final int INDEX_ACTIVATION_TIME = 10;
    protected static final int INDEX_ACTIVATION_TIME_ZONE = 11;

    private static final String PM_MODULE = ABMFirstCallActivationAgent.class.getName();

    public ABMFirstCallActivationAgent(CRMProcessor processor)
    {
        super();
        processor_ = processor;
    }

    public void execute(Context ctx) throws AgentException
    {
        final List params = new ArrayList();
        final ProcessorInfo info = (ProcessorInfo) ctx.get(ProcessorInfo.class);

        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "execute()");
        final SimpleLocks locker = (SimpleLocks) ctx.get(SubscriberHomeFactory.SUBCRIBER_LOCKER);
        String subId = null;
        
        try
        {
            try
            {
                CRMProcessorSupport.makeArray(ctx, params, info.getRecord(), info.getStartIndex(), ',', info.getErid(),
                        this);
            }
            catch (FilterOutException e)
            {
                return;
            }

            if (Logger.isDebugEnabled())
            {
                Logger.debug(ctx, this, ABMProcessor.getDebugParams(params));
            }

            //use the time of the ER to represent the activation time since that was when the activation event occurred.
            
            Date activationDate = new Date(info.getDate());
            
            // Migrating changes to trunk from 9_9_tcb
            // BSS-2318
            // Get current date from context.
            Date runningDate = CalendarSupportHelper.get(ctx).getRunningDate(ctx);
            
            final String activationDateStr;
            final String activationTime = CRMProcessorSupport.getField(params, INDEX_ACTIVATION_TIME);;
            final String activationTimeZone =  CRMProcessorSupport.getField(params, INDEX_ACTIVATION_TIME_ZONE);
            
            // BSS-2318
            // Compare Activation Date with current date. If less than today then use current date for activation.
            if (CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(activationDate).before(CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(runningDate)))
            {
            	activationDate = runningDate;
            	DateFormat fmt = new SimpleDateFormat("yyyy/MM/dd");
            	activationDateStr = fmt.format(runningDate);
            	Logger.debug(ctx, this, "Activation date from ER is old. Setting current date as activation date" + activationDateStr);
            }
            else
            {
            	activationDateStr  = CRMProcessorSupport.getField(params, INDEX_ACTIVATION_DATE);
            }
            
            if(!activationDateStr.trim().equals("") && !activationTime.trim().equals("") && !activationTimeZone.trim().equals(""))
            {
                final String activationDateWithTZ = activationDateStr+" "+activationTime+" "+activationTimeZone;
                DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss Z"); 
                if (activationDateStr.length() == 0)
                {
                    activationDate = null;
                }
                else
                {
                    try
                    {
                        activationDate = new Date(formatter.parse(activationDateWithTZ.trim()).getTime());
                    }
                    catch (ParseException e)
                    {
                        final String formattedMsg = MessageFormat.format(
                                "Could not parse activationDateWithTZ String \"{0}\".",
                                new Object[] {activationDateWithTZ});
                        throw new HomeException(formattedMsg, e);
                    }
                }
            }

            // MSISDN specified in the ER.
            final String msisdnStr = CRMProcessorSupport.getField(params, INDEX_MSISDN);
            String msisdn = "";
            try
            {
                msisdn = CRMProcessorSupport.getMsisdn(msisdnStr);
            }
            catch (ParseException e)
            {
                final String formattedMsg = MessageFormat.format(
                        "Could not parse Msisdn \"{0}\". Caused by {1}.",
                        new Object[] {msisdnStr, e.getMessage()});

                throw new HomeException(formattedMsg, e);
            }

            final String userId = CRMProcessorSupport.getField(params, INDEX_USERID);

            final String appOcgClientUsername = getOcgUsername(ctx);

            if (appOcgClientUsername != null)
            {
                if (userId.equals(appOcgClientUsername))
                {
                    Logger.info(ctx, this, "Activation was triggered from CRM so Activation ER for [msisdn=" + msisdn
                            + ", activationTime=" + activationDate + "] will be ignored.");
                    return;
                }
            }
            else
            {
                Logger.major(ctx, this, "Unable to retreive OCG connection username!");
            }

            // Subscriber with the MSISDN.
            Subscriber subscriber = null;

            try
            {
         	    subId =  SubscriberSupport.lookupSubscriberIdForMSISDN(ctx, msisdn, activationDate);

            	if (locker != null && subId != null )
            	{

            			locker.lock(subId);
         	
            	}
                subscriber = SubscriberSupport.lookupSubscriberForSubId(ctx, subId); 

            }
            catch (HomeException e)
            {
                final String formattedMsg = MessageFormat.format(
                        "Failed to look-up subscriber for MSISDN \"{0}\".",
                        new Object[] {msisdnStr});
                throw new HomeException(formattedMsg);
            }
            
            
            final String expiryDateStr = CRMProcessorSupport.getField(params, INDEX_EXPIRY_DATE);
            final Date expiryDate;
            if (expiryDateStr.length() == 0)
            {
                expiryDate = null;
            }
            else
            {
                try
                {
                    expiryDate = CalendarSupportHelper.get(ctx).convertDateWithNoTimeOfDayToTimeZone(
                            CRMProcessorSupport.getDateOnly(expiryDateStr), subscriber.getTimeZone(ctx));
                }
                catch (ParseException e)
                {
                    final String formattedMsg = MessageFormat.format(
                            "Could not parse FinalExpiryDate String \"{0}\".",
                            new Object[] {expiryDateStr});

                    throw new HomeException(formattedMsg, e);
                }
            }


            Logger.info(ctx, this, "About to activate subscriber [msisdn=" + msisdn
                    + ", id=" + subscriber.getId() + "]");
            activateSubscriber(ctx, subscriber, activationDate, expiryDate);
            Logger.info(ctx, this, "Activation for subscriber [msisdn=" + msisdn
                    + ", id=" + subscriber.getId() + "] complete!");
        }
        catch (final Throwable t)
        {
            Logger.minor(ctx, this, "Failed to process ER 453 because of Exception " + t.getMessage(), t);
            processor_.saveErrorRecord(ctx, info.getRecord());
        }
        finally
        {
           	if( locker != null && subId != null )
            {	
            	locker.unlock(subId);
            }	
 
            pmLogMsg.log(ctx);
        }
    }

    /**
     * Retrieves user name from OCG CORBA client that is used in the ER453 to specify what application triggered the
     * activation.
     * 
     * @param ctx the operation context
     * @return OCG CORBA client user name
     */
    public String getOcgUsername(final Context ctx)
    {
        String result = null;
        final Home home = (Home) ctx.get(ProxyInfoHome.class);

        try
        {
            final String key = PrepaidAccountService.class.getSimpleName();
            final ProxyInfo proxyInfo = (ProxyInfo) home.find(ctx, key);

            if (proxyInfo != null)
            {
                MultiplexerConfiguration mpc = (MultiplexerConfiguration) proxyInfo.getConfig();
                CorbaClientProperty conf = (CorbaClientProperty) mpc.getConfig();
                result = conf.getUsername();
            }
        }
        catch (Exception e)
        {
            Logger.major(ctx, this, "Unable to retreive OCG connection username!", e);
        }

        return result;
    }
}
