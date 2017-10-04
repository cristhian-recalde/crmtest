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
package com.trilogy.app.crm.extension.auxiliaryservice.core.custom;

import java.util.Date;

import com.trilogy.app.crm.bean.HomezoneCount;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberHomezone;
import com.trilogy.app.crm.bean.SubscriberHomezoneHome;
import com.trilogy.app.crm.bean.SubscriberHomezoneXInfo;
import com.trilogy.app.crm.bean.externalapp.ExternalAppEnum;
import com.trilogy.app.crm.client.AppEcpClientSupport;
import com.trilogy.app.crm.client.AppHomezoneClient;
import com.trilogy.app.crm.client.AppHomezoneClientException;
import com.trilogy.app.crm.config.AppHomezoneClientConfig;
import com.trilogy.app.crm.extension.AssociableExtension;
import com.trilogy.app.crm.extension.DependencyValidatableExtension;
import com.trilogy.app.crm.extension.ExtensionAssociationException;
import com.trilogy.app.crm.extension.ExtensionInstallationException;
import com.trilogy.app.crm.extension.InstallableExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.HomeZoneAuxSvcExtensionXInfo;
import com.trilogy.app.crm.homezone.HomezoneInfoValidator;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.ExternalAppSupport;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.homezone.corba.ErrorCode;
import com.trilogy.app.homezone.corba.SubscriberHomezoneInfo;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeOperationEnum;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Implements the HomeZone auxiliary service extension.
 * @author Marcio Marques
 * @since 9.1.2
 *
 */
