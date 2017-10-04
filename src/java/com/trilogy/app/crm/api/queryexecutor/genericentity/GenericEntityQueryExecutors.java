/*
 * Copyright (c) 2007, REDKNEE.com. All Rights Reserved.
 * 
 * This software is the confidential and proprietary information of REDKNEE.com.
 * ("Confidential Information"). You shall not disclose such Confidential Information and
 * shall use it only in accordance with the terms of the license agreement you entered
 * into with REDKNEE.com.
 * 
 * REDKNEE.COM MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT.
 * REDKNEE.COM SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF
 * USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */
package com.trilogy.app.crm.api.queryexecutor.genericentity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.trilogy.app.crm.api.generic.entity.adapter.EntityParsingException;
import com.trilogy.app.crm.api.generic.entity.adapter.GenericEntityAdapter;
import com.trilogy.app.crm.api.generic.entity.bean.GenericEntityApiConfiguration;
import com.trilogy.app.crm.api.generic.entity.bean.Interceptor;
import com.trilogy.app.crm.api.generic.entity.support.GenericEntityApiConfigurationSupport;
import com.trilogy.app.crm.api.generic.entity.support.GenericEntityApiResponseSupport;
import com.trilogy.app.crm.api.queryexecutor.AbstractQueryExecutor;
import com.trilogy.app.crm.api.rmi.support.RmiApiErrorHandlingSupport;
import com.trilogy.app.crm.home.GenericEntityOperationHomeException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;
import com.trilogy.util.crmapi.wsdl.v3_0.types.CRMRequestHeader;
import com.trilogy.util.crmapi.wsdl.v3_0.types.GenericResponse;
import com.trilogy.util.crmapi.wsdl.v3_0.types.genericentity.Entity;
import com.trilogy.util.crmapi.wsdl.v3_0.types.genericentity.EntitySequence;

public class GenericEntityQueryExecutors 
{
	public static final String RETURN_GENERIC_PARAM_LIST = "RETURN_GENERIC_PARAM_LIST";

	public static class CreateEntityQueryExecutor extends AbstractQueryExecutor<GenericResponse>
	{

		public CreateEntityQueryExecutor()
		{
		
		}

		@Override
		public GenericResponse execute(Context parentCtx, Object... parameters) throws CRMExceptionFault
		{
			Context ctx = parentCtx.createSubContext();
			
			CRMRequestHeader header = getParameter(ctx, PARAM_HEADER, PARAM_HEADER_NAME, CRMRequestHeader.class, parameters);
	    	Entity entity = getParameter(ctx, PARAM_ENTITY, PARAM_ENTITY_NAME, Entity.class, parameters);
	        GenericParameter[] genericParameters = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);
	
	        RmiApiErrorHandlingSupport.validateMandatoryObject(entity, PARAM_ENTITY_NAME);
	        
	        List<GenericParameter> paramList = new ArrayList<GenericParameter>();
	        ctx.put(RETURN_GENERIC_PARAM_LIST, paramList);
	        
	        GenericEntityApiConfiguration config = null;
	        try
	        {
	        	config  = GenericEntityApiConfigurationSupport.getEntityConfiguration(ctx, entity);
	        	if(config == null)
	        	{
	        		return GenericEntityApiResponseSupport.createGenericResponse(ctx, GenericEntityApiResponseSupport.ENTITY_NOT_SUPPORTED, 
	        				"Entity " + entity.getType() + " is not supported by the system.");
	        	}
	        }
	        catch(HomeException he)
	        {
	        	LogSupport.minor(ctx, this, "HomeExcption encountred while trying to fetch entity configuration for entity :" + entity.getType(),he);
	        	return GenericEntityApiResponseSupport.createGenericResponse(ctx, GenericEntityApiResponseSupport.INTENAL_ERROR, 
        				GenericEntityApiResponseSupport.INTENAL_ERROR_MESSAGE);
	        }
	        
	        //Step 1 : Entity level Security
	        //Not for phase 1
	        
	        //Step 2 : Adapt
	        GenericEntityAdapter adapter = GenericEntityApiConfigurationSupport.getAdapter(ctx, config.getCreateOperation().getAdapter());
	        
	        if(adapter == null)
	        {
	        	return GenericEntityApiResponseSupport.createGenericResponse(ctx, GenericEntityApiResponseSupport.CONFIGURATION_FAULT, 
        				GenericEntityApiResponseSupport.CONFIGURATION_FAULT_MESSAGE);
	        }
	        
	        if(LogSupport.isDebugEnabled(ctx))
	        {
	        	LogSupport.debug(ctx, this, "Using adapter : " + adapter);
	        }
	        Object createBean = null;
	        try
	        {
	        	 createBean = adapter.adapt(ctx, entity);
	        	 if(createBean == null)
	        	 {
	        		 return GenericEntityApiResponseSupport.createGenericResponse(ctx, GenericEntityApiResponseSupport.ADAPTION_FAILED, 
	         				GenericEntityApiResponseSupport.ADAPTION_FAILED_MESSAGE);
	 	        } 
	        	 
	        }
	        catch(EntityParsingException he)
	        {
	        	LogSupport.minor(ctx, this, "EntityParsingException encountred while trying to adapt the entity to BSS bean. Entity Type :" + entity.getType(),he);
	        	return GenericEntityApiResponseSupport.createGenericResponse(ctx, GenericEntityApiResponseSupport.INTENAL_ERROR, 
        				GenericEntityApiResponseSupport.INTENAL_ERROR_MESSAGE);
	        }
	        
