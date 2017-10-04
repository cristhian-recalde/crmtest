 /** This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee, no
 * unauthorised use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the licence agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
*/
package com.trilogy.app.crm.poller.agent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.ActivationFeeModeEnum;
import com.trilogy.app.crm.bean.core.AuxiliaryService;
import com.trilogy.app.crm.bean.AuxiliaryServiceHome;
import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CallingGroupTypeEnum;
import com.trilogy.app.crm.bean.ClosedUserGroup;
import com.trilogy.app.crm.bean.GLCodeMapping;
import com.trilogy.app.crm.bean.PersonalListPlan;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.TaxAuthority;
import com.trilogy.app.crm.bean.TaxAuthorityXInfo;
import com.trilogy.app.crm.extension.Extension;
import com.trilogy.app.crm.extension.auxiliaryservice.core.custom.CallingGroupAuxSvcExtension;
import com.trilogy.app.crm.poller.Constants;
import com.trilogy.app.crm.poller.FilterOutException;
import com.trilogy.app.crm.poller.ProcessorInfo;
import com.trilogy.app.crm.poller.event.CRMProcessor;
import com.trilogy.app.crm.poller.event.CRMProcessorSupport;
import com.trilogy.app.crm.poller.event.FnFSelfCareProcessor;
import com.trilogy.app.crm.support.AuxiliaryServiceSupport;
import com.trilogy.app.crm.support.CallingGroupSupport;
import com.trilogy.app.crm.support.ClosedUserGroupSupport;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.app.crm.support.GLCodeSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.technology.TechnologyEnum;


/**
 * FnFSelfCareAgent is used to process the ER1900 & perform the corresponding action based upon the Operation code as in the ER
 * 
 * @author abaid
 *
 */

public class FnFSelfCareAgent implements ContextAgent, Constants
{
	
