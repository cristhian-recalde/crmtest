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

import org.omg.CORBA.LongHolder;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.calldetail.CallDetail;
import com.trilogy.app.crm.bean.core.SubscriptionType;
import com.trilogy.app.crm.client.AppOcgClient;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.CallDetailSupportHelper;
import com.trilogy.product.s2100.ErrorCode;


/**
 * @author skularajasingham
 * 
 */
public class CallDetailChargeOcgHome extends HomeProxy
{

    public CallDetailChargeOcgHome()
    {
    }


    public CallDetailChargeOcgHome(final Context ctx)
    {
        super(ctx);
    }


    public CallDetailChargeOcgHome(final Home delegate)
    {
        super(delegate);
    }


    public CallDetailChargeOcgHome(final Context ctx, final Home delegate)
    {
        super(ctx, delegate);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object create(final Context ctx, final Object obj)
            throws HomeException
    {
        if (obj instanceof CallDetail)
        {
            final CallDetail calldetail = (CallDetail) obj;

            final Account account = AccountSupport.getAccount(ctx, calldetail.getBAN());

            final AppOcgClient client = (AppOcgClient) ctx.get(ctx, AppOcgClient.class);
            if (client == null)
            {
                throw new HomeException("Create failed. Cannot find AppOcgClient in context.");
            }

            int result = 0;
            final LongHolder balance = new LongHolder(); // Susbscriber's balance after the transaction
            final boolean bBalFlag = calldetail.getSubscriberType() == SubscriberTypeEnum.PREPAID;
            final String erReference = "AppCrm-Er501-" + calldetail.getId();
            String currency = null;
            currency = account.getCurrency();
            if (calldetail.isChargeOCG())
            {
                final SubscriptionType INSubscriptionType = SubscriptionType.getINSubscriptionType(ctx);
                if (null == INSubscriptionType)
                {
                    throw new HomeException("No IN subscription type defined in system.");
                }

                final StringBuilder dataSentToOcg = new StringBuilder(" Data sent to OCG ");
                dataSentToOcg.append(" Charge => ");
                dataSentToOcg.append(calldetail.getCharge());
                dataSentToOcg.append(" erReference => ");
                dataSentToOcg.append(erReference);
                dataSentToOcg.append(" IN subscription type => ");
                dataSentToOcg.append(INSubscriptionType.getId());
                dataSentToOcg.append(" balance => ");
                dataSentToOcg.append(balance);

                CallDetailSupportHelper.get(ctx).debugMsg(CallDetailChargeOcgHome.class, calldetail, dataSentToOcg.toString(),
                        ctx);

                if (calldetail.getCharge() > 0)
                {
                    result = client.requestDebit(calldetail.getChargedMSISDN(), calldetail.getSubscriberType(),
                                    calldetail.getCharge(), currency, bBalFlag, erReference, 
                                    INSubscriptionType.getId(), balance);

                }
                else if (calldetail.getCharge() < 0)
                {
                    result = client.requestCredit(calldetail.getChargedMSISDN(), calldetail.getSubscriberType(),
                                    -calldetail.getCharge(), currency, bBalFlag, (short) 0, erReference,
                                    INSubscriptionType.getId(), null, balance);

                }
                //if success then set CallDetail.Comments = "Charge to OCG successful - <OCG result Code>".
                if (result == ErrorCode.NO_ERROR)
                {
                    calldetail.setComments("Charging OCG sucess");
                }
                else
                {
                    calldetail.setComments("Charging OCG failed");
                    new MinorLogMsg(this, "Charge to OCG successful - <OCG result Code: " + result + " >", null).log(ctx);
                }
            }

        }
        return getDelegate(ctx).create(ctx, obj);
    }

}
