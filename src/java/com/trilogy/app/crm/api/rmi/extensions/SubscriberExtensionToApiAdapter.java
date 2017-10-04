package com.trilogy.app.crm.api.rmi.extensions;

import com.trilogy.app.crm.api.ApiSupport;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.extension.subscriber.DualBalanceSubExtension;
import com.trilogy.app.crm.extension.subscriber.FixedStopPricePlanSubExtension;
import com.trilogy.app.crm.extension.subscriber.MultiSimSubExtension;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporteeSubExtension;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporterSubExtension;
import com.trilogy.app.crm.extension.subscriber.SubscriberExtension;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v2_2.types.subscription.BaseMutableSubscriptionExtension;
import com.trilogy.util.crmapi.wsdl.v2_2.types.subscription.extensions.MutableDualBalanceSubscriptionExtension;
import com.trilogy.util.crmapi.wsdl.v2_2.types.subscription.extensions.MutablePPSMSupporterSubscriptionExtension;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.BaseSubscriptionExtensionReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.BaseSubscriptionExtensionSequence_type0;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.BaseSubscriptionExtensionSequence_type1;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.BaseSubscriptionExtensionSequence_type2;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.BaseSubscriptionExtensionSequence_type3;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.BaseSubscriptionExtensionSequence_type4;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.extensions.DualBalanceSubscriptionExtensionReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.extensions.FixedStopPricePlanSubscriptionExtensionReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.extensions.MultiSimSubscriptionExtensionReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.extensions.MutableFixedStopPricePlanSubscriptionExtension;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.extensions.MutableMultiSimSubscriptionExtension;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.extensions.MutablePPSMSupporteeSubscriptionExtension;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.extensions.PPSMSupporteeSubscriptionExtensionReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.extensions.PPSMSupporterSubscriptionExtensionReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.extensions.ReadOnlySubscriptionExtension;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.extensions.SubscriptionExtension;


public abstract class SubscriberExtensionToApiAdapter
{
    private static final long serialVersionUID = 1L;
    
    public SubscriberExtensionToApiAdapter(Subscriber subscriber)
    {
        this.subscriber_ = subscriber;
    }

    public static SubscriberExtensionToApiAdapter getInstance(SubscriptionExtension extension, Subscriber subscriber)
    {
        BaseSubscriptionExtensionSequence_type0 ppsmSupportee = extension.getBaseSubscriptionExtensionSequence_type0();
        BaseSubscriptionExtensionSequence_type1 ppsmSupporter = extension.getBaseSubscriptionExtensionSequence_type1();
        BaseSubscriptionExtensionSequence_type2 dualBalance = extension.getBaseSubscriptionExtensionSequence_type2();
        BaseSubscriptionExtensionSequence_type3 multiSim = extension.getBaseSubscriptionExtensionSequence_type3();
        BaseSubscriptionExtensionSequence_type4 fixedStopPricePlan = extension.getBaseSubscriptionExtensionSequence_type4();

        if (ppsmSupportee!=null && ppsmSupportee.getPpsmSupportee()!=null)
        {
            return new PPSMSupporteeSubscriberExtensionToApiAdapter(subscriber);
        }
        else if (ppsmSupporter!=null && ppsmSupporter.getPpsmSupporter()!=null)
        {
            return new PPSMSupporterSubscriberExtensionToApiAdapter(subscriber);
        }
        else if (dualBalance!=null && dualBalance.getDualBalance()!=null)
        {
            return new DualBalanceSubscriberExtensionToApiAdapter(subscriber);
        }
        else if (multiSim!=null && multiSim.getMultiSim()!=null)
        {
            return new MultiSimSubscriberExtensionToApiAdapter(subscriber);
        }
        else if (fixedStopPricePlan!=null && fixedStopPricePlan.getFixedStopPricePlan()!=null)
        {
            return new FixedStopPricePlanSubscriberExtensionToApiAdapter(subscriber);
        }
        else
        {
            return null;
        }
    }
    
