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
package com.trilogy.app.crm.home.pipelineFactory;

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.framework.core.platform.CoreSupport;
import com.trilogy.framework.xhome.beans.CompoundValidator;
import com.trilogy.framework.xhome.cluster.RMIClusteredHome;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.AuditJournalHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.NotifyingHome;
import com.trilogy.framework.xhome.home.ValidatingHome;
import com.trilogy.framework.xhome.msp.SpidAwareHome;

import com.trilogy.app.crm.bean.TaxAuthority;
import com.trilogy.app.crm.bean.TaxAuthorityHome;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.support.ConfigChangeRequestSupportHelper;
import com.trilogy.app.crm.xhome.validator.TaxAuthorityShortCodeValidator;
import com.trilogy.app.crm.xhome.validator.TaxAuthorityTaxCodeValidator;
import com.trilogy.app.crm.xhome.validator.TaxAuthorityTaxationMethodValidator;


/**
 * 
 *
 * @author aaron.gourley@redknee.com
 * @since 
 */
public class TaxAuthorityHomePipelineFactory implements PipelineFactory
{

    /**
     * {@inheritDoc}
     */
    public Home createPipeline(Context ctx, Context serverCtx) throws RemoteException, HomeException, IOException,
            AgentException
    {
        Home taxAuthHome = CoreSupport.bindHome(ctx, TaxAuthority.class);
        taxAuthHome = new NotifyingHome(taxAuthHome);
        taxAuthHome = new AuditJournalHome(ctx, taxAuthHome);
        taxAuthHome = new SpidAwareHome(ctx, taxAuthHome);
        taxAuthHome = new RMIClusteredHome(ctx, TaxAuthorityHome.class.getName(), taxAuthHome);
        
        CompoundValidator validator = new CompoundValidator();
        validator.add(TaxAuthorityTaxCodeValidator.instance());
        validator.add(TaxAuthorityShortCodeValidator.instance());
        validator.add(TaxAuthorityTaxationMethodValidator.instance());
        taxAuthHome = new ValidatingHome(taxAuthHome, validator);
        
        taxAuthHome = ConfigChangeRequestSupportHelper.get(ctx).registerHomeForConfigSharing(ctx, taxAuthHome, TaxAuthority.class);
        
        return taxAuthHome;
    }

}
