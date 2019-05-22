package com.github.bassaer.example;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.Toast;

import com.github.bassaer.chatmessageview.model.IChatUser;
import com.github.bassaer.chatmessageview.model.Message;
import com.github.bassaer.chatmessageview.view.ChatView;
import com.github.bassaer.chatmessageview.view.MessageView;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import org.jetbrains.annotations.NotNull;

import io.realm.Realm;
import io.realm.RealmResults;


/**
 * Simple chat example activity
 */
public class MessengerActivity extends AppCompatActivity {

    @VisibleForTesting
    protected static final int RIGHT_BUBBLE_COLOR = R.color.colorPrimaryDark;
    @VisibleForTesting
    protected static final int LEFT_BUBBLE_COLOR = R.color.gray300;
    @VisibleForTesting
    protected static final int BACKGROUND_COLOR = R.color.blueGray400;
    @VisibleForTesting
    protected static final int SEND_BUTTON_COLOR = R.color.blueGray500;
    @VisibleForTesting
    protected static final int SEND_ICON = R.drawable.ic_action_send;
    @VisibleForTesting
    protected static final int OPTION_BUTTON_COLOR = R.color.teal500;
    @VisibleForTesting
    protected static final int RIGHT_MESSAGE_TEXT_COLOR = Color.WHITE;
    @VisibleForTesting
    protected static final int LEFT_MESSAGE_TEXT_COLOR = Color.BLACK;
    @VisibleForTesting
    protected static final int USERNAME_TEXT_COLOR = Color.WHITE;
    @VisibleForTesting
    protected static final int SEND_TIME_TEXT_COLOR = Color.WHITE;
    @VisibleForTesting
    protected static final int DATA_SEPARATOR_COLOR = Color.WHITE;
    @VisibleForTesting
    protected static final int MESSAGE_STATUS_TEXT_COLOR = Color.WHITE;
    @VisibleForTesting
    protected static final String INPUT_TEXT_HINT = "New memo..";
    @VisibleForTesting
    protected static final int MESSAGE_MARGIN = 5;

    private ChatView mChatView;
    private MessageList mMessageList;
    private ArrayList<User> mUsers;

    private int mReplyDelay = -1;
    private static final int CHANNEL_CONFIG = 2;

    private static final int READ_REQUEST_CODE = 100;
    private static final int CONFIG_REQUEST_CODE = 2;

    static final int CONTEXT_MENU1 = 0;
    static final int CONTEXT_MENU2 = 1;

    static final int INIT_FLAG = 0;
    static final int RELOAD_FLAG = 1;

    private int mMessageId = 0;
    private int mChangeId = 0;

    private Message mMessage;

