package plus.bookmark;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.core.graphics.ColorUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Components.AudioPlayerAlert;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.SizeNotifierFrameLayout;
import org.telegram.ui.Components.ViewPagerFixed;
import org.telegram.ui.ProfileActivity;

import java.util.ArrayList;
import java.util.Locale;

import plus.analyatics.AnalyticsInstance;

public class BookmarkActivity extends BaseFragment implements BookmarkViewPager.MediaPreloaderDelegate, NotificationCenter.NotificationCenterDelegate {

    public final static int deleteItemId = 202;

    private SimpleTextView nameTextView;
    ProfileActivity.AvatarImageView avatarImageView;
    private BookmarkViewPager musicPager;
    AudioPlayerAlert.ClippingTextViewSwitcher mediaCounterTextView;
    private BookmarkViewPager.MediaCountLoader mediaCountLoader;
    private ViewPagerFixed.TabsView tabsView;
    private View shadowLine;

    @Override
    public boolean onFragmentCreate() {
        if (this.mediaCountLoader == null) {
            this.mediaCountLoader = new BookmarkViewPager.MediaCountLoader();
            this.mediaCountLoader.addDelegate(this);
        }
        getNotificationCenter().addObserver(this,NotificationCenter.reloadBookmark);

        return super.onFragmentCreate();
    }

    @Override
    public void onFragmentDestroy() {
        getNotificationCenter().removeObserver(this,NotificationCenter.reloadBookmark);

        super.onFragmentDestroy();
    }



    private FrameLayout avatarContainer;




    @Override
    public View createView(Context context) {
        actionBar.setBackButtonDrawable(new BackDrawable(false));
        actionBar.setCastShadows(false);
        actionBar.setAddToContainer(false);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick(){
            @Override
            public void onItemClick(int id) {
                if ((id == deleteItemId)) {
                    musicPager.onActionBarItemClick(id);
                }else   if (id == -1) {
                    if (actionBar.isActionModeShowed()) {
                        musicPager.showActionMode(false);
                    } else {
                        finishFragment();
                    }
                }
            }
        });
        avatarContainer = new FrameLayout(context);
        SizeNotifierFrameLayout fragmentView = new SizeNotifierFrameLayout(context) {

            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                LayoutParams lp = (LayoutParams) tabsView.getLayoutParams();
                lp.topMargin =ActionBar.getCurrentActionBarHeight() + (actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0);

                lp = (LayoutParams) shadowLine.getLayoutParams();
                lp.topMargin = ActionBar.getCurrentActionBarHeight() + (actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + AndroidUtilities.dp(44 - 1);


                lp = (LayoutParams) musicPager.getLayoutParams();
                lp.topMargin = ActionBar.getCurrentActionBarHeight() + (actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + AndroidUtilities.dp(44);

                lp = (LayoutParams) avatarContainer.getLayoutParams();
                lp.topMargin = actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0;
                lp.height = ActionBar.getCurrentActionBarHeight();

                int textTop = (ActionBar.getCurrentActionBarHeight() / 2 - AndroidUtilities.dp(22)) / 2 + AndroidUtilities.dp(!AndroidUtilities.isTablet() && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? 4 : 5);
                lp = (LayoutParams) nameTextView.getLayoutParams();
                lp.topMargin = textTop;

                textTop = ActionBar.getCurrentActionBarHeight() / 2 + (ActionBar.getCurrentActionBarHeight() / 2 - AndroidUtilities.dp(19)) / 2 - AndroidUtilities.dp(3);
                lp = (LayoutParams) mediaCounterTextView.getLayoutParams();
                lp.topMargin = textTop;

                lp = (LayoutParams) avatarImageView.getLayoutParams();
                lp.topMargin = (ActionBar.getCurrentActionBarHeight() - AndroidUtilities.dp(42)) / 2;




                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }

            @Override
            public boolean dispatchTouchEvent(MotionEvent ev) {

                return super.dispatchTouchEvent(ev);
            }

        };
        fragmentView.needBlur = true;
        this.fragmentView = fragmentView;

        nameTextView = new SimpleTextView(context);

        nameTextView.setTextSize(18);
        nameTextView.setGravity(Gravity.LEFT);
        nameTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        nameTextView.setLeftDrawableTopPadding(-AndroidUtilities.dp(1.3f));
        nameTextView.setScrollNonFitText(true);
        nameTextView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        avatarContainer.addView(nameTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 118, 0, 56, 0));

