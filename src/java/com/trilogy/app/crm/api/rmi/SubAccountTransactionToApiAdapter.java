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
package com.trilogy.app.crm.api.rmi;

import java.util.Date;
import java.util.Map;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;


import com.trilogy.app.crm.api.queryexecutor.transaction.TransactionQueryExecutors;
import com.trilogy.app.crm.api.queryexecutor.transaction.TransactionShowTaxHelper;
import com.trilogy.app.crm.api.rmi.support.APIGenericParameterSupport;
import com.trilogy.app.crm.api.rmi.support.RmiApiErrorHandlingSupport;
import com.trilogy.app.crm.api.rmi.support.RmiApiSupport;
import com.trilogy.app.crm.bean.AdjustmentTypeXInfo;
import com.trilogy.app.crm.bean.OcgGenericParameterHolder;
import com.trilogy.app.crm.bean.ReasonCode;
import com.trilogy.app.crm.bean.ReasonCodeXInfo;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.TransactionMethod;
import com.trilogy.app.crm.bean.TransactionMethodXInfo;
import com.trilogy.app.crm.bean.TransactionXInfo;
import com.trilogy.app.crm.bean.core.AdjustmentType;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.OcgAdj2CrmAdjSupport;
import com.trilogy.app.crm.support.VRASupport;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.util.crmapi.wsdl.v2_1.types.transaction.ProfileType;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;
//import com.trilogy.util.crmapi.wsdl.v3_0.types.transaction.BillingEntityReference;
//import com.trilogy.util.crmapi.wsdl.v3_0.types.transaction.BillingEntityState;
import com.trilogy.util.crmapi.wsdl.v3_0.types.transaction.SubAccountTransactionReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.transaction.Transaction;
import com.trilogy.util.crmapi.wsdl.v3_0.types.transaction.TransactionReference;
//import com.trilogy.util.crmapi.wsdl.v3_0.types.transaction.TransactionReferenceV2;
import com.trilogy.util.crmapi.wsdl.v3_0.types.transaction.TransactionRequest;
//import com.trilogy.util.crmapi.wsdl.v3_0.types.transaction.TransactionV2;


/**
 * Adapts Transaction object to API objects.
 *
 * @author victor.stratan@redknee.com
 */
public class SubAccountTransactionToApiAdapter implements Adapter
{
    private static final long serialVersionUID = 1L;

    @Override
    public Object adapt(final Context ctx, final Object obj) throws HomeException
    {
		return adaptTransactionToReference(ctx,
		    (com.redknee.app.crm.bean.Transaction) obj);
    }

    @Override
    public Object unAdapt(final Context ctx, final Object obj) throws HomeException
    {
        throw new UnsupportedOperationException();
    }

    public static SubAccountTransactionReference adaptTransactionToApi(Context ctx,
    	    final com.redknee.app.crm.bean.Transaction transaction, ProfileType ownerType)
        {
            final SubAccountTransactionReference reference = adaptTransactionToApi(ctx, transaction);
            reference.addParameters(com.redknee.app.crm.api.rmi.support.APIGenericParameterSupport.getTransactionOwnerTypeParameter(ownerType));

            return reference;
        }

        
	public static SubAccountTransactionReference adaptTransactionToApi(Context ctx, final com.redknee.app.crm.bean.Transaction transaction) {
		final SubAccountTransactionReference reference = new SubAccountTransactionReference();
		fillTransaction(ctx, reference, transaction);
		return reference;
	}

	/*public static TransactionV2 adaptTransactionToTransactionV2Api(Context ctx,
			final com.redknee.app.crm.bean.Transaction transaction, ProfileType ownerType) {
		final TransactionV2 reference = new TransactionV2();
		fillTransaction(ctx, reference, transaction);
		reference.addParameters(com.redknee.app.crm.api.rmi.support.APIGenericParameterSupport
				.getTransactionOwnerTypeParameter(ownerType));

		return reference;
	}*/

