/* 
* This code is a protected work and subject to domestic and international
* copyright law(s). A complete listing of authors of this work is readily 
* available. Additionally, source code is, by its very nature, confidential 
* information and inextricably contains trade secrets and other information 
* proprietary, valuable and sensitive to Redknee, no unauthorised use, 
* disclosure, manipulation or otherwise is permitted, and may only be used 
* in accordance with the terms of the licence agreement entered into with 
* Redknee Inc. and/or its subsidiaries. 
* 
* Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved. 
*/ 
package com.trilogy.app.crm.poller.agent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.SubscriberCltc;
import com.trilogy.app.crm.bean.SubscriberCltcErCommandEnum;
import com.trilogy.app.crm.bean.SubscriberCltcHome;
import com.trilogy.app.crm.bean.SubscriberCycleUsageHome;
import com.trilogy.app.crm.bean.core.custom.SubscriberCycleUsage;
import com.trilogy.app.crm.poller.Constants;
import com.trilogy.app.crm.poller.FilterOutException;
import com.trilogy.app.crm.poller.ProcessorInfo;
import com.trilogy.app.crm.poller.event.ABMUnifiedBillingProcessor;
import com.trilogy.app.crm.poller.event.CRMProcessor;
import com.trilogy.app.crm.poller.event.CRMProcessorSupport;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.OMLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;


/**
 * @author vcheng
 */
public class ABMUnifiedBillingAgent implements ContextAgent
{
    public ABMUnifiedBillingAgent(CRMProcessor processor)
    {
        super();
        processor_ = processor;
    }


    /* (non-Javadoc)
     * @see com.redknee.framework.xhome.context.ContextAgent#execute(com.redknee.framework.xhome.context.Context)
     */
    public void execute(Context ctx) throws AgentException
    {
        List params = new ArrayList();
        ProcessorInfo info = (ProcessorInfo) ctx.get(ProcessorInfo.class);

        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "execute()");
                       
