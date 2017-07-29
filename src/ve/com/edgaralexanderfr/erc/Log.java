package ve.com.edgaralexanderfr.erc;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Log {
	private static Log log                 = null;
	
	private StringBuilder logs             = new StringBuilder("");
	private String timeFormatString        = "HH:mm - ";
	private DateFormat timeFormat          = new SimpleDateFormat(this.timeFormatString, Locale.US);
	private LogWriteListener writeListener = null;
	
	/**
	 * Returns a global instance for a Log object.
	 * 
	 * @return {Log}.
	 */
	public static Log get () {
		return (log != null) ? log : (log = new Log()) ;
	}
	
	/**
	 * Returns the logs lines.
	 * 
	 * @return {String}.
	 */
	public static String getLines () {
		return get().getLogs();
	}
	
	/**
	 * Establishes the LogWriteListener to use for invoke the writeListener method each 
	 * time a new log is written.
	 * 
	 * @param {LogWriteListener} writeListener LogWriteListener to set.
	 */
	public static void setGlobalWriteListener (LogWriteListener writeListener) {
		get().setWriteListener(writeListener);
	}
	
	/**
	 * Registers a new log starting with the registration time and separating it from  
	 * the log message itself.
	 * 
	 * @param {String} log Log to register.
	 */
	public static void write (final String log) {
		get().register(log);
	}
	
	/**
	 * Erases all the written logs and calls the onLogWritten method if the writeListener 
	 * is not null.
	 */
	public static void clean () {
		get().clear();
	}
	
	/**
	 * Returns the logs lines.
	 * 
	 * @return {String}.
	 */
	public String getLogs () {
		return this.logs.toString();
	}
	
	/**
	 * Returns the time format string to use when a new log is registered with time.
	 * 
	 * @return {String}.
	 */
	public String getTimeFormatString () {
		return this.timeFormatString;
	}
	
	/**
	 * Establishes the time format string to use when a new log is registered with time.
	 * 
	 * @param {String} timeFormatString Time format string to set.
	 */
	public void setTimeFormatString (final String timeFormatString) {
		this.timeFormatString = timeFormatString;
		this.timeFormat       = new SimpleDateFormat(this.timeFormatString, Locale.US);
	}
	
	/**
	 * Establishes the LogWriteListener to use for invoke the writeListener method each 
	 * time a new log is written.
	 * 
	 * @param {LogWriteListener} writeListener LogWriteListener to set.
	 */
	public void setWriteListener (LogWriteListener writeListener) {
		this.writeListener = writeListener;
	}
	
	/**
	 * Prepends a new log line and calls the onLogWritten method if the writeListener is 
	 * not null.
	 * 
	 * @param {String} log Log to prepend.
	 */
	public void writeLine (final String log) {
		this.logs.insert(0, log + System.getProperty("line.separator"));
		
		if (this.writeListener != null) {
			this.writeListener.onLogWritten(log);
		}
	}
	
	/**
	 * Registers a new log starting with the registration time and separating it from  
	 * the log message itself.
	 * 
	 * @param {String} log Log to register.
	 */
	public void register (final String log) {
		String time = this.timeFormat.format(Calendar.getInstance().getTime());
		this.writeLine(time + log);
	}
	
	/**
	 * Erases all the written logs and calls the onLogWritten method if the writeListener 
	 * is not null.
	 */
	public void clear () {
		this.logs = new StringBuilder("");
		
		if (this.writeListener != null) {
			this.writeListener.onLogWritten("");
		}
	}
}
