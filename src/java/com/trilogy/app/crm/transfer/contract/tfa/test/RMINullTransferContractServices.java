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
package com.trilogy.app.crm.transfer.contract.tfa.test;

import com.trilogy.app.transferfund.rmi.api.transfercontract.TransferContractService;
import com.trilogy.app.transferfund.rmi.api.transfercontract.TransferContractServiceException;
import com.trilogy.app.transferfund.rmi.api.transfercontract.TransferContractServiceInternalException;
import com.trilogy.app.transferfund.rmi.api.transfergroup.TransferGroupService;
import com.trilogy.app.transferfund.rmi.api.transfergroup.TransferGroupServiceException;
import com.trilogy.app.transferfund.rmi.api.transfergroup.TransferGroupServiceInternalException;
import com.trilogy.app.transferfund.rmi.data.AuthCredentials;
import com.trilogy.app.transferfund.rmi.data.Contract;
import com.trilogy.app.transferfund.rmi.data.ContractGroup;
import com.trilogy.app.transferfund.rmi.data.RecordsDeleted;
import com.trilogy.app.transferfund.rmi.exception.ContractProvisioningException;
import com.trilogy.framework.xhome.context.Context;

/**
 * Test class to simulate the RMI transfer services client.
 * @author arturo.medina@redknee.com
 *
 */
