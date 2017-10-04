package com.trilogy.app.crm.poller.agent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporteeSubExtension;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporteeSubExtensionHome;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporteeSubExtensionXInfo;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporterScreenTemplateHome;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporterScreenTemplateXInfo;
import com.trilogy.app.crm.poller.Constants;
import com.trilogy.app.crm.poller.FilterOutException;
import com.trilogy.app.crm.poller.ProcessorInfo;
import com.trilogy.app.crm.poller.VRAPoller;
import com.trilogy.app.crm.poller.event.CRMProcessor;
import com.trilogy.app.crm.poller.event.CRMProcessorSupport;
import com.trilogy.app.crm.poller.event.PPSMScreeningTemplateRemovalProcessor;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;


public class PPSMScreeningTemplateRemovalAgent implements ContextAgent, Constants
{
    
    public PPSMScreeningTemplateRemovalAgent(CRMProcessor processor)
    {
        super();
        processor_ = processor;
    }

    public void execute(Context ctx) throws AgentException
    {        
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "execute()");

        List params = new ArrayList();
        final ProcessorInfo info = (ProcessorInfo) ctx.get(ProcessorInfo.class);
        Common.OM_PPSM_SCREENING_TEMPLATE_REMOVAL_7018.attempt(ctx);
        
        try
        {
            if (Integer.parseInt(info.getErid()) != PPSMScreeningTemplateRemovalProcessor.PPSM_SCREENING_TEMPLATE_REMOVAL_ER_IDENTIFIER)
            {
                LogSupport.minor(ctx, this, "Unknown ER for this processor. PPSM Screening Template Removal Processor can only process ER7018. Unknown ER: " + info.getErid());
                return;
            }
            
            try 
            {
                CRMProcessorSupport.makeArray(ctx, params, info.getRecord(), info.getStartIndex(), ',', info.getErid(),this);
            } 
            catch ( FilterOutException e)
            {
                return; 
            }
            
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Parsed contents of PPSM Screening Template Removal ER: " + params, null).log(ctx);
            }

            final int spid = CRMProcessorSupport.getInt(ctx, CRMProcessorSupport.getField(params, PPSM_SCREENING_TEMPLATE_REMOVAL_SPID_INDEX), -1);
            final long templateId = CRMProcessorSupport.getLong(ctx, CRMProcessorSupport.getField(params, PPSM_SCREENING_TEMPLATE_REMOVAL_TEMPLATE_ID_INDEX), -1);
            final String templateName = CRMProcessorSupport.getField(params, PPSM_SCREENING_TEMPLATE_REMOVAL_TEMPLATE_NAME_INDEX);
            final String referenceId = CRMProcessorSupport.getField(params, PPSM_SCREENING_TEMPLATE_REMOVAL_REFERENCE_ID_INDEX);

            if (templateId!=-1)
            {
                removeTemplateFromSupportees(ctx, templateId, templateName);

                removeTemplateFromSupporters(ctx, templateId);
            }
            else
            {
                throw new Exception("Invalid template identifier ("+CRMProcessorSupport.getField(params, PPSM_SCREENING_TEMPLATE_REMOVAL_TEMPLATE_ID_INDEX)+").");
            }
            
            Common.OM_PPSM_SCREENING_TEMPLATE_REMOVAL_7018.success(ctx);

        } 
        catch (final Throwable t)
        {
            new MinorLogMsg(this, "Could not process PPSM Screening Template Removal ER (ER7018) due to the following exception: " + t.getMessage(), t).log(ctx);
            processor_.saveErrorRecord(ctx, info.getRecord());            
            Common.OM_PPSM_SCREENING_TEMPLATE_REMOVAL_7018.failure(ctx);
        } 
        finally
        {
            pmLogMsg.log(ctx);
        }
    }  
    
    private void removeTemplateFromSupporters(final Context ctx, final long templateId) throws HomeException
    {
        Home supporterMappingHome = (Home) ctx.get(PPSMSupporterScreenTemplateHome.class);
        supporterMappingHome.removeAll(new EQ(PPSMSupporterScreenTemplateXInfo.IDENTIFIER, Long.valueOf(templateId)));
    }
    
    private void removeTemplateFromSupportees(final Context ctx, final long templateId, final String templateName)
    {
        Home supporteeHome = (Home) ctx.get(PPSMSupporteeSubExtensionHome.class);
        try
        {
            Collection<PPSMSupporteeSubExtension> collection = supporteeHome.select(new EQ(PPSMSupporteeSubExtensionXInfo.SCREENING_TEMPLATE, Long.valueOf(templateId)));
            
            if (collection.size()>0)
            {
                StringBuilder sb = new StringBuilder();
                sb.append(collection.size());
                sb.append(" PPSM Supportee extensions found using screening template '");
                sb.append(templateId);
                sb.append(" - ");
                sb.append(templateName);
                sb.append("'. Updating extensions to point to template -1.");
                
                LogSupport.info(ctx, this, sb.toString()); 
            }
            
            for (PPSMSupporteeSubExtension extension : collection)
            {
                try
                {
                    extension.setScreeningTemplate(-1);
                    supporteeHome.store(ctx, extension);
                }
                catch (HomeException e)
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Unable to remove screening template '");
                    sb.append(templateId);
                    sb.append(" - ");
                    sb.append(templateName);
                    sb.append("' from PPSM Supportee extension for subscriber '");
                    sb.append(extension.getSubId());
                    sb.append("'.");
                    LogSupport.minor(ctx, this, sb.toString());
                }
            }  
        }
        catch (HomeException e)
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Unable to retrieve PPSM Supportee extensions with screening template '");
            sb.append(templateId);
            sb.append(" - ");
            sb.append(templateName);
            sb.append("': ");
            sb.append(e.getMessage());
            LogSupport.minor(ctx, this, sb.toString(), e);
        }
    }
    
    private CRMProcessor processor_ = null;
    private static final String PM_MODULE = PPSMScreeningTemplateRemovalProcessor.class.getName();
    
}
