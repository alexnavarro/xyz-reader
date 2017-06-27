package com.example.xyzreader.ui;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ShareCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.app.ActionBar;
import android.text.Html;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.transition.ChangeBounds;
import android.transition.Explode;
import android.transition.Transition;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.util.Util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import static android.view.View.VISIBLE;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "ArticleDetailFragment";

    public static final String ARG_ITEM_ID = "item_id";
    private static final float PARALLAX_FACTOR = 1.25f;

    private Cursor mCursor;
    private long mItemId;
    private View mRootView;
    private ObservableScrollView mScrollView;

    private int mTopInset;
    private View mPhotoContainerView;
    private ImageView mPhotoView;
    private int mScrollY;
    private boolean mIsCard = false;

    private View mActionBarBackgroundView;
    private View mStatusBarBackgroundView;
    private View mActionBarScrimLayout;
    TextView titleView;
    private boolean isActionBarTransparent  = true;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
    // Use default locale format
    private SimpleDateFormat outputFormat = new SimpleDateFormat();
    // Most time functions can only handle 1902 - 2037
    private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2,1,1);

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {
    }

    public static ArticleDetailFragment newInstance(long itemId) {
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItemId = getArguments().getLong(ARG_ITEM_ID);
        }

        mIsCard = getResources().getBoolean(R.bool.detail_is_card);
        setHasOptionsMenu(true);
    }

    public ArticleDetailActivity getActivityCast() {
        return (ArticleDetailActivity) getActivity();
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
        mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);
        mScrollView = (ObservableScrollView) mRootView.findViewById(R.id.scrollview);

        mPhotoView = (ImageView) mRootView.findViewById(R.id.photo);
        mPhotoContainerView = mRootView.findViewById(R.id.photo_container);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mPhotoContainerView.setTransitionName("main_photo" + mItemId);
        }

        mRootView.findViewById(R.id.share_fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setText("Some sample text")
                        .getIntent(), getString(R.string.action_share)));
            }
        });

        bindViews();
        return mRootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        startFragmentTransitions(view);
        mActionBarBackgroundView = mRootView.findViewById(R.id.prop_details_actionbar_background);
        mStatusBarBackgroundView = mRootView.findViewById(R.id.statusbar_background);
        mActionBarScrimLayout = mRootView.findViewById(R.id.actionbar_scrim_layout);
        setupActionBarScrim();

        final ValueAnimator animatorFadeIn = new ValueAnimator();
        animatorFadeIn.setFloatValues(0f, 1f);
        animatorFadeIn.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (mActionBarScrimLayout != null) {
                    mActionBarScrimLayout.setAlpha(((Float) animation.getAnimatedValue()));
                }
            }
        });

        animatorFadeIn.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                ActionBar actionBar = getActivityCast().getSupportActionBar();
                if(actionBar != null) {
                        actionBar.setTitle(titleView.getText());
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });

        final ValueAnimator animatorFadeOut = new ValueAnimator();
        animatorFadeOut.setFloatValues(1f, 0f);
        animatorFadeOut.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (mActionBarScrimLayout != null) {
                    mActionBarScrimLayout.setAlpha(((Float) animation.getAnimatedValue()));
                }
            }
        });
        animatorFadeOut.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                ActionBar actionBar = (getActivityCast()).getSupportActionBar();
                if (actionBar != null) {
                    actionBar.setTitle("");
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });

        mScrollView.setCallbacks(new ObservableScrollView.Callbacks() {
            @Override
            public void onScrollChanged() {
                mScrollY = mScrollView.getScrollY();
                mPhotoContainerView.setY(mScrollY / PARALLAX_FACTOR);
                if(mScrollY > mPhotoContainerView.getHeight()  && isActionBarTransparent){
                    animatorFadeIn.start();
                }

                if (mScrollY > mPhotoContainerView.getHeight() && isActionBarTransparent) {
                    isActionBarTransparent = false;
                    animatorFadeIn.start();
                } else if (mScrollY < (mPhotoContainerView.getHeight() - mActionBarScrimLayout.getHeight()) && !isActionBarTransparent) {
                    isActionBarTransparent = true;
                    animatorFadeOut.start();
                }

            }
        });
    }


    private void startFragmentTransitions(final View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final View shadowTop = view.findViewById(R.id.top_scrim_protector);
            getActivity().getWindow().setSharedElementEnterTransition(new ChangeBounds().setInterpolator(new FastOutSlowInInterpolator()).setDuration(750));
            getActivity().getWindow().setReturnTransition(new Explode().excludeTarget(android.R.id.navigationBarBackground, true));
            getActivity().getWindow().setExitTransition(new Explode().excludeTarget(android.R.id.navigationBarBackground, true));
            getActivity().getWindow().setEnterTransition(new Explode().excludeTarget(android.R.id.navigationBarBackground, true).setInterpolator(new FastOutSlowInInterpolator()).setDuration(750).addListener(new Transition.TransitionListener() {
                @Override
                public void onTransitionStart(Transition transition) {
                    shadowTop.setVisibility(View.GONE);
                }

                @Override
                public void onTransitionEnd(Transition transition) {
                    if (getActivity() != null) {
                        shadowTop.setVisibility(VISIBLE);
                        Animation animation = AnimationUtils.loadAnimation(view.getContext(), android.R.anim.fade_in);
                        shadowTop.startAnimation(animation);
                        final View statusBarBackground = getActivity().getWindow().getDecorView().findViewById(android.R.id.statusBarBackground);
                        if (statusBarBackground != null) {
                            statusBarBackground.startAnimation(animation);
                        }
                    }

                }

                @Override
                public void onTransitionCancel(Transition transition) {

                }

                @Override
                public void onTransitionPause(Transition transition) {

                }

                @Override
                public void onTransitionResume(Transition transition) {

                }
            }));
        }
    }

    static float constrain(float val, float min, float max) {
        if (val < min) {
            return min;
        } else if (val > max) {
            return max;
        } else {
            return val;
        }
    }

    private Date parsePublishedDate() {
        try {
            String date = mCursor.getString(ArticleLoader.Query.PUBLISHED_DATE);
            return dateFormat.parse(date);
        } catch (ParseException ex) {
            Log.e(TAG, ex.getMessage());
            Log.i(TAG, "passing today's date");
            return new Date();
        }
    }

    private void bindViews() {
        if (mRootView == null) {
            return;
        }

        titleView = (TextView) mRootView.findViewById(R.id.article_title);
        TextView bylineView = (TextView) mRootView.findViewById(R.id.article_byline);
        bylineView.setMovementMethod(new LinkMovementMethod());
        final TextView bodyView = (TextView) mRootView.findViewById(R.id.article_body);


        bodyView.setTypeface(Typeface.createFromAsset(getResources().getAssets(), "Rosario-Regular.ttf"));

        if (mCursor != null) {
            mRootView.setAlpha(0);
            mRootView.setVisibility(View.VISIBLE);
            mRootView.animate().alpha(1);
            titleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));
            Date publishedDate = parsePublishedDate();
            if (!publishedDate.before(START_OF_EPOCH.getTime())) {
                bylineView.setText(Html.fromHtml(
                        DateUtils.getRelativeTimeSpanString(
                                publishedDate.getTime(),
                                System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                                DateUtils.FORMAT_ABBREV_ALL).toString()
                                + " by <font color='#ffffff'>"
                                + mCursor.getString(ArticleLoader.Query.AUTHOR)
                                + "</font>"));

            } else {
                // If date is before 1902, just show the string
                bylineView.setText(Html.fromHtml(
                        outputFormat.format(publishedDate) + " by <font color='#ffffff'>"
                                + mCursor.getString(ArticleLoader.Query.AUTHOR)
                                + "</font>"));

            }

            bodyView.setText(Html.fromHtml(mCursor.getString(ArticleLoader.Query.BODY).replaceAll("(\r\n|\n)", "<br />")));

            ImageLoaderHelper.getInstance(getActivity()).getImageLoader()
                    .get(mCursor.getString(ArticleLoader.Query.PHOTO_URL), new ImageLoader.ImageListener() {
                        @Override
                        public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                            Bitmap bitmap = imageContainer.getBitmap();
                            if (bitmap != null) {
                                mPhotoView.setImageBitmap(imageContainer.getBitmap());
                            }
                        }

                        @Override
                        public void onErrorResponse(VolleyError volleyError) {

                        }
                    });
        } else {
            mRootView.setVisibility(View.GONE);
            titleView.setText("N/A");
            bylineView.setText("N/A" );
            bodyView.setText("N/A");
        }
    }

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

        bindViews();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
        bindViews();
    }

    public int getUpButtonFloor() {
        if (mPhotoContainerView == null || mPhotoView.getHeight() == 0) {
            return Integer.MAX_VALUE;
        }

        // account for parallax
        return mIsCard
                ? (int) mPhotoContainerView.getTranslationY() + mPhotoView.getHeight() - mScrollY
                : mPhotoView.getHeight() - mScrollY;
    }

    private void setupActionBarScrim() {
        if (mActionBarBackgroundView != null && mStatusBarBackgroundView != null) {
            ViewGroup.LayoutParams actionBarBackgroundViewLayoutParams = mActionBarBackgroundView.getLayoutParams();
            actionBarBackgroundViewLayoutParams.height = getResources().getDimensionPixelSize(R.dimen.actionbar_height);
            mActionBarBackgroundView.setLayoutParams(actionBarBackgroundViewLayoutParams);

            ViewGroup.LayoutParams statusBarBackgroundViewLayoutParams = mStatusBarBackgroundView.getLayoutParams();
            statusBarBackgroundViewLayoutParams.height = Util.getStatusBarHeight(getActivity());
            mStatusBarBackgroundView.setLayoutParams(statusBarBackgroundViewLayoutParams);
        }
    }
}