    private Realm mRealm;
    private RealmResults<MessageDataBase> mMessageDataBaseRealmResults;

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messenger);

        Realm.init(this);
        mRealm = Realm.getDefaultInstance();
        mMessageDataBaseRealmResults = mRealm.where(MessageDataBase.class).findAll();

        initUsers();

        mChatView = findViewById(R.id.chat_view);

        //Load saved messages
        loadMessages(INIT_FLAG, null);

        //Set UI parameters if you need
        mChatView.setRightBubbleColor(ContextCompat.getColor(this, RIGHT_BUBBLE_COLOR));
        mChatView.setLeftBubbleColor(ContextCompat.getColor(this, LEFT_BUBBLE_COLOR));
        mChatView.setBackgroundColor(ContextCompat.getColor(this, BACKGROUND_COLOR));
        mChatView.setSendButtonColor(ContextCompat.getColor(this, SEND_BUTTON_COLOR));

        mChatView.setSendIcon(SEND_ICON);
        mChatView.setOptionIcon(R.drawable.ic_account_circle);
        mChatView.setOptionButtonColor(OPTION_BUTTON_COLOR);
        mChatView.setRightMessageTextColor(RIGHT_MESSAGE_TEXT_COLOR);
        mChatView.setLeftMessageTextColor(LEFT_MESSAGE_TEXT_COLOR);
        mChatView.setUsernameTextColor(USERNAME_TEXT_COLOR);
        mChatView.setSendTimeTextColor(SEND_TIME_TEXT_COLOR);
        mChatView.setDateSeparatorColor(DATA_SEPARATOR_COLOR);
        mChatView.setMessageStatusTextColor(MESSAGE_STATUS_TEXT_COLOR);
        mChatView.setInputTextHint(INPUT_TEXT_HINT);
        mChatView.setMessageMarginTop(MESSAGE_MARGIN);
        mChatView.setMessageMarginBottom(MESSAGE_MARGIN);
        mChatView.setMaxInputLine(5);
        mChatView.setUsernameFontSize(getResources().getDimension(R.dimen.font_small));
        mChatView.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        mChatView.setInputTextColor(ContextCompat.getColor(this, R.color.red500));
        mChatView.setInputTextSize(TypedValue.COMPLEX_UNIT_SP, 20);

        mChatView.setOnBubbleClickListener(new Message.OnBubbleClickListener() {
            @Override
            public void onClick(Message message) {
                Toast.makeText(
                        MessengerActivity.this,
                        "click : " + message.getText(),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        mChatView.setOnBubbleLongClickListener(new Message.OnBubbleLongClickListener() {
            @Override
            public void onLongClick(final Message message) {
                Toast.makeText(
                        MessengerActivity.this,
                        "Long click : " + message.getText(),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        mChatView.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                MessengerActivity.super.onCreateContextMenu(menu, v, menuInfo);
                menu.setHeaderTitle("menu title");
                menu.add(0, CONTEXT_MENU1, 0, "menu1");
                menu.add(0, CONTEXT_MENU2, 0, "menu2");
            }
        });

        mChatView.setOnIconLongClickListener(new Message.OnIconLongClickListener() {
            @Override
            public void onIconLongClick(Message message) {
                if (message.getType() == Message.Type.PICTURE) {
                    Toast.makeText(
                            MessengerActivity.this,
                            "message \n" + message.getText(),
                            Toast.LENGTH_SHORT
                    ).show();
                } else {
                    /*
                      To ConfigurationActivity
                     */
                    mChangeId = message.getId() - 1;
                    Intent intent = new Intent(MessengerActivity.this, ConfigurationActivity.class);
                    String title = message.getText();
                    String date = message.getDateSeparateText();
                    String time = message.getTimeText();
                    intent.putExtra(ConfigurationActivity.TITLE, title);
                    intent.putExtra(ConfigurationActivity.DATE, date);
                    intent.putExtra(ConfigurationActivity.TIME, time);
                    intent.putExtra(ConfigurationActivity.ID, message.getId());
                    startActivityForResult(intent, CHANNEL_CONFIG);
                }
            }
        });


        //Click Send Button
        mChatView.setOnClickSendButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initUsers();
                if (mChatView.getInputText().equals("")) {
                    Log.d("debug111", "=============");
                } else {
                    //new message
                    Message message = new Message.Builder()
                            .setUser(mUsers.get(0))
                            .setRight(true)
                            .setText(mChatView.getInputText())
                            .hideIcon(false)
                            .setUserIconVisibility(true)
                            .setStatusIconFormatter(new MyMessageStatusFormatter(MessengerActivity.this))
                            .setStatusTextFormatter(new MyMessageStatusFormatter(MessengerActivity.this))
                            .setId(mMessageId + 1)
                            .build();
                    mMessageId += 1;
                    if (message.getText() != null) {
                        //Set to chat view
                        mChatView.send(message);
                        //Add message list
                        mMessageList.add(message);
                        //Reset edit text
                        mChatView.setInputText("");
                    }
                }
            }
        });

        //Click option button
        mChatView.setOnClickOptionButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });
    }

    private void openGallery() {
        Intent intent;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        intent.setType("image/*");

        startActivityForResult(intent, READ_REQUEST_CODE);
    }

