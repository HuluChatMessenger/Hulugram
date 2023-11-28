package plus.games;

import android.content.Context;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.widget.NestedScrollView;


import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.DownloadController;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.R;
import org.telegram.messenger.browser.Browser;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.UserCell;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RadialProgressView;

import java.util.ArrayList;

import plus.analyatics.AnalyticsInstance;
import plus.database.DataStorage;
import plus.database.TableModels;

public class GamesMessageDetailAlert extends BottomSheet {

    private MessageObject themeObject;

    private BaseFragment fragment;
    private String title;
    private TextView requestTextView;
    private RadialProgressView requestProgressView;
    private String themeLink;
    private String desc;

    private ArrayList<String> tags;

    public GamesMessageDetailAlert(BaseFragment baseFragment, MessageObject messageObject, String _title, String _theme_link, String _desc, ArrayList<String> hashTags,boolean hasAdds) {
        super(baseFragment.getParentActivity(), false);

        Context context = baseFragment.getContext();

        setApplyBottomPadding(false);
        setApplyTopPadding(false);
        fixNavigationBar(getThemedColor(Theme.key_windowBackgroundWhite));

        title = _title;
        themeLink = _theme_link;
        fragment = baseFragment;
        themeObject = messageObject;
        this.desc =_desc;
        this.tags =hashTags;

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setClickable(true);

        FrameLayout frameLayout = new FrameLayout(context);
        frameLayout.addView(linearLayout);

        NestedScrollView scrollView = new NestedScrollView(context);
        scrollView.addView(frameLayout);
        setCustomView(scrollView);


        ImageView closeView = new ImageView(context);
        closeView.setBackground(Theme.createSelectorDrawable(getThemedColor(Theme.key_listSelector)));
        closeView.setColorFilter(getThemedColor(Theme.key_sheet_other));
        closeView.setImageResource(R.drawable.ic_layer_close);
        closeView.setOnClickListener((view) -> dismiss());
        int closeViewPadding = AndroidUtilities.dp(8);
        closeView.setPadding(closeViewPadding, closeViewPadding, closeViewPadding, closeViewPadding);
        frameLayout.addView(closeView, LayoutHelper.createFrame(36, 36, Gravity.TOP | Gravity.END, 6, 8, 6, 0));

        UserCell userCell =new UserCell(context,2,0,false);
        userCell.getNameTextView().setText(title);
        if(tags != null && !tags.isEmpty()){
            userCell.getStatusTextView().setText(TextUtils.join(" ",tags));
        }
        BackupImageView avatarImageView = userCell.getAvatarImageView();
        TLRPC.PhotoSize currentPhotoObjectThumb = FileLoader.getClosestPhotoSizeWithSize(messageObject.photoThumbs, 50);
        TLRPC.PhotoSize currentPhotoObject = FileLoader.getClosestPhotoSizeWithSize(messageObject.photoThumbs, 320, false, currentPhotoObjectThumb, false);
        if (messageObject.mediaExists || DownloadController.getInstance(currentAccount).canDownloadMedia(messageObject)) {
            if (currentPhotoObject == currentPhotoObjectThumb) {
                currentPhotoObjectThumb = null;
            }
            if (messageObject.strippedThumb != null) {
                avatarImageView.getImageReceiver().setImage(ImageLocation.getForObject(currentPhotoObject, messageObject.photoThumbsObject), "100_100", null, null, messageObject.strippedThumb, currentPhotoObject != null ? currentPhotoObject.size : 0, null, messageObject, messageObject.shouldEncryptPhotoOrVideo() ? 2 : 1);
            } else {
                avatarImageView.getImageReceiver().setImage(ImageLocation.getForObject(currentPhotoObject, messageObject.photoThumbsObject), "100_100", ImageLocation.getForObject(currentPhotoObjectThumb, messageObject.photoThumbsObject), "b", currentPhotoObject != null ? currentPhotoObject.size : 0, null, messageObject, messageObject.shouldEncryptPhotoOrVideo() ? 2 : 1);
            }
        } else {
            if (messageObject.strippedThumb != null) {
                avatarImageView.setImage(null, null, null, null, messageObject.strippedThumb, null, null, 0, messageObject);
            } else {
                avatarImageView.setImage(null, null, ImageLocation.getForObject(currentPhotoObjectThumb, messageObject.photoThumbsObject), "b", ApplicationLoader.applicationContext.getResources().getDrawable(R.drawable.photo_placeholder_in), null, null, 0, messageObject);
            }
        }

        linearLayout.addView(userCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.LEFT, 0, 29, 0, 0));


//        BackupImageView avatarImageView = new BackupImageView(context);
//        avatarImageView.setRoundRadius(AndroidUtilities.dp(8));
//        //linearLayout.addView(avatarImageView, LayoutHelper.createLinear(100, 100, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 29, 0, 0));


