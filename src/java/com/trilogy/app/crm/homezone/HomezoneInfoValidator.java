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
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */

package com.trilogy.app.crm.homezone;

import java.util.List;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.client.AppHomezoneClient;
import com.trilogy.app.crm.config.AppHomezoneClientConfig;
import com.trilogy.app.crm.extension.auxiliaryservice.core.custom.HomeZoneAuxSvcExtension;
import com.trilogy.app.homezone.corba.ErrorCode;
import com.trilogy.app.homezone.corba.LocationMappingInfo;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;


/**
 * Provides bean level validation for CellID and X,Y values for Auxiliary service of
 * Homezone type Following are the rules:
 * _______________________________________________________________________ # CellID X Y |
 * Rule _______________________________________________________________________ 1 E E E |
 * X and Y take priority, CellID is ignored. 2 E E B | Not allowed, either enter X "AND" Y
 * or none of them. 3 E B E | Not allowed, either enter X "AND" Y or none of them. 4 E B B |
 * CellID is used to fetch X,Y from AppHomzone. Valid mapping shd be in AppHomezone else
 * error. 5 B E E | X and Y are used directly 6 B E B | Not allowed, either enter X "AND"
 * Y or none of them. 7 B B E | Not allowed, either enter X "AND" Y or none of them. 8 B B
 * B | Not allowed, either enter X "AND" Y or CellID.
 *
 * @author pkulkarni
 */
public class HomezoneInfoValidator implements Validator
{
    // INHERIT
    public void validate(final Context ctx, final Object obj) throws IllegalStateException
    {
        final SubscriberAuxiliaryService auxService = (SubscriberAuxiliaryService) obj;

        validateCellIDXnY(ctx, auxService);
    }


    private void validateCellIDXnY(Context ctx, final SubscriberAuxiliaryService auxService)
    {
        final long auxServiceId = auxService.getAuxiliaryServiceIdentifier();
        final String subId = auxService.getSubscriberIdentifier();
        String cellID = auxService.getHzCellID(ctx);
        final double hzX = auxService.getHzX(ctx);
        final double hzY = auxService.getHzY(ctx);
        cellID = cellID == null ? "" : cellID;

        // Rule# 1,5 -No action
        if (hzX != HomeZoneAuxSvcExtension.DEFAULT_HZX && hzY != HomeZoneAuxSvcExtension.DEFAULT_HZY)
        {
            if (hzX < HomeZoneAuxSvcExtension.MIN_X || hzX > HomeZoneAuxSvcExtension.MAX_X
                    || hzY < HomeZoneAuxSvcExtension.MIN_Y || hzY > HomeZoneAuxSvcExtension.MAX_Y)
            {
                throw new IllegalStateException("HomeZone Error::Auxiliary Service ID [" + auxServiceId
                        + "], Subscriber ID[" + subId + "]: X should be in the range " + HomeZoneAuxSvcExtension.MIN_X
                        + " to " + HomeZoneAuxSvcExtension.MAX_X + " and Y should be in the range " + HomeZoneAuxSvcExtension.MIN_Y
                        + " to " + HomeZoneAuxSvcExtension.MAX_Y + " You entered X=" + hzX + " and Y=" + hzY);
            }
            else
            {
                return;
            }
        }

        // Rule# 2,6
        if (hzX != HomeZoneAuxSvcExtension.DEFAULT_HZX)
        {
            if (hzY == HomeZoneAuxSvcExtension.DEFAULT_HZY)
            {
                throw new IllegalStateException("HomeZone Error::Auxiliary Service ID[" + auxServiceId
                        + "],Subscriber ID[" + subId + "]:Either enter CellID or ('X' AND 'Y')."
                        + " You entered only X value.");
            }
        }
        // Rule# 3,7
        if (hzY != HomeZoneAuxSvcExtension.DEFAULT_HZY)
        {
            if (hzX == HomeZoneAuxSvcExtension.DEFAULT_HZX)
            {
                throw new IllegalStateException("HomeZone Error::Auxiliary Service ID[" + auxServiceId
                        + "],Subscriber ID[" + subId + "]:Either enter CellID or ('X' AND 'Y')."
                        + " You entered only Y value.");
            }
        }

        if (hzX == HomeZoneAuxSvcExtension.DEFAULT_HZX && hzY == HomeZoneAuxSvcExtension.DEFAULT_HZY)
        {
            // Rule#8
            if (cellID.trim().equals(""))
            {
                throw new IllegalStateException("HomeZone Error::Auxiliary Service ID[" + auxServiceId
                        + "],Subscriber ID[" + subId + "]:Either enter CellID or ('X' AND 'Y')." + " You entered none.");
            }
            else
            {
                // Rule#4
                // query AppHomezone to see whether the given cellID maps to any
                // X and Y values
                validateCellID(ctx, auxService, auxServiceId, subId, cellID);
            }
        }
    }


