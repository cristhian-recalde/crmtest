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
package com.trilogy.app.crm.api.queryexecutor.account;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.account.state.AccountStateTransitionSupport;
import com.trilogy.app.crm.api.ApiSupport;
import com.trilogy.app.crm.api.Constants;
import com.trilogy.app.crm.api.queryexecutor.AbstractQueryExecutor;
import com.trilogy.app.crm.api.queryexecutor.ExecuteResultQueryExecutor;
import com.trilogy.app.crm.api.rmi.GenericParameterParser;
import com.trilogy.app.crm.api.rmi.impl.AccountsImpl;
import com.trilogy.app.crm.api.rmi.support.APIGenericParameterSupport;
import com.trilogy.app.crm.api.rmi.support.AccountsApiSupport;
import com.trilogy.app.crm.api.rmi.support.RmiApiErrorHandlingSupport;
import com.trilogy.app.crm.api.rmi.support.RmiApiSupport;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.AccountUsage;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.Address;
import com.trilogy.app.crm.bean.AddressTypeEnum;
import com.trilogy.app.crm.bean.AddressXInfo;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.ClosedSub;
import com.trilogy.app.crm.bean.ClosedUserGroup;
import com.trilogy.app.crm.bean.ClosedUserGroupTemplate;
import com.trilogy.app.crm.bean.GroupTypeEnum;
import com.trilogy.app.crm.bean.Invoice;
import com.trilogy.app.crm.bean.QuotaTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.TransactionHome;
import com.trilogy.app.crm.bean.TransactionXInfo;
import com.trilogy.app.crm.bean.account.AccountIdentification;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.calculation.support.InvoiceSupport;
import com.trilogy.app.crm.extension.ExtensionHolder;
import com.trilogy.app.crm.extension.account.AccountExtension;
import com.trilogy.app.crm.extension.account.AccountExtensionHolder;
import com.trilogy.app.crm.extension.account.PoolExtension;
import com.trilogy.app.crm.extension.account.PoolExtensionXInfo;
import com.trilogy.app.crm.extension.account.SubscriptionPoolProperty;
import com.trilogy.app.crm.invoice.bean.AccountIdentificationXInfo;
import com.trilogy.app.crm.move.MoveConstants;
import com.trilogy.app.crm.move.CompoundMoveIllegalSateException;
import com.trilogy.app.crm.move.MoveManager;
import com.trilogy.app.crm.move.MoveRequest;
import com.trilogy.app.crm.move.processor.AccountMoveException;
import com.trilogy.app.crm.move.processor.AcountMoveProducerAgent;
import com.trilogy.app.crm.support.AccountOverPaymentHistorySupport;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.ClosedUserGroupSupport;
import com.trilogy.app.crm.support.CreditCategorySupport;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.app.crm.support.HomeSupport;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.move.request.AccountMoveRequest;
import com.trilogy.app.crm.move.request.ConvertAccountBillingTypeRequest;
import com.trilogy.app.crm.move.request.ConvertAccountBillingTypeRequestXInfo;
import com.trilogy.app.crm.move.request.ConvertAccountGroupTypeRequest;

import com.trilogy.app.crm.move.request.PostpaidServiceBasedSubscriberAccountMoveRequest;
import com.trilogy.app.crm.move.request.PrepaidPooledSubscriberAccountMoveRequest;
import com.trilogy.app.crm.move.support.MoveRequestSupport;
import com.trilogy.framework.xhome.auth.bean.User;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.elang.Limit;
import com.trilogy.framework.xhome.elang.OrderBy;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.OrderByHome;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v2_0.types.SuccessCode;
import com.trilogy.util.crmapi.wsdl.v2_0.types.SuccessCodeEnum;
import com.trilogy.util.crmapi.wsdl.v2_1.exception.ExceptionCode;
import com.trilogy.util.crmapi.wsdl.v2_1.types.account.AccountContactInfo;
import com.trilogy.util.crmapi.wsdl.v2_1.types.account.AccountState;
import com.trilogy.util.crmapi.wsdl.v2_1.types.subscription.SubscriptionStateEnum;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;
import com.trilogy.util.crmapi.wsdl.v3_0.types.CRMRequestHeader;
import com.trilogy.util.crmapi.wsdl.v3_0.types.ExecuteResult;
import com.trilogy.util.crmapi.wsdl.v3_0.types.GenericResponse;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.AccountCugQueryResults;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.AccountForIDResult;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.AccountForIDResults;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.AccountProfileQueryResultsV2;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.AccountProfileWithServiceAddressQueryResults;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.AccountStateTransitionException;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.AccountStateTransitionResult;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.ConvertAccountReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.ConvertBillingTypeRequest;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.ConvertBillingTypeResponse;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.CugDetails;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.CugMember;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.CurrentAccountReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.GroupHierarchyType;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.ServiceAddressOutput;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.extensions.PoolAccountExtension;
import com.trilogy.util.crmapi.wsdl.v3_0.types.generalprovisioning.IdentificationEntry;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.ConvertSubscriptionDetail;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionReference;

import com.trilogy.app.crm.move.request.AbstractAccountExtensionMoveRequest;
import com.trilogy.app.crm.extension.account.*;
import com.trilogy.app.crm.bean.AccountReasonCodeMapping;
import com.trilogy.app.crm.bean.AccountReasonCodeMappingHome;
import com.trilogy.app.crm.bean.AccountReasonCodeMappingXInfo;
import com.trilogy.app.crm.bean.AccountStateChangeReason;
import com.trilogy.app.crm.bean.AccountStateChangeReasonHome;
import com.trilogy.app.crm.bean.AccountStateChangeReasonXInfo;
import com.trilogy.framework.xlog.log.DebugLogMsg;
/**
 * 
 * @author Marcio Marques
 * @since 9.2
 *
 */
public class AccountQueryExecutors 
{
	/**
	 * 
	 * @author Marcio Marques
	 * @since 9.1.3
	 *
	 */
	public static abstract class AbstractAccountExecuteSetQueryExecutor extends ExecuteResultQueryExecutor
	{

	    protected Account getAccount(Context ctx, String accountID) throws CRMExceptionFault
	    {
	        final Account account = AccountsImpl.getCrmAccount(ctx, accountID, this);
	        return account;
	    }
	    
	    protected String getAccountID(Context ctx, int paramAccountID, String paramAccountIDName, Object... parameters) throws CRMExceptionFault
	    {
	    	return getParameter(ctx, paramAccountID, paramAccountIDName, String.class, parameters);
	    }
	}
	
	/**
	 * 
	 * @author Marcio Marques
	 * @since 9.1.3
	 *
	 */
	public static abstract class AbstractAccountQueryExecutor<T> extends AbstractQueryExecutor<T>
	{

	    protected Account getAccount(Context ctx, String accountID) throws CRMExceptionFault
	    {
	        final Account account = AccountsImpl.getCrmAccount(ctx, accountID, this);
	        return account;
	    }
	    
	    /**
	     * Returns the account home.
	     *
	     * @param ctx
	     *            The operating context.
	     * @return The requested home.
	     * @throws CRMExceptionFault
	     *             Thrown if there are problems looking up the home.
	     */
	    protected Home getAccountHome(final Context ctx) throws CRMExceptionFault
	    {
	        return RmiApiSupport.getCrmHome(ctx, AccountHome.class, AccountsImpl.class);
	    }
	    
	    protected String getAccountID(Context ctx, int paramAccountID, String paramAccountIDName, Object... parameters) throws CRMExceptionFault
	    {
	    	return getParameter(ctx, paramAccountID, paramAccountIDName, String.class, parameters);
	    }
	    
	}
	
	/**
	 * 
	 * @author Marcio Marques
	 * @since 9.1.3
	 *
	 */
	public static class AccountBalanceQueryExecutor extends AbstractAccountExecuteSetQueryExecutor
	{

		public AccountBalanceQueryExecutor()
	    {
	    }
		
	    public ExecuteResult execute(Context ctx, Object... parameters) throws CRMExceptionFault
	    {
	        String accountID = getAccountID(ctx, PARAM_ACCOUNT_ID, PARAM_ACCOUNT_ID_NAME, parameters);
	        String[] balanceTypesArray = getParameter(ctx, PARAM_BALANCE_TYPES, PARAM_BALANCE_TYPES_NAME, String[].class, parameters);
	        GenericParameter[] genericParameters = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);
	     
	        
	        int adjustmentType = AdjustmentTypeEnum.StandardPayments.getIndex();
	        
	        if (genericParameters!=null)
	        {
	            GenericParameterParser parser = new GenericParameterParser(genericParameters);
	            adjustmentType = parser.getParameter(PARAM_ADJUSTMENT_TYPE_NAME, Integer.class);
	        }
	        else
	        {
	            if(LogSupport.isDebugEnabled(ctx))
	            {
	                LogSupport.debug(ctx, this, "Adjustment Type not provided, Hence using Stanard Payemnts adjustment type. AdjustmentType : "+ adjustmentType);
	            }
	        }
	        
	        RmiApiErrorHandlingSupport.validateMandatoryObject(accountID, PARAM_ACCOUNT_ID_NAME);
	        RmiApiErrorHandlingSupport.validateMandatoryObject(balanceTypesArray, PARAM_BALANCE_TYPES_NAME);

	        Account account = getAccount(ctx, accountID);

	        Set<String> balanceTypes = extractBalanceTypes(balanceTypesArray);

	        AccountUsage balanceUsage = null;
	        Invoice invoice = null;
	        boolean isPooledAccount = account.isPooled(ctx);
	        
	        ExecuteResult result = new ExecuteResult();
	        
	        for (String balanceType : BALANCE_TYPES)
	        {
	            if (isBalanceTypeRequired(balanceTypes, balanceType))
	            {
	                if (balanceUsage == null && isBalanceUsageRequired(balanceType))
	                {
	                    balanceUsage = account.getAccountUsage(ctx);
	                }
	                
	                if (invoice == null && (account.isPostpaid() || account.isHybrid()) && isLastInvoiceRequired(balanceType))
	                {
	                    invoice = InvoiceSupport.getMostRecentInvoice(ctx, account.getBAN());
	                }
	                
	                Object value = getBalanceTypeValue(ctx, balanceType, account, balanceUsage, invoice, isPooledAccount,adjustmentType);
	                addParameterToResult(result, balanceType, value);
	            }
	        }
	        
	        return result;
	    }
	    
