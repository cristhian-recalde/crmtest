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
package com.trilogy.app.crm.external.vra;
import java.rmi.RemoteException;

import com.trilogy.app.crm.client.AbstractCrmClient;
import com.trilogy.app.vra.interfaces.MsisdnCheckInput;
import com.trilogy.app.vra.interfaces.MsisdnCheckOutput;
import com.trilogy.app.vra.interfaces.SMSCheckInput;
import com.trilogy.app.vra.interfaces.SMSCheckOutput;
import com.trilogy.app.vra.interfaces.VoucherRechargeInput;
import com.trilogy.app.vra.interfaces.VoucherRechargeOutput;
import com.trilogy.app.vra.interfaces.VoucherRedemptionException;
import com.trilogy.app.vra.interfaces.VoucherRedemptionService;
import com.trilogy.framework.xhome.context.Context;

/**
 * RMI Client for services of Voucher Redemption
 * 
 * @author Marcio Marques
 * @since 9.1.2
 */
public class VoucherRedemptionServiceRmiClient extends AbstractCrmClient<VoucherRedemptionService> implements VoucherRedemptionService
{

    private static final String SERVICE_NAME = "VoucherRedemptionServiceClient";
    private static final String SERVICE_DESCRIPTION = "RMI client for VRA services";
    private static final Class<VoucherRedemptionService> RMI_CLIENT_KEY = VoucherRedemptionService.class;
    public static final short RMI_COMM_FAILURE = 301;


    /**
     * 
     * @param ctx
     */
    public VoucherRedemptionServiceRmiClient(Context ctx)
    {
        super(ctx, SERVICE_NAME, SERVICE_DESCRIPTION, RMI_CLIENT_KEY);
    }


    @Override
    public SMSCheckOutput smsNotification(SMSCheckInput arg0) throws VoucherRedemptionException
    {
        VoucherRedemptionService service = getService();
        if (service != null)
        {
            try
            {
                return service.smsNotification(arg0);
            }
            catch (RemoteException e)
            {
                throw new VoucherRedemptionException("Unable to send sms notification: " + e.getMessage(), e);
            }
        }
        else
        {
            throw new VoucherRedemptionException("Unable to send sms notification: RMI communication failure");
        }
    }

    @Override
    public VoucherRechargeOutput voucherRedeem(VoucherRechargeInput arg0) throws VoucherRedemptionException
    {
        VoucherRedemptionService service = getService();
        if (service != null)
        {
            try
            {
                return service.voucherRedeem(arg0);
            }
            catch (RemoteException e)
            {
                throw new VoucherRedemptionException("Unable to redeem voucher: " + e.getMessage(), e);
            }
        }
        else
        {
            throw new VoucherRedemptionException("Unable to redeem voucher: RMI communication failure");
        }
    }


    @Override
    public MsisdnCheckOutput MSISDNcheck(MsisdnCheckInput arg0) throws VoucherRedemptionException
    {
        VoucherRedemptionService service = getService();
        if (service != null)
        {
            try
            {
                return service.MSISDNcheck(arg0);
            }
            catch (RemoteException e)
            {
                throw new VoucherRedemptionException("Unable to check MSISDN: " + e.getMessage(), e);
            }
        }
        else
        {
            throw new VoucherRedemptionException("Unable to check MSISDN: RMI communication failure");
        }
    }


    @Override
    public short isAlive(String arg0) throws VoucherRedemptionException
    {
        VoucherRedemptionService service = getService();
        if (service != null)
        {
            try
            {
                return service.isAlive(arg0);
            }
            catch (RemoteException e)
            {
                throw new VoucherRedemptionException("Unable to check if service is alive: " + e.getMessage(), e);
            }
        }
        else
        {
            throw new VoucherRedemptionException("Unable to check if service is alive: RMI communication failure");
        }
    }

}
