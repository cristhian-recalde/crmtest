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
package com.trilogy.app.crm.api.rmi.support;

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.api.rmi.GenericParameterParser;
import com.trilogy.app.crm.bean.AdjustmentTypeXInfo;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.bean.TransactionHome;
import com.trilogy.app.crm.bean.TransactionXInfo;
import com.trilogy.app.crm.bean.calldetail.CallDetailXInfo;
import com.trilogy.app.crm.bean.paymentgatewayintegration.PaymentGatewayException;
import com.trilogy.app.crm.elang.PagingXStatement;
import com.trilogy.app.crm.paymentgatewayintegration.PaymentGatewaySupport;
import com.trilogy.app.crm.paymentgatewayintegration.PaymentGatewaySupportHelper;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.VRASupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.GTE;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.elang.LTE;
import com.trilogy.framework.xhome.elang.OrderBy;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v2_1.types.transaction.ProfileType;
import com.trilogy.util.crmapi.wsdl.v2_1.types.transaction.ProfileTypeEnum;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;
import com.trilogy.product.s2100.oasis.param.Parameter;

/**
 * Provides utility functions for use with Transaction
 *
 * @author kumaran.sivasubramaniam@redknee.com
 */
public class TransactionsApiSupport
{
    public static Collection<Transaction> getTransactionsUsingGivenParameters(final Context ctx,
            final Object parentCondition, final Calendar start, final Calendar end, final Long category,
            final Long pageKey, final int limit, final Boolean isAscending, ProfileType profileType, Long accountLevelID, Object caller) throws Exception
    {
        final boolean ascending = RmiApiSupport.isSortAscending(isAscending);

        final And condition = new And();
        condition.add(parentCondition);
        if (category != null)
        {
            final com.redknee.app.crm.bean.core.AdjustmentType adjustmentType = getCrmAdjustmentType(ctx, category, caller);
            // TODO improve by filtering on SPID
            final Set<Integer> set = adjustmentType.getSelfAndDescendantCodes(ctx);
            
            if ( set.size() > 999)
            {
                int i = 0;
                Set codeSet = new HashSet<Integer>();

                for(Integer code : set)
                {
                    codeSet.add(code);
                    if ( i == 998)
                    {
                        condition.add(new In(TransactionXInfo.ADJUSTMENT_TYPE, codeSet));                        
                        i = 0;
                        codeSet = new HashSet<Integer>();
                    }
                    else
                    {
                        i++;
                    }                    
                }
                
            }
            else
            {
                condition.add(new In(TransactionXInfo.ADJUSTMENT_TYPE, set));
            }
        }

        if (start != null)
        {
            condition.add(new GTE(TransactionXInfo.TRANS_DATE, CalendarSupportHelper.get(ctx).calendarToDate(start)));
        }
        if (end != null)
        {
            condition.add(new LTE(TransactionXInfo.TRANS_DATE, CalendarSupportHelper.get(ctx).calendarToDate(end)));
        }
        if (pageKey != null)
        {
            condition.add(new PagingXStatement(TransactionXInfo.RECEIPT_NUM, pageKey, ascending));
        }
        
        //commenting changes done for UMP-5292 to support Tybatel specific requirement as reported in bug UMP-6767
		/*if (profileType != null && ProfileTypeEnum.SUBSCRIPTION.getValue().getValue() == profileType.getValue()) {
			condition.add(new EQ(TransactionXInfo.ACCOUNT_RECEIPT_NUM, 0));
		}*/

        // Overriding transaction home in the context with the account transaction home.
        Context subCtx = ctx;
        if (profileType!=null && ProfileTypeEnum.ACCOUNT.getValue().getValue() == profileType.getValue())
        {
            subCtx = ctx.createSubContext();
            subCtx.put(TransactionHome.class, ctx.get(Common.ACCOUNT_TRANSACTION_HOME));
        }
        else if (accountLevelID != null)
        {
            condition.add(new EQ(TransactionXInfo.ACCOUNT_RECEIPT_NUM, accountLevelID));
        }

        final Collection<Transaction> collection = HomeSupportHelper.get(subCtx).getBeans(
                subCtx,
                Transaction.class,
                condition,
                limit,
                ascending,
                TransactionXInfo.RECEIPT_NUM);
        
        return collection;
    }
    
