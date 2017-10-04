package com.trilogy.app.crm.priceplan;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.*;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.*;
import com.trilogy.app.crm.bean.core.ServiceFee2;

/**
 * Ensure there is only one service selected of a service type
 * 
 * @author victor.stratan@redknee.com
 */
public class ServicePackageVersionUniqueServiceValidator implements Validator
{
    public void validate(Context ctx, Object obj) throws IllegalStateException
    {
        ServicePackageVersion spv = (ServicePackageVersion) obj;
        final CompoundIllegalStateException el = new CompoundIllegalStateException();
        final HashMap allTypes = new HashMap();

        Collection serviceFees = spv.getServiceFees().values();
        final Home serviceHome = (Home) ctx.get(ServiceHome.class);
        try
        {
            Iterator it = serviceFees.iterator();
            while (it.hasNext())
            {
                ServiceFee2 serviceFee = (ServiceFee2) it.next();
                try
                {
                    Service service = (Service) serviceHome.find(ctx, Long.valueOf(serviceFee.getServiceId()));
                    ServiceTypeEnum type = service.getType();

                    if (allTypes.containsKey(type))
                    {
                        el.thrown(new IllegalPropertyArgumentException(PricePlanVersionXInfo.SERVICE_PACKAGE_VERSION,
                                "Duplicate service type " + type + " for selected service " + service.getID() + "."));
                        if (allTypes.get(type) != null)
                        {
                            el.thrown(new IllegalPropertyArgumentException(PricePlanVersionXInfo.SERVICE_PACKAGE_VERSION,
                                    "Duplicate service type " + type + " for selected service " + 
                                    ((Service) allTypes.get(type)).getID() + "."));
                            allTypes.put(type, null);
                        }
                    }
                    else
                    {
                        allTypes.put(type, service);
                    }
                } catch (HomeException e)
                {
                    el.thrown(new IllegalArgumentException("Cannot find service " + serviceFee.getServiceId() + "."));
                    new MinorLogMsg(this, "Cannot access " + ServiceHome.class.getName() + " home!", e).log(ctx);
                }
            }
        } finally
        {
            el.throwAll();
        }
    }
}
