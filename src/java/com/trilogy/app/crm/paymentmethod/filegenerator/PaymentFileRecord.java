package com.trilogy.app.crm.paymentmethod.filegenerator;

import java.util.ArrayList;
import java.util.List;

import com.trilogy.app.crm.bean.DirectDebitRecord;
import com.trilogy.app.crm.bean.FileFormatterConfig;
import com.trilogy.app.crm.bean.PaymentMethodFileGenerator;

public class PaymentFileRecord 
{
	private StringBuffer header;
	private StringBuffer trailer;
	private List<StringBuffer> dataRecordList;
	private StringBuffer endRecord;
	private List<StringBuffer> errorRecordList;
	
	private List<DirectDebitRecord> ddrList;
	private FileFormatterConfig fileFormatterConfig;
	private PaymentMethodFileGenerator paymentMethodFileGenerator;
	private Long numberOfRecords;
	private Long totalAmount;
	
	public StringBuffer getHeader() {
		return header;
	}
	
	public void setHeader(StringBuffer header) {
		this.header = header;
	}
	
	public StringBuffer getTrailer() {
		return trailer;
	}
	
	public void setTrailer(StringBuffer trailer) {
		this.trailer = trailer;
	}
	
	public List<StringBuffer> getDataRecordList() 
	{
		if (dataRecordList == null)
		{
			dataRecordList = new ArrayList<StringBuffer>();
		}
		return dataRecordList;
	}
	
	public void setDataRecordList(List<StringBuffer> dataRecordList) {
		this.dataRecordList = dataRecordList;
	}
	
	public StringBuffer getEndRecord() {
		return endRecord;
	}
	
	public void setEndRecord(StringBuffer endRecord) {
		this.endRecord = endRecord;
	}
	
	public List<StringBuffer> getErrorRecordList() 
	{
		if (errorRecordList == null)
		{
			errorRecordList = new ArrayList<StringBuffer>();
		}
		return errorRecordList;
	}
	
	public void setErrorRecordList(List<StringBuffer> errorRecordList) {
		this.errorRecordList = errorRecordList;
	}
	
	public List<DirectDebitRecord> getDdrList() 
	{
		if (ddrList == null)
		{
			ddrList = new ArrayList<DirectDebitRecord>();
		}
		return ddrList;
	}

	public FileFormatterConfig getFileFormatterConfig() {
		return fileFormatterConfig;
	}

	public void setFileFormatterConfig(FileFormatterConfig fileFormatterConfig) {
		this.fileFormatterConfig = fileFormatterConfig;
	}

	public void setPaymentMethodFileGenerator(PaymentMethodFileGenerator paymentMethodFileGenerator) {
		this.paymentMethodFileGenerator = paymentMethodFileGenerator;
	}

	public PaymentMethodFileGenerator getPaymentMethodFileGenerator() {
		return paymentMethodFileGenerator;
	}

	public Long getNumberOfRecords() {
		return numberOfRecords;
	}

	public void setNumberOfRecords(Long numberOfRecords) {
		this.numberOfRecords = numberOfRecords;
	}

	public Long getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(Long totalAmount) {
		this.totalAmount = totalAmount;
	}

	@Override
	public String toString() {
		return "PaymentFileRecord [header=" + header + ", trailer=" + trailer + ", dataRecordList=" + dataRecordList
				+ ", endRecord=" + endRecord + ", errorRecordList=" + errorRecordList + ", ddrList=" + ddrList
				+ ", fileFormatterConfig=" + fileFormatterConfig + ", paymentMethodFileGenerator="
				+ paymentMethodFileGenerator + ", numberOfRecords=" + numberOfRecords + ", totalAmount=" + totalAmount
				+ "]";
	}
	
	
}
