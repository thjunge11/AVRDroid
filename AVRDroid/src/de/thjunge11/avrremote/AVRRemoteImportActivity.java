package de.thjunge11.avrremote;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;
import de.thjunge11.avrremote.xmlLayout.ButtonStore;
import de.thjunge11.avrremote.xmlLayout.XmlValidationResults;

public class AVRRemoteImportActivity extends Activity {
	
	private static final String TAG = AVRRemoteImportActivity.class.getSimpleName();
	private final static String VALID_XML_LAYOUT_CONTENT = "valid";
	private final static String XML_LAYOUT_CONTENT = "xmllayoutcontent";
	private final static String XML_LAYOUT_NAME = "xmllayoutname";
	private final static String VALID_INTENT = "validintent";
	private final static String KEY_FILENAME = "filename";
	
	private static final int DIALOG_IMPORT = 1;
	private static final int DIALOG_SHOWLOG = 2;
	private static final int DIALOG_OVERWRITE = 3;
	
	private ProgressDialog mDialog;
	private Intent mIntent;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mIntent = this.getIntent();
		
		if (mIntent == null)
			this.finish();
		
		// examine intent
		if (Constants.DEBUG) {
			String strIntent = mIntent.toString();
			Log.d(TAG, strIntent);
			// Examine data
			if (mIntent.getData() != null) {
				Log.d(TAG, "Data: toString = " + mIntent.getData().toString());
				Log.d(TAG, "Data: EncodedPath = " + mIntent.getData().getEncodedPath());
				Log.d(TAG, "Data: Path = " + mIntent.getData().getPath());
			}
			// Examine extended data
			Bundle bundle = mIntent.getExtras();
			if (bundle != null) {
				Log.d(TAG, "Extras: size = " + bundle.size());
				if (bundle.size() > 0) {
					Set<String> keyset = bundle.keySet();
					for (String key : keyset) {
						Log.d(TAG, "Extras: key = " + key);
						Log.d(TAG, "Extras: class = " + bundle.get(key).getClass().getName());
						Log.d(TAG, "Extras: contains = " + bundle.get(key).toString());
					}
				}
			}	
		}
		