	private static void fillTransaction(Context ctx, final /*Transaction*/SubAccountTransactionReference reference,
			final com.redknee.app.crm.bean.Transaction transaction) {
		adaptTransactionToReference(ctx, transaction, reference);
//		reference.setMobileNumber(transaction.getMSISDN());
//		reference.setPaymentAgency(transaction.getPaymentAgency());
//		reference.setLocationCode(transaction.getLocationCode());
//		reference.setExternalTransactionNumber(transaction.getExtTransactionId());
//
//		reference.setPaymentDetails(transaction.getPaymentDetails());
//		reference.setCsrInput(transaction.getCSRInput());
//		reference.setGlCode(transaction.getGLCode());
//		reference.setReasonCode(transaction.getReasonCode());
//		reference.setTransactionMethodID(transaction.getTransactionMethod());
//		reference.setSupportedSubscriptionID(transaction.getSupportedSubscriberID());
//		reference.setSubscriptionType(Long.valueOf(transaction.getSubscriptionTypeId()).intValue());
//		reference.setTaxPaid(transaction.getTaxPaid());
		

		fillInTransactionGenericParameters(ctx, transaction, reference);
	}

/*   public static BillingEntityReference adaptTransactionToApi(Context ctx,
            final com.redknee.app.crm.bean.BillingEntity billingEntity)
    {
        final BillingEntityReference reference = new BillingEntityReference();
        
        reference.setAccountID(billingEntity.getBAN());
        reference.setBillingEntityId(billingEntity.getBillingEntityId());
        reference.setCreationDate(CalendarSupportHelper.get().dateToCalendar(new Date(billingEntity.getCreationDate())));
        reference.setInvoiceDate(CalendarSupportHelper.get().dateToCalendar(new Date(billingEntity.getInvoiceDate())));
        reference.setName(billingEntity.getBillingEntityName());
        reference.setPriority(billingEntity.getPriority());
        reference.setState(BillingEntityState.Factory.fromValue(billingEntity.getState().getIndex()));
        reference.setSubscriptionID(billingEntity.getSubscriptionID());
        reference.setTotalDueAmount(billingEntity.getAmount());
        reference.setTotalPaidAmount(billingEntity.getPaidAmount());
        reference.setTotalTaxAmount(billingEntity.getTaxAmount());
        reference.setTotalTaxPaidAmount(billingEntity.getTaxPaidAmount());
        reference.setUserStartDate(CalendarSupportHelper.get().dateToCalendar(new Date(billingEntity.getUsageStartDate())));
        
        return reference;
    }
*/    

/*    public static com.redknee.app.crm.bean.Transaction adaptTransactionRequestToTransaction(
            final Context context, final TransactionRequest request, final Subscriber subscriber, final Map<String, Object> parameters)
        throws CRMExceptionFault, HomeException 
    {

        final com.redknee.app.crm.bean.Transaction transaction = adaptTransactionRequestToTransaction(context, request);
        //Get generic parameter values
        final String originatingApplication = (String)parameters.get(APIGenericParameterSupport.ORIGINATING_APPLICATION); 
        final Boolean exemptFromCreditLimitCheck = (Boolean)parameters.get(APIGenericParameterSupport.EXEMPT_FROM_CREDIT_LIMIT_CHECK);
        final Long resultingBalance = (Long)parameters.get(APIGenericParameterSupport.RESULTING_BALANCE);
        final String ocgAdjustmentTypeOverride = (String)parameters.get(APIGenericParameterSupport.OCG_ADJUSTMENTTYPE_OVERRIDE);
        final Integer vraEventType = (Integer)parameters.get(APIGenericParameterSupport.VRA_EVENT_TYPE);
        final Long voucherValue = (Long)parameters.get(APIGenericParameterSupport.VRA_VOUCHER);
        final String tigoInformation = (String)parameters.get(APIGenericParameterSupport.TIGO_INFORMATION);
        final Integer categoryId = (Integer) parameters.get(APIGenericParameterSupport.CATEGORY_ID);
        final Boolean secondaryBalance = (Boolean) parameters.get(APIGenericParameterSupport.SECONDARY_BALANCE);
        final Long taxAmount = (Long) parameters.get(APIGenericParameterSupport.TAX_AMOUNT);
        
        if(taxAmount != null){
        	transaction.setTaxPaid(taxAmount);
        }
        if (APIGenericParameterSupport.VRA.equals(originatingApplication))
        {
            //Default values of generic parameters
            int eventType = VRASupport.VRA_ER_EVENTTYPE_NORMAL;
            long voucherAmount = transaction.getAmount();
            
            if (vraEventType != null)
            {
                eventType = vraEventType.intValue();
            }
            if (voucherValue != null)
            {
                voucherAmount = voucherValue.longValue();
            }
            
            RmiApiSupport.setOptional(transaction, TransactionXInfo.FROM_VRAPOLLER, true);
            VRASupport.processVRAEventType(context, transaction, subscriber, eventType, transaction.getAmount(), voucherAmount);
        }
        else if (APIGenericParameterSupport.TFA.equals(originatingApplication))
        {
            RmiApiSupport.setOptional(transaction, TransactionXInfo.FROM_TFAPOLLER, true);
        }
        else if (APIGenericParameterSupport.OCG.equals(originatingApplication) || APIGenericParameterSupport.SELF_CARE.equals(originatingApplication))
        {
            //Set to true to skip Forward to OCG
            RmiApiSupport.setOptional(transaction, TransactionXInfo.FROM_VRAPOLLER, true);
            
            if (ocgAdjustmentTypeOverride != null && !"".equals(ocgAdjustmentTypeOverride))
            {
                final AdjustmentType adjType =
                    OcgAdj2CrmAdjSupport.mapOcgAdj2CrmAdjType(context, subscriber.getSpid(), ocgAdjustmentTypeOverride);
                RmiApiSupport.setOptional(transaction, TransactionXInfo.ADJUSTMENT_TYPE, adjType.getCode());
            }
            //Get transaction's adjustment type, in the case where original adjustment is correct and override isn't given
            if(VRASupport.isVRAEvent(context, transaction.getAdjustmentType(),subscriber.getSpid()))  //TT#13071727040
            {
                final int eventType = VRASupport.getVRAEventTypeFromAdjustmentType(context, transaction.getAdjustmentType(),subscriber.getSpid());
                final long voucherAmount = transaction.getAmount();
                VRASupport.processVRAEventType(context, transaction, subscriber, eventType, transaction.getAmount(),voucherAmount);
            }                  
        }
        else if (APIGenericParameterSupport.MVNE.equals(originatingApplication))
        {
        	OcgGenericParameterHolder paramHolder = new OcgGenericParameterHolder();
        	context.put(OcgGenericParameterHolder.class, paramHolder);
        	
        	if(tigoInformation != null)
        	{
        		Parameter infoParam = new Parameter();
        		infoParam.parameterID = ParameterID.TIGO_INFORMATION;
        		ParameterValue paramValue = new ParameterValue();
        		paramValue.stringValue(tigoInformation);
        		infoParam.value = paramValue;
        		paramHolder.addInputParameter(infoParam);
        	}
        }
        else if (originatingApplication != null)
        {
            new InfoLogMsg(SubAccountTransactionToApiAdapter.class, "Ignoring the generic parameter ("+
                    APIGenericParameterSupport.ORIGINATING_APPLICATION + "," + 
                    String.valueOf(originatingApplication) + ")", null).log(context);
        }
        
        if(secondaryBalance!=null && secondaryBalance)
        {
        	OcgGenericParameterHolder paramHolder = new OcgGenericParameterHolder();
        	context.put(OcgGenericParameterHolder.class, paramHolder);
        	
        	if(categoryId != null)
        	{
        		Parameter inParam = new Parameter();
        		ParameterValue paramValue = new ParameterValue();
        		inParam = new Parameter();
        		inParam.parameterID = ParameterID.CATEGORYID;
        		paramValue = new ParameterValue();
        		paramValue.intValue(categoryId.intValue());
        		inParam.value = paramValue;  
        		paramHolder.addInputParameter(inParam);
        		
        		if(LogSupport.isDebugEnabled(context))
        		{
        			LogSupport.debug(context, SubAccountTransactionToApiAdapter.class.getName(), "OCG In Params populated, Category ID:" + categoryId );
        		}
        		        		
        	}
        	else
        	{
        		LogSupport.info(context, SubAccountTransactionToApiAdapter.class.getName(), "Category ID is null eventhough, Secondary Balance flag is:" + secondaryBalance);
        	}
        }

        if (exemptFromCreditLimitCheck != null)
        {
            RmiApiSupport.setOptional(transaction, TransactionXInfo.EXEMPT_CREDIT_LIMIT_CHECKING, exemptFromCreditLimitCheck);
        }
        if (resultingBalance != null)
        {
            RmiApiSupport.setOptional(transaction, TransactionXInfo.BALANCE, resultingBalance);
        }
        if(request.getReceiptNumber() != null)
        {
            RmiApiSupport.setOptional(transaction, TransactionXInfo.UNIFIED_RECEIPT_ID, request.getReceiptNumber());
        }
        
        return transaction;
    }
    
    public static com.redknee.app.crm.bean.Transaction adaptTransactionRequestToTransaction(
            final Context context, final TransactionRequest request) 
        throws CRMExceptionFault, HomeException
    {
        com.redknee.app.crm.bean.Transaction transaction = null;
        try
        {
            transaction = (com.redknee.app.crm.bean.Transaction) XBeans.instantiate(com.redknee.app.crm.bean.Transaction.class, context);
        }
        catch (Exception e)
        {
            new MinorLogMsg(SubAccountTransactionToApiAdapter.class, "Error instantiating new transaction object.  Using default constructor.", e).log(context);
            transaction = new com.redknee.app.crm.bean.core.Transaction();
        }

        RmiApiErrorHandlingSupport.validateMandatoryObject(request.getAdjustmentType(), "Request.AdjustmentType");
        RmiApiErrorHandlingSupport.validateMandatoryObject(request.getAmount(), "Request.Amount");

        transaction.setAdjustmentType((int) request.getAdjustmentType());
        //validation purpose,if in request sending empty amount.
        if(request.getAmount() == Long.MIN_VALUE){
            final String msg = "Amount should not be empty,please provide valid amount";
            RmiApiErrorHandlingSupport.simpleValidation(
                          "Request.Amount", msg);

       }
        else
        {
        transaction.setAmount(request.getAmount());
        }
        if(request.getTransactionDate()!=null)
        {
        	transaction.setTransDate(request.getTransactionDate().getTime());
        }
        if (request.getSubscriptionType() != null)
        {
            transaction.setSubscriptionTypeId(request.getSubscriptionType().longValue());   
        }
        
        RmiApiSupport.setOptional(transaction, TransactionXInfo.EXPIRY_DAYS_EXT, request.getExpiryDateExtension());
        RmiApiSupport.setOptional(transaction, TransactionXInfo.PAYMENT_AGENCY, request.getPaymentAgency());
        RmiApiSupport.setOptional(transaction, TransactionXInfo.LOCATION_CODE, request.getLocationCode());
        RmiApiSupport.setOptional(transaction, TransactionXInfo.EXT_TRANSACTION_ID, request.getExternalTransactionNumber());
        RmiApiSupport.setOptional(transaction, TransactionXInfo.PAYMENT_DETAILS, request.getPaymentDetails());
        RmiApiSupport.setOptional(transaction, TransactionXInfo.TRANS_DATE, request.getTransactionDate());
        RmiApiSupport.setOptional(transaction, TransactionXInfo.CSRINPUT, request.getCsrInput());
        RmiApiSupport.setOptional(transaction, TransactionXInfo.REASON_CODE, request.getReasonCode());
        RmiApiSupport.setOptional(transaction, TransactionXInfo.TRANSACTION_METHOD, request.getTransactionMethodID());
        
        if(request.getReceiptNumber() != null )
        {
            RmiApiSupport.setOptional(transaction, TransactionXInfo.UNIFIED_RECEIPT_ID, request.getReceiptNumber());
        }
        
        return transaction;
    }
*/
	public static SubAccountTransactionReference adaptTransactionToReference(Context ctx,
            final com.redknee.app.crm.bean.Transaction transaction)
    {
        final SubAccountTransactionReference reference = new SubAccountTransactionReference();
		adaptTransactionToReference(ctx, transaction, reference);

        return reference;
    }
	
