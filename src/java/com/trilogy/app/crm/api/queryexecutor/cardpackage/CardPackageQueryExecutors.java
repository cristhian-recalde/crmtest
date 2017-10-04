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
package com.trilogy.app.crm.api.queryexecutor.cardpackage;

import java.util.ArrayList;
import java.util.Collection;

import com.trilogy.app.crm.api.queryexecutor.AbstractQueryExecutor;
import com.trilogy.app.crm.api.rmi.PackageToApiAdapter;
import com.trilogy.app.crm.api.rmi.support.CardPackageApiSupport;
import com.trilogy.app.crm.api.rmi.support.RmiApiErrorHandlingSupport;
import com.trilogy.app.crm.api.rmi.support.RmiApiSupport;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.GSMPackage;
import com.trilogy.app.crm.bean.GSMPackageXInfo;
import com.trilogy.app.crm.bean.Package;
import com.trilogy.app.crm.bean.PackageStateEnum;
import com.trilogy.app.crm.bean.TDMAPackage;
import com.trilogy.app.crm.bean.TDMAPackageXInfo;
import com.trilogy.app.crm.elang.PagingXStatement;
import com.trilogy.app.crm.support.CollectionSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.technology.TechnologyEnum;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.GT;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v2_0.types.TechnologyType;
import com.trilogy.util.crmapi.wsdl.v2_0.types.TechnologyTypeEnum;
import com.trilogy.util.crmapi.wsdl.v2_1.exception.ExceptionCode;
import com.trilogy.util.crmapi.wsdl.v2_1.types.calldetail.UsageTypeReference;
import com.trilogy.util.crmapi.wsdl.v2_1.types.cardpackage.CardPackage;
import com.trilogy.util.crmapi.wsdl.v2_1.types.cardpackage.CardPackageQueryResult;
import com.trilogy.util.crmapi.wsdl.v2_1.types.cardpackage.CardPackageReference;
import com.trilogy.util.crmapi.wsdl.v2_1.types.cardpackage.CardPackageState;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;
import com.trilogy.util.crmapi.wsdl.v3_0.types.CRMRequestHeader;

/**
 * 
 * @author Marcio Marques
 * @since 9.3
 *
 */
public class CardPackageQueryExecutors 
{
    private static final PackageToApiAdapter packageToApiAdapter_ = new PackageToApiAdapter();

    /**
	 * 
	 * @author Marcio Marques
	 * @since 9.3.0
	 *
	 */
	public static class CardPackagesListQueryExecutor extends AbstractQueryExecutor<CardPackageQueryResult>
	{
		public CardPackagesListQueryExecutor()
		{
			
		}

	    public CardPackageQueryResult execute(Context ctx, Object... parameters) throws CRMExceptionFault
	    {
            int spid = getParameter(ctx, PARAM_SPID, PARAM_SPID_NAME, int.class, parameters);
            TechnologyType technology = getParameter(ctx, PARAM_TECHNOLOGY, PARAM_TECHNOLOGY_NAME, TechnologyType.class, parameters);
            String groupName = getParameter(ctx, PARAM_GROUP_NAME, PARAM_GROUP_NAME_NAME, String.class, parameters);
            CardPackageState state = getParameter(ctx, PARAM_CARD_STATE, PARAM_CARD_STATE_NAME, CardPackageState.class, parameters);
	    	int limit = getParameter(ctx, PARAM_LIMIT, PARAM_LIMIT_NAME, int.class, parameters);
            Boolean isAscending = getParameter(ctx, PARAM_IS_ASCENDING, PARAM_IS_ASCENDING_NAME, Boolean.class, parameters);
            String pageKey = getParameter(ctx, PARAM_PAGE_KEY, PARAM_PAGE_KEY_NAME, String.class, parameters);

            RmiApiErrorHandlingSupport.validateMandatoryObject(technology, PARAM_TECHNOLOGY_NAME);
            
            final CRMSpid crmSpid = RmiApiSupport.getCrmServiceProvider(ctx, spid, this);
            RmiApiErrorHandlingSupport.validateLimitInput(ctx, limit, crmSpid.getMaxGetLimit());
            if (technology.getValue() != TechnologyTypeEnum.GSM.getValue().getValue()
                    && technology.getValue() != TechnologyTypeEnum.CDMA.getValue().getValue()
                    && technology.getValue() != TechnologyTypeEnum.TDMA.getValue().getValue())
            {
                RmiApiErrorHandlingSupport.generalException(ctx, null, "Package Home UNKNOWN for technology " + technology,
                        ExceptionCode.GENERAL_EXCEPTION, this);
            }
            CardPackageReference[] cardPackageReferences = new CardPackageReference[]
                {};
            try
            {
                Collection collection = getCRMCardPackages(ctx, spid, RmiApiSupport.convertApiTechnology2Crm(technology),
                        groupName, state, pageKey, limit, RmiApiSupport.isSortAscending(isAscending));
                cardPackageReferences = CollectionSupportHelper.get(ctx).adaptCollection(ctx, collection,
                        packageToApiAdapter_, cardPackageReferences);
            }
            catch (final Exception e)
            {
                final String msg = "Unable to retrieve Packages for spid=" + spid + " technology=" + technology + " limit="
                        + limit;
                RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
            }
            final CardPackageQueryResult result = new CardPackageQueryResult();
            result.setReferences(cardPackageReferences);
            if (cardPackageReferences != null && cardPackageReferences.length > 0)
            {
                result.setPageKey(cardPackageReferences[cardPackageReferences.length - 1].getIdentifier());
            }
            return result;

	    }

