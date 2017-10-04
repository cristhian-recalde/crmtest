/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.servlet.ServletRequest;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.MapVisitor;
import com.trilogy.framework.xhome.visitor.Visitors;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.NoActionsWebControl;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.ViewModeEnum;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bas.tps.ServiceActivationTemplate;
import com.trilogy.app.crm.bean.AuxiliaryBundleSelection;
import com.trilogy.app.crm.bean.AuxiliaryBundleSelectionTableWebControl;
import com.trilogy.app.crm.bean.IdentifierEnum;
import com.trilogy.app.crm.bean.SctAuxiliaryBundle;
import com.trilogy.app.crm.bean.SctAuxiliaryBundleXInfo;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.bean.core.PricePlan;
import com.trilogy.app.crm.bundle.BundleProfileHome;
import com.trilogy.app.crm.bundle.BundleProfileXInfo;
import com.trilogy.app.crm.bundle.BundleSegmentEnum;
import com.trilogy.app.crm.bundle.SubscriberBundleSupport;
import com.trilogy.app.crm.support.IdentifierSequenceSupportHelper;
import com.trilogy.app.crm.support.PricePlanSupport;
import com.trilogy.app.crm.support.SubscriberCreateTemplateSupport;

/**
 * 
 */
public class SctAuxiliaryBundleSelectionWebControl extends ProxyWebControl {

    /**
     * @param arg0
     */
    public SctAuxiliaryBundleSelectionWebControl() 
    {
        super(new NoActionsWebControl(createTableWebControl()));
    }

