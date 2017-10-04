package com.trilogy.app.crm.support;

import java.io.Serializable;
import java.util.Comparator;

import com.trilogy.app.crm.bean.core.Transaction;

public class DiscountEventTransactionComparator implements Comparator<Transaction>, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public int compare(Transaction o1, Transaction o2) {
		// TODO Auto-generated method stub
		
		return (o1.getReceiveDate().compareTo(o2.getReceiveDate()));
	}

}
