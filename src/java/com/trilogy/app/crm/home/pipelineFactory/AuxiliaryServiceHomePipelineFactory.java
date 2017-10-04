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

import com.trilogy.app.crm.bean.AuxiliaryServiceAdapter;
import com.trilogy.app.crm.bean.AuxiliaryServiceHome;
import com.trilogy.app.crm.extension.validator.SingleInstanceExtensionsValidator;
import com.trilogy.app.crm.home.AuxiliaryServiceAdjustmentTypeCreationHome;
import com.trilogy.app.crm.home.AuxiliaryServiceCheckAssociationsHome;
import com.trilogy.app.crm.home.AuxiliaryServiceIdentifierSettingHome;
import com.trilogy.app.crm.home.AuxiliaryServiceOMCreationHome;
import com.trilogy.app.crm.home.ClosedAuxiliaryServiceCleanupHome;
import com.trilogy.app.crm.home.MultiMonthRecurrenceIntervalValidator;
import com.trilogy.app.crm.home.PermissionAwarePermissionSettingHome;
import com.trilogy.app.crm.home.ServiceOneTimeSettingHome;
import com.trilogy.app.crm.home.core.CoreAuxiliaryServiceHomePipelineFactory;
import com.trilogy.app.crm.home.validator.AuxiliaryServiceOneToOneServiceOptionValidator;
import com.trilogy.app.crm.home.validator.FinalStateBeanUpdateValidator;
import com.trilogy.app.crm.technology.TechnologyAwareHome;
import com.trilogy.app.crm.validator.AuxiliaryServicePeriodValidator;
import com.trilogy.app.crm.xhome.adapter.ExtendedBeanAdapter;
import com.trilogy.framework.xhome.beans.CompoundValidator;
import com.trilogy.framework.xhome.beans.ValidatableValidator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.AdapterHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.LastModifiedAwareHome;
import com.trilogy.framework.xhome.home.ValidatingHome;
import com.trilogy.framework.xhome.msp.SpidAwareHome;

/**
 * A factory which creates the pipeline for <code>AuxiliaryServiceHome</code>.
 *
 * @author cindy.wong@redknee.com
 */
public class AuxiliaryServiceHomePipelineFactory extends CoreAuxiliaryServiceHomePipelineFactory
{

    /**
     * {@inheritDoc}
     */
    @Override
    public Home createPipeline(final Context context, final Context serverContext)
    {
    	//It is expected that core storageInstall, has already installed the pipeline.
    	//Using this pipeline, instead of again creating new.
    	//Problem with creating new pipeline was that earlier pipeline was being De-referenced and hence Garbage Collected.
        Home home = (Home) context.get(AuxiliaryServiceHome.class);
        
        if(home == null)
        {
        	home = super.createPipeline(context, serverContext);
        }
        
        // Install a home to adapt between business logic bean and data bean
        home = new AdapterHome(
                context, 
                home, 
                new ExtendedBeanAdapter<com.redknee.app.crm.bean.core.AuxiliaryService, com.redknee.app.crm.bean.core.custom.AuxiliaryService>(
                        com.redknee.app.crm.bean.core.AuxiliaryService.class, 
                        com.redknee.app.crm.bean.core.custom.AuxiliaryService.class));
        
        //should replace adapterHome with AuthMappingHome.
        home = new AdapterHome(home, new AuxiliaryServiceAdapter()); 
        home = new PermissionAwarePermissionSettingHome(home);
        
        home = new AuxiliaryServiceAdjustmentTypeCreationHome(home);
        home = new ServiceOneTimeSettingHome(home);
        home = new AuxiliaryServiceIdentifierSettingHome(home);
        home = new LastModifiedAwareHome(home);
        home = new AuxiliaryServiceOMCreationHome(home);
        
        home = new AuxiliaryServiceCheckAssociationsHome(home);
        
        home = new ClosedAuxiliaryServiceCleanupHome(context, home);
        
        final CompoundValidator validators = new CompoundValidator();
        validators.add(ValidatableValidator.instance());
        validators.add(new AuxiliaryServiceOneToOneServiceOptionValidator());
        validators.add(FinalStateBeanUpdateValidator.instance());
        validators.add(MultiMonthRecurrenceIntervalValidator.instance());
        validators.add(new SingleInstanceExtensionsValidator());
        validators.add(new AuxiliaryServicePeriodValidator());
        
        home = new ValidatingHome(validators, home);
        
        home = new TechnologyAwareHome(context, home);
        home = new SpidAwareHome(context, home);

        return home;
    }
}