    /**
     * Creates the specialized TableWebControl that modifies the way the default
     * TableWebControl handles the accept/reject check boxes that appear in the
     * first column of the table.
     *
     * @return A specialized TableWebControl.
     */
    private static WebControl createTableWebControl()
    {
        final WebControl control =
            new AuxiliaryBundleSelectionTableWebControl()
            {
                // Used to look-up the actual mode in the context.
                public static final String CUSTOM_MODE_KEY = "REALMODE";

                // INHERIT
                @Override
                public void toWeb(
                    Context context,
                    final PrintWriter out,
                    final String name,
                    final Object obj)
                {
                    // If the mode is DISPLAY, then the TableWebController will
                    // not add the column of accept/reject check boxes.
                    final int mode = context.getInt("MODE", DISPLAY_MODE);

                    if (mode == DISPLAY_MODE)
                    {
                        context = context.createSubContext();
                        context.put(CUSTOM_MODE_KEY, mode);
                        context.put("MODE", EDIT_MODE);
                    }
                    // NUM_OF_BLANKS controls the number of blank entries that
                    // appear at the bottom of the list (meant for adding to the
                    // list).
                    context.put(NUM_OF_BLANKS, -1);

                    super.toWeb(context, out, name, obj);
                }

                // INHERIT
                @Override
                public void outputCheckBox(
                    final Context context,
                    final PrintWriter out,
                    final String name,
                    final Object bean,
                    final boolean isChecked)
                {
                    // We're overriding the normal usage of the accept/reject
                    // check boxes to more explicitly associate
                    // acceptance/rejection with the beans.  The motivation for
                    // this behavior is that many table entries need to appear
                    // that are not selected.  Normal behavior of the table is
                    // to remove those entries that are not selected.
                    final AuxiliaryBundleSelection selection = (AuxiliaryBundleSelection) bean;

                    // We always need the hidden value of the identifier of the
                    // bean.
                    out.print("<input type=\"hidden\" name=\"");
                    out.print(name);
                    out.print(SEPERATOR);
                    out.print("selectionIdentifier\" value=\"");
                    out.print(selection.getSelectionIdentifier());
                    out.println("\" />");

                    final int mode = context.getInt(CUSTOM_MODE_KEY, EDIT_MODE);
                    if (mode == DISPLAY_MODE && selection.isChecked())
                    {
                        // In the case that the actual mode is DISPLAY, we don't
                        // want editable check boxes.  Instead, draw an "X".
                        out.print(" <td> &nbsp;<b>X</b><input type=\"hidden\" name=\"");
                        out.print(name);
                        out.print(SEPERATOR);
                        //out.print("_enabled\" value=\"X\" />");
                        out.print("_enabled\" checked />");
                        out.println("</td>");
                    }
                    else if (mode == DISPLAY_MODE)
                    {
                        // In the case that the actual mode is DISPLAY, we don't
                        // want editable check boxes.  Instead, draw a blank.
                        out.print("<td>&nbsp;</td>");
                    }
                    else
                    {
                        // In any other actual mode besides DISPLAY, the default
                        // TableWebControl does what we want.
                        super.outputCheckBox(context, out, name, bean, selection.isChecked());
                    }
                }
            };

        return control;
    }

//  INHERIT
    @Override
    public void toWeb(
        Context context,
        final PrintWriter out,
        final String name,
        final Object obj)
    {
        context = context.createSubContext();
        context.setName(this.getClass().getName());

        Object obj1 = context.get(AbstractWebControl.BEAN);
        
        ServiceActivationTemplate sct = (ServiceActivationTemplate) obj1;        
        
        Collection associations = (Collection) obj;
        
       
        if ((associations == null) || (associations.size() <= 0))
        {
            associations = SubscriberCreateTemplateSupport.getSctAuxiliaryBundles(context, sct);
        }

        Collection bundles = null;
        Collection pricePlanSet = getPricePlanBundles(context, sct);
        
        if (context.getInt("MODE", DISPLAY_MODE) != CREATE_MODE)
        {
            if (associations == null)
            {
                associations = SubscriberCreateTemplateSupport.getSctAuxiliaryBundles(context, sct);   
                if (associations == null)
                {
                    associations = new ArrayList();
                }
            }
            
            bundles =  getSelectableAuxiliaryBundles(context, sct, associations, pricePlanSet);    
        }
        else
        {
            if(LogSupport.isDebugEnabled(context))
            {
                new DebugLogMsg(this,"Auxiliary bundles collection = " + associations,null).log(context);
            }

            if (associations == null || (associations != null && associations.size() <= 0) ||
                (associations != null && (associations.size() > 0) && (associations.iterator().next() instanceof SctAuxiliaryBundle)))
            {
                if (context.getInt("MODE", AbstractWebControl.DISPLAY_MODE) == AbstractWebControl.CREATE_MODE)
                {
                    Home home = (Home)context.get(BundleProfileHome.class);
                    home = SubscriberBundleSupport.filterExpiredOneTimeBundles(context, home);
                    try
                    {
                        And predicate = new And();
                        predicate.add(new EQ(BundleProfileXInfo.SPID, Integer.valueOf(sct.getSpid())));
                        predicate.add(new EQ(BundleProfileXInfo.AUXILIARY, Boolean.TRUE));
                        associations = home.where(context, predicate).selectAll(context);
                    }
                    catch (HomeException he)
                    {
                        new MinorLogMsg(this, "Error while retrieving all the Aux bundles for spid =" + sct.getSpid(), he).log(context);
                    }
                }
            }
                
            bundles = getSelectableAuxiliaryBundles(context, null, associations, pricePlanSet);
        }

        Context subCtx = context.createSubContext();
        subCtx.put("AuxiliaryBundleSelection.selectionIdentifier.mode",ViewModeEnum.READ_ONLY);
        super.toWeb(subCtx, out, name, bundles);
    }
    
    
    // INHERIT
    @Override
    public Object fromWeb(
        Context context,
        final ServletRequest request,
        final String name)
    {
        // The web control will only return to us a collection of the
        // AuxiliaryBundleSelections that are checked on the SCT
        // profile.  From that list, we need to derive a list of
        // SctAuxiliaryBundles to return to the SCT.       
        final ArrayList selections = new ArrayList();

        super.fromWeb(context, selections, request, name);   
        ServiceActivationTemplate sct = (ServiceActivationTemplate)context.get(AbstractWebControl.BEAN);
        

        final ArrayList bundles = new ArrayList(selections.size());

        for (int n = 0; n < selections.size(); ++n)
        {
            final AuxiliaryBundleSelection selection = (AuxiliaryBundleSelection)selections.get(n);

            final SctAuxiliaryBundle bundle = new SctAuxiliaryBundle();
            
            try 
            {
                bundle.setIdentifier(getNextIdentifier(context));
            } 
            catch (IllegalArgumentException e) 
            {
                new MajorLogMsg(this,"Exception while setting the identifier to SCT-Auxiliary Bundle",e).log(context);                
            } 
            catch (HomeException e) 
            {
                new MajorLogMsg(this,"Exception while setting the identifier to SCT-Auxiliary Bundle",e).log(context);                
            }           
            
            bundle.setAuxiliaryBundleIdentifier(selection.getSelectionIdentifier());
            bundle.setSctIdentifier(sct.getIdentifier());
            
            if (selection.getPaymentNum() < 1)
            {
                throw new IllegalArgumentException("Number of Payments should be greater than or equal to 1");
            }
            
            bundle.setPaymentNum(selection.getPaymentNum());

            bundles.add(bundle);
        }
        return bundles;
    }
    
