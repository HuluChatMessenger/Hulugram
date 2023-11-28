package plus.theme;

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
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.browser.Browser;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RadialProgressView;

public class ThemeMessageDetailAlert extends BottomSheet {

    private MessageObject themeObject;

    private BaseFragment fragment;
    private String title;
    private TextView requestTextView;
    private RadialProgressView requestProgressView;
    private String themeLink;

    public ThemeMessageDetailAlert(BaseFragment baseFragment,MessageObject messageObject,String _title,String theme_link) {
        super(baseFragment.getParentActivity(), false);

        Context context = baseFragment.getContext();

        setApplyBottomPadding(false);
        setApplyTopPadding(false);
        fixNavigationBar(getThemedColor(Theme.key_windowBackgroundWhite));

        title = _title;
        themeLink = theme_link;
        fragment = baseFragment;
        themeObject = messageObject;

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

        String  about = messageObject.messageOwner.message;


        BackupImageView avatarImageView = new BackupImageView(context);
        avatarImageView.setRoundRadius(AndroidUtilities.dp(8));
        linearLayout.addView(avatarImageView, LayoutHelper.createLinear(100, 100, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 29, 0, 0));

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

        TextView textView = new TextView(context);
        textView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
        textView.setTextColor(getThemedColor(Theme.key_dialogTextBlack));
        textView.setText(title);
        textView.setSingleLine(true);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        linearLayout.addView(textView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 10, 9, 10, 20));

        boolean hasAbout = !TextUtils.isEmpty(about);

        if (hasAbout) {
            TextView aboutTextView = new TextView(context);
            aboutTextView.setGravity(Gravity.CENTER);
            aboutTextView.setText(about);
            aboutTextView.setTextColor(getThemedColor(Theme.key_dialogTextBlack));
            aboutTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
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
        requestTextView.setText("APPLY THEME");
        requestTextView.setTextColor(getThemedColor(Theme.key_featuredStickers_buttonText));
        requestTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        requestTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        requestTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Browser.openUrl(baseFragment.getParentActivity(),theme_link);
            }
        });
        requestFrameLayout.addView(requestTextView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48, Gravity.START, 16, 0, 16, 0));

    }
}