	//for creating listsubscriptionTransactionsV2 @abhijit
	
	/*public static TransactionReferenceV2 adaptTransactionToReferenceV2(Context ctx,
            final com.redknee.app.crm.bean.Transaction transaction)
    {
        final TransactionReferenceV2 reference = new TransactionReferenceV2();
        reference.setSubscriptionID(transaction.getSubscriberID());
		adaptTransactionToReference(ctx, transaction, reference);

        return reference;
    }*/
	
	

	public static SubAccountTransactionReference adaptTransactionToReference(Context ctx,
            final com.redknee.app.crm.bean.Transaction transaction, final SubAccountTransactionReference reference)
    {
	    reference.setIdentifier(transaction.getReceiptNum());
//        reference.setAccountID(transaction.getBAN());
        reference.setAdjustmentTypeID(Long.valueOf(transaction.getAdjustmentType()));
        reference.setAmount(transaction.getAmount());
        reference.setBalance(transaction.getBalance());
        reference.setAgent(transaction.getAgent());
        reference.setReceiveDate(CalendarSupportHelper.get().dateToCalendar(transaction.getReceiveDate()));
        reference.setTransactionDate(CalendarSupportHelper.get().dateToCalendar(transaction.getTransDate()));
        reference.setCsrInput(transaction.getCSRInput());
        reference.setSubscriptionType((int)transaction.getSubscriptionTypeId());
        //assuming all receipt numbers will be greater than 0
        if (!transaction.getUnifiedReceiptID().isEmpty() )
        {
            reference.setReceiptNumber(transaction.getUnifiedReceiptID());
            
        }
        
        /*fillInTransactionReferenceGenericParametersForOcg(ctx, transaction, reference);*/
        
		fillInTransactionReferenceGenericParameters(ctx,
		    transaction.getAdjustmentType(), reference);
		
		fillInTransactionReferenceGenericParameters(ctx,
			    transaction.getExpDate(), reference);
        return reference;
    }


