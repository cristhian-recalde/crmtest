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

package com.trilogy.app.crm.provision.gateway;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.omg.CORBA.SystemException;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bulkprovisioning.loader.PRBTBulkProvisioningLoader;
import com.trilogy.app.crm.client.ConnectionState;
import com.trilogy.app.crm.client.ConnectionStatus;
import com.trilogy.app.crm.client.RemoteServiceStatus;
import com.trilogy.app.crm.client.bm.ConnectionProperties;
import com.trilogy.app.crm.client.bm.TimerCachedConnectionProperties;
import com.trilogy.app.crm.provision.CommonProvisionAgentBase;
import com.trilogy.app.crm.provision.service.ErrorCode;
import com.trilogy.app.crm.provision.service.ExecuteResult;
import com.trilogy.app.crm.provision.service.ListResult;
import com.trilogy.app.crm.provision.service.ParameterInfo;
import com.trilogy.app.crm.provision.service.ProvisionResult;
import com.trilogy.app.crm.provision.service.ProvisionService;
import com.trilogy.app.crm.provision.service.ProvisionServiceHelper;
import com.trilogy.app.crm.provision.service.ServiceInfo;
import com.trilogy.app.crm.provision.service.ServiceInfoSetHolder;
import com.trilogy.app.crm.provision.service.ServiceResult;
import com.trilogy.app.crm.provision.service.UpdateResult;
import com.trilogy.app.crm.provision.service.param.InParameter;
import com.trilogy.app.crm.provision.service.param.ParameterValue;
import com.trilogy.app.crm.support.CorbaSupportHelper;
import com.trilogy.app.crm.support.ExternalAppSupport;
import com.trilogy.app.crm.support.SystemStatusSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xlog.log.EntryLogMsg;
import com.trilogy.framework.xlog.log.LogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;
import com.trilogy.service.corba.CorbaClientProperty;
import com.trilogy.util.corba.ConnectionListener;
import com.trilogy.util.corba.CorbaClientException;
import com.trilogy.util.corba.CorbaClientProxy;
import com.trilogy.util.snippet.log.Logger;


/**
 * 
 * @author victor.stratan@redknee.com
 * @since 8.5
 */
