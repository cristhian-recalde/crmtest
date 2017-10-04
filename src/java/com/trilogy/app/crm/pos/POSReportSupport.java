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
package com.trilogy.app.crm.pos;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import com.trilogy.framework.core.locale.Currency;
import com.trilogy.framework.core.locale.CurrencyHome;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.csv.AbstractCSVSupport;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.util.pattern.XPattern;
import com.trilogy.framework.xhome.xdb.XDB;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.ContactTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.account.ContactXInfo;
import com.trilogy.app.crm.support.HomeSupportHelper;

/**
 * A Collection of methods used to format values for Point of Sale Reporting.
 * 
 * @author Angie Li
 */
public class POSReportSupport 
{

    /**
     * Returns the Account identified by this ban.
     * @param ctx - context
     * @param ban - account identifier
     * @param logger - log write to use if there is an exception thrown
     * @return
     */
    public static Account getAccount(Context ctx, String ban, POSLogWriter logger)
    {
        Account account = null;
        try 
        {
        	account = getLimitedAccount(ctx,ban);
        }
        catch (HomeException he)
        {
            HomeException e = new HomeException("Failed to retrieve account BAN =" + ban,he);
            logger.thrown(e);
        }
        return account;
    }
    
    /**
	 * @param ctx
	 * @param ban
	 * @return
     * @throws HomeException
	 */
	private static Account getLimitedAccount(Context ctx, String ban) throws HomeException 
	{
        XDB xdb=(XDB) ctx.get(XDB.class);
		POSAccountHomeAdaptedVisitor visitor=new POSAccountHomeAdaptedVisitor();
		StringBuilder sql = new StringBuilder("SELECT ");
		sql.append(AccountXInfo.FIRST_NAME.getSQLName());
		sql.append(",").append(AccountXInfo.LAST_NAME.getSQLName());
		sql.append(",").append(AccountXInfo.BILLING_ADDRESS1.getSQLName());
		sql.append(",").append(AccountXInfo.CURRENCY.getSQLName());
        sql.append(",").append(AccountXInfo.SPID.getSQLName());
		sql.append(" FROM ACCOUNT ");
		sql.append(" WHERE ");
		sql.append(AccountXInfo.BAN.getSQLName()).append("='");
		sql.append(ban).append("'");
		xdb.forEach(ctx,visitor,sql.toString());
		
		if(visitor.getResult()!=null)
		{
			Account acct=visitor.getResult();
			acct.setBAN(ban);
			acct.setContext(ctx);
			return acct;
		}
		
        final Home acctHome = (Home) ctx.get(AccountHome.class);
        if (acctHome == null)
        {
            throw new HomeException("Could not look-up account.  No AccountHome in context.");
        }

        final Account acct = (Account) acctHome.find(ctx, ban);

		return acct;
	}


	/**
     * Formats the Last Name - First Name from the Account 
     * 
     * @param account
     * @return returned string is at most 100 characters long.
     */
    public static String formatAccountName(Account account)
    {
        String name = "";
        if (account != null)
        {
            name = account.getLastName() + "-" + account.getFirstName();
            name = name.replace('\t', ' ').replace("\r", "").replace("\n", "");
            name = (name.length() > 100 ? name.substring(0,99) : name);
        }
        return name;
    }
    
	/**
     * Formats the Last Name - First Name from the Account 
     * 
     * @param account
     * @return returned string is at most 100 characters long.
     */
    public static String formatAccountName(AccountInformation account)
    {
        String name = "";
        if (account != null)
        {
            name = account.getLastName() + "-" + account.getFirstName();
            name = name.replace('\t', ' ').replace("\r", "").replace("\n", "");
            name = (name.length() > 100 ? name.substring(0,99) : name);
        }
        return name;
    }

    
    /**
     * Formats the Address from the Account
     * 
     * @param account
     * @return
     */
    public static String formatAccountAddress(Account account)
    {
        String address = "";
        if (account != null)
        {
            address = account.getBillingAddress1();
            
            if (account.getBillingAddress2().length() > 0 )
            {
                address = address.concat(" "+ account.getBillingAddress2());
            }
            if (account.getBillingAddress3().length() > 0 )
            {
                address = address.concat(" " + account.getBillingAddress3());
            }
            address = address.concat(" " + account.getBillingCity() 
                    + " " + account.getBillingProvince() 
                    + " " + account.getBillingCountry());
            address = address.replace('\t', ' ').replace("\r", "").replace("\n", "");
            address = (address.length() > 100 ? address.substring(0, 99) : address);
        }
        return address;
    }
    