	        //Step 3 : Business Validation
	        Validator validator = GenericEntityApiConfigurationSupport.getValidator(ctx, config.getCreateOperation().getValidator());
	        if(validator == null)
	        {
	        	return GenericEntityApiResponseSupport.createGenericResponse(ctx, GenericEntityApiResponseSupport.CONFIGURATION_FAULT, 
        				GenericEntityApiResponseSupport.CONFIGURATION_FAULT_MESSAGE);
	        }
	        
	        if(LogSupport.isDebugEnabled(ctx))
	        {
	        	LogSupport.debug(ctx, this, "Using validator : " + validator);
	        }
	        try
	        {
	        	 validator.validate(ctx, createBean);
	        }
	        catch(IllegalStateException ise)
	        {
	        	LogSupport.minor(ctx, this, "IllegalStateException encountred while trying to validate the bean of entity type :" + entity.getType(),ise);
	        	return GenericEntityApiResponseSupport.createGenericResponse(ctx, GenericEntityApiResponseSupport.VALIDATION_FAILED, 
        				GenericEntityApiResponseSupport.VALIDATION_FAILED_MESSAGE+": "+ise.getMessage());
	        
	        }
	        
	        //Step 4 : PreProcess
	        Interceptor interceptor = GenericEntityApiConfigurationSupport.getInterceptor(ctx, config.getCreateOperation().getInterceptor());
	        if(interceptor == null)
	        {
	        	return GenericEntityApiResponseSupport.createGenericResponse(ctx, GenericEntityApiResponseSupport.CONFIGURATION_FAULT, 
        				GenericEntityApiResponseSupport.CONFIGURATION_FAULT_MESSAGE);
	        }
	        
	        if(LogSupport.isDebugEnabled(ctx))
	        {
	        	LogSupport.debug(ctx, this, "Using interceptor : " + interceptor);
	        }
	        
	        Home home = interceptor.getHome(ctx, createBean);
	        interceptor.preProcess(ctx, createBean);
	        if(home == null)
	        {
	        	return GenericEntityApiResponseSupport.createGenericResponse(ctx, GenericEntityApiResponseSupport.INTENAL_ERROR, 
        				GenericEntityApiResponseSupport.INTENAL_ERROR_MESSAGE);
	        }
	        if(LogSupport.isDebugEnabled(ctx))
	        {
	        	LogSupport.debug(ctx, this, "Generic Entity = " + createBean.getClass().getName() + ", Home = " + home);
	        }
	        //Step 5 : Create
	        try
	        {
	        	createBean = home.create(ctx, createBean);
	        }
	        catch(GenericEntityOperationHomeException e)
	        {
	            LogSupport.minor(ctx, this, "Exception encountred while trying to create the bean for entity type : " + entity.getType(), e);
                return GenericEntityApiResponseSupport.createGenericResponse(ctx, 
                        String.valueOf(e.getEntityOperationCode()), e.getLocalizedMessage());
	        }
	        catch(HomeException he)
	        {
	        	LogSupport.minor(ctx, this, "HomeException encountred while trying to create the bean for entity type : " + entity.getType(),he);
	        	return GenericEntityApiResponseSupport.createGenericResponse(ctx, GenericEntityApiResponseSupport.INTENAL_ERROR, 
        				GenericEntityApiResponseSupport.INTENAL_ERROR_MESSAGE + ": "+ he.getLocalizedMessage());
	        }
	        
	        //Step 6 : PostProcess
	        interceptor.postProcess(ctx, createBean);
	        
	        //Step 7 : Unadapt
	        Object returnObject = null;
			try 
			{
				returnObject = adapter.unAdapt(ctx, createBean);
			} 
			catch (EntityParsingException he) 
			{
				LogSupport.minor(ctx, this, "EntityParsingException encountred while trying to un-adapt the BSS bean to entity for Entity Type :" + entity.getType(),he);
	        	return GenericEntityApiResponseSupport.createGenericResponse(ctx, GenericEntityApiResponseSupport.UNADAPTION_FAILED, 
        				GenericEntityApiResponseSupport.UNADAPTION_FAILED_MESSAGE);
	        }
			
			if(returnObject == null || !(returnObject instanceof Entity ))
			{
	        	return GenericEntityApiResponseSupport.createGenericResponse(ctx, GenericEntityApiResponseSupport.UNADAPTION_FAILED, 
        				GenericEntityApiResponseSupport.UNADAPTION_FAILED_MESSAGE);
			}
			
	        Entity returnEntity = (Entity) returnObject;
	        GenericParameter  createdEntity = new GenericParameter();
	        createdEntity.setName(ENTITY_CREATED);
	        createdEntity.setValue(returnEntity);
	        
	        paramList.add(createdEntity);
	        
