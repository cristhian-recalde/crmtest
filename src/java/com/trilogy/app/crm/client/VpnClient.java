/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.client;

import java.util.Arrays;

import org.omg.CORBA.SystemException;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.EntryLogMsg;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.client.bm.ConnectionProperties;
import com.trilogy.app.crm.client.bm.TimerCachedConnectionProperties;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.CorbaSupportHelper;
import com.trilogy.app.crm.support.SystemStatusSupportHelper;
import com.trilogy.app.mom.Administrator;
import com.trilogy.app.mom.AdministratorIdentity;
import com.trilogy.app.mom.AdministratorInterface_v4_1;
import com.trilogy.app.mom.AdministratorInterface_v4_1Helper;
import com.trilogy.app.mom.AlternateDn;
import com.trilogy.app.mom.Announcement;
import com.trilogy.app.mom.AuthenticationException;
import com.trilogy.app.mom.AvailabilityStatusType;
import com.trilogy.app.mom.BadDataException;
import com.trilogy.app.mom.BearerCapabilityType;
import com.trilogy.app.mom.Callback;
import com.trilogy.app.mom.ConfigurationException;
import com.trilogy.app.mom.DaySchedule;
import com.trilogy.app.mom.DigitCollectionOptions;
import com.trilogy.app.mom.Divert;
import com.trilogy.app.mom.DivertType;
import com.trilogy.app.mom.DnList;
import com.trilogy.app.mom.Entity;
import com.trilogy.app.mom.EntityIdException;
import com.trilogy.app.mom.EntityType;
import com.trilogy.app.mom.HourMinute;
import com.trilogy.app.mom.HuntType;
import com.trilogy.app.mom.Huntgroup;
import com.trilogy.app.mom.InternalException;
import com.trilogy.app.mom.MaybeEntity;
import com.trilogy.app.mom.Menu;
import com.trilogy.app.mom.MenuOption;
import com.trilogy.app.mom.Operator;
import com.trilogy.app.mom.Queue;
import com.trilogy.app.mom.Realm;
import com.trilogy.app.mom.Router;
import com.trilogy.app.mom.Schedule;
import com.trilogy.app.mom.ScheduleType;
import com.trilogy.app.mom.Subscriber;
import com.trilogy.app.mom.TimeInterval;
import com.trilogy.app.mom.VpnAcActionType;
import com.trilogy.app.mom.VpnAccessCode;
import com.trilogy.app.mom.VpnActionType;
import com.trilogy.app.mom.VpnAppScreening;
import com.trilogy.app.mom.VpnBgRangeTranslation;
import com.trilogy.app.mom.VpnBgSiteRangeTranslation;
import com.trilogy.app.mom.VpnBgc;
import com.trilogy.app.mom.VpnBgsc;
import com.trilogy.app.mom.VpnCallHandlingType;
import com.trilogy.app.mom.VpnCallType;
import com.trilogy.app.mom.VpnCgpnFormatType;
import com.trilogy.app.mom.VpnCorporateHierarchy;
import com.trilogy.app.mom.VpnCorporateHierarchyLevelType;
import com.trilogy.app.mom.VpnDayOfWeekType;
import com.trilogy.app.mom.VpnDirectionType;
import com.trilogy.app.mom.VpnDnList;
import com.trilogy.app.mom.VpnDnListType;
import com.trilogy.app.mom.VpnDnNormalization;
import com.trilogy.app.mom.VpnGenericScreening;
import com.trilogy.app.mom.VpnLcr;
import com.trilogy.app.mom.VpnNdcZone;
import com.trilogy.app.mom.VpnNoaType;
import com.trilogy.app.mom.VpnNpiType;
import com.trilogy.app.mom.VpnPbxDac;
import com.trilogy.app.mom.VpnPrefix;
import com.trilogy.app.mom.VpnPrefixInheritanceType;
import com.trilogy.app.mom.VpnPublicAc;
import com.trilogy.app.mom.VpnPublicRangeTranslation;
import com.trilogy.app.mom.VpnRegion;
import com.trilogy.app.mom.VpnScreeningActionType;
import com.trilogy.app.mom.VpnScreeningListType;
import com.trilogy.app.mom.VpnScreeningOptionType;
import com.trilogy.app.mom.VpnScreeningTemplateGroup;
import com.trilogy.app.mom.VpnScreeningType;
import com.trilogy.app.mom.VpnServiceGradeType;
import com.trilogy.app.mom.VpnSiteIdType;
import com.trilogy.app.mom.VpnTemplateType;
import com.trilogy.app.mom.VpnTimeScreening;
import com.trilogy.app.mom.VpnTimeScreeningOptionType;
import com.trilogy.app.mom.VpnTonType;
import com.trilogy.app.mom.VpnUserStateType;
import com.trilogy.app.mom.WeekSchedule;
import com.trilogy.service.corba.CorbaClientProperty;
import com.trilogy.util.corba.ConnectionListener;
import com.trilogy.util.corba.CorbaClientException;
import com.trilogy.util.corba.CorbaClientProxy;
import com.trilogy.util.snippet.log.Logger;

/**
 * CORBA client for VPN Application provisioning.
 * Most of the CORBA connection logic copied from SubscriberProfileProvisionCorbaClient revision 13852.
 *
 * @author victor.stratan@redknee.com
 */
