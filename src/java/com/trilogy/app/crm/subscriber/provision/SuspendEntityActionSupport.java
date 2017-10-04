package com.trilogy.app.crm.subscriber.provision;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.trilogy.app.crm.bas.recharge.SuspendEntitiesVisitor;
import com.trilogy.app.crm.bas.recharge.SuspensionSupport;
import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.bean.CallingGroupTypeEnum;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServicePackageVersion;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.core.AuxiliaryService;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.bean.core.ServicePackage;
import com.trilogy.app.crm.bean.service.SuspendReasonEnum;
import com.trilogy.app.crm.bundle.service.CRMSubscriberBucketProfile;
import com.trilogy.app.crm.support.BundleSupportHelper;
import com.trilogy.app.crm.support.ServiceSupport;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
import com.trilogy.app.crm.support.SuspendedEntitySupport;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;

public abstract class SuspendEntityActionSupport {


    public static void processServicesExternal(Context ctx, Subscriber sub, Collection services, boolean suspend)
    {
        Iterator it = services.iterator();
        while (it.hasNext())
        {
            ServiceFee2 serviceFee = (ServiceFee2) it.next();
            processServiceExternal(ctx, sub, serviceFee, suspend);
        }
    }

    
    public static int suspendService(Context ctx,Subscriber sub, ServiceFee2 fee)
    {
        int ret =  processServiceExternal(ctx, sub, fee, true);
        
        try {
        	
        	//If Subscriber service is already suspended due to insufficient balance but entry is not available in SuspendEntity.  
        	if (!fee.getSource().startsWith("Package") &&
                    sub.getSuspendedServices(ctx).get(XBeans.getIdentifier(fee))!= null)
            {
        		SuspendedEntitySupport.createSuspendedEntity(ctx, sub.getId(), fee.getServiceId(),
                        SubscriberAuxiliaryServiceSupport.SECONDARY_ID_NOT_USED, ServiceFee2.class);
            }
        	
        	
            if (!fee.getSource().startsWith("Package") &&
                    sub.getSuspendedServices(ctx).get(XBeans.getIdentifier(fee))== null)
            {
            	// signature changed, fix me.
                sub.insertSuspendedService(ctx, fee, SuspendReasonEnum.NONPAYMENT);
            }
        } catch ( Exception e)
        {
            new MinorLogMsg(SuspendEntityActionSupport.class, "fail to inert into suspended entity table subid ="
                    + sub.getId() + " serviceid = " + fee.getServiceId(), e ).log(ctx); 
            if ( ret != SUCCESS )
            {
                ret = BOTH_FAIL;
            } else 
            {
                ret = INTERNAL_FAIL;
            }

        }
        
        return ret; 
    }
    
    public static int unsuspendService(Context ctx,Subscriber sub, ServiceFee2 fee)
    {
        int ret =  processServiceExternal(ctx, sub, fee, false); 
        
        try {
            if ( sub.getSuspendedServices(ctx).get(XBeans.getIdentifier(fee))!= null)
            {
                sub.removeSuspendedService(ctx, fee);
            }
        } catch ( Exception e)
        {
            new MinorLogMsg(SuspendEntityActionSupport.class, "fail to dlete suspended entity table subid ="
                    + sub.getId() + " serviceid = " + fee.getServiceId(), e ).log(ctx); 
            if ( ret != SUCCESS )
            {
                ret = BOTH_FAIL;
            } else 
            {
                ret = INTERNAL_FAIL;
            }

        }
        
        return ret;  
    }

    
    
    public static int processServiceExternal(Context ctx,Subscriber sub, ServiceFee2 fee,  boolean suspend)
    {
        Map<Long, ServiceFee2> map = new HashMap<Long, ServiceFee2>();
        map.put(fee.getServiceId(), fee);
        return SuspensionSupport.suspendServices(ctx, sub, map, suspend)?SuspendEntityActionSupport.SUCCESS:SuspendEntityActionSupport.EXTERNAL_FAIL;
    }
    
    public static void processBundlesExternal(Context ctx, Subscriber sub, Collection bundles, boolean suspend)
    {
        Iterator it = bundles.iterator();
        while (it.hasNext())
        {
            BundleFee bundleFee = (BundleFee) it.next();
            processBundleExternal(ctx, sub, bundleFee, suspend); 
        }
    }

    
    
