package plus.bookmark;

import android.content.Context;
import android.content.DialogInterface;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.GroupCreateUserCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.NumberTextView;
import org.telegram.ui.Components.SizeNotifierFrameLayout;
import org.telegram.ui.Components.ViewPagerFixed;

import java.util.ArrayList;
import java.util.HashMap;

public class BookmarkViewPager extends ViewPagerFixed implements BookmarkUiCallback{

    private final ViewPagerAdapter viewPagerAdapter;

    private NumberTextView selectedMessagesCountTextView;
    private boolean isActionModeShowed;
    private HashMap<Long, Bookmark> selectedBookmarks = new HashMap<>();

    private final static String actionModeTag = "bookmark_view_pager";

    public final static int deleteItemId = 202;

    private ActionBarMenuItem deleteItem;

    int currentAccount = UserConfig.selectedAccount;
    BaseFragment parent;
    private int keyboardSize;

    private boolean showOnlyDialogsAdapter;

    SizeNotifierFrameLayout fragmentView;

    private final int folderId = 0;
    int animateFromCount = 0;
    public interface MediaPreloaderDelegate {
        void mediaCountUpdated();
    }
    private MediaCountLoader mediaPreloader;

    public static class MediaCountLoader {

        private int[] lastMediaCount = new int[]{-1, -1};

        private ArrayList<MediaPreloaderDelegate> delegates = new ArrayList<>();

        public MediaCountLoader() {
        }
        public void addDelegate(MediaPreloaderDelegate delegate) {
            delegates.add(delegate);
        }

        public void removeDelegate(MediaPreloaderDelegate delegate) {
            delegates.remove(delegate);
        }
        public void onDestroy(){
            delegates.clear();
        }

        public ArrayList<MediaPreloaderDelegate> getDelegates() {
            return delegates;
        }

        public int[] getLastMediaCount() {
            return lastMediaCount;
        }
    }
    private FrameLayout avatarContainer;

    public void setAvatarContainer(FrameLayout avatarContainer) {
        this.avatarContainer = avatarContainer;
    }

    public BookmarkViewPager(Context context, BookmarkActivity fragment, MediaCountLoader mediaCountLoader) {
        super(context);
        fragmentView = (SizeNotifierFrameLayout)fragment.getFragmentView();
        mediaPreloader = mediaCountLoader;
        parent = fragment;
        setAdapter(viewPagerAdapter = new ViewPagerAdapter());
    }



    public void showActionMode(boolean show) {
        if (isActionModeShowed == show) {
            return;
        }
        if (show && parent.getActionBar().isActionModeShowed()) {
            return;
        }
        if (show && !parent.getActionBar().actionModeIsExist(actionModeTag)) {
            ActionBarMenu actionMode = parent.getActionBar().createActionMode(true, actionModeTag);

            selectedMessagesCountTextView = new NumberTextView(actionMode.getContext());
            selectedMessagesCountTextView.setTextSize(18);
            selectedMessagesCountTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            selectedMessagesCountTextView.setTextColor(Theme.getColor(Theme.key_actionBarActionModeDefaultIcon));
            actionMode.addView(selectedMessagesCountTextView, LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1.0f, 72, 0, 0, 0));
            selectedMessagesCountTextView.setOnTouchListener((v, event) -> true);

            deleteItem = actionMode.addItemWithWidth(deleteItemId, R.drawable.msg_delete, AndroidUtilities.dp(54), LocaleController.getString("Delete", R.string.Delete));
        }

