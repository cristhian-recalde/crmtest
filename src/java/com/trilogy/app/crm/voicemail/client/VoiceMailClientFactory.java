package com.trilogy.app.crm.voicemail.client;

import com.trilogy.app.crm.bean.VoiceMailClientTypeEnum;
import com.trilogy.app.crm.bean.VoicemailServiceConfig;
import com.trilogy.app.crm.support.SystemStatusSupportHelper;
import com.trilogy.app.crm.voicemail.VoiceMailManageInterface;
import com.trilogy.app.crm.voicemail.VoiceMailService;
import com.trilogy.framework.xhome.context.Context;

public class VoiceMailClientFactory {

    
    public void install(Context ctx)
    {
        final VoicemailServiceConfig config = (VoicemailServiceConfig)ctx.get(VoicemailServiceConfig.class);
        VoiceMailService vmClient = null;
        if (config.getClientType().equals(VoiceMailClientTypeEnum.SOG))
        {
            vmClient = new SOGClient(ctx);
        } else
        {
            vmClient = new MpathixClient(ctx); 
            ctx.put(VoiceMailManageInterface.class, vmClient);
        }

        if( vmClient != null )
        {
            ctx.put(VoiceMailService.class, vmClient);
            SystemStatusSupportHelper.get(ctx).registerExternalService(ctx, vmClient);
        }
    }
    
    
}
