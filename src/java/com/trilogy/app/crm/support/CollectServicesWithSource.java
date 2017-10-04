package com.trilogy.app.crm.support;

import java.util.Iterator;
import java.util.Map;

import com.trilogy.framework.core.locale.Currency;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.visitor.Visitor;

import com.trilogy.app.crm.bean.ChargingLevelEnum;
import com.trilogy.app.crm.bean.ServicePackageFee;
import com.trilogy.app.crm.bean.ServicePackageVersion;
import com.trilogy.app.crm.bean.ServicePreferenceEnum;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.bean.core.ServicePackage;

/**
 * Visits ServicePackage objects and collects ServiceFee2 into provided Map while setting the source property.
 */
public class CollectServicesWithSource implements Visitor
{
    private final Map serviceFeesMap;
    private final Map packageFeeMap;

    public CollectServicesWithSource(Map serviceFeesMap, final Map packageFeeMap)
    {
        this.serviceFeesMap = serviceFeesMap;
        this.packageFeeMap = packageFeeMap;
    }

    public void visit(Context ctx, Object obj)
    {
        ServicePackage pack = (ServicePackage) obj;
        ServicePackageVersion version = pack.getCurrentVersion(ctx);
        ServicePackageFee packFee = (ServicePackageFee)packageFeeMap.get(Integer.valueOf(pack.getId()));
        
        boolean mandatory = packFee.isMandatory();
        
        
        StringBuilder sb = new StringBuilder();
        sb.append("Package: ");
        sb.append(pack.getName());
        if (pack.getChargingLevel().equals(ChargingLevelEnum.PACKAGE))
        {
            sb.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; Fee: ");
            Currency currency = (Currency) ctx.get(Currency.class, Currency.DEFAULT);
            sb.append(currency.formatValue(pack.getRecurringRecharge()));
        }
        String name = sb.toString();
        Map packServices = version.getServiceFees();

        Iterator it = packServices.values().iterator();
        while (it.hasNext())
        {
            ServiceFee2 serviceFee = (ServiceFee2) it.next();
            serviceFee.setSource(name);
            if (mandatory)
             {
                serviceFee.setServicePreference(ServicePreferenceEnum.MANDATORY);
            	//skushwaha serviceFee.setMandatory(true);
            }
            
            serviceFeesMap.put(Long.valueOf(serviceFee.getServiceId()), serviceFee);
        }
    }

    public static final long serialVersionUID = 12L;
}