        if (parent.getActionBar().getBackButton().getDrawable() instanceof BackDrawable) {
            parent.getActionBar().setBackButtonDrawable(new BackDrawable(false));
        }
        isActionModeShowed = show;
        if (show) {
            AndroidUtilities.hideKeyboard(parent.getParentActivity().getCurrentFocus());
            parent.getActionBar().showActionMode();
            selectedMessagesCountTextView.setNumber(selectedBookmarks.size(), false);
            deleteItem.setVisibility(View.VISIBLE);
            if(avatarContainer != null){
                AndroidUtilities.updateViewVisibilityAnimated(avatarContainer,false);
            }
        } else {
            if(avatarContainer != null){
                AndroidUtilities.updateViewVisibilityAnimated(avatarContainer,true);
            }
            parent.getActionBar().hideActionMode();
            selectedBookmarks.clear();
            for (int i = 0; i < getChildCount(); i++) {
                if (getChildAt(i) instanceof BotBookmarkFilterLayout) {
                    ((BotBookmarkFilterLayout)getChildAt(i)).update();
                }
                if (getChildAt(i) instanceof ChannelBookmarkFilterLayout) {
                    ((ChannelBookmarkFilterLayout)getChildAt(i)).update();
                }
            }

            int n = viewsByType.size();
            for (int i = 0; i < n; i++) {
                View v = viewsByType.valueAt(i);
                if (v instanceof ChannelBookmarkFilterLayout) {
                    ((ChannelBookmarkFilterLayout) v).update();
                }

                if (getChildAt(i) instanceof BotBookmarkFilterLayout) {
                    ((BotBookmarkFilterLayout)getChildAt(i)).update();
                }
            }
        }
    }

    public void onActionBarItemClick(int id) {
        if (id == deleteItemId) {
            if (parent == null || parent.getParentActivity() == null) {
                return;
            }
            ArrayList<Bookmark> messageObjects = new ArrayList<>(selectedBookmarks.values());
            AlertDialog.Builder builder = new AlertDialog.Builder(parent.getParentActivity());
            builder.setTitle(LocaleController.formatPluralString("HuluRemoveBookmarksMessage", selectedBookmarks.size()));

            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
            spannableStringBuilder
                    .append(AndroidUtilities.replaceTags(LocaleController.formatPluralString("HuluRemoveBookmarksMessage", selectedBookmarks.size())))
                    .append("\n\n")
                    .append(LocaleController.getString("HuluRemoveBookmarkAlertMessage", R.string.HuluRemoveBookmarkAlertMessage));

            builder.setMessage(spannableStringBuilder);
            builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), (dialogInterface, i) -> dialogInterface.dismiss());
            builder.setPositiveButton(LocaleController.getString("Delete", R.string.Delete), (dialogInterface, i) -> {
                dialogInterface.dismiss();

                parent.getMessagesStorage().removeBookmarks(messageObjects);
                hideActionMode();
            });
            AlertDialog alertDialog = builder.show();
            TextView button = (TextView) alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            if (button != null) {
                button.setTextColor(Theme.getColor(Theme.key_color_red));
            }

        }
    }



    @Override
    public boolean actionModeShowing() {
        return isActionModeShowed;
    }




    public void hideActionMode() {
        showActionMode(false);
    }

    @Override
    public void toggleItemSelection(Bookmark bookmark, View view) {
        long hashId = bookmark.dialog_id;
        if (selectedBookmarks.containsKey(hashId)) {
            selectedBookmarks.remove(hashId);
        } else {
            selectedBookmarks.put(hashId, bookmark);
        }
        if (selectedBookmarks.size() == 0) {
            showActionMode(false);
        } else {
            selectedMessagesCountTextView.setNumber(selectedBookmarks.size(), true);
            if (deleteItem != null) {
                deleteItem.setVisibility(View.VISIBLE);
            }
        }
        if (view instanceof GroupCreateUserCell) {
            ((GroupCreateUserCell) view).setChecked(selectedBookmarks.containsKey(hashId), true);
        }
    }

    @Override
    public boolean isSelected(long dialogId) {
        return selectedBookmarks.containsKey(dialogId);
    }


    @Override
    public void showActionMode() {
        showActionMode(true);
    }

    @Override
    protected void onItemSelected(View currentPage, View oldPage, int position, int oldPosition) {
        onSelectedTabChanged();
    }

    public void getThemeDescriptions(ArrayList<ThemeDescription> arrayList) {


        for (int i = 0; i < getChildCount(); i++) {
            if (getChildAt(i) instanceof ChannelBookmarkFilterLayout) {
                arrayList.addAll(((ChannelBookmarkFilterLayout) getChildAt(i)).getThemeDescriptions());
            }
            if (getChildAt(i) instanceof BotBookmarkFilterLayout) {
                arrayList.addAll(((BotBookmarkFilterLayout) getChildAt(i)).getThemeDescriptions());
            }
        }

        int n = viewsByType.size();
        for (int i = 0; i < n; i++) {
            View v = viewsByType.valueAt(i);
            if (v instanceof ChannelBookmarkFilterLayout) {
                arrayList.addAll(((ChannelBookmarkFilterLayout) v).getThemeDescriptions());
            }
            if (v instanceof BotBookmarkFilterLayout) {
                arrayList.addAll(((BotBookmarkFilterLayout) v).getThemeDescriptions());
            }
        }



    }



    public void reset() {
        setPosition(0);
        viewsByType.clear();
    }

    public void setPosition(int position) {
        if (position < 0) {
            return;
        }
        super.setPosition(position);
        viewsByType.clear();
        if (super.getTabsView() != null) {
            super.getTabsView().selectTabWithId(position, 1f);
        }
        invalidate();
    }

    public void setKeyboardHeight(int keyboardSize) {
        this.keyboardSize = keyboardSize;
        boolean animated = getVisibility() == View.VISIBLE && getAlpha() > 0;
//        for (int i = 0; i < getChildCount(); i++) {
//            if (getChildAt(i) instanceof BotBookmarkFilterLayout) {
//                ((FilteredSearchView) getChildAt(i)).setKeyboardHeight(keyboardSize, animated);
//            }
//        }
    }

    public void showOnlyDialogsAdapter(boolean showOnlyDialogsAdapter) {
        this.showOnlyDialogsAdapter = showOnlyDialogsAdapter;
    }

    public void bookmarkDeleted(ArrayList<Long> markAsDeletedMessages) {
        int n = viewsByType.size();
        for (int i = 0; i < n; i++) {
            View v = viewsByType.valueAt(i);
            if (v instanceof BotBookmarkFilterLayout) {
                ((BotBookmarkFilterLayout) v).messagesDeleted(markAsDeletedMessages);
            }
            if (v instanceof ChannelBookmarkFilterLayout){
                ((ChannelBookmarkFilterLayout) v).messagesDeleted(markAsDeletedMessages);
            }
        }

        for (int i = 0; i < getChildCount(); i++) {
            if (getChildAt(i) instanceof BotBookmarkFilterLayout) {
                ((BotBookmarkFilterLayout)getChildAt(i)).messagesDeleted(markAsDeletedMessages);
            }
            if (getChildAt(i) instanceof ChannelBookmarkFilterLayout){
                ((ChannelBookmarkFilterLayout) getChildAt(i)).messagesDeleted(markAsDeletedMessages);
            }
        }
        if (!selectedBookmarks.isEmpty()) {
            ArrayList<Long> toRemove = null;
            ArrayList<Long> arrayList = new ArrayList<>(selectedBookmarks.keySet());
            for (int k = 0; k < arrayList.size(); k++) {
                Long hashId = arrayList.get(k);
                Bookmark messageObject = selectedBookmarks.get(hashId);
                if (messageObject != null) {
                    for (int i = 0; i < markAsDeletedMessages.size(); i++) {
                        if (messageObject.dialog_id == markAsDeletedMessages.get(i)) {
                            toRemove = new ArrayList<>();
                            toRemove.add(hashId);
                        }
                    }
                }
            }

            if (toRemove != null) {
                for (int a = 0, N = toRemove.size(); a < N; a++) {
                    selectedBookmarks.remove(toRemove.get(a));
                }
                selectedMessagesCountTextView.setNumber(selectedBookmarks.size(), true);
            }
        }
    }

    public TabsView getTabsView() {
        return super.getTabsView();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    protected void invalidateBlur() {
        fragmentView.invalidateBlur();
    }



    public final static int CHANNEL_TYPE = 0;
    public final static int BOT_TYPE = 1;


    private class ViewPagerAdapter extends Adapter {

        ArrayList<Item> items = new ArrayList<>();

        public ViewPagerAdapter() {
            items.add(new Item(CHANNEL_TYPE));
            items.add(new Item(BOT_TYPE));
        }

        @Override
        public String getItemTitle(int position) {
            if (items.get(position).type == CHANNEL_TYPE) {
                return "Chats";
            }
            return  "Users";
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        @Override
        public View createView(int viewType) {
           if (viewType == CHANNEL_TYPE) {
                ChannelBookmarkFilterLayout channelMusicFilterLayout = new ChannelBookmarkFilterLayout(parent,mediaPreloader);
               channelMusicFilterLayout.setBookmarkUiCallback(BookmarkViewPager.this);
               channelMusicFilterLayout.listView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        fragmentView.invalidateBlur();
                    }
                });
                return channelMusicFilterLayout;

            }else{
                BotBookmarkFilterLayout channelMusicFilterLayout = new BotBookmarkFilterLayout(parent,mediaPreloader);
                channelMusicFilterLayout.setBookmarkUiCallback(BookmarkViewPager.this);
                channelMusicFilterLayout.listView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        fragmentView.invalidateBlur();
                    }
                });
                return channelMusicFilterLayout;
            }
        }

        @Override
        public int getItemViewType(int position) {
            return items.get(position).type;
        }

        @Override
        public void bindView(View view, int position, int viewType) {
            if(viewType == CHANNEL_TYPE){
                ChannelBookmarkFilterLayout filterLayout = (ChannelBookmarkFilterLayout)view;
                filterLayout.loadChannelBookmark();
            }else if(viewType == BOT_TYPE){
                BotBookmarkFilterLayout filterLayout = (BotBookmarkFilterLayout)view;
                filterLayout.loadBotBookmark();
            }
        }

        private class Item {
            private final int type;
            private Item(int type) {
                this.type = type;
            }
        }
    }

    protected void onSelectedTabChanged() {

    }
}
