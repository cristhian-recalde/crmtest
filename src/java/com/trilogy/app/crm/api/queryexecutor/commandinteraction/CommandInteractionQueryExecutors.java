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
package com.trilogy.app.crm.api.queryexecutor.commandinteraction;

import java.util.Collection;
import java.util.Iterator;

import com.trilogy.app.crm.ContextHelper;
import com.trilogy.app.crm.api.queryexecutor.AbstractQueryExecutor;
import com.trilogy.app.crm.api.rmi.support.RmiApiErrorHandlingSupport;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.HlrCommandTemplate;
import com.trilogy.app.crm.bean.HlrCommandTemplateHome;
import com.trilogy.app.crm.bean.HlrCommandTemplateXInfo;
import com.trilogy.app.crm.bean.HlrProfile;
import com.trilogy.app.crm.bean.HlrProfileHome;
import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.app.crm.hlr.CrmHlrServiceImpl;
import com.trilogy.app.crm.log.ERLogger;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.SystemSupport;
import com.trilogy.app.crm.xdb.HlrCommandXDBSupport;
import com.trilogy.app.crm.xdb.MsisdnXDBSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.SeverityEnum;
import com.trilogy.interfaces.crm.hlr.CrmHlrResponse;
import com.trilogy.interfaces.crm.hlr.HlrID;
import com.trilogy.interfaces.crm.hlr.HlrIDHome;
import com.trilogy.interfaces.crm.hlr.InterfaceCrmHlrConstants;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v2_1.exception.ExceptionCode;
import com.trilogy.util.crmapi.wsdl.v2_1.types.calldetail.UsageTypeReference;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;
import com.trilogy.util.crmapi.wsdl.v3_0.exception.CRMExceptionFactory;
import com.trilogy.util.crmapi.wsdl.v3_0.types.CRMRequestHeader;
import com.trilogy.util.crmapi.wsdl.v3_0.types.commandinteraction.CommandInteractionException;
import com.trilogy.util.crmapi.wsdl.v3_0.types.commandinteraction.CommandInteractionExceptionCode;
import com.trilogy.util.crmapi.wsdl.v3_0.types.commandinteraction.CommandRequest;
import com.trilogy.util.crmapi.wsdl.v3_0.types.commandinteraction.CommandResult;
import com.trilogy.util.crmapi.wsdl.v3_0.types.commandinteraction.CommandTemplate;
import com.trilogy.util.crmapi.wsdl.v3_0.types.commandinteraction.HLRInformation;
import com.trilogy.util.crmapi.wsdl.v3_0.types.commandinteraction.ProfileCode;

/**
 * 
 * @author Marcio Marques
 * @since 9.3
 *
 */
public class CommandInteractionQueryExecutors 
{
    /**
	 * 
	 * @author Marcio Marques
	 * @since 9.3.0
	 *
	 */
	public static class CommandTemplatesListQueryExecutor extends AbstractQueryExecutor<CommandTemplate[]>
	{
		public CommandTemplatesListQueryExecutor()
		{
			
		}

	    public CommandTemplate[] execute(Context ctx, Object... parameters) throws CRMExceptionFault
	    {
            Integer spid = getParameter(ctx, PARAM_SPID, PARAM_SPID_NAME, Integer.class, parameters);

            CommandTemplate[] ret = new CommandTemplate[] {};
            try
            {
 
                
                EQ condition = null;
                if (spid != null )
                {   
                    condition = new EQ(HlrCommandTemplateXInfo.SPID, spid);
                }
                
                Home home = (Home) ctx.get(HlrCommandTemplateHome.class);
                
                if (condition != null)
                {
                    home = home.where(ctx, condition); 
                }
                
                Collection c = home.selectAll(ctx);
                ret = new CommandTemplate[c.size()];
                int i = 0; 
                
                for (Iterator it = c.iterator(); it.hasNext();)
                {
                    HlrCommandTemplate template = (HlrCommandTemplate) it.next(); 
                    CommandTemplate bean = new CommandTemplate(); 
                    bean.setDescription(template.getDescription());
                    bean.setIdentifier(template.getId()); 
                    bean.setName(template.getName()); 
                    bean.setSpid(template.getSpid()); 
                    ret[i] = bean;
                    ++i; 
                }
                

            }
            catch (final Exception e)
            {
                final String msg = "Unable to retrieve CommandTemplate" ; 
                RmiApiErrorHandlingSupport.generalException(ctx, e, msg, this);
            }

            return ret;
	    }
	    
