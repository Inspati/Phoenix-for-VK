package biz.dealnote.messenger.view;

import android.app.Activity;
import android.content.Context;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.listener.TextWatcherAdapter;
import biz.dealnote.messenger.settings.CurrentTheme;
import biz.dealnote.messenger.util.AppTextUtils;
import biz.dealnote.messenger.view.emoji.EmojiconsPopup;
import biz.dealnote.messenger.view.emoji.StickersGridView;

public class InputViewController {

    private static final String TAG = InputViewController.class.getSimpleName();

    private Context mActivity;
    private OnInputActionCallback callback;
    private EditText mInputField;
    private RelativeLayout rlEmojiContainer;
    private EmojiconsPopup emojiPopup;
    private ImageView ibEmoji;
    private ImageView ibAttach;
    private ViewGroup vgMessageInput;
    private ViewGroup vgVoiceInput;

    private boolean emojiOnScreen;
    private boolean emojiNeed;
    private boolean sendOnEnter;

    private TextView tvAttCount;

    private ImageView mButtonSend;

    private ImageView mRecordResumePause;

    private TextWatcherAdapter mTextWatcher;

    private int mIconColorActive;
    private int mIconColorInactive;
    private int mCurrentMode = Mode.NORMAL;
    private TextView mRecordingDuration;

