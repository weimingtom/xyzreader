package com.example.xyzreader.ui;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import static com.example.xyzreader.ui.ArticleListActivity.CURRENT_IMAGE_POSITION;
import static com.example.xyzreader.ui.ArticleListActivity.STARTING_IMAGE_POSITION;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final    String  TAG = "ArticleDetailFragment";

    public  static final    String ARG_ITEM_ID      = "item_id";
    private static final    float  PARALLAX_FACTOR  = 1.25f;


    private Cursor          mCursor;
    private long            mItemId;
    private View            mRootView;


    private DynamicHeightNetworkImageView mImageView;
    private boolean         mIsCard = false;
    private int             mStatusBarFullOpacityBottom;
    private AppBarLayout    mAppBarLayout;
    private int             mStartingPosition;
    private int             mImagePosition;
    private boolean         mIsTransitioning;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {
    }

    public static ArticleDetailFragment newInstance(long itemId, int position, int startingPosition) {
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        arguments.putInt(STARTING_IMAGE_POSITION, startingPosition);
        arguments.putInt(CURRENT_IMAGE_POSITION, position);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();

        if (arguments.containsKey(ARG_ITEM_ID)) {
            mItemId = arguments.getLong(ARG_ITEM_ID);
        }
        if(arguments.containsKey(STARTING_IMAGE_POSITION)){
            mStartingPosition = arguments.getInt(STARTING_IMAGE_POSITION);
        }
        if(arguments.containsKey(CURRENT_IMAGE_POSITION)){
            mImagePosition = arguments.getInt(CURRENT_IMAGE_POSITION);
        }
        mIsTransitioning = savedInstanceState == null && mStartingPosition == mImagePosition;

        mIsCard = getResources().getBoolean(R.bool.detail_is_card);
        mStatusBarFullOpacityBottom = getResources().getDimensionPixelSize(
                R.dimen.detail_card_top_margin);
        setHasOptionsMenu(true);
    }

    public AppCompatActivity getActivityCast() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return (ArticleDetailActivity) getActivity();
        }else{
          return (ArticleDetailActivity_2) getActivity();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // In support library r8, calling initLoader for a fragment in a FragmentPagerAdapter in
        // the fragment's onCreate may cause the same LoaderManager to be dealt to multiple
        // fragments because their mIndex is -1 (haven't been added to the activity yet). Thus,
        // we do this in onActivityCreated.
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.article_detail_fragment, container, false);


        mAppBarLayout = (AppBarLayout)mRootView.findViewById(R.id.appbar);

        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (verticalOffset < -300) {
                    mAppBarLayout.setTargetElevation(20.0f);
                    // mAppBarLayout.setElevation(20.0f);
                }
            }
        });


        //To resolve issue with Toolbar not appearing on page 2 and scrolling does not work
//        Toolbar toolbar = (Toolbar) mRootView.findViewById(R.id.toolbar);
//        toolbar.setPadding(0, getStatusBarHeight(), 0, 0);
//        toolbar.getLayoutParams().height = toolbar.getLayoutParams().height + getStatusBarHeight();
        return mRootView;
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

//    private void updateStatusBar() {
//        int color = 0;
//        if (mPhotoView != null && mTopInset != 0 && mScrollY > 0) {
//            float f = progress(mScrollY,
//                    mStatusBarFullOpacityBottom - mTopInset * 3,
//                    mStatusBarFullOpacityBottom - mTopInset);
//            color = Color.argb((int) (255 * f),
//                    (int) (Color.red(mMutedColor) * 0.9),
//                    (int) (Color.green(mMutedColor) * 0.9),
//                    (int) (Color.blue(mMutedColor) * 0.9));
//        }
//        mStatusBarColorDrawable.setColor(color);
//        mDrawInsetsFrameLayout.setInsetBackground(mStatusBarColorDrawable);
//    }

