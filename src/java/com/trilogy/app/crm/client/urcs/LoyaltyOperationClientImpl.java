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
import java.util.List;

import org.omg.CORBA.LongHolder;
import org.omg.CORBA.StringHolder;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.client.AbstractCrmClient;
import com.trilogy.app.crm.client.RemoteServiceException;

import com.trilogy.app.urcs.loyaltyoperation.Amount;
import com.trilogy.app.urcs.loyaltyoperation.AmountHolder;
import com.trilogy.app.urcs.loyaltyoperation.LoyaltyOperationsInterface;
import com.trilogy.app.urcs.loyaltyoperation.RedemptionType;
import com.trilogy.app.urcs.loyaltyoperation.param.Parameter;
import com.trilogy.app.urcs.loyaltyoperation.param.ParameterID;
import com.trilogy.app.urcs.loyaltyoperation.param.ParameterValue;
import com.trilogy.app.urcs.loyaltyoperation.param.ParameterSetHolder;
import com.trilogy.app.urcs.loyaltyoperation.error.ErrorCode;


/**
 * CORBA version of LoyaltyOperationClient, provides Loyalty Operation API on URCS.
 * TODO: clean up parameter passing using LoyaltyParameters class.
 * 
 * @author asim.mahmood@redknee.com
 * @since 9.1
 */
