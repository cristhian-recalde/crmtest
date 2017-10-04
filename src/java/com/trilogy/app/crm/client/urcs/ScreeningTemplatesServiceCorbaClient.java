/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee, no unauthorised use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the licence agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.client.urcs;

import static com.redknee.app.crm.client.CorbaClientTrapIdDef.SCREENINGTEMPLATE_PROV_SVC_DOWN;
import static com.redknee.app.crm.client.CorbaClientTrapIdDef.SCREENINGTEMPLATE_PROV_SVC_UP;

import java.util.ArrayList;
import java.util.Collection;

import com.trilogy.app.crm.bean.ScreeningTemplate;
import com.trilogy.app.crm.client.AbstractCrmClient;
import com.trilogy.app.crm.client.RemoteServiceException;
import com.trilogy.app.urcs.ppsm.ScreeningTemplateStateEnum;
import com.trilogy.app.urcs.ppsm.ScreeningTemplates;
import com.trilogy.app.urcs.ppsm.param.Parameter;
import com.trilogy.app.urcs.promotion.PromotionManagement;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.LogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;
import com.trilogy.util.snippet.log.Logger;

/**
 * Screening templates CORBA client.
 *
 * @author Marcio Marques
 * @since 8.5
 */
public class ScreeningTemplatesServiceCorbaClient  extends AbstractCrmClient<ScreeningTemplates>
implements ScreeningTemplatesServiceClient
{
    /**
     * Name of the CORBA client.
     */
    private static final String CLIENT_NAME = "ScreeningTemplatesClient";

    private static final Class<ScreeningTemplates> SERVICE_TYPE = ScreeningTemplates.class;

    /**
     * Service description.
     */
    private static final String SERVICE_DESCRIPTION = "CORBA client for Screening Templates retrieval services";

    public ScreeningTemplatesServiceCorbaClient(final Context ctx)
    {
        // TODO change the trap IDs
        super(ctx, CLIENT_NAME, SERVICE_DESCRIPTION, SERVICE_TYPE,
        SCREENINGTEMPLATE_PROV_SVC_DOWN, SCREENINGTEMPLATE_PROV_SVC_UP);
    }

    public Collection<ScreeningTemplate> retrieveScreeningTemplates(final Context ctx, final int spid) throws RemoteServiceException
    {
        final ScreeningTemplateStateEnum[] states = new ScreeningTemplateStateEnum[] {ScreeningTemplateStateEnum.ACTIVE, ScreeningTemplateStateEnum.DEPRECATED};
        return retrieveScreeningTemplates(ctx, spid, states);
    }

    public Collection<ScreeningTemplate> retrieveActiveScreeningTemplates(final Context ctx, final int spid) throws RemoteServiceException
    {
        final ScreeningTemplateStateEnum[] states = new ScreeningTemplateStateEnum[] {ScreeningTemplateStateEnum.ACTIVE};
        return retrieveScreeningTemplates(ctx, spid, states);
    }

    public Collection<ScreeningTemplate> retrieveDeprecatedScreeningTemplates(final Context ctx, final int spid) throws RemoteServiceException
    {
        final ScreeningTemplateStateEnum[] states = new ScreeningTemplateStateEnum[] {ScreeningTemplateStateEnum.DEPRECATED};
        return retrieveScreeningTemplates(ctx, spid, states);
    }

    private Collection<ScreeningTemplate> retrieveScreeningTemplates(final Context ctx, final int spid, final ScreeningTemplateStateEnum[] states) throws RemoteServiceException
    {
        final LogMsg pm = new PMLogMsg(getModule(), "URCS.ScreeningTemplatesService", "Query screening templates");
        final Collection<ScreeningTemplate> result = new ArrayList<ScreeningTemplate>();
        try
        {
            final ScreeningTemplates service = getService();
            if (service == null)
            {
                final RemoteServiceException exception = new RemoteServiceException((short) FAILED,
                        "Cannot retrieve " + CLIENT_NAME + " CORBA service.");
                Logger.minor(ctx, this, exception.getMessage(), exception);
                throw exception;
            }
        
            if (Logger.isInfoEnabled())
            {
                final StringBuilder msg = new StringBuilder();
                msg.append("Sending CORBA request ScreeningTemplates.retrieveScreeningTemplates() to URCS: ");
                msg.append("spid = ");
                msg.append(spid);
                msg.append(", states = ");
                msg.append(states.toString());
                Logger.info(ctx, this, msg.toString());
            }
        
            final Parameter[] inputParamSet = new Parameter[0];
            com.redknee.app.urcs.ppsm.ScreeningTemplate[] screeningTemplates = service.retrieveScreeningTemplates(
                    spid, states, inputParamSet);
        
            for (int i=0; i<screeningTemplates.length; i++)
            {
                ScreeningTemplate sc = new ScreeningTemplate();
                sc.setIdentifier(screeningTemplates[i].identifier);
                sc.setSpid(screeningTemplates[i].spid);
                sc.setName(screeningTemplates[i].name);
                sc.setDescription(screeningTemplates[i].description);
                if (ScreeningTemplateStateEnum.ACTIVE.equals(screeningTemplates[i].state))
                {
                    sc.setEnabled(true);
                }
                else
                {
                    sc.setEnabled(false);
                }
                result.add(sc);
                
            }
        }
        catch (final org.omg.CORBA.SystemException e)
        {
            final StringBuilder msgBldr = new StringBuilder();
            msgBldr.append("CORBA communication with URCS Screening Templates Service server failed");
            final String msg = msgBldr.toString();
            Logger.minor(ctx, this, msg, e);
            final RemoteServiceException exception = new RemoteServiceException((short) COMMUNICATION_FAILURE,
                    msg, e);
            throw exception;
        }
        catch (final Throwable t)
        {
            final StringBuilder msgBldr = new StringBuilder();
            msgBldr.append("Unexpected failure in CORBA communication with URCS Screening Templates Service");
            final String msg = msgBldr.toString();
            Logger.minor(ctx, this, msg, t);
            final RemoteServiceException exception = new RemoteServiceException((short) COMMUNICATION_FAILURE,
                    msg, t);
            throw exception;
        }
        finally
        {
            pm.log(ctx);
        }
        
        return result;
        
    }
    
    
    /**
     * Used for Logging
     * @return
     */
    private String getModule()
    {
        return this.getClass().getName();
    }
}