//    static float progress(float v, float min, float max) {
//        return constrain((v - min) / (max - min), 0, 1);
//    }
//
//    static float constrain(float val, float min, float max) {
//        if (val < min) {
//            return min;
//        } else if (val > max) {
//            return max;
//        } else {
//            return val;
//        }
//    }

//    private void bindViews() {
//        if (mRootView == null) {
//            return;
//        }
//
//        TextView titleView = (TextView) mRootView.findViewById(R.id.article_title);
//        TextView bylineView = (TextView) mRootView.findViewById(R.id.article_byline);
//        bylineView.setMovementMethod(new LinkMovementMethod());
//        TextView bodyView = (TextView) mRootView.findViewById(R.id.article_body);
//        bodyView.setTypeface(Typeface.createFromAsset(getResources().getAssets(), "Rosario-Regular.ttf"));
//
//        if (mCursor != null) {
//            mRootView.setAlpha(0);
//            mRootView.setVisibility(View.VISIBLE);
//            mRootView.animate().alpha(1);
//            titleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));
//            bylineView.setText(Html.fromHtml(
//                    DateUtils.getRelativeTimeSpanString(
//                            mCursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
//                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
//                            DateUtils.FORMAT_ABBREV_ALL).toString()
//                            + " by <font color='#ffffff'>"
//                            + mCursor.getString(ArticleLoader.Query.AUTHOR)
//                            + "</font>"));
//            bodyView.setText(Html.fromHtml(mCursor.getString(ArticleLoader.Query.BODY)));
//            ImageLoaderHelper.getInstance(getActivity()).getImageLoader()
//                    .get(mCursor.getString(ArticleLoader.Query.PHOTO_URL), new ImageLoader.ImageListener() {
//                        @Override
//                        public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
//                            Bitmap bitmap = imageContainer.getBitmap();
//                            if (bitmap != null) {
//                                Palette p = Palette.generate(bitmap, 12);
//                                mMutedColor = p.getDarkMutedColor(0xFF333333);
//                                mPhotoView.setImageBitmap(imageContainer.getBitmap());
//                                mRootView.findViewById(R.id.meta_bar)
//                                        .setBackgroundColor(mMutedColor);
//                                updateStatusBar();
//                            }
//                        }
//
//                        @Override
//                        public void onErrorResponse(VolleyError volleyError) {
//
//                        }
//                    });
//        } else {
//            mRootView.setVisibility(View.GONE);
//            titleView.setText("N/A");
//            bylineView.setText("N/A" );
//            bodyView.setText("N/A");
//        }
//    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(getActivity(), mItemId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (!isAdded()) {
            if (cursor != null) {
                cursor.close();
            }
            return;
        }

        mCursor = cursor;
        if (mCursor != null && !mCursor.moveToFirst()) {
            Log.e(TAG, "Error reading item detail cursor");
            mCursor.close();
            mCursor = null;
        }
        if(mCursor != null) {
            bindViews2();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
        //bindViews();
    }

//    public int getUpButtonFloor() {
//        if (mPhotoContainerView == null || mPhotoView.getHeight() == 0) {
//            return Integer.MAX_VALUE;
//        }
//
//        // account for parallax
//        return mIsCard
//                ? (int) mPhotoContainerView.getTranslationY() + mPhotoView.getHeight() - mScrollY
//                : mPhotoView.getHeight() - mScrollY;
//    }

    private void bindViews2(){

        mImageView =
                (DynamicHeightNetworkImageView) mRootView.findViewById(R.id.image_background);
        AppCompatActivity activity;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity = (ArticleDetailActivity) getActivity();
        }else{
            activity = (ArticleDetailActivity_2) getActivity();
        }