    /**
     * Gets the next available identifier.
     *
     * @return The next available identifier.
     */
    private long getNextIdentifier(Context ctx)
        throws HomeException
    {
        IdentifierSequenceSupportHelper.get(ctx).ensureSequenceExists(
            ctx,
            IdentifierEnum.SCT_AUX_BUNDLE_ID,
            0,
            Long.MAX_VALUE);

        return IdentifierSequenceSupportHelper.get(ctx).getNextIdentifier(
            ctx,
            IdentifierEnum.SCT_AUX_BUNDLE_ID,
            null);
    }

    
    /**
     * Gets a Collection of AuxiliaryBundleSelections for the given sct.
     * Selections are based on type and existing associations. 
     * 
     * @param context The operating context.
     * @param sct The service ActivationTemplate object for which to get selectable
     * AuxiliaryBundles.
     * @param associations The SctAuxiliaryBundles for the currently
     * associated AuxiliaryBundles.
     *
     * @return A Collection of AuxiliaryBundlesSelections.
     */
    private Collection getSelectableAuxiliaryBundles(final Context context, final ServiceActivationTemplate sct, 
        Collection associations, Collection pricePlanSet)
    {   
        Collection bundles = null;
        SortedSet selectionSet = new TreeSet();
        if (sct != null)
        {   
            bundles = getAllAvailableAuxiliaryBundles(context, sct);
             
             //Get the AuxiliaryBundle Identifiers for selected SCT-Auxiliary bundle associations
            try
            {
                selectionSet.addAll(((List) Visitors.forEach(context, associations, new MapVisitor(SctAuxiliaryBundleXInfo.AUXILIARY_BUNDLE_IDENTIFIER))));
            }
            catch (Exception e)
            {
                new MinorLogMsg(this, "cannot get auxiliary bundle id list from SCT", e).log(context);
            }
        }
        else
        {
            bundles = associations;
        }
        
        final ArrayList selectableBundles = new ArrayList();
        
        final Iterator bundleIterator = bundles.iterator();        
        
        while (bundleIterator.hasNext())
        {
            BundleProfile bundle = null;
            SctAuxiliaryBundle sctAux = null;
            Object obj = bundleIterator.next();
            
            if (obj instanceof SctAuxiliaryBundle)
            {
                sctAux = (SctAuxiliaryBundle) obj;
            }
            else if (obj instanceof BundleProfile)
            {
                bundle = (BundleProfile) obj;
            }
            
            if (bundle == null)
            {
                bundle = SubscriberCreateTemplateSupport.getAuxiliaryBundleforAuxId(context,sctAux.getAuxiliaryBundleIdentifier());
            }

            // if auxiliary bundle is in price plan and MANDATORY, uncheck it and do not add to selectable set
            if (pricePlanSet != null && pricePlanSet.contains(Long.valueOf(bundle.getBundleId())))
            {
                final AuxiliaryBundleSelection selection = adapt(bundle);
                selection.setChecked(false);                                
            }
            else if (selectionSet != null && selectionSet.contains(Long.valueOf(bundle.getBundleId())))
            {   
                Collection<SctAuxiliaryBundle> existingBundles = SubscriberCreateTemplateSupport.getSelectedSctAuxiliaryBundles(associations, bundle.getBundleId());                
                for( SctAuxiliaryBundle existingBundle : existingBundles )
                {
                    final AuxiliaryBundleSelection selection = adapt(bundle);
                    selection.setChecked(true);
                    selection.setPaymentNum(existingBundle.getPaymentNum());               
                    selectableBundles.add(selection);
                }
            }
            else
            {
                final AuxiliaryBundleSelection selection = adapt(bundle);
                selection.setPaymentNum(1);
                selectableBundles.add(selection);
            }

        }
        
        return selectableBundles;
    }
    