	public FnFSelfCareAgent(CRMProcessor processor)
	{
		super();
		processor_ = processor;
	}
	
	
	public void execute(Context ctx) throws AgentException
	{
		if (LogSupport.isDebugEnabled(ctx))
		{
			LogSupport.debug(ctx, this, " FnFSelfCareAgent : execute called ");
		}
		final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "execute()");
		List params = new ArrayList();
		final ProcessorInfo info = (ProcessorInfo) ctx.get(ProcessorInfo.class);
		Common.OM_FnFSELFCARE_1900.attempt(ctx);
		try
		{
			try {
				CRMProcessorSupport.makeArray(ctx, params, info.getRecord(), info.getStartIndex(), ',', info.getErid(), this);
			} catch ( FilterOutException e){
				return; 
			}
			
			
			if (LogSupport.isDebugEnabled(ctx))
			{
				new DebugLogMsg(this, "The parsed contents of SELFCARECUGER " + params, null).log(ctx);
			}
			
			boolean isValidEr = validateEr1900Contents(ctx,params,info);
			
			if(isValidEr)
			{
				String targetMsisdn = getEr1900FieldAtIndex(params,FnF_SELFCARE_ER1900_TARGETSUBDN_INDEX);
				int opCode = Integer.parseInt(getEr1900FieldAtIndex(params,FnF_SELFCARE_ER1900_OPERATIONCODE_INDEX));
				int spid = Integer.parseInt(getEr1900FieldAtIndex(params,FnF_SELFCARE_ER1900_SPID_INDEX));
				long cugId = 0,plpId = 0;
				
				if(opCode == FnF_SELFCARE_OPCODE_CREATEOWNER_CUG || opCode == FnF_SELFCARE_OPCODE_ADDSUBSCRIBERTOCUG ||
						opCode == FnF_SELFCARE_OPCODE_REMOVESUBSCRIBERFROMCUG || opCode == FnF_SELFCARE_OPCODE_REMOVECUG)
				{	
					cugId = Long.parseLong(getEr1900FieldAtIndex(params, FnF_SELFCARE_ER1900_CUGID_INDEX));
				}
				else if(opCode == FnF_SELFCARE_OPCODE_ATTACHPLP || opCode == FnF_SELFCARE_OPCODE_DETACHPLP ||
						opCode == FnF_SELFCARE_OPCODE_CREATEPLPTEMPLATE || opCode == FnF_SELFCARE_OPCODE_REMOVEPLPTEMPLATE)
				{	
					plpId = Long.parseLong(getEr1900FieldAtIndex(params, FnF_SELFCARE_ER1900_PLPID_INDEX));
				}
				
				switch(opCode)
				{
				case FnF_SELFCARE_OPCODE_RESERVED :
					new InfoLogMsg(this,
							"Operation Code 0 [RESERVED] currently has no defined action ",
							null).log(ctx);
					break;
					
				case FnF_SELFCARE_OPCODE_CREATEOWNER_CUG    : 	   createCugAuxsvc(ctx,params,info);
				break;
				
				case FnF_SELFCARE_OPCODE_ADDSUBSCRIBERTOCUG :      addSubToCug(ctx,targetMsisdn,cugId,info,spid);
				break;	
				
				case FnF_SELFCARE_OPCODE_REMOVESUBSCRIBERFROMCUG : removeSubFromCug(ctx,targetMsisdn,cugId,info,spid);
				break;
				
				case FnF_SELFCARE_OPCODE_ATTACHPLP :               attachSubToPlp(ctx,targetMsisdn,plpId,info,spid);
				break;
				
				case FnF_SELFCARE_OPCODE_DETACHPLP :               detachSubfromPlp(ctx,targetMsisdn,plpId,info,spid);
				break;
				
				case FnF_SELFCARE_OPCODE_REMOVECUG :                removeCugAuxsvc(ctx,cugId,info,spid); 
				break;
				
				case FnF_SELFCARE_OPCODE_CREATEPLPTEMPLATE :        createPlpTemplate(ctx,params,info);
				break;
				
				case FnF_SELFCARE_OPCODE_REMOVEPLPTEMPLATE :        removePlpTemplate(ctx,plpId,info,spid);
				
				}
				Common.OM_FnFSELFCARE_1900.success(ctx);
			}
		}
		catch (final Throwable t)
		{
			new MinorLogMsg(this, "Failed to process ER 1900 because of Exception " + t.getMessage(), t).log(ctx);
			processor_.saveErrorRecord(ctx, info.getRecord());
			Common.OM_FnFSELFCARE_1900.failure(ctx);
		}
		finally
		{
			pmLogMsg.log(ctx);
            CRMProcessor.playNice(ctx, CRMProcessor.HIGH_ER_THROTTLING);
		}
	}
	
	
	
	/**
	 * @param ctx
	 * @param plpId
	 * @param info
	 * @param spid
	 */
	private void removePlpTemplate(Context ctx, long plpId,ProcessorInfo info,int spid) 
	{
		AuxiliaryService auxSvc = AuxiliaryServiceSupport.getAuxServiceByCallingGroupIdentifier(ctx,plpId,CallingGroupTypeEnum.PLP,spid);
		try
		{
			Home auxHome = (Home)ctx.get(AuxiliaryServiceHome.class);
			if(auxHome != null)
			{
				if(auxSvc != null)
				{
					auxHome.remove(auxSvc);
				}
				else
				{
					new MinorLogMsg(this,
							"Auxiliary Service for PLP with ID - " +plpId+ " doesn't exist ",
							null).log(ctx);
					processor_.saveErrorRecord(ctx, info.getRecord());
					Common.OM_FnFSELFCARE_1900.failure(ctx);
				}
			}
			else
			{
				throw new HomeException("AuxiliaryServiceHome not found in context ");
			}
		}
        catch (HomeInternalException hie)
        {
            new MinorLogMsg(this, "Exception occurred while trying to remove [PLPID - " + plpId
                    + "] with [AuxiliaryService ID - " + auxSvc.getIdentifier() + "]", hie).log(ctx);
            processor_.saveErrorRecord(ctx, info.getRecord());
            Common.OM_FnFSELFCARE_1900.failure(ctx);
        }
		catch (UnsupportedOperationException uso)
		{
			new MinorLogMsg(this,
					"Exception occurred while trying to remove [PLPID - " +plpId+ "] with [AuxiliaryService ID - " +auxSvc.getIdentifier()+ "]",
					uso).log(ctx);

			processor_.saveErrorRecord(ctx, info.getRecord());
			Common.OM_FnFSELFCARE_1900.failure(ctx);
		}
		catch (HomeException he) 
		{
			new MinorLogMsg(this,
					"Exception occurred while trying to remove [PLPID - " +plpId+ "] with [AuxiliaryService ID - " +auxSvc.getIdentifier()+ "]",
					he).log(ctx);

			processor_.saveErrorRecord(ctx, info.getRecord());
			Common.OM_FnFSELFCARE_1900.failure(ctx);
		}
	}


	/**
	 * @param ctx
	 * @param params
	 * @param info
	 */
	private void createPlpTemplate(Context ctx, List params,ProcessorInfo info) 
	{
		final long plpId = Long.parseLong(getEr1900FieldAtIndex(params, FnF_SELFCARE_ER1900_PLPID_INDEX));
		final long serviceCharge = Long.parseLong(getEr1900FieldAtIndex(params,FnF_SELFCARE_ER1900_CRMSERVICEFEE_INDEX));
		final String glCode = getEr1900FieldAtIndex(params,FnF_SELFCARE_ER1900_CRMGLCODE_INDEX);
		final int taxAuthority = Integer.parseInt(getEr1900FieldAtIndex(params,FnF_SELFCARE_ER1900_CRMTAXAUTHORITY_INDEX));
		final short ss = Short.parseShort(getEr1900FieldAtIndex(params,FnF_SELFCARE_ER1900_SMARTSUSPENSION_INDEX));
		boolean smartSuspension = false;
		String plpName = getEr1900FieldAtIndex(params, FnF_SELFCARE_ER1900_PLPNAME_INDEX);
		int activationFee = Integer.parseInt(getEr1900FieldAtIndex(params,FnF_SELFCARE_ER1900_FULLorPRORATE_INDEX));
		int spid = Integer.parseInt(getEr1900FieldAtIndex(params,FnF_SELFCARE_ER1900_SPID_INDEX));
        AuxiliaryService service;
        try
        {
            service = (AuxiliaryService)XBeans.instantiate(AuxiliaryService.class, ctx);
        }
        catch(Throwable t)
        {
            service = new com.redknee.app.crm.bean.core.AuxiliaryService();
            new MinorLogMsg(this, "XBeans can't isntantiate", t).log(ctx);
        }

		if(ss == SMARTSUSPENSION_ON)
		{
			smartSuspension = true;
		}
		else if(ss == SMARTSUSPENSION_OFF)
		{
			smartSuspension = false;
		}
		
			try
			{
				AuxiliaryService auxSvc = AuxiliaryServiceSupport.getAuxServiceByCallingGroupIdentifier(ctx,plpId,CallingGroupTypeEnum.PLP,spid);
				if(auxSvc == null || auxSvc.getIdentifier()== -1)              //-1 indicates that AuxiliaryService for the PLP is not yet created.
				{
					service.setName(plpName);
					service.setSpid(spid);
					service.setType(AuxiliaryServiceTypeEnum.CallingGroup);

	                Collection<Extension> extensions = new ArrayList<Extension>();

	                CallingGroupAuxSvcExtension extension = new CallingGroupAuxSvcExtension();
	                extension.setAuxiliaryServiceId(service.getID());
	                extension.setSpid(service.getSpid());
	                extension.setCallingGroupIdentifier(plpId);
	                extension.setCallingGroupType(CallingGroupTypeEnum.PLP);
	                extensions.add(extension);
	                
	                service.setAuxiliaryServiceExtensions(ExtensionSupportHelper.get(ctx).wrapExtensions(ctx, extensions));

					service.setChargingModeType(ServicePeriodEnum.MONTHLY);
					service.setSmartSuspension(smartSuspension);                //Not from ER1900, will be auto set to the Default Value, 
					if(activationFee == PRORATEINDICATOR_FULL)   						
					{
						service.setActivationFee(ActivationFeeModeEnum.FULL);
					}
					else if(activationFee == PRORATEINDICATOR_PRORATE)
					{
						service.setActivationFee(ActivationFeeModeEnum.PRORATE);
					}	
					service.setCharge(serviceCharge);							         // Service Fee to be populated from Er1900
					service.setAdjustmentTypeDescription("PLP " + plpName);
					service.setGLCode(glCode);
					service.setInvoiceDescription("PLP " + plpName);
					service.setTaxAuthority(taxAuthority);								//Tax authority should preExist in CRM
					service.setTechnology(TechnologyEnum.ANY);
					
					Home auxSvcHome = (Home) ctx.get(AuxiliaryServiceHome.class);
					if (auxSvcHome == null)
					{
						new MinorLogMsg(this, "System error: no AuxiliaryServiceHome found in context."
								,null).log(ctx);
						processor_.saveErrorRecord(ctx, info.getRecord());
						Common.OM_FnFSELFCARE_1900.failure(ctx);
						return;
					}
					service = (AuxiliaryService) auxSvcHome.create(ctx,service);
				}
				else
				{
					new MinorLogMsg(this, "AuxiliaryService for CUG ID" + plpId + " already exists ."
							,null).log(ctx);
				}

			}	
			catch (final HomeException he)
			{
				new MajorLogMsg(this, "Problem encounterred while trying to Create the AuxliaryService " + service.getName() + ")."
						,he).log(ctx);
				
				processor_.saveErrorRecord(ctx, info.getRecord());
				Common.OM_FnFSELFCARE_1900.failure(ctx);
			}
	}


	/**
	 * @param ctx
	 * @param cugId
	 * @param info
	 * @param spid
	 */
	private void removeCugAuxsvc(Context ctx, long cugId,ProcessorInfo info,int spid)
	{
        AuxiliaryService auxSvc = null;
        try
        {
            ClosedUserGroup cug = ClosedUserGroupSupport.getCUG(ctx, cugId, spid);
            if (cug == null)
            {
                new MinorLogMsg(this, "Fail to find the CUG with ID - " + cugId + " at ECP", null).log(ctx);
                processor_.saveErrorRecord(ctx, info.getRecord());
                Common.OM_FnFSELFCARE_1900.failure(ctx);
                return;
            }
            auxSvc = AuxiliaryServiceSupport.getAuxServiceByCallingGroupIdentifier(ctx, cug.getCugTemplateID(),
                    CallingGroupTypeEnum.CUG, spid);
            
            if (auxSvc == null)
            {
                auxSvc = AuxiliaryServiceSupport.getAuxServiceByCallingGroupIdentifier(ctx, cug.getCugTemplateID(),
                        CallingGroupTypeEnum.PCUG, spid);
            }
            
            Home auxHome = (Home) ctx.get(AuxiliaryServiceHome.class);
            if (auxHome != null)
            {
                if (auxSvc != null)
                {
                    auxHome.remove(auxSvc);
                }
                else
                {
                    new MinorLogMsg(this, "Auxiliary Service for CUG with ID - " + cugId + ", cugTemplate - "
                            + cug.getCugTemplateID() + " doesn't exist ", null).log(ctx);
                    processor_.saveErrorRecord(ctx, info.getRecord());
                    Common.OM_FnFSELFCARE_1900.failure(ctx);
                }
            }
            else
            {
                throw new HomeInternalException("AuxiliaryServiceHome not found in context ");
            }
        }
        catch (HomeInternalException hie)
        {
            new MinorLogMsg(this, "Exception occurred while trying to remove [CUGID - " + cugId
                    + "] with [AuxiliaryService ID - " + (auxSvc != null ? auxSvc.getIdentifier() : "") + "]", hie)
                    .log(ctx);
            processor_.saveErrorRecord(ctx, info.getRecord());
            Common.OM_FnFSELFCARE_1900.failure(ctx);
        }
        catch (HomeException he)
        {
            new MinorLogMsg(this, "Exception occurred while trying to remove [CUGID - " + cugId
                    + "] with [AuxiliaryService ID - " + (auxSvc != null ? auxSvc.getIdentifier() : "") + "]", he)
                    .log(ctx);
            processor_.saveErrorRecord(ctx, info.getRecord());
            Common.OM_FnFSELFCARE_1900.failure(ctx);
        }
	}


	/**
	 * Basically validates the contents of the ER1900. 
	 * @param ctx
	 * @param params The contents of ER1900
	 * @return true -> ValidER , false->InvalidER
	 */
	private boolean validateEr1900Contents(Context ctx, List params,ProcessorInfo info)
	{
		//Attributes like SPID,MSISDN,CUG,PLP,TaxAuthority,GlCode should pre-exist in CRM.If they dont exist as contained in the ER then,
		//the ER is considered to be Invalid, 
		int spid = 0,opCode = 0,resultCode = 0,taxAuthId = 0,serviceType = 0;
		String targetMsisdn = null,glCode = null,serviceFee = null,cugName = null,plpName = null;
		long cugId = 0,plpId = 0;

		try
		{
			spid = Integer.parseInt(getEr1900FieldAtIndex(params,FnF_SELFCARE_ER1900_SPID_INDEX));
			opCode = Integer.parseInt(getEr1900FieldAtIndex(params,FnF_SELFCARE_ER1900_OPERATIONCODE_INDEX));
			resultCode = Integer.parseInt(getEr1900FieldAtIndex(params,FnF_SELFCARE_ER1900_RESULTCODE_INDEX));
			targetMsisdn = getEr1900FieldAtIndex(params,FnF_SELFCARE_ER1900_TARGETSUBDN_INDEX);
			glCode = getEr1900FieldAtIndex(params, FnF_SELFCARE_ER1900_CRMGLCODE_INDEX);
			serviceFee = getEr1900FieldAtIndex(params,FnF_SELFCARE_ER1900_CRMSERVICEFEE_INDEX);
			serviceType = Integer.parseInt(getEr1900FieldAtIndex(params,FnF_SELFCARE_ER1900_SERVICETYPE_INDEX));
			cugName = getEr1900FieldAtIndex(params, FnF_SELFCARE_ER1900_CUGNAME_INDEX);
			plpName = getEr1900FieldAtIndex(params, FnF_SELFCARE_ER1900_PLPNAME_INDEX);
		}
		catch(NumberFormatException ne)
		{
			processor_.saveErrorRecord(ctx, info.getRecord());
			Common.OM_FnFSELFCARE_1900.failure(ctx);
			new MajorLogMsg(this,
					"Failed to parse the contents of ER1900 " +params,
					ne).log(ctx);
			return false;
		}
		
		//Validate the Result Code
		{
			if(resultCode != SUCCESS)
			{
				processor_.saveErrorRecord(ctx, info.getRecord());
				Common.OM_FnFSELFCARE_1900.failure(ctx);
				new MinorLogMsg(this,
								"Non-Success ResultCode specified in the ER,Cannot process further " +params,
								null).log(ctx);

				return false;
			}
		}

		//Validate the Service Type 
		{
			if(!(serviceType == SERVICETYPE_VOICE || serviceType == SERVICETYPE_VOICEnSMS))
			{
				processor_.saveErrorRecord(ctx, info.getRecord());
				Common.OM_FnFSELFCARE_1900.failure(ctx);
				new MinorLogMsg(this,
								"Invalid ServiceType specified in the ER,Cannot process further " +params,
								null).log(ctx);

				return false;
			}
		}
		
		//Validate the Operation Code 
		{
			if(opCode < OPCODE_LOWERLIMIT || opCode > OPCODE_UPPERLIMIT)
			{
				processor_.saveErrorRecord(ctx, info.getRecord());
				Common.OM_FnFSELFCARE_1900.failure(ctx);
				new MinorLogMsg(this,
								"Invalid OperationCode specified in the ER,Cannot process further " +params,
								null).log(ctx);

				return false;
			}
		}
		
		
		//Validate the SPID 
		{
			CRMSpid sp = null;
			try 
			{
				sp = SpidSupport.getCRMSpid(ctx,spid);
				if(sp == null)
				{
					processor_.saveErrorRecord(ctx, info.getRecord());
					Common.OM_FnFSELFCARE_1900.failure(ctx);
					new MinorLogMsg(this,
									"Invalid SPID specified in the ER,Cannot process further " +params,
									null).log(ctx);
					return false;
				}
			}
			catch (HomeException e)
			{
				new MinorLogMsg(this,
						"Exception occurred while looking up SPID with ID " + spid + "[ER " + params,
						e).log(ctx);
				processor_.saveErrorRecord(ctx, info.getRecord());
				Common.OM_FnFSELFCARE_1900.failure(ctx);
				return false;
			}
		}

		//Validate the Tax Authority
		{
			TaxAuthority taxAuth = null;
			try 
			{
				if(opCode == FnF_SELFCARE_OPCODE_CREATEOWNER_CUG || opCode == FnF_SELFCARE_OPCODE_CREATEPLPTEMPLATE)
				{
					taxAuthId = Integer.parseInt(getEr1900FieldAtIndex(params, FnF_SELFCARE_ER1900_CRMTAXAUTHORITY_INDEX)); 
					
					final And filter = new And();
					filter.add(new EQ(TaxAuthorityXInfo.TAX_ID, taxAuthId));
					filter.add(new EQ(TaxAuthorityXInfo.SPID, spid));
                    taxAuth = HomeSupportHelper.get(ctx).findBean(ctx, TaxAuthority.class, filter);
					
                    if(taxAuth == null)
					{
						processor_.saveErrorRecord(ctx, info.getRecord());
						Common.OM_FnFSELFCARE_1900.failure(ctx);
						new MinorLogMsg(this,
										"No matching tax authority for the SPID " +spid+ " ,Cannot process further " +params,
										null).log(ctx);

						return false;
					}
				}
			}
			catch(NumberFormatException ne)
			{
				new MinorLogMsg(this,
						"Invalid TaxAuthority ID specified in the ER,Cannot process further " +taxAuthId+ " [ER: " +params,
						ne).log(ctx);
				processor_.saveErrorRecord(ctx, info.getRecord());
				Common.OM_FnFSELFCARE_1900.failure(ctx);
				return false;
			}
			catch (HomeException e)
			{
				new MinorLogMsg(this,
						"Exception occurred while looking up TaxAuthority with ID " +taxAuthId+ " [ER: " +params,
						e).log(ctx);
				processor_.saveErrorRecord(ctx, info.getRecord());
				Common.OM_FnFSELFCARE_1900.failure(ctx);
				return false;
			}
		}
		
		//Validate the GlCode
		{
			GLCodeMapping gl = null;
			try 
			{
				if(opCode == FnF_SELFCARE_OPCODE_CREATEOWNER_CUG || opCode == FnF_SELFCARE_OPCODE_CREATEPLPTEMPLATE)
				{
					gl = GLCodeSupportHelper.get(ctx).getGLCodeFromDescriptionBySpid(ctx,glCode,spid);
					if(gl == null)
					{
						processor_.saveErrorRecord(ctx, info.getRecord());
						Common.OM_FnFSELFCARE_1900.failure(ctx);
						new MinorLogMsg(this,
								"Invalid GlCode specified in the ER,Cannot process further " +params,
								null).log(ctx);
						
						return false;
					}
				}
			} 
			catch (HomeException e)
			{
				new MinorLogMsg(this,
						"Exception occurred while looking up GLcode with Description " +glCode+ " [ER "+params,
						e).log(ctx);
				processor_.saveErrorRecord(ctx, info.getRecord());
				Common.OM_FnFSELFCARE_1900.failure(ctx);
				return false;	
			}
		}
		
		//Validate the Service Activation fee
		{
			if(opCode == FnF_SELFCARE_OPCODE_CREATEOWNER_CUG || opCode == FnF_SELFCARE_OPCODE_CREATEPLPTEMPLATE)
			{
				if(serviceFee.equals(""))
				{
					processor_.saveErrorRecord(ctx, info.getRecord());
					Common.OM_FnFSELFCARE_1900.failure(ctx);
					new MinorLogMsg(this,
									"Incorrect Service Activation fee specified in the ER " +params,
									null).log(ctx);

					return false;

				}
			}
		}

		//Validate the CUG Name
		{
			if(opCode == FnF_SELFCARE_OPCODE_CREATEOWNER_CUG)
			{
				if(cugName.equals(""))
				{
					processor_.saveErrorRecord(ctx, info.getRecord());
					Common.OM_FnFSELFCARE_1900.failure(ctx);
					new MinorLogMsg(this,
									"Incorrect CUG Name specified in the ER " +params,
									null).log(ctx);

					return false;
				}
			}
		}
		
		//Validate the PLP Name
		{
			if(opCode == FnF_SELFCARE_OPCODE_CREATEPLPTEMPLATE)
			{
				if(plpName.equals(""))
				{
					processor_.saveErrorRecord(ctx, info.getRecord());
					Common.OM_FnFSELFCARE_1900.failure(ctx);
					new MinorLogMsg(this,
									"Incorrect PLP Name specified in the ER " +params,
									null).log(ctx);

					return false;
				}
			}
		}
		
		//Validate the CRM Fee Prorate Indicator
		{
			if(opCode == FnF_SELFCARE_OPCODE_CREATEOWNER_CUG || opCode == FnF_SELFCARE_OPCODE_CREATEPLPTEMPLATE)
			{
				int prorateIndicator = 0;
				try
				{
					prorateIndicator = Integer.parseInt(getEr1900FieldAtIndex(params, FnF_SELFCARE_ER1900_FULLorPRORATE_INDEX));
					if(!(prorateIndicator == PRORATEINDICATOR_FULL || prorateIndicator == PRORATEINDICATOR_PRORATE))
					{
						processor_.saveErrorRecord(ctx, info.getRecord());
						Common.OM_FnFSELFCARE_1900.failure(ctx);
						new MinorLogMsg(this,
										"Incorrect 'Prorate Indicator' specified in the ER " +params,
										null).log(ctx);

						return false;
					}
				}
				catch(NumberFormatException ne)
				{
					new MinorLogMsg(this,
							"Invalid 'CRM Fee Prorate Indicator' specified in the ER,Cannot process further " +taxAuthId+ " [ER: " +params,
							ne).log(ctx);
					processor_.saveErrorRecord(ctx, info.getRecord());
					Common.OM_FnFSELFCARE_1900.failure(ctx);
					return false;
				}
			}
		}
		
		//Validate the MSISDN
		{
			Subscriber subscriber = null;
			try 
			{
//Validate the targetMsisdn only for (add/remove Sub to/from CUG) or (add/remove Sub to/from PLP) Operations codes 
				if(opCode == FnF_SELFCARE_OPCODE_ADDSUBSCRIBERTOCUG || opCode == FnF_SELFCARE_OPCODE_REMOVESUBSCRIBERFROMCUG
						|| opCode == FnF_SELFCARE_OPCODE_ATTACHPLP || opCode == FnF_SELFCARE_OPCODE_DETACHPLP)				
				{
					if(targetMsisdn.equals(""))
					{
						processor_.saveErrorRecord(ctx, info.getRecord());
						Common.OM_FnFSELFCARE_1900.failure(ctx);
						new MinorLogMsg(this,
								"Invalid MSISDN " +targetMsisdn+ " specified in the ER,Cannot process further "+params,
								null).log(ctx);
						
						return false;
					}
					subscriber = SubscriberSupport.lookupSubscriberForMSISDN(ctx, targetMsisdn ,new Date(info.getDate()));
					if (subscriber == null)
					{
						processor_.saveErrorRecord(ctx, info.getRecord());
						Common.OM_FnFSELFCARE_1900.failure(ctx);
						new MinorLogMsg(this,
								"Invalid MSISDN " +targetMsisdn+ " specified in the ER,Cannot process further "+params,
								null).log(ctx);
						
						return false;
					}
				}
			}
			catch (HomeException e)
			{
				new MinorLogMsg(this,
								"Exception occurred while looking up subscriber with MSISDN " +targetMsisdn+ "[ER " +params,
								e).log(ctx);
				processor_.saveErrorRecord(ctx, info.getRecord());
				Common.OM_FnFSELFCARE_1900.failure(ctx);
				return false;
			}
		}
		
		//Validate the CUG ID
		{
			if(opCode == FnF_SELFCARE_OPCODE_CREATEOWNER_CUG || opCode == FnF_SELFCARE_OPCODE_ADDSUBSCRIBERTOCUG ||
					opCode == FnF_SELFCARE_OPCODE_REMOVESUBSCRIBERFROMCUG || opCode == FnF_SELFCARE_OPCODE_REMOVECUG)
			{	
				try
				{
					cugId = Long.parseLong(getEr1900FieldAtIndex(params, FnF_SELFCARE_ER1900_CUGID_INDEX));
				}
				catch(NumberFormatException ne)
				{
					processor_.saveErrorRecord(ctx, info.getRecord());
					Common.OM_FnFSELFCARE_1900.failure(ctx);
					new MinorLogMsg(this,
							"Invalid CUG ID specified in the ER,Cannot process further " +params,
							null).log(ctx);
					
					return false;
				}
				
				if(opCode == FnF_SELFCARE_OPCODE_CREATEOWNER_CUG || opCode == FnF_SELFCARE_OPCODE_ADDSUBSCRIBERTOCUG
							|| opCode == FnF_SELFCARE_OPCODE_REMOVESUBSCRIBERFROMCUG)
				{
                    ClosedUserGroup cug = null;
                    try
                    {
                        cug = ClosedUserGroupSupport.getCug(ctx,cugId);
                    }
                    catch (Exception e)
                    {
                        processor_.saveErrorRecord(ctx, info.getRecord());
                        Common.OM_FnFSELFCARE_1900.failure(ctx);
                        new MinorLogMsg(this,
                                "Invalid CUG ID specified in the ER,Cannot process further " +params,
                                null).log(ctx);
                        
                        return false;    
                    }
                    
                    if(cug == null)
					{
						processor_.saveErrorRecord(ctx, info.getRecord());
						Common.OM_FnFSELFCARE_1900.failure(ctx);
						new MinorLogMsg(this,
								"Invalid CUG ID specified in the ER,Cannot process further " +params,
								null).log(ctx);
						
						return false;
					}
				}
			}
		}
		
		//Validate the PLP ID
		{
			if(opCode == FnF_SELFCARE_OPCODE_ATTACHPLP || opCode == FnF_SELFCARE_OPCODE_DETACHPLP ||
					opCode == FnF_SELFCARE_OPCODE_CREATEPLPTEMPLATE || opCode == FnF_SELFCARE_OPCODE_REMOVEPLPTEMPLATE)
			{
				try
				{
					plpId = Long.parseLong(getEr1900FieldAtIndex(params, FnF_SELFCARE_ER1900_PLPID_INDEX));
				}
				catch(NumberFormatException ne)
				{
					processor_.saveErrorRecord(ctx, info.getRecord());
					Common.OM_FnFSELFCARE_1900.failure(ctx);
					new MinorLogMsg(this,
							"Invalid PLP ID specified in the ER,Cannot process further " +params,
							null).log(ctx);
					
					return false;
				}
				
				if(opCode == FnF_SELFCARE_OPCODE_CREATEPLPTEMPLATE || opCode == FnF_SELFCARE_OPCODE_ATTACHPLP
						|| opCode == FnF_SELFCARE_OPCODE_DETACHPLP)
				{
					PersonalListPlan plp = AuxiliaryServiceSupport.getPlpFromPlpIdBySpid(ctx,plpId,spid);
					if(plp == null)
					{
						processor_.saveErrorRecord(ctx, info.getRecord());
						Common.OM_FnFSELFCARE_1900.failure(ctx);
						new MinorLogMsg(this,
								"Invalid CUG ID specified in the ER,Cannot process further " +params,
								null).log(ctx);
						
						return false;
					}
				}
				else if(opCode == FnF_SELFCARE_OPCODE_REMOVEPLPTEMPLATE)
				{
					AuxiliaryService auxSvc = AuxiliaryServiceSupport.getAuxServiceByCallingGroupIdentifier(ctx,plpId,CallingGroupTypeEnum.PLP,spid);
					if(auxSvc == null)
					{
						new MinorLogMsg(this,
								"Auxiliary Service for PLP with ID - " +plpId+ " doesn't exist, [ER " +params,
								null).log(ctx);
						processor_.saveErrorRecord(ctx, info.getRecord());
						Common.OM_FnFSELFCARE_1900.failure(ctx);
						return false;
					}
				}
			}
		}
		
		return true;
	}

	/**
	 * First,Create the Corresponding auxiliary service for CUG with ID as specified in the ER,
	 * Next, Subscribe the newly created auxiliary service to the subscriber with 'MSISDN=targetMsisdn' 
	 * @param ctx
	 * @param params
	 */	

	protected void createCugAuxsvc(Context ctx, List params,ProcessorInfo info) 
	{
		final long cugId = Long.parseLong(getEr1900FieldAtIndex(params, FnF_SELFCARE_ER1900_CUGID_INDEX));
		final long serviceCharge = Long.parseLong(getEr1900FieldAtIndex(params,FnF_SELFCARE_ER1900_CRMSERVICEFEE_INDEX));
		final String glCode = getEr1900FieldAtIndex(params,FnF_SELFCARE_ER1900_CRMGLCODE_INDEX);
		final int taxAuthority = Integer.parseInt(getEr1900FieldAtIndex(params,FnF_SELFCARE_ER1900_CRMTAXAUTHORITY_INDEX));
		final short ss = Short.parseShort(getEr1900FieldAtIndex(params,FnF_SELFCARE_ER1900_SMARTSUSPENSION_INDEX));
		boolean smartSuspension = false;
		String cugName = getEr1900FieldAtIndex(params, FnF_SELFCARE_ER1900_CUGNAME_INDEX);
		int activationFee = Integer.parseInt(getEr1900FieldAtIndex(params,FnF_SELFCARE_ER1900_FULLorPRORATE_INDEX));
        AuxiliaryService service;
        try
        {
            service = (AuxiliaryService)XBeans.instantiate(AuxiliaryService.class, ctx);
        }
        catch(Throwable t)
        {
            service = new com.redknee.app.crm.bean.core.AuxiliaryService();
            new MinorLogMsg(this, "XBeans can't isntantiate", t).log(ctx);
        }
		int spid = Integer.parseInt(getEr1900FieldAtIndex(params,FnF_SELFCARE_ER1900_SPID_INDEX));
		
		if(ss == SMARTSUSPENSION_ON)
		{
			smartSuspension = true;
		}
		else if(ss == SMARTSUSPENSION_OFF)
		{
			smartSuspension = false;
		}
		
		try
		{
		    ClosedUserGroup cug = ClosedUserGroupSupport.getCug(ctx, cugId);
            
            if (cug == null)
            {
                new MinorLogMsg(this,
                        "Fail to find the CUG with ID - " +cugId+ " at ECP",
                        null).log(ctx);
                processor_.saveErrorRecord(ctx, info.getRecord());
                Common.OM_FnFSELFCARE_1900.failure(ctx);
                return;
            }
            
            AuxiliaryService auxSvc = AuxiliaryServiceSupport.getAuxServiceByCallingGroupIdentifier(ctx,
                    cug.getCugTemplateID(),
                    CallingGroupTypeEnum.CUG,
                    spid);
            if(auxSvc == null)
			{
				service.setName(cugName);
				service.setSpid(spid);
				service.setType(AuxiliaryServiceTypeEnum.CallingGroup);
				
		        Collection<Extension> extensions = new ArrayList<Extension>();

		        CallingGroupAuxSvcExtension extension = new CallingGroupAuxSvcExtension();
	            extension.setAuxiliaryServiceId(service.getID());
	            extension.setSpid(service.getSpid());
                extension.setCallingGroupIdentifier(cug.getCugTemplateID());
                extension.setCallingGroupType(CallingGroupTypeEnum.CUG);
	            extensions.add(extension);
	            
	            service.setAuxiliaryServiceExtensions(ExtensionSupportHelper.get(ctx).wrapExtensions(ctx, extensions));
	            
                service.setChargingModeType(ServicePeriodEnum.MONTHLY);
				
				service.setSmartSuspension(smartSuspension);                //Not from ER1900, will be auto set to the Default Value,
				if(activationFee == PRORATEINDICATOR_FULL)   						//Not from ER1900, will be auto set to the Default Value,
				{
					service.setActivationFee(ActivationFeeModeEnum.FULL);
				}
				else if(activationFee == PRORATEINDICATOR_PRORATE)
				{
					service.setActivationFee(ActivationFeeModeEnum.PRORATE);
				}	
				service.setCharge(serviceCharge);							         // Service Fee to be populated from Er1900
				service.setAdjustmentTypeDescription("CUG " + cugName);
				service.setGLCode(glCode);
				service.setInvoiceDescription("CUG " + cugName);
				service.setTaxAuthority(taxAuthority);								//Tax authority should preExist in CRM
				service.setTechnology(TechnologyEnum.ANY);
				 
				Home auxSvcHome = (Home) ctx.get(AuxiliaryServiceHome.class);
				if (auxSvcHome == null)
				{
					new MajorLogMsg(this, "System error: no AuxiliaryServiceHome found in context." +params
							,null).log(ctx);
					processor_.saveErrorRecord(ctx, info.getRecord());
					Common.OM_FnFSELFCARE_1900.failure(ctx);
					return;
				}
				
				service = (AuxiliaryService) auxSvcHome.create(ctx,service);
			}
			else
			{
			    new InfoLogMsg(this, "AuxiliaryService for CUG id " + cugId + 
                        ", cugTemplate ID " +  cug.getCugTemplateID() + " already exists ."
                        ,null).log(ctx);
			}
		}
		catch (final HomeException he)
		{
			new MinorLogMsg(this, "Problem encounterred while trying to create the AuxiliaryService " + service.getName() + ")."
					,he).log(ctx);
			processor_.saveErrorRecord(ctx, info.getRecord());
			Common.OM_FnFSELFCARE_1900.failure(ctx);
		}
}
	
	
	/**
	 * Un-Subscribe the CUG auxiliary service with ID 'cugId' to subscriber with 'MSISDN=targetMsisdn' 
	 * @param ctx
	 * @param targetMsisdn
	 * @param cugId
	 */
	protected void removeSubFromCug(Context ctx,String targetMsisdn, long cugId,ProcessorInfo info,int spid) 
	{
		long auxId = 0;
		try
		{
	        ClosedUserGroup cug = ClosedUserGroupSupport.getCug(ctx, cugId);
            if (cug == null)
            {
                new MinorLogMsg(this,
                        "Fail to find the CUG with ID - " +cugId+ " at FnF",
                        null).log(ctx);
                processor_.saveErrorRecord(ctx, info.getRecord());
                Common.OM_FnFSELFCARE_1900.failure(ctx);
                return;
            }

            final AuxiliaryService auxSvc = 
                CallingGroupSupport.getAuxiliaryServiceForCUGTemplate(
                        ctx, 
                        cug.getCugTemplateID());
            if(auxSvc != null)
			{
				auxId = auxSvc.getIdentifier();
                Subscriber sub = SubscriberSupport.lookupSubscriberForMSISDN(ctx, targetMsisdn ,new Date(info.getDate()));

                if(associationAlreadyExist(ctx,sub,auxSvc,cugId))
                {
					Context subCtx = ctx.createSubContext();
					subCtx.put(FnFSelfCareProcessor.FNF_FROM_SELFCARE_ER_POLLER, true);
					SubscriberAuxiliaryServiceSupport.removeAssociation(subCtx, targetMsisdn, auxId, cugId);
					subCtx.remove(FnFSelfCareProcessor.FNF_FROM_SELFCARE_ER_POLLER);
				}
				else
				{
					new MinorLogMsg(this,
							"Association between AuxiliaryService" +auxSvc.getName()+ " and " +targetMsisdn+ "doesn't exist.",
							null).log(ctx);
				}
			}
			else
			{
	            new MinorLogMsg(this, "AuxiliaryService for CUG id " + cugId + 
                         ", cugTemplate ID " +  cug.getCugTemplateID() + " already exists ."
                         ,null).log(ctx);
	            
	            processor_.saveErrorRecord(ctx, info.getRecord());
				Common.OM_FnFSELFCARE_1900.failure(ctx);
			}

		}
		catch (final HomeException he)
		{
			new MinorLogMsg(this, "Problem encounterred while trying to remove an association between MSISDN "
					+ targetMsisdn + " and AuxiliaryService (" + auxId + ").", he).log(ctx);
			
			processor_.saveErrorRecord(ctx, info.getRecord());
			Common.OM_FnFSELFCARE_1900.failure(ctx);
			ctx.remove(FnFSelfCareProcessor.FNF_FROM_SELFCARE_ER_POLLER);
		}
	}
	
	/**
	 * @param ctx
	 * @param targetMsisdn
	 * @param cugId
	 * @param info
	 * @param spid
	 */
	protected void addSubToCug(Context ctx,String targetMsisdn, long cugId,ProcessorInfo info,int spid)
	{
		long auxId = 0;
		try
		{
		    ClosedUserGroup cug = ClosedUserGroupSupport.getCug(ctx, cugId);
            
            if (cug == null)
            {
                new MinorLogMsg(this,
                        "Fail to find the CUG with ID - " +cugId+ " at ECP",
                        null).log(ctx);
                processor_.saveErrorRecord(ctx, info.getRecord());
                Common.OM_FnFSELFCARE_1900.failure(ctx);
                return;
            }
            
            final AuxiliaryService auxSvc = 
                CallingGroupSupport.getAuxiliaryServiceForCUGTemplate(
                        ctx, 
                        cug.getCugTemplateID());
            
            if(auxSvc != null)
			{
                Subscriber sub = SubscriberSupport.lookupSubscriberForMSISDN(ctx, targetMsisdn ,new Date(info.getDate()));
				auxId = auxSvc.getIdentifier();
				if( !associationAlreadyExist(ctx,sub,auxSvc,cugId))
				{
				    SubscriberAuxiliaryService auxService = SubscriberAuxiliaryServiceSupport.createAssociationForSubscriber(
                            ctx, 
                            sub, 
                            auxId,
                            cugId, 
                            AuxiliaryServiceTypeEnum.CallingGroup);				}
				else
				{
					new MinorLogMsg(this,
							"Association between AuxiliaryService" +auxSvc.getName()+ " and " +targetMsisdn+ "already exist",
							null).log(ctx);
				}
			}
			else
			{
                new MinorLogMsg(this,
                        "Auxiliary Service for CUG with ID - " +cugId+
                        ", cug template ID - " + cug.getCugTemplateID() + 
                        " doesn't exist ",
                        null).log(ctx);
                processor_.saveErrorRecord(ctx, info.getRecord());
				Common.OM_FnFSELFCARE_1900.failure(ctx);
			}
		}
		catch (final HomeException he)
		{
			new MinorLogMsg(this, "Problem encounterred while trying to Add an association between MSISDN "
					+ targetMsisdn + " and AuxiliaryService (" + auxId + ").", he).log(ctx);
			
			processor_.saveErrorRecord(ctx, info.getRecord());
			Common.OM_FnFSELFCARE_1900.failure(ctx);
		}
	}

	private boolean associationAlreadyExist(Context ctx, Subscriber sub, AuxiliaryService auxSvc, long secondaryId)
	{
        SubscriberAuxiliaryService service = SubscriberAuxiliaryServiceSupport.getSubscriberAuxiliaryService(ctx, sub
                .getId(), auxSvc.getIdentifier(), secondaryId);
        if (service != null)
        {
            return true;
        }
        return false;

	}


	/**
	 * @param ctx
	 * @param targetMsisdn
	 * @param plpId
	 * @param info
	 * @param spid
	 */
	protected void detachSubfromPlp(Context ctx,String targetMsisdn, long plpId,ProcessorInfo info,int spid) 
	{
		long auxId = 0;
		try
		{
			AuxiliaryService auxSvc = AuxiliaryServiceSupport.getAuxServiceByCallingGroupIdentifier(ctx,plpId,CallingGroupTypeEnum.PLP,spid);
			if(auxSvc != null)
			{
				auxId = auxSvc.getIdentifier();
                Subscriber sub = SubscriberSupport.lookupSubscriberForMSISDN(ctx, targetMsisdn ,new Date(info.getDate()));

                if(associationAlreadyExist(ctx,sub,auxSvc,SubscriberAuxiliaryServiceSupport.SECONDARY_ID_NOT_USED))
				{
					Context subCtx = ctx.createSubContext();
					subCtx.put(FnFSelfCareProcessor.FNF_FROM_SELFCARE_ER_POLLER, true);
					SubscriberAuxiliaryServiceSupport.removeAssociation(subCtx, targetMsisdn, auxId, SubscriberAuxiliaryServiceSupport.SECONDARY_ID_NOT_USED);
					subCtx.remove(FnFSelfCareProcessor.FNF_FROM_SELFCARE_ER_POLLER);
				}
				else
				{
					new MinorLogMsg(this,
							"Association between AuxiliaryService" +auxSvc.getName()+ " and " +targetMsisdn+ "doesn't exist.",
							null).log(ctx);
				}
			}
			else
			{
				new MinorLogMsg(this,
						"Auxiliary Service for PLP with ID - " +plpId+ " under SPID " +spid+ "doesn't exist ",
						null).log(ctx);
				processor_.saveErrorRecord(ctx, info.getRecord());
				Common.OM_FnFSELFCARE_1900.failure(ctx);
			}
			
		}
		catch (final HomeException he)
		{
			new MinorLogMsg(this, "Problem encounterred while trying to remove an association between MSISDN "
					+ targetMsisdn + " and AuxiliaryService (" + auxId + ").", he).log(ctx);
			
			processor_.saveErrorRecord(ctx, info.getRecord());
			Common.OM_FnFSELFCARE_1900.failure(ctx);
			ctx.remove(FnFSelfCareProcessor.FNF_FROM_SELFCARE_ER_POLLER);
		}
	}
	
	
	/**
	 * @param ctx
	 * @param targetMsisdn
	 * @param plpId
	 * @param info
	 * @param spid
	 */
	protected void attachSubToPlp(Context ctx,String targetMsisdn, long plpId,ProcessorInfo info,int spid) 
	{
		long auxId = 0;
		try
		{
			AuxiliaryService auxSvc = AuxiliaryServiceSupport.getAuxServiceByCallingGroupIdentifier(ctx,plpId,CallingGroupTypeEnum.PLP,spid);
			if(auxSvc != null)
			{
				auxId = auxSvc.getIdentifier();
                Subscriber sub = SubscriberSupport.lookupSubscriberForMSISDN(ctx, targetMsisdn ,new Date(info.getDate()));

                if( !associationAlreadyExist(ctx,sub,auxSvc,SubscriberAuxiliaryServiceSupport.SECONDARY_ID_NOT_USED))
				{
					SubscriberAuxiliaryServiceSupport.createAssociation(ctx, targetMsisdn, auxId, SubscriberAuxiliaryServiceSupport.SECONDARY_ID_NOT_USED);
				}
				else
				{
					new MinorLogMsg(this,
							"Association between AuxiliaryService" +auxSvc.getName()+ " and " +targetMsisdn+ "already exists.",
							null).log(ctx);
				}
			}
			else
			{
				new InfoLogMsg(this,
								"Auxiliary service for PLP with ID " +plpId+ "under SPID " +spid+ "doesn't exist "
								,null);
			}
		}
		catch (final HomeException he)
		{
			new MinorLogMsg(this, "Problem encounterred while trying to add an association between MSISDN "
					+ targetMsisdn + " and AuxiliaryService (" + auxId + ").", he).log(ctx);
			
			processor_.saveErrorRecord(ctx, info.getRecord());
			Common.OM_FnFSELFCARE_1900.failure(ctx);
		}
	}
	
	/**
	 * @param params
	 * @param index
	 * @return ER1900 field at the specified 'index'
	 */
	protected String getEr1900FieldAtIndex(List params,int index)
	{
		return(CRMProcessorSupport.getField(params,index));
	}
	
	private CRMProcessor processor_ = null;
	private static final String PM_MODULE = FnFSelfCareProcessor.class.getName();
	private short SMARTSUSPENSION_ON = 1;
	private short SMARTSUSPENSION_OFF = 0;
	private int OPCODE_UPPERLIMIT = 8;
	private int OPCODE_LOWERLIMIT = 0;
	private int SUCCESS = 0;
	private int SERVICETYPE_VOICE = 1;
	private int SERVICETYPE_VOICEnSMS = 3;	
	private int PRORATEINDICATOR_FULL = 1;
	private int PRORATEINDICATOR_PRORATE = 2;
	
}
