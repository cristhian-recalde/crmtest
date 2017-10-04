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
package com.trilogy.app.crm.client.urcs;

import static com.redknee.app.crm.client.CorbaClientTrapIdDef.PRICE_PLAN_MGMT_SVC_DOWN;
import static com.redknee.app.crm.client.CorbaClientTrapIdDef.PRICE_PLAN_MGMT_SVC_UP;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.LogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;

import com.trilogy.app.urcs.vpn.BusinessGroupPricePlanProvisioning;
import com.trilogy.app.urcs.vpn.ErrorCode;
import com.trilogy.app.urcs.vpn.GetBusinessGroupPricePlanResult;
import com.trilogy.app.urcs.vpn.SetBusinessGroupPricePlanResult;
import com.trilogy.app.urcs.vpn.param.Parameter;
import com.trilogy.util.snippet.log.Logger;

import com.trilogy.app.crm.client.AbstractCrmClient;

/**
 * CORBA client for the PricePlanMgmt interface to URCS.
 * Supports clustered CORBA client. Uses reusable elements to an abstract class AbstractCrmClient<T>
 * 
 * @author victor.stratan@redknee.com
 * @since CRM 8.2.2
 */
public class BGroupPricePlanMgmtCorbaClient extends AbstractCrmClient<BusinessGroupPricePlanProvisioning>
        implements BGroupPricePlanClient 
{
    /**
     * Name of the CORBA client.
     */
    private static final String CLIENT_NAME = "BusinessGroupPricePlanClient";

    /**
     * Service description.
     */
    private static final String SERVICE_DESCRIPTION = "CORBA client to update and query Business Group/Price Plan mapping";

    public BGroupPricePlanMgmtCorbaClient(final Context ctx)
    {
        // TODO change the trap IDs
        super(ctx, CLIENT_NAME, SERVICE_DESCRIPTION, BusinessGroupPricePlanProvisioning.class,
                PRICE_PLAN_MGMT_SVC_DOWN, PRICE_PLAN_MGMT_SVC_UP);
    }
    
    
    /**
     * Used for Logging
     * @return
     */
    private String getModule()
    {
        return this.getClass().getName();
    }
    


    /**
     *
     * {@inheritDoc}
     */
    public long getBusinessGroupPricePlan(final Context ctx, final int spid, final String businessGroupID)
        throws BGroupPricePlanException
    {
        final LogMsg pm = new PMLogMsg(getModule(), "URCS.BusinessGroupPricePlan",
                "Query a Business Group mappingD");
        GetBusinessGroupPricePlanResult callResult;
        long result = -1;
        try
        {
            final BusinessGroupPricePlanProvisioning service = getService();
            if (service == null)
            {
                final BGroupPricePlanException exception = new BGroupPricePlanException((short) FAILED,
                        "Cannot retrieve " + CLIENT_NAME + " CORBA service.");
                Logger.minor(ctx, this, exception.getMessage(), exception);
                throw exception;
            }

            if (Logger.isInfoEnabled())
            {
                final StringBuilder msg = new StringBuilder();
                msg.append("Sending CORBA request BusinessGroupPricePlanProvisioning.getBusinessGroupPricePlan");
                msg.append(" to URCS with parameters ");
                toStringGetRequest(msg, spid, businessGroupID);
                Logger.info(ctx, this, msg.toString());
            }

            final Parameter[] inParam = new Parameter[0];
            callResult = service.getBusinessGroupPricePlan(spid, businessGroupID, inParam);

            if (callResult.resultCode != ErrorCode.SUCCESS)
            {
                final StringBuilder msgBldr = new StringBuilder();
                msgBldr.append("URCS Business Group/Price Plan mapping query request=");
                toStringGetRequest(msgBldr, spid, businessGroupID);
                msgBldr.append(" returned with this error: ");
                msgBldr.append(BGroupPricePlanException.getVerboseResult(ctx, callResult.resultCode));
                Logger.minor(ctx, this, msgBldr.toString());
                final BGroupPricePlanException exception = new BGroupPricePlanException(callResult.resultCode,
                        msgBldr.toString());
                throw exception;
            }
            
            result = Long.valueOf(callResult.pricePlanID);
        }
        catch (final org.omg.CORBA.SystemException e)
        {
            final StringBuilder msgBldr = new StringBuilder();
            msgBldr.append("CORBA communication with URCS BusinessGroupPricePlanProvisioning Service server failed");
            msgBldr.append(" in request=");
            toStringGetRequest(msgBldr, spid, businessGroupID);
            final String msg = msgBldr.toString();
            Logger.minor(ctx, this, msg, e);
            final BGroupPricePlanException exception = new BGroupPricePlanException((short) COMMUNICATION_FAILURE,
                    msg, e);
            throw exception;
        }
        catch (final NumberFormatException e)
        {
            final StringBuilder msgBldr = new StringBuilder();
            msgBldr.append("Unable to parse to long the result returned from BusinessGroupPricePlanProvisioning ");
            msgBldr.append(" request=");
            toStringGetRequest(msgBldr, spid, businessGroupID);
            final String msg = msgBldr.toString();
            Logger.minor(ctx, this, msg, e);
            final BGroupPricePlanException exception = new BGroupPricePlanException((short) COMMUNICATION_FAILURE,
                    msg, e);
            throw exception;
        }
        catch (final Throwable t)
        {
            final StringBuilder msgBldr = new StringBuilder();
            msgBldr.append("Unexpected failure in CORBA communication with URCS BusinessGroupPricePlanProvisioning");
            msgBldr.append(" in request=");
            toStringGetRequest(msgBldr, spid, businessGroupID);
            final String msg = msgBldr.toString();
            Logger.minor(ctx, this, msg, t);
            final BGroupPricePlanException exception = new BGroupPricePlanException((short) COMMUNICATION_FAILURE,
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
     *
     * {@inheritDoc}
     */
    public void setBusinessGroupPricePlan(final Context ctx, final int spid, final String businessGroupID,
            final long pricePlanID) throws BGroupPricePlanException
    {
        final LogMsg pm = new PMLogMsg(getModule(), "URCS.BusinessGroupPricePlan",
                "Associate (or disassociate) a Business group with a Priceplan ID");
        SetBusinessGroupPricePlanResult callResult;
        try
        {
            final BusinessGroupPricePlanProvisioning service = getService();
            if (service == null)
            {
                final BGroupPricePlanException exception = new BGroupPricePlanException((short) FAILED,
                        "Cannot retrieve PricePlanMgmt CORBA service.");
                Logger.minor(ctx, this, exception.getMessage(), exception);
                throw exception;
            }

            if (Logger.isInfoEnabled())
            {
                final StringBuilder msg = new StringBuilder();
                msg.append("Sending CORBA request BusinessGroupPricePlanProvisioning.setBusinessGroupPricePlan");
                msg.append(" to URCS with parameters ");
                toStringSetRequest(msg, spid, businessGroupID, pricePlanID);
                Logger.info(ctx, this, msg.toString());
            }

            final String ppID;
            if (pricePlanID >= 0)
            {
                ppID = String.valueOf(pricePlanID);
            }
            else
            {
                ppID = "";
            }
            final Parameter[] inParam = new Parameter[0];
            callResult = service.setBusinessGroupPricePlan(spid, businessGroupID, ppID, inParam);

            if (callResult.resultCode != ErrorCode.SUCCESS)
            {
                final StringBuilder msgBldr = new StringBuilder();
                msgBldr.append("URCS Business Group/Price Plan mapping request=");
                toStringSetRequest(msgBldr, spid, businessGroupID, pricePlanID);
                msgBldr.append(" returned with this error: ");
                msgBldr.append(BGroupPricePlanException.getVerboseResult(ctx, callResult.resultCode));
                Logger.minor(ctx, this, msgBldr.toString());
                final BGroupPricePlanException exception = new BGroupPricePlanException(callResult.resultCode,
                        msgBldr.toString());
                throw exception;
            }
        }
        catch (final org.omg.CORBA.SystemException e)
        {
            final StringBuilder msgBldr = new StringBuilder();
            msgBldr.append("CORBA communication with URCS BusinessGroupPricePlanProvisioning Service server failed");
            msgBldr.append(" while mapping Business Group to Price Plan with ");
            toStringSetRequest(msgBldr, spid, businessGroupID, pricePlanID);
            final String msg = msgBldr.toString();
            Logger.minor(ctx, this, msg, e);
            final BGroupPricePlanException exception = new BGroupPricePlanException((short) COMMUNICATION_FAILURE,
                    msg, e);
            throw exception;
        }
        catch (final Throwable t)
        {
            final StringBuilder msgBldr = new StringBuilder();
            msgBldr.append("Unexpected failure in CORBA communication with URCS BusinessGroupPricePlanProvisioning");
            msgBldr.append(" while mapping Business Group to Price Plan with ");
            toStringSetRequest(msgBldr, spid, businessGroupID, pricePlanID);
            final String msg = msgBldr.toString();
            Logger.minor(ctx, this, msg, t);
            final BGroupPricePlanException exception = new BGroupPricePlanException((short) COMMUNICATION_FAILURE,
                    msg, t);
            throw exception;
        }
        finally
        {
            pm.log(ctx);
        }
    }

    private void toStringGetRequest(final StringBuilder msg, final long spid, final String businessGroupID)
    {
        msg.append("[getBusinessGroupPricePlan SPID=");
        msg.append(spid);
        msg.append(", BusinessGroupID=");
        msg.append(businessGroupID);
        msg.append("]");
    }

    private void toStringSetRequest(final StringBuilder msg, final long spid, final String businessGroupID,
            final long pricePlanID)
    {
        msg.append("[setBusinessGroupPricePlan SPID=");
        msg.append(spid);
        msg.append(", BusinessGroupID=");
        msg.append(businessGroupID);
        msg.append(", PricePlanId=");
        msg.append(pricePlanID);
        msg.append("]");
    }
}
