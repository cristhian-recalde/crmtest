package com.trilogy.app.crm.support;

import java.io.Serializable;
import java.util.Comparator;

import com.trilogy.app.crm.bean.DiscountEventActivity;

public class DiscountEventActivityComparator implements Comparator<DiscountEventActivity>, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public int compare(DiscountEventActivity o1, DiscountEventActivity o2) {
		// TODO Auto-generated method stub
		return o1.getSubId().compareTo(o2.getSubId());
	}

}
