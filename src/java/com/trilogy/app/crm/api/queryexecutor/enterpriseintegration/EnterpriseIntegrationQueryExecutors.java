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
package com.trilogy.app.crm.api.queryexecutor.enterpriseintegration;

import com.trilogy.app.crm.api.queryexecutor.AbstractQueryExecutor;
import com.trilogy.app.crm.api.rmi.GenericParameterListParser;
import com.trilogy.app.crm.api.rmi.GenericParameterParser;
import com.trilogy.app.crm.api.rmi.support.RmiApiErrorHandlingSupport;
import com.trilogy.app.crm.bean.GeneralConfig;
import com.trilogy.app.crm.bean.KeyValueFeatureEnum;
import com.trilogy.app.crm.grr.ClientToXMLTemplateConfig;
import com.trilogy.app.crm.grr.GrrGeneratorGeneralConfig;
import com.trilogy.app.crm.grr.XMLTemplateConfig;
import com.trilogy.app.crm.grr.generator.GrrGenerationException;
import com.trilogy.app.crm.grr.generator.RequestXMLGeneratorSupport;
import com.trilogy.app.crm.home.grr.GenericRequestResponseSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;
import com.trilogy.util.crmapi.wsdl.v3_0.types.CRMRequestHeader;

public class EnterpriseIntegrationQueryExecutors {

	/**
	 * 
	 * @author Suyash Gaidhani
	 * @since 9.4
	 *
	 */
	public static class CreateGenericRequestXMLQueryExecutor extends AbstractQueryExecutor<String>
	{

		public CreateGenericRequestXMLQueryExecutor()
		{
		}

		@Override
		public String execute(Context parentCtx, Object... parameters) throws CRMExceptionFault
		{
			Context ctx = parentCtx.createSubContext();

			String generatedRequest = null;
			CRMRequestHeader header = getParameter(ctx, PARAM_HEADER, PARAM_HEADER_NAME, CRMRequestHeader.class, parameters);
			String templateName = getParameter(ctx, PARAM_TEMPLATENAME, PARAM_TEMPLATENAME_NAME, String.class, parameters);
			GenericParameter[] genericParameters = getParameter(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, GenericParameter[].class, parameters);
			GenericParameterParser parser = new GenericParameterParser(genericParameters);
			GenericParameterListParser listParser = new GenericParameterListParser(genericParameters);

			RmiApiErrorHandlingSupport.validateMandatoryObject(templateName, PARAM_TEMPLATENAME_NAME);

			try
			{
				ctx.put(GenericParameterParser.class, parser);
				ctx.put(GenericParameterListParser.class, listParser);
				ctx.put(CRMRequestHeader.class, header);
				ctx.put(PARAM_TEMPLATENAME_NAME, templateName);

				int spid = getSpidSafe(ctx,parser);
				ClientToXMLTemplateConfig mappingConfig = getMappingConfigurationSafe(ctx, header, templateName, spid);
				XMLTemplateConfig templateConfig = getTemplateConfigurationSafe(ctx, mappingConfig);

				ctx.put(ClientToXMLTemplateConfig.class, mappingConfig);
				ctx.put(XMLTemplateConfig.class, templateConfig);
				
				if(LogSupport.isDebugEnabled(ctx))
                {
                    LogSupport.debug(ctx, this, "Evaluting Template Version - <" + templateConfig.getTemplateID() + "> using SPID - <" + spid +
                            ">, USER - <" + header.getUsername() + "> and Template Name - <"+ templateName + ">");
                }
				
				generatedRequest =  templateConfig.getXMLContent();
				generatedRequest = RequestXMLGeneratorSupport.replaceVariables(ctx, generatedRequest, KeyValueFeatureEnum.GRR_XML_GENERATOR, KeyValueFeatureEnum.GENERIC);

			}
			catch(Exception e)
			{
				StringBuilder buf = new StringBuilder();
				buf.append("Unable to Process GRR Generation Request.");
				buf.append(e.getLocalizedMessage());
				RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, e, buf.toString(), this);

			}
			return generatedRequest;
		}

