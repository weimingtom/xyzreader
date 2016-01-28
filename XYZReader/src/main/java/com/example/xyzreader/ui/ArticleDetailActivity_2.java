package com.example.xyzreader.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.app.SharedElementCallback;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;

import java.util.List;
import java.util.Map;

import static com.example.xyzreader.ui.ArticleListActivity.CURRENT_IMAGE_POSITION;
import static com.example.xyzreader.ui.ArticleListActivity.STARTING_IMAGE_POSITION;

/**
 * An activity representing a single Article detail screen, letting you swipe between articles.
 */
public class ArticleDetailActivity_2 extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>{

    private Cursor                  mCursor;
    private long                    mStartId;


    private ViewPager               mPager;
    private MyPagerAdapter          mPagerAdapter;
    private static final String     CURRENT_PAGE_POSITION = "current_page_position";

    private ArticleDetailFragment   mCurrentDetailsFragment;
    private int                     mCurrentPosition;
    private int                     mStartingPosition;
    private boolean                 mIsReturning;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            getWindow().getDecorView().setSystemUiVisibility(
//                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
//                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
//        }
        setContentView(R.layout.activity_article_detail);

        getLoaderManager().initLoader(0, null, this);

        mPagerAdapter = new MyPagerAdapter(getFragmentManager());
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setPageTransformer(false,new ZoomOutPageTransformer());
        mPager.setAdapter(mPagerAdapter);
        mPager.setPageMargin((int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
        mPager.setPageMarginDrawable(new ColorDrawable(0x22000000));
        mStartingPosition = getIntent().getIntExtra(STARTING_IMAGE_POSITION, 0);
        if(savedInstanceState == null){
            mCurrentPosition = mStartingPosition;
        }else{
            mCurrentPosition = savedInstanceState.getInt(STARTING_IMAGE_POSITION);
        }
        mPager.setCurrentItem(mCurrentPosition);
        mPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);

            }

            @Override
            public void onPageSelected(int position) {
                if (mCursor != null) {
                    mCursor.moveToPosition(position);
                }
                //mSelectedItemId = mCursor.getLong(ArticleLoader.Query._ID);
                mCurrentPosition = position;
            }
        });



        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().getData() != null) {
                mStartId = ItemsContract.Items.getItemId(getIntent().getData());

            }
        }
        Intent intent = getIntent();
        if (intent != null) {
            mStartingPosition = getIntent().getIntExtra(STARTING_IMAGE_POSITION,0);
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            final SharedElementCallback mCallback = new SharedElementCallback() {
                @Override
                public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                    if (mIsReturning) {
                        ImageView sharedElement = mCurrentDetailsFragment.getArticleImage();
                        if (sharedElement == null) {
                            // If shared element is null, then it has been scrolled off screen and
                            // no longer visible. In this case we cancel the shared element transition by
                            // removing the shared element from the shared elements map.
                            names.clear();
                            sharedElements.clear();
                        } else if (mStartingPosition != mCurrentPosition) {
                            // If the user has swiped to a different ViewPager page, then we need to
                            // remove the old shared element and replace it with the new shared element
                            // that should be transitioned instead.
                            names.clear();
                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                names.add(sharedElement.getTransitionName());
                                sharedElements.clear();
                                sharedElements.put(sharedElement.getTransitionName(), sharedElement);
                            }
                        }
                    }
                }

            };
            postponeEnterTransition();
            setEnterSharedElementCallback(mCallback);
        }



    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(CURRENT_PAGE_POSITION, mCurrentPosition);
    }

    @Override
    public void finishAfterTransition() {
        mIsReturning = true;
        Intent data = new Intent();
        data.putExtra(STARTING_IMAGE_POSITION, mStartingPosition);
        data.putExtra(CURRENT_IMAGE_POSITION, mCurrentPosition);
        setResult(RESULT_OK, data);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            super.finishAfterTransition();
        }else{
            finish();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mCursor = cursor;
        mPagerAdapter.notifyDataSetChanged();

        // Select the start ID
        if (mStartId > 0) {
            mCursor.moveToFirst();
            // TODO: optimize
            while (!mCursor.isAfterLast()) {
                if (mCursor.getLong(ArticleLoader.Query._ID) == mStartId) {
                    final int position = mCursor.getPosition();
                    mPager.setCurrentItem(position, false);
                    break;
                }
                mCursor.moveToNext();
            }
            mStartId = 0;

        }

//        ImageView imageView = (ImageView) findViewById(R.id.image_background);
//
//        Glide.with(this).load(mCursor.getString(ArticleLoader.Query.PHOTO_URL))
//                    .into(imageView);
//
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        this.setSupportActionBar(toolbar);
//        ActionBar actionBar = this.getSupportActionBar();
//        if(actionBar != null){
//            actionBar.setDisplayHomeAsUpEnabled(true);
//            //actionBar.setHomeButtonEnabled(true);
//        }
//
//        TextView bodyText = (TextView) findViewById(R.id.article_body);
//        bodyText.setText(Html.fromHtml(mCursor.getString(ArticleLoader.Query.BODY)));

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        switch (id){
            case android.R.id.home :
                finishAfterTransition();
                return  true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
        mPagerAdapter.notifyDataSetChanged();
    }




    private class MyPagerAdapter extends FragmentStatePagerAdapter {
        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            ArticleDetailFragment fragment = (ArticleDetailFragment) object;
            if (fragment != null) {
                mCurrentDetailsFragment = fragment;
            }
        }

        @Override
        public Fragment getItem(int position) {
            mCursor.moveToPosition(position);
            return ArticleDetailFragment.newInstance(mCursor.getLong(ArticleLoader.Query._ID),
                        position, mStartingPosition);
        }

        @Override
        public int getCount() {
            return (mCursor != null) ? mCursor.getCount() : 0;
        }
    }

//    public void startSharedElementTransition(final View sharedElement) {
//
//        sharedElement.getViewTreeObserver().addOnPreDrawListener(
//                new ViewTreeObserver.OnPreDrawListener() {
//
//                    @Override
//                    public boolean onPreDraw() {
//
//                        sharedElement.getViewTreeObserver().removeOnPreDrawListener(this);
//                        startPostponedEnterTransition();
//                        return true;
//
//                    }
//                }
//
//
//        );
//
//    }


}
