package com.trilogy.app.crm.account.financial;

import java.util.Date;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;

public class FinancialAccount {

	private Account account = null;
	private long pastDueAmount = 0;
	private Date paymentDueDate = null;
	private String paymentPlan = null;
	private long paymentPlanOutstandingAmount = 0;
	private String tax = null;
	private int debtAge = 0;
	private boolean isExemptFromDunning = false;
	private boolean papIndicator = false;
	private String invoiceDeliveryMethod = null;
	private String currentInvoice = null;
	private String nextInvoice = null;
	private GenericParameter[] responseParameter;
	private Date nextDunningLevelDate;

	
	
	public FinancialAccount(Account account) {
		this.account = account;
	}
	
	
	public Date getNextDunningLevelDate() {
		return nextDunningLevelDate;
	}

	public void setNextDunningLevelDate(Date nextDunningLevelDate) {
		this.nextDunningLevelDate = nextDunningLevelDate;
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public long getPastDueAmount() {
		return pastDueAmount;
	}

	public void setPastDueAmount(long pastDueAmount) {
		this.pastDueAmount = pastDueAmount;
	}

	public Date getPaymentDueDate() {
		return paymentDueDate;
	}

	public void setPaymentDueDate(Date paymentDueDate) {
		this.paymentDueDate = paymentDueDate;
	}

	public String getPaymentPlan() {
		return paymentPlan;
	}

	public void setPaymentPlan(String paymentPlan) {
		this.paymentPlan = paymentPlan;
	}

	public long getPaymentPlanOutstandingAmount() {
		return paymentPlanOutstandingAmount;
	}

	public void setPaymentPlanOutstandingAmount(
			long paymentPlanOutstandingAmount) {
		this.paymentPlanOutstandingAmount = paymentPlanOutstandingAmount;
	}

	public String getTax() {
		return tax;
	}

	public void setTax(String tax) {
		this.tax = tax;
	}

	public int getDebtAge() {
		return debtAge;
	}

	public void setDebtAge(int debtAge) {
		this.debtAge = debtAge;
	}

	public boolean isExemptFromDunning() {
		return isExemptFromDunning;
	}

	public void setExemptFromDunning(boolean isExemptFromDunning) {
		this.isExemptFromDunning = isExemptFromDunning;
	}

	public boolean isPapIndicator() {
		return papIndicator;
	}

	public void setPapIndicator(boolean papIndicator) {
		this.papIndicator = papIndicator;
	}

	public String getInvoiceDeliveryMethod() {
		return invoiceDeliveryMethod;
	}

	public void setInvoiceDeliveryMethod(String invoiceDeliveryMethod) {
		this.invoiceDeliveryMethod = invoiceDeliveryMethod;
	}

	public String getCurrentInvoice() {
		return currentInvoice;
	}
	
	public String getNextInvoice() {
		return nextInvoice;
	}

	public void setCurrentInvoice(String currentInvoice) {
		this.currentInvoice = currentInvoice;
	}
	public void setNextInvoice(String nextInvoice) {
		this.nextInvoice = nextInvoice;
	}

	public GenericParameter[] getResponseParameter() {
		return responseParameter;
	}

	public void setResponseParameter(GenericParameter[] responseParameter) {
		this.responseParameter = responseParameter;
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

		FinancialAccount that = (FinancialAccount) object;

		if (pastDueAmount != that.pastDueAmount) {
			return false;
		}
		if (paymentPlanOutstandingAmount != that.paymentPlanOutstandingAmount) {
			return false;
		}
		if (debtAge != that.debtAge) {
			return false;
		}
		if (isExemptFromDunning != that.isExemptFromDunning) {
			return false;
		}
		if (papIndicator != that.papIndicator) {
			return false;
		}
		if (account != null ? !account.equals(that.account) : that.account != null) {
			return false;
		}
		if (paymentDueDate != null ? !paymentDueDate.equals(that.paymentDueDate) : that.paymentDueDate != null) {
			return false;
		}
		if (paymentPlan != null ? !paymentPlan.equals(that.paymentPlan) : that.paymentPlan != null) {
			return false;
		}
		if (tax != null ? !tax.equals(that.tax) : that.tax != null) {
			return false;
		}
		if (invoiceDeliveryMethod != null ? !invoiceDeliveryMethod.equals(that.invoiceDeliveryMethod)
				: that.invoiceDeliveryMethod != null) {
			return false;
		}
		if (currentInvoice != null ? !currentInvoice.equals(that.currentInvoice) : that.currentInvoice != null) {
			return false;
		}
		if (nextInvoice != null ? !nextInvoice.equals(that.nextInvoice) : that.nextInvoice != null) {
			return false;
		}
		// Probably incorrect - comparing Object[] arrays with Arrays.equals
		if (!java.util.Arrays.equals(responseParameter, that.responseParameter)) {
			return false;
		}
		if (nextDunningLevelDate != null ? !nextDunningLevelDate.equals(that.nextDunningLevelDate)
				: that.nextDunningLevelDate != null) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (account != null ? account.hashCode() : 0);
		result = 31 * result + (int) (pastDueAmount ^ (pastDueAmount >>> 32));
		result = 31 * result + (paymentDueDate != null ? paymentDueDate.hashCode() : 0);
		result = 31 * result + (paymentPlan != null ? paymentPlan.hashCode() : 0);
		result = 31 * result + (int) (paymentPlanOutstandingAmount ^ (paymentPlanOutstandingAmount >>> 32));
		result = 31 * result + (tax != null ? tax.hashCode() : 0);
		result = 31 * result + debtAge;
		result = 31 * result + (isExemptFromDunning ? 1 : 0);
		result = 31 * result + (papIndicator ? 1 : 0);
		result = 31 * result + (invoiceDeliveryMethod != null ? invoiceDeliveryMethod.hashCode() : 0);
		result = 31 * result + (currentInvoice != null ? currentInvoice.hashCode() : 0);
		result = 31 * result + (nextInvoice != null ? nextInvoice.hashCode() : 0);
		result = 31 * result + Arrays.hashCode(responseParameter);
		result = 31 * result + (nextDunningLevelDate != null ? nextDunningLevelDate.hashCode() : 0);
		return result;
	}
}
