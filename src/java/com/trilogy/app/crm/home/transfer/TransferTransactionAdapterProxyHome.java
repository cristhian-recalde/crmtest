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
package com.trilogy.app.crm.home.transfer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.bean.TransactionHome;
import com.trilogy.app.crm.bean.TransactionXInfo;
import com.trilogy.app.crm.support.TransferSupport;
import com.trilogy.app.crm.transfer.TransfersView;
import com.trilogy.app.crm.transfer.TransfersViewXInfo;

import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.home.AdapterHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.ReadOnlyHome;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.LogSupport;
/*
 * TODO: re-write this class to use HomeSupport
 */
public class TransferTransactionAdapterProxyHome
    extends AdapterHome
{
    public TransferTransactionAdapterProxyHome(Context ctx, Home delegate)
    {
        super(ctx, new ReadOnlyHome(delegate), new TransferTransactionAdapter());
    }

    public Object find(Context ctx, Object key)
        throws HomeException, HomeInternalException
    {
        // The trasfer is keyed on the external transaction number
        // we need to grab all the transactions with this external transaction number
        // and then adapt them to a Transfer bean
        EQ where = new EQ(TransactionXInfo.EXT_TRANSACTION_ID, key);
        Collection ret = getDelegate(ctx).select(ctx, where);

        return ( ret == null ) ?
            null :
            getAdapter().adapt(ctx, ret);
    }

    public Collection select(Context ctx, Object where)
        throws HomeException, HomeInternalException
    {
        And and = new And();
        and.add(where);
        and.add(new In(TransactionXInfo.ADJUSTMENT_TYPE, TransferSupport.getMMAdjustmentTypes(ctx)));
        Object newWhere = removePredicateFromWhere(ctx, and);

        // we have a list of all MM transactions
        Collection list = getDelegate(ctx).select(ctx, newWhere);

        // need to group all the entries by external transasction number before adapting them
        Collection <ArrayList<Transaction> > tranList = groupTransactionsByExtTransactionNum(ctx, list);
        Collection ret = new ArrayList();


        for(ArrayList<Transaction> l : tranList)
        {
            TransfersView tfa = (TransfersView)getAdapter().adapt(ctx, l);
            if(null != tfa)
            {
                ret.add(tfa);
            }
        }

        return ret;
    }

    public Visitor forEach(Context ctx, Visitor visitor, Object where)
        throws HomeException, HomeInternalException
    {
        throw new UnsupportedOperationException();
    }

    private Collection<ArrayList<Transaction> > groupTransactionsByExtTransactionNum(final Context ctx, final Collection list)
    {
        Hashtable<String, ArrayList<Transaction> > ret = new Hashtable<String, ArrayList<Transaction> >();
        Iterator i = list.iterator();
        while(i.hasNext())
        {
            Transaction t = (Transaction)i.next();
            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(getContext(), this, " Transaction: " + t);
            }
            
            String extTranId = t.getExtTransactionId();
            ArrayList<Transaction> tranCol = ret.get(extTranId);
            if(null == tranCol)
            {
                tranCol = new ArrayList<Transaction>();
                ret.put(extTranId, tranCol);
            }

            tranCol.add(t);
        }

        return ret.values();
    }
}