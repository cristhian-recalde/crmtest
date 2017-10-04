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

package com.trilogy.app.crm.home;

import java.util.Calendar;
import java.util.Date;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SystemNoteSubTypeEnum;
import com.trilogy.app.crm.bean.SystemNoteTypeEnum;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.bean.TransactionHome;
import com.trilogy.app.crm.bean.TransactionXInfo;
import com.trilogy.app.crm.config.CRMConfigInfoForVRA;
import com.trilogy.app.crm.support.VRASupport;
import com.trilogy.app.crm.extension.MovableExtension;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporteeSubExtension;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporterSubExtension;
import com.trilogy.app.crm.home.sub.PPSMSupporteeExtensionDeactivationHome;
import com.trilogy.app.crm.subscriber.charge.AbstractSubscriberProvisioningCharger;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.CoreTransactionSupportHelper;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.app.crm.support.NoteSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.xhome.home.OcgTransactionException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;



/**
 * This class will redirect all recurring charge from prepaid subscriber to postpaid
 * support msisdn if postpaid support msisdn is in valid states.
 *
 * @author joe.chen@redknee.com
 */
public class PostpaidSupportMsisdnTransHome extends HomeProxy
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 8179433153634693587L;
    
    public static String PPSM_BILLING_DATE = "ppsmBillingDate" ;


    /**
     * Create a new instance of <code>PostpaidSupportMsisdnTransHome</code>.
     *
     * @param delegate
     *            Delegate of this decorator.
     */
    public PostpaidSupportMsisdnTransHome(final Home delegate)
    {
        super(delegate);
    }


    /**
     * Redirect charge to postpaid support msisdn, if state is correct any failure
     * encounter, will continue charge to prepaid.
     *
     * @param ctx
     *            The operating context.
     * @param obj
     *            The transaction to be created.
     * @return The created transaction.
     * @throws HomeException
     *             Thrown if there are problems creating the transaction.
     */
    @Override
    public Object create(final Context ctx, final Object obj) throws HomeException
    {
        Object ret = null;

        // Larry: the following code need some refactoing, have to reduce the condition
        // nesting.

        if (obj instanceof Transaction)
        {
            final Transaction transaction = (Transaction) obj;
            Transaction duplicate = null;

            final CRMConfigInfoForVRA config = VRASupport.getCRMConfigInfoForVRA(ctx,transaction.getSpid());
            final int oldAdjustmentType = transaction.getAdjustmentType();

            // Larry: special exception for VRA transaction. don't like the solution, but
            // SDA said it is fine.

            /*
             * [Cindy Wong] 2008-04-24: There are now different VRA adjustment types for
             * prepaid and postpaid. Checking for both right now.
             */
            if (config.getPrepaidAdjustmentType() == oldAdjustmentType
                || config.getPostpaidAdjustmentType() == oldAdjustmentType)
            {

                duplicate = findDuplicateTransaction(ctx, transaction.getSubscriberID(), transaction.getMSISDN(),
                        transaction.getAdjustmentType(), transaction.getAmount(), transaction.getTransDate(), transaction.getBalance(),
                        transaction.getExtTransactionId());

                if (duplicate == null)
                {
                    return super.create(ctx, obj);
                }
                else
                {
                    final StringBuilder sb = new StringBuilder();
                    sb.append("The VRA transaction has already polled into CRM for adjType=");
                    sb.append(transaction.getAdjustmentType());
                    sb.append(" amount=");
                    sb.append(transaction.getAmount());
                    sb.append(" balance=");
                    sb.append(transaction.getBalance());
                    sb.append(" type=");
                    sb.append(transaction.getAction());
                    new MajorLogMsg(this, sb.toString(), null).log(ctx);
                    // return the found duplicate Transaction
                    return duplicate;

                }
            }
            
            
            final PMLogMsg ppsmInitialLogic = new PMLogMsg("Transaction", "PPSM initial logic", "");
            final PMLogMsg ppsmLogicPm = new PMLogMsg("Transaction", "PPSM complete logic", "");
            try
            {
                Subscriber subscriber = (Subscriber) ctx.get(Subscriber.class);
                if (subscriber==null || subscriber.getId()==null || !subscriber.getId().equals(transaction.getSubscriberID()))
                {
                    subscriber = SubscriberSupport.lookupSubscriberForMSISDN(ctx,transaction.getMSISDN(), transaction.getTransDate());
                }

                /*
                 * TT#11042941060: if no subscriber is found, attempt to look up by
                 * subscriber ID.  If subscriber is still not found, throw exception.
                 */
                if (subscriber == null)
                {
                    subscriber = SubscriberSupport.lookupSubscriberForSubId(ctx, transaction.getSubscriberID());
                }

                if (subscriber == null)
                {
                    String msg = "Subscriber not found for transaction " + transaction.getReceiptNum();
                    HomeException e = new HomeException(msg);
                    new MinorLogMsg(this, "Subscriber not found for this transaction",
                            e).log(ctx);
                }

                String supporterSubscriberMSISDN = null;
                PPSMSupporteeSubExtension supporteeExtension = PPSMSupporteeSubExtension.getPPSMSupporteeSubscriberExtension(ctx, subscriber.getId());
                boolean ppsmTransaction = supporteeExtension!=null && supporteeExtension.supportsAdjustmentType(ctx, transaction.getAdjustmentType());

                boolean ppsmMoveTransaction = false;
                boolean ppsmDeactivationTransaction = false;

                if (ppsmTransaction)
                {
                    supporterSubscriberMSISDN = supporteeExtension.getSupportMSISDN();
                }
                // Checking if it's a transaction during a move where the PPSM extension has not been copied yet.
                else if (supporteeExtension==null && ctx.get(MovableExtension.MOVED_SUBSCRIBER_CTX_KEY)!=null)
                {
                    Subscriber movedSubscriber = (Subscriber) ctx.get(MovableExtension.MOVED_SUBSCRIBER_CTX_KEY);
                    PPSMSupporteeSubExtension movedSupporteeExtension = PPSMSupporteeSubExtension.getPPSMSupporteeSubscriberExtension(ctx, movedSubscriber.getId());
                    ppsmMoveTransaction =  movedSupporteeExtension!=null && movedSupporteeExtension.supportsAdjustmentType(ctx, transaction.getAdjustmentType());
                    if (ppsmMoveTransaction)
                    {
                        supporterSubscriberMSISDN = movedSupporteeExtension.getSupportMSISDN();
                    }
                }
                // Checking if it's a transaction during a subscriber deactivation where the PPSM extension has already been removed.
                else if (supporteeExtension==null && ctx.get(PPSMSupporteeExtensionDeactivationHome.DEACTIVATED_SUPPORTEE_EXTENSION)!=null)
                {
                    PPSMSupporteeSubExtension deactivatedSupporteeExtension = (PPSMSupporteeSubExtension) ctx.get(PPSMSupporteeExtensionDeactivationHome.DEACTIVATED_SUPPORTEE_EXTENSION);
                    ppsmDeactivationTransaction =  subscriber.isInFinalState() && subscriber.getId().equals(deactivatedSupporteeExtension.getSubId()) && deactivatedSupporteeExtension.supportsAdjustmentType(ctx, transaction.getAdjustmentType());
                    if (ppsmDeactivationTransaction)
                    {
                        supporterSubscriberMSISDN = deactivatedSupporteeExtension.getSupportMSISDN();
                    }
                }
                
                if (ppsmTransaction || ppsmMoveTransaction || ppsmDeactivationTransaction)
                {
                    Subscriber supporterSubscriber = SubscriberSupport.lookupSubscriberForMSISDN(ctx, supporterSubscriberMSISDN);
                    if (supporterSubscriber!=null)
                    {
                        PPSMSupporterSubExtension supporterExtension = PPSMSupporterSubExtension.getPPSMSupporterSubscriberExtension(ctx, supporterSubscriber.getId());
                        if (supporterExtension.isEnabled())
                        {
                            ret = createPPSMTransaction(ctx, subscriber, supporterSubscriber, transaction); 
                        }
                    }
                    else
                    {
                        String subNote = "Postpaid Account not valid. PPSM Transaction failed for" + " adjType="
                            + transaction.getAdjustmentType() + " amount=" + transaction.getAmount()
                            + " type=" + transaction.getAction();
                        NoteSupportHelper.get(ctx).addSubscriberNote(ctx, subscriber.getId(), subNote, SystemNoteTypeEnum.ADJUSTMENT,
                                SystemNoteSubTypeEnum.PPSM);
                    }            
                }
                else
                {
                    ppsmInitialLogic.log(ctx);
                }
            }
            finally
            {
                ppsmLogicPm.log(ctx);
            }
        }

        if (ret == null)
        {
            ret = super.create(ctx, obj);
        }
        
        return ret;
    }
    
    private Object createPPSMTransaction(Context ctx, Subscriber supporteeSubscriber, Subscriber supporterSubscriber, Transaction supporteeTransaction) throws HomeException
    {
        Object result = null;
        final int supporterAdjustmentType = supporteeTransaction.getAdjustmentType();
        
      //TT#11112253045
        if(EnumStateSupportHelper.get(ctx).isOneOfStates(supporterSubscriber.getState(), AbstractSubscriberProvisioningCharger.getNonChargeableStates()))
        {
            String subNote = "Postpaid supported is not in a chargeable state";
            NoteSupportHelper.get(ctx).addSubscriberNote(ctx, supporteeSubscriber.getId(), subNote, SystemNoteTypeEnum.ADJUSTMENT,
                    SystemNoteSubTypeEnum.PPSM);
            final String message = "Postpaid Support Msisdn charge failed because Postpaid supporter is not in a chargeable state, prepaid=" + supporteeSubscriber.getMSISDN()
                    + ",postpaid=" + supporterSubscriber.getMSISDN();
            new InfoLogMsg(this, message, null).log(ctx);
            // if I don't throw an OcgTransactionException and if this transaction were an aux-service charge, profile will move to a suspended state. 
            throw new OcgTransactionException(message,com.redknee.product.s2100.ErrorCode.BALANCE_INSUFFICIENT);
        }

        /*
        final Service service = ServiceSupport.getServiceByAdjustment(ctx, supporteeTransaction.getAdjustmentType());
        if (service != null)
        {
            supporterAdjustmentType = service.getPPSMAdjustmentType();
        }
        else
        {
            supporterAdjustmentType = AdjustmentTypeSupportHelper.get(ctx).getAdjustmentTypeCodeByAdjustmentTypeEnum(ctx,
                    AdjustmentTypeEnum.PostpaidSupportRedirectCharges);
        }
        */

        try
        {
        
            Transaction transaction = (Transaction) supporteeTransaction.clone();
            transaction.setAdjustmentType(supporterAdjustmentType);
            transaction.setMSISDN(supporterSubscriber.getMSISDN());
            transaction.setAcctNum(supporterSubscriber.getBAN());
            transaction.setSubscriberID(supporterSubscriber.getId());
            transaction.setSubscriberType(supporterSubscriber.getSubscriberType());
            
          //To set recieve date one day prior to billing date in case of recurring recharge of prepaid(suportee) subscriber. 
            if(ctx.get(PPSM_BILLING_DATE) != null) { 
                Date receiveDate = transaction.getReceiveDate(); 
                Calendar cal = Calendar.getInstance(); 
                cal.setTime(receiveDate); 
                CalendarSupportHelper.get(ctx).clearTimeOfDay(cal); 
                receiveDate = cal.getTime();   
  
                Long billingDateLongValue = (Long)ctx.get(PPSM_BILLING_DATE); 
                Date billingDate = new Date(billingDateLongValue.longValue()); 
                cal.setTime(billingDate); 
                CalendarSupportHelper.get(ctx).clearTimeOfDay(cal); 
                billingDate = cal.getTime();   
  
                if(billingDate !=null && billingDate.equals(receiveDate)){ 
                        receiveDate = CalendarSupportHelper.get(ctx).getDayBefore(billingDate); 
                        transaction.setReceiveDate(receiveDate); 
                } 
            } 

            
            // Keep supported subscriber id in case it's already a supported transaction.
            if (transaction.getSupportedSubscriberID()==null || transaction.getSupportedSubscriberID().trim().isEmpty())
            {
                transaction.setSupportedSubscriberID(supporteeSubscriber.getId());
            }
            
            // reset some property, so TransactionHome will do it
            // transactionPostp.setTaxPaid(0);
            // transactionPostp.setGLCode("");
            
            String note = "PPSM Charge, " + supporteeSubscriber.getMSISDN() + "," + supporteeTransaction.getSubscriberID()
                    + ", adjType=" + supporteeTransaction.getAdjustmentType();
            if (supporteeTransaction.getCSRInput()!=null && !supporteeTransaction.getCSRInput().isEmpty())
            {
                note = supporteeTransaction.getCSRInput() + "\n" + note;
            }
            note = note.substring(0, Math.min(Transaction.CSRINPUT_WIDTH - 1, note.length()));
            transaction.setCSRInput(note);
            
            // TODO, any currency issues here
            // ret = super.create(obj);
            // we can't call super here.
            // Make sure no recursive from home pipe line
            
            final Context subCtx = ctx.createSubContext();
            subCtx.put(Subscriber.class, supporterSubscriber);
            final Home home = (Home) ctx.get(TransactionHome.class);
            result = home.create(subCtx, transaction);
        }
        catch (final OcgTransactionException e)
        {
            String subNote = "Exception when charging postpaid account." + e;
            NoteSupportHelper.get(ctx).addSubscriberNote(ctx, supporteeSubscriber.getId(), subNote, SystemNoteTypeEnum.ADJUSTMENT,
                    SystemNoteSubTypeEnum.PPSM);
            new MajorLogMsg(this, "Postpaid Support Msisdn charge failed, prepaid=" + supporteeSubscriber.getMSISDN()
                + ",postpaid=" + supporterSubscriber.getMSISDN(), e).log(ctx);
            throw e;
        }
        catch (final CloneNotSupportedException e)
        {
            String subNote = "Exception when charging postpaid account." + e;
            NoteSupportHelper.get(ctx).addSubscriberNote(ctx, supporteeSubscriber.getId(), subNote, SystemNoteTypeEnum.ADJUSTMENT,
                    SystemNoteSubTypeEnum.PPSM);
            new MajorLogMsg(this, "Exception", e).log(ctx);
            throw new HomeException("Cannot clone transaction !", e);
        }
        finally
        {
            final int succeed;
            if (result != null)
            {
                succeed = 1;
            }
            else
            {
                succeed = 0;
            }
            logRedirectER(supporteeSubscriber.getMSISDN(), supporteeSubscriber.getBAN(), supporteeTransaction.getAdjustmentType(),
                    supporterSubscriber.getMSISDN(), supporterSubscriber.getBAN(), supporterAdjustmentType,
                    supporteeTransaction.getAmount(), succeed);
        }
        
        return result;
    }


    /**
     * Looks up a duplicate VRA transaction.
     *
     * @param ctx
     *            The operating context.
     * @param ban
     *            Account number.
     * @param msisdn
     *            Subscriber mobile number.
     * @param adjustmentType
     *            Adjustment type of the transaction.
     * @param amount
     *            Amount to be charged.
     * @param dateTobeChecked
     *            Date of the transaction.
     * @return Whether there is one or more duplicate transactions.
     * @throws HomeException
     *             Thrown if there are problems verifying the transaction.
     */
    private Transaction findDuplicateTransaction(final Context ctx, final String subscriberId, final String msisdn,
        final int adjustmentType, final long amount, final Date dateTobeChecked, final long balance, String extTxnId) throws HomeException
    {
        final And andPredicate = new And();
        andPredicate.add(new EQ(TransactionXInfo.SUBSCRIBER_ID, subscriberId));
        andPredicate.add(new EQ(TransactionXInfo.TRANS_DATE, dateTobeChecked));
        andPredicate.add(new EQ(TransactionXInfo.MSISDN, msisdn));
        andPredicate.add(new EQ(TransactionXInfo.ADJUSTMENT_TYPE, Integer.valueOf(adjustmentType)));
        andPredicate.add(new EQ(TransactionXInfo.AMOUNT, Long.valueOf(amount)));
        andPredicate.add(new EQ(TransactionXInfo.BALANCE, Long.valueOf(balance)));
        if(extTxnId != null && 
                !extTxnId.isEmpty())
        {
            andPredicate.add(new EQ(TransactionXInfo.EXT_TRANSACTION_ID, extTxnId));
        }
        return CoreTransactionSupportHelper.get(ctx).findDuplicateTransaction(ctx, andPredicate);
    }

    /**
     * ER is not requested from FS
     *
     * @param prepaidMsisdn
     * @param prepaidBAN
     * @param prepaidAdj
     * @param postpaidMsisdn
     * @param postpaidBAN
     * @param postpaidAdj
     * @param amount
     * @param suceeded
     */
    void logRedirectER(final String prepaidMsisdn, final String prepaidBAN, final int prepaidAdj,
        final String postpaidMsisdn, final String postpaidBAN, final int postpaidAdj, final long amount,
        final int suceeded)
    {
        // TODO
    }
}
