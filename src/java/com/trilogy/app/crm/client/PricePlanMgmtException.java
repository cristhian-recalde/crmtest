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
package com.trilogy.app.crm.client;

import com.trilogy.app.urcs.provision.PricePlanMgmt;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.language.MessageMgr;

/**
 * Exception thrown by PricePlanMgmt interface
 * @author angie.li@redknee.com
 *
 */
public class PricePlanMgmtException extends Exception 
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public PricePlanMgmtException(final short resultCode, final String message)
    {
        super(message);
        this.resultCode_ = resultCode;
    }
    
    public static String getVerboseResult(Context ctx, final short resultCode)
    {
          MessageMgr mmgr = new MessageMgr(ctx, getModule());
          String value;
          switch(resultCode)
          {
          case PricePlanMgmt.CODE_SUCCESS:
              value = mmgr.get(SUCCESS_KEY, SUCCESS_DEFAULT_MESSAGE);
              break;
          case PricePlanMgmt.ILLEGAL_RATEPLAN_TYPE:
              value = mmgr.get(ILLEGAL_RATEPLAN_TYPE_KEY, ILLEGAL_RATEPLAN_TYPE_DEFAULT_MESSAGE);
              break;
          case PricePlanMgmt.NO_RATEPLAN_FOUND:
              value = mmgr.get(NO_RATEPLAN_FOUND_KEY, NO_RATEPLAN_FOUND_DEFAULT_MESSAGE);
              break;
          case PricePlanMgmt.INTERNAL_ERROR:
              value = mmgr.get(INTERNAL_ERROR_KEY, INTERNAL_ERROR_DEFAULT_MESSAGE);
              break;
          default:
              value = "Unspecified Error occurred.";
          }
          return value;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
    	final StringBuilder sb = new StringBuilder();
    	sb.append("Result code: ");
    	sb.append(this.resultCode_);
    	sb.append(", ");
    	sb.append(super.toString());
    	return sb.toString();
    }
    
    public short getResultCode()
    {
    	return resultCode_;
    }
    
    private static Class getModule()
    {
        return PricePlanMgmtException.class;
    }
    /**
     * URCS Result Code
     */
    private final short resultCode_;
    
    private final static String SUCCESS_KEY = "SUCCESS";
    private final static String SUCCESS_DEFAULT_MESSAGE = "Successful Result";
    private final static String NO_RATEPLAN_FOUND_KEY = "NO_RATEPLAN_FOUND";
    private final static String NO_RATEPLAN_FOUND_DEFAULT_MESSAGE = "No such Rate Plan was found in URCS.";
    private final static String ILLEGAL_RATEPLAN_TYPE_KEY = "ILLEGAL_RATEPLAN_TYPE";
    private final static String ILLEGAL_RATEPLAN_TYPE_DEFAULT_MESSAGE = "The rateplan type passed in was not one of the values corresponding to VOICE, SMS, or DATA.";
    private final static String INTERNAL_ERROR_KEY = "INTERNAL_ERROR";
    private final static String INTERNAL_ERROR_DEFAULT_MESSAGE = "Internal Error occurred in URCS.";
}
