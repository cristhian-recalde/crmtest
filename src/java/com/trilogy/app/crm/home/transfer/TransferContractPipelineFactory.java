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
package com.trilogy.app.crm.home.transfer;

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.transfer.ContractGroupHome;
import com.trilogy.app.crm.transfer.TransferAgreementHome;
import com.trilogy.app.crm.transfer.TransferContractHome;
import com.trilogy.app.crm.transfer.TransferTypeHome;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.ValidatingHome;
import com.trilogy.framework.xhome.msp.SpidAwareHome;

/**
 * Sets up the home decorators for Transfer Contract and Contract groups
 * @author arturo.medina@redknee.com
 *
 */
public class TransferContractPipelineFactory implements PipelineFactory
{

    /**
     * {@inheritDoc}
     */
    public Home createPipeline(Context ctx, Context serverCtx)
            throws RemoteException, HomeException, IOException, AgentException
    {
        Home transferHome = new TransferContractServiceHome(ctx, null);
        transferHome = new SpidAwareHome(ctx, transferHome);
        
        ctx.put(TransferContractHome.class, transferHome);
        
        Home contractGroupHome = new ContractGroupServiceHome(ctx, null);
        contractGroupHome = new SpidAwareHome(ctx, contractGroupHome);
        ctx.put(ContractGroupHome.class, contractGroupHome);

        Home agreementHome = new TransferAgreementServiceHome(ctx, null);
        agreementHome = new SpidAwareHome(ctx, agreementHome);
        agreementHome = new ValidatingHome(new TransferAgreementValidator(), agreementHome);
        ctx.put(TransferAgreementHome.class, agreementHome);

        
        Home transferTypeHome = new TransferTypeServiceHome(ctx, null);
        ctx.put(TransferTypeHome.class, transferTypeHome);
        
        return null;
    }

}
