/**
    SIMBulkLoader

    @Author : Lanny Tse
    Date   : Oct 20, 2003

    Copyright (c) Redknee, 2002
        - all rights reserved
**/

package com.trilogy.app.crm.bulkloader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.StringTokenizer;

import com.trilogy.app.crm.bean.GSMPackage;
import com.trilogy.app.crm.bean.GSMPackageHome;
import com.trilogy.app.crm.bean.PackageStateEnum;
import com.trilogy.app.crm.bean.TDMAPackage;
import com.trilogy.app.crm.bean.TDMAPackageHome;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xlog.log.EntryLogSupport;


/**
 * SIMBulkLoader - parse the file and load the SIM into the SIM table
 *
 */
public class IbisGSMSIMBulkLoader extends Thread implements ContextAware
{

	
	
	public IbisGSMSIMBulkLoader(Context ctx,
		String fileName, int spid, String packageGroup, String dealerCode,  int packageState, String batchId)
	{
		try
		{
            spid_ = spid;
            packageGroup_ = packageGroup;
            dealerCode_ =   dealerCode;
            packageState_ = packageState;
            batchId_ = batchId;

            setContext(ctx);
			log_ = new EntryLogSupport(ctx, this.getClass().getName());

			in_ = new BufferedReader(
				new FileReader(fileName));

			out_ = new PrintWriter(new BufferedWriter(new FileWriter(
			   	fileName + ERR_EXT)));
			generateErrorHeader();

			setBulkLoadFile(fileName);


		}
		catch(Exception e)
		{
			log_.info("Fail to init SIMBulkLoader",e);
			closeReader();
			closeWriter();
		}
	}

	public void generateErrorHeader()
	{
    	//Multilingual support added by skrishnan
    	final MessageMgr mmgr = new MessageMgr(ContextLocator.locate(), this);

		out_.println("*************");
		out_.println("*  "+mmgr.get("Header.Error","Errors")+"   *");
		//out_.println("*  Errors   *");
		out_.println("*************");
	}

	public void closeWriter()
	{
		if (out_ != null)
		{
			try
			{
				out_.close();
			}
			catch(Exception e)
			{
				//ignore
			}
		}
	}

	public void closeReader()
	{
		if (in_ !=  null)
		{
			try {
				in_.close();
			} catch (IOException e)
			{
				//ignore
			}
		}
	}

	public void writeError(String line, int errorCode)
	{
		final MessageMgr mmgr = new MessageMgr(ContextLocator.locate(), this);
		String errorMessage = "";
		switch(errorCode)
		{
		case(0):
			errorMessage=mmgr.get("Not_Enough_Parameter.Error","Not enough parameter");
		
			//errorMessage="Not enough parameter";
			break;
		
		case(1):
			errorMessage=mmgr.get("Parse.Error","Parse Error");
			break;
		case(2):
			errorMessage=mmgr.get("Database.Error","Database Error");
			break;
		case(3):
			errorMessage=mmgr.get("Unknown.Error","Unknown Error");
			break;
		case(4):
			errorMessage=mmgr.get("SP_Not_Found.Error","Service Provider Not Found");
			break;	
		case(5):
			errorMessage=mmgr.get("Incorrect_Format.Error","Incorrect Format");
		    break;
			
				
		}
		out_.println(line + DELIMITER + errorCode + DELIMITER + errorMessage);

	}

	public void run()
	{

		try
		{
			String line = null;
			int count = 0;

			while ((line = in_.readLine()) != null)
			{
				count++;
				if(count<=35)
					continue;//neglecting first 35 lines as those lines contain header data
					createGSMPackage(line, count); // support GSM only
			}

		}
		catch (Exception ee)
		{
			log_.info("Error when running SIM bulk loader for file " + getBulkLoadFile(), ee);
		}
		finally
		{
			closeReader();
			closeWriter();
		}

	}
	
	
	public void createGSMPackage(String line, int count){
		StringTokenizer tokenizer = null;
		boolean startRead = false;
		GSMPackage simCard = new GSMPackage();
		Home simCardHome = (Home)getContext().get(GSMPackageHome.class);

		
		try
		{
			tokenizer = new StringTokenizer(line, DELIMITER);

			if (log_.isDebugEnabled())
			{
				log_.debug("Reading line <" + line + "> with tokens " + tokenizer.countTokens());
			}

			if (tokenizer.countTokens() < 10)
			{
                if (Character.isDigit(line.trim().charAt(0)))
                {
                    startRead = true;
                }
				if (startRead)
				{
					writeError(line, INCORRECT_FORMAT);
				}
				return;

			}
			startRead = true;

			simCard.setIMSI(tokenizer.nextToken());
			simCard.setSerialNo(tokenizer.nextToken());
			simCard.setPIN1(tokenizer.nextToken());
			simCard.setPUK1(tokenizer.nextToken());
			simCard.setPIN2(tokenizer.nextToken());
			simCard.setPUK2(tokenizer.nextToken());
			simCard.setADM1(tokenizer.nextToken());
			simCard.setKI(tokenizer.nextToken());
			simCard.setKAPPLI(tokenizer.nextToken());
			simCard.setPackId(tokenizer.nextToken().trim());
            simCard.setSpid(spid_);
            simCard.setBatchId(batchId_);
            simCard.setPackageGroup(packageGroup_);
            simCard.setDealer(dealerCode_);
            simCard.setState(PackageStateEnum.get((short)packageState_));

            simCardHome.create(simCard);


		}
		catch (IllegalArgumentException iae)
		{
			writeError(line, PARSE_ERROR);
			log_.info(getBulkLoadFile() + ":" + count, iae);
		}
		catch(HomeException he)
		{
			writeError(line, DB_ERROR);
			log_.info(getBulkLoadFile() + ":" + count, he);
		}
		catch (Throwable e)
		{
			writeError(line, UNKNOWN_ERROR);
			log_.info(getBulkLoadFile() + ":" + count, e);
		}
		
		
	}

	
	/**
	 * Returns the bulkLoadFile_.
	 * @return String
	 */
	public String getBulkLoadFile() {
		return bulkLoadFile_;
	}

	/**
	 * Sets the bulkdLoadFile_.
	 * @param bulkLoadFile The bulkdLoadFile_ to set
	 */
	public void setBulkLoadFile(String bulkLoadFile) {
		this.bulkLoadFile_ = bulkLoadFile;
	}

	/**
	 * Returns the context_.
	 * @return Context
	 */
	public Context getContext() {
		return context_;
	}

	/**
	 * Sets the context_.
	 * @param context The context_ to set
	 */
	public void setContext(Context context) {
		this.context_ = context;
	}
	
	public static final String DELIMITER = "   ";
    public static final String ERR_EXT = ".err";
    
    public static final int NOT_ENOUGH_PARAMS = 0;
    public static final int PARSE_ERROR = 1;
    public static final int DB_ERROR = 2;
    public static final int UNKNOWN_ERROR = 3;
    public static final int SP_NOT_FOUND = 4;
    public static final int INCORRECT_FORMAT = 5;

    String bulkLoadFile_ = null;
    BufferedReader in_ = null;
    PrintWriter out_ = null;
    Context context_ = null;
    EntryLogSupport log_ = null;
    int spid_;
    int packageState_;
    String packageGroup_ = null;
    String dealerCode_ = null;
    String batchId_ = null;


}

