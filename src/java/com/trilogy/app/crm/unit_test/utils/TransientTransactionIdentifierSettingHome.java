package com.trilogy.app.crm.unit_test.utils;

import com.trilogy.app.crm.bean.IdentifierEnum;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.bean.payment.PaymentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

/**
 * Home decorator that updates the Transient Identifier Setting Home for Transactions
 * @author angie.li
 *
 */
public class TransientTransactionIdentifierSettingHome extends TransientIdentifierSequenceHome
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new TransientTransactionIdentifierSettingHome.
     *
     * @param ctx
     *            The operation context.
     * @param delegate
     *            The Home to which we delegate.
     * @exception HomeException
     *                Thrown if there are problems accessing Home data in the context used
     *                to set-up this home.
     */
    public TransientTransactionIdentifierSettingHome(final Context ctx, final Home delegate) throws HomeException
    {
        super(ctx, delegate, IdentifierEnum.TRANSACTION_ID);
    }


    /**
     * Set a new Transaction Id every time a create is called. 
     */
    public Object create(final Context ctx, final Object obj) throws HomeException
    {
        Transaction transaction = (Transaction) obj;
        
        final long identifier = getNextIdentifier(ctx);
        transaction.setReceiptNum(identifier);
        
        return super.create(ctx, transaction);
    }
} // class
