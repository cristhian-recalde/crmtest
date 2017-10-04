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
package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletRequest;

import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.NEQ;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.NoActionsWebControl;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.ViewModeEnum;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bas.tps.ServiceActivationTemplate;
import com.trilogy.app.crm.bean.AuxiliaryServiceSelection;
import com.trilogy.app.crm.bean.AuxiliaryServiceSelectionXInfo;
import com.trilogy.app.crm.bean.AuxiliaryServiceStateEnum;
import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.bean.AuxiliaryServiceXInfo;
import com.trilogy.app.crm.bean.CallingGroupTypeEnum;
import com.trilogy.app.crm.bean.IdentifierEnum;
import com.trilogy.app.crm.bean.SctAuxiliaryService;
import com.trilogy.app.crm.bean.core.custom.AuxiliaryService;
import com.trilogy.app.crm.filter.AuxServiceTypePredicate;
import com.trilogy.app.crm.filter.OneTimeChargeAuxServPredicate;
import com.trilogy.app.crm.support.AuxiliaryServiceSupport;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.IdentifierSequenceSupportHelper;
import com.trilogy.app.crm.support.SubscriberCreateTemplateSupport;


/**
 * Auxiliary service selection web control for SCT.
 *
 * @author manda.subramanyam@redknee.com
 */
public class SctAuxiliaryServiceSelectionWebControl extends ProxyWebControl
{

    /**
     * Create a new instance of <code>SctAuxiliaryServiceSelectionWebControl</code>.
     */
    public SctAuxiliaryServiceSelectionWebControl()
    {
        super(new NoActionsWebControl(createTableWebControl()));
    }


