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
package com.trilogy.app.crm.home.transaction;

import com.trilogy.app.crm.bean.TaxAuthority;
import com.trilogy.app.crm.bean.TaxAuthorityTypeEnum;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.bean.core.Msisdn;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.support.TaxationMethodSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * @author bdhavalshankh
 * @since 9.5.5 for Canadian tax support feature enhancement - Optimization on invoice generation 
 * TT#12112702058
 */
public class TransactionTaxAuthoritySettingHome extends HomeProxy
{
    /**
     * Creates a new decorator for the given Home delegate.
     *
     * @param context The operating context.
     * @param delegate The Home to which this decorator delegates.
     */
    public TransactionTaxAuthoritySettingHome(final Context context, final Home delegate)
    {
        super(context, delegate);
    }


    /**
     * {@inheritDoc}
     */
    public Object create(final Context context, final Object obj)
        throws HomeException
    {
        Transaction transaction = (Transaction)obj;
        int adjType = transaction.getAdjustmentType();
        TaxAuthority taxAuth =  AdjustmentTypeSupportHelper.get(context).
                getTaxAuthorityForSpid(context, adjType, transaction.getSpid());
        
        if(taxAuth != null)
        {
            if(TaxAuthorityTypeEnum.INTERNAL.equals(taxAuth.getTaxAuthorityType()))
            {
                transaction.setTaxAuthority(taxAuth.getTaxId());
            }
            else if(TaxAuthorityTypeEnum.EXTERNAL.equals(taxAuth.getTaxAuthorityType()))
            {
                String msisdn = transaction.getMSISDN();
                if(msisdn != null)
                {
                    LogSupport.info(context, this, "Found external tax authority associated with adjustment type id : "+adjType+" for " +
                            " transaction with MSISDN : "+msisdn+" . Going to resolve external to internal" +
                            " tax authority... ");
                    
                    Context subCtx = context.createSubContext();
                    subCtx.put(Msisdn.class, msisdn);
                    int resolvedTaxAuthority = TaxationMethodSupport.resolveExternalTaxAuthority(subCtx, taxAuth);
                    transaction.setTaxAuthority(resolvedTaxAuthority);
                }
            }
            if(LogSupport.isDebugEnabled(context))
            {
                LogSupport.debug(context, this, "Setting tax authority id "+transaction.getTaxAuthority()+" " +
                        "for transaction with receipt number "+transaction.getReceiptNum());
            }
        }
        return super.create(context, transaction);
    }

} // class