    public static int suspendBundle(Context ctx, Subscriber sub, BundleFee fee)
    {
        int ret =  processBundleExternal(ctx, sub, fee, true);
        
        try {
            if (!fee.getSource().startsWith("Package") &&
                    sub.getSuspendedBundles(ctx).get(XBeans.getIdentifier(fee))== null)
            {
                sub.insertSuspendedBundles(ctx, fee);
            }
        } catch ( Exception e)
        {
            new MinorLogMsg(SuspendEntityActionSupport.class, "fail to inert into suspended entity table subid ="
                    + sub.getId() + " bundle id = " + fee.getId(), e ).log(ctx); 
            if ( ret != SUCCESS )
            {
                ret = BOTH_FAIL;
            } else 
            {
                ret = INTERNAL_FAIL;
            }

        }
        
        return ret; 
    }
    
    public static int unsuspendBundle(Context ctx, Subscriber sub, BundleFee fee)
    {
        int ret =  processBundleExternal(ctx, sub, fee, false); 
        
        try {
            if ( sub.getSuspendedBundles(ctx).get(XBeans.getIdentifier(fee))!= null)
            {
                sub.removeSuspendedBundles(ctx, fee);
            }
        } catch ( Exception e)
        {
            new MinorLogMsg(SuspendEntityActionSupport.class, "fail to delete suspended entity table subid ="
                    + sub.getId() + "  bundle id = " + fee.getId(), e ).log(ctx); 
            if ( ret != SUCCESS )
            {
                ret = BOTH_FAIL;
            } else 
            {
                ret = INTERNAL_FAIL;
            }

        }
        
        return ret;  
    }   
    
    public static int processBundleExternal(Context ctx, Subscriber sub, BundleFee fee, boolean suspend)
    {
        int ret = SUCCESS; 

        final CRMSubscriberBucketProfile bucketService;
        final BundleProfile bundleProfile;
        try
        {
            bucketService = (CRMSubscriberBucketProfile)ctx.get(CRMSubscriberBucketProfile.class);
            bundleProfile = BundleSupportHelper.get(ctx).getBundleProfile(ctx, fee.getId());
            if (bucketService == null)
            {
                throw new HomeException("CRMSubscriberBucketProfile.class not found in Context");
            }
        }
        catch (Exception e)
        {
            new MajorLogMsg(SuspendEntitiesVisitor.class.getName(),
                    "Problem occurred while suspending bundles for"
                    + " Prepaid subscriber " + sub.getId()
                    + " with insufficient balance.",
                    e).log(ctx);            
            return INTERNAL_FAIL; 
        }

        try
        {
            bucketService.updateBucketStatus(ctx, sub.getMSISDN(), sub.getSpid(),
                    (int) sub.getSubscriptionType(), fee.getId(),
                    !suspend, !bundleProfile.getSmartSuspensionEnabled() && !bundleProfile.getRecurrenceScheme().isOneTime());
        }
        catch (Exception e)
        {
            new MajorLogMsg(SuspendEntitiesVisitor.class.getName(),
                    "Problem occurred while suspending bundles for"
                    + " Prepaid subscriber " + sub.getId()
                    + " with insufficient balance.",
                    e).log(ctx);
            ret = EXTERNAL_FAIL;
        }
        
        
        try {
            if (!fee.getSource().startsWith("Package") && 
                    sub.getSuspendedBundles(ctx).get(XBeans.getIdentifier(fee))== null){
                sub.insertSuspendedBundles(ctx, fee);
            }
        } 
        catch ( Exception e)
        {
            new MinorLogMsg(SuspendEntityActionSupport.class, "fail to inert into suspended entity table subid ="
                    + sub.getId() + " bundleid = " + fee.getId(), e ).log(ctx); 
            if ( ret != SUCCESS )
            {
                ret = BOTH_FAIL;
            } 
            else 
            {
                ret = INTERNAL_FAIL;
            }

        }

        return ret; 
    } 


