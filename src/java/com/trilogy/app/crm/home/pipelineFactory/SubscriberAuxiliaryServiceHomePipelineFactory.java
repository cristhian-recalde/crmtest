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

import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryServiceXDBHome;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryServiceXInfo;
import com.trilogy.app.crm.bean.core.AuxiliaryService;
import com.trilogy.app.crm.extension.ExtensionAssociationHome;
import com.trilogy.app.crm.extension.auxiliaryservice.AuxiliaryServiceExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.AuxiliaryServiceExtensionXInfo;
import com.trilogy.app.crm.home.CallingGroupECPActivationHome;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.home.SubscriberAuxServiceNextRecurChargeDateUpdateHome;
import com.trilogy.app.crm.home.SubscriberAuxiliaryServiceBulkloadChargingHome;
import com.trilogy.app.crm.home.SubscriberAuxiliaryServiceContextPrepareHome;
import com.trilogy.app.crm.home.SubscriberAuxiliaryServiceHistoryCreationHome;
import com.trilogy.app.crm.home.sub.SubscriptionAuxiliaryServiceDiscountEventHome;
import com.trilogy.app.crm.home.SubscriberAuxiliaryServiceIdentifierSettingHome;
import com.trilogy.app.crm.home.SubscriberAuxiliaryServiceOMCreationHome;
import com.trilogy.app.crm.home.SubscriberAuxiliaryServiceSubscriberValidator;
import com.trilogy.app.crm.home.SubscriberAuxiliaryServicesPersonalizedFeeUpdateHome;
import com.trilogy.app.crm.home.SubscriberProvisionableAuxiliaryServiceCreatingHome;
import com.trilogy.app.crm.home.SubscriberProvisionableAuxiliaryServiceRemovingHome;
import com.trilogy.app.crm.home.SubscriberProvisionableAuxiliaryServiceUpdateHome;
import com.trilogy.app.crm.home.validator.AuxiliaryServiceAssociationCreateStateValidator;
import com.trilogy.app.crm.subscriber.provision.TFAAuxServiceProvisionHome;
import com.trilogy.app.crm.vpn.SubscriberProvisionableVpnAuxiliaryServiceUtilityHome;
import com.trilogy.framework.xhome.beans.CompoundValidator;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.ContextualizingHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.ValidatingHome;

/**
 * Creates the pipeline for SubscriberAuxiliaryServiceHome.
 *
 * @author cindy.wong@redknee.com
 */
public class SubscriberAuxiliaryServiceHomePipelineFactory implements PipelineFactory
{
    /**
     * Create a new instance of <code>SubscriberAuxiliaryServiceHomePipelineFactory</code>.
     */
    protected SubscriberAuxiliaryServiceHomePipelineFactory()
    {
        super();
    }

    /**
     * Returns an instance of <code>SubscriberAuxiliaryServiceHomePipelineFactory</code>.
     *
     * @return An instance of <code>SubscriberAuxiliaryServiceHomePipelineFactory</code>.
     */
    public static SubscriberAuxiliaryServiceHomePipelineFactory instance()
    {
        if (instance == null)
        {
            instance = new SubscriberAuxiliaryServiceHomePipelineFactory();
        }
        return instance;
    }

    /**
     * {@inheritDoc}
     */
    public Home createPipeline(final Context ctx, final Context serverCtx) throws RemoteException, HomeException,
        IOException, AgentException
    {
        Home home = (Home) ctx.get(SubscriberAuxiliaryServiceXDBHome.class);
        
        home = new TFAAuxServiceProvisionHome(ctx,home);
        home = new SubscriberAuxiliaryServicesPersonalizedFeeUpdateHome(ctx, home);
        home = new SubscriberAuxServiceNextRecurChargeDateUpdateHome(ctx,home);
        home = new SubscriberProvisionableAuxiliaryServiceRemovingHome(ctx, home);
        home = new SubscriberProvisionableVpnAuxiliaryServiceUtilityHome(ctx, home);
        home = new SubscriberProvisionableAuxiliaryServiceUpdateHome(ctx, home);
        home = new SubscriberProvisionableAuxiliaryServiceCreatingHome(ctx, home);
        

        home = new ExtensionAssociationHome<AuxiliaryServiceExtension, SubscriberAuxiliaryService>(
                ctx, 
                AuxiliaryServiceExtension.class, 
                AuxiliaryServiceExtensionXInfo.AUXILIARY_SERVICE_ID, 
                SubscriberAuxiliaryServiceXInfo.AUXILIARY_SERVICE_IDENTIFIER,
                AuxiliaryService.class,
                home);

        home = new SubscriberAuxiliaryServiceIdentifierSettingHome(ctx, home);
        home = new CallingGroupECPActivationHome(ctx, home);
        home = new SubscriberAuxiliaryServiceBulkloadChargingHome(ctx, home);
        home = new SubscriberAuxiliaryServiceOMCreationHome(ctx, home);
        home = new SubscriptionAuxiliaryServiceDiscountEventHome(ctx, home);
        home = new SubscriberAuxiliaryServiceHistoryCreationHome(ctx, home);
        
        home = new ContextualizingHome(ctx, home);

        final CompoundValidator storeValidator = new CompoundValidator();
        storeValidator.add(SubscriberAuxiliaryServiceSubscriberValidator.instance());

        final CompoundValidator createValidator = new CompoundValidator();
        createValidator.add(storeValidator);
        createValidator.add(AuxiliaryServiceAssociationCreateStateValidator.instance());
        
        home = new ValidatingHome(createValidator, storeValidator, home);
        
        home = new SubscriberAuxiliaryServiceContextPrepareHome(ctx, home);
        return home;
    }

    /**
     * Singleton instance.
     */
    private static SubscriberAuxiliaryServiceHomePipelineFactory instance;
}
