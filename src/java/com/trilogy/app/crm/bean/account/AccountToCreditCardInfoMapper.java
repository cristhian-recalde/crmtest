package com.trilogy.app.crm.bean.account;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.CreditCardAccountTypeEnum;
import com.trilogy.app.crm.bean.CreditCardEntry;
import com.trilogy.app.crm.bean.CreditCardInfo;
import com.trilogy.app.crm.bean.CreditCardInfoHome;
import com.trilogy.app.crm.home.generic.OnewayMapper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

public class AccountToCreditCardInfoMapper 
implements OnewayMapper
{
 

	public Object create(Context ctx, Object obj)
	throws HomeException
	{
	   Account newAcct = (Account) obj;
	   if (newAcct.isCreditCardPayment())
	   {   
		   createCreditCardInfo(ctx, newAcct);
	   }
       return obj;
	}
	
	public Object update(Context ctx, Object obj)
	throws HomeException
	{
		Account newAcct = (Account) obj;
        Home home = (Home) ctx.get(CreditCardInfoHome.class);
        CreditCardInfo info = (CreditCardInfo) home.find(ctx, newAcct.getBAN());
        if (needsCreateCreditCardInfo(ctx, info, newAcct))
        {
            createCreditCardInfo(ctx, newAcct);
        } else if (needsDeleteCreditCardInfo(ctx, info, newAcct))
        {
            deleteCreditCardInfo(ctx, newAcct);
        }
        else 
        {
            updateCreditCardInfo(ctx, info, newAcct);
        }
        return newAcct;
	}
    
    public void delete(Context ctx, Object obj)
    throws HomeException
    {
        deleteCreditCardInfo(ctx, (Account) obj);
    }
    
    
    /**
     * Checks if we need to delete a <code>CreditCardInfo</code> entry into the credit card table
     * 
     * @param ctx
     * @param oldAcct
     * @param newAcct
     * @return
     */
    private boolean needsDeleteCreditCardInfo(Context ctx,CreditCardInfo cardInfo, Account newAcct)
    {
         return (cardInfo != null &&  !newAcct.isCreditCardPayment());

    }
    

    /**
     * Checks if we need to create a <code>CreditCardInfo</code> entry into the credit card table
     * 
     * @param ctx
     * @param oldAcct
     * @param newAcct
     * @return
     */
    private boolean needsCreateCreditCardInfo(Context ctx, CreditCardInfo cardInfo, Account newAcct)
    {
         return  (cardInfo == null && newAcct.isCreditCardPayment() ); 
        
    }
    

    
    /**
     * Creates credit card info into the credit card table
     * 
     * @param ctx
     * @param acct
     * @throws HomeException
     */
    private void createCreditCardInfo(Context ctx, Account acct)
        throws HomeException
    {
        Home home = (Home) ctx.get(CreditCardInfoHome.class);
        CreditCardInfo info = toCreditCardInfo(ctx, acct);
        info.setSpid(acct.getSpid());
        home.create(ctx, info);
    }
    
    
    /**
     * Deletes credit card info from the credit card table
     * 
     * @param ctx
     * @param acct
     * @throws HomeException
     */
    private void deleteCreditCardInfo(Context ctx, Account acct)
        throws HomeException
    {
        Home home = (Home) ctx.get(CreditCardInfoHome.class);
        CreditCardInfo info = (CreditCardInfo) home.find(ctx, acct.getBAN());
        if (info!=null)
        {
            info.setSpid(acct.getSpid()); 
            home.remove(ctx, info);
        }
    }
    
    
    /**
     * Updates credit card info into the credit card table
     * 
     * @param ctx
     * @param acct
     * @throws HomeException
     */
    private void updateCreditCardInfo(Context ctx, CreditCardEntry oldEntry, Account newAccount)
        throws HomeException
    {
    
    	if ( oldEntry != null)
    	{	
    		CreditCardInfo info = toCreditCardInfo(ctx, newAccount);
        
    		// this will cover in case other field except creditcardnumber is updated.
    		if (oldEntry.getEncodedCreditCardNumber()!= null )
    		{	
    			info.setEncodedCreditCardNumber(oldEntry.getEncodedCreditCardNumber());
    		}
    		//info.setDecodedCreditCardNumber(oldEntry.getDecodedCreditCardNumber());
    		info.setSpid(newAccount.getSpid());
        
    		Home home = (Home) ctx.get(CreditCardInfoHome.class);
    		home.store(ctx, info);
    	}
    }
    
    
    
    /**
     * Gets a <code>CreditCardInfo</code> object from the Account's 
     * transient credit card information.
     * 
     * @param acct
     * @return
     * @throws HomeException 
     * @throws InstantiationException 
     */
    private CreditCardInfo toCreditCardInfo(Context ctx, Account acct)
        throws HomeException
    {
        CreditCardInfo info = new CreditCardInfo();
        info.setId(acct.getBAN());
        info.setLevelType(CreditCardAccountTypeEnum.ACCOUNT);
        
        try
        {
             copyEntryIntoInfo(acct.getCreditCardInfo(), info);
        }
        catch (Exception e)
        {
            throw new HomeException("Exception encountered copying info from Account credit card entry into credit card info object.", e);
        }
        
        return info;
    }
    

    
    /**
     * This method copies all the information from <code>CreditCardEntry</code>
     * that should be in <code>CreditCardInfo</code>.
     * 
     * @param entry Source of information to copy from, must not be null
     * @param info Destination to copy entries into, must not be null
     */
    public static void copyEntryIntoInfo(CreditCardEntry entry, CreditCardInfo info)
    {
        info.setCardName(entry.getCardName());
        info.setCardNumber(entry.getCardNumber());
         info.setExpiryDate(entry.getExpiryDate());
        info.setCardTypeId(entry.getCardTypeId());
         
    }

}
