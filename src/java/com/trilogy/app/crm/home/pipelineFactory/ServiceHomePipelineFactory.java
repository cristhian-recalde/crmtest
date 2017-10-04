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

import com.trilogy.app.crm.bean.IdentifierEnum;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServiceXMLHome;
import com.trilogy.app.crm.extension.ExtensionForeignKeyAdapter;
import com.trilogy.app.crm.extension.ExtensionHandlingHome;
import com.trilogy.app.crm.extension.service.ServiceExtension;
import com.trilogy.app.crm.extension.service.ServiceExtensionXInfo;
import com.trilogy.app.crm.extension.validator.SingleInstanceExtensionsValidator;
import com.trilogy.app.crm.home.MultiMonthRecurrenceIntervalValidator;
import com.trilogy.app.crm.home.PermissionAwarePermissionSettingHome;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.home.ServiceAdjustmentTypeCreationHome;
import com.trilogy.app.crm.home.ServiceFieldSettingHome;
import com.trilogy.app.crm.home.ServiceOneTimeSettingHome;
import com.trilogy.app.crm.home.VoicemailServiceValidator;
import com.trilogy.app.crm.sequenceId.IdentifierSettingHome;
import com.trilogy.app.crm.service.ServiceRemovalValidatorHome;
import com.trilogy.app.crm.service.home.ServiceIDSettingHome;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.IdentifierSequenceSupportHelper;
import com.trilogy.app.crm.support.StorageSupportHelper;
import com.trilogy.app.crm.technology.TechnologyAwareHome;
import com.trilogy.app.crm.validator.BlackberryExtensionSelectedValidator;
import com.trilogy.app.crm.validator.BlackberryServiceMandatoryFieldsValidator;
import com.trilogy.app.crm.validator.ServicePeriodValidator;
import com.trilogy.app.crm.xhome.adapter.ExtendedBeanAdapter;
import com.trilogy.app.crm.xhome.visitor.HomeMigrationVisitor;
import com.trilogy.framework.core.platform.CoreSupport;
import com.trilogy.framework.xhome.beans.CompoundValidator;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.True;
import com.trilogy.framework.xhome.home.AdapterHome;
import com.trilogy.framework.xhome.home.AuditJournalHome;
import com.trilogy.framework.xhome.home.ContextualizingHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.home.LRUCachingHome;
import com.trilogy.framework.xhome.home.SortingHome;
import com.trilogy.framework.xhome.home.ValidatingHome;
import com.trilogy.framework.xhome.msp.SpidAwareHome;
import com.trilogy.framework.xhome.xdb.XDBHome;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;

/**
 * Creates the service home decorators and put is in the context.
 * @author arturo.medina@redknee.com
 *
 */
public class ServiceHomePipelineFactory implements PipelineFactory
{

    /**
     * {@inheritDoc}
     */
    public Home createPipeline(final Context ctx, final Context serverCtx)
        throws HomeException, IOException, AgentException
    {
        LogSupport.info(ctx, this, "Installing the service home ");

        // Services used to be stored in the journal.  If journal contains bindHome
        // entry then the old Home will be non-null and we will migrate data from
        // the journal to the database.
        Home journalHome = CoreSupport.bindHome(ctx, Service.class);
        if (journalHome instanceof XDBHome
                || (journalHome instanceof HomeProxy && ((HomeProxy)journalHome).hasDecorator(XDBHome.class)))
        {
            journalHome = null;
        }
        
        Home serviceHome = StorageSupportHelper.get(ctx).createHome(ctx, Service.class, "SERVICE");
        serviceHome = new LRUCachingHome(ctx, Service.class, true, serviceHome);      
        serviceHome = new AuditJournalHome(ctx, serviceHome);

        // Install a home to adapt between business logic bean and data bean
        serviceHome = new AdapterHome(
                ctx, 
                serviceHome, 
                new ExtendedBeanAdapter<com.redknee.app.crm.bean.Service, com.redknee.app.crm.bean.core.Service>(
                        com.redknee.app.crm.bean.Service.class, 
                        com.redknee.app.crm.bean.core.Service.class));

        // Contextualize on the local side of the clustering home since the context field is transient
        serviceHome = new ContextualizingHome(ctx, serviceHome);
        
        serviceHome = new SortingHome(serviceHome);

        serviceHome = new ExtensionHandlingHome<ServiceExtension>(
                ctx, 
                ServiceExtension.class, 
                ServiceExtensionXInfo.SERVICE_ID, 
                serviceHome);
        serviceHome = new AdapterHome(serviceHome, 
                new ExtensionForeignKeyAdapter(
                        ServiceExtensionXInfo.SERVICE_ID));
        
        serviceHome = new PermissionAwarePermissionSettingHome(serviceHome);
        serviceHome = new ServiceAdjustmentTypeCreationHome(serviceHome);
        serviceHome = new ServiceOneTimeSettingHome(serviceHome);
        serviceHome = new ServiceFieldSettingHome(serviceHome);
        

        if (journalHome != null)
        {
            // Migrate data from the journal to the database if applicable, skipping some of the business logic provisioning homes.
            String failureFilename = CoreSupport.getFile(ctx, "MigrationFailures-Services.xml");
            try
            {
                Home backupHome = new ServiceXMLHome(ctx, failureFilename);
                journalHome.forEach(ctx, 
                        new HomeMigrationVisitor(
                                journalHome, 
                                serviceHome, 
                                backupHome,
                                false));
                if (!HomeSupportHelper.get(ctx).hasBeans(ctx, backupHome, True.instance()))
                {
                    backupHome.drop(ctx);
                }
            }
            catch (Exception e)
            {
                new MajorLogMsg(this, "Error(s) occurred migrating services from journal to database.  Correct the issues, then use the 'migration.failures.services' script and/or '" + failureFilename + "' failure XML file to complete the migration.", e).log(ctx);
            }
        }
        
        serviceHome = new TechnologyAwareHome(ctx, serviceHome);
        
        CompoundValidator validator = new CompoundValidator();
        validator.add(MultiMonthRecurrenceIntervalValidator.instance());
        validator.add(VoicemailServiceValidator.instance());
        validator.add(new SingleInstanceExtensionsValidator());
        validator.add(new BlackberryExtensionSelectedValidator());
        validator.add(new BlackberryServiceMandatoryFieldsValidator());
        validator.add(new ServicePeriodValidator());
        serviceHome = new ValidatingHome(serviceHome, validator);
        
        serviceHome = new SpidAwareHome(ctx, serviceHome);
        serviceHome = new ServiceRemovalValidatorHome(serviceHome);


            //serviceHome = new IdentifierSettingHome(ctx, serviceHome, IdentifierEnum.SERVICE_ID, null);
        serviceHome =  new ServiceIDSettingHome(ctx, serviceHome);
            IdentifierSequenceSupportHelper.get(ctx).ensureNextIdIsLargeEnough(ctx, IdentifierEnum.SERVICE_ID, serviceHome);
        
        LogSupport.info(ctx, this, "Service Home installed succesfully");

        return serviceHome;
    }
}
