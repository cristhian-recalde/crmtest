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
package com.trilogy.app.crm.transfer.contract;

import java.util.List;

import com.trilogy.app.crm.transfer.TransferType;
import com.trilogy.framework.xhome.context.Context;

/**
 * Interface that contains all the business logic for Transfer types.
 * @author arturo.medina@redknee.com
 *
 */
public interface TransferTypeFacade
{
    /**
     * retrieves all the transfer types from any transfer fund external service and maps it to CRM's Transfer types
     * @param context the operating context
     * @param spid the spit to search for
     * @return the list of CRM's transfer types
     * @throws TransferContractException
     */
    public List<TransferType> retrieveTransferType(Context context, int spid) throws TransferContractException;
}
