package com.trilogy.app.crm.bean;

/**
 * specify the system defined transaction method id here. 
 * it should be above 1000, and below 10000
 * @author lxia
 *
 */
public interface SystemTransactionMethodsConstants
{

	public final static int TRANSACTION_METHOD_CASH = 1; 
	public final static int TRANSACTION_METHOD_CHEQUE = 2; 
	public final static int TRANSACTION_METHOD_DEBIT_CARD = 3; 
	public final static int TRANSACTION_METHOD_TELEANKING = 4; 
	public final static int TRANSACTION_METHOD_CREDIT_CARD = 5; 
	public final static int TRANSACTION_METHOD_INVOICE = 9;
	public final static int TRANSACTION_METHOD_TRANSFER = 10; 
	public final static int TRANSACTION_METHOD_DIRECT_BANK_SLIP = 1001;
	public final static int TRANSACTION_METHOD_GOVERNMANET_VOUCHER = 1002;
	public final static int TRANSACTION_METHOD_LOYALTY_VOUCHER = 1003;
	public final static int TRANSACTION_METHOD_BANK_LOAN = 1004;
	public final static int TRANSACTION_METHOD_COMPANY_LOAN = 1005;
	
	
}