	        GenericResponse genericResponse = GenericEntityApiResponseSupport.createGenericResponse(ctx, GenericEntityApiResponseSupport.SUCCESS, "Created Successfully", paramList);
	        
	        return genericResponse;
		}

		@Override
		public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
		{
			Object[] result = null;
		    if (isGenericExecution(ctx, parameters))
		    {
		        result = new Object[3];
		        result[0] = parameters[0];
		        result[1] = getParameter(ctx, PARAM_ENTITY, PARAM_ENTITY_NAME, Entity.class, parameters);
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
		    result = result && Entity.class.isAssignableFrom(parameterTypes[PARAM_ENTITY]);
		    result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
		    return result;
		}
		
		@Override
		public boolean validateReturnType(Class<?> resultType)
		{
		    return GenericResponse.class.isAssignableFrom(resultType);
		}
		
		public static final int PARAM_HEADER = 0;
		public static final int PARAM_ENTITY = 1;
		public static final int PARAM_GENERIC_PARAMETERS = 2;
		
		public static final String PARAM_HEADER_NAME = "header";
		public static final String PARAM_ENTITY_NAME = "entity";
		public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
		
		public static final String ENTITY_CREATED = "EntityCreated" ;

	}

	public static class RetrieveEntityQueryExecutor extends AbstractQueryExecutor<GenericResponse>
	{

		public RetrieveEntityQueryExecutor()
		{
		
		}

		@Override
		public GenericResponse execute(Context parentCtx, Object... parameters) throws CRMExceptionFault
		{
			//TODO: It's just a skeleton Implementation, actual implementation to follow later
			Context ctx = parentCtx.createSubContext();
			
			CRMRequestHeader header = getParameter(ctx, PARAM_HEADER, PARAM_HEADER_NAME, CRMRequestHeader.class, parameters);
	    	Entity entity = getParameter(ctx, PARAM_ENTITY, PARAM_ENTITY_NAME, Entity.class, parameters);
	        GenericParameter[] genericParameters = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);
	
	        RmiApiErrorHandlingSupport.validateMandatoryObject(entity, PARAM_ENTITY_NAME);
	        
	        List<GenericParameter> paramList = new ArrayList<GenericParameter>();
	        ctx.put(RETURN_GENERIC_PARAM_LIST, paramList);
	        
	        GenericEntityApiConfiguration config = null;
	        try
	        {
	        	config  = GenericEntityApiConfigurationSupport.getEntityConfiguration(ctx, entity);
	        	if(config == null)
	        	{
	        		return GenericEntityApiResponseSupport.createGenericResponse(ctx, GenericEntityApiResponseSupport.ENTITY_NOT_SUPPORTED, 
	        				"Entity " + entity.getType() + " is not supported by the system.");
	        	}
	        }
	        catch(HomeException he)
	        {
	        	LogSupport.minor(ctx, this, "HomeExcption encountred while trying to fetch entity configuration for entity :" + entity.getType(),he);
	        	return GenericEntityApiResponseSupport.createGenericResponse(ctx, GenericEntityApiResponseSupport.INTENAL_ERROR, 
        				GenericEntityApiResponseSupport.INTENAL_ERROR_MESSAGE);
	        }
	        
	        
	        
	        //Step 1 : Entity level Security
	        //Not for phase 1
	        
	        
	        GenericEntityAdapter adapter = GenericEntityApiConfigurationSupport.getAdapter(ctx, config.getRetrieveOperation().getAdapter());
	        
	        if(adapter == null)
	        {
	        	return GenericEntityApiResponseSupport.createGenericResponse(ctx, GenericEntityApiResponseSupport.CONFIGURATION_FAULT, 
        				GenericEntityApiResponseSupport.CONFIGURATION_FAULT_MESSAGE);
	        }
	        
	        if(LogSupport.isDebugEnabled(ctx))
	        {
	        	LogSupport.debug(ctx, this, "Using adapter : " + adapter);
	        }
	        Validator validator = GenericEntityApiConfigurationSupport.getValidator(ctx, config.getRetrieveOperation().getValidator());
	        if(validator == null)
	        {
	        	return GenericEntityApiResponseSupport.createGenericResponse(ctx, GenericEntityApiResponseSupport.CONFIGURATION_FAULT, 
        				GenericEntityApiResponseSupport.CONFIGURATION_FAULT_MESSAGE);
	        }
	        
	        if(LogSupport.isDebugEnabled(ctx))
	        {
	        	LogSupport.debug(ctx, this, "Using validator : " + validator);
	        }
	        Interceptor interceptor = GenericEntityApiConfigurationSupport.getInterceptor(ctx, config.getRetrieveOperation().getInterceptor());
	        if(interceptor == null)
	        {
	        	return GenericEntityApiResponseSupport.createGenericResponse(ctx, GenericEntityApiResponseSupport.CONFIGURATION_FAULT, 
        				GenericEntityApiResponseSupport.CONFIGURATION_FAULT_MESSAGE);
	        }
	        
	        if(LogSupport.isDebugEnabled(ctx))
	        {
	        	LogSupport.debug(ctx, this, "Using interceptor : " + interceptor);
	        }
	        
	        //Step 2 : Adapt
	        
