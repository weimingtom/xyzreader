package com.example.xyzreader.ui;

import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.SharedElementCallback;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewTreeObserver;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleAdapter;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.UpdaterService;

import java.util.List;
import java.util.Map;

/**
 * An activity representing a list of Articles. This activity has different presentations for
 * handset and tablet-size devices. On handsets, the activity presents a list of items, which when
 * touched, lead to a {@link ArticleDetailActivity} representing item details. On tablets, the
 * activity presents a grid of items as cards.
 */
public class ArticleListActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private Toolbar             mToolbar;
    private SwipeRefreshLayout  mSwipeRefreshLayout;
    private RecyclerView        mRecyclerView;

    private Bundle              mTmpReenterState;
    private final String        KNOWN_POSITION = "KNOWN_POSITION";
    public static final    String STARTING_IMAGE_POSITION   = "starting_image_position";
    public static final    String CURRENT_IMAGE_POSITION   = "current_image_position";

    private final SharedElementCallback mCallBack = new SharedElementCallback() {
        @Override
        public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {

            if(mTmpReenterState == null){
                // If mTmpReenterState is null, then the activity is exiting.
                View imageView = findViewById(R.id.image_background);

                if (imageView != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    names.add(imageView.getTransitionName());
                    sharedElements.put(imageView.getTransitionName(), imageView);
                }

            }else{

                int startingPosition = mTmpReenterState.getInt(STARTING_IMAGE_POSITION);
                int currentPosition = mTmpReenterState.getInt(CURRENT_IMAGE_POSITION);
                if (startingPosition != currentPosition) {
                    // If startingPosition != currentPosition the user must have swiped to a
                    // different page in the DetailsActivity. We must update the shared element
                    // so that the correct one falls into place.
                    String newTransitionName = "ImageTransition_" + Integer.toString(currentPosition);
                    View newSharedElement = mRecyclerView.findViewWithTag(newTransitionName);

                    if (newSharedElement != null) {
                        names.clear();
                        names.add(newTransitionName);
                        sharedElements.clear();
                        sharedElements.put(newTransitionName, newSharedElement);
                    }
                }
                mTmpReenterState = null;
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_list);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        if(mToolbar!=null){
            setSupportActionBar(mToolbar);

        }
        ActionBar actionBar =   getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);
        }


        //final View toolbarContainerView = findViewById(R.id.toolbar_container);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        getLoaderManager().initLoader(0, null, this);

        if (savedInstanceState == null) {
            refresh();
        }

        //mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        setExitSharedElementCallback(mCallBack);
    }

    private void refresh() {

        startService(new Intent(this, UpdaterService.class));
    }



    @Override
    protected void onResume() {
        super.onResume();
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
        registerReceiver(mRefreshingReceiver,
                new IntentFilter(UpdaterService.BROADCAST_ACTION_STATE_CHANGE));


    }

    @Override
    protected void onPause() {
        mSwipeRefreshLayout.setOnRefreshListener(null);
        unregisterReceiver(mRefreshingReceiver);

        super.onPause();
    }



    private BroadcastReceiver mRefreshingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (UpdaterService.BROADCAST_ACTION_STATE_CHANGE.equals(intent.getAction())) {
                boolean refreshing = intent.getBooleanExtra(UpdaterService.EXTRA_REFRESHING, false);
                mSwipeRefreshLayout.setRefreshing(refreshing);

            }

        }
    };


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);
        mTmpReenterState = new Bundle(data.getExtras());
        int startingPosition = mTmpReenterState.getInt(STARTING_IMAGE_POSITION);
        int currentPosition = mTmpReenterState.getInt(CURRENT_IMAGE_POSITION);
        if (startingPosition != currentPosition) {
            mRecyclerView.scrollToPosition(currentPosition);
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            postponeEnterTransition();
            mRecyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    mRecyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                    mRecyclerView.requestLayout();//?
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        startPostponedEnterTransition();
                    }
                    return true;
                }
            });
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        ArticleAdapter adapter = new ArticleAdapter(cursor, this);

        adapter.setHasStableIds(true);
        mRecyclerView.setAdapter(adapter);
        int columnCount = getResources().getInteger(R.integer.list_column_count);

        StaggeredGridLayoutManager sglm =
                new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(sglm);


    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        mRecyclerView.setAdapter(null);
    }


}
