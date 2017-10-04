/*
� * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.api.queryexecutor.bundlequery;

import java.util.Collection;
import java.util.Collections;

import com.trilogy.app.crm.api.queryexecutor.AbstractQueryExecutor;
import com.trilogy.app.crm.api.rmi.AuxiliaryBundleProfileToApiAdapter;
import com.trilogy.app.crm.api.rmi.support.RmiApiErrorHandlingSupport;
import com.trilogy.app.crm.api.rmi.support.RmiApiSupport;
import com.trilogy.app.crm.bundle.BundleProfile;
import com.trilogy.app.crm.bundle.BundleProfileHome;
import com.trilogy.app.crm.bundle.BundleSegmentEnum;
import com.trilogy.app.crm.bundle.service.CRMBundleProfile;
import com.trilogy.app.crm.support.CollectionSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v2_0.types.PaidType;
import com.trilogy.util.crmapi.wsdl.v2_0.types.PaidTypeEnum;
import com.trilogy.util.crmapi.wsdl.v2_1.types.bundlequery.AuxiliaryBundle;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;
import com.trilogy.util.crmapi.wsdl.v3_0.api.types.bundlequery.Spid_type1;
import com.trilogy.util.crmapi.wsdl.v3_0.types.CRMRequestHeader;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.AccountForIDResults;

/**
 * 
 * @author Marcio Marques
 * @since 9.3
 *
 */
public class BundleQueryQueryExecutors 
{
    protected static final Adapter auxiliaryBundleAdapter_ = new AuxiliaryBundleProfileToApiAdapter();

    /**
	 * 
	 * @author Marcio Marques
	 * @since 9.3.0
	 *
	 */
	public static class AuxiliaryBundlesRequestQueryExecutor extends AbstractQueryExecutor<AuxiliaryBundle[]>
	{
		public AuxiliaryBundlesRequestQueryExecutor()
		{
			
		}

	    public AuxiliaryBundle[] execute(Context ctx, Object... parameters) throws CRMExceptionFault
	    {
	        Spid_type1 spidObj = getParameter(ctx, PARAM_SPID_OBJ, PARAM_SPID_OBJ_NAME, Spid_type1.class, parameters);
	        PaidType subscriberType = getParameter(ctx, PARAM_PAID_TYPE, PARAM_PAID_TYPE_NAME, PaidType.class, parameters);
            int subscriptionType = getParameter(ctx, PARAM_SUBSCRIPTION_TYPE, PARAM_SUBSCRIPTION_TYPE_NAME, int.class, parameters);

            RmiApiErrorHandlingSupport.validateMandatoryObject(spidObj, PARAM_SPID_OBJ_NAME);
            RmiApiErrorHandlingSupport.validateMandatoryObject(subscriberType, PARAM_PAID_TYPE_NAME);
            
            int spid = spidObj.getSpid_type0();
            
            RmiApiSupport.getCrmServiceProvider(ctx, spid, this);
            
            if (subscriptionType >= 0)
            {
                RmiApiErrorHandlingSupport.identificationException(ctx, String.valueOf(subscriptionType), this);
            }
            
            AuxiliaryBundle[] auxBundles = new AuxiliaryBundle[]
                {};
            
            try
            {
                CRMBundleProfile service = (CRMBundleProfile) ctx.get(CRMBundleProfile.class);
                if (service == null)
                {
                    throw new IllegalStateException("Bundle service is not configured properly.");
                }
                // Get the SPID filtered home and put it in the context for HomeSupport
                Home home = service.getAuxiliaryBundlesBySPID(ctx, spid);
                ctx.put(BundleProfileHome.class, home);
                if (subscriberType != PaidTypeEnum.valueOf(BundleSegmentEnum.HYBRID_INDEX))
                {
                    home = service.getBundlesBySegment(ctx, BundleSegmentEnum.get((short) subscriberType.getValue()),
                            Collections.EMPTY_LIST, true);
                    ctx.put(BundleProfileHome.class, home);
                }
                final Collection<BundleProfile> collection = HomeSupportHelper.get(ctx).getBeans(ctx, BundleProfile.class);
                auxBundles = CollectionSupportHelper.get(ctx).adaptCollection(ctx, collection,
                        auxiliaryBundleAdapter_, auxBundles);
            }
            catch (final Exception e)
            {
                final String msg = "Unable to retrieve Auxiliary Bundles for Service Provider=" + spid;
                RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
            }
            
            return auxBundles;
	    }
	    
	    @Override
	    public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
	    {
	        Object[] result = null;
	        if (isGenericExecution(ctx, parameters))
	        {
	            result = new Object[5];
	            result[0] = parameters[0];
	            result[1] = getParameter(ctx, PARAM_SPID_OBJ, PARAM_SPID_OBJ_NAME, Spid_type1.class, parameters);
                result[2] = getParameter(ctx, PARAM_PAID_TYPE, PARAM_PAID_TYPE_NAME, PaidType.class, parameters);
                result[3] = getParameter(ctx, PARAM_SUBSCRIPTION_TYPE, PARAM_SUBSCRIPTION_TYPE_NAME, int.class, parameters);
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
	        result = result && Spid_type1.class.isAssignableFrom(parameterTypes[PARAM_SPID_OBJ]);
            result = result && PaidType.class.isAssignableFrom(parameterTypes[PARAM_PAID_TYPE]);
            result = result && int.class.isAssignableFrom(parameterTypes[PARAM_SUBSCRIPTION_TYPE]);
            result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
	        return result;
	    }
	    
        @Override
        public boolean validateReturnType(Class<?> resultType)
        {
            return AuxiliaryBundle[].class.isAssignableFrom(resultType);
            
        }


	    protected static final String COLLISION_AVOIDANCE = "COLLISION_AVOIDANCE";
	    
	    public static final int PARAM_HEADER = 0;
	    public static final int PARAM_SPID_OBJ = 1;
        public static final int PARAM_PAID_TYPE = 2;
        public static final int PARAM_SUBSCRIPTION_TYPE = 3;
        public static final int PARAM_GENERIC_PARAMETERS = 4;
	    
        public static final String PARAM_SPID_OBJ_NAME = "spidObj";
        public static final String PARAM_PAID_TYPE_NAME = "subscriberType";
        public static final String PARAM_SUBSCRIPTION_TYPE_NAME = "subscriptionType";
        public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
	}
	
}
