package de.thjunge11.avrremote;

import java.io.File;
import java.io.FilenameFilter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.Toast;

public class AVRRemoteSelectActivity extends ListActivity {
	
	private static final String TAG = AVRRemoteSelectActivity.class.getSimpleName();
	private List<Map<String, Object>> layoutList;
	private static final String KEY_FILENAME = "f";
	private static final String KEY_LAYOUTNAME = "l";
	private static final String KEY_DETAILS = "d";
	private static final String KEY_IMGSOURCE = "i";
	private static final String KEY_POS_ID = "pos";
	private static final int DEFAULT_LAYOUT_ID = 0;
	private static final int DIALOG_RENAME = 1;
	public static final String INTENT_EXTRA_FILENAME = "de.thjunge11.avrremote.filename";
	public static final String INTENT_EXTRA_IS_DEFAULT = "de.thjunge11.avrremote.isdefault";
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		layoutList = new ArrayList<Map<String,Object>>();
		
		// add first entry for default layout (DEFAULT_LAYOUT_ID = 0) 
		Map<String, Object> mapDef = new HashMap<String, Object>();
		mapDef.put(KEY_LAYOUTNAME, getResources().getString(R.string.filelist_default));
		mapDef.put(KEY_FILENAME, null);
		mapDef.put(KEY_DETAILS, getResources().getString(R.string.filelist_default_name));
		mapDef.put(KEY_IMGSOURCE, null);
		layoutList.add(mapDef);
		
