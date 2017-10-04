/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.api.queryexecutor.generalprovisioning;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import com.trilogy.app.crm.api.queryexecutor.ExecuteResultQueryExecutor;
import com.trilogy.app.crm.api.rmi.GenericParameterParser;
import com.trilogy.app.crm.api.rmi.support.RmiApiErrorHandlingSupport;
import com.trilogy.app.crm.bean.AdjustmentInfo;
import com.trilogy.app.crm.bean.AdjustmentType;
import com.trilogy.app.crm.bean.TaxAuthority;
import com.trilogy.app.crm.client.urcs.UrcsClientInstall;
import com.trilogy.app.crm.config.CRMConfigInfoForVRA;
import com.trilogy.app.crm.support.VRASupport;
import com.trilogy.app.crm.external.vra.VoucherInfoRetrieveServiceRmiClient;
import com.trilogy.app.crm.invoice.Amount;
import com.trilogy.app.crm.invoice.InvoiceCalculationSupport;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.support.TaxAuthoritySupportHelper;
import com.trilogy.app.vra.interfaces.AuthHolder;
import com.trilogy.app.vra.interfaces.VoucherDetails;
import com.trilogy.app.vra.interfaces.VoucherDetailsReturnObject;
import com.trilogy.app.vra.interfaces.VoucherInfoRetrieveInput;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v2_0.types.PaidType;
import com.trilogy.util.crmapi.wsdl.v2_0.types.PaidTypeEnum;
import com.trilogy.util.crmapi.wsdl.v2_1.types.calldetail.UsageTypeReference;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;
import com.trilogy.util.crmapi.wsdl.v3_0.types.CRMRequestHeader;
import com.trilogy.util.crmapi.wsdl.v3_0.types.ExecuteResult;
import com.trilogy.app.crm.config.VRALoginConfig;

/**
 * 
 * @author Marcio Marques
 * @since 9.2
 *
 */
public class GeneralProvisioningQueryExecutors 
{
	/**
	 * 
	 * @author Marcio Marques
	 * @since 9.2.0
	 *
	 */
	public static class VoucherInfoQueryExecutor extends ExecuteResultQueryExecutor
	{
		public VoucherInfoQueryExecutor()
		{
			
		}

	    public ExecuteResult execute(Context ctx, Object... parameters) throws CRMExceptionFault
	    {
	    	String voucher = getParameter(ctx, PARAM_VOUCHER_NUMBER, PARAM_VOUCHER_NUMBER_NAME, String.class, parameters);
	    	PaidType paidType = getParameter(ctx, PARAM_PAID_TYPE, PARAM_PAID_TYPE_NAME, PaidType.class, parameters);
	    	int spid = getParameter(ctx, PARAM_SPID, PARAM_SPID_NAME, int.class, parameters);
	    	GenericParameter[] genericParameters = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);

	    	GenericParameterParser parser = new GenericParameterParser(genericParameters);
	    	
	    	RmiApiErrorHandlingSupport.validateMandatoryObject(voucher, PARAM_VOUCHER_NUMBER_NAME);
	    	
	        VoucherInfoRetrieveServiceRmiClient client = (VoucherInfoRetrieveServiceRmiClient) ctx.get(UrcsClientInstall.VOUCHER_INFO_RETRIEVE_SERVICE_RMI_KEY);
	        try
	        {
	        	final VRALoginConfig vraLoginConfig = (VRALoginConfig) ctx.get(VRALoginConfig.class);
	        	final CRMConfigInfoForVRA vraCrmInfoConfig = VRASupport.getCRMConfigInfoForVRA(ctx,spid);

	        	AuthHolder authHolder = new AuthHolder();
	        	authHolder.setUsername(vraLoginConfig.getUserName());
	        	authHolder.setPassword(vraLoginConfig.getPassword(ctx));
	        	VoucherInfoRetrieveInput input = new VoucherInfoRetrieveInput();
	        	input.setVoucherNumber(voucher);
	        	input.setVoucherLocation(vraCrmInfoConfig.getVoucherLocation());
	        	input.setSubscriberLocation(vraCrmInfoConfig.getSubLocation());
	        	input.setHomeLocation(vraCrmInfoConfig.getHomeLocation());


	        	VoucherDetailsReturnObject voucherResult = client.getVoucherInfoByVoucherNum(authHolder, input);
	        	if (voucherResult!=null && voucherResult.getResultCode() == SUCCESS)
	        	{
	        		VoucherDetails voucherDetails = voucherResult.getVoucherDetails();
	        		if (voucherDetails!=null && (voucherDetails.getVoucherStatus() == VOUCHER_STATUS_USED || parser.getParameter(ALL_VOUCHERS, Boolean.class, Boolean.FALSE)));
	        		{
	        			ExecuteResult result = new ExecuteResult();

	        			long taxAmount = 0;
	        			if (paidType!=null)
	        			{
	        				calculateTaxAmount(ctx, voucherDetails.getTotalVoucherAmount(), vraCrmInfoConfig, spid, paidType);
	        				addParameterToResult(result, TAX_AMOUNT, taxAmount);
	        			}

	        			addParameterToResult(result, DATE_USED, new Date(voucherDetails.getDateUsed()));
	        			addParameterToResult(result, VOUCHER_STATUS, voucherDetails.getVoucherStatus());
	        			addParameterToResult(result, VOUCHER_AMOUNT, voucherDetails.getVoucherAmount());
	        			addParameterToResult(result, VOUCHER_BONUS_AMOUNT, voucherDetails.getBonusAmount());
	        			addParameterToResult(result, TOTAL_VOUCHER_AMOUNT, voucherDetails.getTotalVoucherAmount() + taxAmount);
	        			addParameterToResult(result, VOUCHER_TYPE_ID, voucherDetails.getVoucherTypeID());

	        			return result;
	        		}
	        	}
	        }
            catch (Exception e)
            {
            	RmiApiErrorHandlingSupport.generalException(ctx, e, "Unable to retrieve voucher information", this);
            }
	    	
