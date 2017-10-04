package com.trilogy.app.crm.bulkprovisioning;
/**
 * 
 * @author simar.singh@redknee.com
 * 
 */
public interface PRBTTelnetConstants
{

    public static final String DEFAULT_INIT_STRING = "";
    public static final String DEFAULT_PRBT_PROMPT_PATTERN = ".*PRBT>.*";
    public final static String DEFAULT_HEART_BEATING_REPLY_PATTERN = ".*<.*";
    public final static String DEFAULT_CONFIRM_COMMAND = ";";
    public static final String DEFAULT_EXIT_COMMAND = "exit";
    public final static String DEFAULT_HEART_BEATING_CMD = "\r\n";
    public final static String DEFAULT_TELNET_LOGIN_PATTERN = ".*Login:.*";
    public final static String DEFAULT_TELNET_LOGIN_PASSWORD_PATTERN = ".*Password:.*";
    public final static String DEFAULT_HLR_USERCODE_PROPMPT_PATTERN = ".*USERCODE:.*";
    public final static String DEFAULT_HLR_PASSWORD_PROMPT_PATTERN = ".*PASSWORD:.*";
    public final static String DEFAULT_HLR_DOMAIN_PROMPT_PATTERN = ".*DOMAIN:.*";
    public static final String DEFAULT_COMMAND_RESPONSE_PATTERN = "(.*[0-9]:.*:.*)|(.*[0-9][0-9]:.*:.*)";
    public final static String DEFAULT_EXECUTED_RESP = "EXECUTED";
    public final static String DEFAULT_NOT_ACCEPTED_RESP = "NOT ACCEPTED";
    public final static String DEFAULT_SYNTAX_FAULT_RESP = "SYNTAX FAULT";
    public final static int DEFAULT_HLR_TASK_POOL_SIZE = 50;
    public static final Object APG_NEW_LINE_SEPERATOR_KEY = "APG_NEW_LINE_SEPERATOR_KEY";
    
    
}
