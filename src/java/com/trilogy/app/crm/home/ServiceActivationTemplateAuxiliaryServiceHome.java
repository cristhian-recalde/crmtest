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
 *
 */
package com.trilogy.app.crm.home;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bas.tps.ServiceActivationTemplate;
import com.trilogy.app.crm.bean.SctAuxiliaryService;
import com.trilogy.app.crm.bean.SctAuxiliaryServiceHome;
import com.trilogy.app.crm.bean.SctAuxiliaryServiceXInfo;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.SubscriberCreateTemplateSupport;

/**
 * This decorator handles the creation/deletion/updation of the SCT Auxiliary Service Associations.
 * 
 * @author msubramanyam
 */
public class ServiceActivationTemplateAuxiliaryServiceHome extends HomeProxy implements ContextAware
{
    /**
     * default Serial verions Id
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor method with two params context and home delegate
     * @param context Context object
     * @param delegate Home object
     */
    public ServiceActivationTemplateAuxiliaryServiceHome(final Context context, final Home delegate)
    {
        super(delegate);
    }

    /**
     *  Inherit Create method - Here we create the SCt- Aux service records.
     */
    @Override
    public Object create(Context ctx, Object obj) throws HomeException
    {
        final ServiceActivationTemplate sct = (ServiceActivationTemplate) obj;

        final Collection<SctAuxiliaryService> services = sct.getAuxiliaryServices();
        if (services != null && services.size() > 0)
        {
            final Home sctAuxHome = (Home) ctx.get(SctAuxiliaryServiceHome.class);
            for (SctAuxiliaryService sctAux : services)
            {
                /*if ((sctAux.getSctIdentifier() == null) 
                        || (sctAux.getSctIdentifier().length() <= 0) 
                        || sctAux.getSctIdentifier().equals("0")
                        || ! sctAux.getSctIdentifier().equals("" + sct.getIdentifier()))*/
                if (sctAux.getSctIdentifier() <= 0 
                	|| sctAux.getSctIdentifier() != sct.getIdentifier())
                {
                    sctAux.setSctIdentifier(sct.getIdentifier());
                }

                try
                {
                    sctAuxHome.create(ctx, sctAux);
                }
                catch (HomeException he)
                {
                    new MinorLogMsg(this, "Error creating the SCTAuxiliaryService association - ",
                	    he).log(getContext());
                    throw he;
                }
            }
        }

        return super.create(ctx, sct);
    }

