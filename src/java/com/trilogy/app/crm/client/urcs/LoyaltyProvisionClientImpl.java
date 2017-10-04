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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.product.bundle.manager.provision.v4_0.loyalty.LoyaltyCardProfile;
import com.trilogy.product.bundle.manager.provision.v4_0.loyalty.LoyaltyCardProfileHolder;
import com.trilogy.product.bundle.manager.provision.v4_0.loyalty.LoyaltyProfileProvision;
import com.trilogy.product.bundle.manager.provision.v4_0.loyalty.error.ErrorCode;
import com.trilogy.product.bundle.manager.provision.v4_0.loyalty.param.Parameter;
import com.trilogy.product.bundle.manager.provision.v4_0.loyalty.param.ParameterID;
import com.trilogy.product.bundle.manager.provision.v4_0.loyalty.param.ParameterSetHolder;
import com.trilogy.product.bundle.manager.provision.v4_0.loyalty.param.ParameterValue;

import com.trilogy.app.crm.client.AbstractCrmClient;
import com.trilogy.app.crm.client.RemoteServiceException;



/**
 * CORBA version of LoyaltyProvisionClient, provides Loyalty Provision API on URCS.
 * 
 * @author asim.mahmood@redknee.com
 * @since 9.1
 */
public class LoyaltyProvisionClientImpl extends AbstractCrmClient<LoyaltyProfileProvision>
        implements LoyaltyProvisionClient
{

    private static final Class<LoyaltyProfileProvision> SERVICE_TYPE = LoyaltyProfileProvision.class;
    private static final String FAILED_MESSAGE_PREFIX = "CORBA comunication failure during ";
    private static final String FAILED_MESSAGE_SUFFIX = " failed.";
    public static final String URCS_SERVICE_NAME = "LoyaltyProvisionClient";
    public static final String URCS_SERVICE_DESCRIPTION = "CORBA client for Loyalty Provision services";


    public LoyaltyProvisionClientImpl(final Context ctx)
    {
        super(ctx, URCS_SERVICE_NAME, URCS_SERVICE_DESCRIPTION, SERVICE_TYPE);
    }


    @Override
    public String version()
    {
        return "1.0";
    }



    @Override
    public LoyaltyCardProfile createLoyaltyProfile(Context ctx, LoyaltyCardProfile profile)
            throws RemoteServiceException
    {
        final String methodName = "createLoyaltyProfile";
        final LoyaltyProfileProvision client = getClient(ctx);
        
        if (LogSupport.isDebugEnabled(ctx))
        {
            String msg = String.format("%s(profile=%s)", methodName, LoyaltyParameters.loyaltyCardProfileString(profile));
            new DebugLogMsg(this, msg, null).log(ctx);
        }
        //Input
        final Parameter[] inParams = new Parameter[0];
        
        //Output
        final ParameterSetHolder outParams = new ParameterSetHolder();
        final int resultCode;
             
        try
        {
            resultCode = client.createLoyaltyProfile(profile, inParams, outParams);
        }
        catch (Exception exception)
        {
            throw new RemoteServiceException(ErrorCode.INTERNAL_ERROR, FAILED_MESSAGE_PREFIX + methodName
                    + FAILED_MESSAGE_SUFFIX, exception);
        }

        if (resultCode != ErrorCode.SUCCESS)
        {
            final String msg = String.format(
                    "Failure in URCS method '%s' [rc:%d] (%s) when creating loyalty profile for profile=%s",
                    methodName, resultCode, LoyaltyReturnCodeHelper.getProfileMessage(ctx, resultCode), LoyaltyParameters.loyaltyCardProfileString(profile));
            new MinorLogMsg(this, msg, null).log(ctx);
            throw new RemoteServiceException(resultCode, msg);
        }
        return profile;
    }


    @Override
    public LoyaltyCardProfile getLoyaltyProfile(Context ctx, String ban, String cardId, String programId) throws RemoteServiceException
    {
        final String methodName = "getLoyaltyProfile";
        final LoyaltyProfileProvision client = getClient(ctx);
        
        if (LogSupport.isDebugEnabled(ctx))
        {
            String msg = String.format("%s(ban=%s, cardId=%s, programId=%s)", methodName, ban, cardId, programId);
            new DebugLogMsg(this, msg, null).log(ctx);
        }
        //Input
        ban = ban == null ? "" : ban;
        cardId = cardId == null ? "" : cardId;
        List<Parameter> inParams = new ArrayList<Parameter>();
        addParameter(inParams, ParameterID.IN_PROGRAM_ID, programId);
        Parameter[] inParamSet = inParams.toArray(new Parameter[inParams.size()]);


        //Output
        LoyaltyCardProfileHolder cardHolder = new LoyaltyCardProfileHolder();
        final ParameterSetHolder outParams = new ParameterSetHolder();
        final int resultCode;
         
        try
        {
            
            resultCode = client.getLoyaltyProfile(cardId, ban, inParamSet, cardHolder, outParams);
        }
        catch (Exception exception)
        {
            throw new RemoteServiceException(ErrorCode.INTERNAL_ERROR, FAILED_MESSAGE_PREFIX + methodName
                    + FAILED_MESSAGE_SUFFIX, exception);
        }

        //Return null if referenced by BAN
        if (resultCode == ErrorCode.LOYALTY_RECORD_NOT_FOUND && !ban.isEmpty())
        {
            final String msg = String.format(
                    "Failure in '%s' [rc:%d] (%s) when querying points balance in URCS for account=%s, cardId=%s, programId=%s. Returning null",
                    methodName, resultCode, LoyaltyReturnCodeHelper.getOperationMessage(ctx, resultCode), ban, cardId, programId);
            new InfoLogMsg(this, msg, null).log(ctx);
            return null;
        }
        else if (resultCode != ErrorCode.SUCCESS)
        {
            final String msg = String.format(
                    "Failure in URCS method '%s' [rc:%d] (%s) when retriving loyalty profile for account=%s, cardId=%s, programId=%s",
                    methodName, resultCode, LoyaltyReturnCodeHelper.getProfileMessage(ctx, resultCode), ban, cardId, programId);
            new MinorLogMsg(this, msg, null).log(ctx);
            throw new RemoteServiceException(resultCode, msg);
        }
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, methodName + " response=" + LoyaltyParameters.loyaltyCardProfileString(cardHolder.value), null).log(ctx);
        }
        return cardHolder.value;
    }


    @Override
    public void updateLoyaltyProfile(Context ctx, String ban, String cardId, String programId, Boolean enableAccumulation,
            Boolean enableRedemption) throws RemoteServiceException
    {
        final String methodName = "updateLoyaltyProfile";
        final LoyaltyProfileProvision client = getClient(ctx);
        
        if (LogSupport.isDebugEnabled(ctx))
        {
            String msg = String.format("%s(ban=%s, cardId=%s, programId=%s, enableAccumulation=%s, enableRedemption=%s)",
                    methodName, ban, cardId, programId, enableAccumulation, enableRedemption);
            new DebugLogMsg(this, msg, null).log(ctx);
        }
        
        //Input
        ban = (ban == null ? "" : ban);
        cardId = (cardId == null ? "" : cardId);
        List<Parameter> inParams = new ArrayList<Parameter>();
        addParameter(inParams, ParameterID.IN_PROGRAM_ID, programId);
        addParameter(inParams, ParameterID.IN_REDEMPTION_ALLOWED, enableRedemption);
        addParameter(inParams, ParameterID.IN_ACCUMULATION_ALLOWED, enableAccumulation);
        Parameter[] inParamSet = inParams.toArray(new Parameter[inParams.size()]);     

        //Output
        final ParameterSetHolder outParams = new ParameterSetHolder();
        final int resultCode;
       
        try
        {
            resultCode = client.updateLoyaltyProfile(cardId, ban, inParamSet, outParams);
        }
        catch (Exception exception)
        {
            throw new RemoteServiceException(ErrorCode.INTERNAL_ERROR, FAILED_MESSAGE_PREFIX + methodName
                    + FAILED_MESSAGE_SUFFIX, exception);
        }

        if (resultCode != ErrorCode.SUCCESS)
        {
            final String msg = String.format(
                    "Failure in URCS method '%s' [rc:%d] (%s) when updating loyalty profile for account=%s, cardId=%s, programId=%s",
                    methodName, resultCode, LoyaltyReturnCodeHelper.getProfileMessage(ctx, resultCode), ban, cardId, programId);
            new MinorLogMsg(this, msg, null).log(ctx);
            throw new RemoteServiceException(resultCode, msg);
        }
        return;
    }


    private void addParameter(List<Parameter> inParams, short paramId,  String value)
    {
        if (value != null)
        {
            Parameter param = new Parameter();
            param.parameterID = paramId;
            param.value = new ParameterValue();
            param.value.stringValue(value);
            inParams.add(param);
        }
    }


    private void addParameter(List<Parameter> inParams, short paramId, Boolean value)
    {
        if (value != null)
        {
            Parameter param = new Parameter();
            param.parameterID = paramId;
            param.value = new ParameterValue();
            param.value.booleanValue(value);
            inParams.add(param);
        }
    }
}
