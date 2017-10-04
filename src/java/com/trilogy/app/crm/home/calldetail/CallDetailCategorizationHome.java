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
package com.trilogy.app.crm.home.calldetail;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.BillingOptionMapping;
import com.trilogy.app.crm.bean.calldetail.AbstractCallDetail;
import com.trilogy.app.crm.bean.calldetail.CallCategorization;
import com.trilogy.app.crm.bean.calldetail.CallDetail;
import com.trilogy.app.crm.support.CallDetailSupportHelper;


/**
 * Categorizes call detail.
 *
 * @author paul.sperneac@redknee.com
 */
public class CallDetailCategorizationHome extends HomeProxy
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;


    /**
     * Create a new instance of <code>CallDetailCategorizationHome</code>.
     *
     * @param delegate
     *            The delegate of this home.
     */
    public CallDetailCategorizationHome(final Home delegate)
    {
        super(delegate);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object create(final Context ctx, final Object obj) throws HomeException
    {
        final CallDetail cd = categorizeCall(ctx, (CallDetail) obj);

        return super.create(ctx, cd);
    }


    /**
     * Add categorization information into a call detail.
     *
     * @param ctx
     *            The operating context.
     * @param cdr
     *            Call detail record.
     * @return A categorized call detail record.
     */
    private CallDetail categorizeCall(final Context ctx, final CallDetail cdr)
    {
        final CallCategorization cc = (CallCategorization) ctx.get(CallCategorization.class);

        if (cc == null)
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                new InfoLogMsg(this, "Cannot find CallCategorization service in context", null).log(ctx);
            }

            return cdr;
        }


        final BillingOptionMapping opt = cc.categorizeCall(ctx, cdr);
        StringBuilder msg = new StringBuilder(" Call categorization ");
        if (opt != null)
        {
            cdr.setTaxAuthority1(opt.getTaxAuthority());
            cdr.setTaxAuthority2(opt.getTaxAuthority2());
            
            if (cdr.getBillingCategory() == AbstractCallDetail.DEFAULT_BILLINGCATEGORY)
            {
                cdr.setBillingCategory(opt.getBillingCategory());
            }

            cdr.setUsageType(opt.getUsageType());
            msg.append(" Tax Authority 1 => ");
            msg.append(cdr.getTaxAuthority1());
            msg.append(" Tax Authority 2 => ");
            msg.append(cdr.getTaxAuthority2());
            msg.append(" Billing Category => ");
            msg.append(cdr.getBillingCategory());
            msg.append(" UsageType => ");
            msg.append(cdr.getUsageType());

        }
        else
        {
            msg.append(" Billing Option Mapping is NULL");
        }
        CallDetailSupportHelper.get(ctx).debugMsg(CallDetailCategorizationHome.class, cdr, msg.toString(), ctx);
        
        return cdr;
    }

}