		// validate intent in async task
		new ProcessIntent().execute(mIntent);
		mDialog = ProgressDialog.show(AVRRemoteImportActivity.this, "", "Importing. Please wait...", true);
	}
	
	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		final Bundle bundle = args;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		Dialog dialog;

		switch (id) {
		
		case DIALOG_IMPORT :
			builder.setTitle(getResources().getString(R.string.dialog_import));
			if(args.getBoolean(VALID_XML_LAYOUT_CONTENT)) {
				final EditText inputname = new EditText(this);
				inputname.setText(args.getString(XML_LAYOUT_NAME));
				builder.setView(inputname);
				builder.setMessage(getResources().getString(R.string.dialog_import_valid_layout) + 
						args.getString(XML_LAYOUT_CONTENT))
				.setPositiveButton(R.string.dialog_import_positive, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					File file = new File (getExternalFilesDir(null), inputname.getText().toString() + ".xml");
					if (!file.exists()) {
						if (AVRRemoteImportActivity.this.copyIntent(inputname.getText().toString())) {
							Toast.makeText(AVRRemoteImportActivity.this, "Layout " + inputname.getText().toString()
									+ " saved.", Toast.LENGTH_SHORT).show();
						}
						AVRRemoteImportActivity.this.finish();
					}
					else {
						Bundle localBundle = new Bundle();
						localBundle.putString(KEY_FILENAME, inputname.getText().toString());
						AVRRemoteImportActivity.this.showDialog(DIALOG_OVERWRITE, localBundle);
					}
				  }
				});
			}
			else {
				final Bundle args_log = new Bundle(args);
				builder.setMessage(R.string.toast_no_valid_xml_file)
				.setPositiveButton(R.string.dialog_import_showlog, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// AVRRemoteImportActivity.this.finish();
						AVRRemoteImportActivity.this.showDialog(DIALOG_SHOWLOG, args_log);
					  }
				});
			}
			builder.setNegativeButton(R.string.dialog_import_negative, new DialogInterface.OnClickListener() {
			  public void onClick(DialogInterface dialog, int id) {
				  AVRRemoteImportActivity.this.finish();
			  }
			});
			dialog = builder.create();
			break;
			
		
		case DIALOG_SHOWLOG :
			builder.setTitle(getResources().getString(R.string.dialog_title_showlog))
			.setMessage(args.getString(XML_LAYOUT_CONTENT))
			.setNegativeButton(R.string.dialog_import_negative, new DialogInterface.OnClickListener() {
			  public void onClick(DialogInterface dialog, int id) {
				  AVRRemoteImportActivity.this.finish();
			  }
			});
			dialog = builder.create();
			break;
			
			
		case DIALOG_OVERWRITE:
			AlertDialog.Builder builderov = new AlertDialog.Builder(this);
			builderov.setTitle(R.string.dialog_overwrite)
			.setPositiveButton(R.string.dialog_bt_overwrite, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				File file = new File (getExternalFilesDir(null), bundle.getString(KEY_FILENAME) + ".png");
				if (file.exists()) { file.delete(); }
				if (AVRRemoteImportActivity.this.copyIntent(bundle.getString(KEY_FILENAME))) {
					Toast.makeText(AVRRemoteImportActivity.this, "Layout " + bundle.getString(KEY_FILENAME)
							+ " saved.", Toast.LENGTH_SHORT).show();
				}
				AVRRemoteImportActivity.this.finish();
			  }
			})
			.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
			  public void onClick(DialogInterface dialog, int id) {
				  AVRRemoteImportActivity.this.finish();
			  }
			});
			dialog = builderov.create();
	        break;	
			
			
		default:
			dialog = null;
		
		}
		
		return dialog;
	}
	
	private boolean copyIntent(String layoutname) {
		Uri uri = null;
		boolean success = false;
		
		if (Intent.ACTION_VIEW.equals(mIntent.getAction())) {
			uri = mIntent.getData();
		}	
		else if (Intent.ACTION_SEND.equals(mIntent.getAction())) {
			uri = (Uri) mIntent.getParcelableExtra (Intent.EXTRA_STREAM);
		}
		else if (mIntent.getComponent() != null) {
			uri = (Uri) mIntent.getParcelableExtra (Intent.EXTRA_STREAM);
		}
		
		if (uri != null) {
			success = copyUri(uri, layoutname);
		}
		return success;
	}
	
	private boolean copyUri(Uri uri, String layoutname) {
		boolean success = false;
		if (("content").equals(uri.getScheme())) {
			try {
				InputStream is = getContentResolver().openInputStream(uri);
				success = this.copy(is, layoutname);
				is.close();				
			} catch (FileNotFoundException e) {
				Log.e(TAG, e.getMessage());
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
			}
		}
		else if (("file").equals(uri.getScheme())) {
			try {
				InputStream is = new FileInputStream(uri.getPath());
				success = this.copy(is, layoutname);
				is.close();
			} catch (FileNotFoundException e) {
				Log.e(TAG, e.getMessage());
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
			}
		}
		return success;
	}
	
	private boolean copy(InputStream is, String filename) {
		
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			if (!filename.endsWith(".xml")) {
				filename += ".xml";
			}
			final File outFile = new File(getExternalFilesDir(null), filename);
			try {
		        OutputStream os = new FileOutputStream(outFile);
		        byte[] buffer = new byte[1024];
		        while (is.read(buffer) != -1) {
		        	os.write(buffer);
		        }
		        os.close();
		    } catch (IOException e) {
		    	Log.e(TAG, e.getMessage());
		    	return false;
		    }
			return true;
		} 
		else {
			Toast.makeText(this, R.string.toast_media_not_mounted, Toast.LENGTH_SHORT).show();
			return false;
		}
	}
	
	
	
	protected class ProcessIntent extends AsyncTask<Intent, Void, Bundle> {
		
		@Override
		protected Bundle doInBackground(Intent... params) {
			Intent intent = new Intent(params[0]);
			Bundle bundle = new Bundle();
			// initialize bad bundle
			bundle.putBoolean(VALID_INTENT, false);
			bundle.putBoolean(VALID_XML_LAYOUT_CONTENT, false);
			bundle.putString(XML_LAYOUT_CONTENT, "no content");
			bundle.putString(XML_LAYOUT_NAME, "no content");
			
			// process ACTION_VIEW intent by intent data
			if (Intent.ACTION_VIEW.equals(intent.getAction())) {
				
				if (intent.getData() != null) {
					bundle = processUri(intent.getData());
					bundle.putBoolean(VALID_INTENT, true);
					return bundle;
				}
			}	
			// process ACTION_SEND intent by intent extra data
			else if (Intent.ACTION_SEND.equals(intent.getAction())) {
				Uri uri = (Uri) intent.getParcelableExtra (Intent.EXTRA_STREAM);
				if (uri != null) {
					bundle = processUri(uri);
					bundle.putBoolean(VALID_INTENT, true);
					return bundle;
				}
			}
			// process explicit intent from AVRRemoteActivity - Menu Import
			// Component will be me only, so its not null
			if (intent.getComponent() != null)
			{	
				Uri uri = (Uri) intent.getParcelableExtra (Intent.EXTRA_STREAM);
				if (uri != null) {
					bundle = processUri(uri);
					bundle.putBoolean(VALID_INTENT, true);
					return bundle;
				}
			}
			return bundle;
		}
		
		protected void onPostExecute(Bundle bundle) {
			mDialog.dismiss();
			if (Constants.DEBUG) Log.d(TAG, bundle.getString(XML_LAYOUT_CONTENT));
			if (bundle.getBoolean(VALID_INTENT)) {
				AVRRemoteImportActivity.this.showDialog(DIALOG_IMPORT, bundle);		
			}
			else {
				AVRRemoteImportActivity.this.finish();
			}
		}
		
		private Bundle processUri(Uri uri) {
			Bundle bundle = new Bundle();
			// create bad bundle
			bundle.putBoolean(VALID_XML_LAYOUT_CONTENT, false);
			bundle.putString(XML_LAYOUT_CONTENT, "no content");
			bundle.putString(XML_LAYOUT_NAME, "no content");
			// process content scheme
			if (("content").equals(uri.getScheme())) {
				try {
					InputStream is = getContentResolver().openInputStream(uri);
					XmlValidationResults xmlValidationResults = ButtonStore.validateXmlInputStream(is);
					bundle.putBoolean(VALID_XML_LAYOUT_CONTENT, xmlValidationResults.isValid());
					bundle.putString(XML_LAYOUT_CONTENT, xmlValidationResults.toString());
					String layoutname = new File(uri.getPath()).getName();
					if (layoutname.endsWith(".xml")) {
						layoutname = layoutname.substring(0, layoutname.length() - 4);
					}
					else {
						layoutname = "untitled";
					}
					bundle.putString(XML_LAYOUT_NAME, layoutname);
					is.close();
				} catch (FileNotFoundException e) {
					Log.e(TAG, "doInBackground(): " + e.getMessage());
					bundle.putString(XML_LAYOUT_CONTENT, e.getMessage());
				} catch (IOException e) {
					Log.e(TAG, "doInBackground(): " + e.getMessage());
					bundle.putString(XML_LAYOUT_CONTENT, e.getMessage());
				}
			}
			// process file scheme
			else if (("file").equals(uri.getScheme())) {
				try {
					InputStream is = new FileInputStream(uri.getPath());
					XmlValidationResults xmlValidationResults = ButtonStore.validateXmlInputStream(is);
					bundle.putBoolean(VALID_XML_LAYOUT_CONTENT, xmlValidationResults.isValid());
					bundle.putString(XML_LAYOUT_CONTENT, xmlValidationResults.toString());
					String layoutname = new File(uri.getPath()).getName();
					if (layoutname.endsWith(".xml")) {
						layoutname = layoutname.substring(0, layoutname.length() - 4);
					}
					bundle.putString(XML_LAYOUT_NAME, layoutname);
					is.close();
				} catch (FileNotFoundException e) {
					Log.e(TAG, "doInBackground(): " + e.getMessage());
					bundle.putString(XML_LAYOUT_CONTENT, e.getMessage());
				} catch (IOException e) {
					Log.e(TAG, "doInBackground(): " + e.getMessage());
					bundle.putString(XML_LAYOUT_CONTENT, e.getMessage());
				}
			}
			return bundle;
		}
	}
}	
		
