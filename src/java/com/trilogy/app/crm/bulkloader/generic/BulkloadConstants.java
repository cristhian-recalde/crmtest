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
package com.trilogy.app.crm.bulkloader.generic;

/**
 * Constants used in the Bulkloader implementation
 * @author angie.li@redknee.com
 *
 */
public interface BulkloadConstants 
{

    /**
     * Default Bulkloader delimiter
     */
    public static final char DEFAULT_DELIMITER = ',';
    
    /**
     * Key for Message Manager Date Format.
     */
    static final String MSG_MGR_KEY = "Bulkload.Input.DateFormat";
    
    /** 
     * The default format for the data in this web control 
     */
    public final String DEFAULT_DATE_FORMAT_PATTERN = "yyyy/MM/dd";

    /**
     * Generic Bean Bulkloader Module name
     */
    public final String GENERIC_BULKLOADER_MODULE = "GenericBeanBulkloadManager";

    /**
     * PM: Bean bulkload (includes the CSV command Parse and Home Operation action execution)
     */
    public final String PM_BULKLOAD_RECORD = "Bean Bulk Load";

    /**
     * PM: Parse CSV input 
     */
    public final String PM_PARSE_RECORD = "CSV Input Parse";
    
    /**
     * PM: Parse CSV input activity: Build Search Bean
     */
    public final String PM_PARSE_BUILD_FIND_CONDITION = "CSV Input Parse: Build Find Condition";
    
    /**
     * PM: Parse CSV input activity: Build Search Bean
     */
    public final String PM_PARSE_BUILD_SEARCH_BEAN = "CSV Input Parse: Build Search Bean";
    
    /**
     * PM: Parse CSV input activity:  Bean Retrieval
     */
    public final String PM_PARSE_LOOKUP_BEAN = "CSV Input Parse: Bean Retrieval";
    
    /**
     * PM: Parse CSV input activity:  Modifying the Bean
     */
    public final String PM_PARSE_MODIFY_BEAN = "CSV Input Parse: Modify Bean";
    
    /**
     * PM: Bean Home operation execution (only done if there is a bean exists to update)
     */
    public final String PM_HOME_OPERATION_RECORD = "Home Operation Execution";
    
    /**
     * PM: Measures how long it took to shutdown the threadpool once all the bulkloading tasks had been launched.
     */
    public final String PM_BULKLOAD_PRODUCER_AGENT_SHUTDOWN = "Bulkloader ThreadPool shutdown";
    
    /**
     * PM: Measures how long the Logger Agent Release takes.
     */
    public final String PM_LOGGER_RELEASE = "Bulkloader Logger LifecycleControlAgent Release";
    
    /**
     * OM: Attempt on bean bulkload (includes the CSV command Parse and Home Operation action execution)
     */
    public final String OM_BULKLOAD_ATTEMPT = "Bean Bulk Load: ATTEMPT";
    
    /**
     * OM: Successful bean bulkload (includes the CSV command Parse and Home Operation action execution)
     */
    public final String OM_BULKLOAD_SUCCESS = "Bean Bulk Load: SUCCESS";
    
    /**
     * OM: Failed bean bulkload (includes the CSV command Parse and Home Operation action execution)
     */
    public final String OM_BULKLOAD_FAILURE = "Bean Bulk Load: FAILURE";
    
    /**
     * OM: Attempt to parse CSV input 
     */
    public final String OM_PARSE_ATTEMPT = "CSV Input Parse: ATTEMPT";

    /**
     * OM: Successful parse of CSV input
     */
    public final String OM_PARSE_SUCCESS = "CSV Input Parse: SUCCESS";
    
    /**
     * OM: Failed parse of CSV input
     */
    public final String OM_PARSE_FAILURE = "CSV Input Parse: FAILURE";

    /**
     * OM: Attempt on Bean Home operation execution
     */
    public final String OM_HOME_OPERATION_ATTEMPT = "Home Operation Execution: ATTEMPT";
    
    /**
     * OM: Successful bean Bean Home operation execution (Only counted if parse was completed successfully)
     */
    public final String OM_HOME_OPERATION_SUCCESS = "Home Operation Execution: SUCCESS";
    
    /**
     * OM: Failed bean Bean Home operation execution (Only counted if parse was completed successfully)
     */
    public final String OM_HOME_OPERATION_FAILURE = "Home Operation Execution: FAILURE";
    
    /**
     * Generic Bean Bulkloader Log File Logger Configuration key
     */
    public final String GENERIC_BULKLOADER_LOG_FILE_LOGGER = "GenericBeanBulkloadProgressLog";
    
    /**
     * Generic Bean Bulkloader Error File Logger Configuration key
     */
    public final String GENERIC_BULKLOADER_ERROR_FILE_LOGGER = "GenericBeanBulkloadErrorLog";

    /**
     * Context Key (identifier) for Bulkloader Session ID. Used to pass parameters from the Manager to the Agent.
     */
    public static final String GENERIC_BEAN_BULKLOAD_SESSION_ID = "GENERIC_BEAN_BULKLOAD_SESSION_ID";

    /**
     * Context Key (identifier) for Bulkloader CSV command.  Used to pass parameters from the Manager to the Agent.
     */
    public static final String GENERIC_BEAN_BULKLOAD_CSV_COMMAND = "GENERIC_BEAN_BULKLOAD_CSV_COMMAND";
    
    
}
