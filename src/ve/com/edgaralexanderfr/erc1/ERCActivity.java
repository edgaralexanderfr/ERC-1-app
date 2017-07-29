package ve.com.edgaralexanderfr.erc1;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.Toast;

public class ERCActivity extends Activity {
	private String STRING_ALERT_POSITIVE_BUTTON_TEXT         = "";
	private String STRING_SELECT_DIALOG_POSITIVE_BUTTON_TEXT = "";
	private String STRING_SELECT_DIALOG_NEGATIVE_BUTTON_TEXT = "";
	
	/**
	 * Creates the activity itself.
	 * 
	 * @param {Bundle} savedInstanceState Saved instance state.
	 */
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.init();
	}
	
	/**
	 * Initializes all the necessary resources.
	 */
	private void init () {
		Resources resources                            = this.getResources();
		this.STRING_ALERT_POSITIVE_BUTTON_TEXT         = resources.getString(R.string.erc_activity_alert_positive_button_text);
		this.STRING_SELECT_DIALOG_POSITIVE_BUTTON_TEXT = resources.getString(R.string.erc_activity_select_dialog_positive_button_text);
		this.STRING_SELECT_DIALOG_NEGATIVE_BUTTON_TEXT = resources.getString(R.string.erc_activity_select_dialog_negative_button_text);
	}
	
	/**
	 * Pops up a short toast message within the current activity.
	 * 
	 * @param {String} message Toast message to display. 
	 */
	public void shortToast (final String message) {
		try {
			Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
		} catch (Exception ex1) {
			
		}
	}
	
	/**
	 * Pops up a long toast message within the current activity.
	 * 
	 * @param {String} message Toast message to display. 
	 */
	public void longToast (final String message) {
		try {
			Toast.makeText(this, message, Toast.LENGTH_LONG).show();
		} catch (Exception ex1) {
			
		}
	}
	
	/**
	 * Pops up a simple alert message.
	 * 
	 * @param {String} message Alert message to display.
	 */
	public void alert (final String message) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setMessage(message);
		dialog.setPositiveButton(this.STRING_ALERT_POSITIVE_BUTTON_TEXT, null);
		dialog.create();
		
		try {
			dialog.show();
		} catch (Exception ex1) {
			
		}
	}
	
	/**
	 * Pops up a selection dialog.
	 * 
	 * @param {String}       title        Selection dialog title.
	 * @param {String[]}     options      Selection dialog options.
	 * @param {SelectDialog} selectDialog Select dialog interface for call the onSelection method.
	 */
	public void selectDialog (final String title, final String[] options, final SelectDialog selectDialog) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle(title);
		dialog.setSingleChoiceItems(options, 0, new DialogInterface.OnClickListener () {
			/**
			 * This method will be invoked when a button in the dialog is clicked.
			 * 
			 * @param {DialogInterface} dialog     The dialog that received the click.
			 * @param {int}             The button that was clicked (e.g. BUTTON1) or the position of the item clicked.
			 */
			@Override
			public void onClick (DialogInterface dialog, int which) {
				((AlertDialog) dialog).getListView().setTag(Integer.valueOf(which));
			}
		});
		
		dialog.setPositiveButton(this.STRING_SELECT_DIALOG_POSITIVE_BUTTON_TEXT, new DialogInterface.OnClickListener () {
			/**
			 * This method will be invoked when a button in the dialog is clicked.
			 * 
			 * @param {DialogInterface} dialog     The dialog that received the click.
			 * @param {int}             The button that was clicked (e.g. BUTTON1) or the position of the item clicked.
			 */
			@Override
			public void onClick (DialogInterface dialog, int which) {
				Object listViewTag = ((AlertDialog) dialog).getListView().getTag();
				selectDialog.onSelection((listViewTag != null) ? (Integer) listViewTag : 0 );
			}
		});
		
		dialog.setNegativeButton(this.STRING_SELECT_DIALOG_NEGATIVE_BUTTON_TEXT, null);
		dialog.create();
		
		try {
			dialog.show();
		} catch (Exception ex1) {
			
		}
	}
}