public class RMINullTransferContractServices implements
        TransferContractService, TransferGroupService
{

    /**
     * {@inheritDoc}
     */
    public Contract createTransferContract(AuthCredentials arg0, Contract arg1,
            String arg2) throws TransferContractServiceException,
            TransferContractServiceInternalException,
            ContractProvisioningException
    {
        return arg1;
    }

    /**
     * {@inheritDoc}
     */
    public int deleteAllTransferContracts(AuthCredentials arg0, String arg1,
            String arg2) throws TransferContractServiceException,
            TransferContractServiceInternalException,
            ContractProvisioningException
    {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public void deleteTransferContract(AuthCredentials arg0, long arg1,
            String arg2) throws TransferContractServiceException,
            TransferContractServiceInternalException,
            ContractProvisioningException
    {

    }

    /**
     * {@inheritDoc}
     */
    public Contract retrieveTransferContract(AuthCredentials arg0, long arg1,
            String arg2) throws TransferContractServiceException,
            TransferContractServiceInternalException,
            ContractProvisioningException
    {
        return new Contract();
    }

    /**
     * {@inheritDoc}
     */
    public Contract[] retrieveTransferContractByOwner(AuthCredentials arg0,
            String arg1, String arg2) throws TransferContractServiceException,
            TransferContractServiceInternalException,
            ContractProvisioningException
    {
        Contract[] contracts = {new Contract()};
        return contracts;
    }

    /**
     * {@inheritDoc}
     */
    public Contract createTransferContract(Context arg0, AuthCredentials arg1,
            Contract arg2, String arg3)
            throws TransferContractServiceException,
            TransferContractServiceInternalException,
            ContractProvisioningException
    {
        return new Contract();
    }

    /**
     * {@inheritDoc}
     */
    public int deleteAllTransferContracts(Context arg0, AuthCredentials arg1,
            String arg2, String arg3) throws TransferContractServiceException,
            TransferContractServiceInternalException,
            ContractProvisioningException
    {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public void deleteTransferContract(Context arg0, AuthCredentials arg1,
            long arg2, String arg3) throws TransferContractServiceException,
            TransferContractServiceInternalException,
            ContractProvisioningException
    {

    }

    /**
     * {@inheritDoc}
     */
    public Contract retrieveTransferContract(Context arg0,
            AuthCredentials arg1, long arg2, String arg3)
            throws TransferContractServiceException,
            TransferContractServiceInternalException,
            ContractProvisioningException
    {
        return new Contract();
    }

    /**
     * {@inheritDoc}
     */
    public Contract[] retrieveTransferContractByOwner(Context arg0,
            AuthCredentials arg1, String arg2, String arg3)
            throws TransferContractServiceException,
            TransferContractServiceInternalException,
            ContractProvisioningException
    {
        Contract[] contracts = {new Contract()};
        return contracts;
    }

    /**
     * {@inheritDoc}
     */
    public ContractGroup createContractGroup(AuthCredentials arg0,
            ContractGroup arg1, String arg2)
            throws TransferGroupServiceException,
            TransferGroupServiceInternalException,
            ContractProvisioningException
    {
        return new ContractGroup();
    }

    /**
     * {@inheritDoc}
     */
    public void deleteContractGroup(AuthCredentials arg0, long arg1, String arg2)
            throws TransferGroupServiceException,
            TransferGroupServiceInternalException,
            ContractProvisioningException
    {

    }

    /**
     * {@inheritDoc}
     */
    public RecordsDeleted deleteOwner(AuthCredentials arg0, String arg1, String arg2)
            throws TransferGroupServiceException,
            TransferGroupServiceInternalException,
            ContractProvisioningException
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public ContractGroup retrieveContractGroup(AuthCredentials arg0, long arg1,
            String arg2) throws TransferGroupServiceException,
            TransferGroupServiceInternalException,
            ContractProvisioningException
    {
        return new ContractGroup();
    }

    /**
     * {@inheritDoc}
     */
    public ContractGroup[] retrieveContractGroupByOwner(AuthCredentials arg0,
            String arg1, String arg2) throws TransferGroupServiceException,
            TransferGroupServiceInternalException,
            ContractProvisioningException
    {
        ContractGroup[] contracts = {new ContractGroup()};
        return contracts;
    }

    /**
     * {@inheritDoc}
     */
    public ContractGroup createContractGroup(Context arg0,
            AuthCredentials arg1, ContractGroup arg2, String arg3)
            throws TransferGroupServiceException,
            TransferGroupServiceInternalException,
            ContractProvisioningException
    {
        return new ContractGroup();
    }

    /**
     * {@inheritDoc}
     */
    public void deleteContractGroup(Context arg0, AuthCredentials arg1,
            long arg2, String arg3) throws TransferGroupServiceException,
            TransferGroupServiceInternalException,
            ContractProvisioningException
    {

    }

    /**
     * {@inheritDoc}
     */
    public RecordsDeleted deleteOwner(Context arg0, AuthCredentials arg1, String arg2,
            String arg3) throws TransferGroupServiceException,
            TransferGroupServiceInternalException,
            ContractProvisioningException
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public ContractGroup retrieveContractGroup(Context arg0,
            AuthCredentials arg1, long arg2, String arg3)
            throws TransferGroupServiceException,
            TransferGroupServiceInternalException,
            ContractProvisioningException
    {
        return new ContractGroup();
    }

    /**
     * {@inheritDoc}
     */
    public ContractGroup[] retrieveContractGroupByOwner(Context arg0,
            AuthCredentials arg1, String arg2, String arg3)
            throws TransferGroupServiceException,
            TransferGroupServiceInternalException,
            ContractProvisioningException
    {
        ContractGroup[] contracts = {new ContractGroup()};
        return contracts;
    }


	@Override
	public Contract[] retrieveTransferContractsByMember(Context ctx,
			AuthCredentials authCredentials, String chargingId, String erRef)
			throws TransferContractServiceException,
			TransferContractServiceInternalException,
			ContractProvisioningException {
		// TODO Auto-generated method stub
		Contract[] contract = {new Contract()};
		return contract;
		//return null;
	}

	@Override
	public Contract[] retrieveTransferContractsByMember(
			AuthCredentials authCredentials, String chargingId, String erRef)
			throws TransferContractServiceException,
			TransferContractServiceInternalException,
			ContractProvisioningException {
		// TODO Auto-generated method stub
		Contract[] contract = {new Contract()};
		return contract;
	}
}
