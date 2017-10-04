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
package com.trilogy.app.crm.home;

import com.trilogy.app.crm.bean.AdjustmentType;
import com.trilogy.app.crm.bean.AuxiliaryService;
import com.trilogy.app.crm.bean.ui.ChargingTemplate;
import com.trilogy.app.crm.bean.ChargingTemplateAdjType;
import com.trilogy.app.crm.bean.ChargingTemplateAdjTypeHome;
import com.trilogy.app.crm.bean.ChargingTemplateAdjTypeXInfo;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bundle.BundleProfile;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Saves the adjustment types modifications (addition and removals) for the charging template.
 * @author Marcio Marques
 * @since 8.5
 *
 */
public class ChargingTemplateAdjustmentTypeMappingSavingHome extends HomeProxy
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public ChargingTemplateAdjustmentTypeMappingSavingHome(Context ctx, Home delegate)
    {
        super(ctx, delegate);
    }

    @Override
    public Object create(Context ctx, Object obj) throws HomeException
    {
        Object ret = super.create(ctx, obj);
        Home home = (Home) ctx.get(ChargingTemplateAdjTypeHome.class);
        ChargingTemplate chargingTemplate = (ChargingTemplate) obj;
        chargingTemplate.setIdentifier(((ChargingTemplate) ret).getIdentifier());
        
        for (Service service : chargingTemplate.getAddedServices(ctx))
        {
            ChargingTemplateAdjType mapping = new ChargingTemplateAdjType();
            mapping.setIdentifier(chargingTemplate.getIdentifier());
            mapping.setAdjustmentTypeId(service.getAdjustmentType());
            try
            {
                home.create(ctx, mapping);
            }
            catch (HomeException e)
            {
                StringBuilder sb = new StringBuilder();
                sb.append("Error while creating mapping between Charging Template '");
                sb.append(chargingTemplate.getIdentifier());
                sb.append(" - ");
                sb.append(chargingTemplate.getName());
                sb.append("' and Service '");
                sb.append(service.getIdentifier());
                sb.append(" - ");
                sb.append(service.getName());
                sb.append("' (AdjustmentType = ");
                sb.append(service.getAdjustmentType());
                sb.append("): ");
                sb.append(e.getMessage());
                LogSupport.minor(ctx, this, sb.toString(), e);
            }
        }

        for (AuxiliaryService auxService : chargingTemplate.getAddedAuxiliaryServices(ctx))
        {
            ChargingTemplateAdjType mapping = new ChargingTemplateAdjType();
            mapping.setIdentifier(chargingTemplate.getIdentifier());
            mapping.setAdjustmentTypeId(auxService.getAdjustmentType());
            try
            {
                home.create(ctx, mapping);
            }
            catch (HomeException e)
            {
                StringBuilder sb = new StringBuilder();
                sb.append("Error while creating mapping between Charging Template '");
                sb.append(chargingTemplate.getIdentifier());
                sb.append(" - ");
                sb.append(chargingTemplate.getName());
                sb.append("' and Auxiliary Service '");
                sb.append(auxService.getIdentifier());
                sb.append(" - ");
                sb.append(auxService.getName());
                sb.append("' (AdjustmentType = ");
                sb.append(auxService.getAdjustmentType());
                sb.append("): ");
                sb.append(e.getMessage());
                LogSupport.minor(ctx, this, sb.toString(), e);
            }
        }


        for (BundleProfile bundle : chargingTemplate.getAddedBundles(ctx))
        {
            ChargingTemplateAdjType mapping = new ChargingTemplateAdjType();
            mapping.setIdentifier(chargingTemplate.getIdentifier());
            mapping.setAdjustmentTypeId(bundle.getAdjustmentType());
            try
            {
                home.create(ctx, mapping);
            }
            catch (HomeException e)
            {
                StringBuilder sb = new StringBuilder();
                sb.append("Error while creating mapping between Charging Template '");
                sb.append(chargingTemplate.getIdentifier());
                sb.append(" - ");
                sb.append(chargingTemplate.getName());
                sb.append("' and Bundle '");
                sb.append(bundle.getBundleId());
                sb.append(" - ");
                sb.append(bundle.getName());
                sb.append("' (AdjustmentType = ");
                sb.append(bundle.getAdjustmentType());
                sb.append("): ");
                sb.append(e.getMessage());
                LogSupport.minor(ctx, this, sb.toString(), e);
            }

        }
        
        for (BundleProfile bundle : chargingTemplate.getAddedAuxiliaryBundles(ctx))
        {
            ChargingTemplateAdjType mapping = new ChargingTemplateAdjType();
            mapping.setIdentifier(chargingTemplate.getIdentifier());
            mapping.setAdjustmentTypeId(bundle.getAdjustmentType());
            try
            {
                home.create(ctx, mapping);
            }
            catch (HomeException e)
            {
                StringBuilder sb = new StringBuilder();
                sb.append("Error while creating mapping between Charging Template '");
                sb.append(chargingTemplate.getIdentifier());
                sb.append(" - ");
                sb.append(chargingTemplate.getName());
                sb.append("' and Auxiliary Bundle '");
                sb.append(bundle.getBundleId());
                sb.append(" - ");
                sb.append(bundle.getName());
                sb.append("' (AdjustmentType = ");
                sb.append(bundle.getAdjustmentType());
                sb.append("): ");
                sb.append(e.getMessage());
                LogSupport.minor(ctx, this, sb.toString(), e);
            }
        
            ChargingTemplateAdjType auxMapping = new ChargingTemplateAdjType();
            auxMapping.setIdentifier(chargingTemplate.getIdentifier());
            auxMapping.setAdjustmentTypeId(bundle.getAuxiliaryAdjustmentType());
            try
            {
                home.create(ctx, auxMapping);
            }
            catch (HomeException e)
            {
                StringBuilder sb = new StringBuilder();
                sb.append("Error while creating mapping between Charging Template '");
                sb.append(chargingTemplate.getIdentifier());
                sb.append(" - ");
                sb.append(chargingTemplate.getName());
                sb.append("' and Auxiliary Bundle '");
                sb.append(bundle.getBundleId());
                sb.append(" - ");
                sb.append(bundle.getName());
                sb.append("' (AuxiliaryAdjustmentType = ");
                sb.append(bundle.getAdjustmentType());
                sb.append("): ");
                sb.append(e.getMessage());
                LogSupport.minor(ctx, this, sb.toString(), e);
            }
        }

        for (AdjustmentType adjType : chargingTemplate.getAddedAdjustmentTypes(ctx))
        {
            ChargingTemplateAdjType mapping = new ChargingTemplateAdjType();
            mapping.setIdentifier(chargingTemplate.getIdentifier());
            mapping.setAdjustmentTypeId(adjType.getCode());
            try
            {
                home.create(ctx, mapping);
            }
            catch (HomeException e)
            {
                StringBuilder sb = new StringBuilder();
                sb.append("Error while creating mapping between Charging Template '");
                sb.append(chargingTemplate.getIdentifier());
                sb.append(" - ");
                sb.append(chargingTemplate.getName());
                sb.append("' and Adjustment Type '");
                sb.append(adjType.getName());
                sb.append("' (AdjustmentType = ");
                sb.append(adjType.getCode());
                sb.append("): ");
                sb.append(e.getMessage());
                LogSupport.minor(ctx, this, sb.toString(), e);
            }
        }
        chargingTemplate.resetSavedValues(ctx);
        return chargingTemplate;
    }
    
    public Object store(Context ctx, Object obj) throws HomeException
    {
        super.store(ctx, obj);
        Home home = (Home) ctx.get(ChargingTemplateAdjTypeHome.class);
        ChargingTemplate chargingTemplate = (ChargingTemplate) obj;
        
        for (Service service : chargingTemplate.getAddedServices(ctx))
        {
            ChargingTemplateAdjType mapping = new ChargingTemplateAdjType();
            mapping.setIdentifier(chargingTemplate.getIdentifier());
            mapping.setAdjustmentTypeId(service.getAdjustmentType());
            try
            {
                home.create(ctx, mapping);
            }
            catch (HomeException e)
            {
                StringBuilder sb = new StringBuilder();
                sb.append("Error while creating mapping between Charging Template '");
                sb.append(chargingTemplate.getIdentifier());
                sb.append(" - ");
                sb.append(chargingTemplate.getName());
                sb.append("' and Service '");
                sb.append(service.getIdentifier());
                sb.append(" - ");
                sb.append(service.getName());
                sb.append("' (AdjustmentType = ");
                sb.append(service.getAdjustmentType());
                sb.append("): ");
                sb.append(e.getMessage());
                LogSupport.minor(ctx, this, sb.toString(), e);
            }
        }

        for (Service service : chargingTemplate.getRemovedServices(ctx))
        {
            ChargingTemplateAdjType mapping = new ChargingTemplateAdjType();
            mapping.setIdentifier(chargingTemplate.getIdentifier());
            mapping.setAdjustmentTypeId(service.getAdjustmentType());
            try
            {
                home.remove(ctx, mapping);
            }
            catch (HomeException e)
            {
                StringBuilder sb = new StringBuilder();
                sb.append("Error while removing mapping between Charging Template '");
                sb.append(chargingTemplate.getIdentifier());
                sb.append(" - ");
                sb.append(chargingTemplate.getName());
                sb.append("' and Service '");
                sb.append(service.getIdentifier());
                sb.append(" - ");
                sb.append(service.getName());
                sb.append("' (AdjustmentType = ");
                sb.append(service.getAdjustmentType());
                sb.append("): ");
                sb.append(e.getMessage());
                LogSupport.minor(ctx, this, sb.toString(), e);
            }
        }

        for (AuxiliaryService auxService : chargingTemplate.getAddedAuxiliaryServices(ctx))
        {
            ChargingTemplateAdjType mapping = new ChargingTemplateAdjType();
            mapping.setIdentifier(chargingTemplate.getIdentifier());
            mapping.setAdjustmentTypeId(auxService.getAdjustmentType());
            try
            {
                home.create(ctx, mapping);
            }
            catch (HomeException e)
            {
                StringBuilder sb = new StringBuilder();
                sb.append("Error while creating mapping between Charging Template '");
                sb.append(chargingTemplate.getIdentifier());
                sb.append(" - ");
                sb.append(chargingTemplate.getName());
                sb.append("' and Auxiliary Service '");
                sb.append(auxService.getIdentifier());
                sb.append(" - ");
                sb.append(auxService.getName());
                sb.append("' (AdjustmentType = ");
                sb.append(auxService.getAdjustmentType());
                sb.append("): ");
                sb.append(e.getMessage());
                LogSupport.minor(ctx, this, sb.toString(), e);
            }
        }

        for (AuxiliaryService auxService : chargingTemplate.getRemovedAuxiliaryServices(ctx))
        {
            ChargingTemplateAdjType mapping = new ChargingTemplateAdjType();
            mapping.setIdentifier(chargingTemplate.getIdentifier());
            mapping.setAdjustmentTypeId(auxService.getAdjustmentType());
            try
            {
                home.remove(ctx, mapping);
            }
            catch (HomeException e)
            {
                StringBuilder sb = new StringBuilder();
                sb.append("Error while removing mapping between Charging Template '");
                sb.append(chargingTemplate.getIdentifier());
                sb.append(" - ");
                sb.append(chargingTemplate.getName());
                sb.append("' and Auxiliary Service '");
                sb.append(auxService.getIdentifier());
                sb.append(" - ");
                sb.append(auxService.getName());
                sb.append("' (AdjustmentType = ");
                sb.append(auxService.getAdjustmentType());
                sb.append("): ");
                sb.append(e.getMessage());
                LogSupport.minor(ctx, this, sb.toString(), e);
            }
        }

        for (BundleProfile bundle : chargingTemplate.getAddedBundles(ctx))
        {
            ChargingTemplateAdjType mapping = new ChargingTemplateAdjType();
            mapping.setIdentifier(chargingTemplate.getIdentifier());
            mapping.setAdjustmentTypeId(bundle.getAdjustmentType());
            try
            {
                home.create(ctx, mapping);
            }
            catch (HomeException e)
            {
                StringBuilder sb = new StringBuilder();
                sb.append("Error while creating mapping between Charging Template '");
                sb.append(chargingTemplate.getIdentifier());
                sb.append(" - ");
                sb.append(chargingTemplate.getName());
                sb.append("' and Bundle '");
                sb.append(bundle.getBundleId());
                sb.append(" - ");
                sb.append(bundle.getName());
                sb.append("' (AdjustmentType = ");
                sb.append(bundle.getAdjustmentType());
                sb.append("): ");
                sb.append(e.getMessage());
                LogSupport.minor(ctx, this, sb.toString(), e);
            }
        }

        for (BundleProfile bundle : chargingTemplate.getRemovedBundles(ctx))
        {
            ChargingTemplateAdjType mapping = new ChargingTemplateAdjType();
            mapping.setIdentifier(chargingTemplate.getIdentifier());
            mapping.setAdjustmentTypeId(bundle.getAdjustmentType());
            try
            {
                home.remove(ctx, mapping);
            }
            catch (HomeException e)
            {
                StringBuilder sb = new StringBuilder();
                sb.append("Error while removing mapping between Charging Template '");
                sb.append(chargingTemplate.getIdentifier());
                sb.append(" - ");
                sb.append(chargingTemplate.getName());
                sb.append("' and Bundle '");
                sb.append(bundle.getBundleId());
                sb.append(" - ");
                sb.append(bundle.getName());
                sb.append("' (AdjustmentType = ");
                sb.append(bundle.getAdjustmentType());
                sb.append("): ");
                sb.append(e.getMessage());
                LogSupport.minor(ctx, this, sb.toString(), e);
            }
        }
        for (BundleProfile bundle : chargingTemplate.getAddedAuxiliaryBundles(ctx))
        {
            ChargingTemplateAdjType mapping = new ChargingTemplateAdjType();
            mapping.setIdentifier(chargingTemplate.getIdentifier());
            mapping.setAdjustmentTypeId(bundle.getAdjustmentType());
            try
            {
                home.create(ctx, mapping);
            }
            catch (HomeException e)
            {
                StringBuilder sb = new StringBuilder();
                sb.append("Error while creating mapping between Charging Template '");
                sb.append(chargingTemplate.getIdentifier());
                sb.append(" - ");
                sb.append(chargingTemplate.getName());
                sb.append("' and Auxiliary Bundle '");
                sb.append(bundle.getBundleId());
                sb.append(" - ");
                sb.append(bundle.getName());
                sb.append("' (AdjustmentType = ");
                sb.append(bundle.getAdjustmentType());
                sb.append("): ");
                sb.append(e.getMessage());
                LogSupport.minor(ctx, this, sb.toString(), e);
            }

            ChargingTemplateAdjType auxMapping = new ChargingTemplateAdjType();
            auxMapping.setIdentifier(chargingTemplate.getIdentifier());
            auxMapping.setAdjustmentTypeId(bundle.getAuxiliaryAdjustmentType());
            try
            {
                home.create(ctx, auxMapping);
            }
            catch (HomeException e)
            {
                StringBuilder sb = new StringBuilder();
                sb.append("Error while creating mapping between Charging Template '");
                sb.append(chargingTemplate.getIdentifier());
                sb.append(" - ");
                sb.append(chargingTemplate.getName());
                sb.append("' and Auxiliary Bundle '");
                sb.append(bundle.getBundleId());
                sb.append(" - ");
                sb.append(bundle.getName());
                sb.append("' (AuxiliaryAdjustmentType = ");
                sb.append(bundle.getAdjustmentType());
                sb.append("): ");
                sb.append(e.getMessage());
                LogSupport.minor(ctx, this, sb.toString(), e);
            }
        }

        for (BundleProfile bundle : chargingTemplate.getRemovedAuxiliaryBundles(ctx))
        {
            ChargingTemplateAdjType mapping = new ChargingTemplateAdjType();
            mapping.setIdentifier(chargingTemplate.getIdentifier());
            mapping.setAdjustmentTypeId(bundle.getAdjustmentType());
            try
            {
                home.remove(ctx, mapping);
            }
            catch (HomeException e)
            {
                StringBuilder sb = new StringBuilder();
                sb.append("Error while removing mapping between Charging Template '");
                sb.append(chargingTemplate.getIdentifier());
                sb.append(" - ");
                sb.append(chargingTemplate.getName());
                sb.append("' and Auxiliary Bundle '");
                sb.append(bundle.getBundleId());
                sb.append(" - ");
                sb.append(bundle.getName());
                sb.append("' (AdjustmentType = ");
                sb.append(bundle.getAdjustmentType());
                sb.append("): ");
                sb.append(e.getMessage());
                LogSupport.minor(ctx, this, sb.toString(), e);
            }

            ChargingTemplateAdjType auxMapping = new ChargingTemplateAdjType();
            auxMapping.setIdentifier(chargingTemplate.getIdentifier());
            auxMapping.setAdjustmentTypeId(bundle.getAuxiliaryAdjustmentType());
            try
            {
                home.remove(ctx, auxMapping);
            }
            catch (HomeException e)
            {
                StringBuilder sb = new StringBuilder();
                sb.append("Error while removing mapping between Charging Template '");
                sb.append(chargingTemplate.getIdentifier());
                sb.append(" - ");
                sb.append(chargingTemplate.getName());
                sb.append("' and Auxiliary Bundle '");
                sb.append(bundle.getBundleId());
                sb.append(" - ");
                sb.append(bundle.getName());
                sb.append("' (AuxiliaryAdjustmentType = ");
                sb.append(bundle.getAdjustmentType());
                sb.append("): ");
                sb.append(e.getMessage());
                LogSupport.minor(ctx, this, sb.toString(), e);
            }
        }

        for (AdjustmentType adjType : chargingTemplate.getAddedAdjustmentTypes(ctx))
        {
            ChargingTemplateAdjType mapping = new ChargingTemplateAdjType();
            mapping.setIdentifier(chargingTemplate.getIdentifier());
            mapping.setAdjustmentTypeId(adjType.getCode());
            try
            {
                home.create(ctx, mapping);
            }
            catch (HomeException e)
            {
                StringBuilder sb = new StringBuilder();
                sb.append("Error while creating mapping between Charging Template '");
                sb.append(chargingTemplate.getIdentifier());
                sb.append(" - ");
                sb.append(chargingTemplate.getName());
                sb.append("' and Adjustment Type '");
                sb.append(adjType.getName());
                sb.append("' (AdjustmentType = ");
                sb.append(adjType.getCode());
                sb.append("): ");
                sb.append(e.getMessage());
                LogSupport.minor(ctx, this, sb.toString(), e);
            }
        }

        for (AdjustmentType adjType : chargingTemplate.getRemovedAdjustmentTypes(ctx))
        {
            ChargingTemplateAdjType mapping = new ChargingTemplateAdjType();
            mapping.setIdentifier(chargingTemplate.getIdentifier());
            mapping.setAdjustmentTypeId(adjType.getCode());
            try
            {
                home.remove(ctx, mapping);
            }
            catch (HomeException e)
            {
                StringBuilder sb = new StringBuilder();
                sb.append("Error while removing mapping between Charging Template '");
                sb.append(chargingTemplate.getIdentifier());
                sb.append(" - ");
                sb.append(chargingTemplate.getName());
                sb.append("' and Adjustment Type '");
                sb.append(adjType.getName());
                sb.append("' (AdjustmentType = ");
                sb.append(adjType.getCode());
                sb.append("): ");
                sb.append(e.getMessage());
                LogSupport.minor(ctx, this, sb.toString(), e);
            }
        }
        
        chargingTemplate.resetSavedValues(ctx);
        return chargingTemplate;
    }    
    
    public void remove(Context ctx, Object obj) throws HomeException
    {
        final Home home = (Home) ctx.get(ChargingTemplateAdjTypeHome.class);
        final ChargingTemplate chargingTemplate = (ChargingTemplate) obj;
        home.where(ctx, new EQ(ChargingTemplateAdjTypeXInfo.IDENTIFIER, chargingTemplate.getIdentifier())).forEach(ctx,
                new Visitor()
                {

                    @Override
                    public void visit(Context ctx, Object obj) throws AgentException, AbortVisitException
                    {
                        ChargingTemplateAdjType mapping = (ChargingTemplateAdjType) obj;
                        try
                        {
                            home.remove(obj);
                        }
                        catch (HomeException e)
                        {
                            LogSupport.minor(ctx, this,
                                    "Unable to remove Charging Template -> Adjustment Type mapping. ChargingTemplate="
                                            + mapping.getIdentifier() + ", AdjType=" + mapping.getAdjustmentTypeId()
                                            + ": " + e.getMessage(), e);
                        }
                    }
                });
        chargingTemplate.resetSavedValues(ctx);
        super.remove(ctx, obj);
    }
}