	    private Collection getCRMCardPackages(final Context ctx, final int spid, final TechnologyEnum technology,
	            final String groupName, final CardPackageState state, final String pageKey, final int limit,
	            boolean isAscending) throws HomeException, com.redknee.util.crmapi.wsdl.v3_0.api.CRMExceptionFault
	    {
	        if (technology == TechnologyEnum.GSM)
	        {
	            And condition = new And();
	            condition.add(new EQ(GSMPackageXInfo.SPID, spid));
	            condition.add(new EQ(GSMPackageXInfo.TECHNOLOGY, technology));
	            if (groupName != null && groupName.length() > 0)
	            {
	                condition.add(new EQ(GSMPackageXInfo.PACKAGE_GROUP, groupName));
	            }
	            if (state != null)
	            {
	                PackageStateEnum pkgState = RmiApiSupport.convertApiCardPackageState2Crm(state);
	                condition.add(new EQ(GSMPackageXInfo.STATE, pkgState));
	            }
	            if (pageKey != null && pageKey.length() > 0)
	            {
	                condition.add(new PagingXStatement(GSMPackageXInfo.PACK_ID, pageKey, isAscending));
	            }
	            return HomeSupportHelper.get(ctx).getBeans(ctx, GSMPackage.class, condition, limit, isAscending);
	        }
	        else if (technology == TechnologyEnum.TDMA || technology == TechnologyEnum.CDMA)
	        {
	            And condition = new And();
	            condition.add(new EQ(TDMAPackageXInfo.SPID, spid));
	            condition.add(new EQ(TDMAPackageXInfo.TECHNOLOGY, technology));
	            if (groupName != null && groupName.length() > 0)
	            {
	                condition.add(new EQ(TDMAPackageXInfo.PACKAGE_GROUP, groupName));
	            }
	            if (state != null)
	            {
	                PackageStateEnum pkgState = RmiApiSupport.convertApiCardPackageState2Crm(state);
	                condition.add(new EQ(TDMAPackageXInfo.STATE, pkgState));
	            }
	            if (pageKey != null && pageKey.length() > 0)
	            {
	                condition.add(new GT(TDMAPackageXInfo.PACK_ID, pageKey));
	            }
	            return HomeSupportHelper.get(ctx).getBeans(ctx, TDMAPackage.class, condition, limit, isAscending);
	        }
	        return new ArrayList();
	    }