    public InputViewController(@NonNull final Activity activity, @NonNull View rootView, @NonNull OnInputActionCallback callback) {
        this.callback = callback;
        this.mActivity = activity.getApplicationContext();

        mRecordingDuration = rootView.findViewById(R.id.recording_duration);

        mInputField = rootView.findViewById(R.id.fragment_input_text);
        mTextWatcher = new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                callback.onInputTextChanged(s.toString());
            }
        };

        vgMessageInput = rootView.findViewById(R.id.message_input_container);
        vgVoiceInput = rootView.findViewById(R.id.voice_input_container);

        mIconColorActive = CurrentTheme.getColorPrimary(activity);
        mIconColorInactive = CurrentTheme.getColorOnSurface(activity);

        mButtonSend = rootView.findViewById(R.id.buttonSend);
        mButtonSend.setOnClickListener(v -> onSendButtonClick());

        tvAttCount = rootView.findViewById(R.id.fragment_input_att_count);

        rlEmojiContainer = rootView.findViewById(R.id.fragment_input_emoji_container);

        ibAttach = rootView.findViewById(R.id.buttonAttach);
        ibEmoji = rootView.findViewById(R.id.buttonEmoji);

        ibAttach.setOnClickListener(view -> callback.onAttachClick());
        ibEmoji.setOnClickListener(view -> onEmojiButtonClick());

        mInputField.addTextChangedListener(mTextWatcher);

        mInputField.setOnClickListener(view -> showEmoji(false));

        mInputField.setOnKeyListener((v, keyCode, event) -> {
            if (sendOnEnter && event.getAction() == KeyEvent.ACTION_DOWN) {
                switch (keyCode) {
                    //case KeyEvent.KEYCODE_DPAD_CENTER:
                    case KeyEvent.KEYCODE_ENTER:
                        callback.onSendClicked(getTrimmedText());
                        return true;
                    default:
                        break;
                }
            }
            return false;
        });


        emojiPopup = new EmojiconsPopup(rootView, activity);
        setupEmojiView();

        rootView.findViewById(R.id.cancel_voice_message).setOnClickListener(v -> cancelVoiceMessageRecording());

        mRecordResumePause = rootView.findViewById(R.id.pause_voice_message);
        mRecordResumePause.setOnClickListener(v -> onResumePauseButtonClick());

        resolveModeViews();
    }

    public void destroyView() {
        emojiPopup.destroy();
        emojiPopup = null;
    }

    private void onResumePauseButtonClick() {
        if (mRecordActionsCallback != null) {
            mRecordActionsCallback.onResumePauseClick();
        }
    }

    private void cancelVoiceMessageRecording() {
        if (mRecordActionsCallback != null) {
            mRecordActionsCallback.onRecordCancel();
        }
    }

    private void onEmojiButtonClick() {
        if (emojiPopup.isKeyBoardOpen()) {
            InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mInputField.getWindowToken(), 0);
            emojiNeed = true;
            //ibEmoji.setImageResource(R.drawable.keyboard_arrow_down);
        } else {
            showEmoji(!emojiOnScreen);
            //ibEmoji.setImageResource(R.drawable.emoticon);
        }
    }

    public void setSendOnEnter(boolean sendOnEnter) {
        this.sendOnEnter = sendOnEnter;
    }

    private void showEmoji(boolean visible) {
        if (emojiOnScreen == visible) {
            return;
        }

        if (visible && rlEmojiContainer.getChildCount() == 0) {
            View emojiView = emojiPopup.getEmojiView(rlEmojiContainer);
            rlEmojiContainer.addView(emojiView);
        }

        rlEmojiContainer.setVisibility(visible ? View.VISIBLE : View.GONE);
        emojiOnScreen = visible;
    }

    private void setupEmojiView() {
        emojiPopup.setOnSoftKeyboardOpenCloseListener(new EmojiconsPopup.OnSoftKeyboardOpenCloseListener() {
            @Override
            public void onKeyboardOpen() {
                if (emojiOnScreen) {
                    showEmoji(false);
                }
                ibEmoji.setImageResource(R.drawable.keyboard_arrow_down);
            }

            @Override
            public void onKeyboardClose() {
                if (emojiNeed) {
                    showEmoji(true);
                    emojiNeed = false;
                }

                ibEmoji.setImageResource(R.drawable.emoticon);
            }
        });

        emojiPopup.setOnEmojiconClickedListener(emojicon -> EmojiconsPopup.input(mInputField, emojicon));
        emojiPopup.setOnEmojiconBackspaceClickedListener(v -> {
            KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
            mInputField.dispatchKeyEvent(event);
        });
    }

    private String getText() {
        return mInputField.getText().toString();
    }

    private String getTrimmedText() {
        return getText().trim();
    }

    public void setTextQuietly(String text) {
        this.mInputField.removeTextChangedListener(mTextWatcher);
        this.mInputField.setText(text);
        this.mInputField.addTextChangedListener(mTextWatcher);
    }

    public void setAttachmentsCount(int count) {
        tvAttCount.setText(String.valueOf(count));
        tvAttCount.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
        tvAttCount.setTextSize(TypedValue.COMPLEX_UNIT_DIP, count > 9 ? 10 : 12);

        int color = count > 0 ? mIconColorActive : mIconColorInactive;
        tvAttCount.setTextColor(color);
        ibAttach.getDrawable().setTint(color);
    }

    public boolean onBackPressed() {
        if (mCurrentMode == Mode.VOICE_RECORD) {
            cancelVoiceMessageRecording();
            return false;
        }

        if (emojiOnScreen) {
            showEmoji(false);
            return false;
        }

        return true;
    }

    public void setOnSickerClickListener(StickersGridView.OnStickerClickedListener sickerClickListener) {
        if (emojiPopup != null) {
            emojiPopup.setOnStickerClickedListener(sickerClickListener);
        }
    }

    private void swithModeTo(int mode) {
        if (mCurrentMode != mode) {
            mCurrentMode = mode;
            resolveModeViews();
        }
    }

    private void onSendButtonClick() {
        switch (mCurrentMode) {
            case Mode.NORMAL:
                if (canNormalSend) {
                    callback.onSendClicked(getTrimmedText());
                } else if (canStartRecording) {
                    if (mRecordActionsCallback != null) {
                        mRecordActionsCallback.onSwithToRecordMode();
                    }
                }
                break;

            case Mode.VOICE_RECORD:
                if (mRecordActionsCallback != null) {
                    mRecordActionsCallback.onRecordSendClick();
                }
                break;
            case Mode.EDITING:
                callback.onSaveClick();
                break;
        }
    }

    private void resolveModeViews() {
        switch (mCurrentMode) {
            case Mode.NORMAL:
                vgVoiceInput.setVisibility(View.GONE);
                vgMessageInput.setVisibility(View.VISIBLE);
                break;
            case Mode.VOICE_RECORD:
                vgVoiceInput.setVisibility(View.VISIBLE);
                vgMessageInput.setVisibility(View.GONE);
                break;
        }
    }

    private void resolveSendButton() {
        switch (mCurrentMode) {
            case Mode.VOICE_RECORD:
                mButtonSend.setImageResource(R.drawable.check);
                setupPrimaryButton(true);
                break;
            case Mode.NORMAL:
                mButtonSend.setImageResource(!canNormalSend && canStartRecording ? R.drawable.voice : R.drawable.send);
                setupPrimaryButton(canNormalSend || canStartRecording);
                break;
            case Mode.EDITING:
                mButtonSend.setImageResource(R.drawable.check);
                setupPrimaryButton(canEditingSave);
                break;
        }
    }

    private void setupPrimaryButton(boolean active) {
        mButtonSend.getDrawable().setTint(active ? mIconColorActive : mIconColorInactive);
    }

    public void setupRecordPauseButton(boolean visible, boolean isRecording) {
        mRecordResumePause.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        mRecordResumePause.setImageResource(visible ? (isRecording ? R.drawable.pause : R.drawable.play) : R.drawable.pause_disabled);
    }

    private boolean canEditingSave;
    private boolean canNormalSend;
    private boolean canStartRecording;

    public void swithModeToEditing(boolean canSave) {
        swithModeTo(Mode.EDITING);
        this.canEditingSave = canSave;
        resolveSendButton();
    }

    public void swithModeToNormal(boolean canSend, boolean canStartRecoring) {
        swithModeTo(Mode.NORMAL);
        this.canNormalSend = canSend;
        this.canStartRecording = canStartRecoring;
        resolveSendButton();
    }

    public void swithModeToRecording() {
        swithModeTo(Mode.VOICE_RECORD);
        resolveSendButton();
    }

    public interface OnInputActionCallback {
        void onInputTextChanged(String s);

        void onSendClicked(String body);

        void onAttachClick();

        void onSaveClick();
    }

    private RecordActionsCallback mRecordActionsCallback;

    public void setRecordActionsCallback(RecordActionsCallback recordActionsCallback) {
        this.mRecordActionsCallback = recordActionsCallback;
    }

    public interface RecordActionsCallback {
        void onRecordCancel();

        void onSwithToRecordMode();

        void onRecordSendClick();

        void onResumePauseClick();
    }

    public static final class Mode {
        public static final int NORMAL = 1;
        public static final int VOICE_RECORD = 2;
        public static final int EDITING = 3;
    }

    public void setRecordingDuration(long time) {
        String str = AppTextUtils.getDurationString((int) (time / 1000));
        mRecordingDuration.setText(mActivity.getString(R.string.recording_time, str));
    }
}