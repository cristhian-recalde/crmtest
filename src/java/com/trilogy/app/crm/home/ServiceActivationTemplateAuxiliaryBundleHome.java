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
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.visitor.MapVisitor;
import com.trilogy.framework.xhome.visitor.Visitors;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bas.tps.ServiceActivationTemplate;
import com.trilogy.app.crm.bean.SctAuxiliaryBundle;
import com.trilogy.app.crm.bean.SctAuxiliaryBundleHome;
import com.trilogy.app.crm.bean.SctAuxiliaryBundleXInfo;
import com.trilogy.app.crm.support.SubscriberCreateTemplateSupport;

/**
 * This decorator handles the creation/deletion/updation of the SCT Auxiliary Bundle Associations.
 * 
 */
public class ServiceActivationTemplateAuxiliaryBundleHome extends HomeProxy implements ContextAware
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
    public ServiceActivationTemplateAuxiliaryBundleHome(final Context context, final Home delegate)
    {
        super(delegate);
    }

    /**
     *  Inherit Create method - Here we create the SCt- Aux service records.
     */
    public Object create(Context ctx, Object obj) throws HomeException
    {
        final ServiceActivationTemplate sct = (ServiceActivationTemplate) obj;
        final Collection bundles = sct.getAuxiliaryBundles();
        final Home sctAuxHome = (Home) ctx.get(SctAuxiliaryBundleHome.class);

        if (bundles != null && bundles.size() > 0)
        {
            final Iterator servIter = bundles.iterator();

            while (servIter.hasNext())
            {
                SctAuxiliaryBundle sctAux = null;
                final Object iterObj = servIter.next();
                if ((iterObj != null) && (iterObj instanceof SctAuxiliaryBundle))
                {
                    sctAux = (SctAuxiliaryBundle) iterObj;
                }
                else
                {
                    continue;
                }

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
                    new MinorLogMsg(this, "Error creating the SCTAuxiliaryBundle association - ",
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
    public void remove(Context ctx, Object obj) throws HomeException
    {
    final ServiceActivationTemplate sct = (ServiceActivationTemplate) obj;

        final Home sctAuxHome = (Home) ctx.get(SctAuxiliaryBundleHome.class);

        final long sctId = sct.getIdentifier();

        final Collection bundles = getSctAuxiliaryBundlesForSctId(ctx, Long.valueOf(sctId).toString());

        if (bundles != null)
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
            new DebugLogMsg(this, "SCT Aux Bundles being removed for SCT = " + sct.getIdentifier() + " are ==> "
                + bundles, null).log(ctx);
            }
            final Iterator servIter = bundles.iterator();
            while (servIter.hasNext())
            {
                final SctAuxiliaryBundle sctAux = (SctAuxiliaryBundle) servIter.next();
                try
                {
                    sctAuxHome.remove(ctx, sctAux);
                }
                catch (HomeException e)
                {
                    new MinorLogMsg(this, "Error removing the SCTAuxiliaryBundle association with Identifier = " 
                        + sctAux.getIdentifier(), e).log(getContext());
                }
            }
        }
        super.remove(ctx, obj);
    }

    /**
     * Inherit method.
     */
    public Object store(Context ctx, Object obj) throws HomeException
    {
        final Object obj1 = super.store(ctx, obj);
        final ServiceActivationTemplate sct = (ServiceActivationTemplate) obj1;
        final Home sctAuxHome = (Home) ctx.get(SctAuxiliaryBundleHome.class);

        final Collection chosenSctAuxAssociations = sct.getAuxiliaryBundles();

        final Collection existingBundles = SubscriberCreateTemplateSupport.getSctAuxiliaryBundles(ctx, sct.getIdentifier());

        final SortedSet selectedSet = new TreeSet();
        
        try
        {
            selectedSet.addAll(((List) Visitors.forEach(ctx, existingBundles, new MapVisitor(SctAuxiliaryBundleXInfo.AUXILIARY_BUNDLE_IDENTIFIER))));
        }
        catch (Exception e)
        {
            new MinorLogMsg(this, "cannot get auxiliary bundle id list from SCT", e).log(ctx);
        }
       
        // Check if the chosen Association already exists in DB, then update
        // otherwise create a new entry
        if (selectedSet != null && selectedSet.size() > 0)
        {
            final Iterator bundleIter = chosenSctAuxAssociations.iterator();
            while (bundleIter.hasNext())
            {
                final SctAuxiliaryBundle sctAux = (SctAuxiliaryBundle) bundleIter.next();

                if (selectedSet.contains(Long.valueOf(sctAux.getAuxiliaryBundleIdentifier())))
                {
                    assignIdentifier(existingBundles, sctAux);
                    try
                    {
                        sctAuxHome.store(ctx, sctAux);
                    }
                    catch (HomeException e) 
                    {
                    new MinorLogMsg(this, "Error Updating the SCTAuxiliaryBundle association 1- ",
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
                    new MinorLogMsg(this, "Error Updating the SCTAuxiliaryBundle association 2- ", e).log(getContext());
                        throw e;
                    }
                }
            }

            final HashSet oldSet = new HashSet(existingBundles);
            final HashSet newSet = new HashSet(chosenSctAuxAssociations);

            if (oldSet.size() > newSet.size())
            {
                oldSet.removeAll(newSet);
                removeUnwantedAssociations(ctx,sctAuxHome, oldSet);
            }
            else if (oldSet.size() < newSet.size())
            {
                newSet.removeAll(oldSet);
                addWantedAssociations(ctx,sctAuxHome, newSet);
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
        else
        {
            final Iterator bundleIter = chosenSctAuxAssociations.iterator();
            while (bundleIter.hasNext())
            {
                final SctAuxiliaryBundle sctAux = (SctAuxiliaryBundle) bundleIter.next();
                
                try
                {
                    sctAuxHome.create(ctx, sctAux);
                }
                catch (HomeException e) 
                {
                    new MinorLogMsg(this, "Error Updating the SCTAuxiliaryBundle association 3- ", e).log(getContext());
                    throw e;
                }
            }
        }
        return sct;
    }
    
    /**
     * Every time an update is made the SCT, the pipe line assigns new id's to the selections already existing,
     * so for that reason, I had to explicitly modify the id's for selections
     * @param existingBundles SctAuxiliaryBundle Collection
     * @param sctAux SctAuxiliaryBundle object
     */
    private void assignIdentifier(Collection existingBundles, final SctAuxiliaryBundle sctAux)
    {
        if (existingBundles != null && existingBundles.size() > 0 && sctAux != null)
        {
            final Iterator iter = existingBundles.iterator();
            while (iter.hasNext())
            {
                final SctAuxiliaryBundle bundle = (SctAuxiliaryBundle) iter.next();
                if (bundle.getAuxiliaryBundleIdentifier() == sctAux.getAuxiliaryBundleIdentifier())
                {
                    sctAux.setIdentifier(bundle.getIdentifier());                    
                    bundle.setCreated(sctAux.getCreated());
                    break;
                }
            }
        }
    }
    
    /**
     * This method removes the associations for the corresponding SCT
     * @param ctx Context object
     * @param sctAuxHome SctAuxiliaryBundleHome object
     * @param associations Collection of SCTAux associations
     */
    private void removeUnwantedAssociations(final Context ctx, final Home sctAuxHome, final Set associations)
    {
        Object[] coll = associations.toArray();
        for (int i = 0; i < coll.length; i++)
        {
            try
            {
                final Object obj = (Object) coll[i];
                if (obj instanceof SctAuxiliaryBundle)
                {
                    sctAuxHome.remove(ctx, (SctAuxiliaryBundle)obj);
                }
            }
            catch (HomeException e)
            {
                new MinorLogMsg(this, "Error Removing the SCTAuxiliaryBundle association as " 
                    + "part of SCT-AuxiliaryBundle Updation - ", e).log(getContext());
            }
    }
    }
    
    /**
     * This method add the wanted associations to the database
     * @param ctx context object
     * @param sctAuxHome SctAuxiliaryBundleHome object
     * @param associations Collection of wanted sct-aux associations
     */
    private void addWantedAssociations(final Context ctx, final Home sctAuxHome, final Set associations)
    { 
        Object[] coll = associations.toArray();
        for (int i = 0; i < coll.length; i++)
        {
            try
            {
            Object obj = (Object) coll[i];
            if (obj instanceof SctAuxiliaryBundle)
            {
                sctAuxHome.create(ctx, (SctAuxiliaryBundle)obj);
            }
            }
            catch (HomeException e)
            {
            new MinorLogMsg(this, "Error Creating a new SCTAuxiliaryBundle association as part of "
            + "SCT-AuxiliaryBundle Updation - ", e).log(getContext());
            }
        }
    }

    /**
     * This method filters and returns the SCTAuxiliaryBundle objects for the given
     * Service Activation Template ID
     * @param ctx Context object
     * @param sctId Service Activation Template Id
     * @return Collection object holding the SctAuxiliaryBundle objects
     * @throws com.redknee.framework.xhome.home.HomeException Exception thrown during the query operation
     * @throws com.redknee.framework.xhome.home.HomeInternalException Exception thrown during the query operation
     */
    private Collection getSctAuxiliaryBundlesForSctId(final Context ctx, final String sctId)
                                    throws HomeInternalException, HomeException
    { 
        final Home sctAuxHome = (Home) ctx.get(SctAuxiliaryBundleHome.class);
        Collection bundles = null;
        try
        {
            bundles = sctAuxHome.where(ctx, 
                new EQ(SctAuxiliaryBundleXInfo.SCT_IDENTIFIER, Long.valueOf(sctId))).selectAll(ctx);
        }
        catch (HomeException he)
        {
            new MinorLogMsg(this,
                "Exception while getting the sct auxiliary bundles for the sct id = " + sctId, he).log(ctx);
            bundles = new ArrayList();
        }
        return bundles;
    }
}
