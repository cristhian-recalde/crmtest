package com.trilogy.app.crm.voicemail.client;

public class ExternalProvisionResult {
    public ExternalProvisionResult(int crm, int orig)
    {
        this.crmVMResultCode = crm;
        this.origVMResultCode = orig; 
     }

    public int getCrmVMResultCode() {
        return crmVMResultCode;
    }
    public void setCrmVMResultCode(int crmVMResultCode) {
        this.crmVMResultCode = crmVMResultCode;
    }
    public int getOrigVMResultCode() {
        return origVMResultCode;
    }
    public void setOrigVMResultCode(int origVMResultCode) {
        this.origVMResultCode = origVMResultCode;
    } 

    
    int crmVMResultCode;
    int origVMResultCode;
    String command; 
}
