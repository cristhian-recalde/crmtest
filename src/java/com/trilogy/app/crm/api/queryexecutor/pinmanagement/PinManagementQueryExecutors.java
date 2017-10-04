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
package com.trilogy.app.crm.api.queryexecutor.pinmanagement;


import com.trilogy.app.crm.api.queryexecutor.AbstractQueryExecutor;
import com.trilogy.app.crm.api.rmi.support.RmiApiErrorHandlingSupport;
import com.trilogy.app.crm.client.ProvisioningHomeException;
import com.trilogy.app.crm.support.PinManagerSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v2_1.types.pinmanagement.PinReference;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;
import com.trilogy.util.crmapi.wsdl.v3_0.types.CRMRequestHeader;

/**
 * 
 * @author Marcio Marques
 * @since 9.3
 *
 */
public class PinManagementQueryExecutors 
{
    /**
     * Implements method deletePin
     * @author Marcio Marques
     * @since 9.3
     *
     */
    public static class PinRemovalQueryExecutor extends AbstractQueryExecutor<PinReference>
    {
        public PinRemovalQueryExecutor()
        {
            
        }

        public PinReference execute(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            CRMRequestHeader header = getParameter(ctx, PARAM_HEADER, "header", CRMRequestHeader.class, parameters);
            String msisdn = getParameter(ctx, PARAM_MSISDN, PARAM_MSISDN_NAME, String.class, parameters);
            String erReference = getParameter(ctx, PARAM_ER_REFERENCE, PARAM_ER_REFERENCE_NAME, String.class, parameters);
            GenericParameter[] genericParameters = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);

            RmiApiErrorHandlingSupport.validateMandatoryObject(msisdn, PARAM_MSISDN_NAME);
            RmiApiErrorHandlingSupport.validateMandatoryObject(erReference, PARAM_ER_REFERENCE_NAME);
            
            if (LogSupport.isDebugEnabled(ctx))
            {
	            final StringBuilder buf = new StringBuilder();
	            buf.append("Request Parameters received by deletePin : MSISDN : [");
	            buf.append(msisdn);
	            buf.append("], erReference : [");
	            buf.append(erReference);
	            buf.append("]");
	            LogSupport.debug(ctx, this, buf.toString());
            }
            
            PinReference pinReference = new PinReference();
            short resultCode = DEFAULT_RESULT;
            
            try
            {
                resultCode = PinManagerSupport.deletePin(ctx, msisdn, erReference);
            }
            catch (ProvisioningHomeException pe)
            {
                String msg = "Problem occured while deleting Pin for MSISDN : " + msisdn;
                resultCode = (short) pe.getResultCode();
                // pin manager returned error code
                if (resultCode != -1)
                {
                    msg = msg + ", PIN Manager ErrorCode : " + resultCode;
                    LogSupport.minor(ctx, this, msg + ", " + pe.getMessage(), pe);                    
                }
                else
                {
                    // connection to pin manager was down
                    LogSupport.minor(ctx, this, msg + ", " + pe.getMessage(), pe);
                    RmiApiErrorHandlingSupport.generalException(ctx, pe, msg, this);
                }
            }
            catch (Exception e)
            {
                String msg = "Problem occured while deleting Pin for MSISDN : " + msisdn;            
                LogSupport.minor(ctx, this, msg + ", " + e.getMessage(), e);
                RmiApiErrorHandlingSupport.generalException(ctx, e, msg, this);
            }
            pinReference.setResultCode(new Long(resultCode));
            return pinReference;
        }
        
