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
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.bas.tps.pipe;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.trilogy.app.crm.bas.tps.TPSRecord;
import com.trilogy.app.crm.bas.tps.TPSSupport;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AdjustmentInfo;
import com.trilogy.app.crm.bean.CurrencyPrecision;
import com.trilogy.app.crm.bean.PayeeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.bean.TransactionHome;
import com.trilogy.app.crm.bean.core.SubscriptionType;
import com.trilogy.app.crm.log.ERLogger;
import com.trilogy.app.crm.tps.pipe.TPSPipeConstant;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xlog.log.EntryLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.util.snippet.log.Logger;

/**
 * Convert TPS record to a Transaction record, because in the following 
 * processing step, TPS processing will share code with processing from 
 * GUI. see TransactionHome and other two pipe line for details. 
 * 
 * @author lanny.tse@redknee.com
 *
  */
public class ConvertTpsToTransactionAgent extends PipelineAgent 
{

    /**
     * the serial version uid
     */
    private static final long serialVersionUID = 8839663359193312729L;

    /**
     * Date format string.
     */
    public static final String DATE_FORMAT_STRING = "yyyyMMdd";

    /**
     * Constructor that accepts the next logic in the pipeline.
     * @param delegate the next logic in the pipeline
     */
	public ConvertTpsToTransactionAgent(ContextAgent delegate)
	{
	   super(delegate);
	}

	/**
     * Executes the logic for this particular pipeline.
    * @param ctx
    *           A context
    * @exception AgentException
    *               thrown if one of the services fails to initialize
    */

