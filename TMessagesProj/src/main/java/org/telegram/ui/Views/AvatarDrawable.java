/*
 * This is the source code of Telegram for Android v. 1.7.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2014.
 */

package org.telegram.ui.Views;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import org.telegram.android.AndroidUtilities;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.R;
import org.telegram.messenger.TLRPC;
import org.telegram.ui.ApplicationLoader;

public class AvatarDrawable extends Drawable {

    private static Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static TextPaint namePaint;
    private static int[] arrColors = {0xffe56555, 0xfff28c48, 0xffeec764, 0xff76c84d, 0xff5fbed5, 0xff549cdd, 0xff8e85ee, 0xfff2749a};
    private static int[] arrColorsProfiles = {0xffd86f65, 0xffdc9663, 0xffdebc68, 0xff67b35d, 0xff56a2bb, 0xff5c98cd, 0xff8c79d2, 0xffda738e};
    private static int[] arrColorsProfilesBack = {0xffca6056, 0xffcf8550, 0xffcfa742, 0xff56a14c, 0xff4492ac, 0xff4c84b6, 0xff7d6ac4, 0xffc9637e};
    private static int[] arrColorsProfilesText = {0xfff9cbc5, 0xfffadbc4, 0xfff7e7bf, 0xffc0edba, 0xffb8e2f0, 0xffb3d7f7, 0xffcdc4ed, 0xfff2cfd8};
    private static int[] arrColorsButtons = {R.drawable.bar_selector_red, R.drawable.bar_selector_orange, R.drawable.bar_selector_yellow,
            R.drawable.bar_selector_green, R.drawable.bar_selector_cyan, R.drawable.bar_selector_blue, R.drawable.bar_selector_violet, R.drawable.bar_selector_pink};

    private static Drawable broadcastDrawable;
    private static Drawable photoDrawable;

    private int color;
    private StaticLayout textLayout;
    private float textWidth;
    private float textHeight;
    private boolean isProfile;
    private boolean drawBrodcast;
    private boolean drawPhoto;

    public AvatarDrawable() {
        super();

        if (namePaint == null) {
            namePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            namePaint.setColor(0xffffffff);
            namePaint.setTextSize(AndroidUtilities.dp(20));

            broadcastDrawable = ApplicationLoader.applicationContext.getResources().getDrawable(R.drawable.broadcast_w);
        }
    }

    public AvatarDrawable(TLRPC.User user) {
        this();
        if (user != null) {
            setInfo(user.id, user.first_name, user.last_name, false);
        }
    }

    public AvatarDrawable(TLRPC.Chat chat) {
        this();
        if (chat != null) {
            setInfo(chat.id, chat.title, null, chat.id < 0);
        }
    }

    public AvatarDrawable(TLRPC.User user, boolean profile) {
        this(user);
        isProfile = profile;
    }

    public AvatarDrawable(TLRPC.Chat chat, boolean profile) {
        this(chat);
        isProfile = profile;
    }

    public static int getColorForId(int id) {
        return arrColors[Math.abs(id) % arrColors.length];
    }

    public static int getButtonColorForId(int id) {
        return arrColorsButtons[Math.abs(id) % arrColorsButtons.length];
    }

    public static int getProfileColorForId(int id) {
        return arrColorsProfiles[Math.abs(id) % arrColorsProfiles.length];
    }

    public static int getProfileTextColorForId(int id) {
        return arrColorsProfilesText[Math.abs(id) % arrColorsProfilesText.length];
    }

    public static int getProfileBackColorForId(int id) {
        return arrColorsProfilesBack[Math.abs(id) % arrColorsProfilesBack.length];
    }

    public void setInfo(TLRPC.User user) {
        if (user != null) {
            setInfo(user.id, user.first_name, user.last_name, false);
        }
    }

    public void setInfo(TLRPC.Chat chat) {
        if (chat != null) {
            setInfo(chat.id, chat.title, null, chat.id < 0);
        }
    }

    public void setColor(int value) {
        color = value;
    }

    public void setInfo(int id, String firstName, String lastName, boolean isBroadcast) {
        if (isProfile) {
            color = arrColorsProfiles[Math.abs(id) % arrColorsProfiles.length];
        } else {
            color = arrColors[Math.abs(id) % arrColors.length];
        }

        drawBrodcast = isBroadcast;

        String text = "";
        if (firstName != null && firstName.length() > 0) {
            text += firstName.substring(0, 1);
        }
        if (lastName != null && lastName.length() > 0) {
            text += lastName.substring(0, 1);
        }
        if (text.length() > 0) {
            text = text.toUpperCase();
            try {
                textLayout = new StaticLayout(text, namePaint, AndroidUtilities.dp(100), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                if (textLayout.getLineCount() > 0) {
                    textWidth = textLayout.getLineWidth(0);
                    textHeight = textLayout.getLineBottom(0);
                }
            } catch (Exception e) {
                FileLog.e("tmessages", e);
            }
        } else {
            textLayout = null;
        }
    }

    public void setDrawPhoto(boolean value) {
        if (value && photoDrawable == null) {
            photoDrawable = ApplicationLoader.applicationContext.getResources().getDrawable(R.drawable.photo_w);
        }
        drawPhoto = value;
    }

    @Override
    public void draw(Canvas canvas) {
        Rect bounds = getBounds();
        if (bounds == null) {
            return;
        }
        int size = bounds.width();
        paint.setColor(color);
        canvas.save();
        canvas.translate(bounds.left, bounds.top);
        canvas.drawCircle(size / 2, size / 2, size / 2, paint);

        if (drawBrodcast && broadcastDrawable != null) {
            int x = (size - broadcastDrawable.getIntrinsicWidth()) / 2;
            int y = (size - broadcastDrawable.getIntrinsicHeight()) / 2;
            broadcastDrawable.setBounds(x, y, x + broadcastDrawable.getIntrinsicWidth(), y + broadcastDrawable.getIntrinsicHeight());
            broadcastDrawable.draw(canvas);
        } else {
            if (textLayout != null) {
                canvas.translate((size - textWidth) / 2, (size - textHeight) / 2);
                textLayout.draw(canvas);
            } else if (drawPhoto && photoDrawable != null) {
                int x = (size - photoDrawable.getIntrinsicWidth()) / 2;
                int y = (size - photoDrawable.getIntrinsicHeight()) / 2;
                photoDrawable.setBounds(x, y, x + photoDrawable.getIntrinsicWidth(), y + photoDrawable.getIntrinsicHeight());
                photoDrawable.draw(canvas);
            }
        }
        canvas.restore();
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(ColorFilter cf) {

    }

    @Override
    public int getOpacity() {
        return 0;
    }

    @Override
    public int getIntrinsicWidth() {
        return 0;
    }

    @Override
    public int getIntrinsicHeight() {
        return 0;
    }
}