package com.trilogy.app.crm.subscriber.charge.handler;

public interface HandlerConstants {

    public static final int HANDLER_TYPE_LOGGING_HANDLER =0; 
    public static final int HANDLER_TYPE_PREPAID_HANDLER =1; 
    public static final int HANDLER_TYPE_REMOVE_AUXSERVICE_HANDLER =2; 
    public static final int HANDLER_TYPE_SUSPENDED_ENTITY_RELEASING_HANDLER =3; 
    public static final int HANDLER_TYPE_SUSPEND_BUNDLE_HANDLER =4; 
    public static final int HANDLER_TYPE_SUSPEND_PACKAGE_HANDLER =5; 
    public static final int HANDLER_TYPE_SUSPEND_SERVICE_HANDLER =6; 
    public static final int HANDLER_TYPE_SKIPPED_CHARGE_HANDLER =7;
    public static final int HANDLER_TYPE_IGNORE_UNAPPLICABLE_TRANSACTION_HANDLER =8;
    public static final int HANDLER_TYPE_SUSPEND_POSTPAID_BUNDLE_HANDLER =9; 
    public static final int HANDLER_TYPE_SUSPEND_POSTPAID_SERVICE_HANDLER =10; 
    
    public final static Class[] HANDLER_CLASSES =  {
        LoggingHandler.class, 
        PrepaidHandler.class,
        RemoveAuxServiceHandler.class,
        SuspendedEntityReleaseHandler.class,
        SuspendingBundleHandler.class, 
        SuspendingPackageHandler.class,
        SuspendingServiceHandler.class, 
        SkippedChargingTypeHandler.class, 
        IgnoreUnapplicableTransactionHandler.class,
        SuspendingPostpaidBundleHandler.class,
        SuspendingPostpaidServiceHandler.class,
    };
}
