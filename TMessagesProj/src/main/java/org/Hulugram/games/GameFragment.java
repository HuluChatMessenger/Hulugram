package plus.games;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;


import org.telegram.messenger.AccountInstance;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.DialogObject;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.browser.Browser;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Adapters.FiltersView;
import org.telegram.ui.Cells.ChatActionCell;
import org.telegram.ui.Cells.DialogCell;
import org.telegram.ui.Cells.GraySectionCell;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.HintDialogCell;
import org.telegram.ui.Cells.LoadingCell;
import org.telegram.ui.Cells.ProfileSearchCell;
import org.telegram.ui.Cells.SharedAudioCell;
import org.telegram.ui.Cells.SharedDocumentCell;
import org.telegram.ui.Cells.SharedLinkCell;
import org.telegram.ui.Cells.SharedMediaSectionCell;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BlurredRecyclerView;
import org.telegram.ui.Components.ChatAvatarContainer;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.FlickerLoadingView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.SizeNotifierFrameLayout;
import org.telegram.ui.Components.StickerEmptyView;
import org.telegram.ui.FilteredSearchView;
import org.telegram.ui.PhotoViewer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import plus.analyatics.AnalyticsInstance;
import plus.database.DataStorage;
import plus.database.TableModels;

public class GameFragment extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {


    private RecyclerListView innerListView;
    private RecyclerListView innerListView2;


    public   String game_channel;
    static final String q= "";

    private long themeDialogId;


    public RecyclerListView recyclerListView;
    private ThemeAdapter bookmarkAdapter;
    StickerEmptyView emptyView;

    Runnable searchRunnable;

    public ArrayList<MessageObject> messages = new ArrayList<>();
    public SparseArray<MessageObject> messagesById = new SparseArray<>();
    public ArrayList<String> sections = new ArrayList<>();
    public HashMap<String, ArrayList<MessageObject>> sectionArrays = new HashMap<>();


    private int nextSearchRate;
    String lastMessagesSearchString;
    String lastSearchFilterQueryString;

    FiltersView.MediaFilterData currentSearchFilter;
    long currentSearchDialogId;
    long currentSearchMaxDate;
    long currentSearchMinDate;
    String currentSearchString;
    boolean currentIncludeFolder;


    Activity parentActivity;
    private boolean isLoading;
    private boolean endReached;
    private int totalCount;
    private int requestIndex;

    private String currentDataQuery;

    Runnable clearCurrentResultsRunnable = new Runnable() {
        @Override
        public void run() {
            if (isLoading) {
                messages.clear();
                sections.clear();
                sectionArrays.clear();
                if (bookmarkAdapter != null) {
                    bookmarkAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    private UiCallback uiCallback;
    private final MessageHashId messageHashIdTmp = new MessageHashId(0, 0);

    public  LinearLayoutManager layoutManager;
    private  FlickerLoadingView loadingView;
    private boolean firstLoading = true;
    private int animationIndex = -1;
    public int keyboardHeight;
    private  ChatActionCell floatingDateView;

    private AnimatorSet floatingDateAnimation;
    private Runnable hideFloatingDateRunnable = () -> hideFloatingDateView(true);

    ChatAvatarContainer avatarContainer;
    private ActionBarMenuItem searchItem;



    @Override
    public boolean onFragmentCreate() {
        getNotificationCenter().addObserver(this, NotificationCenter.emojiLoaded);
         FirebaseRemoteConfig firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
         game_channel = firebaseRemoteConfig.getString("game_channel");
        if(TextUtils.isEmpty(game_channel)){
            game_channel = "hulugames";
        }
//        loadRecentGames();
        loadGames();

        return super.onFragmentCreate();
    }

    public DataStorage getDataStorage(){
        return DataStorage.getInstance(currentAccount);
    }

//
//    private List<TableModels.GameModel> recentGames = new ArrayList<>();
//    private void loadRecentGames(){
//        getDataStorage().getStorageQueue().postRunnable(new Runnable() {
//            @Override
//            public void run() {
//               List<TableModels.GameModel> gameModelList =  getDataStorage().getDatabase().gameDao().getGames();
//               AndroidUtilities.runOnUIThread(() -> {
//                   recentGames = gameModelList;
//                   if(bookmarkAdapter != null){
//                       bookmarkAdapter.notifyDataSetChanged();
//                   }
//               });
//            }
//        });
//    }
//
//    public void addGameToRecent(TableModels.GameModel gameModel){
//        getDataStorage().getStorageQueue().postRunnable(new Runnable() {
//            @Override
//            public void run() {
//                getDataStorage().getDatabase().gameDao().updateGameOrder(gameModel);
//            }
//        });
//
//    }





    @Override
    public void onFragmentDestroy() {
        getNotificationCenter().removeObserver(this, NotificationCenter.emojiLoaded);
        super.onFragmentDestroy();
    }

    public void goToMessage(MessageObject messageObject) {
        Bundle args = new Bundle();
        long dialogId = messageObject.getDialogId();
        if (DialogObject.isEncryptedDialog(dialogId)) {
            args.putInt("enc_id", DialogObject.getEncryptedChatId(dialogId));
        } else if (DialogObject.isUserDialog(dialogId)) {
            args.putLong("user_id", dialogId);
        } else {
            TLRPC.Chat chat = AccountInstance.getInstance(currentAccount).getMessagesController().getChat(-dialogId);
            if (chat != null && chat.migrated_to != null) {
                args.putLong("migrated_to", dialogId);
                dialogId = -chat.migrated_to.channel_id;
            }
            args.putLong("chat_id", -dialogId);
        }
        args.putInt("message_id", messageObject.getId());
        presentFragment(new ChatActivity(args));
        //showActionMode(false);
    }


    @Override
    public boolean isLightStatusBar() {
        int color = Theme.getColor(Theme.key_windowBackgroundWhite);
        return ColorUtils.calculateLuminance(color) > 0.7f;
    }


    @Override
    public View createView(Context context) {

        uiCallback = new UiCallback() {
            @Override
            public void goToMessage(MessageObject messageObject) {

            }

            @Override
            public boolean actionModeShowing() {
                return false;
            }

            @Override
            public void toggleItemSelection(MessageObject item, View view, int a) {

            }

            @Override
            public boolean isSelected(MessageHashId messageHashId) {
                return false;
            }

            @Override
            public void showActionMode() {

            }

            @Override
            public int getFolderId() {
                return 0;
            }
        };
        actionBar.setAddToContainer(false);
        actionBar.setCastShadows(false);
        actionBar.setClipContent(true);
        actionBar.setOccupyStatusBar(!AndroidUtilities.isTablet() && !inPreviewMode);
        actionBar.setBackButtonDrawable(new BackDrawable(false));

        SizeNotifierFrameLayout frameLayout;
        fragmentView  = frameLayout = new SizeNotifierFrameLayout(context) {
            {
                setWillNotDraw(false);
            }

            public int getActionBarFullHeight() {
                float h = actionBar.getHeight();
                return (int) h;
            }

            @Override
            protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
                if (child == actionBar && !isInPreviewMode()) {
                    int y = (int) (actionBar.getY() + getActionBarFullHeight());
                    getParentLayout().drawHeaderShadow(canvas, (int) (255), y);
                }
                return super.drawChild(canvas, child, drawingTime);
            }

            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                int width = MeasureSpec.getSize(widthMeasureSpec);
                int height = MeasureSpec.getSize(heightMeasureSpec);

                int actionBarHeight = 0;
                for (int i = 0; i < getChildCount(); i++) {
                    View child = getChildAt(i);
                    if (child instanceof ActionBar) {
                        child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
                        actionBarHeight = child.getMeasuredHeight();
                    }
                }
                for (int i = 0; i < getChildCount(); i++) {
                    View child = getChildAt(i);
                    if (!(child instanceof ActionBar)) {
                        if (child.getFitsSystemWindows()) {
                            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
                        } else {
                            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, actionBarHeight);
                        }
                    }
                }
                setMeasuredDimension(width, height);
            }

            @Override
            protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
                final int count = getChildCount();

                final int parentLeft = getPaddingLeft();
                final int parentRight = right - left - getPaddingRight();

                final int parentTop = getPaddingTop();
                final int parentBottom = bottom - top - getPaddingBottom();

                for (int i = 0; i < count; i++) {
                    final View child = getChildAt(i);
                    if (child.getVisibility() != GONE) {
                        final LayoutParams lp = (LayoutParams) child.getLayoutParams();

                        final int width = child.getMeasuredWidth();
                        final int height = child.getMeasuredHeight();

                        int childLeft;
                        int childTop;

                        int gravity = lp.gravity;
                        if (gravity == -1) {
                            gravity = Gravity.NO_GRAVITY;
                        }

                        boolean forceLeftGravity = false;
                        final int layoutDirection;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                            layoutDirection = getLayoutDirection();
                        } else {
                            layoutDirection = 0;
                        }
                        final int absoluteGravity = Gravity.getAbsoluteGravity(gravity, layoutDirection);
                        final int verticalGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;

                        switch (absoluteGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
                            case Gravity.CENTER_HORIZONTAL:
                                childLeft = parentLeft + (parentRight - parentLeft - width) / 2 +
                                        lp.leftMargin - lp.rightMargin;
                                break;
                            case Gravity.RIGHT:
                                if (!forceLeftGravity) {
                                    childLeft = parentRight - width - lp.rightMargin;
                                    break;
                                }
                            case Gravity.LEFT:
                            default:
                                childLeft = parentLeft + lp.leftMargin;
                        }

                        switch (verticalGravity) {
                            case Gravity.CENTER_VERTICAL:
                                childTop = parentTop + (parentBottom - parentTop - height) / 2 +
                                        lp.topMargin - lp.bottomMargin;
                                break;
                            case Gravity.BOTTOM:
                                childTop = parentBottom - height - lp.bottomMargin;
                                break;
                            case Gravity.TOP:
                            default:
                                childTop = parentTop + lp.topMargin;
                                if (!(child instanceof ActionBar) && !isInPreviewMode()) {
                                    childTop += actionBar.getTop() + actionBar.getMeasuredHeight();
                                }
                        }

                        child.layout(childLeft, childTop, childLeft + width, childTop + height);
                    }
                }
            }

//            @Override
//            protected void drawList(Canvas blurCanvas, boolean top) {
//                for (int i = 0; i < recyclerListView.getChildCount(); i++) {
//                    View child = recyclerListView.getChildAt(i);
//                    if (child.getY() < AndroidUtilities.dp(100) && child.getVisibility() == View.VISIBLE) {
//                        int restore = blurCanvas.save();
//                        blurCanvas.translate(recyclerListView.getX() + child.getX(), getY() + recyclerListView.getY() + child.getY());
//                        child.draw(blurCanvas);
//                        blurCanvas.restoreToCount(restore);
//                    }
//                }
//            }


        };
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick(){
            @Override
            public void onItemClick(int id) {
                if(id == -1){
                    finishFragment();
                }
            }
        });
        ActionBarMenu menu = actionBar.createMenu();