    /**
     * Gets the Bundle Identifiers of mandatory bundles from the selected price plan in the given SCT
     * @param context
     * @param sct
     * @return
     */
    private Collection getPricePlanBundles(final Context context, final ServiceActivationTemplate sct)
    {
        Collection pricePlanSet = null;
        if (sct != null)
        {
            long ppIdentifier = sct.getPricePlan();
            if (ppIdentifier > 0)                               
            {
                PricePlan selectedPricePlan = null;
                try
                {
                    selectedPricePlan = PricePlanSupport.getPlan(context, ppIdentifier);                     
                }
                catch (HomeException e)
                {
                    new MajorLogMsg(
                            this,
                            "Failed to look-up price plan",
                            e).log(context);
                }
                if (selectedPricePlan != null)
                {
                    // Get the Bundle Identifiers that are MANDATORY in the selected price plan
                    try
                    {
                        pricePlanSet = PricePlanSupport.getBundleIds(context, selectedPricePlan).keySet();
                    }
                    catch (HomeException e)
                    {
                        new MajorLogMsg(
                                this,
                                "Failed to look-up price plan bundles.",
                                e).log(context);
                    }
                }
            }
        }
        return pricePlanSet;
    }
    
    /**
     * Creates an AuxiliaryBundleSelection corresponding to the given
     * BundleProfile.
     *
     * @param bundle The BundleProfile to adapt.
     * @return An AuxiliaryBundleSelection corresponding to the given
     * BundleProfile.
     */
    private AuxiliaryBundleSelection adapt(final BundleProfile bundle)
    {
        final AuxiliaryBundleSelection selection = new AuxiliaryBundleSelection();

        selection.setSelectionIdentifier(bundle.getBundleId());
        selection.setName(bundle.getName());
        selection.setChargingModeType(ServicePeriodEnum.MONTHLY);
        selection.setCharge(bundle.getAuxiliaryServiceCharge());

        return selection;
    }
    
    /**
     * Gets all the available AuxiliaryBundles for the given SCT.  An
     * AuxiliaryBundle is available if it has the same SPID as the subscriber.
     *
     * @param context The operating context.
     * @param sct The SCT for which to get all the available
     * AuxiliaryBundles.
     * @return All the available AuxiliaryBundles for the given subscriber.
     */
    private Collection getAllAvailableAuxiliaryBundles(
        final Context context,
        final ServiceActivationTemplate sct)
    {
        Home home = (Home)context.get(BundleProfileHome.class);
        
        And predicate = new And();
        predicate.add(new EQ(BundleProfileXInfo.SPID, Long.valueOf(sct.getSpid())));
        predicate.add(new EQ(BundleProfileXInfo.AUXILIARY, Boolean.TRUE));
        predicate.add(new EQ(BundleProfileXInfo.SEGMENT, BundleSegmentEnum.PREPAID));
        try
        {
            home = SubscriberBundleSupport.filterExpiredOneTimeBundles(context, home);
            final Collection bundles = home.select(context, predicate);

            return bundles;
        }
        catch (final Throwable t)
        {
            new MajorLogMsg(
                this,
                "Failed to look-up available auxiliary bundles.",
                t).log(context);

            return new ArrayList();
        }
    }



}
