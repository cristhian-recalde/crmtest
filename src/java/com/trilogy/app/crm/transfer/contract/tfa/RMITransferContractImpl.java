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

import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.config.TFAClientConfig;
import com.trilogy.app.crm.transfer.ContractGroup;
import com.trilogy.app.crm.transfer.ParticipantTypeEnum;
import com.trilogy.app.crm.transfer.TransferContract;
import com.trilogy.app.crm.transfer.contract.ContractGroupNotFoundException;
import com.trilogy.app.crm.transfer.contract.TransferContractException;
import com.trilogy.app.crm.transfer.contract.TransferContractFacade;
import com.trilogy.app.crm.transfer.contract.TransferContractGroupFacade;
import com.trilogy.app.crm.transfer.contract.TransferContractNotFoundException;
import com.trilogy.app.crm.transfer.contract.TransferContractOwnerNotFoundException;
import com.trilogy.app.transferfund.rmi.api.transfercontract.TransferContractService;
import com.trilogy.app.transferfund.rmi.data.AuthCredentials;
import com.trilogy.app.transferfund.rmi.data.Contract;
import com.trilogy.app.transferfund.rmi.exception.ContractProvisioningException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * RMI implementation of the RMI client for transfer contracts on TFA.
 * @author arturo.medina@redknee.com
 *
 */
public class RMITransferContractImpl implements TransferContractFacade
{

    /**
     * {@inheritDoc}
     */
    public TransferContract createTransferContract(final Context ctx,
            TransferContract contract)
        throws TransferContractException
    {
        final AuthCredentials credentials = TransferContractSupport.getCredentials(ctx);
        Contract tfaContract = adaptTransferContract(ctx, contract);
        final TransferContractService service = getService(ctx);
        try
        {
            tfaContract = service.createTransferContract(credentials, tfaContract, TransferContractSupport.getErReference(ctx));
            contract = adaptContract(ctx, tfaContract);
        }
        catch (ContractProvisioningException e)
        {
            LogSupport.major(ctx, this, "ContractProvisioningException when trying to create the contract "
                    + contract.getIdentifier()
                    + " error code : "
                    + e.getResponseCode());
            
            throw new TransferContractException(
                    TransferContractSupport.mapErrorCodeWithMessage(e.getResponseCode()),e);
        }
        return contract;
    }