//        menu.addItem(1, R.drawable.msg_discussion, LocaleController.getString("TopicViewAsMessages", R.string.TopicViewAsMessages));
//        searchItem = menu.addItem(0, R.drawable.ic_ab_search).setIsSearchField(true).setActionBarMenuItemSearchListener(new ActionBarMenuItem.ActionBarMenuItemSearchListener() {
//            @Override
//            public void onSearchExpand() {
////                animateToSearchView(true);
////                searchContainer.setSearchString("");
////                searchContainer.setAlpha(0);
////                searchContainer.emptyView.showProgress(true, false);
//            }
//
//            @Override
//            public void onSearchCollapse() {
//               // animateToSearchView(false);
//            }
//
//            @Override
//            public void onTextChanged(EditText editText) {
//                String text = editText.getText().toString();
//                //searchContainer.setSearchString(text);
//            }
//
//            @Override
//            public void onSearchFilterCleared(FiltersView.MediaFilterData filterData) {
//
//            }
//        });
//        searchItem.setSearchPaddingStart(56);
//        searchItem.setSearchFieldHint(LocaleController.getString("Search", R.string.Search));
//        EditTextBoldCursor editText = searchItem.getSearchField();
//        editText.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
//        editText.setHintTextColor(Theme.getColor(Theme.key_player_time));
//        editText.setCursorColor(Theme.getColor(Theme.key_chat_messagePanelCursor));


        avatarContainer = new ChatAvatarContainer(context, this, false);
        //avatarContainer.getAvatarImageView().setRoundRadius(AndroidUtilities.dp(16));
        avatarContainer.setOccupyStatusBar(!AndroidUtilities.isTablet() && !inPreviewMode);
        AvatarDrawable avatarDrawable = new AvatarDrawable();
        avatarDrawable.setAvatarType(AvatarDrawable.AVATAR_TYPE_LIVE_SCORE);
        avatarContainer.setTitle("Games");
        avatarContainer.getAvatarImageView().setImage(null,null,avatarDrawable);
        actionBar.addView(avatarContainer, 0, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT, 56, 0, 86, 0));
        avatarContainer.setTitleColors(Theme.getColor(Theme.key_player_actionBarTitle), Theme.getColor(Theme.key_player_actionBarSubtitle));

        actionBar.setItemsColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2), false);
        actionBar.setItemsBackgroundColor(Theme.getColor(Theme.key_actionBarActionModeDefaultSelector), false);
        actionBar.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));


        recyclerListView = new BlurredRecyclerView(context);
        recyclerListView.setPadding(0, 0, 0, AndroidUtilities.dp(3));

        layoutManager = new LinearLayoutManager(context);
        recyclerListView.setLayoutManager(layoutManager);
        frameLayout.addView(loadingView = new FlickerLoadingView(context) {
            @Override
            public int getColumnsCount() {
                return 2;
            }
        });
        frameLayout.addView(recyclerListView);

        recyclerListView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    // AndroidUtilities.hideKeyboard(parentActivity.getCurrentFocus());
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (recyclerView.getAdapter() == null || bookmarkAdapter == null) {
                    return;
                }
                int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
                int lastVisibleItem = layoutManager.findLastVisibleItemPosition();
                int visibleItemCount = Math.abs(lastVisibleItem - firstVisibleItem) + 1;
                int totalItemCount = recyclerView.getAdapter().getItemCount();
                if (!isLoading && visibleItemCount > 0 && lastVisibleItem >= totalItemCount - 10 && !endReached) {
                    AndroidUtilities.runOnUIThread(() -> {
                        search(currentSearchDialogId, currentSearchMinDate, currentSearchMaxDate, currentSearchFilter, currentIncludeFolder, lastMessagesSearchString, false);
                    });
                }