public class ServiceProvisionGatewayOldCorbaClient
        implements RemoteServiceStatus, ConnectionListener, ServiceProvisionGatewayClient
{

    public ServiceProvisionGatewayOldCorbaClient(Context ctx)
    {
        fallbackContext_ = new ContextAwareSupport()
        {
            // EMPTY
        };

        fallbackContext_.setContext(ctx);

        connectionProperties_ = new TimerCachedConnectionProperties(CONNECTION_PROPERTIES_KEY);

        state_ = ConnectionState.UNINITIALIZED;
    }


    /**
     * {@inheritDoc}
     */
    public synchronized void connectionDown()
    {
        if (state_ == ConnectionState.DOWN)
        {
            return;
        }

        state_ = ConnectionState.DOWN;

        final Context ctx = fallbackContext_.getContext();

        // Note that the alive check above is also used to ensure that the
        // invalidate() call below does not recurse infinitely.
        invalidate(ctx);

        final String[] arguments = getEntryLogParameters(ctx);
        // TODO change the number
        new EntryLogMsg(12654L, this, CONNECTION_PROPERTIES_KEY, "", arguments, null).log(ctx);
    }


    /**
     * {@inheritDoc}
     */
    public synchronized void connectionUp()
    {
        if (state_ == ConnectionState.UP)
        {
            return;
        }

        state_ = ConnectionState.UP;

        final Context context = fallbackContext_.getContext();
        final String[] arguments = getEntryLogParameters(context);
        // TODO change the number
        new EntryLogMsg(12655L, this, CONNECTION_PROPERTIES_KEY, "", arguments, null).log(context);
    }


    public synchronized ConnectionStatus[] getConnectionStatus()
    {
        final Context context = fallbackContext_.getContext();
        final CorbaClientProperty properties = connectionProperties_.getProperties(context);

        return SystemStatusSupportHelper.get(context).generateConnectionStatus(properties, state_);
    }


    public String getServiceStatus()
    {
        final Context context = fallbackContext_.getContext();
        return SystemStatusSupportHelper.get(context).generateServiceStatusString(isAlive());
    }


    /**
     * {@inheritDoc}
     */
    public String getDescription()
    {
        return SERVICE_DESCRIPTION;
    }


    public String getName()
    {
        return CONNECTION_PROPERTIES_KEY;
    }


    /**
     * {@inheritDoc}
     */
    public boolean isAlive()
    {
        return state_ == ConnectionState.UP;
    }


    public synchronized void invalidate(final Context ctx)
    {
        connectionDown();

        // looks like it's not needed
        //corbaProxy_.invalidate();
        service_ = null;
    }


    private synchronized ProvisionService getService(final Context ctx)
    {
        if (connectionProperties_.updateAvailable(ctx))
        {
            if (Logger.isInfoEnabled())
            {
                Logger.info(ctx, this, "New " + CONNECTION_PROPERTIES_KEY
                        + " connection properties available. Reinitializing.");
            }

            connectionProperties_.refreshProperties(ctx);
            // Proxy only needs to be discarded when the properties change.
            corbaProxy_ = null;
            invalidate(ctx);
            initializeService(ctx);
        }
        else if (service_ == null || service_._non_existent())
        {
            if (Logger.isInfoEnabled())
            {
                Logger.info(ctx, this, CONNECTION_PROPERTIES_KEY + " Null service. Reinitializing.");
            }

            invalidate(ctx);
            initializeService(ctx);
        }

        return service_;
    }


    /**
     * Initializes the CORBA service.
     * 
     * @param ctx The operating context.
     */
    private void initializeService(final Context ctx)
    {
        if (corbaProxy_ == null)
        {
            final CorbaClientProperty properties = connectionProperties_.getProperties(ctx);

            if (properties != null)
            {
                try
                {
                    corbaProxy_ = CorbaSupportHelper.get(ctx).createProxy(ctx, properties, this);
                }
                catch (final CorbaClientException exception)
                {
                    Logger.major(ctx, this, "Failure to create CORBA proxy for " + properties, exception);
                    invalidate(ctx);
                }
            }
            else
            {
                Logger.major(ctx, this, "Failed to find CORBA properties for " + CONNECTION_PROPERTIES_KEY);
                invalidate(ctx);
            }
        }

        if (corbaProxy_ != null)
        {
            org.omg.CORBA.Object objServant = null;
            try
            {
                objServant = corbaProxy_.instance();
            }
            catch (final SystemException exception)
            {
                Logger.major(ctx, this, "Failure while attempting to instantiate proxy.", exception);
                invalidate(ctx);
            }

            if (objServant != null)
            {
                try
                {
                    service_ = ProvisionServiceHelper.narrow(objServant);

                    if (service_ != null)
                    {
                        connectionUp();
                    }
                }
                catch (final SystemException exception)
                {
                    Logger.major(ctx, this, "Failed to narrow to service.", exception);
                    invalidate(ctx);
                }
            }
            else
            {
                Logger.major(ctx, this, "Failed to instantiate proxy.");
                invalidate(ctx);
            }
        }
    }


    /**
     * Gets the EntryLog parameters for 13666(down) and 13667(up).
     * 
     * @param context The operating context.
     * @return Appropriate EntryLog parameters.
     */
    private synchronized String[] getEntryLogParameters(final Context context)
    {
        final String hostname;
        final String port;

        final CorbaClientProperty properties = connectionProperties_.getProperties(context);
        if (properties != null)
        {
            final String nameServiceHost = properties.getNameServiceHost();
            if (nameServiceHost != null)
            {
                hostname = nameServiceHost;
            }
            else
            {
                hostname = "(not set)";
            }

            port = Integer.toString(properties.getNameServicePort());
        }
        else
        {
            final String noConfig = "(no config)";
            hostname = noConfig;
            port = noConfig;
        }

        final String[] parameters =
            {hostname, port, ProvisionService.class.getSimpleName(), CONNECTION_PROPERTIES_KEY,};

        return parameters;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void provision(final Context ctx, final Set<Long> removeList, 
            final Set<Long> addList, final Map<Integer, String> values,
            Subscriber sub)
            throws ServiceProvisionGatewayException
    {
        
        final LogMsg pm = new PMLogMsg(PM_MODULE, "provision", "Provision services");
        ProvisionResult result;
        try
        {
            final ProvisionService service = getService(ctx);
            if (service == null)
            {
                final ServiceProvisionGatewayException exception = new ServiceProvisionGatewayException((short) ExternalAppSupport.NO_CONNECTION,
                        "Service Provision Gateway down or not available.");
                Logger.minor(ctx, this, exception.getMessage(), exception);
                throw exception;
            }

            if (Logger.isInfoEnabled())
            {
                final StringBuilder msgBldr = new StringBuilder();
                msgBldr.append("Sending CORBA request ServiceProvisionGateway.provision with parameters ");
                toStringProvisionRequest(msgBldr, removeList, addList, values);
                Logger.info(ctx, this, msgBldr.toString());
            }

            long[] removeArray = converToLongArray(removeList);
            long[] addArray = converToLongArray(addList);
            final InParameter[] inParamSet = convertToParamSet(values);
            
            short subscriberType = 0;
            String subscriberId = null;
            if (sub == null)
            {
                if (ctx.has(PRBTBulkProvisioningLoader.PRBT_SUBSCRIBER_TYPE))
                {
                    subscriberType = (Short) ctx.get(PRBTBulkProvisioningLoader.PRBT_SUBSCRIBER_TYPE);
                    subscriberId = (String) ctx.get(PRBTBulkProvisioningLoader.PRBT_SUBSCRIBER_ID);
                }
                else
                {
                    ServiceProvisionGatewayException exception = new ServiceProvisionGatewayException(
                            (short) ErrorCode.INVALID_DATA, "Subscriber not found");
                    Logger.minor(ctx, this, exception.getMessage(), exception);
                    throw exception;
                }
            }
            else
            {
                subscriberType = sub.getSubscriberType().getIndex();
                subscriberId = sub.getId();
            }
            
            result = service.provision(removeArray, addArray, inParamSet,
                    subscriberType, subscriberId);

            if (result.resultCode != ErrorCode.SUCCESS)
            {
                final StringBuilder msgBldr = new StringBuilder();
                msgBldr.append("ServiceProvisionGateway.provision query with parameters ");
                toStringProvisionRequest(msgBldr, removeList, addList, values);
                msgBldr.append(" returned with error:");
                List<Long> failedServices = new ArrayList<Long>();
                getVerboseResultMessage(ctx, result, msgBldr, failedServices);
                final String msg = msgBldr.toString();
                Logger.minor(ctx, this, msg);

                final ServiceProvisionGatewayException exception = new ServiceProvisionGatewayException(result.resultCode,
                        msg, failedServices);
                throw exception;
            }
            else if (Logger.isInfoEnabled())
            {
                final StringBuilder msgBldr = new StringBuilder();
                msgBldr.append("ServiceProvisionGateway.provision query with parameters ");
                toStringProvisionRequest(msgBldr, removeList, addList, values);
                msgBldr.append(" SUCCESS");
                Logger.info(ctx, this, msgBldr.toString());
            }

            // TODO treat errors properly
        }
        catch (final ServiceProvisionGatewayException e)
        {
            throw e;
        }
        catch (final org.omg.CORBA.SystemException e)
        {
            final StringBuilder msgBldr = new StringBuilder();
            msgBldr.append("CORBA communication with ServiceProvisionGateway.provision failed");
            msgBldr.append(" with parameters ");
            toStringProvisionRequest(msgBldr, removeList, addList, values);
            final String msg = msgBldr.toString();
            Logger.minor(ctx, this, msg, e);
            final ServiceProvisionGatewayException exception = new ServiceProvisionGatewayException(
                    ExternalAppSupport.REMOTE_EXCEPTION, msg, e);
            throw exception;
        }
        catch (final Throwable t)
        {
            final StringBuilder msgBldr = new StringBuilder();
            msgBldr.append("Unexpected failure in CORBA communication with ServiceProvisionGateway.provision");
            msgBldr.append(" with parameters ");
            toStringProvisionRequest(msgBldr, removeList, addList, values);
            final String msg = msgBldr.toString();
            Logger.minor(ctx, this, msg, t);
            final ServiceProvisionGatewayException exception = new ServiceProvisionGatewayException(
                    ExternalAppSupport.COMMUNICATION_FAILURE, msg, t);
            throw exception;
        }
        finally
        {
            pm.log(ctx);
        }

        return;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void update(final Context ctx, final Set<Long> currentList,
            final Map<Integer, String> keyValues,
            final Map<Integer, String> values, 
            final Subscriber sub) throws ServiceProvisionGatewayException
    {
        final LogMsg pm = new PMLogMsg(PM_MODULE, "update", "Update services");
        UpdateResult result;
        try
        {
            final ProvisionService service = getService(ctx);
            if (service == null)
            {
                final ServiceProvisionGatewayException exception = new ServiceProvisionGatewayException((short) ExternalAppSupport.NO_CONNECTION,
                        "Service Provision Gateway down or not available.");
                Logger.minor(ctx, this, exception.getMessage(), exception);
                throw exception;
            }

            if (Logger.isInfoEnabled())
            {
                final StringBuilder msgBldr = new StringBuilder();
                msgBldr.append("Sending CORBA request ServiceProvisionGateway.update with parameters ");
                toStringUpdateRequest(msgBldr, currentList, keyValues, values);
                Logger.info(ctx, this, msgBldr.toString());
            }

            long[] currentArray = converToLongArray(currentList);
            final InParameter[] inKeyParamSet = convertToParamSet(keyValues);
            final InParameter[] inParamSet = convertToParamSet(values);
            
            short subscriberType = 0;
            String subscriberId = null;
            if (sub == null)
            {
                if (ctx.has(PRBTBulkProvisioningLoader.PRBT_SUBSCRIBER_TYPE))
                {
                    subscriberType = (Short) ctx.get(PRBTBulkProvisioningLoader.PRBT_SUBSCRIBER_TYPE);
                    subscriberId = (String) ctx.get(PRBTBulkProvisioningLoader.PRBT_SUBSCRIBER_ID);
                }
                else
                {
                    ServiceProvisionGatewayException exception = new ServiceProvisionGatewayException(
                            (short) ErrorCode.INVALID_DATA, "Subscriber not found");
                    Logger.minor(ctx, this, exception.getMessage(), exception);
                    throw exception;
                }
            }
            else
            {
                subscriberType = sub.getSubscriberType().getIndex();
                subscriberId = sub.getId();
            }
                        
            result = service.update(currentArray, inKeyParamSet, inParamSet, 
                    subscriberType, subscriberId);

            if (result.resultCode != ErrorCode.SUCCESS)
            {
                final StringBuilder msgBldr = new StringBuilder();
                msgBldr.append("ServiceProvisionGateway.update with parameters ");
                toStringUpdateRequest(msgBldr, currentList, keyValues, values);
                msgBldr.append(" returned with this error: ");
                List<Long> failedServices = new ArrayList<Long>();
                getVerboseResultMessage(ctx, result, msgBldr, failedServices);
                Logger.minor(ctx, this, msgBldr.toString());

                final ServiceProvisionGatewayException exception = new ServiceProvisionGatewayException(result.resultCode,
                        result.resultMessage, failedServices);
                throw exception;
            }
            else if (Logger.isInfoEnabled())
            {
                final StringBuilder msgBldr = new StringBuilder();
                msgBldr.append("Sending CORBA request ServiceProvisionGateway.update with parameters ");
                toStringUpdateRequest(msgBldr, currentList, keyValues, values);
                msgBldr.append(" SUCCESS");
                Logger.info(ctx, this, msgBldr.toString());
            }

            // TODO treat errors properly
        }
        catch (final ServiceProvisionGatewayException e)
        {
            throw e;
        }
        catch (final org.omg.CORBA.SystemException e)
        {
            final StringBuilder msgBldr = new StringBuilder();
            msgBldr.append("CORBA communication with ServiceProvisionGateway.update failed");
            msgBldr.append(" in request=");
            final String msg = msgBldr.toString();
            Logger.minor(ctx, this, msg, e);
            final ServiceProvisionGatewayException exception = new ServiceProvisionGatewayException(
                    ExternalAppSupport.REMOTE_EXCEPTION, msg, e);
            throw exception;
        }
        catch (final Throwable t)
        {
            final StringBuilder msgBldr = new StringBuilder();
            msgBldr.append("Unexpected failure in CORBA communication with ServiceProvisionGateway.update");
            msgBldr.append(" in request=");
            final String msg = msgBldr.toString();
            Logger.minor(ctx, this, msg, t);
            final ServiceProvisionGatewayException exception = new ServiceProvisionGatewayException(
                    ExternalAppSupport.COMMUNICATION_FAILURE, msg, t);
            throw exception;
        }
        finally
        {
            pm.log(ctx);
        }

        return;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(final Context ctx, final int command, final long serviceID,
            Map<Integer, String> values,
            final Subscriber sub)
            throws ServiceProvisionGatewayException
    {
        final LogMsg pm = new PMLogMsg(PM_MODULE, "execute", "Execute command for service");
        ExecuteResult result;
        try
        {
            final ProvisionService service = getService(ctx);
            if (service == null)
            {
                final ServiceProvisionGatewayException exception = new ServiceProvisionGatewayException((short) ExternalAppSupport.NO_CONNECTION,
                        "Service Provision Gateway down or not available.");
                Logger.minor(ctx, this, exception.getMessage(), exception);
                throw exception;
            }

            if (Logger.isInfoEnabled())
            {
                final StringBuilder msgBldr = new StringBuilder();
                msgBldr.append("Sending CORBA request ServiceProvisionGateway.execute with parameters ");
                toStringExecuteRequest(msgBldr, command, serviceID, values);
                Logger.info(ctx, this, msgBldr.toString());
            }

            final InParameter[] inParamSet = convertToParamSet(values);
            result = service.execute(command, serviceID, inParamSet,  sub.getSubscriberType().getIndex(),sub.getId());

            if (result.resultCode != ErrorCode.SUCCESS)
            {
                final StringBuilder msgBldr = new StringBuilder();
                msgBldr.append("ServiceProvisionGateway.execute query with parameters ");
                toStringExecuteRequest(msgBldr, command, serviceID, values);
                msgBldr.append(" returned with this error: ");
                getVerboseResultMessage(ctx, result, msgBldr);
                final String msg = msgBldr.toString();
                Logger.minor(ctx, this, msg);
                final ServiceProvisionGatewayException exception = new ServiceProvisionGatewayException(result.resultCode, msg);
                if("API".equals(ctx.get(CommonProvisionAgentBase.SPG_PROVISIONING_CLIENT_VERSION)))
                {
                	Context appCtx = (Context) ctx.get("app");
                    appCtx.put(CommonProvisionAgentBase.SPG_PROVISIONING_EXAPTION_CODE, result.resultCode);
                    appCtx.put(CommonProvisionAgentBase.SPG_PROVISIONING_EXAPTION, "Service Unavailable or Subscriber not created on HLR. "+result.resultMessage);
                }
                throw exception;
            }
            else if (Logger.isInfoEnabled())
            {
                final StringBuilder msgBldr = new StringBuilder();
                msgBldr.append("Sending CORBA request ServiceProvisionGateway.execute with parameters ");
                toStringExecuteRequest(msgBldr, command, serviceID, values);
                msgBldr.append(" SUCCESS");
                Logger.info(ctx, this, msgBldr.toString());
            }

            // TODO treat errors properly
        }
        catch (final ServiceProvisionGatewayException e)
        {
            throw e;
        }
        catch (final org.omg.CORBA.SystemException e)
        {
            final StringBuilder msgBldr = new StringBuilder();
            msgBldr.append("CORBA communication with ServiceProvisionGateway.execute failed");
            msgBldr.append(" with parameters ");
            toStringExecuteRequest(msgBldr, command, serviceID, values);
            final String msg = msgBldr.toString();
            Logger.minor(ctx, this, msg, e);
            final ServiceProvisionGatewayException exception = new ServiceProvisionGatewayException(
                    ExternalAppSupport.REMOTE_EXCEPTION, msg, e);
            throw exception;
        }
        catch (final Throwable t)
        {
            final StringBuilder msgBldr = new StringBuilder();
            msgBldr.append("Unexpected failure in CORBA communication with ServiceProvisionGateway.execute");
            msgBldr.append(" with parameters ");
            toStringExecuteRequest(msgBldr, command, serviceID, values);
            final String msg = msgBldr.toString();
            Logger.minor(ctx, this, msg, t);
            final ServiceProvisionGatewayException exception = new ServiceProvisionGatewayException(
                    ExternalAppSupport.COMMUNICATION_FAILURE, msg, t);
            throw exception;
        }
        finally
        {
            pm.log(ctx);
        }

        return;
    }




    /**
     * @param msg
     * @param removeList
     * @param addList
     * @param sub
     */
    private void toStringProvisionRequest(final StringBuilder msg, final Set<Long> removeList,
            final Set<Long> addList,
            final Map<Integer, String> values)
    {
        msg.append("[ RemoveList=[");
        for (Number id : removeList)
        {
            msg.append(id);
            msg.append(',');
        }
        msg.append("], AddList=[");
        for (Number id : addList)
        {
            msg.append(id);
            msg.append(',');
        }
        msg.append("], Params=[");
        for (Map.Entry<Integer, String> entry : values.entrySet())
        {   
            Integer key = entry.getKey();
            msg.append(key);
            msg.append('=');

            String value = entry.getValue();

            if (isItBinary(key))
            {
                value = "%BINARY_CODE%";
            }
            msg.append(value);
            msg.append(',');
        }
        msg.append("] ]");
    }

    private boolean isItBinary(final Integer key)
    {
        if (key.equals(new Integer(1010)))
        {
            return true;
        }
        
        return false;
    }
    
    /**
     * @param msg
     * @param currentList
     * @param sub
     */
    private void toStringUpdateRequest(final StringBuilder msg, final Set<Long> currentList,
            final Map<Integer, String> keyValues, final Map<Integer, String> values)
    {
        msg.append("[ CurrentList=[");
        for (Number id : currentList)
        {
            msg.append(id);
            msg.append(',');
        }
        msg.append("], KeyParams=[");
        for (Map.Entry<Integer, String> entry : keyValues.entrySet())
        {
            msg.append(entry.getKey());
            msg.append('=');
            msg.append(entry.getValue());
            msg.append(',');
        }
        msg.append("], Params=[");
        for (Map.Entry<Integer, String> entry : values.entrySet())
        {
            msg.append(entry.getKey());
            msg.append('=');
            msg.append(entry.getValue());
            msg.append(',');
        }
        msg.append("] ]");
    }


    /**
     * @param msg
     * @param command
     * @param serviceID
     * @param sub
     */
    private void toStringExecuteRequest(final StringBuilder msg, final int command, final long serviceID,
            final Map<Integer, String> values)
    {
        msg.append("[ Command=");
        msg.append(command);
        msg.append(", Service=");
        msg.append(serviceID);
        msg.append(", Params=[");
        for (Map.Entry<Integer, String> entry : values.entrySet())
        {
            msg.append(entry.getKey());
            msg.append('=');
            msg.append(entry.getValue());
            msg.append(',');
        }
        msg.append("] ]");
    }


    private StringBuilder getVerboseResultMessage(Context ctx, ProvisionResult result, StringBuilder builder,
            List<Long> failedServices)
    {
        builder.append(" [");
        builder.append(result.resultCode);
        builder.append("] message [");
        builder.append(result.resultMessage);
        builder.append("] ");
        if (result.serviceResults != null && result.serviceResults.length > 0)
        {
            builder.append(" Service errors=[");
            for (final ServiceResult srvResult : result.serviceResults)
            {
                builder.append("Service [");
                builder.append(srvResult.ID);
                builder.append("] errorCode [");
                builder.append(srvResult.resultCode);
                builder.append("] message [");
                builder.append(srvResult.resultMessage);
                builder.append("], ");
                if (srvResult.resultCode != ErrorCode.SUCCESS)
                {
                    failedServices.add(Long.valueOf(srvResult.ID));
                }
            }
            builder.append("] ");
        }

        return builder;
    }

    private StringBuilder getVerboseResultMessage(Context ctx, UpdateResult result, StringBuilder builder,
            List<Long> failedServices)
    {
        builder.append(" [");
        builder.append(result.resultCode);
        builder.append("] message [");
        builder.append(result.resultMessage);
        builder.append("] ");
        if (result.serviceResults != null && result.serviceResults.length > 0)
        {
            builder.append(" Service errors=[");
            for (final ServiceResult srvResult : result.serviceResults)
            {
                builder.append("Service [");
                builder.append(srvResult.ID);
                builder.append("] errorCode [");
                builder.append(srvResult.resultCode);
                builder.append("] message [");
                builder.append(srvResult.resultMessage);
                builder.append("], ");
                if (srvResult.resultCode != ErrorCode.SUCCESS)
                {
                    failedServices.add(Long.valueOf(srvResult.ID));
                }
            }
            builder.append("] ");
        }

        return builder;
    }

    private StringBuilder getVerboseResultMessage(Context ctx, ExecuteResult result, StringBuilder builder)
    {
        builder.append(" [");
        builder.append(result.resultCode);
        builder.append("] ");
        builder.append(result.resultMessage);

        return builder;
    }

    private StringBuilder getVerboseResultMessage(Context ctx, ListResult result, StringBuilder builder)
    {
        builder.append(" [");
        builder.append(result.resultCode);
        builder.append("] ");
        builder.append(result.resultMessage);

        return builder;
    }

    /**
     * @param addList
     * @return
     */
    private long[] converToLongArray(Set<Long> list) throws ServiceProvisionGatewayException
    {
        long[] result = new long[list.size()];
        Iterator<Long> it = list.iterator();
        for (int i = 0; i < result.length; i++)
        {
            final Long number = it.next();
            result[i] = number.longValue();
        }
        return result;
    }


    /**
     * @param values
     * @return
     */
    private InParameter[] convertToParamSet(Map<Integer, String> values)
    {
        final InParameter[] result = new InParameter[values.size()];
        final Iterator<Map.Entry <Integer, String>> it = values.entrySet().iterator();
        for (int i = 0; it.hasNext(); i++)
        {
            final Map.Entry<Integer, String> entry = it.next();
            result[i] = new InParameter(entry.getKey().intValue(), new ParameterValue());
            result[i].value.stringValue(entry.getValue());
        }
        return result;
    }


    protected SPGService convertService(final ServiceInfo srvInfo)
    {
        final SPGService spgServ = new SPGService();
        spgServ.setID(srvInfo.ID);
        spgServ.setName(srvInfo.name);
        final List<SPGParameter> paramsList = spgServ.getParameters();
        for (ParameterInfo parameterInfo : srvInfo.parameters)
        {
            final SPGParameter spgParameter = new SPGParameter();
            spgParameter.setID(parameterInfo.ID);
            spgParameter.setName(parameterInfo.name);
            spgParameter.setDescription(parameterInfo.description);
            spgParameter.setParameterType(parameterInfo.type);

            paramsList.add(spgParameter);
        }

        return spgServ;
    }

    public static final int SUCCESS = 0;

    private static final String CLIENT_NAME = "ServiceProvisionGatewayClient";

    private static final String PM_MODULE = ServiceProvisionGatewayOldCorbaClient.class.getName();

    /**
     * The current state of this client.
     */
    private ConnectionState state_;

    /**
     * The underlying CORBA service.
     */
    private ProvisionService service_;

    /**
     * Provides the context used during object initialization, which is only retained for
     * logging purposes within methods that are not context-oriented. Prefer local
     * contexts where available.
     */
    private final ContextAwareSupport fallbackContext_;

    /**
     * Provides a convenient method of caching the look-up of CORBA connection properties,
     * and of determining when these properties have been updated.
     */
    private final ConnectionProperties connectionProperties_;

    /**
     * The CORBA proxy.
     */
    private CorbaClientProxy corbaProxy_;

    /**
     * The name of the key used to look-up the connection properties in the
     * CorbaClientProperty Home.
     */
    private static final String CONNECTION_PROPERTIES_KEY = CLIENT_NAME;

    /**
     * The service description string.
     */
    private static final String SERVICE_DESCRIPTION = "CORBA client for SPG services";

}
