package com.trilogy.app.crm.bean.paymentgatewayintegration;

import java.util.Comparator;

import com.trilogy.app.crm.bean.*;

public class MsisdnPrefixDescendingComparator implements Comparator<CreditCardPrefixRateMap> {

	@Override
	public int compare(CreditCardPrefixRateMap mapping1, CreditCardPrefixRateMap mapping2) {
		
		return -( mapping1.getMsisdnPrefix().compareTo(mapping2.getMsisdnPrefix()) );
	}	
}