        if(hasAdds){
            TextView textView = new TextView(context);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
            textView.setTextColor(getThemedColor(Theme.key_dialogTextBlack));
            textView.setMaxLines(3);
            textView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            textView.setText(AndroidUtilities.replaceTags("Note that any ads you see while playing this game are not from **Hulugram**, but rather from the **game's publishers**"));
            textView.setEllipsize(TextUtils.TruncateAt.END);
            linearLayout.addView(textView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP, 24, 10, 24, 20));

        }

        boolean hasAbout = !TextUtils.isEmpty(desc);

        if (hasAbout) {
            TextView aboutTextView = new TextView(context);
            aboutTextView.setGravity(Gravity.LEFT);
            aboutTextView.setText(desc);
            aboutTextView.setTextColor(getThemedColor(Theme.key_dialogTextGray));
            aboutTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            linearLayout.addView(aboutTextView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP, 24, 10, 24, 20));
        }

        FrameLayout requestFrameLayout = new FrameLayout(getContext());
        linearLayout.addView(requestFrameLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        requestProgressView = new RadialProgressView(getContext(), resourcesProvider);
        requestProgressView.setProgressColor(getThemedColor(Theme.key_featuredStickers_addButton));
        requestProgressView.setSize(AndroidUtilities.dp(32));
        requestProgressView.setVisibility(View.INVISIBLE);
        requestFrameLayout.addView(requestProgressView, LayoutHelper.createFrame(48, 48, Gravity.CENTER));

        requestTextView = new TextView(getContext());
        requestTextView.setBackground(Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(6), getThemedColor(Theme.key_featuredStickers_addButton), getThemedColor(Theme.key_featuredStickers_addButtonPressed)));
        requestTextView.setEllipsize(TextUtils.TruncateAt.END);
        requestTextView.setGravity(Gravity.CENTER);
        requestTextView.setSingleLine(true);
        requestTextView.setText("Play Game");
        requestTextView.setTextColor(getThemedColor(Theme.key_featuredStickers_buttonText));
        requestTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        requestTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        requestTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Browser.openUrl(baseFragment.getParentActivity(),themeLink);

                AnalyticsInstance.getInstance().trackEventGame(title);
//                TableModels.GameModel gameModel =new TableModels.GameModel();
//                gameModel.link = _theme_link;
//                gameModel.name  = title;
//                gameModel.photo_id = 1;
//                DataStorage.getInstance(currentAccount).getStorageQueue().postRunnable(new Runnable() {
//                    @Override
//                    public void run() {
//                        DataStorage.getInstance(currentAccount).getDatabase().gameDao().updateGameOrder(gameModel);
//                    }
//                });
            }
        });
        requestFrameLayout.addView(requestTextView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48, Gravity.START, 16, 0, 16, 0));

    }


    @Override
    public void dismiss() {

        title = null;
        themeLink =  null;
        themeObject = null;
        this.desc = null;
        this.tags = new ArrayList<>();

        super.dismiss();

    }
}
