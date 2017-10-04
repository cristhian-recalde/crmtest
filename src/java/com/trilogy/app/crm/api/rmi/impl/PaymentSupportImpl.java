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

package com.trilogy.app.crm.api.rmi.impl;

import java.util.Arrays;

import com.trilogy.app.crm.api.rmi.DepositDetailsToDeositReferenceAdapter;
import com.trilogy.app.crm.api.rmi.DepositResponseAdapter;
import com.trilogy.app.crm.api.rmi.support.RmiApiErrorHandlingSupport;
import com.trilogy.app.crm.api.rmi.support.RmiApiSupport;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CreateDepositResponse;
import com.trilogy.app.crm.bean.DepositReference;
import com.trilogy.app.crm.support.CollectionSupportHelper;
import com.trilogy.app.crm.support.DepositSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;
import com.trilogy.util.crmapi.wsdl.v3_0.api.PaymentSupportServiceSkeletonInterface;
import com.trilogy.util.crmapi.wsdl.v3_0.api.types.paymentsupportservice.CalculateUpfrontPaymentTaxResponse;
import com.trilogy.util.crmapi.wsdl.v3_0.api.types.paymentsupportservice.CreateUpfrontTransactionsResponse;
import com.trilogy.util.crmapi.wsdl.v3_0.types.CRMRequestHeader;
import com.trilogy.util.crmapi.wsdl.v3_0.types.paymentsupportservice.ChargeDetails;
import com.trilogy.util.crmapi.wsdl.v3_0.types.paymentsupportservice.CreateUpfrontTransactionsResult;
import com.trilogy.util.crmapi.wsdl.v3_0.types.paymentsupportservice.DepositDetails;
import com.trilogy.util.crmapi.wsdl.v3_0.types.paymentsupportservice.DepositResponse;

public class PaymentSupportImpl implements PaymentSupportServiceSkeletonInterface, ContextAware {
	/**
	 * The operating context.
	 */
	private Context context_;
	protected static final Adapter depositeReferenceAdapter_ = new DepositDetailsToDeositReferenceAdapter();
	protected static final Adapter depositeResponseAdapter_ = new DepositResponseAdapter();

	public PaymentSupportImpl(Context context) {
		this.context_ = context;
	}

	@Override
	public Context getContext() {
		return this.context_;
	}

	@Override
	public void setContext(final Context ctx) {
		this.context_ = ctx;
	}

	@Override
	public CalculateUpfrontPaymentTaxResponse calculateUpfrontPaymentTax(CRMRequestHeader header, int spid, String ban,
			GenericParameter[] parameters) throws CRMExceptionFault {
		return null;
	}

	@Override
	public CreateUpfrontTransactionsResponse createUpfrontTransactions(CRMRequestHeader header, int spid, String ban,
			ChargeDetails[] chargeDetils, DepositDetails[] depositDetails, GenericParameter[] parameters)
					throws CRMExceptionFault {
		LogSupport.info(context_, this,"Spid : "+spid+" And BAN : "+ban );
		
		DepositReference[] depositReferences = new DepositReference[] {};
		CreateUpfrontTransactionsResponse response = new CreateUpfrontTransactionsResponse();
		
		try {
			
			if(spid==Integer.MIN_VALUE){
				throw new HomeException("Invalid Service Provider");
			}
				
			final CRMSpid sp = RmiApiSupport.getCrmServiceProvider(context_, Integer.valueOf(spid), this);
			if(sp==null)
				throw new HomeException("Invalid Service Provider :"+spid);
			
			depositReferences = CollectionSupportHelper.get(context_).adaptCollection(context_,
					Arrays.asList(depositDetails), depositeReferenceAdapter_, depositReferences);
			
			for (DepositReference depositReference : depositReferences) {
				depositReference.setSpid(spid);
				depositReference.setBan(ban);
			}
			
			CreateDepositResponse[] depositResponses = DepositSupport.createDeposit(context_, depositReferences, header.getAgentID());
			
			if(null != depositResponses && depositResponses.length > 0)
			{
				int i=0;
				CreateUpfrontTransactionsResult[] result = new CreateUpfrontTransactionsResult[depositResponses.length]; 
				
				for (CreateDepositResponse createDepositResponse : depositResponses) {
					DepositResponse[] depositResponse = {};
					CreateUpfrontTransactionsResult upfrontTransactionsResult = new CreateUpfrontTransactionsResult();
					depositResponse = CollectionSupportHelper.get(context_).adaptCollection(context_,
							Arrays.asList(createDepositResponse), depositeResponseAdapter_, depositResponse);
					upfrontTransactionsResult.setDepositresponse(depositResponse);
					result[i++] = upfrontTransactionsResult;
				}
				
				response.setCreateUpfrontTransactionsResult(result);
			}
			
			
		} catch (HomeInternalException e) {
			RmiApiErrorHandlingSupport.handleQueryExceptions(context_, e, e.getMessage(), this);
		} catch (HomeException e) {
			RmiApiErrorHandlingSupport.handleQueryExceptions(context_, e, e.getMessage(), this);
		}

		return response;
	}

}