        @Override
        public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            Object[] result = null;
            if (isGenericExecution(ctx, parameters))
            {
                result = new Object[9];
                result[0] = parameters[0];
                result[1] = getParameter(ctx, PARAM_SPID, PARAM_SPID_NAME, int.class, parameters);
                result[2] = getParameter(ctx, PARAM_TECHNOLOGY, PARAM_TECHNOLOGY_NAME, TechnologyType.class, parameters);
                result[3] = getParameter(ctx, PARAM_GROUP_NAME, PARAM_GROUP_NAME_NAME, String.class, parameters);
                result[4] = getParameter(ctx, PARAM_CARD_STATE, PARAM_CARD_STATE_NAME, CardPackageState.class, parameters);
                result[5] = getParameter(ctx, PARAM_PAGE_KEY, PARAM_PAGE_KEY_NAME, String.class, parameters);
                result[6] = getParameter(ctx, PARAM_LIMIT, PARAM_LIMIT_NAME, int.class, parameters);
                result[7] = getParameter(ctx, PARAM_IS_ASCENDING, PARAM_IS_ASCENDING_NAME, Boolean.class, parameters);
                result[8] = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);
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
	        result = result && (parameterTypes.length>=9);
	        result = result && CRMRequestHeader.class.isAssignableFrom(parameterTypes[PARAM_HEADER]);
            result = result && int.class.isAssignableFrom(parameterTypes[PARAM_SPID]);
	        result = result && TechnologyType.class.isAssignableFrom(parameterTypes[PARAM_TECHNOLOGY]);
            result = result && String.class.isAssignableFrom(parameterTypes[PARAM_GROUP_NAME]);
            result = result && CardPackageState.class.isAssignableFrom(parameterTypes[PARAM_CARD_STATE]);
            result = result && String.class.isAssignableFrom(parameterTypes[PARAM_PAGE_KEY]);
            result = result && int.class.isAssignableFrom(parameterTypes[PARAM_LIMIT]);
            result = result && Boolean.class.isAssignableFrom(parameterTypes[PARAM_IS_ASCENDING]);
            result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
	        return result;
	    }
        
        @Override
        public boolean validateReturnType(Class<?> resultType)
        {
            return CardPackageQueryResult[].class.isAssignableFrom(resultType);
        }


        
        public static final int PARAM_HEADER = 0;
        public static final int PARAM_SPID = 1;
	    public static final int PARAM_TECHNOLOGY = 2;
        public static final int PARAM_GROUP_NAME = 3;
        public static final int PARAM_CARD_STATE = 4;
        public static final int PARAM_PAGE_KEY = 5;
        public static final int PARAM_LIMIT = 6;
        public static final int PARAM_IS_ASCENDING = 7;
        public static final int PARAM_GENERIC_PARAMETERS = 8;
	    