	/**
	 * This method will fill in the Generic Parameters coming back from OCG into the TransactionReference object.
	 * @param ctx
	 * @param transaction
	 * @param reference
	 */
/*    private static void fillInTransactionReferenceGenericParametersForOcg(
			Context ctx, com.redknee.app.crm.bean.Transaction transaction,
			SubAccountTransactionReference reference) 
    {
    	OcgGenericParameterHolder paramHolder = (OcgGenericParameterHolder) ctx.get(OcgGenericParameterHolder.class);
    	if(paramHolder != null)
    	{
    		Parameter authParam = paramHolder.getOutputParameter(ParameterID.TIGO_AUTHORIZATION);
    		if(authParam != null)
    		{
    			String authorizationCode = authParam.value.stringValue();
    			reference.addParameters(APIGenericParameterSupport
    					.getTransactionRNTigoAuthorizationCode(ctx, authorizationCode));
    		}
    		
    		Parameter statusParam = paramHolder.getOutputParameter(ParameterID.TIGO_STATUS);
    		if(statusParam != null)
    		{
    			int statusCode = statusParam.value.intValue();
    			reference.addParameters(APIGenericParameterSupport
    					.getTransactionRNTigoStatus(ctx, statusCode));
    		}
    		
    		Parameter tigoTransactionId = paramHolder.getOutputParameter(ParameterID.TIGO_TRANSACTION_ID);
    		if(tigoTransactionId != null)
    		{
    			String tigoTxnIdValue = tigoTransactionId.value.stringValue();
    			reference.addParameters(APIGenericParameterSupport
    					.getTransactionRNTigoTransactionId(ctx, tigoTxnIdValue));
    		}
    		
    		
    	}
    }

*/	public static SubAccountTransactionReference adaptTransactionToReference(final Context ctx, final long identifier,
            final TransactionRequest request, SubAccountTransactionReference reference, final String accountGuid, long code,
            final Date transactionDate, final String user)
    {
        reference.setIdentifier(identifier);
//        reference.setAccountID(accountGuid);
        reference.setAdjustmentTypeID(Long.valueOf(code));
        reference.setAmount(request.getAmount());
        reference.setAgent(user);
        reference.setReceiveDate(CalendarSupportHelper.get().dateToCalendar(transactionDate));
        reference.setTransactionDate(CalendarSupportHelper.get().dateToCalendar(transactionDate));
        reference.setCsrInput(request.getCsrInput());
        
        //assuming all receipt numbers will be greater than 0
        if (request.getReceiptNumber() != null)
        {
            reference.setReceiptNumber(request.getReceiptNumber());
            
        }
		fillInTransactionReferenceGenericParameters(ctx, (int) code, reference);
        return reference;
    }

