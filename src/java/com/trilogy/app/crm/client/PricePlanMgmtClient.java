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

import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.urcs.provision.RatePlanInfo;
import com.trilogy.app.urcs.provision.RatePlanType;
import com.trilogy.framework.xhome.context.Context;

/**
 * Interface used to update the external mapping of Price Plan and Rate Plan
 * @author angie.li@redknee.com
 *
 * Refactored reusable elements to an abstract class AbstractCrmClient<T>
 * @author rchen
 * @since June 26, 2009 
 */
public interface PricePlanMgmtClient 
{

    /**
     *  Create, update or remove an entry in the pricePlan to ratePlan mapping table.
     *
     *  If a mapping already exists for this combination of spid, pricePlanId, and RatePlanType, then the entry is updated.  
     *  If no entry currently exists, then a new one is created.
     *  If the ratePlanId is an empty string, then any existing mappings between the priceplan and rateplan are removed.
     *
     *  @param spid          the Service Provider Id
     *  @param pricePlanId   the ID of the priceplan mapping to create/update
     *  @param type          the type of ratePlan to update
     *  @param ratePlanId    the string identifier of the rateplan 
     *
     *  @return
     *  <ul>
     *   <li> SUCCESS - if the mapping was created/updated sucessfully</li>
     *   <li> NO_RATEPLAN_FOUND - if no ratePlan currently exists with the given ratePlanId and type</li>
     *   <li>ILLEGAL_RATEPLAN_TYPE � the value of <CODE>type</CODE> was not one of VOICE, SMS, or DATA</li>
     *   <li> INTERNAL_ERRROR - if an internal error occurs</li>
     * </ul>
     */
    void mapPricePlan(final int spid, final String pricePlanId, final RatePlanType type, final String ratePlanId) throws PricePlanMgmtException; 
    
    /**
     * Query for all rateplans of a given type, with a given SpID and whose IDs begin with the supplied prefix.
     *
     * If the supplied ratePlanIdPrefix is an empty string, then all information about all rateplans with the given spid and type 
     * will be returned.
     *
     * @param spid              the spid of the RatePlan.  used to restrict the search
     * @param type              the type of RatePlan. used to restrict the search
     * @param ratePlanIdPrefix  the prefix of the rateplan to search for.
     *
     * @param ratePlans    output paramter to hold the array of returned rateplans.  This output parameter is only defined if
     *                     the return code of the method is ERR_CODE_SUCCESS.  Otherwise, its value is undefined.
     *
     * @return
     *  <ul>
     *   <li> SUCCESS - if the query completed sucessfully</li>
     *   <li> NO_RATEPLAN_FOUND - if the query returned no results</li>
     *   <li>ILLEGAL_RATEPLAN_TYPE � the value of type was not recognized</li>
     *   <li> INTERNAL_ERRROR - if an internal error occurs</li>
     * </ul>
     *
     */
    RatePlanInfo[] queryRatePlans(final int spid, final RatePlanType type, final String ratePlanIdPrefix) throws PricePlanMgmtException;
    
	/**
	 * Result code to set when there are communication errors with the server.
	 */
	public final short COMM_ERROR_RESULT_CODE = 301;

	/**
	 * Result code to set when service is not available.
	 */
	public final short NO_SERVICE_ERROR_CODE = -1;
}
