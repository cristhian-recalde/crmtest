package com.trilogy.app.crm.validator;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.ui.ServiceXInfo;
import com.trilogy.app.crm.blackberry.BlackberrySupport;
import com.trilogy.app.crm.extension.Extension;
import com.trilogy.app.crm.extension.ExtensionAware;
import com.trilogy.app.crm.extension.service.BlackberryServiceExtension;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;


public class BlackberryServiceMandatoryFieldsValidator implements Validator
{
    public void validate(Context ctx, Object obj)
    throws IllegalStateException 
    {
        Service service = (Service) obj;
        if (service.getType().equals(ServiceTypeEnum.BLACKBERRY))
        {
            CompoundIllegalStateException exception = new CompoundIllegalStateException();
            if (service.getProvisionConfigs().trim().length() == 0)
            {
                exception.thrown(new IllegalPropertyArgumentException(ServiceXInfo.PROVISION_CONFIGS,
                        "Postpaid provisioning HLR Command cannot be empty for BlackBerry services."));
            }
            if (service.getUnprovisionConfigs().trim().length() == 0)
            {
                exception.thrown(new IllegalPropertyArgumentException(ServiceXInfo.UNPROVISION_CONFIGS,
                        "Postpaid unprovisioning HLR Command cannot be empty for BlackBerry services."));
            }
            if (service.getSuspendConfigs().trim().length() == 0)
            {
                exception.thrown(new IllegalPropertyArgumentException(ServiceXInfo.SUSPEND_CONFIGS,
                        "Postpaid suspending HLR Command cannot be empty for BlackBerry services."));
            }
            if (service.getResumeConfigs().trim().length() == 0)
            {
                exception.thrown(new IllegalPropertyArgumentException(ServiceXInfo.RESUME_CONFIGS,
                        "Postpaid resuming HLR Command cannot be empty for BlackBerry services."));
            }
            if (service.getPrepaidProvisionConfigs().trim().length() == 0)
            {
                exception.thrown(new IllegalPropertyArgumentException(ServiceXInfo.PREPAID_PROVISION_CONFIGS,
                        "Prepaid provisioning HLR Command cannot be empty for BlackBerry services."));
            }
            if (service.getPrepaidUnprovisionConfigs().trim().length() == 0)
            {
                exception.thrown(new IllegalPropertyArgumentException(ServiceXInfo.PREPAID_UNPROVISION_CONFIGS,
                        "Prepaid unprovisioning HLR Command cannot be empty for BlackBerry services."));
            }
            if (service.getPrepaidSuspendConfigs().trim().length() == 0)
            {
                exception.thrown(new IllegalPropertyArgumentException(ServiceXInfo.PREPAID_SUSPEND_CONFIGS,
                        "Prepaid suspending HLR Command cannot be empty for BlackBerry services."));
            }
            if (service.getPrepaidResumeConfigs().trim().length() == 0)
            {
                exception.thrown(new IllegalPropertyArgumentException(ServiceXInfo.PREPAID_RESUME_CONFIGS,
                        "Prepaid resuming HLR Command cannot be empty for BlackBerry services."));
            }
            exception.throwAll();
        }
    }    
}