	        Object retrieveBean = null;
	        try
	        {
	        	retrieveBean = adapter.adapt(ctx, entity);
	        	 if(retrieveBean == null)
	        	 {
	        		 return GenericEntityApiResponseSupport.createGenericResponse(ctx, GenericEntityApiResponseSupport.ADAPTION_FAILED, 
	         				GenericEntityApiResponseSupport.ADAPTION_FAILED_MESSAGE);
	 	        } 
	        	 
	        }
	        catch(EntityParsingException he)
	        {
	        	LogSupport.minor(ctx, this, "EntityParsingException encountred while trying to adapt the entity to BSS bean. Entity Type :" + entity.getType(),he);
	        	return GenericEntityApiResponseSupport.createGenericResponse(ctx, GenericEntityApiResponseSupport.INTENAL_ERROR, 
        				GenericEntityApiResponseSupport.INTENAL_ERROR_MESSAGE);
	        }
	        
	        Object filter = null;
	        try
	        {
	        	filter = adapter.getRetrieveCriteria(ctx, entity, retrieveBean, genericParameters);
	        	 if(filter == null)
	        	 {
	        		 return GenericEntityApiResponseSupport.createGenericResponse(ctx, GenericEntityApiResponseSupport.ADAPTION_FAILED, 
	         				GenericEntityApiResponseSupport.ADAPTION_FAILED_MESSAGE);
	 	        } 
	        	 
	        }
	        catch(EntityParsingException he)
	        {
	        	LogSupport.minor(ctx, this, "EntityParsingException encountred while trying to adapt the entity to BSS bean. Entity Type :" + entity.getType(),he);
	        	return GenericEntityApiResponseSupport.createGenericResponse(ctx, GenericEntityApiResponseSupport.INTENAL_ERROR, 
        				GenericEntityApiResponseSupport.INTENAL_ERROR_MESSAGE);
	        }
	        
	        Home home = interceptor.getHome(ctx, retrieveBean);
	        if(home == null)
	        {
	        	return GenericEntityApiResponseSupport.createGenericResponse(ctx, GenericEntityApiResponseSupport.INTENAL_ERROR, 
        				GenericEntityApiResponseSupport.INTENAL_ERROR_MESSAGE);
	        }
	        
	        
	        //Step 3 : Business Validation
	        
	        try
	        {
	        	 validator.validate(ctx, retrieveBean);
	        }
	        catch(IllegalStateException ise)
	        {
	        	LogSupport.minor(ctx, this, "IllegalStateException encountred while trying to validate the bean of entity type :" + entity.getType(),ise);
	        	return GenericEntityApiResponseSupport.createGenericResponse(ctx, GenericEntityApiResponseSupport.VALIDATION_FAILED, 
        				GenericEntityApiResponseSupport.VALIDATION_FAILED_MESSAGE+": "+ise.getMessage());
	        
	        }
	        
	        //Step 4 : PreProcess
	        
	        interceptor.preProcess(ctx, retrieveBean);
	        
	        //Step 5 : Create
	        Object retrievedBean = null;
	        Collection collection = null;
	        try
	        {
	            Object object = adapter.adaptQuery(ctx, entity);
	            
                Home queryHome = home.where(ctx, object);
                collection = queryHome.selectAll();
	        }
	        catch (EntityParsingException e)
            {
	            LogSupport.minor(ctx, this, "EntityParsingException encountred while generate query : " + entity.getType(),e);
                return GenericEntityApiResponseSupport.createGenericResponse(ctx, GenericEntityApiResponseSupport.INTENAL_ERROR, 
                        GenericEntityApiResponseSupport.INTENAL_ERROR_MESSAGE);
            }
	        catch(HomeException he)
	        {
	        	LogSupport.minor(ctx, this, "HomeExcption encountred while trying to retrieve the bean for entity type : " + entity.getType(),he);
	        	return GenericEntityApiResponseSupport.createGenericResponse(ctx, GenericEntityApiResponseSupport.INTENAL_ERROR, 
        				GenericEntityApiResponseSupport.INTENAL_ERROR_MESSAGE);
	        }
	        
	        //Step 6 : PostProcess
	        interceptor.postProcess(ctx, collection);
	        
	        //Step 7 : Unadapt
	        EntitySequence entitySequence = new EntitySequence(); 
	        Object returnObject = null;
			try 
			{
				for(Object bean : collection)
				{
    				if(bean != null)
    				{
    					returnObject = adapter.unAdapt(ctx, bean);
    					if(returnObject == null || !(returnObject instanceof Entity ))
    					{
    						return GenericEntityApiResponseSupport.createGenericResponse(ctx, GenericEntityApiResponseSupport.UNADAPTION_FAILED, 
    								GenericEntityApiResponseSupport.UNADAPTION_FAILED_MESSAGE);
    					}
    					entitySequence.addEntity((Entity)returnObject);
    				}
				}
			} 
			catch (EntityParsingException he) 
			{
				LogSupport.minor(ctx, this, "EntityParsingException encountred while trying to un-adapt the BSS bean to entity for Entity Type :" + entity.getType(),he);
	        	return GenericEntityApiResponseSupport.createGenericResponse(ctx, GenericEntityApiResponseSupport.UNADAPTION_FAILED, 
        				GenericEntityApiResponseSupport.UNADAPTION_FAILED_MESSAGE);
	        }
			