    private static Map<Long, Map<Long, SubscriberAuxiliaryService>> getSuspendedAuxiliaryServices(Context ctx, Subscriber sub,
            AuxiliaryService auxSrv)
    {
        Collection<SubscriberAuxiliaryService> col = sub.getAuxiliaryServices(ctx);
        Collection<SubscriberAuxiliaryService> suspendList = new ArrayList<SubscriberAuxiliaryService>();
        Map<Long, Map<Long, SubscriberAuxiliaryService>> map = new HashMap<Long, Map<Long, SubscriberAuxiliaryService>>();
        for (SubscriberAuxiliaryService subAuxService : col)
        {
            if (auxSrv.getIdentifier() == subAuxService.getAuxiliaryServiceIdentifier())
            {
                Map<Long, SubscriberAuxiliaryService> subAuxSrvMap = map.get(auxSrv.getIdentifier());
                if (subAuxSrvMap == null)
                {
                    subAuxSrvMap = new HashMap<Long, SubscriberAuxiliaryService>();
                }
                subAuxSrvMap.put(Long.valueOf(subAuxService.getSecondaryIdentifier()), subAuxService);
                map.put(Long.valueOf(auxSrv.getIdentifier()), subAuxSrvMap);
                suspendList.add(subAuxService);
            }
        }
        return map;
    }
    

    public static int suspendAuxService(Context ctx, Subscriber sub, AuxiliaryService auxSrv, Object caller)
    {
        Map<Long, Map<Long, SubscriberAuxiliaryService>> map = getSuspendedAuxiliaryServices(ctx, sub, auxSrv);
        int ret = SuspensionSupport.suspendAuxServices(ctx, sub, map, true, caller)
                ? SuspendEntityActionSupport.SUCCESS
                : SuspendEntityActionSupport.EXTERNAL_FAIL;
        try
        {
            if ((ret == SuspendEntityActionSupport.SUCCESS)
                    && sub.getSuspendedAuxServices(ctx).get(XBeans.getIdentifier(auxSrv)) == null)
            {
                Map<Long, SubscriberAuxiliaryService> suspendMap = map.get(Long.valueOf(auxSrv.getID()));
                if (suspendMap != null)
                {
                    for (SubscriberAuxiliaryService subAuxService : suspendMap.values())
                    {
                        // signature changed, fix me
                        sub.insertSuspendedAuxService(ctx, subAuxService);
                    }
                }
                ret = SUCCESS;
            }
        }
        catch (Exception e)
        {
            new MinorLogMsg(SuspendEntityActionSupport.class, "fail to insert into suspended entity table subid ="
                    + sub.getId() + " Aux service id = " + auxSrv.getIdentifier(), e).log(ctx);
            if (ret != SUCCESS)
            {
                ret = BOTH_FAIL;
            }
            else
            {
                ret = INTERNAL_FAIL;
            }
        }
        return ret;
    }


    public static int unsuspendAuxService(Context ctx, Subscriber sub, AuxiliaryService auxSrv, Object caller)
    {
        Map<Long, Map<Long, SubscriberAuxiliaryService>> map = getSuspendedAuxiliaryServices(ctx, sub, auxSrv);
        int ret = SuspensionSupport.suspendAuxServices(ctx, sub, map, false,caller)
                ? SuspendEntityActionSupport.SUCCESS
                : SuspendEntityActionSupport.EXTERNAL_FAIL;
        try
        {
            if ((ret == SuspendEntityActionSupport.SUCCESS)
                    && sub.getSuspendedAuxServices(ctx).get(XBeans.getIdentifier(auxSrv)) != null)
            {
                Map<Long, SubscriberAuxiliaryService> suspendMap = map.get(Long.valueOf(auxSrv.getID()));
                if (suspendMap != null)
                {
                    for (SubscriberAuxiliaryService subAuxService : suspendMap.values())
                    {
                        sub.removeSuspendedAuxService(ctx, subAuxService);
                    }
                    ret = SUCCESS;
                }
            }
        }
        catch (Exception e)
        {
            new MinorLogMsg(SuspendEntityActionSupport.class, "fail to delete suspended entity table subid ="
                    + sub.getId() + " Aux serviceid = " + auxSrv.getIdentifier(), e).log(ctx);
            if (ret != SUCCESS)
            {
                ret = BOTH_FAIL;
            }
            else
            {
                ret = INTERNAL_FAIL;
            }
        }
        return ret;
    }
     
     

    
 
 
    public static void processPackagesExternal(Context ctx, Subscriber sub, Collection pkgs, boolean suspend)
    {
        Iterator it = pkgs.iterator();
        while (it.hasNext())
        {
            ServicePackage pkg = (ServicePackage) it.next();
            processPackageExternal(ctx, sub, pkg, suspend); 
        }
    }

 
    public static int suspendPackage(Context ctx,Subscriber sub, ServicePackage fee)
    {
        int ret = processPackageExternal(ctx, sub, fee, true);
        
        try {
            if (sub.getSuspendedPackages(ctx).get(XBeans.getIdentifier(fee))== null)
            {
                sub.insertSuspendedPackage(ctx, fee);
            }
        } catch ( Exception e)
        {
            new MinorLogMsg(SuspendEntityActionSupport.class, "fail to inert into suspended entity table subid ="
                    + sub.getId() + " package id = " + fee.getId(), e ).log(ctx); 
            if ( ret != SUCCESS )
            {
                ret = BOTH_FAIL;
            } else 
            {
                ret = INTERNAL_FAIL;
            }

        }
        
        return ret; 
    }
    