    /**
     * Header must be authenticated. This method does not do authentication.
     * Does not return null. If account not found Exception is thrown.
     *
     * @param ctx the operating context
     * @param code adjustment type code
     * @return adjustment type object
     * @throws RemoteException if CRM call returns exception
     */
    public static com.redknee.app.crm.bean.core.AdjustmentType getCrmAdjustmentType(final Context ctx, final Long code, Object caller)
        throws CRMExceptionFault
    {
        com.redknee.app.crm.bean.core.AdjustmentType adjustmentType = null;
        try
        {
            final Object condition = new EQ(AdjustmentTypeXInfo.CODE, code.intValue());
            adjustmentType = HomeSupportHelper.get(ctx).findBean(ctx, com.redknee.app.crm.bean.core.AdjustmentType.class, condition);
        }
        catch (Exception e)
        {
            final String msg = "Unable to retrieve Adjustment Type " + code;
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, caller);
        }

        if (adjustmentType == null)
        {
            final String msg = "Adjustment Type " + code;
            RmiApiErrorHandlingSupport.identificationException(ctx, msg, caller);
        }

        return adjustmentType;
    }
    
    /**
     * If the parameters listed are given, then returns true.
     *  
     * @param ctx
     * @param parameters        {("OriginatingApplication", "VRANormal"),("Clawback", Long)}
     * @return
     */
    public static boolean requiresVRAClawbackTransaction(final Context ctx, final Map<String, Object> parameters)
    {
        final String origin = (String)parameters.get(APIGenericParameterSupport.ORIGINATING_APPLICATION);
        final Integer vraEventType = (Integer)parameters.get(APIGenericParameterSupport.VRA_EVENT_TYPE);
        final Long clawback = (Long)parameters.get(APIGenericParameterSupport.VRA_CLAWBACK);
        
        return APIGenericParameterSupport.VRA.equals(origin) && 
                vraEventType != null && vraEventType == VRASupport.VRA_ER_EVENTTYPE_NORMAL &&
                clawback != null && clawback > 0;
    }

    /**
     * Creates a VRA clawback transaction from given request parameters.
     * 
     * @param ctx
     * @param subscriber
     * @param transaction
     * @param parameters        {("Clawback", Long), ("ResultingBalance", Long)}
     * @throws HomeException
     */
    public static void createVRAClawbackTransaction(final Context ctx, final Subscriber subscriber, 
            final Transaction transaction, Map<String, Object> parameters) 
        throws HomeException
    {
        Long clawback = (Long)parameters.get(APIGenericParameterSupport.VRA_CLAWBACK);
        Long balance = (Long)parameters.get(APIGenericParameterSupport.RESULTING_BALANCE);
        
        if (clawback != null && clawback > 0)
        {
            final long creditValue = clawback.longValue();
            balance = balance==null? 0 : balance;
            
            VRASupport.createClawbackTransaction(ctx, transaction.getTransDate(), clawback, balance, 
                    creditValue, transaction.getExpiryDaysExt(), subscriber, transaction.getCSRInput());   
        }
    }
    
    
    public static void handlePaymentCharges(Context ctx, Subscriber sub, GenericParameter[] parameters, Object caller) throws CRMExceptionFault
    {
        handlePaymentCharges(ctx,sub,new GenericParameterParser(parameters),caller);
    }
    public static void handlePaymentCharges(Context ctx, Subscriber sub, GenericParameterParser parameters, Object caller) throws CRMExceptionFault
    {
        String tokenValue = parameters.getParameter("CreditCardTokenValue", String.class);
        if (tokenValue != null && !tokenValue.isEmpty())
        {
            String mask = parameters.getParameter("CreditCardMask", String.class);
            try
            {
                long paymentChargeAmount = parameters.getParameter("PaymentChargeAmount", Long.class).longValue();
                long paymentChargeTaxAmount = parameters.getParameter("PaymentChargeTaxAmount", Long.class).longValue();
                createPaymentGatewayTransaction(ctx, caller, sub, tokenValue, mask, paymentChargeAmount,
                        paymentChargeTaxAmount);
            }
            catch (Exception e)
            {
                RmiApiErrorHandlingSupport.ocgTransactionException(ctx, e, -1,
                        "Error making charge payment request to Payment Gateway.", caller);
            }
        }
    }
    
    public static void createPaymentGatewayTransaction(final Context ctx, Object caller, final Subscriber sub,
            String creditCardTokenValue, String creditCardmask, long paymentChargeAmount, long paymentChargeTaxAmount)
            throws CRMExceptionFault
    {
        int result;
        Map<Short, Parameter> outParams;
        try
        {
            outParams = new HashMap<Short, Parameter>();
            
            result = PaymentGatewaySupportHelper.get(ctx).chargePaymentGateway(ctx, (paymentChargeAmount + paymentChargeTaxAmount),
                    paymentChargeTaxAmount, sub, false, creditCardmask, creditCardTokenValue, outParams);
            if (result != PaymentGatewaySupport.DEFAULT_SUCCESS)
            {
                RmiApiErrorHandlingSupport.ocgTransactionException(ctx, null, result,
                        "Payment gateway returned Erorr [" + result + "]", caller);
            }
        }
        catch (PaymentGatewayException e)
        {
            RmiApiErrorHandlingSupport.ocgTransactionException(ctx, e, -1, "Could not call payment Gateway. Error ["
                    + e.getMessage() + "]", caller);
        }
    }
    
    public static Collection<Transaction> getTransactionsUsingGivenParametersOrderByDateTime(final Context ctx,
            final Subscriber subscriber,final Object parentCondition, final Calendar start, final Calendar end, 
            final Long pageKey, final int limit, final Boolean isAscending, ProfileType profileType, Long accountLevelID, Object caller) throws Exception
    {
        final boolean ascending = RmiApiSupport.isSortAscending(isAscending);

        final And condition = new And();
        if(parentCondition != null)
        {
        	condition.add(parentCondition);
        }
        condition.add(new EQ(TransactionXInfo.SUBSCRIBER_ID, subscriber.getId()));

        if (start != null)
        {
            condition.add(new GTE(TransactionXInfo.TRANS_DATE, CalendarSupportHelper.get(ctx).calendarToDate(start)));
        }
        if (end != null)
        {
            condition.add(new LTE(TransactionXInfo.TRANS_DATE, CalendarSupportHelper.get(ctx).calendarToDate(end)));
        }
        if (pageKey != null)
        {
            condition.add(new PagingXStatement(TransactionXInfo.RECEIPT_NUM, pageKey, ascending));
        }
        // Overriding transaction home in the context with the account transaction home.
        Context subCtx = ctx;
        if (profileType!=null && ProfileTypeEnum.ACCOUNT.getValue().getValue() == profileType.getValue())
        {
            subCtx = ctx.createSubContext();
            subCtx.put(TransactionHome.class, ctx.get(Common.ACCOUNT_TRANSACTION_HOME));
        }
        else if (accountLevelID != null)
        {
            condition.add(new EQ(TransactionXInfo.ACCOUNT_RECEIPT_NUM, accountLevelID));
        }

        final Collection<Transaction> collection = HomeSupportHelper.get(subCtx).getBeans(
                subCtx,
                Transaction.class,
                condition,
                limit,
                ascending,
                TransactionXInfo.TRANS_DATE);
        
        return collection;
    }
    
    
}