	        GenericParameter  retievedEntity = new GenericParameter();
	        retievedEntity.setName(RETRIEVED_ENTITIES);
	        retievedEntity.setValue(entitySequence);
	        
	        paramList.add(retievedEntity);
	        
	        GenericResponse genericResponse = GenericEntityApiResponseSupport.createGenericResponse(ctx, GenericEntityApiResponseSupport.SUCCESS, "Operation completed successfully", paramList);
	        
	        return genericResponse;
		}

		@Override
		public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
		{
			Object[] result = null;
		    if (isGenericExecution(ctx, parameters))
		    {
		        result = new Object[3];
		        result[0] = parameters[0];
		        result[1] = getParameter(ctx, PARAM_ENTITY, PARAM_ENTITY_NAME, Entity.class, parameters);
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
		    result = result && Entity.class.isAssignableFrom(parameterTypes[PARAM_ENTITY]);
		    result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
		    return result;
		}
		
		@Override
		public boolean validateReturnType(Class<?> resultType)
		{
		    return GenericResponse.class.isAssignableFrom(resultType);
		}
		
		public static final int PARAM_HEADER = 0;
		public static final int PARAM_ENTITY = 1;
		public static final int PARAM_GENERIC_PARAMETERS = 2;
		
		public static final String PARAM_HEADER_NAME = "header";
		public static final String PARAM_ENTITY_NAME = "entity";
		public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
		
		public static final String RETRIEVED_ENTITIES = "RetrievedEntities";
		
	}
	
	public static class UpdateEntityQueryExecutor extends AbstractQueryExecutor<GenericResponse>
	{

		public UpdateEntityQueryExecutor()
		{
		
		}

		@Override
		public GenericResponse execute(Context parentCtx, Object... parameters) throws CRMExceptionFault
		{
			//TODO: It's just a skeleton Implementation, actual implementation to follow later
			Context ctx = parentCtx.createSubContext();
			
			CRMRequestHeader header = getParameter(ctx, PARAM_HEADER, PARAM_HEADER_NAME, CRMRequestHeader.class, parameters);
	    	Entity entity = getParameter(ctx, PARAM_ENTITY, PARAM_ENTITY_NAME, Entity.class, parameters);
	        GenericParameter[] genericParameters = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);
	
	        RmiApiErrorHandlingSupport.validateMandatoryObject(entity, PARAM_ENTITY_NAME);
	        
	        List<GenericParameter> paramList = new ArrayList<GenericParameter>();
	        ctx.put(RETURN_GENERIC_PARAM_LIST, paramList);
	        
	        GenericEntityApiConfiguration config = null;
	        try
	        {
	        	config  = GenericEntityApiConfigurationSupport.getEntityConfiguration(ctx, entity);
	        	if(config == null)
	        	{
	        		return GenericEntityApiResponseSupport.createGenericResponse(ctx, GenericEntityApiResponseSupport.ENTITY_NOT_SUPPORTED, 
	        				"Entity " + entity.getType() + " is not supported by the system.");
	        	}
	        }
	        catch(HomeException he)
	        {
	        	LogSupport.minor(ctx, this, "HomeExcption encountred while trying to fetch entity configuration for entity :" + entity.getType(),he);
	        	return GenericEntityApiResponseSupport.createGenericResponse(ctx, GenericEntityApiResponseSupport.INTENAL_ERROR, 
        				GenericEntityApiResponseSupport.INTENAL_ERROR_MESSAGE);
	        }
	        
	        
	        
	        //Step 1 : Entity level Security
	        //Not for phase 1
	        
	        
	        GenericEntityAdapter adapter = GenericEntityApiConfigurationSupport.getAdapter(ctx, config.getUpdateOperation().getAdapter());
	        
	        if(adapter == null)
	        {
	        	return GenericEntityApiResponseSupport.createGenericResponse(ctx, GenericEntityApiResponseSupport.CONFIGURATION_FAULT, 
        				GenericEntityApiResponseSupport.CONFIGURATION_FAULT_MESSAGE);
	        }
	        
	        if(LogSupport.isDebugEnabled(ctx))
	        {
	        	LogSupport.debug(ctx, this, "Using adapter : " + adapter);
	        }
	        Validator validator = GenericEntityApiConfigurationSupport.getValidator(ctx, config.getUpdateOperation().getValidator());
	        if(validator == null)
	        {
	        	return GenericEntityApiResponseSupport.createGenericResponse(ctx, GenericEntityApiResponseSupport.CONFIGURATION_FAULT, 
        				GenericEntityApiResponseSupport.CONFIGURATION_FAULT_MESSAGE);
	        }
	        
	        if(LogSupport.isDebugEnabled(ctx))
	        {
	        	LogSupport.debug(ctx, this, "Using validator : " + validator);
	        }
	        Interceptor interceptor = GenericEntityApiConfigurationSupport.getInterceptor(ctx, config.getUpdateOperation().getInterceptor());
	        if(interceptor == null)
	        {
	        	return GenericEntityApiResponseSupport.createGenericResponse(ctx, GenericEntityApiResponseSupport.CONFIGURATION_FAULT, 
        				GenericEntityApiResponseSupport.CONFIGURATION_FAULT_MESSAGE);
	        }
	        
