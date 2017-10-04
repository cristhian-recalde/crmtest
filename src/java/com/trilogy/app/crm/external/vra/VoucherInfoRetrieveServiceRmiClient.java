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
import com.trilogy.app.vra.interfaces.AuthHolder;
import com.trilogy.app.vra.interfaces.RMIVoucherInfoRetrieveService;
import com.trilogy.app.vra.interfaces.VoucherDetailsReturnObject;
import com.trilogy.app.vra.interfaces.VoucherInfoRetrieveInput;
import com.trilogy.app.vra.interfaces.VoucherInfoRetrieveService;
import com.trilogy.app.vra.interfaces.VoucherInfoRetrieveServiceException;
import com.trilogy.app.vra.interfaces.VoucherInfoRetrieveServiceInternalException;
import com.trilogy.framework.xhome.context.Context;

/**
 * @author sbanerjee
 *
 */
public class VoucherInfoRetrieveServiceRmiClient extends AbstractCrmClient<RMIVoucherInfoRetrieveService>
    implements RMIVoucherInfoRetrieveService
{
    private static final String SERVICE_NAME = "VoucherInfoRetrieveServiceClient";
    private static final String SERVICE_DESCRIPTION = "RMI client for Voucher Information Retrieval";
    private static final Class<RMIVoucherInfoRetrieveService> RMI_CLIENT_KEY = RMIVoucherInfoRetrieveService.class;
    public static final short RMI_COMM_FAILURE = 301;

    public VoucherInfoRetrieveServiceRmiClient(Context ctx)
    {
        super(ctx, SERVICE_NAME, SERVICE_DESCRIPTION, RMI_CLIENT_KEY);
    }
    
    @Override
    public VoucherDetailsReturnObject getVoucherInfoByVoucherNum(
            AuthHolder auth, VoucherInfoRetrieveInput input)
            throws VoucherInfoRetrieveServiceException,
            RemoteException, VoucherInfoRetrieveServiceInternalException
    {
    	RMIVoucherInfoRetrieveService service = getService();
        if (service != null)
        {
            return service.getVoucherInfoByVoucherNum(auth, input);
        }
        else
        {
            throw new VoucherInfoRetrieveServiceException("Unable to retrieve voucher info: RMI communication failure");
        }
    }
}
