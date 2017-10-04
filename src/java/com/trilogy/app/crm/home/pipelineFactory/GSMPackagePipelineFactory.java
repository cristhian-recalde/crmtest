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

import com.trilogy.framework.xhome.beans.CompoundValidator;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.LastModifiedAwareHome;
import com.trilogy.framework.xhome.home.NoSelectAllHome;
import com.trilogy.framework.xhome.home.SortingHome;
import com.trilogy.framework.xhome.home.ValidatingHome;
import com.trilogy.framework.xhome.msp.SpidAwareHome;

import com.trilogy.app.crm.home.GSMPackageERLogHome;
import com.trilogy.app.crm.home.IMSIChangeHome;
import com.trilogy.app.crm.home.OldGSMPackageLookupHome;
import com.trilogy.app.crm.home.PackageRemovalValidatorProxyHome;
import com.trilogy.app.crm.home.PackageValidator;
import com.trilogy.app.crm.home.core.CoreGSMPackageHomePipelineFactory;
import com.trilogy.app.crm.home.validator.FinalStateChangeBlockingValidator;
import com.trilogy.app.crm.numbermgn.GenericPackageDefaultResourceSaveHome;
import com.trilogy.app.crm.numbermgn.PackageChangeAppendHistoryHome;
import com.trilogy.app.crm.technology.TechnologyAwareHome;
import com.trilogy.app.crm.validator.ImsiOrMinValidator;

/**
 * Creates the home decorators pipeline fo the GSMPackageHome
 *
 * @author arturo.medina@redknee.com
 */
public class GSMPackagePipelineFactory extends CoreGSMPackageHomePipelineFactory
{

    public GSMPackagePipelineFactory()
    {
        super();
    }

    @Override
    public Home createPipeline(final Context ctx, final Context serverCtx)
        throws RemoteException, HomeException, IOException, AgentException
    {
        // GSM Package Home
        Home home = super.createPipeline(ctx, serverCtx);
        
        home = new GSMPackageERLogHome(home);
        home = new PackageChangeAppendHistoryHome(home);
        home = new GenericPackageDefaultResourceSaveHome(home);
        home = new IMSIChangeHome(ctx, home);
        home = new LastModifiedAwareHome(home);
        home = new SortingHome(home);
        home = new PackageRemovalValidatorProxyHome(home);
        home = new ValidatingHome(FinalStateChangeBlockingValidator.instance(),home);
        home = new ValidatingHome(PackageValidator.instance(), home);
        home = new TechnologyAwareHome(ctx, home);
        home = new SpidAwareHome(ctx, home);
        home = new NoSelectAllHome(home);
        home = new OldGSMPackageLookupHome(ctx, home);

        // bean validators
        final CompoundValidator validator = new CompoundValidator();
        validator.add(ImsiOrMinValidator.instance());

        home = new ValidatingHome(validator, home);
        return home;
    }
}
