package com.trilogy.app.crm.bulkprovisioning.loader;

import java.util.Date;
import java.util.Iterator;

import com.trilogy.app.crm.api.rmi.support.SubscribersApiSupport;
import com.trilogy.app.crm.bean.AuxiliaryService;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.SysFeatureCfg;
import com.trilogy.app.crm.bulkprovisioning.DownloadServiceCsvRecord;
import com.trilogy.app.crm.bulkprovisioning.PRBTBulkProvisioningConfig;
import com.trilogy.app.crm.bulkprovisioning.PRBTBulkProvisioningConfigHome;
import com.trilogy.app.crm.bulkprovisioning.PRBTServerTypeEnum;
import com.trilogy.app.crm.bulkprovisioning.prbtconnection.GreencityPRBTTelnetConnection;
import com.trilogy.app.crm.bulkprovisioning.prbtconnection.Media3GPRBTTelnetConnection;
import com.trilogy.app.crm.bulkprovisioning.prbtconnection.PRBTTelnetConnection;
import com.trilogy.app.crm.log.ERLogger;
import com.trilogy.app.crm.provision.CommonProvisionAgentBase;
import com.trilogy.app.crm.provision.gateway.ServiceProvisionGatewayException;
import com.trilogy.app.crm.provision.gateway.ServiceProvisioningGatewaySupport;
import com.trilogy.app.crm.provision.service.param.ProvisionEntityType;
import com.trilogy.app.crm.support.DefaultHomeSupport;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.lifecycle.LifecycleAgent;
import com.trilogy.framework.lifecycle.LifecycleException;
import com.trilogy.framework.lifecycle.LifecycleSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.NEQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.util.crmapi.wsdl.v2_1.types.serviceandbundle.PricePlanOptionTypeEnum;
import com.trilogy.framework.xlog.log.InfoLogMsg;

public class PRBTBulkProvisioningLoader implements BulkProvisioningLoaderI
{

    public static String PROVISION_SUBSCRIBER = "p_subscriber";
    public static String UNPROVISION_SUBSCRIBER = "r_subscriber";
    private static String NETWORK_LOG_NAME = "NetworkElement";
    private static String EXTERNAL_PROVISIONING_SERVER_LOG_NAME = "ProvisiongServer";
    private static String RK_BSS_LOG_NAME = "RKBSS";
    private static String TELNET_REPLY_SUCCESS ="0:PROCESS_OK";
    private static Long PRBT_SERVICE_GATEWAY_SERVICE_ID = 2L;

    // SPG parameter context keys
    public static final String CUSTOM_DOWNLOAD_SERVER_MSISDN= "CustomMsisdn";
    public static final String CUSTOM_DOWNLOAD_SERVER_IMSI= "CustomImsi";
    public static final String CUSTOM_DOWNLOAD_SERVER_SERVICEID= "CustomServiceId";
    public static final String CUSTOM_DOWNLOAD_SERVER_HLRID ="HlrId";

    @Override
    public void provision(Context mainCtx, Object obj, Object source)
    {
        DownloadServiceCsvRecord record = (DownloadServiceCsvRecord) obj;
        Context ctx = mainCtx.createSubContext();
        removeSemiColon(ctx, record);
        
        try{
        	ensureLoggers(ctx);
        } catch (Exception e) {
        	LogSupport.minor(ctx, this, "Failed to instantiate Logger");
        	return;
        }


        Subscriber sub = getNonInactiveSubscriber(ctx,record.getMsisdn(), getActualImsi(record,ctx));
        int hlrResult = 0;
        int telnetResult = 0;
        int tcbResult = 0;
        SysFeatureCfg config = (SysFeatureCfg) ctx.get(SysFeatureCfg.class);
        if (sub == null && config.isEnableExternalSubscriberPRBTProv())
        {
            ctx.put(PRBT_SUBSCRIBER_ID, EXTERNAL_SUBSCRIBER_ID);
            short type = EXTERNAL_SUBSCRIBER_TYPE.getIndex();
            if (record.getSubscriberType() != null && record.getSubscriberType().trim().length() > 0)
            {
                try
                {
                    short recordSubType = Short.valueOf(record.getSubscriberType());
                    if (recordSubType == 0)
                    {
                        type = SubscriberTypeEnum.PREPAID_INDEX;
                    }
                    else
                    {
                        type = SubscriberTypeEnum.POSTPAID_INDEX;
                    }
                }
                catch (NumberFormatException e)
                {
                }
            }
            ctx.put(PRBT_SUBSCRIBER_TYPE, Short.valueOf(type));
        }
        if (provisioningStartingPoint == NETWORK_PROVISIONING_STARTING_POINT)
        {
            hlrResult = prepareAndSendSPGServiceProvisionCollector(ctx, sub,record);
        }
        if (hlrResult == 0 && provisioningStartingPoint != TCB_PROVISIONING_STARTING_POINT)
        {
            telnetResult = sendTelnetResponseToPRBTServer(ctx,record);
            if (telnetResult == 0 && sub != null)
            {
                provisionAuxiliaryService(ctx, sub,record);
            }
        }
        logER(ctx, sub, hlrResult, telnetResult, tcbResult,record);
    }

