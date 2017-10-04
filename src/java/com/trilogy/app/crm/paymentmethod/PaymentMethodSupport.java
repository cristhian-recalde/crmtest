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
package com.trilogy.app.crm.paymentmethod;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.GTE;
import com.trilogy.framework.xhome.elang.LTE;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.elang.Order;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
//import com.trilogy.app.crm.bas.tps.PaymentMethodEnum;
import com.trilogy.app.crm.bean.AccountPaymentMethod;
import com.trilogy.app.crm.bean.AccountPaymentMethodXInfo;
//import com.trilogy.app.crm.bean.ChangePaymentMethodMapping;
//import com.trilogy.app.crm.bean.ChangePaymentMethodMappingXInfo;
import com.trilogy.app.crm.bean.FileFormatterConfig;
//import com.trilogy.app.crm.bean.InvoiceGenerationPreferenceEnum;
//import com.trilogy.app.crm.bean.PaymentMethodConstants;
/*import com.trilogy.app.crm.bean.PaymentMethodOperationEnum;
import com.trilogy.app.crm.bean.PaymentMethodProcessorConfig;
import com.trilogy.app.crm.bean.PaymentMethodProcessorConfigXInfo;
import com.trilogy.app.crm.bean.PaymentMethodProcessorMapping;
import com.trilogy.app.crm.bean.PaymentMethodProcessorMappingXInfo;
import com.trilogy.app.crm.bean.PaymentTypeValidationStateEnum;
import com.trilogy.app.crm.bean.PaymentValidationHistory;
import com.trilogy.app.crm.bean.TopAndCreditCard;
import com.trilogy.app.crm.bean.TopAndCreditCardHome;
import com.trilogy.app.crm.bean.TransactionMethod;
import com.trilogy.app.crm.bean.TransactionMethodXInfo;
import com.trilogy.app.crm.chargetransfer.loader.EnergyChargeTransferSupportHelper;
import com.trilogy.app.crm.paymentmethod.exception.PaymentMethodProcessorException;
*/
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.GTE;
import com.trilogy.framework.xhome.elang.LTE;
import com.trilogy.framework.xhome.elang.Order;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
/*
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v3_0.types.paymentgatewayintegration.AccountPaymentMethodHistoryRequest;
import com.trilogy.util.crmapi.wsdl.v3_0.types.paymentgatewayintegration.ChangeAccountPaymentMethodRequest;
import com.trilogy.util.crmapi.wsdl.v3_0.types.paymentgatewayintegration.ChangeAccountPaymentMethodResponse;
*/import com.trilogy.util.snippet.log.Logger;



/**
 * @author bdhavalshankh
 * since 10.2
 */
public class PaymentMethodSupport 
{

	public final static String MODULE = PaymentMethodSupport.class.getName();
	public final static String MEDIATION_CALLS = "fromMediation";
	 
	/**
	 * Generic parameter to be retreived from DCRM request.
	 */
	public static final String INVOICE_PREF_OPTION = "invoiceGenPreference";
	public static final String   SHOW_BANK_DETAILS ="showBankDetails";
	public static final String   FIRST_NAME ="FirstName";
	public static final String   LAST_NAME ="LastName";
	public static final String   LAST_TOKEN_DELETED_MAP ="LastTokenDeletedMap";
	
	public static final String PREAUTHORISED = "PreAuthorised";
	public static final String CREDIT_CARD_TOKEN = "CreditCardToken";

	
	/**
	 * 
	 * @param ctx
	 * @param fileFormatterId
	 * @return
	 */
	public static FileFormatterConfig retrieveFileFormatterConfig(Context ctx, long fileFormatterId)
	{
		FileFormatterConfig formatterConfig=null;	
		try 
		{
			formatterConfig=(FileFormatterConfig)HomeSupportHelper.get(ctx).findBean(ctx, FileFormatterConfig.class, fileFormatterId);
		}
		catch (HomeException e)
		{
			LogSupport.major(ctx, MODULE, "Exception occurred while retrieving File Formatter Configurations " + e.getMessage());
		}
		return formatterConfig;
	}

	
}
