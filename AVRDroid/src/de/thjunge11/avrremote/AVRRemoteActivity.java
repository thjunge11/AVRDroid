package de.thjunge11.avrremote;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;
import de.thjunge11.avrremote.SimpleGestureFilter.SimpleGestureListener;
import de.thjunge11.avrremote.xmlLayout.ButtonIcons;
import de.thjunge11.avrremote.xmlLayout.ButtonStore;
import de.thjunge11.avrremote.xmlLayout.XmlButton;

public class AVRRemoteActivity extends AVRActivity implements SimpleGestureListener {
	
	private static final String TAG = AVRRemoteActivity.class.getSimpleName();
	private static final int OP_MENU_ITEM_SHELL = 0x01;
	private static final int OP_MENU_IMPORT = 0x02;
	private static final int OP_MENU_SELECT = 0x03;
	private static final int OP_MENU_SHARE = 0x04;
	private static final int OP_MENU_SAVE = 0x05;
	private static final int OP_MENU_EDIT = 0x06;
	private static final int CMENU_EDIT_LABEL = 0x07;
	private static final int CMENU_EDIT_ICON = 0x08;
	private static final int CMENU_EDIT_COMMAND = 0x09;
	private static final int CMENU_EDIT_COMMENT = 0x10;
	private static final int OP_MENU_OVERWRITE = 0x11;
	private static final int CMENU_EDIT_STYLE = 0x12;
	private static final int REQUEST_CODE_IMPORT = 0x51;
	private static final int REQUEST_CODE_SELECT = 0x52;
	
	private static final String CURRENTLAYOUT = "currentlayout.xml"; 
	private static final String CURRENTLAYOUTSHARE = "currentlayout_share.xml";
	private static final String KEY_FILENAME = "filename";
	
	private static final int HOR_DUMMY_VIEW_HEIGHT = 20;
	private static final int BUTTON_MARGIN = 0;
	private static final int BUTTON_PADDING = 5;	
	private static final int LAYOUT_MARGIN = 10;
		
	private RelativeLayout layoutButtons;	
	private int storedCurrentPage;
	private AVRButtonOnClickListener avrButtonOnClickListener;
	private SimpleGestureFilter detector;