            if (parser.getParameter(ALL_VOUCHERS, Boolean.class, Boolean.FALSE))
            {
                RmiApiErrorHandlingSupport.simpleValidation(PARAM_VOUCHER_NUMBER_NAME, "Voucher not found");
            }
            else
            {
                RmiApiErrorHandlingSupport.simpleValidation(PARAM_VOUCHER_NUMBER_NAME, "Voucher not found or not used");
            }
            
            return null;
	    }
	    
	    protected long calculateTaxAmount(Context ctx, long voucherAmount, CRMConfigInfoForVRA vraCrmInfoConfig, int spid, PaidType paidType)
	    {
	    	long result = 0;
	    	int adjustmentTypeCode = -1;
	    	if (paidType.getValue() == PaidTypeEnum.POSTPAID.getValue().getValue())
	    	{
		    	adjustmentTypeCode = vraCrmInfoConfig.getPostpaidAdjustmentType();
	    	}
	    	else if (paidType.getValue() == PaidTypeEnum.POSTPAID.getValue().getValue())
	    	{
		    	adjustmentTypeCode = vraCrmInfoConfig.getPrepaidAdjustmentType();
	    	}
	    	
	    	if (adjustmentTypeCode!=-1)
	    	{
	    		TaxAuthority authority = getTaxAuthority(ctx, adjustmentTypeCode, spid);
	    		if (authority!=null)
	    		{
	    			Amount amount = new Amount(voucherAmount);
	                Map<String, Amount> taxMap = new LinkedHashMap<String, Amount>();
	                if (authority.isCompounded())
	                {
	                    // calculate compound tax
	                	InvoiceCalculationSupport.populateCompoundTaxComponents(ctx, authority, amount,  taxMap);
	                }
	                else
	                {
	                	InvoiceCalculationSupport.populateSimpleTaxComponents(ctx, authority, amount,  taxMap);
	                }
	                
	                for (Amount taxAmount : taxMap.values())
	                {
	                	result += taxAmount.get(); 
	                }
	    		}
	    	}
	    	
	    	return result;
	    }
	
		protected TaxAuthority getTaxAuthority(Context ctx, int adjustmentTypeCode, int spid)
		{
			TaxAuthority result = null;
    		AdjustmentInfo adjustmentInfo = getAdjustmentTypeInfo(ctx, adjustmentTypeCode, spid);
    		if (adjustmentInfo!=null)
    		{
    			result = TaxAuthoritySupportHelper.get(ctx).getTaxAuthorityById(ctx, adjustmentInfo.getTaxAuthority(), spid);
    		}
			return result;
		}
	    
	    protected AdjustmentInfo getAdjustmentTypeInfo(Context ctx, int adjustmentTypeCode, int spid)
	    {
	    	AdjustmentInfo result = null;
    		try
    		{
	    		AdjustmentType adjustmentType = AdjustmentTypeSupportHelper.get(ctx).getAdjustmentType(ctx, adjustmentTypeCode);
	            final Map<Integer, AdjustmentInfo> infoMap = adjustmentType.getAdjustmentSpidInfo();
	            if (infoMap != null)
	            {
	            	result = (AdjustmentInfo) infoMap.get(Integer.valueOf(spid));
	            }
	            else
	            {
	    			LogSupport.minor(ctx,  this, "No adjustment spid info configured for adjustment type " + adjustmentTypeCode);
	            }
    		}
    		catch (HomeException e)
    		{
    			LogSupport.minor(ctx,  this, "Unable to retrieve adjustment type info for adjustment type " + adjustmentTypeCode + ": " + e.getMessage(), e);
    		}
    		return result;
	    }
	    
