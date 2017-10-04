package com.trilogy.app.crm.api.rmi.extensions;

import java.util.List;

import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.api.rmi.support.RmiApiErrorHandlingSupport;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.extension.subscriber.DualBalanceSubExtension;
import com.trilogy.app.crm.extension.subscriber.DualBalanceSubExtensionXInfo;
import com.trilogy.app.crm.extension.subscriber.SubscriberExtension;
import com.trilogy.app.crm.extension.subscriber.SubscriberExtensionXInfo;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v2_2.types.subscription.BaseMutableSubscriptionExtension;
import com.trilogy.util.crmapi.wsdl.v2_2.types.subscription.extensions.DualBalanceSubscriptionExtension;
import com.trilogy.util.crmapi.wsdl.v2_2.types.subscription.extensions.ReadOnlyDualBalanceSubscriptionExtension;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.BaseReadOnlySubscriptionExtensionSequence_type2;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.BaseSubscriptionExtensionReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.extensions.DualBalanceSubscriptionExtensionReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.extensions.ReadOnlySubscriptionExtension;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.extensions.SubscriptionExtension;


public class DualBalanceSubscriberExtensionToApiAdapter extends SubscriberExtensionToApiAdapter
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    public DualBalanceSubscriberExtensionToApiAdapter(Subscriber subscriber)
    {
        super(subscriber);
    }


    @Override
    public BaseSubscriptionExtensionReference toAPIReference(final Context ctx, final SubscriberExtension extension)
            throws HomeException
    {
        DualBalanceSubscriptionExtensionReference apiReference = new DualBalanceSubscriptionExtensionReference();
        return apiReference;
    }


    @Override
    public ReadOnlySubscriptionExtension toAPI(final Context ctx, final SubscriberExtension extension)
            throws HomeException
    {
        // Instantiating read only subscription extension
        ReadOnlyDualBalanceSubscriptionExtension apiExtension;
        try
        {
            apiExtension = (ReadOnlyDualBalanceSubscriptionExtension) XBeans.instantiate(
                    ReadOnlyDualBalanceSubscriptionExtension.class, ctx);
        }
        catch (Exception e)
        {
            new MinorLogMsg(this,
                    "Error instantiating new Read Only Dual Balance extension. Using default constructor.", e).log(ctx);
            apiExtension = new ReadOnlyDualBalanceSubscriptionExtension();
        }
        // Populating read only subscription extension
        DualBalanceSubExtension crmExtension = (DualBalanceSubExtension) extension;
        // Returning read only default subscription extension with correct choice
        final ReadOnlySubscriptionExtension result = new ReadOnlySubscriptionExtension();
        BaseReadOnlySubscriptionExtensionSequence_type2 choice = new BaseReadOnlySubscriptionExtensionSequence_type2();
        choice.setDualBalance(apiExtension);
        result.setBaseReadOnlySubscriptionExtensionSequence_type2(choice);
        return result;
    }


    @Override
    public SubscriberExtension toCRM(final Context ctx, final Object obj, GenericParameter[] parameters) throws HomeException
    {
        DualBalanceSubExtension crmExtension;
        try
        {
            crmExtension = (DualBalanceSubExtension) XBeans.instantiate(DualBalanceSubExtension.class, ctx);
        }
        catch (Exception e)
        {
            new MinorLogMsg(this,
                    "Error instantiating new Dual Balance Subscriber extension. Using default constructor.", e)
                    .log(ctx);
            crmExtension = new DualBalanceSubExtension();
        }
        DualBalanceSubscriptionExtension apiExtension;
        if (obj instanceof SubscriptionExtension)
        {
            apiExtension = ((SubscriptionExtension) obj).getBaseSubscriptionExtensionSequence_type2().getDualBalance();
        }
        else
        {
            apiExtension = (DualBalanceSubscriptionExtension) obj;
        }
        if (apiExtension != null)
        {
            crmExtension.setSubId(getSubscriber().getId());
            crmExtension.setBAN(getSubscriber().getBAN());
            crmExtension.setSpid(getSubscriber().getSpid());
        }
        else
        {
            crmExtension = null;
        }
        return crmExtension;
    }


    @Override
    public boolean update(Context ctx, BaseSubscriptionExtensionReference extensionReference, BaseMutableSubscriptionExtension extension, 
            GenericParameter[] parameters) throws CRMExceptionFault
    {
        RmiApiErrorHandlingSupport.assertExtensionUpdateEnabled(ctx, DualBalanceSubExtension.class, this);
        
        boolean extensionExists = false;
        List<DualBalanceSubExtension> crmExtensions = ExtensionSupportHelper.get(ctx).getExtensions(ctx,
                DualBalanceSubExtension.class, new EQ(DualBalanceSubExtensionXInfo.SUB_ID, getSubscriber().getId()));
        for (DualBalanceSubExtension crmExtension : crmExtensions)
        {
            extensionExists = true;
            try
            {
                //Whenever update to dual balance happens through API, it will always be enabled
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
                final String msg = "Unable to update Dual Balance Subscription Extension";
                RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, e, msg, this);
            }
            break;
        }
        return extensionExists;
    }


    @Override
    public void remove(final Context ctx, final BaseSubscriptionExtensionReference extension, 
            GenericParameter[] parameters) throws CRMExceptionFault
    {
        RmiApiErrorHandlingSupport.assertExtensionRemoveEnabled(ctx, DualBalanceSubExtension.class, this);
        
        List<DualBalanceSubExtension> crmExtensions = ExtensionSupportHelper.get(ctx).getExtensions(ctx,
                DualBalanceSubExtension.class, new EQ(SubscriberExtensionXInfo.SUB_ID, getSubscriber().getId()));
        for (DualBalanceSubExtension crmExtension : crmExtensions)
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
                final String msg = "Unable to remove Dual Balance Subscription Extension ";
                RmiApiErrorHandlingSupport.handleDeleteExceptions(ctx, e, msg, this);
            }
        }
    }


    @Override
    public ReadOnlySubscriptionExtension find(final Context ctx, final BaseSubscriptionExtensionReference extension)
            throws CRMExceptionFault
    {
        List<DualBalanceSubExtension> crmExtensions = ExtensionSupportHelper.get(ctx).getExtensions(ctx,
                DualBalanceSubExtension.class, new EQ(SubscriberExtensionXInfo.SUB_ID, getSubscriber().getId()));
        for (DualBalanceSubExtension crmExtension : crmExtensions)
        {
            try
            {
                return this.toAPI(ctx, crmExtension);
            }
            catch (final Exception e)
            {
                final String msg = "Unable to retrieve Dual Subscription Extension";
                RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
            }
            break;
        }
        // Returning read only default subscription extension with correct choice
        final ReadOnlySubscriptionExtension result = new ReadOnlySubscriptionExtension();
        BaseReadOnlySubscriptionExtensionSequence_type2 choice = new BaseReadOnlySubscriptionExtensionSequence_type2();
        choice.setDualBalance(null);
        result.setBaseReadOnlySubscriptionExtensionSequence_type2(choice);
        return result;
        
    }
}
