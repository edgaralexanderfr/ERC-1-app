package ve.com.edgaralexanderfr.erc;

public interface ERCSerialProtocolEvents {
	/**
	 * Invoked when ERC has been connected.
	 */
	public void onConnectionSuccess();
	
	/**
	 * Invoked when there's a failure during the connection attempt with the ERC.
	 */
	public void onConnectionFailed();
	
	/**
	 * Invoked when ERC notifies a log.
	 * 
	 * @param {byte} logCode ERC's log code.
	 */
	public void onLogWrite(final byte logCode);
	
	/**
	 * Invoked when ERC reports its used memory.
	 * 
	 * @param {int} freeMemory ERC's free memory.
	 */
	public void onLogWriteFreeMemory(final int freeMemory);
	
	/**
	 * Invoked when ERC updates the collision sectors state.
	 * 
	 * @param {byte} northWestCollisionSectorDistance Scaled distance with closest north-west object.
	 * @param {byte} northEastCollisionSectorDistance Scaled distance with closest north-east object.
	 * @param {byte} southWestCollisionSectorDistance Scaled distance with closest south-west object.
	 * @param {byte} southEastCollisionSectorDistance Scaled distance with closest south-east object.
	 */
	public void onCollisionSectorsUpdate(final byte northWestCollisionSectorDistance, final byte northEastCollisionSectorDistance, final byte southWestCollisionSectorDistance, final byte southEastCollisionSectorDistance);
	
	/**
	 * Invoked when ERC has been disconnected.
	 */
	public void onDisconnection();
}
