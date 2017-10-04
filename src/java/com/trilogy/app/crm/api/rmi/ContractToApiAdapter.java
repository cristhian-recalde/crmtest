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
package com.trilogy.app.crm.api.rmi;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.payment.Contract;
import com.trilogy.util.crmapi.wsdl.v2_2.types.generalprovisioning.ContractFeeFrequencyEnum;
import com.trilogy.util.crmapi.wsdl.v2_2.types.generalprovisioning.ContractReference;
import com.trilogy.util.crmapi.wsdl.v2_2.types.generalprovisioning.ContractStateEnum;

/**
 * Adapts Contract object to API objects.
 *
 * @author victor.stratan@redknee.com
 */
public class ContractToApiAdapter implements Adapter
{
    public Object adapt(final Context ctx, final Object obj) throws HomeException
    {
        return adaptContractToReference((Contract) obj);
    }

    public Object unAdapt(final Context ctx, final Object obj) throws HomeException
    {
        throw new UnsupportedOperationException();
    }

    public static com.redknee.util.crmapi.wsdl.v2_2.types.generalprovisioning.Contract adaptContractToApi(final Contract contract)
    {
        final com.redknee.util.crmapi.wsdl.v2_2.types.generalprovisioning.Contract apiContract;
        apiContract = new com.redknee.util.crmapi.wsdl.v2_2.types.generalprovisioning.Contract();
        adaptContractToReference(contract, apiContract);
        apiContract.setDescription(contract.getDesc());
        apiContract.setDuration(contract.getDuration());
        apiContract.setDurationFrequency(ContractFeeFrequencyEnum.valueOf(contract.getDurationFrequency().getIndex()));

        return apiContract;
    }

    public static ContractReference adaptContractToReference(final Contract contract)
    {
        final ContractReference reference = new ContractReference();
        adaptContractToReference(contract, reference);

        return reference;
    }

    public static ContractReference adaptContractToReference(final Contract contract, final ContractReference reference)
    {
        reference.setIdentifier(contract.getId());
        reference.setSpid(contract.getSpid());
        reference.setName(contract.getName());
        reference.setFee(contract.getContractFee());
        reference.setState(ContractStateEnum.valueOf(contract.getState().getIndex()));

        return reference;
    }
}