    /**
     * Creates the specialized TableWebControl that modifies the way the default
     * TableWebControl handles the accept/reject checkboxes that appear in the first
     * column of the table.
     *
     * @return A specialized TableWebControl.
     */
    private static WebControl createTableWebControl()
    {
        final WebControl control = new SctAuxiliaryServiceSelectionTableWebControl();

        return control;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void toWeb(final Context ctx, final PrintWriter out, final String name, final Object obj)
    {
        Collection<SctAuxiliaryService> associations = (Collection<SctAuxiliaryService>) obj;
        
        final Context subCtx = ctx.createSubContext();

        AbstractWebControl.setMode(subCtx, AuxiliaryServiceSelectionXInfo.EXTRA_FIELDS, ViewModeEnum.NONE);
        AbstractWebControl.setMode(subCtx, AuxiliaryServiceSelectionXInfo.START_DATE, ViewModeEnum.NONE);
        AbstractWebControl.setMode(subCtx, AuxiliaryServiceSelectionXInfo.END_DATE, ViewModeEnum.NONE);
        
        final ServiceActivationTemplate sct = (ServiceActivationTemplate) subCtx.get(AbstractWebControl.BEAN);

        Collection<AuxiliaryServiceSelection> selections = getSelections(subCtx, sct, associations);
        super.toWeb(subCtx, out, name, selections);
    }

    /**
     * Returns all the available auxiliary services for SCT selection, with the existing
     * ones marked as selected.
     *
     * @param context
     *            The operating context.
     * @param sct
     *            The subscriber creation template.
     * @param existingAssociations
     *            The set of existing associations.
     * @return A collection of auxiliary service available for selection during SCT
     *         creation.
     */
    private Collection<AuxiliaryServiceSelection> getSelections(final Context context,
        final ServiceActivationTemplate sct, final Collection<SctAuxiliaryService> existingAssociations)
    {
        Collection<SctAuxiliaryService> associations = existingAssociations;
        if (associations == null || associations.size() <= 0)
        {
            associations = SubscriberCreateTemplateSupport.getSctAuxiliaryServices(context, sct);
        }

        Collection<AuxiliaryServiceSelection> selections = getSelectableAuxiliaryServicesFromSelections(context, sct, associations);
        return selections;
    }


    /**
     * Gets a Collection of AuxiliaryServiceSelections for the given sct. Selections are
     * based on type and existing associations. An AuxiliaryServices is selectable if it
     * is a PLP type with the same SPID as the sct, or if it is a CUG type that is already
     * associated with the subscriber.
     *
     * @param context
     *            The operating context.
     * @param sct
     *            The service ActivationTemplate object for which to get selectable
     *            AuxiliaryServices.
     * @param associations
     *            The SctAuxiliaryServices for the currently associated AuxiliaryServices.
     * @return A Collection of AuxiliaryServiceSelections.
     */
    private Collection<AuxiliaryServiceSelection> getSelectableAuxiliaryServicesFromSelections(final Context context,
        final ServiceActivationTemplate sct, final Collection<SctAuxiliaryService> associations)
    {
        /*
         * this services contains all PLP, CUG, Basic, Provisionable types with the
         * same spid as the subscriber
         */
        Collection<AuxiliaryService> services = getAllAvailableAuxiliaryServices(context, sct);

        /*
         * Get the AuxiliaryService Identifiers for selected SCT-Auxiliary service
         * associations
         */
        Map<Long, SctAuxiliaryService> selectionMap = SubscriberCreateTemplateSupport.getSelectionMap(associations);

        final List<AuxiliaryServiceSelection> selectableServices = new ArrayList<AuxiliaryServiceSelection>();

        for (final AuxiliaryService service : services)
        {
            boolean selected = selectionMap.containsKey(Long.valueOf(service.getIdentifier()));
            
            AuxiliaryServiceSelection selection = adapt(service);
            if (selected || isSelectable(context, service))
            {
                if (selected)
                {
                    selection.setChecked(true);

                    final SctAuxiliaryService existingService = selectionMap.get(service.getIdentifier());

                    selection.setPaymentNum(existingService.getPaymentNum());
                }
                else
                {
                    selection.setPaymentNum(1);
                }
                selectableServices.add(selection);
            }
        }

        return selectableServices;
    }


    /**
     * Gets all the available AuxiliaryServices for the given SCT. An AuxiliaryService is
     * available if it has the same SPID as the subscriber.
     *
     * @param context
     *            The operating context.
     * @param sct
     *            The SCT for which to get all the available AuxiliaryServices.
     * @return All the available AuxiliaryServices for the given subscriber.
     */
    private Collection<AuxiliaryService> getAllAvailableAuxiliaryServices(final Context context,
        final ServiceActivationTemplate sct)
    {
        final And filter = new And();
        filter.add(new EQ(AuxiliaryServiceXInfo.SPID, sct.getSpid()));
        filter.add(new NEQ(AuxiliaryServiceXInfo.STATE, AuxiliaryServiceStateEnum.CLOSED));

        // TT 7082700017: Do not show AMSISDN auxiliary services on SCT.
        filter.add(new NEQ(AuxiliaryServiceXInfo.TYPE, AuxiliaryServiceTypeEnum.AdditionalMsisdn));

        try
        {   
            final Collection<AuxiliaryService> services = HomeSupportHelper.get(context).getBeans(
                    context, 
                    AuxiliaryService.class, 
                    filter, 
                    true, AuxiliaryServiceXInfo.TYPE, AuxiliaryServiceXInfo.NAME, AuxiliaryServiceXInfo.IDENTIFIER);
            return services;
        }
        catch (final Throwable t)
        {
            new MajorLogMsg(this, "Failed to look-up available auxiliary services.", t).log(context);

            return new ArrayList<AuxiliaryService>();
        }
    }


    /**
     * Returns true iff the auxiliary service is allowed to be selected.
     * 
     * @param service Auxiliary Service
     * @return true iff the auxiliary service is allowed to be selected.
     */
    protected boolean isSelectable(Context ctx, AuxiliaryService service)
    {
        boolean result = true;
        
        if (!EnumStateSupportHelper.get(ctx).stateEquals(service, AuxiliaryServiceStateEnum.ACTIVE))
        {
            result = false;
        }
        else if (service.isCUG(ctx))
        {
            // CUG is not selectable. It can only be provisioned through CUG screen.
            result = false;
        }
        else if (service.isPrivateCUG(ctx))
        {
            // PCUG is not selectable. It can only be provisioned through CUG screen.
            result = false;
        }
        
        return result;
    }


    /**
     * Creates an AuxiliaryServiceSelection corresponding to the given AuxiliaryService.
     *
     * @param service
     *            The AuxiliaryService to adapt.
     * @return An AuxiliaryServiceSelection corresponding to the given AuxiliaryService.
     */
    private AuxiliaryServiceSelection adapt(final AuxiliaryService service)
    {
        final AuxiliaryServiceSelection selection = new AuxiliaryServiceSelection();

        selection.setSelectionIdentifier(service.getIdentifier());
        selection.setName(service.getName());
        selection.setChargingModeType(service.getChargingModeType());
        selection.setCharge(service.getCharge());
        selection.setType(service.getType());

        return selection;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object fromWeb(final Context context, final ServletRequest request, final String name)
    {
        /*
         * The web control will only return to us a collection of the
         * AuxiliaryServiceSelections that are checked on the SCT profile. From that list,
         * we need to derive a list of SctAuxiliaryServices to return to the SCT.
         */

        /*
         * From above list, we need to derive lists of SctAuxiliaryServices
         */

        final List<AuxiliaryServiceSelection> selections = new ArrayList<AuxiliaryServiceSelection>();

        super.fromWeb(context, selections, request, name);
        
        final ServiceActivationTemplate sct = (ServiceActivationTemplate) context.get(AbstractWebControl.BEAN);

        final List<SctAuxiliaryService> services = new ArrayList<SctAuxiliaryService>(selections.size());
        // final ArrayList futureServices = new ArrayList(selections.size());

        for (final AuxiliaryServiceSelection selection : selections)
        {
            final SctAuxiliaryService service = new SctAuxiliaryService();

            try
            {
                service.setIdentifier(getNextIdentifier(context));
            }
            catch (final IllegalArgumentException e)
            {
                new MajorLogMsg(this, "Exception while setting the identifier to SCT-Auxiliary Service", e)
                    .log(context);
            }
            catch (final HomeException e)
            {
                new MajorLogMsg(this, "Exception while setting the identifier to SCT-Auxiliary Service", e)
                    .log(context);
            }

            service.setAuxiliaryServiceIdentifier(selection.getSelectionIdentifier());
            service.setSctIdentifier(sct.getIdentifier());
            
            if (selection.getPaymentNum() < 1)
            {
                AuxiliaryService auxiliaryService = null;
                try
                {
                    auxiliaryService = AuxiliaryServiceSupport.getAuxiliaryService(context, service.getAuxiliaryServiceIdentifier());
                }
                catch (HomeException e)
                {
                    String msg = "Error retrieving auxiliary service with ID " + service.getAuxiliaryServiceIdentifier();
                    new MinorLogMsg(this, msg, e).log(context);
                    throw new IllegalArgumentException(msg); 
                }
                if (auxiliaryService == null
                        || !(new OneTimeChargeAuxServPredicate().f(context, auxiliaryService)
                                || new AuxServiceTypePredicate(AuxiliaryServiceTypeEnum.URCS_Promotion, AuxiliaryServiceTypeEnum.Discount).f(context, auxiliaryService)))
                {
                    throw new IllegalArgumentException("Number of Payments should be greater than or equal to 1");   
                }
            }

            service.setPaymentNum(selection.getPaymentNum());

            services.add(service);
        }
        return services;
    }


    /**
     * Gets the next available identifier.
     *
     * @param ctx
     *            The operating context.
     * @return The next available identifier.
     * @throws HomeException
     *             Thrown if there are problems retrieveing the next identifier.
     */
    private long getNextIdentifier(final Context ctx) throws HomeException
    {
        IdentifierSequenceSupportHelper.get(ctx).ensureSequenceExists(ctx, IdentifierEnum.SCT_AUX_SERVICE_ID, 1, Long.MAX_VALUE);

        /*
         * TODO - 2004-08-04 - Should provide roll-over function. The defaults should not
         * require roll over for a very long time, but there is nothing to prevent an
         * admin from changing the next or end values.
         */
        return IdentifierSequenceSupportHelper.get(ctx).getNextIdentifier(ctx, IdentifierEnum.SCT_AUX_SERVICE_ID, null);
    }
}
