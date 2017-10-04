package com.trilogy.app.crm.support;

import com.trilogy.app.crm.bean.externalapp.ExternalAppEnum;
import com.trilogy.app.crm.bean.externalapp.ExternalAppErrorCodeMsg;
import com.trilogy.app.crm.bean.externalapp.ExternalAppErrorCodeMsgHome;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

public class CRMExternalAppSupport extends DefaultExternalAppSupport
{
    public static ExternalAppSupport instance()
    {
        if (instance_ == null)
        {
            instance_ = new CRMExternalAppSupport();
        }
        return instance_;
    }


    public void addExternalAppErrorCodeMessages(Context ctx)
    {
        addVoiceExternalAppErrorCodeMessages(ctx);
        addSMSExternalAppErrorCodeMessages(ctx);
        addDataExternalAppErrorCodeMessages(ctx);
        addVoicemailExternalAppErrorCodeMessages(ctx);
        addSPGExternalAppErrorCodeMessages(ctx);
        addBlackberryExternalAppErrorCodeMessages(ctx);
        addHLRExternalAppErrorCodeMessages(ctx);
        addAlcatelExternalAppErrorCodeMessages(ctx);
        addBSSExternalAppErrorCodeMessages(ctx);
        addURCSExternalAppErrorCodeMessages(ctx);
        addRBTExternalAppErrorCodeMessages(ctx);
        addNGRCSoapExternalAppErrorCodeMessages(ctx);
        addFFExternalAppErrorCodeMessages(ctx);
        addHomeZoneExternalAppErrorCodeMessages(ctx);
    }

    private void addExternalAppErrorCodeMessageBean(Context ctx, ExternalAppEnum externalApp, int errorCode, String message)
    {
      try
      {
        final Home home = (Home) ctx.get(ExternalAppErrorCodeMsgHome.class);
        ExternalAppErrorCodeMsg record = new ExternalAppErrorCodeMsg();
        record.setExternalApp(externalApp.getIndex());
        record.setErrorCode(errorCode);
        record.setMessage(message);
        record.setLanguage("en");
        home.create(ctx, record);
        record.setLanguage(ExternalAppErrorCodeMsg.DEFAULT_LANGUAGE);
        home.create(ctx, record);
      }
      catch (HomeException e)
      {
          LogSupport.minor(ctx, this, "Unable to add external application '" + externalApp.getDescription() + "' error code " + errorCode + ": " + e.getMessage(), e);
      }
    }

