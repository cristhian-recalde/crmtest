package com.trilogy.app.crm.voicemail;

public interface VoiceMailConstants 
{

    public static final int RESULT_SUCCESS = 0; 
    public static final int RESULT_FAIL_UNKNOWN = 1; 
    public static final int RESULT_FAIL_TIMEOUT = 2; 
    public static final int RESULT_FAIL_NO_VM_SERVER_CONNECTED = 3; 
    public static final int RESULT_FAIL_SUBSCRIBER_NOT_FOUND =4; 
    public static final int RESULT_FAIL_VM_CLIENT_NOT_FOUND=5;
    public static final int RESULT_FAIL_VOID_COMMAND=6; 
    public static final int RESULT_FAIL_INVALID_VM_PLAN=7; 
    public static final int RESULT_FAIL_NOT_VM_SERVICE_FOUND_FOR_SUBSCRIBER =8; 
    
    
    public static final int ORIG_VM_RETURN_UNKNOWN = -1; 
    
    
    public static final int SOG_VM_PROVISION = 0; 
    public static final int SOG_VM_UNPROVISION = 1;
    public static final int SOG_VM_DEACTIVATE = 2;
    public static final int SOG_VM_ACTIVE = 3;
    public static final int SOG_VM_PASSWORD =4;
    public static final int SOG_VM_MSISDN_CHANGE = 5;
    

    public static final String SOG_VM_COMMAND_REPLACEMENT_PASSWORD = "%VOICE_PASSWORD%";
    public static final String SOG_VM_COMMAND_REPLACEMENT_MSISDN = "%NEW_VM_MSISDN%";

}
