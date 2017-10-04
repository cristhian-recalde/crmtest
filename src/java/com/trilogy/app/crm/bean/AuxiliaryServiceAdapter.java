package com.trilogy.app.crm.bean;

import com.trilogy.app.crm.bean.core.AuxiliaryService;
import com.trilogy.app.crm.extension.auxiliaryservice.core.CallingGroupAuxSvcExtension;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;

public class AuxiliaryServiceAdapter  implements Adapter
{
	/**
     * INHERIT
     */
	public Object adapt(Context ctx,Object obj) throws HomeException
	{
		if (obj == null)
		{
			return null;
		}
		
        AuxiliaryService service = (AuxiliaryService) obj;
        if (AuxiliaryServiceTypeEnum.CallingGroup.equals(service.getType()))
        {
            CallingGroupAuxSvcExtension callingGroupAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx,
                    service, CallingGroupAuxSvcExtension.class);
            if (callingGroupAuxSvcExtension!=null)
            {
                if(callingGroupAuxSvcExtension.getCallingGroupType().equals(CallingGroupTypeEnum.PCUG))
                {
                    callingGroupAuxSvcExtension.setServiceChargePostpaid( service.getCharge()); 
                }
            }
        }
        
        return service; 
	}
	
    /**
     * INHERIT
     */
	public Object unAdapt(Context ctx,Object obj) throws HomeException
	{
		if (obj == null)
		{
			return null;
		}
		
        AuxiliaryService service = (AuxiliaryService) obj;
        if (AuxiliaryServiceTypeEnum.CallingGroup.equals(service.getType()))
        {
            CallingGroupAuxSvcExtension callingGroupAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx,
                    service, CallingGroupAuxSvcExtension.class);
            if (callingGroupAuxSvcExtension!=null)
            {
                if(callingGroupAuxSvcExtension.getCallingGroupType().equals(CallingGroupTypeEnum.PCUG))
                {
                	service.setCharge(callingGroupAuxSvcExtension.getServiceChargePostpaid()); 
                }
            }
        }
        
        return service; 
	}
}