	    @Override
	    public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
	    {
	        Object[] result = null;
	        if (isGenericExecution(ctx, parameters))
	        {
	            result = new Object[5];
	            result[0] = parameters[0];
	            result[1] = getParameter(ctx, PARAM_VOUCHER_NUMBER, PARAM_VOUCHER_NUMBER_NAME, String.class, parameters);
	            result[2] = getParameter(ctx, PARAM_SPID, PARAM_SPID_NAME, int.class, parameters);
	            result[3] = getParameter(ctx, PARAM_PAID_TYPE, PARAM_PAID_TYPE_NAME, PaidType.class, parameters);
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
	        result = result && String.class.isAssignableFrom(parameterTypes[PARAM_VOUCHER_NUMBER]);
	        result = result && int.class.isAssignableFrom(parameterTypes[PARAM_SPID]);
	        result = result && PaidType.class.isAssignableFrom(parameterTypes[PARAM_PAID_TYPE]);
	        result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
	        return result;
	    }
	    
        @Override
        public boolean validateReturnType(Class<?> resultType)
        {
            return ExecuteResult.class.isAssignableFrom(resultType);
        }


	    protected static final short SUCCESS = 0;
	    protected static final short VOUCHER_STATUS_USED = 2;
	    
	    public static final String ALL_VOUCHERS = "AllVouchers";
	    
	    protected static final String DATE_USED = "DateUsed";
	    protected static final String VOUCHER_AMOUNT = "VoucherAmount";
        protected static final String VOUCHER_STATUS = "VoucherStatus";
	    protected static final String TOTAL_VOUCHER_AMOUNT = "TotalVoucherAmount";
	    protected static final String TAX_AMOUNT = "VoucherTaxAmount";
	    protected static final String VOUCHER_BONUS_AMOUNT = "VoucherBonusAmount";
	    /**
         * @since 9.6
         */
        protected static final String VOUCHER_TYPE_ID = "VoucherTypeId";
	    
	    public static final int PARAM_HEADER = 0;
	    public static final int PARAM_VOUCHER_NUMBER = 1;
	    public static final int PARAM_SPID = 2;
	    public static final int PARAM_PAID_TYPE = 3;
	    public static final int PARAM_GENERIC_PARAMETERS = 4;
	    
	    public static final String PARAM_ACCOUNT_ID_NAME = "accountID";
	    public static final String PARAM_VOUCHER_NUMBER_NAME = "voucher";
	    public static final String PARAM_SPID_NAME = "spid";
	    public static final String PARAM_PAID_TYPE_NAME = "paidType";
	    public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
	}
	
	
	/**
	 * 
	 * @author Marcio Marques
	 * @since 9.2.0
	 *
	 */
	public static class VouchersInfoQueryExecutor extends VoucherInfoQueryExecutor
	{
		public VouchersInfoQueryExecutor()
		{
			
		}

	    public ExecuteResult execute(Context ctx, Object... parameters) throws CRMExceptionFault
	    {
	    	String[] vouchers = getParameter(ctx, PARAM_VOUCHERS_NUMBERS, PARAM_VOUCHERS_NUMBERS_NAME, String[].class, parameters);
	    	PaidType paidType = getParameter(ctx, PARAM_PAID_TYPE, PARAM_PAID_TYPE_NAME, PaidType.class, parameters);
	    	int spid = getParameter(ctx, PARAM_SPID, PARAM_SPID_NAME, int.class, parameters);
	    	GenericParameter[] genericParameters = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);

	    	GenericParameterParser parser = new GenericParameterParser(genericParameters);

	    	RmiApiErrorHandlingSupport.validateMandatoryObject(vouchers, PARAM_VOUCHER_NUMBER_NAME);

	    	VoucherInfoRetrieveServiceRmiClient client = (VoucherInfoRetrieveServiceRmiClient) ctx.get(UrcsClientInstall.VOUCHER_INFO_RETRIEVE_SERVICE_RMI_KEY);

