package vn.iotstar.bt10_ltmobile.adapter;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import vn.iotstar.bt10_ltmobile.model.VideoModel;
import vn.iotstar.bt10_ltmobile.R;

public class VideosAdapter extends RecyclerView.Adapter<VideosAdapter.MyViewHolder>{
    private Context context;
    List<VideoModel> videoModels;
    private boolean isFav = false;
    public VideosAdapter(Context context, List<VideoModel> videoModels) {
        this.context = context;
        this.videoModels = videoModels;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_video_row, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        VideoModel videoModel = videoModels.get(position);
        holder.textVideoDesc.setText(videoModel.getDesc());
        holder.textVideoTitle.setText(videoModel.getTitle());
        holder.videoView.setVideoURI(Uri.parse(videoModel.getUrl()));
        holder.videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                holder.progressBar.setVisibility(View.GONE);
                mp.start();
                float videoRatio = mp.getVideoWidth () / (float) mp.getVideoHeight();
                float screenRatio = holder. videoView. getWidth() / (float) holder. videoView. getHeight();
                float scale = videoRatio /screenRatio;
                if(scale >= 1f){
                    holder. videoView. setScaleX(scale);
                }else {
                    holder.videoView.setScaleY(1f / scale);
                }
            }
        });
        holder.videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                holder.videoView.setMediaController(new MediaController(context));
                holder.videoView.requestFocus();
                mp.start();
            }
        });
        holder.favorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isFav){
                    holder.favorites.setImageResource(R.drawable.ic_fill_favorite);
                    isFav = true;
                }
                else{
                    isFav = false;
                    holder.favorites.setImageResource(R.drawable.ic_favorite);
                }
            }
        });
    }
    public class MyViewHolder extends RecyclerView.ViewHolder {
        private VideoView videoView;
        private ProgressBar progressBar;
        private TextView textVideoTitle, textVideoDesc;
        private ImageView imPerson, favorites, imShare, imMore;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            videoView = itemView. findViewById (R. id. videoView) ;
            progressBar = itemView. findViewById(R.id.videoProgressBar);
            textVideoTitle = itemView. findViewById(R.id. textVideoTitle);
            textVideoDesc = itemView. findViewById(R.id. textVideoDescription);
            imPerson = itemView. findViewById(R.id.imPerson);
            favorites = itemView. findViewById(R.id.favorites);
            imShare = itemView. findViewById(R.id. imShare);
            imMore = itemView. findViewById(R.id.imMore);
        }
    }
    @Override
    public int getItemCount() {
        return videoModels==null?0: videoModels.size();
    }
}