    /**
     * Formats the Address from the Account
     * 
     * @param account
     * @return
     */
    public static String formatAccountAddress(AccountInformation account)
    {
        String address = "";
        if (account != null)
        {
            address = account.getBillingAddress1();
            
            if (account.getBillingAddress2().length() > 0 )
            {
                address = address.concat(" "+ account.getBillingAddress2());
            }
            if (account.getBillingAddress3().length() > 0 )
            {
                address = address.concat(" " + account.getBillingAddress3());
            }
            address = address.concat(" " + account.getBillingCity() 
                    + " " + account.getBillingProvince() 
                    + " " + account.getBillingCountry());
            address = address.replace('\t', ' ').replace("\r", "").replace("\n", "");
            address = (address.length() > 100 ? address.substring(0, 99) : address);
        }
        return address;
    }

    
    /**
     * Formats the Date to yyyy/MM/dd
     * @param date
     * @return a String displaying the date as yyyy/MM/dd
     */
    public static String formatDate(Date date)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        return dateFormat.format(date);
    }
    
    /**
     * Formats the cents amount to a dollar and cents amount using a lookup to the 
     * currency precision in the account profile.
     * @param ctx
     * @param amount
     * @param ban
     * @return String format in dollars and cents (0.00)
     */
    public static String formatAmount(Context ctx, String currID, long amount, String ban, POSLogWriter logger, int maxWidth)
    {
        String transAmount = "";
        try 
        {
            if (currID != null)
            {
                Currency currency = (Currency) ((Home) ctx.get(CurrencyHome.class)).find(currID);
                if (currency != null)
                {
                    transAmount = currency.formatValue(amount);
                    if (transAmount.length() > maxWidth)
                    {
                    	transAmount = transAmount.substring(0, maxWidth);
                    }
                }
            }
        }
        catch (HomeException he)
        {
            HomeException e = new HomeException("Failed to look up this account's currency. account=" + ban, he);
            logger.thrown(e);
        }
        return transAmount;
    }
    
    /**
     * Formats the cents amount to a dollar and cents amount using a lookup to the 
     * currency precision in the account profile.
     * @param ctx
     * @param amount
     * @param ban
     * @return String format in dollars and cents (0.00)
     */
    public static String formatIVRAmount(Context ctx, long amount, String ban, POSLogWriter logger, int maxWidth)
    {
        String transAmount = "";
        
        DecimalFormat formatter = new DecimalFormat("####0.00");
        
        transAmount = formatter.format(amount * .01);
        
        if (transAmount.length() > maxWidth)
        {
        	transAmount = transAmount.substring(0, maxWidth);
        }
        
        
        return transAmount;
    }


    /**
     * Formats the cents amount to a dollar and cents amount using a lookup to the 
     * currency precision in the account profile.
     * @param ctx
     * @param amount
     * @param ban
     * @return String format in dollars and cents (0.00)
     */
    public static String formatAmount(Context ctx, long amount, String ban, POSLogWriter logger, int maxWidth)
    {
        String transAmount = "";
        Account account = getAccount(ctx, ban, logger);
        if (account != null)
        {
        	transAmount = formatAmount(ctx, account.getCurrency(), amount, ban, logger, maxWidth);
        }
        return transAmount;
    }

    
    /**

     *      * Returns a msisdn of 7 characters.  
     * Always truncate to the last 7 digits.
     * @param msisdn
     * @return
     */
    public static String formatAccountMSISDN(String msisdn)
    {
        int length = msisdn.length();
        int startIndex = 0;
        if (length > 7)
        {
            startIndex = length - 7;
        }
        return msisdn.substring(startIndex);
    }

    /**
     * Returns a string that is that ban of given number of characters.  
     * Always truncate to the rightmost x number of digits.
     * @param ban given ban string
     * @param maxLength given length which we truncate
     * @return
     */
    public static String formatValue(String ban, int maxLength)
    {
        ban = ban.replace('\t', ' ').replace("\r", "").replace("\n", "");
        int length = ban.length();
        int startIndex = 0;
        if (length > maxLength)
        {
            startIndex = length - maxLength;
        }
        return ban.substring(startIndex);
    }
    
	/**
	 * @param ctx
	 * @param ban
	 * @return
	 * @throws HomeException
	 */
	public static Home getImmediateChildrenSubHome(Context ctx, String ban) throws HomeException
	{
	    
	    
        XDB xdb=(XDB) ctx.get(XDB.class);
		PostpaidSubscriberHomeAdaptedVisitor visitor=new PostpaidSubscriberHomeAdaptedVisitor(ctx);
		StringBuilder sql = new StringBuilder("SELECT ");
		sql.append(SubscriberXInfo.ID.getSQLName());
		sql.append(", ").append(SubscriberXInfo.MSISDN.getSQLName());
		sql.append(" FROM SUBSCRIBER ");
		sql.append(" WHERE ");
		sql.append(SubscriberXInfo.BAN.getSQLName()).append("='");
        sql.append(ban).append("'");
		sql.append(" AND ").append(SubscriberXInfo.SUBSCRIBER_TYPE.getSQLName()).append("=").append(SubscriberTypeEnum.POSTPAID_INDEX);

		xdb.forEach(ctx,visitor,sql.toString());
		
		if(visitor.getResult()!=null)
		{
			Home home=visitor.getResult();
			return home;
		}
		
		And filter = new And();
		filter.add(new EQ(SubscriberXInfo.BAN, ban));
		filter.add(new EQ(SubscriberXInfo.SUBSCRIBER_TYPE, SubscriberTypeEnum.POSTPAID));

		Context whereContext = HomeSupportHelper.get(ctx).getWhereContext(ctx, Subscriber.class, filter);
		Home subHome = (Home) whereContext.get(SubscriberHome.class);
        return subHome;
	}

	/**
	 * 
	 * @param ctx
	 * @param ban
	 * @param logger
	 * @return
	 * @throws HomeException
	 */
	public static Collection getAccountAccumulatorInformations(Context ctx, String ban, POSLogWriter logger) 
	throws HomeException 
	{
        XDB xdb=(XDB) ctx.get(XDB.class);
		POSAccountInfoAdaptedVisitor visitor=new POSAccountInfoAdaptedVisitor();
		
		final StringBuilder sql = new StringBuilder(512);
		sql.append("SELECT ");
		sql.append(" A.").append(AccountInformationXInfo.FIRST_NAME.getSQLName());
		sql.append(", A.").append(AccountInformationXInfo.LAST_NAME.getSQLName());
        sql.append(", A.").append(AccountInformationXInfo.BILLING_ADDRESS1.getSQLName());
        sql.append(", C.").append(ContactXInfo.ADDRESS_LINE_TWO.getSQLName()).append(" ").append(AccountInformationXInfo.BILLING_ADDRESS2.getSQLName());
        sql.append(", C.").append(ContactXInfo.ADDRESS_LINE_THREE.getSQLName()).append(" ").append(AccountInformationXInfo.BILLING_ADDRESS3.getSQLName());
        sql.append(", C.").append(ContactXInfo.CITY.getSQLName()).append(" ").append(AccountInformationXInfo.BILLING_CITY.getSQLName());
        sql.append(", C.").append(ContactXInfo.PROVINCE.getSQLName()).append(" ").append(AccountInformationXInfo.BILLING_PROVINCE.getSQLName());
        sql.append(", C.").append(ContactXInfo.COUNTRY.getSQLName()).append(" ").append(AccountInformationXInfo.BILLING_COUNTRY.getSQLName());
        sql.append(", A.").append(AccountInformationXInfo.CURRENCY.getSQLName());
        sql.append(", A.").append(AccountInformationXInfo.SPID.getSQLName());
        sql.append(", S.").append(AccountInformationXInfo.MSISDN.getSQLName());
        sql.append(" FROM ACCOUNT A, XCONTACT C, SUBSCRIBER S ");
        sql.append(" WHERE");
        sql.append(" A.").append(AccountXInfo.BAN.getSQLName()).append("= '").append(ban).append("'");
		sql.append(" AND S.").append(SubscriberXInfo.BAN.getSQLName()).append("=A.").append(AccountXInfo.BAN.getSQLName());
		sql.append(" AND S.").append(SubscriberXInfo.SUBSCRIBER_TYPE.getSQLName()).append("=").append(SubscriberTypeEnum.POSTPAID_INDEX);
		sql.append(" AND C.").append(ContactXInfo.ACCOUNT.getSQLName()).append("=A.").append(AccountXInfo.BAN.getSQLName());
		sql.append(" AND C.").append(ContactXInfo.TYPE.getSQLName()).append("=").append(ContactTypeEnum.PERSON_INDEX);
		
		xdb.forEach(ctx,visitor,sql.toString());
		
		return visitor.getResult();
	}
	
    public static final String QUOTES = "\"|\'";
    public static final XPattern QUOTES_PATTERN = XPattern.compile(QUOTES);
    /**
     * Replacement of Framework's AbstractCSVSupport.appendString(), which filters
     * out any single or double quotes. 
     * 
     * @param buf       Modified StringBuffer
     * @param str       String to be filtered and appended to buf
     * @return
     */
    public static StringBuffer appendStringWithoutQuotes(final StringBuffer buf, final String str)
    {
          if ( AbstractCSVSupport.NULL_STRING.equals(str) )
          {
             buf.append(AbstractCSVSupport.ESCAPED_NULL_STRING);
          }
          else if (str == null)
          {
              buf.append(str);
          }
          else
          {
             // remove single and double-quote characters
             buf.append(QUOTES_PATTERN.matcher(str).replaceAll(" "));
          }
          
          return buf;
    }
}
