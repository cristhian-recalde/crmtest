package com.trilogy.app.crm.priceplan;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.bean.PricePlanSubTypeEnum;
import com.trilogy.app.crm.bean.PricePlanVersion;
import com.trilogy.app.crm.bean.PricePlanVersionXInfo;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServiceHome;
import com.trilogy.app.crm.bean.ServicePackageFee;
import com.trilogy.app.crm.bean.ServicePackageHome;
import com.trilogy.app.crm.bean.ServicePackageXInfo;
import com.trilogy.app.crm.bean.ServicePreferenceEnum;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.core.PricePlan;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.blackberry.BlackberrySupport;
import com.trilogy.app.crm.support.LicensingSupportHelper;
import com.trilogy.app.crm.support.PricePlanSupport;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;

/**
 * Ensure there is only one service selected of a service type
 * in selected services or inside selected packages
 *
 * @author victor.stratan@redknee.com
 */
public class PricePlanVersionUniqueServiceValidator implements Validator
{
    public void validate(Context ctx, Object obj) throws IllegalStateException
    {
        PricePlanVersion ppv = (PricePlanVersion) obj;
        final CompoundIllegalStateException el = new CompoundIllegalStateException();
        final HashMap<ServiceTypeEnum, Object> allTypes = new HashMap<ServiceTypeEnum, Object>();

        Collection serviceFees = ppv.getServicePackageVersion().getServiceFees().values();
        final Home serviceHome = (Home) ctx.get(ServiceHome.class);
        try
        {
            int numData = 0;
            int numBlackberry = 0;
            boolean mandatoryData = false;
            boolean mandatoryBlackberry = false;
            Service lastBlackberry = null;
            
            PricePlan pricePlan = null;
            try
            {
                pricePlan = PricePlanSupport.getPlan(ctx, ppv.getId());
            }
            catch (HomeException e)
            {
                String msg = "Exception occured when trying to retrieve the Price Plan for Price Plan Version: " + ppv.getId() + ". " + e.getMessage();
                LogSupport.minor(ctx, this, msg, e);
                el.thrown(new IllegalArgumentException(msg));
            }
            
            Iterator it = serviceFees.iterator();
            while (it.hasNext())
            {
                ServiceFee2 serviceFee = (ServiceFee2) it.next();

                try
                {
                    Service service = (Service) serviceHome.find(ctx, Long.valueOf(serviceFee.getServiceId()));
                    ServiceTypeEnum type = service.getType();
                    // apply "one service of X type" constraint only for
                    // non-Generic, non-Data services and non-Transfer
                    if (type == ServiceTypeEnum.DATA)
                    {
                        numData++;
                        if (ServicePreferenceEnum.MANDATORY.equals(serviceFee.getServicePreference()))
                        {
                            mandatoryData = true;
                        }

                        if (mandatoryData && numData>1)
                        {
                            el.thrown(new IllegalPropertyArgumentException(
                                    PricePlanVersionXInfo.SERVICE_PACKAGE_VERSION, "Mandatory "
                                            + ServiceTypeEnum.DATA
                                            + " service is present and therefore only one "
                                            + ServiceTypeEnum.DATA + " can be selected."));
                        }

                        if (allTypes.containsKey(ServiceTypeEnum.BLACKBERRY) && BlackberrySupport.areBlackberryServicesProvisionedToIPC(ctx))
                        {
                            el.thrown(new IllegalPropertyArgumentException(
                                PricePlanVersionXInfo.SERVICE_PACKAGE_VERSION, ServiceTypeEnum.DATA + " service '"
                                        + service.getID() + " - " + service.getName() + "' cannot be selected while "
                                        + ServiceTypeEnum.BLACKBERRY + " service '"
                                        + ((Service) allTypes.get(ServiceTypeEnum.BLACKBERRY)).getID() + " - "
                                        + ((Service) allTypes.get(ServiceTypeEnum.BLACKBERRY)).getName()
                                        + "' is selected."));
                        }
                        
                        if (!allTypes.containsKey(ServiceTypeEnum.DATA))
                        {
                            allTypes.put(type, service);
                        }

                        continue;
                    }
                    else if (type == ServiceTypeEnum.BLACKBERRY)
                    {
                        numBlackberry++;
                        lastBlackberry = service;
                        if (ServicePreferenceEnum.MANDATORY.equals(serviceFee.getServicePreference()))
                        {
                            mandatoryBlackberry = true;
                        }
                        
                        if (mandatoryBlackberry && numBlackberry>1)
                        {
                            el.thrown(new IllegalPropertyArgumentException(
                                    PricePlanVersionXInfo.SERVICE_PACKAGE_VERSION, "Mandatory "
                                            + ServiceTypeEnum.BLACKBERRY
                                            + " service is present and therefore only one "
                                            + ServiceTypeEnum.BLACKBERRY + " can be selected."));
                        }
                        
                        if (allTypes.containsKey(ServiceTypeEnum.DATA) && BlackberrySupport.areBlackberryServicesProvisionedToIPC(ctx))
                        {
                            el.thrown(new IllegalPropertyArgumentException(
                                    PricePlanVersionXInfo.SERVICE_PACKAGE_VERSION, ServiceTypeEnum.DATA + " service '"
                                            + ((Service) allTypes.get(ServiceTypeEnum.DATA)).getID() + " - "
                                            + ((Service) allTypes.get(ServiceTypeEnum.DATA)).getName()
                                            + "' cannot be selected while " + ServiceTypeEnum.BLACKBERRY + " service '"
                                            + service.getID() + " - " + service.getName() + "' is selected."));
                        }

                        if (!allTypes.containsKey(ServiceTypeEnum.BLACKBERRY))
                        {
                            allTypes.put(type, service);
                        }
                        continue;
                    }
                    else if (type == ServiceTypeEnum.GENERIC
                            || type == ServiceTypeEnum.TRANSFER || type == ServiceTypeEnum.EXTERNAL_PRICE_PLAN || type == ServiceTypeEnum.PACKAGE || type == ServiceTypeEnum.URCS_PROMOTION
                            || type == ServiceTypeEnum.CALLING_GROUP)
                    {
                        continue;
                    } else if ( type == ServiceTypeEnum.VOICEMAIL && 
                    		LicensingSupportHelper.get(ctx).isLicensed(ctx, LicenseConstants.TELUS_GATEWAY_LICENSE_KEY))
                    {
                    	// allow multiple voice mail service in price plan. multiple vm_id need to be mapped into multiple vm service. 
                    	continue; 
                    }                   
                    
                    if (allTypes.containsKey(type))
                    {
                    	// PickNPay Feature: There can be multiple optional voice, data, sms (basic) services with different recurrence interval
                    	if(pricePlan!=null && pricePlan.getPricePlanSubType().equals(PricePlanSubTypeEnum.PICKNPAY) 
                    			&& allTypes.get(type) != null)
                    	{
                    		if(((Service)allTypes.get(type)).getChargeScheme().equals(service.getChargeScheme())
                    				&& ((Service)allTypes.get(type)).getRecurrenceInterval() == service.getRecurrenceInterval())
                    		{
                    			 el.thrown(new IllegalPropertyArgumentException(PricePlanVersionXInfo.SERVICE_PACKAGE_VERSION,
                                         "Duplicate service type " + type + " for selected PickNPay service '" + service.getID() + " - " + service.getName() + "'."));
                    			 
                    			 el.thrown(new IllegalPropertyArgumentException(PricePlanVersionXInfo.SERVICE_PACKAGE_VERSION,
 	                                    "Duplicate service type " + type + " for selected PickNPay service '" + ((Service) allTypes.get(type)).getID() + " - " + 
 	                                    ((Service) allTypes.get(type)).getName() + "'."));
                    		}
                    		else
                    		{
                    			continue;
                    		}
                    	}
                    	else
                    	{
	                        el.thrown(new IllegalPropertyArgumentException(PricePlanVersionXInfo.SERVICE_PACKAGE_VERSION,
	                                "Duplicate service type " + type + " for selected service '" + service.getID() + " - " + service.getName() + "'."));
	                        if (allTypes.get(type) != null)
	                        {
	                            el.thrown(new IllegalPropertyArgumentException(PricePlanVersionXInfo.SERVICE_PACKAGE_VERSION,
	                                    "Duplicate service type " + type + " for selected service '" + ((Service) allTypes.get(type)).getID() + " - " + 
	                                    ((Service) allTypes.get(type)).getName() + "'."));
	                            allTypes.put(type, null);
	                        }
                    	}
                    }
                    else
                    {
                        allTypes.put(type, service);
                    }
                } catch (HomeException e)
                {
                    el.thrown(new IllegalArgumentException("Cannot find service " + serviceFee.getServiceId() + "."));
                }
            }
            
            if (lastBlackberry!=null)
            {
                allTypes.put(ServiceTypeEnum.BLACKBERRY, lastBlackberry);
            }

            final Map packages = ppv.getServicePackageVersion().getPackageFees();

            final Set mandatoryPackages = new HashSet();
            Iterator pkgIter = packages.entrySet().iterator();
            while (pkgIter.hasNext())
            {
                Map.Entry entry = (Map.Entry) pkgIter.next();
                Object key = entry.getKey();
                ServicePackageFee fee = (ServicePackageFee) entry.getValue();
                if (fee.isMandatory())
                {
                    mandatoryPackages.add(key);
                }
            }

            Home home = (Home) ctx.get(ServicePackageHome.class);
            home = home.where(ctx, new In(ServicePackageXInfo.ID, mandatoryPackages));
            home.forEach(new CollectUniqueServiceTypesVisitor(allTypes, el));
        } catch (HomeException e)
        {
            el.thrown(new IllegalArgumentException("Cannot access " + ServicePackageHome.class.getName() + " home!"));
            new MajorLogMsg(this, "Cannot access " + ServicePackageHome.class.getName() + " home!", e).log(ctx);
        } finally
        {
            el.throwAll();
        }
    }
}