//
//                if (recyclerView.getAdapter() == bookmarkAdapter) {
//                    if (dy != 0 && !messages.isEmpty() && TextUtils.isEmpty(currentDataQuery)) {
//                        showFloatingDateView();
//                    }
//                    RecyclerListView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(firstVisibleItem);
//                    if (holder != null && holder.getItemViewType() == 0) {
//                        if (holder.itemView instanceof SharedPhotoVideoCell) {
//                            SharedPhotoVideoCell cell = (SharedPhotoVideoCell) holder.itemView;
//                            MessageObject messageObject = cell.getMessageObject(0);
//                            if (messageObject != null) {
//                                floatingDateView.setCustomDate(messageObject.messageOwner.date, false, true);
//                            }
//                        }
//                    }
//                }
            }
        });
        recyclerListView.setAdapter(bookmarkAdapter = new ThemeAdapter(context));
        recyclerListView.setVerticalScrollBarEnabled(true);

        floatingDateView = new ChatActionCell(context);
        floatingDateView.setCustomDate((int) (System.currentTimeMillis() / 1000), false, false);
        floatingDateView.setAlpha(0.0f);
        floatingDateView.setOverrideColor(Theme.key_chat_mediaTimeBackground, Theme.key_chat_mediaTimeText);
        floatingDateView.setTranslationY(-AndroidUtilities.dp(48));
        frameLayout.addView(floatingDateView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 4, 0, 0));

        emptyView = new StickerEmptyView(context, loadingView, StickerEmptyView.STICKER_TYPE_NO_CONTACTS);
        frameLayout.addView(emptyView);
        emptyView.title.setText("Empty Games!");
        recyclerListView.setEmptyView(emptyView);

        FrameLayout.LayoutParams layoutParams = LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT);
        if (inPreviewMode && Build.VERSION.SDK_INT >= 21) {
            layoutParams.topMargin = AndroidUtilities.statusBarHeight;
        }
        if (!isInPreviewMode()) {
            frameLayout.addView(actionBar, layoutParams);
        }
        TLObject object =getMessagesController().getUserOrChat(game_channel);
        if(object instanceof TLRPC.Chat){
            TLRPC.Chat chat =(TLRPC.Chat)object;
            themeDialogId =  -chat.id;
            loadThemes(chat);
        }else{
            checkThemeChannel();
        }
        emptyView.showProgress(true, false);
        AnalyticsInstance.getInstance().trackEvent(AnalyticsInstance.EVENT_GAME_OPEND);
        return fragmentView;
    }


    private void loadThemes(TLRPC.Chat chat){
        if(chat == null){
            return;
        }
        FiltersView.MediaFilterData filter= new FiltersView.MediaFilterData(R.drawable.search_media_filled, LocaleController.getString("SharedMediaTab2", R.string.SharedMediaTab2), new TLRPC.TL_inputMessagesFilterPhotos(), 1);
        search(themeDialogId,0,0, filter,false,q,true);
    }

    private boolean resolving;
    private void checkThemeChannel(){
        if(resolving){
            return;
        }
        resolving = true;
        TLRPC.TL_contacts_resolveUsername req = new TLRPC.TL_contacts_resolveUsername();
        req.username = game_channel;
        int id =  getConnectionsManager().sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                resolving = false;
                if(error == null){
                    TLRPC.TL_contacts_resolvedPeer res = (TLRPC.TL_contacts_resolvedPeer) response;
                    getMessagesController().putUsers(res.users, false);
                    getMessagesController().putChats(res.chats, false);
                    getMessagesStorage().putUsersAndChats(res.users, res.chats, false, true);
                    if(res.chats != null && !res.chats.isEmpty()){
                        TLRPC.Chat chat = res.chats.get(0);
                        themeDialogId =  -chat.id;
                        loadThemes(chat);
                    }
                }else {

                }
            }
        }));
        getConnectionsManager().bindRequestToGuid(id,getClassGuid());

    }



    private void onItemClick(int index, View view, MessageObject mob, int b) {
        if (mob == null) {
            return;
        }
        if (uiCallback.actionModeShowing()) {
            uiCallback.toggleItemSelection(mob, view, b);
            return;
        }
       MessageObject messageObject1 = messages.get(index);
        if(messageObject1 != null){
            String title =null;
            String link = null;
            String desc = null;
            boolean hasadds = true;
            ArrayList<String> tags =  new ArrayList<>();
            String message = messageObject1.messageOwner.message;
            if(message != null){
                ArrayList<TLRPC.MessageEntity> entities = messageObject1.messageOwner.entities;
                for(int a = 0; a < entities.size();a++){
                    TLRPC.MessageEntity entity = entities.get(a);
                    if(entity instanceof TLRPC.TL_messageEntityTextUrl){
                        TLRPC.TL_messageEntityTextUrl urltext = (TLRPC.TL_messageEntityTextUrl)entity;
                        String url = entity.url;
                        if(url != null){
                            link = url;
                            title = message.substring(urltext.offset,urltext.offset + urltext.length);
                        }
                    }else if(entity instanceof TLRPC.TL_messageEntityHashtag){
                        TLRPC.TL_messageEntityHashtag hashtag = (TLRPC.TL_messageEntityHashtag)entity;
                        String hashTag = message.substring(hashtag.offset,hashtag.offset + hashtag.length);
                        if(!tags.contains(hashTag)){
                            tags.add(hashTag);
                        }
                        if(hashTag.contains("ad_free")){
                            hasadds = false;
                        }
                    }else if(entity instanceof TLRPC.TL_messageEntityItalic){
                        TLRPC.TL_messageEntityItalic tag = (TLRPC.TL_messageEntityItalic)entity;
                        desc = message.substring(tag.offset,tag.offset + tag.length);
                    }
                }
            }

            if( !TextUtils.isEmpty(link)){
                GamesMessageDetailAlert themeMessageDetailAlert = new GamesMessageDetailAlert(GameFragment.this,messageObject1,title,link,desc,tags,hasadds);
                showDialog(themeMessageDetailAlert);
            }
        }


//        if(view instanceof GameCell){
//            GameCell gameCell = (GameCell)view;
//            for (int i = 0; i < 6; i++) {
//                MessageObject message = gameCell.getMessageObject(i);
//                if (message == null) {
//                    break;
//                }
//                if (message.getId() == messageObject.getId()) {
//                    String title = gameCell.getView(i).getTitle();
//                    String link = gameCell.getView(i).getGameLink();
//                    String desc = gameCell.getView(i).getDesc();
//                    boolean hasadds = gameCell.getView(i).isHasAdds();
//
//                    ArrayList<String> tags = gameCell.getView(i).getHashTags();
//                    if( !TextUtils.isEmpty(link)){
//                        GamesMessageDetailAlert themeMessageDetailAlert = new GamesMessageDetailAlert(GameFragment.this,messageObject,title,link,desc,tags,hasadds);
//                        showDialog(themeMessageDetailAlert);
//                    }
//                }
//            }
//        }

    }


    @Override
    public void onResume() {
        super.onResume();
        if (bookmarkAdapter != null) {
            bookmarkAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.emojiLoaded) {
            int n = recyclerListView.getChildCount();
            for (int i = 0; i < n; i++) {
                if (recyclerListView.getChildAt(i) instanceof DialogCell) {
                    ((DialogCell) recyclerListView.getChildAt(i)).update(0);
                }
                recyclerListView.getChildAt(i).invalidate();
            }
        }
    }



    public void search(long dialogId, long minDate, long maxDate, FiltersView.MediaFilterData currentSearchFilter, boolean includeFolder, String query, boolean clearOldResults) {
        String currentSearchFilterQueryString = String.format(Locale.ENGLISH, "%d%d%d%d%s%s", dialogId, minDate, maxDate, currentSearchFilter == null ? -1 : currentSearchFilter.filterType, query, includeFolder);
        boolean filterAndQueryIsSame = lastSearchFilterQueryString != null && lastSearchFilterQueryString.equals(currentSearchFilterQueryString);
        boolean forceClear = !filterAndQueryIsSame && clearOldResults;
        this.currentSearchFilter = currentSearchFilter;
        this.currentSearchDialogId = dialogId;
        this.currentSearchMinDate = minDate;
        this.currentSearchMaxDate = maxDate;
        this.currentSearchString = query;
        this.currentIncludeFolder = includeFolder;
        if (searchRunnable != null) {
            AndroidUtilities.cancelRunOnUIThread(searchRunnable);
        }
        AndroidUtilities.cancelRunOnUIThread(clearCurrentResultsRunnable);
        if (filterAndQueryIsSame && clearOldResults) {
            return;
        }
        if (forceClear || currentSearchFilter == null && dialogId == 0 && minDate == 0 && maxDate == 0) {
            messages.clear();
            sections.clear();
            sectionArrays.clear();
            isLoading = true;
            emptyView.setVisibility(View.VISIBLE);
            if (bookmarkAdapter != null) {
                bookmarkAdapter.notifyDataSetChanged();
            }
            requestIndex++;
            firstLoading = true;
            if (recyclerListView.getPinnedHeader() != null) {
                recyclerListView.getPinnedHeader().setAlpha(0);
            }
            if (!forceClear) {
                return;
            }
        } else if (clearOldResults && !messages.isEmpty()) {
            return;
        }
        isLoading = true;
        if (bookmarkAdapter != null) {
            bookmarkAdapter.notifyDataSetChanged();
        }

        if (!filterAndQueryIsSame) {
            clearCurrentResultsRunnable.run();
            emptyView.showProgress(true, !clearOldResults);
        }

        requestIndex++;
        final int requestId = requestIndex;
        int currentAccount = UserConfig.selectedAccount;

        AndroidUtilities.runOnUIThread(searchRunnable = () -> {
            TLObject request;

            ArrayList<Object> resultArray = null;
            final TLRPC.TL_messages_search req = new TLRPC.TL_messages_search();
            req.q = query != null ? query : "";
            req.limit = 20;
            req.filter = currentSearchFilter == null ? new TLRPC.TL_inputMessagesFilterEmpty() : currentSearchFilter.filter;
            req.peer = AccountInstance.getInstance(currentAccount).getMessagesController().getInputPeer(dialogId);
            if (minDate > 0) {
                req.min_date = (int) (minDate / 1000);
            }
            if (maxDate > 0) {
                req.max_date = (int) (maxDate / 1000);
            }
            if (filterAndQueryIsSame && query.equals(lastMessagesSearchString) && !messages.isEmpty()) {
                MessageObject lastMessage = messages.get(messages.size() - 1);
                req.offset_id = lastMessage.getId();
            } else {
                req.offset_id = 0;
            }
//            if (filter_dialog_id != 0) {
//                Log.d("sechidialo","searching dialog for itelr dialog id" + filter_dialog_id);
//                req.from_id = AccountInstance.getInstance(currentAccount).getMessagesController().getInputPeer(filter_dialog_id);
//                req.flags |= 1;
//            }
            request = req;
            lastMessagesSearchString = query;
            lastSearchFilterQueryString = currentSearchFilterQueryString;

            ArrayList<Object> finalResultArray = resultArray;
            final ArrayList<FiltersView.DateData> dateData = new ArrayList<>();
            FiltersView.fillTipDates(lastMessagesSearchString, dateData);
            ConnectionsManager.getInstance(currentAccount).sendRequest(request, (response, error) -> {
                ArrayList<MessageObject> messageObjects = new ArrayList<>();
                if (error == null) {
                    TLRPC.messages_Messages res = (TLRPC.messages_Messages) response;
                    int n = res.messages.size();
                    for (int i = 0; i < n; i++) {
                        MessageObject messageObject = new MessageObject(currentAccount, res.messages.get(i), false, true);
                        messageObject.setQuery(query);
                        messageObjects.add(messageObject);
                    }
                }

                AndroidUtilities.runOnUIThread(() -> {
                    if (requestId != requestIndex) {
                        return;
                    }
                    isLoading = false;
                    if (error != null) {
                        emptyView.title.setText(LocaleController.getString("SearchEmptyViewTitle2", R.string.SearchEmptyViewTitle2));
                        emptyView.subtitle.setVisibility(View.VISIBLE);
                        emptyView.subtitle.setText(LocaleController.getString("SearchEmptyViewFilteredSubtitle2", R.string.SearchEmptyViewFilteredSubtitle2));
                        emptyView.showProgress(false, true);
                        return;
                    }

                    emptyView.showProgress(false);

                    TLRPC.messages_Messages res = (TLRPC.messages_Messages) response;
                    nextSearchRate = res.next_rate;
                    MessagesStorage.getInstance(currentAccount).putUsersAndChats(res.users, res.chats, true, true);
                    MessagesController.getInstance(currentAccount).putUsers(res.users, false);
                    MessagesController.getInstance(currentAccount).putChats(res.chats, false);
                    if (!filterAndQueryIsSame) {
                        messages.clear();
                        messagesById.clear();
                        sections.clear();
                        sectionArrays.clear();
                    }
                    totalCount = res.count;
                    currentDataQuery = query;
                    int n = messageObjects.size();
                    for (int i = 0; i < n; i++) {
                        MessageObject messageObject = messageObjects.get(i);
                        long channel_id =  messageObject.messageOwner.from_id.channel_id;
                        long chat_id =  messageObject.messageOwner.from_id.chat_id;
                        long user_id =  messageObject.messageOwner.from_id.user_id;

                        ArrayList<MessageObject> messageObjectsByDate = sectionArrays.get(messageObject.monthKey);
                        if (messageObjectsByDate == null) {
                            messageObjectsByDate = new ArrayList<>();
                            sectionArrays.put(messageObject.monthKey, messageObjectsByDate);
                            sections.add(messageObject.monthKey);
                        }
                        messageObjectsByDate.add(messageObject);
                        messages.add(messageObject);
                        messagesById.put(messageObject.getId(), messageObject);

                        if (PhotoViewer.getInstance().isVisible()) {
                            PhotoViewer.getInstance().addPhoto(messageObject, getClassGuid());
                        }
                    }
                    if (messages.size() > totalCount) {
                        totalCount = messages.size();
                    }
                    endReached = messages.size() >= totalCount;

                    if (messages.isEmpty()) {
                        if (currentSearchFilter != null) {
                            if (TextUtils.isEmpty(currentDataQuery) && dialogId == 0 && minDate == 0) {
                                emptyView.title.setText(LocaleController.getString("SearchEmptyViewTitle", R.string.SearchEmptyViewTitle));
                                String str;
                                if (currentSearchFilter.filterType == FiltersView.FILTER_TYPE_FILES) {
                                    str = LocaleController.getString("SearchEmptyViewFilteredSubtitleFiles", R.string.SearchEmptyViewFilteredSubtitleFiles);
                                } else if (currentSearchFilter.filterType == FiltersView.FILTER_TYPE_MEDIA) {
                                    str = LocaleController.getString("SearchEmptyViewFilteredSubtitleMedia", R.string.SearchEmptyViewFilteredSubtitleMedia);
                                } else if (currentSearchFilter.filterType == FiltersView.FILTER_TYPE_LINKS) {
                                    str = LocaleController.getString("SearchEmptyViewFilteredSubtitleLinks", R.string.SearchEmptyViewFilteredSubtitleLinks);
                                } else if (currentSearchFilter.filterType == FiltersView.FILTER_TYPE_MUSIC) {
                                    str = LocaleController.getString("SearchEmptyViewFilteredSubtitleMusic", R.string.SearchEmptyViewFilteredSubtitleMusic);
                                } else {
                                    str = LocaleController.getString("SearchEmptyViewFilteredSubtitleVoice", R.string.SearchEmptyViewFilteredSubtitleVoice);
                                }
                                emptyView.subtitle.setVisibility(View.VISIBLE);
                                emptyView.subtitle.setText(str);
                            } else {
                                emptyView.title.setText(LocaleController.getString("SearchEmptyViewTitle2", R.string.SearchEmptyViewTitle2));
                                emptyView.subtitle.setVisibility(View.VISIBLE);
                                emptyView.subtitle.setText(LocaleController.getString("SearchEmptyViewFilteredSubtitle2", R.string.SearchEmptyViewFilteredSubtitle2));
                            }
                        } else {
                            emptyView.title.setText(LocaleController.getString("SearchEmptyViewTitle2", R.string.SearchEmptyViewTitle2));
                            emptyView.subtitle.setVisibility(View.GONE);
                        }
                    }

                    if (recyclerListView.getAdapter() != bookmarkAdapter) {
                        recyclerListView.setAdapter(bookmarkAdapter);
                    }

                    firstLoading = false;
                    View progressView = null;
                    int progressViewPosition = -1;
                    for (int i = 0; i < n; i++) {
                        View child = recyclerListView.getChildAt(i);
                        if (child instanceof FlickerLoadingView) {
                            progressView = child;
                            progressViewPosition = recyclerListView.getChildAdapterPosition(child);
                        }
                    }
                    final View finalProgressView = progressView;
                    if (progressView != null) {
                        recyclerListView.removeView(progressView);
                    }
                    if ((loadingView.getVisibility() == View.VISIBLE && recyclerListView.getChildCount() == 0) ||  progressView != null) {
                        int finalProgressViewPosition = progressViewPosition;
                        fragmentView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                            @Override
                            public boolean onPreDraw() {
                                fragmentView.getViewTreeObserver().removeOnPreDrawListener(this);
                                int n = recyclerListView.getChildCount();
                                AnimatorSet animatorSet = new AnimatorSet();
                                for (int i = 0; i < n; i++) {
                                    View child = recyclerListView.getChildAt(i);
                                    if (finalProgressView != null) {
                                        if (recyclerListView.getChildAdapterPosition(child) < finalProgressViewPosition) {
                                            continue;
                                        }
                                    }
                                    child.setAlpha(0);
                                    int s = Math.min(recyclerListView.getMeasuredHeight(), Math.max(0, child.getTop()));
                                    int delay = (int) ((s / (float) recyclerListView.getMeasuredHeight()) * 100);
                                    ObjectAnimator a = ObjectAnimator.ofFloat(child, View.ALPHA, 0, 1f);
                                    a.setStartDelay(delay);
                                    a.setDuration(200);
                                    animatorSet.playTogether(a);
                                }
                                animatorSet.addListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        NotificationCenter.getInstance(currentAccount).onAnimationFinish(animationIndex);
                                    }
                                });
                                animationIndex = NotificationCenter.getInstance(currentAccount).setAnimationInProgress(animationIndex, null);
                                animatorSet.start();

                                if (finalProgressView != null && finalProgressView.getParent() == null) {
                                    recyclerListView.addView(finalProgressView);
                                    RecyclerView.LayoutManager layoutManager = recyclerListView.getLayoutManager();
                                    if (layoutManager != null) {
                                        layoutManager.ignoreView(finalProgressView);
                                        Animator animator = ObjectAnimator.ofFloat(finalProgressView, View.ALPHA, finalProgressView.getAlpha(), 0);
                                        animator.addListener(new AnimatorListenerAdapter() {
                                            @Override
                                            public void onAnimationEnd(Animator animation) {
                                                finalProgressView.setAlpha(1f);
                                                layoutManager.stopIgnoringView(finalProgressView);
                                                recyclerListView.removeView(finalProgressView);
                                            }
                                        });
                                        animator.start();
                                    }
                                }
                                return true;
                            }
                        });
                    }
                    bookmarkAdapter.notifyDataSetChanged();
                    if(avatarContainer != null){
                        avatarContainer.setSubtitle(String.format(Locale.US,"%s games",bookmarkAdapter.getItemCount()));
                    }
                });
            });
        }, (filterAndQueryIsSame && !messages.isEmpty()) ? 0 : 350);
        loadingView.setViewType(FlickerLoadingView.PHOTOS_TYPE);
    }
    private boolean onItemLongClick(MessageObject item, View view, int a) {
        if (!uiCallback.actionModeShowing()) {
            uiCallback.showActionMode();
        }
        if (uiCallback.actionModeShowing()) {
            uiCallback.toggleItemSelection(item, view, a);
        }
        return true;
    }

    int columnsCount = 2;
    private class ThemeAdapter extends RecyclerListView.SelectionAdapter {

        private Context mContext;

        public ThemeAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getItemCount() {
            if (messages.isEmpty()) {
                return 0;
            }
            return (int) Math.ceil(messages.size() / (float) columnsCount) +  (endReached ? 0 : 1);
        }


        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return false;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case 0:
                    view = new GameCell(mContext, GameCell.VIEW_TYPE_GLOBAL_SEARCH);
                    GameCell cell = (GameCell) view;
                    cell.setDelegate(new GameCell.VideoCellDelegate() {
                        @Override
                        public void didClickItem(GameCell cell, int index, MessageObject messageObject, int a) {

                            onItemClick(index, cell, messageObject, a);
                        }

                        @Override
                        public boolean didLongClickItem(GameCell cell, int index, MessageObject messageObject, int a) {
                            if (uiCallback.actionModeShowing()) {
                                didClickItem(cell, index, messageObject, a);
                                return true;
                            }
                            return onItemLongClick(messageObject, cell, a);
                        }
                    });
                    break;
                case 2:
                    view = new GraySectionCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_graySection) & 0xf2ffffff);
                    break;
                case 4:
                    RecyclerListView horizontalListView = new RecyclerListView(mContext) {
                        @Override
                        public boolean onInterceptTouchEvent(MotionEvent e) {
                            if (getParent() != null && getParent().getParent() != null) {
                                getParent().getParent().requestDisallowInterceptTouchEvent(canScrollHorizontally(-1) || canScrollHorizontally(1));
                            }
                            return super.onInterceptTouchEvent(e);
                        }
                    };
                    horizontalListView.setSelectorDrawableColor(Theme.getColor(Theme.key_listSelector));
                    horizontalListView.setTag(9);
                    horizontalListView.setItemAnimator(null);
                    horizontalListView.setLayoutAnimation(null);
                    LinearLayoutManager layoutManager = new LinearLayoutManager(mContext) {
                        @Override
                        public boolean supportsPredictiveItemAnimations() {
                            return false;
                        }
                    };
                    layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                    horizontalListView.setLayoutManager(layoutManager);
                    //horizontalListView.setDisallowInterceptTouchEvents(true);
                    horizontalListView.setAdapter(new RecentGameAdapter(mContext, currentAccount, false));
                    horizontalListView.setOnItemClickListener((view1, position) -> {
//                        if(recentGames.size() > 0 && position >= 0 && position < recentGames.size()){
//                            TableModels.GameModel gameModel = recentGames.get(position);
//                            Browser.openUrl(getContext(),gameModel.link);
//                            addGameToRecent(gameModel);
//                        }

//                        if (delegate != null) {
//                            delegate.didPressedOnSubDialog((Long) view1.getTag());
//                        }
                    });
                    horizontalListView.setOnItemLongClickListener((view12, position) -> {
//                        if (delegate != null) {
//                            delegate.needRemoveHint((Long) view12.getTag());
//                        }
                        return true;
                    });
                    view = horizontalListView;
                    innerListView = horizontalListView;
                    break;
                case 5:
                    HeaderCell headerCell = new HeaderCell(getContext());
                    headerCell.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundWhite));
                    view  = headerCell;
                    break;
                case 6:
                    horizontalListView = new RecyclerListView(mContext) {
                        @Override
                        public boolean onInterceptTouchEvent(MotionEvent e) {
                            if (getParent() != null && getParent().getParent() != null) {
                                getParent().getParent().requestDisallowInterceptTouchEvent(canScrollHorizontally(-1) || canScrollHorizontally(1));
                            }
                            return super.onInterceptTouchEvent(e);
                        }
                    };
                    horizontalListView.setSelectorDrawableColor(Theme.getColor(Theme.key_listSelector));
                    horizontalListView.setTag(9);
                    horizontalListView.setItemAnimator(null);
                    horizontalListView.setLayoutAnimation(null);
                    layoutManager = new LinearLayoutManager(mContext) {
                        @Override
                        public boolean supportsPredictiveItemAnimations() {
                            return false;
                        }
                    };
                    layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                    horizontalListView.setLayoutManager(layoutManager);
                    //horizontalListView.setDisallowInterceptTouchEvents(true);
                    horizontalListView.setAdapter(contextGameAdapter = new ContextGameAdapter(mContext));
                    horizontalListView.setOnItemClickListener((view1, position) -> {
//                        if (delegate != null) {
//                            delegate.didPressedOnSubDialog((Long) view1.getTag());
//                        }
                    });
                    horizontalListView.setOnItemLongClickListener((view12, position) -> {
//                        if (delegate != null) {
//                            delegate.needRemoveHint((Long) view12.getTag());
//                        }
                        return true;
                    });
                    view = horizontalListView;
                    innerListView2 = horizontalListView;
                    break;
                case 1:
                default:
                    FlickerLoadingView flickerLoadingView = new FlickerLoadingView(mContext) {
                        @Override
                        public int getColumnsCount() {
                            return columnsCount;
                        }
                    };
                    flickerLoadingView.setIsSingleCell(true);
                    flickerLoadingView.setViewType(FlickerLoadingView.PHOTOS_TYPE);
                    view = flickerLoadingView;
                    break;
            }
            view.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder.getItemViewType() == 0) {
                ArrayList<MessageObject> messageObjects = messages;
                GameCell cell = (GameCell) holder.itemView;
                cell.setItemsCount(columnsCount);
                cell.setIsFirst(position == 0);
                for (int a = 0; a < columnsCount; a++) {
                    int index = position * columnsCount + a;
                    if (index < messageObjects.size()) {
                        MessageObject messageObject = messageObjects.get(index);
                        cell.setItem(a, messages.indexOf(messageObject), messageObject);
                        if (uiCallback.actionModeShowing()) {
                            messageHashIdTmp.set(messageObject.getId(), messageObject.getDialogId());
                            cell.setChecked(a, uiCallback.isSelected(messageHashIdTmp), true);
                        } else {
                            cell.setChecked(a, false, true);
                        }
                    } else {
                        cell.setItem(a, index, null);
                    }
                }
                cell.requestLayout();
            } else if (holder.getItemViewType() == 3) {
                DialogCell cell = (DialogCell) holder.itemView;
                cell.useSeparator = (position != getItemCount() - 1);
                MessageObject messageObject = messages.get(position);
                boolean animated = cell.getMessage() != null && cell.getMessage().getId() == messageObject.getId();
                cell.setDialog(messageObject.getDialogId(), messageObject, messageObject.messageOwner.date, false,false);
                if (uiCallback.actionModeShowing()) {
                    messageHashIdTmp.set(messageObject.getId(), messageObject.getDialogId());
                    cell.setChecked(uiCallback.isSelected(messageHashIdTmp), animated);
                } else {
                    cell.setChecked(false, animated);
                }
            } else if (holder.getItemViewType() == 1) {
                FlickerLoadingView flickerLoadingView = (FlickerLoadingView) holder.itemView;
                int count = (int) Math.ceil(messages.size() / (float) columnsCount);
                flickerLoadingView.skipDrawItemsCount(columnsCount - (columnsCount * count - messages.size()));
            }else if(holder.getItemViewType() == 4){
                RecyclerListView recyclerListView = (RecyclerListView) holder.itemView;
                ((RecentGameAdapter) recyclerListView.getAdapter()).setIndex(position / 2);
            }else if(holder.getItemViewType() == 5){
                HeaderCell headerCell = (HeaderCell) holder.itemView;
                if(position == 0){
                    headerCell.setText("Recent Games");
                }else if(position == 2){
                    headerCell.setText("Play With Friends");

                }else if(position == 4){
                    headerCell.setText("More Games");

                }
            }
        }
        @Override
        public int getItemViewType(int position) {
            int count = (int) Math.ceil(messages.size() / (float) columnsCount);
            if (position < count) {
                return 0;
            }
            return 1;
        }

    }

