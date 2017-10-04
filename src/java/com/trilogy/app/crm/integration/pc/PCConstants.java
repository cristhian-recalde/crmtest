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

package com.trilogy.app.crm.integration.pc;

/**
 * All the constants required for COM - Product and Catalog Integration.
 */

public class PCConstants {
	public final static String PC_SOAP_CLIENT = "Comapi-v3_0-PcClient-v1_0";
	public final static String PC_SOAP_CLASS = "com.redknee.util.crmapi.wsdl.v3_0.api.PcService";
	public final static String PC_PRICINGTEMPLATE_SOURSE = "UNIFIEDBILLING";
	public static final String FREQUENCY = "MONTHLY";
	public static final  String  EXTERNAL_KEY  = "ExternalKey";
	public static final  String  SOURCE        = "UNIFIEDBILLING";
	public static final String STATUS = "SUCCESS";
	
	/**
	 * Business Key for charSpec of Technical Service Template will have the following prefix
	 */
	public static final String TECHNICAL_SERVICE_TEMPLATE_CS_BK_PREFIX = "2B_TST_";
	
	/**
	 * Business Key for charSpec of Price Template will have the following prefix
	 */
	public static final String PRICE_TEMPLATE_CS_BK_PREFIX = "2B_PT_";
	
	//Status can either be success or failed. Hence created redundant constant for success. Will add the constant for faliure when needed.
	public static final String STATUS_SUCCESS = "SUCCESS";
	
	public static final String STATUS_FAIL = "FAIL";
}
