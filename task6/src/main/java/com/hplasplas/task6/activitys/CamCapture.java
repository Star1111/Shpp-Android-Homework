package com.hplasplas.task6.activitys;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hplasplas.task6.R;
import com.hplasplas.task6.adapters.PictureInFolderAdapter;
import com.hplasplas.task6.dialogs.FileNameInputDialog;
import com.hplasplas.task6.dialogs.RenameErrorDialog;
import com.hplasplas.task6.loaders.BitmapLoader;
import com.hplasplas.task6.models.ListItemModel;
import com.hplasplas.task6.util.IntQueue;
import com.starsoft.recyclerViewItemClickSupport.ItemClickSupport;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static com.hplasplas.task6.setting.Constants.DEBUG;
import static com.hplasplas.task6.setting.Constants.DEFAULT_FILE_NAME_PREFIX;
import static com.hplasplas.task6.setting.Constants.ERROR_DIALOG_TAG;
import static com.hplasplas.task6.setting.Constants.FILE_NAME_SUFFIX;
import static com.hplasplas.task6.setting.Constants.FILE_NAME_TO_LOAD;
import static com.hplasplas.task6.setting.Constants.FILE_RENAME_DIALOG_TAG;
import static com.hplasplas.task6.setting.Constants.FIRST_LOAD_PICTURE_HEIGHT;
import static com.hplasplas.task6.setting.Constants.FIRST_LOAD_PICTURE_WIDTH;
import static com.hplasplas.task6.setting.Constants.GET_PICTURE_REQUEST_CODE;
import static com.hplasplas.task6.setting.Constants.MAIN_PICTURE_LOADER_ID;
import static com.hplasplas.task6.setting.Constants.NEED_PRIVATE_FOLDER;
import static com.hplasplas.task6.setting.Constants.NO_EXISTING_FILE_NAME;
import static com.hplasplas.task6.setting.Constants.PICTURE_FOLDER_NAME;
import static com.hplasplas.task6.setting.Constants.PREFERENCES_FILE;
import static com.hplasplas.task6.setting.Constants.PREF_FOR_LAST_FILE_NAME;
import static com.hplasplas.task6.setting.Constants.PREVIEW_PICTURE_HEIGHT;
import static com.hplasplas.task6.setting.Constants.PREVIEW_PICTURE_LOADER_START_ID;
import static com.hplasplas.task6.setting.Constants.PREVIEW_PICTURE_WIDTH;
import static com.hplasplas.task6.setting.Constants.REQUESTED_PICTURE_HEIGHT;
import static com.hplasplas.task6.setting.Constants.REQUESTED_PICTURE_WIDTH;
import static com.hplasplas.task6.setting.Constants.ROWS_IN_TABLE;
import static com.hplasplas.task6.setting.Constants.TIME_STAMP_PATTERN;