    public void removeSemiColon(final Context ctx, final DownloadServiceCsvRecord record)
    {
        if (isUnProvision(record))
        {
            record.setMsisdn(record.getMsisdn().substring(0, record.getMsisdn().length()-1));
        }
        else
        {
            if(record.getComments()!=null)
            {
                try
                {
                    record.setComments(record.getComments().substring(0, record.getComments().length()-1));
                }
                catch(Exception ex)
                {
                    new MinorLogMsg(this, " Invalid record " + record, ex).log(ctx);
                }
                
            }
        }
        
    }

    /**
     * 
     * @param ctx
     * @param extId
     * @throws HomeException
     */
    public synchronized void reset(final Context ctx, final String extId) throws HomeException
    {
        new InfoLogMsg(this, "STARTING: Initializing PRBTBulkProvisioning Config to => " + extId, null).log(ctx);
        this.externalServerId_ = extId;
        Home configHome = (Home) ctx.get(PRBTBulkProvisioningConfigHome.class);
        config_ = (PRBTBulkProvisioningConfig) configHome.find(ctx, externalServerId_);
        if (config_ != null)
        {
            auxiliaryService_ = HomeSupportHelper.get(ctx).findBean(ctx,
                    com.redknee.app.crm.bean.AuxiliaryService.class,
                    new EQ(com.redknee.app.crm.bean.AuxiliaryServiceXInfo.IDENTIFIER, config_.getAuxiliaryService()));
            if (auxiliaryService_ == null)
            {
                String errorMsg = " Auxiliary service can not be found. Please check configurations";
                new MinorLogMsg(this, errorMsg, null).log(ctx);
                throw new HomeException(errorMsg);
            }
        }
        if (telnetPRBTconnection_ != null)
        {
            telnetPRBTconnection_.close();
        }
        if (config_.getTypeOfServer()== PRBTServerTypeEnum.Greencity)
        {
            telnetPRBTconnection_ = new GreencityPRBTTelnetConnection(ctx.createSubContext(), config_);
        }
        else if (config_.getTypeOfServer() == PRBTServerTypeEnum.Media3G)
        {
            telnetPRBTconnection_ = new Media3GPRBTTelnetConnection(ctx.createSubContext(), config_.getHostname(),
                    config_.getPortNumber(), config_.getTimeoutTelnetServer());
        }
        
        new InfoLogMsg(this, "COMPLETED: Initializing PRBTBulkProvisioning Config to => " + extId + " with Aux Id "
                + auxiliaryService_, null).log(ctx);
    }
    