public class VpnClient implements RemoteServiceStatus, ConnectionListener
{
    public VpnClient(final Context ctx)
    {
        fallbackContext_ = new ContextAwareSupport()
        {
            // EMPTY
        };

        fallbackContext_.setContext(ctx);

        connectionProperties_ = new TimerCachedConnectionProperties(CONNECTION_PROPERTIES_KEY);

        state_ = ConnectionState.UNINITIALIZED;
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void connectionDown()
    {
        if (state_ == ConnectionState.DOWN)
        {
            return;
        }

        state_ = ConnectionState.DOWN;

        final Context ctx = fallbackContext_.getContext();

        // Note that the alive check above is also used to ensure that the
        // invalidate() call below does not recurse infinitely.
        invalidate(ctx);

        final String[] arguments = getEntryLogParameters(ctx);
        new EntryLogMsg(12654L, this, CONNECTION_PROPERTIES_KEY, "", arguments, null).log(ctx);
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void connectionUp()
    {
        if (state_ == ConnectionState.UP)
        {
            return;
        }

        state_ = ConnectionState.UP;

        final Context context = fallbackContext_.getContext();
        final String[] arguments = getEntryLogParameters(context);
        new EntryLogMsg(12655L, this, CONNECTION_PROPERTIES_KEY, "", arguments, null).log(context);
    }

    public synchronized ConnectionStatus[] getConnectionStatus()
    {
        final Context context = fallbackContext_.getContext();
        final CorbaClientProperty properties = connectionProperties_.getProperties(context);

        return SystemStatusSupportHelper.get().generateConnectionStatus(properties, state_);
    }

    public String getServiceStatus()
    {
        return SystemStatusSupportHelper.get().generateServiceStatusString(isAlive());
    }

    /**
     * {@inheritDoc}
     */
    public String getDescription()
    {
        return SERVICE_DESCRIPTION;
    }

    public String getName()
    {
        return CONNECTION_PROPERTIES_KEY;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isAlive()
    {
        return state_ == ConnectionState.UP;
    }

    public synchronized void invalidate(final Context ctx)
    {
        connectionDown();

        // looks like it's not needed
        //corbaProxy_.invalidate();
        service_ = null;
    }

    private synchronized AdministratorInterface_v4_1 getService(Context ctx)
    {
        if (connectionProperties_.updateAvailable(ctx))
        {
            if (Logger.isInfoEnabled())
            {
                Logger.info(ctx, this, "New " + CONNECTION_PROPERTIES_KEY
                        + " connection properties available. Reinitializing.");
            }

            connectionProperties_.refreshProperties(ctx);
            // Proxy only needs to be discarded when the properties change.
            corbaProxy_ = null;
            invalidate(ctx);
            initializeService(ctx);
        }
        else if (service_ == null || service_._non_existent())
        {
            if (Logger.isInfoEnabled())
            {
                Logger.info(ctx, this, CONNECTION_PROPERTIES_KEY + " Null service. Reinitializing.");
            }

            invalidate(ctx);
            initializeService(ctx);
        }

        return service_;
    }

    /**
     * Initializes the CORBA service.
     *
     * @param ctx The operating context.
     */
    private void initializeService(final Context ctx)
    {
        if (corbaProxy_ == null)
        {
            final CorbaClientProperty properties = connectionProperties_.getProperties(ctx);

            if (properties != null)
            {
                try
                {
                    corbaProxy_ = CorbaSupportHelper.get(ctx).createProxy(ctx, properties, this);
                }
                catch (final CorbaClientException exception)
                {
                    Logger.major(ctx, this, "Failure to create CORBA proxy for " + properties, exception);
                    invalidate(ctx);
                }
            }
            else
            {
                Logger.major(ctx, this, "Failed to find CORBA properties for " + CONNECTION_PROPERTIES_KEY);
                invalidate(ctx);
            }
        }

        if (corbaProxy_ != null)
        {
            org.omg.CORBA.Object objServant = null;
            try
            {
                objServant = corbaProxy_.instance();
            }
            catch (final SystemException exception)
            {
                Logger.major(ctx, this, "Failure while attempting to instantiate proxy.", exception);
                invalidate(ctx);
            }

            if (objServant != null)
            {
                try
                {
                    service_ = AdministratorInterface_v4_1Helper.narrow(objServant);

                    if (service_ != null)
                    {
                        connectionUp();
                    }
                }
                catch (final SystemException exception)
                {
                    Logger.major(ctx, this, "Failed to narrow to service.", exception);
                    invalidate(ctx);
                }
            }
            else
            {
                Logger.major(ctx, this, "Failed to instantiate proxy.");
                invalidate(ctx);
            }
        }
    }

    /**
     * Gets the EntryLog parameters for 13666(down) and 13667(up).
     *
     * @param context The operating context.
     * @return Appropriate EntryLog parameters.
     */
    private synchronized String[] getEntryLogParameters(final Context context)
    {
        final String hostname;
        final String port;

        final CorbaClientProperty properties = connectionProperties_.getProperties(context);
        if (properties != null)
        {
            final String nameServiceHost = properties.getNameServiceHost();
            if (nameServiceHost != null)
            {
                hostname = nameServiceHost;
            }
            else
            {
                hostname = "(not set)";
            }

            port = Integer.toString(properties.getNameServicePort());
        }
        else
        {
            final String noConfig = "(no config)";
            hostname = noConfig;
            port = noConfig;
        }

        final String[] parameters =
        {
            hostname,
            port,
            AdministratorInterface_v4_1.class.getSimpleName(),
            CONNECTION_PROPERTIES_KEY,
        };

        return parameters;
    }

    public String createVpnEntity(final Context ctx, final com.redknee.app.crm.bean.Subscriber subscriber)
        throws VpnClientException
    {
        // TODO 2009-11-11 continue the refactoring of methods
        String retValue = null;
        try
        {
            final AdministratorInterface_v4_1 service = getService(ctx);
            if (service == null)
            {
                throw new VpnClientException("Unable to connect to VPN application!");
            }

            final Subscriber sub = createMomSubscriber(ctx, subscriber, "", false);
            final Entity entity = new Entity();
            initialiseNonSubscribers(entity);
            entity.subscriber(EntityType.from_int(EntityType._SUBSCRIBER_ENTITY_TYPE), sub);
            final AdministratorIdentity administratorIdentity = getAdminIdentity(ctx);

            // Log what is being sent to the mom application, short for info
            if (Logger.isInfoEnabled())
            {
                final String msg = "AppVpn.createEntity [Subscriber "
                        + " vpnEntityID= " + sub.id
                        + "] "
                        + "[AdministratorIdentity"
                        + " id=" + administratorIdentity.id
                        + ", password=" + administratorIdentity.password;
                Logger.info(ctx, this, msg);
            }

            // Log what is being sent to the mom application, long for debug
            debugLogMOMSubscriberAndAdministrator(ctx, sub, administratorIdentity);

            final Entity newEntity = service.createEntity(entity, administratorIdentity);

            retValue = newEntity.subscriber().id;
        }
        catch (SystemException e)
        {
            invalidate(ctx);
            throw new VpnClientException("Create VPN Entity for Subscriber= " + subscriber.getId() + " [" + e + "]", e);
        }
        catch (EntityIdException e)
        {
            throw new VpnClientException("Create VPN Entity for Subscriber= " + subscriber.getId() + " [" + e + "]", e);
        }
        catch (BadDataException e)
        {
            throw new VpnClientException("Create VPN Entity for Subscriber= " + subscriber.getId() + " [" + e + "]", e);
        }
        catch (AuthenticationException e)
        {
            throw new VpnClientException("Create VPN Entity for Subscriber= " + subscriber.getId() + " [" + e + "]", e);
        }
        catch (VpnClientException e)
        {
            throw e;
        }
        catch (Throwable e)
        {
            final String msg = "Creating VPN Entity for Subscriber= " + subscriber.getId() + " [" + e + "]";
            Logger.minor(ctx, this, msg, e);
            throw new VpnClientException(msg, e);
        }

        return retValue;
    }

    /**
     * @param entity
     */
    private void initialiseNonSubscribers(final Entity entity)
    {
        final AlternateDn alternateDn = new AlternateDn(DEFAULT_VALUE, DEFAULT_VALUE, DEFAULT_VALUE);
        entity.alternateDn(alternateDn);

        final Administrator admin = new Administrator(DEFAULT_VALUE, DEFAULT_VALUE, DEFAULT_VALUE);
        entity.administrator(admin);

        final Announcement announce = new Announcement(DEFAULT_VALUE, DEFAULT_VALUE, DEFAULT_VALUE);
        entity.announcement(announce);

        final DigitCollectionOptions dgo = new DigitCollectionOptions(DEFAULT_VALUE, 0, 0, 0, 0);
        final Callback callback = new Callback(DEFAULT_VALUE, createDivert(DEFAULT_VALUE), createDivert(DEFAULT_VALUE),
                dgo, DEFAULT_VALUE, DEFAULT_VALUE, DEFAULT_VALUE, DEFAULT_VALUE, DEFAULT_VALUE, 0, DEFAULT_VALUE);
        entity.callback(callback);

        final String emptyString[] = {DEFAULT_VALUE};
        final DnList dnList = new DnList(DEFAULT_VALUE, emptyString, DEFAULT_VALUE);
        entity.dnList(dnList);

        final Router router = new Router(DEFAULT_VALUE, DEFAULT_VALUE, createDivert(DEFAULT_VALUE), DEFAULT_VALUE);
        entity.router(router);

        final Queue queue = new Queue(DEFAULT_VALUE, 0, createDivert(DEFAULT_VALUE));
        final Huntgroup huntGroup = new Huntgroup(DEFAULT_VALUE, DEFAULT_VALUE, false, HuntType.LINEAR_HUNT_TYPE, queue,
                emptyString, DEFAULT_VALUE);
        entity.huntgroup(huntGroup);

        final MenuOption mo = new MenuOption(DEFAULT_VALUE, createDivert(DEFAULT_VALUE));
        final MenuOption moArray[] = {mo};
        final Menu menu = new Menu(DEFAULT_VALUE, DEFAULT_VALUE, DEFAULT_VALUE, dgo, createDivert(DEFAULT_VALUE),
                moArray, DEFAULT_VALUE);
        entity.menu(menu);

        final Operator op = new Operator(DEFAULT_VALUE, DEFAULT_VALUE, DEFAULT_VALUE, DEFAULT_VALUE, false, queue,
                DEFAULT_VALUE);
        entity._operator(op);

        final Realm realm = new Realm(DEFAULT_VALUE, 0, 0, DEFAULT_VALUE);
        entity.realm(realm);

        final HourMinute hminute = new HourMinute(0, 0);
        final TimeInterval timeInterval = new TimeInterval(hminute, hminute);
        final TimeInterval tI[] = {timeInterval};
        final DaySchedule dsch = new DaySchedule(tI);
        final WeekSchedule wsch = new WeekSchedule(dsch, dsch, dsch, dsch, dsch, dsch, dsch);
        final Schedule schedule = new Schedule(DEFAULT_VALUE, ScheduleType.ALWAYS_SCHEDULE_TYPE, dsch, wsch,
                DEFAULT_VALUE);
        entity.schedule(schedule);

        final VpnAppScreening vpnScreening = new VpnAppScreening(DEFAULT_VALUE, DEFAULT_VALUE, DEFAULT_VALUE,
                VpnScreeningType.VOICE_AND_SMS_MO_VPN_SCREENING_TYPE, 0,
                VpnCallHandlingType.CONTINUE_VPN_CALL_HANDLING_TYPE, 0);
        entity.vpnAppScreening(vpnScreening);

        final VpnGenericScreening vpnGenericScreening = new VpnGenericScreening(DEFAULT_VALUE, DEFAULT_VALUE, 0,
                VpnScreeningOptionType.AT_VPN_SCREENING_OPTION_TYPE, DEFAULT_VALUE, 0,
                VpnActionType.BLOCK_VPN_ACTION_TYPE, 0,
                VpnScreeningActionType.ASSIGN_BILLING_DN_VPN_SCREENING_ACTION_TYPE, DEFAULT_VALUE,
                VpnActionType.BLOCK_VPN_ACTION_TYPE, 0);
        entity.vpnGenericScreening(vpnGenericScreening);

        final VpnTimeScreening vpnTimeScreening = new VpnTimeScreening(DEFAULT_VALUE, DEFAULT_VALUE, 0,
                VpnTimeScreeningOptionType.WEEKLY_VPN_TIME_SCREENING_OPTION_TYPE,
                0, 0, VpnDayOfWeekType.EVD_VPN_DAY_OF_WEEK_TYPE, 0, 0, 0, VpnDayOfWeekType.EVD_VPN_DAY_OF_WEEK_TYPE, 0,
                false,
                VpnActionType.BLOCK_VPN_ACTION_TYPE, 0,
                VpnScreeningActionType.ASSIGN_BILLING_DN_VPN_SCREENING_ACTION_TYPE, DEFAULT_VALUE,
                VpnActionType.BLOCK_VPN_ACTION_TYPE, 0);
        entity.vpnTimeScreening(vpnTimeScreening);

        final VpnLcr vpnLcr = new VpnLcr(DEFAULT_VALUE, DEFAULT_VALUE, DEFAULT_VALUE, DEFAULT_VALUE, DEFAULT_VALUE);
        entity.vpnLcr(vpnLcr);

        final VpnDnList vpnDnList = new VpnDnList(DEFAULT_VALUE, DEFAULT_VALUE, 0, emptyString,
                VpnDnListType.DNLIST_VPN_DN_LIST_TYPE);
        entity.vpnDnList(vpnDnList);

        final VpnScreeningTemplateGroup vpnScreeningTemplateGroup = new VpnScreeningTemplateGroup("", "", "",
                VpnTemplateType.SMS_VPN_TEMPLATE_TYPE, VpnScreeningListType.DN_SCREENING_VPN_SCREENING_LIST_TYPE);
        entity.vpnScreeningTemplateGroup(vpnScreeningTemplateGroup);

        final VpnAccessCode vpnAccessCode = new VpnAccessCode(DEFAULT_VALUE, VpnCallType.OFF_NET_VPN_CALL_TYPE,
                VpnAcActionType.ADD_PREFIX_AA_VPN_AC_ACTION_TYPE, DEFAULT_VALUE);
        final VpnAccessCode vpnAC[] = {vpnAccessCode};
        final VpnBgc vpnBgc = new VpnBgc(DEFAULT_VALUE, DEFAULT_VALUE, DEFAULT_VALUE, DEFAULT_VALUE, DEFAULT_VALUE,
                0, 0, 0, 0, 0, 0, vpnAC, VpnCallType.OFF_NET_VPN_CALL_TYPE, emptyString,
                VpnCgpnFormatType.EXTENSION_ONLY_VPN_CGPN_FORMAT_TYPE, DEFAULT_VALUE, DEFAULT_VALUE,
                VpnServiceGradeType.PO_VPN_SERVICE_GRADE_TYPE, (short) 0);
        entity.vpnBgc(vpnBgc);

        final VpnBgsc vpnBgsc = new VpnBgsc(DEFAULT_VALUE, DEFAULT_VALUE, DEFAULT_VALUE, 0,
                VpnSiteIdType.MOBILE_VPN_SITE_ID_TYPE, DEFAULT_VALUE, vpnAC);
        entity.vpnBgsc(vpnBgsc);

        final VpnPbxDac vpnPbxDac = new VpnPbxDac(DEFAULT_VALUE, DEFAULT_VALUE, DEFAULT_VALUE, DEFAULT_VALUE,
                VpnCgpnFormatType.EXTENSION_ONLY_VPN_CGPN_FORMAT_TYPE, false, 0, 0);
        entity.vpnPbxDac(vpnPbxDac);

        final VpnPublicAc vpnPublicAc = new VpnPublicAc(DEFAULT_VALUE, DEFAULT_VALUE, DEFAULT_VALUE, DEFAULT_VALUE);
        entity.vpnPublicAc(vpnPublicAc);

        final VpnBgRangeTranslation vpnBgRangeTranslation = new VpnBgRangeTranslation(DEFAULT_VALUE, DEFAULT_VALUE,
                DEFAULT_VALUE, DEFAULT_VALUE, 0, DEFAULT_VALUE);
        entity.vpnBgRangeTranslation(vpnBgRangeTranslation);

        final VpnBgSiteRangeTranslation vpnBgSiteRangeTranslation = new VpnBgSiteRangeTranslation(DEFAULT_VALUE,
                DEFAULT_VALUE, DEFAULT_VALUE, DEFAULT_VALUE, DEFAULT_VALUE, 0, DEFAULT_VALUE);
        entity.vpnBgSiteRangeTranslation(vpnBgSiteRangeTranslation);

        final VpnPublicRangeTranslation vpnPublicRangeTranslation = new VpnPublicRangeTranslation(DEFAULT_VALUE,
                DEFAULT_VALUE, DEFAULT_VALUE, DEFAULT_VALUE, DEFAULT_VALUE, 0, DEFAULT_VALUE, false, DEFAULT_VALUE);
        entity.vpnPublicRangeTranslation(vpnPublicRangeTranslation);

        final VpnCorporateHierarchy vpnCorporateHierarchy = new VpnCorporateHierarchy(DEFAULT_VALUE, DEFAULT_VALUE, 0,
                DEFAULT_VALUE, 0, VpnCorporateHierarchyLevelType.BAN_VPN_CORPORATE_HIERARCHY_LEVEL_TYPE,
                VpnScreeningListType.DN_SCREENING_VPN_SCREENING_LIST_TYPE, DEFAULT_VALUE, DEFAULT_VALUE, false);
        entity.vpnCorporateHierarchy(vpnCorporateHierarchy);

        final VpnRegion vpnRegion = new VpnRegion(DEFAULT_VALUE, DEFAULT_VALUE, DEFAULT_VALUE, emptyString);
        entity.vpnRegion(vpnRegion);

        final VpnDnNormalization vpnDnNormalization = new VpnDnNormalization(DEFAULT_VALUE, DEFAULT_VALUE, 0, 0,
                DEFAULT_VALUE, VpnTonType.ABBREVIATED_VPN_TON_TYPE, VpnNoaType.INTERNATIONAL_VPN_NOA_TYPE,
                VpnNpiType.DATA_VPN_NPI_TYPE, 0, VpnDirectionType.ANY_VPN_DIRECTION_TYPE, DEFAULT_VALUE, 0, 0,
                DEFAULT_VALUE, DEFAULT_VALUE, false, VpnTonType.ABBREVIATED_VPN_TON_TYPE,
                VpnNoaType.INTERNATIONAL_VPN_NOA_TYPE, VpnNpiType.DATA_VPN_NPI_TYPE,
                VpnPrefixInheritanceType.MSISDN_VPN_PREFIX_INHERITANCE_TYPE);
        entity.vpnDnNormalization(vpnDnNormalization);

        final VpnNdcZone vpnNdcZone = new VpnNdcZone(DEFAULT_VALUE, DEFAULT_VALUE, DEFAULT_VALUE, DEFAULT_VALUE);
        entity.vpnNdcZone(vpnNdcZone);

        final VpnPrefix vpnPrefix = new VpnPrefix(DEFAULT_VALUE, DEFAULT_VALUE, emptyString);
        entity.vpnPrefix(vpnPrefix);
    }

    public void updateVpnEntity(final Context ctx, final com.redknee.app.crm.bean.Subscriber subscriber,
            final String vpnEntityId, final boolean disable) throws VpnClientException
    {
        try
        {
            final AdministratorInterface_v4_1 service = getService(ctx);
            if (service == null)
            {
                throw new VpnClientException("Unable to connect to VPN application!");
            }

            final Subscriber sub = createMomSubscriber(ctx, subscriber, vpnEntityId, disable);
            final Entity entity = new Entity();
            entity.subscriber(EntityType.from_int(EntityType._SUBSCRIBER_ENTITY_TYPE), sub);

            final AdministratorIdentity administratorIdentity = getAdminIdentity(ctx);

            // Log what is being sent to the mom application, short for info
            if (Logger.isInfoEnabled())
            {
                final String msg = "AppVpn.updateEntity [Subscriber "
                        + " vpnEntityID= " + sub.id
                        + "] "
                        + "[AdministratorIdentity"
                        + " id=" + administratorIdentity.id
                        + ", password=" + administratorIdentity.password;
                Logger.info(ctx, this, msg);
            }

            // Log what is being sent to the mom application, long for debug
            debugLogMOMSubscriberAndAdministrator(ctx, sub, administratorIdentity);

            service.updateEntity(entity, administratorIdentity);
        }
        catch (SystemException e)
        {
            invalidate(ctx);
            throw new VpnClientException("Failed to update VPN Entity " + vpnEntityId
                    + " for Subscriber=" + subscriber.getId() + " [" + e + "]", e);
        }
        catch (EntityIdException e)
        {
            throw new VpnClientException("Failed to update VPN Entity " + vpnEntityId
                    + " for Subscriber=" + subscriber.getId() + " [" + e + "]", e);
        }
        catch (BadDataException e)
        {
            throw new VpnClientException("Failed to update VPN Entity " + vpnEntityId
                    + " for Subscriber=" + subscriber.getId() + " [" + e + "]", e);
        }
        catch (AuthenticationException e)
        {
            Logger.minor(ctx, this, "Authentication Exception", e);
            throw new VpnClientException("Failed to update VPN Entity " + vpnEntityId
                    + " for Subscriber=" + subscriber.getId() + " [" + e + "]", e);
        }
        catch (VpnClientException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            final String msg = "Failed to update VPN Entity " + vpnEntityId
            + " for Subscriber=" + subscriber.getId() + " [" + e + "]";
            Logger.minor(ctx, this, msg, e);
            throw new VpnClientException(msg, e);
        }
    }

    public void deleteVpnEntity(final Context ctx, final com.redknee.app.crm.bean.Subscriber subscriber,
            final String vpnEntityId) throws VpnClientException
    {
        try
        {
            final AdministratorInterface_v4_1 service = getService(ctx);
            if (service == null)
            {
                throw new VpnClientException("Unable to connect to VPN application!");
            }

            final AdministratorIdentity administratorIdentity = getAdminIdentity(ctx);

            // Log what is being sent to the mom application
            if (Logger.isInfoEnabled())
            {
                final String msg = "AppVpn.deleteEntity [Subscriber"
                        + " vpnEntityID= " + vpnEntityId
                        + "] "
                        + "[AdministratorIdentity"
                        + " id=" + administratorIdentity.id
                        + ", password=" + administratorIdentity.password;
                Logger.info(ctx, this, msg);
            }

            service.deleteEntity(EntityType.from_int(EntityType._SUBSCRIBER_ENTITY_TYPE), vpnEntityId,
                    administratorIdentity);
        }
        catch (SystemException e)
        {
            invalidate(ctx);
            throw new VpnClientException("Delete VPN Entity " + vpnEntityId
                    + " for Subscriber=" + subscriber.getId() + " failed. " + e, e);
        }
        catch (VpnClientException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            final String msg = "Delete VPN Entity " + vpnEntityId
                    + " for Subscriber=" + subscriber.getId() + " failed. " + e;
            Logger.minor(ctx, this, msg, e);
            throw new VpnClientException(msg, e);
        }
    }

    /**
     * This method disables the VPN entity for the subscriber. This method will not delete the VPN entity rather makes
     * the state of the subscriber on AppVpn to suspended.
     * @param ctx the operating context
     * @param subscriber CRM Subscriber object
     *
     * @throws VpnClientException exception
     */
    public void disableVpnEntity(final Context ctx, final com.redknee.app.crm.bean.Subscriber subscriber,
            final String vpnEntityId) throws VpnClientException
    {
        // TODO 2009-11-09 substitute this method with direct calls to updateVpnEntity()
        this.updateVpnEntity(ctx, subscriber, vpnEntityId, true);

        if (Logger.isInfoEnabled())
        {
            Logger.info(ctx, this, " Successfully disabled VPN Entity for subscriber = " + subscriber.getId());
        }
    }

    /**
     * This method enables the VPN entity for the subscriber. This method will not delete the VPN entity
     * but rather change the state of the subscriber on AppVpn to Active.
     * @param ctx the operating context
     * @param subscriber CRM Subscriber object
     *
     * @throws VpnClientException exception
     */
    public void enableVpnEntity(final Context ctx, final com.redknee.app.crm.bean.Subscriber subscriber,
            final String vpnEntityId) throws VpnClientException
    {
        // TODO 2009-11-09 substitute this method with direct calls to updateVpnEntity()
        this.updateVpnEntity(ctx, subscriber, vpnEntityId, false);

        if (Logger.isInfoEnabled())
        {
            Logger.info(ctx, this, " Successfully Enabled VPN Entity for subscriber = " + subscriber.getId());
        }
    }

    private Divert createDivert(final String subscriber)
    {
        final Divert divert = new Divert();
        divert.alternateDnId(DivertType.ALTERNATE_DN_DIVERT_TYPE, DEFAULT_VALUE);
        divert.announcementId(DivertType.ANNOUNCEMENT_DIVERT_TYPE, DEFAULT_VALUE);
        divert.callbackId(DivertType.CALLBACK_DIVERT_TYPE, DEFAULT_VALUE);
        divert.routerId(DivertType.ROUTER_DIVERT_TYPE, DEFAULT_VALUE);
        divert.dn(DivertType.DN_DIVERT_DIVERT_TYPE, DEFAULT_VALUE);
        divert.huntgroupId(DivertType.HUNTGROUP_DIVERT_TYPE, DEFAULT_VALUE);
        divert.menuId(DivertType.MENU_DIVERT_TYPE, DEFAULT_VALUE);
        divert.operatorHandsetId(DivertType.OPERATOR_HANDSET_DIVERT_TYPE, DEFAULT_VALUE);
        divert.operatorQueueId(DivertType.OPERATOR_QUEUE_DIVERT_TYPE, DEFAULT_VALUE);
        divert.subscriberId(DivertType.SUBSCRIBER_DIVERT_TYPE, subscriber);
        return divert;
    }

    public Subscriber createMomSubscriber(final Context ctx, final com.redknee.app.crm.bean.Subscriber subscriber,
            final String vpnEntityId, final boolean disable) throws VpnClientException
    {
        final Subscriber sub = new Subscriber();
        sub.id = subscriber.getId();

        Account account = null;
        try
        {
            account = AccountSupport.getAccount(ctx, subscriber.getBAN());
            if (account.getFirstName() != null && account.getFirstName().length() > 0)
            {
                sub.firstName = account.getFirstName();
            }
            else
            {
                sub.firstName = DEFAULT_VALUE;
            }
            if (account.getLastName() != null && account.getLastName().length() > 0)
            {
                sub.lastName = account.getLastName();
            }
            else
            {
                sub.lastName = DEFAULT_VALUE;
            }
        }
        catch (HomeException e)
        {
            throw new VpnClientException("Error while getting subscriber account " + subscriber.getBAN(), e);
        }

        final Divert divert = createDivert(subscriber.getId());
        try
        {
            // now it will be root account
            account = account.getRootAccount(ctx);
        }
        catch (HomeException e)
        {
            throw new VpnClientException("Error while getting Root account for " + subscriber.getBAN(), e);
        }

        sub.publicDn = subscriber.getMSISDN();
        sub.privateDn = "";
        sub.voiceMailDn = DEFAULT_VALUE;
        sub.realmId = account.getBAN();
        sub.password = DEFAULT_VALUE;
        sub.languageSet = DEFAULT_VALUE;
        sub.skillSet = DEFAULT_VALUE;
        sub.bearerCapability = BearerCapabilityType.from_int(0);
        sub.unreachableDivert = divert;
        sub.overrideAvailabilityStatus = AvailabilityStatusType.AVAILABLE_AVAILABILITY_STATUS_TYPE;
        sub.availabilityScheduleId = DEFAULT_VALUE;
        sub.dndScheduleId = DEFAULT_VALUE;
        sub.dndDivert = divert;
        sub.blockedDivert = divert;
        final String[] emptyArray = {DEFAULT_VALUE};
        sub.dndAllowedDnListIdList = emptyArray;
        sub.blockedDnListIdList = emptyArray;
        if (disable)
        {
            sub.state = VpnUserStateType.SUSPENDED_VPN_USER_STATE_TYPE;
        }
        else
        {
            sub.state = VpnUserStateType.ACTIVE_VPN_USER_STATE_TYPE;
        }
        sub.feature = DEFAULT_VALUE;
        sub.siteCode = DEFAULT_VALUE;
        sub.prefix = DEFAULT_VALUE;
        sub.subgroup = DEFAULT_VALUE;
        sub.hotLineDn = DEFAULT_VALUE;
        sub.billingNumber = account.getVpnMSISDN();
        sub.templateName = DEFAULT_VALUE;
        sub.smsTemplateName = DEFAULT_VALUE;
        sub.enhancedAccessCode = new VpnAccessCode(DEFAULT_VALUE, VpnCallType.from_int(0), VpnAcActionType.from_int(0),
                DEFAULT_VALUE);
        sub.id = vpnEntityId;
        sub.vpnEnabled = account.getVpn();
        sub.icmEnabled = account.getIcm();

        return sub;
    }

    /**
     * Return the MOM Entity with the given entity ID
     * @param ctx the operating context
     * @param vpnEntityID ID of the Entity to retrieve
     *
     * @return
     * @throws VpnClientException
     */
    public Entity retrieveMomEntity(final Context ctx, final String vpnEntityID) throws VpnClientException
    {
        MaybeEntity returnedEntity = null;
        try
        {
            final AdministratorInterface_v4_1 service = getService(ctx);
            if (service == null)
            {
                throw new VpnClientException("Unable to connect to VPN application!");
            }

            final AdministratorIdentity administratorIdentity = getAdminIdentity(ctx);

            // Log what is being sent to the mom application
            if (Logger.isInfoEnabled())
            {
                final String msg = "AppVpn.retrieveEntity [Subscriber "
                        + " vpnEntityID= " + vpnEntityID
                        + "] "
                        + "[AdministratorIdentity"
                        + " id=" + administratorIdentity.id
                        + ", password=" + administratorIdentity.password;
                Logger.info(ctx, this, msg);
            }

            returnedEntity = service.retrieveEntity(EntityType.SUBSCRIBER_ENTITY_TYPE, vpnEntityID,
                    administratorIdentity);
        }
        catch (SystemException e)
        {
            invalidate(ctx);
            throw new VpnClientException("Retrieve VPN Entity " + vpnEntityID + " failed: " + e, e);
        }
        catch (ConfigurationException e)
        {
            final String msg = "Retrieve VPN Entity " + vpnEntityID + " failed: " + e;
            Logger.minor(ctx, this, msg, e);
            throw new VpnClientException(msg, e);
        }
        catch (InternalException e)
        {
            final String msg = "Retrieve VPN Entity " + vpnEntityID + " failed: " + e;
            Logger.minor(ctx, this, msg, e);
            throw new VpnClientException(msg, e);
        }
        catch (AuthenticationException e)
        {
            final String msg = "Retrieve VPN Entity " + vpnEntityID + " failed: " + e;
            Logger.minor(ctx, this, msg, e);
            throw new VpnClientException(msg, e);
        }
        catch (VpnClientException e)
        {
            throw e;
        }
        catch (Throwable e)
        {
            final String msg = "Retrieve VPN Entity " + vpnEntityID + " failed: " + e;
            Logger.minor(ctx, this, msg, e);
            throw new VpnClientException(msg, e);
        }
        return (returnedEntity != null ? returnedEntity.entity() : null);
    }

    /**
     * Utility method that creates AdministratorIdentity bean.
     *
     * @param ctx the operating context
     * @return AdministratorIdentity bean for CORBA calls
     */
    private AdministratorIdentity getAdminIdentity(final Context ctx)
    {
        final CorbaClientProperty connectionProperties = connectionProperties_.getProperties(ctx);
        final AdministratorIdentity administratorIdentity = new AdministratorIdentity();
        administratorIdentity.id = connectionProperties.getUsername();
        administratorIdentity.password = connectionProperties.getPassword();
        return administratorIdentity;
    }

    private void debugLogMOMSubscriberAndAdministrator(final Context ctx, final Subscriber sub,
            final AdministratorIdentity administratorIdentity)
    {
        if (Logger.isDebugEnabled())
        {
            final String msg = "[Subscriber"
                    + " vpnEntityID= " + sub.id
                    + ", publicDn=" + sub.publicDn
                    + ", privateDn=" + sub.privateDn
                    + ", voiceMailDn=" + sub.voiceMailDn
                    + ", firstName=" + sub.firstName
                    + ", lastName=" + sub.lastName
                    + ", realId=" + sub.realmId
                    + ", password=" + sub.password
                    + ", languageSet=" + sub.languageSet
                    + ", skillSet=" + sub.skillSet
                    + ", bearerCapability=" + sub.bearerCapability
                    + ", unreachableDivert=" + sub.unreachableDivert
                    + ", overrideAvailabilityStatusType=" + sub.overrideAvailabilityStatus
                    + ", availabilityScheduleId=" + sub.availabilityScheduleId
                    + ", dndScheduleId=" + sub.dndScheduleId
                    + ", dndDivert=" + sub.dndDivert
                    + ", blockedDivert" + sub.blockedDivert
                    + ", dndAllowedDnListIdList=" + Arrays.toString(sub.dndAllowedDnListIdList)
                    + ", blockedDnListIdList=" + Arrays.toString(sub.blockedDnListIdList)
                    + ", state=" + sub.state
                    + ", feature=" + sub.feature
                    + ", siteCode=" + sub.siteCode
                    + ", prefix=" + sub.prefix
                    + ", subgroup=" + sub.subgroup
                    + ", hotLineDn=" + sub.hotLineDn
                    + ", billingNumber=" + sub.billingNumber
                    + ", templateName=" + sub.templateName
                    + ", smsTemplateName=" + sub.smsTemplateName
                    + ", enhancedAccessCode=" + sub.enhancedAccessCode
                    + ", vpnEnabled=" + sub.vpnEnabled
                    + ", icmEnabled=" + sub.icmEnabled
                    + "] "
                    + "[AdministratorIdentity"
                    + " id=" + administratorIdentity.id
                    + ", password=" + administratorIdentity.password
                    + "]";
            Logger.debug(ctx, this, msg);
        }
    }

    private static final String DEFAULT_VALUE = "empty";

    /**
     * The current state of this client.
     */
    private ConnectionState state_;

    /**
     * The underlying CORBA service.
     */
    private AdministratorInterface_v4_1 service_;

    /**
     * Provides the context used during object initialization, which is only
     * retained for logging purposes within methods that are not
     * context-oriented. Prefer local contexts where available.
     */
    private final ContextAwareSupport fallbackContext_;

    /**
     * Provides a convenient method of caching the look-up of CORBA connection
     * properties, and of determining when these properties have been updated.
     */
    private final ConnectionProperties connectionProperties_;

    /**
     * The CORBA proxy.
     */
    private CorbaClientProxy corbaProxy_;

    /**
     * The name of the key used to look-up the connection properties in the CorbaClientProperty Home.
     */
    private static final String CONNECTION_PROPERTIES_KEY = "AppVpn";

    /**
     * The service description string.
     */
    private static final String SERVICE_DESCRIPTION = "CORBA client for provisioning VPN subscribers";

    /**
     * Suffix for exception message used when no connection is available.
     */
    private static final String NO_CONNECTION_MESSAGE_SUFFIX = ": could not establish a connection to VPN.";

    /**
     * Suffix for exception message used then the connection fails.
     */
    private static final String FAILED_MESSAGE_SUFFIX = " failed.";
}
