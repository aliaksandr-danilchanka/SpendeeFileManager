package myproject.spendeefilemanager.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import myproject.spendeefilemanager.R;
import myproject.spendeefilemanager.manager.FileManager;

/**
 * Created by Aliaksandr on 9/8/2017.
 */

public class DefaultFolderAdapter extends RecyclerView.Adapter<DefaultFolderAdapter.ListItemViewHolder> {

    private ArrayList<File> mFilesAndFolders;
    private OnItemClickListener mOnItemClickListener;
    private Context mContext;
    private File mBeforeFile;


    public DefaultFolderAdapter(ArrayList<File> filesAndFolders, Context context, File file, OnItemClickListener onItemClickListener) {
        this.mFilesAndFolders = filesAndFolders;
        this.mOnItemClickListener = onItemClickListener;
        if (!file.getAbsolutePath().equals(FileManager.getInstance().getStartUrl(context))) {
            File beforeFile = new File(file.getAbsolutePath()
                    .substring(0, file.getAbsolutePath().length() - file.getName().length() - 1));
            this.mBeforeFile = beforeFile;
            this.mFilesAndFolders.add(0, mBeforeFile);
        }
        this.mContext = context;
    }

    @Override
    public ListItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_files, parent, false);

        return new ListItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ListItemViewHolder holder, final int position) {

        final File singleItem = mFilesAndFolders.get(position);

        if (singleItem == mBeforeFile) {
            holder.mTitle.setText(mContext.getString(R.string.before_package_name));
            holder.mLastModified.setText(singleItem.getAbsolutePath());
            holder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_back));
        } else {
            holder.mTitle.setText(singleItem.getName());
            holder.mLastModified.setText(new Date(singleItem.lastModified()).toString());
            setIcon(singleItem, holder);
        }

        holder.mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListener.onItemClick(v, position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mFilesAndFolders.size();
    }

    public void setIcon(File file, ListItemViewHolder holder) {

        String extension;
        Drawable drawable = null;

        try {

            extension = FileManager.getInstance().getExtension(file.getAbsolutePath());

            if (file.isFile()) {

                switch (extension) {

                    case ".c":
                    case ".cpp":
                    case ".doc":
                    case ".docx":
                    case ".exe":
                    case ".h":
                    case ".html":
                    case ".java":
                    case ".log":
                    case ".txt":
                    case ".pdf":
                    case ".ppt":
                    case ".xls":
                        drawable = ContextCompat.getDrawable(mContext, R.drawable.ic_file);
                        break;

                    case ".3ga":
                    case ".aac":
                    case ".mp3":
                    case ".m4a":
                    case ".ogg":
                    case ".wav":
                    case ".wma":
                        drawable = ContextCompat.getDrawable(mContext, R.drawable.ic_audio);
                        break;

                    case ".3gp":
                    case ".avi":
                    case ".mpg":
                    case ".mpeg":
                    case ".mp4":
                    case ".mkv":
                    case ".webm":
                    case ".wmv":
                    case ".vob":
                        drawable = ContextCompat.getDrawable(mContext, R.drawable.ic_video);
                        break;

                    case ".ai":
                    case ".bmp":
                    case ".exif":
                    case ".gif":
                    case ".jpg":
                    case ".jpeg":
                    case ".png":
                    case ".svg":
                        drawable = ContextCompat.getDrawable(mContext, R.drawable.ic_image);
                        break;

                    case ".rar":
                    case ".zip":
                    case ".ZIP":
                        drawable = ContextCompat.getDrawable(mContext, R.drawable.ic_compressed);
                        break;

                    default:
                        drawable = ContextCompat.getDrawable(mContext, R.drawable.ic_error);
                        break;
                }

            } else if (file.isDirectory()) {
                drawable = ContextCompat.getDrawable(mContext, R.drawable.ic_folder);
            } else drawable = ContextCompat.getDrawable(mContext, R.drawable.ic_error);

        } catch (Exception e) {
            drawable = ContextCompat.getDrawable(mContext, R.drawable.ic_error);
        }

        drawable = DrawableCompat.wrap(drawable);
        holder.mIcon.setImageDrawable(drawable);

    }

    public interface OnItemClickListener {
        public void onItemClick(View view, int position);

    }

    static class ListItemViewHolder extends RecyclerView.ViewHolder {

        CardView mCardView;
        TextView mTitle;
        TextView mLastModified;
        ImageView mIcon;
        LinearLayout mLinearLayout;

        public ListItemViewHolder(View itemView) {
            super(itemView);
            mCardView = (CardView) itemView.findViewById(R.id.cardView);
            mTitle = (TextView) itemView.findViewById(R.id.title);
            mIcon = (ImageView) itemView.findViewById(R.id.icon);
            mLinearLayout = (LinearLayout) itemView.findViewById(R.id.linearLayout);
            mLastModified = (TextView) itemView.findViewById(R.id.lastModified);
        }
    }
}