public class CamCapture extends AppCompatActivity implements View.OnClickListener, LoaderManager.LoaderCallbacks<Bitmap>,
        PopupMenu.OnMenuItemClickListener, FileNameInputDialog.FileNameInputDialogListener {
    
    private final String TAG = getClass().getSimpleName();
    public ArrayList<ListItemModel> myFilesItemList;
    private boolean myMainPictureLoaded;
    private boolean mCanComeBack;
    private boolean mNewPhoto;
    private IntQueue myPreviewInLoad;
    private int myContextMenuPosition = -1;
    private int myFilesInFolder;
    private ImageView myImageView;
    private TextView myFilesInFolderText;
    private Bitmap myMainBitmap;
    private Button myButton;
    private ProgressBar mainProgressBar;
    private RecyclerView myRecyclerView;
    private PictureInFolderAdapter myPictureInFolderAdapter;
    private File myCurrentPictureFile;
    private File myPictureDirectory;
    private int myMainImageViewHeight;
    private int myMainImageViewWidth;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
        if (DEBUG) {
            Log.d(TAG, "onCreate: ");
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cam_capture_activity);
        findViews();
        myButton.setOnClickListener(this);
        adjustRecyclerView();
        loadMainBitmap(getMyPreferences());
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        
        if (DEBUG) {
            Log.d(TAG, "onActivityResult: ");
        }
        if (mNewPhoto) {
            if (requestCode == GET_PICTURE_REQUEST_CODE && resultCode == RESULT_OK) {
                loadMainBitmap(myCurrentPictureFile.getPath());
                myFilesItemList.add(new ListItemModel(myCurrentPictureFile));
                myPictureInFolderAdapter.notifyItemInserted(myFilesItemList.size() - 1);
                loadPreview(myFilesItemList.size() - 1);
                myRecyclerView.scrollToPosition(myFilesItemList.size() - 1);
                myFilesInFolder++;
                setFilesInFolderText(myFilesInFolder);
            }
        }
        myButton.setEnabled(true);
    }
    
    @Override
    protected void onResume() {
        
        if (DEBUG) {
            Log.d(TAG, "onResume: ");
        }
        mCanComeBack = false;
        if (!mNewPhoto) {
            myFilesInFolder = getDirectory().listFiles().length;
            setFilesInFolderText(myFilesInFolder);
            createFilesItemList(getDirectory().listFiles());
            myPictureInFolderAdapter = setAdapter(myRecyclerView);
        } else {
            mNewPhoto = false;
        }
        loadMainBitmap();
        super.onResume();
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        
        if (DEBUG) {
            Log.d(TAG, "onSaveInstanceState: ");
        }
        mCanComeBack = true;
        super.onSaveInstanceState(outState);
    }
    
    @Override
    protected void onPause() {
        
        if (DEBUG) {
            Log.d(TAG, "onPause: ");
        }
        if (myCurrentPictureFile != null) {
            getMyPreferences().edit()
                    .putString(PREF_FOR_LAST_FILE_NAME, myCurrentPictureFile.getPath())
                    .apply();
        }
        super.onPause();
    }
    
    @Override
    protected void onDestroy() {
        
        if (DEBUG) {
            Log.d(TAG, "onDestroy: ");
            if (myMainBitmap != null && !mCanComeBack) {
                myMainBitmap.recycle();
            }
            recyclePreviewBitmaps(myFilesItemList);
        }
        super.onDestroy();
    }
    
    private void recyclePreviewBitmaps(ArrayList<ListItemModel> filesItemList) {
        
        for (int i = 0, y = filesItemList.size(); i < y; i++) {
            Bitmap currentBitmap = filesItemList.get(i).getPicturePreview();
            if (currentBitmap != null) {
                currentBitmap.recycle();
            }
        }
    }
    
    private void findViews() {
        
        myImageView = (ImageView) findViewById(R.id.foto_frame);
        mainProgressBar = (ProgressBar) findViewById(R.id.mainProgressBar);
        mainProgressBar.setVisibility(View.VISIBLE);
        myFilesInFolderText = (TextView) findViewById(R.id.files_in_folder);
        myButton = (Button) findViewById(R.id.foto_button);
        myRecyclerView = (RecyclerView) findViewById(R.id.foto_list);
    }
    
    private void adjustRecyclerView() {
        
        myRecyclerView.setHasFixedSize(true);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            myRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        } else {
            myRecyclerView.setLayoutManager(new GridLayoutManager(this, ROWS_IN_TABLE, LinearLayoutManager.HORIZONTAL, false));
            myRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        }
        myRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL));
        
        ItemClickSupport.addTo(myRecyclerView).setOnItemClickListener((recyclerView, position, v) -> onMyRecyclerViewItemClicked(position, v));
        ItemClickSupport.addTo(myRecyclerView).setOnItemLongClickListener((recyclerView, position, v) -> onMyRecyclerViewItemLongClicked(position, v));
    }
    
    private PictureInFolderAdapter setAdapter(RecyclerView recyclerView) {
        
        PictureInFolderAdapter adapter = new PictureInFolderAdapter(myFilesItemList);
        if (recyclerView.getAdapter() == null) {
            recyclerView.setAdapter(adapter);
        } else {
            recyclerView.swapAdapter(adapter, true);
        }
        return adapter;
    }
    
    private SharedPreferences getMyPreferences() {
        
        return this.getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE);
    }
    
    @Override
    public void onClick(View v) {
        
        mNewPhoto = true;
        myButton.setEnabled(false);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        myCurrentPictureFile = generateFileForPicture();
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(myCurrentPictureFile));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, GET_PICTURE_REQUEST_CODE);
        }
    }
    
    private void setFilesInFolderText(int filesInFolder) {
        
        myFilesInFolderText.setText(getString(R.string.files_in_folder, filesInFolder));
    }
    
    private void onMyRecyclerViewItemClicked(int position, View v) {
        
        if (!myMainPictureLoaded) {
            File clickedFile = myFilesItemList.get(position).getPictureFile();
            if (myCurrentPictureFile == null || !myCurrentPictureFile.equals(clickedFile)) {
                myCurrentPictureFile = clickedFile;
                loadMainBitmap(myCurrentPictureFile.getPath());
            }
        }
    }
    
    private boolean onMyRecyclerViewItemLongClicked(int position, View v) {
        
        PopupMenu popup = new PopupMenu(this, v);
        popup.inflate(R.menu.item_context_menu);
        popup.setOnMenuItemClickListener(this);
        myContextMenuPosition = position;
        popup.show();
        
        return false;
    }
    
    private void createFilesItemList(File[] filesList) {
        
        myFilesItemList = new ArrayList<>();
        for (int i = 0; i < filesList.length; i++) {
            myFilesItemList.add(new ListItemModel(filesList[i]));
            loadPreview(i);
        }
    }
    
    private int getFilePositionInList(File fileToSearch, ArrayList<ListItemModel> filesItemList) {
        
        for (int i = 0, y = filesItemList.size(); i < y; i++) {
            if (filesItemList.get(i).getPictureFile().equals(fileToSearch)) {
                return i;
            }
        }
        return -1;
    }
    
    private void loadMainBitmap() {
        
        if (!myCurrentPictureFile.exists() && !myFilesItemList.isEmpty()) {
            myCurrentPictureFile = myFilesItemList.get(myFilesItemList.size() - 1).getPictureFile();
            loadMainBitmap(myCurrentPictureFile.getPath());
        } else if (myFilesItemList.isEmpty()) {
            loadMainBitmap(NO_EXISTING_FILE_NAME);
        }
    }
    
    private void loadMainBitmap(SharedPreferences preferences) {
        
        myCurrentPictureFile = new File(preferences.getString(PREF_FOR_LAST_FILE_NAME, NO_EXISTING_FILE_NAME));
        Bundle bundle = new Bundle();
        bundle.putString(FILE_NAME_TO_LOAD, myCurrentPictureFile.getPath());
        bundle.putInt(REQUESTED_PICTURE_HEIGHT, FIRST_LOAD_PICTURE_HEIGHT);
        bundle.putInt(REQUESTED_PICTURE_WIDTH, FIRST_LOAD_PICTURE_WIDTH);
        myMainPictureLoaded = true;
        getSupportLoaderManager().initLoader(MAIN_PICTURE_LOADER_ID, bundle, this);
    }
    
    private void loadMainBitmap(String fileName) {
        
        if (myMainImageViewHeight == 0 || myMainImageViewWidth == 0) {
            myMainImageViewHeight = myImageView.getHeight();
            myMainImageViewWidth = myImageView.getWidth();
        }
        loadMainBitmap(fileName, myMainImageViewHeight, myMainImageViewWidth);
    }
    
    private void loadMainBitmap(String fileName, int requestedHeight, int requestedWidth) {
        
        myMainPictureLoaded = true;
        mainProgressBar.setVisibility(View.VISIBLE);
        Bundle bundle = new Bundle();
        bundle.putString(FILE_NAME_TO_LOAD, fileName);
        bundle.putInt(REQUESTED_PICTURE_HEIGHT, requestedHeight);
        bundle.putInt(REQUESTED_PICTURE_WIDTH, requestedWidth);
        getSupportLoaderManager().restartLoader(MAIN_PICTURE_LOADER_ID, bundle, this);
    }
    
    private void loadPreview(int index) {
        
        if (myPreviewInLoad == null) {
            myPreviewInLoad = new IntQueue();
        }
        myPreviewInLoad.add(index);
        if (myPreviewInLoad.size() == 1) {
            loadPreview();
        }
    }
    
    private void loadPreview() {
        
        if (!myPreviewInLoad.isEmpty()) {
            loadPreview(myFilesItemList.get(myPreviewInLoad.peek()).getPictureFile().getPath());
        }
    }
    
    private void loadPreview(String fileName) {
        
        Bundle bundle = new Bundle();
        bundle.putString(FILE_NAME_TO_LOAD, fileName);
        bundle.putInt(REQUESTED_PICTURE_HEIGHT, PREVIEW_PICTURE_HEIGHT);
        bundle.putInt(REQUESTED_PICTURE_WIDTH, PREVIEW_PICTURE_WIDTH);
        getSupportLoaderManager().restartLoader(PREVIEW_PICTURE_LOADER_START_ID, bundle, this);
    }
    
    @Override
    public Loader<Bitmap> onCreateLoader(int id, Bundle args) {
        
        return new BitmapLoader(this, args);
    }
    
    @Override
    public void onLoadFinished(Loader<Bitmap> loader, Bitmap data) {
        
        if (loader.getId() == MAIN_PICTURE_LOADER_ID) {
            if (myMainBitmap != null) {
                myMainBitmap.recycle();
            }
            myMainBitmap = data;
            myImageView.setImageBitmap(myMainBitmap);
            mainProgressBar.setVisibility(View.INVISIBLE);
            myMainPictureLoaded = false;
        } else {
            setPreview(data, myPreviewInLoad.poll());
            loadPreview();
        }
    }
    
    @Override
    public void onLoaderReset(Loader<Bitmap> loader) {
        
    }
    
    private void setPreview(Bitmap data, int position) {
        
        myFilesItemList.get(position).setPicturePreview(data);
        myPictureInFolderAdapter.notifyItemChanged(position);
    }
    
    private File getDirectory(boolean needPrivate) {
        
        if (needPrivate) {
            return getExternalFilesDir(PICTURE_FOLDER_NAME);
        } else {
            return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), PICTURE_FOLDER_NAME);
        }
    }
    
    private File getDirectory() {
        
        if (myPictureDirectory == null) {
            myPictureDirectory = getDirectory(NEED_PRIVATE_FOLDER);
        }
        return myPictureDirectory;
    }
    
    private File generateFileForPicture() {
        
        String fileName = DEFAULT_FILE_NAME_PREFIX + new SimpleDateFormat(TIME_STAMP_PATTERN, Locale.getDefault()).format(new Date()) + FILE_NAME_SUFFIX;
        return generateFileForPicture(fileName);
    }
    
    private File generateFileForPicture(String fileName) {
        
        return new File(getDirectory().getPath() + "/" + fileName);
    }
    
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        
        File clickedItemFile = myFilesItemList.get(myContextMenuPosition).getPictureFile();
        switch (item.getItemId()) {
            case R.id.menu_delete:
                if (clickedItemFile.delete()) {
                    loadPreview(myContextMenuPosition);
                    myFilesInFolder--;
                    setFilesInFolderText(myFilesInFolder);
                }
                if (myCurrentPictureFile == null || !myCurrentPictureFile.exists()) {
                    loadMainBitmap(NO_EXISTING_FILE_NAME);
                }
                break;
            case R.id.menu_rename:
                if (clickedItemFile.exists()) {
                    FileNameInputDialog fileRenameDialog = FileNameInputDialog.newInstance(clickedItemFile);
                    fileRenameDialog.show(getSupportFragmentManager(), FILE_RENAME_DIALOG_TAG);
                }
                break;
        }
        return false;
    }
    
    @Override
    public void onOkButtonClick(AppCompatDialogFragment dialog, String newFileName, File renamedFile, boolean successfully) {
        
        if (successfully) {
            int position = getFilePositionInList(renamedFile, myFilesItemList);
            File newFile = generateFileForPicture(newFileName);
            if (position < 0 || newFile.exists() || !renamedFile.renameTo(newFile)) {
                RenameErrorDialog.newInstance(getString(R.string.rename_failed)).show(getSupportFragmentManager(), ERROR_DIALOG_TAG);
            } else {
                myFilesItemList.get(position).setPictureFile(newFile);
                myPictureInFolderAdapter.notifyItemChanged(position);
            }
        } else {
            RenameErrorDialog.newInstance(getString(R.string.invalid_file_name)).show(getSupportFragmentManager(), ERROR_DIALOG_TAG);
        }
    }
    
    @Override
    public void onDialogNegativeClick(AppCompatDialogFragment dialog) {
        
    }
}