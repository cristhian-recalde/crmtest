package com.trilogy.app.crm.api.rmi.extensions;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.api.rmi.SubscriberToApiAdapter;
import com.trilogy.app.crm.api.rmi.support.RmiApiErrorHandlingSupport;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporterSubExtension;
import com.trilogy.app.crm.extension.subscriber.SubscriberExtension;
import com.trilogy.app.crm.extension.subscriber.SubscriberExtensionXInfo;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v2_2.types.subscription.BaseMutableSubscriptionExtension;
import com.trilogy.util.crmapi.wsdl.v2_2.types.subscription.extensions.MutablePPSMSupporterSubscriptionExtension;
import com.trilogy.util.crmapi.wsdl.v2_2.types.subscription.extensions.PPSMSupporterSubscriptionExtension;
import com.trilogy.util.crmapi.wsdl.v2_2.types.subscription.extensions.ReadOnlyPPSMSupporterSubscriptionExtension;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.BaseReadOnlySubscriptionExtensionSequence_type1;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.BaseSubscriptionExtensionReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.extensions.PPSMSupporterSubscriptionExtensionReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.extensions.ReadOnlySubscriptionExtension;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.extensions.SubscriptionExtension;

public class PPSMSupporterSubscriberExtensionToApiAdapter extends SubscriberExtensionToApiAdapter
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public PPSMSupporterSubscriberExtensionToApiAdapter(Subscriber subscriber)
    {
        super(subscriber);
    }
    
    @Override
    public BaseSubscriptionExtensionReference toAPIReference(final Context ctx, final SubscriberExtension extension) throws HomeException
    {
        PPSMSupporterSubscriptionExtensionReference apiReference = new PPSMSupporterSubscriptionExtensionReference();
        ReadOnlyPPSMSupporterSubscriptionExtension apiExtension = (toAPI(ctx, extension)).getBaseReadOnlySubscriptionExtensionSequence_type1().getPpsmSupporter();
        
        apiReference.setChargingTemplates(apiExtension.getChargingTemplates());
        apiReference.setScreeningTemplates(apiExtension.getScreeningTemplates());
        SubscriberToApiAdapter adapter = new SubscriberToApiAdapter();
        apiReference.setSubscriptionRef((SubscriptionReference) adapter.adapt(ctx, getSubscriber()));

        return apiReference;        
    }
    
    
    @Override
    public ReadOnlySubscriptionExtension toAPI(final Context ctx, final SubscriberExtension extension) throws HomeException
    {
        // Instantiating read only subscription extension
        ReadOnlyPPSMSupporterSubscriptionExtension apiExtension;
        try
        {
            apiExtension = (ReadOnlyPPSMSupporterSubscriptionExtension) XBeans.instantiate(ReadOnlyPPSMSupporterSubscriptionExtension.class, ctx);
        }
        catch (Exception e)
        {
            new MinorLogMsg(this, "Error instantiating new Read Only PPSM Supporter extension. Using default constructor.", e).log(ctx);
            apiExtension = new ReadOnlyPPSMSupporterSubscriptionExtension();
        }

        
        // Populating read only subscription extension
        PPSMSupporterSubExtension crmExtension = (PPSMSupporterSubExtension) extension;
        String[] crmChargingTemplates = crmExtension.getChargingTemplates(ctx).toArray(new String[]{});
        String[] crmScreeningTemplates = crmExtension.getScreeningTemplates(ctx).toArray(new String[]{});
        Long[] chargingTemplates = new Long[crmChargingTemplates.length];
        Long[] screeningTemplates = new Long[crmScreeningTemplates.length];
        
        for (int i=0;i<crmChargingTemplates.length;i++)
        {
            chargingTemplates[i] = Long.valueOf(crmChargingTemplates[i]);
        }
        
        for (int i=0;i<crmScreeningTemplates.length;i++)
        {
            screeningTemplates[i] = Long.valueOf(crmScreeningTemplates[i]);
        }
        
        apiExtension.setChargingTemplates(chargingTemplates);
        apiExtension.setScreeningTemplates(screeningTemplates);
        
        // Returning read only default subscription extension with correct choice
        final ReadOnlySubscriptionExtension result = new ReadOnlySubscriptionExtension();

        BaseReadOnlySubscriptionExtensionSequence_type1 choice = new BaseReadOnlySubscriptionExtensionSequence_type1();
        choice.setPpsmSupporter(apiExtension);
        result.setBaseReadOnlySubscriptionExtensionSequence_type1(choice);

        return result;
    }

    @Override
    public SubscriberExtension toCRM(final Context ctx, final Object obj, GenericParameter[] parameters) throws HomeException
    {
        
        PPSMSupporterSubExtension crmExtension;
        try
        {
            crmExtension = (PPSMSupporterSubExtension) XBeans.instantiate(PPSMSupporterSubExtension.class, ctx);
        }
        catch (Exception e)
        {
            new MinorLogMsg(this, "Error instantiating new PPSM Supporter extension. Using default constructor.", e).log(ctx);
            crmExtension = new PPSMSupporterSubExtension();
        }
        
        PPSMSupporterSubscriptionExtension apiExtension;
        
        if (obj instanceof SubscriptionExtension)
        {
            apiExtension = ((SubscriptionExtension) obj).getBaseSubscriptionExtensionSequence_type1().getPpsmSupporter();
        }
        else
        {
            apiExtension = (PPSMSupporterSubscriptionExtension) obj;
        }

        crmExtension.setSubId(getSubscriber().getId());
        crmExtension.setBAN(getSubscriber().getBAN());
        crmExtension.setEnabled(true);
        crmExtension.setSpid(getSubscriber().getSpid());
        
        Set<String> chargingTemplates = new HashSet<String>();
        Set<String> screeningTemplates = new HashSet<String>();
        
		if (apiExtension.getChargingTemplates() != null)
		{
			for (Long template : apiExtension.getChargingTemplates())
			{
				chargingTemplates.add(String.valueOf(template));
			}
		}
        
        if (apiExtension.getScreeningTemplates() != null)
		{
			for (Long template : apiExtension.getScreeningTemplates())
			{
				screeningTemplates.add(String.valueOf(template));
			}
        }
		crmExtension.setSavedChargingTemplates("");
		crmExtension.setSavedScreeningTemplates("");
        crmExtension.setChargingTemplates(chargingTemplates);
        crmExtension.setScreeningTemplates(screeningTemplates);
        
        return crmExtension;
    }

    
    @Override
    public boolean update(Context ctx, BaseSubscriptionExtensionReference extensionReference, 
            BaseMutableSubscriptionExtension extension, GenericParameter[] parameters) throws CRMExceptionFault
    {
        RmiApiErrorHandlingSupport.assertExtensionUpdateEnabled(ctx, PPSMSupporterSubExtension.class, this);
        
        boolean extensionExists = false;
        List<PPSMSupporterSubExtension> crmExtensions = ExtensionSupportHelper.get(ctx).getExtensions(ctx, PPSMSupporterSubExtension.class, new EQ(SubscriberExtensionXInfo.SUB_ID, getSubscriber().getId()));
        for (PPSMSupporterSubExtension crmExtension : crmExtensions)
        {
            extensionExists = true;
            boolean dirty = updatePPSMSupporterSubscriptionExtension(ctx, crmExtension, (MutablePPSMSupporterSubscriptionExtension) extension);
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
                    final String msg = "Unable to update PPSM Supporter Subscription Extension";
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
        RmiApiErrorHandlingSupport.assertExtensionRemoveEnabled(ctx, PPSMSupporterSubExtension.class, this);
        
        List<PPSMSupporterSubExtension> crmExtensions = ExtensionSupportHelper.get(ctx).getExtensions(ctx, PPSMSupporterSubExtension.class, new EQ(SubscriberExtensionXInfo.SUB_ID, getSubscriber().getId()));
        for (PPSMSupporterSubExtension crmExtension : crmExtensions)
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
                final String msg = "Unable to remove PPSM Supporter Subscription Extension";
                RmiApiErrorHandlingSupport.handleDeleteExceptions(ctx, e, msg, this);
            }
        }
    }

    @Override
    public ReadOnlySubscriptionExtension find(final Context ctx, final BaseSubscriptionExtensionReference extension) throws CRMExceptionFault
    {
        List<PPSMSupporterSubExtension> crmExtensions = ExtensionSupportHelper.get(ctx).getExtensions(ctx, PPSMSupporterSubExtension.class, new EQ(SubscriberExtensionXInfo.SUB_ID, getSubscriber().getId()));
        for (PPSMSupporterSubExtension crmExtension : crmExtensions)
        {
            try
            {
                return this.toAPI(ctx, crmExtension);
            }
            catch (final Exception e)
            {
                final String msg = "Unable to retrieve PPSM Supporter Subscription Extension";
                RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
            }
            break;
        }
        // Returning read only default subscription extension with correct choice
        final ReadOnlySubscriptionExtension result = new ReadOnlySubscriptionExtension();
        BaseReadOnlySubscriptionExtensionSequence_type1 choice = new BaseReadOnlySubscriptionExtensionSequence_type1();
        choice.setPpsmSupporter(null);
        result.setBaseReadOnlySubscriptionExtensionSequence_type1(choice);
        return result;
        
    }
    
    private boolean updatePPSMSupporterSubscriptionExtension(Context ctx, PPSMSupporterSubExtension crmExtension, MutablePPSMSupporterSubscriptionExtension extension)
    {
        boolean dirty = false;
        
        crmExtension.getChargingTemplates(ctx).clear();
        crmExtension.getScreeningTemplates(ctx).clear();
        
        if (extension.getChargingTemplates() != null)
        {
            for (Long template : extension.getChargingTemplates())
            {
                crmExtension.getChargingTemplates().add(String.valueOf(template));
            }
        }
        
        if (extension.getScreeningTemplates() != null)
        {
            for (Long template : extension.getScreeningTemplates())
            {
                crmExtension.getScreeningTemplates().add(String.valueOf(template));
            }
        }
        
        if (crmExtension.getAddedChargingTemplates(ctx).size()>0 || crmExtension.getRemovedChargingTemplates(ctx).size()>0 ||
                crmExtension.getAddedScreeningTemplates(ctx).size()>0 || crmExtension.getRemovedScreeningTemplates(ctx).size()>0)
        {
            dirty = true;
        }
        
        return dirty;
    }
}