    /**
     * {@inheritDoc}
     */
    public void deleteTransferContract(final Context ctx, final long contractId)
        throws TransferContractException, TransferContractNotFoundException
    {
        final AuthCredentials credentials = TransferContractSupport.getCredentials(ctx);
        final TransferContractService service = getService(ctx);
        try
        {
            service.deleteTransferContract(credentials, contractId, TransferContractSupport.getErReference(ctx));
        }
        catch (ContractProvisioningException e)
        {
            LogSupport.major(ctx, this, "ContractProvisioningException when trying to delete the contract "
                    + contractId
                    + " error code : "
                    + e.getResponseCode());
            
            if (e.getResponseCode() == TransferContractSupport.ERR_CONTRACT_NOT_EXIST)
            {
                throw new TransferContractNotFoundException(
                        TransferContractSupport.mapErrorCodeWithMessage(e.getResponseCode()), e);
            }
            else
            {
                throw new TransferContractException(
                    TransferContractSupport.mapErrorCodeWithMessage(e.getResponseCode()),e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void deleteTransferContractsByOwner(final Context ctx,
            final String contractOwner)
        throws TransferContractException, TransferContractOwnerNotFoundException
    {
        final AuthCredentials credentials = TransferContractSupport.getCredentials(ctx);
        final TransferContractService service = getService(ctx);
        try
        {
            service.deleteAllTransferContracts(credentials, contractOwner, TransferContractSupport.getErReference(ctx));
        }
        catch (ContractProvisioningException e)
        {
            LogSupport.major(ctx, this, "ContractProvisioningException when trying to delete the contract owner"
                    + contractOwner
                    + " error code : "
                    + e.getResponseCode());
            if (e.getResponseCode() == TransferContractSupport.ERR_OWNER_NOT_EXISTS_CONTRACT_TABLE)
            {
                throw new TransferContractOwnerNotFoundException(
                        TransferContractSupport.mapErrorCodeWithMessage(e.getResponseCode()), e);
            }
            else
            {
                throw new TransferContractException(
                    TransferContractSupport.mapErrorCodeWithMessage(e.getResponseCode()),e);
            }
        }

    }

    /**
     * {@inheritDoc}
     */
    public TransferContract retrieveTransferContract(final Context ctx,
            final long contractId)
        throws TransferContractException, TransferContractNotFoundException
    {
        final AuthCredentials credentials = TransferContractSupport.getCredentials(ctx);
        final TransferContractService service = getService(ctx);
        TransferContract crmContract = null;
        Contract contract;
        try
        {
            contract = service.retrieveTransferContract(credentials, contractId, TransferContractSupport.getErReference(ctx));
            crmContract = adaptContract(ctx, contract);
        }
        catch (ContractProvisioningException e)
        {
            LogSupport.major(ctx, this, "ContractProvisioningException when trying to get the contract "
                    + contractId
                    + " error code : "
                    + e.getResponseCode());
            if (e.getResponseCode() == TransferContractSupport.ERR_CONTRACTID_NOTEXIST)
            {
                throw new TransferContractNotFoundException(
                        TransferContractSupport.mapErrorCodeWithMessage(e.getResponseCode()),e);
            }
            else
            {
                throw new TransferContractException(
                        TransferContractSupport.mapErrorCodeWithMessage(e.getResponseCode()),e);
            }

        }
        return crmContract;
    }

    /**
     * {@inheritDoc}
     */
    public List<TransferContract> retrieveTransferContractByOwner(final Context ctx,
            final String owner)
        throws TransferContractException, TransferContractOwnerNotFoundException
    {
        final AuthCredentials credentials = TransferContractSupport.getCredentials(ctx);
        final TransferContractService service = getService(ctx);
        final List<TransferContract> contracts = new ArrayList<TransferContract>();

        try
        {
            final Contract[] tfaContracts = service.retrieveTransferContractByOwner(credentials,
                    owner, TransferContractSupport.getErReference(ctx));
            for (int i = 0; i < tfaContracts.length; i++)
            {
                if (tfaContracts[i] != null)
                {
                    contracts.add(adaptContract(ctx, tfaContracts[i]));
                }
            }
        }
        catch (ContractProvisioningException e)
        {
            LogSupport.major(ctx, this, "ContractProvisioningException when trying to get the contract owner"
                    + owner
                    + " error code : "
                    + e.getResponseCode());
            
            if (e.getResponseCode() == TransferContractSupport.ERR_OWNER_NOT_EXISTS_CONTRACT_TABLE)
            {
                throw new TransferContractOwnerNotFoundException(
                        TransferContractSupport.mapErrorCodeWithMessage(e.getResponseCode()),e);
            }
            else
            {
                throw new TransferContractException(
                        TransferContractSupport.mapErrorCodeWithMessage(e.getResponseCode()),e);
            }
        }
        return contracts;
    }

    /**
     * Adapts the transfer contract bean from CRM into the Contract from TFA.
     * @param ctx the operating context
     * @param tContract the source to adapt
     * @return the tfa contract bean
     */
    public Contract adaptTransferContract(final Context ctx, final TransferContract tContract)
    {
        final Contract contract = new Contract();

        contract.setContractId(tContract.getIdentifier());
        contract.setSpid(tContract.getSpid());
        contract.setType(tContract.getParticipantType().getIndex());
        contract.setContractOwner(tContract.getOwnerID());
        contract.setTransferTypeId(tContract.getTransferType());
        contract.setName(tContract.getDescription());
        contract.setContributorChargingType(tContract.getContributorChargingType().getIndex());
        contract.setRecipientChargingType(tContract.getRecipientChargingType().getIndex());

        final TFAClientConfig config = (TFAClientConfig)ctx.get(TFAClientConfig.class);

        if (tContract.getParticipantType().equals(ParticipantTypeEnum.CONTRACT_GROUP))
        {
            if (tContract.getContributorIsOperator())
            {
                contract.setContributorGroupId(config.getOperatorChargingID());
            }
            else
            {
                contract.setContributorGroupId(tContract.getContributorGroupID());
            }
    
            if (tContract.getRecipientIsOperator())
            {
                contract.setRecipientGroupId(config.getOperatorChargingID());
            }
            else
            {
                contract.setRecipientGroupId(tContract.getRecipientGroupID());
            }
        }
        else //if (tContract.getParticipantType().equals(ParticipantTypeEnum.SERVICE_PROVIDER))
        {
            contract.setContributorGroupId(tContract.getContributingSpid());
            contract.setRecipientGroupId(tContract.getRecipientSpid());
        }
        
        contract.setAgreementId(tContract.getAgreementID());
        return contract;
    }


    /**
     * Adapts the Contract from TFA bean into the transfer contract from CRM.
     * @param ctx the operating context
     * @param contract the source to adapt
     * @return the CRM contract bean
     */
    public TransferContract adaptContract(final Context ctx, final Contract contract)
    {
        final TransferContract tContract = new TransferContract();

        tContract.setIdentifier(contract.getContractId());
        tContract.setSpid(contract.getSpid());
        tContract.setParticipantType(ParticipantTypeEnum.get(contract.getType()));
        tContract.setOwnerID(contract.getContractOwner());
        tContract.setTransferType(contract.getTransferTypeId());
        tContract.setDescription(contract.getName());

        final TFAClientConfig config = (TFAClientConfig)ctx.get(TFAClientConfig.class);

        if (contract.getContributorGroupId() == config.getOperatorChargingID())
        {
            tContract.setContributorIsOperator(true);
        }
        else
        {
            tContract.setContributorIsOperator(false);
            tContract.setContributorChargingType(SubscriberTypeEnum.get(contract.getContributorChargingType()));
        }        
        
        if (contract.getRecipientGroupId() == config.getOperatorChargingID())
        {
            tContract.setRecipientIsOperator(true);
        }
        else
        {
            tContract.setRecipientIsOperator(false);
            tContract.setRecipientChargingType(SubscriberTypeEnum.get(contract.getRecipientChargingType()));
        }

        if (tContract.getParticipantType().equals(ParticipantTypeEnum.CONTRACT_GROUP))
        {
            if (!tContract.isContributorIsOperator())
            {
                tContract.setContributorGroupID(contract.getContributorGroupId());
                tContract.setContributorOwner(getGroupOwner(ctx, contract.getContributorGroupId()));
            }
            if (!tContract.isRecipientIsOperator())
            {
                tContract.setRecipientGroupID(contract.getRecipientGroupId());
                tContract.setRecipientOwner(getGroupOwner(ctx, contract.getRecipientGroupId()));                
            }
        }
        else //if (tContract.getParticipantType().equals(ParticipantTypeEnum.SERVICE_PROVIDER))
        {
            tContract.setContributingSpid((int)contract.getContributorGroupId());
            tContract.setRecipientSpid((int)contract.getRecipientGroupId());
        }
        
        tContract.setAgreementID(contract.getAgreementId());

        return tContract;
    }
    
    private String getGroupOwner(final Context ctx, final long groupId)
    {
        String owner = "";
        final TransferContractGroupFacade service = 
            (TransferContractGroupFacade) ctx.get(TransferContractGroupFacade.class);
        try
        {
            ContractGroup group = service.retrieveContractGroup(ctx, groupId);
            owner = group.getOwnerID();
        }
        catch (TransferContractException e)
        {
            LogSupport.minor(ctx, this, "Unable to determine group owner. TransferContractException while retrieving the contract ", e);
        }
        catch (ContractGroupNotFoundException e)
        {
            LogSupport.minor(ctx, this, "Unable to determine group owner. TransferContractNotFoundException while retrieving the contract ", e);
        }
        return owner;
    }

    /**
     * Returns the RMI service configured.
     * @param ctx the operating context
     * @return the RMI service.
     */
    private TransferContractService getService(final Context ctx)
    {
        final TransferContractService service =
            (TransferContractService)ctx.get(TransferContractService.class);
        return service;
    }

}