		// add entries from xml files in external storage dir
		if ((Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) ||
				(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED_READ_ONLY))) {
			
			File externalStorageDir = this.getExternalFilesDir(null);
			File[] files = externalStorageDir.listFiles(new FilenameFilter() {
				
				@Override
				public boolean accept(File dir, String filename) {
					return filename.endsWith("xml");
				}
			});
			
			DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(this);
			DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(this);
			
			for (File file : files) {
				// new map to add to fileList
				Map<String, Object> map = new HashMap<String, Object>();
				map.put(KEY_FILENAME, file);
				map.put(KEY_LAYOUTNAME, file.getName().substring(0, file.getName().length() - 4));
				Date date = new Date(file.lastModified());
				map.put(KEY_DETAILS, "last modified on: " + dateFormat.format(date) + " " + timeFormat.format(date));
				// check if thumbnail to xml file exists
				File thumbnail = new File(file.getAbsolutePath().substring(0, file.getAbsolutePath().length() - 4) + ".png");
				if (thumbnail.exists()) {
					map.put(KEY_IMGSOURCE, thumbnail);
				}
				else {
					map.put(KEY_IMGSOURCE, null);
				}
				layoutList.add(map);
			}
		}
		
		// set up adapter
		String[] from = {KEY_LAYOUTNAME, KEY_DETAILS, KEY_IMGSOURCE};
		int[] to = {R.id.filelist_item_name, R.id.filelist_item_detail, R.id.filelist_item_thumbnail};
		
		SimpleAdapter adapter = new SimpleAdapter(this, layoutList, R.layout.filelist_item, from, to);
		
		// setup view binder
		adapter.setViewBinder(new ViewBinder() {
			
			@Override
			public boolean setViewValue(View view, Object data, String textRepresentation) {
				
				switch (view.getId()) {
				
				case R.id.filelist_item_thumbnail :
					if (data != null) {
						Bitmap bm = BitmapFactory.decodeFile(((File) data).getAbsolutePath());
						((ImageView) view).setImageBitmap(bm);
					}
					else 
					{
						((ImageView) view).setImageResource(R.drawable.dummy);
					}
					return true; // data is bound
				}
				return false; // no binding has occurred, use default SimpleAdapter binding
			}
		});
		
		this.setListAdapter(adapter);
			
		this.getListView().setOnItemClickListener(new OnItemClickListener() {
			
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				
				AVRRemoteSelectActivity.this.selectLayout(id);
			}
		});
		
		this.registerForContextMenu(getListView());
		
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		
		AdapterContextMenuInfo adapterInfo = (AdapterContextMenuInfo) menuInfo;
		
		// no menu for first entry default
		if (adapterInfo.id > 0) {
			MenuInflater inflater = this.getMenuInflater();
			inflater.inflate(R.menu.context_list, menu);
		}
		
		if (Constants.DEBUG) {
			Log.d(TAG, "onCreateContextMenu(): menuInfo.id: " + adapterInfo.id);
			Log.d(TAG, "onCreateContextMenu(): menuInfo.position :" + adapterInfo.position);
			Log.d(TAG, "onCreateContextMenu(): menuInfo targetVioew Id: " + adapterInfo.targetView.getId());
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		 
		AdapterContextMenuInfo adapterInfo = (AdapterContextMenuInfo) item.getMenuInfo();
		 
		 switch (item.getItemId()) {
		 
		 case R.id.cmenulist_select :
			 this.selectLayout(adapterInfo.id);
			 return true;
		
		 case R.id.cmenulist_rename :
			 Bundle bundle = new Bundle();
			 bundle.putString(KEY_LAYOUTNAME, (String) layoutList.get((int) adapterInfo.id).get(KEY_LAYOUTNAME));
			 bundle.putLong(KEY_POS_ID, adapterInfo.id);
			 this.showDialog(DIALOG_RENAME, bundle);
			 return true;
		
		 case R.id.cmenulist_delete :
			 this.deleteLayout(adapterInfo.id);
			 ((SimpleAdapter)this.getListAdapter()).notifyDataSetChanged();
			 return true;
			 
		 default :
			 return super.onContextItemSelected(item);
		 }
	}
	
	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		Dialog dialog;
		switch(id) {
		
		case DIALOG_RENAME:
			final EditText input = new EditText(this);
			final long longId = args.getLong(KEY_POS_ID);
			input.setText(args.getString(KEY_LAYOUTNAME));
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.cmenulist_rename)
			.setView(input)
			.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				String newName = input.getText().toString();
				AVRRemoteSelectActivity.this.renameLayout(longId, newName);
				((SimpleAdapter)AVRRemoteSelectActivity.this.getListAdapter()).notifyDataSetChanged();
				removeDialog(DIALOG_RENAME); 
			  }
			})
			.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
			  public void onClick(DialogInterface dialog, int id) {
				  removeDialog(DIALOG_RENAME);
			  }
			});
			dialog = builder.create();
	        break;
		
		default: 
			dialog = null;
		}
		return dialog;
	}
	
	
	private void selectLayout(long id) {
		
		Intent intent = new Intent(AVRRemoteSelectActivity.this, AVRRemoteActivity.class);
		if (id == DEFAULT_LAYOUT_ID) {
			intent.putExtra(INTENT_EXTRA_FILENAME, "default");
			intent.putExtra(INTENT_EXTRA_IS_DEFAULT, true);
		}
		else {
			intent.putExtra(INTENT_EXTRA_FILENAME, ((File) layoutList.get((int) id).get(KEY_FILENAME)).getName());
			intent.putExtra(INTENT_EXTRA_IS_DEFAULT, false);
		}
		AVRRemoteSelectActivity.this.setResult(Activity.RESULT_OK,intent);
		AVRRemoteSelectActivity.this.finish();
	}
	
	private void deleteLayout(long id) {
	
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			File file = (File) layoutList.get((int) id).get(KEY_FILENAME);
			File fileimg = (File) layoutList.get((int) id).get(KEY_IMGSOURCE);

			if (file != null) {
				
				if (file.exists() && file.isFile() && file.canWrite()) {
					file.delete();
				}
				
				if (fileimg != null) {
					
					if (fileimg.exists() && fileimg.isFile() && fileimg.canWrite())
					{
						fileimg.delete();
					}
				
					// check for file and thumbnail
					if (!file.exists() && !fileimg.exists()) {
						Toast.makeText(this, R.string.toast_list_delete, Toast.LENGTH_SHORT).show();
						layoutList.remove((int) id);
					}
				}
				else {
					// check for file only
					if (!file.exists()) {
						Toast.makeText(this, R.string.toast_list_delete, Toast.LENGTH_SHORT).show();
						layoutList.remove((int) id);
					}
				}				
			}
		}
		else {
			Toast.makeText(this, R.string.toast_media_not_mounted, Toast.LENGTH_LONG).show();
		}
	}
	
	private void renameLayout(long id, String newName) {
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			File file = (File) layoutList.get((int) id).get(KEY_FILENAME);
			File fileimg = (File) layoutList.get((int) id).get(KEY_IMGSOURCE);
			File newFile = null;
			File newimgFile = null;

			if (file != null) {
				
				if (file.exists() && file.isFile() && file.canWrite()) {
					newFile = new File(file.getParent(), newName + ".xml");
					file.renameTo(newFile);
				}
				
				if (fileimg != null) {
					
					if (fileimg.exists() && fileimg.isFile() && fileimg.canWrite())
					{
						newimgFile = new File(fileimg.getParent(), newName + ".png");
						fileimg.renameTo(newimgFile);
					}
				
					// check for file and thumbnail
					if (newFile.exists() && newimgFile.exists()) {
						Toast.makeText(this, R.string.toast_list_rename, Toast.LENGTH_SHORT).show();
						layoutList.remove((int) id);
						
						// update list
						DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(this);
						DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(this);
						Map<String, Object> map = new HashMap<String, Object>();
						map.put(KEY_FILENAME, newFile);
						map.put(KEY_LAYOUTNAME, newFile.getName().substring(0, newFile.getName().length() - 4));
						Date date = new Date(newFile.lastModified());
						map.put(KEY_DETAILS, "last modified on: " + dateFormat.format(date) + " " + timeFormat.format(date));
						map.put(KEY_IMGSOURCE, newimgFile);
						layoutList.add(map);
					}
				}
				else {
					// check for file only
					if (newFile.exists()) {
						Toast.makeText(this, R.string.toast_list_rename, Toast.LENGTH_SHORT).show();
						layoutList.remove((int) id);
						// update list
						DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(this);
						DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(this);
						Map<String, Object> map = new HashMap<String, Object>();
						map.put(KEY_FILENAME, newFile);
						map.put(KEY_LAYOUTNAME, newFile.getName().substring(0, newFile.getName().length() - 4));
						Date date = new Date(newFile.lastModified());
						map.put(KEY_DETAILS, "last modified on: " + dateFormat.format(date) + " " + timeFormat.format(date));
						map.put(KEY_IMGSOURCE, null);
						layoutList.add(map);
					}
				}				
			}
		}
		else {
			Toast.makeText(this, R.string.toast_media_not_mounted, Toast.LENGTH_LONG).show();
		}
	}
}