	    	final VRALoginConfig vraLoginConfig = (VRALoginConfig) ctx.get(VRALoginConfig.class);
	    	CRMConfigInfoForVRA vraCrmInfoConfig;
	    	ExecuteResult result = new ExecuteResult();
	    	try{
	    		vraCrmInfoConfig = VRASupport.getCRMConfigInfoForVRA(ctx,spid);

	    		AuthHolder authHolder = new AuthHolder();
	    		authHolder.setUsername(vraLoginConfig.getUserName());
	    		authHolder.setPassword(vraLoginConfig.getPassword(ctx));

	    		int voucherID = 0;

	    		for (String voucher : vouchers)
	    		{
	    			VoucherInfoRetrieveInput input = new VoucherInfoRetrieveInput();
	    			input.setVoucherNumber(voucher);
	    			input.setVoucherLocation(vraCrmInfoConfig.getVoucherLocation());
	    			input.setSubscriberLocation(vraCrmInfoConfig.getSubLocation());
	    			input.setHomeLocation(vraCrmInfoConfig.getHomeLocation());

	    			try
	    			{
	    				VoucherDetailsReturnObject voucherResult = client.getVoucherInfoByVoucherNum(authHolder, input);
	    				if (voucherResult!=null && voucherResult.getResultCode() == SUCCESS)
	    				{
	    					VoucherDetails voucherDetails = voucherResult.getVoucherDetails();
	    					if (voucherDetails!=null && (voucherDetails.getVoucherStatus() == VOUCHER_STATUS_USED || parser.getParameter(ALL_VOUCHERS, Boolean.class, Boolean.FALSE)))
	    					{
	    						long taxAmount = 0;

	    						if (paidType!=null)
	    						{
	    							taxAmount = calculateTaxAmount(ctx, voucherDetails.getTotalVoucherAmount(), vraCrmInfoConfig, spid, paidType);
	    							addParameterToResult(result, VOUCHER_PREFIX + SEPARATOR + voucherID + SEPARATOR + TAX_AMOUNT, taxAmount);
	    						}

	    						addParameterToResult(result, VOUCHER_PREFIX + SEPARATOR + voucherID + SEPARATOR + DATE_USED, new Date(voucherDetails.getDateUsed()));
	    						addParameterToResult(result, VOUCHER_PREFIX + SEPARATOR + voucherID + SEPARATOR + VOUCHER_STATUS, voucherDetails.getVoucherStatus());
	    						addParameterToResult(result, VOUCHER_PREFIX + SEPARATOR + voucherID + SEPARATOR + VOUCHER_AMOUNT, voucherDetails.getVoucherAmount());
	    						addParameterToResult(result, VOUCHER_PREFIX + SEPARATOR + voucherID + SEPARATOR + VOUCHER_BONUS_AMOUNT, voucherDetails.getBonusAmount());
	    						addParameterToResult(result, VOUCHER_PREFIX + SEPARATOR + voucherID + SEPARATOR + TOTAL_VOUCHER_AMOUNT, voucherDetails.getTotalVoucherAmount() + taxAmount);
	    						addParameterToResult(result, VOUCHER_PREFIX + SEPARATOR + voucherID + SEPARATOR + VOUCHER_TYPE_ID, voucherDetails.getVoucherTypeID());
	    					}
	    				}
	    			}
	    			catch (Exception e)
	    			{
	    				LogSupport.minor(ctx,  this, "Unable to retrieve voucher information: " + e.getMessage(), e);
	    			}
	    			voucherID ++;
	    		}
	    	}catch(HomeException he)
	    	{
	    		LogSupport.major(ctx, this, "Unable to get voucher config details "+he.getMessage(),he);
	    	}

	    	return result;
	    }
	    
	    @Override
	    public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
	    {
	        Object[] result = null;
	        if (isGenericExecution(ctx, parameters))
	        {
	            result = new Object[5];
	            result[0] = parameters[0];
	            result[1] = getParameter(ctx, PARAM_VOUCHERS_NUMBERS, PARAM_VOUCHERS_NUMBERS_NAME, String[].class, parameters);
	            result[2] = getParameter(ctx, PARAM_SPID, PARAM_SPID_NAME, int.class, parameters);
	            result[3] = getParameter(ctx, PARAM_PAID_TYPE, PARAM_PAID_TYPE_NAME, PaidType.class, parameters);
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
	        result = result && String[].class.isAssignableFrom(parameterTypes[PARAM_VOUCHERS_NUMBERS]);
	        result = result && int.class.isAssignableFrom(parameterTypes[PARAM_SPID]);
	        result = result && PaidType.class.isAssignableFrom(parameterTypes[PARAM_PAID_TYPE]);
	        result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
	        return result;
	    }
	    
        @Override
        public boolean validateReturnType(Class<?> resultType)
        {
            return ExecuteResult.class.isAssignableFrom(resultType);
        }


	    private static final String VOUCHER_PREFIX = "voucher";
	    private static final String SEPARATOR = ".";
	    
	    public static final int PARAM_HEADER = 0;
	    public static final int PARAM_VOUCHERS_NUMBERS = 1;
	    public static final int PARAM_SPID = 2;
	    public static final int PARAM_PAID_TYPE = 3;
	    public static final int PARAM_GENERIC_PARAMETERS = 4;
	    
	    public static final String PARAM_ACCOUNT_ID_NAME = "accountID";
	    public static final String PARAM_VOUCHERS_NUMBERS_NAME = "vouchers";
	    public static final String PARAM_SPID_NAME = "spid";
	    public static final String PARAM_PAID_TYPE_NAME = "paidType";
	    public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
	}
}
