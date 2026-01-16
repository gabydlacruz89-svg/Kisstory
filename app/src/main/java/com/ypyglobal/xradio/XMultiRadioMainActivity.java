/*
 * Copyright (c) 2017. YPY Global - All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at.
 *
 *         http://ypyglobal.com/sourcecode/policy
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.ypyglobal.xradio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;

import com.behavior.model.FixAppBarLayoutBehavior;
import com.behavior.model.YPYBottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.tabs.TabLayout;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.UserMessagingPlatform;
import com.ypyglobal.xradio.ads.RewardedAdsHelper;
import com.ypyglobal.xradio.constants.IXRadioConstants;
import com.ypyglobal.xradio.databinding.ActivityAppBarMainBinding;
import com.ypyglobal.xradio.fragment.FragmentDetailList;
import com.ypyglobal.xradio.fragment.FragmentDragDrop;
import com.ypyglobal.xradio.fragment.FragmentFavorite;
import com.ypyglobal.xradio.fragment.FragmentGenre;
import com.ypyglobal.xradio.fragment.FragmentTheme;
import com.ypyglobal.xradio.fragment.FragmentTopChart;
import com.ypyglobal.xradio.gdpr.GDPRManager;
import com.ypyglobal.xradio.model.ConfigureModel;
import com.ypyglobal.xradio.model.GenreModel;
import com.ypyglobal.xradio.model.RadioModel;
import com.ypyglobal.xradio.model.UIConfigModel;
import com.ypyglobal.xradio.setting.XRadioSettingManager;
import com.ypyglobal.xradio.stream.constant.IYPYStreamConstants;
import com.ypyglobal.xradio.stream.manager.YPYStreamManager;
import com.ypyglobal.xradio.stream.mediaplayer.YPYMediaPlayer;
import com.ypyglobal.xradio.ypylibs.fragment.YPYFragment;
import com.ypyglobal.xradio.ypylibs.fragment.YPYFragmentAdapter;
import com.ypyglobal.xradio.ypylibs.imageloader.GlideImageLoader;
import com.ypyglobal.xradio.ypylibs.listener.IYPYSearchViewInterface;
import com.ypyglobal.xradio.ypylibs.task.IYPYCallback;
import com.ypyglobal.xradio.ypylibs.utils.ApplicationUtils;
import com.ypyglobal.xradio.ypylibs.utils.IOUtils;
import com.ypyglobal.xradio.ypylibs.utils.ShareActionUtils;

import java.util.ArrayList;




/**
 * @author:YPY Global
 * @Email: bl911vn@gmail.com
 * @Website: http://ypyglobal.com
 * Created by YPY Global on 10/19/17.
 */
public class XMultiRadioMainActivity extends XRadioFragmentActivity<ActivityAppBarMainBinding> implements IYPYStreamConstants, View.OnClickListener {

    private RewardedAdsHelper rewardedHelper;

    private static final String KEY_TOP_INDEX = "view_pager_index";

    private int mStartHeight;

    private ConfigureModel mConfigureModel;
    private UIConfigModel mUIConfigModel;

    private final ArrayList<Fragment> mListHomeFragments = new ArrayList<>();

    private FragmentTopChart mFragmentTopChart;
    private FragmentFavorite mFragmentFavorite;
    private YPYBottomSheetBehavior<RelativeLayout> mBottomSheetBehavior;

    public String mUrlHost;
    public String mApiKey;
    private ApplicationBroadcast mApplicationBroadcast;
    private FragmentDragDrop mFragmentDragDrop;
    private int countInterstitial;

    public boolean isAllCheckNetWorkOff;
    private int mCurrentIndex = 0;
    private boolean isNotSetUp;
    private boolean isHiddenAdsWhenFavEmpty;


    @Override
    public void onDoWhenDone() {
        super.onDoWhenDone();

        rewardedHelper = new RewardedAdsHelper(this);
        rewardedHelper.loadRewardedAd();


        XRadioSettingManager.setOnline(this, true);
        if (mSavedInstance != null) {
            mCurrentIndex = mSavedInstance.getInt(KEY_TOP_INDEX, 0);
        }

        resetTimer();
        setUpActionBar();

        ((CoordinatorLayout.LayoutParams) viewBinding.appBar.getLayoutParams()).setBehavior(new FixAppBarLayoutBehavior());
        setIsAllowPressMoreToExit(true);

        mFragmentDragDrop = (FragmentDragDrop) getSupportFragmentManager().findFragmentById(R.id.fragment_drag_drop);
        findViewById(R.id.img_touch).setOnTouchListener((v, event) -> true);

        viewBinding.layoutTotalDragDrop.btnSmallNext.setOnClickListener(this);
        viewBinding.layoutTotalDragDrop.btnSmallPrev.setOnClickListener(this);
        viewBinding.layoutTotalDragDrop.btnSmallPlay.setOnClickListener(this);

        setUpTab();
        showAppRate();

        setUpColorWidget();
        registerApplicationBroadcastReceiver();

        //TODO WHEN SAVED INSTANCE !=NULL
        if (mListFragments != null && mListFragments.size() > 0) {
            showHideLayoutContainer(true);
            YPYFragment<?> mFragment = (YPYFragment<?>) mListFragments.get(mListFragments.size() - 1);
            if (!TextUtils.isEmpty(mFragment.getScreenName())) {
                setActionBarTitle(mFragment.getScreenName());
            }
        }

    }

