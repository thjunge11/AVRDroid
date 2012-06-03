package de.thjunge11.avrremote;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.thjunge11.avrremote.R;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

public class AVRShellActivity extends AVRActivity {

	private static final String TAG = AVRShellActivity.class.getSimpleName();
	
	private TextView tv_response;
	private EditText ev_send;
	private TextView tv_status;
	private ScrollView sv_response;
	private ProgressDialog mDialog;
	private AVRInteraction taskHandler;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shell);
        
        // initialization
        tv_response = (TextView) this.findViewById(R.id.tv_response);
        ev_send = (EditText)this.findViewById(R.id.ev_send);
        tv_status = (TextView) this.findViewById(R.id.tv_status);
        sv_response = (ScrollView) this.findViewById(R.id.scroll);

        
        Button bt = (Button) this.findViewById(R.id.bt_setget);
        bt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AVRShellActivity.this.uiSetGet();
			}
		});
               
        Button bt_read = (Button) this.findViewById(R.id.bt_read);
        bt_read.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AVRShellActivity.this.uiReadResponse();
			}
		});
        
        Button bt_connect = (Button) this.findViewById(R.id.bt_connect);
        bt_connect.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AVRShellActivity.this.uiReConnect();
			}		
		});
        
        Button bt_clear = (Button) this.findViewById(R.id.bt_clear);
        bt_clear.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AVRShellActivity.this.tv_response.setText("");
			}
		});
        
        Button bt_dump = (Button) this.findViewById(R.id.bt_dump);
        bt_dump.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AVRShellActivity.this.uiDumpToFile();
			}
		});
	}
		
	@Override
	protected void updateConnectionStatus(boolean status) {
		super.updateConnectionStatus(status);
		if (status) {			
			AVRShellActivity.this.uiUpdateStatus(true);
		}
		else {
			AVRShellActivity.this.uiUpdateStatus(false);
		}		
	}
	
	private void uiReConnect() {
		this.showProgressDialog();
		taskHandler = new AVRInteraction();
		taskHandler.execute("reconnect", "");
	}

	private void uiSetGet() {
		this.showProgressDialog();
		taskHandler = new AVRInteraction();
		taskHandler.execute("send", ev_send.getText().toString());
	}
	
	private void uiReadResponse() {
		this.showProgressDialog();
		taskHandler = new AVRInteraction();
		taskHandler.execute("get", "");
	}
	
	private void showProgressDialog() {
		mDialog = ProgressDialog.show(AVRShellActivity.this, "", "Talking to device. Please wait...", true);
	}
		
	private class AVRInteraction extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			
			if (params.length == 2) {
				if (params[0].equals("reconnect")) {
					AVRConnection.reconnect();
					return null;
				}
				else if (params[0].equals("send")) {
					String answer = AVRConnection.setget(params[1]);
					if (answer == null) {
						return answer;
					}
					else {
						return "Out: " + params[1] + "\nIn [" + answer.length() + "B]:\n" + answer;
					}
				}
				else if (params[0].equals("get")) {
					String answer = AVRConnection.getResponse();
					if (answer == null) {
						return answer;
					}
					else {
						return "In [" + answer.length() + "B]:\n" + answer;
					}
				}
			}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
			AVRShellActivity.this.mDialog.dismiss();
						
			if (AVRConnection.isAVRconnected()) {			
				AVRShellActivity.this.uiUpdateStatus(true);
				if (result != null) {
					AVRShellActivity.this.uiAppend(result);
				}
			}
			else {
				AVRShellActivity.this.uiUpdateStatus(false);
			}
		}
	}
	
	private void uiUpdateStatus(boolean status) {
		if(status) tv_status.setText(R.string.status_connected);
		else tv_status.setText(R.string.status_not_connected);
	}
	
	private void uiAppend(String str) {
		if (!str.endsWith("\n")) {
			str += "\n";
		}
		tv_response.append(str);

		// sv_response.fullScroll(ScrollView.FOCUS_DOWN);
		sv_response.post(new Runnable()
	    {
	        @Override
			public void run()
	        {
	            sv_response.fullScroll(View.FOCUS_DOWN);
	        }
	    });
	}
	
	private void uiDumpToFile () {
		
		boolean mExternalStorageAvailable = false;
		boolean mExternalStorageWriteable = false;
		String state = Environment.getExternalStorageState();
		if (Constants.DEBUG) Log.d(TAG," uiDumpToFile(): ExternalStorageState = " + state);

		if (Environment.MEDIA_MOUNTED.equals(state)) {
		    // We can read and write the media
		    mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
		    // We can only read the media
		    mExternalStorageAvailable = true;
		    mExternalStorageWriteable = false;
		} else {
		    // Something else is wrong. It may be one of many other states, but all we need
		    //  to know is we can neither read nor write
		    mExternalStorageAvailable = mExternalStorageWriteable = false;
		}
		
		if (Constants.DEBUG) Log.d(TAG," uiDumpToFile(): Is ExternalStorage Writeable = " + mExternalStorageWriteable);
		
		if (mExternalStorageAvailable && mExternalStorageWriteable) {
			// write file on different thread
			new Thread(new Runnable() {
				@Override
				public void run() {
				    // Create a path where we will place our private file on external
				    // storage.
					SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_hhmmss");			
					String strFilename = sdf.format(new Date()) + "_AVR_Remote_Dump.log";
				    final File file = new File(getExternalFilesDir(null), strFilename);
				    if (Constants.DEBUG) Log.d(TAG," uiDumpToFile(): file = " + file);

				    try {
				        OutputStream os = new FileOutputStream(file);
				        os.write(tv_response.getText().toString().getBytes());
				        os.close();
				        tv_response.post(new Runnable() {
							
							@Override
							public void run() {
								AVRShellActivity.this.uiAppend("Dumped log to file: " + file);
							}
						});
				    } catch (IOException e) {
				        // Unable to create file, likely because external storage is
				        // not currently mounted.
				        Log.w("ExternalStorage", "Error writing " + file, e);
				    }
				}
			}).start();
		}
		
	}	
}