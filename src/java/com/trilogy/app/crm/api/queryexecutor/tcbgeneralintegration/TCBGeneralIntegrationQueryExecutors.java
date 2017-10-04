package com.trilogy.app.crm.api.queryexecutor.tcbgeneralintegration;

import com.trilogy.app.crm.api.queryexecutor.AbstractQueryExecutor;
import com.trilogy.app.crm.api.queryexecutor.GenericParametersAdapter;
import com.trilogy.app.crm.api.queryexecutor.QueryExecutorFactory;
import com.trilogy.app.crm.api.rmi.GenericParameterParser;
import com.trilogy.app.crm.api.rmi.support.RmiApiErrorHandlingSupport;
import com.trilogy.app.crm.api.rmi.support.SubscribersApiSupport;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.hlr.HlrSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;
import com.trilogy.util.crmapi.wsdl.v3_0.types.CRMRequestHeader;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.tcbgeneralintegration.Response;

public class TCBGeneralIntegrationQueryExecutors
{
    /**
     * 
     * @author Marcio Marques
     * @since 9.3
     *
     */
    public static class SendQueryExecutor extends AbstractQueryExecutor<Response>
    {
        
        public SendQueryExecutor()
        {
            
        }

        @Override
        public Response execute(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            GenericParameter[] genericParameters = (GenericParameter[]) parameters[0];
            
            RmiApiErrorHandlingSupport.validateMandatoryObject(genericParameters, PARAM_GENERIC_PARAMETERS_NAME);
            
            GenericParameterParser parser = new GenericParameterParser(genericParameters);
            String command = parser.getParameter("Body.Command", String.class);
            CRMRequestHeader header = null;
            
            try
            {
                header =  (CRMRequestHeader) new GenericParametersAdapter<CRMRequestHeader>(CRMRequestHeader.class, "Header").unAdapt(ctx, genericParameters);        
            }
            catch (HomeException e)
            {
                LogSupport.minor(ctx, this, "Unable to retrieve header: " + e.getMessage(), e);
            }
            
            if (command.equals(ISSUE_GLOBAL_PROVISIONING_COMMAND))
            {
                command = ISSUE_GLOBAL_PROVISIONING_COMMAND_METHOD_NAME;
            }
            
            return QueryExecutorFactory.getInstance().execute(ctx, command, Response.class, header, genericParameters);
        }
            

        @Override
        public boolean validateParameterTypes(Class<?>[] parameterTypes)
        {
            boolean result = true;
            result = result && (parameterTypes.length>=1);
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
            return Response.class.isAssignableFrom(returnType);
        }

        @Override
        public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            return parameters;
        }
        
        public static final int PARAM_GENERIC_PARAMETERS = 0;
        
        public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
        
        public static final String ISSUE_GLOBAL_PROVISIONING_COMMAND = "Issue Global Provisioning Command";
        public static final String ISSUE_GLOBAL_PROVISIONING_COMMAND_METHOD_NAME = "TCBGeneralIntegrationServiceSkeletonInterface.issueGlobalProvisioningCommand";
    }
    
    /**
     * 
     * @author Larry Xia
     * @since 9.3
     *
     */
    public static class GlobalProvisioningCommandIssueQueryExecutor extends AbstractQueryExecutor<Response>
    {
        public GlobalProvisioningCommandIssueQueryExecutor()
        {
            
        }

        @Override
        public Response execute(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            GenericParameter[] genericParameters = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);
            
            RmiApiErrorHandlingSupport.validateMandatoryObject(genericParameters, PARAM_GENERIC_PARAMETERS_NAME);

            GenericParameterParser parser = new GenericParameterParser(genericParameters);
            String cmd = parser.getParameter(PARAMETER_NAME_COMMAND, String.class);
            String subid = parser.getParameter(PARAMETER_NAME_SUBID, String.class);
            String msisdn = parser.getParameter(PARAMETER_NAME_MSISDN, String.class);
            String prvcmd = parser.getParameter(PARAMETER_NAME_PRV_CMD, String.class);
            
            RmiApiErrorHandlingSupport.validateMandatoryObject(cmd, PARAMETER_NAME_COMMAND);
            
            if (!cmd.equals(SendQueryExecutor.ISSUE_GLOBAL_PROVISIONING_COMMAND) )
            {
                RmiApiErrorHandlingSupport.simpleValidation(PARAMETER_NAME_COMMAND, "Invalid command provided");
            }
            
            if ( subid == null && msisdn == null)
            {
                RmiApiErrorHandlingSupport.simpleValidation(PARAMETER_NAME_SUBID, "Neither the subscription ID nor the MSISDN were provided");
            }
            
            if (prvcmd == null)
            {
                RmiApiErrorHandlingSupport.simpleValidation(PARAMETER_NAME_PRV_CMD, "Provisioning command not provided");
            }

            Response result = new Response();

            try
            {
                SubscriptionReference subscriptionRef = new SubscriptionReference();
                subscriptionRef.setIdentifier(subid);
                subscriptionRef.setMobileNumber(msisdn);

                Subscriber subscriber = SubscribersApiSupport.getCrmSubscriber(ctx, subscriptionRef, this);

                HlrSupport.updateHlr(ctx, subscriber, prvcmd);
                result.setResponseCode(SUCCESS_CODE);

            }
            catch (final Exception e)
            {
                final String msg = "Unable to retrieve HLR lists";
                RmiApiErrorHandlingSupport.generalException(ctx, e, msg, this);
            }

            return result;

        }

        @Override
        public boolean validateParameterTypes(Class<?>[] parameterTypes)
        {
            boolean result = true;
            result = result && (parameterTypes.length>=2);
            result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
            result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
            return result;
        }

        @Override
        public boolean validateReturnType(Class<?> returnType)
        {
            return Response.class.isAssignableFrom(returnType);
        }

        @Override
        public boolean isGenericExecution(Context ctx, Object... parameters)
        {
            return false;
        }
        
        public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            return parameters;
        }
        
        public static final int PARAM_HEADER = 0;
        public static final int PARAM_GENERIC_PARAMETERS = 1;
        
        public static final String PARAM_HEADER_NAME = "Header";    
        public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";  
        
        public static final String PARAMETER_NAME_COMMAND = "Body.Command";
        public static final String PARAMETER_NAME_PRV_CMD = "Body.ProvisioningCommand";
        public static final String PARAMETER_NAME_SUBID = "Body.TargetSubscriptionID";
        public static final String PARAMETER_NAME_MSISDN = "Body.TargetSubscriptionMSISDN";
        
        public static final Integer SUCCESS_CODE = 0;

    }


}
