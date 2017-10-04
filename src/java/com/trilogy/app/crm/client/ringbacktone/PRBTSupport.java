package com.trilogy.app.crm.client.ringbacktone;

import com.trilogy.app.crm.bean.core.custom.AuxiliaryService;
import com.trilogy.app.crm.bean.externalapp.ExternalAppResultCode;
import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.extension.auxiliaryservice.PRBTAuxSvcExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.core.MultiSimAuxSvcExtension;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.app.crm.support.ExternalAppSupport;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xlog.log.LogSupport;

public class PRBTSupport
{
    static public PRBTInfo getPrbtConfig(Context context) throws RBTClientException
    {
        AuxiliaryService service = (AuxiliaryService) context.get(AuxiliaryService.class);
        if (service == null)
        {
            throw new RBTClientException("AuxiliaryService is not defined.", ExternalAppSupport.BSS_DATABASE_FAILURE_AUXILIARY_SERVICE_RETRIEVAL);
        }
        
        if (service.getType()!= AuxiliaryServiceTypeEnum.PRBT)
        {
            throw new RBTClientException("Invalid AuxiliaryService:"+service, ExternalAppSupport.BSS_DATABASE_FAILURE_AUXILIARY_SERVICE_RETRIEVAL);
        }
                
        try
        {
            long rbtId = PRBTAuxSvcExtension.DEFAULT_RBTID;
            PRBTAuxSvcExtension prbtAuxSvcExtension = ExtensionSupportHelper.get(context).getExtension(context, service, PRBTAuxSvcExtension.class);
            if (prbtAuxSvcExtension!=null)
            {
                rbtId = prbtAuxSvcExtension.getRbtId();
            }
            else 
            {
                LogSupport.minor(context, PRBTSupport.class,
                        "Unable to find required extension of type '" + PRBTAuxSvcExtension.class.getSimpleName()
                                + "' for auxiliary service " + service.getIdentifier());
            }

            PRBTConfiguration  config = HomeSupportHelper.get(context).findBean(context, 
                PRBTConfiguration.class, 
                new EQ(PRBTConfigurationXInfo.ID, new Long(rbtId)));
        
            if (config != null)
            {
               return  config.getDriver();
            }
        } catch(Exception e)
        {
            RBTClientException ex = new RBTClientException(e.getMessage(), ExternalAppSupport.UNKNOWN);
            ex.setStackTrace(e.getStackTrace()); 
            throw ex; 
        }
        
        throw new RBTClientException("AuxiliaryService is not defined.", ExternalAppSupport.BSS_DATABASE_FAILURE_AUXILIARY_SERVICE_RETRIEVAL);
    }
}
