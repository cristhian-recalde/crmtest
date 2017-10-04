package com.trilogy.app.crm.hlr.bulkload;

import java.util.Random;

import com.trilogy.app.crm.provision.CommonProvisionAgentBase;


public class DorySimcardInput 
extends AbstractSimCardInput
{
	
	public DorySimcardInput(String line)
	{
		this.rawline = line;
	}
	
	public String getEki() {
		return eki;
	}
	public void setEki(final String eki) {
		this.eki = eki;
	}


	public String mapToCommand(String cmd)
	{
		
		if (this.imsi != null)
		{
		    cmd  = cmd.replaceAll(IMSI_VARIABLE, this.imsi);
		}
		    
		if ( this.eki != null)
		{
		     cmd  = cmd.replaceAll(EKI_VARIABLE, this.eki);
		}

		    
		return cmd; 
	}
	
	
	public void parse() throws InvalidInputException
	{
		//Definition: IMSI,x,y,Ki,z
		//12345678,x,y,ABCD1234,z;
		
		if(rawline != null)
		{
			String[] result = rawline.split(DELIM);
			
			if (result.length < 4)
			{
				throw new InvalidInputException("invalide simcard bulk load record" , rawline); 
			}
			
			this.imsi = result[POS_IMSI].trim();
			this.eki = result[POS_EKI].trim();
			
		} else 
		{
			throw new InvalidInputException("invalide simcard bulk load record, input is empty", rawline);
		}

		
	}
	
	
	String eki=""; 
	static final String DELIM = ",";
	static final int POS_IMSI =0;
	static final int POS_EKI = 3; 
	
	static final String IMSI_VARIABLE = "%IMSI%";
	static final String EKI_VARIABLE= "%EKI%";
}