	        if(LogSupport.isDebugEnabled(ctx))
	        {
	        	LogSupport.debug(ctx, this, "Using interceptor : " + interceptor);
	        }
	        
	        //Step 2 : Adapt
	        
	        Object updateBean = null;
	        try
	        {
	        	 updateBean = adapter.adapt(ctx, entity);
	        	 if(updateBean == null)
	        	 {
	        		 return GenericEntityApiResponseSupport.createGenericResponse(ctx, GenericEntityApiResponseSupport.ADAPTION_FAILED, 
	         				GenericEntityApiResponseSupport.ADAPTION_FAILED_MESSAGE);
	 	        } 
	        	 
	        }
	        catch(EntityParsingException he)
	        {
	        	LogSupport.minor(ctx, this, "EntityParsingException encountred while trying to adapt the entity to BSS bean. Entity Type :" + entity.getType(),he);
	        	return GenericEntityApiResponseSupport.createGenericResponse(ctx, GenericEntityApiResponseSupport.INTENAL_ERROR, 
        				GenericEntityApiResponseSupport.INTENAL_ERROR_MESSAGE);
	        }
	        
	        Home home = interceptor.getHome(ctx, updateBean);
	        if(home == null)
	        {
	        	return GenericEntityApiResponseSupport.createGenericResponse(ctx, GenericEntityApiResponseSupport.INTENAL_ERROR, 
        				GenericEntityApiResponseSupport.INTENAL_ERROR_MESSAGE);
	        }
	        
	        Object originalBean = null;
	        try
	        {
	        	originalBean = home.find(ctx, updateBean);
	        }
	        catch(HomeException he)
	        {
	        	LogSupport.minor(ctx, this, "HomeExcption encountred while trying to create the bean for entity type : " + entity.getType(),he);
	        	return GenericEntityApiResponseSupport.createGenericResponse(ctx, GenericEntityApiResponseSupport.INTENAL_ERROR, 
	        			GenericEntityApiResponseSupport.INTENAL_ERROR_MESSAGE);
	        }
	        
	        if(originalBean == null)
	        {
	        	return GenericEntityApiResponseSupport.createGenericResponse(ctx, GenericEntityApiResponseSupport.ENTITY_DOES_NOT_EXISTS, 
        				GenericEntityApiResponseSupport.ENTITY_DOES_NOT_EXISTS_MESSAGE);
	        }
	        
	        try
	        {
	        	updateBean = adapter.adapt(ctx, entity,originalBean);
	        }
	        catch(EntityParsingException he)
	        {
	        	LogSupport.minor(ctx, this, "EntityParsingException encountred while trying to adapt the entity to BSS bean. Entity Type :" + entity.getType(),he);
	        	return GenericEntityApiResponseSupport.createGenericResponse(ctx, GenericEntityApiResponseSupport.ADAPTION_FAILED, 
        				GenericEntityApiResponseSupport.ADAPTION_FAILED_MESSAGE);
	        }
	        
	        
	        //Step 3 : Business Validation
	        
	        try
	        {
	        	 validator.validate(ctx, updateBean);
	        }
	        catch(IllegalStateException ise)
	        {
	        	LogSupport.minor(ctx, this, "IllegalStateException encountred while trying to validate the bean of entity type :" + entity.getType(),ise);
	        	return GenericEntityApiResponseSupport.createGenericResponse(ctx, GenericEntityApiResponseSupport.VALIDATION_FAILED, 
        				GenericEntityApiResponseSupport.VALIDATION_FAILED_MESSAGE+": "+ise.getMessage());
	        
	        }
	        
	        //Step 4 : PreProcess
	        
	        interceptor.preProcess(ctx, updateBean);
	        
	        //Step 5 : Create
	        try
	        {
	        	updateBean = home.store(ctx, updateBean);
	        }
	        catch(HomeException he)
	        {
	        	LogSupport.minor(ctx, this, "HomeExcption encountred while trying to create the bean for entity type : " + entity.getType(),he);
	        	return GenericEntityApiResponseSupport.createGenericResponse(ctx, GenericEntityApiResponseSupport.INTENAL_ERROR, 
        				GenericEntityApiResponseSupport.INTENAL_ERROR_MESSAGE);
	        }
	        
	        //Step 6 : PostProcess
	        interceptor.postProcess(ctx, updateBean);
	        
	        //Step 7 : Unadapt
	        Object returnObject = null;
			try 
			{
				returnObject = adapter.unAdapt(ctx, updateBean);
			} 
			catch (EntityParsingException he) 
			{
				LogSupport.minor(ctx, this, "EntityParsingException encountred while trying to un-adapt the BSS bean to entity for Entity Type :" + entity.getType(),he);
	        	return GenericEntityApiResponseSupport.createGenericResponse(ctx, GenericEntityApiResponseSupport.UNADAPTION_FAILED, 
        				GenericEntityApiResponseSupport.UNADAPTION_FAILED_MESSAGE);
	        }
			
