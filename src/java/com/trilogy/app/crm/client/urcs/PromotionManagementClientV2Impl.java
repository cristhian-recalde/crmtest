package com.trilogy.app.crm.client.urcs;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.trilogy.app.crm.client.AbstractCrmClient;
import com.trilogy.app.crm.client.RemoteServiceException;
import com.trilogy.app.urcs.ParamUtil;
import com.trilogy.app.urcs.param.Parameter;
import com.trilogy.app.urcs.param.ParameterSetHolder;
import com.trilogy.app.urcs.promotion.v2_0.Counter;
import com.trilogy.app.urcs.promotion.v2_0.CounterDelta;
import com.trilogy.app.urcs.promotion.v2_0.CounterHolder;
import com.trilogy.app.urcs.promotion.v2_0.CounterProfile;
import com.trilogy.app.urcs.promotion.v2_0.CounterProfileHolder;
import com.trilogy.app.urcs.promotion.v2_0.CounterSetHolder;
import com.trilogy.app.urcs.promotion.v2_0.Promotion;
import com.trilogy.app.urcs.promotion.v2_0.PromotionErrorCode;
import com.trilogy.app.urcs.promotion.v2_0.PromotionHolder;
import com.trilogy.app.urcs.promotion.v2_0.PromotionManagement;
import com.trilogy.app.urcs.promotion.v2_0.PromotionMgmtParamID;
import com.trilogy.app.urcs.promotion.v2_0.PromotionSetHolder;
import com.trilogy.app.urcs.promotion.v2_0.PromotionType;
import com.trilogy.app.urcs.promotion.v2_0.SubscriberIdentity;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;


/**
 * CORBA version of PromotionManagementClientV2, provides counter API on URCS.
 * 
 * @author amahmood
 * @since 8.5
 */