    //This one to fix stupid problem about pivot height
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        try {
            if (!isNotSetUp) {
                isNotSetUp = true;
                setUpBottomPlayer();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setUpBottomPlayer() {
        setUpDragDropLayout();
        boolean isHaving = isHavingListStream();
        showLayoutListenMusic(isHaving);
        if (isHaving) {
            boolean isPlay = YPYStreamManager.getInstance().isPlaying();
            boolean isLoad = YPYStreamManager.getInstance().isLoading();

            showLoading(isLoad);
            updateStatePlayer(isPlay);
            updateInfoOfPlayingTrack(true);
            YPYMediaPlayer.StreamInfo mStrInfo = YPYStreamManager.getInstance().getStreamInfo();
            processUpdateImage(mStrInfo != null ? mStrInfo.imgUrl : null);
        }
    }

    private void setUpDragDropLayout() {
        findViewById(R.id.img_fake_touch).setOnTouchListener((v, event) -> true);
        mStartHeight = getResources().getDimensionPixelOffset(R.dimen.size_img_big);
        viewBinding.layoutTotalDragDrop.layoutSmallControl.setOnClickListener(view -> expandLayoutListenMusic());

        mBottomSheetBehavior = (YPYBottomSheetBehavior<RelativeLayout>) BottomSheetBehavior.from(viewBinding.layoutTotalDragDrop.getRoot());
        mBottomSheetBehavior.setPeekHeight(mStartHeight);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        mBottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            boolean isHidden;
            float mSlideOffset;

            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                try {
                    if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                        showAppBar(false);
                        showHeaderMusicPlayer(true);
                        enableDragForBottomSheet(ALLOW_DRAG_DROP_WHEN_EXPAND);
                    }
                    else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                        isHidden = false;
                        showAppBar(true);
                        enableDragForBottomSheet(true);
                        showHeaderMusicPlayer(false);
                        if (!isHavingListStream()) {
                            showLayoutListenMusic(false);
                        }
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                try {
                    if (mSlideOffset > 0 && slideOffset > mSlideOffset && !isHidden) {
                        showAppBar(false);
                        isHidden = true;
                    }
                    mSlideOffset = slideOffset;
                    viewBinding.layoutTotalDragDrop.layoutSmallControl.setVisibility(View.VISIBLE);
                    viewBinding.layoutTotalDragDrop.dragDropContainer.setVisibility(View.VISIBLE);
                    viewBinding.layoutTotalDragDrop.layoutSmallControl.setAlpha(1f - slideOffset);
                    viewBinding.layoutTotalDragDrop.dragDropContainer.setAlpha(slideOffset);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
        showLayoutListenMusic(false);
    }

    private void showHeaderMusicPlayer(boolean b) {
        viewBinding.layoutTotalDragDrop.layoutSmallControl.setVisibility(!b ? View.VISIBLE : View.GONE);
        viewBinding.layoutTotalDragDrop.dragDropContainer.setVisibility(b ? View.VISIBLE : View.INVISIBLE);
    }

    public void showAppBar(boolean b) {
        viewBinding.appBar.setExpanded(b);
    }

    private void showLayoutListenMusic(boolean b) {
        if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED && !b) {
            return;
        }
        viewBinding.layoutTotalDragDrop.getRoot().setVisibility(b ? View.VISIBLE : View.GONE);
        viewBinding.viewPager.setPadding(0, 0, 0, b ? mStartHeight : 0);
        viewBinding.container.setPadding(0, 0, 0, b ? mStartHeight : 0);
        if (!b) {
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
    }

    private void expandLayoutListenMusic() {
        if (mBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            if (mFragmentDragDrop != null) {
                mFragmentDragDrop.updateVolume();
            }
            enableDragForBottomSheet(ALLOW_DRAG_DROP_WHEN_EXPAND);
        }
    }

    public boolean collapseListenMusic() {
        if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            enableDragForBottomSheet(ALLOW_DRAG_DROP_WHEN_EXPAND);
            return true;
        }
        return false;
    }

    public void enableDragForBottomSheet(boolean b) {
        mBottomSheetBehavior.setAllowUserDragging(b);
    }


    private void setUpActionBar() {
        mConfigureModel = mTotalMng.getConfigureModel();
        mUIConfigModel = mTotalMng.getUiConfigModel();

        removeElevationActionBar();
        setUpCustomizeActionBar(Color.TRANSPARENT);
        setActionBarTitle(R.string.title_home_screen);

        mUrlHost = mConfigureModel != null ? mConfigureModel.getUrlEndPoint() : null;
        mApiKey = mConfigureModel != null ? mConfigureModel.getApiKey() : null;
    }

    private void setUpColorWidget() {
        try {
            int typeBg = mUIConfigModel != null ? mUIConfigModel.getIsFullBg() : UI_BG_JUST_ACTIONBAR;
            if (typeBg == UI_BG_FULL) {
                viewBinding.container.setBackgroundColor(Color.TRANSPARENT);
                viewBinding.viewPager.setBackgroundColor(Color.TRANSPARENT);
                viewBinding.tabLayout.setBackgroundColor(getResources().getColor(R.color.tab_overlay_color));
                if (mLayoutAds != null) {
                    mLayoutAds.setBackgroundColor(Color.TRANSPARENT);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected ActivityAppBarMainBinding getViewBinding() {
        return ActivityAppBarMainBinding.inflate(getLayoutInflater());
    }

    @Override
    public void updateBackground() {
        setUpBackground(this.viewBinding.layoutBg);
        int startColor = parseColor(XRadioSettingManager.getStartColor(this));
        int endColor = parseColor(XRadioSettingManager.getEndColor(this));
        if (startColor != 0 || endColor != 0) {
            GradientDrawable gradientDrawable = getGradientDrawable(startColor, 0, endColor);
            viewBinding.layoutTotalDragDrop.layoutSmallControl.setBackground(gradientDrawable);
        }
        if (mFragmentDragDrop != null) {
            mFragmentDragDrop.updateBackground();
        }
    }

    private void setUpTab() {
        viewBinding.tabLayout.setTabTextColors(getResources().getColor(R.color.tab_text_normal_color),
                getResources().getColor(R.color.tab_text_focus_color));
        viewBinding.tabLayout.setTabMode(TabLayout.MODE_FIXED);
        viewBinding.tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        viewBinding.tabLayout.setTabRippleColor(null);
        ViewCompat.setElevation(viewBinding.tabLayout, 0f);
        viewBinding.viewPager.setPagingEnabled(true);

        int uiGenre = mUIConfigModel != null ? mUIConfigModel.getUiGenre() : UI_MAGIC_GRID;
        int uiTheme = mUIConfigModel != null ? mUIConfigModel.getUiThemes() : UI_CARD_GRID;

        boolean isOnlineApp = mConfigureModel != null && mConfigureModel.isOnlineApp();
        viewBinding.tabLayout.addTab(viewBinding.tabLayout.newTab().setText(R.string.title_tab_top_chart));
        if (uiGenre > UI_HIDDEN) {
            viewBinding.tabLayout.addTab(viewBinding.tabLayout.newTab().setText(R.string.title_tab_discover));
        }
        viewBinding.tabLayout.addTab(viewBinding.tabLayout.newTab().setText(R.string.title_tab_favorite));

        if (uiTheme > UI_HIDDEN) {
            viewBinding.tabLayout.addTab(viewBinding.tabLayout.newTab().setText(R.string.title_tab_themes));
        }

        Bundle mBundle1 = new Bundle();
        mBundle1.putInt(KEY_TYPE_FRAGMENT, TYPE_TAB_FEATURED);
        mBundle1.putBoolean(KEY_IS_TAB, true);
        mBundle1.putBoolean(KEY_ALLOW_READ_CACHE, true);
        mBundle1.putBoolean(KEY_ALLOW_MORE, isOnlineApp);
        mBundle1.putBoolean(KEY_ALLOW_REFRESH, isOnlineApp);
        mBundle1.putBoolean(KEY_READ_CACHE_WHEN_NO_DATA, true);

        mFragmentTopChart = (FragmentTopChart) getSupportFragmentManager().getFragmentFactory().instantiate(getClassLoader(), FragmentTopChart.class.getName());
        mFragmentTopChart.setArguments(mBundle1);
        mListHomeFragments.add(mFragmentTopChart);

        if (uiGenre > UI_HIDDEN) {
            Bundle mBundle2 = new Bundle();
            mBundle2.putInt(KEY_TYPE_FRAGMENT, TYPE_TAB_GENRE);
            mBundle2.putBoolean(KEY_IS_TAB, true);
            mBundle2.putBoolean(KEY_ALLOW_READ_CACHE, true);
            mBundle2.putBoolean(KEY_ALLOW_REFRESH, isOnlineApp);
            mBundle2.putBoolean(KEY_READ_CACHE_WHEN_NO_DATA, true);

            FragmentGenre mFragmentGenre = (FragmentGenre) getSupportFragmentManager().getFragmentFactory().instantiate(getClassLoader(), FragmentGenre.class.getName());
            mFragmentGenre.setArguments(mBundle2);
            mListHomeFragments.add(mFragmentGenre);
        }

        Bundle mBundle3 = new Bundle();
        mBundle3.putInt(KEY_TYPE_FRAGMENT, TYPE_TAB_FAVORITE);
        mBundle3.putBoolean(KEY_IS_TAB, true);
        mBundle3.putBoolean(KEY_OFFLINE_DATA, true);
        mBundle3.putBoolean(KEY_ALLOW_REFRESH, false);
        mBundle3.putBoolean(KEY_ALLOW_SHOW_NO_DATA, false);

        mFragmentFavorite = (FragmentFavorite) getSupportFragmentManager().getFragmentFactory().instantiate(getClassLoader(), FragmentFavorite.class.getName());
        mFragmentFavorite.setArguments(mBundle3);
        mListHomeFragments.add(mFragmentFavorite);

        if (uiTheme > UI_HIDDEN) {
            Bundle mBundle4 = new Bundle();
            mBundle4.putInt(KEY_TYPE_FRAGMENT, TYPE_TAB_THEMES);
            mBundle4.putBoolean(KEY_IS_TAB, true);
            mBundle4.putBoolean(KEY_ALLOW_MORE, true);
            mBundle4.putBoolean(KEY_ALLOW_READ_CACHE, true);
            mBundle4.putBoolean(KEY_READ_CACHE_WHEN_NO_DATA, true);

            FragmentTheme mFragmentThemes = (FragmentTheme) getSupportFragmentManager().getFragmentFactory().instantiate(getClassLoader(), FragmentTheme.class.getName());
            mFragmentThemes.setArguments(mBundle4);
            mListHomeFragments.add(mFragmentThemes);
        }

        ((YPYFragment<?>) mListHomeFragments.get(mCurrentIndex)).setFirstInTab(true);

        YPYFragmentAdapter mTabAdapters = new YPYFragmentAdapter(getSupportFragmentManager(), mListHomeFragments, viewBinding.viewPager);
        viewBinding.viewPager.setAdapter(mTabAdapters);
        viewBinding.viewPager.setOffscreenPageLimit(mListHomeFragments.size());

        viewBinding.viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(viewBinding.tabLayout) {
            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });
        viewBinding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                hiddenKeyBoardForSearchView();
                int pos = tab.getPosition();
                showAppBar(true);
                viewBinding.viewPager.setCurrentItem(pos);
                checkShowAdsWhenGoingFavTab(pos);
                ((YPYFragment<?>) mListHomeFragments.get(pos)).startLoadData();

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        viewBinding.viewPager.setCurrentItem(mCurrentIndex);
    }

    private void checkShowAdsWhenGoingFavTab(int pos) {
        try {
            if (mFragmentFavorite != null && pos == mListHomeFragments.indexOf(mFragmentFavorite)) {
                ArrayList<RadioModel> listFavs = (ArrayList<RadioModel>) mTotalMng.getListData(IXRadioConstants.TYPE_TAB_FAVORITE);
                if (listFavs == null || listFavs.isEmpty()) {
                    isHiddenAdsWhenFavEmpty = true;
                    hideAds();
                    return;
                }
            }
            if (isHiddenAdsWhenFavEmpty) {
                isHiddenAdsWhenFavEmpty = false;
                showAds();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onDestroyData() {
        XRadioSettingManager.setOnline(this, false);
        resetTimer();
        if (isHavingListStream()) {
            startMusicService(ACTION_STOP);
        }
        else {
            YPYStreamManager.getInstance().onDestroy();
        }
        super.onDestroyData();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            getMenuInflater().inflate(R.menu.menu_main, menu);

            // 🔍 Search
            MenuItem searchItem = menu.findItem(R.id.action_search);
            if (searchItem != null) {
                searchItem.setVisible(true);
            }

            // 😴 Sleep mode
            MenuItem sleepItem = menu.findItem(R.id.action_sleep_mode);
            if (sleepItem != null) {
                sleepItem.setVisible(true);
            }

            // ⭐ Rate app
            MenuItem rateItem = menu.findItem(R.id.action_rate_me);
            if (rateItem != null) {
                rateItem.setVisible(true);
            }

            // ❌ SOCIAL LINKS ELIMINADOS
            // action_facebook
            // action_twitter
            // action_insta
            // action_website
            // action_share

            // ⚙️ GDPR Ads
            ConsentInformation consentInformation =
                    UserMessagingPlatform.getConsentInformation(this);
            boolean isAvailable = consentInformation.isConsentFormAvailable();

            MenuItem adsItem = menu.findItem(R.id.action_setting_ads);
            if (adsItem != null) {
                adsItem.setVisible(isAvailable);
            }

            initSetupForSearchView(menu, R.id.action_search, new IYPYSearchViewInterface() {
                @Override
                public void onStartSuggestion(String keyword) {}

                @Override
                public void onProcessSearchData(String keyword) {
                    if (!TextUtils.isEmpty(keyword)) {
                        searchView.setQuery(keyword, false);
                        goToSearch(keyword);
                    }
                }

                @Override
                public void onClickSearchView() {}

                @Override
                public void onCloseSearchView() {}
            });

            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void goToSearch(String keyword) {
        if (!TextUtils.isEmpty(keyword)) {
            hiddenKeyBoardForSearchView();
            FragmentDetailList mFragmentSearch = (FragmentDetailList) getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT_DETAIL_SEARCH);
            if (mFragmentSearch != null) {
                mFragmentSearch.startSearch(keyword);
            }
            else {
                boolean isOnlineApp = mConfigureModel != null && mConfigureModel.isOnlineApp();
                backStack();
                setActionBarTitle(R.string.title_search);
                showHideLayoutContainer(true);
                Bundle mBundle = new Bundle();
                mBundle.putInt(KEY_TYPE_FRAGMENT, TYPE_SEARCH);
                mBundle.putBoolean(KEY_ALLOW_MORE, isOnlineApp);
                mBundle.putString(KEY_SEARCH, keyword);
                mBundle.putBoolean(KEY_ALLOW_READ_CACHE, false);
                mBundle.putBoolean(KEY_ALLOW_REFRESH, false);
                mBundle.putString(KEY_NAME_SCREEN, getString(R.string.title_search));

                goToFragment(TAG_FRAGMENT_DETAIL_SEARCH, R.id.container, FragmentDetailList.class.getName(), 0, mBundle);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_sleep_mode) {
            showDialogSleepMode();
        }
        else if (itemId == R.id.action_rate_me) {
            String urlApp = String.format(URL_FORMAT_LINK_APP, getPackageName());
            ShareActionUtils.goToUrl(this, urlApp);
            XRadioSettingManager.setRateApp(this, true);
        }
        else if (itemId == R.id.action_share) {
            String urlApp1 = String.format(URL_FORMAT_LINK_APP, getPackageName());
            String msg = String.format(getString(R.string.info_share_app), getString(R.string.app_name), urlApp1);
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/*");
            sharingIntent.putExtra(Intent.EXTRA_TEXT, msg);
            startActivity(Intent.createChooser(sharingIntent, getString(R.string.title_menu_share)));
        }
        else if (itemId == R.id.action_contact_us) {
            ShareActionUtils.shareViaEmail(this, YOUR_CONTACT_EMAIL, "", "");
        }
        else if (itemId == R.id.action_term_of_use) {
            goToUrl(getString(R.string.title_term_of_use), URL_TERM_OF_USE);
        }
        else if (itemId == R.id.action_privacy_policy) {
            goToUrl(getString(R.string.title_privacy_policy), URL_PRIVACY_POLICY);
        }
        else if (itemId == R.id.action_setting_ads) {
            GDPRManager.getInstance().loadConsentForm(this, null);
        }
        else if (itemId == R.id.action_support_radio) {

            if (rewardedHelper.isRewardActive()) {
                showToast("Thanks ❤️ You are already supporting the radio");
                return true;
            }

            rewardedHelper.showRewardedAd(
                    mLayoutAds, // o viewBinding.layoutAds si existe
                    () -> showToast("Thanks for supporting the radio ❤️")
            );
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                if (mFragmentDragDrop != null) {
                    mFragmentDragDrop.increaseVolume();
                }
                return true;
            }

        }
        else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                if (mFragmentDragDrop != null) {
                    mFragmentDragDrop.downVolume();
                }
                return true;
            }
        }
        else if (keyCode == KeyEvent.KEYCODE_MEDIA_NEXT) {
            if (ApplicationUtils.isOnline(this) && isHavingListStream()) {
                startMusicService(ACTION_NEXT);
                return true;
            }
        }
        else if (keyCode == KeyEvent.KEYCODE_MEDIA_PREVIOUS) {
            if (ApplicationUtils.isOnline(this) && isHavingListStream()) {
                startMusicService(ACTION_PREVIOUS);
                return true;
            }

        }
        else if (keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
            if (ApplicationUtils.isOnline(this) && isHavingListStream()) {
                if (YPYStreamManager.getInstance().isPlaying()) {
                    startMusicService(ACTION_TOGGLE_PLAYBACK);
                    return true;
                }
            }

        }
        else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
            if (ApplicationUtils.isOnline(this) && isHavingListStream()) {
                if (YPYStreamManager.getInstance().isPrepareDone() &&
                        !YPYStreamManager.getInstance().isPlaying()) {
                    startMusicService(ACTION_TOGGLE_PLAYBACK);
                    return true;
                }
            }

        }
        else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
            if (ApplicationUtils.isOnline(this) && isHavingListStream()) {
                startMusicService(ACTION_TOGGLE_PLAYBACK);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean backToHome() {
        if (collapseListenMusic()) {
            return true;
        }
        boolean b = super.backToHome();
        if (b) {
            return true;
        }
        b = backStack();
        if (b) {
            showHideLayoutContainer(false);
            return true;
        }
        return false;
    }

    @Override
    public boolean isFragmentCheckBack() {
        try {
            if (mListHomeFragments != null && mListHomeFragments.size() > 0) {
                for (Fragment mFragment : mListHomeFragments) {
                    if (mFragment instanceof YPYFragment) {
                        boolean isBack = ((YPYFragment<?>) mFragment).isCheckBack();
                        if (isBack) {
                            return true;
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return super.isFragmentCheckBack();
    }

    public void showHideLayoutContainer(boolean b) {
        viewBinding.container.setVisibility(b ? View.VISIBLE : View.GONE);
        viewBinding.tabLayout.setVisibility(b ? View.GONE : View.VISIBLE);
        viewBinding.viewPager.setVisibility(b ? View.GONE : View.VISIBLE);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(b);
            getSupportActionBar().setHomeButtonEnabled(b);
            getSupportActionBar().setDisplayUseLogoEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
            if (b) {
                showAppBar(true);
                getSupportActionBar().setHomeAsUpIndicator(mBackDrawable);
            }
            else {
                setActionBarTitle(R.string.title_home_screen);
            }
        }

    }

    @Override
    public void notifyFavorite(int type, long id, boolean isFav) {
        super.notifyFavorite(type, id, isFav);
        if (mFragmentTopChart != null) {
            mFragmentTopChart.notifyFavorite(id, isFav);
        }
        runOnUiThread(() -> {
            if (mFragmentFavorite != null) {
                mFragmentFavorite.notifyData();
                checkShowAdsWhenGoingFavTab(viewBinding.viewPager.getCurrentItem());
            }
            if (mFragmentDragDrop != null) {
                mFragmentDragDrop.notifyFavorite(id, isFav);
            }
        });

    }

    public void goToGenreModel(GenreModel model) {
        if (model != null) {
            boolean isOnlineApp = mConfigureModel != null && mConfigureModel.isOnlineApp();

            setActionBarTitle(model.getName());
            showHideLayoutContainer(true);
            Bundle mBundle = new Bundle();
            mBundle.putInt(KEY_TYPE_FRAGMENT, TYPE_DETAIL_GENRE);
            mBundle.putBoolean(KEY_ALLOW_MORE, true);
            mBundle.putBoolean(KEY_ALLOW_READ_CACHE, false);
            mBundle.putBoolean(KEY_ALLOW_REFRESH, true);
            mBundle.putString(KEY_NAME_SCREEN, model.getName());
            mBundle.putBoolean(KEY_ALLOW_REFRESH, isOnlineApp);

            mBundle.putLong(KEY_GENRE_ID, model.getId());

            String tag = getCurrentFragmentTag();
            if (TextUtils.isEmpty(tag)) {
                goToFragment(TAG_FRAGMENT_DETAIL_GENRE, R.id.container, FragmentDetailList.class.getName(), 0, mBundle);
            }
            else {
                goToFragment(TAG_FRAGMENT_DETAIL_GENRE, R.id.container, FragmentDetailList.class.getName(), tag, mBundle);
            }
        }
    }

    public void startPlayingList(RadioModel model, ArrayList<RadioModel> listRadioModels) {
        if (!ApplicationUtils.isOnline(this)) {
            if (isAllCheckNetWorkOff) {
                showToast(R.string.info_connect_to_play);
                return;
            }
            if (YPYStreamManager.getInstance().isPrepareDone()) {
                startMusicService(ACTION_STOP);
            }
            showToast(R.string.info_connect_to_play);
            return;
        }
        RadioModel currentRadio = YPYStreamManager.getInstance().getCurrentRadio();
        if (currentRadio != null && currentRadio.equals(model)) {
            return;
        }
        showModeInterstitial(() -> playRadio(model, listRadioModels));
    }

    private void playRadio(RadioModel model, ArrayList<RadioModel> listRadioModels) {
        updateInfoOfPlayingTrack(model, true);
        String url = model != null ? model.getArtWork(mUrlHost) : null;
        if (mFragmentDragDrop != null) {
            mFragmentDragDrop.updateImage(url);
        }
        if (listRadioModels != null && listRadioModels.size() > 0) {
            ArrayList<RadioModel> mListPlaying = YPYStreamManager.getInstance().getListModels();
            if (mListPlaying == null || !mTotalMng.isListEqual(mListPlaying, listRadioModels)) {
                ArrayList<RadioModel> mListDatas = (ArrayList<RadioModel>) listRadioModels.clone();
                YPYStreamManager.getInstance().setListModels(mListDatas);

            }
            startPlayRadio(model);
        }
    }

    public void startPlayRadio(RadioModel trackModel) {
        try {
            viewBinding.layoutTotalDragDrop.btnSmallPlay.setImageResource(R.drawable.ic_play_arrow_white_36dp);
            boolean b = YPYStreamManager.getInstance().setCurrentData(trackModel);
            if (b) {
                startMusicService(ACTION_PLAY);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            viewBinding.layoutTotalDragDrop.btnSmallPlay.setImageResource(R.drawable.ic_play_arrow_white_36dp);
            startMusicService(ACTION_STOP);
        }

    }

    private void updateInfoOfPlayingTrack(boolean isNeedUpdateSocial) {
        RadioModel ringtoneModel = YPYStreamManager.getInstance().getCurrentRadio();
        updateInfoOfPlayingTrack(ringtoneModel, isNeedUpdateSocial);
    }

    private void updateInfoOfPlayingTrack(RadioModel radioModel, boolean isNeedUpdateSocial) {
        try {
            if (radioModel != null) {
                showLayoutListenMusic(true);
                viewBinding.layoutTotalDragDrop.tvRadioName.setText(Html.fromHtml(radioModel.getName()));
                String artist = radioModel.getMetaData();
                if (TextUtils.isEmpty(artist)) {
                    artist = radioModel.getTags();
                    if (TextUtils.isEmpty(artist)) {
                        artist = getString(R.string.title_unknown);
                    }
                }
                viewBinding.layoutTotalDragDrop.tvInfo.setText(artist);
                viewBinding.layoutTotalDragDrop.tvInfo.setSelected(true);

                String imgSong = radioModel.getArtWork(mUrlHost);
                if (!TextUtils.isEmpty(imgSong)) {
                    GlideImageLoader.displayImage(this, viewBinding.layoutTotalDragDrop.imgSong, imgSong, R.drawable.ic_rect_img_default);
                }
                else {
                    viewBinding.layoutTotalDragDrop.imgSong.setImageResource(R.drawable.ic_rect_img_default);
                }
                if (mFragmentDragDrop != null) {
                    mFragmentDragDrop.updateInfo(isNeedUpdateSocial);
                }

            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void registerApplicationBroadcastReceiver() {
        if (mApplicationBroadcast != null) {
            return;
        }
        mApplicationBroadcast = new ApplicationBroadcast();
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(getPackageName() + ACTION_BROADCAST_PLAYER);
        if(IOUtils.isAndroid14()){
            registerReceiver(mApplicationBroadcast, mIntentFilter,RECEIVER_EXPORTED);
        }
        else{
            registerReceiver(mApplicationBroadcast, mIntentFilter);
        }

    }

    private class ApplicationBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (intent != null) {
                    String action = intent.getAction();
                    if (action != null && !TextUtils.isEmpty(action)) {
                        String packageName = getPackageName();
                        if (action.equals(packageName + ACTION_BROADCAST_PLAYER)) {
                            String actionPlay = intent.getStringExtra(KEY_ACTION);
                            if (!TextUtils.isEmpty(actionPlay)) {
                                if (actionPlay.equalsIgnoreCase(ACTION_UPDATE_COVER_ART)) {
                                    String value = intent.getStringExtra(KEY_VALUE);
                                    processUpdateImage(value);

                                }
                                else {
                                    long value = intent.getLongExtra(KEY_VALUE, -1);
                                    processBroadcast(actionPlay, value);
                                }

                            }
                        }

                    }
                }

            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void processBroadcast(String actionPlay, long value) {
        if (actionPlay.equalsIgnoreCase(ACTION_LOADING)) {
            showLoading(true);
            updateInfoOfPlayingTrack(true);
            if (mFragmentDragDrop != null) {
                mFragmentDragDrop.showLoading(true);
                mFragmentDragDrop.updateInfo(false);
                RadioModel model = YPYStreamManager.getInstance().getCurrentRadio();
                mFragmentDragDrop.updateImage(model != null ? model.getArtWork(mUrlHost) : null);
            }
        }
        if (actionPlay.equalsIgnoreCase(ACTION_DIMINISH_LOADING)) {
            showLoading(false);
            if (mFragmentDragDrop != null) {
                mFragmentDragDrop.showLoading(false);
                mFragmentDragDrop.showLayoutControl();
            }
        }
        if (actionPlay.equalsIgnoreCase(ACTION_RESET_INFO)) {
            if (mFragmentDragDrop != null) {
                mFragmentDragDrop.updateInfo(false);
                mFragmentDragDrop.updateImage(null);
            }
        }
        if (actionPlay.equalsIgnoreCase(ACTION_COMPLETE)) {
            updateStatePlayer(false);
            viewBinding.layoutTotalDragDrop.tvInfo.setText(R.string.info_radio_ended_title);
            if (mFragmentDragDrop != null) {
                mFragmentDragDrop.updateInfoWhenComplete();
                mFragmentDragDrop.updateImage(null);
            }
        }
        if (actionPlay.equalsIgnoreCase(ACTION_CONNECTION_LOST)) {
            updateStatePlayer(false);
            viewBinding.layoutTotalDragDrop.tvInfo.setText(R.string.info_radio_ended_title);
            if (mFragmentDragDrop != null) {
                mFragmentDragDrop.updateInfoWhenComplete();
                mFragmentDragDrop.updateImage(null);
            }
        }
        else if (actionPlay.equalsIgnoreCase(ACTION_BUFFERING)) {
            showLoading(true);
            if (mFragmentDragDrop != null) {
                mFragmentDragDrop.showLoading(false);
                mFragmentDragDrop.updatePercent(value);
            }
        }
        else if (actionPlay.equalsIgnoreCase(ACTION_PAUSE)) {
            updateStatePlayer(false);
        }
        else if (actionPlay.equalsIgnoreCase(ACTION_PLAY)) {
            updateStatePlayer(true);
        }
        else if (actionPlay.equalsIgnoreCase(ACTION_STOP) || actionPlay.equalsIgnoreCase(ACTION_ERROR)) {
            updateStatePlayer(false);
            showLayoutListenMusic(false);
            if (mFragmentDragDrop != null) {
                mFragmentDragDrop.updateSleepMode(0);
                mFragmentDragDrop.updateStatusPlayer(false);
            }
            collapseListenMusic();
            if (actionPlay.equalsIgnoreCase(ACTION_ERROR)) {
                int resId = ApplicationUtils.isOnline(this) ? R.string.info_play_error : R.string.info_connect_to_play;
                showToast(resId);
            }
        }
        else if (actionPlay.equalsIgnoreCase(ACTION_UPDATE_INFO)) {
            updateInfoOfPlayingTrack(false);
        }
        else if (actionPlay.equalsIgnoreCase(ACTION_UPDATE_SLEEP_MODE)) {
            if (mFragmentDragDrop != null) {
                mFragmentDragDrop.updateSleepMode(value);
            }

        }

    }

    @Override
    public void onDoWhenNetworkOn() {
        super.onDoWhenNetworkOn();
        if (isHavingListStream()) {
            if (isAllCheckNetWorkOff) {
                isAllCheckNetWorkOff = false;
                startMusicService(ACTION_TOGGLE_PLAYBACK);
            }
        }

    }

    @Override
    public void onDoWhenNetworkOff() {
        super.onDoWhenNetworkOff();
        if (isHavingListStream()) {
            isAllCheckNetWorkOff = true;
            startMusicService(ACTION_CONNECTION_LOST);
        }
    }

    public void processUpdateImage(String imgSong) {
        try {
            if (TextUtils.isEmpty(imgSong)) {
                RadioModel ringtoneModel = YPYStreamManager.getInstance().getCurrentRadio();
                imgSong = ringtoneModel.getArtWork(mUrlHost);
            }
            if (!TextUtils.isEmpty(imgSong)) {
                GlideImageLoader.displayImage(this, viewBinding.layoutTotalDragDrop.imgSong, imgSong, R.drawable.ic_rect_img_default);
            }
            else {
                viewBinding.layoutTotalDragDrop.imgSong.setImageResource(R.drawable.ic_rect_img_default);
            }
            if (mFragmentDragDrop != null) {
                mFragmentDragDrop.updateImage(imgSong);
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showLoading(boolean b) {
        viewBinding.layoutTotalDragDrop.btnSmallPlay.setVisibility(!b ? View.VISIBLE : View.INVISIBLE);
        viewBinding.layoutTotalDragDrop.btnSmallPrev.setVisibility(!b ? View.VISIBLE : View.INVISIBLE);
        viewBinding.layoutTotalDragDrop.btnSmallNext.setVisibility(!b ? View.VISIBLE : View.INVISIBLE);
        viewBinding.layoutTotalDragDrop.statusProgressBar.setVisibility(b ? View.VISIBLE : View.GONE);
    }

    public void updateStatePlayer(boolean isPlaying) {
        int playId = isPlaying ? R.drawable.ic_pause_white_36dp : R.drawable.ic_play_arrow_white_36dp;
        viewBinding.layoutTotalDragDrop.btnSmallPlay.setImageResource(playId);
        if (mFragmentDragDrop != null) {
            mFragmentDragDrop.updateStatusPlayer(isPlaying);
        }

    }

    @Override
    protected void onDestroy() {
        if (isHavingListStream()) {
            startMusicService(ACTION_STOP);
        }
        if (mApplicationBroadcast != null) {
            unregisterReceiver(mApplicationBroadcast);
            mApplicationBroadcast = null;
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        if (isAllCheckNetWorkOff && !ApplicationUtils.isOnline(this)) {
            showToast(R.string.info_connect_to_play);
            return;
        }
        int id = view.getId();
        if (id == R.id.btn_small_next) {
            startMusicService(ACTION_NEXT);
        }
        else if (id == R.id.btn_small_prev) {
            startMusicService(ACTION_PREVIOUS);
        }
        else if (id == R.id.btn_small_play) {
            startMusicService(ACTION_TOGGLE_PLAYBACK);
        }
    }

    public void showModeInterstitial(IYPYCallback mCallback) {
        if (mCallback != null) {
            mCallback.onAction();
        }
    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (viewBinding.viewPager.getCurrentItem() >= 0) {
            outState.putInt(KEY_TOP_INDEX, viewBinding.viewPager.getCurrentItem());
        }
    }

    @Override
    public void onUpdateUIWhenSupportRTL() {
        super.onUpdateUIWhenSupportRTL();
        viewBinding.layoutTotalDragDrop.tvRadioName.setGravity(Gravity.END);
        viewBinding.layoutTotalDragDrop.tvInfo.setGravity(Gravity.END);
        viewBinding.layoutTotalDragDrop.btnSmallNext.setImageResource(R.drawable.ic_skip_previous_white_36dp);
        viewBinding.layoutTotalDragDrop.btnSmallPrev.setImageResource(R.drawable.ic_skip_next_white_36dp);

    }
}