public class LoyaltyOperationClientImpl extends AbstractCrmClient<LoyaltyOperationsInterface>
        implements LoyaltyOperationClient
{

    private static final Class<LoyaltyOperationsInterface> SERVICE_TYPE = LoyaltyOperationsInterface.class;
    private static final String FAILED_MESSAGE_PREFIX = "CORBA comunication failure during ";
    private static final String FAILED_MESSAGE_SUFFIX = " method.";
    public static final String URCS_SERVICE_NAME = "LoyaltyOperationsInterface";
    public static final String URCS_SERVICE_DESCRIPTION = "CORBA client for Loyalty Operations services";


    public LoyaltyOperationClientImpl(final Context ctx)
    {
        super(ctx, URCS_SERVICE_NAME, URCS_SERVICE_DESCRIPTION, SERVICE_TYPE);
    }


    @Override
    public String version()
    {
        return "1.0";
    }


    @Override
    public LoyaltyParameters redeemLoyaltyPoints(Context ctx, String ban, String cardId, String programId, 
            String extTransactionId, Integer sourceType, long points, String userId, String userNote, 
            String userLocation)
            throws RemoteServiceException
    {
        final String methodName = "redeemLoyaltyPoints";
        final LoyaltyOperationsInterface client = getClient(ctx);

        if (LogSupport.isDebugEnabled(ctx))
        {
            String msg = String.format(
                    "%s(ban=%s, cardId=%s, programId=%s, extTransactionId=%s, sourceType=%s, points=%d, userId=%s, userNote=%s, userLocation=%s)", 
                    methodName, ban, cardId, programId, extTransactionId, sourceType, points, userId, userNote, userLocation);
            new DebugLogMsg(this, msg, null).log(ctx);
        }
        //Input
        ban = (ban == null ? "" : ban);
        cardId = (cardId == null ? "" : cardId);
       
        List<Parameter> inParams = new ArrayList<Parameter>();
        addParameter(inParams, ParameterID.IN_SUBSCRIBER_ID, ban);
        addParameter(inParams, ParameterID.IN_PROGRAM_ID, programId);
        addParameter(inParams, ParameterID.IN_EXTERNAL_TRANSACTION_ID, extTransactionId);
        addParameter(inParams, ParameterID.IN_OPERATION_TYPE, sourceType);
        addParameter(inParams, ParameterID.IN_USER_ID, userId);
        addParameter(inParams, ParameterID.IN_USER_NOTE, userNote);
        addParameter(inParams, ParameterID.IN_USER_LOCATION, userLocation);
        Parameter[] inParamSet = inParams.toArray(new Parameter[inParams.size()]);     

        //Output
        final ParameterSetHolder outParams = new ParameterSetHolder();
        final StringHolder transactionId = new StringHolder();
        final int resultCode;
        
        try
        {
            resultCode = client.redeemLoyaltyPoints(cardId, points, inParamSet, outParams, transactionId);
        }
        catch (Exception exception)
        {
            throw new RemoteServiceException(ErrorCode.INTERNAL_ERROR, FAILED_MESSAGE_PREFIX + methodName
                    + FAILED_MESSAGE_SUFFIX, exception);
        }

        if (resultCode != ErrorCode.SUCCESS)
        {
            final String msg = String.format(
                    "Failure in URCS method '%s' [rc:%d] (%s) when redeeming points for account=%s, cardId=%s, programId=%s",
                    methodName, resultCode, LoyaltyReturnCodeHelper.getOperationMessage(ctx, resultCode), ban, cardId, programId);
            new MinorLogMsg(this, msg, null).log(ctx);
            throw new RemoteServiceException(resultCode, msg);
        }
        
        LoyaltyParameters result = new LoyaltyParameters(outParams.value);
        result.setTransactionId(transactionId.value);
        
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, methodName + " response=" + result, null).log(ctx);
        }
        return result;
    }



    @Override
    public LoyaltyParameters adjustLoyaltyPoints(Context ctx, String ban, String cardId, String programId, 
            String extTransactionId, Integer sourceType, long points, String userId, String userNote, 
            String userLocation)
            throws RemoteServiceException
    {
        final String methodName = "adjustLoyaltyPoints";
        final LoyaltyOperationsInterface client = getClient(ctx);
        
        if (LogSupport.isDebugEnabled(ctx))
        {
            String msg = String.format(
                    "%s(ban=%s, cardId=%s, programId=%s, extTransactionId=%s, sourceType=%s, points=%d, userId=%s, userNote=%s, userLocation=%s)", 
                    methodName, ban, cardId, programId, extTransactionId, sourceType, points, userId, userNote, userLocation);
            new DebugLogMsg(this, msg, null).log(ctx);
        }
        //Input
        ban = (ban == null ? "" : ban);
        cardId = (cardId == null ? "" : cardId);
       
        List<Parameter> inParams = new ArrayList<Parameter>();
        addParameter(inParams, ParameterID.IN_SUBSCRIBER_ID, ban);
        addParameter(inParams, ParameterID.IN_PROGRAM_ID, programId);
        addParameter(inParams, ParameterID.IN_EXTERNAL_TRANSACTION_ID, extTransactionId);
        addParameter(inParams, ParameterID.IN_OPERATION_TYPE, sourceType);
        addParameter(inParams, ParameterID.IN_USER_ID, userId);
        addParameter(inParams, ParameterID.IN_USER_NOTE, userNote);
        addParameter(inParams, ParameterID.IN_USER_LOCATION, userLocation);
        Parameter[] inParamSet = inParams.toArray(new Parameter[inParams.size()]);     

        //Output
        final ParameterSetHolder outParams = new ParameterSetHolder();
        final StringHolder transactionId = new StringHolder();
        final int resultCode;    
        try
        {
            resultCode = client.adjustLoyaltyPoints(cardId, points, inParamSet, outParams, transactionId);
        }
        catch (Exception exception)
        {
            throw new RemoteServiceException(ErrorCode.INTERNAL_ERROR, FAILED_MESSAGE_PREFIX + methodName
                    + FAILED_MESSAGE_SUFFIX, exception);
        }

        if (resultCode != ErrorCode.SUCCESS)
        {
            final String msg = String.format(
                    "Failure in URCS method in '%s' [rc:%d] (%s) when adjusting points for account=%s, cardId=%s, programId=%s",
                    methodName, resultCode, LoyaltyReturnCodeHelper.getOperationMessage(ctx, resultCode), ban, cardId, programId);
            new MinorLogMsg(this, msg, null).log(ctx);
            throw new RemoteServiceException(resultCode, msg);
        }
        
        LoyaltyParameters result = new LoyaltyParameters(outParams.value);
        result.setTransactionId(transactionId.value);
        
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, methodName + " response=" + result, null).log(ctx);
        }
        return result;
    }


    @Override
    public LoyaltyParameters accumulateLoyaltyPoints(Context ctx, String ban, String cardId, String programId, 
            String extTransactionId, Integer sourceType, long points, String userId, String userNote, String userLocation)
            throws RemoteServiceException
    {
        final String methodName = "accumulateLoyaltyPoints";
        final LoyaltyOperationsInterface client = getClient(ctx);
        
        if (LogSupport.isDebugEnabled(ctx))
        {
            String msg = String.format(
                    "%s(ban=%s, cardId=%s, programId=%s, extTransactionId=%s, sourceType=%s, points=%d, userId=%s, userNote=%s, userLocation=%s)", 
                    methodName, ban, cardId, programId, extTransactionId, sourceType, points, userId, userNote, userLocation);
            new DebugLogMsg(this, msg, null).log(ctx);
        }
        //Input
        ban = (ban == null ? "" : ban);
        cardId = (cardId == null ? "" : cardId);
       
        List<Parameter> inParams = new ArrayList<Parameter>();
        addParameter(inParams, ParameterID.IN_SUBSCRIBER_ID, ban);
        addParameter(inParams, ParameterID.IN_PROGRAM_ID, programId);
        addParameter(inParams, ParameterID.IN_EXTERNAL_TRANSACTION_ID, extTransactionId);
        addParameter(inParams, ParameterID.IN_OPERATION_TYPE, sourceType);
        addParameter(inParams, ParameterID.IN_USER_ID, userId);
        addParameter(inParams, ParameterID.IN_USER_NOTE, userNote);
        addParameter(inParams, ParameterID.IN_USER_LOCATION, userLocation);
        Parameter[] inParamSet = inParams.toArray(new Parameter[inParams.size()]);     

        //Output
        final ParameterSetHolder outParams = new ParameterSetHolder();
        final StringHolder transactionId = new StringHolder();
        final int resultCode;

        try
        {
            
            resultCode = client.accumulateLoyaltyPoints(cardId, points, inParamSet, outParams, transactionId);
        }
        catch (Exception exception)
        {
            throw new RemoteServiceException(ErrorCode.INTERNAL_ERROR, FAILED_MESSAGE_PREFIX + methodName
                    + FAILED_MESSAGE_SUFFIX, exception);
        }

        if (resultCode != ErrorCode.SUCCESS)
        {
            final String msg = String.format(
                    "Failure in URCS method '%s' [rc:%d] (%s) when accumulating points for account=%s, cardId=%s, programId=%s",
                    methodName, resultCode, LoyaltyReturnCodeHelper.getOperationMessage(ctx, resultCode), ban, cardId, programId);
            new MinorLogMsg(this, msg, null).log(ctx);
            throw new RemoteServiceException(resultCode, msg);
        }
        
        LoyaltyParameters result = new LoyaltyParameters(outParams.value);
        result.setTransactionId(transactionId.value);
        
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, methodName + " response=" + result, null).log(ctx);
        }
        return result;
    }



    @Override
    public long queryLoyaltyPoints(Context ctx, String ban, String cardId, String programId) throws RemoteServiceException
    {
        final String methodName = "queryLoyaltyPoints";
        final LoyaltyOperationsInterface client = getClient(ctx);

        if (LogSupport.isDebugEnabled(ctx))
        {
            String msg = String.format("%s(ban=%s, cardId=%s, programId=%s)",
                    methodName, ban, cardId, programId);
            new DebugLogMsg(this, msg, null).log(ctx);
        }
        //Input
        ban = (ban == null ? "" : ban);
        cardId = (cardId == null ? "" : cardId);
        List<Parameter> inParams = new ArrayList<Parameter>();
        addParameter(inParams, ParameterID.IN_SUBSCRIBER_ID, ban);
        addParameter(inParams, ParameterID.IN_PROGRAM_ID, programId);
        Parameter[] inParamSet = inParams.toArray(new Parameter[inParams.size()]);     

        //Output
        final ParameterSetHolder outParams = new ParameterSetHolder();
        LongHolder points = new LongHolder();
        final int resultCode;

        try
        {
            resultCode = client.queryLoyaltyPoints(cardId, inParamSet, outParams, points);
        }
        catch (Exception exception)
        {
            throw new RemoteServiceException(ErrorCode.INTERNAL_ERROR, FAILED_MESSAGE_PREFIX + methodName
                    + FAILED_MESSAGE_SUFFIX, exception);
        }

        //Return 0 if referenced by BAN
        if (resultCode == ErrorCode.LOYALTY_PROFILE_NOT_FOUND && !ban.isEmpty())
        {
            final String msg = String.format(
                    "Failure in URCS method '%s' [rc:%d] (%s) when querying points balance for account=%s, cardId=%s, programId=%s. Returning points=0",
                    methodName, resultCode, LoyaltyReturnCodeHelper.getOperationMessage(ctx, resultCode), ban, cardId, programId);
            new InfoLogMsg(this, msg, null).log(ctx);
            return 0;
        }
        else if (resultCode != ErrorCode.SUCCESS)
        {
            final String msg = String.format(
                    "Failure in URCS method '%s' [rc:%d] (%s) when querying points balance for account=%s, cardId=%s, programId=%s",
                    methodName, resultCode, LoyaltyReturnCodeHelper.getOperationMessage(ctx, resultCode), ban, cardId, programId);
            new MinorLogMsg(this, msg, null).log(ctx);
            throw new RemoteServiceException(resultCode, msg);
        }
        
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, methodName + " response=" + points.value, null).log(ctx);
        }
        return points.value;
    }


    @Override
    public LoyaltyParameters convertLoyaltyPoints(Context ctx, String ban, String cardId, String programId,
            long redemptionType, String partnerId, int voucherType, Long points, Long amount)  throws RemoteServiceException
    {
        final String methodName = "convertLoyaltyPoints";
        final LoyaltyOperationsInterface client = getClient(ctx);
        
        if (LogSupport.isDebugEnabled(ctx))
        {
            String msg = String.format("%s(ban=%s, cardId=%s, programId=%s, redemptionType=%d, partnerId=%s, voucherType=%d, points=%s, amount=%s)",
                    methodName, ban, cardId, programId, redemptionType, partnerId, voucherType, points, amount);
            new DebugLogMsg(this, msg, null).log(ctx);
        }
        //Input
        ban = (ban == null ? "" : ban);
        cardId = (cardId == null ? "" : cardId);
        points = (points == null ? 0 : points);
        Amount inAmount = new Amount();
        inAmount.value = (amount == null ? 0 : amount);
        RedemptionType type = RedemptionType.from_int((int)redemptionType);
        List<Parameter> inParams = new ArrayList<Parameter>();
        addParameter(inParams, ParameterID.IN_SUBSCRIBER_ID, ban);
        addParameter(inParams, ParameterID.IN_PROGRAM_ID, programId);
        Parameter[] inParamSet = inParams.toArray(new Parameter[inParams.size()]);     
        
        //Output
        final ParameterSetHolder outParams = new ParameterSetHolder();
        LongHolder calculatedPoints = new LongHolder();
        AmountHolder calcAmount = new AmountHolder();
        final int resultCode;

        
        try
        {
            
            resultCode = client.convertLoyaltyPoints(cardId, type, partnerId, voucherType, points, inAmount, inParamSet, 
                    outParams, calculatedPoints, calcAmount);
        }
        catch (Exception exception)
        {
            throw new RemoteServiceException(ErrorCode.INTERNAL_ERROR, FAILED_MESSAGE_PREFIX + methodName
                    + FAILED_MESSAGE_SUFFIX, exception);
        }

        if (resultCode != ErrorCode.SUCCESS)
        {
            final String msg = String.format(
                    "Failure in URCS method '%s' [rc:%d] (%s) when converting points for account=%s, cardId=%s, programId=%s",
                    methodName, resultCode, LoyaltyReturnCodeHelper.getOperationMessage(ctx, resultCode), ban, cardId, programId);
            new MinorLogMsg(this, msg, null).log(ctx);
            throw new RemoteServiceException(resultCode, msg);
        }
        
        LoyaltyParameters result = new LoyaltyParameters(outParams.value);
        result.setCalculatedPoints(calculatedPoints.value);
        result.setCalculatedAmount(calcAmount.value.value);
        
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, methodName + " response=" + result, null).log(ctx);
        }
        return result;
    }
    

    private void addParameter(List<Parameter> inParams, short paramId, String value)
    {
        if (value != null && !value.isEmpty())
        {
            Parameter param = new Parameter();
            param.parameterID = paramId;
            param.value = new ParameterValue();
            param.value.stringValue(value);
            inParams.add(param);
        }
    }


    private void addParameter(List<Parameter> inParams, short paramId, Long value)
    {
        if (value != null)
        {
            Parameter param = new Parameter();
            param.parameterID = paramId;
            param.value = new ParameterValue();
            param.value.longValue(value);
            inParams.add(param);
        }
    }

    private void addParameter(List<Parameter> inParams, short paramId, Integer value)
    {
        if (value != null)
        {
            Parameter param = new Parameter();
            param.parameterID = paramId;
            param.value = new ParameterValue();
            param.value.intValue(value);
            inParams.add(param);
        }
    }

}
