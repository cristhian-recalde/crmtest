package com.trilogy.app.crm.api.rmi.extensions;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.api.rmi.SubscriberToApiAdapter;
import com.trilogy.app.crm.api.rmi.support.APIGenericParameterSupport;
import com.trilogy.app.crm.api.rmi.support.RmiApiErrorHandlingSupport;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.extension.subscriber.FixedStopPricePlanSubExtension;
import com.trilogy.app.crm.extension.subscriber.FixedStopPricePlanSubExtensionXInfo;
import com.trilogy.app.crm.extension.subscriber.MultiSimRecordHolder;
import com.trilogy.app.crm.extension.subscriber.MultiSimSubExtension;
import com.trilogy.app.crm.extension.subscriber.SubscriberExtension;
import com.trilogy.app.crm.extension.subscriber.SubscriberExtensionXInfo;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v2_2.types.subscription.BaseMutableSubscriptionExtension;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.BaseReadOnlySubscriptionExtensionSequence_type3;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.BaseReadOnlySubscriptionExtensionSequence_type4;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.BaseSubscriptionExtensionReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.extensions.FixedStopPricePlanSubscriptionExtension;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.extensions.FixedStopPricePlanSubscriptionExtensionReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.extensions.MultiSimChargeType;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.extensions.MultiSimChargeTypeEnum;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.extensions.MultiSimSubscriptionExtension;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.extensions.MultiSimSubscriptionExtensionReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.extensions.MutableFixedStopPricePlanSubscriptionExtension;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.extensions.MutableMultiSimSubscriptionExtension;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.extensions.PackageReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.extensions.ReadOnlyFixedStopPricePlanSubscriptionExtension;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.extensions.ReadOnlyMultiSimSubscriptionExtension;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.extensions.ReadOnlySubscriptionExtension;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.extensions.SubscriptionExtension;


/**
 * 
 * @author asim.mahmood@redknee.com
 * @since 9.2
 *
 */
