package ve.com.edgaralexanderfr.erc;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ERCSerialProtocol implements SerialProtocol {
	public static final short PENDING_RESPONSE_TIMEOUT_TIME       = 2500;
	public static final byte RECEIVED_PACKET_MAX_SIZE             = 5;
	public static final byte NO_VALUE                             = -1;
	public static final byte IN_LOG_WRITE                         = 0;
	public static final byte IN_LOG_WRITE_FREE_MEMORY             = 1;
	public static final byte IN_UPDATE_COLLISION_SECTORS          = 2;
	public static final byte OUT_SET_DIRECTION                    = 0;
	public static final byte OUT_SET_ACCELERATION                 = 1;
	public static final byte OUT_SET_DIRECTION_AND_ACCELERATION   = 2;
	public static final byte OUT_SET_FORWARD                      = 3;
	public static final byte OUT_SET_NEUTRAL                      = 4;
	public static final byte OUT_SET_REVERSE                      = 5;
	public static final byte OUT_START_BRAKING                    = 6;
	public static final byte OUT_STOP_BRAKING                     = 7;
	public static final byte OUT_ACCELERATE                       = 8;
	public static final byte OUT_DECELERATE                       = 9;
	public static final byte OUT_SET_MOTORS_LEVEL_FACTOR          = 10;
	public static final byte OUT_SET_DIRECTION_FOR_ROTATION       = 11;
	public static final byte OUT_SET_COLLISION_PROTECTION_BRAKE   = 12;
	public static final byte OUT_UNSET_COLLISION_PROTECTION_BRAKE = 13;
	public static final byte OUT_SET_PLC_ROUTINE                  = 14;
	
	private boolean onePacketAtTimeMode                           = true;
	private long lastSentPacketTime                               = 0;
	private boolean pendingResponse                               = false;
	private List<byte[]> pendingPacketsStack                      = new ArrayList<byte[]>();
	private BluetoothSerial bluetoothSerial                       = null;
	private ERCSerialProtocolEvents events                        = null;
	
	/**
	 * Returns whether packets must be sent only there's no pending response from ERC.
	 * 
	 * @return {boolean}
	 */
	public boolean onePacketAtTimeModeEnabled () {
		return this.onePacketAtTimeMode;
	}
	
	/**
	 * Returns the last sent packet time.
	 * 
	 * @return {long}
	 */
	public long getLastSentPacketTime () {
		return this.lastSentPacketTime;
	}
	
	/**
	 * Returns whether protocol has a pending response for a sent packet to ERC.
	 * 
	 * @return {boolean}
	 */
	public boolean isPendingForResponse () {
		return this.pendingResponse;
	}
	
	/**
	 * Enables/Disables if packets must be sent only there's no pending response from ERC.
	 * 
	 * @param {boolean} onePacketAtTimeMode One packet at time mode value.
	 */
	public void setOnePacketAtTimeMode (final boolean onePacketAtTimeMode) {
		this.onePacketAtTimeMode = onePacketAtTimeMode;
		
		if (!this.onePacketAtTimeMode) {
			this.resetPacketControllerState();
		}
	}
	
	/**
	 * Set the BluetoothSerial object to use.
	 * 
	 * @param {BluetoothSerial} bluetoothSerial BluetoothSerial object to use.
	 */
	public void setBluetoothSerial (BluetoothSerial bluetoothSerial) {
		this.bluetoothSerial = bluetoothSerial;
	}
	
	/**
	 * Set the ERCSerialProtocolEvents to trigger.
	 * 
	 * @param {ERCSerialProtocol} events ERCSerialProtocolEvents object to use.
	 */
	public void setEvents (ERCSerialProtocolEvents events) {
		this.events = events;
	}
	
	/**
	 * Constructs a new ERCSerialProtocol object.
	 * 
	 * @param {BluetoothSerial} bluetoothSerial BluetoothSerial object to use.
	 */
	public ERCSerialProtocol (BluetoothSerial bluetoothSerial) {
		this.setBluetoothSerial(bluetoothSerial);
	}
	
	/**
	 * Invoked when the connection attempt is successful.
	 */
	@Override
	public void onConnectionSuccess () {
		if (this.events != null) {
			this.events.onConnectionSuccess();
		}
	}
	
	/**
	 * Invoked when the connection attempt is failed.
	 */
	@Override
	public void onConnectionFailed () {
		if (this.events != null) {
			this.events.onConnectionFailed();
		}
	}
	
	/**
	 * Invoked when a full packet content is dispatched.
	 * 
	 * @param {byte[]} content Content of the full packet in bytes.
	 */
	@Override
	public void onPacketReceived (byte[] content) {
		if (content.length != RECEIVED_PACKET_MAX_SIZE) {
			return;
		}
		
		byte packetName   = (byte) content[0];
		byte packetValue1 = (byte) content[1];
		byte packetValue2 = (byte) content[2];
		byte packetValue3 = (byte) content[3];
		byte packetValue4 = (byte) content[4];
		
		switch (packetName) {
			case IN_LOG_WRITE                : {
				if (this.onePacketAtTimeMode) {
					this.pendingResponse = false;
					this.dispatchNextPendingPacket();
				}
				
				this.logWrite(packetValue1);
			}; break;
			case IN_LOG_WRITE_FREE_MEMORY    : {
				this.logWriteFreeMemory(word32ToInt(new byte[] {packetValue1, packetValue2, packetValue3, packetValue4}));
			}; break;
			case IN_UPDATE_COLLISION_SECTORS : {
				this.updateCollisionSectors(packetValue1, packetValue2, packetValue3, packetValue4);
			};
		}
	}
	
	/**
	 * Invoked when ERC notifies a log.
	 * 
	 * @param {byte} logCode ERC's log code.
	 */
	private void logWrite (final byte log) {
		if (this.events != null) {
			this.events.onLogWrite(log);
		}
	}
	
	/**
	 * Invoked when ERC reports its used memory.
	 * 
	 * @param {int} freeMemory ERC's free memory.
	 */
	private void logWriteFreeMemory (final int freeMemory) {
		if (this.events != null) {
			this.events.onLogWriteFreeMemory(freeMemory);
		}
	}
	
	/**
	 * Invoked when ERC updates the collision sectors state.
	 * 
	 * @param {byte} northWestCollisionSectorDistance Scaled distance with closest north-west object.
	 * @param {byte} northEastCollisionSectorDistance Scaled distance with closest north-east object.
	 * @param {byte} southWestCollisionSectorDistance Scaled distance with closest south-west object.
	 * @param {byte} southEastCollisionSectorDistance Scaled distance with closest south-east object.
	 */
	private void updateCollisionSectors (final byte upperCollisionSectorDistance, final byte lowerCollisionSectorDistance, final byte leftCollisionSectorDistance, final byte rightCollisionSectorDistance) {
		if (this.events != null) {
			this.events.onCollisionSectorsUpdate(upperCollisionSectorDistance, lowerCollisionSectorDistance, leftCollisionSectorDistance, rightCollisionSectorDistance);
		}
	}
	
	/**
	 * Invoked when the device has been disconnected from the target device.
	 */
	@Override
	public void onDisconnection () {
		this.resetPacketControllerState();
		
		if (this.events != null) {
			this.events.onDisconnection();
		}
	}
	
	/**
	 * Converts a 32-bits word vector (4 bytes) into its respective int32 value.
	 * 
	 * @param  {byte[]} word Word value to convert.
	 * @return {int}
	 */
	public static int word32ToInt (byte[] word) {
		return ((word[0] & 0xff) << 24) | 
		       ((word[1] & 0xff) << 16) | 
		       ((word[2] & 0xff) <<  8) | 
		        (word[3] & 0xff);
	}
	
	/**
	 * Send a packet to the ERC.
	 * 
	 * @param {byte} name   Packet name.
	 * @param {byte} value1 Packet value 1.
	 * @param {byte} value2 Packet value 2.
	 * @param {byte} value3 Packet value 3.
	 * @param {byte} value4 Packet value 4.
	 */
	public synchronized void send (final byte name, final byte value1, final byte value2, final byte value3, final byte value4) {
		if (this.bluetoothSerial == null) {
			return;
		}
		
		final byte[] packet = new byte[] { name, value1, value2, value3, value4 };
		
		if (this.onePacketAtTimeMode) {
			this.stackAndDispatchPacket(packet);
		} else {
			this.bluetoothSerial.sendPacket(packet);
		}
	}
	
	/**
	 * Stacks the provided packet into the stack and dispatches the next pending packet 
	 * if possible.
	 * 
	 * @param {byte[]} packet Packet to stack/send.
	 */
	private void stackAndDispatchPacket (final byte[] packet) {
		this.pendingPacketsStack.add(packet);
		this.dispatchNextPendingPacket();
	}
	
	/**
	 * it pops the pending packets stack in FIFO order. It will remain blocked if there's 
	 * a pending response and in case of timeout it will allow a new dispatch setting 
	 * the pending response state to false.
	 */
	private void dispatchNextPendingPacket () {
		final long packetToSendTime          = Calendar.getInstance().getTime().getTime();
		final long lastSentPacketElapsedTime = packetToSendTime - this.lastSentPacketTime;
		
		if (lastSentPacketElapsedTime > PENDING_RESPONSE_TIMEOUT_TIME) {
			this.pendingResponse = false;
		}
		
		if (this.pendingResponse || this.pendingPacketsStack.size() == 0) {
			return;
		}
		
		this.pendingResponse    = true;
		this.lastSentPacketTime = packetToSendTime;
		this.bluetoothSerial.sendPacket(this.pendingPacketsStack.get(0));
		this.pendingPacketsStack.remove(0);
	}
	
	/**
	 * Resets everything related with the one packet at time mode.
	 */
	private void resetPacketControllerState () {
		this.lastSentPacketTime  = 0;
		this.pendingResponse     = false;
		this.pendingPacketsStack = new ArrayList<byte[]>();
	}
	
	/**
	 * Set the direction of the ERC.
	 * 
	 * @param {byte} direction Direction where 0 is the max. left point, 63 the middle 
	 						   (straight) and 126 the max. right point.
	 */
	public void setDirection (final byte direction) {
		this.send(OUT_SET_DIRECTION, direction, NO_VALUE, NO_VALUE, NO_VALUE);
	}
	
	/**
	 * Establishes a fixed acceleration for the ERC.
	 * 
	 * @param {byte} acceleration Fixed acceleration level.
	 */
	public void setAcceleration (final byte acceleration) {
		this.send(OUT_SET_ACCELERATION, acceleration, NO_VALUE, NO_VALUE, NO_VALUE);
	}
	
	/**
	 * Establishes the direction and fixed acceleration for the ERC.
	 * 
	 * @param {byte} direction    Direction where 0 is the max. left point, 63 the middle 
	 							  (straight) and 126 the max. right point.
	 * @param {byte} acceleration Fixed acceleration level.
	 */
	public void setDirectionAndAcceleration (final byte direction, final byte acceleration) {
		this.send(OUT_SET_DIRECTION_AND_ACCELERATION, direction, acceleration, NO_VALUE, NO_VALUE);
	}
	
	/**
	 * Set the transmission to go forward always.
	 */
	public void setForward () {
		this.send(OUT_SET_FORWARD, NO_VALUE, NO_VALUE, NO_VALUE, NO_VALUE);
	}
	
	/**
	 * Set the transmission for no acceleration.
	 */
	public void setNeutral () {
		this.send(OUT_SET_NEUTRAL, NO_VALUE, NO_VALUE, NO_VALUE, NO_VALUE);
	}
	
	/**
	 * Set the transmission to go always in reverse.
	 */
	public void setReverse () {
		this.send(OUT_SET_REVERSE, NO_VALUE, NO_VALUE, NO_VALUE, NO_VALUE);
	}
	
	/**
	 * Commands the ERC to start braking
	 */
	public void startBraking () {
		this.send(OUT_START_BRAKING, NO_VALUE, NO_VALUE, NO_VALUE, NO_VALUE);
	}
	
	/**
	 * Commands the ERC to stop braking
	 */
	public void stopBraking () {
		this.send(OUT_STOP_BRAKING, NO_VALUE, NO_VALUE, NO_VALUE, NO_VALUE);
	}
	
	/**
	 * Commands the ERC to accelerate (progressively if allowed) until it reaches the max 
	 * acceleration.
	 */
	public void accelerate () {
		this.send(OUT_ACCELERATE, NO_VALUE, NO_VALUE, NO_VALUE, NO_VALUE);
	}
	
	/**
	 * Commands the ERC to decelerate (progressively if allowed) until it stops 
	 * accelerating.
	 */
	public void stopAccelerating () {
		this.send(OUT_DECELERATE, NO_VALUE, NO_VALUE, NO_VALUE, NO_VALUE);
	}
	
	/**
	 * Establishes the factor that the ERC must use in order to level the motors pulse.
	 * 
	 * @param {byte} motorsLevelFactor Factor that the ERC uses to level the motors pulse.
	 */
	public void setMotorsLevelFactor (final byte motorsLevelFactor) {
		this.send(OUT_SET_MOTORS_LEVEL_FACTOR, motorsLevelFactor, NO_VALUE, NO_VALUE, NO_VALUE);
	}
	
	/**
	 * Establishes the direction level (left and right) that the ERC must considerate in 
	 * order to turn the car around its own axis when brakes are ON.
	 * 
	 * @param {byte} directionForRotation Direction level (left and right) to considerate.
	 */
	public void setDirectionForRotation (final byte directionForRotation) {
		this.send(OUT_SET_DIRECTION_FOR_ROTATION, directionForRotation, NO_VALUE, NO_VALUE, NO_VALUE);
	}
	
	/**
	 * Configures the ERC to considerate the collision protection brake.
	 */
	public void setCollisionProtectionBrake () {
		this.send(OUT_SET_COLLISION_PROTECTION_BRAKE, NO_VALUE, NO_VALUE, NO_VALUE, NO_VALUE);
	}
	
	/**
	 * Configures the ERC to ignore the collision protection brake.
	 */
	public void unsetCollisionProtectionBrake () {
		this.send(OUT_UNSET_COLLISION_PROTECTION_BRAKE, NO_VALUE, NO_VALUE, NO_VALUE, NO_VALUE);
	}
	
	/**
	 * Establishes a specific PLC routine that the ERC must follow.
	 * 
	 * @param {byte} plcRoutine PLC routine index.
	 */
	public void setPlcRoutine (final byte plcRoutine) {
		this.send(OUT_SET_PLC_ROUTINE, plcRoutine, NO_VALUE, NO_VALUE, NO_VALUE);
	}
}