		/**
		 * This method will fetch and return XML Template Configuration bean for the given mapping.
		 * Safe indicates that it will always return not null value or else throw Exception.
		 * @param ctx
		 * @param mappingConfig
		 * @return
		 * @throws GrrGenerationException
		 */
		private XMLTemplateConfig getTemplateConfigurationSafe(Context ctx,
				ClientToXMLTemplateConfig mappingConfig)
						throws GrrGenerationException {

			XMLTemplateConfig templateConfig = null;
			try
			{
				templateConfig = GenericRequestResponseSupport.getXMLTemplateConfig(ctx, mappingConfig.getTemplateID());

			}
			catch(HomeException he)
			{
				StringBuilder buf = new StringBuilder();
				buf.append("HomeException encountered while trying to fetch the XML Template Configuration for the current Request with parameters :");
				buf.append("templateID :");
				buf.append(templateConfig.getTemplateID());
				buf.append(".");

				throw new GrrGenerationException(buf.toString(),he);
			}
			if(templateConfig == null)
			{
				StringBuilder buf = new StringBuilder();
				buf.append("Unable to fetch XML Template Configuration for the current Request with following parameters..");
				buf.append("templateID :");
				buf.append(mappingConfig.getTemplateID());
				buf.append(".");

				throw new GrrGenerationException(buf.toString());
			}

			if(LogSupport.isDebugEnabled(ctx))
			{
				StringBuilder buf = new StringBuilder();
				buf.append("Fetched the templateConfig Successfully :");
				buf.append(templateConfig);
				buf.append(".");
			}
			return templateConfig;
		}

		/**
		 * This method will return ClientToXMLTemplateConfig for the given parameters.
		 * Safe indicates that it will always return not null value or else throw Exception.
		 * @param ctx
		 * @param header
		 * @param templateName
		 * @param spid
		 * @return
		 * @throws GrrGenerationException
		 */
		private ClientToXMLTemplateConfig getMappingConfigurationSafe(
				Context ctx, CRMRequestHeader header, String templateName,
				int spid) throws GrrGenerationException 
				{
			ClientToXMLTemplateConfig mappingConfig = null;
			try
			{
				mappingConfig = GenericRequestResponseSupport.getClientToXMLTemplateConfig(ctx, header.getUsername(), templateName,spid );
			}
			catch(HomeException he)
			{
				StringBuilder buf = new StringBuilder();
				buf.append("Unable to retrieve template for the current Request with following parameters....");
				buf.append("Username :");
				buf.append(header.getUsername());
				buf.append(", SPID :");
				buf.append(spid);
				buf.append(", templateName :");
				buf.append(templateName);
				buf.append(".");

				throw new GrrGenerationException(buf.toString(),he);
			}
			if(mappingConfig == null)
			{
				StringBuilder buf = new StringBuilder();
				buf.append("Unable to retrieve template for the current Request with following parameters....");
				buf.append("Username :");
				buf.append(header.getUsername());
				buf.append(", SPID :");
				buf.append(spid);
				buf.append(", templateName :");
				buf.append(templateName);
				buf.append(".");

				throw new GrrGenerationException(buf.toString());
			}
			return mappingConfig;
				}


		/**
		 * This method will return SPID. If spid is provided as part of input generic parameter, this SPID will be used.
		 * If not, default spid in the General Configuration will be used.
		 * Safe indicates, this method will always return a not null value or else will throw Runtime Exception.
		 * @param ctx
		 * @param parser
		 * @return
		 * @throws GrrGenerationException 
		 */
		private int getSpidSafe(Context ctx, GenericParameterParser parser ) throws GrrGenerationException {

			int spidId = ((GeneralConfig) ctx.get(GeneralConfig.class)).getDefaultGrrSpid();

			try
			{
				String spidStr = parser.getParameter(SPID, java.lang.String.class);
				if (spidStr == null || "".equals(spidStr)){
					spidStr = parser.getParameter(SPID_UpperCase, java.lang.String.class);
				}
				
				if(spidStr != null && !("".equals(spidStr)))
				{
					spidId = Integer.parseInt(spidStr);
				}

			}catch(Exception e)
			{
				StringBuilder buf = new StringBuilder();
				buf.append("Unable to retrieve spid :");
				buf.append(spidId);
				buf.append(".");

				throw new GrrGenerationException(buf.toString());
			}

			return spidId;
		}

		@Override
		public boolean validateParameterTypes(Class<?>[] parameterTypes)
		{
			boolean result = true;
			result = result && (parameterTypes.length>=3);
			result = result && CRMRequestHeader.class.isAssignableFrom(parameterTypes[PARAM_HEADER]);
			result = result && String.class.isAssignableFrom(parameterTypes[PARAM_TEMPLATENAME]);
			result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
			return result;
		}

		@Override
		public boolean isGenericExecution(Context ctx, Object... parameters)
		{
			return false;
		}

		@Override
		public boolean validateReturnType(Class<?> returnType)
		{
			return String.class.isAssignableFrom(returnType);
		}

		@Override
		public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
		{
			return parameters;
		}

		public static final int PARAM_HEADER = 0;
		public static final int PARAM_TEMPLATENAME = 1;
		public static final int PARAM_GENERIC_PARAMETERS = 2;

		public static final String PARAM_HEADER_NAME = "header";
		public static final String PARAM_TEMPLATENAME_NAME = "templateName";
		public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";

		public static final String SPID = "spid";
		public static final String SPID_UpperCase = "SPID";
	}

}
