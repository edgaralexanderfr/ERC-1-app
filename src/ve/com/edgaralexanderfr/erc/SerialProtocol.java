package ve.com.edgaralexanderfr.erc;

public interface SerialProtocol {
	/**
	 * Invoked when the connection attempt is successful.
	 */
	public void onConnectionSuccess();
	
	/**
	 * Invoked when the connection attempt is failed.
	 */
	public void onConnectionFailed();
	
	/**
	 * Invoked when a full packet content is dispatched.
	 * 
	 * @param {byte[]} content Content of the full packet in bytes.
	 */
	public void onPacketReceived(byte[] content);
	
	/**
	 * Invoked when the device has been disconnected from the target device.
	 */
	public void onDisconnection();
}