    /**
     * Inherit remove method
     * 
     */
    @Override
    public void remove(Context ctx, Object obj) throws HomeException
    {
        final ServiceActivationTemplate sct = (ServiceActivationTemplate) obj;

        final long sctId = sct.getIdentifier();

        final Collection<SctAuxiliaryService> services = getSctAuxiliaryServicesForSctId(ctx, sctId);

        if (services != null)
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "SCT Aux Services being removed for SCT = " + sctId + " are ==> " + services, null).log(ctx);
            }

            final Home sctAuxHome = (Home) ctx.get(SctAuxiliaryServiceHome.class);
            for (SctAuxiliaryService sctAux : services)
            {
                try
                {
                    sctAuxHome.remove(ctx, sctAux);
                }
                catch (HomeException e)
                {
                    new MinorLogMsg(this, "Error removing the SCTAuxiliaryService association with Identifier = " 
                	    + sctAux.getIdentifier(), e).log(getContext());
                }
            }
        }
        super.remove(ctx, obj);
    }

    /**
     * Inherit method.
     */
    @Override
    public Object store(Context ctx, Object obj) throws HomeException
    {
        final ServiceActivationTemplate sct = (ServiceActivationTemplate) super.store(ctx, obj);
        
        final Collection<SctAuxiliaryService> chosenSctAuxAssociations = sct.getAuxiliaryServices();

        final Collection<SctAuxiliaryService> existingServices = SubscriberCreateTemplateSupport.getSctAuxiliaryServices(ctx, sct.getIdentifier());

        final Map<Long, SctAuxiliaryService> selectedMap = SubscriberCreateTemplateSupport.getSelectionMap(existingServices);

        // Check if the chosen Association already exists in DB, then update
        // otherwise create a new entry
        final Home sctAuxHome = (Home) ctx.get(SctAuxiliaryServiceHome.class);
        for (SctAuxiliaryService sctAux : chosenSctAuxAssociations)
        {
            if (selectedMap.containsKey(sctAux.getAuxiliaryServiceIdentifier()))
            {
                // Every time an update is made the SCT, the pipe line assigns new id's to the selections already existing,
                // so for that reason, I had to explicitly modify the id's for selections
                SctAuxiliaryService existingService = selectedMap.get(sctAux.getAuxiliaryServiceIdentifier());
                sctAux.setIdentifier(existingService.getIdentifier());                    
                sctAux.setCreated(existingService.getCreated());
                try
                {
                    sctAuxHome.store(ctx, sctAux);
                }
                catch (HomeException e) 
                {
                    new MinorLogMsg(this, "Error updating the SCTAuxiliaryService association - ",
                            e).log(getContext());
                    throw e;
                }
            }
            else
            {
                try
                {
                    sctAuxHome.create(ctx, sctAux);
                }
                catch (HomeException e) 
                {
                    new MinorLogMsg(this, "Error creating the SCTAuxiliaryService association - ", e).log(getContext());
                    throw e;
                }
            }
        }

        if (selectedMap != null && selectedMap.size() > 0)
        {
            final Set<SctAuxiliaryService> oldSet = new HashSet<SctAuxiliaryService>(existingServices);
            final Set<SctAuxiliaryService> newSet = new HashSet<SctAuxiliaryService>(chosenSctAuxAssociations);

            if (oldSet.size() > newSet.size())
            {
                oldSet.removeAll(newSet);
                removeUnwantedAssociations(ctx, sctAuxHome, oldSet);
            }
            else if (oldSet.size() < newSet.size())
            {
                newSet.removeAll(oldSet);
                addWantedAssociations(ctx, sctAuxHome, newSet);
            }
            else if (oldSet.size() == newSet.size())
            {
                oldSet.removeAll(newSet);
                if (oldSet.size() > 0)
                {
                    removeUnwantedAssociations(ctx, sctAuxHome, oldSet);
                }
            }
        }
        
        return sct;
    }
    
    /**
     * This method add the wanted associations to the database
     * @param ctx context object
     * @param sctAuxHome SctAuxiliaryServiceHome object
     * @param associations Collection of wanted sct-aux associations
     */
    private void addWantedAssociations(final Context ctx, final Home sctAuxHome, final Set<SctAuxiliaryService> associations)
    { 
        for (SctAuxiliaryService association : associations)
        {
            try
            {
                sctAuxHome.create(ctx, association);
            }
            catch (HomeException e)
            {
                new MinorLogMsg(this, "Error Creating a new SCTAuxiliaryService association as part of "
                        + "SCT-AuxiliaryService Updation - ", e).log(getContext());
            }
        }
    }
    
    /**
     * This method removes the associations for the corresponding SCT
     * @param ctx Context object
     * @param sctAuxHome SctAuxiliaryServiceHome object
     * @param associations Collection of SCTAux associations
     */
    private void removeUnwantedAssociations(final Context ctx, final Home sctAuxHome, final Set<SctAuxiliaryService> associations)
    {
        for (SctAuxiliaryService association : associations)
        {
            try
            {
                sctAuxHome.remove(ctx, association);
            }
            catch (HomeException e)
            {
                new MinorLogMsg(this, "Error Removing the SCTAuxiliaryService association as " 
                        + "part of SCT-AuxiliaryService Updation - ", e).log(getContext());
            }
        }
    }

    /**
     * This method filters and returns the SCTAuxiliaryService objects for the given
     * Service Activation Template ID
     * @param ctx Context object
     * @param sctId Service Activation Template Id
     * @return Collection object holding the SctAuxiliaryService objects
     */
    private Collection<SctAuxiliaryService> getSctAuxiliaryServicesForSctId(final Context ctx, final long sctId)
    { 
        Collection<SctAuxiliaryService> services = null;
        try
        {
            services = HomeSupportHelper.get(ctx).getBeans(ctx, SctAuxiliaryService.class, new EQ(SctAuxiliaryServiceXInfo.SCT_IDENTIFIER, sctId));
        }
        catch (HomeException he)
        {
            new MinorLogMsg(this,
        	    "Exception while getting the sct auxiliary services for the sct id = " + sctId, he).log(ctx);
            services = new ArrayList<SctAuxiliaryService>();
        }
        return services;
    }
}
