package ve.com.edgaralexanderfr.erc1.api;

import ve.com.edgaralexanderfr.erc.ERCSerialProtocol;
import ve.com.edgaralexanderfr.erc.ERCSettings;

public class ERC1Settings extends ERCSettings {
	protected static ERC1Settings settings = null;
	
	protected boolean automaticHeadlights  = true;
	
	/**
	 * Returns a global Settings instance.
	 * 
	 * @return {Settings}.
	 */
	public static ERC1Settings get () {
		return (settings != null) ? settings : (settings = new ERC1Settings()) ;
	}
	
	/**
	 * Tells whether automatic headlights are enabled or not.
	 * 
	 * @return {boolean}.
	 */
	public boolean automaticHeadlightsEnabled () {
		return this.automaticHeadlights;
	}
	
	/**
	 * Establishes the automatic headlights.
	 * 
	 * @param {boolean} automaticHeadlights Automatic headlights state.
	 */
	public void setAutomaticHeadlights (final boolean automaticHeadlights) {
		this.automaticHeadlights = automaticHeadlights;
		
		if (this.ercSerialProtocol == null) {
			return;
		}
		
		if (this.automaticHeadlights) {
			((ERC1SerialProtocol) this.ercSerialProtocol).setAutomaticHeadlights();
		} else {
			((ERC1SerialProtocol) this.ercSerialProtocol).unsetAutomaticHeadlights();
		}
	}
	
	/**
	 * If set (not null) the settings will call the protocol's corresponding commands.
	 * 
	 * @param {ERC1SerialProtocol} erc1SerialProtocol ERC1 Serial Protocol.
	 */
	public void setERC1SerialProtocol (ERC1SerialProtocol erc1SerialProtocol) {
		this.ercSerialProtocol = (ERCSerialProtocol) erc1SerialProtocol;
	}
}
