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
import com.trilogy.app.crm.extension.service.BlackberryServiceExtensionXInfo;
import com.trilogy.app.crm.support.FrameworkSupportHelper;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;


public class BlackberryExtensionSelectedValidator implements Validator
{
    public void validate(Context ctx, Object obj)
    throws IllegalStateException 
    {
        Service service = (Service) obj;

        if (service.getType().equals(ServiceTypeEnum.BLACKBERRY))
        {
            CompoundIllegalStateException exception = new CompoundIllegalStateException();
            
            if (obj instanceof ExtensionAware)
            {
                ExtensionAware parentBean = (ExtensionAware)obj; 
                
                Set<Class> extensionTypeSet = new HashSet<Class>();

                Collection<Extension> extensions = parentBean.getExtensions();
                boolean found = false;
                if (extensions != null)
                {
                    
                    for (Extension extension : extensions)
                    {
                        if (extension!=null && BlackberryServiceExtension.class.isAssignableFrom(extension.getClass()))
                        {
                            found = true;
                            break;
                        }
                    }    
                }
                
                if (!found)
                {
                    exception.thrown(new IllegalPropertyArgumentException(ServiceXInfo.SERVICE_EXTENSIONS, "BlackBerry extension MUST be selected for services of the type BlackBerry."));
                }
            }
            
            exception.throwAll();
        }
    }    
}
