package com.trilogy.app.crm.account.foo;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.smartlead.common.utils.StringUtilsSL;

public class StringUtil {

    private static final Logger LOGGER = LogManager.getLogger(StringUtil.class);
    public static final String TRUE = "true";
    public static final String FALSE = "false";

    public static BigDecimal convertToBigDecimal(String value) {
        if (StringUtils.isNotEmpty(value)) {
            try {
                return new BigDecimal(value);
            } catch (Throwable e) {
                LOGGER.error(e);
            }
        }

        return null;
    }

    public static Boolean convertToBoolean(String value) {
        return Boolean.valueOf(value);
    }

    public static Long convertToLong(String value) {
        if (StringUtils.isNotEmpty(value)) {
            try {
                return Long.valueOf(value);
            } catch (Throwable e) {
                LOGGER.error(e);
            }
        }

        return null;
    }

    public static Short convertToShort(String value) {
        if (StringUtils.isNotEmpty(value)) {
            try {
                return Short.valueOf(value);
            } catch (Throwable e) {
                LOGGER.error(e);
            }
        }

        return null;
    }

    public static String cleanFlexibleXpath(String input) {
        input = StringUtils.replace(input, "*[local-name()", StringUtilsSL.EMPTY);
        input = StringUtils.replace(input, "']", StringUtilsSL.EMPTY);
        input = StringUtils.replace(input, " ", StringUtilsSL.EMPTY);
        input = StringUtils.replace(input, "=", StringUtilsSL.EMPTY);
        input = StringUtils.replace(input, "'", StringUtilsSL.EMPTY);
        return input;
    }

    /**
     * Null safe way to normalize any input to contain only numeric character
     *
     * @param val string to be normalized
     * @return normalized string
     */
    public static String parseNumeric(String val) {
        if (val != null) {
            return val.trim().replaceAll("[^\\d]", StringUtilsSL.EMPTY);
        }

        return null;
    }

    /**
     * This method will strip all non-numeric characters from the string
     *
     * @param val string from which non-numeric characters to be removed
     * @return string containing only numbers
     */
    public static String stripNonNumericChar(String val) {
        if (val != null) {
            val = val.replaceAll("[^\\d]", StringUtilsSL.EMPTY);
        }

        return val;
    }

    /**
     * This method transform simple wildcard (*, ?) to regular expression.
     *
     * @param wildcard - string with wildcard
     * @return - regular expression for the incoming wildcard
     */
    public static String wildcardToRegex(String wildcard) {
        StringBuffer sb = new StringBuffer(wildcard.length());
        sb.append('^');
        for (int i = 0, is = wildcard.length(); i < is; i++) {
            char c = wildcard.charAt(i);
            switch (c) {
                case '*':
                    sb.append(".*");
                    break;
                case '?':
                    sb.append(StringUtilsSL.DOT);
                    break;
                // escape special regexp-characters
                case '(':
                case ')':
                case '[':
                case ']':
                case '$':
                case '^':
                case '.':
                case '{':
                case '}':
                case '|':
                case '\\':
                    sb.append("\\");
                    sb.append(c);
                    break;
                default:
                    sb.append(c);
                    break;
            }
        }
        sb.append('$');
        return sb.toString();
    }

    /**
     * This method remove all non-ascii characters from the input string
     *
     * @param val string from which all non-ascii characters need to be removed.
     * @return string containing only ascii characters
     */
    public static String removeAllNonAsciiCharacters(String val) {
        if (val != null) {
            val = val.replaceAll(StringUtilsSL.NON_ASCII_CHARS, StringUtilsSL.EMPTY);
        }

        return val;
    }

    /**
     * This method will convert integers sent in string form "100-102,300" to integer array [100,101,102,300]
     *
     * @param intsInString integers sent as String
     * @return list of integers
     */
    public static List<Integer> parseIntegers(String intsInString) {

        List<Integer> result = new ArrayList<>();

        if (StringUtils.isNotEmpty(intsInString)) {
            try {
                // split by comma first
                String[] intStrArray = intsInString.split(StringUtilsSL.COMMA);

                for (String intStr : intStrArray) {
                    if (intStr.contains(StringUtilsSL.DASH)) {
                        // split by comma dash
                        String[] rangeArray = intStr.split(StringUtilsSL.DASH);
                        int start = Integer.parseInt(rangeArray[0].trim());
                        int end = Integer.parseInt(rangeArray[1].trim());

                        while (start <= end) {
                            result.add(start++);
                        }
                    } else {
                        result.add(Integer.parseInt(intStr.trim()));
                    }
                }
            } catch (Exception ex) {
                LOGGER.error("Error parsing string[{}] to integer array", intsInString, ex);
            }
        }

        return result;

    }

    /**
     * This method checks if a string matches with one of many strings
     *
     * @param str     String to match
     * @param strings array of strings to match against.
     * @return true if the string matches against any 1 / false otherwise.
     */
    public static boolean equalsIgnoreCase(String str, String[] strings) {
        if (str != null) {
            for (String s : strings) {
                if (str.equalsIgnoreCase(s)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * This method returns concatenated values of multiple strings and uses blank string for those that have null
     * values.
     *
     * @param str var args indicating any number of strings that need to be concatenated.
     * @return concatenated value
     */
    public static String concatWithNullHandling(String... str) {
        String concatenatedValue = StringUtils.EMPTY;
        for (String s : str) {
            concatenatedValue = (s != null) ? (concatenatedValue + s) : concatenatedValue;
        }
        return concatenatedValue;
    }

}