//        Glide.with(activity).load(mCursor.getString(ArticleLoader.Query.PHOTO_URL))
//                    .into(imageView);
        Float fAspectRatio = mCursor.getFloat(ArticleLoader.Query.ASPECT_RATIO);
        mImageView.setAspectRatio(fAspectRatio);
        final XyzAppBarLayout appBarLayout = (XyzAppBarLayout) mRootView.findViewById(R.id.appbar);
        appBarLayout.setAspectRatio(fAspectRatio);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mImageView.setTransitionName("ImageTransition_" + Integer.toString(mImagePosition));
        }
        LoadImage(mImageView, activity);



        Toolbar toolbar = (Toolbar) mRootView.findViewById(R.id.toolbar);
        activity.setSupportActionBar(toolbar);
        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);

        }
        TextView bodyText = (TextView) mRootView.findViewById(R.id.article_body);
        bodyText.setText(Html.fromHtml(mCursor.getString(ArticleLoader.Query.BODY)));

        final CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) mRootView.findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitleEnabled(false);

        final TextView titleText = (TextView) mRootView.findViewById(R.id.id_text_title);
        titleText.setText(mCursor.getString(ArticleLoader.Query.TITLE));

        NestedScrollView nestedScrollView = (NestedScrollView)mRootView.findViewById(R.id.ns_view);

        nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {

            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                //Log.d("Raj", Integer.toString(scrollX)+","+Integer.toString(scrollY)+","+Integer.toString(oldScrollX)+","+Integer.toString(oldScrollY));

                if(collapsingToolbar.getHeight() == appBarLayout.getHeight()){
                    collapsingToolbar.setTitleEnabled(true);
                    collapsingToolbar.setTitle(titleText.getText());
                }else{
                    collapsingToolbar.setTitleEnabled(false);
                    collapsingToolbar.setTitle("");
                }
            }
        });
        //collapsingToolbar.scrollTo();

    }

    private void LoadImage(final DynamicHeightNetworkImageView imageView, final AppCompatActivity activity){

        Picasso.with(activity.getApplicationContext()).load(mCursor.getString(ArticleLoader.Query.THUMB_URL))
                .placeholder(R.drawable.logo)
                .into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                            @Override
                            public void onGenerated(Palette palette) {
                                Palette.Swatch vibrantSwatch = palette.getDarkMutedSwatch();//palette.getDarkVibrantSwatch();
                                //Palette.Swatch mutedSwatch = palette.getDarkMutedSwatch();
                                if (vibrantSwatch != null) {
                                    //int titleColor = Color.WHITE;
                                    int rgb = vibrantSwatch.getRgb();

                                    CollapsingToolbarLayout collapsingToolbar =
                                            (CollapsingToolbarLayout) mRootView.findViewById(R.id.collapsing_toolbar);
//                                    collapsingToolbar.setExpandedTitleColor(rgb);

                                    ActionBar actionBar = activity.getSupportActionBar();
                                    if (actionBar != null) {
                                        ShapeDrawable drawable = new ShapeDrawable(new RectShape());
                                        int colors[] = {rgb, Color.parseColor("#00000000"), rgb};
                                        LinearGradient gradient = new LinearGradient(0, 0, 0, actionBar.getHeight(),
                                                colors, null, Shader.TileMode.REPEAT);
                                        drawable.getPaint().setShader(gradient);
                                        //actionBar.setBackgroundDrawable(drawable);
                                        collapsingToolbar.setContentScrim(drawable);
                                    }
                                }


                            }
                        });
                        mAppBarLayout.setMinimumHeight(bitmap.getHeight());
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && imageView != null) {
                            ((ArticleDetailActivity)getActivityCast()).startSharedElementTransition(imageView);
                        }
                    }

                    @Override
                    public void onError() {
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && imageView != null) {
                            ((ArticleDetailActivity)getActivityCast()).startSharedElementTransition(imageView);
                        }
                    }
                });

    }


    public ImageView getArticleImage(){
        if (isViewInBounds(getActivity().getWindow().getDecorView(), mImageView)) {
            return mImageView;
        }
        return null;
    }

    private static boolean isViewInBounds(@NonNull View container, @NonNull View view) {
        Rect containerBounds = new Rect();
        container.getHitRect(containerBounds);
        return view.getLocalVisibleRect(containerBounds);
    }




}
