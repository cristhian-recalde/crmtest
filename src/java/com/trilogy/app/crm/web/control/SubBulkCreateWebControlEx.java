/*
 * Created on 2004-12-31
 *
 * Copyright (c) 1999-2003 REDKNEE.com. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * REDKNEE.com. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with REDKNEE.com.
 *
 * REDKNEE.COM MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE
 * SUITABILITY OF THE SOFTWARE, EITHCDR EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE IMPLIED WARRANTIES OF MCDRCHANTABILITY, FITNESS FOR
 * A PARTICULAR PURPOSE, OR NON-INFRINGEMENT. REDKNEE.COM SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFCDRED BY LICENSEE AS A RESULT OF USING,
 * MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DCDRIVATIVES.
 */
package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;
import java.sql.SQLException;

import com.trilogy.app.crm.bean.*;
import com.trilogy.app.crm.numbermgn.NumberMgnSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xlog.log.MajorLogMsg;
/**
 * @author jchen
 *@author klin
 * Customized WebControl to reflect correct "number of subscribers" in the match mobile number and group 
 */
public class SubBulkCreateWebControlEx extends ProxyWebControl
{

    /**
     * Apply SAT, make sure SAT value is on the top of subscriber profile. 
     * @see com.redknee.framework.xhome.webcontrol.OutputWebControl#toWeb(com.redknee.framework.xhome.context.Context, java.io.PrintWriter, java.lang.String, java.lang.Object)
     */
    public SubBulkCreateWebControlEx(WebControl delegate){
    	super(delegate);
    }
	
	public void toWeb(Context ctx, PrintWriter out, String name, Object obj) 
    {
        SubBulkCreate form = (SubBulkCreate)obj;
        form.setStartingMsisdn("");
        try
        {
        	if (form.getTechnology() != null)
        	{
	        	long subCnt = calcMatchSubscribers(ctx, form); 
	        	form.setSubCnt(subCnt);
        	}        	
        	updateStartingMsisdn(ctx, form);
        	if (form.getSubNumToCreate() <= 0)
        		form.setSubNumToCreate(form.getSubCnt());        	
        }
        catch(HomeException e)
        {
            new MajorLogMsg(this, "Database error.", e).log(ctx);
        }
        super.toWeb(ctx, out, name, obj);
    }
    
    
    /*
     * Updates starting msisdn if msisdn group changed.
     */
    public void updateStartingMsisdn(Context ctx, SubBulkCreate form) throws HomeException
    {
        if (form.getMsisdnGroup() != form.getLastMsisdnGroup() || form.getStartingMsisdn().trim().length()==0)
        {   
            form.setStartingMsisdn(getFirstAvailMsisdn(ctx, form));
            form.setLastMsisdnGroup( form.getMsisdnGroup());
        }        
    }
    
    String getFirstAvailMsisdn(Context ctx, SubBulkCreate form) throws HomeException
    {
        Msisdn msisdn = NumberMgnSupport.getFirstAvailMsisdn(ctx, form.getAccount().getSpid(), form.getMsisdnGroup(), SubscriberTypeEnum.PREPAID_INDEX);
        return msisdn == null ? "" : msisdn.getMsisdn();
    }
    
    
    
    /**
     * UPdates the number of matching subscribers.
     * @param ctx Context object
     * @param form SubBulkCreate Object
     * @return int representing the calculated Match of Subscribers count.
     */
    public static long calcMatchSubscribers(Context ctx, SubBulkCreate form) throws HomeException
    {
        return Math.min(getAvailMsisdnCnt(ctx, form), getAvailPackageCnt(ctx, form));
    }
    
    /**
     * This method returns the Available Msisdns count
     * @param ctx Context object
     * @param form SubBulkCreate Object
     * @return integer representing number of available msisdns 
     * @throws HomeException 
     */
    public static long getAvailMsisdnCnt(Context ctx, SubBulkCreate form) throws HomeException
    {
        long count =  NumberMgnSupport.getAvailMsisdnCnt(ctx, form.getAccount().getSpid(), form.getMsisdnGroup(), SubscriberTypeEnum.PREPAID_INDEX, form.getStartingMsisdn());
        form.setFreeMsisdn(count);
        return count;
    }
  
    
    /**
     * Gets the available package number
     * @param ctx Context object
     * @param form SubBulkCreate Object
     * @return int representing the Available packages
     * @throws HomeException exception thrown
     */
    public static long getAvailPackageCnt(Context ctx, SubBulkCreate form) throws HomeException
    {
        long count = NumberMgnSupport.getAvailPackageCount(ctx, form.getTechnology(), form.getAccount().getSpid(), form.getPackageGroup());
        form.setFreePackage(count);
        return count;
    
    }
    
    /**
     * validates  the number of matching subscribers.
     * @param ctx context object 
     * @param form SubBulkCreate form object
     * @return integer value representing the number of Subscribers
     */
    public static long validateCalcMatchSubscribers(Context ctx, SubBulkCreate form) throws HomeException
    {
        return Math.min(validateAvailMsisdnCnt(ctx, form), validateAvailPackageCnt(ctx, form));
        
    }
    
    /**
     * Returns the available msisdns for that msisdn group
     * @param ctx context object
     * @param form SubBulkCreate object
     * @return int representing the available msisdns for that group
     * @throws HomeException exception thrown
     */
    public static long validateAvailMsisdnCnt(Context ctx, SubBulkCreate form) throws HomeException
    {
        long count =  NumberMgnSupport.getAvailMsisdnCnt(ctx, form.getAccount().getSpid(), form.getMsisdnGroup(), SubscriberTypeEnum.PREPAID_INDEX, form.getStartingMsisdn());
        return count;
    }
    
    /**
     * Gets the available package number
     * @param ctx
     * @param form
     * @return
     * @throws SQLException
     */
    public static long validateAvailPackageCnt(Context ctx, SubBulkCreate form) throws HomeException
    {
        long count = NumberMgnSupport.getAvailPackageCount(ctx, form.getTechnology(), form.getAccount().getSpid(), form.getPackageGroup());
        return count;
    }
    
    
   
    
    
}
