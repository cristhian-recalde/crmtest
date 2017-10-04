package com.trilogy.app.crm.priceplan;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.PricePlanVersionXInfo;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServiceHome;
import com.trilogy.app.crm.bean.ServicePackageVersion;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.bean.core.ServicePackage;

/**
 * Collects services with unique types and logs exceptions in CompoundIllegalStateException
 * for services that have duplicated types
 *
 * @author victor.stratan@redknee.com
 */
public class CollectUniqueServiceTypesVisitor implements Visitor
{
    final HashMap allTypes;
    final CompoundIllegalStateException el;

    public CollectUniqueServiceTypesVisitor(HashMap allTypes, CompoundIllegalStateException el)
    {
        this.allTypes = allTypes;
        this.el = el;
    }

    public void visit(Context ctx, Object obj) throws AgentException
    {
        ServicePackage pack = (ServicePackage) obj;
        ServicePackageVersion version = pack.getCurrentVersion(ctx);
        Map packServices = version.getServiceFees();
        Iterator it = packServices.values().iterator();
        final Home serviceHome = (Home) ctx.get(ServiceHome.class);
        while (it.hasNext())
        {
            Service service = null;
            ServiceFee2 fee = (ServiceFee2) it.next();

            try
            {
                service = (Service) serviceHome.find(ctx, Long.valueOf(fee.getServiceId()));
            }
            catch (HomeException e)
            {
                el.thrown(new IllegalArgumentException("Cannot find service " + it.next() + "."));
                new MinorLogMsg(this, "Cannot access " + ServiceHome.class.getName() + " home!", e).log(ctx);
                throw new AgentException("Cannot find service " + it.next() + ".", e);
            }

            ServiceTypeEnum type = service.getType();
            // apply "one service of X type" constraint only for
            // non-Generic, non-Data services and non-Transfer
            if (type == ServiceTypeEnum.GENERIC || type == ServiceTypeEnum.DATA
                    || type == ServiceTypeEnum.TRANSFER || type == ServiceTypeEnum.EXTERNAL_PRICE_PLAN
                    	|| type == ServiceTypeEnum.PACKAGE)
            {
                continue;
            }

            if (allTypes.containsKey(type))
            {
                el.thrown(new IllegalPropertyArgumentException(
                        PricePlanVersionXInfo.SERVICE_PACKAGE_VERSION,
                        "Duplicate service type " + type + " inside package '" + pack.getId() + " - " + pack.getName() + "'."));
                if (allTypes.get(type) instanceof ServicePackage)
                {
                    el.thrown(new IllegalPropertyArgumentException(PricePlanVersionXInfo.SERVICE_PACKAGE_VERSION,
                            "Duplicate service type " + type + " inside package '"
                                    + ((ServicePackage) allTypes.get(type)).getId() + " - "
                                    + ((ServicePackage) allTypes.get(type)).getName() + "'."));
                    allTypes.put(type, null);
                }
                else if (allTypes.get(type) instanceof Service)
                {
                    long serviceID = ((Service) allTypes.get(type)).getID();
                    String serviceName = ((Service) allTypes.get(type)).getName();
                    el.thrown(new IllegalPropertyArgumentException(
                            PricePlanVersionXInfo.SERVICE_PACKAGE_VERSION,
                            "Duplicate service type " + type + " for selected service '" + serviceID + " - " + serviceName + "'."));
                    allTypes.put(type, null);
                }
            }
            else
            {
                allTypes.put(type, pack);
            }
        }
    }

    private static final long serialVersionUID = 12L;
}