			if(returnObject == null || !(returnObject instanceof Entity ))
			{
	        	return GenericEntityApiResponseSupport.createGenericResponse(ctx, GenericEntityApiResponseSupport.UNADAPTION_FAILED, 
        				GenericEntityApiResponseSupport.UNADAPTION_FAILED_MESSAGE);
			}
			
	        Entity returnEntity = (Entity) returnObject;
	        GenericParameter  updatedEntity = new GenericParameter();
	        updatedEntity.setName(UDPATED_ENTITY);
	        updatedEntity.setValue(returnEntity);
	        
	        paramList.add(updatedEntity);
	        
	        GenericResponse genericResponse = GenericEntityApiResponseSupport.createGenericResponse(ctx, GenericEntityApiResponseSupport.SUCCESS, "Updated Successfully", paramList);
	        
	        return genericResponse;
		}

		@Override
		public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
		{
			Object[] result = null;
		    if (isGenericExecution(ctx, parameters))
		    {
		        result = new Object[3];
		        result[0] = parameters[0];
		        result[1] = getParameter(ctx, PARAM_ENTITY, PARAM_ENTITY_NAME, Entity.class, parameters);
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
		    result = result && Entity.class.isAssignableFrom(parameterTypes[PARAM_ENTITY]);
		    result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
		    return result;
		}
		
		@Override
		public boolean validateReturnType(Class<?> resultType)
		{
		    return GenericResponse.class.isAssignableFrom(resultType);
		}
		
		public static final int PARAM_HEADER = 0;
		public static final int PARAM_ENTITY = 1;
		public static final int PARAM_GENERIC_PARAMETERS = 2;
		
		public static final String PARAM_HEADER_NAME = "header";
		public static final String PARAM_ENTITY_NAME = "entity";
		public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
		
		public static final String UDPATED_ENTITY = "UpdatedEntity" ;

	}
	
	public static class DeleteEntityQueryExecutor extends AbstractQueryExecutor<GenericResponse>
	{

		
		public DeleteEntityQueryExecutor()
		{
		
		}

		@Override
		public GenericResponse execute(Context parentCtx, Object... parameters) throws CRMExceptionFault
		{
			//TODO: It's just a skeleton Implementation, actual implementation to follow later
			Context ctx = parentCtx.createSubContext();
			
			CRMRequestHeader header = getParameter(ctx, PARAM_HEADER, PARAM_HEADER_NAME, CRMRequestHeader.class, parameters);
	    	Entity entity = getParameter(ctx, PARAM_ENTITY, PARAM_ENTITY_NAME, Entity.class, parameters);
	        GenericParameter[] genericParameters = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);
	
	        RmiApiErrorHandlingSupport.validateMandatoryObject(entity, PARAM_ENTITY_NAME);
	        
	        List<GenericParameter> paramList = new ArrayList<GenericParameter>();
	        ctx.put(RETURN_GENERIC_PARAM_LIST, paramList);
	        
	        GenericEntityApiConfiguration config = null;
	        try
	        {
	        	config  = GenericEntityApiConfigurationSupport.getEntityConfiguration(ctx, entity);
	        	if(config == null)
	        	{
	        		return GenericEntityApiResponseSupport.createGenericResponse(ctx, GenericEntityApiResponseSupport.ENTITY_NOT_SUPPORTED, 
	        				"Entity " + entity.getType() + " is not supported by the system.");
	        	}
	        }
	        catch(HomeException he)
	        {
	        	LogSupport.minor(ctx, this, "HomeExcption encountred while trying to fetch entity configuration for entity :" + entity.getType(),he);
	        	return GenericEntityApiResponseSupport.createGenericResponse(ctx, GenericEntityApiResponseSupport.INTENAL_ERROR, 
        				GenericEntityApiResponseSupport.INTENAL_ERROR_MESSAGE);
	        }
	        
	        
	        
	        //Step 1 : Entity level Security
	        //Not for phase 1
	        
	        
	        GenericEntityAdapter adapter = GenericEntityApiConfigurationSupport.getAdapter(ctx, config.getDeleteOperation().getAdapter());
	        
	        if(adapter == null)
	        {
	        	return GenericEntityApiResponseSupport.createGenericResponse(ctx, GenericEntityApiResponseSupport.CONFIGURATION_FAULT, 
        				GenericEntityApiResponseSupport.CONFIGURATION_FAULT_MESSAGE);
	        }
	        
	        if(LogSupport.isDebugEnabled(ctx))
	        {
	        	LogSupport.debug(ctx, this, "Using adapter : " + adapter);
	        }
	        Validator validator = GenericEntityApiConfigurationSupport.getValidator(ctx, config.getDeleteOperation().getValidator());
	        if(validator == null)
	        {
	        	return GenericEntityApiResponseSupport.createGenericResponse(ctx, GenericEntityApiResponseSupport.CONFIGURATION_FAULT, 
        				GenericEntityApiResponseSupport.CONFIGURATION_FAULT_MESSAGE);
	        }
	        
