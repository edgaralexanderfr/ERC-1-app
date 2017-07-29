package ve.com.edgaralexanderfr.erc1;

import ve.com.edgaralexanderfr.erc.Log;
import ve.com.edgaralexanderfr.erc.LogWriteListener;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class LogActivity extends ERCActivity implements View.OnClickListener {
	private TextView log       = null;
	private Button clearButton = null;
	
	/**
	 * Creates the activity itself.
	 * 
	 * @param {Bundle} savedInstanceState Saved instance state.
	 */
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_log);
		this.init();
	}
	
	/**
	 * Initializes all the necessary resources.
	 */
	private void init () {
		this.findViews();
		this.setListeners();
		this.refresh();
	}
	
	/**
	 * Finds the references of all the activity's views.
	 */
	private void findViews () {
		this.log         = (TextView) this.findViewById(R.id.log_activity_log);
		this.clearButton = (Button)   this.findViewById(R.id.log_activity_clear_button);
	}
	
	/**
	 * Establishes all the necessary listeners.
	 */
	private void setListeners () {
		final LogActivity $this = this;
		
		Log.setGlobalWriteListener(new LogWriteListener () {
			/**
			 * Invoked when a log is written.
			 * 
			 * @param {String} log Written log.
			 */
			@Override
			public void onLogWritten (String log) {
				$this.runOnUiThread(new Runnable () {
					/**
					 * Starts executing the active part of the class' code.
					 */
					@Override
					public void run () {
						$this.refresh();
					}
				});
			}
		});
		
		this.clearButton.setOnClickListener(this);
	}
	
	/**
	 * Called when a view has been clicked.
	 * 
	 * @param {View} view The view that was clicked.
	 */
	@Override
	public void onClick (View view) {
		Log.clean();
		this.refresh();
	}
	
	/**
	 * Refreshes/updates the log history.
	 */
	public void refresh () {
		this.log.setText(Log.getLines());
	}
}
