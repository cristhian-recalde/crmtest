/*
 *  HLRConstants.java
 *
 *  Author : Kevin Greer
 *  Date   : Feb 13, 2004
 *
 *  Copyright (c) Redknee, 2004
 *  - all rights reserved
 */

package com.trilogy.app.crm.home.sub;

import com.trilogy.app.crm.bean.ProvisionCommandTypeEnum;

/** Constants for HLR Commands. **/
public interface HLRConstants
{
	public static final String PRV_CMD_TYPE_INACTIVE                      = ProvisionCommandTypeEnum.inactive.getDescription();
	public static final String PRV_CMD_TYPE_DEACTIVE                      = ProvisionCommandTypeEnum.deactivate.getDescription(); //"deactivate";
    public static final String PRV_CMD_TYPE_CREATE                        = ProvisionCommandTypeEnum.create.getDescription(); //"create";
	public static final String PRV_CMD_TYPE_BARRED                        = ProvisionCommandTypeEnum.barred_locked.getDescription(); //"barred/locked";
	public static final String PRV_CRM_TYPE_ACTIVATE                      = ProvisionCommandTypeEnum.active.getDescription(); //"active";
	public static final String PRV_CMR_TYPE_PACKAGEUPDATE                 = ProvisionCommandTypeEnum.packageUpdate.getDescription(); //"packageUpdate";
    public static final String PRV_CMD_TYPE_CONVERSION                    = ProvisionCommandTypeEnum.conversion.getDescription(); //"conversion";
	public static final String PRV_CMD_TYPE_MSISDN_CHANGE                 = ProvisionCommandTypeEnum.msisdnChange.getDescription(); //"msisdnChange";
    public static final String PRV_CMD_TYPE_DORMANTTOACTIVE               = ProvisionCommandTypeEnum.dormantToActive.getDescription(); //"dormantToActive";
    public static final String PRV_CMD_TYPE_DORMANT                       = ProvisionCommandTypeEnum.dormant.getDescription(); //"dormant";
    public static final String PRV_CMD_TYPE_MULTISIM_REMOVE_MSISDN        = ProvisionCommandTypeEnum.multiSimRemoveMsisdn.getDescription(); //"multiSimRemoveMsisdn";
    public static final String PRV_CMD_TYPE_MULTISIM_SUSPEND              = ProvisionCommandTypeEnum.multiSimSuspend.getDescription(); //"multiSimSuspend";
    public static final String PRV_CMD_TYPE_MULTISIM_UNSUSPEND            = ProvisionCommandTypeEnum.multiSimUnsuspend.getDescription(); //"multiSimUnsuspend";
    public static final String PRV_CMD_TYPE_CLEAN_PROFILE                 = ProvisionCommandTypeEnum.CleanProfile.getDescription(); //"CleanProfile";
    public static final String PRV_CMD_TYPE_REFRESH_PROFILE               = ProvisionCommandTypeEnum.refresh.getDescription(); //"refresh";
    public static final String PRV_CMD_TYPE_RESET_VM_PIN                  = ProvisionCommandTypeEnum.vmpin_reset.getDescription(); //"vmpin_reset";
    public static final String PRV_CMD_TYPE_BULK_SERVICE_UPDATE           = ProvisionCommandTypeEnum.bulkServiceUpdate.getDescription(); //"bulkServiceUpdate";
    
    public static final String PRV_CMD_TYPE_PRICEPLAN_CHANGE              = ProvisionCommandTypeEnum.pricePlanChange.getDescription(); //"pricePlanChange";
    public static final String PRV_CMD_TYPE_BILLCYCLE_CHANGE              = ProvisionCommandTypeEnum.billCycleChange.getDescription(); //"billCycleChange";
    public static final String PRV_CMD_TYPE_LANG_CHANGE					  = ProvisionCommandTypeEnum.languageChange.getDescription(); // language change
    public static final String PRV_CMD_TYPE_MULTISIM_IMSI_SWAP			  = ProvisionCommandTypeEnum.multiSimImsiSwap.getDescription(); // "multisimSwap"
    
    
    public static final String HLR_PARAMKEY_OLD_IMSI_KEY             = "HLR.paramkey.old_imsi";
    public static final String HLR_PARAMKEY_BEARER_TYPE_KEY          = "HLR.paramkey.bearer_type";
    public static final String HLR_PARAMKEY_PROVISION_CMD_NAME       = "HLR.paramkey.prov_cmd_name";
    
    public static final String SUCCESS_CODE_STRING                   = "0";
    public static final int SUCCESS_CODE                             = 0;
    public static final Object ACCOUNT_BILLCYCLE_CHANGE = "ACCOUNT_BILLCYCLE_CHANGE";
}
