/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home;

import com.trilogy.framework.core.locale.Currency;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.PayeeEnum;
import com.trilogy.app.crm.bean.SystemNoteSubTypeEnum;
import com.trilogy.app.crm.bean.SystemNoteTypeEnum;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.support.NoteSupport;
import com.trilogy.app.crm.support.NoteSupportHelper;


/**
 * Provides a decorator for the Transaction Home pipeline that, upon creation, ensures
 * that the transaction is applicable to the Payee. We need to add this for a special case
 * that exists, where the Pool-Leader-Subscriber is virtual and hence not chargeable. In
 * future, we must remove this other wise we will never ablet o charge pooled-items, like
 * bundle-fee, over-usage etc
 * 
 * <b>ATTENTION ATTENTION ATTENTION !!!!</b> PLEASE NOTE THAT THIS DECORATION IS JUST
 * MAKING UP FOR DESIGN/REQUIREMNET LIMITATION. POOLS EVENTHOUGH MODELLED AS HIDDEN SUBS,
 * SHOULD BE CHARGEABLE AND SOON THIS DECORATION SHOULD CEASE TO EXIST. IT IS VERY
 * IMPORTANT FOR POSTPAID POOLS TO NOT HAVE ANY CHARGE/OWING OTHERWISE WE WOULD NOT BE
 * ABLE TO PROCESSS PAYMENTS PROPERLY AS THE PAYMENT SPLITS (PRO-RATED). SINCE WE CANNOT
 * ASK FOR PAYMENTS FROM HIDDEN SUB, THEY SHOULD HAVE NO OWING
 * 
 * 
 * @author siamr.singh@redknee.com
 */
public class TransactionNonApplicableSubscrberHome extends HomeProxy
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    /**
     * Creates a new decorator for the given Home delegate.
     * 
     * @param context
     *            The operating context.
     * @param delegate
     *            The Home to which this decorator delegates.
     */
    public TransactionNonApplicableSubscrberHome(final Context context, final Home delegate)
    {
        super(context, delegate);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object create(final Context context, final Object obj) throws HomeException
    {
        if (obj instanceof Transaction)
        {
            final Transaction transaction = (Transaction) obj;
            if (!ensureApplicable(context, transaction))
            {
                if (LogSupport.isDebugEnabled(context))
                {
                    new DebugLogMsg(this, "TRANSACTION [" + transaction.toString()
                            + "] connot be passe further in the pipeline.", null).log(context);
                }
                // this is bad - but we do not want any transaction to show up for the
                // Pooled Subscription. Hence we will just eat it up.
                // I am not returning an exception because i do not want callers to adjust
                // for this nasty non-chargeable pool limitation. The limitation will
                // go away soon with a proper design that scopes it in (Accout Charges)
                return obj;
            }
        }
        return super.create(context, obj);
    }


    /**
     * Ensures that a Transaction can is applicable to the Target (Subscriber) If
     * Transaction cannot be processed and a meaningful action has been taken on its
     * account. If true, simply continue procession the transaction, it will leave
     * Transaction records inconsistent However, since the intention to commit transaction
     * may be important and hence may be recorded (as an account note)
     * 
     * @param context
     *            The operating context.
     * @param transaction
     *            The transaction to check and configure.
     * 
     * @exception HomeException
     *                Thrown if there are problems accessing the AdjustmentType Home data
     *                in the context.
     */
    private boolean ensureApplicable(final Context context, final Transaction transaction) throws HomeException
    {
        if (LogSupport.isDebugEnabled(context))
        {
            new DebugLogMsg(context, "Ensuring Transaction is chargeable", null).log(context);
        }
        if (PayeeEnum.Subscriber.equals(transaction.getPayee()))
        {
            Account account = (Account) context.get(Account.class);
            if (account == null || account.getBAN() == null
                || !SafetyUtil.safeEquals(account.getBAN().trim(), transaction.getBAN().trim()))
            {
                account = AccountSupport.getAccount(context, transaction.getBAN());
            }
            
            if (account.isPooled(context))
            {
                /*
                 * Rejects Transaction to a Payee of Type Subscriber if he is postpaid
                 * Pool-Virtual-Sub. This check should eventually get removed when when we
                 * handle charging to pools properly
                 */
                handleNonCharegeableTransaction(context, transaction);
                if (LogSupport.isDebugEnabled(context))
                {
                    new DebugLogMsg(context, "Transaction is not chargeable", null).log(context);
                }
                return false;
            }
        }
        if (LogSupport.isDebugEnabled(context))
        {
            new DebugLogMsg(context, "Transaction is accepatable", null).log(context);
        }
        
        return true;
    }


    /**
     * Handle non-applicable transactions ( Add an Account Note)
     * 
     * @param context
     * @param transaction
     */
    private void handleNonCharegeableTransaction(final Context context, final Transaction transaction)
    {
        // logs a message on GUI
        // leaves an Account Note
        final Currency currency = (Currency) context.get(Currency.class, Currency.DEFAULT);
        String note = "ADJUSTMENT ["
                + AdjustmentTypeSupportHelper.get(context).getAdjustmentTypeName(context, transaction.getAdjustmentType())
                + "] of  AMOUNT[" + currency.formatValue(transaction.getAmount())
                + "] has been recorded in this note only. No transaction record exists.]";
        try
        {
            if (transaction.getAmount() != 0)
            {
                NoteSupportHelper.get(context).addAccountNote(context,
                        AccountSupport.getAccount(context, transaction.getBAN()).getBAN(), note,
                        SystemNoteTypeEnum.ADJUSTMENT, SystemNoteSubTypeEnum.ACCUPDATE);
            }
            new InfoLogMsg(this, note, null).log(context);
        }
        catch (HomeException e)
        {
            String errorMessage = "Could not add Note [" + note + " ] to Account [ " + transaction.getAcctNum() + "]";
            // TODO Auto-generated catch block
            new MajorLogMsg(this, errorMessage, null).log(context);
            new DebugLogMsg(this, errorMessage, e).log(context);
        }
    }
} // class