	    @Override
	    public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
	    {
	        Object[] result = null;
	        if (isGenericExecution(ctx, parameters))
	        {
	            result = new Object[3];
	            result[0] = parameters[0];
	            result[1] = getParameter(ctx, PARAM_SPID, PARAM_SPID_NAME, Integer.class, parameters);
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
            result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
	        result = result && Integer.class.isAssignableFrom(parameterTypes[PARAM_SPID]);
	        return result;
	    }

        @Override
        public boolean validateReturnType(Class<?> resultType)
        {
            return CommandTemplate[].class.isAssignableFrom(resultType);
        }

	    public static final int PARAM_HEADER = 0;
	    public static final int PARAM_SPID = 1;
        public static final int PARAM_GENERIC_PARAMETERS = 2;
	    
        public static final String PARAM_SPID_NAME = "spid";
        public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
	}
	
    /**
	 * 
	 * @author Marcio Marques
	 * @since 9.3.0
	 *
	 */
	public static class HLRsListQueryExecutor extends AbstractQueryExecutor<HLRInformation[]>
	{
		public HLRsListQueryExecutor()
		{
			
		}

	    public HLRInformation[] execute(Context ctx, Object... parameters) throws CRMExceptionFault
	    {
            HLRInformation[] ret = new HLRInformation[] {};
            try
            {
 
                final Home home = (Home) ctx.get(HlrIDHome.class);              
  
                Collection c = home.selectAll(ctx); 
                ret = new HLRInformation[c.size()]; 
                int i = 0; 
                
                for (Iterator it = c.iterator(); it.hasNext();)
                {
                    HlrID hlrConfig = (HlrID) it.next(); 
                    HLRInformation bean = new HLRInformation(); 
                    bean.setDescription(hlrConfig.getDescription());
                    bean.setIdentifier(hlrConfig.getId()); 
                    bean.setName(hlrConfig.getName());  
                    ret[i] = bean;
                    ++i; 
                }
                

            }
            catch (final Exception e)
            {
                final String msg = "Unable to retrieve HLR lists" ; 
                RmiApiErrorHandlingSupport.generalException(ctx, e, msg, this);
            }


            return ret;
            
	    }
	    
	    @Override
	    public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
	    {
	        Object[] result = null;
	        if (isGenericExecution(ctx, parameters))
	        {
	            result = new Object[2];
	            result[0] = parameters[0];
                result[1] = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);
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
	        result = result && (parameterTypes.length>=2);
	        result = result && CRMRequestHeader.class.isAssignableFrom(parameterTypes[PARAM_HEADER]);
            result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
	        return result;
	    }

        @Override
        public boolean validateReturnType(Class<?> resultType)
        {
            return HLRInformation[].class.isAssignableFrom(resultType);
        }

	    public static final int PARAM_HEADER = 0;
        public static final int PARAM_GENERIC_PARAMETERS = 1;
	    
        public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
	}
	
    /**
     * 
     * @author Marcio Marques
     * @since 9.3.0
     *
     */
    public static class ProfileCodesListQueryExecutor extends AbstractQueryExecutor<ProfileCode[]>
    {
        public ProfileCodesListQueryExecutor()
        {
            
        }

        public ProfileCode[] execute(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            ProfileCode[] ret = new ProfileCode[] {};
            try
            {
                Home home = (Home) ctx.get(HlrProfileHome.class);
  
                Collection c = home.selectAll(ctx); 
                ret = new ProfileCode[c.size()];
                int i = 0; 
                
                for (Iterator it = c.iterator(); it.hasNext();)
                {
                    HlrProfile profile = (HlrProfile) it.next(); 
                    ProfileCode bean = new ProfileCode(); 
                    bean.setDescription(profile.getDescription());
                    bean.setIdentifier(profile.getId()); 
                    bean.setName(profile.getName());  
                    ret[i] = bean;
                    ++i; 
                }
                

            }
            catch (final Exception e)
            {
                final String msg = "Unable to retrieve Profile codes" ; 
                RmiApiErrorHandlingSupport.generalException(ctx, e, msg, this);
            }

            return ret;            
        }
        
        @Override
        public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            Object[] result = null;
            if (isGenericExecution(ctx, parameters))
            {
                result = new Object[2];
                result[0] = parameters[0];
                result[1] = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);
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
            result = result && (parameterTypes.length>=2);
            result = result && CRMRequestHeader.class.isAssignableFrom(parameterTypes[PARAM_HEADER]);
            result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
            return result;
        }