//    private class ThemeAdapter extends RecyclerListView.SelectionAdapter {
//
//        private Context mContext;
//
//        public ThemeAdapter(Context context) {
//            mContext = context;
//        }
//
//        @Override
//        public int getItemCount() {
//            if (messages.isEmpty()) {
//                return 0;
//            }
//            int count = 0;
//            if(!recentGames.isEmpty()){
//                count +=2;
//            }
//            count +=(int) Math.ceil(messages.size() / (float) columnsCount) + +  (endReached ? 0 : 1);
//            return count  +  3;//plus
//        }
//
//        @Override
//        public boolean isEnabled(RecyclerView.ViewHolder holder) {
//            return false;
//        }
//
//        @Override
//        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//            View view;
//            switch (viewType) {
//                case 0:
//                    view = new GameCell(mContext, GameCell.VIEW_TYPE_GLOBAL_SEARCH);
//                    GameCell cell = (GameCell) view;
//                    cell.setDelegate(new GameCell.VideoCellDelegate() {
//                        @Override
//                        public void didClickItem(GameCell cell, int index, MessageObject messageObject, int a) {
//
//                            onItemClick(index, cell, messageObject, a);
//                        }
//
//                        @Override
//                        public boolean didLongClickItem(GameCell cell, int index, MessageObject messageObject, int a) {
//                            if (uiCallback.actionModeShowing()) {
//                                didClickItem(cell, index, messageObject, a);
//                                return true;
//                            }
//                            return onItemLongClick(messageObject, cell, a);
//                        }
//                    });
//                    break;
//                case 2:
//                    view = new GraySectionCell(mContext);
//                    view.setBackgroundColor(Theme.getColor(Theme.key_graySection) & 0xf2ffffff);
//                    break;
//                case 4:
//                    RecyclerListView horizontalListView = new RecyclerListView(mContext) {
//                        @Override
//                        public boolean onInterceptTouchEvent(MotionEvent e) {
//                            if (getParent() != null && getParent().getParent() != null) {
//                                getParent().getParent().requestDisallowInterceptTouchEvent(canScrollHorizontally(-1) || canScrollHorizontally(1));
//                            }
//                            return super.onInterceptTouchEvent(e);
//                        }
//                    };
//                    horizontalListView.setSelectorDrawableColor(Theme.getColor(Theme.key_listSelector));
//                    horizontalListView.setTag(9);
//                    horizontalListView.setItemAnimator(null);
//                    horizontalListView.setLayoutAnimation(null);
//                    LinearLayoutManager layoutManager = new LinearLayoutManager(mContext) {
//                        @Override
//                        public boolean supportsPredictiveItemAnimations() {
//                            return false;
//                        }
//                    };
//                    layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
//                    horizontalListView.setLayoutManager(layoutManager);
//                    //horizontalListView.setDisallowInterceptTouchEvents(true);
//                    horizontalListView.setAdapter(new RecentGameAdapter(mContext, currentAccount, false));
//                    horizontalListView.setOnItemClickListener((view1, position) -> {
//                        if(recentGames.size() > 0 && position >= 0 && position < recentGames.size()){
//                            TableModels.GameModel gameModel = recentGames.get(position);
//                            Browser.openUrl(getContext(),gameModel.link);
//                            addGameToRecent(gameModel);
//                        }
//
////                        if (delegate != null) {
////                            delegate.didPressedOnSubDialog((Long) view1.getTag());
////                        }
//                    });
//                    horizontalListView.setOnItemLongClickListener((view12, position) -> {
////                        if (delegate != null) {
////                            delegate.needRemoveHint((Long) view12.getTag());
////                        }
//                        return true;
//                    });
//                    view = horizontalListView;
//                    innerListView = horizontalListView;
//                    break;
//                case 5:
//                    HeaderCell headerCell = new HeaderCell(getContext());
//                    headerCell.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundWhite));
//                    view  = headerCell;
//                    break;
//                case 6:
//                    horizontalListView = new RecyclerListView(mContext) {
//                        @Override
//                        public boolean onInterceptTouchEvent(MotionEvent e) {
//                            if (getParent() != null && getParent().getParent() != null) {
//                                getParent().getParent().requestDisallowInterceptTouchEvent(canScrollHorizontally(-1) || canScrollHorizontally(1));
//                            }
//                            return super.onInterceptTouchEvent(e);
//                        }
//                    };
//                    horizontalListView.setSelectorDrawableColor(Theme.getColor(Theme.key_listSelector));
//                    horizontalListView.setTag(9);
//                    horizontalListView.setItemAnimator(null);
//                    horizontalListView.setLayoutAnimation(null);
//                    layoutManager = new LinearLayoutManager(mContext) {
//                        @Override
//                        public boolean supportsPredictiveItemAnimations() {
//                            return false;
//                        }
//                    };
//                    layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
//                    horizontalListView.setLayoutManager(layoutManager);
//                    //horizontalListView.setDisallowInterceptTouchEvents(true);
//                    horizontalListView.setAdapter(contextGameAdapter = new ContextGameAdapter(mContext));
//                    horizontalListView.setOnItemClickListener((view1, position) -> {
////                        if (delegate != null) {
////                            delegate.didPressedOnSubDialog((Long) view1.getTag());
////                        }
//                    });
//                    horizontalListView.setOnItemLongClickListener((view12, position) -> {
////                        if (delegate != null) {
////                            delegate.needRemoveHint((Long) view12.getTag());
////                        }
//                        return true;
//                    });
//                    view = horizontalListView;
//                    innerListView2 = horizontalListView;
//                    break;
//                case 1:
//                default:
//                    FlickerLoadingView flickerLoadingView = new FlickerLoadingView(mContext) {
//                        @Override
//                        public int getColumnsCount() {
//                            return columnsCount;
//                        }
//                    };
//                    flickerLoadingView.setIsSingleCell(true);
//                    flickerLoadingView.setViewType(FlickerLoadingView.PHOTOS_TYPE);
//                    view = flickerLoadingView;
//                    break;
//            }
//            view.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//            return new RecyclerListView.Holder(view);
//        }
//
//        @Override
//        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
//            if (holder.getItemViewType() == 0) {
//                ArrayList<MessageObject> messageObjects = messages;
//                GameCell cell = (GameCell) holder.itemView;
//                cell.setItemsCount(columnsCount);
//                position -=3;
//                if(!recentGames.isEmpty()){
//                    position-=2;
//                }
//                cell.setIsFirst(position == 0);
//                for (int a = 0; a < columnsCount; a++) {
//                    int index = position * columnsCount + a;
//                    if (index < messageObjects.size()) {
//                        MessageObject messageObject = messageObjects.get(index);
//                        cell.setItem(a, messages.indexOf(messageObject), messageObject);
//                        if (uiCallback.actionModeShowing()) {
//                            messageHashIdTmp.set(messageObject.getId(), messageObject.getDialogId());
//                            cell.setChecked(a, uiCallback.isSelected(messageHashIdTmp), true);
//                        } else {
//                            cell.setChecked(a, false, true);
//                        }
//                    } else {
//                        cell.setItem(a, index, null);
//                    }
//                }
//                cell.requestLayout();
//            } else if (holder.getItemViewType() == 3) {
//                DialogCell cell = (DialogCell) holder.itemView;
//                cell.useSeparator = (position != getItemCount() - 1);
//                MessageObject messageObject = messages.get(position);
//                boolean animated = cell.getMessage() != null && cell.getMessage().getId() == messageObject.getId();
//                cell.setDialog(messageObject.getDialogId(), messageObject, messageObject.messageOwner.date, false,false);
//                if (uiCallback.actionModeShowing()) {
//                    messageHashIdTmp.set(messageObject.getId(), messageObject.getDialogId());
//                    cell.setChecked(uiCallback.isSelected(messageHashIdTmp), animated);
//                } else {
//                    cell.setChecked(false, animated);
//                }
//            } else if (holder.getItemViewType() == 1) {
//                FlickerLoadingView flickerLoadingView = (FlickerLoadingView) holder.itemView;
//                int count = (int) Math.ceil(messages.size() / (float) columnsCount);
//                flickerLoadingView.skipDrawItemsCount(columnsCount - (columnsCount * count - messages.size()));
//            }else if(holder.getItemViewType() == 4){
//                RecyclerListView recyclerListView = (RecyclerListView) holder.itemView;
//                ((RecentGameAdapter) recyclerListView.getAdapter()).setIndex(position / 2);
//            }else if(holder.getItemViewType() == 5){
//                HeaderCell headerCell = (HeaderCell) holder.itemView;
//                if(position == 0){
//                    headerCell.setText("Recent Games");
//                }else if(position == 2){
//                    headerCell.setText("Play With Friends");
//
//                }else if(position == 4){
//                    headerCell.setText("More Games");
//
//                }
//            }
//        }
//
//        @Override
//        public int getItemViewType(int position) {
//            if(!recentGames.isEmpty() && !messages.isEmpty()){
//                if(position == 0){
//                    return 5;
//                }
//                if(position ==1){
//                    return 4;
//                }
//                position-=2;
//            }
//
//            if(position == 0 && !messages.isEmpty()){
//                return 5;
//            }else if(position == 1 && !messages.isEmpty()){
//                return 6;
//            }else if(position == 2 && !messages.isEmpty()){
//                return 5;
//            }
//            position-=3;
//            int count = (int) Math.ceil(messages.size() / (float) columnsCount);
//            if (position < count) {
//                return 0;
//            }
//            return 1;
//        }
//    }


    public void messagesDeleted(long channelId, ArrayList<Integer> markAsDeletedMessages) {
        boolean changed = false;
        for (int j = 0; j < messages.size(); j++) {
            MessageObject messageObject = messages.get(j);
            long dialogId = messageObject.getDialogId();
            int currentChannelId = dialogId < 0 && ChatObject.isChannel((int) -dialogId, UserConfig.selectedAccount) ? (int) -dialogId : 0;
            if (currentChannelId == channelId) {
                for (int i = 0; i < markAsDeletedMessages.size(); i++) {
                    if (messageObject.getId() == markAsDeletedMessages.get(i)) {
                        changed = true;
                        messages.remove(j);
                        messagesById.remove(messageObject.getId());

                        ArrayList<MessageObject> section = sectionArrays.get(messageObject.monthKey);
                        section.remove(messageObject);
                        if (section.size() == 0) {
                            sections.remove(messageObject.monthKey);
                            sectionArrays.remove(messageObject.monthKey);
                        }
                        j--;
                        totalCount--;
                    }
                }
            }
        }
        if (changed && bookmarkAdapter != null) {
            bookmarkAdapter.notifyDataSetChanged();
        }
    }


    private void hideFloatingDateView(boolean animated) {
        AndroidUtilities.cancelRunOnUIThread(hideFloatingDateRunnable);
        if (floatingDateView.getTag() == null) {
            return;
        }
        floatingDateView.setTag(null);
        if (floatingDateAnimation != null) {
            floatingDateAnimation.cancel();
            floatingDateAnimation = null;
        }
        if (animated) {
            floatingDateAnimation = new AnimatorSet();
            floatingDateAnimation.setDuration(180);
            floatingDateAnimation.playTogether(
                    ObjectAnimator.ofFloat(floatingDateView, View.ALPHA, 0.0f),
                    ObjectAnimator.ofFloat(floatingDateView, View.TRANSLATION_Y, -AndroidUtilities.dp(48)));
            floatingDateAnimation.setInterpolator(CubicBezierInterpolator.EASE_OUT);
            floatingDateAnimation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    floatingDateAnimation = null;
                }
            });
            floatingDateAnimation.start();
        } else {
            floatingDateView.setAlpha(0.0f);
        }
    }
    private void showFloatingDateView() {
        AndroidUtilities.cancelRunOnUIThread(hideFloatingDateRunnable);
        AndroidUtilities.runOnUIThread(hideFloatingDateRunnable, 650);
        if (floatingDateView.getTag() != null) {
            return;
        }
        if (floatingDateAnimation != null) {
            floatingDateAnimation.cancel();
        }
        floatingDateView.setTag(1);
        floatingDateAnimation = new AnimatorSet();
        floatingDateAnimation.setDuration(180);
        floatingDateAnimation.playTogether(
                ObjectAnimator.ofFloat(floatingDateView, View.ALPHA, 1.0f),
                ObjectAnimator.ofFloat(floatingDateView, View.TRANSLATION_Y, 0));
        floatingDateAnimation.setInterpolator(CubicBezierInterpolator.EASE_OUT);
        floatingDateAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                floatingDateAnimation = null;
            }
        });
        floatingDateAnimation.start();
    }

    public interface UiCallback {
        void goToMessage(MessageObject messageObject);

        boolean actionModeShowing();

        void toggleItemSelection(MessageObject item, View view, int a);

        boolean isSelected(MessageHashId messageHashId);

        void showActionMode();

        int getFolderId();
    }

    public static class MessageHashId {
        public long dialogId;
        public int messageId;

        public MessageHashId(int messageId, long dialogId) {
            this.dialogId = dialogId;
            this.messageId = messageId;
        }

        public void set(int messageId, long dialogId) {
            this.dialogId = dialogId;
            this.messageId = messageId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FilteredSearchView.MessageHashId that = (FilteredSearchView.MessageHashId) o;
            return dialogId == that.dialogId && messageId == that.messageId;
        }

        @Override
        public int hashCode() {
            return messageId;
        }
    }


    public interface Delegate {
        void onContextSearch(boolean searching);
    }

    public  class RecentGameAdapter extends RecyclerListView.SelectionAdapter {

        private final Context mContext;
        private final int currentAccount;
        private boolean drawChecked;
        private boolean forceDarkTheme;

        public RecentGameAdapter(Context context, int account, boolean drawChecked) {
            this.drawChecked = drawChecked;
            mContext = context;
            currentAccount = account;
        }

        public void setIndex(int value) {
            notifyDataSetChanged();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            HintDialogCell cell = new HintDialogCell(mContext, drawChecked);
            cell.setLayoutParams(new RecyclerView.LayoutParams(AndroidUtilities.dp(80), AndroidUtilities.dp(86)));
            return new RecyclerListView.Holder(cell);
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return true;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            HintDialogCell cell = (HintDialogCell) holder.itemView;
//            TableModels.GameModel gameModel =recentGames.get(position);
//            cell.setDialog(1, false, gameModel.name);
        }

        @Override
        public int getItemCount() {
            return  0;
        }
    }
    public String[] game_bots = {"gamee"};
    private int contextQueryReqid;
    private Runnable contextQueryRunnable;
    private TLRPC.User foundContextBot;
    private String searchingContextQuery;
    private String searchingContextUsername;
    private int contextUsernameReqid;
    private boolean loading;
    private String nextQueryOffset;
    private ArrayList<TLRPC.BotInlineResult> searchResultBotContext;
    private boolean contextMedia;
    private Runnable cancelDelayRunnable;

    private final Delegate delegate = searching -> {
        if(searching){

        }
    };;
    private void loadGames(){
        loading = true;
        for(int a = 0; a < game_bots.length;a++){
            searchForContextBot(game_bots[a], "");
        }
    }
    private void searchForContextBot(final String username, final String query) {
        if (foundContextBot != null && foundContextBot.username != null && foundContextBot.username.equals(username) && searchingContextQuery != null && searchingContextQuery.equals(query)) {
            return;
        }
        if (foundContextBot != null) {
            if (username != null && query != null) {
                return;
            }
        }
        if (contextQueryRunnable != null) {
            AndroidUtilities.cancelRunOnUIThread(contextQueryRunnable);
            contextQueryRunnable = null;
        }
        if (TextUtils.isEmpty(username) || searchingContextUsername != null && !searchingContextUsername.equals(username)) {
            if (contextUsernameReqid != 0) {
                ConnectionsManager.getInstance(currentAccount).cancelRequest(contextUsernameReqid, true);
                contextUsernameReqid = 0;
            }
            if (contextQueryReqid != 0) {
                ConnectionsManager.getInstance(currentAccount).cancelRequest(contextQueryReqid, true);
                contextQueryReqid = 0;
            }
            foundContextBot = null;
            searchingContextUsername = null;
            searchingContextQuery = null;

            if (delegate != null) {
                delegate.onContextSearch(false);
            }
            if (username == null || username.length() == 0) {
                return;
            }
        }
        if (query == null) {
            if (contextQueryReqid != 0) {
                ConnectionsManager.getInstance(currentAccount).cancelRequest(contextQueryReqid, true);
                contextQueryReqid = 0;
            }
            searchingContextQuery = null;
            if (delegate != null) {
                delegate.onContextSearch(false);
            }
            return;
        }
        if (delegate != null) {
            if (foundContextBot != null) {
                delegate.onContextSearch(true);
            }
        }
        final MessagesController messagesController = MessagesController.getInstance(currentAccount);
        final MessagesStorage messagesStorage = MessagesStorage.getInstance(currentAccount);
        searchingContextQuery = query;
        contextQueryRunnable = new Runnable() {
            @Override
            public void run() {
                if (contextQueryRunnable != this) {
                    return;
                }
                contextQueryRunnable = null;
                if (foundContextBot != null) {
                    searchForContextBotResults(true, foundContextBot, query, "");
                } else {
                    searchingContextUsername = username;
                    TLObject object = messagesController.getUserOrChat(searchingContextUsername);
                    if (object instanceof TLRPC.User) {
                        processFoundUser((TLRPC.User) object);
                    } else {
                        TLRPC.TL_contacts_resolveUsername req = new TLRPC.TL_contacts_resolveUsername();
                        req.username = searchingContextUsername;
                        contextUsernameReqid = ConnectionsManager.getInstance(currentAccount).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
                            if (searchingContextUsername == null || !searchingContextUsername.equals(username)) {
                                return;
                            }
                            TLRPC.User user = null;
                            if (error == null) {
                                TLRPC.TL_contacts_resolvedPeer res = (TLRPC.TL_contacts_resolvedPeer) response;
                                if (!res.users.isEmpty()) {
                                    user = res.users.get(0);
                                    messagesController.putUser(user, false);
                                    messagesStorage.putUsersAndChats(res.users, null, true, true);
                                }
                            }
                            processFoundUser(user);
                            contextUsernameReqid = 0;
                        }));
                    }
                }
            }
        };
        AndroidUtilities.runOnUIThread(contextQueryRunnable, 400);
    }

    private void processFoundUser(TLRPC.User user) {
        contextUsernameReqid = 0;
        if (user != null && user.bot && user.bot_inline_placeholder != null) {
            foundContextBot = user;
        } else {
            foundContextBot = null;
        }
        if (foundContextBot == null) {
        } else {
            if (delegate != null) {
                delegate.onContextSearch(true);
            }
            searchForContextBotResults(true, foundContextBot, searchingContextQuery, "");
        }
    }


    private void searchForContextBotResults(final boolean cache, final TLRPC.User user, final String query, final String offset) {
        if (contextQueryReqid != 0) {
            ConnectionsManager.getInstance(currentAccount).cancelRequest(contextQueryReqid, true);
            contextQueryReqid = 0;
        }
        if (query == null || user == null) {
            searchingContextQuery = null;
            return;
        }
        long dialog_id =  getUserConfig().getClientUserId();
        final String key = dialog_id + "_" + query + "_" + offset + "_" + dialog_id + "_" + user.id + "_" + ("");
        final MessagesStorage messagesStorage = MessagesStorage.getInstance(currentAccount);
        RequestDelegate requestDelegate = (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            if (!query.equals(searchingContextQuery)) {
                return;
            }
            contextQueryReqid = 0;
            if (cache && response == null) {
                searchForContextBotResults(false, user, query, offset);
            } else if (delegate != null) {
                delegate.onContextSearch(false);
            }
            if (response instanceof TLRPC.TL_messages_botResults) {
                TLRPC.TL_messages_botResults res = (TLRPC.TL_messages_botResults) response;
                if (!cache && res.cache_time != 0) {
                    messagesStorage.saveBotCache(key, res);
                }
                nextQueryOffset = res.next_offset;
                for (int a = 0; a < res.results.size(); a++) {
                    TLRPC.BotInlineResult result = res.results.get(a);
                    if (!(result.document instanceof TLRPC.TL_document) && !(result.photo instanceof TLRPC.TL_photo) && !"game".equals(result.type) && result.content == null && result.send_message instanceof TLRPC.TL_botInlineMessageMediaAuto) {
                        res.results.remove(a);
                        a--;
                    }
                    result.query_id = res.query_id;
                }
                boolean added = false;
                if (searchResultBotContext == null || offset.length() == 0) {
                    searchResultBotContext = res.results;
                    contextMedia = res.gallery;
                } else {
                    added = true;
                    searchResultBotContext.addAll(res.results);
                    if (res.results.isEmpty()) {
                        nextQueryOffset = "";
                    }
                }
                if (cancelDelayRunnable != null) {
                    AndroidUtilities.cancelRunOnUIThread(cancelDelayRunnable);
                    cancelDelayRunnable = null;
                }
                if(contextGameAdapter != null){
                    if (added) {
                        contextGameAdapter.notifyItemChanged(searchResultBotContext.size() - res.results.size() - 1);
                        contextGameAdapter.notifyItemRangeInserted(searchResultBotContext.size() - res.results.size(), res.results.size());
                    } else {
                        contextGameAdapter.notifyDataSetChanged();
                    }
                }

            }
        });

        if (cache) {
            messagesStorage.getBotCache(key, requestDelegate);
        } else {
            TLRPC.TL_messages_getInlineBotResults req = new TLRPC.TL_messages_getInlineBotResults();
            req.bot = MessagesController.getInstance(currentAccount).getInputUser(user);
            req.query = query;
            req.offset = offset;
            if (DialogObject.isEncryptedDialog(dialog_id)) {
                req.peer = new TLRPC.TL_inputPeerEmpty();
            } else {
                req.peer = MessagesController.getInstance(currentAccount).getInputPeer(dialog_id);
            }
            contextQueryReqid = ConnectionsManager.getInstance(currentAccount).sendRequest(req, requestDelegate, ConnectionsManager.RequestFlagFailOnServerErrors);
        }
    }


    private ContextGameAdapter contextGameAdapter;
    private class ContextGameAdapter extends RecyclerListView.SelectionAdapter{

        private Context context;


        public ContextGameAdapter(Context context){
            this.context = context;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            GameCell2 contextLinkCell = new GameCell2(context);
            contextLinkCell.setLayoutParams(new RecyclerView.LayoutParams(AndroidUtilities.dp(100), AndroidUtilities.dp(120)));
            return new RecyclerListView.Holder(contextLinkCell);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            GameCell2 contextLinkCell = (GameCell2) holder.itemView;
            TLRPC.BotInlineResult result = searchResultBotContext.get(position);
            contextLinkCell.setTag(result);
            contextLinkCell.setInlineResult(result,foundContextBot,true);
        }

        @Override
        public int getItemCount() {
            return searchResultBotContext != null?searchResultBotContext.size():0;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return true;
        }
    }

    @Override
    public ArrayList<ThemeDescription> getThemeDescriptions() {
        ArrayList<ThemeDescription> arrayList = new ArrayList<>();
        arrayList.add(new ThemeDescription(fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundWhite));
        arrayList.add(new ThemeDescription(fragmentView, 0, null, null, null, null, Theme.key_dialogBackground));
        arrayList.add(new ThemeDescription(fragmentView, 0, null, null, null, null, Theme.key_windowBackgroundGray));

        arrayList.add(new ThemeDescription(recyclerListView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{SharedDocumentCell.class}, new String[]{"nameTextView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        arrayList.add(new ThemeDescription(recyclerListView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{SharedDocumentCell.class}, new String[]{"dateTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText3));
        arrayList.add(new ThemeDescription(recyclerListView, ThemeDescription.FLAG_PROGRESSBAR, new Class[]{SharedDocumentCell.class}, new String[]{"progressView"}, null, null, null, Theme.key_sharedMedia_startStopLoadIcon));
        arrayList.add(new ThemeDescription(recyclerListView, ThemeDescription.FLAG_IMAGECOLOR, new Class[]{SharedDocumentCell.class}, new String[]{"statusImageView"}, null, null, null, Theme.key_sharedMedia_startStopLoadIcon));
        arrayList.add(new ThemeDescription(recyclerListView, ThemeDescription.FLAG_CHECKBOX, new Class[]{SharedDocumentCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_checkbox));
        arrayList.add(new ThemeDescription(recyclerListView, ThemeDescription.FLAG_CHECKBOXCHECK, new Class[]{SharedDocumentCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_checkboxCheck));
        arrayList.add(new ThemeDescription(recyclerListView, ThemeDescription.FLAG_IMAGECOLOR, new Class[]{SharedDocumentCell.class}, new String[]{"thumbImageView"}, null, null, null, Theme.key_files_folderIcon));
        arrayList.add(new ThemeDescription(recyclerListView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{SharedDocumentCell.class}, new String[]{"extTextView"}, null, null, null, Theme.key_files_iconText));

        arrayList.add(new ThemeDescription(recyclerListView, 0, new Class[]{LoadingCell.class}, new String[]{"progressBar"}, null, null, null, Theme.key_progressCircle));

        arrayList.add(new ThemeDescription(recyclerListView, ThemeDescription.FLAG_CHECKBOX, new Class[]{SharedAudioCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_checkbox));
        arrayList.add(new ThemeDescription(recyclerListView, ThemeDescription.FLAG_CHECKBOXCHECK, new Class[]{SharedAudioCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_checkboxCheck));
        arrayList.add(new ThemeDescription(recyclerListView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{SharedAudioCell.class}, Theme.chat_contextResult_titleTextPaint, null, null, Theme.key_windowBackgroundWhiteBlackText));
        arrayList.add(new ThemeDescription(recyclerListView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{SharedAudioCell.class}, Theme.chat_contextResult_descriptionTextPaint, null, null, Theme.key_windowBackgroundWhiteGrayText2));

        arrayList.add(new ThemeDescription(recyclerListView, ThemeDescription.FLAG_CHECKBOX, new Class[]{SharedLinkCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_checkbox));
        arrayList.add(new ThemeDescription(recyclerListView, ThemeDescription.FLAG_CHECKBOXCHECK, new Class[]{SharedLinkCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_checkboxCheck));
        arrayList.add(new ThemeDescription(recyclerListView, 0, new Class[]{SharedLinkCell.class}, new String[]{"titleTextPaint"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        arrayList.add(new ThemeDescription(recyclerListView, 0, new Class[]{SharedLinkCell.class}, null, null, null, Theme.key_windowBackgroundWhiteLinkText));
        arrayList.add(new ThemeDescription(recyclerListView, 0, new Class[]{SharedLinkCell.class}, Theme.linkSelectionPaint, null, null, Theme.key_windowBackgroundWhiteLinkSelection));
        arrayList.add(new ThemeDescription(recyclerListView, 0, new Class[]{SharedLinkCell.class}, new String[]{"letterDrawable"}, null, null, null, Theme.key_sharedMedia_linkPlaceholderText));
        arrayList.add(new ThemeDescription(recyclerListView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{SharedLinkCell.class}, new String[]{"letterDrawable"}, null, null, null, Theme.key_sharedMedia_linkPlaceholder));

        arrayList.add(new ThemeDescription(recyclerListView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR | ThemeDescription.FLAG_SECTIONS, new Class[]{SharedMediaSectionCell.class}, null, null, null, Theme.key_windowBackgroundWhite));
        arrayList.add(new ThemeDescription(recyclerListView, ThemeDescription.FLAG_SECTIONS, new Class[]{SharedMediaSectionCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        arrayList.add(new ThemeDescription(recyclerListView, 0, new Class[]{SharedMediaSectionCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));

        arrayList.add(new ThemeDescription(recyclerListView, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, null, Theme.avatarDrawables, null, Theme.key_avatar_text));
        arrayList.add(new ThemeDescription(recyclerListView, 0, new Class[]{DialogCell.class}, Theme.dialogs_countPaint, null, null, Theme.key_chats_unreadCounter));
        arrayList.add(new ThemeDescription(recyclerListView, 0, new Class[]{DialogCell.class}, Theme.dialogs_countGrayPaint, null, null, Theme.key_chats_unreadCounterMuted));
        arrayList.add(new ThemeDescription(recyclerListView, 0, new Class[]{DialogCell.class}, Theme.dialogs_countTextPaint, null, null, Theme.key_chats_unreadCounterText));
        arrayList.add(new ThemeDescription(recyclerListView, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, null, new Drawable[]{Theme.dialogs_lockDrawable}, null, Theme.key_chats_secretIcon));
        arrayList.add(new ThemeDescription(recyclerListView, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, null, new Drawable[]{Theme.dialogs_scamDrawable, Theme.dialogs_fakeDrawable}, null, Theme.key_chats_draft));
        arrayList.add(new ThemeDescription(recyclerListView, 0, new Class[]{DialogCell.class}, null, new Drawable[]{Theme.dialogs_pinnedDrawable, Theme.dialogs_reorderDrawable}, null, Theme.key_chats_pinnedIcon));
        arrayList.add(new ThemeDescription(recyclerListView, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, null, new Paint[]{Theme.dialogs_namePaint[0], Theme.dialogs_namePaint[1], Theme.dialogs_searchNamePaint}, null, null, Theme.key_chats_name));
        arrayList.add(new ThemeDescription(recyclerListView, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, null, new Paint[]{Theme.dialogs_nameEncryptedPaint[0], Theme.dialogs_nameEncryptedPaint[1], Theme.dialogs_searchNameEncryptedPaint}, null, null, Theme.key_chats_secretName));
        arrayList.add(new ThemeDescription(recyclerListView, 0, new Class[]{DialogCell.class}, Theme.dialogs_messagePaint[1], null, null, Theme.key_chats_message_threeLines));
        arrayList.add(new ThemeDescription(recyclerListView, 0, new Class[]{DialogCell.class}, Theme.dialogs_messagePaint[0], null, null, Theme.key_chats_message));
        arrayList.add(new ThemeDescription(recyclerListView, 0, new Class[]{DialogCell.class}, Theme.dialogs_messageNamePaint, null, null, Theme.key_chats_nameMessage_threeLines));
        arrayList.add(new ThemeDescription(recyclerListView, 0, new Class[]{DialogCell.class}, null, null, null, Theme.key_chats_draft));

        arrayList.add(new ThemeDescription(recyclerListView, 0, new Class[]{DialogCell.class}, null, Theme.dialogs_messagePrintingPaint, null, null, Theme.key_chats_actionMessage));
        arrayList.add(new ThemeDescription(recyclerListView, 0, new Class[]{DialogCell.class}, Theme.dialogs_timePaint, null, null, Theme.key_chats_date));
        arrayList.add(new ThemeDescription(recyclerListView, 0, new Class[]{DialogCell.class}, Theme.dialogs_pinnedPaint, null, null, Theme.key_chats_pinnedOverlay));
        arrayList.add(new ThemeDescription(recyclerListView, 0, new Class[]{DialogCell.class}, Theme.dialogs_tabletSeletedPaint, null, null, Theme.key_chats_tabletSelectedOverlay));
        arrayList.add(new ThemeDescription(recyclerListView, 0, new Class[]{DialogCell.class}, null, new Drawable[]{Theme.dialogs_checkDrawable}, null, Theme.key_chats_sentCheck));
        arrayList.add(new ThemeDescription(recyclerListView, 0, new Class[]{DialogCell.class}, null, new Drawable[]{Theme.dialogs_checkReadDrawable, Theme.dialogs_halfCheckDrawable}, null, Theme.key_chats_sentReadCheck));
        arrayList.add(new ThemeDescription(recyclerListView, 0, new Class[]{DialogCell.class}, null, new Drawable[]{Theme.dialogs_clockDrawable}, null, Theme.key_chats_sentClock));
        arrayList.add(new ThemeDescription(recyclerListView, 0, new Class[]{DialogCell.class}, Theme.dialogs_errorPaint, null, null, Theme.key_chats_sentError));
        arrayList.add(new ThemeDescription(recyclerListView, 0, new Class[]{DialogCell.class}, null, new Drawable[]{Theme.dialogs_errorDrawable}, null, Theme.key_chats_sentErrorIcon));
        arrayList.add(new ThemeDescription(recyclerListView, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, null, new Drawable[]{Theme.dialogs_verifiedCheckDrawable}, null, Theme.key_chats_verifiedCheck));
        arrayList.add(new ThemeDescription(recyclerListView, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, null, new Drawable[]{Theme.dialogs_verifiedDrawable}, null, Theme.key_chats_verifiedBackground));
        arrayList.add(new ThemeDescription(recyclerListView, 0, new Class[]{DialogCell.class}, null, new Drawable[]{Theme.dialogs_muteDrawable}, null, Theme.key_chats_muteIcon));
        arrayList.add(new ThemeDescription(recyclerListView, 0, new Class[]{DialogCell.class}, null, new Drawable[]{Theme.dialogs_mentionDrawable}, null, Theme.key_chats_mentionIcon));

        arrayList.add(new ThemeDescription(recyclerListView, 0, new Class[]{DialogCell.class}, null, null, null, Theme.key_chats_archivePinBackground));
        arrayList.add(new ThemeDescription(recyclerListView, 0, new Class[]{DialogCell.class}, null, null, null, Theme.key_chats_archiveBackground));

        arrayList.add(new ThemeDescription(recyclerListView, 0, new Class[]{DialogCell.class}, null, null, null, Theme.key_chats_onlineCircle));
        arrayList.add(new ThemeDescription(recyclerListView, 0, new Class[]{DialogCell.class}, null, null, null, Theme.key_windowBackgroundWhite));
        arrayList.add(new ThemeDescription(recyclerListView, ThemeDescription.FLAG_CHECKBOX, new Class[]{DialogCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_windowBackgroundWhite));
        arrayList.add(new ThemeDescription(recyclerListView, ThemeDescription.FLAG_CHECKBOXCHECK, new Class[]{DialogCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_checkboxCheck));

        arrayList.add(new ThemeDescription(recyclerListView, ThemeDescription.FLAG_SECTIONS, new Class[]{GraySectionCell.class}, new String[]{"textView"}, null, null, null, Theme.key_graySectionText));
        arrayList.add(new ThemeDescription(recyclerListView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR | ThemeDescription.FLAG_SECTIONS, new Class[]{GraySectionCell.class}, null, null, null, Theme.key_graySection));

        arrayList.add(new ThemeDescription(emptyView.title, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        arrayList.add(new ThemeDescription(emptyView.subtitle, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_windowBackgroundWhiteGrayText));


        return arrayList;
    }
}