	private int storedViewonCreateContext;
	private SendAVRCommand taskHandlerSendAVRCommand;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.remote);
		
		// initialization
		layoutButtons = (RelativeLayout) this.findViewById(R.id.remote_buttons);
		detector = new SimpleGestureFilter(this, this);
		avrButtonOnClickListener = new AVRButtonOnClickListener();
		AVRLayoutUtils.bScreenLock = false;
		
		// load current layout from private file if possible, else load default
		storedCurrentPage = 1;
		try {			
			FileInputStream fis = this.openFileInput(CURRENTLAYOUT);
			if (ButtonStore.readButtonsFromXmlInputStream(fis)) {
				if (Constants.DEBUG) Log.d(TAG, "onCreate(): current layout loaded successfully");
			}
			else {
				Log.e(TAG, "onCreate(): current layout not valid, load default");
				ButtonStore.readButtonsFromXmlInputStream(this.getResources().openRawResource(R.raw.buttonslayout));
			}
			fis.close();
		} catch (FileNotFoundException e) {
			Log.e(TAG, "onCreate(): error loading current layout " + e.getMessage());
			ButtonStore.readButtonsFromXmlInputStream(this.getResources().openRawResource(R.raw.buttonslayout));
		} catch (IOException e) {
			Log.e(TAG, "onCreate(): error loading current layout " + e.getMessage());
			ButtonStore.readButtonsFromXmlInputStream(this.getResources().openRawResource(R.raw.buttonslayout));
		}
	};
	
	@Override
	protected void onResume() {
		AVRLayoutUtils.setLayoutPreferences(this, this, layoutButtons);
		this.selectPage(storedCurrentPage);
		AVRLayoutUtils.lockScreen(false, false, this);
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		/* When activity B is launched in front of activity A, this callback 
		 * will be invoked on A. B will not be created until A's onPause() returns, 
		 * Running AVRSendCommand Task should be canceled here
		 */
		if (taskHandlerSendAVRCommand != null) {
			if (taskHandlerSendAVRCommand.getStatus() == AsyncTask.Status.RUNNING) {
				taskHandlerSendAVRCommand.cancel(true);
			}
		}
		super.onPause();
	}
	
	@Override
	protected void onStop() {
		// persist current layout
		try {
			FileOutputStream fos = openFileOutput(CURRENTLAYOUT, MODE_PRIVATE);
			ButtonStore.writeButtonsToOutputStream(fos);
			fos.close();
			if (Constants.DEBUG) Log.d(TAG, "onStop(): current layout written successfully");
		} catch (FileNotFoundException e) {
			Log.e(TAG, "onStop(): error writing current layout " + e.getMessage());
		} catch (IOException e) {
			Log.e(TAG, "onStop(): error writing current layout " + e.getMessage());
		}
		super.onStop();
	}
	
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflator = this.getMenuInflater();
		inflator.inflate(R.menu.context_buttons, menu);
		// store view id which called onCreateContextMenu
		storedViewonCreateContext = v.getId();
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
		case R.id.cmenu_show_comment : 
			String comment = ButtonStore.getButtonComment(storedViewonCreateContext); 
 			if (comment != null && !comment.equals("")) {
 				Toast.makeText(this, comment, Toast.LENGTH_SHORT).show(); }
 			else 
 				Toast.makeText(this, R.string.toast_no_comment, Toast.LENGTH_SHORT).show();
			return true;
		case R.id.cmenu_edit_label :
			this.showDialog(CMENU_EDIT_LABEL);
			return true;
		case R.id.cmenu_edit_icon : 
			this.showDialog(CMENU_EDIT_ICON);
			return true;
		case R.id.cmenu_edit_command :
			this.showDialog(CMENU_EDIT_COMMAND);
			return true;
		case R.id.cmenu_edit_comment :
			this.showDialog(CMENU_EDIT_COMMENT);
			return true;
		case R.id.cmenu_edit_style :
			this.showDialog(CMENU_EDIT_STYLE);
			return true;	
			
		default :
			return super.onContextItemSelected(item);
		}
	}
		
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, OP_MENU_SELECT, Menu.FIRST, this.getString(R.string.op_menu_select))
		.setIcon(android.R.drawable.ic_menu_view);
		menu.add(Menu.NONE, OP_MENU_SAVE, Menu.FIRST+1, this.getString(R.string.op_menu_save))
		.setIcon(android.R.drawable.ic_menu_save);
		menu.add(Menu.NONE, OP_MENU_IMPORT, Menu.FIRST+2, this.getString(R.string.op_menu_import))
		.setIcon(android.R.drawable.ic_menu_add);
		menu.add(Menu.NONE, OP_MENU_SHARE, Menu.FIRST+3, this.getString(R.string.op_menu_share))
		.setIcon(android.R.drawable.ic_menu_share);
		menu.add(Menu.NONE, OP_MENU_EDIT, Menu.FIRST+4, this.getString(R.string.op_menu_edit))
		.setIcon(android.R.drawable.ic_menu_edit);
		
		menu.add(Menu.NONE, OP_MENU_ITEM_SHELL, Menu.FIRST+50, this.getString(R.string.op_menu_shell));
		
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		
		switch (item.getItemId()) {
		
		case OP_MENU_ITEM_SHELL :
			Intent shell = new Intent(this, AVRShellActivity.class);
			this.startActivity(shell);
			return true;
		
    	

		case OP_MENU_IMPORT :
        	Intent importXml = new Intent("android.intent.action.GET_CONTENT");
			importXml.setType("text/plain");
			try {
				startActivityForResult(importXml, REQUEST_CODE_IMPORT);
			} catch (ActivityNotFoundException e) {
				Log.e(TAG,"onOptionsItemSelected(): " + e.getMessage());
				Toast.makeText(this, R.string.toast_import_no_activity_found, Toast.LENGTH_LONG).show();
			}
			return true;
		
			
		case OP_MENU_SAVE :
			this.showDialog(OP_MENU_SAVE);
			return true;
			
			
		case OP_MENU_SHARE :
			this.showDialog(OP_MENU_SHARE);
			return true;
			
			
		case OP_MENU_SELECT :
			if (settings.getBoolean("select_warning_dialog", true)) {
	        	this.showDialog(OP_MENU_SELECT);
	        }
	        else {
	        	Intent intent = new Intent(AVRRemoteActivity.this, AVRRemoteSelectActivity.class);
				AVRRemoteActivity.this.startActivityForResult(intent, REQUEST_CODE_SELECT);
	        }
			return true;
			
			
		case OP_MENU_EDIT :
			// save current layout to internal storage world readable
			try {
				FileOutputStream fos = openFileOutput(CURRENTLAYOUTSHARE, MODE_WORLD_READABLE);
				ButtonStore.writeButtonsToOutputStream(fos);
				fos.close();
				if (Constants.DEBUG) Log.d(TAG, "onCreateDialog(): current layout written successfully");
			} catch (FileNotFoundException e) {
				Log.e(TAG, "onCreateDialog(): error writing current layout " + e.getMessage());
			} catch (IOException e) {
				Log.e(TAG, "onCreateDialog(): error writing current layout " + e.getMessage());
			}
			Intent editXmlfile = new Intent(Intent.ACTION_VIEW);
			File file = new File(AVRRemoteActivity.this.getFilesDir().getAbsolutePath() + "/" + CURRENTLAYOUTSHARE);
			Uri uri = Uri.fromFile(file);
			editXmlfile.setDataAndType(uri, "text/plain"); // set to plain to invoke plain text editors as well
			Toast.makeText(this, R.string.hint_edit_file, Toast.LENGTH_LONG).show();
			this.startActivity(Intent.createChooser(editXmlfile, AVRRemoteActivity.this.getResources().getString(R.string.edit_chooser)));
			return true;
			
			
		default :
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (Constants.DEBUG) Log.d(TAG, "onActivityResult(): request: " + requestCode + ", result code: " + resultCode);
		
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			
			case REQUEST_CODE_IMPORT :
				if (data.getData() != null) {
					Intent intent = new Intent(this, AVRRemoteImportActivity.class);
					intent.putExtra(Intent.EXTRA_STREAM, data.getData());
					this.startActivity(intent);
					// this.loadLayoutFromStorage(data.getData().getPath());
				}
				break;
				
			case REQUEST_CODE_SELECT :
				// load received layout from private file or default if selected in list
				// onActivityResult is called before onResume, so no need to build layout here
				if (!data.getBooleanExtra(AVRRemoteSelectActivity.INTENT_EXTRA_IS_DEFAULT, true)) {
					this.loadLayoutFromExternalStorage(data.getStringExtra(AVRRemoteSelectActivity.INTENT_EXTRA_FILENAME));
				}
				else {
					storedCurrentPage = 1;
					ButtonStore.readButtonsFromXmlInputStream(this.getResources().openRawResource(R.raw.buttonslayout));
					if (Constants.DEBUG) Log.d(TAG, "onActivityResult(): default layout loaded");
				} 
				break;
				  
			default :
				
			}
		}
	}
	
	
	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		Dialog dialog;
		final Bundle bundle = args;

		switch(id) {
		
		case CMENU_EDIT_LABEL:
			final EditText inputLabel = new EditText(this);
			inputLabel.setText(ButtonStore.getButtonLabel(storedViewonCreateContext));
			AlertDialog.Builder builderLabel = new AlertDialog.Builder(this);
			builderLabel.setTitle(R.string.cmenu_edit_label)
			.setView(inputLabel)
			.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				String newLabel = inputLabel.getText().toString();
				if (ButtonStore.modify(ButtonStore.MODIFY_LABEL, storedViewonCreateContext, newLabel)) {
					Toast.makeText(AVRRemoteActivity.this, R.string.toast_edit_label, Toast.LENGTH_SHORT).show();
					AVRRemoteActivity.this.selectPage(storedCurrentPage);
				}
				removeDialog(CMENU_EDIT_LABEL); // <-- else view id is not updated
			  }
			})
			.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
			  public void onClick(DialogInterface dialog, int id) {
				  removeDialog(CMENU_EDIT_LABEL); // <-- else view id is not updated
			  }
			});
			dialog = builderLabel.create();
	        break;
	    
	        
		case CMENU_EDIT_STYLE:
			AlertDialog.Builder builderSytle = new AlertDialog.Builder(this);
			builderSytle.setTitle(R.string.cmenu_edit_style)
			.setItems(R.array.buttonstyle_items, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					String[] itemValues = getResources().getStringArray(R.array.buttonstyle_item_values); 
					if (ButtonStore.modify(ButtonStore.MODIFY_STYLE, storedViewonCreateContext, itemValues[item])) {
						Toast.makeText(AVRRemoteActivity.this, R.string.toast_edit_style, Toast.LENGTH_SHORT).show();
						AVRRemoteActivity.this.selectPage(storedCurrentPage);
					}
					removeDialog(CMENU_EDIT_STYLE);
				}
			});
			dialog = builderSytle.create();
	        break;
	        
		
		case CMENU_EDIT_COMMAND:
			final EditText inputCommand = new EditText(this);
			inputCommand.setText(ButtonStore.getButtonCommand(storedViewonCreateContext));
			AlertDialog.Builder builderCommand = new AlertDialog.Builder(this);
			builderCommand.setTitle(R.string.cmenu_edit_command)
			.setView(inputCommand)
			.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				String newCommand = inputCommand.getText().toString();
				if (ButtonStore.modify(ButtonStore.MODIFY_COMMAND, storedViewonCreateContext, newCommand)) {
					Toast.makeText(AVRRemoteActivity.this, R.string.toast_edit_command, Toast.LENGTH_SHORT).show();
					AVRRemoteActivity.this.selectPage(storedCurrentPage);
				}
				removeDialog(CMENU_EDIT_COMMAND); // <-- else view id is not updated
			  }
			})
			.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
			  public void onClick(DialogInterface dialog, int id) {
				  removeDialog(CMENU_EDIT_COMMAND); // <-- else view id is not updated
			  }
			});
			dialog = builderCommand.create();
	        break;
	        
		case CMENU_EDIT_COMMENT:
			final EditText inputComment = new EditText(this);
			inputComment.setText(ButtonStore.getButtonComment(storedViewonCreateContext));
			AlertDialog.Builder builderComment = new AlertDialog.Builder(this);
			builderComment.setTitle(R.string.cmenu_edit_comment)
			.setView(inputComment)
			.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				String newComment = inputComment.getText().toString();
				if (ButtonStore.modify(ButtonStore.MODIFY_COMMENT, storedViewonCreateContext, newComment)) {
					Toast.makeText(AVRRemoteActivity.this, R.string.toast_edit_comment, Toast.LENGTH_SHORT).show();
					AVRRemoteActivity.this.selectPage(storedCurrentPage);
				}
				removeDialog(CMENU_EDIT_COMMENT); // <-- else view id is not updated
			  }
			})
			.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
			  public void onClick(DialogInterface dialog, int id) {
				  removeDialog(CMENU_EDIT_COMMENT); // <-- else view id is not updated
			  }
			});
			dialog = builderComment.create();
	        break;
	        
		case CMENU_EDIT_ICON:
			final EditText inputIcon = new EditText(this);
			inputIcon.setText(Integer.toString(ButtonStore.getButtonIconId(storedViewonCreateContext)));
			AlertDialog.Builder builderIcon = new AlertDialog.Builder(this);
			builderIcon.setTitle(R.string.cmenu_edit_icon)
			.setView(inputIcon)
			.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				String newIcon = inputIcon.getText().toString();
				if (ButtonStore.modify(ButtonStore.MODIFY_ICONID, storedViewonCreateContext, newIcon)) {
					Toast.makeText(AVRRemoteActivity.this, R.string.toast_edit_iconid, Toast.LENGTH_SHORT).show();
					AVRRemoteActivity.this.selectPage(storedCurrentPage);
				}
				removeDialog(CMENU_EDIT_ICON); // <-- else view id is not updated
			  }
			})
			.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
			  public void onClick(DialogInterface dialog, int id) {
				  removeDialog(CMENU_EDIT_ICON); // <-- else view id is not updated
			  }
			});
			dialog = builderIcon.create();
	        break;


		case OP_MENU_SAVE:
			final EditText input = new EditText(this);
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.dialog_save)
			.setView(input)
			.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				String name = input.getText().toString();
				// check if file exists
				File file = new File (getExternalFilesDir(null), name + ".xml");
				if (!file.exists()) {
					AVRRemoteActivity.this.writeLayoutToExternalStorage(name);
				}
				else {
					Bundle bundle = new Bundle();
					bundle.putString(KEY_FILENAME, name);
					AVRRemoteActivity.this.showDialog(OP_MENU_OVERWRITE, bundle);
				}
				removeDialog(OP_MENU_SAVE);
			  }
			})
			.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
			  public void onClick(DialogInterface dialog, int id) {
				  // removeDialog(OP_MENU_SAVE); <-- to preserve filename
			  }
			});
			dialog = builder.create();
	        break;
	        
	        
		case OP_MENU_OVERWRITE:
			AlertDialog.Builder builderov = new AlertDialog.Builder(this);
			builderov.setTitle(R.string.dialog_overwrite)
			.setPositiveButton(R.string.dialog_bt_overwrite, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				AVRRemoteActivity.this.writeLayoutToExternalStorage(bundle.getString(KEY_FILENAME));
			  }
			})
			.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
			  public void onClick(DialogInterface dialog, int id) {
				  // removeDialog(OP_MENU_SAVE); <-- to preserve filename
			  }
			});
			dialog = builderov.create();
	        break;
	        

		case OP_MENU_SELECT:
			AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
			builder2.setTitle(R.string.dialog_select_warning)
			.setPositiveButton(R.string.button_continue, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				Intent intent = new Intent(AVRRemoteActivity.this, AVRRemoteSelectActivity.class);
				AVRRemoteActivity.this.startActivityForResult(intent, REQUEST_CODE_SELECT);
			  }
			})
			.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
			  public void onClick(DialogInterface dialog, int id) {
				  // do nothing
			  }
			});
			dialog = builder2.create();
	        break;
	        
		
		case OP_MENU_SHARE:
			AlertDialog.Builder builder4 = new AlertDialog.Builder(this);
			builder4.setTitle(R.string.dialog_share)
			.setPositiveButton(R.string.dialog_share_plaintext, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					// write layout xml code to string
					OutputStream os = new ByteArrayOutputStream();
			        ButtonStore.writeButtonsToOutputStream(os);
			        
			        Intent sharePlainText = new Intent(Intent.ACTION_SEND);
					sharePlainText.setType("text/plain");
					sharePlainText.putExtra(Intent.EXTRA_TEXT, os.toString());
					AVRRemoteActivity.this.startActivity(Intent.createChooser(sharePlainText, AVRRemoteActivity.this.getResources().getString(R.string.dialog_share_chooser)));
					}
			})
			.setNegativeButton(R.string.dialog_share_file, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					// save current layout to internal storage world readable
					try {
						FileOutputStream fos = openFileOutput(CURRENTLAYOUTSHARE, MODE_WORLD_READABLE);
						ButtonStore.writeButtonsToOutputStream(fos);
						fos.close();
						if (Constants.DEBUG) Log.d(TAG, "onCreateDialog(): current layout written successfully");
					} catch (FileNotFoundException e) {
						Log.e(TAG, "onCreateDialog(): error writing current layout " + e.getMessage());
					} catch (IOException e) {
						Log.e(TAG, "onCreateDialog(): error writing current layout " + e.getMessage());
					}
					Intent shareXmlfile = new Intent(Intent.ACTION_SEND);
					shareXmlfile.setType("text/xml");
					File file = new File(AVRRemoteActivity.this.getFilesDir().getAbsolutePath() + "/" + CURRENTLAYOUTSHARE);
					Uri uri = Uri.fromFile(file);
					shareXmlfile.putExtra(Intent.EXTRA_STREAM, uri);
					AVRRemoteActivity.this.startActivity(Intent.createChooser(shareXmlfile, AVRRemoteActivity.this.getResources().getString(R.string.dialog_share_chooser)));  
			  }
			});
			dialog = builder4.create();
			break;
			
			
		default:
			return super.onCreateDialog(id, bundle);
	    }
	    
		return dialog;
	}
	
	
	// events dispatching & listeners & worker threads
	@Override
	public boolean dispatchTouchEvent(MotionEvent me) {
		this.detector.onTouchEvent(me);
		return super.dispatchTouchEvent(me);
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
	    int action = event.getAction();
	    int keyCode = event.getKeyCode();
	    switch (keyCode) {
        case KeyEvent.KEYCODE_VOLUME_UP:
            if (action == KeyEvent.ACTION_DOWN) {
                return VolumeKeyUpEvent();
            }
            
        case KeyEvent.KEYCODE_VOLUME_DOWN:
            if (action == KeyEvent.ACTION_DOWN) {
            	return VolumeKeyDownEvent();
            }
            
        default:
            return super.dispatchKeyEvent(event);
	    }
	}
	
	private boolean VolumeKeyUpEvent() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
        String strVolUpAction = prefs.getString("volupkey_action", 
        		getResources().getStringArray(R.array.entries_volume_up_keys)[0]);

        if (strVolUpAction.equals(getResources().getStringArray(R.array.entries_volume_up_keys)[0])) {
			return false;			
		}
		else if (strVolUpAction.equals(getResources().getStringArray(R.array.entries_volume_up_keys)[1])){
			if (taskHandlerSendAVRCommand != null) {
				if (taskHandlerSendAVRCommand.getStatus() == AsyncTask.Status.RUNNING) {
					taskHandlerSendAVRCommand.cancel(true);
				}
			}
			taskHandlerSendAVRCommand = new SendAVRCommand();
			taskHandlerSendAVRCommand.execute(getString(R.string.pref_cat_volumekeys_volup_custom_default));
			return true;
		}
		else if (strVolUpAction.equals(getResources().getStringArray(R.array.entries_volume_up_keys)[2])){
			String customcommand = prefs.getString("volupkey_custom", getString(R.string.pref_cat_volumekeys_volup_custom_default));
			if (taskHandlerSendAVRCommand != null) {
				if (taskHandlerSendAVRCommand.getStatus() == AsyncTask.Status.RUNNING) {
					taskHandlerSendAVRCommand.cancel(true);
				}
			}
			taskHandlerSendAVRCommand = new SendAVRCommand();
			taskHandlerSendAVRCommand.execute(customcommand);
			return true;
		}
		return false;
	}
	
	private boolean VolumeKeyDownEvent() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
        String strVolDownAction = prefs.getString("voldownkey_action", 
        		getResources().getStringArray(R.array.entries_volume_down_keys)[0]);

        if (strVolDownAction.equals(getResources().getStringArray(R.array.entries_volume_down_keys)[0])) {
			return false;			
		}
		else if (strVolDownAction.equals(getResources().getStringArray(R.array.entries_volume_down_keys)[1])){
			if (taskHandlerSendAVRCommand != null) {
				if (taskHandlerSendAVRCommand.getStatus() == AsyncTask.Status.RUNNING) {
					taskHandlerSendAVRCommand.cancel(true);
				}
			}
			taskHandlerSendAVRCommand = new SendAVRCommand();
			taskHandlerSendAVRCommand.execute(getString(R.string.pref_cat_volumekeys_voldown_custom_default));
			return true;
		}
		else if (strVolDownAction.equals(getResources().getStringArray(R.array.entries_volume_down_keys)[2])){
			String customcommand = prefs.getString("voldownkey_custom", getString(R.string.pref_cat_volumekeys_voldown_custom_default));
			if (taskHandlerSendAVRCommand != null) {
				if (taskHandlerSendAVRCommand.getStatus() == AsyncTask.Status.RUNNING) {
					taskHandlerSendAVRCommand.cancel(true);
				}
			}
			taskHandlerSendAVRCommand = new SendAVRCommand();
			taskHandlerSendAVRCommand.execute(customcommand);
			return true;
		}
		return false;
	}

	
	@Override
	public void onSwipe(int direction) {
		
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		
		switch (direction) {

		case SimpleGestureFilter.SWIPE_RIGHT:
			this.selectPage(storedCurrentPage-1);
			break;
		case SimpleGestureFilter.SWIPE_LEFT:
			this.selectPage(storedCurrentPage+1);
			break;
		case SimpleGestureFilter.SWIPE_DOWN:
			if (settings.getBoolean("screenlock", false))
				AVRLayoutUtils.lockScreen(false, true, this);
			break;
		case SimpleGestureFilter.SWIPE_UP:
			if (settings.getBoolean("screenlock", false))
				AVRLayoutUtils.lockScreen(true, true, this);
			break;
		}
	}
	
	private class AVRButtonOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			if (taskHandlerSendAVRCommand != null) {
				if (taskHandlerSendAVRCommand.getStatus() == AsyncTask.Status.RUNNING) {
					taskHandlerSendAVRCommand.cancel(true);
				}
			}
			taskHandlerSendAVRCommand = new SendAVRCommand();
			taskHandlerSendAVRCommand.execute(ButtonStore.getButtonCommand(v.getId()));
		}
	}
	
	protected class SendAVRCommand extends AsyncTask<String, Void, Boolean> {
		@Override
		protected Boolean doInBackground(String... params) {
			
			ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI); 
			if (networkInfo.isConnected()) {
				
				// to block multiple send tasks esp in case of no connection
				if (AVRConnection.isAVRconnected()) {
					return AVRConnection.sendComplexCommand(params[0]);
				}
				// try reconnect
				if (AVRConnection.reconnect()) {
					if (Constants.DEBUG) Log.d(TAG, "SendAVRCommand.doInBackground(); reconnect success");
					return AVRConnection.sendComplexCommand(params[0]);
				}
				else {
					if (Constants.DEBUG) Log.d(TAG, "SendAVRCommand.doInBackground(); reconnect unsuccessfull");
					return false;
				}
			}
			else {
				return false;
			}
			
		}
		protected void onPostExecute(Boolean connected) {
			updateConnectionStatus(connected);
		}
	}
	
	@Override
	protected void updateConnectionStatus(boolean status) {
		super.updateConnectionStatus(status);
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI); 
		if (!networkInfo.isConnected()) {
			Toast.makeText(this, R.string.wifi_not_avaiable, Toast.LENGTH_SHORT).show();
		}
		else if (!status) {
			Toast.makeText(this, R.string.toast_no_connection, Toast.LENGTH_SHORT).show();
		}
	}
	
	// layout state handling
	private boolean loadLayoutFromExternalStorage (String filename) {
		
		// reset page
		storedCurrentPage = 1;

		if ((Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) ||
				(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED_READ_ONLY))) {
			
			final File xmlFile = new File(getExternalFilesDir(null), filename);
			
			try {
				InputStream fis = new FileInputStream(xmlFile);
				if (ButtonStore.readButtonsFromXmlInputStream(fis)) {
					fis.close();
					if (Constants.DEBUG) Log.d(TAG, "loadLayoutFromExternalStorage(): layout " + filename + "loaded successfully");
					return true; // good case
				}
				else {
					fis.close();
					Toast.makeText(this, R.string.toast_no_valid_xml_file, Toast.LENGTH_LONG).show();
				}
			} catch (FileNotFoundException e) {
				Log.e(TAG, "loadLayoutFromExternalStorage(): error loading layout " + e.getMessage());
				Toast.makeText(this, R.string.toast_import_file_not_found, Toast.LENGTH_LONG).show();
			} catch (IOException e) {
				Log.e(TAG, "loadLayoutFromExternalStorage(): error loading layout " + e.getMessage());
				Toast.makeText(this, R.string.toast_import_file_not_found, Toast.LENGTH_LONG).show();
			}
			
			// load current layout or default
			try {			
				FileInputStream fis = this.openFileInput(CURRENTLAYOUT);
				ButtonStore.readButtonsFromXmlInputStream(fis);
				fis.close();
				if (Constants.DEBUG) Log.d(TAG, "loadLayoutFromExternalStorage(): current layout loaded successfully");
			} catch (FileNotFoundException e) {
				Log.e(TAG, "loadLayoutFromExternalStorage(): error loading current layout " + e.getMessage());
				ButtonStore.readButtonsFromXmlInputStream(this.getResources().openRawResource(R.raw.buttonslayout));
			} catch (IOException e) {
				Log.e(TAG, "loadLayoutFromExternalStorage(): error loading current layout " + e.getMessage());
				ButtonStore.readButtonsFromXmlInputStream(this.getResources().openRawResource(R.raw.buttonslayout));
			}
		}
		else {
			Toast.makeText(this, R.string.toast_media_not_mounted, Toast.LENGTH_LONG).show();
		}
				
		return false;
	}
	
	private boolean writeLayoutToExternalStorage(String filename) {
		
		if (filename.equals("")) {
			Toast.makeText(this, R.string.toast_could_not_write_file, Toast.LENGTH_LONG).show();
			return false;
		}
		
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			// prepare file
			final File xmlFile = new File(getExternalFilesDir(null), filename + ".xml");
			final File screenshotFile = new File(getExternalFilesDir(null), filename + ".png");
			boolean success = false;
			
			try {
				// write xml file
		        OutputStream os = new FileOutputStream(xmlFile);
		        success = ButtonStore.writeButtonsToOutputStream(os);	
		        os.close();
		        // write layout screenshot 
		        Bitmap b = Bitmap.createBitmap( layoutButtons.getWidth(), layoutButtons.getHeight(), Bitmap.Config.ARGB_8888);                
		        Canvas c = new Canvas(b);
		        layoutButtons.draw(c);
		        Bitmap b_scaled = Bitmap.createScaledBitmap(b, b.getWidth()/2, b.getHeight()/2, false);
		        OutputStream bos = new FileOutputStream(screenshotFile);
		        b_scaled.compress(Bitmap.CompressFormat.PNG, 80, bos);
		        bos.flush();
		        bos.close();
		    } catch (IOException e) {
		    	Log.e("ExternalStorage", "Error writing " + xmlFile, e);
		    }
		    if (success) {
		    	Toast.makeText(this, "Layout " + filename + " saved.", Toast.LENGTH_SHORT).show();
		    	if (Constants.DEBUG) Log.d(TAG," writeCurrentLayoutToExternalStorage(): written " + xmlFile + ", " + screenshotFile);
		    	return true;
		    }
		    else {
		    	Toast.makeText(this, R.string.toast_could_not_write_file, Toast.LENGTH_LONG).show();
				return false;
		    }	
		} 
		else {
			Toast.makeText(this, R.string.toast_media_not_mounted, Toast.LENGTH_LONG).show();
			return false;
		}
	}
	
	// layout
	private void selectPage(int pageNo) {
		// ensure that page exists
		int pageCount = ButtonStore.getNoOfPages(AVRLayoutUtils.determinOrientation(this));
		Display display = this.getWindowManager().getDefaultDisplay();
		if (pageNo > 0 && pageNo <= pageCount) {
			AVRRemoteActivity.this.clearButtonLayout();
			AVRRemoteActivity.this.buildButtonLayout(pageNo, display.getWidth());
			storedCurrentPage = pageNo;
		}	
	}
	
	private void clearButtonLayout() {
		layoutButtons.removeAllViews();
	}
	
	private void buildButtonLayout(int pageNo, int displayWidth) {
	
		ButtonStore.initializeButtonStream(pageNo,AVRLayoutUtils.determinOrientation(this), displayWidth, 
				2 * BUTTON_MARGIN, 2 * LAYOUT_MARGIN);
		
		// local helper variables
		int buttonHeight = ButtonStore.getButtonHeight();
		int buttonWidth = ButtonStore.getButtonwidth();
		// int buttonMargin = buttonStore.getButtonMargin();
		int maxButtonPerRow = (int) Math.floor((double) displayWidth / (double) (buttonWidth + 2 * BUTTON_MARGIN));
		boolean firstButton = true;
		boolean alignTop = true;
		boolean alignLeft = true;
		int alignBelowId = 0;
		int storeId = 0;
		int currentButtonRowFillup = 0;
		
		// calculate max button row width
		int maxButtonRowWidth = maxButtonPerRow * (buttonWidth + 2 * BUTTON_MARGIN);
		// int maxButtonRowWidth = (int) 0.9 * displayWidth;
		
		alignBelowId = this.buildPageBarLayout(maxButtonRowWidth, buttonHeight, pageNo);
		alignTop = false;
		
		// range 10-999, to be below first possible button id (1000)
		int dividerId = 100;
		dividerId = alignBelowId = addHorizontalDummyView(dividerId, alignBelowId, HOR_DUMMY_VIEW_HEIGHT, buttonWidth, maxButtonPerRow);
		
				
		while(ButtonStore.streamHasButton()) {
			
			XmlButton xmlButton = ButtonStore.getButtonFromStream();
			
			
			// consider newrow or seperator if not first element
			if ((ButtonStore.streamIsNewRow() || ButtonStore.streamIsSeperator()) && !firstButton) {
				alignTop = false;
				alignLeft = true;
				currentButtonRowFillup = 0;
				alignBelowId = storeId;
				// add horizontal seperator
				if (ButtonStore.streamIsSeperator()) {
					dividerId = alignBelowId = this.addHorizontalDummyView(dividerId, alignBelowId, HOR_DUMMY_VIEW_HEIGHT, buttonWidth, maxButtonPerRow);
				}
			}
			firstButton = false;
			
			// consider and limit button span
			int span = xmlButton.span; 
			if (span > maxButtonPerRow) span = maxButtonPerRow;
			int tmpButtonWidth = span * buttonWidth + (span - 1) * (2 * BUTTON_MARGIN);
			
			// check if new row is needed because current button does not match
			if ((currentButtonRowFillup + span) > maxButtonPerRow) {
				alignTop = false;
				alignLeft = true;
				currentButtonRowFillup = 0;
				alignBelowId = storeId;
			}
			
			// consider skip button, until end of current row only
			int skip = ButtonStore.streamSkipButton();
			while((skip > 0) && (currentButtonRowFillup < maxButtonPerRow)) {
				skip--;
				currentButtonRowFillup++;
				boolean addDivider = false;
				if (currentButtonRowFillup < maxButtonPerRow) addDivider = true;
				boolean dividerVisible = false;
				if ((skip == 0) && ((currentButtonRowFillup + span) <= maxButtonPerRow)) dividerVisible = true;
				dividerId = storeId = this.addDummyButton(dividerId, storeId, alignBelowId, alignTop, alignLeft, buttonHeight, buttonWidth, addDivider, dividerVisible);
				alignLeft = false;
			}
			
			// check again if new row is needed because current button does not match
			if ((currentButtonRowFillup + span) > maxButtonPerRow) {
				alignTop = false;
				alignLeft = true;
				currentButtonRowFillup = 0;
				alignBelowId = storeId;
			}
			
			// add current button to row fillup counter
			currentButtonRowFillup += span;
			
			// create layout params
			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(tmpButtonWidth, buttonHeight);
			lp.setMargins(BUTTON_MARGIN, BUTTON_MARGIN, BUTTON_MARGIN, BUTTON_MARGIN);
			// set alignment rules
			if (alignTop) {
				lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);				
			}
			else {
				lp.addRule(RelativeLayout.BELOW, alignBelowId);
			}
			
			if (alignLeft) {
				lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			}
			else {
				lp.addRule(RelativeLayout.RIGHT_OF, storeId);
			}
			
			// add button to view
			if ((ButtonIcons.getResId(xmlButton.iconid) != ButtonIcons.NO_ICON) && AVRLayoutUtils.UseIcons) {
				ImageButton button = new ImageButton(this);
				button.setColorFilter(getResources().getColor(AVRLayoutUtils.resIdBtColor), PorterDuff.Mode.SRC_ATOP);
				button.setImageDrawable(getResources().getDrawable(ButtonIcons.getResId(xmlButton.iconid)));
				button.setId(xmlButton.id);
				button.setBackgroundResource(AVRLayoutUtils.getButtonStyleResId(xmlButton.style));
				button.setPadding(BUTTON_PADDING * 2, BUTTON_PADDING, BUTTON_PADDING * 2, BUTTON_PADDING);
				layoutButtons.addView(button, lp);
				button.setOnClickListener(avrButtonOnClickListener);
				this.registerForContextMenu(button);
			}
			else {
				Button button = new Button(this);
				button.setId(xmlButton.id);
				button.setTextAppearance(this, AVRLayoutUtils.resIdTextAppearence);
				button.setTextColor(getResources().getColor(AVRLayoutUtils.resIdBtColor));
				button.setBackgroundResource(AVRLayoutUtils.getButtonStyleResId(xmlButton.style));
				button.setText(xmlButton.label);
				button.setShadowLayer(3, -1, -1, getResources().getColor(R.color.text_shadow));
				button.setPadding(BUTTON_PADDING * 2, BUTTON_PADDING, BUTTON_PADDING * 2, BUTTON_PADDING);
				layoutButtons.addView(button, lp);
				button.setOnClickListener(avrButtonOnClickListener);
				this.registerForContextMenu(button);
			}
			
			// update storeId and alignLeft
			storeId = xmlButton.id;
			alignLeft = false;			
		}
	}
	
	private int buildPageBarLayout (int maxButtonRowWidth, int buttonheight, int pageNo) {
		
		int pageCount = ButtonStore.getNoOfPages(AVRLayoutUtils.determinOrientation(this));
		
		int buttonwidth = (maxButtonRowWidth - pageCount * (2 * BUTTON_MARGIN)) / pageCount;
		boolean alignLeft = true;
		int storeid = 0;
		
		for (int id = 1; id <= pageCount; id++) {
			
			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(buttonwidth, LayoutParams.WRAP_CONTENT);
			lp.setMargins(BUTTON_MARGIN, BUTTON_MARGIN, BUTTON_MARGIN , BUTTON_MARGIN);
			// set alignment rules
			lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);				
			if (alignLeft) {
				lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			}
			else {
				lp.addRule(RelativeLayout.RIGHT_OF, storeid);
			}
			
			// add button to view
			Button button = new Button(this);
			button.setId(id);
			button.setTextAppearance(this, AVRLayoutUtils.resIdTextAppearence);
			button.setSingleLine(true);
			button.setTextColor(getResources().getColor(AVRLayoutUtils.resIdBtColor));
			button.setShadowLayer(3, -1, -1, getResources().getColor(R.color.text_shadow));
			button.setBackgroundResource(AVRLayoutUtils.resIdBtStateList);
			String pageName = ButtonStore.getPageName(id, AVRLayoutUtils.determinOrientation(this));
			if (pageName.equals("")) 
				button.setText("Page " + Integer.toString(id));
			else
				button.setText(pageName);
			layoutButtons.addView(button, lp);
			
			if (id == pageNo) {
				// add bar below
				View bar = new View(this);
				bar.setBackgroundColor(getResources().getColor(AVRLayoutUtils.resIdTabColor));
				RelativeLayout.LayoutParams barlp = new RelativeLayout.LayoutParams(buttonwidth, 6);
				barlp.setMargins(BUTTON_MARGIN, 0, BUTTON_MARGIN, 0);
				barlp.addRule(RelativeLayout.BELOW, id);
				if (alignLeft) {
					barlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
				}
				else {
					barlp.addRule(RelativeLayout.RIGHT_OF, storeid);
				}
				bar.setId(50);
				layoutButtons.addView(bar, barlp);
			}
			
			storeid = id;
			alignLeft = false;
			
			button.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					AVRRemoteActivity.this.selectPage(v.getId());
				}
			});
		}
		
		View horbar = new View(this);
		horbar.setBackgroundColor(getResources().getColor(AVRLayoutUtils.resIdTabColor));
		RelativeLayout.LayoutParams horbarlp = new RelativeLayout.LayoutParams(maxButtonRowWidth, 1);
		horbarlp.setMargins(BUTTON_MARGIN, 0, BUTTON_MARGIN, 0);
		horbarlp.addRule(RelativeLayout.BELOW, 50);
		horbarlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		horbar.setId(51);
		layoutButtons.addView(horbar, horbarlp);
		
		return 51;
	}
	
	private int addDummyButton(int dividerId, int storeId, int alignBelowId, boolean alignTop, boolean alignLeft, int buttonHeight, int buttonwidth, boolean addDivider, boolean dividerVisible) {
		
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(buttonwidth, buttonHeight);
		lp.setMargins(BUTTON_MARGIN, BUTTON_MARGIN, BUTTON_MARGIN, BUTTON_MARGIN);
		// set alignment rules
		if (alignTop) lp.addRule(RelativeLayout.ALIGN_PARENT_TOP); else lp.addRule(RelativeLayout.BELOW, alignBelowId);
		if (alignLeft) lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT); else lp.addRule(RelativeLayout.RIGHT_OF, storeId);		
		// add button to view
		Button button = new Button(this);
		button.setEnabled(false);
		dividerId++;
		button.setId(dividerId);
		button.setBackgroundResource(AVRLayoutUtils.resIdBtStateList);
		layoutButtons.addView(button, lp);
		return dividerId;
	}
	
	private int addHorizontalDummyView(int dividerId, int alignBelowId, int height, int buttonWidth, int maxButtonPerRow) {
		View horDivider = new View(this);
		horDivider.setBackgroundColor(Color.TRANSPARENT);
		int viewWidth = buttonWidth * maxButtonPerRow + BUTTON_MARGIN * 2 * (maxButtonPerRow - 1);
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(viewWidth, height);
		lp.setMargins(BUTTON_MARGIN, 0, BUTTON_MARGIN, 0);
		lp.addRule(RelativeLayout.BELOW, alignBelowId);
		lp.addRule(RelativeLayout.ALIGN_LEFT);
		dividerId++;
		horDivider.setId(dividerId);
		layoutButtons.addView(horDivider, lp);
		return dividerId;
	}
}