//    private void receiveMessage(String sendText) {
//        //Ignore hey
//        if (!sendText.contains("hey")) {
//
//            //Receive message
//            final Message receivedMessage = new Message.Builder()
//                    .setUser(mUsers.get(1))
//                    .setRight(false)
//                    .setText(ChatBot.INSTANCE.talk(mUsers.get(0).getName(), sendText))
//                    .setStatusIconFormatter(new MyMessageStatusFormatter(MessengerActivity.this))
//                    .setStatusTextFormatter(new MyMessageStatusFormatter(MessengerActivity.this))
//                    .setStatusStyle(Message.Companion.getSTATUS_ICON())
//                    .setStatus(MyMessageStatusFormatter.STATUS_DELIVERED)
//                    .build();

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case CONTEXT_MENU1: {
                Log.d("debug1111", "menu1");
//                mChatView.getMessageView().remove();
                return super.onContextItemSelected(item);
            }
            case CONTEXT_MENU2: {
                Toast.makeText(
                        MessengerActivity.this,
                        "click : " + CONTEXT_MENU2,
                        Toast.LENGTH_SHORT
                ).show();
                return super.onContextItemSelected(item);
            }
        }
        return super.onContextItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == READ_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            try {
                Bitmap picture = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                Message message = new Message.Builder()
                        .setRight(true)
                        .setText(Message.Type.PICTURE.name())
                        .setUser(mUsers.get(0))
                        .hideIcon(false)
                        .setUserIconVisibility(true)
                        .setPicture(picture)
                        .setType(Message.Type.PICTURE)
                        .setId(mMessageId + 1)
                        .setStatusIconFormatter(new MyMessageStatusFormatter(MessengerActivity.this))
                        .build();
                mChatView.send(message);
                //Add message list
                mMessageList.add(message);
                mMessageId += 1;
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == CONFIG_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            String title = data.getStringExtra(ConfigurationActivity.TITLE);
            String date = data.getStringExtra(ConfigurationActivity.DATE);
            String time = data.getStringExtra(ConfigurationActivity.TIME);
            loadMessages(RELOAD_FLAG, title);
        } else {
            return;
        }
    }

    private void initUsers() {
        mUsers = new ArrayList<>();
        //User id
        int myId = 0;
        //User icon
        Bitmap myIcon = BitmapFactory.decodeResource(getResources(), R.drawable.face_2);
        //User name
        String myName = "Michael";

        final User me = new User(myId, myName, myIcon);

        mUsers.add(me);
    }

    /**
     * Load saved messages
     */
    public void loadMessages(int flag, String title) {
        if (flag == INIT_FLAG) {
            List<Message> messages = new ArrayList<>();
            mMessageList = AppData.getMessageList(this);
            if (mMessageList == null) {
                mMessageList = new MessageList();
                mMessageId = 0;
            } else {
                Message message = null;
                for (int i = 0; i < mMessageList.size(); i++) {
                    message = mMessageList.get(i);
                    //Set extra info because they were removed before save messages.
                    for (IChatUser user : mUsers) {
                        if (message.getUser().getId().equals(user.getId())) {
                            message.getUser().setIcon(user.getIcon());
                        }
                    }
                    message.setStatusIconFormatter(new MyMessageStatusFormatter(this));
                    message.setStatus(MyMessageStatusFormatter.STATUS_DELIVERED);
                    messages.add(message);
                }
                mMessageId = message.getId();
            }
            MessageView messageView = mChatView.getMessageView();
            messageView.init(messages);
            messageView.setSelection(messageView.getCount() - 1);

        } else if (flag == RELOAD_FLAG) {

            mMessageList = AppData.getMessageList(this);
            Message message = mMessageList.get(mChangeId);
            message.setText(title);

            MessageView messageView = mChatView.getMessageView();
            messageView.setSelection(messageView.getCount() - 1);

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        initUsers();
    }

    @Override
    public void onPause() {
        super.onPause();
        //Save message
        mMessageList = new MessageList();
        mMessageList.setMessages(mChatView.getMessageView().getMessageList());
        AppData.putMessageList(this, mMessageList);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRealm.close();
    }

    @VisibleForTesting
    public ArrayList<User> getUsers() {
        return mUsers;
    }

    public void setReplyDelay(int replyDelay) {
        mReplyDelay = replyDelay;
    }

    private void showDialog() {
        final String[] items = {
                getString(R.string.send_picture),
                getString(R.string.clear_messages)
        };

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.options))
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int position) {
                        switch (position) {
                            case 0:
                                openGallery();
                                break;
                            case 1:
                                mMessageId = 0;
                                mChatView.getMessageView().removeAll();
                                RealmResults<MessageDataBase> results = mRealm
                                        .where(MessageDataBase.class)
                                        .findAll();
                                mRealm.beginTransaction();
                                results.deleteAllFromRealm();
                                mRealm.commitTransaction();

                                break;
                        }
                    }
                })
                .show();
    }
}
