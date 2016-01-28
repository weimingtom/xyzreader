package com.example.xyzreader.data;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.xyzreader.R;
import com.example.xyzreader.ui.ArticleDetailActivity;
import com.example.xyzreader.ui.ArticleDetailActivity_2;
import com.example.xyzreader.ui.ArticleListActivity;
import com.example.xyzreader.ui.DynamicHeightNetworkImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

/**
 * Created by rajsettipalli on 7/01/2016.
 */


public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ViewHolder> {
    private Cursor      mCursor;
    private Context     mContext;
    final private AppCompatActivity mActivity;
    Typeface            rosario;
    private int         mLastPosition = -1;
    private int         mPosition;

    public ArticleAdapter(Cursor cursor, Context context) {
        mCursor = cursor;
        mContext = context;
        mActivity = (AppCompatActivity)mContext;

    }

    @Override
    public long getItemId(int position) {
        mCursor.moveToPosition(position);
        return mCursor.getLong(ArticleLoader.Query._ID);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_article, parent, false);
        //getLayoutInflater().inflate(R.layout.list_item_article, parent, false);
        final ViewHolder vh = new ViewHolder(view);
        final Context context = parent.getContext();
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Intent.ACTION_VIEW,
                        ItemsContract.Items.buildItemUri(getItemId(vh.getAdapterPosition())),context,
                        ArticleDetailActivity.class);
                intent.putExtra(ArticleListActivity.STARTING_IMAGE_POSITION,mPosition);
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    //Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(mActivity).toBundle();
                    Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(mActivity,
                                vh.thumbnailView,vh.thumbnailView.getTransitionName()).toBundle();

                    context.startActivity(intent, bundle);
                }else{
                    intent = new Intent(context, ArticleDetailActivity_2.class);
                    intent.putExtra(ArticleListActivity.STARTING_IMAGE_POSITION,mPosition);
                    context.startActivity(intent);
                }
            }
        });
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        mPosition = position;
        mCursor.moveToPosition(position);
        holder.titleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));
        holder.subtitleView.setText(
                DateUtils.getRelativeTimeSpanString(
                        mCursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                        System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                        DateUtils.FORMAT_ABBREV_ALL).toString());

        holder.authorView.setText(String.format(mContext.getString(R.string.author_name),
                mCursor.getString(ArticleLoader.Query.AUTHOR)));

        holder.thumbnailView.setAspectRatio(mCursor.getFloat(ArticleLoader.Query.ASPECT_RATIO));
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            holder.thumbnailView.setTransitionName("ImageTransition_" + Integer.toString(position));
            holder.thumbnailView.setTag("ImageTransition_" + Integer.toString(position));
        }


        Picasso.with(mContext).load(mCursor.getString(ArticleLoader.Query.THUMB_URL))
                    //.centerCrop()
                    .placeholder(R.drawable.logo)
                .into(holder.thumbnailView, new Callback() {
                    @Override
                    public void onSuccess() {
                        Bitmap bitmap = ((BitmapDrawable) holder.thumbnailView.getDrawable()).getBitmap();
                        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                            @Override
                            public void onGenerated(Palette palette) {
                                Palette.Swatch vibrantSwatch = palette.getDarkVibrantSwatch();
                                //Palette.Swatch swatch = palette.getLightMutedSwatch();
                                if (vibrantSwatch != null) {
                                    int titleColor = Color.WHITE;
                                    int rgb = vibrantSwatch.getRgb();
                                    holder.titleView.setTextColor(rgb);
                                    holder.authorView.setTextColor(titleColor);
                                    holder.subtitleView.setTextColor(titleColor);
                                    //holder.layoutView.setBackgroundColor(vibrantSwatch.getRgb());
                                    ShapeDrawable drawable = new ShapeDrawable(new RectShape());
                                    int colors[] = {Color.parseColor("#00000000"), rgb};
                                    LinearGradient gradient = new LinearGradient(0, 0, 0, holder.layoutView.getHeight(),
                                            colors, null, Shader.TileMode.REPEAT);
                                    drawable.getPaint().setShader(gradient);
                                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                        holder.layoutView.setBackground(drawable);
                                    }

                                }

                            }
                        });
                    }

                    @Override
                    public void onError() {

                    }
                });


        if (position > mLastPosition) {
            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.item_animator);
            holder.cardView.startAnimation(animation);
            mLastPosition = position;
        }


    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        public DynamicHeightNetworkImageView thumbnailView;
        //public ImageView thumbnailView;
        public TextView     titleView;
        public TextView     subtitleView;
        public TextView     authorView;
        public LinearLayout layoutView;
        //public View         titleScrim;
        public CardView     cardView;

        public ViewHolder(View view) {
            super(view);
            thumbnailView   = (DynamicHeightNetworkImageView) view.findViewById(R.id.thumbnail);
            titleView       = (TextView) view.findViewById(R.id.article_title);
            subtitleView    = (TextView) view.findViewById(R.id.article_subtitle);
            authorView      = (TextView) view.findViewById(R.id.article_author);
            layoutView      = (LinearLayout) view.findViewById(R.id.text_objects);
           // titleScrim      = view.findViewById(R.id.title_scrim);
            cardView        = (CardView) view.findViewById(R.id.id_card_view);

        }
    }

}