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
package com.trilogy.app.crm.api.queryexecutor.provisioningcommand;

import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.api.queryexecutor.AbstractQueryExecutor;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;
import com.trilogy.util.crmapi.wsdl.v3_0.api.types.ExecuteResultResponse;
import com.trilogy.util.crmapi.wsdl.v3_0.types.provisioningcommand.SubscriberHlrInfo;

/**
 * Marker QueryExecutor of Provisioning Command interface.
 * ESB interacts with HLR to work on these operations. Also ESB mimics this API to cater to calls by external systems. 
 * BSS is not part of this call flow for Gamma.
 *
 * @author isha.aderao
 * @since 9.13
 */
public class ProvisioningCommandQueryExecutors {

	/**
	 * 
	 */
	public ProvisioningCommandQueryExecutors() {
	}
	
	
	public static class AddOrUpdateForwardToNumberQueryExecutor extends AbstractQueryExecutor<ExecuteResultResponse>
	{

		/**
		 * {@inheritDoc}
		 */
		public ExecuteResultResponse execute(Context ctx, Object... parameters)
				throws CRMExceptionFault {
			
			throw new CRMExceptionFault("Operation not supported");
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean validateParameterTypes(Class<?>[] parameterTypes) {
			return false;
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean validateReturnType(Class<?> returnType) {
			return false;
		}

		/**
		 * {@inheritDoc}
		 */
		public Object[] getParameters(Context ctx, Object... parameters)
				throws CRMExceptionFault {
			return null;
		}
		
	}
	
	public static class RemoveForwardToNumberQueryExecutor extends AbstractQueryExecutor<ExecuteResultResponse>
	{

		/**
		 * {@inheritDoc}
		 */
		public ExecuteResultResponse execute(Context ctx, Object... parameters)
				throws CRMExceptionFault {
			throw new CRMExceptionFault("Operation not supported");
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean validateParameterTypes(Class<?>[] parameterTypes) {
			return false;
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean validateReturnType(Class<?> returnType) {
			return false;
		}

		/**
		 * {@inheritDoc}
		 */
		public Object[] getParameters(Context ctx, Object... parameters)
				throws CRMExceptionFault {
			return null;
		}
	}
	
	public static class SubscriberHlrInfoQueryExecutor extends AbstractQueryExecutor<SubscriberHlrInfo>
	{

		/**
		 * {@inheritDoc}
		 */
		public SubscriberHlrInfo execute(Context ctx, Object... parameters)
				throws CRMExceptionFault 
		{
			throw new CRMExceptionFault("Operation not supported");
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean validateParameterTypes(Class<?>[] parameterTypes) {
			return false;
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean validateReturnType(Class<?> returnType) {
			return false;
		}

		/**
		 * {@inheritDoc}
		 */
		public Object[] getParameters(Context ctx, Object... parameters)
				throws CRMExceptionFault {
			return null;
		}
	}

}
