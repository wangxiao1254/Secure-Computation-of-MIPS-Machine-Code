/**
 * 
 */
package com.appcomsci.sfe.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A simple property manager.
 * @author Chris Lott
 *
 */
public class SfeProperties {

	private Properties properties = new Properties();

	private Logger logger = Logger.getLogger(SfeProperties.class.getName());

	/**
	 * Populates itself from the specified input stream.
	 * @param inStream
	 * @throws IOException
	 *             if an error occurred when reading from the input stream.
	 */
	public SfeProperties(InputStream inStream) throws IOException,
			IllegalArgumentException {
		properties.load(inStream);
	}
	
	/**
	 * Empty constructor for emergencies
	 */
	public SfeProperties() {
	}

	/**
	 * Logs the property key/value pairs to the specified logger at level
	 * CONFIG.
	 * 
	 * @param logger
	 */
	public void logProperties(Logger logger) {
		for (Enumeration<?> e = properties.propertyNames(); e.hasMoreElements();) {
			String key = e.nextElement().toString();
			String value = properties.getProperty(key).toString();
			logger.config("Property " + key + "=" + value);
		}
	}

	/**
	 * @return a set of String property names
	 */
	public Set<String> propertyNames() {
		return properties.stringPropertyNames();
	}

	/**
	 * Gets boolean-valued property. Logs a warning if the value cannot be
	 * converted suitably.
	 * 
	 * @param prop
	 * @param defaultValue
	 * @return boolean from properties file; default value if not found.
	 */
	public boolean getProperty(String prop, boolean defaultValue) {
		String value = properties.getProperty(prop);
		if (value != null)
			return new Boolean(value.trim()).booleanValue();
		return defaultValue;
	}

	/**
	 * Gets a double-valued property. Logs a warning if value in properties
	 * cannot be converted suitably.
	 * 
	 * @param prop
	 * @param defaultValue
	 * @return double from properties file; default value if not found or value
	 *         cannot be converted.
	 */
	public double getProperty(String prop, double defaultValue) {
		String value = properties.getProperty(prop);
		if (value != null) {
			try {
				return Double.parseDouble(value.trim());
			} catch (NumberFormatException ex) {
				logger.log(Level.WARNING,
						"Invalid value for property " + prop, ex);
			}
		}
		return defaultValue;
	}

	/**
	 * Gets a long-valued property. Logs a warning if value in properties cannot
	 * be converted suitably.
	 * 
	 * @param prop
	 * @param defaultValue
	 * @return long from properties file; default value if not found or value
	 *         cannot be converted.
	 */
	public long getProperty(String prop, long defaultValue) {
		String value = properties.getProperty(prop);
		if (value != null) {
			try {
				return Long.parseLong(value.trim());
			} catch (NumberFormatException ex) {
				logger.log(Level.WARNING,
						"Invalid value for property " + prop, ex);
			}
		}
		return defaultValue;
	}

	/**
	 * Gets a long-valued property after converting from string using the
	 * specified radix.  Logs a warning if value in properties cannot be
	 * converted suitably.
	 * 
	 * @param prop
	 * @param defaultValue
	 * @param radix
	 * @return long from properties file; default value if not found or value
	 *         cannot be converted.
	 */
	public long getProperty(String prop, long defaultValue, int radix) {
		String value = properties.getProperty(prop);
		if (value != null) {
			try {
				return Long.parseLong(value.trim(), radix);
			} catch (NumberFormatException ex) {
				logger.log(Level.WARNING,
						"Invalid value for property " + prop, ex);
			}
		}
		return defaultValue;
	}

	/**
	 * Gets an int-valued property. Logs a warning if value in properties cannot
	 * be converted suitably.
	 * 
	 * @param prop
	 * @param defaultValue
	 * @return int from properties file; default value if not found or value
	 *         cannot be converted.
	 */
	public int getProperty(String prop, int defaultValue) {
		String value = properties.getProperty(prop);
		if (value != null) {
			try {
				return Integer.parseInt(value.trim());
			} catch (NumberFormatException ex) {
				logger.log(Level.WARNING,
						"Invalid value for property " + prop, ex);
			}
		}
		return defaultValue;
	}

	/**
	 * Gets a Logger.Level-valued property. Logs a warning if the value cannot
	 * be converted suitably.
	 * 
	 * @param prop
	 * @param defaultValue
	 * @return Level from properties file; default value if not found.
	 */
	public Level getProperty(String prop, Level defaultValue) {
		String value = properties.getProperty(prop);
		if (value != null)
			try {
				return Level.parse(value.trim());
			} catch (IllegalArgumentException ex) {
				logger.log(Level.WARNING,
						"Invalid value for property " + prop, ex);
			}
		return defaultValue;
	}

	/**
	 * Gets the string-valued property, null if not found.
	 * 
	 * @param prop
	 * @return Result of looking up the specified property.
	 */
	public String getProperty(String prop) {
		String result = properties.getProperty(prop);
		if (result != null)
			result = result.trim();
		return result;
	}

	/**
	 * Gets a string-valued property and trims whitespace. The properties code
	 * drops the leading whitespace (after =, before first nonblank) so it
	 * doesn't make sense to preserve trailing whitespace.
	 * 
	 * @param prop
	 * @param defaultValue
	 * @return String from properties file; default value if not found.
	 */
	public String getProperty(String prop, String defaultValue) {
		String result = properties.getProperty(prop, defaultValue);
		if (result != null)
			result = result.trim();
		return result;
	}

	/**
	 * Gets a string-valued property and splits into parts using comma as the
	 * separator.
	 * 
	 * @param prop
	 * @return Array of string from properties file; null if not found; empty
	 *         array is possible.
	 */
	public String[] getPropertyArray(String prop) {
		String list = getProperty(prop);
		if (list == null)
			return null;
		String[] values = list.split(",");
		return values;
	}
}
