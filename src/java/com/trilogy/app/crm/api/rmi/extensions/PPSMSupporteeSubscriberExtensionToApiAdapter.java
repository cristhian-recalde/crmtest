package com.trilogy.app.crm.api.rmi.extensions;

import java.util.List;

import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.api.rmi.SubscriberToApiAdapter;
import com.trilogy.app.crm.api.rmi.support.RmiApiErrorHandlingSupport;
import com.trilogy.app.crm.api.rmi.support.SubscribersApiSupport;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporteeSubExtension;
import com.trilogy.app.crm.extension.subscriber.SubscriberExtension;
import com.trilogy.app.crm.extension.subscriber.SubscriberExtensionXInfo;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v2_2.types.subscription.BaseMutableSubscriptionExtension;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.BaseReadOnlySubscriptionExtensionSequence_type0;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.BaseSubscriptionExtensionReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.extensions.MutablePPSMSupporteeSubscriptionExtension;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.extensions.PPSMSupporteeSubscriptionExtension;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.extensions.PPSMSupporteeSubscriptionExtensionReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.extensions.ReadOnlyPPSMSupporteeSubscriptionExtension;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.extensions.ReadOnlySubscriptionExtension;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.extensions.SubscriptionExtension;


public class PPSMSupporteeSubscriberExtensionToApiAdapter extends SubscriberExtensionToApiAdapter
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public PPSMSupporteeSubscriberExtensionToApiAdapter(Subscriber subscriber)
    {
        super(subscriber);
    }
    
    @Override
    public BaseSubscriptionExtensionReference toAPIReference(final Context ctx, final SubscriberExtension extension) throws HomeException
    {
        
        PPSMSupporteeSubscriptionExtensionReference apiReference = new PPSMSupporteeSubscriptionExtensionReference();
        ReadOnlyPPSMSupporteeSubscriptionExtension apiExtension = (toAPI(ctx, extension)).getBaseReadOnlySubscriptionExtensionSequence_type0().getPpsmSupportee();
        
        apiReference.setAppliedChargingTemplate(apiExtension.getAppliedChargingTemplate());
        apiReference.setAppliedScreeningTemplate(apiExtension.getAppliedScreeningTemplate());
        
        SubscriberToApiAdapter adapter = new SubscriberToApiAdapter();
        apiReference.setSubscriptionRef((SubscriptionReference) adapter.adapt(ctx, getSubscriber()));
        apiReference.setSupporterRef(apiExtension.getSupporterRef());
        
        return apiReference;        
    }

    @Override
    public ReadOnlySubscriptionExtension toAPI(final Context ctx, final SubscriberExtension extension) throws HomeException
    {
        // Instantiating read only subscription extension
        ReadOnlyPPSMSupporteeSubscriptionExtension apiExtension;
        try
        {
            apiExtension = (ReadOnlyPPSMSupporteeSubscriptionExtension) XBeans.instantiate(ReadOnlyPPSMSupporteeSubscriptionExtension.class, ctx);
        }
        catch (Exception e)
        {
            new MinorLogMsg(this, "Error instantiating new Read Only PPSM Supportee extension. Using default constructor.", e).log(ctx);
            apiExtension = new ReadOnlyPPSMSupporteeSubscriptionExtension();
        }

        
        // Populating read only subscription extension
        PPSMSupporteeSubExtension crmExtension = (PPSMSupporteeSubExtension) extension;
        
        apiExtension.setAppliedChargingTemplate(crmExtension.getChargingTemplate());
        apiExtension.setAppliedScreeningTemplate(crmExtension.getScreeningTemplate());
        Subscriber supportedSubscriber = SubscriberSupport.lookupSubscriberForMSISDN(ctx, crmExtension.getSupportMSISDN());
        
        SubscriberToApiAdapter adapter = new SubscriberToApiAdapter();
        apiExtension.setSupporterRef((SubscriptionReference) adapter.adapt(ctx, supportedSubscriber));
        
        // Returning read only default subscription extension with correct choice
        final ReadOnlySubscriptionExtension result = new ReadOnlySubscriptionExtension();

        BaseReadOnlySubscriptionExtensionSequence_type0 choice = new BaseReadOnlySubscriptionExtensionSequence_type0();
        choice.setPpsmSupportee(apiExtension);
        result.setBaseReadOnlySubscriptionExtensionSequence_type0(choice);

        return result;
    }

    @Override
    public SubscriberExtension toCRM(final Context ctx, final Object obj, GenericParameter[] parameters) throws HomeException
    {
        PPSMSupporteeSubExtension crmExtension;
        try
        {
            crmExtension = (PPSMSupporteeSubExtension) XBeans.instantiate(PPSMSupporteeSubExtension.class, ctx);
        }
        catch (Exception e)
        {
            new MinorLogMsg(this, "Error instantiating new PPSM Supportee extension. Using default constructor.", e).log(ctx);
            crmExtension = new PPSMSupporteeSubExtension();
        }
        
        PPSMSupporteeSubscriptionExtension apiExtension;
        
        if (obj instanceof SubscriptionExtension)
        {
            apiExtension = ((SubscriptionExtension) obj).getBaseSubscriptionExtensionSequence_type0().getPpsmSupportee();
        }
        else
        {
            apiExtension = (PPSMSupporteeSubscriptionExtension) obj;
        }

        crmExtension.setSubId(getSubscriber().getId());
        crmExtension.setBAN(getSubscriber().getBAN());
        crmExtension.setSpid(getSubscriber().getSpid());
        crmExtension.setMSISDN(getSubscriber().getMSISDN());
        
        try
        {
            final Subscriber supporterSubscriber = SubscribersApiSupport.getCrmSubscriber(ctx, apiExtension.getSupporterRef(), this);
            if (supporterSubscriber!=null)
            {
                crmExtension.setSupportMSISDN(supporterSubscriber.getMSISDN());
            }
        }
        catch (CRMExceptionFault e)
        {
            throw new HomeException(e);
        }
        
        if (apiExtension.getAppliedChargingTemplate()!=null)
        {
            crmExtension.setChargingTemplate(apiExtension.getAppliedChargingTemplate());
        }
        else
        {
            crmExtension.setChargingTemplate(-1);
            
        }
        if (apiExtension.getAppliedScreeningTemplate()!=null)
        {
            crmExtension.setScreeningTemplate(apiExtension.getAppliedScreeningTemplate());
        }
        else
        {
            crmExtension.setScreeningTemplate(-1);
        }
        
        return crmExtension;
    }

    @Override
    public boolean update(Context ctx, BaseSubscriptionExtensionReference extensionReference, 
            BaseMutableSubscriptionExtension extension, GenericParameter[] parameters) throws CRMExceptionFault
    {
        RmiApiErrorHandlingSupport.assertExtensionUpdateEnabled(ctx, PPSMSupporteeSubExtension.class, this);
        
        boolean extensionExists = false;
        List<PPSMSupporteeSubExtension> crmExtensions = ExtensionSupportHelper.get(ctx).getExtensions(ctx, PPSMSupporteeSubExtension.class, new EQ(SubscriberExtensionXInfo.SUB_ID, getSubscriber().getId()));
        for (PPSMSupporteeSubExtension crmExtension : crmExtensions)
        {
            extensionExists = true;
            boolean dirty = updatePPSMSupporteeSubscriptionExtension(ctx, crmExtension, (MutablePPSMSupporteeSubscriptionExtension) extension);
            if (dirty)
            {
                try
                {
                    Home extensionHome = getExtensionHome(ctx, ExtensionSupportHelper.get(ctx).getExtensionHome(ctx, crmExtension));
                    if (extensionHome != null)
                    {
                        extensionHome.store(ctx, crmExtension);  
                    } 
                    else
                    {
                        final String msg = "Subscription Extension type not supported: " + crmExtension.getClass().getName();
                        RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, null, msg, this);
                    }
                }
                catch (CRMExceptionFault e)
                {
                    throw e;
                }
                catch (final Exception e)
                {
                    final String msg = "Unable to update PPSM Supportee Subscription Extension";
                    RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, e, msg, this);
                }
            }
            break;
        }
        return extensionExists;
    }

    @Override
    public void remove(final Context ctx, final BaseSubscriptionExtensionReference extension,
            GenericParameter[] parameters) throws CRMExceptionFault
    {
        RmiApiErrorHandlingSupport.assertExtensionRemoveEnabled(ctx, PPSMSupporteeSubExtension.class, this);
        
        List<PPSMSupporteeSubExtension> crmExtensions = ExtensionSupportHelper.get(ctx).getExtensions(ctx, PPSMSupporteeSubExtension.class, new EQ(SubscriberExtensionXInfo.SUB_ID, getSubscriber().getId()));
        for (PPSMSupporteeSubExtension crmExtension : crmExtensions)
        {
            try
            {
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
                final String msg = "Unable to remove PPSM Supportee Subscription Extension";
                RmiApiErrorHandlingSupport.handleDeleteExceptions(ctx, e, msg, this);
            }
        }
    }

    @Override
    public ReadOnlySubscriptionExtension find(final Context ctx, final BaseSubscriptionExtensionReference extension) throws CRMExceptionFault
    {
        List<PPSMSupporteeSubExtension> crmExtensions = ExtensionSupportHelper.get(ctx).getExtensions(ctx, PPSMSupporteeSubExtension.class, new EQ(SubscriberExtensionXInfo.SUB_ID, getSubscriber().getId()));
        for (PPSMSupporteeSubExtension crmExtension : crmExtensions)
        {
            try
            {
                return this.toAPI(ctx, crmExtension);
            }
            catch (final Exception e)
            {
                final String msg = "Unable to retrieve PPSM Supportee Subscription Extension";
                RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
            }
            break;
        }
        // Returning read only default subscription extension with correct choice
        final ReadOnlySubscriptionExtension result = new ReadOnlySubscriptionExtension();
        BaseReadOnlySubscriptionExtensionSequence_type0 choice = new BaseReadOnlySubscriptionExtensionSequence_type0();
        choice.setPpsmSupportee(null);
        result.setBaseReadOnlySubscriptionExtensionSequence_type0(choice);
        return result;
        
        
    }
    
    private boolean updatePPSMSupporteeSubscriptionExtension(Context ctx, PPSMSupporteeSubExtension crmExtension, MutablePPSMSupporteeSubscriptionExtension extension) throws CRMExceptionFault
    {
        boolean dirty = false;
        
        final Subscriber supporterSubscriber = SubscribersApiSupport.getCrmSubscriber(ctx, extension.getSupporterRef(), this);

        if (!crmExtension.getSupportMSISDN().equals(supporterSubscriber.getMSISDN()))
        {
            dirty = true;
            crmExtension.setSupportMSISDN(supporterSubscriber.getMSISDN());
        }
        
        if ((extension.getAppliedChargingTemplate() == null && crmExtension.getChargingTemplate() != -1)
                || (extension.getAppliedChargingTemplate() != null && crmExtension.getChargingTemplate() != extension
                        .getAppliedChargingTemplate().longValue()))
        {
            dirty = true;
            if (extension.getAppliedChargingTemplate() != null)
            {
                crmExtension.setChargingTemplate(extension.getAppliedChargingTemplate().longValue());
            }
            else
            {
                crmExtension.setChargingTemplate(-1);
            }
        }
        
        if ((extension.getAppliedScreeningTemplate() == null && crmExtension.getScreeningTemplate() != -1)
                || (extension.getAppliedScreeningTemplate() != null && crmExtension.getScreeningTemplate() != extension
                        .getAppliedScreeningTemplate().longValue()))
        {
            dirty = true;
            if (extension.getAppliedScreeningTemplate() != null)
            {
                crmExtension.setScreeningTemplate(extension.getAppliedScreeningTemplate().longValue());
            }
            else
            {
                crmExtension.setScreeningTemplate(-1);
            }
        }
        return dirty;
    }
    
}