        avatarImageView = new ProfileActivity.AvatarImageView(context) {
            @Override
            public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
                super.onInitializeAccessibilityNodeInfo(info);
                if (getImageReceiver().hasNotThumb()) {
                    info.setText(LocaleController.getString("AccDescrProfilePicture", R.string.AccDescrProfilePicture));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        info.addAction(new AccessibilityNodeInfo.AccessibilityAction(AccessibilityNodeInfo.ACTION_CLICK, LocaleController.getString("Open", R.string.Open)));
                        info.addAction(new AccessibilityNodeInfo.AccessibilityAction(AccessibilityNodeInfo.ACTION_LONG_CLICK, LocaleController.getString("AccDescrOpenInPhotoViewer", R.string.AccDescrOpenInPhotoViewer)));
                    }
                } else {
                    info.setVisibleToUser(false);
                }
            }
        };
        avatarImageView.getImageReceiver().setAllowDecodeSingleFrame(true);
        avatarImageView.setRoundRadius(AndroidUtilities.dp(21));
        avatarImageView.setPivotX(0);
        avatarImageView.setPivotY(0);
        AvatarDrawable avatarDrawable = new AvatarDrawable();
        avatarDrawable.setAvatarType(AvatarDrawable.AVATAR_TYPE_BOOKMARK);
        avatarDrawable.setProfile(true);

        avatarImageView.setImageDrawable(avatarDrawable);
        avatarContainer.addView(avatarImageView, LayoutHelper.createFrame(42, 42, Gravity.TOP | Gravity.LEFT, 64, 0, 0, 0));


        mediaCounterTextView = new AudioPlayerAlert.ClippingTextViewSwitcher(context) {
            @Override
            protected TextView createTextView() {
                TextView textView = new TextView(context);
                textView.setTextColor(Theme.getColor(Theme.key_player_actionBarSubtitle));
                textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
                textView.setSingleLine(true);
                textView.setEllipsize(TextUtils.TruncateAt.END);
                textView.setGravity(Gravity.LEFT);
                return textView;
            }
        };
        avatarContainer.addView(mediaCounterTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 118, 0, 56, 0));


        musicPager = new BookmarkViewPager(context,this,mediaCountLoader){
            @Override
            protected void onSelectedTabChanged() {
                updateMediaCount();
            }
        };
        musicPager.setAvatarContainer(avatarContainer);
        tabsView = musicPager.createTabsView(true,8);
        fragmentView.addView(tabsView,LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,44));
        fragmentView.addView(musicPager);
        fragmentView.addView(actionBar);
        fragmentView.addView(avatarContainer);
        fragmentView.blurBehindViews.add(musicPager);

        shadowLine = new View(context);
        shadowLine.setBackgroundColor(getThemedColor(Theme.key_divider));
        fragmentView.addView(shadowLine,LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,1));

        nameTextView.setText("Bookmarks");
        actionBar.setDrawBlurBackground(fragmentView);
        AndroidUtilities.updateViewVisibilityAnimated(avatarContainer, true, 1, false);
        updateMediaCount();
        updateColors();
        AnalyticsInstance.getInstance().trackEvent(AnalyticsInstance.EVENT_BOOKMARK_OPENED);

        return fragmentView;
    }


    private void updateMediaCount() {
        if(musicPager.getTabsView() == null){
            return;
        }

        int id = musicPager.getTabsView().getCurrentTabId();
        int[] mediaCount = mediaCountLoader.getLastMediaCount();
        if (id < 0 || mediaCount[id] < 0) {
            return;
        }
        if (id == BookmarkViewPager.BOT_TYPE) {
            mediaCounterTextView.setText(String.format(Locale.US,"%s Bots",mediaCount[id]));
        } else if (id == BookmarkViewPager.CHANNEL_TYPE) {
            mediaCounterTextView.setText(String.format(Locale.US,"%s Channels",mediaCount[id]));

        }
    }

    private void updateColors() {
        actionBar.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        actionBar.setItemsColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2), false);
        actionBar.setItemsBackgroundColor(Theme.getColor(Theme.key_actionBarActionModeDefaultSelector), false);
        actionBar.setTitleColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        nameTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
    }
    @Override
    public ArrayList<ThemeDescription> getThemeDescriptions() {
        ThemeDescription.ThemeDescriptionDelegate themeDelegate = this::updateColors;
        ArrayList<ThemeDescription> arrayList = new ArrayList<>();
        arrayList.add(new ThemeDescription(null, 0, null, null, null, themeDelegate, Theme.key_windowBackgroundWhite));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, themeDelegate, Theme.key_actionBarActionModeDefaultSelector));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, themeDelegate, Theme.key_windowBackgroundWhiteBlackText));
        musicPager.getThemeDescriptions(arrayList);
        return arrayList;
    }

    @Override
    public boolean isLightStatusBar() {
        int color = Theme.getColor(Theme.key_windowBackgroundWhite);
        if (actionBar.isActionModeShowed()) {
            color = Theme.getColor(Theme.key_actionBarActionModeDefault);
        }
        return ColorUtils.calculateLuminance(color) > 0.7f;
    }


    @Override
    public void mediaCountUpdated() {
        updateMediaCount();
    }


    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if(id == NotificationCenter.reloadBookmark){
            if(musicPager != null){
                ArrayList<Long> dialogs = (ArrayList<Long>)args[0];
                musicPager.bookmarkDeleted(dialogs);
            }
        }
    }
}