    @Override
    public synchronized void initialize(Context ctx, String[] parameters) throws HomeException
    {
        new InfoLogMsg(this, " Starting initializing PRBTBulkProvisioning ", null).log(ctx);
        
        String exId = getExternalId(ctx, parameters);
        if (exId == null)
        {
            String errorMsg = " External server Id is not defined. Please check configurations";
            new MinorLogMsg(this, errorMsg, null).log(ctx);
            throw new HomeException(errorMsg);
        }        
        reset(ctx, exId);
        ensureLoggers(ctx);
        initilizeImsiIndex();
        setProvisionStartingPoint(ctx,parameters);
        new InfoLogMsg(this, " Initialized PRBTBulkProvisioning with Starting Point " + provisioningStartingPoint
                + " Auxiliary Service " + auxiliaryService_.getName() + " for external Server " + externalServerId_,
                null).log(ctx);        
    }
    
    

    
    private void ensureLoggers(Context ctx) throws HomeException
    {
        final String netLoggerKey;
        final String provErrLoggerKey;
        final String tcbErrLoggerKey;
        {
            netLoggerKey = config_.getExternalServerId() + " - " + BulkErrorLogWriter.NETWORK_ELEMENT_ERROR_FILE_LOGGER;
            provErrLoggerKey = config_.getExternalServerId() + " - " + BulkErrorLogWriter.PROVISIONING_SERVER_ERROR_LOG_FILE_LOGGER;
            tcbErrLoggerKey = config_.getExternalServerId() + " - " + BulkErrorLogWriter.TCB_ERROR_FILE_LOGGER;
        }
        //if (null != LifecycleSupport.getLifecycleAgent(ctx, netLoggerKey))
        if (null == networkErrorWriter_)
        {
            networkErrorWriter_ = new BulkErrorLogWriter();
            networkErrorWriter_.initialize(ctx, config_.getLogDirectory(), NETWORK_LOG_NAME, netLoggerKey);
        }
       // if (null != LifecycleSupport.getLifecycleAgent(ctx, provErrLoggerKey))
        if (null == provisioningErrorWriter_)
        {
            provisioningErrorWriter_ = new BulkErrorLogWriter();
            provisioningErrorWriter_.initialize(ctx, config_.getLogDirectory(), EXTERNAL_PROVISIONING_SERVER_LOG_NAME,
                    provErrLoggerKey);
        }
        if (null == tcbErrorWriter_)
        {
            tcbErrorWriter_ = new BulkErrorLogWriter();
            tcbErrorWriter_.initialize(ctx, config_.getLogDirectory(), RK_BSS_LOG_NAME, tcbErrLoggerKey);
        }
    }
    
    private String getExternalId(Context ctx, String[] parameters)
    {
        if (parameters != null && parameters.length > 0)
        {
            return parameters[0];
        }
        return null;
    }


    private String setProvisionStartingPoint(Context ctx, String[] parameters)
    {
        if (parameters != null && parameters.length > 1)
        {
            try
            {
                provisioningStartingPoint = Integer.parseInt(parameters[1]);
            }
            catch (NumberFormatException ex)
            {
                new MinorLogMsg(this, "Invalid provisioning starting point " + parameters[1], ex).log(ctx);
            }
        }
        return null;
    }


    @Override
    public void update(Context ctx, Object obj, Object source)
    {
        // TODO Auto-generated method stub
    }


    private void logErrorProvisioningServcer(Context context, String errorMsg)
    {
        provisioningErrorWriter_.logError(context, errorMsg);
    }


    private void logErrorNetworkElement(Context context, String errorMsg)
    {

        networkErrorWriter_.logError(context, errorMsg);
    }


    private void logRKCrmProvisioningError(Context context, String errorMsg)
    {
        tcbErrorWriter_.logError(context, errorMsg);
    }


    private void logER(final Context ctx, final Subscriber sub, final int hlrResult, final int telnetResult,
            final int tcbREsult, final DownloadServiceCsvRecord record)
    {
        if (record != null)
        {
            int spid = -1;
            String subId = "";
            if (sub != null)
            {
                subId = sub.getId();
                spid = sub.getSpid();
            }
            ERLogger.logPRBTBulkProvisioningER(ctx, new Date(), spid, record.getMsisdn(), record.getImsi(), "system",
                    subId, hlrResult, telnetResult, tcbREsult);
        }
    }


    private boolean isProvision(DownloadServiceCsvRecord record)
    {
        return record.getAction().equalsIgnoreCase(PROVISION_SUBSCRIBER);
    }


    private boolean isUnProvision(DownloadServiceCsvRecord record)
    {
        return record.getAction().equalsIgnoreCase(UNPROVISION_SUBSCRIBER);
    }