    // if valid mapping found X and Y values are set to the fetched values.
    private void validateCellID(Context ctx, final SubscriberAuxiliaryService auxService,
            final long auxServiceId, final String subId, final String cellID)
    {
        final boolean updateHomeZone = ((AppHomezoneClientConfig) ctx.get(AppHomezoneClientConfig.class))
                .getContactHomezone();

        if (updateHomeZone)
        {
            final AppHomezoneClient hzClient = (AppHomezoneClient) ctx.get(AppHomezoneClient.class);
            LocationMappingVO locnVO = null;

            if (hzClient == null)
            {
                throw new IllegalStateException("Configuration Error: Can not connect to AppHomezone.");
            }

            try
            {
                locnVO = hzClient.queryLocationMapping(cellID);
            }
            catch (final Throwable t)
            {
                throw new IllegalStateException("Error: Error while querying AppHomezone for cellID=" + cellID);
            }

            if (locnVO.getResultCode_() != ErrorCode.SUCCESS)
            {
                throw new IllegalStateException("HomeZone Error::Auxiliary Service ID[" + auxServiceId
                        + "],Subscriber ID[" + subId
                        + "]:Can not fetch corresponding X,Y vales. No valid location mapping exists for CellId="
                        + cellID + " Please verify CellID");
            }
            else
            {
                final LocationMappingInfo locationInfo = locnVO.getLocnMapInfo_();
                if (locationInfo != null)
                {
                    auxService.setHzX(locationInfo.x);
                    auxService.setHzY(locationInfo.y);
                }
            }
        }
    }


    // if valid mapping found X and Y values are set to the fetched values.
    public void validatePriorities(Context ctx, final List auxServices, final int spID)
    {
        SubscriberAuxiliaryService currentSvc = null;
        SubscriberAuxiliaryService nextSvc = null;
        CRMSpid provider = null;
        try
        {
            final Home home = (Home) ctx.get(CRMSpidHome.class);
            provider = (CRMSpid) home.find(ctx, Integer.valueOf(spID));
        }
        catch (final HomeException e)
        {
            throw new IllegalStateException("HomeZone Configuration Error::No maximum home zones entry found for SPID"
                    + spID);
        }

        final int maxAllowedHZ = provider.getMaxZones();
        final int numberOfSelectedSvc = auxServices.size();
        if (numberOfSelectedSvc != 0)
        {
            if (numberOfSelectedSvc > maxAllowedHZ)
            {
                throw new IllegalStateException("HomeZone Error::Subscriber ID["
                        + ((SubscriberAuxiliaryService) auxServices.get(0)).getSubscriberIdentifier() + "]: Only "
                        + maxAllowedHZ + " homezone services are allowed," + "You have selected " + numberOfSelectedSvc);
            }

            for (int i = 0; i < numberOfSelectedSvc; i++)
            {
                currentSvc = (SubscriberAuxiliaryService) auxServices.get(i);
                for (int j = i + 1; j < numberOfSelectedSvc; j++)
                {
                    nextSvc = (SubscriberAuxiliaryService) auxServices.get(j);
                    if (currentSvc.getHzPriority() == nextSvc.getHzPriority())
                    {
                        throw new IllegalStateException("HomeZone Error::Subscriber ID["
                                + currentSvc.getSubscriberIdentifier()
                                + "]: No two homezone auxiliary services can have same priority");
                    }
                }
            }
        }
    }


    /*
     * *******************************************************************************************
     */
    /*
     * Validate methods used for For AuxiliaryService, same as above only change is they
     * validate info entered in AuxiliaryServcie Instead of info in
     * SubscriberAuxiliaryService
     */
    public static void validateCellIDXnYForAuxSvc(Context ctx, final HomeZoneAuxSvcExtension extension)
    {
        String cellID = extension.getHzCellID();
        cellID = cellID == null ? "" : cellID;

        if (extension.getHzX() == HomeZoneAuxSvcExtension.DEFAULT_HZX && extension.getHzY() == HomeZoneAuxSvcExtension.DEFAULT_HZY)
        {
            // Rule#8
            if (cellID.length() > 0)
            {
                // Rule#4
                // query AppHomezone to see whether the given cellID maps to any X and Y values
                validateCellIDForAuxSvc(ctx, extension, cellID);
            }
        }
    }


    // if valid mapping found X and Y values are set to the fetched values.
    private static void validateCellIDForAuxSvc(Context ctx, final HomeZoneAuxSvcExtension extension, final String cellID)
    {
        final boolean updateHomeZone = ((AppHomezoneClientConfig) ctx.get(AppHomezoneClientConfig.class))
                .getContactHomezone();

        if (updateHomeZone)
        {
            final AppHomezoneClient hzClient = (AppHomezoneClient) ctx.get(AppHomezoneClient.class);
            LocationMappingVO locnVO = null;

            if (hzClient == null)
            {
                throw new IllegalStateException("Configuration Error: Can not connect to AppHomezone.");
            }

            try
            {
                locnVO = hzClient.queryLocationMapping(cellID);
            }
            catch (final Throwable t)
            {
                final IllegalStateException isExcp = new IllegalStateException(
                        "Error: Error while querying AppHomezone for cellID=" + cellID);
                isExcp.initCause(t);
                throw isExcp;
            }

            if (locnVO.getResultCode_() != ErrorCode.SUCCESS)
            {
                throw new IllegalStateException("HomeZone Error::Auxiliary Service ID[" + extension.getAuxiliaryServiceId()
                        + "]:Can not fetch corresponding X,Y vales. No valid location mapping exists for CellId="
                        + cellID + " Please verify CellID");
            }
            else
            {
                final LocationMappingInfo locationInfo = locnVO.getLocnMapInfo_();
                if (locationInfo != null)
                {
                    extension.setHzX(locationInfo.x);
                    extension.setHzY(locationInfo.y);
                }
            }
        }
    }

}
