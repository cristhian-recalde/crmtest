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

import com.trilogy.app.crm.transfer.ContractGroup;
import com.trilogy.app.crm.transfer.GroupPrivacyEnum;
import com.trilogy.app.crm.transfer.contract.ContractGroupNotFoundException;
import com.trilogy.app.crm.transfer.contract.TransferContractException;
import com.trilogy.app.crm.transfer.contract.TransferContractGroupFacade;
import com.trilogy.app.crm.transfer.contract.TransferContractOwnerNotFoundException;
import com.trilogy.app.crm.transfer.membergroup.MemberGroupSupport;
import com.trilogy.app.transferfund.rmi.api.transfergroup.TransferGroupService;
import com.trilogy.app.transferfund.rmi.data.AuthCredentials;
import com.trilogy.app.transferfund.rmi.exception.ContractProvisioningException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * RMI implementation of the RMI client for transfer contracts group on TFA.
 * @author arturo.medina@redknee.com
 *
 */
public class RMITransferContractGroupImpl implements
        TransferContractGroupFacade
{

    /**
     * {@inheritDoc}
     */
    public ContractGroup createContractGroup(final Context ctx,
            ContractGroup contract)
        throws TransferContractException
    {
        final AuthCredentials credentials = TransferContractSupport.getCredentials(ctx);
        final TransferGroupService service = getService(ctx);
        com.redknee.app.transferfund.rmi.data.ContractGroup contractGroup =
            adaptCRMContractGroup(contract);

        try
        {
            contractGroup = service.createContractGroup(credentials, contractGroup, TransferContractSupport.getErReference(ctx));
            contract = adaptTFAContractGroup(ctx, contractGroup);
            
        }
        catch (ContractProvisioningException e)
        {
            LogSupport.major(ctx, this, "ContractProvisioningException when trying to create the contract group"
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
    public void deleteContractGroup(final Context ctx, final long contractId)
        throws TransferContractException, ContractGroupNotFoundException
    {
        final AuthCredentials credentials = TransferContractSupport.getCredentials(ctx);
        final TransferGroupService service = getService(ctx);

        try
        {
            service.deleteContractGroup(credentials, contractId, TransferContractSupport.getErReference(ctx));
        }
        catch (ContractProvisioningException e)
        {
            LogSupport.major(ctx, this, "ContractProvisioningException when trying to delete the contract group"
                    + contractId
                    + " error code : "
                    + e.getResponseCode());

            if (e.getResponseCode() == TransferContractSupport.ERR_MEMBER_GROUP_NOTEXISTS)
            {
                throw new ContractGroupNotFoundException(
                        TransferContractSupport.mapErrorCodeWithMessage(e.getResponseCode()),e);
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
    public void deleteOwner(final Context ctx, final String ownerId)
        throws TransferContractException, TransferContractOwnerNotFoundException
    {
        final AuthCredentials credentials = TransferContractSupport.getCredentials(ctx);
        final TransferGroupService service = getService(ctx);

        try
        {
            service.deleteOwner(credentials, ownerId, TransferContractSupport.getErReference(ctx));
        }
        catch (ContractProvisioningException e)
        {
            LogSupport.major(ctx, this, "ContractProvisioningException when trying to delete the contract group owner"
                    + ownerId
                    + " error code : "
                    + e.getResponseCode());

            if (e.getResponseCode() == TransferContractSupport.ERR_OWNER_NOT_EXISTS_TRANSFER_GROUP)
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
    }

    /**
     * {@inheritDoc}
     */
    public ContractGroup retrieveContractGroup(final Context ctx, final long groupId)
        throws TransferContractException, ContractGroupNotFoundException
    {
        final AuthCredentials credentials = TransferContractSupport.getCredentials(ctx);
        final TransferGroupService service = getService(ctx);
        ContractGroup group = null;

        try
        {
            com.redknee.app.transferfund.rmi.data.ContractGroup tfaGroup =
                service.retrieveContractGroup(credentials, groupId, TransferContractSupport.getErReference(ctx));
            group = adaptTFAContractGroup(ctx, tfaGroup);
        }
        catch (ContractProvisioningException e)
        {
            LogSupport.major(ctx, this, "ContractProvisioningException when trying to retrieve the contract group "
                    + groupId
                    + " error code : "
                    + e.getResponseCode());
            if (e.getResponseCode() == TransferContractSupport.ERR_TRANSFER_GROUP_NOTEXISTS)
            {
                throw new ContractGroupNotFoundException(
                        TransferContractSupport.mapErrorCodeWithMessage(e.getResponseCode()),e);
            }
            else
            {
                throw new TransferContractException(
                        TransferContractSupport.mapErrorCodeWithMessage(e.getResponseCode()),e);
            }
        }

        return group;
    }

    /**
     * {@inheritDoc}
     */
    public List<ContractGroup> retrieveContractGroupByOwner(final Context ctx,
            final String owner)
            throws TransferContractException, TransferContractOwnerNotFoundException
    {
        final AuthCredentials credentials = TransferContractSupport.getCredentials(ctx);
        final TransferGroupService service = getService(ctx);
        final List<ContractGroup>groups = new ArrayList<ContractGroup>();

        final com.redknee.app.transferfund.rmi.data.ContractGroup[] tfaGroups;
        try
        {
            tfaGroups = service.retrieveContractGroupByOwner(credentials, owner, TransferContractSupport.getErReference(ctx));
            for (int i = 0; i < tfaGroups.length; i++)
            {
                if (tfaGroups[i] != null)
                {
                    groups.add(adaptTFAContractGroup(ctx, tfaGroups[i]));
                }
            }
        }
        catch (ContractProvisioningException e)
        {
            LogSupport.major(ctx, this, "ContractProvisioningException when trying to retrieve the contract group "
                    + owner
                    + " error code : "
                    + e.getResponseCode());
            
            if (e.getResponseCode() != TransferContractSupport.ERR_OWNER_NOT_EXISTS_TRANSFER_GROUP)
            {
                throw new TransferContractException(
                        TransferContractSupport.mapErrorCodeWithMessage(e.getResponseCode()),e);
            }
        }

        return groups;
    }


    /**
     * Converts the CRM contract group to a TFA Contract group.
     * @param crmContract the CRM contract to convert
     * @return the TFA contract group
     */
    public com.redknee.app.transferfund.rmi.data.ContractGroup adaptCRMContractGroup(ContractGroup crmContract)
    {
        final com.redknee.app.transferfund.rmi.data.ContractGroup cGroup =
            new com.redknee.app.transferfund.rmi.data.ContractGroup();

        cGroup.setGroupId(crmContract.getIdentifier());
        cGroup.setDescription(crmContract.getDescription());
        cGroup.setGroupOwner(crmContract.getOwnerID());
        cGroup.setSpid(crmContract.getSpid());

        return cGroup;
    }

    /**
     * Converts the TFA contract group to a CRM Contract group.
     * @param tfaContract the TFA contract to convert
     * @return the CRM contract group converted
     */
    public ContractGroup adaptTFAContractGroup(final Context ctx,
            final com.redknee.app.transferfund.rmi.data.ContractGroup tfaContract)
    {
        final ContractGroup contractGroup = new ContractGroup();

        contractGroup.setIdentifier(tfaContract.getGroupId());
        contractGroup.setDescription(tfaContract.getDescription());
        contractGroup.setOwnerID(tfaContract.getGroupOwner());
        contractGroup.setSpid(tfaContract.getSpid());
        try
        {
            if (!contractGroup.getOwnerID().equals(TransferContractSupport.getPublicGroupOwnerName(ctx)))
            {
                contractGroup.setPrivacy(GroupPrivacyEnum.PRIVATE);
            }
        }
        catch (Exception e)
        {
        }
        return contractGroup;
    }

    /**
     * Returns the RMI service configured.
     * @param ctx the operating context
     * @return the RMI service.
     */
    private TransferGroupService getService(final Context ctx)
    {
        final TransferGroupService service =
            (TransferGroupService)ctx.get(TransferGroupService.class);
        return service;
    }

}
