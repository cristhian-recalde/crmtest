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
package com.trilogy.app.crm.client.urcs;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.TimeZone;

import com.trilogy.app.crm.bean.CurrencyPrecision;
import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.client.AbstractCrmClient;
import com.trilogy.app.crm.client.RemoteServiceException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.product.bundle.manager.charging.v4_0.Account;
import com.trilogy.product.bundle.manager.charging.v4_0.Amount;
import com.trilogy.product.bundle.manager.charging.v4_0.RequestAmountsHolder;
import com.trilogy.product.bundle.manager.charging.v4_0.param.Parameter;
import com.trilogy.product.bundle.manager.charging.v4_0.param.ParameterID;
import com.trilogy.product.bundle.manager.charging.v4_0.param.ParameterSetHolder;
import com.trilogy.product.bundle.manager.charging.v4_0.param.ParameterValue;

/**
 * @author sbanerjee
 *
 */
public class AccountOperationsClientV4Impl 
    extends AbstractCrmClient<Account>
    implements AccountOperationsClientV4
{

    private static final Class<Account> SERVICE_TYPE = Account.class;
    private static final String URCS_SERVICE_NAME = "AccountOperationsClientV4";
    private static final String URCS_SERVICE_DESCRIPTION = "CORBA service for CPS Account Balance and Bundle Management";

    public AccountOperationsClientV4Impl(Context ctx)
    {
        super(ctx, URCS_SERVICE_NAME, URCS_SERVICE_DESCRIPTION, SERVICE_TYPE);
    }


    @Override
    public BundleTopupResponse topupBundle(Context ctx, String msisdn,
            TimeZone subscriberTz, String currency, BundleProfile bundleProfile, String erReference) 
                throws RemoteServiceException
    {
        int rc = -1;
        try
        {
            /*
             * This can be null only if the service IOR was not available 
             * due to service not available. Will land into the catch-all
             * below
             */
            Account accStub = getClient(ctx);
            
            CurrencyPrecision precision = (CurrencyPrecision) ctx.get(CurrencyPrecision.class);
            if (precision == null)
            {
                String msg = "Cannot find currency precision object in the context.";
                LogSupport.minor(ctx, this,
                  msg);
                throw new RemoteServiceException(rc,msg);
            }
            int storagePrecision = precision.getStoragePrecision();
    
            Amount[] amounts = new Amount[1];
            amounts[0] = new Amount(bundleProfile.getInitialBalanceLimit(), 
                    com.redknee.product.bundle.manager.charging.v4_0.UnitType.from_int(
                            bundleProfile.getType()), storagePrecision);
            
            /*
             * TODO Add params:
             *  APPLICATION_ID          (O) - TBD (need more info)
             *  EXPIRY_EXTENSION        (O) - DONE!
             *  IN_OPT_SUBSCRIPTIONTYPE (O) - TBD (need more info)
             *  UPDATE_EXPIRY           (O) - DONE!
             * 
             */
            Parameter[] inParamSet = new Parameter[2];
            final ParameterValue paramValueExpiryExtn = new ParameterValue();
            paramValueExpiryExtn.intValue(bundleProfile.getExpiryExtensionOnRepurchase());
            inParamSet[0] = new Parameter(ParameterID.EXPIRY_EXTENSION, 
                    paramValueExpiryExtn);
            final ParameterValue paramValueUpdateExpiry = new ParameterValue();
            paramValueUpdateExpiry.booleanValue(bundleProfile.isRepurchasable());
            inParamSet[1] = new Parameter(ParameterID.UPDATE_EXPIRY,
                    paramValueUpdateExpiry );
            
            ParameterSetHolder outParamSet = new ParameterSetHolder(
                    new Parameter[]{}
                    );
            
            RequestAmountsHolder newBalance = new RequestAmountsHolder(
                    Arrays.copyOf(amounts, amounts.length));
            
            rc = accStub.topupBundle(msisdn, currency, amounts, 
                    bundleProfile.getBundleCategoryId(), 
                        bundleProfile.getBundleId(), 
                        "BSS", inParamSet, outParamSet, newBalance);
            
            String newExpiryDate = null;
            for(Parameter param: outParamSet.value)
            {
                if(param.parameterID == ParameterID.NEW_EXPIRY)
                {
                    newExpiryDate = param.value.stringValue();
                }
            }
            
            String msg = MessageFormat.format(
                    "Received value {0} as New-Expiry for bundle: {1}, subscriber {2},  from URCS/CPS.", 
                        new Object[]{newExpiryDate, Long.valueOf(bundleProfile.getBundleId()), msisdn});
            if(LogSupport.isDebugEnabled(ctx))
                LogSupport.debug(ctx, this, msg);
            
            /*
             * If URCS/CPS returned no value, return;
             */
            if(newExpiryDate==null || newExpiryDate.isEmpty())
                return new BundleTopupResponse(rc, 
                        newBalance.value!=null && newBalance.value.length>0 ? newBalance.value[0].value : -1,
                                null);
            
            Calendar calendar = UrcsDateExtensionSupport.getUpdatedExpiryDate(ctx, 
                    Calendar.getInstance(subscriberTz), newExpiryDate);
            
            return new BundleTopupResponse(rc, 
                    newBalance.value!=null && newBalance.value.length>0 ? newBalance.value[0].value : -1,
                            calendar);
        } 
        catch (Throwable th)
        {
            throw new RemoteServiceException(rc, "Could not invoke 'topupBundle' on remote service", th);
        }
    }


    @Override
    public boolean isAlive(Context ctx)
    {
        try
        {
            return getClient(ctx) != null;
        } 
        catch (RemoteServiceException e)
        {
            return false;
        }
    }
}