    public static int unsuspendPackage(Context ctx,Subscriber sub, ServicePackage fee)
    {
        int ret =  processPackageExternal(ctx, sub, fee, false); 
        
        try {
            if ( sub.getSuspendedPackages(ctx).get(XBeans.getIdentifier(fee))!= null)
            {
                sub.removeSuspendedPackage(ctx, fee);
            }
        } catch ( Exception e)
        {
            new MinorLogMsg(SuspendEntityActionSupport.class, "fail to dlete suspended entity table subid ="
                    + sub.getId() + " package id = " + fee.getId(), e ).log(ctx); 
            if ( ret != SUCCESS )
            {
                ret = BOTH_FAIL;
            } else 
            {
                ret = INTERNAL_FAIL;
            }

        }
        
        return ret;  
    }
    
    public static int processPackageExternal(Context ctx, Subscriber sub, ServicePackage pkg, boolean suspend)
    {
        int ret = SUCCESS; 

        final HashMap serviceMap = new HashMap();
        final HashMap bundleMap = new HashMap();

        ServicePackageVersion ver = pkg.getCurrentVersion(ctx);

        Map serviceFees = ver.getServiceFees();
        Iterator iter = serviceFees.values().iterator();
        while (iter.hasNext())
        {
            ServiceFee2 serviceFee = (ServiceFee2) iter.next();
            serviceMap.put(XBeans.getIdentifier(serviceFee), serviceFee);
        }

        Map bundleFees = ver.getBundleFees();
        iter = bundleFees.values().iterator();
        while (iter.hasNext())
        {
            BundleFee bundleFee = (BundleFee) iter.next();
            bundleMap.put(XBeans.getIdentifier(bundleFee), bundleFee);
        }

        processServicesExternal(ctx, sub, serviceMap.values(), suspend);
        processBundlesExternal(ctx, sub, bundleMap.values(), suspend);

        try {
            if (sub.getSuspendedPackages(ctx).get(XBeans.getIdentifier(pkg))== null){
                sub.insertSuspendedPackage(ctx, pkg);
            }
        } catch ( Exception e)
        {
            new MinorLogMsg(SuspendEntityActionSupport.class, "fail to inert into suspended entity table subid ="
                    + sub.getId() + " packageid = " + pkg.getId(), e ).log(ctx); 
            ret = INTERNAL_FAIL;
        }

        return ret; 
    }

    public static Service serviceFromServiceFee(Context ctx, ServiceFee2 serviceFee, Subscriber sub)
    {
        try
        {
            return ServiceSupport.getService(ctx, serviceFee.getServiceId());
        }
        catch (HomeException e)
        {
            new MajorLogMsg(SuspendEntitiesVisitor.class.getName(),
                    "Problem occurred while retrieveing service " + serviceFee.getServiceId()
                    + " for subscriber " + sub.getId(),
                    e).log(ctx);
        }
        return null;
    }


    public static boolean willUnsubscribe(Context ctx, Map packages, Map services, Map bundles, Map auxServices)
    {
        boolean result = false;
        Iterator it = auxServices.values().iterator();
        while (it.hasNext())
        {
            AuxiliaryService auxSrv = (AuxiliaryService) it.next();

            if ((auxSrv.isPLP(ctx))
                    || auxSrv.getType() == AuxiliaryServiceTypeEnum.HomeZone)
            {
                result = true;
                break;
            }
        }

        return result;
    }
    
    public static final int SUCCESS = 0;
    public static final int EXTERNAL_FAIL = 1; 
    public static final int INTERNAL_FAIL = 2; 
    public static final int BOTH_FAIL = 3; 
    public static final int REMOVED = 4; 
    
}