    public void execute(final Context ctx) throws AgentException
    {

        final TPSRecord tps = (TPSRecord) ctx.get(TPSRecord.class);
        final Home  home = (Home) ctx.get(TransactionHome.class);

        try
        {
            final Transaction transaction = (Transaction) XBeans.instantiate(Transaction.class, ctx);

            updateMandatoryInfomation(ctx,
                    tps,
                    transaction);

           	transaction.setTransactionMethod( TPSSupport.getPaymentMethod(tps)); 


            if (tps.getExportDate() != null)
            {
                final DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_STRING);
                
                String csrInput = transaction.getCSRInput() + " export date " + 
                		dateFormat.format(tps.getExportDate());
                 transaction.setCSRInput( csrInput);

            }

            transaction.setAgent(tps.getTpsFileName());

           // assume IN type for now
          SubscriptionType subcriptionType = SubscriptionType.getINSubscriptionType(ctx);
            if(null == subcriptionType)
            {
                throw new Exception("An IN subscription type is not defined in the system.");
            }
            transaction.setSubscriptionTypeId(subcriptionType.getId());


            home.create(ctx, transaction);

            pass(ctx, this, "Converted the tps record to transaction entry and save to database.");
        }
        catch (Exception e)
        {

            if (tps.isAccountLevel())
            {
                ERLogger.genAccountAdjustmentER(ctx,
                        tps,
                        TPSPipeConstant.FAIL_TO_FILL_IN_TRASACTION_RECORD);
            }
            else
            {
                Subscriber subs = (Subscriber) ctx.get(Subscriber.class);
                AdjustmentInfo info = (AdjustmentInfo)tps.getAdjType().getAdjustmentSpidInfo().get(
                        new Integer(subs.getSpid()));
                ERLogger.createSubAdjustmentEr(
                        ctx,
                        subs.getSpid(),
                        tps.getAccountNum(),
                        subs.getId(),
                        tps.getAmount(),
                        "",
                        String.valueOf(tps.getAdjType().getCode()),
                        info.getGLCode(),
                        TPSPipeConstant.FAIL_TO_FILL_IN_TRASACTION_RECORD, 
                        tps.getTpsFileName(), 
                        (tps.getLocationCode().length() > 0) ? tps.getLocationCode() : "none"
                        );            
            }


                // send out alarm
            new EntryLogMsg(10534,
                    this,
                    "",
                    "",
                    new String[]{"Fail to convert the tps rec to transaction entry"},
                    e).log(ctx);
            fail(ctx,
                    this,
                    "Fail to convert tps record to transaction entry:" + tps.getTelephoneNum(),

                    e,
                    TPSPipeConstant.FAIL_TO_FILL_IN_TRASACTION_RECORD);
        }

    }

    /**
     * UPdates the transaction with the mandatory information.
     * @param ctx the operating context
     * @param acct the account to fill information
     * @param tps the tps format bean to mat
     * @param transaction the transaction to copy information.
     * @param adjustInfo the adjustment type information from the adjustment type
     * @param type the adjustment type to get the code and name
     * @param msisdn the already prefixed msisdn
     */
    private void updateMandatoryInfomation(final Context ctx,
            final TPSRecord tps,
            final  Transaction transaction) throws AgentException
    {
        final Subscriber sub = (Subscriber) ctx.get(Subscriber.class);

        long transactionAmount = tps.getAmount();
        CurrencyPrecision precision = (CurrencyPrecision) ctx.get(CurrencyPrecision.class);
        if (precision == null)
        {
            String msg = "Cannot find currency precision object in the context.";
            LogSupport.minor(ctx, this,
                    msg);
            throw new AgentException(msg);
        }
        else
        {
            int precisionDiff = precision.getStoragePrecision()-precision.getDisplayPrecision();
            if (precisionDiff>0)
            {
                // Adding 0's to the end of the amount if storage precision is greater than display precision.
                for (int i=0;i<precisionDiff;i++)
                {
                    transactionAmount *= 10;
                }
            }
        }

        transaction.setAmount(-1 * transactionAmount);
        transaction.setTaxPaid(0);
        transaction.setAdjustmentType(tps.getAdjType().getCode());
        transaction.setAction(tps.getAdjType().getAction());
        
        transaction.setReceiveDate(new Date());
        transaction.setTransDate(tps.getPaymentDate());
        transaction.setLocationCode(tps.getLocationCode());
        transaction.setCSRInput(""); 

        if (tps.getTransactionNum() != null && tps.getTransactionNum().trim().length() != 0)
        {
            transaction.setExtTransactionId(tps.getTransactionNum());
        }
        transaction.setPaymentDetails(tps.getPaymentDetail());

        /**
         * msisdn has higher priority than BAN in TPS processing. policy changed by 
         * request from IBIS Jamaica. 
         */
        if (!tps.isAccountLevel())
        {
            transaction.setMSISDN(tps.getMsisdn());
            transaction.setSubscriberID(tps.getSubscriber().getId()); 
            transaction.setBAN(sub.getBAN());
            try
            {
                transaction.setResponsibleBAN(sub.getAccount(ctx).getResponsibleBAN());
             }
            catch (Exception e)
            {
                Logger.minor(ctx, this, "Unable to retreive Account [" + sub.getBAN() + "]", e);
            }
            
            if(tps.getAccountNum() != null)
            {	
            	transaction.setCSRInput("TPS BAN: " + tps.getAccountNum());
            } 
            
 			AdjustmentInfo info = (AdjustmentInfo)tps.getAdjType().getAdjustmentSpidInfo().get(
					new Integer(sub.getSpid()));
			transaction.setGLCode(info.getGLCode());
			transaction.setSubscriberType(sub.getSubscriberType());

        }
        else
        {
            Account account = (Account) ctx.get(Account.class); 
            
            transaction.setSubscriberType(((SubscriberTypeEnum.PREPAID == account.getSystemType())? SubscriberTypeEnum.PREPAID:SubscriberTypeEnum.POSTPAID));
            transaction.setBAN(tps.getAccountNum());
            transaction.setResponsibleBAN(account.getResponsibleBAN());
            transaction.setPayee(PayeeEnum.Account);
           	transaction.setCSRInput("TPS BAN: " + tps.getAccountNum());

			AdjustmentInfo info = (AdjustmentInfo)tps.getAdjType().getAdjustmentSpidInfo().get(
					new Integer(account.getSpid()));
			transaction.setGLCode(info.getGLCode());
            
        }
    }

}
