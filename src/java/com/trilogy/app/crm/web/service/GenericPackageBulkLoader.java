/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */

package com.trilogy.app.crm.web.service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import com.trilogy.framework.license.LicenseMgr;
import com.trilogy.framework.xhome.beans.FieldValueTooLongException;
import com.trilogy.framework.xhome.beans.MissingRequireValueException;
import com.trilogy.framework.xhome.beans.NullValueException;
import com.trilogy.framework.xhome.beans.PatternMismatchException;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xlog.log.EntryLogSupport;
import com.trilogy.framework.xlog.log.PPMLogMsg;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.bean.DealerCode;
import com.trilogy.app.crm.bean.DealerCodeHome;
import com.trilogy.app.crm.bean.DealerCodeXInfo;
import com.trilogy.app.crm.bean.GSMPackage;
import com.trilogy.app.crm.bean.GSMPackageHome;
import com.trilogy.app.crm.bean.GSMPackageXInfo;
import com.trilogy.app.crm.bean.GenericPackageImport;
import com.trilogy.app.crm.bean.PackageGroup;
import com.trilogy.app.crm.bean.PackageGroupHome;
import com.trilogy.app.crm.bean.PackageGroupXInfo;
import com.trilogy.app.crm.bean.PackageStateEnum;
import com.trilogy.app.crm.bean.PackageType;
import com.trilogy.app.crm.bean.PackageTypeHome;
import com.trilogy.app.crm.bean.PackageTypeXInfo;
import com.trilogy.app.crm.bean.PackageXInfo;
import com.trilogy.app.crm.bean.TDMAPackage;
import com.trilogy.app.crm.bean.TDMAPackageHome;
import com.trilogy.app.crm.bean.TDMAPackageXInfo;
import com.trilogy.app.crm.bean.VSATPackage;
import com.trilogy.app.crm.bean.VSATPackageHome;
import com.trilogy.app.crm.exception.SpidNotFoundException;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.technology.TechnologyEnum;

/**
 * Load the Packages from the files provided for Bulk Load
 * 
 * @author deepak.mishra@redknee.com
 */
