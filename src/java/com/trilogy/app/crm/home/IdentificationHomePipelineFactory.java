package com.trilogy.app.crm.home;

import java.io.IOException;

import com.trilogy.app.crm.bean.Identification;
import com.trilogy.app.crm.bean.IdentificationHome;
import com.trilogy.app.crm.bean.IdentificationTransientHome;
import com.trilogy.app.crm.bean.IdentificationXMLHome;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.StorageSupportHelper;
import com.trilogy.app.crm.xhome.home.ConfigShareTotalCachingHome;
import com.trilogy.app.crm.xhome.visitor.HomeMigrationVisitor;
import com.trilogy.framework.core.platform.CoreSupport;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.True;
import com.trilogy.framework.xhome.home.AuditJournalHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.NotifyingHome;
import com.trilogy.framework.xhome.home.SortingHome;
import com.trilogy.framework.xhome.msp.SpidAwareHome;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;


public class IdentificationHomePipelineFactory implements PipelineFactory
{
    /**
     * {@inheritDoc}
     */
    @Override
    public Home createPipeline(final Context ctx, final Context serverCtx)
        throws HomeException, IOException, AgentException
    {
        LogSupport.info(ctx, this, "Installing the Identification Home ");
        
        // Services used to be stored in the journal.  If journal contains bindHome 
        // entry then the old Home will be non-null and we will migrate data from 
        // the journal to the database.
        Home oldHome = CoreSupport.bindHome(ctx, Identification.class); 
        
        Home identificationHome = StorageSupportHelper.get(ctx).createHome(ctx, Identification.class, "IDENTIFICATION");
        identificationHome = new NotifyingHome(identificationHome);
		identificationHome =
		    new ConfigShareTotalCachingHome(ctx, new IdentificationTransientHome(ctx),
		        identificationHome, false);
        identificationHome = new AuditJournalHome(ctx, identificationHome);
        identificationHome = new SortingHome(ctx, identificationHome);
        identificationHome = new SpidAwareHome(ctx, identificationHome);
        
        if (oldHome!=null)
        {
            String failureFilename = CoreSupport.getFile(ctx, "MigrationFailures-Identifications.xml");
            try
            {
                Home backupHome = new IdentificationXMLHome(ctx, failureFilename);
                oldHome.forEach(ctx, new HomeMigrationVisitor(oldHome, identificationHome, backupHome, false));
                if (!HomeSupportHelper.get(ctx).hasBeans(ctx, backupHome, True.instance()))
                {
                    backupHome.drop(ctx);
                }
            }
            catch (Exception e)
            {
                new MajorLogMsg(
                        this,
                        "Error(s) occurred migrating identifications from journal to database.  Correct the issues, then use the 'migration.failures.identifications' script and/or '"
                                + failureFilename + "' failure XML file to complete the migration.", e).log(ctx);
            } 
            
        }

        ctx.put(IdentificationHome.class, identificationHome);
        
        LogSupport.info(ctx, this, "Identification Home installed succesfully");

        return identificationHome;
    }
}
