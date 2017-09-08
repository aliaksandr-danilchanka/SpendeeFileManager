package myproject.spendeefilemanager.fragment;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import myproject.spendeefilemanager.R;
import myproject.spendeefilemanager.activity.MainActivity;
import myproject.spendeefilemanager.adapter.FileManagerAdapter;
import myproject.spendeefilemanager.manager.FileManager;


/**
 * Created by Aliaksandr on 9/6/2017.
 */

public class FileManagerFragment extends Fragment {

    public static final String PATH_KEY = "PATH_KEY";

    private static File mPath;
    private LinearLayout mViewFileIsEmpty;
    private RecyclerView mRecyclerView;
    private ArrayList<File> mFilesAndFolders;
    private Toolbar mToolbar;
    private FileManagerAdapter mAdapter;
    private ActionMode mActionModes;
    private boolean mClickAllowed;


    private ActionMode.Callback actionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater menuInflater = mode.getMenuInflater();
            menuInflater.inflate(R.menu.menu_action_mode, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(final ActionMode mode, final MenuItem item) {

            switch (item.getItemId()) {

                case R.id.delete_button:
                    deleteDialog(getString(R.string.delete_dialog), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            delete(mAdapter.getSelectedItems());
                            mode.finish();
                        }
                    });
                    return true;

                default:
                    return false;
            }
        }


        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionModes = null;
            mAdapter.clearSelection();
            mClickAllowed = true;
        }

    };

    public static FileManagerFragment newInstance(String file) {

        Bundle args = new Bundle();

        args.putString(PATH_KEY, file);
        FileManagerFragment fragment = new FileManagerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPath = new File(getArguments().getString(PATH_KEY));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_start_folder, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mViewFileIsEmpty = (LinearLayout) view.findViewById(R.id.view_file_is_empty);
        mToolbar = (Toolbar) getActivity().findViewById(R.id.toolbar_actionbar);
        mToolbar.setTitle(mPath.getName());

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2);
        gridLayoutManager.setOrientation(GridLayoutManager.VERTICAL);

        open(mPath);

        ((MainActivity) getActivity()).setFragmentRefreshListener(new MainActivity.FragmentRefreshListener() {
            @Override
            public void onRefresh() {
                openDirectory(mPath);
            }
        });

        mClickAllowed = true;
        mRecyclerView.setLayoutManager(gridLayoutManager);
        return view;
    }

    public void open(File file) {

        if (!file.canRead()) {
            Toast.makeText(getContext(), "Do not have read access", Toast.LENGTH_SHORT).show();
            return;
        }

        if (file.isFile()) {

            MimeTypeMap mime = MimeTypeMap.getSingleton();
            Intent i = new Intent();
            i.setAction(Intent.ACTION_VIEW);
            String mimeType = mime.getMimeTypeFromExtension(FileManager.getInstance()
                    .getExtension(file.getAbsolutePath()).substring(1));
            i.setDataAndType(Uri.fromFile(file), mimeType);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            try {
                getContext().startActivity(i);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(getContext(), "No handler for this type of file.", Toast.LENGTH_LONG).show();
            }
        } else if (file.isDirectory()) {
            openDirectory(file);
        }
    }

    public void openDirectory(File file) {
        if (mFilesAndFolders != null) {
            mFilesAndFolders.clear();
        } else {
            mFilesAndFolders = new ArrayList<>();
        }
        ArrayList<File> list = new ArrayList<>(Arrays.asList(file.listFiles()));
        if (list.size() != 0) {
            mFilesAndFolders.addAll(list);
            initializeAdapter();
            showRecyclerView();
        } else {
            showFileIsEmptyView();
        }
    }

    public void delete(ArrayList<File> files) {

        for (File file : files) {

            try {
                deleteFile(file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Toast.makeText(getContext(), "Files successfully deleted", Toast.LENGTH_SHORT).show();
        openDirectory(mPath);
    }

    public boolean deleteFile(File file) {

        if (file.isFile()) {
            file.delete();
        } else if (file.isDirectory() && file.canWrite()) {
            ArrayList<File> subFiles = new ArrayList<>(Arrays.asList(file.listFiles()));

            for (File subFile : subFiles) {

                deleteFile(subFile);
            }
            file.delete();
        }

        return true;
    }


    private void deleteDialog(String message, DialogInterface.OnClickListener onClickListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage(message)
                .setPositiveButton(getString(R.string.ok), onClickListener)
                .setNegativeButton(getString(R.string.cancel), null)
                .create()
                .show();

    }

    private void initializeAdapter() {
        mAdapter = new FileManagerAdapter(mFilesAndFolders, getContext(), new FileManagerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                File singleItem = mFilesAndFolders.get(position);
                if (mClickAllowed) {
                    if (singleItem.isDirectory()) {
                        Fragment fragment = FileManagerFragment.newInstance(singleItem.getAbsolutePath());
                        getActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.container, fragment)
                                .addToBackStack(null)
                                .commit();
                    } else {
                        open(singleItem);
                    }
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {
                mClickAllowed = false;

                if (mActionModes != null) {

                    mAdapter.toggleSelection(position);
                    mActionModes.setTitle(mAdapter.getSelectedItemsCount() + "  " + getString(R.string.info_items_selected));

                    if (mAdapter.getSelectedItemsCount() <= 0)
                        mActionModes.finish();

                    return;
                }

                mActionModes = getActivity().startActionMode(actionModeCallback);
                mAdapter.toggleSelection(position);
                mActionModes.setTitle(mAdapter.getSelectedItemsCount() + "  " + getString(R.string.info_items_selected));
            }
        });
        mRecyclerView.setAdapter(mAdapter);
    }

    private void showRecyclerView() {
        mRecyclerView.setVisibility(View.VISIBLE);
        mViewFileIsEmpty.setVisibility(View.GONE);
    }

    private void showFileIsEmptyView() {
        mRecyclerView.setVisibility(View.GONE);
        mViewFileIsEmpty.setVisibility(View.VISIBLE);
    }

}