public class HomeZoneAuxSvcExtension extends
        com.redknee.app.crm.extension.auxiliaryservice.core.HomeZoneAuxSvcExtension implements
        DependencyValidatableExtension, InstallableExtension, AssociableExtension<SubscriberAuxiliaryService>
{
    @Override
    public void validateDependency(Context ctx) throws IllegalStateException
    {
        CompoundIllegalStateException cise = new CompoundIllegalStateException();
        
        if (HomeZoneAuxSvcExtension.DEFAULT_HZX != this.getHzX() && (this.getHzX() < MIN_X || this.getHzX() > MAX_X))
        {
            cise.thrown(new IllegalPropertyArgumentException(HomeZoneAuxSvcExtensionXInfo.HZ_X,
                    "X should be in the range " + MIN_X + " to " + MAX_X));
        }

        if (HomeZoneAuxSvcExtension.DEFAULT_HZY != this.getHzY() && (this.getHzY() < MIN_Y || this.getHzY() > MAX_Y))
        {
            cise.thrown(new IllegalPropertyArgumentException(HomeZoneAuxSvcExtensionXInfo.HZ_Y,
                    "Y should be in the range " + MIN_Y + " to " + MAX_Y));
        }

        if ((HomeZoneAuxSvcExtension.DEFAULT_HZX != this.getHzX()
                || HomeZoneAuxSvcExtension.DEFAULT_HZY != this.getHzY())
                && (this.getHzCellID() != null && this.getHzCellID().length() != 0))
        {
            cise.thrown(new IllegalPropertyArgumentException(HomeZoneAuxSvcExtensionXInfo.HZ_CELL_ID,
                    "Either enter CellID or (X AND Y). You entered both."));
        }
        else if (HomeZoneAuxSvcExtension.DEFAULT_HZX != this.getHzX())
        {
            if (HomeZoneAuxSvcExtension.DEFAULT_HZY == this.getHzY())
            {
                cise.thrown(new IllegalPropertyArgumentException(HomeZoneAuxSvcExtensionXInfo.HZ_Y,
                        "Either enter CellID or (X AND Y). You entered only X value."));
            }
        }
        else if (HomeZoneAuxSvcExtension.DEFAULT_HZY != this.getHzY())
        {
            if (HomeZoneAuxSvcExtension.DEFAULT_HZX == this.getHzX())
            {
                cise.thrown(new IllegalPropertyArgumentException(HomeZoneAuxSvcExtensionXInfo.HZ_X,
                "Either enter CellID or (X AND Y). You entered only Y value."));
            }
        }
        else if (HomeZoneAuxSvcExtension.DEFAULT_HZX == this.getHzX()
                && HomeZoneAuxSvcExtension.DEFAULT_HZY == this.getHzY()
                && (this.getHzCellID() == null || this.getHzCellID().length() == 0))
        {
            cise.thrown(new IllegalPropertyArgumentException(HomeZoneAuxSvcExtensionXInfo.HZ_CELL_ID,
                    "Either enter CellID or (X AND Y). You entered none."));
        }

        AppHomezoneClient hzClient = (AppHomezoneClient) ctx.get(AppHomezoneClient.class);
        if (hzClient==null || !hzClient.isAlive())
        {
            cise.thrown(new IllegalStateException("HomezoneClient not correctly initialized."));
        }

        HomeOperationEnum operation = (HomeOperationEnum) ctx.get(HomeOperationEnum.class);

        cise.throwAll();

        if (HomeOperationEnum.CREATE.equals(operation))
        {
            validateOnCreate(ctx);
        }
        else if (HomeOperationEnum.STORE.equals(operation))
        {
            validateOnStore(ctx);
        }
        else if (HomeOperationEnum.REMOVE.equals(operation))
        {
            validateOnRemove(ctx);
        }
    }
    
    private void validateOnCreate(Context ctx) throws IllegalStateException
    {
        HomezoneInfoValidator.validateCellIDXnYForAuxSvc(ctx, this);
    }

    private void validateOnStore(Context ctx) throws IllegalStateException
    {
        CompoundIllegalStateException cise = new CompoundIllegalStateException();
        checkIfSubscriberAttached(ctx, "store", cise);
        cise.throwAll();
        HomezoneInfoValidator.validateCellIDXnYForAuxSvc(ctx, this);
    }

    private void validateOnRemove(Context ctx) throws IllegalStateException
    {
        CompoundIllegalStateException cise = new CompoundIllegalStateException();
        checkIfSubscriberAttached(ctx, "remove", cise);
        cise.throwAll();
    }
    
    
    /**
     * Puts debug log for the displaying the resulcode description
     * 
     * @param resultcode
     * @param methodName
     *            from which putResultCodeDebug is called
     * @return the message accortding to the resultcode
     */
    private String putResultCodeDebug(int resultcode)
    {
        String message = null;
        switch (resultcode)
        {
            case ErrorCode.SUCCESS:
                message = "SUCCESS";
                break;
            case ErrorCode.SQL_ERROR:
                message = "SQL_ERROR";
                break;
            case ErrorCode.INTERNAL_ERROR:
                message = "INTERNAL_ERROR";
                break;
            case ErrorCode.MANDATORY_INFO_MISSING:
                message = "MANDATORY_INFO_MISSING";
                break;
            case ErrorCode.INVALID_PARAMETER:
                message = "INVALID_PARAMETER";
                break;
            case ErrorCode.UPDATE_NOT_ALLOWED:
                message = "UPDATE_NOT_ALLOWED";
                break;
            case ErrorCode.ENTRY_ALREADY_EXISTED:
                message = "ENTRY_ALREADY_EXISTED";
                break;
            case ErrorCode.ENTRY_NOT_FOUND:
                message = "ENTRY_NOT_FOUND";
                break;
            case ErrorCode.INVALID_HOMEZONE_PRIORITY:
                message = "INVALID_HOMEZONE_PRIORITY";
                break;
            case ErrorCode.HOMEZONE_PRIORITY_ALREADY_EXISTED:
                message = "HOMEZONE_PRIORITY_ALREADY_EXISTED";
                break;
            default:
                message = "ERROR_UNKNOWN";
                break;
        }
        return message;
    }

    private void checkIfSubscriberAttached(Context ctx, String action, CompoundIllegalStateException cise)
    {
        try
        {
            Object sub = HomeSupportHelper.get(ctx).findBean(ctx, SubscriberHomezone.class, new EQ(SubscriberHomezoneXInfo.HZ_ID,
                    Long.valueOf(this.getAuxiliaryServiceId())));
    
            if (sub != null)
            {
                cise.thrown(new IllegalStateException(
                        "Cannot "+ action + " this homezone auxiliary service as it is attached to at least one subscriber"));
            }
        }
        catch (HomeException e)
        {
            cise.thrown(new IllegalStateException(
                    "Unable to check if homezone auxiliary service is attached to any subscribers"));
        }
    }

    @Override
    public void install(Context ctx) throws ExtensionInstallationException
    {
        final AppHomezoneClientConfig config = (AppHomezoneClientConfig) ctx.get(AppHomezoneClientConfig.class);
        boolean updateHomeZone = config.getContactHomezone();
        if (updateHomeZone)
        {
            //createDiscount
            AppHomezoneClient hzClient = (AppHomezoneClient) ctx.get(AppHomezoneClient.class);

            if (hzClient == null)
                throw new ExtensionInstallationException(
                        "Configuration Error: Can not connect to AppHomezone, please check CORBA client settings for homezone client", false, true);

            try
            {
                createDiscount(ctx, hzClient);
            }
            catch (IllegalStateException e)
            {
                LogSupport.info(ctx, this, e.getMessage(), e);
                throw new ExtensionInstallationException(e, false, true);
            }

        }
    }

    @Override
    public void update(Context ctx) throws ExtensionInstallationException
    {
        boolean successDiscount = true;
        boolean successRadius = true;
        try
        {
            com.redknee.app.crm.extension.auxiliaryservice.core.HomeZoneAuxSvcExtension oldExtension = getHomeZoneAuxSvcExtension(ctx, this.getAuxiliaryServiceId());
            
            int oldRadius = oldExtension.getRadius();
            int newRadius = this.getRadius();
            int oldDiscount = oldExtension.getDiscount();
            int newDiscount = this.getDiscount();
    
            final AppHomezoneClientConfig config = (AppHomezoneClientConfig) ctx.get(AppHomezoneClientConfig.class);
            boolean updateHomeZone = config.getContactHomezone();
    
            if (updateHomeZone)
            {
                //in this case follow the normal flow...real time
                // mode...provision
                // to AppHomeZone database
                // and then in the local homezone database
                AppHomezoneClient hzClient = (AppHomezoneClient) ctx.get(AppHomezoneClient.class);
    
                if (hzClient == null || !hzClient.isAlive())
                    throw new HomeException(
                            "Configuration Error: Can not connect to AppHomezone, please check CORBA client settings for homezone client");
    
                if (oldRadius != newRadius)
                {
                    try
                    {
                        modifyRadius(ctx, hzClient, oldRadius);
                    }
                    catch (IllegalStateException e)
                    {
                        this.setRadius(oldRadius);
                        successRadius = false;
                    }
                }
                if (oldDiscount != newDiscount)
                {
                    try
                    {
                        modifyDiscount(ctx, hzClient, oldDiscount);
                    }
                    catch (IllegalStateException e)
                    {
                        this.setDiscount(oldDiscount);
                        successDiscount = false;
                    }
                }
                
                if (!successDiscount || !successRadius)
                {
                    String message = "Unable to update ";
                    if (!successDiscount)
                    {
                        message+="Home Zone Discount";
                        if (!successRadius)
                        {
                            message+=" nor Home Zone Radius";
                        }
                    }
                    else if (!successRadius)
                    {
                        message+="Home Zone Radius";
                    }
                    
                    throw new ExtensionInstallationException(message, true, false);
                }
            }
        }
        catch (HomeException e)
        {
            throw new ExtensionInstallationException("Unable to retrieve current configuration", e, false, true);
        }
    }


    @Override
    public void uninstall(Context ctx) throws ExtensionInstallationException
    {
        final AppHomezoneClientConfig config = (AppHomezoneClientConfig) ctx.get(AppHomezoneClientConfig.class);
        boolean updateHomeZone = config.getContactHomezone();

        if (updateHomeZone)
        {
            //in this case follow the normal flow...real time
            // mode...provision
            // to AppHomeZone database
            // and then in the local homezone database
            AppHomezoneClient hzClient = (AppHomezoneClient) ctx.get(AppHomezoneClient.class);

            if (hzClient == null || !hzClient.isAlive())
                throw new ExtensionInstallationException(
                        "Configuration Error: Can not connect to AppHomezone, please check CORBA client settings for homezone client", false, true);

            try
            {
                deleteDiscount(ctx, hzClient);
            }
            catch (IllegalStateException e)
            {
                LogSupport.debug(ctx, this, e.getMessage(), e);
                throw new ExtensionInstallationException(e, false, true);
            }
        }
    }

    private void modifyRadius(Context ctx, AppHomezoneClient hzClient,
            int oldRadius) throws IllegalStateException
    {
        int resultCode = hzClient.modifyHomezoneRadius((int) this.getAuxiliaryServiceId(), this.getRadius());

        if (resultCode != ErrorCode.SUCCESS)
        {
            if (resultCode == ErrorCode.ENTRY_NOT_FOUND)
            {
                if (LogSupport.isDebugEnabled(ctx))
                    new DebugLogMsg(this,
                            "No Entry found in HomeZone application -HomeZoneId yet to be created: Id"
                                    + this.getAuxiliaryServiceId() + " for raidus change from "
                                    + oldRadius + " to " + this.getRadius()
                                    + ", Still changing the values locally",
                            null).log(ctx);
            }
            else
            {
                throw new IllegalStateException("Error: Could not modify the radius from " + oldRadius
                        + " to " + this.getRadius() + " for " + this.getAuxiliaryServiceId() + ", Reason:"
                        + putResultCodeDebug(resultCode));
            }
        }
        else if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, "Radius changed For HomeZone:" + this.getAuxiliaryServiceId()
                    + "from " + oldRadius + " to " + getRadius(), null).log(ctx);
        }
    }

    private void modifyDiscount(Context ctx, AppHomezoneClient hzClient,
            int oldDiscount) throws IllegalStateException
    {
        int resultCode = hzClient.modifyHomezoneDiscount((int) this.getAuxiliaryServiceId(), this.getDiscount());

        if (resultCode != ErrorCode.SUCCESS)
        {
            if (resultCode == ErrorCode.ENTRY_NOT_FOUND)
            {
                if (LogSupport.isDebugEnabled(ctx))
                    new DebugLogMsg(this,
                            "No Entry found in HomeZone application -HomeZoneId yet to be created: Id"
                                    + this.getAuxiliaryServiceId() + " for Discount change from "
                                    + oldDiscount + " to " + this.getDiscount()
                                    + ", Still changing the values locally",
                            null).log(ctx);
            }
            else
            {
                throw new IllegalStateException("Error: Could not modify the Discount from "
                        + oldDiscount + " to " + this.getDiscount() + " for " + this.getAuxiliaryServiceId()
                        + ", Reason:" + putResultCodeDebug(resultCode));
            }
        }
        else if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, "Discount changed For HomeZone:" + this.getAuxiliaryServiceId()
                    + "from " + oldDiscount + " to " + this.getDiscount(), null)
                    .log(ctx);
        }
    }


    private void deleteDiscount(Context ctx, AppHomezoneClient hzClient) throws IllegalStateException
    {
        int resultCode = hzClient.deleteHomezoneDiscount((int) this.getAuxiliaryServiceId());

        if (resultCode != ErrorCode.SUCCESS)
        {
            throw new IllegalStateException("Unable to delete discount homezone for auxiliary service " + this.getAuxiliaryServiceId() + " due to " + putResultCodeDebug(resultCode));
        }
        else if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, this, "Discount removed For HomeZone " + this.getAuxiliaryServiceId(), null);
        }
    }
    
    private void createDiscount(Context ctx, AppHomezoneClient hzClient) throws IllegalStateException
    {
        int resultCode = hzClient.createHomezoneDiscount((int) this.getAuxiliaryServiceId(), getDiscount());

        if (resultCode != ErrorCode.SUCCESS)
        {
            throw new IllegalStateException("Unable to create discount homezone for auxiliary service " + this.getAuxiliaryServiceId() + " due to " + putResultCodeDebug(resultCode));
        }
        else if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, this, "Discount created For HomeZone " + this.getAuxiliaryServiceId() + " with discount " + getDiscount(), null);
        }
    }
    
    @Override
    public void associate(Context ctx, SubscriberAuxiliaryService subAuxSvc) throws ExtensionAssociationException
    {
        SubscriberHomezone subHomezone = null;

        try
        {
            
        boolean updateHomeZone = (((AppHomezoneClientConfig) ctx.get(AppHomezoneClientConfig.class))
                .getContactHomezone());
        enableHZ(ctx, subAuxSvc);
        if (updateHomeZone)//If provisoning in homezone enabled
        {
            //provision to homezone only if the start date <= current date
            Date runningDate = CalendarSupportHelper.get(ctx).getRunningDate(ctx);
            if (CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(subAuxSvc.getStartDate()).equals(
                    CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(runningDate))
                    || CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(subAuxSvc.getStartDate()).before(
                            CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(runningDate)))
            {
                subHomezone = provisionHomeZone(ctx, subAuxSvc);
            }
            else
            {
                subHomezone = createSubHomeZoneForDB(ctx, subAuxSvc);
            }
        }
        else
        {
            subHomezone = createSubHomeZoneForDB(ctx, subAuxSvc);
        }
        

            Home subHomeZoneHome = (Home) ctx.get(SubscriberHomezoneHome.class);
            subHomeZoneHome.create(ctx, subHomezone);
            subAuxSvc.setProvisionActionState(true);
        }
        catch (Throwable t)
        {
            subAuxSvc.setProvisionActionState(false);
            throw new ExtensionAssociationException(ExternalAppEnum.BSS, "Unable to create subscriber HomeZone for subscriber " + subAuxSvc.getSubscriberIdentifier() + " and auxiliary serivce " + subAuxSvc.getAuxiliaryServiceIdentifier() + ": "
                    + t.getMessage(), ExternalAppSupport.BSS_DATABASE_FAILURE_SUBSCRIPTION_HOMEZONE_CREATION, t, false);
        }

    }

    @Override
    public void updateAssociation(Context ctx, SubscriberAuxiliaryService subAuxSvc) throws ExtensionAssociationException
    {
        Home subHomeZoneHome = null;
        SubscriberHomezone subHomezone = null;
        try
        {
            
        boolean updateHomeZone = (((AppHomezoneClientConfig) ctx.get(AppHomezoneClientConfig.class))
                .getContactHomezone());
        
        if (updateHomeZone)//If provisoning in homezone enabled
        {
            //provision to homezone only if the start date <= current date
            Date runningDate = CalendarSupportHelper.get(ctx).getRunningDate(ctx);
            if ((CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(subAuxSvc.getStartDate()).equals(
                    CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(runningDate)) || CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(
                    subAuxSvc.getStartDate()).before(CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(runningDate)))
                    && subAuxSvc.getProvisioned())
            {
                subHomezone = modifyProvisionedHomeZone(ctx, subAuxSvc);
            }
            else
            {
                subHomezone = createSubHomeZoneForDB(ctx, subAuxSvc);
            }
        }
        else
        {
            subHomezone = createSubHomeZoneForDB(ctx, subAuxSvc);
        }


            subHomeZoneHome = (Home) ctx.get(SubscriberHomezoneHome.class);
            subHomeZoneHome.store(ctx, subHomezone);
            subAuxSvc.setProvisionActionState(true);
        }
        catch (Throwable t)
        {
            subAuxSvc.setProvisionActionState(false);
            throw new ExtensionAssociationException(ExternalAppEnum.BSS, "Unable to store subscriber HomeZone for subscriber " + subAuxSvc.getSubscriberIdentifier() + " and auxiliary serivce " + subAuxSvc.getAuxiliaryServiceIdentifier() + ": "
                    + t.getMessage(), ExternalAppSupport.BSS_DATABASE_FAILURE_SUBSCRIPTION_HOMEZONE_UPDATE, t, false);
        }
    }

    @Override
    public void dissociate(Context ctx, SubscriberAuxiliaryService subAuxSvc) throws ExtensionAssociationException
    {
        try
        {
            
        boolean updateHomeZone = (((AppHomezoneClientConfig) ctx.get(AppHomezoneClientConfig.class))
                .getContactHomezone());
        if (updateHomeZone)//If provisoning in homezone enabled
        {
            //provision to homezone only if the start date <= current date
            Date runningDate = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(CalendarSupportHelper.get(ctx).getRunningDate(ctx));
            Date startDate = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(subAuxSvc.getStartDate());
            if ((startDate.equals(runningDate) || startDate.before(runningDate))
                    && subAuxSvc.getProvisioned())
            {
                removeProvisionedHomeZone(ctx, subAuxSvc);
            }
        }
        disableHZ(ctx, subAuxSvc);
        Home subHomeZoneHome = (Home) ctx.get(SubscriberHomezoneHome.class);
        

            SubscriberHomezone subZone = (SubscriberHomezone) (subHomeZoneHome.find(ctx,
                    Long.valueOf(subAuxSvc.getIdentifier())));
            subHomeZoneHome.remove(ctx, subZone);
            subAuxSvc.setProvisionActionState(true);
        }
        catch (Throwable t)
        {
            subAuxSvc.setProvisionActionState(false);
            throw new ExtensionAssociationException(ExternalAppEnum.BSS, "Unable to remove subscriber HomeZone for subscriber " + subAuxSvc.getSubscriberIdentifier() + " and auxiliary serivce " + subAuxSvc.getAuxiliaryServiceIdentifier() + ": "
                    + t.getMessage(), ExternalAppSupport.BSS_DATABASE_FAILURE_SUBSCRIPTION_HOMEZONE_REMOVAL, t, false);
        }
        
    }


    private SubscriberHomezone createSubHomeZoneForDB(Context ctx, SubscriberAuxiliaryService subAuxSvc)
            throws ExtensionAssociationException
    {
        SubscriberHomezone hZone = null;
        try
        {
            long auxServiceId = subAuxSvc.getAuxiliaryServiceIdentifier();
            Subscriber newSub = SubscriberSupport.getSubscriberForAuxiliaryService(ctx, subAuxSvc);
            hZone = new SubscriberHomezone();
            hZone.setIdentifier(subAuxSvc.getIdentifier());
            hZone.setHzId(auxServiceId);
            hZone.setHzCellID(subAuxSvc.getHzCellID(ctx));
            hZone.setHzX(subAuxSvc.getHzX(ctx));
            hZone.setHzY(subAuxSvc.getHzY(ctx));
            hZone.setHzPriority(subAuxSvc.getHzPriority(ctx));// change this later
            hZone.setMsisdn(newSub.getMSISDN());
        }
        catch (HomeException t)
        {
            throw new ExtensionAssociationException(ExternalAppEnum.BSS, "Unable to retrieve subscriber " + subAuxSvc.getSubscriberIdentifier() + ": "
                    + t.getMessage(), ExternalAppSupport.BSS_DATABASE_FAILURE_SUBSCRIPTION_RETRIEVAL, t, false);
        }
        
        return hZone;
    }


    private SubscriberHomezone modifyProvisionedHomeZone(Context ctx, SubscriberAuxiliaryService subAuxSvc)
            throws ExtensionAssociationException
    {
        SubscriberHomezone subHomeZone = null;
        long auxServiceId = subAuxSvc.getAuxiliaryServiceIdentifier();
        HomeZoneAuxSvcExtension basicHZone = getRelatedHomeZone(ctx, auxServiceId);
        Subscriber newSub = (Subscriber) ctx.get(Subscriber.class);
        Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
        // we should not get any of subscriber null here as modifyProvisionedHomeZone
        // always gets called from GUI
        AppHomezoneClient hzClient = getHomezoneClient(ctx);
        // provisions the subscriber in the homezone
        SubscriberHomezoneInfo subHomezoneInfo = fillUpSubscriberHomezoneInfo(ctx, newSub.getMSISDN(), subAuxSvc,
                this.getAuxiliaryServiceId(), basicHZone);
        try
        {
            if (isMSISDNSame(oldSub.getMSISDN(), newSub.getMSISDN()))
            {
                int rsCode = hzClient.modifySubscriberHomezone(subHomezoneInfo);
                if (rsCode != ErrorCode.SUCCESS)
                {
                    throw new AppHomezoneClientException(
                            hzClient.putResultCodeDebug(rsCode, "modifySubscriberHomezone"), (short) rsCode);
                }
            }
            else
            {
                // as msisdn is changed delete the related homezone from
                // Apphomezone then create the new one
                deleteAndAddInHomezone(hzClient, subHomezoneInfo, oldSub.getMSISDN(), newSub.getMSISDN());
            }
            subHomeZone = convertIntoSubHZone(subHomezoneInfo);
            subHomeZone.setIdentifier(subAuxSvc.getIdentifier());
            subHomeZone.setHzCellID(subAuxSvc.getHzCellID(ctx));
        }
        catch (AppHomezoneClientException t)
        {
            subHomeZone = null;
            throw new ExtensionAssociationException(ExternalAppEnum.HOMEZONE, "Error while modifying homezone in Homezone application. Message returned:"
                    + t.getMessage(), t.getResultCode(), t, false);
        }
        return subHomeZone;
    }


    private void removeProvisionedHomeZone(Context ctx, SubscriberAuxiliaryService subAuxSvc) throws ExtensionAssociationException
    {
        String subscriberId = subAuxSvc.getSubscriberIdentifier();
        Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
        if (oldSub == null)
        {
            try
            {
                oldSub = SubscriberSupport.lookupSubscriberForSubId(ctx, subscriberId);
            }
            catch (HomeException t)
            {
                throw new ExtensionAssociationException(ExternalAppEnum.BSS, "Unable to retrieve subscriber "
                        + subscriberId + ": " + t.getMessage(), ExternalAppSupport.BSS_DATABASE_FAILURE_SUBSCRIPTION_RETRIEVAL, t, false);
            }

        }
        AppHomezoneClient hzClient = getHomezoneClient(ctx);
        try
        {
            // no need to apply MSISDN change here as we can direclty remove the
            // homezone using old MSISDN
            int rsCode = hzClient.deleteSubscriberHomezone(oldSub.getMSISDN(),
                    (int) subAuxSvc.getAuxiliaryServiceIdentifier());
            if (rsCode != ErrorCode.SUCCESS)
            {
                throw new AppHomezoneClientException(hzClient.putResultCodeDebug(rsCode, "deleteSubscriberHomezone"),
                        (short) rsCode);
            }
        }
        catch (AppHomezoneClientException t)
        {
            throw new ExtensionAssociationException(ExternalAppEnum.HOMEZONE, "Error while modifying homezone in Homezone application. Message returned:"
                    + t.getMessage(), t.getResultCode(), t, false);
        }
    }


    private void deleteAndAddInHomezone(AppHomezoneClient hzClient, SubscriberHomezoneInfo subHomezoneInfo,
            String oldMSISDN, String newMSISDN) throws ExtensionAssociationException
    {
        try
        {
            int rsCode = hzClient.deleteSubscriberHomezone(oldMSISDN, subHomezoneInfo.zoneId);
            if (rsCode != ErrorCode.SUCCESS)
            {
                throw new AppHomezoneClientException(hzClient.putResultCodeDebug(rsCode,
                        "ChangeMSISDN->1.deleteSubscriberHomezone"), (short) rsCode);
            }
        }
        catch (AppHomezoneClientException t)
        {
            throw new ExtensionAssociationException(ExternalAppEnum.HOMEZONE, 
                    "Unable to remove homezone in HomeZone application.(The homezone removal was attepted as the subscriber MSISDN is changed, the delete and add attempt falied at delete stage) Message returned: "
                            + t.getMessage(), t.getResultCode(), t, false);
        }
        try
        {
            SubscriberHomezoneInfo[] subHomezoneArr = new SubscriberHomezoneInfo[1];
            subHomezoneArr[0] = subHomezoneInfo;
            int rsCode = hzClient.createSubscriberHomezone(newMSISDN, subHomezoneArr);
            if (rsCode != ErrorCode.SUCCESS)
            {
                throw new AppHomezoneClientException(hzClient.putResultCodeDebug(rsCode,
                        "ChangeMSISDN->2.createSubscriberHomezone"), (short) rsCode);
            }
        }
        catch (AppHomezoneClientException t)
        {
            throw new ExtensionAssociationException(ExternalAppEnum.HOMEZONE, 
                    "Error while creating homezone in Homezone application.(The homezone creation was attepted as the subscriber MSISDN is changed, the delete and add attempt falied at add stage) Message returned: "
                            + t.getMessage(), t.getResultCode(), t, false);
        }
    }


    private void enableHZ(Context ctx, SubscriberAuxiliaryService subAuxSvc) throws ExtensionAssociationException
    {
        try
        {
            Subscriber newSub = SubscriberSupport.getSubscriberForAuxiliaryService(ctx, subAuxSvc);
            enableHZ(ctx, newSub);
        }
        catch (HomeException t)
        {
            throw new ExtensionAssociationException(ExternalAppEnum.BSS, "Unable to retrieve subscriber "
                    + subAuxSvc.getSubscriberIdentifier() + ": " + t.getMessage(), ExternalAppSupport.BSS_DATABASE_FAILURE_SUBSCRIPTION_RETRIEVAL, t, false);
        }
    }


    private void enableHZ(Context ctx, Subscriber sub) throws ExtensionAssociationException
    {
        boolean updateHomeZone = (((AppHomezoneClientConfig) ctx.get(AppHomezoneClientConfig.class))
                .getContactHomezone());
        HomezoneCount hzCntBean = null;
        try
        {
            hzCntBean = HomeSupportHelper.get(ctx).findBean(ctx, HomezoneCount.class, sub.getId());
        }
        catch (Exception e)
        {
            // ignore
        }
        if (hzCntBean == null)
        {
            LogSupport.info(ctx, this, "No homezone count bean found with subscriberID " + sub.getId() + ". Assuming first time homezone is getting added.", null);
            hzCntBean = new HomezoneCount();
            hzCntBean.setSubscriberIdentifier(sub.getId());
            hzCntBean.setHzcount(0);
        }
        int hzCount = hzCntBean.getHzcount();
        if (hzCount == 0)
        {
            try
            {
                int result = com.redknee.app.osa.ecp.provision.ErrorCode.SUCCESS;
                if (updateHomeZone)
                {
                    AppEcpClientSupport.enableHomeZoneInECP(ctx, sub);
                }
                if (result!=com.redknee.app.osa.ecp.provision.ErrorCode.SUCCESS)
                {
                    throw new ExtensionAssociationException(ExternalAppEnum.VOICE, "Error enabling home zone in ECP", "Unable to enable HomeZone on URCS Voice", result);
                }
            }
            catch (Exception e)
            {
                String msg = "Unable to enable HomeZone in ECP for subscriber " + sub.getId() + ": " + e.getMessage();
                LogSupport.minor(ctx, this, msg, e);
                throw new ExtensionAssociationException(ExternalAppEnum.VOICE, msg, ExternalAppSupport.NO_CONNECTION, e, false);
            }
        }
        hzCntBean.setHzcount(hzCount + 1);
        try
        {
            SubscriberAuxiliaryServiceSupport.updateSubscriberHzCountWithBean(ctx, hzCntBean);
        }
        catch (HomeException t)
        {
            throw new ExtensionAssociationException(ExternalAppEnum.BSS, "Unable to update HomeZone Count for subscriber " + sub.getId()
                    + ": " + t.getMessage(), ExternalAppSupport.BSS_DATABASE_FAILURE_SUBSCRIPTION_HOMEZONE_COUNT_UPDATE, t, false);
        }
    }


    private void disableHZ(Context ctx, SubscriberAuxiliaryService subAuxSvc) throws ExtensionAssociationException
    {
        String subscriberId = subAuxSvc.getSubscriberIdentifier();
        Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
        if (oldSub == null)
        {
            try
            {
                oldSub = SubscriberSupport.lookupSubscriberForSubId(ctx, subscriberId);
            }
            catch (HomeException t)
            {
                throw new ExtensionAssociationException(ExternalAppEnum.BSS, "Unable to retrieve subscriber "
                        + subscriberId + ": " + t.getMessage(), ExternalAppSupport.BSS_DATABASE_FAILURE_SUBSCRIPTION_RETRIEVAL, t, false);
            }
        }
        disableHZ(ctx, oldSub);
    }


    private void disableHZ(Context ctx, Subscriber sub) throws ExtensionAssociationException
    {
        boolean updateHomeZone = (((AppHomezoneClientConfig) ctx.get(AppHomezoneClientConfig.class))
                .getContactHomezone());
        HomezoneCount hzCntBean = null;
        try
        {
            hzCntBean = HomeSupportHelper.get(ctx).findBean(ctx, HomezoneCount.class, sub.getId());
        }
        catch (HomeException t)
        {
            throw new ExtensionAssociationException(ExternalAppEnum.BSS, "Unable to retrieve HomeZone Count for subscriber " + sub.getId()
                    + ": " + t.getMessage(), ExternalAppSupport.BSS_DATABASE_FAILURE_SUBSCRIPTION_HOMEZONE_COUNT_RETRIEVAL, t, false);
        }

        if (hzCntBean == null)
        {
            LogSupport.minor(ctx, this, "No homezone count bean found with subscriberID" + sub.getId(), null);
            throw new ExtensionAssociationException(ExternalAppEnum.BSS, "No HomeZone Count could be find for subscriber " + sub.getId(), ExternalAppSupport.BSS_DATABASE_FAILURE_SUBSCRIPTION_HOMEZONE_COUNT_RETRIEVAL);
        }

        int hzCount = hzCntBean.getHzcount();
        
        if (hzCount == 1)
        {
            try
            {
                int result = com.redknee.app.osa.ecp.provision.ErrorCode.SUCCESS;
                if (updateHomeZone)
                {
                    result = AppEcpClientSupport.disableHomeZoneInECP(ctx, sub);
                }
                if (result!=com.redknee.app.osa.ecp.provision.ErrorCode.SUCCESS)
                {
                    throw new ExtensionAssociationException(ExternalAppEnum.VOICE, "Error disabling home zone in ECP", "Unable to disable HomeZone on URCS Voice", result);
                }
            }
            catch (Exception e)
            {
                String msg = "Could not disable homezone flag in ECP for subscriber:" + sub.getId() + ": " + e.getMessage();
                LogSupport.minor(ctx, this, msg, e);
                throw new ExtensionAssociationException(ExternalAppEnum.VOICE, msg, ExternalAppSupport.NO_CONNECTION, e, false);
            }
        }
        hzCntBean.setHzcount(hzCount - 1);
        
        try
        {
            SubscriberAuxiliaryServiceSupport.updateSubscriberHzCountWithBean(ctx, hzCntBean);
        }
        catch (HomeException t)
        {
            throw new ExtensionAssociationException(ExternalAppEnum.BSS, "Unable to update HomeZone Count for subscriber " + sub.getId()
                    + ": " + t.getMessage(), ExternalAppSupport.BSS_DATABASE_FAILURE_SUBSCRIPTION_HOMEZONE_COUNT_UPDATE, t, false);
        }
    }
        

    private AppHomezoneClient getHomezoneClient(Context ctx) throws ExtensionAssociationException
    {
        AppHomezoneClient hzClient = (AppHomezoneClient) ctx.get(AppHomezoneClient.class);
        if (hzClient == null)
        {
            throw new ExtensionAssociationException(ExternalAppEnum.HOMEZONE, "AppHomezoneClient not installed.", ExternalAppSupport.NO_CONNECTION);
        }
        return hzClient;
    }


    private SubscriberHomezoneInfo fillUpSubscriberHomezoneInfo(Context ctx, String MSISDN,
            SubscriberAuxiliaryService subAuxSvc, long auxIdentifier, HomeZoneAuxSvcExtension basicHZone)
    {
        SubscriberHomezoneInfo subHomezone = new SubscriberHomezoneInfo();
        subHomezone.zoneId = (int) auxIdentifier;
        subHomezone.msisdn = MSISDN;
        subHomezone.priority = subAuxSvc.getHzPriority(ctx) + 1;// change this
        // later
        subHomezone.x = subAuxSvc.getHzX(ctx);
        subHomezone.y = subAuxSvc.getHzY(ctx);
        subHomezone.r = basicHZone.getRadius();
        return subHomezone;
    }


    private SubscriberHomezone convertIntoSubHZone(SubscriberHomezoneInfo subHomezone)
    {
        SubscriberHomezone hZone = new SubscriberHomezone();
        hZone.setHzId(subHomezone.zoneId);
        hZone.setHzPriority(subHomezone.priority - 1);// change this later
        hZone.setHzX(subHomezone.x);
        hZone.setHzY(subHomezone.y);
        hZone.setMsisdn(subHomezone.msisdn);
        return hZone;
    }


    private boolean isMSISDNSame(String oldMSISDN, String newMSISDN)
    {
        return SafetyUtil.safeEquals(oldMSISDN, newMSISDN);
    }


    private SubscriberHomezone provisionHomeZone(Context ctx, SubscriberAuxiliaryService subAuxSvc)
            throws ExtensionAssociationException
    {
        SubscriberHomezone subHomeZone = null;
        String subscriberId = subAuxSvc.getSubscriberIdentifier();
        long auxServiceId = subAuxSvc.getAuxiliaryServiceIdentifier();
        try
        {
            
            Subscriber newSub = SubscriberSupport.getSubscriberForAuxiliaryService(ctx, subAuxSvc);
            HomeZoneAuxSvcExtension basicHZone = getRelatedHomeZone(ctx, auxServiceId);
            AppHomezoneClient hzClient = getHomezoneClient(ctx);
            // provisions the subscriber in the homezone
            SubscriberHomezoneInfo subHomezoneArr[] = new SubscriberHomezoneInfo[1];
            
            subHomezoneArr[0] = fillUpSubscriberHomezoneInfo(ctx, newSub.getMSISDN(), subAuxSvc,
                    this.getAuxiliaryServiceId(), basicHZone);

            int rsCode = hzClient.createSubscriberHomezone(newSub.getMSISDN(), subHomezoneArr);
            if (rsCode != ErrorCode.SUCCESS)
            {
                throw new AppHomezoneClientException(hzClient.putResultCodeDebug(rsCode, "createSubscriberHomezone"),
                        (short) rsCode);
            }

            subHomeZone = convertIntoSubHZone(subHomezoneArr[0]);
            subHomeZone.setIdentifier(subAuxSvc.getIdentifier());
            subHomeZone.setHzCellID(subAuxSvc.getHzCellID(ctx));
        }
        catch (HomeException t)
        {
            throw new ExtensionAssociationException(ExternalAppEnum.BSS,
                    "Provision homezone could be find for subscriber ",
                    ExternalAppSupport.BSS_DATABASE_FAILURE_SUBSCRIPTION_RETRIEVAL);
        }
        catch (AppHomezoneClientException t)
        {
            throw new ExtensionAssociationException(ExternalAppEnum.HOMEZONE, "Unable to create homezone in HomeZone application. SubscriberID="
                    + subscriberId + ", auxiliaryServiceID=" + auxServiceId + ": Message returned: " + t.getMessage(), t.getResultCode(),
                    t, false);
        }
        return subHomeZone;
    }
    
    
    private HomeZoneAuxSvcExtension getRelatedHomeZone(Context ctx, long auxServiceId)
            throws ExtensionAssociationException
    {
        try
        {
            HomeZoneAuxSvcExtension hZone = HomeSupportHelper.get(ctx).findBean(ctx, HomeZoneAuxSvcExtension.class,
                    new EQ(HomeZoneAuxSvcExtensionXInfo.AUXILIARY_SERVICE_ID, Long.valueOf(auxServiceId)));
            return hZone;
        }
        catch (final HomeException exception)
        {
            final String message = "HomeZone info with ID = " + auxServiceId + " couldn't be found";
            throw new ExtensionAssociationException(ExternalAppEnum.BSS, message, ExternalAppSupport.BSS_DATABASE_FAILURE_SUBSCRIPTION_HOMEZONE_RETRIEVAL, exception, false);
        }
    }

    public static final double MAX_X = 180;
    public static final double MIN_X = -180;
    public static final double MAX_Y = 90;
    public static final double MIN_Y = -90;

}