public class PromotionManagementClientV2Impl extends AbstractCrmClient<PromotionManagement>
        implements PromotionManagementClientV2
{

    private static final Class<PromotionManagement> SERVICE_TYPE = PromotionManagement.class;
    private static final String FAILED_MESSAGE_PREFIX = "CORBA comunication failure during ";
    private static final String FAILED_MESSAGE_SUFFIX = " for subscription ";
    public static final String URCS_SERVICE_NAME = "PromotionManagementClientV2";
    public static final String URCS_SERVICE_DESCRIPTION = "CORBA client for Promotion Management V2 services";


    public PromotionManagementClientV2Impl(final Context ctx)
    {
        super(ctx, URCS_SERVICE_NAME, URCS_SERVICE_DESCRIPTION, SERVICE_TYPE);
    }


    @Override
    public String version()
    {
        return "2.0";
    }


    @Override
    public Counter retrieveCounterForSub(Context ctx, String msisdn, int subscriptionType, long counterProfileID)
            throws RemoteServiceException
    {
        final String methodName = "retrieveCounterForSub";
        final PromotionManagement client = getClient(ctx);
        final CounterHolder counterHolder = new CounterHolder();
        final Parameter[] inParams = new Parameter[0];
        final ParameterSetHolder outParams = new ParameterSetHolder();
        final int resultCode;
        try
        {
            resultCode = client.retrieveCounterForSub(msisdn, subscriptionType, counterProfileID, counterHolder,
                    inParams, outParams);
        }
        catch (final Throwable exception)
        {
            throw new RemoteServiceException(PromotionErrorCode.CONNECTION_ERROR, FAILED_MESSAGE_PREFIX + methodName
                    + FAILED_MESSAGE_SUFFIX, exception);
        }
        if (resultCode == PromotionErrorCode.COUNTER_NOT_FOUND)
        {
            final String msg = String.format("Counter not found on URCS for msisdn=%s, type=%d, counterProfileID=%d",
                    msisdn, subscriptionType, counterProfileID);
            new InfoLogMsg(this.getClass(), msg, null).log(ctx);
            throw new RemoteServiceException(resultCode, msg);
        }
        else if (resultCode != PromotionErrorCode.SUCCESSFUL)
        {
            final String msg = String.format(
                    "Failure [rc:%d] while retrieving counter on URCS for msisdn=%s, type=%d, counterProfileID=%d : " + PromotionReturnCodeMsgMapping.getMessage(resultCode),
                    resultCode, msisdn, subscriptionType, counterProfileID);
            new MinorLogMsg(this.getClass(), msg, null).log(ctx);
            throw new RemoteServiceException(resultCode, msg);
        }
        return counterHolder.value;
    }


    @Override
    public Collection<Counter> updateCounters(Context ctx, String msisdn, int subscriptionType,
            Collection<CounterDelta> deltas) throws RemoteServiceException
    {
        final String methodName = "updateCounters";
        final PromotionManagement client = getClient(ctx);
        final CounterSetHolder counterSetHolder = new CounterSetHolder();
        final Parameter[] inParams = new Parameter[0];
        final ParameterSetHolder outParams = new ParameterSetHolder();
        final int resultCode;
        try
        {
            resultCode = client.updateCounters(msisdn, subscriptionType,
                    deltas.toArray(new CounterDelta[deltas.size()]), counterSetHolder, inParams, outParams);
        }
        catch (final Throwable exception)
        {
            throw new RemoteServiceException(PromotionErrorCode.CONNECTION_ERROR, FAILED_MESSAGE_PREFIX + methodName
                    + FAILED_MESSAGE_SUFFIX, exception);
        }

        if (resultCode != PromotionErrorCode.SUCCESSFUL)
        {
            final String msg = String.format(
                    "Failure [rc:%d] while updating counter(s) on URCS for msisdn=%s, type=%d, deltas=%s : " + PromotionReturnCodeMsgMapping.getMessage(resultCode), 
                    resultCode, msisdn, subscriptionType, getCounterDeltaDescription(deltas));            
            new MinorLogMsg(this.getClass(), msg, null).log(ctx);
            
            throw new RemoteServiceException(resultCode, msg);
        }
        
        
        return Arrays.asList(counterSetHolder.value);
    }


    @Override
    public Collection<Promotion> listAllPromotionsForSpid(Context ctx, int spid, PromotionType type)
        throws RemoteServiceException
    {
        final String methodName = "listAllPromotionsForSpid";
        final PromotionManagement client = getClient(ctx);
        final PromotionSetHolder holder = new PromotionSetHolder();
        final Parameter[] inParams = new Parameter[1];
        final ParameterSetHolder outParams = new ParameterSetHolder();
        final int resultCode;
        try
        {
            if (type != null)
            {
                inParams[0] = new Parameter(PromotionMgmtParamID.PROMOTYPE_FILTER, ParamUtil.createValue((int)type.value()));    
            }
            resultCode = client.listAllPromotionsForSpid(spid, holder, inParams, outParams);
        }
        catch (final Throwable exception)
        {
            throw new RemoteServiceException(PromotionErrorCode.CONNECTION_ERROR, FAILED_MESSAGE_PREFIX + methodName
                    + FAILED_MESSAGE_SUFFIX, exception);
        }
        if (resultCode != PromotionErrorCode.SUCCESSFUL)
        {
            final String msg = "Failure [rc:" + resultCode + "] while listing promotions on URCS: " + PromotionReturnCodeMsgMapping.getMessage(resultCode);
            new MinorLogMsg(this.getClass(), msg, null).log(ctx);
            
            throw new RemoteServiceException(resultCode, msg);
        }
        return Arrays.asList(holder.value);
    }

    @Override
    public Promotion retrievePromotion(Context ctx, long promotionId) throws RemoteServiceException
    {
        final String methodName = "retrievePromotion";
        final PromotionManagement client = getClient(ctx);
        final PromotionHolder holder = new PromotionHolder();
        final Parameter[] inParams = new Parameter[0];
        final ParameterSetHolder outParams = new ParameterSetHolder();
        final int resultCode;
        try
        {
            resultCode = client.retrievePromotion(promotionId, holder, inParams, outParams);
        }
        catch (final Throwable exception)
        {
            throw new RemoteServiceException(PromotionErrorCode.CONNECTION_ERROR, FAILED_MESSAGE_PREFIX + methodName
                    + FAILED_MESSAGE_SUFFIX, exception);
        }
        if (resultCode != PromotionErrorCode.SUCCESSFUL)
        {
            final String msg = "Failure [rc:" + resultCode + "] while retrieving promotion on URCS: " + PromotionReturnCodeMsgMapping.getMessage(resultCode);
            new MinorLogMsg(this.getClass(), msg, null).log(ctx);
            
            throw new RemoteServiceException(resultCode, msg);
        }
        return holder.value;
    }


    @Override
    public CounterProfile retrieveCounterProfile(Context ctx, long profileId) throws RemoteServiceException
    {
        final String methodName = "retrieveCounterProfile";
        final PromotionManagement client = getClient(ctx);
        final CounterProfileHolder holder = new CounterProfileHolder();
        final Parameter[] inParams = new Parameter[0];
        final ParameterSetHolder outParams = new ParameterSetHolder();
        final int resultCode;
        try
        {
            resultCode = client.retrieveCounterProfile(profileId, holder, inParams, outParams);
        }
        catch (final Throwable exception)
        {
            throw new RemoteServiceException(PromotionErrorCode.CONNECTION_ERROR, FAILED_MESSAGE_PREFIX + methodName
                    + FAILED_MESSAGE_SUFFIX, exception);
        }
        if (resultCode != PromotionErrorCode.SUCCESSFUL)
        {
            final String msg = "Failure [rc:" + resultCode + "] while retrieving counter profile on URCS: " + PromotionReturnCodeMsgMapping.getMessage(resultCode);
            new MinorLogMsg(this.getClass(), msg, null).log(ctx);
            
            throw new RemoteServiceException(resultCode, msg);
        }
        return holder.value;
    }


    @Override
    public List<Counter> retrieveAllCountersForSub(Context ctx, String msisdn, int subscriptionType)
            throws RemoteServiceException
    {
        final String methodName = "retrieveAllCountersForSub";
        final PromotionManagement client = getClient(ctx);
        final CounterSetHolder holder = new CounterSetHolder();
        final Parameter[] inParams = new Parameter[0];
        final ParameterSetHolder outParams = new ParameterSetHolder();
        final int resultCode;

        try
        {
            resultCode = client.retrieveAllCountersForSub(msisdn, subscriptionType, holder, inParams, outParams);
        }
        catch (final Throwable exception)
        {
            throw new RemoteServiceException(PromotionErrorCode.CONNECTION_ERROR, FAILED_MESSAGE_PREFIX + methodName
                    + FAILED_MESSAGE_SUFFIX, exception);
        }
        if (resultCode != PromotionErrorCode.SUCCESSFUL)
        {
            final String msg = String.format(
                    "Failure [rc:%d] while retrieving all counters on URCS for msisdn=%s, type=%d : " + PromotionReturnCodeMsgMapping.getMessage(resultCode), 
                    resultCode, msisdn, subscriptionType);            
            new MinorLogMsg(this.getClass(), msg, null).log(ctx);
            
            throw new RemoteServiceException(resultCode, msg);
        }
        return Arrays.asList(holder.value);
    }


    @Override
    public void deleteCounter(Context ctx, String msisdn, int subscriptionType, long counterProfileID)
            throws RemoteServiceException
    {
        final String methodName = "deleteCounter";
        final PromotionManagement client = getClient(ctx);
        final Parameter[] inParams = new Parameter[0];
        final ParameterSetHolder outParams = new ParameterSetHolder();
        final int resultCode;

        try
        {
            resultCode = client.deleteCounter(msisdn, subscriptionType, counterProfileID, inParams, outParams);
        }
        catch (final Throwable exception)
        {
            throw new RemoteServiceException(PromotionErrorCode.CONNECTION_ERROR, FAILED_MESSAGE_PREFIX + methodName
                    + FAILED_MESSAGE_SUFFIX, exception);
        }
        if (resultCode != PromotionErrorCode.SUCCESSFUL)
        {
            final String msg = String.format(
                    "Failure [rc:%d] while deleting counter on URCS for msisdn=%s, type=%d, counterProfileID=%d : " + PromotionReturnCodeMsgMapping.getMessage(resultCode), 
                    resultCode, msisdn, subscriptionType, counterProfileID);            
            new MinorLogMsg(this.getClass(), msg, null).log(ctx);
            
            throw new RemoteServiceException(resultCode, msg);
        }
    }


    @Override
    public void deleteAllCountersForSub(Context ctx, String msisdn, int subscriptionType) throws RemoteServiceException
    {
        final String methodName = "deleteAllCountersForSub";
        final PromotionManagement client = getClient(ctx);
        final Parameter[] inParams = new Parameter[0];
        final ParameterSetHolder outParams = new ParameterSetHolder();
        final int resultCode;

        try
        {
            resultCode = client.deleteAllCountersForSub(msisdn, subscriptionType, inParams, outParams);
        }
        catch (final Throwable exception)
        {
            throw new RemoteServiceException(PromotionErrorCode.CONNECTION_ERROR, FAILED_MESSAGE_PREFIX + methodName
                    + FAILED_MESSAGE_SUFFIX, exception);
        }
        if (resultCode != PromotionErrorCode.SUCCESSFUL)
        {
            final String msg = String.format(
                    "Failure [rc:%d] while deleting all counters on URCS for msisdn=%s, type=%d : " + PromotionReturnCodeMsgMapping.getMessage(resultCode), 
                    resultCode, msisdn, subscriptionType);            
            new MinorLogMsg(this.getClass(), msg, null).log(ctx);
            
            throw new RemoteServiceException(resultCode, msg);
        }
    }


    @Override
    public CounterProfile createSystemCounter(Context ctx, CounterProfile counterProfile) throws RemoteServiceException
    {
        throw new UnsupportedOperationException();
    }


    @Override
    public void setSubscriberOptions(Context ctx, SubscriberIdentity subscriberId, Collection<Long> addOptions,
            Collection<Long> removeOptions) throws RemoteServiceException
    {
        throw new UnsupportedOperationException();
    }
        private String getCounterDeltaDescription(Collection<CounterDelta> deltas)
    {
        final StringBuilder builder = new StringBuilder();

        builder.append("[");
        for (CounterDelta delta : deltas)
        {
            builder.append("[CounterProfileID=");
            builder.append(delta.counterId);                
            builder.append(", delta=");
            builder.append(delta.delta);
            builder.append("]");
        }
        builder.append("]");
        
        return builder.toString();
    }


    @Override
    public Promotion retrievePromotionWithSPID(Context ctx, int spid, long promotionId) throws RemoteServiceException
    {
        final String methodName = "retrievePromotionWithSPID";
        final PromotionManagement client = getClient(ctx);
        final PromotionHolder holder = new PromotionHolder();
        final Parameter[] inParams = new Parameter[0];
        final ParameterSetHolder outParams = new ParameterSetHolder();
        final int resultCode;
        try
        {
            resultCode = client.retrievePromotionWithSPID(spid, promotionId, holder, inParams, outParams);
        }
        catch (final Throwable exception)
        {
            throw new RemoteServiceException(PromotionErrorCode.CONNECTION_ERROR, FAILED_MESSAGE_PREFIX + methodName
                    + FAILED_MESSAGE_SUFFIX, exception);
        }
        if (resultCode != PromotionErrorCode.SUCCESSFUL)
        {
            final String msg = "Failure [rc:" + resultCode + "] while retrieving promotion on URCS: "
                    + PromotionReturnCodeMsgMapping.getMessage(resultCode);
            new MinorLogMsg(this.getClass(), msg, null).log(ctx);
            throw new RemoteServiceException(resultCode, msg);
        }
        return holder.value;
    }
}