	        if(LogSupport.isDebugEnabled(ctx))
	        {
	        	LogSupport.debug(ctx, this, "Using validator : " + validator);
	        }
	        Interceptor interceptor = GenericEntityApiConfigurationSupport.getInterceptor(ctx, config.getDeleteOperation().getInterceptor());
	        if(interceptor == null)
	        {
	        	return GenericEntityApiResponseSupport.createGenericResponse(ctx, GenericEntityApiResponseSupport.CONFIGURATION_FAULT, 
        				GenericEntityApiResponseSupport.CONFIGURATION_FAULT_MESSAGE);
	        }
	        
	        if(LogSupport.isDebugEnabled(ctx))
	        {
	        	LogSupport.debug(ctx, this, "Using interceptor : " + interceptor);
	        }

	        //Step 2 : Adapt
	        Object deleteBean = null;
	        try
	        {
	        	 deleteBean = adapter.adapt(ctx, entity);
	        	 if(deleteBean == null)
	        	 {
	        		 return GenericEntityApiResponseSupport.createGenericResponse(ctx, GenericEntityApiResponseSupport.ADAPTION_FAILED, 
	         				GenericEntityApiResponseSupport.ADAPTION_FAILED_MESSAGE);
	 	        } 
	        }
	        catch(EntityParsingException he)
	        {
	        	LogSupport.minor(ctx, this, "EntityParsingException encountred while trying to adapt the entity to BSS bean. Entity Type :" + entity.getType(),he);
	        	return GenericEntityApiResponseSupport.createGenericResponse(ctx, GenericEntityApiResponseSupport.INTENAL_ERROR, 
        				GenericEntityApiResponseSupport.INTENAL_ERROR_MESSAGE);
	        }
	        
	        Home home = interceptor.getHome(ctx, deleteBean);
	        if(home == null)
	        {
	        	return GenericEntityApiResponseSupport.createGenericResponse(ctx, GenericEntityApiResponseSupport.INTENAL_ERROR, 
        				GenericEntityApiResponseSupport.INTENAL_ERROR_MESSAGE);
	        }
	        
	        Object originalBean = null;
	        try
	        {
	        	originalBean = home.find(ctx, deleteBean);
	        }
	        catch(HomeException he)
	        {
	        	LogSupport.minor(ctx, this, "HomeExcption encountred while trying to create the bean for entity type : " + entity.getType(),he);
	        	return GenericEntityApiResponseSupport.createGenericResponse(ctx, GenericEntityApiResponseSupport.INTENAL_ERROR, 
	        			GenericEntityApiResponseSupport.INTENAL_ERROR_MESSAGE);
	        }
	        
	        if(originalBean == null)
	        {
	        	return GenericEntityApiResponseSupport.createGenericResponse(ctx, GenericEntityApiResponseSupport.ENTITY_DOES_NOT_EXISTS, 
        				GenericEntityApiResponseSupport.ENTITY_DOES_NOT_EXISTS_MESSAGE);
	        }
	        
	        //Step 3 : Business Validation
	        try
	        {
	        	 validator.validate(ctx, deleteBean);
	        }
	        catch(IllegalStateException ise)
	        {
	        	LogSupport.minor(ctx, this, "IllegalStateException encountred while trying to validate the bean of entity type :" + entity.getType(),ise);
	        	return GenericEntityApiResponseSupport.createGenericResponse(ctx, GenericEntityApiResponseSupport.VALIDATION_FAILED, 
        				GenericEntityApiResponseSupport.VALIDATION_FAILED_MESSAGE);
	        }
	        
	        //Step 4 : PreProcess
	        interceptor.preProcess(ctx, deleteBean);
	        
	        //Step 5 : Create
	        try
	        {
	        	home.remove(ctx, deleteBean);
	        }
	        catch(HomeException he)
	        {
	        	LogSupport.minor(ctx, this, "HomeExcption encountred while trying to create the bean for entity type : " + entity.getType(),he);
	        	return GenericEntityApiResponseSupport.createGenericResponse(ctx, GenericEntityApiResponseSupport.INTENAL_ERROR, 
        				GenericEntityApiResponseSupport.INTENAL_ERROR_MESSAGE);
	        }
	        
	        //Step 6 : PostProcess
	        interceptor.postProcess(ctx, deleteBean);
	        
	        GenericResponse genericResponse = GenericEntityApiResponseSupport.createGenericResponse(ctx, GenericEntityApiResponseSupport.SUCCESS, "Deleted Successfully", paramList);
	        
	        return genericResponse;
		}

		@Override
		public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
		{
			Object[] result = null;
		    if (isGenericExecution(ctx, parameters))
		    {
		        result = new Object[3];
		        result[0] = parameters[0];
		        result[1] = getParameter(ctx, PARAM_ENTITY, PARAM_ENTITY_NAME, Entity.class, parameters);
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
		    result = result && Entity.class.isAssignableFrom(parameterTypes[PARAM_ENTITY]);
		    result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
		    return result;
		}
		
		@Override
		public boolean validateReturnType(Class<?> resultType)
		{
		    return GenericResponse.class.isAssignableFrom(resultType);
		}
		
		public static final int PARAM_HEADER = 0;
		public static final int PARAM_ENTITY = 1;
		public static final int PARAM_GENERIC_PARAMETERS = 2;
		
		public static final String PARAM_HEADER_NAME = "header";
		public static final String PARAM_ENTITY_NAME = "entity";
		public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
	}
}