    private int prepareAndSendSPGServiceProvisionCollector(Context mainCtx, Subscriber sub, DownloadServiceCsvRecord record)
    {
       int hlrResult = 0;
        Context ctx = mainCtx.createSubContext();
        
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, "STARTING: Sending command to SPG/HLR for record " + record.toString(), null).log(ctx);
        }
        
        ctx.put(CUSTOM_DOWNLOAD_SERVER_HLRID, Long.valueOf(getHlrId(sub)));
        ctx.put(CUSTOM_DOWNLOAD_SERVER_MSISDN, record.getMsisdn());
        ctx.put(CUSTOM_DOWNLOAD_SERVER_IMSI, getActualImsi(record,ctx));
        ctx.put(CUSTOM_DOWNLOAD_SERVER_SERVICEID, Long.valueOf(config_.getAuxiliaryService()));
        ctx.put(CommonProvisionAgentBase.SPG_PROVISIONING_CUSTOM_PROVISION_ENTITY_TYPE, 
                String.valueOf(ProvisionEntityType.PROVISION_ENTITY_TYPE_AUX_SERVICE));
        
        if (record.getAction().equalsIgnoreCase(PROVISION_SUBSCRIBER))
        {
            hlrResult = ServiceProvisioningGatewaySupport.prepareAndSendIndividualServiceToSPG(ctx, sub,
                    getAuxiliaryServiceForExternalService(ctx), PRBT_SERVICE_GATEWAY_SERVICE_ID, true, this);
        }
        else if (record.getAction().equalsIgnoreCase(UNPROVISION_SUBSCRIBER))
        {
            hlrResult = ServiceProvisioningGatewaySupport.prepareAndSendIndividualServiceToSPG(ctx, sub,
                    getAuxiliaryServiceForExternalService(ctx), PRBT_SERVICE_GATEWAY_SERVICE_ID, false, this);
        }
        else
        {
            hlrResult = -1;
        }
       
        if (hlrResult != 0)
        {
            new MinorLogMsg(this, "Unable to provision to hlr for msisdn => " + record.getMsisdn()
                    + ", imsi => " + getActualImsi(record,ctx), null).log(ctx);
            logErrorNetworkElement(ctx, getOriginalRecord(ctx,record));
        }
        
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, "FINISHED: Sending command to SPG/HLR for record " + record.toString() + " ResultCode => " + hlrResult, null).log(ctx);
        }
        return hlrResult;
    }


    private short getHlrId(Subscriber sub)
    {
        if (sub != null)
        {
            return sub.getHlrId();
        }
        return Short.valueOf(config_.getHlrId());
    }


    private int checkForExceptionsOnHlr(Context ctx, DownloadServiceCsvRecord record)
    {
        int result = 0;
        // print the warnings to the screen if the screen is available
        final HTMLExceptionListener el = (HTMLExceptionListener) ctx.get(HTMLExceptionListener.class);
        if (el != null && el.hasErrors())
        {
            for (Iterator<Throwable> i = el.getExceptions().iterator(); i.hasNext();)
            {
                Throwable t = i.next();
                if (t instanceof ServiceProvisionGatewayException)
                {
                    ServiceProvisionGatewayException gatewayEx = (ServiceProvisionGatewayException) t;
                    result = gatewayEx.getResultCode();
                    logErrorNetworkElement(ctx, getOriginalRecord(ctx,record));
                    new MinorLogMsg(this, "Unable to provision to hlr for msisdn => " + record.getMsisdn()
                            + ", imsi => " + getActualImsi(record,ctx),gatewayEx).log(ctx);
                }
            }
        }
        return result;
    }


    private int sendTelnetResponseToPRBTServer(Context ctx, DownloadServiceCsvRecord record)
    {
        int result = 0;
        String command = this.getOriginalRecord(ctx, record);
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, "STARTING: Send telnet msg to PRBT Server for record " + record.toString(), null)
                    .log(ctx);
        }
        try
        {
            boolean connected = telnetPRBTconnection_.open();
            if (connected)
            {
                String reply = telnetPRBTconnection_.send(command);
                if (reply != null && (!reply.isEmpty()))
                {
                    result = getResultCode(ctx, record, reply);
                }
                else
                {
                    result = -1;
                    new MinorLogMsg(this, "  Unable to telnet to provisioning server  for msisdn => "
                            + record.getMsisdn() + ", imsi=> " + record.getImsi()
                            + ".  Received empty or null response from PRBT server. ", null).log(ctx);
                    logErrorProvisioningServcer(ctx, getOriginalRecord(ctx, record));
                }
            }
            else
            {
                logErrorProvisioningServcer(ctx, getOriginalRecord(ctx, record));
                result = 13;
                new MinorLogMsg(this, " Connection failed! Unable to telnet to provisioning server  for msisdn => "
                        + record.getMsisdn() + ", imsi=> " + record.getImsi(), null).log(ctx);
            }
        }
        catch (Exception ex)
        {
            result = -1;
            new MinorLogMsg(this, " Unable to telnet to provisioning server  for msisdn => " + record.getMsisdn()
                    + ", imsi=> " + record.getImsi(), ex).log(ctx);
            logErrorProvisioningServcer(ctx, getOriginalRecord(ctx, record));            
        }
        finally
        {
            // connection.close();
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "COMPLETED: Sending command to PRBT telnet server " + record + " RESULT => "
                        + result, null).log(ctx);
            }
        }
        return result;
    }

    
    private int getResultCode(final Context ctx, final DownloadServiceCsvRecord record, final String response)
    {
        int result = 0;
        if (config_.getTypeOfServer() == PRBTServerTypeEnum.Greencity)
        {
            if (response.indexOf(TELNET_REPLY_SUCCESS) < 0)
            {
                result = -1;
                int index = response.indexOf(":");
                if ((index - 2) >= 0)
                {
                    String firstResultCode = response.substring(index - 1, index);
                    try
                    {
                        String twoDigitResultCode = response.substring(index - 2, index);
                        result = Integer.valueOf(twoDigitResultCode).intValue();
                    }
                    catch (NumberFormatException ex)
                    {
                        result = Integer.valueOf(firstResultCode).intValue();
                    }
                }
            }
        }
        else
        {
            if (!response.startsWith(TELNET_REPLY_SUCCESS))
            {
                result = -1;
                String errorCode = response.substring(0, response.indexOf(':'));
                if (errorCode != null && (!errorCode.isEmpty()))
                {
                    try
                    {
                        result = Integer.valueOf(errorCode);
                    }
                    catch (Exception ex)
                    {
                        result = -1;
                    }
                }
            }
        }
        if (result != 0)
        {
            logErrorProvisioningServcer(ctx, getOriginalRecord(ctx, record));
            new MinorLogMsg(this, "  Unable to telnet to provisioning server  for msisdn => "
                    + record.getMsisdn() + " : ERROR CODE " + result, null)
                    .log(ctx);

        }
        return result;
    }
    
    private int provisionAuxiliaryService(Context ctx, Subscriber sub, DownloadServiceCsvRecord record)
    {
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this,"STARTING: BSS provisioning for sub: " + sub.getId() + " with record => " + record.toString() , null).log(ctx);
        }
        int result = 0;
        if (sub != null)
        {
            try
            {
                if (isProvision(record))
                {
                    SubscribersApiSupport.enableAuxiliaryService(ctx, sub, config_.getAuxiliaryService(), null, null,
                            null, null, false);
                }
                else if (isUnProvision(record))
                {
                    SubscribersApiSupport.disablePricePlanOption(ctx, sub,
                            PricePlanOptionTypeEnum.AUXILIARY_SERVICE.getValue(), config_.getAuxiliaryService(), null);
                }
                DefaultHomeSupport.instance().storeBean(ctx, sub);
            }
            catch (HomeException homeEx)
            {
                String errorMsg = "Unable to update RK BSS for subscriber Id " + sub.getId() + " due HomeException "
                        + homeEx.getLocalizedMessage();
                new MinorLogMsg(this, errorMsg, homeEx).log(ctx);
                logRKCrmProvisioningError(ctx, getOriginalRecord(ctx,record));
                result = 2;
            }
            catch (Exception ex)
            {
                String errorMsg = "Unable to update RK BSS for subscriber Id " + sub.getId() + " due to a "
                        + ex.getLocalizedMessage();
                new MinorLogMsg(this, errorMsg, ex).log(ctx);
                logRKCrmProvisioningError(ctx, getOriginalRecord(ctx,record));
                result = 1;
            }
        }
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this,"COMPLETED: BSS provisioning for sub: " + sub.getId() + " with record => " + record.toString() , null).log(ctx);
        }
        return result;
    }


    private Subscriber getNonInactiveSubscriber(final Context ctx, final String msisdn, final String imsi)
    {
        Subscriber sub = null;
        And and = new And();
        and.add(new NEQ(SubscriberXInfo.STATE, SubscriberStateEnum.INACTIVE));
        if (msisdn != null && !msisdn.isEmpty())
        {
            and.add(new EQ(SubscriberXInfo.MSISDN, msisdn));
        }
        else if (imsi != null && !imsi.isEmpty())
        {
            and.add(new EQ(SubscriberXInfo.IMSI, imsi));
        }
        try
        {
            sub = HomeSupportHelper.get(ctx).findBean(ctx, com.redknee.app.crm.bean.Subscriber.class,
                    and);
        }
        catch (HomeException homeEx)
        {
        }
        return sub;
    }


    public AuxiliaryService getAuxiliaryServiceForExternalService(Context ctx)
    {
        if (auxiliaryService_ == null)
        {
            new MinorLogMsg(this, " Unable to find auxiliary service " + config_.getAuxiliaryService(), null).log(ctx);
        }
        return auxiliaryService_;
    }


    private String getOriginalRecord(Context ctx, DownloadServiceCsvRecord record)
    {
        StringBuffer buf = new StringBuffer();
        buf.append(record.getAction());
        buf.append(':');
        buf.append(record.getUniqueId());
        buf.append(':');
        buf.append(record.getProvisionDate());
        buf.append(':');
        buf.append(record.getProvisionTime());
        buf.append(':');
        buf.append(record.getMsisdn());
        if (isProvision(record))
        {
            buf.append(':');
            buf.append(record.getSubscriberType());
            buf.append(':');
            buf.append(record.getCos());
            buf.append(':');
            buf.append(record.getImsi());
            buf.append(':');
            buf.append(record.getComments());
        }
        buf.append(';');
        return buf.toString();
    }

    private String getImsiFromSub(DownloadServiceCsvRecord record,Context ctx1)
    {

    	String imsi = "";
    	try
    	{
    		And and = new And();
    		and.add(new EQ(SubscriberXInfo.MSISDN, record.getMsisdn()));
    		and.add(new NEQ(SubscriberXInfo.STATE, SubscriberStateEnum.INACTIVE_INDEX));
        	Subscriber sub = HomeSupportHelper.get(ctx1).findBean(ctx1, Subscriber.class,
                   and );
        	     
        	if (sub != null)
            imsi = sub.getIMSI();
    	}
    	catch(HomeException he)
    	{
            String errorMsg = "Unable to find IMSI RK due to a "
                    + he.getLocalizedMessage();
    		new MinorLogMsg(this, errorMsg, he).log(ctx1);
    	}

       return imsi;
    
    }

    private String getActualImsi(DownloadServiceCsvRecord record, Context ctx1)
    {
        String imsi = record.getImsi();
        if (imsi != null && imsi.length() > 0 &&  (startImsiIndex_ != -1 || endImsiIndex_ != -1))
        {
            int endPoint = record.getImsi().length();
            if (endImsiIndex_ != -1)
            {
                endPoint = endImsiIndex_;
            }
            imsi = record.getImsi().substring(startImsiIndex_, endPoint);

        }
        if (imsi != null && (imsi.equals("")||(imsi.length()<=0))||imsi.equalsIgnoreCase("1234")) // check for "1234" added because its an agreement with customer that p_subscriber command will have this as default value in case of PRBT. 
        {
        	imsi = getImsiFromSub(record,ctx1);
        }
        else if (imsi == null)
        {
        	imsi = getImsiFromSub(record,ctx1);
        }
        return imsi;
    }


    private void initilizeImsiIndex()
    {
        String indexes = config_.getImsiSubStringIndex();
        if (indexes != null & (!indexes.isEmpty()))
        {
            String[] values = indexes.split(",");
            startImsiIndex_ = Integer.valueOf(values[0]);
            if (values.length > 1)
            {
                endImsiIndex_ = Integer.valueOf(values[1]);
            }
        }
    }

    private int provisioningStartingPoint = NETWORK_PROVISIONING_STARTING_POINT;
    private String externalServerId_;
    private PRBTBulkProvisioningConfig config_;
    private AuxiliaryService auxiliaryService_;
    private BulkErrorLogWriter networkErrorWriter_ = null;
    private BulkErrorLogWriter provisioningErrorWriter_ = null;
    private BulkErrorLogWriter tcbErrorWriter_ = null;
    private PRBTTelnetConnection telnetPRBTconnection_;
    private int startImsiIndex_ = -1;
    private int endImsiIndex_ = -1;
    public static final int NETWORK_PROVISIONING_STARTING_POINT = 1;
    public static final int PROVISIONING_SERVER_PROVISIONING_STARTING_POINT = 2;
    public static final int TCB_PROVISIONING_STARTING_POINT = 3;    
    public static SubscriberTypeEnum EXTERNAL_SUBSCRIBER_TYPE = SubscriberTypeEnum.PREPAID;
    public static String EXTERNAL_SUBSCRIBER_ID = "";
    public static final String PRBT_SUBSCRIBER_TYPE = "PRBTBulkProvisioningLoader_SUBSCRIBER_TYPE";
    public static final String PRBT_SUBSCRIBER_ID = "PRBTBulkProvisioningLoader_SUBSCRIBER_ID";
}
