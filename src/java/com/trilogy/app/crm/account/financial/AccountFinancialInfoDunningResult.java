package com.trilogy.app.crm.account.financial;

import com.trilogy.app.crm.bean.AgedDebt;

public class AccountFinancialInfoDunningResult {

	int number;
	private static AgedDebt agedDebt = null;
	private boolean isDebtOutstanding = false;

	public static AgedDebt getAgedDebt() {
		return agedDebt;
	}

	public static void setAgedDebt(AgedDebt agedDebt) {
		AccountFinancialInfoDunningResult.agedDebt = agedDebt;
	}

	public boolean isDebtOutstanding() {
		return isDebtOutstanding;
	}
	public void setDebtOutstanding(boolean isDebtOutstanding) {
		this.isDebtOutstanding = isDebtOutstanding;
	}


	public boolean getNumber() {
		if (number < 5) {
			return true;
		}
		return false;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (object == null || getClass() != object.getClass()) {
			return false;
		}
		if (!super.equals(object)) {
			return false;
		}

		AccountFinancialInfoDunningResult that = (AccountFinancialInfoDunningResult) object;

		if (isDebtOutstanding != that.isDebtOutstanding) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (isDebtOutstanding ? 1 : 0);
		return result;
	}
}
