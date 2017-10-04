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

import com.trilogy.app.crm.transfer.ContractGroup;
import com.trilogy.framework.xhome.context.Context;

/**
 * This interface holds all logic for Transfer contract groups on CRM.
 * @author arturo.medina@redknee.com
 *
 */
public interface TransferContractGroupFacade
{
    /**
     * Creates a Transfer Contract.
     * @param ctx the operating context
     * @param contract the contract to
     * @return the transfer contract
     * @throws TransferContractException 
     */
    public ContractGroup createContractGroup(Context ctx, ContractGroup contract)
    throws TransferContractException;

    /**
     * Returns a CRM Transfer contract from whatever source it needs.
     * @param ctx the operating context
     * @param groupId the group identifier
     * @return the transfer contract
     * @throws TransferContractException 
     * @throws ContractGroupNotFoundException 
     */
    public ContractGroup retrieveContractGroup(Context ctx, long groupId)
    throws TransferContractException, ContractGroupNotFoundException;

    /**
     * Returns a list of Contract based on an Owner.
     * @param ctx the operating context
     * @param owner the owner Identifier
     * @return A list of Transfer contracts
     * @throws TransferContractException 
     * @throws TransferContractOwnerNotFoundException 
     */
    public List<ContractGroup> retrieveContractGroupByOwner(Context ctx, String owner)
    throws TransferContractException, TransferContractOwnerNotFoundException;

    /**
     * Deletes a particular Contract in the external segment.
     * @param ctx the operating context
     * @param ownerId the contract Identifier to delete
     * @throws TransferContractException 
     * @throws TransferContractOwnerNotFoundException 
     */
    public void deleteOwner(Context ctx, String ownerId)
    throws TransferContractException, TransferContractOwnerNotFoundException;

    /**
     * Deletes all the contracts from a particular owner.
     * @param ctx
     * @param contractId
     * @throws TransferContractException 
     * @throws ContractGroupNotFoundException 
     */
    public void deleteContractGroup(Context ctx, long contractId)
    throws TransferContractException, ContractGroupNotFoundException;

}
