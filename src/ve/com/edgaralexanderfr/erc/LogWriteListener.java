package ve.com.edgaralexanderfr.erc;

public interface LogWriteListener {
	/**
	 * Invoked when a log is written.
	 * 
	 * @param {String} log Written log.
	 */
	public void onLogWritten(String log);
}
