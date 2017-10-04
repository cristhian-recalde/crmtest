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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.OMLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.poller.FilterOutException;
import com.trilogy.app.crm.poller.ProcessorInfo;
import com.trilogy.app.crm.poller.VRAPoller;
import com.trilogy.app.crm.poller.event.CRMProcessor;
import com.trilogy.app.crm.poller.event.CRMProcessorSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.support.SubscriptionNotificationSupport;
import com.trilogy.app.crm.support.VRASupport;

/**
 * @author vcheng
 */
public class VRAERAgent implements ContextAgent
{
	public static final String CPS_INFINITE_EXPIRY_DATE_STRING = "99991231";
    public VRAERAgent( CRMProcessor processor)
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

        if ( LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, "VRA Poller ER " + info.getErid(), null).log(ctx);
        }

        try
        {
        	try {
        		CRMProcessorSupport.makeArray(ctx, params, info.getRecord(), info.getStartIndex(), ',',info.getErid(), this);
           	} catch ( FilterOutException e){
				return; 
			}

            switch (Integer.parseInt(info.getErid()))
            {
                case VRAPoller.VRA_ER_IDENTIFIER:
                {
                    boolean createClawbackTransaction = true;
                    createTransaction(ctx, new Date(info.getDate()), params, createClawbackTransaction);
                    break;
                }
                default:
                {
                    // Unknown VRA ER -- Ignore.
                }
            }
        }
       	catch (final Throwable t)
       	{
            new MinorLogMsg(this, "Failed to process ER " +info.getErid()+ " because of Exception " + t.getMessage(), t).log(ctx);
            processor_.saveErrorRecord(ctx, info.getRecord());
        }
       	finally
       	{
            pmLogMsg.log(ctx);
            CRMProcessor.playNice(ctx, CRMProcessor.MEDIUM_ER_THROTTLING);
        }
    }

	/**
     * Update subscriber profile and create Transaction Entry per VRA ER.
     *
     * @param transDate Transaction date.
     * @param params1 The parsed ER fields value list.
     */

    public void createTransaction(final Context ctx, final Date transDate, final List params1, boolean createClawbackTransaction)
    throws Exception
    {
        new OMLogMsg(Common.OM_MODULE, Common.OM_VRA_ER).log(ctx);

        short vraErrorCode = Short.parseShort((String)params1.get(VRA_ER_ARRAY_INDEX_VRA_UPDATERESULT));
        String msisdn      = ((String)params1.get(VRA_ER_ARRAY_INDEX_MSISDN)).trim();
        Subscriber subscriber = SubscriberSupport.lookupSubscriberForMSISDN(ctx, msisdn, transDate);
        
        if ( subscriber == null )
        {
            new InfoLogMsg(this,"Subscriber with MSISDN [" + msisdn +  "] could not be found. Failed to process VRA ER properly.",null).log(ctx);
            return;
        }
        // only handle successful cases
        if ( vraErrorCode != 0 )
        {
        	SubscriptionNotificationSupport.sendVoucherTopupFailureNotification(ctx, subscriber);
            new InfoLogMsg(this, "VRA ER for MSISDN " + msisdn + " indicates a failed VRA transaction. error code=["+vraErrorCode+"]", null).log(ctx);
            return;
        }

       

        // Retrieve event type.
        int eventType = -1;
        if( params1.size() > VRA_ER_ARRAY_INDEX_EVENTTYPE+1 /* +1 here because params puts a blank at the end */ )
        {
            // This is in an if block for backwards compatibility with pre-overdraft versions of VRA
            eventType = Integer.parseInt((String)params1.get(VRA_ER_ARRAY_INDEX_EVENTTYPE));
        }

        long voucherValue = Long.parseLong((String)params1.get(VRA_ER_ARRAY_INDEX_VOUCHERVALUE));
        final long newBalance   = Long.parseLong((String)params1.get(VRA_ER_ARRAY_INDEX_NEWBALANCE));
        final long creditValue = Long.parseLong((String)params1.get(VRA_ER_ARRAY_INDEX_CREDITVALUE));
        final Date newExiryDate = new SimpleDateFormat(NEW_EXPIRYDATE_FORMAT_STRING).parse((String)params1.get(VRA_ER_ARRAY_INDEX_NEW_EXPIRY));
        
        int expiryExtensionDays = daysBetweenDates(transDate, newExiryDate);
        
        //TT#12033002010 Fixed. If CPS is sending infinite expiry date, then no need to calculate expiry Date extension.
        //If this value is calculated, it becomes quite large int which after down-casting to short becomes a -ve value and hence 
        //transaction creation fails.
        final Date infiniteExiryDate = new SimpleDateFormat(NEW_EXPIRYDATE_FORMAT_STRING).parse(CPS_INFINITE_EXPIRY_DATE_STRING);
        if(infiniteExiryDate.equals(newExiryDate))
        {
        	expiryExtensionDays = 0;
        	if ( LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Setting the expiryDateExtensionDays = 0 as this is infinite expiry case", null).log(ctx);
            }
        }
        String csrInput = getCsrInput(params1);

        // Create VRA transaction
        VRASupport.createVRATransaction(ctx, transDate, voucherValue, newBalance, creditValue, expiryExtensionDays, subscriber, csrInput, eventType, "");

        // Create clawback transaction
        if( eventType == VRASupport.VRA_ER_EVENTTYPE_NORMAL && createClawbackTransaction)
        {
            final int clearedOverdraftAmount = Integer.parseInt((String)params1.get(VRA_ER_ARRAY_INDEX_CLEAREDOVERDRAFTAMOUNT));
            VRASupport.createClawbackTransaction(ctx, transDate, clearedOverdraftAmount, newBalance, creditValue, expiryExtensionDays, subscriber, csrInput);
        }
    }
    
    @SuppressWarnings("unchecked")
    private String getCsrInput(final List params1)
    {
        String ret = "";
        if (params1.size()>VRA_ER_ARRAY_INDEX_VOUCHER_TYPE_ID)
        {
            String voucherTypeId = (String)params1.get(VRA_ER_ARRAY_INDEX_VOUCHER_TYPE_ID);
            ret = "Voucher Type ID=" + voucherTypeId;
        }
        return ret;
    }    

    private int daysBetweenDates(Date date1, Date date2)
    {
        {
            // use 0 time of day
        	Calendar calendar = Calendar.getInstance();
            calendar.setTime(date1);
            calendar.set(Calendar.HOUR, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            date1 = calendar.getTime();
        }
        
        {
        	// use noon of day - a DST shift of half day will never be there.
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date2);
            calendar.set(Calendar.HOUR, 12);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            date2 = calendar.getTime();
        }
        
        long millis = Math.abs(date1.getTime()  - (date2.getTime() + 1));
        return (int) (millis / (1000L*3600*24));
    }
    



    private CRMProcessor processor_;
    
    public static final String PM_MODULE = VRAERAgent.class.getName();
    public static final String VRA_ADJUSTMENTTYPE_NAME                 = "AdjustmentType For VRA";
    private static final int VRA_ER_ARRAY_INDEX_MSISDN                 = 5;
    private static final int VRA_ER_ARRAY_INDEX_VOUCHERVALUE           = 15;
    private static final int VRA_ER_ARRAY_INDEX_CREDITVALUE 	       = 19;
    private static final int VRA_ER_ARRAY_INDEX_NEWBALANCE             = 21;
    //private static final int VRA_ER_ARRAY_INDEX_NEWEXPIREDDATE       = 16;
    private static final int VRA_ER_ARRAY_INDEX_VRA_UPDATERESULT       = 28;
    private static final int VRA_ER_ARRAY_INDEX_EVENTTYPE              = 32;
    private static final int VRA_ER_ARRAY_INDEX_CLEAREDOVERDRAFTAMOUNT = 35;
    private static final int VRA_ER_ARRAY_INDEX_VOUCHER_TYPE_ID        = 39;
    private static final int VRA_ER_ARRAY_INDEX_NEW_EXPIRY             = 23;

    private static final String FORMAT_STRING = "yyyy/MM/dd HH:mm:ss";
    private static final String  NEW_EXPIRYDATE_FORMAT_STRING = "yyyyMMdd";   
    

    


}
