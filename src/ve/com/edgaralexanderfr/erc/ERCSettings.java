package ve.com.edgaralexanderfr.erc;

public class ERCSettings {
	public static final byte PLC_ROUTINE_NONE     = 0;
	
	protected boolean resetDirectionOnDrop        = true;
	protected boolean resetAccelerationOnDrop     = true;
	protected boolean wheel                       = false;
	protected boolean throttle                    = false;
	protected short motorsLevelFactor             = 32;
	protected byte directionForRotation           = 100;
	protected boolean collisionProtectionBrake    = true;
	protected byte plcRoutine                     = PLC_ROUTINE_NONE;
	protected ERCSerialProtocol ercSerialProtocol = null;
	
	/**
	 * Returns whether to reset the direction on drop.
	 * 
	 * @return {boolean}.
	 */
	public boolean resetDirectionOnDropEnabled () {
		return this.resetDirectionOnDrop;
	}
	
	/**
	 * Returns whether to reset the acceleration on drop.
	 * 
	 * @return {boolean}.
	 */
	public boolean resetAccelerationOnDropEnabled () {
		return this.resetAccelerationOnDrop;
	}
	
	/**
	 * Tells whether the wheel is enabled or not.
	 * 
	 * @return {boolean}.
	 */
	public boolean wheelEnabled () {
		return this.wheel;
	}
	
	/**
	 * Tells whether the throttle is enabled or not.
	 * 
	 * @return {boolean}.
	 */
	public boolean throttleEnabled () {
		return this.throttle;
	}
	
	/**
	 * Returns the motors level factor.
	 * 
	 * @return {short}.
	 */
	public short getMotorsLevelFactor () {
		return this.motorsLevelFactor;
	}
	
	/**
	 * Returns the direction for rotation.
	 * 
	 * @return {byte}.
	 */
	public short getDirectionForRotation () {
		return this.directionForRotation;
	}
	
	/**
	 * Tells whether collision protection brake is enabled or not.
	 * 
	 * @return {boolean}.
	 */
	public boolean collisionProtectionBrakeEnabled () {
		return this.collisionProtectionBrake;
	}
	
	/**
	 * Returns followed PLC routine.
	 * 
	 * @return {byte}.
	 */
	public byte getPlcRoutine () {
		return this.plcRoutine;
	}
	
	/**
	 * Establishes whether to reset the direction on drop or not.
	 * 
	 * @param {boolean} resetDirectionOnDrop Direction reset state.
	 */
	public void setResetDirectionOnDrop (final boolean resetDirectionOnDrop) {
		this.resetDirectionOnDrop = resetDirectionOnDrop;
	}
	
	/**
	 * Establishes whether to reset the acceleration on drop or not.
	 * 
	 * @param {boolean} resetAccelerationOnDrop Acceleration reset state.
	 */
	public void setResetAccelerationOnDrop (final boolean resetAccelerationOnDrop) {
		this.resetAccelerationOnDrop = resetAccelerationOnDrop;
	}
	
	/**
	 * Establishes whether to use the wheel or not.
	 * 
	 * @param {boolean} wheel Wheel state.
	 */
	public void setWheel (final boolean wheel) {
		this.wheel = wheel;
	}
	
	/**
	 * Establishes whether to use the throttle or not.
	 * 
	 * @param {boolean} throttle Throttle state.
	 */
	public void setThrottle (final boolean throttle) {
		this.throttle = throttle;
	}
	
	/**
	 * Establishes the motorsLevelFactor.
	 * 
	 * @param {short} motorsLevelFactor Motors level factor.
	 */
	public void setMotorsLevelFactor (final short motorsLevelFactor) {
		this.motorsLevelFactor = motorsLevelFactor;
		
		if (this.ercSerialProtocol == null) {
			return;
		}
		
		this.ercSerialProtocol.setMotorsLevelFactor((byte) this.motorsLevelFactor);
	}
	
	/**
	 * Establishes the motorsLevelFactor.
	 * 
	 * @param {int} motorsLevelFactor Motors level factor.
	 */
	public void setMotorsLevelFactor (final int motorsLevelFactor) {
		this.setMotorsLevelFactor((short) motorsLevelFactor);
	}
	
	/**
	 * Establishes the direction for rotation.
	 * 
	 * @param {byte} directionForRotation Direction for rotation.
	 */
	public void setDirectionForRotation (final byte directionForRotation) {
		this.directionForRotation = directionForRotation;
		
		if (this.ercSerialProtocol == null) {
			return;
		}
		
		this.ercSerialProtocol.setDirectionForRotation((byte) this.directionForRotation);
	}
	
	/**
	 * Establishes the direction for rotation.
	 * 
	 * @param {int} directionForRotation Direction for rotation.
	 */
	public void setDirectionForRotation (final int directionForRotation) {
		this.setDirectionForRotation((byte) directionForRotation);
	}
	
	/**
	 * Establishes the collision protection brake.
	 * 
	 * @param {boolean} collisionProtectionBrake Collision protection brake state.
	 */
	public void setCollisionProtectionBrake (final boolean collisionProtectionBrake) {
		this.collisionProtectionBrake = collisionProtectionBrake;
		
		if (this.ercSerialProtocol == null) {
			return;
		}
		
		if (this.collisionProtectionBrake) {
			this.ercSerialProtocol.setCollisionProtectionBrake();
		} else {
			this.ercSerialProtocol.unsetCollisionProtectionBrake();
		}
	}
	
	/**
	 * Establishes the PLC routine to follow.
	 * 
	 * @param {byte} plcRoutine PLC routine to follow.
	 */
	public void setPlcRoutine (final byte plcRoutine) {
		if (plcRoutine == this.plcRoutine) {
			return;
		}
		
		this.plcRoutine = plcRoutine;
		
		if (this.ercSerialProtocol != null) {
			this.ercSerialProtocol.setPlcRoutine(this.plcRoutine);
		}
	}
	
	/**
	 * Establishes the PLC routine to follow.
	 * 
	 * @param {int} plcRoutine PLC routine to follow.
	 */
	public void setPlcRoutine (final int plcRoutine) {
		this.setPlcRoutine((byte) plcRoutine);
	}
	
	/**
	 * If set (not null) the settings will call the protocol's corresponding commands.
	 * 
	 * @param {ERCSerialProtocol} ercSerialProtocol ERC Serial Protocol.
	 */
	public void setERCSerialProtocol (ERCSerialProtocol ercSerialProtocol) {
		this.ercSerialProtocol = ercSerialProtocol;
	}
}