    public static SubscriberExtensionToApiAdapter getInstance(BaseSubscriptionExtensionReference extensionReference, Subscriber subscriber)
    {
        if (extensionReference instanceof PPSMSupporteeSubscriptionExtensionReference)
        {
            return new PPSMSupporteeSubscriberExtensionToApiAdapter(subscriber);
        }
        else if (extensionReference instanceof PPSMSupporterSubscriptionExtensionReference)
        {
            return new PPSMSupporterSubscriberExtensionToApiAdapter(subscriber);
        }
        else if (extensionReference instanceof DualBalanceSubscriptionExtensionReference)
        {
            return new DualBalanceSubscriberExtensionToApiAdapter(subscriber);
        }
        else if (extensionReference instanceof MultiSimSubscriptionExtensionReference)
        {
            return new MultiSimSubscriberExtensionToApiAdapter(subscriber);
        }
        else if (extensionReference instanceof FixedStopPricePlanSubscriptionExtensionReference)
        {
            return new FixedStopPricePlanSubscriberExtensionToApiAdapter(subscriber);
        }        
        else
        {
            return null;
        }
    }

    public static SubscriberExtensionToApiAdapter getInstance(SubscriberExtension extension, Subscriber subscriber)
    {
        if (extension instanceof PPSMSupporteeSubExtension)
        {
            return new PPSMSupporteeSubscriberExtensionToApiAdapter(subscriber);
        }
        else if (extension instanceof PPSMSupporterSubExtension)
        {
            return new PPSMSupporterSubscriberExtensionToApiAdapter(subscriber);
        }
        else if (extension instanceof DualBalanceSubExtension)
        {
            return new DualBalanceSubscriberExtensionToApiAdapter(subscriber);   
        }
        else if (extension instanceof MultiSimSubExtension)
        {
            return new MultiSimSubscriberExtensionToApiAdapter(subscriber);
        }
        else if (extension instanceof FixedStopPricePlanSubExtension)
        {
            return new FixedStopPricePlanSubscriberExtensionToApiAdapter(subscriber);
        }        
        else
        {
            return null;
        }
    }

    public static SubscriberExtensionToApiAdapter getInstance(BaseSubscriptionExtensionReference extensionReference, BaseMutableSubscriptionExtension extension, Subscriber subscriber)
    {
        if (extensionReference instanceof PPSMSupporteeSubscriptionExtensionReference 
                && extension instanceof MutablePPSMSupporteeSubscriptionExtension)
        {
            return new PPSMSupporteeSubscriberExtensionToApiAdapter(subscriber);
        }
        else if (extensionReference instanceof PPSMSupporterSubscriptionExtensionReference
            && extension instanceof MutablePPSMSupporterSubscriptionExtension)
        {
            return new PPSMSupporterSubscriberExtensionToApiAdapter(subscriber);
        }
        else if (extensionReference instanceof DualBalanceSubscriptionExtensionReference
            && extension instanceof MutableDualBalanceSubscriptionExtension)
        {
            return new DualBalanceSubscriberExtensionToApiAdapter(subscriber);
        }
        else if (extensionReference instanceof MultiSimSubscriptionExtensionReference
                && extension instanceof MutableMultiSimSubscriptionExtension)
        {
            return new MultiSimSubscriberExtensionToApiAdapter(subscriber);
        }
        else if (extensionReference instanceof FixedStopPricePlanSubscriptionExtensionReference
                && extension instanceof MutableFixedStopPricePlanSubscriptionExtension)
        {
            return new FixedStopPricePlanSubscriberExtensionToApiAdapter(subscriber);
        }        
        else
        {
            return null;
        }
    }
    
    public Home getExtensionHome(final Context ctx, final Home home)
    {
        return ApiSupport.injectAPIUpdateEntityHomeIntoPipeline(ctx, home);
    }

    public abstract BaseSubscriptionExtensionReference toAPIReference(final Context ctx, final SubscriberExtension extension) throws HomeException;

    public abstract ReadOnlySubscriptionExtension toAPI(final Context ctx, final SubscriberExtension crmExtension) throws HomeException;

    public abstract SubscriberExtension toCRM(final Context ctx, final Object obj, GenericParameter[] parameters) throws HomeException;

    public abstract boolean update(final Context ctx, final BaseSubscriptionExtensionReference extensionReference, final BaseMutableSubscriptionExtension extension, GenericParameter[] parameters) throws CRMExceptionFault;
    
    public abstract void remove(final Context ctx, final BaseSubscriptionExtensionReference extension, GenericParameter[] parameters) throws CRMExceptionFault;

    public abstract ReadOnlySubscriptionExtension find(final Context ctx, final BaseSubscriptionExtensionReference extension) throws CRMExceptionFault;

    public Subscriber getSubscriber()
    {
        return subscriber_;
    }

    
    public void setSubscriber(Subscriber subscriber)
    {
        subscriber_ = subscriber;
    }
    
    private Subscriber subscriber_;
}
