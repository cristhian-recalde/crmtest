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
package com.trilogy.app.crm.transfer.contract.tfa;

import java.util.ArrayList;
import java.util.List;

import com.trilogy.app.crm.transfer.TransferType;
import com.trilogy.app.crm.transfer.contract.TransferContractException;
import com.trilogy.app.crm.transfer.contract.TransferTypeFacade;
import com.trilogy.app.transferfund.rmi.api.transfertype.TransferTypeService;
import com.trilogy.app.transferfund.rmi.data.AuthCredentials;
import com.trilogy.app.transferfund.rmi.exception.ContractProvisioningException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * RMI implementation of the RMI client for transfer types on TFA.
 * @author arturo.medina@redknee.com
 *
 */
public class RMITransferTypeImpl implements TransferTypeFacade
{

    /**
     * {@inheritDoc}
     */
    public List<TransferType> retrieveTransferType(Context ctx, int spid)
            throws TransferContractException
    {
        final AuthCredentials credentials = TransferContractSupport.getCredentials(ctx);
        
        final TransferTypeService service = getService(ctx);
        final List<TransferType> contracts = new ArrayList<TransferType>();
        
        try
        {
            com.redknee.app.transferfund.rmi.data.TransferType[] types = 
                service.retrieveTransferTypeId(credentials, spid, TransferContractSupport.getErReference(ctx));
            
            for (int i = 0; i < types.length; i++)
            {
                if (types[i] != null)
                {
                    contracts.add(adaptTransferType(ctx, types[i]));
                }
            }

        }
        catch (ContractProvisioningException e)
        {
            LogSupport.major(ctx, this, "ContractProvisioningException when trying to get the Transfer type for spid "
                    + spid
                    + " error code : "
                    + e.getResponseCode());
            
            if (e.getResponseCode() != TransferContractSupport.ERR_TRANSFERID_NOT_EXIST)
            {
                throw new TransferContractException(
                        TransferContractSupport.mapErrorCodeWithMessage(e.getResponseCode()),e);
            }
        }

        return contracts;
    }

    /**
     * Adapts the Transfer type from TFA bean into the transfer type from CRM.
     * @param ctx the operating context
     * @param transferType the source to adapt
     * @return the CRM transfer type bean
     */
    private TransferType adaptTransferType(Context ctx,
            com.redknee.app.transferfund.rmi.data.TransferType transferType)
    {
        TransferType type = new TransferType();
        
        type.setIdentifier(transferType.getTransferTypeId());
        type.setDescription(transferType.getDescription());
        type.setContributorTypeID(transferType.getContributorSubscriptionType());
        type.setRecipientTypeID(transferType.getRecipientSubscriptionType());
        
        return type;
    }

    /**
     * Returns the RMI service configured.
     * @param ctx the operating context
     * @return the RMI service.
     */
    private TransferTypeService getService(Context ctx)
    {
        final TransferTypeService service =
            (TransferTypeService)ctx.get(TransferTypeService.class);
        return service;
    }

}