        @Override
        public boolean validateReturnType(Class<?> resultType)
        {
            return ProfileCode[].class.isAssignableFrom(resultType);
        }

        public static final int PARAM_HEADER = 0;
        public static final int PARAM_GENERIC_PARAMETERS = 1;
        
        public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
    }
	
    /**
     * 
     * @author Marcio Marques
     * @since 9.3.0
     *
     */
    public static class CommandSenderQueryExecutor extends AbstractQueryExecutor<CommandResult>
    {
        public CommandSenderQueryExecutor()
        {
            
        }


        public CommandResult execute(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            CommandRequest request = getParameter(ctx, PARAM_COMMAND_REQUEST, PARAM_COMMAND_REQUEST_NAME,
                    CommandRequest.class, parameters);

            int spid = -1;

            RmiApiErrorHandlingSupport.validateMandatoryObject(request, PARAM_COMMAND_REQUEST_NAME);

            validateCommand(request);

            CrmHlrServiceImpl service = CrmHlrServiceImpl.instance();

            HlrCommandTemplate template = null;

            try
            {
                template = HlrCommandXDBSupport.getTemplateByID(request.getCommandTemplateID());
                if (template == null)
                {
                    throw new HomeException("Fail to find Command HLR tempalte" + request.getCommandTemplateID());
                }

                String msisdn = (String) getParameter(request.getCommandParameters(), KEY_MSISDN);

                if (msisdn != null)
                {
                    Msisdn msisdnObject = MsisdnXDBSupport.getMsisdn(msisdn);
                    if (msisdnObject != null)
                    {
                        if (msisdnObject.getSpid() != template.getSpid())
                        {
                            throw generateException(CommandInteractionExceptionCode.value3,
                                    "Spid mismatching detected, the Spid of msisdn = " + msisdnObject.getSpid()
                                            + " while the command tempalte HLR spid = " + template.getSpid(), null);
                        }
                        spid = msisdnObject.getSpid();
                    }

                }

            }
            catch (HomeException e)
            {
                throw generateException(CommandInteractionExceptionCode.value1, e.getMessage(), e);
            }

            String command = getCommand(template.getHlrCommandTemplate(), request);

            try
            {
                CrmHlrResponse resp = service.process(ContextHelper.getContext(), (short) getHlrID(request, template), command);

                ERLogger.generateHlrCommandInteractionEr(ctx, spid, SystemSupport.getAgent(ctx),
                        (String) getParameter(request.getCommandParameters(), KEY_MSISDN),
                        (String) getParameter(request.getCommandParameters(), KEY_OLD_MSISDN),
                        (String) getParameter(request.getCommandParameters(), KEY_IMSI),
                        (String) getParameter(request.getCommandParameters(), KEY_OLD_IMSI),
                        (Long) getParameter(request.getCommandParameters(), KEY_PROFILE_CODE),
                        (String) getParameter(request.getAdditionalParameters(), KEY_COMMENT), resp.getCrmHlrCode());

                return adaptResponse(resp);
            }
            catch (CRMExceptionFault v30Exception)
            {
                throw v30Exception;
            }
            catch (Exception t)
            {
                throw generateException(CommandInteractionExceptionCode.value2,
                        "Unexpected Internal error" + t.getMessage(), t);
            }

        }
        

        private void validateCommand(CommandRequest request) throws CRMExceptionFault
        {
            if (request.getCommandTemplateID() == null)
            {
                throw generateException(CommandInteractionExceptionCode.value1, "Command Template is null", null);

            }

            if (request.getCommandParameters() == null)
            {
                throw generateException(CommandInteractionExceptionCode.value1, "no parameter is specified", null);
            }

        }


        private CRMExceptionFault generateException(CommandInteractionExceptionCode code, final String message,
                final Exception e) throws CRMExceptionFault
        {
            final Context ctx = ContextHelper.getContext().createSubContext();

            if (e != null)
            {
                RmiApiErrorHandlingSupport.log(ctx, SeverityEnum.MINOR, this, message, e);
            }

            final CommandInteractionException exception = new CommandInteractionException();

            exception.setCommandResult(code);
            exception.setCode(ExceptionCode.GENERAL_EXCEPTION);
            return CRMExceptionFactory.create(exception);
        }


