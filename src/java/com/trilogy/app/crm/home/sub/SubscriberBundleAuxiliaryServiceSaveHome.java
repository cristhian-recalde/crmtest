package com.trilogy.app.crm.home.sub;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bundle.BundleAuxiliaryService;
import com.trilogy.app.crm.bundle.BundleAuxiliaryServiceHome;
import com.trilogy.app.crm.bundle.BundleAuxiliaryServiceXInfo;
import com.trilogy.app.crm.support.BundleAuxiliaryServiceSupport;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.LogSupport;


public class SubscriberBundleAuxiliaryServiceSaveHome extends HomeProxy
{

    public SubscriberBundleAuxiliaryServiceSaveHome(Context ctx, Home home)
    {
        super(ctx,home);
    }
    
    /**
     * Save the association between subscriber to its bundles to the BundleAuxiliaryServiceHome
     */
    public Object create(Context ctx, Object obj) throws HomeException
    {
    	LogSupport.debug(ctx, this, "SubscriberPipeline[create].....");
    	Subscriber sub = (Subscriber) super.create(ctx, obj);

        Home home = (Home) ctx.get(BundleAuxiliaryServiceHome.class);

        // Store their bundles into the bundleauxiliaryservicehome
        Collection bundles = sub.getBundles().values();
        Iterator bundlesIterator = bundles.iterator();
        for ( ; bundlesIterator.hasNext(); )
        {
            BundleFee bundleFee = (BundleFee) bundlesIterator.next();
            BundleAuxiliaryService bundle = BundleAuxiliaryServiceSupport.adaptBundleFeeToBundleAuxiliaryService(ctx, bundleFee, sub);
            /*
             * Make sure this bundle isin't already in the home
             */
            BundleAuxiliaryService foundBundle = (BundleAuxiliaryService) home.find(ctx, new And()
                    .add(new EQ(BundleAuxiliaryServiceXInfo.ID, Long.valueOf(bundle.getId())))
                    .add(new EQ(BundleAuxiliaryServiceXInfo.SUBSCRIBER_ID, bundle.getSubscriberId())));

            bundle.setProvisioned(BundleAuxiliaryServiceSupport.isBucketProvisioned(ctx,
                    (int) sub.getSubscriptionType(), sub.getMSISDN(), bundle.getId()));

            if (foundBundle == null)
            {
                home.create(ctx, bundle);
            }
        }

        return sub;
    }
    
    /**
     * @see com.redknee.framework.xhome.home.HomeSPI#store(com.redknee.framework.xhome.context.Context, java.lang.Object)
     */
    public Object store(Context ctx, Object obj) throws HomeException 
    {
    	LogSupport.debug(ctx, this, "SubscriberPipeline[store].....");
    	Subscriber sub = (Subscriber) super.store(ctx, obj);
        
        Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);

        if (sub.getState() == SubscriberStateEnum.INACTIVE)
        {
            deleteRecords(ctx, sub);
            return sub;
        }

        Home home = (Home) ctx.get(BundleAuxiliaryServiceHome.class);
        
        // Get list of bundles we have kept track of from the BundleAuxiliaryServiceHome
        home = home.where(ctx, new EQ(BundleAuxiliaryServiceXInfo.SUBSCRIBER_ID,sub.getId()));
        
        // Store the list of bundles in a map for easier handling
        final Map currentBundles = new HashMap();
        home.forEach(ctx, new Visitor()
                {
                    public void visit(Context ctx, Object obj) throws AgentException, AbortVisitException
                    {
                        BundleAuxiliaryService bundleAuxiliaryService = (BundleAuxiliaryService) obj;
                        currentBundles.put(Long.valueOf(bundleAuxiliaryService.getId()), bundleAuxiliaryService);
                    }
                });
        
        // Go through list of subscirber services 
        // if we find the bundle within the BundleAuxiliaryServiceHome, do a store on the
        // bundle to update the info
        final Collection bundles = sub.getBundles().values();
        final Iterator bundlesIterator = bundles.iterator();
        for ( ; bundlesIterator.hasNext(); )
        {
            BundleFee bundleFee = (BundleFee) bundlesIterator.next();
            BundleAuxiliaryService bundle = BundleAuxiliaryServiceSupport.adaptBundleFeeToBundleAuxiliaryService(ctx, bundleFee, sub);
            // If we do not currently track the bundle, put the association in the BundleAuxiliaryServiceHome
            BundleAuxiliaryService trackedBundle = (BundleAuxiliaryService) currentBundles.get(Long.valueOf(bundleFee.getId()));
            if (trackedBundle == null)
            {
                home.create(ctx, bundle);
            }
            else
            {
            	// The bundle from the subscriber won't have the primary key of the bundle
            	// we are already tracking, so we need to set that before we can do a store
            	// to update any info
            	bundle.setBundleAuxServId(trackedBundle.getBundleAuxServId());
            	
            	home.store(ctx, bundle);
            	currentBundles.remove(Long.valueOf(bundleFee.getId()));
            }
        }
        
        // Go though list of current bundles we keep track of
        // that the subscriber no longer subscribers to
        final Iterator currentBundlesIterator = currentBundles.values().iterator();
        for ( ; currentBundlesIterator.hasNext(); )
        {
            BundleAuxiliaryService bundleAuxiliaryService = (BundleAuxiliaryService) currentBundlesIterator.next();
            home.remove(ctx, bundleAuxiliaryService);
        }

        return sub;
    }

    /**
     * @see com.redknee.framework.xhome.home.HomeSPI#remove(com.redknee.framework.xhome.context.Context, java.lang.Object)
     */
    public void remove(Context ctx, Object obj) throws HomeException 
    {
    	LogSupport.debug(ctx, this, "SubscriberPipeline[remove].....");
    	final Subscriber sub = (Subscriber) obj;
        deleteRecords(ctx, sub);
        super.remove(ctx, sub);
    }

    private void deleteRecords(Context ctx, Subscriber sub) throws HomeException
    {
        final Home home = (Home) ctx.get(BundleAuxiliaryServiceHome.class);

        // Get list of bundles we have kept track of from the BundleAuxiliaryServiceHome and remove them all
        home.where(ctx, new EQ(BundleAuxiliaryServiceXInfo.SUBSCRIBER_ID, sub.getId())).removeAll();
    }
}
