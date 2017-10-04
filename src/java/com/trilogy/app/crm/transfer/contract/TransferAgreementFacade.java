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

import com.trilogy.app.crm.transfer.TransferAgreement;
import com.trilogy.framework.xhome.context.Context;

/**
 * Interface that contains all the business logic for transfer agreement.
 * @author arturo.medina@redknee.com
 *
 */
public interface TransferAgreementFacade
{
    /**
     * Creates a Transfer Contract.
     * @param ctx the operating context
     * @param agreement the contract to
     * @return the transfer contract
     * @throws TransferContractException 
     */
    public TransferAgreement createTransferAgreement(Context ctx, TransferAgreement agreement)
        throws TransferContractException;

    
    /**
     * Creates a Transfer Contract.
     * @param ctx the operating context
     * @param agreement the contract to
     * @return the transfer contract
     * @throws TransferContractException 
     */
    public TransferAgreement updateTransferContract(Context ctx, TransferAgreement agreement)
        throws TransferContractException;

    
    /**
     * Returns a CRM Transfer contract from whatever source it needs.
     * @param ctx the operating context
     * @param agreementId the contract id
     * @return the transfer contract
     * @throws TransferContractException 
     * @throws TransferContractNotFoundException 
     */
    public TransferAgreement retrieveAgreement(Context ctx, long agreementId)
    throws TransferContractException, TransferAgreementNotFoundException;

    /**
     * Returns a list of Contract based on an Owner.
     * @param ctx the operating context
     * @param agreementOwner the owner Identifier
     * @return A list of Transfer contracts
     * @throws TransferContractException 
     */
    public List<TransferAgreement> retrieveOwnerAgreements(Context ctx, String agreementOwner)
    throws TransferContractException;

    /**
     * Deletes a particular Contract in the external segment.
     * @param ctx the operating context
     * @param agreementId the contract Identifier to delete
     * @throws TransferContractException 
     */
    public void deleteAgreement(Context ctx,long agreementId)
    throws TransferContractException;

    /**
     * Deletes all the contracts from a particular owner.
     * @param ctx
     * @param owner
     * @throws TransferContractException 
     */
    public void deleteAgreementsByOwner(Context ctx,String owner)
    throws TransferContractException;
    
    
}
