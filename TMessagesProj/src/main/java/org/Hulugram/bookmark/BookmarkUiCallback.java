package plus.bookmark;

import android.view.View;

public interface BookmarkUiCallback {

        boolean actionModeShowing();

        void toggleItemSelection(Bookmark item, View view);

        boolean isSelected(long dialog_id);

        void showActionMode();
}