    private void addSMSExternalAppErrorCodeMessages(Context ctx)
    {
        String SUBSCRIBER_NOT_FOUND = "Subscription profile not found";
        String SQL_ERROR = "SQL error occurred";
        String INTERNAL_ERROR = "Internal error occurred";
        String CORBA_COMM_FAILURE = "Communication error with URCS";
        String RECORD_NOT_FOUND = "Record not found";
        String SERVICE_NOT_AVAILABLE = "URCS down or not available";
        String REMOTE_EXCEPTION = "Remote exception on URCS";

        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.SMS, com.redknee.app.crm.client.smsb.AppSmsbClient.RECORD_NOT_FOUND,RECORD_NOT_FOUND);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.SMS, com.redknee.app.crm.client.smsb.AppSmsbClient.SUBSCRIBER_NOT_FOUND, SUBSCRIBER_NOT_FOUND);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.SMS, com.redknee.app.crm.client.smsb.AppSmsbClient.SQL_ERROR, SQL_ERROR);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.SMS, com.redknee.app.crm.client.smsb.AppSmsbClient.INTERNAL_ERROR, INTERNAL_ERROR);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.SMS, ExternalAppSupport.COMMUNICATION_FAILURE, CORBA_COMM_FAILURE);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.SMS, ExternalAppSupport.NO_CONNECTION, SERVICE_NOT_AVAILABLE);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.SMS, ExternalAppSupport.REMOTE_EXCEPTION, REMOTE_EXCEPTION);
    }

    private void addBlackberryExternalAppErrorCodeMessages(Context ctx)
    {

        String SERVICE_ALREADY_ACTIVE = "Service is already active on the RIM Provisioning System";
        String SERVICE_DEACTIVATED = "Service is inactive on the RIM Provisioning System";
        String OLD_BILLING_NOT_FOUND = "Old billing identifier was not found";
        String NEW_BILLING_DEACTIVATED = "New billing identifier is deactivated on the RIM Provisioning System";
        String NEW_BILLING_SUSPENDED = "New billing identifier is suspended on the RIM Provisioning System";
        String INVALID_IMSI = "The provided IMSI is invalid on the RIM Provisioning System";
        String INVALID_FORMAT_OR_LENGTH = "Provided IMSI or MSISDN has an invalid format or length";
        String COMMUNICATION_FAILURE = "Communication error with the RIM Provisioning System";
        String SERVICE_NOT_AVAILABLE = "RIM Provisioning System is down or not available";

        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.BLACKBERRY, com.redknee.app.crm.blackberry.error.RIMBlackBerryErrorCodes.SERVICE_ALREADY_ACTIVE, SERVICE_ALREADY_ACTIVE);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.BLACKBERRY, com.redknee.app.crm.blackberry.error.RIMBlackBerryErrorCodes.SERVICE_DEACTIVATED, SERVICE_DEACTIVATED);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.BLACKBERRY, com.redknee.app.crm.blackberry.error.RIMBlackBerryErrorCodes.OLD_BILLING_NOT_FOUND, OLD_BILLING_NOT_FOUND);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.BLACKBERRY, com.redknee.app.crm.blackberry.error.RIMBlackBerryErrorCodes.NEW_BILLING_DEACTIVATED, NEW_BILLING_DEACTIVATED);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.BLACKBERRY, com.redknee.app.crm.blackberry.error.RIMBlackBerryErrorCodes.NEW_BILLING_SUSPENDED, NEW_BILLING_SUSPENDED);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.BLACKBERRY, com.redknee.app.crm.blackberry.error.RIMBlackBerryErrorCodes.INVALID_IMSI, INVALID_IMSI);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.BLACKBERRY, com.redknee.app.crm.blackberry.error.RIMBlackBerryErrorCodes.INVALID_FORMAT_OR_LENGTH, INVALID_FORMAT_OR_LENGTH);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.BLACKBERRY, ExternalAppSupport.COMMUNICATION_FAILURE, COMMUNICATION_FAILURE);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.BLACKBERRY, ExternalAppSupport.NO_CONNECTION, SERVICE_NOT_AVAILABLE);
    }

    private void addSPGExternalAppErrorCodeMessages(Context ctx)
    {
        String ENTRY_NOT_FOUND = "Entry not found";
        String SQL_ERROR = "SQL error occurred";
        String INTERNAL_ERROR = "Internal error occurred";
        String INVALID_DATA = "Invalid data informed";
        String INVALID_SPID = "Invalid spid informed";

        String SERVICE_COMMAND_UNSUPPORTED = "Service command is not supported";
        String SERVICE_DRIVER_INVALID = "Service driver is invalid";
        String SERVICE_NOT_FOUND = "Service was not found";
        String SERVICE_PARAMETER_MISSING = "Service parameter is missing";
        String SERVICE_REMOVE_ADD_UNSUPPORTED = "Service operation (remove or add) is not supported";
        String SERVICES_ERRORS = "Services errors";

        String CORBA_COMM_FAILURE = "Communication error with Service Provisioning Gateway";
        String SERVICE_NOT_AVAILABLE = "Service Provisioning Gateway down or not available";
        String REMOTE_EXCEPTION = "Remote exception on NGRC";


        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.SPG, com.redknee.app.crm.provision.service.ErrorCode.ENTRY_NOT_FOUND, ENTRY_NOT_FOUND);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.SPG, com.redknee.app.crm.provision.service.ErrorCode.INTERNAL_ERROR, INTERNAL_ERROR);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.SPG, com.redknee.app.crm.provision.service.ErrorCode.INVALID_DATA, INVALID_DATA);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.SPG, com.redknee.app.crm.provision.service.ErrorCode.INVALID_SPID, INVALID_SPID);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.SPG, com.redknee.app.crm.provision.service.ErrorCode.SQL_ERROR, SQL_ERROR);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.SPG, com.redknee.app.crm.provision.service.ErrorCode.SERVICE_COMMAND_UNSUPPORTED, SERVICE_COMMAND_UNSUPPORTED);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.SPG, com.redknee.app.crm.provision.service.ErrorCode.SERVICE_DRIVER_INVALID, SERVICE_DRIVER_INVALID);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.SPG, com.redknee.app.crm.provision.service.ErrorCode.SERVICE_NOT_FOUND, SERVICE_NOT_FOUND);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.SPG, com.redknee.app.crm.provision.service.ErrorCode.SERVICE_PARAMETER_MISSING, SERVICE_PARAMETER_MISSING);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.SPG, com.redknee.app.crm.provision.service.ErrorCode.SERVICE_REMOVE_ADD_UNSUPPORTED, SERVICE_REMOVE_ADD_UNSUPPORTED);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.SPG, com.redknee.app.crm.provision.service.ErrorCode.SERVICES_ERRORS, SERVICES_ERRORS);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.SPG, ExternalAppSupport.NO_CONNECTION, SERVICE_NOT_AVAILABLE);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.SPG, ExternalAppSupport.COMMUNICATION_FAILURE, CORBA_COMM_FAILURE);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.SPG, ExternalAppSupport.REMOTE_EXCEPTION, REMOTE_EXCEPTION);

    }

    private void addVoicemailExternalAppErrorCodeMessages(Context ctx)
    {
        String SUBSCRIBER_NOT_FOUND = "Subscription profile not found";
        String TIMEOUT = "Timeout while communicating to Voicemail server";
        String NO_VM_SERVER_CONNECTED = "No Voicemail server connected";
        String VM_CLIENT_NOT_FOUND = "Voicemail client not found";
        String VOID_COMMAND = "Empty command configured";
        String NOT_VM_SERVICE_FOUND_FOR_SUBSCRIBER = "Voicemail service not found for subscription";
        String INVALID_VM_PLAN = "Invalid voicemail plan";

        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.VOICEMAIL, com.redknee.app.crm.voicemail.VoiceMailConstants.RESULT_FAIL_TIMEOUT, TIMEOUT);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.VOICEMAIL, com.redknee.app.crm.voicemail.VoiceMailConstants.RESULT_FAIL_NO_VM_SERVER_CONNECTED, NO_VM_SERVER_CONNECTED);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.VOICEMAIL, com.redknee.app.crm.voicemail.VoiceMailConstants.RESULT_FAIL_SUBSCRIBER_NOT_FOUND, SUBSCRIBER_NOT_FOUND);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.VOICEMAIL, com.redknee.app.crm.voicemail.VoiceMailConstants.RESULT_FAIL_VM_CLIENT_NOT_FOUND, VM_CLIENT_NOT_FOUND);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.VOICEMAIL, com.redknee.app.crm.voicemail.VoiceMailConstants.RESULT_FAIL_VOID_COMMAND, VOID_COMMAND);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.VOICEMAIL, com.redknee.app.crm.voicemail.VoiceMailConstants.RESULT_FAIL_INVALID_VM_PLAN, INVALID_VM_PLAN);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.VOICEMAIL, com.redknee.app.crm.voicemail.VoiceMailConstants.RESULT_FAIL_NOT_VM_SERVICE_FOUND_FOR_SUBSCRIBER, NOT_VM_SERVICE_FOUND_FOR_SUBSCRIBER);

    }

    private void addVoiceExternalAppErrorCodeMessages(Context ctx)
    {
        String SUBSCRIBER_NOT_FOUND = "Subscription profile not found";
        String SQL_ERROR = "SQL error occurred";
        String INTERNAL_ERROR = "Internal error occurred";
        String INVALID_PARAMETER = "Invalid parameter informed";

        String ALL_FF_UPDATE_FAILED = "Friends And Family update failed";
        String AMSISDN_EDIT_NOT_PERMITTED = "Additional MSISDN edit not allowed";
        String AMSISDN_LIMIT_EXCEEDED = "Additional MSISDN limit exceeded for subscription";
        String MAIN_SUBSCRIBER_NOT_FOUND = "Subscription profile not found";
        String MANDATORY_INFO_MISSING = "Mandatory info missing";
        String SOME_FF_UPDATE_FAILED = "Friends And Family update failed";
        String SUBSCRIBER_INFO_ALREADY_EXIST = "Additional MSISDN already exists";
        String UPDATE_NOT_ALLOWED = "Update not allowed";
        String CORBA_COMM_FAILURE = "Communication error with URCS";
        String SERVICE_NOT_AVAILABLE = "URCS down or not available";
        String REMOTE_EXCEPTION = "Remote exception on URCS";

        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.VOICE, com.redknee.app.osa.ecp.provision.ErrorCode.ALL_FF_UPDATE_FAILED, ALL_FF_UPDATE_FAILED);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.VOICE, com.redknee.app.osa.ecp.provision.ErrorCode.AMSISDN_EDIT_NOT_PERMITTED, AMSISDN_EDIT_NOT_PERMITTED);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.VOICE, com.redknee.app.osa.ecp.provision.ErrorCode.AMSISDN_LIMIT_EXCEEDED, AMSISDN_LIMIT_EXCEEDED);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.VOICE, com.redknee.app.osa.ecp.provision.ErrorCode.INTERNAL_ERROR, INTERNAL_ERROR);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.VOICE, com.redknee.app.osa.ecp.provision.ErrorCode.INVALID_PARAMETER, INVALID_PARAMETER);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.VOICE, com.redknee.app.osa.ecp.provision.ErrorCode.MAIN_SUBSCRIBER_NOT_FOUND, MAIN_SUBSCRIBER_NOT_FOUND);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.VOICE, com.redknee.app.osa.ecp.provision.ErrorCode.MANDATORY_INFO_MISSING, MANDATORY_INFO_MISSING);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.VOICE, com.redknee.app.osa.ecp.provision.ErrorCode.SOME_FF_UPDATE_FAILED, SOME_FF_UPDATE_FAILED);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.VOICE, com.redknee.app.osa.ecp.provision.ErrorCode.SQL_ERROR, SQL_ERROR);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.VOICE, com.redknee.app.osa.ecp.provision.ErrorCode.SUBSCRIBER_INFO_ALREADY_EXIST, SUBSCRIBER_INFO_ALREADY_EXIST);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.VOICE, com.redknee.app.osa.ecp.provision.ErrorCode.SUBSCRIBER_NOT_FOUND, SUBSCRIBER_NOT_FOUND);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.VOICE, com.redknee.app.osa.ecp.provision.ErrorCode.UPDATE_NOT_ALLOWED, UPDATE_NOT_ALLOWED);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.VOICE, ExternalAppSupport.COMMUNICATION_FAILURE, CORBA_COMM_FAILURE);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.VOICE, ExternalAppSupport.NO_CONNECTION, SERVICE_NOT_AVAILABLE);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.VOICE, ExternalAppSupport.REMOTE_EXCEPTION, REMOTE_EXCEPTION);

    }

    private void addHLRExternalAppErrorCodeMessages(Context ctx)
    {
        String CODE_MAPPING_NOT_FOUND = "Code mapping not found";
        String CONGESTION = "Congestion";
        String CONNECTION_FAIL = "Connection failed";
        String DISABLED = "HLR is disabled";
        String HANDLE_NOT_HANDLED = "Handle cannot be handled";
        String HLR_LOGIN_FAIL = "Login failed";
        String HLR_OUT_OF_SYNC = "HLR out of sync";
        String INTERNAL_ERROR = "Internal error occurred";
        String INVALID_HLRID = "Invalid HLR identifier";
        String INVALID_RESPONSE = "Invalid response";
        String IOEXCEPTION = "IO Exception";
        String MAX_RETRY = "Maximum number of retries exceeded";
        String SOCKET_OPEN_FAILURE = "Unable to open socket";
        String TIMEOUT_EXECUTING = "Timeout executing operation";
        String TIMEOUT_IN_QUEUE = "Timeout waiting in queue";
        String TIMEOUT_INQUEUE = "Timeout waiting in queue";
        String UNKNOWN = "Unknown failure";
        String UNRECOVERABLE_END = "Unrecoverable end";
        String UNRECOVERABLE_START = "Unrecoverable start";

        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.HLR, com.redknee.interfaces.crm.hlr.InterfaceCrmHlrConstants.HLR_FAILURE_CODE_MAPPING_NOT_FOUND, CODE_MAPPING_NOT_FOUND);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.HLR, com.redknee.interfaces.crm.hlr.InterfaceCrmHlrConstants.HLR_FAILURE_CONGESTION, CONGESTION);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.HLR, com.redknee.interfaces.crm.hlr.InterfaceCrmHlrConstants.HLR_FAILURE_CONNECTION_FAIL, CONNECTION_FAIL);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.HLR, com.redknee.interfaces.crm.hlr.InterfaceCrmHlrConstants.HLR_FAILURE_DISABLED, DISABLED);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.HLR, com.redknee.interfaces.crm.hlr.InterfaceCrmHlrConstants.HLR_FAILURE_HANDLE_NOT_HANDLED, HANDLE_NOT_HANDLED);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.HLR, com.redknee.interfaces.crm.hlr.InterfaceCrmHlrConstants.HLR_FAILURE_HLR_LOGIN_FAIL, HLR_LOGIN_FAIL);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.HLR, com.redknee.interfaces.crm.hlr.InterfaceCrmHlrConstants.HLR_FAILURE_HLR_OUT_OF_SYNC, HLR_OUT_OF_SYNC);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.HLR, com.redknee.interfaces.crm.hlr.InterfaceCrmHlrConstants.HLR_FAILURE_INTERNAL, INTERNAL_ERROR);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.HLR, com.redknee.interfaces.crm.hlr.InterfaceCrmHlrConstants.HLR_FAILURE_INVALID_HLRID, INVALID_HLRID);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.HLR, com.redknee.interfaces.crm.hlr.InterfaceCrmHlrConstants.HLR_FAILURE_INVALID_RESPONSE, INVALID_RESPONSE);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.HLR, com.redknee.interfaces.crm.hlr.InterfaceCrmHlrConstants.HLR_FAILURE_IOEXCEPTION, IOEXCEPTION);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.HLR, com.redknee.interfaces.crm.hlr.InterfaceCrmHlrConstants.HLR_FAILURE_MAX_RETRY, MAX_RETRY);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.HLR, com.redknee.interfaces.crm.hlr.InterfaceCrmHlrConstants.HLR_FAILURE_SOCKET_OPEN_FAILURE, SOCKET_OPEN_FAILURE);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.HLR, com.redknee.interfaces.crm.hlr.InterfaceCrmHlrConstants.HLR_FAILURE_TIMEOUT_EXECUTING, TIMEOUT_EXECUTING);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.HLR, com.redknee.interfaces.crm.hlr.InterfaceCrmHlrConstants.HLR_FAILURE_TIMEOUT_IN_QUEUE, TIMEOUT_IN_QUEUE);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.HLR, com.redknee.interfaces.crm.hlr.InterfaceCrmHlrConstants.HLR_FAILURE_TIMEOUT_INQUEUE, TIMEOUT_INQUEUE);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.HLR, com.redknee.interfaces.crm.hlr.InterfaceCrmHlrConstants.HLR_FAILURE_UNKNOWN, UNKNOWN);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.HLR, com.redknee.interfaces.crm.hlr.InterfaceCrmHlrConstants.HLR_FAILURE_UNRECOVERABLE_END, UNRECOVERABLE_END);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.HLR, com.redknee.interfaces.crm.hlr.InterfaceCrmHlrConstants.HLR_FAILURE_UNRECOVERABLE_START, UNRECOVERABLE_START);

    }

    private void addBSSExternalAppErrorCodeMessages(Context ctx)
    {
        String BSS_DATABASE_FAILURE_SUBSCRIPTION_RETRIEVAL = "Unable to retrieve subscription";

        String BSS_DATABASE_FAILURE_ACCOUNT_RETRIEVAL = "Unable to retrieve or update account";
        String BSS_DATABASE_FAILURE_ACCOUNT_UPDATE = "Unable to create or update account";

        String BSS_DATABASE_FAILURE_MSISDN_RETRIEVAL = "Unable to retrieve mobile number";

        String BSS_DATABASE_FAILURE_PACKAGE_RETRIEVAL = "Unable to retrieve package";
        String BSS_DATABASE_FAILURE_PACKAGE_UPDATE_IN_USE = "Unable to update package state to IN USE";
        String BSS_DATABASE_FAILURE_PACKAGE_UPDATE_HELD = "Unable to update package state to HELD";
        String BSS_DATABASE_FAILURE_PACKAGE_NOT_IN_USE_VALIDATION = "Unable to validate that package is not in use";
        String BSS_DATABASE_FAILURE_IMSI_HISTORY_UPDATE = "Unable to update Imsi History for package";

        String BSS_DATABASE_FAILURE_AUXILIARY_SERVICE_RETRIEVAL = "Unable to retrieve or update auxiliary service";
        String BSS_DATABASE_FAILURE_AUXILIARY_SERVICE_CREATION = "Unable to create auxiliary service";
        String BSS_DATABASE_FAILURE_AUXILIARY_SERVICE_UPDATE = "Unable to update auxiliary service";
        String BSS_DATABASE_FAILURE_AUXILIARY_SERVICE_REMOVAL = "Unable to remove auxiliary service";

        String BSS_DATABASE_FAILURE_SUBSCRIPTION_HOMEZONE_RETRIEVAL = "Unable to retrieve HomeZone for subscription";
        String BSS_DATABASE_FAILURE_SUBSCRIPTION_HOMEZONE_CREATION = "Unable to create HomeZone for subscription";
        String BSS_DATABASE_FAILURE_SUBSCRIPTION_HOMEZONE_UPDATE = "Unable to update HomeZone for subscription";
        String BSS_DATABASE_FAILURE_SUBSCRIPTION_HOMEZONE_REMOVAL = "Unable to remove HomeZone from subscription";

        String BSS_DATABASE_FAILURE_SUBSCRIPTION_HOMEZONE_COUNT_RETRIEVAL = "Unable to retrieve HomeZone count for subscription";
        String BSS_DATABASE_FAILURE_SUBSCRIPTION_HOMEZONE_COUNT_UPDATE = "Unable to update HomeZone count for subscription";

        String BSS_DATABASE_FAILURE_EXTENSION_REMOVAL = "Unable to remove related extension";

        String BSS_DATABASE_FAILURE_MSISDN_CLAIM = "Unable to claim mobile number for subscriber account";
        String BSS_DATABASE_FAILURE_MSISDN_RELEASE = "Unable to release mobile number from subscriber account";
        String BSS_DATABASE_FAILURE_MSISDN_ASSOCIATION = "Unable to associate mobile number with subscription";
        String BSS_DATABASE_FAILURE_MSISDN_DEASSOCIATION = "Unable to deassociate mobile number from subscription";
        String BSS_DATABASE_FAILURE_MSISDN_UPDATE = "Unable to update mobile number";
        String BSS_DATABASE_FAILURE_MSISDN_HISTORY_UPDATE = "Unable to update mobile number history";
        String BSS_DATABASE_FAILURE_CUSTOMER_SWAP_RETRIEVAL = "Unable to retrieve customer swap event";
        String BSS_DATABASE_FAILURE_FEATURE_MODIFICATION_RETRIEVAL = "Unable to retrieve feature modification event";

        String UNKNOWN = "Failure retrieving required information";

        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.BSS, ExternalAppSupport.BSS_DATABASE_FAILURE_SUBSCRIPTION_RETRIEVAL, BSS_DATABASE_FAILURE_SUBSCRIPTION_RETRIEVAL);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.BSS, ExternalAppSupport.BSS_DATABASE_FAILURE_ACCOUNT_RETRIEVAL, BSS_DATABASE_FAILURE_ACCOUNT_RETRIEVAL);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.BSS, ExternalAppSupport.BSS_DATABASE_FAILURE_ACCOUNT_UPDATE, BSS_DATABASE_FAILURE_ACCOUNT_UPDATE);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.BSS, ExternalAppSupport.BSS_DATABASE_FAILURE_MSISDN_RETRIEVAL, BSS_DATABASE_FAILURE_MSISDN_RETRIEVAL);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.BSS, ExternalAppSupport.BSS_DATABASE_FAILURE_PACKAGE_RETRIEVAL, BSS_DATABASE_FAILURE_PACKAGE_RETRIEVAL);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.BSS, ExternalAppSupport.BSS_DATABASE_FAILURE_PACKAGE_UPDATE_IN_USE, BSS_DATABASE_FAILURE_PACKAGE_UPDATE_IN_USE);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.BSS, ExternalAppSupport.BSS_DATABASE_FAILURE_PACKAGE_UPDATE_HELD, BSS_DATABASE_FAILURE_PACKAGE_UPDATE_HELD);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.BSS, ExternalAppSupport.BSS_DATABASE_FAILURE_PACKAGE_NOT_IN_USE_VALIDATION, BSS_DATABASE_FAILURE_PACKAGE_NOT_IN_USE_VALIDATION);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.BSS, ExternalAppSupport.BSS_DATABASE_FAILURE_IMSI_HISTORY_UPDATE, BSS_DATABASE_FAILURE_IMSI_HISTORY_UPDATE);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.BSS, ExternalAppSupport.BSS_DATABASE_FAILURE_AUXILIARY_SERVICE_RETRIEVAL, BSS_DATABASE_FAILURE_AUXILIARY_SERVICE_RETRIEVAL);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.BSS, ExternalAppSupport.BSS_DATABASE_FAILURE_AUXILIARY_SERVICE_CREATION, BSS_DATABASE_FAILURE_AUXILIARY_SERVICE_CREATION);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.BSS, ExternalAppSupport.BSS_DATABASE_FAILURE_AUXILIARY_SERVICE_UPDATE, BSS_DATABASE_FAILURE_AUXILIARY_SERVICE_UPDATE);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.BSS, ExternalAppSupport.BSS_DATABASE_FAILURE_AUXILIARY_SERVICE_REMOVAL, BSS_DATABASE_FAILURE_AUXILIARY_SERVICE_REMOVAL);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.BSS, ExternalAppSupport.BSS_DATABASE_FAILURE_SUBSCRIPTION_HOMEZONE_RETRIEVAL, BSS_DATABASE_FAILURE_SUBSCRIPTION_HOMEZONE_RETRIEVAL);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.BSS, ExternalAppSupport.BSS_DATABASE_FAILURE_SUBSCRIPTION_HOMEZONE_CREATION, BSS_DATABASE_FAILURE_SUBSCRIPTION_HOMEZONE_CREATION);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.BSS, ExternalAppSupport.BSS_DATABASE_FAILURE_SUBSCRIPTION_HOMEZONE_UPDATE, BSS_DATABASE_FAILURE_SUBSCRIPTION_HOMEZONE_UPDATE);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.BSS, ExternalAppSupport.BSS_DATABASE_FAILURE_SUBSCRIPTION_HOMEZONE_REMOVAL, BSS_DATABASE_FAILURE_SUBSCRIPTION_HOMEZONE_REMOVAL);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.BSS, ExternalAppSupport.BSS_DATABASE_FAILURE_SUBSCRIPTION_HOMEZONE_COUNT_RETRIEVAL, BSS_DATABASE_FAILURE_SUBSCRIPTION_HOMEZONE_COUNT_RETRIEVAL);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.BSS, ExternalAppSupport.BSS_DATABASE_FAILURE_SUBSCRIPTION_HOMEZONE_COUNT_UPDATE, BSS_DATABASE_FAILURE_SUBSCRIPTION_HOMEZONE_COUNT_UPDATE);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.BSS, ExternalAppSupport.BSS_DATABASE_FAILURE_EXTENSION_REMOVAL, BSS_DATABASE_FAILURE_EXTENSION_REMOVAL);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.BSS, ExternalAppSupport.BSS_DATABASE_FAILURE_MSISDN_CLAIM, BSS_DATABASE_FAILURE_MSISDN_CLAIM);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.BSS, ExternalAppSupport.BSS_DATABASE_FAILURE_MSISDN_RELEASE, BSS_DATABASE_FAILURE_MSISDN_RELEASE);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.BSS, ExternalAppSupport.BSS_DATABASE_FAILURE_MSISDN_ASSOCIATION, BSS_DATABASE_FAILURE_MSISDN_ASSOCIATION);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.BSS, ExternalAppSupport.BSS_DATABASE_FAILURE_MSISDN_DEASSOCIATION, BSS_DATABASE_FAILURE_MSISDN_DEASSOCIATION);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.BSS, ExternalAppSupport.BSS_DATABASE_FAILURE_MSISDN_UPDATE, BSS_DATABASE_FAILURE_MSISDN_UPDATE);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.BSS, ExternalAppSupport.BSS_DATABASE_FAILURE_MSISDN_HISTORY_UPDATE, BSS_DATABASE_FAILURE_MSISDN_HISTORY_UPDATE);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.BSS, ExternalAppSupport.BSS_DATABASE_FAILURE_CUSTOMER_SWAP_RETRIEVAL, BSS_DATABASE_FAILURE_CUSTOMER_SWAP_RETRIEVAL);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.BSS, ExternalAppSupport.BSS_DATABASE_FAILURE_FEATURE_MODIFICATION_RETRIEVAL, BSS_DATABASE_FAILURE_FEATURE_MODIFICATION_RETRIEVAL);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.BSS, ExternalAppSupport.UNKNOWN, UNKNOWN);




    }                             

    private void addNGRCSoapExternalAppErrorCodeMessages(Context ctx)
    {
        String SERVICE_NOT_AVAILABLE = "NGRC Soap down or not available";
        String REMOTE_EXCEPTION = "Remote exception on NGRC Soap";
        String UNKNOWN = "Unknown result code on NGRC Soap";
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.DATA_OPTIN, ExternalAppSupport.NO_CONNECTION, SERVICE_NOT_AVAILABLE);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.DATA_OPTIN, ExternalAppSupport.REMOTE_EXCEPTION, REMOTE_EXCEPTION);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.DATA_OPTIN, ExternalAppSupport.UNKNOWN, UNKNOWN);
    }

    private void addURCSExternalAppErrorCodeMessages(Context ctx)
    {
        String SERVICE_NOT_AVAILABLE = "URCS down or not available";
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.URCS, ExternalAppSupport.NO_CONNECTION, SERVICE_NOT_AVAILABLE);
    }

    private void addRBTExternalAppErrorCodeMessages(Context ctx)
    {
        String SERVICE_NOT_AVAILABLE = "Ring Back Tone Server down or not available";
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.RBT, ExternalAppSupport.NO_CONNECTION, SERVICE_NOT_AVAILABLE);
    }

    private void addDataExternalAppErrorCodeMessages(Context ctx)
    {
        String CUSTOMER_SPECIFIC_BASE = "Base is customer specific";
        String DB_DISABLED = "Database is disabled";
        String INTERNAL_ERROR = "Internal error occurred";
        String DB_NOT_AVAILABLE = "Database not available";
        String EXCEED_MAX_PACKAGE_PLAN = "Maximum package plan exceeded";
        String INVALID_DATA_RECEIVED = "Invalid data informed";
        String MSISDN_ALREADY_EXISTS = "Profile with MSISDN already exists";
        String MSISDN_NOT_EXISTS = "Profile with MSISDN does not exist";
        String PACKAGE_PLAN_ALREADY_EXISTS = "Package plan already exists";
        String PACKAGE_PLAN_NOT_FOUND = "Package plan not found";
        String SERVICE_NOT_AVAILABLE = "NGRC down or not available";
        String REMOTE_EXCEPTION = "Remote exception on NGRC";
        String CORBA_COMM_FAILURE = "Communication error with NGRC";


        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.DATA, ExternalAppSupport.NO_CONNECTION, SERVICE_NOT_AVAILABLE);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.DATA, ExternalAppSupport.COMMUNICATION_FAILURE, CORBA_COMM_FAILURE);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.DATA, ExternalAppSupport.REMOTE_EXCEPTION, REMOTE_EXCEPTION);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.DATA, com.redknee.product.s5600.ipcg.provisioning.ResponseCode.CUSTOMER_SPECIFIC_BASE, CUSTOMER_SPECIFIC_BASE);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.DATA, com.redknee.product.s5600.ipcg.provisioning.ResponseCode.DB_DISABLED, DB_DISABLED);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.DATA, com.redknee.product.s5600.ipcg.provisioning.ResponseCode.DB_NOT_AVAILABLE, DB_NOT_AVAILABLE);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.DATA, com.redknee.product.s5600.ipcg.provisioning.ResponseCode.EXCEED_MAX_PACKAGE_PLAN, EXCEED_MAX_PACKAGE_PLAN);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.DATA, com.redknee.product.s5600.ipcg.provisioning.ResponseCode.INTERNAL_ERROR, INTERNAL_ERROR);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.DATA, com.redknee.product.s5600.ipcg.provisioning.ResponseCode.INVALID_DATA_RECEIVED, INVALID_DATA_RECEIVED);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.DATA, com.redknee.product.s5600.ipcg.provisioning.ResponseCode.MSISDN_ALREADY_EXISTS, MSISDN_ALREADY_EXISTS);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.DATA, com.redknee.product.s5600.ipcg.provisioning.ResponseCode.MSISDN_NOT_EXISTS, MSISDN_NOT_EXISTS);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.DATA, com.redknee.product.s5600.ipcg.provisioning.ResponseCode.PACKAGE_PLAN_ALREADY_EXISTS, PACKAGE_PLAN_ALREADY_EXISTS);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.DATA, com.redknee.product.s5600.ipcg.provisioning.ResponseCode.PACKAGE_PLAN_NOT_FOUND, PACKAGE_PLAN_NOT_FOUND);
    }

    private void addAlcatelExternalAppErrorCodeMessages(Context ctx)
    {
        String SERVICE_NOT_AVAILABLE = "Alcatel SSC down or not available";
        String ERROR_TRANSLATOR_NOT_FOUND = "Translator not installed";
        String ERROR_DATABASE_ERROR = "Database error occurred";
        String ERROR_UNKNOWN_REQUEST = "Unknown error occurred while creating request";
        String ERROR_UNKNOWN_RESPONSE = "Unknown error occurred while processing response";
        String ERROR_UNREADABLE_DATA = "Unreadable data received from Alcatel SSC";
        String ERROR_INVALID_RESPONSE_CODE = "Invalid response code received";
        String ERROR_INVALID_RESPONSE_OBJECT = "Invalid response received";
        String ERROR_HLR = "Error while communicating with Alcatel SSC HLR";


        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.ALCATEL_SSC, ExternalAppSupport.NO_CONNECTION, SERVICE_NOT_AVAILABLE);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.ALCATEL_SSC, com.redknee.app.crm.client.alcatel.AlcatelProvisioning.ERROR_TRANSLATOR_NOT_FOUND, ERROR_TRANSLATOR_NOT_FOUND);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.ALCATEL_SSC, com.redknee.app.crm.client.alcatel.AlcatelProvisioning.ERROR_DATABASE_ERROR, ERROR_DATABASE_ERROR);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.ALCATEL_SSC, com.redknee.app.crm.client.alcatel.AlcatelProvisioning.ERROR_UNKNOWN_REQUEST, ERROR_UNKNOWN_REQUEST);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.ALCATEL_SSC, com.redknee.app.crm.client.alcatel.AlcatelProvisioning.ERROR_UNKNOWN_RESPONSE, ERROR_UNKNOWN_RESPONSE);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.ALCATEL_SSC, com.redknee.app.crm.client.alcatel.AlcatelProvisioning.ERROR_UNREADABLE_DATA, ERROR_UNREADABLE_DATA);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.ALCATEL_SSC, com.redknee.app.crm.client.alcatel.AlcatelProvisioning.ERROR_INVALID_RESPONSE_CODE, ERROR_INVALID_RESPONSE_CODE);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.ALCATEL_SSC, com.redknee.app.crm.client.alcatel.AlcatelProvisioning.ERROR_INVALID_RESPONSE_OBJECT, ERROR_INVALID_RESPONSE_OBJECT);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.ALCATEL_SSC, com.redknee.app.crm.client.alcatel.AlcatelProvisioning.ERROR_HLR, ERROR_HLR);
    }


    private void addFFExternalAppErrorCodeMessages(Context ctx)
    {
        String SERVICE_NOT_AVAILABLE = "Friends And Family down or not available";
        String REMOTE_EXCEPTION = "Remote exception on Friends And Family";
        String CORBA_COMM_FAILURE = "Communication error with Friends And Family";
        String UNKNOWN = "Unknown result code on Friends And Family";
        String FF_ECARE_PROFILE_NOT_FOUND = "Profile was not found";
        String FF_ECARE_INVALID_INPUT = "Invalid input";
        String FF_ECARE_UNABLE_TO_DELETE_PLP_SUBSCRIBERS_EXISTING = "Unable to delete Personal List Plan for there are subscriptions associated with it";
        String FF_ECARE_UNABLE_TO_UPDATE_PLP_DOWNGRADE_NOT_SUPPORTED = "Downgrade of Personal List Plan not supported";
        String FF_ECARE_INVALID_DATE_RANGE = "Invalid date range provided";
        String FF_ECARE_BIRTHDAY_PLAN_NOT_FOUND = "Birthday Plan not found";
        String FF_ECARE_UNABLE_TO_DELETE_BIRTHDAY_SUBSCRIBERS_EXISTING = "Unable to delete Birthday Plan  for there are subscriptions associated with it";
        String FF_ECARE_PLP_NOT_FOUND = "Personal List Plan not found";
        String FF_ECARE_INTERNAL_FAILURE = "Internal error";
        String FF_ECARE_CUG_TEMPLATE_NOT_FOUND = "Closed User Group template not found";
        String FF_ECARE_CUG_INSTANCE_NOT_FOUND = "Closed User Group instance not found";
        String FF_ECARE_TEMPLATE_DEPENDANCY = "Template dependancy";
        String FF_ECARE_USER_LIMIT_EXCEEDED = "User limit exceeded";
        String FF_ECARE_CUG_TEMPLATE_NOT_AVAILABLE = "Closed User Group Template is not available";
        String FF_ECARE_CUG_DUPLICATE_SHORTCODE = "Duplicated shortcode in Closed User Group";
        String FF_ECARE_PLP_MEMBER_ADD_EXCEEDED = "Maximum number of members in Personal List Plan reached";
        String FF_ECARE_DISALLOWED_PLP_MEMBER_REMOVAL = "Personal List Plan member removal not allowed";
        String FF_ECARE_MSISDN_NOT_FOUND = "Mobile Number was not found";
        String FF_ECARE_SHORTCODE_ALREADY_EXISTS = "Shortcode already exists";

        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.FF, ExternalAppSupport.NO_CONNECTION, SERVICE_NOT_AVAILABLE);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.FF, ExternalAppSupport.COMMUNICATION_FAILURE, CORBA_COMM_FAILURE);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.FF, ExternalAppSupport.REMOTE_EXCEPTION, REMOTE_EXCEPTION);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.FF, ExternalAppSupport.UNKNOWN, UNKNOWN);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.FF, com.redknee.app.ff.ecare.rmi.FFECareRmiConstants.FF_ECARE_PROFILE_NOT_FOUND, FF_ECARE_PROFILE_NOT_FOUND);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.FF, com.redknee.app.ff.ecare.rmi.FFECareRmiConstants.FF_ECARE_INVALID_INPUT, FF_ECARE_INVALID_INPUT);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.FF, com.redknee.app.ff.ecare.rmi.FFECareRmiConstants.FF_ECARE_UNABLE_TO_DELETE_PLP_SUBSCRIBERS_EXISTING, FF_ECARE_UNABLE_TO_DELETE_PLP_SUBSCRIBERS_EXISTING);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.FF, com.redknee.app.ff.ecare.rmi.FFECareRmiConstants.FF_ECARE_UNABLE_TO_UPDATE_PLP_DOWNGRADE_NOT_SUPPORTED, FF_ECARE_UNABLE_TO_UPDATE_PLP_DOWNGRADE_NOT_SUPPORTED);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.FF, com.redknee.app.ff.ecare.rmi.FFECareRmiConstants.FF_ECARE_INVALID_DATE_RANGE, FF_ECARE_INVALID_DATE_RANGE);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.FF, com.redknee.app.ff.ecare.rmi.FFECareRmiConstants.FF_ECARE_BIRTHDAY_PLAN_NOT_FOUND, FF_ECARE_BIRTHDAY_PLAN_NOT_FOUND);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.FF, com.redknee.app.ff.ecare.rmi.FFECareRmiConstants.FF_ECARE_UNABLE_TO_DELETE_BIRTHDAY_SUBSCRIBERS_EXISTING, FF_ECARE_UNABLE_TO_DELETE_BIRTHDAY_SUBSCRIBERS_EXISTING);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.FF, com.redknee.app.ff.ecare.rmi.FFECareRmiConstants.FF_ECARE_PLP_NOT_FOUND, FF_ECARE_PLP_NOT_FOUND);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.FF, com.redknee.app.ff.ecare.rmi.FFECareRmiConstants.FF_ECARE_INTERNAL_FAILURE, FF_ECARE_INTERNAL_FAILURE);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.FF, com.redknee.app.ff.ecare.rmi.FFECareRmiConstants.FF_ECARE_CUG_TEMPLATE_NOT_FOUND, FF_ECARE_CUG_TEMPLATE_NOT_FOUND);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.FF, com.redknee.app.ff.ecare.rmi.FFECareRmiConstants.FF_ECARE_CUG_INSTANCE_NOT_FOUND, FF_ECARE_CUG_INSTANCE_NOT_FOUND);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.FF, com.redknee.app.ff.ecare.rmi.FFECareRmiConstants.FF_ECARE_TEMPLATE_DEPENDANCY, FF_ECARE_TEMPLATE_DEPENDANCY);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.FF, com.redknee.app.ff.ecare.rmi.FFECareRmiConstants.FF_ECARE_USER_LIMIT_EXCEEDED, FF_ECARE_USER_LIMIT_EXCEEDED);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.FF, com.redknee.app.ff.ecare.rmi.FFECareRmiConstants.FF_ECARE_CUG_TEMPLATE_NOT_AVAILABLE, FF_ECARE_CUG_TEMPLATE_NOT_AVAILABLE);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.FF, com.redknee.app.ff.ecare.rmi.FFECareRmiConstants.FF_ECARE_CUG_DUPLICATE_SHORTCODE, FF_ECARE_CUG_DUPLICATE_SHORTCODE);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.FF, com.redknee.app.ff.ecare.rmi.FFECareRmiConstants.FF_ECARE_PLP_MEMBER_ADD_EXCEEDED, FF_ECARE_PLP_MEMBER_ADD_EXCEEDED);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.FF, com.redknee.app.ff.ecare.rmi.FFECareRmiConstants.FF_ECARE_DISALLOWED_PLP_MEMBER_REMOVAL, FF_ECARE_DISALLOWED_PLP_MEMBER_REMOVAL);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.FF, com.redknee.app.ff.ecare.rmi.FFECareRmiConstants.FF_ECARE_MSISDN_NOT_FOUND, FF_ECARE_MSISDN_NOT_FOUND);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.FF, com.redknee.app.ff.ecare.rmi.FFECareRmiConstants.FF_ECARE_SHORTCODE_ALREADY_EXISTS, FF_ECARE_SHORTCODE_ALREADY_EXISTS);
    }

    private void addHomeZoneExternalAppErrorCodeMessages(Context ctx)
    {
      String SQL_ERROR = "SQL error occurred";
      String INTERNAL_ERROR = "Internal error occurred";
      String MANDATORY_INFO_MISSING = "Mandatory info missing";
      String INVALID_PARAMETER = "Invalid parameter provided";
      String UPDATE_NOT_ALLOWED = "Update is not allowed";
      String ENTRY_ALREADY_EXISTED = "Entry already exists";
      String ENTRY_NOT_FOUND = "Entry was not found";
      String INVALID_HOMEZONE_PRIORITY = "Invalid HomeZone priority informed";
      String HOMEZONE_PRIORITY_ALREADY_EXISTED = "HomeZone priority informed already exists";

        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.HOMEZONE, com.redknee.app.homezone.corba.ErrorCode.SQL_ERROR, SQL_ERROR);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.HOMEZONE, com.redknee.app.homezone.corba.ErrorCode.INTERNAL_ERROR, INTERNAL_ERROR);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.HOMEZONE, com.redknee.app.homezone.corba.ErrorCode.MANDATORY_INFO_MISSING, MANDATORY_INFO_MISSING);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.HOMEZONE, com.redknee.app.homezone.corba.ErrorCode.INVALID_PARAMETER, INVALID_PARAMETER);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.HOMEZONE, com.redknee.app.homezone.corba.ErrorCode.UPDATE_NOT_ALLOWED, UPDATE_NOT_ALLOWED);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.HOMEZONE, com.redknee.app.homezone.corba.ErrorCode.ENTRY_ALREADY_EXISTED, ENTRY_ALREADY_EXISTED);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.HOMEZONE, com.redknee.app.homezone.corba.ErrorCode.ENTRY_NOT_FOUND, ENTRY_NOT_FOUND);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.HOMEZONE, com.redknee.app.homezone.corba.ErrorCode.INVALID_HOMEZONE_PRIORITY, INVALID_HOMEZONE_PRIORITY);
        addExternalAppErrorCodeMessageBean(ctx, ExternalAppEnum.HOMEZONE, com.redknee.app.homezone.corba.ErrorCode.HOMEZONE_PRIORITY_ALREADY_EXISTED, HOMEZONE_PRIORITY_ALREADY_EXISTED);
    }

    protected static ExternalAppSupport instance_ = null;
}
