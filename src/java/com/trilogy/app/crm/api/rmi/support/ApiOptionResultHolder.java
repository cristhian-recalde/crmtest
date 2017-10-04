/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.api.rmi.support;

import com.trilogy.app.crm.bean.service.SubscriberSubscriptionHistory;

/**
 * @author sbanerjee
 *
 */
public class ApiOptionResultHolder
{
    /**
     * The reference to the ChargingHistory record, so we can avoid querying the DB back again
     * for compiling the option-result in the API 
     */
    private SubscriberSubscriptionHistory chargingHistoryRecord;
    
    private int ocgResultCode;
    
    private int urcsResultCode;
    
    private int bssResultCode;
    
    /*
     * For now only SUCCESS (0) and FAIL (-1)
     */
    private int overallResultCode;
    
    private OptionUpdateType optionUpdateType = OptionUpdateType.PROVISION;
    
    /**
     * @return the optionUpdateType
     */
    public final OptionUpdateType getOptionUpdateType()
    {
        return this.optionUpdateType;
    }

    /**
     * @param optionUpdateType the optionUpdateType to set
     */
    public final void setOptionUpdateType(OptionUpdateType optionUpdateType)
    {
        this.optionUpdateType = optionUpdateType;
    }

    /**
     * @return the errorMessage
     */
    public final String getErrorMessage()
    {
        return errorMessage;
    }

    /**
     * @param errorMessage the errorMessage to set
     */
    public final void setErrorMessage(String errorMessage)
    {
        this.errorMessage = errorMessage;
    }

    private String errorMessage= " SUCCESS";

    /**
     * @return the ocgResultCode
     */
    public int getOcgResultCode()
    {
        return ocgResultCode;
    }
    
    /**
     * @return the overallResultCode
     */
    public int getOverallResultCode()
    {
        return overallResultCode;
    }


    /**
     * @param overallResultCode the overallResultCode to set
     */
    public void setOverallResultCode(int overallResultCode)
    {
        this.overallResultCode = overallResultCode;
    }



    /**
     * @param ocgResultCode the ocgResultCode to set
     */
    public void setOcgResultCode(int ocgResultCode)
    {
        this.ocgResultCode = ocgResultCode;
    }

    /**
     * @return the urcsResultCode
     */
    public int getUrcsResultCode()
    {
        return urcsResultCode;
    }

    /**
     * @param urcsResultCode the urcsResultCode to set
     */
    public void setUrcsResultCode(int urcsResultCode)
    {
        this.urcsResultCode = urcsResultCode;
    }
    
    /**
     * @return the bssResultCode
     */
	public int getBssResultCode() {
		return bssResultCode;
	}

	 /**
     * @param bssResultCode the bssResultCode to set
     */
	public void setBssResultCode(int bssResultCode) {
		this.bssResultCode = bssResultCode;
	}

    /**
     * @return the chargingHistoryRecord
     */
    public SubscriberSubscriptionHistory getChargingHistoryRecord()
    {
        return chargingHistoryRecord;
    }

    /**
     * @param chargingHistoryRecord the chargingHistoryRecord to set
     */
    public void setChargingHistoryRecord(
            SubscriberSubscriptionHistory chargingHistoryRecord)
    {
        this.chargingHistoryRecord = chargingHistoryRecord;
    }
    
    /*
     * Helper methods
     */
    
    /**
     * @param h
     * @return
     */
    public ApiErrorSource getApiErrorSource()
    {
        if(getOcgResultCode()!=0)
            return ApiErrorSource.OCG;
        
        if(getUrcsResultCode()!=0)
            return ApiErrorSource.URCS;
        
        
        if(getBssResultCode()!=0)
        	return ApiErrorSource.BSS;
    
        /*
         * -- Add more as you need --
         */
        
        return ApiErrorSource.NONE;
    }
    
    /**
     * @param h
     * @return
     */
    public int getApiInternalErrorCode()
    {
        if(getOcgResultCode()!=0)
            return getOcgResultCode();
        
        if(getUrcsResultCode()!=0)
            return getUrcsResultCode();
    
        /*
         * -- Add more as you need --
         */
        
        return 0;
    }


}