        public static final String PARAM_SPID_NAME = "spid";
        public static final String PARAM_TECHNOLOGY_NAME = "technology";
        public static final String PARAM_GROUP_NAME_NAME = "groupName";
        public static final String PARAM_CARD_STATE_NAME = "state";
        public static final String PARAM_PAGE_KEY_NAME = "pageKey";
        public static final String PARAM_LIMIT_NAME = "limit";
        public static final String PARAM_IS_ASCENDING_NAME = "isAscending";
        public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
	}
	
    /**
     * 
     * @author Marcio Marques
     * @since 9.3.0
     *
     */
    public static class CardPackageQueryExecutor extends AbstractQueryExecutor<CardPackage>
    {
        public CardPackageQueryExecutor()
        {
            
        }

        public CardPackage execute(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            String packageID = getParameter(ctx, PARAM_PACKAGE_IDENTIFIER, PARAM_PACKAGE_IDENTIFIER_NAME, String.class, parameters);

            RmiApiErrorHandlingSupport.validateMandatoryObject(packageID, PARAM_PACKAGE_IDENTIFIER_NAME);
            
            CardPackage result = null;
            
            try
            {
                final GSMPackage gsmCard = HomeSupportHelper.get(ctx).findBean(ctx, GSMPackage.class, packageID);
                if (gsmCard != null)
                {
                    result = PackageToApiAdapter.adaptPackageToCardPackage(gsmCard);
                }
                else
                {
                    /**
                     *  Reason for adding ELang below:-
                     *  As primary key is composite now, in case of multispid,
                     *  there might be more than one packages existing for the same packageID. 
                     *  We will return the first record given by the home.
                     */
                    final TDMAPackage tdmaCard = HomeSupportHelper.get(ctx).findBean(ctx, TDMAPackage.class, new EQ(TDMAPackageXInfo.PACK_ID, packageID));
                    if (tdmaCard != null)
                    {
                        result = PackageToApiAdapter.adaptPackageToCardPackage(tdmaCard);
                    }
                }
            }
            catch (final Exception e)
            {
                final String msg = "Unable to retrieve Card Package " + packageID;
                RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
            }

            if (result == null)
            {
                final String msg = "Card Package " + packageID;
                RmiApiErrorHandlingSupport.identificationException(ctx, msg, this);
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
                result[1] = getParameter(ctx, PARAM_PACKAGE_IDENTIFIER, PARAM_PACKAGE_IDENTIFIER_NAME, String.class, parameters);
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
            result = result && String.class.isAssignableFrom(parameterTypes[PARAM_PACKAGE_IDENTIFIER]);
            result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
            return result;
        }
        
        @Override
        public boolean validateReturnType(Class<?> resultType)
        {
            return CardPackage.class.isAssignableFrom(resultType);
        }


        
        public static final int PARAM_HEADER = 0;
        public static final int PARAM_PACKAGE_IDENTIFIER = 1;
        public static final int PARAM_GENERIC_PARAMETERS = 2;
        
        public static final String PARAM_PACKAGE_IDENTIFIER_NAME = "packageID";
        public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
    }
    
    public static class CardPackageCreationQueryExecutor extends AbstractQueryExecutor<CardPackageReference>
    {
        public CardPackageCreationQueryExecutor()
        {
            
        }

        public CardPackageReference execute(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            CardPackage card = getParameter(ctx, PARAM_CARD, PARAM_CARD_NAME, CardPackage.class, parameters);

            RmiApiErrorHandlingSupport.validateMandatoryObject(card, PARAM_CARD_NAME);
            RmiApiErrorHandlingSupport.validateMandatoryObject(card.getSpid(), PARAM_CARD_NAME + ".spid");
            RmiApiErrorHandlingSupport.validateMandatoryObject(card.getState(), PARAM_CARD_NAME + ".state");
            RmiApiErrorHandlingSupport.validateMandatoryObject(card.getTechnology(), PARAM_CARD_NAME + ".technology");

            CardPackageReference result = null;
            try
            {
                final Object resultPackage = CardPackageApiSupport.createCrmCardPackage(ctx, card, this);
                // the above throws CRM exception rather then returning null
                result = (CardPackageReference) packageToApiAdapter_.adapt(ctx, resultPackage);
            }
            catch (final com.redknee.util.crmapi.wsdl.v3_0.api.CRMExceptionFault e)
            {
                throw e;
            }
            catch (final Exception e)
            {
                final String msg = "Unable to create " + RmiApiSupport.cardPackageToString(card);
                Class<? extends Package> pkgType = null;
                final TechnologyType technology = card.getTechnology();
                if (technology != null)
                {
                    long technologyValue = technology.getValue();
                    if (technologyValue == TechnologyTypeEnum.GSM.getValue().getValue())
                    {
                        pkgType = GSMPackage.class;
                    }
                    else if (technologyValue == TechnologyTypeEnum.TDMA.getValue().getValue()
                            || technologyValue == TechnologyTypeEnum.CDMA.getValue().getValue())
                    {
                        pkgType = TDMAPackage.class;
                    }
                }
                
                RmiApiErrorHandlingSupport.handleCreateExceptions(ctx, e, msg, false, pkgType, card.getIdentifier(), this);
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
                result[1] = getParameter(ctx, PARAM_CARD, PARAM_CARD_NAME, CardPackage.class, parameters);
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
            result = result && CardPackage.class.isAssignableFrom(parameterTypes[PARAM_CARD]);
            result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
            return result;
        }

        @Override
        public boolean validateReturnType(Class<?> resultType)
        {
            return CardPackageReference.class.isAssignableFrom(resultType);
        }

        
        public static final int PARAM_HEADER = 0;
        public static final int PARAM_CARD = 1;
        public static final int PARAM_GENERIC_PARAMETERS = 2;
        
        public static final String PARAM_CARD_NAME = "card";
        public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
    }

}
