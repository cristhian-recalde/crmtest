package com.trilogy.app.crm.api.generic.entity.support;

import java.util.List;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v3_0.types.GenericResponse;

public class GenericEntityApiResponseSupport {
	
	public static final String SUCCESS = "0000";
	public static final String INTENAL_ERROR = "0001";
	public static final String ENTITY_NOT_SUPPORTED = "0002";
	public static final String USER_NOT_AUTHORIZED_FOR_ENTITY = "0003";
	public static final String VALIDATION_FAILED = "0004";
	public static final String CONFIGURATION_FAULT = "0005";
	public static final String ADAPTION_FAILED = "0006";
	public static final String UNADAPTION_FAILED = "0007";
	public static final String ENTITY_DOES_NOT_EXISTS = "0008";
	public static final String FILTER_CREATION_FAILED = "0009";
	
	public static final String SUCCESS_MESSAGE = "Operation Successful";
	public static final String INTENAL_ERROR_MESSAGE = "Internal Error";
	public static final String ENTITY_NOT_SUPPORTED_MESSAGE = "Enity is not supported";
	public static final String USER_NOT_AUTHORIZED_FOR_ENTITY_MESSAGE = "User does not have permission to perform this operation on the entity";
	public static final String VALIDATION_FAILED_MESSAGE = "Validation failed";
	public static final String CONFIGURATION_FAULT_MESSAGE = "Configuration fault";
	public static final String ADAPTION_FAILED_MESSAGE = "Adaption failed";
	public static final String UNADAPTION_FAILED_MESSAGE = "Un-adaption failed";
	public static final String ENTITY_DOES_NOT_EXISTS_MESSAGE = "Entity does not exists in BSS";
	public static final String FILTER_CREATION_FAILED_MESSAGE = "Filter creation failed";


	public static GenericResponse createGenericResponse(Context ctx, String statusCode, String statusMessage)
	{
		return createGenericResponse(ctx, statusCode, statusMessage, null);
	}
	
	public static GenericResponse createGenericResponse(Context ctx, String statusCode, String statusMessage, List<GenericParameter> paramList)
	{
		GenericResponse response = new GenericResponse();
		response.setStatusCode(statusCode);
		response.setStatusMessage(statusMessage);
		
		
		if(paramList != null)
		{
			GenericParameter[] parameters = paramList.toArray(new GenericParameter[0]);
			response.setParameters(parameters);
		}
		
		return response;
	}
	
	

	

}