	    private Object getBalanceTypeValue(Context ctx, String balanceType, Account account, 
	            AccountUsage balanceUsage, Invoice invoice, boolean isPooledAccount, int adjustmentType)
	                    throws CRMExceptionFault
	    {
	    	/* TT#14012445037 Added support for Hybrid accounts for below 7 parameters */
	        if (BALANCE.equals(balanceType))
	        {
	            if (account.isPostpaid()||account.isHybrid())
	            {
	                return balanceUsage.getBalance();
	            }
	        }
	        else if (CURRENCY.equals(balanceType))
	        {
	            return account.getCurrency();
	        }
	        else if (LAST_INVOICE_AMOUNT.equals(balanceType))
	        {
	            if (account.isPostpaid()||account.isHybrid())
	            {
	                return balanceUsage.getAmountDue();
	            }
	        }
	        else if (PAYMENTS_SINCE_LAST_INVOICE.equals(balanceType))
	        {
	            if (account.isPostpaid()||account.isHybrid())
	            {
	                return balanceUsage.getPayment();
	            }
	        }
	        else if (ADJUSTMENTS_SINCE_LAST_INVOICE.equals(balanceType))
	        {
	            if (account.isPostpaid()||account.isHybrid())
	            {
	                return balanceUsage.getOtherAdjustments();
	            }
	        }
	        else if (LAST_INVOICE_IDENTIFIER.equals(balanceType))
	        {
	            if (account.isPostpaid()||account.isHybrid())
	            {
	                if (invoice!=null)
	                {
	                    return invoice.getInvoiceId();
	                }
	            }
	        }
	        else if (LAST_INVOICE_DATE.equals(balanceType))
	        {
	            if (account.isPostpaid()||account.isHybrid())
	            {
	                if (invoice!=null)
	                {
	                    return invoice.getInvoiceDate();
	                }
	            }
	        }
	        else if (LAST_INVOICE_DUE_DATE.equals(balanceType))
	        {
	            if (account.isPostpaid()||account.isHybrid())
	            {
	                if (invoice!=null)
	                {
	                    return invoice.getDueDate();
	                }
	            }
	        }
	        else if(LAST_TRANSACTION_AMT.equals(balanceType))
	        {
	            return calculateLastAccountPayment(ctx, account, adjustmentType);
	        }
	        else if(LAST_TRANSACTION_DATE.equals(balanceType))
            {
                return getLastAccountPaymentTransactionDate(ctx, account, adjustmentType);
            }
	        else if(OVER_PAYMENT_BALANCE.equals(balanceType))
	        {
	        	try
	        	{	  
	        		return AccountOverPaymentHistorySupport.getOverPaymentBalance(ctx, account.getBAN());	
	        	}
	        	catch(Exception e)
	        	{
	        		if(LogSupport.isDebugEnabled(ctx))
	        		{
	        			LogSupport.debug(ctx, AbstractAccountQueryExecutor.class, e.getMessage());
	        		}    
	        	}

	        }

	        return null;
	    }
	    private Object getLastAccountPaymentTransactionDate(Context ctx, final Account account, int adjustmentType) throws CRMExceptionFault
	    {
	        Date transactionDate = null;
	        
	        try 
            {
	            final Home transactionHome = (Home)ctx.get(TransactionHome.class);
                And filter = new And();
                
                if(account.getGroupType() == GroupTypeEnum.SUBSCRIBER)
                {
                    filter.add(new EQ(TransactionXInfo.BAN, account.getBAN()));
                }
                else
                {
                    filter.add(new EQ(TransactionXInfo.RESPONSIBLE_BAN, account.getBAN()));
                }
                filter.add(new EQ(TransactionXInfo.ADJUSTMENT_TYPE, adjustmentType));
                filter.add(new Limit(1));
                
                Home orderHome = new OrderByHome(ctx, new OrderBy(TransactionXInfo.TRANS_DATE,false), transactionHome); 
                Collection<Transaction> coll = orderHome.select(ctx, filter);
                
                Iterator<Transaction> iterator = coll.iterator();
                Transaction transaction = null;
                if(iterator.hasNext())
                {
                    transaction = iterator.next();
                }
                if(transaction != null )
                {
                    transactionDate = transaction.getTransDate();
                }
                else
                {
                    RmiApiErrorHandlingSupport.generalException(ctx, null,"No data found for account, verify the input data, BAN:" + account.getBAN() + " and AdjustmentType:"+ adjustmentType, ExceptionCode.INVALID_IDENTIFICATION, this);
                }
            }
	        catch(HomeException e)
            {
                throw new CRMExceptionFault("Failed to get Last Transaction Date for account:" + account.getBAN() + " and AdjustmentType : "+ adjustmentType, e);
            }
            if(LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, this, "Return Last Transaction Dat for BAN : "+ account.getBAN() + " AdjustmentType : "+ adjustmentType + " and Date : "+ transactionDate);
            }
            return transactionDate != null ? transactionDate : "";
	    }
	    private long calculateLastAccountPayment(Context ctx, final Account account, int adjustmentType) throws CRMExceptionFault
	    {
	        long totalAmount = 0L;
	        try 
	        {
	            final Home transactionHome = (Home)ctx.get(TransactionHome.class);
                And filter = new And();
                And and = new And();
                And filterLimit = new And();
                
                if(account.getGroupType() == GroupTypeEnum.SUBSCRIBER)
                {
                    filter.add(new EQ(TransactionXInfo.BAN, account.getBAN()));
                }
                else
                {
                    filter.add(new EQ(TransactionXInfo.RESPONSIBLE_BAN, account.getBAN()));
                }
                filter.add(new EQ(TransactionXInfo.ADJUSTMENT_TYPE, adjustmentType));
                filterLimit.add(new Limit(1));
                and.add(filter);
                and.add(filterLimit);
                
                Home orderHome = new OrderByHome(ctx, new OrderBy(TransactionXInfo.TRANS_DATE,false), transactionHome); 
                Collection<Transaction> coll = orderHome.select(ctx, and);
                
                Iterator<Transaction> iterator = coll.iterator();
                Transaction transaction = null;
                if(iterator.hasNext())
                {
                    transaction = iterator.next();
                }
                if(transaction != null )
                {
                    if(account.getGroupType() == GroupTypeEnum.SUBSCRIBER)
                    {
                        totalAmount = transaction.getAmount();
                    }
                    else
                    {
                        filter.add(new EQ(TransactionXInfo.ACCOUNT_RECEIPT_NUM, transaction.getAccountReceiptNum()));
                        Collection<Transaction> tranColl = transactionHome.select(ctx, filter);
                        for(Transaction trans : tranColl)
                        {
                            totalAmount += trans.getAmount();
                        }
                    }
                }
                else
                {
                    RmiApiErrorHandlingSupport.generalException(ctx, null,"No data found for account, verify the input data, BAN:" + account.getBAN() + " and AdjustmentType:"+ adjustmentType, ExceptionCode.INVALID_IDENTIFICATION, this);
                }
	        }
	        catch(HomeException e)
	        {
	            throw new CRMExceptionFault("Failed to calculate total amount for account:" + account.getBAN() + " and AdjustmentType : "+ adjustmentType, e);
	        }
	        if(LogSupport.isDebugEnabled(ctx))
	        {
	            LogSupport.debug(ctx, this, "Return calculated amount for BAN : "+ account.getBAN() + " AdjustmentType : "+ adjustmentType + " and Total Amount : "+ totalAmount);
	        }
	        return totalAmount;
	    }
	    
	    private boolean isBalanceUsageRequired(String balanceType)
	    {
	        return BALANCE_USAGE_REQUIRED_BALANCE_TYPES.contains(balanceType);
	               
	    }
	    
	    private boolean isLastInvoiceRequired(String balanceType)
	    {
	        return LAST_INVOICE_REQUIRED_BALANCE_TYPES.contains(balanceType);
	               
	    }
	    private Set<String> extractBalanceTypes(String[] balanceTypes)
	    {
	        Set<String> result = new HashSet<String>();
	        for (String balanceType : balanceTypes)
	        {
	            result.add(balanceType.toUpperCase());
	        }
	        return result;
	    }
	    
	    private boolean isBalanceTypeRequired(Set<String> balanceTypes, String balanceType)
	    {
	        boolean result = false;
	        
	        if (balanceTypes.contains(balanceType.toUpperCase()))
	        {
	            result = true;
	        }
	        
	        return result;
	    }

	    @Override
	    public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
	    {
	        Object[] result = null;
	        if (isGenericExecution(ctx, parameters))
	        {
	            result = new Object[4];
	            result[0] = parameters[0];
	            result[1] = getAccountID(ctx, PARAM_ACCOUNT_ID, PARAM_ACCOUNT_ID_NAME, parameters);
	            result[2] = getParameter(ctx, PARAM_BALANCE_TYPES, PARAM_BALANCE_TYPES_NAME, String[].class, parameters);
	            result[3] = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);
	        }
	        else
	        {
	            result = parameters;
	        }
	        
	        return result;
	    }

	    @Override
	    public boolean validateParameterTypes(Class<?>[] parameterTypes)
	    {
	        boolean result = true;
	        result = result && (parameterTypes.length>=4);
	        result = result && CRMRequestHeader.class.isAssignableFrom(parameterTypes[PARAM_HEADER]);
	        result = result && String.class.isAssignableFrom(parameterTypes[PARAM_ACCOUNT_ID]);
	        result = result && String[].class.isAssignableFrom(parameterTypes[PARAM_BALANCE_TYPES]);
	        result = result && Object[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
	        return result;
	    }

        @Override
        public boolean validateReturnType(Class<?> resultType)
        {
            return ExecuteResult.class.isAssignableFrom(resultType);
            
        }
        
	    private static final String BALANCE = "BALANCE";
	    private static final String CURRENCY = "CURRENCY";
	    private static final String POOL_QUOTA = "POOL_QUOTA";
	    private static final String POOL_USAGE = "POOL_USAGE";
	    private static final String LAST_INVOICE_AMOUNT = "LAST_INVOICE_AMOUNT";
	    private static final String LAST_INVOICE_DATE = "LAST_INVOICE_DATE";
	    private static final String LAST_INVOICE_DUE_DATE = "LAST_INVOICE_DUE_DATE";
	    private static final String LAST_INVOICE_IDENTIFIER = "LAST_INVOICE_IDENTIFIER";
	    private static final String PAYMENTS_SINCE_LAST_INVOICE = "PAYMENTS_SINCE_LAST_INVOICE";
	    private static final String ADJUSTMENTS_SINCE_LAST_INVOICE = "ADJUSTMENTS_SINCE_LAST_INVOICE";
	    private static final String WRITTEN_OFF_BALANCE = "WRITTEN_OFF_BALANCE";
	    private static final String LAST_TRANSACTION_AMT = "LAST_TRANSACTION_AMT";
	    private static final String OVER_PAYMENT_BALANCE = "OVER_PAYMENT_BALANCE";
	    private static final String LAST_TRANSACTION_DATE = "LAST_TRANSACTION_DATE";
	    
	    private final String[] BALANCE_TYPES = new String[]
	    {
	        BALANCE, 
	        CURRENCY,
	        LAST_INVOICE_AMOUNT,
	        PAYMENTS_SINCE_LAST_INVOICE,
	        ADJUSTMENTS_SINCE_LAST_INVOICE,
	        LAST_INVOICE_DATE,
	        LAST_INVOICE_DUE_DATE,
	        LAST_INVOICE_IDENTIFIER,
	        POOL_QUOTA, // Not Implemented
	        POOL_USAGE, // Not Implemented
	        WRITTEN_OFF_BALANCE, // Not Implemented
	        LAST_TRANSACTION_AMT,
	        LAST_TRANSACTION_DATE,
	        OVER_PAYMENT_BALANCE
	        };

	    private Set<String> BALANCE_USAGE_REQUIRED_BALANCE_TYPES = new HashSet<String>(Arrays.asList(new String[] {
	            BALANCE, 
	            LAST_INVOICE_AMOUNT,
	            PAYMENTS_SINCE_LAST_INVOICE,
	            ADJUSTMENTS_SINCE_LAST_INVOICE,
	            OVER_PAYMENT_BALANCE
	            }));
	        
	    private Set<String> LAST_INVOICE_REQUIRED_BALANCE_TYPES = new HashSet<String>(Arrays.asList(new String[] {
	            LAST_INVOICE_DATE,
	            LAST_INVOICE_DUE_DATE,
	            LAST_INVOICE_IDENTIFIER
	            }));

	    public static final String METHOD_SIMPLE_NAME = "executeAccountBalanceQuery";
	    public static final String METHOD_NAME = "AccountService." + METHOD_SIMPLE_NAME;

	    public static final String PERMISSION = Constants.PERMISSION_ACCOUNTS_READ_EXECUTEACCOUNTBALANCEQUERY;
	    
	    public static final int PARAM_HEADER = 0;
	    public static final int PARAM_ACCOUNT_ID = 1;
	    public static final int PARAM_BALANCE_TYPES = 2;
	    public static final int PARAM_GENERIC_PARAMETERS = 3;
	    
	    public static final String PARAM_ACCOUNT_ID_NAME = "accountID";
	    public static final String PARAM_BALANCE_TYPES_NAME = "balanceTypes";
	    public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
	    public static final String PARAM_ADJUSTMENT_TYPE_NAME = "adjustmentType";

	}

	/**
	 * 
	 * @author Marcio Marques
	 * @since 9.1.3
	 *
	 */
	public static class AccountStateUpdateQueryExecutor extends AbstractQueryExecutor<SuccessCode> 
	{
	    public AccountStateUpdateQueryExecutor()
	    {
	        delegate_ = new AccountUpdateWithStateTransitionQueryExecutor();
	    }
	    
	    @Override
	    public SuccessCode execute(Context ctx, Object... parameters) throws CRMExceptionFault
	    {
	        try
	        {
	            if (isGenericExecution(ctx, parameters))
	            {
	                delegate_.execute(ctx, parameters);
	            }
	            else
	            {
	                RmiApiErrorHandlingSupport.validateMandatoryObject(parameters[PARAM_ACCOUNT_ID], PARAM_ACCOUNT_ID_NAME);
	                RmiApiErrorHandlingSupport.validateMandatoryObject(parameters[PARAM_NEW_STATE], PARAM_NEW_STATE_NAME);
	    
	                Object[] newParameters = new Object[5];
	                newParameters[AccountUpdateWithStateTransitionQueryExecutor.PARAM_HEADER] = parameters[PARAM_HEADER];
	                newParameters[AccountUpdateWithStateTransitionQueryExecutor.PARAM_ACCOUNT_ID] = parameters[PARAM_ACCOUNT_ID];
	                newParameters[AccountUpdateWithStateTransitionQueryExecutor.PARAM_CURRENT_STATES] = null;
	                newParameters[AccountUpdateWithStateTransitionQueryExecutor.PARAM_NEW_STATE] = parameters[PARAM_NEW_STATE];
	                newParameters[AccountUpdateWithStateTransitionQueryExecutor.PARAM_GENERIC_PARAMETERS] = parameters[PARAM_GENERIC_PARAMETERS];
	    
	                delegate_.execute(ctx, newParameters);
	            }
	        }
	        catch (CRMExceptionFault e)
	        {
	            if (e.getFaultMessage().getCRMException() instanceof AccountStateTransitionException)
	            {
	                RmiApiErrorHandlingSupport.generalException(ctx, (Exception) e.getCause(), e.getFaultMessage().getCRMException().getMessage(), e.getFaultMessage().getCRMException().getCode(), this);
	            }
	            else
	            {
	                throw e;
	            }
	        }
	        
	        return SuccessCodeEnum.SUCCESS.getValue();
	        
	    }

	    @Override
	    public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
	    {
	        Object[] result = null;
	        if (isGenericExecution(ctx, parameters))
	        {
	            Object[] delegateParameters = delegate_.getParameters(ctx, parameters);
	            result = new Object[4];
	            result[PARAM_HEADER] = delegateParameters[AccountUpdateWithStateTransitionQueryExecutor.PARAM_HEADER];
	            result[PARAM_ACCOUNT_ID] = delegateParameters[AccountUpdateWithStateTransitionQueryExecutor.PARAM_ACCOUNT_ID];
	            result[PARAM_NEW_STATE] = delegateParameters[AccountUpdateWithStateTransitionQueryExecutor.PARAM_NEW_STATE];
	            result[PARAM_GENERIC_PARAMETERS] = delegateParameters[AccountUpdateWithStateTransitionQueryExecutor.PARAM_GENERIC_PARAMETERS];
	        }
	        else
	        {
	            result = parameters;
	        }
	        return result;
	    }
	    
	    @Override
	    public boolean validateParameterTypes(Class<?>[] parameterTypes)
	    {
	        boolean result = true;
	        result = result && (parameterTypes.length>=4);
	        result = result && CRMRequestHeader.class.isAssignableFrom(parameterTypes[PARAM_HEADER]);
	        result = result && String.class.isAssignableFrom(parameterTypes[PARAM_ACCOUNT_ID]);
	        result = result && AccountState.class.isAssignableFrom(parameterTypes[PARAM_NEW_STATE]);
	        result = result && Object[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
	        return result;
	    }
	    
        @Override
        public boolean validateReturnType(Class<?> resultType)
        {
            return SuccessCode.class.isAssignableFrom(resultType);
            
        }


	    private AccountUpdateWithStateTransitionQueryExecutor delegate_;

	    public static final int PARAM_HEADER = 0;
	    public static final int PARAM_ACCOUNT_ID = 1;
	    public static final int PARAM_NEW_STATE = 2;
	    public static final int PARAM_GENERIC_PARAMETERS = 3;
	    
	    public static final String PARAM_ACCOUNT_ID_NAME = "accountID";
	    public static final String PARAM_NEW_STATE_NAME = "state";
	    public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
	}	

	/**
	 * 
	 * @author Marcio Marques
	 * @since 9.1.3
	 *
	 */
	public static class AccountUpdateWithStateTransitionQueryExecutor extends AbstractAccountQueryExecutor<AccountStateTransitionResult>
	{
	    public AccountUpdateWithStateTransitionQueryExecutor()
	    {
	    }
	    
	    public AccountStateTransitionResult execute(Context ctx, Object... parameters) throws CRMExceptionFault
	    {
	        String accountID = getAccountID(ctx, PARAM_ACCOUNT_ID, PARAM_ACCOUNT_ID_NAME, parameters);
	        AccountState[] currentStates = getParameter(ctx, PARAM_CURRENT_STATES, PARAM_CURRENT_STATES_NAME, AccountState[].class, parameters);
	        AccountState newState = getParameter(ctx, PARAM_NEW_STATE, PARAM_NEW_STATE_NAME, AccountState.class, parameters);
	        
	        RmiApiErrorHandlingSupport.validateMandatoryObject(accountID, PARAM_ACCOUNT_ID_NAME);
	        RmiApiErrorHandlingSupport.validateMandatoryObject(newState, PARAM_NEW_STATE_NAME);

	        Account account = getAccount(ctx, accountID);

	        GenericParameter[] genericParameters = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);
	        final AccountState oldState = RmiApiSupport.convertCrmAccountState2Api(account.getState());
	        
	        final Home home = ApiSupport.injectAPIUpdateEntityHomeIntoPipeline(ctx,  getAccountHome(ctx), getMethodSimpleName(ctx));

	        final AccountStateEnum crmNewState = RmiApiSupport.convertApiAccountState2Crm(newState);
	        final AccountStateEnum[] crmCurrentStates = RmiApiSupport.convertApiAccountState2Crm(currentStates);

	        validateUpdateAccountState(ctx, account, crmNewState, crmCurrentStates);
	        
	        try
	        {
	            account.setState(crmNewState);
	            updateSupplementaryAccountData(ctx,account, genericParameters);
	            home.store(ctx, account);
	        }
	        catch (final Exception e)
	        {
	            final String msg = "Unable to update State for Account " + account.getBAN();
	            Account currentAccount = getAccount(ctx, account.getBAN());
	            RmiApiErrorHandlingSupport.accountStateTransitionException(ctx, e, msg, oldState, newState,
	                    RmiApiSupport.convertCrmAccountState2Api(currentAccount.getState()), this);
	        }
	        
	        AccountStateTransitionResult result = new AccountStateTransitionResult();
	        result.setOldState(oldState);
	        
	        GenericParameter[] responseGenericParameters = new GenericParameter[1];
	        responseGenericParameters[0] = RmiApiSupport.createGenericParameter(PARAM_DEACTIVATION_REASON_CODE, account.getStateChangeReason());
	        
	        if(responseGenericParameters.length > 0) {
	            result.setParameters(responseGenericParameters);
	        }
	        
	        return result;
	    }
	    
	    private void updateSupplementaryAccountData(Context ctx,Account account, GenericParameter[] genericParameters) throws CRMExceptionFault,HomeException
	    {
	        if (genericParameters!=null)
	        {
	            GenericParameterParser parser = new GenericParameterParser(genericParameters);
	            String reasonCode = parser.getParameter(STATE_CHANGE_REASON_CODE, String.class);
	            
	            Long debtCollectionAgencyId = parser.getParameter(COLLECTION_AGENCY_ID, Long.class);
	            Date promiseToPayDate = parser.getParameter(PROMISE_TO_PAY_DATE, Date.class);

	            if (debtCollectionAgencyId!=null)
	            {
	                account.setDebtCollectionAgencyId(debtCollectionAgencyId);
	            }
	            
	            if (promiseToPayDate!=null)
	            {
	                account.setPromiseToPayDate(promiseToPayDate);
	            }
	            	validateReasonCode(ctx, reasonCode,account);
	            
	            
	        }
	    }
	    
	    private void validateReasonCode(Context ctx,String reasonCode,Account account) throws CRMExceptionFault, HomeInternalException, HomeException
	    {	    	
	    	if((!reasonCode.equals(null))&&(!reasonCode.equals("")))
	    	{
	    		Home home = (Home)ctx.get(AccountStateChangeReasonHome.class);
	    		Home mapHome = (Home)ctx.get(AccountReasonCodeMappingHome.class);
		    	And filter = new And();
		    	And mapFilter = new And();
		    	filter.add(new EQ(AccountStateChangeReasonXInfo.SPID,account.getSpid()));
		    	filter.add(new EQ(AccountStateChangeReasonXInfo.REASON_CODE,reasonCode));
		    	
		    	AccountStateChangeReason reason = (AccountStateChangeReason)home.find(ctx, filter);
		    	if(reason != null)
		    	{
		    		mapFilter.add(new EQ(AccountReasonCodeMappingXInfo.ACCOUNT_STATE,account.getState()));
			    	mapFilter.add(new EQ(AccountReasonCodeMappingXInfo.REASON_CODE,reason.getID()));
			    	AccountReasonCodeMapping mapBean = (AccountReasonCodeMapping)mapHome.find(ctx, mapFilter);
			    	if (LogSupport.isDebugEnabled(ctx))
		            {
		                new DebugLogMsg(this, "Account State Change Reason Object recieved: "+ reason);
		            }
			    	if(mapBean != null)
			    	{
			    		if (LogSupport.isDebugEnabled(ctx))
			            {
			                new DebugLogMsg(this, "Account Reason Code Mapping Object recieved: "+ reason);
			            }
			    		account.setStateChangeReason(mapBean.getReasonCode());
			    	}else
			    	{
			    		throw new CRMExceptionFault("Reason code "+ reasonCode +" is not mapped for the account state "+ account.getState()+" in the Billing system");
			    	}
		    		
		    	}else
		    	{
		    		//throw new HomeException("There are no reason codes found in the Billing System for the reason codes"+ reasonCode);
		    		throw new CRMExceptionFault("There are no reason codes found in the Billing System for the reason codes"+ reasonCode);
		    	}
	    	}else if(reasonCode.equals(null))
	    	{
	    		account.setStateChangeReason(0);
	    	}else if(reasonCode.equals(""))
	    	{
	    		account.setStateChangeReason(-1);
	    	}
	    	
	    	
	    }
	    private void validateUpdateAccountState(final Context ctx, final Account account, final AccountStateEnum state, final AccountStateEnum[] validCurrentStates)
	            throws CRMExceptionFault
	    {
	        if (account.getState() == state)
	        {
	            RmiApiErrorHandlingSupport.simpleValidation("state",
	                    "Account is currently in the given State. Cannot change state to the same State.");
	        }
	        else if (!AccountStateTransitionSupport.instance().isManualStateTransitionAllowed(ctx, account, state))
	        {
	            final StringBuilder sb = new StringBuilder();
	            sb.append("Account state transition from ");
	            sb.append(account.getState().getDescription());
	            sb.append(" state to ");
	            sb.append(state.getDescription());
	            sb.append(" state is not allowed.");

	            RmiApiErrorHandlingSupport.accountStateTransitionException(ctx, null, sb.toString(),
	                    RmiApiSupport.convertCrmAccountState2Api(account.getState()),
	                    RmiApiSupport.convertCrmAccountState2Api(state),
	                    RmiApiSupport.convertCrmAccountState2Api(account.getState()), this);
	        }
	        else if (validCurrentStates!=null && validCurrentStates.length > 0 && !EnumStateSupportHelper.get(ctx).isOneOfStates(account.getState(), validCurrentStates))
	        {
	            
	            final StringBuilder sb = new StringBuilder();
	            sb.append("Account current state is ");
	            sb.append(account.getState().getDescription());
	            sb.append(".");

	            RmiApiErrorHandlingSupport.accountStateTransitionException(ctx, null, sb.toString(),
	                    RmiApiSupport.convertCrmAccountState2Api(account.getState()),
	                    RmiApiSupport.convertCrmAccountState2Api(state),
	                    RmiApiSupport.convertCrmAccountState2Api(account.getState()), this);
	        }
	    }
	    
	    @Override
	    public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
	    {
	        Object[] result = null;
	        if (isGenericExecution(ctx, parameters))
	        {
	            result = new Object[5];
	            result[0] = parameters[0];
	            result[1] = getAccountID(ctx, PARAM_ACCOUNT_ID, PARAM_ACCOUNT_ID_NAME, parameters);
	            result[2] = getParameter(ctx, PARAM_CURRENT_STATES, PARAM_CURRENT_STATES_NAME, AccountState[].class, parameters);
	            result[3] = getParameter(ctx, PARAM_NEW_STATE, PARAM_NEW_STATE_NAME, AccountState.class, parameters);
	            result[4] = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);
	        }
	        else
	        {
	            result = parameters;
	        }
	        
	        return result;
	    }

	    @Override
	    public boolean validateParameterTypes(Class<?>[] parameterTypes)
	    {
	        boolean result = true;
	        result = result && (parameterTypes.length>=5);
	        result = result && CRMRequestHeader.class.isAssignableFrom(parameterTypes[PARAM_HEADER]);
	        result = result && String.class.isAssignableFrom(parameterTypes[PARAM_ACCOUNT_ID]);
	        result = result && AccountState[].class.isAssignableFrom(parameterTypes[PARAM_CURRENT_STATES]);
	        result = result && AccountState.class.isAssignableFrom(parameterTypes[PARAM_NEW_STATE]);
	        result = result && Object[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
	        return result;
	    }

        @Override
        public boolean validateReturnType(Class<?> resultType)
        {
            return AccountStateTransitionResult.class.isAssignableFrom(resultType);
            
        }

	    public static final int PARAM_HEADER = 0;
	    public static final int PARAM_ACCOUNT_ID = 1;
	    public static final int PARAM_CURRENT_STATES = 2;
	    public static final int PARAM_NEW_STATE = 3;
	    public static final int PARAM_GENERIC_PARAMETERS = 4;
	    
	    public static final String PARAM_ACCOUNT_ID_NAME = "accountID";
	    public static final String PARAM_CURRENT_STATES_NAME = "currentStates";
	    public static final String PARAM_NEW_STATE_NAME = "newState";
	    public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";

	    public static final String COLLECTION_AGENCY_ID = "CollectionAgencyID";

	    public static final String PROMISE_TO_PAY_DATE = "PromiseToPayDate";
	    public static final String STATE_CHANGE_REASON_CODE = "StateChangeReasonCode";
	    public static final String PARAM_DEACTIVATION_REASON_CODE = "AccountDeactivationReasonCode";
	}
	
	/**
	 * 
	 * @author Marcio Marques
	 * @since 9.2
	 *
	 */
	public static class AccountForIDsListQueryExecutor extends AbstractAccountQueryExecutor<AccountForIDResults>
	{
		private class AccountIdentificationEntries
		{
			public AccountIdentificationEntries(String ban)
			{
				ban_ = ban;
				identifications_ = new HashSet<IdentificationEntry>();
			}
			
			public String getBan()
			{
				return ban_;
			}
			
			public void addIdentificationEntry(IdentificationEntry identification)
			{
				identifications_.add(identification);
			}
			
			public IdentificationEntry[] getIdentificationEntries()
			{
				return identifications_.toArray(new IdentificationEntry[0]);
			}
			
			private String ban_;
			private Set<IdentificationEntry> identifications_;
			
			
		}

		public AccountForIDsListQueryExecutor()
	    {
	    }
	    
	    public AccountForIDResults execute(Context ctx, Object... parameters) throws CRMExceptionFault
	    {
            CRMRequestHeader header = getParameter(ctx, PARAM_HEADER, "header", CRMRequestHeader.class, parameters);
            IdentificationEntry[] ids = getParameter(ctx, PARAM_IDENTIFICATION_ENTRIES, PARAM_IDENTIFICATION_ENTRIES_NAME, IdentificationEntry[].class, parameters);
            AccountState[] states = getParameter(ctx, PARAM_STATES, PARAM_STATES_NAME, AccountState[].class, parameters);
            GenericParameter[] genericParameters = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);

	        RmiApiErrorHandlingSupport.validateMandatoryObject(ids, PARAM_IDENTIFICATION_ENTRIES_NAME);
	        
	        AccountForIDResults result = new AccountForIDResults();
	        
	        Set<String> bansToRetrieve = new HashSet<String>();
	        Map<String, AccountIdentificationEntries> accountsIdentifications = new HashMap<String, AccountIdentificationEntries>();

	        for (IdentificationEntry id : ids)
	        {
	        	// Retrieve all account identifications relationships for given identification
	        	Collection<AccountIdentification> accountIdentifications = getAccountIdentifications(ctx, id);
	        	
	        	for (AccountIdentification identification : accountIdentifications)
	        	{
	        		String ban = identification.getBAN();
	        		
	        		// Add ban to the list of accounts that should be retrieved.
	        		bansToRetrieve.add(ban);
	        		
	        		// Retrieve account identification entries entry for given account from map (add it there if if doesn't exist).
	        		AccountIdentificationEntries accountIdentificationEntries = accountsIdentifications.get(ban);
	        		if (accountIdentificationEntries == null)
	        		{
	        			accountIdentificationEntries = new AccountIdentificationEntries(identification.getBAN());
	        			accountsIdentifications.put(ban, accountIdentificationEntries);
	        		}
	        		
	        		// Add identification to the account identification entries (remember to set the expiry date).
	        		IdentificationEntry accountId = new IdentificationEntry();
	        		accountId.setType(id.getType());
	        		accountId.setValue(id.getValue());
	        		if (identification.getExpiryDate()!=null)
	        		{
	        			accountId.setExpiry(CalendarSupportHelper.get(ctx).dateToCalendar(identification.getExpiryDate()));
	        		}
	        		accountIdentificationEntries.addIdentificationEntry(accountId);
	        	}
	        }
	        
            final User user = RmiApiSupport.retrieveUser(ctx, header, this.getClass().getName());
	        int spid = user.getSpid();
	        
	        CRMSpid crmSpid = RmiApiSupport.getCrmServiceProvider(ctx, spid, this);
	        long maxThreshold = crmSpid.getMaxGetLimit();
	        
	        if (bansToRetrieve.size()>maxThreshold)
	        {
				RmiApiErrorHandlingSupport
						.generalException(
								ctx,
								null,
								"The number of matching accounts " + "("
										+ bansToRetrieve.size()
										+ ") exceeds the maximum threshold ("
										+ maxThreshold + ")",
								ExceptionCode.RESULTS_EXCEED_MAXIMUM_THRESHOLD_EXCEPTION,
								this);
	        }
	        
	        GenericParameterParser parser = new GenericParameterParser(genericParameters);
            
	        GroupHierarchyType[] groupHierarchyTypes = parser.getParameter(GROUP_HIERARCHY_TYPES, GroupHierarchyType[].class);
	        
	        // Return all accounts which given identifications filtering by state and group hierarchy type.
	        Collection<Account> accounts = getAccounts(ctx, bansToRetrieve, RmiApiSupport.convertApiAccountState2Crm(states), RmiApiSupport.convertApiGroupHierarchyType2CrmGroupType(groupHierarchyTypes));
	        
	        for (Account account : accounts)
	        {
	        	// For each account, create an AccountForIDResult and add it to the final result.
	        	AccountIdentificationEntries accountIdentificationEntries  = accountsIdentifications.get(account.getBAN());
	        	AccountForIDResult singleResult = new AccountForIDResult();
	        	singleResult.setAccountID(account.getBAN());
	        	singleResult.setState(RmiApiSupport.convertCrmAccountState2Api(account.getState()));
	        	singleResult.setIds(accountIdentificationEntries.getIdentificationEntries());
	        	result.addResults(singleResult);
	        }
	        
	        return result;
	        
	    }
	   
	    /**
	     * Return account identifications for specific identification
	     * @param ctx
	     * @param id
	     * @return
	     * @throws CRMExceptionFault
	     */
	    private Collection<AccountIdentification> getAccountIdentifications(Context ctx, IdentificationEntry id) throws CRMExceptionFault
	    {
	    	try
	    	{
		    	And filter = new And();
		    	filter.add(new EQ(AccountIdentificationXInfo.ID_TYPE, Integer.valueOf((int) id.getType().longValue())));
		    	filter.add(new EQ(AccountIdentificationXInfo.ID_NUMBER, id.getValue()));
		    	return HomeSupportHelper.get(ctx).getBeans(ctx, AccountIdentification.class, filter);
	    	}
	    	catch (HomeException e)
	    	{
	    		RmiApiErrorHandlingSupport.generalException(ctx, e, "Unable to retrieve account identification", this);
	    	}
	    	
	    	return null;
		    	
	    }
	    
	    /**
	     * Return accounts with specified BANs in the specified states.
	     * @param ctx
	     * @param BANs
	     * @param states
	     * @return
	     * @throws CRMExceptionFault
	     */
	    private Collection<Account> getAccounts(Context ctx, Set<String> BANs, AccountStateEnum[] states, GroupTypeEnum[] groupTypes) throws CRMExceptionFault
	    {
	    	try
	    	{
		    	And filter = new And();
		    	filter.add(new In(AccountXInfo.BAN, BANs));
		    	if (states!=null)
		    	{
		    	    Set<AccountStateEnum> stateSet = new HashSet<AccountStateEnum>();
		    		for (AccountStateEnum state : states)
		    		{
		    		    stateSet.add(state);				    	
		    		}
                    if (stateSet.size() != 0)
                    {
                        filter.add(new In(AccountXInfo.STATE, stateSet));
                    }
		    	}
		    	if (groupTypes!=null)
                {
                    Set<GroupTypeEnum> groupTypeSet = new HashSet<GroupTypeEnum>();
                    for (GroupTypeEnum groupType : groupTypes)
                    {
                        groupTypeSet.add(groupType);                        
                    }
                    if (groupTypeSet.size() != 0)
                    {
                        filter.add(new In(AccountXInfo.GROUP_TYPE, groupTypeSet));
                    }
                }
		    	return HomeSupportHelper.get(ctx).getBeans(ctx, Account.class, filter);
	    	}
	    	catch (HomeException e)
	    	{
	    		RmiApiErrorHandlingSupport.generalException(ctx, e, "Unable to retrieve accounts", this);
	    	}
	    	
	    	return null;
		    	
	    }

	    @Override
	    public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
	    {
	        Object[] result = null;
	        if (isGenericExecution(ctx, parameters))
	        {
	            result = new Object[4];
	            result[0] = parameters[0];
	            result[1] = getParameter(ctx, PARAM_IDENTIFICATION_ENTRIES, PARAM_IDENTIFICATION_ENTRIES_NAME, IdentificationEntry[].class, parameters);
	            result[2] = getParameter(ctx, PARAM_STATES, PARAM_STATES_NAME, AccountState[].class, parameters);
	            result[3] = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);
	        }
	        else
	        {
	            result = parameters;
	        }
	        
	        return result;
	    }

	    @Override
	    public boolean validateParameterTypes(Class<?>[] parameterTypes)
	    {
	        boolean result = true;
	        result = result && (parameterTypes.length>=4);
	        result = result && CRMRequestHeader.class.isAssignableFrom(parameterTypes[PARAM_HEADER]);
	        result = result && IdentificationEntry[].class.isAssignableFrom(parameterTypes[PARAM_IDENTIFICATION_ENTRIES]);
	        result = result && AccountState[].class.isAssignableFrom(parameterTypes[PARAM_STATES]);
	        result = result && Object[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
	        return result;
	    }

        @Override
        public boolean validateReturnType(Class<?> resultType)
        {
            return AccountForIDResults.class.isAssignableFrom(resultType);
            
        }

        public static final int PARAM_HEADER = 0;
	    public static final int PARAM_IDENTIFICATION_ENTRIES = 1;
	    public static final int PARAM_STATES = 2;
	    public static final int PARAM_GENERIC_PARAMETERS = 3;
	    
	    public static final String PARAM_IDENTIFICATION_ENTRIES_NAME = "ids";
	    public static final String PARAM_STATES_NAME = "states";
	    public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
	    public static final String GROUP_HIERARCHY_TYPES = "GroupHierarchyTypes";
	}

	/**
	 * 
	 * @author vijay.gote
	 * @since 9.5
	 *
	 */
	public static class UpdateAccountParentV2QueryExecutor extends AbstractAccountQueryExecutor<GenericResponse>
	{
	    Context ctx = null; 
	    Account account = null; 
	    String parentID = null;
	    Long newDepositAmount;
	    Boolean responsible; 
	    Integer expiryExtention;
	    GenericResponse genericResponse = null;
	    String accountID = null;
	    
	    public UpdateAccountParentV2QueryExecutor()
	    {
	        
	    }
	    
	    public UpdateAccountParentV2QueryExecutor(Account account, String parentID, Long newDepositAmount, Boolean responsible, Integer expiryExtention) {
	        this.account = account;
	        this.parentID = parentID;
	        this.newDepositAmount = newDepositAmount;
	        this.responsible = responsible;
	        this.expiryExtention = expiryExtention;
	    }
	    
	    public GenericResponse execute(Context ctx, Object... parameters) throws CRMExceptionFault
	    {
	        try
	        {
	            accountID = getParameter(ctx, PARAM_ACCOUNT_ID, PARAM_ACCOUNT_ID_NAME, String.class, parameters);
	            parentID = getParameter(ctx, PARAM_PARENT_ID, PARAM_PARENT_NAME, String.class, parameters);
	            Long newDepositAmount = getParameter(ctx, PARAM_NEW_DEPOSIT_AMOUNT, PARAM_NEW_DEPOSIT_AMOUNT_NAME, Long.class, parameters);
	            Boolean responsible = getParameter(ctx, PARAM_RESPONSIBLE, PARAM_RESPONSIBLE_NAME, Boolean.class, parameters);
	            Integer expiryExtention = getParameter(ctx, PARAM_EXPIRY_EXTENTION, PARAM_EXPIRY_EXTENTION_NAME, Integer.class, parameters);
                if (responsible != null)
                {
                    this.responsible = responsible;
                }

	            GenericParameter[] genericParameters = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);

	            RmiApiErrorHandlingSupport.validateMandatoryObject(accountID, PARAM_ACCOUNT_ID_NAME);

	            account = getAccount(ctx, accountID);
	            ctx.put("MOVE_ACCOUNTID", accountID);

	            if (responsible != null && account.isPrepaid())
	            {
	                final String msg = "Unable to change responsible flag for prepaid account " + accountID;
	                RmiApiErrorHandlingSupport.simpleValidation("responsible", msg);
	            }

	            MoveRequest request = MoveRequestSupport.getMoveRequest(ctx, account);

	            if (genericParameters!=null)
	            {
	                GenericParameterParser parser = new GenericParameterParser(genericParameters);
	                Boolean validateOnly = parser.getParameter(VALIDATE_ONLY, Boolean.class);
	                if(validateOnly) {
	                    return validateOnly(ctx, accountID, genericParameters, request);
	                }
	            }
	            Home crmSpidHome = (Home) ctx.get(CRMSpidHome.class);
	            CRMSpid crmSpidBean = null;
	            try {
	                crmSpidBean = (CRMSpid) crmSpidHome.find(ctx, account.getSpid());
	            } catch(Exception e) {
	            }

	            if(crmSpidBean.getNumberOfBANsForMove() < AccountSupport.getTotalAccountsMoved(ctx, account)) 
	            {
	                genericResponse = new GenericResponse();
	                genericResponse.setStatusCode(STATUS_CODE_MOVE_IN_PROGRESS);
	                genericResponse.setStatusMessage(STATUS_MESSAGE_MOVE_IN_PROGRESS);	                
	                AcountMoveProducerAgent producer = (AcountMoveProducerAgent) ctx.get(AcountMoveProducerAgent.class);
	                producer.produceAccountMoveThread(ctx, account, parentID, newDepositAmount, responsible, expiryExtention);	                
	                return genericResponse;
	            } 
	            UpdateAccountParentV2QueryExecutor updateAccountParentV2 =  new UpdateAccountParentV2QueryExecutor(account, parentID, newDepositAmount, responsible, expiryExtention);
	            updateAccountParentV2.moveAccount(ctx);
	        }
	        catch (CRMExceptionFault e)
	        {
	            if (e.getFaultMessage().getCRMException() instanceof AccountStateTransitionException)
	            {
	                RmiApiErrorHandlingSupport.generalException(ctx, (Exception) e.getCause(), e.getFaultMessage().getCRMException().getMessage(), e.getFaultMessage().getCRMException().getCode(), this);
	            }
	            else
	            {
	                throw e;
	            }
	        }
            catch (AccountMoveException ae)
            {
                String msg = (ae.getCause() != null) ? ae.getCause().getMessage() : ae.getMessage();
                RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, (Exception) ae.getCause(), msg, this);
            }

	        List<String> newBANs = (List<String>)ctx.get(MoveConstants.NEW_BANs);
	        genericResponse = new GenericResponse();
	        genericResponse.setStatusCode(STATUS_CODE_MOVE_SUCCESS);
	        genericResponse.setStatusMessage(STATUS_MESSAGE_MOVE_SUCCESS);
	        GenericParameter[] apiGenericParameters;    
	        if(!newBANs.isEmpty()){
	            apiGenericParameters = new GenericParameter[newBANs.size()*7];
	            int counter = 0;
	            for(int i = 0; i<newBANs.size(); i++) 
	            {
	                apiGenericParameters[counter] = RmiApiSupport.createGenericParameter(Constants.GENERICPARAMETER_OLDBAN, accountID);
    	            apiGenericParameters[counter+1] = RmiApiSupport.createGenericParameter(Constants.GENERICPARAMETER_NEWBAN, newBANs.get(i));
    	            apiGenericParameters[counter+2] = RmiApiSupport.createGenericParameter(Constants.GENERICPARAMETER_OLDPARENTBAN, account.getParentBAN());
                    apiGenericParameters[counter+3] = RmiApiSupport.createGenericParameter(Constants.GENERICPARAMETER_NEWPARENTBAN, parentID);
                    apiGenericParameters[counter+4] = RmiApiSupport.createGenericParameter(Constants.GENERICPARAMETER_OLDRESPONSIBLEBAN, account.getResponsibleBAN());
                    apiGenericParameters[counter+5] = RmiApiSupport.createGenericParameter(Constants.GENERICPARAMETER_NEWRESPONSIBLEBAN, getAccount(ctx,  newBANs.get(i)).getResponsibleBAN());
                    apiGenericParameters[counter+6] = RmiApiSupport.createGenericParameter(Constants.GENERICPARAMETER_NUMOFACCOUNTSELIGIBLEFORMOVE, AccountSupport.getTotalAccountsMoved(ctx, account));
                    counter = counter + 7;
	            }
                genericResponse.setParameters(apiGenericParameters);
	        } 
	        else {
	            apiGenericParameters = new GenericParameter[7];
	            apiGenericParameters[0] = RmiApiSupport.createGenericParameter(Constants.GENERICPARAMETER_OLDBAN, accountID);
    	        apiGenericParameters[1] = RmiApiSupport.createGenericParameter(Constants.GENERICPARAMETER_NEWBAN, parentID);
    	        apiGenericParameters[2] = RmiApiSupport.createGenericParameter(Constants.GENERICPARAMETER_OLDPARENTBAN, account.getParentBAN());
    	        apiGenericParameters[3] = RmiApiSupport.createGenericParameter(Constants.GENERICPARAMETER_NEWPARENTBAN, parentID);
    	        apiGenericParameters[4] = RmiApiSupport.createGenericParameter(Constants.GENERICPARAMETER_OLDRESPONSIBLEBAN, account.getResponsibleBAN());
    	        apiGenericParameters[5] = RmiApiSupport.createGenericParameter(Constants.GENERICPARAMETER_NEWRESPONSIBLEBAN, getAccount(ctx, accountID).getResponsibleBAN());
    	        apiGenericParameters[6] = RmiApiSupport.createGenericParameter(Constants.GENERICPARAMETER_NUMOFACCOUNTSELIGIBLEFORMOVE, AccountSupport.getTotalAccountsMoved(ctx, account));
    	        genericResponse.setParameters(apiGenericParameters);
	        }
	        return genericResponse;
	    }

	    private GenericResponse validateOnly(Context ctx, String accountID,
	            GenericParameter[] genericParameters, MoveRequest request) throws CRMExceptionFault
	    {
	        
	        genericResponse = new GenericResponse();
	        try{
	            if (request instanceof AccountMoveRequest)
	            {
	                AccountMoveRequest accRequest = (AccountMoveRequest) request;
	                if(parentID == null)
	                {
	                	accRequest.setNewParentBAN("");
	                }
	                else
	                {
	                	accRequest.setNewParentBAN(parentID);
	                }
	                if (responsible != null)
	                {
	                    accRequest.setNewResponsible(responsible);
	                }

	                new MoveManager().validate(ctx, accRequest);
	            }
	            genericResponse.setStatusCode(STATUS_CODE_MOVE_IS_VALID);
	            genericResponse.setStatusMessage(STATUS_MESSAGE_MOVE_IS_VALID);
	            GenericParameter[] apiGenericParameters = new GenericParameter[7];        
	            apiGenericParameters[0] = RmiApiSupport.createGenericParameter(Constants.GENERICPARAMETER_OLDBAN, accountID);
	            apiGenericParameters[1] = RmiApiSupport.createGenericParameter(Constants.GENERICPARAMETER_NEWBAN, parentID);
	            apiGenericParameters[2] = RmiApiSupport.createGenericParameter(Constants.GENERICPARAMETER_OLDPARENTBAN, account.getParentBAN());
	            apiGenericParameters[3] = RmiApiSupport.createGenericParameter(Constants.GENERICPARAMETER_NEWPARENTBAN, parentID);
	            apiGenericParameters[4] = RmiApiSupport.createGenericParameter(Constants.GENERICPARAMETER_OLDRESPONSIBLEBAN, account.getResponsibleBAN());
	            apiGenericParameters[5] = RmiApiSupport.createGenericParameter(Constants.GENERICPARAMETER_NEWRESPONSIBLEBAN, getAccount(ctx, accountID).getResponsibleBAN());
	            apiGenericParameters[6] = RmiApiSupport.createGenericParameter(Constants.GENERICPARAMETER_NUMOFACCOUNTSELIGIBLEFORMOVE, AccountSupport.getTotalAccountsMoved(ctx, account));
	            genericResponse.setParameters(apiGenericParameters);
	            return genericResponse;
	        } catch (IllegalStateException ise) {
	            genericResponse.setStatusCode(STATUS_CODE_MOVE_IS_NOT_VALID);
	            genericResponse.setStatusMessage(STATUS_MESSAGE_MOVE_IS_NOT_VALID);
	            return genericResponse;
	        }
	    }

	    @Override
	    public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
	    {
	        Object[] result = null;
	        if (isGenericExecution(ctx, parameters))
	        {
	            result = new Object[7];
	            result[0] = parameters[0];
	            result[1] = getAccountID(ctx, PARAM_ACCOUNT_ID, PARAM_ACCOUNT_ID_NAME, parameters);
	            result[2] = getAccountID(ctx, PARAM_PARENT_ID, PARAM_PARENT_NAME, parameters);
	            result[3] = getParameter(ctx, PARAM_NEW_DEPOSIT_AMOUNT, PARAM_NEW_DEPOSIT_AMOUNT_NAME, Long.class, parameters);
	            result[4] = getParameter(ctx, PARAM_RESPONSIBLE, PARAM_RESPONSIBLE_NAME, Boolean.class, parameters);
	            result[5] = getParameter(ctx, PARAM_EXPIRY_EXTENTION, PARAM_EXPIRY_EXTENTION_NAME, Integer.class, parameters);
	            result[6] = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);
	        }
	        else
	        {
	            result = parameters;
	        }

	        return result;
	    }

	    @Override
	    public boolean validateParameterTypes(Class<?>[] parameterTypes)
	    {
	        boolean result = true;
	        result = result && (parameterTypes.length>=7);
	        result = result && CRMRequestHeader.class.isAssignableFrom(parameterTypes[PARAM_HEADER]);
	        result = result && String.class.isAssignableFrom(parameterTypes[PARAM_ACCOUNT_ID]);
	        result = result && String.class.isAssignableFrom(parameterTypes[PARAM_PARENT_ID]);
	        result = result && Long.class.isAssignableFrom(parameterTypes[PARAM_NEW_DEPOSIT_AMOUNT]);
	        result = result && Boolean.class.isAssignableFrom(parameterTypes[PARAM_RESPONSIBLE]);
	        result = result && Integer.class.isAssignableFrom(parameterTypes[PARAM_EXPIRY_EXTENTION]);
	        result = result && Object[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
	        return result;
	    }

	    @Override
	    public boolean validateReturnType(Class<?> resultType)
	    {
	        return SuccessCode.class.isAssignableFrom(resultType);

	    }

	    public void moveAccount(Context ctx) throws AccountMoveException
	    {
	        try
	        {
	            List<String> newAccountBANs = new ArrayList<String>();
	            MoveRequest request = MoveRequestSupport.getMoveRequest(ctx, account);
 	            if (request instanceof AccountMoveRequest)
	            {
	                AccountMoveRequest accRequest = (AccountMoveRequest) request;
	                if(parentID == null)
	                {
	                	accRequest.setNewParentBAN("");
	                }
	                else
	                {
	             
	                	accRequest.setNewParentBAN(parentID);
	                }
	                accRequest.setOldBAN(account.getBAN());
	                
	                if (request instanceof PostpaidServiceBasedSubscriberAccountMoveRequest)
	                {
	                    long depositValue = 0;
	                    if (newDepositAmount != null)
	                    {
	                        depositValue = newDepositAmount.longValue();            
	                    }

	                    PostpaidServiceBasedSubscriberAccountMoveRequest postAccRequest = (PostpaidServiceBasedSubscriberAccountMoveRequest) accRequest;
	                    postAccRequest.setNewDepositAmount(depositValue);
	                }
	                else if (request instanceof PrepaidPooledSubscriberAccountMoveRequest)
	                {
	                    int expiryExtValue = 0;
	                    if (expiryExtention != null)
	                    {
	                        expiryExtValue = expiryExtention.intValue();            
	                    }
	                    PrepaidPooledSubscriberAccountMoveRequest preAccRequest = (PrepaidPooledSubscriberAccountMoveRequest) accRequest;
	                    preAccRequest.setExpiryExtension(expiryExtValue);
	                }

	                if (responsible != null)
	                {
	                    accRequest.setNewResponsible(responsible);
	                }

	                new MoveManager().move(ctx, accRequest);
	                if(accRequest.getNewBAN() != null)
	                {
	                    newAccountBANs.add(accRequest.getNewBAN());
	                }
	                ctx.put(MoveConstants.NEW_BANs, newAccountBANs);
	            }
	        }
	        catch (final Exception e)
	        {
	            final String msg = "Unable to update Parent for Account " + accountID;
	            throw new AccountMoveException(msg, e);
	        }
	    }
	    
	    public static final String METHOD_SIMPLE_NAME = "updateAccountParentV2";
        public static final String METHOD_NAME = "AccountService." + METHOD_SIMPLE_NAME;

        public static final String PERMISSION = Constants.PERMISSION_ACCOUNTS_WRITE_UPDATEACCOUNTPARENTV2;

        public static final int PARAM_HEADER = 0;
        public static final int PARAM_ACCOUNT_ID = 1;
        public static final int PARAM_PARENT_ID = 2;
        public static final int PARAM_NEW_DEPOSIT_AMOUNT = 3;
        public static final int PARAM_RESPONSIBLE = 4;
        public static final int PARAM_EXPIRY_EXTENTION = 5;
        public static final int PARAM_GENERIC_PARAMETERS = 6;

        public static final String PARAM_ACCOUNT_ID_NAME = "accountID";
        public static final String PARAM_PARENT_NAME = "parentID";
        public static final String PARAM_NEW_DEPOSIT_AMOUNT_NAME = "newDepositAmount";
        public static final String PARAM_RESPONSIBLE_NAME = "responsible";
        public static final String PARAM_EXPIRY_EXTENTION_NAME = "expiryExtention";

        public static final String VALIDATE_ONLY = "validateOnly";

        public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";

        public static final String STATUS_CODE_MOVE_SUCCESS = "0000";
        public static final String STATUS_MESSAGE_MOVE_SUCCESS = "Moved Successfully.";
        public static final String STATUS_CODE_MOVE_IN_PROGRESS = "1001";
        public static final String STATUS_MESSAGE_MOVE_IN_PROGRESS = "Move In Progress.";
        public static final String STATUS_CODE_MOVE_IS_VALID = "1002";
        public static final String STATUS_MESSAGE_MOVE_IS_VALID = "Move Is Valid.";
        public static final String STATUS_CODE_MOVE_IS_NOT_VALID = "1003";
        public static final String STATUS_MESSAGE_MOVE_IS_NOT_VALID = "Move Is Not Valid.";
	}
	
	/**
	 * 
	 * @author Bhagyashree Dhavalshankh
	 * @since 9.5.1
	 *
	 */
	public static class GetAccountProfileV2QueryExecutor extends AbstractAccountQueryExecutor<AccountProfileQueryResultsV2>
	{

		public GetAccountProfileV2QueryExecutor()
	    {
			
	    }
		
	    public AccountProfileQueryResultsV2 execute(Context ctx, Object... parameters) throws CRMExceptionFault
	    {
	        String accountID = getAccountID(ctx, PARAM_ACCOUNT_ID, PARAM_ACCOUNT_ID_NAME, parameters);
	        GenericParameter[] genericParameters = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);
	        
	        RmiApiErrorHandlingSupport.validateMandatoryObject(accountID, PARAM_ACCOUNT_ID_NAME);
	        
	        final Account account = getAccount(ctx, accountID);

	        final AccountProfileQueryResultsV2 result = new AccountProfileQueryResultsV2();
	        try
	        {
	            result.setProfile(AccountsApiSupport.extractProfile(account));
	            result.setDetail(AccountsApiSupport.extractDetail(account));
	            result.setBilling(AccountsApiSupport.extractBilling(account));
	            result.setPaymentInfo(AccountsApiSupport.extractPaymentInfo(ctx, account));
	            result.setIdentification(AccountsApiSupport.extractIdentification(ctx, account));
	            result.setCompany(AccountsApiSupport.extractCompany(account));
	            result.setBank(AccountsApiSupport.extractBank(account));
	            result.setContact(AccountsApiSupport.extractContact(account));
	            
	            GenericParameter[] outGenericParameters = {};
                List<GenericParameter> genericParamList = new ArrayList<GenericParameter>();
                
                if(account.getResponsibleBAN() != null)
                {
                    genericParamList.add(RmiApiSupport.createGenericParameter(APIGenericParameterSupport.RESPONSIBLE_BAN, account.getResponsibleBAN()));
                }
                if(account.getOldBAN() != null)
                {
                    genericParamList.add(RmiApiSupport.createGenericParameter(APIGenericParameterSupport.OLD_BAN, account.getOldBAN()));
                }
                if(account.getCreateAccountReason() != null)
                {
                    genericParamList.add(RmiApiSupport.createGenericParameter(APIGenericParameterSupport.ACCOUNT_CREATE_REASON, account.getCreateAccountReason()));
                }
                if( (account.getCsa() != null ) && (!account.getCsa().isEmpty()))
                {
                    genericParamList.add(RmiApiSupport.createGenericParameter(AccountsImpl.CSA, account.getCsa()));                    
                }
                try
                {
	                int currentNumPTPTransitions = 0;
	                if(account.getCurrentNumPTPTransitions()>0)
	                {
	                	currentNumPTPTransitions = account.getCurrentNumPTPTransitions();
	                }
	                genericParamList.add(RmiApiSupport.createGenericParameter(APIGenericParameterSupport.CURRENT_NUM_PTP_TRANSITIONS, account.getCurrentNumPTPTransitions()));
    	        }
    	        catch (final Exception e)
    	        {
    	            final String msg = "Unable to retreive CURRENT_NUM_PTP_TRANSITIONS for accountID : " + accountID;
    	            LogSupport.minor(ctx, this, msg + e.getLocalizedMessage() );
    	        }
                
                try
                {
                	genericParamList.add(RmiApiSupport.createGenericParameter(APIGenericParameterSupport.MAX_NUM_PTP_TRANSITIONS, CreditCategorySupport.findCreditCategory(ctx, account.getCreditCategory()).getMaxNumberPTP() ));
    	        }
    	        catch (final Exception e)
    	        {
    	            final String msg = "Unable to retreive MAX_NUM_PTP_TRANSITIONS for accountID : " + accountID;
    	            LogSupport.minor(ctx, this, msg + e.getLocalizedMessage() );
    	        }
                
                outGenericParameters = genericParamList.toArray(new GenericParameter[genericParamList.size()]);
                result.setParameters(outGenericParameters);
                
	        }
	        catch (final Exception e)
	        {
	            final String msg = "Unable to retreive Account for accountID : " + accountID;
	            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
	        }

	        return result;        
	    }
	    
	    @Override
	    public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
	    {
	        Object[] result = null;
	        if (isGenericExecution(ctx, parameters))
	        {
	            result = new Object[3];
	            result[0] = parameters[0];
	            result[1] = getAccountID(ctx, PARAM_ACCOUNT_ID, PARAM_ACCOUNT_ID_NAME, parameters);
	            result[2] = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);
	        }
	        else
	        {
	            result = parameters;
	        }
	        
	        return result;
	    }

	    @Override
	    public boolean validateParameterTypes(Class<?>[] parameterTypes)
	    {
	        boolean result = true;
	        result = result && (parameterTypes.length>=3);
	        result = result && CRMRequestHeader.class.isAssignableFrom(parameterTypes[PARAM_HEADER]);
	        result = result && String.class.isAssignableFrom(parameterTypes[PARAM_ACCOUNT_ID]);
	        result = result && Object[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
	        return result;
	    }

        @Override
        public boolean validateReturnType(Class<?> resultType)
        {
            return AccountProfileQueryResultsV2.class.isAssignableFrom(resultType);
            
        }
        
	    public static final String METHOD_SIMPLE_NAME = "getAccountProfileV2";
	    public static final String METHOD_NAME = "AccountService." + METHOD_SIMPLE_NAME;

	    public static final String PERMISSION = Constants.PERMISSION_ACCOUNTS_READ_GETACCOUNTPROFILEV2;
	    
	    public static final int PARAM_HEADER = 0;
	    public static final int PARAM_ACCOUNT_ID = 1;
	    public static final int PARAM_GENERIC_PARAMETERS = 2;
	    
	    public static final String PARAM_ACCOUNT_ID_NAME = "accountID";
	    public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";

	}
	
	/**
	 * 
	 * @author vijay.gote
	 * @since 9.5.1
	 *
	 */
	public static class GetAccountCugQueryExecutor extends AbstractAccountQueryExecutor<AccountCugQueryResults>
	{
		public AccountCugQueryResults execute(Context ctx, Object... parameters) throws CRMExceptionFault
		{
			String accountID = null;
			AccountCugQueryResults accountCugQueryResults = new AccountCugQueryResults();
			ClosedUserGroup closedUserGroup = null;
			CugDetails cugDetail = null;
						
			//-- In present one BAN belongs to one CUG only but we are using this as an array for future use.
			CugDetails[] cugDetails = new CugDetails[1];
			accountID = getParameter(ctx, PARAM_ACCOUNT_ID, PARAM_ACCOUNT_ID_NAME, String.class, parameters);
			RmiApiErrorHandlingSupport.validateMandatoryObject(accountID, PARAM_ACCOUNT_ID_NAME);
			
			try
			{
				GenericParameter[] genericParameters = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);

				
				closedUserGroup = ClosedUserGroupSupport.getCug(ctx, accountID);
				if(closedUserGroup != null) 
				{
					cugDetail = new CugDetails();
					cugDetail.setCugId(closedUserGroup.getID());
					cugDetail.setCugOwnerMsisdn(closedUserGroup.getOwnerMSISDN());
					ClosedUserGroupTemplate cugTemplate = ClosedUserGroupSupport.getCugTemplate(ctx, closedUserGroup.getCugTemplateID());
					if(cugTemplate != null)
					{
						cugDetail.setCugTemplateName(cugTemplate.getName());
						cugDetail.setIsShortCodeEnabled(cugTemplate.getShortCodeEnable());
						cugDetail.setShortCodePattern(cugTemplate.getShortCodePattern());
					}
					cugDetail.setCugState((int)closedUserGroup.getCugState().getIndex());
					final Map<String, ClosedSub> subscriberMap = closedUserGroup.getSubscribers();
					if(subscriberMap != null && !subscriberMap.isEmpty())
					{	
						CugMember[] cugMembers = new CugMember[subscriberMap.size()];
						int counter = 0;
						for (Map.Entry<String, ClosedSub> entry : subscriberMap.entrySet())
		                {
		                    final ClosedSub closedSub = entry.getValue();
		                    if(closedSub != null)
		                    {
			                    final CugMember cugMember;
		                        cugMember = new CugMember();
		                        cugMember.setMsisdn(closedSub.getPhoneID());
		                        cugMember.setShortCode(closedSub.getShortCode());
			                    cugMembers[counter] = cugMember;
			                    counter++;
		                    }
		                }
						cugDetail.setCugMember(cugMembers);
					}
				}
				if(cugDetail != null)
				{
					cugDetails[0] = cugDetail;
					accountCugQueryResults.setCugDetails(cugDetails);
				}
			}
			catch (final Exception e)
	        {
	            final String msg = "Unable to retreive Closed User Group for Account, accountID : " + accountID;
	            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
	        }
			return accountCugQueryResults;
		}
		
		/**
		 * {@inheritDoc}
		 */
		public boolean validateParameterTypes(Class<?>[] parameterTypes) 
		{
			boolean result = true;
			result = result && (parameterTypes.length>=3);
			result = result && CRMRequestHeader.class.isAssignableFrom(parameterTypes[PARAM_HEADER]);
			result = result && String.class.isAssignableFrom(parameterTypes[PARAM_ACCOUNT_ID]);
			result = result && Object[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
			return result;
		}
		
		/**
		 * {@inheritDoc}
		 */
		public boolean validateReturnType(Class<?> resultType) 
		{
			return AccountCugQueryResults.class.isAssignableFrom(resultType);
		}
		
		/**
		 * {@inheritDoc}
		 */
		public Object[] getParameters(Context ctx, Object... parameters)
				throws CRMExceptionFault 
		{
			Object[] result = null;
	        if (isGenericExecution(ctx, parameters))
	        {
	            result = new Object[3];
	            result[0] = parameters[0];
	            result[1] = getAccountID(ctx, PARAM_ACCOUNT_ID, PARAM_ACCOUNT_ID_NAME, parameters);
	            result[2] = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);
	        }
	        else
	        {
	            result = parameters;
	        }

	        return result;
		}
		
		public static final int PARAM_HEADER = 0;
		public static final int PARAM_ACCOUNT_ID = 1;
		public static final int PARAM_GENERIC_PARAMETERS = 2;

		public static final String PARAM_ACCOUNT_ID_NAME = "accountID";
		public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
		
	}
	
	 /* 
	 * @author Bhagyashree Dhavalshankh
	 * @since 9.5.1
	 *
	 */
	public static class ConvertIndividualToGroupQueryExecutor extends AbstractAccountQueryExecutor<GenericResponse>
	{
	    GenericResponse genericResponse = null;
		public ConvertIndividualToGroupQueryExecutor()
	    {
			
	    }
		
	    public GenericResponse execute(Context ctx, Object... parameters) throws CRMExceptionFault
	    {
            try
            {
                String accountID = getParameter(ctx, PARAM_ACCOUNT_ID, PARAM_ACCOUNT_ID_NAME, String.class, parameters);

                boolean validateOnly = getParameter(ctx, PARAM_VALIDATE_ONLY, PARAM_VALIDATE_ONLY_NAME, Boolean.class, parameters);
                GenericParameter[] genericParameters = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);

                RmiApiErrorHandlingSupport.validateMandatoryObject(accountID, PARAM_ACCOUNT_ID_NAME);
                Account account = getAccount(ctx, accountID);

                // read generic parameter
				GenericParameterParser parser = new GenericParameterParser(genericParameters);
				@SuppressWarnings("unchecked")
				List<ExtensionHolder> accountExtensions = new ArrayList<ExtensionHolder>(
						account.getAccountExtensions());
				int groupType = GroupTypeEnum.GROUP_INDEX;
				
                
                ConvertAccountGroupTypeRequest request = (ConvertAccountGroupTypeRequest) MoveRequestSupport.getMoveRequest(ctx, account, ConvertAccountGroupTypeRequest.class);
                if (parser.containsParam(GROUP_TYPE)) {

					groupType = parser.getParameter(GROUP_TYPE, Integer.class);
					
					
					GroupTypeEnum groupTypeEnum = GroupTypeEnum.get((short) groupType);
					
					//worng group type ..check validation

					if (groupType ==GroupTypeEnum.GROUP_INDEX ) {

						// These params has to be set for individual to group
						// conversion
						request.setGroupType(GroupTypeEnum.GROUP);
						request.setRetainOriginalAccount(true);
					}

					else if(groupType==GroupTypeEnum.GROUP_POOLED_INDEX) {
						request.setGroupType(GroupTypeEnum.GROUP_POOLED);
						fillPoolAccount(ctx, parser, account, accountExtensions);// for Account pool extension
						request.setAccountExtensions(accountExtensions);
						if((account.getSubscriberType()==SubscriberTypeEnum.POSTPAID)||(account.getSubscriberType()==SubscriberTypeEnum.HYBRID))
						{
						account.setIdentificationGroupList(account.getIdentificationGroupList());
						account.setSecurityQuestionsAndAnswers(account.getSecurityQuestionsAndAnswers());
						}
						
					
					}
				}else
				{
					request.setGroupType(GroupTypeEnum.GROUP);
					request.setRetainOriginalAccount(true);
				}
                //These params has to be set for individual to group conversion
               // request.setGroupType(GroupTypeEnum.GROUP);
               // request.setRetainOriginalAccount(true);
                
                if(validateOnly)
                {
                    return validateOnly(ctx, accountID, request);
                }
                else
                {
                   return convertAccount(ctx, request, account);
                }
                
            }
            catch (CRMExceptionFault e)
            {
                	genericResponse = new GenericResponse();
                	genericResponse.setStatusCode(e.getFaultMessage().getCRMException().getCode() + "");
                    genericResponse.setStatusMessage(e.getFaultMessage().getCRMException().getMessage());
                	
            }
            catch (Exception ae)
            {
                String msg = (ae.getCause() != null) ? ae.getCause().getMessage() : ae.getMessage();
                RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, (Exception) ae.getCause(), msg, this);
            }
            return  genericResponse;
 
        }
	    /**
		 * 
		 * @author Nilima Lad
		 * @desc Convert Individual Account to group pool
		 */

		// @SuppressWarnings("null")
		public void fillPoolAccount(Context ctx, GenericParameterParser parser,Account account, List<ExtensionHolder> accountExtensions)throws CRMExceptionFault
		{

			PoolExtension poolExtension = new PoolExtension();
			ExtensionHolder holder;
			int quotaType = QuotaTypeEnum.UNLIMITED_QUOTA_INDEX;
			int limitedQuotaValue = 0;
			String bundles = null;
			String subscriptionType = new String();

			try {
				poolExtension = (PoolExtension) XBeans.instantiate(PoolExtension.class, ctx);
			} catch (Exception e) {
				new MinorLogMsg(AccountsApiSupport.class,"Error instantiating new pool extension.  Using default constructor.",e).log(ctx);
				poolExtension = new PoolExtension();
			}
			
			poolExtension.setSpid(account.getSpid());
			poolExtension.setBAN(account.getBAN());
			//p.getPoolMSISDN();

			if (parser.containsParam(QUOTA_TYPE)) {
				quotaType = parser.getParameter(QUOTA_TYPE, Integer.class);
				QuotaTypeEnum quotaTypeEnum = QuotaTypeEnum.get((short) quotaType);
				poolExtension.setQuotaType(quotaTypeEnum);

			}else
			{
				poolExtension.setQuotaType(QuotaTypeEnum.UNLIMITED_QUOTA);
			}

			
			if (quotaType == QuotaTypeEnum.LIMITED_QUOTA_INDEX ) 
			{
				if( !parser.containsParam(LIMITED_QUOTAVALUE))
				{	
					RmiApiErrorHandlingSupport.validateMandatoryObject
					  (null, LIMITED_QUOTAVALUE); 
				}
				else
				{
					limitedQuotaValue = parser.getParameter(LIMITED_QUOTAVALUE,Integer.class);
					if(limitedQuotaValue <= 0)
					{
						RmiApiErrorHandlingSupport.validateMandatoryObject
						  (null, LIMITED_QUOTAVALUE); 
					}
					poolExtension.setQuotaLimit(limitedQuotaValue);
				}
			}
			else
			{
				poolExtension.setQuotaLimit(limitedQuotaValue);
			}

			if (parser.containsParam(BUNDLES)) 
			{
				bundles = parser.getParameter(BUNDLES, String.class);

				Map<Long, BundleFee> poolBundles = new HashMap<Long, BundleFee>();
				
				String bundleIDTokens[] = bundles.split(",");
				
				Long[] bundleID=new Long[bundleIDTokens.length];
				
				int j=0;
				
				for (String retval : bundleIDTokens) 
				{
					 bundleID[j]=Long.parseLong(retval.trim());
					 j++;
				}
				
				
				if(null != bundleID && bundleID.length > 0)
		        {
		            final CompoundIllegalStateException bundleTransformException = new CompoundIllegalStateException(); 
		            poolBundles = PoolExtension.transformBundles(ctx, bundleTransformException,bundleID);
		            if(bundleTransformException.getSize() >0 )
		            {
		                bundleTransformException.throwAll();
		            }
		        }
				poolExtension.setPoolBundles(poolBundles);

			}
			else
	        {

				RmiApiErrorHandlingSupport.validateMandatoryObject
				  (null, BUNDLES); //exception
	        }
			if (parser.containsParam(SUBSCRIPTIONPOOLS)) 
			{
				subscriptionType = parser.getParameter(SUBSCRIPTIONPOOLS, String.class);

				SubscriptionPoolProperty crmProperty;

				final Map<Long, SubscriptionPoolProperty> crmPoolProperties = new HashMap<Long, SubscriptionPoolProperty>();

				String[] tokens = subscriptionType.split("},");
				Map<String, String> map = new HashMap<String, String>();
				for (int i = 0; i < tokens.length; i++)
				{
					String[] strings = tokens[i].split(",");
					if (strings.length == 2)
						map.put(strings[0].replace("{", ""),
								strings[1].replace("}", ""));
				}

				for (Map.Entry<String, String> entry : map.entrySet()) {

					crmProperty = (SubscriptionPoolProperty) XBeans.instantiate(SubscriptionPoolProperty.class, ctx);
					crmProperty.setInitialPoolBalance(Long.parseLong(entry.getKey().trim()));
					crmProperty.setSubscriptionType(Long.parseLong(entry.getValue().trim()));

					crmPoolProperties.put(crmProperty.getSubscriptionType(),crmProperty);

				}
				poolExtension.setSubscriptionPoolProperties(crmPoolProperties);

			}else
			{

				RmiApiErrorHandlingSupport.validateMandatoryObject
				  (null, SUBSCRIPTIONPOOLS); //Exception
			}

			try {
				holder = (AccountExtensionHolder) XBeans.instantiate(AccountExtensionHolder.class, ctx);
			} catch (Exception e) {
				new MinorLogMsg(AccountsApiSupport.class,"Error instantiating new account extension holder.  Using default constructor.",e).log(ctx);
				holder = new AccountExtensionHolder();
			}

			holder.setExtension(poolExtension);

			accountExtensions.add(holder);
			account.setAccountExtensions(accountExtensions);

		}


        private GenericResponse validateOnly(Context ctx, String accountID,
                 ConvertAccountGroupTypeRequest request) throws CRMExceptionFault
        {
            
            genericResponse = new GenericResponse();
            try
            {
                AccountMoveRequest accRequest = (AccountMoveRequest) request;
                new MoveManager().validate(ctx, accRequest);
                genericResponse.setStatusCode(STATUS_CODE_CONVERT_IS_VALID);
                genericResponse.setStatusMessage(STATUS_MESSAGE_CONVERT_IS_VALID);
                return genericResponse;
                
            } 
            catch (Exception e) 
            {
                if(e instanceof CompoundMoveIllegalSateException)
                {
                    CompoundMoveIllegalSateException moveExce = (CompoundMoveIllegalSateException) e;
                    genericResponse.setStatusCode(moveExce.getErrorCode());
                    genericResponse.setStatusMessage(moveExce.getErrorMessage());
                }
                else
                {
                    genericResponse.setStatusCode(STATUS_CODE_CONVERT_IS_INVALID);
                    genericResponse.setStatusMessage(STATUS_MESSAGE_CONVERT_IS_INVALID);
                }
                 
                return genericResponse;
            }
        }

        
        // Actual convert logic goes here
        public GenericResponse convertAccount(Context ctx, ConvertAccountGroupTypeRequest request, Account account) throws AccountMoveException, CRMExceptionFault
        {
            genericResponse = new GenericResponse();
            GenericParameter[] responseGenericParameters = new GenericParameter[2];
            try
            {
                String newAccountBAN = null;
                new MoveManager().move(ctx, request);
                if(request.getNewBAN() != null)
                {
                    newAccountBAN = request.getNewBAN();
                }
                responseGenericParameters[0] = RmiApiSupport.createGenericParameter(OUT_PARAM_ORIGINAL_BAN_NAME, account.getBAN());
                responseGenericParameters[1] = RmiApiSupport.createGenericParameter(OUT_PARAM_NEW_GROUP_BAN_NAME, newAccountBAN);
                if(responseGenericParameters.length > 0)
                {
                    genericResponse.setParameters(responseGenericParameters);
                }
                genericResponse.setStatusCode(STATUS_CODE_CONVERT_SUCCESSFUL);
                genericResponse.setStatusMessage(STATUS_MESSAGE_CONVERT_SUCCESSFUL);
                    
                return genericResponse;
            }
            catch (final Exception e)
            {
                if(e instanceof CompoundMoveIllegalSateException)
                {
                    CompoundMoveIllegalSateException moveExce = (CompoundMoveIllegalSateException) e;
                    genericResponse.setStatusCode(moveExce.getErrorCode());
                    genericResponse.setStatusMessage(moveExce.getErrorMessage());
                }
                else
                {
                    genericResponse.setStatusCode(CompoundMoveIllegalSateException.DEFAULT_ERROR_CODE);
                    genericResponse.setStatusMessage("INTERNAL ERROR");
                }
            }
            
            return genericResponse;
        }
        
        
        @Override
        public boolean validateParameterTypes(Class<?>[] parameterTypes)
        {
            boolean result = true;
            result = result && (parameterTypes.length>=4);
            result = result && CRMRequestHeader.class.isAssignableFrom(parameterTypes[PARAM_HEADER]);
            result = result && String.class.isAssignableFrom(parameterTypes[PARAM_ACCOUNT_ID]);
            result = result && Boolean.class.isAssignableFrom(parameterTypes[PARAM_VALIDATE_ONLY]);
            result = result && Object[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
            return result;
        }

        @Override
        public boolean validateReturnType(Class<?> returnType)
        {
            return GenericResponse.class.isAssignableFrom(returnType);
        }

        @Override
        public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            Object[] result = null;
            if (isGenericExecution(ctx, parameters))
            {
                result = new Object[4];
                result[0] = parameters[0];
                result[1] = getAccountID(ctx, PARAM_ACCOUNT_ID, PARAM_ACCOUNT_ID_NAME, parameters);
                result[2] = parameters[2];
                result[3] = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);
            }
            else
            {
                result = parameters;
            }

            return result;
        }
        
	    public static final String METHOD_SIMPLE_NAME = "convertIndividualToGroup";
	    public static final String METHOD_NAME = "AccountService." + METHOD_SIMPLE_NAME;

	    public static final String PERMISSION = Constants.PERMISSION_ACCOUNTS_WRITE_CONVERTINDIVIDUALTOGROUP;
	    
	    //Input Parameter ID
	    public static final int PARAM_HEADER = 0;
	    public static final int PARAM_ACCOUNT_ID = 1;
	    public static final int PARAM_VALIDATE_ONLY = 2;
	    public static final int PARAM_GENERIC_PARAMETERS = 3;
	    
	    //Input Parameters
	    public static final String PARAM_ACCOUNT_ID_NAME = "accountID";
	    public static final String PARAM_VALIDATE_ONLY_NAME = "validateOnly";
	    public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
	    
	    //Output Parameters
	    public static final String OUT_PARAM_ORIGINAL_BAN_NAME = "OriginalBAN";
	    public static final String OUT_PARAM_NEW_GROUP_BAN_NAME = "NewGroupBAN";
	    
	    //Response Codes & Messages
	    public static final String STATUS_CODE_CONVERT_IS_VALID = "1000";
	    public static final String STATUS_MESSAGE_CONVERT_IS_VALID = "Conversion Is Valid";
	    
	    public static final String STATUS_CODE_CONVERT_IS_INVALID = "1001";
	    public static final String STATUS_MESSAGE_CONVERT_IS_INVALID = "Conversion Is Not Valid";
	    
	    public static final String STATUS_CODE_CONVERT_SUCCESSFUL = "0000";
	    public static final String STATUS_MESSAGE_CONVERT_SUCCESSFUL  = "Converted successfully";
	    
	    public static final String GROUP_TYPE = "groupType";
		public static final String QUOTA_TYPE = "quotaType";
		public static final String LIMITED_QUOTAVALUE = "limitedQuotaValue";
		public final static String SUBSCRIPTIONPOOLS = "subscriptionPools";
		public static final String BUNDLES = "bundles";

	    

	}
	
	/**
	 *
	 * @author isha.aderao
	 * @since 9.12
	 */
	
	public static class ConvertBillingTypeExecutor extends AbstractAccountQueryExecutor<ConvertBillingTypeResponse>
	{

		Context ctx = null; 
	    boolean validateOnly = false;
	    ConvertBillingTypeRequest convertRequest;
	    GenericParameter parameters = null;
	    ConvertBillingTypeResponse convertBillingTypeResponse = null;
	    String accountId = null;
	    Account account = null;
	    
	    public ConvertBillingTypeExecutor()
	    {
	        
	    }
	   
	    public ConvertBillingTypeExecutor(ConvertBillingTypeRequest convertRequest, boolean validateOnly, int operationType) 
	    {
	        this.convertRequest = convertRequest;
	        this.validateOnly = validateOnly;
	    }

	    /**
	     *  
	     * {@inheritDoc}
	     */
	    public ConvertBillingTypeResponse execute(Context ctx, Object... parameters) throws CRMExceptionFault
	    {
	        try
	        {
	        	convertRequest = getParameter(ctx, PARAM_CONVERT_REQUEST, PARAM_CONVERT_REQUEST_NAME, ConvertBillingTypeRequest.class, parameters);
	            Boolean validateOnly = getParameter(ctx, PARAM_VALIDATE_ONLY, PARAM_VALIDATE_ONLY_NAME, Boolean.class, parameters);

	            GenericParameter[] genericParameters = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);

	            CurrentAccountReference currentAcctRef = convertRequest.getCurrentAccountReference();
	            ConvertAccountReference convertAcctRef = convertRequest.getConvertAccountReference();
	            ConvertSubscriptionDetail convertSubDetail = convertRequest.getConvertSubscriptionDetail();
	            
	            RmiApiErrorHandlingSupport.validateMandatoryObject(convertSubDetail, "Subscription Detail");
	            
	            validateCurrentAccountReference(currentAcctRef);
	            accountId = currentAcctRef.getAccountID();
 	            this.account = getAccount(ctx, accountId);
 	            
 	            if(!(this.account.getState().equals(AccountStateEnum.ACTIVE)))
 	            {
 	            	throw new CRMExceptionFault("Account is not Active.");
 	            	    
 	            }
 	            if(!GroupTypeEnum.SUBSCRIBER.equals(this.account.getGroupType()))
 	            {
 	            	 RmiApiErrorHandlingSupport.simpleValidation("CurrentAccountReference", "Billing type conversion is supported only for Individual Accounts");
 	            }
	            ctx.put("MOVE_ACCOUNTID", accountId);
	            
	            validateConvertAccountReference(convertAcctRef);
	            
	            validateConvertSubscriptionDetails(convertSubDetail);
	            
	            MoveRequest request = MoveRequestSupport.getMoveRequest(ctx, this.account,
	                    ConvertAccountBillingTypeRequest.class);
	            
	            return validateAndConvert(ctx, request, currentAcctRef, convertAcctRef, convertSubDetail, genericParameters, validateOnly);
                          
	        }
	        catch (CRMExceptionFault e)
	        {
	        	LogSupport.minor(ctx, this, "CRMException occured while converting billing type:"+e.getLocalizedMessage() );
	            throw e;
	           
	        }catch(Exception e)
	        {
	        	LogSupport.minor(ctx, this, "Exception occured while converting billing type:"+e.getLocalizedMessage() );
	        	throw new CRMExceptionFault(e);
	        }
	        
	    } 
	    
		private void validateCurrentAccountReference(CurrentAccountReference currentAcctReference) throws CRMExceptionFault
	    {
	    	RmiApiErrorHandlingSupport.validateMandatoryObject(currentAcctReference, "Current Account Reference");
	    	RmiApiErrorHandlingSupport.validateMandatoryObject(currentAcctReference.getAccountID(),"AccountId");
	    	RmiApiErrorHandlingSupport.validateMandatoryObject(currentAcctReference.getSpid(),"SPID for Current Account");
	    }
	    
	    private void validateConvertAccountReference(ConvertAccountReference convertAcctRef) throws CRMExceptionFault
	    {

	    	 RmiApiErrorHandlingSupport.validateMandatoryObject(convertAcctRef, "Convert Account Reference");
	    	 RmiApiErrorHandlingSupport.validateMandatoryObject(convertAcctRef.getBillCycle(),"Bill Cycle");
	    	
	    	 SubscriberTypeEnum subType = this.account.getSystemType();
	    	 
	    	 if(subType == SubscriberTypeEnum.PREPAID) 
	    	 {	//Current Account is Prepaid. Validation if all fields mandatory for a postpaid account are populated.
	    		 
	    		 RmiApiErrorHandlingSupport.validateMandatoryObject(convertAcctRef.getContact(), "Convert Account Reference: Contact Information");
	    		 RmiApiErrorHandlingSupport.validateMandatoryObject(convertAcctRef.getContact().getFirstName(), "Contact Information: First Name");
	    		 RmiApiErrorHandlingSupport.validateMandatoryObject(convertAcctRef.getContact().getLastName(), "Contact Information: Last Name");
	    		 RmiApiErrorHandlingSupport.validateMandatoryObject(convertAcctRef.getContact().getAddress1(), "Contact Information: Address");
	    		 RmiApiErrorHandlingSupport.validateMandatoryObject(convertAcctRef.getContact().getCity(), "Contact Information: City");
	    		 RmiApiErrorHandlingSupport.validateMandatoryObject(convertAcctRef.getContact().getCountry(), "Contact Information: Country");
	    		 RmiApiErrorHandlingSupport.validateMandatoryObject(convertAcctRef.getContact().getContactNumber(), "Contact Information: Contact Number");
	    		 RmiApiErrorHandlingSupport.validateMandatoryObject(convertAcctRef.getContact().getContactName(), "Contact Information: Contact Name");
	    		 RmiApiErrorHandlingSupport.validateMandatoryObject(convertAcctRef.getIdentification(), "Convert Account Reference:Identification");
	    		 RmiApiErrorHandlingSupport.validateMandatoryObject(convertAcctRef.getIdentification().getDateOfBirth(), "Identification: Date of Birth");
	    		 RmiApiErrorHandlingSupport.validateMandatoryObject(convertAcctRef.getIdentification().getSecurityQuestion(), "Identification: Security Question and Answers");
	    		 RmiApiErrorHandlingSupport.validateMandatoryObject(convertAcctRef.getIdentification().getOccupationID(), "Identification: Occupation");
	    	 }
	    }
	    
	    private void validateConvertSubscriptionDetails(ConvertSubscriptionDetail convertSubDetail) throws CRMExceptionFault
	    {
	    	RmiApiErrorHandlingSupport.validateMandatoryObject(convertSubDetail.getPriceplanDetails(), "Convert Subscription Detail: Price Plan Details");
			Long priceplanId = convertSubDetail.getPriceplanDetails().getPriceplanId();
			RmiApiErrorHandlingSupport.validateMandatoryObject(priceplanId, "Convert Subscription Detail: Price plan Id");
			RmiApiErrorHandlingSupport.validateMandatoryObject(convertSubDetail.getBilling(), "Convert Subscription Detail: Billing Information");
	    	
	    	SubscriberTypeEnum subType = this.account.getSystemType();
	    	
	    	if(subType == SubscriberTypeEnum.POSTPAID) 
	    	{
	    		RmiApiErrorHandlingSupport.validateMandatoryObject(convertSubDetail.getBilling().getInitialBalance(), "Convert Subscription Detail: Initial Balance");
	    	}
	    	else if(subType == SubscriberTypeEnum.PREPAID)
	    	{
	    		RmiApiErrorHandlingSupport.validateMandatoryObject(convertSubDetail.getBilling().getCreditLimit(), "Convert Subscription Detail: Credit Limit");
				RmiApiErrorHandlingSupport.validateMandatoryObject(convertSubDetail.getBilling().getDeposit(), "Convert Subscription Detail: Deposit");
	    	}
	    }
	    
	    
	   @SuppressWarnings("finally")
	   private ConvertBillingTypeResponse validateAndConvert(Context ctx, MoveRequest request,
			   CurrentAccountReference currentAcctRef, ConvertAccountReference convertAcctRef, ConvertSubscriptionDetail convertSubDetail, 
	           GenericParameter[] genericParameters, boolean validateOnly) throws CRMExceptionFault
	    {
		   convertBillingTypeResponse = new ConvertBillingTypeResponse();
		   try
		   {
			   if (request instanceof com.redknee.app.crm.move.request.ConvertAccountBillingTypeRequest)
			   {
				   ConvertAccountBillingTypeRequest accRequest = (com.redknee.app.crm.move.request.ConvertAccountBillingTypeRequest) request;
				   
				   accRequest.setBillCycleID(convertAcctRef.getBillCycle());
				  
				   AccountContactInfo contactInfo = convertAcctRef.getContact();
				   if(contactInfo != null) //Only used for Prepaid to Postpaid Conversion.
				   {
					   accRequest.setBillingAddress1(contactInfo.getAddress1());
					   if(contactInfo.getAddress2() !=null)
						   accRequest.setBillingAddress2(contactInfo.getAddress2());
					   else 
						   accRequest.setBillingAddress2("");
					   
					   if(contactInfo.getAddress3() !=null)
						    accRequest.setBillingAddress3(contactInfo.getAddress3());
					   else
						   	accRequest.setBillingAddress3("");
					   
					   accRequest.setBillingCity(contactInfo.getCity());
					   accRequest.setBillingCountry(contactInfo.getCountry());
					   accRequest.setContactName(contactInfo.getContactName());
					   accRequest.setContactTel(contactInfo.getContactNumber());
					   accRequest.setFirstName(contactInfo.getFirstName());
					   accRequest.setLastName(contactInfo.getLastName());
				   }
				   
				   accRequest.setExistingBAN(accountId);
				   
				   //Only used for Prepaid to Postpaid Conversion
				   if(convertAcctRef.getIdentification() != null)
				   {
					   AccountsApiSupport.fillInIdentificationForAccountConvert(ctx, accRequest, convertAcctRef.getIdentification());
					   RmiApiSupport.setOptional(accRequest, ConvertAccountBillingTypeRequestXInfo.OCCUPATION, convertAcctRef.getIdentification().getOccupationID());
					   RmiApiSupport.setOptional(accRequest, ConvertAccountBillingTypeRequestXInfo.DATE_OF_BIRTH, convertAcctRef.getIdentification().getDateOfBirth());
					   
				   }
				   
				   accRequest.setOldBAN(accountId);
				   
				  // if(convertAcctRef.getProfile() != null)
				   {
					   accRequest.setSpid(account.getSpid());
				   }
		  
				   if(account.getSystemType().equals(SubscriberTypeEnum.PREPAID))
				   {
					   accRequest.setSystemType(SubscriberTypeEnum.POSTPAID);
				   }
				   else if(account.getSystemType().equals(SubscriberTypeEnum.POSTPAID))
				   { 
					   accRequest.setSystemType(SubscriberTypeEnum.PREPAID);
				   }
				   else
				   {
					   accRequest.setSystemType(SubscriberTypeEnum.HYBRID);
					   throw new CRMExceptionFault("Conversion of Hybrid Account is not Supported.");
				   }
				   
				   GenericParameterParser parser = new GenericParameterParser(genericParameters);
		           
		           if(parser != null && parser.containsParam(APIGenericParameterSupport.SUBSCRIPTION_CLASS))
		           {
		           		Long subscriptionClass =  parser.getParameter(APIGenericParameterSupport.SUBSCRIPTION_CLASS, Long.class);
		           		if(subscriptionClass != null)
				        {
		           			accRequest.setSubscriptionClass(subscriptionClass.longValue());
				        }
	               }
			        
				   if(convertSubDetail == null)
				   {
					   throw new CRMExceptionFault("Subscription Details are missing.");
				   }

				   Collection <Subscriber> subList= account.getSubscribers(ctx);
				   if(subList.size() == 0)
					   throw new CRMExceptionFault("Account does not have any immediate ACTIVE Subscription.");
				   if(subList.size() > 1)
					   throw new CRMExceptionFault("Can not convert Billing type for Account with multiple subscriptions");
				   
				   //  subscribers under current account
				   Iterator<Subscriber> it = subList.iterator();
				   
				   if(it.hasNext()) //Pick the first and only subscriber, since as of now,
					   				//move is applicable for individual account with single subscription
				   {
					   Subscriber sub = (Subscriber) it.next();
					   
					   if((convertSubDetail.getOriginalIdentifier()!=null && !convertSubDetail.getOriginalIdentifier().equals(sub.getId()))
							   || (convertSubDetail.getMsisdn()!=null && !convertSubDetail.getMsisdn().equals(sub.getMsisdn())))
					   { // fields are added for future support
						   throw new CRMExceptionFault("Subscriber with requested Identifier / MSISDN not found. Send correct values or empty for default subscriber");
					   }
					   if(!sub.getState().equals(SubscriberStateEnum.ACTIVE))
					   {
						   throw new CRMExceptionFault("Subscription [ID = "+ sub.getId()+ "is not in ACTIVE state.");
					   }
					   
					   ctx.put(Subscriber.class, sub);
					   ctx.put(Common.BILLING_TYPE_CONVERSION, sub); // Need to put subscriber in context to avoid failure in move pipeline.

					   accRequest.setPricePlan(convertSubDetail.getPriceplanDetails().getPriceplanId());

					   if(accRequest.getSystemType().equals(SubscriberTypeEnum.PREPAID))
					   {   
						   accRequest.setInitialAmount(convertSubDetail.getBilling().getInitialBalance());
					   }
					   else
					   {
						   accRequest.setNewCreditLimit(convertSubDetail.getBilling().getCreditLimit());
						   accRequest.setNewDepositAmount(convertSubDetail.getBilling().getDeposit());
					   }
				   }
				   
				   if(validateOnly)
				   {
					   new MoveManager().validate(ctx, accRequest);
					   convertBillingTypeResponse.setResultCode(0);
					   return convertBillingTypeResponse;
				   }
				   else
				   {
					   new MoveManager().validate(ctx, accRequest);
					   new MoveManager().move(ctx, accRequest);
				   }

				   //Format Response Object.
				   String newBan = accRequest.getNewBAN();
				   LogSupport.info(ctx, this, "Convert Billing Type Executor : new BAN:"+newBan);

				   convertBillingTypeResponse.setConvertedBan(newBan);
				   Collection<Subscriber> newSubList = accRequest.getNewAccount(ctx).getSubscribers(ctx);
				   Iterator<Subscriber > it1 = newSubList.iterator();
				   
				   SubscriptionReference subRef = new SubscriptionReference();
				   SubscriptionReference [] subRefList = new SubscriptionReference[newSubList.size()];
				   
				   int count = 0;
				   // Converted subscriber's data, as of now single subscriber
				   while (it1.hasNext())
				   {
					   Subscriber sub = (Subscriber) it1.next();
					   subRef.setAccountID(newBan);
					   subRef.setIdentifier(sub.getId());
					   subRef.setSpid(sub.getSpid());
					   subRef.setMobileNumber(sub.getMsisdn());
					   subRef.setSubscriptionType(((Long)sub.getSubscriptionType()).intValue());
					   subRef.setState(SubscriptionStateEnum.valueOf(sub.getStateWithExpired().getIndex()));
					   subRefList[count] = subRef;
					   count++;
				   }
				   convertBillingTypeResponse.setConvertedSubscriptionRef(subRefList);
				   convertBillingTypeResponse.setResultCode(STATUS_CODE_CONVERT_SUCCESS);
				   convertBillingTypeResponse.setResultMessage(STATUS_MESSAGE_CONVERT_SUCCESS);
			   }

		   }
		   catch (CRMExceptionFault e)
		   {
	        	LogSupport.minor(ctx, this, "Convert Billing Type Failed: CRMException occured"+e.getLocalizedMessage());
	           	convertBillingTypeResponse.setResultCode(STATUS_CODE_CONVERT_FAILED);
	        	convertBillingTypeResponse.setResultMessage(STATUS_MESSAGE_CONVERT_FAILED+":"+e.getLocalizedMessage());
	        	throw e;
	        	
		   }
		   catch(Exception e)
		   {
			   	LogSupport.minor(ctx, this, "Convert Billing Type Failed: Exception occured"+e.getLocalizedMessage());
			   	convertBillingTypeResponse.setResultCode(STATUS_CODE_CONVERT_FAILED);
	        	convertBillingTypeResponse.setResultMessage(STATUS_MESSAGE_CONVERT_FAILED+":"+e.getLocalizedMessage());
		   }
		   finally
		   {
			   return convertBillingTypeResponse;
		   }
	    }

	    @Override
	    public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
	    {
	        Object[] result = null;
	        if (isGenericExecution(ctx, parameters))
	        {
	            result = new Object[4];
	        }
	        else
	        {
	            result = parameters;
	        }

	        return result;
	    }

	    @Override
	    public boolean validateParameterTypes(Class<?>[] parameterTypes)
	    {
	        boolean result = true;
	        result = result && (parameterTypes.length>=4);
	        result = result && CRMRequestHeader.class.isAssignableFrom(parameterTypes[PARAM_HEADER]);
            result = result && Boolean.class.isAssignableFrom(parameterTypes[PARAM_VALIDATE_ONLY]);
            result = result && String.class.isAssignableFrom(parameterTypes[PARAM_CONVERT_REQUEST]);
	        result = result && Object[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
	        return result;
	    }

	    @Override
	    public boolean validateReturnType(Class<?> resultType)
	    {
	        return SuccessCode.class.isAssignableFrom(resultType);

	    }

	    public static final String METHOD_SIMPLE_NAME = "convertBillingType";
        public static final String METHOD_NAME = "AccountService." + METHOD_SIMPLE_NAME;

        public static final String PERMISSION = Constants.PERMISSION_ACCOUNTS_WRITE_CONVERTBILLINGTYPE;

        public static final int PARAM_HEADER = 0;
        public static final int PARAM_VALIDATE_ONLY = 1;
        public static final int PARAM_CONVERT_REQUEST = 2;
        public static final int PARAM_GENERIC_PARAMETERS = 3;

        public static final String PARAM_CONVERT_REQUEST_NAME = "request";
        public static final String PARAM_VALIDATE_ONLY_NAME = "validateOnly";
        public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";

        public static final int STATUS_CODE_CONVERT_SUCCESS = 0;
        public static final int STATUS_CODE_CONVERT_FAILED = 1;
        public static final String STATUS_MESSAGE_CONVERT_SUCCESS = "Billing Type Converted Successfully.";
        public static final String STATUS_MESSAGE_CONVERT_FAILED = "Billing Type Conversion Failed.";
 
	}

	public static class GetAccountProfileWithServiceAddress
			extends AbstractAccountQueryExecutor<AccountProfileWithServiceAddressQueryResults> {

		public GetAccountProfileWithServiceAddress() {

		}

		public AccountProfileWithServiceAddressQueryResults execute(Context ctx, Object... parameters)
				throws CRMExceptionFault {
			String accountID = getAccountID(ctx, PARAM_ACCOUNT_ID, PARAM_ACCOUNT_ID_NAME, parameters);
			GenericParameter[] genericParameters = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS,
					PARAM_GENERIC_PARAMETERS_NAME, parameters);

			RmiApiErrorHandlingSupport.validateMandatoryObject(accountID, PARAM_ACCOUNT_ID_NAME);

			final Account account = getAccount(ctx, accountID);

			final AccountProfileWithServiceAddressQueryResults result = new AccountProfileWithServiceAddressQueryResults();
			try {
				result.setProfile(AccountsApiSupport.extractProfile(account));
				result.setDetail(AccountsApiSupport.extractDetail(account));
				result.setBilling(AccountsApiSupport.extractBilling(account));
				result.setPaymentInfo(AccountsApiSupport.extractPaymentInfo(ctx, account));
				result.setIdentification(AccountsApiSupport.extractIdentification(ctx, account));
				result.setCompany(AccountsApiSupport.extractCompany(account));
				result.setBank(AccountsApiSupport.extractBank(account));
				result.setContact(AccountsApiSupport.extractContact(account));
				prepareServiceAddresses(result,
						AccountsApiSupport.getServiceAddresses(getAddressPerAccount(ctx, accountID, this)));
			} catch (final Exception e) {
				final String msg = "Unable to retreive Account for accountID : " + accountID;
				RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
			}

			return result;
		}

		@Override
		public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault {
			Object[] result = null;
			if (isGenericExecution(ctx, parameters)) {
				result = new Object[3];
				result[0] = parameters[0];
				result[1] = getAccountID(ctx, PARAM_ACCOUNT_ID, PARAM_ACCOUNT_ID_NAME, parameters);
				result[2] = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME,
						parameters);
			} else {
				result = parameters;
			}

			return result;
		}

		@Override
		public boolean validateParameterTypes(Class<?>[] parameterTypes) {
			boolean result = true;
			result = result && (parameterTypes.length >= 3);
			result = result && CRMRequestHeader.class.isAssignableFrom(parameterTypes[PARAM_HEADER]);
			result = result && String.class.isAssignableFrom(parameterTypes[PARAM_ACCOUNT_ID]);
			result = result && Object[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
			return result;
		}

		@Override
		public boolean validateReturnType(Class<?> resultType) {
			return AccountProfileWithServiceAddressQueryResults.class.isAssignableFrom(resultType);

		}

		public static final String METHOD_SIMPLE_NAME = "getAccountProfileV2";
		public static final String METHOD_NAME = "AccountService." + METHOD_SIMPLE_NAME;

		public static final String PERMISSION = Constants.PERMISSION_ACCOUNTS_READ_GETACCOUNTPROFILEV2;

		public static final int PARAM_HEADER = 0;
		public static final int PARAM_ACCOUNT_ID = 1;
		public static final int PARAM_GENERIC_PARAMETERS = 2;

		public static final String PARAM_ACCOUNT_ID_NAME = "accountID";
		public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";

		private void prepareServiceAddresses(AccountProfileWithServiceAddressQueryResults result,
				List<ServiceAddressOutput> serviceAddresses) {
			if (null != serviceAddresses) {
				for (ServiceAddressOutput serviceAddress : serviceAddresses) {
					result.addAddress(serviceAddress);
				}
			}
		}

		private Collection<Address> getAddressPerAccount(final Context ctx, final String accountID, final Object caller)
				throws CRMExceptionFault {
			Collection<Address> addresses = null;

			try {
				And condition = new And();
				condition.add(new EQ(AddressXInfo.BAN, accountID));
				condition.add(new EQ(AddressXInfo.ADDRESS_TYPE, AddressTypeEnum.SERVICE_ADDRESS_INDEX));
				addresses = HomeSupportHelper.get(ctx).getBeans(ctx, Address.class, condition);
			} catch (final Exception e) {
				final String msg = "Unable to retrieve Address for " + accountID;
				RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, caller);
			}

			if (null == addresses || addresses.isEmpty()) {
				addresses = Collections.EMPTY_LIST;
			}

			return addresses;
		}
	}
}