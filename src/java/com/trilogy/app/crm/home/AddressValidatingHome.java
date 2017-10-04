package com.trilogy.app.crm.home;

import java.util.Collection;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.Address;
import com.trilogy.app.crm.bean.AddressXInfo;
import com.trilogy.app.crm.bean.SupplementaryData;
import com.trilogy.app.crm.bean.SupplementaryDataEntityEnum;
import com.trilogy.app.crm.bean.SupplementaryDataXInfo;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;

public class AddressValidatingHome extends HomeProxy {

	private static final long serialVersionUID = 1L;
	
	public AddressValidatingHome(final Context ctx, final Home delegate) 
	{
		super(ctx, delegate);
	}
	
	public Object create(Context ctx, Object obj) throws HomeException, HomeInternalException 
	{
		Address address = (Address) obj;
		
		validateAddressBAN(ctx, address);
		validateEmptyAddress(ctx, address);
		
		return super.create(ctx, address);
	}
	
	/*public Collection select(Context ctx, Object obj) throws HomeException, HomeInternalException 
	{
		Address address = (Address) obj;
		if(address.getAddressId() == null || address.getAddressId().isEmpty())
		{
			throw new HomeException("AddressId is mandatory");
		}
		
		return super.select(ctx, address);
		
	}*/


	public Object store(Context ctx, Object obj) throws HomeException, HomeInternalException 
	{
		Address address = (Address) obj;
		validateEmptyAddress(ctx, address);
		
		return super.store(ctx, address);
	}
	
	
	public void remove(Context ctx, Object obj) throws HomeException, HomeInternalException 
	{
		Address address = (Address) obj;
		validateAddressAssociated(ctx, address);
		
		super.remove(ctx, address);
	}

	
	/**
	 * validates account to be associated with address exists
	 * @param ctx
	 * @param address
	 * @throws HomeException
	 */
	private void validateAddressBAN(Context ctx, Address address) throws HomeException 
	{
		String ban = null;
		
		if(address == null)
		{
			LogSupport.minor(ctx, this, "Address request can not be null");
			throw new HomeException("Address request can not be null");
		}
		else 
		{
			ban = address.getBan();
			if(ban == null)
			{
				LogSupport.minor(ctx, this, "BAN can not be empty in request");
				throw new HomeException("BAN can not be empty in request");
			}
		}
	
		try 
		{
			Account account = HomeSupportHelper.get(ctx).findBean(ctx, Account.class, new EQ(AccountXInfo.BAN, ban));
			
			if(account == null)
			{
				LogSupport.minor(ctx, this, "No Account found for requested BAN :"+ban);
				throw new HomeException("No Account found for requested BAN :"+ban);
			}
		} 
		catch (HomeException e) 
		{
			LogSupport.minor(ctx, this, "Exception while retrieving account with BAN: "+ban+" : "+e.getMessage());
			throw new HomeException("Exception while retrieving account with BAN: "+ban+" : "+e.getMessage());
		}
	}

	/**
	 * validation that addressLine1 can not be empty in request
	 * @param ctx
	 * @param address
	 * @throws HomeException
	 */
	private void validateEmptyAddress(Context ctx, Address address)	throws HomeException 
	{
		if(address.getAddressLine1() == null || address.getAddressLine1().isEmpty())
		{
			LogSupport.minor(ctx, this, "Address can not be empty, providing at least Address Line1 is mandatory.");
			throw new HomeException("Address can not be empty, providing at least Address Line1 is mandatory.");
		}
	}
	
	
	/**
	 * Validates if address is already associated with any subscription
	 * 
	 * @param ctx
	 * @param address
	 * @throws HomeException 
	 */
	private void validateAddressAssociated(Context ctx, Address address) throws HomeException 
	{
		if (address != null && address.getAddressId() != null) 
		{
			Address fetchedAddress = fetchAddress(ctx, address);
			Collection<SupplementaryData> supplementaryDatas = fetchAssociatedAddress(ctx, fetchedAddress);
			if (supplementaryDatas != null && supplementaryDatas.size() > 0) 
			{
				StringBuilder stringBuilder = new StringBuilder();
				boolean firstDone = false;
				for (SupplementaryData supplementaryData : supplementaryDatas) 
				{
					if (firstDone) 
					{
						stringBuilder.append(", ");
					}

					if (supplementaryData.getIdentifier() != null) 
					{
						stringBuilder.append(supplementaryData.getIdentifier());
					}
					firstDone = true;
				}
				throw new HomeException("Address with addressId : " + address.getAddressId()
						+ " is already associated to " + stringBuilder.toString() + " subscription(s).");
			}
		}
	}
	
	/**
	 * Fetches {@link Address} from the database
	 * 
	 * @param ctx
	 * @param address
	 * @return {@link Address}
	 * @throws HomeException 
	 */
	private Address fetchAddress(Context ctx, Address apiAddress) throws HomeException 
	{
		Address address = null;
		if (apiAddress != null && apiAddress.getAddressId() != null) 
		{
			And andFilter = new And();
			andFilter.add(new EQ(AddressXInfo.ADDRESS_ID, apiAddress.getAddressId()));
			//andFilter.add(new EQ(AddressXInfo.BAN, apiAddress.getBan()));
			address = HomeSupportHelper.get(ctx).findBean(ctx, Address.class, andFilter);

			if (address == null) 
			{
				throw new HomeException(
						"No address record found with addressId: "+ apiAddress.getAddressId());
					//	+ "and BAN: "+ apiAddress.getBan());
			}
		}
		return address;
	}
	
	/**
	 * Fetches {@link SupplementaryData} from the database for entity=subscriber
	 * and input addressId
	 * 
	 * @param ctx
	 * @param address
	 * @return Collection<SupplementaryData>
	 * @throws HomeException 
	 */
	private Collection<SupplementaryData> fetchAssociatedAddress(Context ctx, Address address) throws HomeException 
	{
		try 
		{
			And andFilter = new And();
			andFilter.add(new EQ(SupplementaryDataXInfo.ENTITY, SupplementaryDataEntityEnum.SUBSCRIPTION_INDEX));
			andFilter.add(new EQ(SupplementaryDataXInfo.VALUE, address.getAddressId()));

			return HomeSupportHelper.get(ctx).getBeans(ctx, SupplementaryData.class, andFilter);
		} 
		catch (HomeException e) 
		{
			LogSupport.minor(ctx, this, "Exception while retrieving Subscriber Address with addressId="
					+address.getAddressId());
			throw new HomeException("Exception while retrieving Subscriber Address for addressId="+address.getAddressId(), e);
		}
	}

}