public class GenericPackageBulkLoader extends Thread implements ContextAgent,
    ContextAware
{

	/**
	 * Name of the Bulk Load File.
	 */
	private String bulkLoadFile_ = null;
	private PrintWriter out_ = null;
	private Context context_ = null;
	private EntryLogSupport log_ = null;
	private final String batchID_;

	private boolean gsmLicensed_ = false;
	private boolean tdmaLicensed_ = false;
	private boolean vsatLicensed_ = false;

	private static final char DELIMITER = ',';
	private static final String ERR_EXT = ".err";
	private static final String LEFT_BRACE = "(" ;
	private static final String RIGHT_BRACE =")" ;
	private static final String COLON = ":";

	/**
	 * Error Codes.
	 */
	public static final int NOT_ENOUGH_PARAMS = 0;
	public static final int PARSE_ERROR = 1;
	public static final int DB_ERROR = 2;
	public static final int UNKNOWN_ERROR = 3;
	public static final int WRONG_TECHNOLOGY = 4;
	public static final int LICENSE_NOT_AVAILABLE = 5;
	public static final int INTERNAL_ERROR = 6;
	
	/**
	 * Parse Error codes
	 */
	public static final int NOT_EXIST_IN_BSS_ERROR = 101;
	public static final int FIELD_TYPE_MISMATCH_ERROR = 102;
	public static final int INVALID_FIELD_FORMAT_ERROR = 103;
	public static final int MISSING_MANDATORY_VALUE_ERROR = 104;
	public static final int FIELD_VALUE_TOO_LONG_ERROR = 105;
	public static final int NULL_VALUE_ERROR = 106;
	public static final int INVALID_PACKAGE_STATE = 107;
	

	/**
	 * Constructor to initialize the BulkLoader
	 * 
	 * @param ctx
	 *            Context
	 * @param fileName
	 *            name of the Bulk load File
	 */
	public GenericPackageBulkLoader(Context ctx, String fileName, String batchID)
	{
        batchID_ = batchID;
        setContext(ctx);
        setBulkLoadFile(fileName);
        log_ = new EntryLogSupport(ctx, this.getClass().getName());
        try
		{	
			out_ =
			    new PrintWriter(new BufferedWriter(new FileWriter(fileName
			        + ERR_EXT)));
			generateErrorHeader();
		}
		catch (Exception e)
		{
			log_.info("Fail to init PackageBulkLoader", e);
			closeWriter();
		}
	}

	/**
	 * Prints Error Header.
	 */
	public void generateErrorHeader()
	{
		out_.println("*************");
		out_.println("*  Errors   *");
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
			catch (Exception e)
			{
				// ignore
			}
		}
	}

	public void writeError(final Object importedPackage, int errorCode)
	{
		final StringBuffer buffer = new StringBuffer();
		buffer.append(importedPackage.toString());
		buffer.append(DELIMITER);
		buffer.append(errorCode);
		out_.println(buffer.toString());
	}

	public void writeError(String line, int errorCode)
	{
		out_.println(line + DELIMITER + errorCode);
	}
	
	/**
	 * This method will handle all parse errors and put this info into *.err file.
	 */
	public void writeError(final Object importedPackage, int errorCode, final Exception e)
	{
		final StringBuilder builder = new StringBuilder();
		builder.append(importedPackage.toString());
		builder.append(DELIMITER);
		builder.append(errorCode);
		
		if(e instanceof MissingRequireValueException){
			builder.append(LEFT_BRACE+((MissingRequireValueException) e).getPropertyName()+ COLON + MISSING_MANDATORY_VALUE_ERROR + RIGHT_BRACE);
		}else if(e instanceof FieldValueTooLongException){
			builder.append(LEFT_BRACE+((FieldValueTooLongException) e).getPropertyName()+ COLON + FIELD_VALUE_TOO_LONG_ERROR + RIGHT_BRACE);
		}else if(e instanceof NumberFormatException){
			builder.append(LEFT_BRACE+((NumberFormatException)e).getMessage()+ COLON + FIELD_TYPE_MISMATCH_ERROR + RIGHT_BRACE);
		}else if(e instanceof PatternMismatchException){
			builder.append(LEFT_BRACE+((PatternMismatchException)e).getPropertyName()+ COLON + INVALID_FIELD_FORMAT_ERROR+ RIGHT_BRACE );
		}else if(e instanceof NullValueException){
			builder.append(LEFT_BRACE+((NullValueException)e).getPropertyName()+ COLON + NULL_VALUE_ERROR + RIGHT_BRACE);
		}else if (e instanceof SpidNotFoundException){
			builder.append(LEFT_BRACE + ((SpidNotFoundException)e).getPropertyName() + COLON + NOT_EXIST_IN_BSS_ERROR + RIGHT_BRACE);
		}else if(e instanceof IllegalArgumentException){
			builder.append(e.getMessage());
		}else {
			writeError(importedPackage, errorCode);
			return;
		}
		out_.println(builder.toString());
	}

	/*
	 * Avoid using it this; the thread model was just preserved for backward
	 * compatibility
	 * with (existing loader request services) Use an external thread model that
	 * runs the
	 * execute(ctx)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run()
	{
		try
		{
			execute(getContext());
		}
		catch (AgentException e)
		{
			// TODO Auto-generated catch block
			log_.major(
			    getBulkLoadFile() + ": Bulkload Operation Failed [ "
			        + e.getMessage() + "]", e);
		}
	}

	/**
	 * Returns the bulkLoadFile_.
	 * 
	 * @return String
	 */
	public String getBulkLoadFile()
	{
		return bulkLoadFile_;
	}

	/**
	 * Sets the bulkdLoadFile_.
	 * 
	 * @param bulkLoadFile
	 *            The bulkdLoadFile_ to set
	 */
	public void setBulkLoadFile(String bulkLoadFile)
	{
		this.bulkLoadFile_ = bulkLoadFile;
	}

	/**
	 * Returns the context_.
	 * 
	 * @return Context
	 */
	@Override
	public Context getContext()
	{
		return context_;
	}

	/**
	 * Sets the context_.
	 * 
	 * @param context
	 *            The context_ to set
	 */
	@Override
	public void setContext(Context context)
	{
		this.context_ = context;
	}

	protected void createGSMPackage(Context ctx, Object obj, int count)
	{
		if (obj instanceof GenericPackageImport)
		{
			createGSMPackage(ctx, (GenericPackageImport) obj, count);
		}
		else
		{
			writeError(obj, INTERNAL_ERROR);
			log_.info(getBulkLoadFile() + ":" + count + " Invalid class type",
			    null);
		}
	}

	/**
	 * @param importedPackage
	 *            - entry which is to be inserted in GSMPackage table
	 * @param count
	 *            -- record number
	 */
	protected void createGSMPackage(Context ctx,
	    GenericPackageImport importedPackage, int count)
	{
		try
		{
			GSMPackage pack =
			    (GSMPackage) XBeans.instantiate(GSMPackage.class, ctx);
			fillInCommonPackageProperties(ctx, pack, importedPackage, TechnologyEnum.GSM);
			if (null == importedPackage.getMinOrIMSI() || importedPackage.getMinOrIMSI().isEmpty() )
			{
				throw new MissingRequireValueException(GSMPackageXInfo.IMSI, "IMSI NOT PROVIDED");
			}
			pack.setIMSI(importedPackage.getMinOrIMSI());
			pack.setIMSI1(importedPackage.getMinOrIMSI1());
			pack.setIMSI2(importedPackage.getMinOrIMSI2());
			pack.setSerialNo(importedPackage.getSerialNo());
			pack.setPIN1(importedPackage.getPIN1OrSubsidyKey());
			pack.setPUK1(importedPackage.getPUK1OrMassSubsidyKey());
			pack.setPIN2(importedPackage.getPIN2());
			pack.setPUK2(importedPackage.getPUK2());
			pack.setADM1(importedPackage.getADM1());
			pack.setKI(importedPackage.getKIorAuthKey());
			pack.setKAPPLI(importedPackage.getKAPPLI());
			pack.setTechnology(TechnologyEnum.GSM);
			pack.setServiceLogin1(importedPackage.getAAALogin());
			pack.setServicePassword1(importedPackage.getAAAPwd());
			pack.setServiceLogin2(importedPackage.getNAAAALogin());
			pack.setServicePassword2(importedPackage.getNAAAAPwd());
			final Home packageHome = (Home) ctx.get(GSMPackageHome.class);
			packageHome.create(ctx, pack);
		}
		catch (IllegalArgumentException iae)
		{
			writeError(importedPackage, PARSE_ERROR, iae);
			log_.info(getBulkLoadFile() + COLON + count, iae);
		}
		catch (HomeException he)
		{
			writeError(importedPackage, DB_ERROR);
			log_.info(getBulkLoadFile() + COLON + count, he);
		}
		catch (Throwable e)
		{
			writeError(importedPackage, UNKNOWN_ERROR);
			log_.info(getBulkLoadFile() + COLON + count, e);
		}
	}

	protected void createTDMACDMAPackage(Context ctx, Object obj,
	    TechnologyEnum tech, int count)
	{
		if (obj instanceof GenericPackageImport)
		{
			createTDMACDMAPackage(ctx, (GenericPackageImport) obj, tech, count);
		}
		else
		{
			writeError(obj, INTERNAL_ERROR);
			log_.info(getBulkLoadFile() + COLON + count + " Invalid class type",
			    null);
		}
	}

	/**
	 * @param importedPackage
	 *            Entry which has to be inserted in TDMAPackage Table
	 * @param tech
	 *            -- CDMA/TDMA Technology Type.
	 * @param count
	 *            -- record no.
	 */
	protected void createTDMACDMAPackage(Context ctx,
	    final GenericPackageImport importedPackage, final TechnologyEnum tech,
	    final int count)
	{
		try
		{
			TDMAPackage pack =
			    (TDMAPackage) XBeans.instantiate(TDMAPackage.class, ctx);
			fillInCommonPackageProperties(ctx, pack, importedPackage, tech);
			if (null == importedPackage.getMinOrIMSI() || importedPackage.getMinOrIMSI().isEmpty() )
			{
				throw new MissingRequireValueException(TDMAPackageXInfo.MIN, "MIN NOT PROVIDED");
			}
			pack.setMin(importedPackage.getMinOrIMSI());
			pack.setESN(importedPackage.getESN());
			pack.setSerialNo(importedPackage.getSerialNo());
			pack.setSubsidyKey(importedPackage.getPIN1OrSubsidyKey());
			pack.setMassSubsidyKey(importedPackage.getPUK1OrMassSubsidyKey());
			pack.setAuthKey(importedPackage.getKIorAuthKey());
			pack.setTechnology(tech);
			pack.setServiceLogin1(importedPackage.getAAALogin());
			pack.setServicePassword1(importedPackage.getAAAPwd());
			pack.setServiceLogin2(importedPackage.getNAAAALogin());
			pack.setServicePassword2(importedPackage.getNAAAAPwd());
			pack.setCallbackID(importedPackage.getCallbackID());
			pack.setRadiusProfileName(importedPackage.getRadiusProfileName());
			
			/**
			 * TT# 13061234060
			 * For CDMA, Validate if packageType exists or not, if not exists then set it default.
			 * For TDMA, packageType will always be set to default 0.
			 */
			if (TechnologyEnum.CDMA.equals(tech) && isPackageTypeExists(ctx, importedPackage.getPackageType(), importedPackage.getSpid())){
				pack.setPackageType(importedPackage.getPackageType());
			} else {
				pack.setPackageType(TDMAPackage.DEFAULT_PACKAGETYPE);
			}
			
			pack.setExternalMSID(importedPackage.getExternalMSID());
			pack.setCustomerOwned(importedPackage.getCustomerOwned());
			pack.setDescription(importedPackage.getDescription());
			
			final Home packageHome = (Home) ctx.get(TDMAPackageHome.class);
			packageHome.create(ctx, pack);
		}
		catch (IllegalArgumentException iae)
		{
			writeError(importedPackage, PARSE_ERROR ,iae);
			log_.info(getBulkLoadFile() + ":" + count, iae);
		}
		catch (HomeException he)
		{
			writeError(importedPackage, DB_ERROR);
			log_.info(getBulkLoadFile() + ":" + count, he);
		}
		catch (Throwable e)
		{
			writeError(importedPackage, UNKNOWN_ERROR);
			log_.info(getBulkLoadFile() + ":" + count, e);
		}
	}

	protected void createVSATPackage(Context ctx, Object obj,int count)
	{
		if (obj instanceof GenericPackageImport)
		{
			createVSATPackage(ctx, (GenericPackageImport) obj, count);
		}
		else
		{
			writeError(obj, INTERNAL_ERROR);
			log_.info(getBulkLoadFile() + ":" + count + " Invalid class type",
			    null);
		}
	}

	/**
	 * @param importedPackage
	 *            Entry which has to be inserted in TDMAPackage Table
	 * @param tech
	 *            -- CDMA/TDMA Technology Type.
	 * @param count
	 *            -- record no.
	 */
	protected void createVSATPackage(Context ctx,
	    final GenericPackageImport importedPackage,
	    final int count)
	{
		try
		{
			VSATPackage pack =
			    (VSATPackage) XBeans.instantiate(VSATPackage.class, ctx);
			fillInCommonPackageProperties(ctx, pack, importedPackage, TechnologyEnum.VSAT_PSTN);
			pack.setVsatId((importedPackage.getVsatId()));

			// pack.setSerialNo(importedPackage.getSerialNo());
			pack.setChannel(importedPackage.getChannel());
			pack.setPort(importedPackage.getPort());
			
			
			pack.setTechnology(TechnologyEnum.VSAT_PSTN);
			final Home packageHome = (Home) ctx.get(VSATPackageHome.class);
			packageHome.create(ctx, pack);
		}
		catch (IllegalArgumentException iae)
		{
			writeError(importedPackage, PARSE_ERROR, iae);
			log_.info(getBulkLoadFile() + ":" + count, iae);
		}
		catch (HomeException he)
		{
			writeError(importedPackage, DB_ERROR);
			log_.info(getBulkLoadFile() + ":" + count, he);
		}
		catch (Throwable e)
		{
			writeError(importedPackage, UNKNOWN_ERROR);
			log_.info(getBulkLoadFile() + ":" + count, e);
		}
	}
	
	private void fillInCommonPackageProperties(Context ctx, final com.redknee.app.crm.bean.Package toPackage, final GenericPackageImport importedPackage, final TechnologyEnum tech) throws IllegalArgumentException, HomeException
    {
		if (null == importedPackage.getSpid() || importedPackage.getSpid().isEmpty() )
        {
            throw new MissingRequireValueException(PackageXInfo.SPID, "SPID NOT PROVIDED.");
        }
        try
        {
        	int spid = Integer.parseInt(importedPackage.getSpid());
        	if(!isSpidExist(ctx, spid)){
            	throw new SpidNotFoundException(PackageXInfo.SPID, "SPID NOT FOUND");
            }
        	toPackage.setSpid(spid);
        	
        }catch(NumberFormatException nfe){
        	throw new NumberFormatException("Invalid " + PackageXInfo.SPID);
        }
        
        toPackage.setPackId(importedPackage.getPackId());
		if (null == importedPackage.getPackId()
				|| importedPackage.getPackId().isEmpty()) {
			throw new MissingRequireValueException(PackageXInfo.PACK_ID,
					"PackageId is Empty");
		}
		
		toPackage.setPackageGroup(importedPackage.getPackGroup());
		if (!isPackageGroupExists(ctx, importedPackage.getPackGroup(),
				toPackage.getSpid(), tech)) {
			throw new IllegalArgumentException(LEFT_BRACE
					+ PackageXInfo.PACKAGE_GROUP + COLON
					+ NOT_EXIST_IN_BSS_ERROR + RIGHT_BRACE);
		}
        
		toPackage.setDealer(importedPackage.getDealerCode());
		if (!isDealerCodeExists(ctx, importedPackage.getDealerCode(),
				toPackage.getSpid())) {
			throw new IllegalArgumentException(LEFT_BRACE + PackageXInfo.DEALER
					+ COLON + NOT_EXIST_IN_BSS_ERROR + RIGHT_BRACE);
		}
		
        toPackage.setBatchId(batchID_);
        toPackage.setState(getPackageState(importedPackage.getState()));
        toPackage.setTechnology(tech);
       
        toPackage.setDefaultResourceID(importedPackage.getDefaultResource());
        toPackage.setLastModified(new Date());
        
    }

	/**
	 * @param ctx 
	 * @param spid
	 * @return true if SPID exist in BSS, false otherwise
	 * @throws HomeException 
	 */
	private boolean isSpidExist(Context ctx, int spid) throws HomeException {
		
		return (null != SpidSupport.getCRMSpid(ctx, spid));
	}

	/**
	 * @param state
	 *            -- Package state string value
	 * @return mapping of Package state string value to corresponding
	 *         PackageStateEnum
	 *         value.
	 */
	private PackageStateEnum getPackageState(String state) throws IllegalArgumentException
	{
		PackageStateEnum packageState = null;
		if (state.trim().equals("") || state.equals("1"))
		{
			packageState = PackageStateEnum.AVAILABLE;
		}
		else if (state.equals("2"))
		{
			packageState = PackageStateEnum.IN_USE;
		}
		else if (state.equals("3"))
		{
			packageState = PackageStateEnum.HELD;
		}
		else
		{
		    throw new IllegalArgumentException(LEFT_BRACE + PackageXInfo.STATE + COLON + INVALID_PACKAGE_STATE + RIGHT_BRACE);
		}
		// wrong value is provided
		return packageState;
	}

	private boolean isPackageGroupExists(Context ctx, String packageGroupName,
	    int spid, TechnologyEnum tech) throws HomeException
	{
		final And condition = new And();
		condition.add(new EQ(PackageGroupXInfo.SPID, Integer.valueOf(spid)));
		condition.add(new EQ(PackageGroupXInfo.TECHNOLOGY, tech));
		Home packageGroupHome = (Home) ctx.get(PackageGroupHome.class);
		Home filteredHome = packageGroupHome.where(ctx, condition);
		PackageGroup pkgGroup = new PackageGroup();
		pkgGroup.setName(packageGroupName);
		pkgGroup = (PackageGroup) filteredHome.find(ctx, pkgGroup);
		return pkgGroup != null;
	}

	private boolean
	    isDealerCodeExists(Context ctx, String dealerCode, int spid)
	        throws HomeException
	{
		Home dealerCodeHome = (Home) ctx.get(DealerCodeHome.class);
		
		And and = new And();
        and.add(new EQ(DealerCodeXInfo.CODE, dealerCode));
        and.add(new EQ(DealerCodeXInfo.SPID, Integer.valueOf(spid)));
		
        DealerCode dealerCodeBean = (DealerCode) dealerCodeHome.find(ctx, and);
		return dealerCodeBean != null;
	}
	
	/**
	 * This method will look for packageType exists in BSS or not.
	 * Applicable only for CDMA Technology
	 * @param ctx
	 * @param packageType
	 * @param spid
	 * @return
	 * @throws HomeException 
	 * @throws HomeInternalException 
	 */
	private boolean isPackageTypeExists(Context ctx, int packageType, String spid) throws HomeInternalException, HomeException {
		final And condition = new And ();
		condition.add(new EQ(PackageGroupXInfo.SPID, Integer.valueOf(spid)));
		
		Home packageTypeHome = ((Home)ctx.get(PackageTypeHome.class)).where(ctx, new EQ (
				PackageTypeXInfo.SPID, Integer.valueOf(spid)));
		PackageType packageTypeBean = new PackageType();
		packageTypeBean.setId(packageType);
		packageTypeBean.setSpid(Integer.valueOf(spid));
		packageTypeBean = (PackageType) packageTypeHome.find(ctx, packageTypeBean);
		
		return packageTypeBean != null;
	}

	protected Home createImportHome(Context ctx) throws HomeException
	{
		return new CustomGenericPackageImportCSVHome(ctx, getBulkLoadFile());
	}

	protected int getPackageTechnology(Object pkg)
	{
		int technology = -1;
		if (pkg instanceof GenericPackageImport)
		{
			GenericPackageImport importedPackage = (GenericPackageImport) pkg;
			if (importedPackage.getTechnology() != null)
				try
				{
			        if (null == importedPackage.getTechnology() || importedPackage.getTechnology().isEmpty() )
			        {
			            throw new MissingRequireValueException(PackageXInfo.TECHNOLOGY, "Technology TYPE NOT PROVIDED");
			        }
					technology =
					    Integer.parseInt(importedPackage.getTechnology());
				}
				catch (NumberFormatException ex)
				{
					log_.info("Technology \"" + importedPackage.getTechnology()
					    + "\" is not a valid number");
					throw new NumberFormatException(PackageXInfo.TECHNOLOGY.getName());
				}
		}

		return technology;
	}

	protected void updateLicenses(Context ctx)
	{
		final LicenseMgr lMgr = (LicenseMgr) ctx.get(LicenseMgr.class);
		if (lMgr == null)
		{
			gsmLicensed_ = false;
			tdmaLicensed_ = false;
			vsatLicensed_ = false;
		}
		else
		{
			gsmLicensed_ =
			    lMgr.isLicensed(ctx, LicenseConstants.GSM_LICENSE_KEY);
			tdmaLicensed_ =
			    lMgr.isLicensed(ctx, LicenseConstants.TDMA_CDMA_LICENSE_KEY);
			vsatLicensed_ =
			    lMgr.isLicensed(ctx, LicenseConstants.VSAT_PSTN_LICENSE_KEY);
		}
	}

	/**
	 * Prepares the importHome with entries in bulk load file and processes
	 * entries
	 * depending upon the type of package.
	 */
	@Override
	public void execute(Context ctx) throws AgentException
	{
		try
		{
			updateLicenses(ctx);
			final Home importHome = createImportHome(ctx);
			@SuppressWarnings("rawtypes")
            final Collection imports = importHome.selectAll();
			final int total = imports.size();
			int count = 0;

			if (imports != null && imports.size() <= 0)
			{
				writeError(
				    "Error while Parsing the CSV file, Please look into logs for any GSMPackageImport exceptions",
				    PARSE_ERROR);
			}

			for (@SuppressWarnings("rawtypes")
            final Iterator importIterator = imports.iterator(); importIterator
			    .hasNext();)
			{
				final Object importedPackage = importIterator.next();
				try
                {
                    count++; 
                    int packagetype = getPackageTechnology(importedPackage);
                    boolean licenseNotAvailable = false;
                    switch (packagetype)
                    {
                    	// GSM
                    	case TechnologyEnum.GSM_INDEX: // Check if Technology
                    		                           // specific license is
                    		                           // enabled
                    		if (!gsmLicensed_)
                    		{
                    			licenseNotAvailable = true;
                    		}
                    		else
                    		{
                    			createGSMPackage(ctx, importedPackage, count);
                    		}
                    		break;
                    	// TDMA
                    	case TechnologyEnum.TDMA_INDEX: // Check if Technology
                    		                            // specific license is
                    		                            // enabled
                    		if (!tdmaLicensed_)
                    		{
                    			licenseNotAvailable = true;
                    		}
                    		else
                    		{
                    			createTDMACDMAPackage(ctx, importedPackage,
                    			    TechnologyEnum.TDMA, count);
                    		}
                    		break;
                    	// CDMA
                    	case TechnologyEnum.CDMA_INDEX: // Check if Technology
                    		                            // specific license is
                    		                            // enabled
                    		if (!tdmaLicensed_)
                    		{
                    			licenseNotAvailable = true;
                    		}
                    		else
                    		{
                    			createTDMACDMAPackage(ctx, importedPackage,
                    			    TechnologyEnum.CDMA, count);
                    		}
                    		break;
                    	case TechnologyEnum.VSAT_PSTN_INDEX:
                    		// Check if Technology specific license is enabled
                    		if (!vsatLicensed_)
                    		{
                    			licenseNotAvailable = true;
                    		}
                    		else
                    		{
                    			createVSATPackage(ctx, importedPackage, count);
                    		}
                    		break;
      
                    	default:
                    		writeError(importedPackage, WRONG_TECHNOLOGY);
                    		log_.info(getBulkLoadFile() + ":" + count
                    		    + " Invalid Technology", null);
                    		break;
                    }
                    if (licenseNotAvailable)
                    {
                    	writeError(importedPackage, LICENSE_NOT_AVAILABLE);
                    	log_.info(getBulkLoadFile() + ":" + count
                    	    + "LICENSE_NOT_AVAILABLE ", null);
                    }
                    reportProgress(ctx, count, total);
                }
				catch(IllegalArgumentException iae)
				{
					writeError(importedPackage, PARSE_ERROR, iae);
				}
                catch (Throwable t)
                {
                    log_.info("Error when processing Package record-number ["+ count +"] in bulk-load-file ["+ getBulkLoadFile() + "]", t);
                }
			}
		}
		catch (Throwable ee)
		{
			log_.info("Error when running Package bulk loader for file "
			    + getBulkLoadFile(), ee);
		}
		finally
		{
			closeWriter();
		}
	}

	private void reportProgress(Context ctx, long count, long total)
	{
		PPMLogMsg ppmLogMsg = (PPMLogMsg) ctx.get(PPMLogMsg.class);
		if (null != ppmLogMsg)
		{
			ppmLogMsg.progress(ctx, count, total);
		}
	}
}