public class FixedStopPricePlanSubscriberExtensionToApiAdapter extends SubscriberExtensionToApiAdapter
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    public FixedStopPricePlanSubscriberExtensionToApiAdapter(Subscriber subscriber)
    {
        super(subscriber);
    }


    @Override
    public BaseSubscriptionExtensionReference toAPIReference(final Context ctx, final SubscriberExtension extension)
            throws HomeException
    {
        FixedStopPricePlanSubscriptionExtensionReference apiReference = new FixedStopPricePlanSubscriptionExtensionReference();
        ReadOnlyFixedStopPricePlanSubscriptionExtension apiExtension = (toAPI(ctx, extension)).getBaseReadOnlySubscriptionExtensionSequence_type4().getFixedStopPricePlan();
        Subscriber sub = getSubscriber();
        
        apiReference.setFixedStop(apiExtension.getFixedStop());
        apiReference.setBalanceExpiry(apiExtension.getBalanceExpiry());
        
        SubscriberToApiAdapter adapter = new SubscriberToApiAdapter();
        apiReference.setSubscriptionRef((SubscriptionReference) adapter.adapt(ctx, sub));
        
        return apiReference; 
    }


    @Override
    public ReadOnlySubscriptionExtension toAPI(final Context ctx, final SubscriberExtension extension)
            throws HomeException
    {
        // Instantiating read only subscription extension
        ReadOnlyFixedStopPricePlanSubscriptionExtension apiExtension;
        try
        {
            apiExtension = (ReadOnlyFixedStopPricePlanSubscriptionExtension) XBeans.instantiate(
                    ReadOnlyFixedStopPricePlanSubscriptionExtension.class, ctx);
        }
        catch (Exception e)
        {
            new MinorLogMsg(this,
                    "Error instantiating new Read Only Fixed Price Plan Subscriber extension. Using default constructor.", e).log(ctx);
            apiExtension = new ReadOnlyFixedStopPricePlanSubscriptionExtension();
        }
        
        // Populating read only subscription extension
        FixedStopPricePlanSubExtension crmExtension = (FixedStopPricePlanSubExtension) extension;
        
        if (crmExtension.getEndDate() != null)
        {
            final Calendar endDate = CalendarSupportHelper.get(ctx).dateToCalendar(crmExtension.getEndDate());
            apiExtension.setFixedStop(endDate);
        }
        
        Date balanceExpiry = null;
        
        if ((extension instanceof FixedStopPricePlanSubExtension) && ((FixedStopPricePlanSubExtension) extension).getBalanceExpiry()!=null)
        {
            balanceExpiry = ((FixedStopPricePlanSubExtension) extension).getBalanceExpiry();
        }
        else if (getSubscriber().getExpiryDate() != null)
        {
            balanceExpiry = getSubscriber().getExpiryDate();
        }
        
        if (balanceExpiry!=null)
        {
            apiExtension.setBalanceExpiry(CalendarSupportHelper.get(ctx).dateToCalendar(((balanceExpiry))));
        }
        
        // Returning read only default subscription extension with correct choice
        final ReadOnlySubscriptionExtension result = new ReadOnlySubscriptionExtension();
        BaseReadOnlySubscriptionExtensionSequence_type4 choice = new BaseReadOnlySubscriptionExtensionSequence_type4();
        choice.setFixedStopPricePlan(apiExtension);
        result.setBaseReadOnlySubscriptionExtensionSequence_type4(choice);
        return result;
    }


    @Override
    public SubscriberExtension toCRM(final Context ctx, final Object obj, GenericParameter[] parameters) throws HomeException
    {
        final Map<String, Object> paramMap = APIGenericParameterSupport.createGenericParameterMap(parameters);
        FixedStopPricePlanSubExtension crmExtension;
        try
        {
            crmExtension = (FixedStopPricePlanSubExtension) XBeans.instantiate(FixedStopPricePlanSubExtension.class, ctx);
        }
        catch (Exception e)
        {
            new MinorLogMsg(this, "Error instantiating new Fixed Stop Price Plan subscriber extension. Using default constructor.", e).log(ctx);
            crmExtension = new FixedStopPricePlanSubExtension();
        }
        
        FixedStopPricePlanSubscriptionExtension apiExtension;
        
        if (obj instanceof SubscriptionExtension)
        {
            apiExtension = ((SubscriptionExtension) obj).getBaseSubscriptionExtensionSequence_type4().getFixedStopPricePlan();
        }
        else
        {
            apiExtension = (FixedStopPricePlanSubscriptionExtension) obj;
        }

        crmExtension.setSubId(getSubscriber().getId());
        crmExtension.setBAN(getSubscriber().getBAN());
        crmExtension.setSpid(getSubscriber().getSpid());
        
        if (apiExtension.getFixedStop() != null)
        {
            final Date endDate = CalendarSupportHelper.get(ctx).calendarToDate(apiExtension.getFixedStop());
            crmExtension.setEndDate(endDate);
        }
        if (apiExtension.getBalanceExpiry() != null)
        {
            final Date balanceExpiry = CalendarSupportHelper.get(ctx).calendarToDate(apiExtension.getBalanceExpiry());
            crmExtension.setBalanceExpiry(balanceExpiry);
        }
        updatePricePlan(ctx, crmExtension, paramMap);
        
        return crmExtension;
    }


    @Override
    public boolean update(Context ctx, BaseSubscriptionExtensionReference extensionReference, 
            BaseMutableSubscriptionExtension extension, GenericParameter[] parameters) throws CRMExceptionFault
    {
        RmiApiErrorHandlingSupport.assertExtensionUpdateEnabled(ctx, FixedStopPricePlanSubExtension.class, this);
        
        boolean extensionExists = false;
        final Map<String, Object> paramMap = APIGenericParameterSupport.createGenericParameterMap(parameters);
        List<FixedStopPricePlanSubExtension> crmExtensions = ExtensionSupportHelper.get(ctx).getExtensions(ctx,
                FixedStopPricePlanSubExtension.class, new EQ(FixedStopPricePlanSubExtensionXInfo.SUB_ID, getSubscriber().getId()));
        for (FixedStopPricePlanSubExtension crmExtension : crmExtensions)
        {
            extensionExists = true;
            boolean dirty = updateFixedStopPricePlanSubscriptionExtension(ctx, crmExtension, (MutableFixedStopPricePlanSubscriptionExtension) extension);
            updatePricePlan(ctx, crmExtension, paramMap);
            if (dirty)
            {
                try
                {
                    //Whenever update to the extension though API, it will always be enabled
                    Home extensionHome = getExtensionHome(ctx,
                            ExtensionSupportHelper.get(ctx).getExtensionHome(ctx, crmExtension));
                    if (extensionHome != null)
                    {
                        extensionHome.store(ctx, crmExtension);
                    }
                    else
                    {
                        final String msg = "Subscription Extension type not supported: "
                                + crmExtension.getClass().getName();
                        RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, null, msg, this);
                    }
                }
                catch (CRMExceptionFault e)
                {
                    throw e;
                }
                catch (final Exception e)
                {
                    final String msg = "Unable to update Fixed Stop Price Plan Subscription Extension";
                    RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, e, msg, this);
                }
            }
            break;
        }
        return extensionExists;
    }


    protected boolean updateFixedStopPricePlanSubscriptionExtension(Context ctx,
            FixedStopPricePlanSubExtension crmExtension, MutableFixedStopPricePlanSubscriptionExtension extension)
    {
        boolean dirty = false;
        
        if (extension.getFixedStop() != null)
        {
            final Date endDate = CalendarSupportHelper.get(ctx).calendarToDate(extension.getFixedStop());
            crmExtension.setEndDate(endDate);
            dirty = true;
        }
        if (extension.getBalanceExpiry() != null)
        {
            final Date balanceExpiry = CalendarSupportHelper.get(ctx).calendarToDate(extension.getBalanceExpiry());
            crmExtension.setBalanceExpiry(balanceExpiry);
            dirty = true;
        }
        
        return dirty;
    }


    protected boolean updatePricePlan(Context ctx, FixedStopPricePlanSubExtension crmExtension, Map<String, Object> paramMap)
    {
        Long pricePlanId = (Long)paramMap.get(APIGenericParameterSupport.FIXED_STOP_PRICEPLAN_SWITCH);
        if (pricePlanId != null)
        {
            crmExtension.setPrimaryPricePlanId(pricePlanId);
            return true;
        }
        return false;
    }


    @Override
    public void remove(final Context ctx, final BaseSubscriptionExtensionReference extension,
            GenericParameter[] parameters) throws CRMExceptionFault
    {
        RmiApiErrorHandlingSupport.assertExtensionRemoveEnabled(ctx, FixedStopPricePlanSubExtension.class, this);
        
        final Map<String, Object> paramMap = APIGenericParameterSupport.createGenericParameterMap(parameters);
        List<FixedStopPricePlanSubExtension> crmExtensions = ExtensionSupportHelper.get(ctx).getExtensions(ctx,
                FixedStopPricePlanSubExtension.class, new EQ(SubscriberExtensionXInfo.SUB_ID, getSubscriber().getId()));
        for (FixedStopPricePlanSubExtension crmExtension : crmExtensions)
        {
            try
            {
                updatePricePlan(ctx, crmExtension, paramMap);
                Home extensionHome = getExtensionHome(ctx, ExtensionSupportHelper.get(ctx).getExtensionHome(ctx, crmExtension));
                if (extensionHome != null)
                {
                    extensionHome.remove(ctx, crmExtension);
                }
                else
                {
                    final String msg = "Subscription Extension type not supported: " + crmExtension.getClass().getName();
                    RmiApiErrorHandlingSupport.handleDeleteExceptions(ctx, null, msg, this);
                }
            }
            catch (CRMExceptionFault e)
            {
                throw e;
            }
            catch (final Exception e)
            {
                final String msg = "Unable to remove Fixed Stop Price Plan Subscription Extension ";
                RmiApiErrorHandlingSupport.handleDeleteExceptions(ctx, e, msg, this);
            }
        }
    }


    @Override
    public ReadOnlySubscriptionExtension find(final Context ctx, final BaseSubscriptionExtensionReference extension)
            throws CRMExceptionFault
    {
        List<FixedStopPricePlanSubExtension> crmExtensions = ExtensionSupportHelper.get(ctx).getExtensions(ctx,
                FixedStopPricePlanSubExtension.class, new EQ(SubscriberExtensionXInfo.SUB_ID, getSubscriber().getId()));
        for (FixedStopPricePlanSubExtension crmExtension : crmExtensions)
        {
            try
            {
                return this.toAPI(ctx, crmExtension);
            }
            catch (final Exception e)
            {
                final String msg = "Unable to retrieve Fixed Stop Price Plan Subscription Extension";
                RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
            }
            break;
        }
        // Returning read only default subscription extension with correct choice
        final ReadOnlySubscriptionExtension result = new ReadOnlySubscriptionExtension();
        BaseReadOnlySubscriptionExtensionSequence_type4 choice = new BaseReadOnlySubscriptionExtensionSequence_type4();
        choice.setFixedStopPricePlan(null);
        result.setBaseReadOnlySubscriptionExtensionSequence_type4(choice);
        return result;
        
    }
}