	protected static void fillInTransactionReferenceGenericParameters(
	    Context ctx, final int code, final SubAccountTransactionReference reference)
	{
		try
		{
			AdjustmentType adjustmentType =
			    HomeSupportHelper.get(ctx).findBean(ctx, AdjustmentType.class,
			        new EQ(AdjustmentTypeXInfo.CODE, Integer.valueOf(code)));
			if (adjustmentType == null)
			{
				LogSupport.minor(ctx, SubAccountTransactionToApiAdapter.class,
				    "Cannot find Adjustment Type " + code);
			}
			else
			{
				reference.addParameters(APIGenericParameterSupport
				    .getTransactionRNAdjustmentTypeID(ctx, adjustmentType));
			}
		}
		catch (HomeException e)
		{
			LogSupport.minor(ctx, SubAccountTransactionToApiAdapter.class,
			    "Lookup of Adjustment Type falied.", e);
		}
	}

	protected static void fillInTransactionReferenceGenericParameters(Context ctx, final String expDate,
			final SubAccountTransactionReference reference) {

		try {

			final String EXPIRY_DATE_FORMAT = "yyyyMMdd";
			DateFormat expDateFormat = new SimpleDateFormat(EXPIRY_DATE_FORMAT);
			if (expDate != null && !expDate.isEmpty()) {
				try {
					Date expDateTmp = expDateFormat.parse(expDate);
					reference.addParameters(
							APIGenericParameterSupport.getSubscriptionExpiryDateParameter(ctx, expDateTmp));
					LogSupport.debug(ctx, "TransactionToApiAdapter::fillInTransactionReferenceGenericParameters",
							"Expiry date [ " + expDate + " ] set in the response for the webapi call");

				} catch (ParseException ex) {
					if (LogSupport.isDebugEnabled(ctx)) {
						LogSupport.debug(ctx, "TransactionToApiAdapter::fillInTransactionReferenceGenericParameters",
								"Failed to parse the expiry date" + expDate);
					}
				}
			}
		} catch (Exception ex) {
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(ctx, "TransactionToApiAdapter::fillInTransactionReferenceGenericParameters",
						"Exception occured " + ex);
			}
		}
	}	
	
	protected static void fillInTransactionGenericParameters(Context ctx,
	    final com.redknee.app.crm.bean.Transaction transaction,
	    final SubAccountTransactionReference reference)
	{
		try
		{
			ReasonCode reasonCode =
			    HomeSupportHelper.get(ctx).findBean(
			        ctx,
			        ReasonCode.class,
			        new EQ(ReasonCodeXInfo.REASON_ID, Long.valueOf(transaction
			            .getReasonCode())));
			if (reasonCode == null)
			{
				LogSupport.minor(ctx, SubAccountTransactionToApiAdapter.class,
				    "Cannot find Reason Code " + transaction.getReasonCode());
			}
			else
			{
				reference.addParameters(APIGenericParameterSupport
				    .getTransactionRNReasonCode(ctx, reasonCode));
			}
		}
		catch (HomeException e)
		{
			LogSupport.minor(ctx, SubAccountTransactionToApiAdapter.class,
			    "Lookup of Reason Code falied.", e);
		}

		try
		{
			TransactionMethod method =
			    HomeSupportHelper.get(ctx).findBean(
			        ctx,
			        TransactionMethod.class,
			        new EQ(TransactionMethodXInfo.IDENTIFIER, Long
			            .valueOf(transaction.getTransactionMethod())));
			if (method == null)
			{
				LogSupport.minor(
				    ctx,
				    SubAccountTransactionToApiAdapter.class,
				    "Cannot find Transaction Method "
				        + transaction.getTransactionMethod());
			}
			else
			{
				reference.addParameters(APIGenericParameterSupport
				    .getTransactionRNTransactionMethodID(ctx, method));
			}
		}
		catch (HomeException e)
		{
			LogSupport.minor(ctx, SubAccountTransactionToApiAdapter.class,
			    "Lookup of Transaction Method falied.", e);
		}
		
		
		boolean showTax = (Boolean) ctx.getBoolean(TransactionQueryExecutors.SHOW_TAXAMOUNT);
		if(showTax)
		{
			long taxAmount = TransactionShowTaxHelper.calculateTaxForTransaction(ctx, transaction);
			reference.addParameters(APIGenericParameterSupport.getTransactionTaxAmount(ctx, taxAmount));
		}
		else
		{
			if(LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, "TransactionToApiAdapter", "showTax attribute is disabled .Not showing Tax Amount details");
            }
		}

	}
}
