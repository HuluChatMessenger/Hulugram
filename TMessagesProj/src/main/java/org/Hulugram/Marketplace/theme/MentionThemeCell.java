/*
 * This is the source code of Telegram for Android v. 5.x.x
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package plus.theme;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.MediaDataController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AnimatedEmojiDrawable;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.LayoutHelper;

public class MentionThemeCell extends LinearLayout {

    private BackupImageView imageView;
    private TextView nameTextView;
    private AvatarDrawable avatarDrawable;
    private Theme.ResourcesProvider resourcesProvider;

    private Drawable emojiDrawable;

    public MentionThemeCell(Context context, Theme.ResourcesProvider resourcesProvider) {
        super(context);
        this.resourcesProvider = resourcesProvider;

        setOrientation(HORIZONTAL);

        avatarDrawable = new AvatarDrawable();
        avatarDrawable.setTextSize(AndroidUtilities.dp(12));

        imageView = new BackupImageView(context);
        imageView.setRoundRadius(AndroidUtilities.dp(12));
        addView(imageView, LayoutHelper.createLinear(24, 24, 8, 4, 0, 0));

        nameTextView = new TextView(context) {
            @Override
            public void setText(CharSequence text, BufferType type) {
                text = Emoji.replaceEmoji(text, getPaint().getFontMetricsInt(), false);
                super.setText(text, type);
            }
        };
        NotificationCenter.listenEmojiLoading(nameTextView);
        nameTextView.setTextColor(getThemedColor(Theme.key_windowBackgroundWhiteBlackText));
        nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        nameTextView.setSingleLine(true);
        nameTextView.setGravity(Gravity.LEFT);
        nameTextView.setEllipsize(TextUtils.TruncateAt.END);
        addView(nameTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL, 8, 0, 0, 0));


    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(32), MeasureSpec.EXACTLY));
    }

    public void setUser(TLRPC.User user) {
        resetEmojiSuggestion();
        if (user == null) {
            nameTextView.setText("");
            imageView.setImageDrawable(null);
            return;
        }
        avatarDrawable.setInfo(user);
        if (user.photo != null && user.photo.photo_small != null) {
            imageView.setForUserOrChat(user, avatarDrawable);
        } else {
            imageView.setImageDrawable(avatarDrawable);
        }
        nameTextView.setText(UserObject.getUserName(user));
        imageView.setVisibility(VISIBLE);
    }

    private boolean needsDivider = false;
    public void setDivider(boolean enabled) {
        if (enabled != needsDivider) {
            needsDivider = enabled;
            setWillNotDraw(!needsDivider);
            invalidate();
        }
    }



    public void setChat(TLRPC.Chat chat) {
        resetEmojiSuggestion();
        if (chat == null) {
            nameTextView.setText("");
            imageView.setImageDrawable(null);
            return;
        }
        avatarDrawable.setInfo(chat);
        if (chat.photo != null && chat.photo.photo_small != null) {
            imageView.setForUserOrChat(chat, avatarDrawable);
        } else {
            imageView.setImageDrawable(avatarDrawable);
        }
        nameTextView.setText(chat.title);
        imageView.setVisibility(VISIBLE);
    }

    public void setText(String text) {
        resetEmojiSuggestion();
        imageView.setVisibility(INVISIBLE);
        nameTextView.setText(text);
    }

    @Override
    public void invalidate() {
        super.invalidate();
        nameTextView.invalidate();
    }

    public void resetEmojiSuggestion() {
        nameTextView.setPadding(0, 0, 0, 0);
        if (emojiDrawable != null) {
            if (emojiDrawable instanceof AnimatedEmojiDrawable) {
                ((AnimatedEmojiDrawable) emojiDrawable).removeView(this);
            }
            emojiDrawable = null;
            invalidate();
        }
    }

    public void setEmojiSuggestion(MediaDataController.KeywordResult suggestion) {
        imageView.setVisibility(INVISIBLE);
        if (suggestion.emoji != null && suggestion.emoji.startsWith("animated_")) {
            try {
                if (emojiDrawable instanceof AnimatedEmojiDrawable) {
                    ((AnimatedEmojiDrawable) emojiDrawable).removeView(this);
                    emojiDrawable = null;
                }
                long documentId = Long.parseLong(suggestion.emoji.substring(9));
                emojiDrawable = AnimatedEmojiDrawable.make(UserConfig.selectedAccount, AnimatedEmojiDrawable.CACHE_TYPE_MESSAGES, documentId);
                if (attached) {
                    ((AnimatedEmojiDrawable) emojiDrawable).addView(this);
                }
            } catch (Exception ignore) {
                emojiDrawable = Emoji.getEmojiDrawable(suggestion.emoji);
            }
        } else {
            emojiDrawable = Emoji.getEmojiDrawable(suggestion.emoji);
        }
        if (emojiDrawable == null) {
            nameTextView.setPadding(0, 0, 0, 0);
            nameTextView.setText(new StringBuilder().append(suggestion.emoji).append(":  ").append(suggestion.keyword));
        } else {
            nameTextView.setPadding(AndroidUtilities.dp(22), 0, 0, 0);
            nameTextView.setText(new StringBuilder().append(":  ").append(suggestion.keyword));
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        if (emojiDrawable != null) {
            final int sz = AndroidUtilities.dp(emojiDrawable instanceof AnimatedEmojiDrawable ? 24 : 20);
            final int offsetX = AndroidUtilities.dp(emojiDrawable instanceof AnimatedEmojiDrawable ? -2 : 0);
            emojiDrawable.setBounds(
                nameTextView.getLeft() + offsetX,
                (nameTextView.getTop() + nameTextView.getBottom() - sz) / 2,
                nameTextView.getLeft() + offsetX + sz,
                (nameTextView.getTop() + nameTextView.getBottom() + sz) / 2
            );
            if (emojiDrawable instanceof AnimatedEmojiDrawable) {
                ((AnimatedEmojiDrawable) emojiDrawable).setTime(System.currentTimeMillis());
            }
            emojiDrawable.draw(canvas);
        }
    }

    public void setBotCommand(String command, String help, TLRPC.User user) {
        resetEmojiSuggestion();
        if (user != null) {
            imageView.setVisibility(VISIBLE);
            avatarDrawable.setInfo(user);
            if (user.photo != null && user.photo.photo_small != null) {
                imageView.setForUserOrChat(user, avatarDrawable);
            } else {
                imageView.setImageDrawable(avatarDrawable);
            }
        } else {
            imageView.setVisibility(INVISIBLE);
        }
        nameTextView.setText(command);
    }

    public void setIsDarkTheme(boolean isDarkTheme) {
        if (isDarkTheme) {
            nameTextView.setTextColor(0xffffffff);
        } else {
            nameTextView.setTextColor(getThemedColor(Theme.key_windowBackgroundWhiteBlackText));
        }
    }

    private int getThemedColor(int key) {
        Integer color = resourcesProvider != null ? resourcesProvider.getColor(key) : null;
        return color != null ? color : Theme.getColor(key);
    }

    private boolean attached;

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        attached = false;
        if (emojiDrawable instanceof AnimatedEmojiDrawable) {
            ((AnimatedEmojiDrawable) emojiDrawable).removeView(this);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        attached = true;
        if (emojiDrawable instanceof AnimatedEmojiDrawable) {
            ((AnimatedEmojiDrawable) emojiDrawable).addView(this);
        }
    }
}
