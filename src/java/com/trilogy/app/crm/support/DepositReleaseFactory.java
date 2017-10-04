package com.trilogy.app.crm.support;

public class DepositReleaseFactory {

	public static ReleaseDeposit releaseDeposit(int optCode) {
		
		if (optCode == 3) {
			return new DepositCredit();
		} else if (optCode == 2) {
			return new DepositPayment();
		}
		
		throw new IllegalArgumentException("Invalid Option");
	}
}
