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
import com.trilogy.app.vra.interfaces.PricePlanVoucherTypeMappingList;
import com.trilogy.app.vra.interfaces.PricePlanVoucherTypeMappingServiceException;
import com.trilogy.app.vra.interfaces.PricePlanVoucherTypeMappingServiceInternalException;
import com.trilogy.app.vra.interfaces.RMIPricePlanVoucherTypeMappingService;
import com.trilogy.framework.xhome.context.Context;

/**
 * RMI Client for services of Voucher Redemption
 * 
 * @author Marcio Marques
 * @since 9.1.2
 */
public class PricePlanVoucherTypeMappingServiceRmiClient extends AbstractCrmClient<RMIPricePlanVoucherTypeMappingService> implements RMIPricePlanVoucherTypeMappingService
{

    private static final String SERVICE_NAME = "PricePlanVoucherTypeMappingServiceClient";
    private static final String SERVICE_DESCRIPTION = "RMI client for PricePlan Voucher Type Mapping services";
    private static final Class<RMIPricePlanVoucherTypeMappingService> RMI_CLIENT_KEY = RMIPricePlanVoucherTypeMappingService.class;
    public static final short RMI_COMM_FAILURE = 301;


    /**
     * 
     * @param ctx
     */
    public PricePlanVoucherTypeMappingServiceRmiClient(Context ctx)
    {
        super(ctx, SERVICE_NAME, SERVICE_DESCRIPTION, RMI_CLIENT_KEY);
    }



    @Override
    public PricePlanVoucherTypeMappingList getMappingsForPricePlan(String arg0, String arg1, long arg2)
            throws PricePlanVoucherTypeMappingServiceException, PricePlanVoucherTypeMappingServiceInternalException, RemoteException
    {
    	RMIPricePlanVoucherTypeMappingService service = getService();
        if (service != null)
        {
            return service.getMappingsForPricePlan(arg0, arg1, arg2);
        }
        else
        {
            throw new PricePlanVoucherTypeMappingServiceException("Unable to get mappings for price plan: RMI communication failure");
        }
    }


    @Override
    public PricePlanVoucherTypeMappingList getMappingsForVoucherType(String arg0, String arg1, long arg2)
            throws PricePlanVoucherTypeMappingServiceException, PricePlanVoucherTypeMappingServiceInternalException, RemoteException
    {
    	RMIPricePlanVoucherTypeMappingService service = getService();
        if (service != null)
        {
            return service.getMappingsForVoucherType(arg0, arg1, arg2);
        }
        else
        {
            throw new PricePlanVoucherTypeMappingServiceException("Unable to get mappings for voucher type: RMI communication failure");
        }
    }

}
