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
package com.trilogy.app.crm.client.urcs;

import com.trilogy.app.urcs.loyaltyoperation.Amount;
import com.trilogy.app.urcs.loyaltyoperation.param.Parameter;
import com.trilogy.app.urcs.loyaltyoperation.param.ParameterID;
import com.trilogy.product.bundle.manager.provision.v4_0.loyalty.LoyaltyCardProfile;

/**
 * Helper class to hold return values for {@link LoyaltyOperationsClient} interface.
 * Available values depend on method used in {@link LoyaltyOperationsClient}. 
 * 
 * @author asim.mahmood@redknee.com
 * @since 9.1
 */
public class LoyaltyParameters
{


    private long availablePoints = 0;
    private String transactionId = "";
    private long calculatedPoints = 0;
    private long calculatedAmount = 0;

    public LoyaltyParameters(Parameter[] parameters)
    {
        for (Parameter param : parameters)
        {
            switch (param.parameterID)
            {
                case ParameterID.OUT_AVAILABLE_POINTS:
                    setAvailablePoints(param.value.longValue());
            }
        }
    }

    public void setAvailablePoints(long longValue)
    {
        this.availablePoints = longValue;
        
    }
    
    public long getAvailablePoints()
    {
        return availablePoints;
    }

    public void setTransactionId(String value)
    {
        this.transactionId = value;
    }
  
    public String getTransactionId()
    {
        return transactionId;
    }

    public void setCalculatedPoints(long value)
    {
        this.calculatedPoints = value;
        
    }
    public long getCalculatedPoints()
    {
        return calculatedPoints;
    }

    public void setCalculatedAmount(long value)
    {
        this.calculatedAmount = value;
        
    }
    
    public long getCalculatedAmount()
    {
        return calculatedAmount;
    }
    
    
    public StringBuffer append(StringBuffer buf)
    {
       return buf.append("LoyaltyParameters(")      
          .append("availablePoints: ")
          .append(getAvailablePoints())
          .append(", ")
          .append("transactionId: ")
          .append(getTransactionId())
          .append(", ")
          .append("calculatedPoints: ")
          .append(getCalculatedPoints())
          .append(", ")
          .append("calculatedAmount: ")
          .append(getCalculatedAmount())
          .append(")");
    }

    @Override
    public String toString()
    {
       return append(new StringBuffer()).toString();
    }
    
    public static String loyaltyCardProfileString(LoyaltyCardProfile profile)
    {
        if (profile == null)
        {
            return "LoyaltyCardProfile=null";
        }
        StringBuffer buffer = new StringBuffer();
        buffer.append("LoyaltyCardProfile(")      
        .append("subscriberId: ")
        .append(profile.subscriberId)
        .append(", ")
        .append("programId: ")
        .append(profile.programId)
        .append(", ")
        .append("loyaltyCardId: ")
        .append(profile.loyaltyCardId)
        .append(", ")
        .append("redemptionEnableFlag: ")
        .append(profile.redemptionEnableFlag)
        .append(", ")
        .append("accumulationEnableFlag: ")
        .append(profile.accumulationEnableFlag)
        .append(", ")
        .append("expiryDate: ")
        .append(profile.expiryDate)
        .append(")");
        
        return buffer.toString();
    }
}
