package com.planetpeopleplatform.freegan.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.planetpeopleplatform.freegan.R;
import com.planetpeopleplatform.freegan.model.Message;
import com.planetpeopleplatform.freegan.model.User;
import com.planetpeopleplatform.freegan.utils.Utils;

import java.text.ParseException;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter {

    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

    private User mChatMate;
    private Context mContext;
    private List<Message> mMessageList;

    public MessageAdapter(Context context, List<Message> messageList, User chatMate) {
        mContext = context;
        mMessageList = messageList;
        mChatMate = chatMate;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view;

        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageHolder(view);
        } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageHolder(view);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = mMessageList.get(position);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentMessageHolder) holder).bind(message);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivedMessageHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        if (mMessageList == null){
            return 0;
        }
        return mMessageList.size();
    }

    // Determines the appropriate ViewType according to the sender of the message.
    @Override
    public int getItemViewType(int position) {

        Message message = mMessageList.get(position);

        if (mChatMate.getUserName().equals(message.getSenderName())) {
            // If some other user sent the message
            return VIEW_TYPE_MESSAGE_RECEIVED;
        } else {
            // If the current user is the sender of the message
            return VIEW_TYPE_MESSAGE_SENT;
        }
    }



    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText, nameText;
        CircleImageView profileImage;

        ReceivedMessageHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.text_message_body);
            timeText = itemView.findViewById(R.id.text_message_time);
            nameText = itemView.findViewById(R.id.text_message_name);
            profileImage = itemView.findViewById(R.id.image_message_profile);
        }

        void bind(Message message) {

            messageText.setText(message.getMessage());
            nameText.setText(message.getSenderName());

            // Format the stored timestamp into a readable String using method.
            try {
                timeText.setText(DateUtils.getRelativeDateTimeString(mContext,
                        Utils.DateHelper.DF_SIMPLE_FORMAT.get().parse(message.getDate()).getTime(), DateUtils.SECOND_IN_MILLIS,
                        DateUtils.SECOND_IN_MILLIS, 0));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            // Insert the profile image from the URL into the ImageView.
            Glide.with(mContext).load(mChatMate.getUserImgUrl()).into(profileImage);
        }
    }


    private class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText;

        SentMessageHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.text_message_body);
            timeText = itemView.findViewById(R.id.text_message_time);
        }

        void bind(Message message) {
            messageText.setText(message.getMessage());

            // Format the stored timestamp into a readable String using method.
            try {
                timeText.setText(DateUtils.getRelativeDateTimeString(mContext,
                        Utils.DateHelper.DF_SIMPLE_FORMAT.get().parse(message.getDate()).getTime(), DateUtils.SECOND_IN_MILLIS,
                        DateUtils.SECOND_IN_MILLIS, 0));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }
}