        private static CommandResult adaptResponse(CrmHlrResponse resp) throws CRMExceptionFault
        {
            GenericParameter[] param = new GenericParameter[1];
            param[0] = new GenericParameter();
            param[0].setName(KEY_DATA);
            param[0].setValue(resp.getRawHlrData());

            if (resp.getCrmHlrCode() == InterfaceCrmHlrConstants.HLR_SUCCESS)
            {
                CommandResult ret = new CommandResult();
                ret.setResultCode((long) resp.getCrmHlrCode());
                ret.setResultMessage((resp.getMessage() == null) ? "Updating HLR success" : resp.getMessage());

                ret.setAdditionalParameters(param);
                return ret;
            }
            else
            {
                final CommandInteractionException exception = new CommandInteractionException();
                exception.setCommandResult(CommandInteractionExceptionCode.value2);
                exception.setMessage((resp.getMessage() == null) ? "Fail updating HLR" : resp.getMessage());
                exception.setAdditionalParameters(param);
                exception.setCode(ExceptionCode.GENERAL_EXCEPTION);
                throw CRMExceptionFactory.create(exception);
            }

        }


        private String getCommand(String template, CommandRequest request) throws CRMExceptionFault
        {
            String result = template;

            for (final GenericParameter parameter : request.getCommandParameters())
            {
                final String key = parameter.getName();
                String value = getHlrValue(parameter, key);

                result = result.replaceAll('%' + key + '%', value);
            }

            return result;

        }


        private String getHlrValue(final GenericParameter parameter, final String key) throws CRMExceptionFault
        {
            String value = parameter.getValue().toString();

            // Special case the profile code, which needs to be swapped for a value stored locally.
            if (KEY_PROFILE_CODE.equals(key))
            {
                final HlrProfile hlrProfile;
                try
                {
                    hlrProfile = HlrCommandXDBSupport.getHlrProfile(Long.valueOf(value));
                }
                catch (final Exception e)
                {
                    throw generateException(CommandInteractionExceptionCode.value1, "fail to find hlr profile code", e);
                }

                if (hlrProfile != null)
                {
                    value = hlrProfile.getProfileCode();
                }
                else
                {
                    throw generateException(CommandInteractionExceptionCode.value1, "fail to find hlr profile code",
                            null);
                }
            }
            return value;
        }


        private Object getParameter(GenericParameter[] paraSet, String key)
        {
            if (paraSet != null)
            {
                for (GenericParameter parameter : paraSet)
                {
                    if (key.equals(parameter.getName()))
                    {
                        return parameter.getValue();
                    }
                }
            }

            return null;
        }


        private long getHlrID(CommandRequest request, HlrCommandTemplate template) throws Exception
        {
            Short hlrid = (Short) getParameter(request.getCommandParameters(), KEY_HLR_ID);
            final Context ctx = ContextHelper.getContext().createSubContext();
            if (hlrid == null)
            {
                final CRMSpid crmspid = HomeSupportHelper.get(ctx).findBean(ctx, CRMSpid.class, template.getSpid());
                return crmspid.getDefaultHlrId();
            }
            else
            {
                return hlrid.shortValue();
            }

        }

        
        @Override
        public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            Object[] result = null;
            if (isGenericExecution(ctx, parameters))
            {
                result = new Object[3];
                result[0] = parameters[0];
                result[1] = getParameter(ctx, PARAM_COMMAND_REQUEST, PARAM_COMMAND_REQUEST_NAME, CommandRequest.class, parameters);
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
            result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
            result = result && CommandRequest.class.isAssignableFrom(parameterTypes[PARAM_COMMAND_REQUEST]);
            return result;
        }
        
        @Override
        public boolean validateReturnType(Class<?> resultType)
        {
            return CommandResult.class.isAssignableFrom(resultType);
        }


        public static final int PARAM_HEADER = 0;
        public static final int PARAM_COMMAND_REQUEST = 1;
        public static final int PARAM_GENERIC_PARAMETERS = 2;
        
        public static final String PARAM_COMMAND_REQUEST_NAME = "request";
        public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
    }
    	
    
    
    private static String KEY_HLR_ID = "HLRID";
    private static String KEY_MSISDN = "MSISDN";
    private static String KEY_OLD_MSISDN = "OLD_MSISDN";
    private static String KEY_IMSI = "IMSI";
    private static String KEY_OLD_IMSI = "OLD_IMSI";
    private static String KEY_PROFILE_CODE = "PROFILE_CODE";
    private static String KEY_COMMENT = "COMMENT"; 
    private static String KEY_DATA="DATA"; 
}