        try
        {
	
        	try {
        		CRMProcessorSupport.makeArray(ctx, params, info.getRecord(), info.getStartIndex(), ',',info.getErid(), this);
        	} catch ( FilterOutException e){
				return; 
			}
        	
             switch (Integer.parseInt(info.getErid()))
            {
//                case ABM_CLTC_ER_IDENTIFIER:
//                {
//                    //createPostpaidSubscriberCltc(params);
//                	//moved to ABMLowBalanceProcessor.java
//                    break;
//                }
                case ABMUnifiedBillingProcessor.ABM_FREE_USAGE_ER_IDENTIFIER:
                {
                    createSubscriberCycleUsage(ctx, new Date(info.getDate()), params);
                    break;
                }
                default:
                {
                    // Unknown ABM ER -- Ignore.
                }
            }
        }
       	catch (final Throwable t)
       	{
            new MinorLogMsg(this, "Failed to process ER 448 because of Exception " + t.getMessage(), t).log(ctx);
            processor_.saveErrorRecord(ctx, info.getRecord());
        }
       	finally 
       	{
            pmLogMsg.log(ctx);
        }
	}
    
    
    public static void createSubscriberCltc(final Context ctx, final List params1)
    {
        String msisdn = "";
        
        try
        {	
            new OMLogMsg(Common.OM_MODULE, Common.OM_CLCT_ER).log(ctx);
            
            msisdn =(String)params1.get(ABM_CLTC_ER_ARRAY_INDEX_MSISDN);
            String operation = (String) params1.get(ABM_CLTC_ER_ARRAY_INDEX_OPERATION);
            short command = Short.parseShort((String)params1.get(ABM_CLTC_ER_ARRAY_INDEX_COMMAND));
            long abmBal = Long.parseLong((String)params1.get(ABM_CLTC_ER_ARRAY_INDEX_ABMBAL));
            long creditLimit = Long.parseLong((String)params1.get(ABM_CLTC_ER_ARRAY_INDEX_CREDITLIMIT));
            long threshold = Long.parseLong((String)params1.get(ABM_CLTC_ER_ARRAY_INDEX_THRESHOLD));
            int spid = Integer.parseInt((String)params1.get(ABM_CLTC_ER_ARRAY_INDEX_SPID));
            long initialBal = Long.parseLong((String)params1.get(ABM_CLTC_ER_ARRAY_INDEX_INITIAL_BALANCE));
            long newBal = Long.parseLong((String)params1.get(ABM_CLTC_ER_ARRAY_INDEX_NEW_BALANCE));
            
            String dependentServices = null;
            if(params1.size() >= ABM_CLTC_ER_ARRAY_INDEX_SERVICE_IDS)
            {
                dependentServices = (String) params1.get(ABM_CLTC_ER_ARRAY_INDEX_SERVICE_IDS);
            }	
            
            String thresholdName = null;
            if(params1.size() >= ABM_CLTC_ER_ARRAY_INDEX_ATU_INVOKE)
            {
                thresholdName = (String) params1.get(ABM_CLTC_ER_ARRAY_INDEX_ATU_INVOKE);
            }    
                    
            SubscriberCltc cltc = new SubscriberCltc();
            cltc.setSpid(spid);
            cltc.setMsisdn(msisdn);
            cltc.setAbmBalance(abmBal);
            if(operation != null && !operation.isEmpty())
            {
                cltc.setOperation(Integer.parseInt(operation));
            }
            cltc.setCommand(SubscriberCltcErCommandEnum.get(command));
            cltc.setCltcThreshold(threshold);
            cltc.setCreditLimit(creditLimit);
            cltc.setNewBalance(newBal);
            cltc.setOldBalance(initialBal);
            cltc.setBundleServicesChanged(dependentServices == null ? "" : dependentServices.replaceAll("\\|", ","));
			cltc.setThresholdTags(thresholdName);
			
            Home home = (Home)ctx.get(SubscriberCltcHome.class);
            home.create(ctx,cltc);
        }
        catch (HomeException e)
        {
            final IllegalStateException newException = new IllegalStateException("Cannot create SubscribleCLTC record for msisdn "+ msisdn);
            newException.initCause(e);
            throw newException;
        }
        catch (Exception e)
        {
            final IllegalStateException newException = new IllegalStateException("Cannot create SubscribleCLTC record");
            newException.initCause(e);
            throw newException;
        }
    }
    

    /**
     * Creates a SubscriberCycleUsage from an ABM Free Usage ER.
     *
     * @param date The date of the ER.
     * @param fields The list of field values from an ER.  The indices are
     * defined in {@link Constants}.
     */
    private void createSubscriberCycleUsage(final Context ctx, final Date date, final List fields)
    {
        final SubscriberCycleUsage usage = new SubscriberCycleUsage();
        String msisdn = "";
        
        try
        {
            msisdn = CRMProcessorSupport.getField(fields, ABM_FREE_USAGE_ER_INDEX_MSISDN);
            
            // The usage object does the work of looking up and calculating all of
            // the identifiecation information of the MSISDN.
            usage.initializeIdentification(ctx, msisdn, date);

            // For added integrity, we can check that the SPID in the ER matches the
            // SPID looked-up by the usage object.
            final int spid = Integer.parseInt(CRMProcessorSupport.getField(fields, ABM_FREE_USAGE_ER_INDEX_SPID));

            if (spid != usage.getSpid())
            {
                throw new IllegalStateException(
                    "The service provider in the ER, \""
                    + spid
                    + "\", does not match the service provider of the subscriber, \""
                    + usage.getSpid()
                    + "\".");
            }

            usage.setPastMonthlyFCT(
                roundSecondsToMinutes(Integer.parseInt(CRMProcessorSupport.getField(fields, ABM_FREE_USAGE_ER_INDEX_PAST_MONTHLY_FCT))));
            usage.setPastFCTUsed(
                roundSecondsToMinutes(Integer.parseInt(CRMProcessorSupport.getField(fields, ABM_FREE_USAGE_ER_INDEX_PAST_FCT_USED))));
            usage.setPastRollOverFCTAwarded(
                roundSecondsToMinutes(Integer.parseInt(CRMProcessorSupport.getField(fields, ABM_FREE_USAGE_ER_INDEX_PAST_ROLLOVER_FCT_AWARDED))));
            usage.setPastRollOverFCTUsed(
                roundSecondsToMinutes(Integer.parseInt(CRMProcessorSupport.getField(fields, ABM_FREE_USAGE_ER_INDEX_PAST_ROLLOVER_FCT_USED))));
            usage.setPastUsedGroupFCT(
                roundSecondsToMinutes(Integer.parseInt(CRMProcessorSupport.getField(fields, ABM_FREE_USAGE_ER_INDEX_PAST_USED_GROUP_FCT))));
            usage.setRollOverFCT(
                roundSecondsToMinutes(Integer.parseInt(CRMProcessorSupport.getField(fields, ABM_FREE_USAGE_ER_INDEX_ROLLOEVER_FCT))));
            usage.setExpiredFCT(
                    roundSecondsToMinutes(Integer.parseInt(CRMProcessorSupport.getField(fields, ABM_FREE_USAGE_ER_INDEX_EXPIRED_FCT))));

            final Home home = (Home)ctx.get(SubscriberCycleUsageHome.class);
            home.create(ctx,usage);
        }
        catch (final Throwable t)
        {
            String msg = "Failed to create SubscriberCycleUsage for subscriber mobile number , \""
                + msisdn
                + "\". " + t.getMessage(); 
            
            new MinorLogMsg(this, msg, t).log(ctx);
            
            final IllegalStateException newException =
                new IllegalStateException(msg);
                   
            
            newException.initCause(t);
            throw newException;
        }
    }


    /**
     * Rounds the given number of seconds to the nearest minute.
     *
     * @param seconds The number of seconds.
     * @return The number of minutes.
     */
    private int roundSecondsToMinutes(final int seconds)
    {
        return (int)Math.round(seconds / 60.0);
    }

    
    private CRMProcessor processor_= null;
    
    //ABM Credit Limit Threshold ER
    //A Sample ER:
    //2004/11/09,16:04:42,447,700,Synaxis2200,0,3977222412,5,6,7,0,1
    public static final int ABM_CLTC_ER_IDENTIFIER = 447;
    private static final int ABM_CLTC_ER_ARRAY_INDEX_SPID = 2;
    private static final int ABM_CLTC_ER_ARRAY_INDEX_MSISDN = 3;
    private static final int ABM_CLTC_ER_ARRAY_INDEX_THRESHOLD = 4;
    private static final int ABM_CLTC_ER_ARRAY_INDEX_ABMBAL = 5;
    private static final int ABM_CLTC_ER_ARRAY_INDEX_CREDITLIMIT = 6;
    private static final int ABM_CLTC_ER_ARRAY_INDEX_OPERATION = 7; 
    private static final int ABM_CLTC_ER_ARRAY_INDEX_COMMAND = 8;
    private static final int ABM_CLTC_ER_ARRAY_INDEX_INITIAL_BALANCE = 10;
    private static final int ABM_CLTC_ER_ARRAY_INDEX_NEW_BALANCE = 11;
    private static final int ABM_CLTC_ER_ARRAY_INDEX_SERVICE_IDS = 12;
    private static final int ABM_CLTC_ER_ARRAY_INDEX_ATU_INVOKE = 13;
    
	
    // ABM Free Usage ER
    // TODO - 2004-11-09 - Verify the identifier and indices in the ABM FS.
    private static final int ABM_FREE_USAGE_ER_INDEX_SPID = 2;
    private static final int ABM_FREE_USAGE_ER_INDEX_MSISDN = 3;
    private static final int ABM_FREE_USAGE_ER_INDEX_PAST_MONTHLY_FCT = 4;
    private static final int ABM_FREE_USAGE_ER_INDEX_PAST_FCT_USED = 5;
    private static final int ABM_FREE_USAGE_ER_INDEX_PAST_ROLLOVER_FCT_AWARDED = 6;
    private static final int ABM_FREE_USAGE_ER_INDEX_PAST_ROLLOVER_FCT_USED = 7;
    private static final int ABM_FREE_USAGE_ER_INDEX_PAST_USED_GROUP_FCT = 8;
    private static final int ABM_FREE_USAGE_ER_INDEX_ROLLOEVER_FCT = 9;
    private static final int ABM_FREE_USAGE_ER_INDEX_EXPIRED_FCT = 12;	  

    private static final String PM_MODULE = ABMUnifiedBillingAgent.class.getName();

}