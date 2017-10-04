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

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.InnerBeanPropertiesValidator;
import com.trilogy.app.crm.billing.message.BillingMessageAwareHomeDecorator;
import com.trilogy.app.crm.extension.ExtensionForeignKeyAdapter;
import com.trilogy.app.crm.extension.ExtensionHandlingHome;
import com.trilogy.app.crm.extension.spid.SpidExtension;
import com.trilogy.app.crm.extension.spid.SpidExtensionXInfo;
import com.trilogy.app.crm.extension.validator.SingleInstanceExtensionsValidator;
import com.trilogy.app.crm.home.AutoCreatePropertiesSpidHome;
import com.trilogy.app.crm.home.CRMSpidLazyLoadedPropertyUpdateHome;
import com.trilogy.app.crm.home.NoRemoveHome;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.home.SpidERLogHome;
import com.trilogy.app.crm.home.validator.PricePlanSwitchLimitSpidExtensionValidator;
import com.trilogy.app.crm.home.validator.SpidPreWarnNotificationValidator;
import com.trilogy.app.crm.support.ConfigChangeRequestSupportHelper;
import com.trilogy.app.crm.xhome.home.TransientFieldResettingHome;
import com.trilogy.framework.core.home.PMHome;
import com.trilogy.framework.core.platform.CoreSupport;
import com.trilogy.framework.xhome.beans.CompoundValidator;
import com.trilogy.framework.xhome.cluster.RMIClusteredHome;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.AdapterHome;
import com.trilogy.framework.xhome.home.AuditJournalHome;
import com.trilogy.framework.xhome.home.ContextualizingHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.NotifyingHome;
import com.trilogy.framework.xhome.home.SortingHome;
import com.trilogy.framework.xhome.home.ValidatingHome;
import com.trilogy.framework.xhome.msp.SpidAwareHome;


/**
 * 
 *
 * @author aaron.gourley@redknee.com
 * @since 8.2
 */
public class CRMSpidHomePipelineFactory implements PipelineFactory
{
    private static PipelineFactory instance_ = null;
    public static PipelineFactory instance()
    {
        if (instance_ == null)
        {
            instance_ = new CRMSpidHomePipelineFactory();
        }
        return instance_;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Home createPipeline(Context ctx, Context serverCtx) throws RemoteException, HomeException, IOException,
            AgentException
    {
        Home spidHome = CoreSupport.bindHome(ctx, CRMSpid.class);
        
        // Reset s-transient fields to default values before storing to journal (i.e. TransientHome).
        spidHome = new TransientFieldResettingHome(ctx, spidHome);
        spidHome = new PMHome(ctx, CRMSpidHome.class.getName(), spidHome);
        
        spidHome = new RMIClusteredHome(ctx, CRMSpidHome.class.getName(), spidHome);

        spidHome = new CRMSpidLazyLoadedPropertyUpdateHome(ctx, spidHome);

        spidHome = new ExtensionHandlingHome<SpidExtension>(
                ctx, 
                SpidExtension.class, 
                SpidExtensionXInfo.SPID, 
                spidHome);
        spidHome = new AdapterHome(spidHome, 
                new ExtensionForeignKeyAdapter(
                        SpidExtensionXInfo.SPID));
        
        //spidHome = new AdapterHome(ctx, spidHome, new SpidLanguageAdapter());
        
        spidHome = new AutoCreatePropertiesSpidHome(ctx, spidHome);
        spidHome = new AuditJournalHome(ctx, spidHome);
        spidHome = new SpidERLogHome(spidHome);
        
        spidHome = new SortingHome(spidHome);
        spidHome = new NoRemoveHome(spidHome);
        
        CompoundValidator validator = new CompoundValidator();
        
        validator.add(new SingleInstanceExtensionsValidator());
        validator.add(new InnerBeanPropertiesValidator());
        validator.add(SpidPreWarnNotificationValidator.instance());
        validator.add(new PricePlanSwitchLimitSpidExtensionValidator());
                
        spidHome = new ValidatingHome(spidHome, validator);
        
        spidHome = new ContextualizingHome(ctx, spidHome);
        
        spidHome = new SpidAwareHome(ctx, spidHome) {
            @Override
            public  Object create(Context saCtx, Object obj)
              throws HomeException
           {
              return getDelegate().create(saCtx, obj);
           }
        };

        // this has to be near the end because it instruments the pipeline
        spidHome = new BillingMessageAwareHomeDecorator().decorateHome(ctx, spidHome);

        spidHome = ConfigChangeRequestSupportHelper.get(ctx).registerHomeForConfigSharing(ctx, spidHome, CRMSpid.class);

        spidHome = new NotifyingHome(spidHome);
        
        return spidHome;
    }

}
