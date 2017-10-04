package com.trilogy.app.crm.voicemail;

import com.trilogy.app.crm.bean.ServiceBase;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.client.RemoteServiceStatus;
import com.trilogy.app.crm.voicemail.client.ExternalProvisionResult;
import com.trilogy.framework.xhome.context.Context;

public interface VoiceMailService extends RemoteServiceStatus {
    /**
     * add a subscriber profile to VM
     * @param ctx 
     * @param sub  subscriber 
     * @param service - the service or auxiliary service which type is VM
     * @return - result code of VM provisioning, defined in VoiceMailConstants 
     */
    public ExternalProvisionResult provision(Context ctx, Subscriber sub, ServiceBase service); 
    /**
     * delete a subscriber profile from VM
     * @param ctx
     * @param Sub
     * @param service - the service or auxiliary service which type is VM
     * @return  - result code of VM provisioning, defined in VoiceMailConstants
     */
    public ExternalProvisionResult unprovision(Context ctx, Subscriber Sub, ServiceBase service); 
    /**
     * deactivate subscriber on VM
     * @param ctx
     * @param sub
     * @param service
     * @return - result code of VM provisioning, defined in VoiceMailConstants
     */
    public ExternalProvisionResult deactivate(Context ctx,Subscriber sub, ServiceBase service); 
    /**
     * activate subscriber on VM
     * @param ctx
     * @param sub
     * @param service - the service or auxiliary service which type is VM
     * @return - result code of VM provisioning, defined in VoiceMailConstants
     */
    public ExternalProvisionResult activate(Context ctx,Subscriber sub, ServiceBase service);
    /**
     * Change the msisdn of subscriber profile on VM
     * @param ctx
     * @param sub
     * @param newMsisdn - the new msisdn
     * @return - result code of VM provisioning, defined in VoiceMailConstants
     */
    public ExternalProvisionResult changeMsisdn(Context ctx, Subscriber sub, String newMsisdn); 
    /**
     * change the password of subscriber on VM
     * @param ctx
     * @param sub
     * @param password - the new password
     * @return - result code of VM provisioning, defined in VoiceMailConstants
     */
    public ExternalProvisionResult resetPassword(Context ctx, Subscriber sub,  String password); 

}