        @Override
        public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            Object[] result = null;
            if (isGenericExecution(ctx, parameters))
            {
                result = new Object[4];
                result[0] = parameters[0];
                result[1] = getParameter(ctx, PARAM_MSISDN, PARAM_MSISDN_NAME, String.class, parameters);
                result[2] = getParameter(ctx, PARAM_ER_REFERENCE, PARAM_ER_REFERENCE_NAME, String.class, parameters);
                result[3] = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);
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
            result = result && (parameterTypes.length>=4);
            result = result && CRMRequestHeader.class.isAssignableFrom(parameterTypes[PARAM_HEADER]);
            result = result && String.class.isAssignableFrom(parameterTypes[PARAM_MSISDN]);
            result = result && String.class.isAssignableFrom(parameterTypes[PARAM_ER_REFERENCE]);
            result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
            return result;
        }

        @Override
        public boolean validateReturnType(Class<?> resultType)
        {
            return PinReference.class.isAssignableFrom(resultType);
        }

        public static final int PARAM_HEADER = 0;
        public static final int PARAM_MSISDN = 1;
        public static final int PARAM_ER_REFERENCE = 2;
        public static final int PARAM_GENERIC_PARAMETERS = 3;
        
        public static final String PARAM_MSISDN_NAME = "msisdn";
        public static final String PARAM_ER_REFERENCE_NAME = "erReference";
        public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
        
    }
    
    /**
     * Implements method resetPin
     * @author Marcio Marques
     * @since 9.3
     *
     */
    public static class PinResetQueryExecutor extends AbstractQueryExecutor<PinReference>
    {
        public PinResetQueryExecutor()
        {
            
        }

        public PinReference execute(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            CRMRequestHeader header = getParameter(ctx, PARAM_HEADER, "header", CRMRequestHeader.class, parameters);
            String msisdn = getParameter(ctx, PARAM_MSISDN, PARAM_MSISDN_NAME, String.class, parameters);
            String erReference = getParameter(ctx, PARAM_ER_REFERENCE, PARAM_ER_REFERENCE_NAME, String.class, parameters);
            GenericParameter[] genericParameters = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);

            RmiApiErrorHandlingSupport.validateMandatoryObject(msisdn, PARAM_MSISDN_NAME);
            RmiApiErrorHandlingSupport.validateMandatoryObject(erReference, PARAM_ER_REFERENCE_NAME);
            
            if (LogSupport.isDebugEnabled(ctx))
            {
	            final StringBuilder buf = new StringBuilder();
	            buf.append("Request Parameters received by resetPin : MSISDN : [");
	            buf.append(msisdn);
	            buf.append("], erReference : [");
	            buf.append(erReference);
	            buf.append("]");
	            LogSupport.debug(ctx, this, buf.toString());
            }
            
            PinReference pinReference = new PinReference();
            short resultCode = DEFAULT_RESULT;
            
            try
            {
                resultCode = PinManagerSupport.resetPin(ctx, msisdn, erReference);
            }
            catch (ProvisioningHomeException pe)
            {                
                String msg = "Problem occured while setting Pin for MSISDN : " + msisdn;
                resultCode = (short) pe.getResultCode();
                // pin manager returned error code
                if (resultCode != -1)
                {
                    msg = msg + ", PIN Manager ErrorCode : " + resultCode;
                    LogSupport.minor(ctx, this, msg + ", " + pe.getMessage(), pe);                    
                }
                else
                {
                    // connection to pin manager was down
                    LogSupport.minor(ctx, this, msg + ", " + pe.getMessage(), pe);
                    RmiApiErrorHandlingSupport.generalException(ctx, pe, msg, this);
                }
            }
            catch (Exception e)
            {
                String msg = "Problem occured while resetting Pin for MSISDN : " + msisdn;
                LogSupport.minor(ctx, this, msg + ", " + e.getMessage(), e);
                RmiApiErrorHandlingSupport.generalException(ctx, e, msg, this);
            }
            pinReference.setResultCode(new Long(resultCode));
            return pinReference;
        }
        
        @Override
        public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            Object[] result = null;
            if (isGenericExecution(ctx, parameters))
            {
                result = new Object[4];
                result[0] = parameters[0];
                result[1] = getParameter(ctx, PARAM_MSISDN, PARAM_MSISDN_NAME, String.class, parameters);
                result[2] = getParameter(ctx, PARAM_ER_REFERENCE, PARAM_ER_REFERENCE_NAME, String.class, parameters);
                result[3] = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);
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
            result = result && (parameterTypes.length>=4);
            result = result && CRMRequestHeader.class.isAssignableFrom(parameterTypes[PARAM_HEADER]);
            result = result && String.class.isAssignableFrom(parameterTypes[PARAM_MSISDN]);
            result = result && String.class.isAssignableFrom(parameterTypes[PARAM_ER_REFERENCE]);
            result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
            return result;
        }

        @Override
        public boolean validateReturnType(Class<?> resultType)
        {
            return PinReference.class.isAssignableFrom(resultType);
        }

        public static final int PARAM_HEADER = 0;
        public static final int PARAM_MSISDN = 1;
        public static final int PARAM_ER_REFERENCE = 2;
        public static final int PARAM_GENERIC_PARAMETERS = 3;
        
        public static final String PARAM_MSISDN_NAME = "msisdn";
        public static final String PARAM_ER_REFERENCE_NAME = "erReference";
        public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
    }
    
    /**
     * Implements method generatePin
     * @author Marcio Marques
     * @since 9.3
     *
     */
    public static class PinGenerationQueryExecutor extends AbstractQueryExecutor<PinReference>
    {
        public PinGenerationQueryExecutor()
        {
            
        }

        public PinReference execute(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            CRMRequestHeader header = getParameter(ctx, PARAM_HEADER, "header", CRMRequestHeader.class, parameters);
            String msisdn = getParameter(ctx, PARAM_MSISDN, PARAM_MSISDN_NAME, String.class, parameters);
            int spid = getParameter(ctx, PARAM_SPID, PARAM_SPID_NAME, int.class, parameters);
            String erReference = getParameter(ctx, PARAM_ER_REFERENCE, PARAM_ER_REFERENCE_NAME, String.class, parameters);
            GenericParameter[] genericParameters = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);

            RmiApiErrorHandlingSupport.validateMandatoryObject(msisdn, PARAM_MSISDN_NAME);
            RmiApiErrorHandlingSupport.validateMandatoryObject(spid, PARAM_SPID_NAME);
            RmiApiErrorHandlingSupport.validateMandatoryObject(erReference, PARAM_ER_REFERENCE_NAME);
            
            if (LogSupport.isDebugEnabled(ctx))
            {
	            final StringBuilder buf = new StringBuilder();
	            buf.append("Request Parameters received by generatePin : MSISDN : [");
	            buf.append(msisdn);
	            buf.append("], SPID : [");
	            buf.append(spid);
	            buf.append("], erReference : [");
	            buf.append(erReference);
	            buf.append("]");
	            LogSupport.debug(ctx, this, buf.toString());
            }
            
            PinReference pinReference = new PinReference();
            short resultCode = DEFAULT_RESULT;
            
            
            try
            {
                resultCode = PinManagerSupport.generatePin(ctx, msisdn, spid, erReference);
            }
            catch (ProvisioningHomeException pe)
            {                
                String msg = "Problem occured while setting Pin for MSISDN : " + msisdn;
                resultCode = (short) pe.getResultCode();
                // pin manager returned error code
                if (resultCode != -1)
                {
                    msg = msg + ", PIN Manager ErrorCode : " + resultCode;
                    LogSupport.minor(ctx, this, msg + ", " + pe.getMessage(), pe);                    
                }
                else
                {
                    // connection to pin manager was down
                    LogSupport.minor(ctx, this, msg + ", " + pe.getMessage(), pe);
                    RmiApiErrorHandlingSupport.generalException(ctx, pe, msg, this);
                }
            }
            catch (Exception e)
            {
                String msg = "Problem occured while generating Pin for MSISDN : " + msisdn;
                LogSupport.minor(ctx, this, msg + ", " + e.getMessage(), e);
                RmiApiErrorHandlingSupport.generalException(ctx, e, msg, this);
            }
            pinReference.setResultCode(new Long(resultCode));
            return pinReference;
        }
        
        @Override
        public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            Object[] result = null;
            if (isGenericExecution(ctx, parameters))
            {
                result = new Object[5];
                result[0] = parameters[0];
                result[1] = getParameter(ctx, PARAM_MSISDN, PARAM_MSISDN_NAME, String.class, parameters);
                result[2] = getParameter(ctx, PARAM_SPID, PARAM_SPID_NAME, int.class, parameters);
                result[3] = getParameter(ctx, PARAM_ER_REFERENCE, PARAM_ER_REFERENCE_NAME, String.class, parameters);
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
            result = result && String.class.isAssignableFrom(parameterTypes[PARAM_MSISDN]);
            result = result && int.class.isAssignableFrom(parameterTypes[PARAM_SPID]);
            result = result && String.class.isAssignableFrom(parameterTypes[PARAM_ER_REFERENCE]);
            result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
            return result;
        }
        
        @Override
        public boolean validateReturnType(Class<?> resultType)
        {
            return PinReference.class.isAssignableFrom(resultType);
        }


        public static final int PARAM_HEADER = 0;
        public static final int PARAM_MSISDN = 1;
        public static final int PARAM_SPID = 2;
        public static final int PARAM_ER_REFERENCE = 3;
        public static final int PARAM_GENERIC_PARAMETERS = 4;
        
        public static final String PARAM_MSISDN_NAME = "msisdn";
        public static final String PARAM_SPID_NAME = "spid";
        public static final String PARAM_ER_REFERENCE_NAME = "erReference";
        public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
    }
    

    /**
     * Implements method verifyPin
     * @author Marcio Marques
     * @since 9.3
     *
     */
    public static class PinVerificationQueryExecutor extends AbstractQueryExecutor<PinReference>
    {
        public PinVerificationQueryExecutor()
        {
            
        }

        public PinReference execute(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            CRMRequestHeader header = getParameter(ctx, PARAM_HEADER, "header", CRMRequestHeader.class, parameters);
            String msisdn = getParameter(ctx, PARAM_MSISDN, PARAM_MSISDN_NAME, String.class, parameters);
            String pinNumber = getParameter(ctx, PARAM_PIN_NUMBER, PARAM_PIN_NUMBER_NAME, String.class, parameters);
            String erReference = getParameter(ctx, PARAM_ER_REFERENCE, PARAM_ER_REFERENCE_NAME, String.class, parameters);
            GenericParameter[] genericParameters = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);

            RmiApiErrorHandlingSupport.validateMandatoryObject(msisdn, PARAM_MSISDN_NAME);
            RmiApiErrorHandlingSupport.validateMandatoryObject(pinNumber, PARAM_PIN_NUMBER_NAME);
            RmiApiErrorHandlingSupport.validateMandatoryObject(erReference, PARAM_ER_REFERENCE_NAME);
            
            if (LogSupport.isDebugEnabled(ctx))
            {
	            final StringBuilder buf = new StringBuilder();
	            buf.append("Request Parameters received by verifyPin : MSISDN : [");
	            buf.append(msisdn);
	            buf.append("], erReference : [");
	            buf.append(erReference);
	            buf.append("]");
	            LogSupport.debug(ctx, this, buf.toString());
            }
            
            PinReference pinReference = new PinReference();
            short resultCode = DEFAULT_RESULT;
            
            try
            {
                resultCode = PinManagerSupport.verifyPin(ctx, msisdn, pinNumber, erReference);
            }
            catch (ProvisioningHomeException pe)
            {    
                String msg = "Problem occured while setting Pin for MSISDN : " + msisdn;
                resultCode = (short) pe.getResultCode();
                // pin manager returned error code
                if (resultCode != -1)
                {
                    msg = msg + ", PIN Manager ErrorCode : " + resultCode;
                    LogSupport.minor(ctx, this, msg + ", " + pe.getMessage(), pe);                    
                }
                else
                {
                    // connection to pin manager was down
                    LogSupport.minor(ctx, this, msg + ", " + pe.getMessage(), pe);
                    RmiApiErrorHandlingSupport.generalException(ctx, pe, msg, this);
                }
            }
            catch (Exception e)
            {
                String msg = "Problem occured while verifying Pin for MSISDN : " + msisdn;
                LogSupport.minor(ctx, this, msg + ", " + e.getMessage(), e);
                RmiApiErrorHandlingSupport.generalException(ctx, e, msg, this);
            }
            pinReference.setResultCode(new Long(resultCode));
            return pinReference;
        }
        
        @Override
        public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            Object[] result = null;
            if (isGenericExecution(ctx, parameters))
            {
                result = new Object[5];
                result[0] = parameters[0];
                result[1] = getParameter(ctx, PARAM_MSISDN, PARAM_MSISDN_NAME, String.class, parameters);
                result[2] = getParameter(ctx, PARAM_PIN_NUMBER, PARAM_PIN_NUMBER_NAME, String.class, parameters);
                result[3] = getParameter(ctx, PARAM_ER_REFERENCE, PARAM_ER_REFERENCE_NAME, String.class, parameters);
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
            result = result && String.class.isAssignableFrom(parameterTypes[PARAM_MSISDN]);
            result = result && String.class.isAssignableFrom(parameterTypes[PARAM_PIN_NUMBER]);
            result = result && String.class.isAssignableFrom(parameterTypes[PARAM_ER_REFERENCE]);
            result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
            return result;
        }

        @Override
        public boolean validateReturnType(Class<?> resultType)
        {
            return PinReference.class.isAssignableFrom(resultType);
        }

        public static final int PARAM_HEADER = 0;
        public static final int PARAM_MSISDN = 1;
        public static final int PARAM_PIN_NUMBER = 2;
        public static final int PARAM_ER_REFERENCE = 3;
        public static final int PARAM_GENERIC_PARAMETERS = 4;
        
        public static final String PARAM_MSISDN_NAME = "msisdn";
        public static final String PARAM_PIN_NUMBER_NAME = "pinNumber";
        public static final String PARAM_ER_REFERENCE_NAME = "erReference";
        public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
    }

    /**
     * Implements method changePin
     * @author Marcio Marques
     * @since 9.3
     *
     */
    public static class PinModificationQueryExecutor extends AbstractQueryExecutor<PinReference>
    {
        public PinModificationQueryExecutor()
        {
            
        }

        public PinReference execute(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            CRMRequestHeader header = getParameter(ctx, PARAM_HEADER, "header", CRMRequestHeader.class, parameters);
            String msisdn = getParameter(ctx, PARAM_MSISDN, PARAM_MSISDN_NAME, String.class, parameters);
            String oldNumber = getParameter(ctx, PARAM_OLD_NUMBER, PARAM_OLD_NUMBER_NAME, String.class, parameters);
            String newPinNumber1 = getParameter(ctx, PARAM_NEW_PIN_NUMBER_1, PARAM_NEW_PIN_NUMBER_1_NAME, String.class, parameters);
            String newPinNumber2 = getParameter(ctx, PARAM_NEW_PIN_NUMBER_2, PARAM_NEW_PIN_NUMBER_2_NAME, String.class, parameters);
            String erReference = getParameter(ctx, PARAM_ER_REFERENCE, PARAM_ER_REFERENCE_NAME, String.class, parameters);
            GenericParameter[] genericParameters = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);

            RmiApiErrorHandlingSupport.validateMandatoryObject(msisdn, PARAM_MSISDN_NAME);
            RmiApiErrorHandlingSupport.validateMandatoryObject(oldNumber, PARAM_OLD_NUMBER_NAME);
            RmiApiErrorHandlingSupport.validateMandatoryObject(newPinNumber1, PARAM_NEW_PIN_NUMBER_1_NAME);
            RmiApiErrorHandlingSupport.validateMandatoryObject(newPinNumber2, PARAM_NEW_PIN_NUMBER_2_NAME);
            RmiApiErrorHandlingSupport.validateMandatoryObject(erReference, PARAM_ER_REFERENCE_NAME);
            
            if (LogSupport.isDebugEnabled(ctx))
            {
	            final StringBuilder buf = new StringBuilder();
	            buf.append("Request Parameters received by changePin : MSISDN : [");
	            buf.append(msisdn);
	            buf.append("], erReference : [");
	            buf.append(erReference);
	            buf.append("]");
	            LogSupport.debug(ctx, this, buf.toString());
            }
            
            PinReference pinReference = new PinReference();
            short resultCode = DEFAULT_RESULT;
            
            try
            {
                resultCode = PinManagerSupport.changePin(ctx, msisdn, oldNumber, newPinNumber1, newPinNumber2,
                        erReference);
            }
            catch (ProvisioningHomeException pe)
            {    
                String msg = "Problem occured while setting Pin for MSISDN : " + msisdn;
                resultCode = (short) pe.getResultCode();
                // pin manager returned error code
                if (resultCode != -1)
                {
                    msg = msg + ", PIN Manager ErrorCode : " + resultCode;
                    LogSupport.minor(ctx, this, msg + ", " + pe.getMessage(), pe);                    
                }
                else
                {
                    // connection to pin manager was down
                    LogSupport.minor(ctx, this, msg + ", " + pe.getMessage(), pe);
                    RmiApiErrorHandlingSupport.generalException(ctx, pe, msg, this);
                }
            }
            catch (Exception e)
            {
                String msg = "Problem occured while changing Pin for MSISDN : " + msisdn;
                LogSupport.minor(ctx, this, msg + ", " + e.getMessage(), e);
                RmiApiErrorHandlingSupport.generalException(ctx, e, msg, this);
            }
            pinReference.setResultCode(new Long(resultCode));
            return pinReference;
        }
        
        @Override
        public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            Object[] result = null;
            if (isGenericExecution(ctx, parameters))
            {
                result = new Object[7];
                result[0] = parameters[0];
                result[1] = getParameter(ctx, PARAM_MSISDN, PARAM_MSISDN_NAME, String.class, parameters);
                result[2] = getParameter(ctx, PARAM_OLD_NUMBER, PARAM_OLD_NUMBER_NAME, String.class, parameters);
                result[3] = getParameter(ctx, PARAM_NEW_PIN_NUMBER_1, PARAM_NEW_PIN_NUMBER_1_NAME, String.class, parameters);
                result[4] = getParameter(ctx, PARAM_NEW_PIN_NUMBER_2, PARAM_NEW_PIN_NUMBER_2_NAME, String.class, parameters);
                result[5] = getParameter(ctx, PARAM_ER_REFERENCE, PARAM_ER_REFERENCE_NAME, String.class, parameters);
                result[6] = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);
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
            result = result && (parameterTypes.length>=7);
            result = result && CRMRequestHeader.class.isAssignableFrom(parameterTypes[PARAM_HEADER]);
            result = result && String.class.isAssignableFrom(parameterTypes[PARAM_MSISDN]);
            result = result && String.class.isAssignableFrom(parameterTypes[PARAM_OLD_NUMBER]);
            result = result && String.class.isAssignableFrom(parameterTypes[PARAM_NEW_PIN_NUMBER_1]);
            result = result && String.class.isAssignableFrom(parameterTypes[PARAM_NEW_PIN_NUMBER_2]);
            result = result && String.class.isAssignableFrom(parameterTypes[PARAM_ER_REFERENCE]);
            result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
            return result;
        }
        
        @Override
        public boolean validateReturnType(Class<?> resultType)
        {
            return PinReference.class.isAssignableFrom(resultType);
        }


        public static final int PARAM_HEADER = 0;
        public static final int PARAM_MSISDN = 1;
        public static final int PARAM_OLD_NUMBER = 2;
        public static final int PARAM_NEW_PIN_NUMBER_1 = 3;
        public static final int PARAM_NEW_PIN_NUMBER_2 = 4;
        public static final int PARAM_ER_REFERENCE = 5;
        public static final int PARAM_GENERIC_PARAMETERS = 6;
        
        public static final String PARAM_MSISDN_NAME = "msisdn";
        public static final String PARAM_OLD_NUMBER_NAME = "oldNumber";
        public static final String PARAM_NEW_PIN_NUMBER_1_NAME = "newPinNumber1";
        public static final String PARAM_NEW_PIN_NUMBER_2_NAME = "newPinNumber2";
        public static final String PARAM_ER_REFERENCE_NAME = "erReference";
        public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
    }

    /**
     * Implements method setPin
     * @author Marcio Marques
     * @since 9.3
     *
     */
    public static class PinSetQueryExecutor extends AbstractQueryExecutor<PinReference>
    {
        public PinSetQueryExecutor()
        {
            
        }

        public PinReference execute(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            CRMRequestHeader header = getParameter(ctx, PARAM_HEADER, "header", CRMRequestHeader.class, parameters);
            String msisdn = getParameter(ctx, PARAM_MSISDN, PARAM_MSISDN_NAME, String.class, parameters);
            Integer spid = getParameter(ctx, PARAM_SPID, PARAM_SPID_NAME, Integer.class, parameters);
            String newPinNumber = getParameter(ctx, PARAM_NEW_PIN_NUMBER, PARAM_NEW_PIN_NUMBER_NAME, String.class, parameters);
            String erReference = getParameter(ctx, PARAM_ER_REFERENCE, PARAM_ER_REFERENCE_NAME, String.class, parameters);
            GenericParameter[] genericParameters = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);

            RmiApiErrorHandlingSupport.validateMandatoryObject(msisdn, PARAM_MSISDN_NAME);
            RmiApiErrorHandlingSupport.validateMandatoryObject(newPinNumber, PARAM_NEW_PIN_NUMBER_NAME);
            RmiApiErrorHandlingSupport.validateMandatoryObject(erReference, PARAM_ER_REFERENCE_NAME);
            
            if (LogSupport.isDebugEnabled(ctx))
            {
	            final StringBuilder buf = new StringBuilder();
	            buf.append("Request Parameters received by setPin : MSISDN : [");
	            buf.append(msisdn);
	            buf.append("], SPID : [");
	            buf.append(spid);
	            buf.append("], erReference : [");
	            buf.append(erReference);
	            buf.append("]");
	            LogSupport.debug(ctx, this, buf.toString());
            }
            
            PinReference pinReference = new PinReference();
            short resultCode = DEFAULT_RESULT;
            
            try
            {
                resultCode = PinManagerSupport.setPin(ctx, msisdn, spid, newPinNumber, erReference);
            }
            catch (ProvisioningHomeException pe)
            {   
                String msg = "Problem occured while setting Pin for MSISDN : " + msisdn;
                resultCode = (short) pe.getResultCode();                
                // pin manager returned error code
                if (resultCode != -1)
                {
                    msg = msg + ", PIN Manager ErrorCode : " + resultCode;
                    LogSupport.minor(ctx, this, msg + ", " + pe.getMessage(), pe);                    
                }
                else
                {
                    // connection to pin manager was down
                    LogSupport.minor(ctx, this, msg + ", " + pe.getMessage(), pe);
                    RmiApiErrorHandlingSupport.generalException(ctx, pe, msg, this);
                }
            }
            catch (Exception e)
            {
                String msg = "Problem occured while setting Pin for MSISDN : " + msisdn;
                LogSupport.minor(ctx, this, msg + ", " + e.getMessage(), e);
                RmiApiErrorHandlingSupport.generalException(ctx, e, msg, this);
            }
            pinReference.setResultCode(new Long(resultCode));
            return pinReference;
        }
        
        @Override
        public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            Object[] result = null;
            if (isGenericExecution(ctx, parameters))
            {
                result = new Object[6];
                result[0] = parameters[0];
                result[1] = getParameter(ctx, PARAM_MSISDN, PARAM_MSISDN_NAME, String.class, parameters);
                result[2] = getParameter(ctx, PARAM_SPID, PARAM_SPID_NAME, Integer.class, parameters);
                result[3] = getParameter(ctx, PARAM_NEW_PIN_NUMBER, PARAM_NEW_PIN_NUMBER_NAME, String.class, parameters);
                result[4] = getParameter(ctx, PARAM_ER_REFERENCE, PARAM_ER_REFERENCE_NAME, String.class, parameters);
                result[5] = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);
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
            result = result && (parameterTypes.length>=6);
            result = result && CRMRequestHeader.class.isAssignableFrom(parameterTypes[PARAM_HEADER]);
            result = result && String.class.isAssignableFrom(parameterTypes[PARAM_MSISDN]);
            result = result && Integer.class.isAssignableFrom(parameterTypes[PARAM_SPID]);
            result = result && String.class.isAssignableFrom(parameterTypes[PARAM_NEW_PIN_NUMBER]);
            result = result && String.class.isAssignableFrom(parameterTypes[PARAM_ER_REFERENCE]);
            result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
            return result;
        }

        @Override
        public boolean validateReturnType(Class<?> resultType)
        {
            return PinReference.class.isAssignableFrom(resultType);
        }

        public static final int PARAM_HEADER = 0;
        public static final int PARAM_MSISDN = 1;
        public static final int PARAM_SPID = 2;
        public static final int PARAM_NEW_PIN_NUMBER = 3;
        public static final int PARAM_ER_REFERENCE = 4;
        public static final int PARAM_GENERIC_PARAMETERS = 5;
        
        public static final String PARAM_MSISDN_NAME = "msisdn";
        public static final String PARAM_SPID_NAME = "spid";
        public static final String PARAM_NEW_PIN_NUMBER_NAME = "newPinNumber";
        public static final String PARAM_ER_REFERENCE_NAME = "erReference";
        public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
    }
    
    private static final short DEFAULT_RESULT = 0;
    
}
