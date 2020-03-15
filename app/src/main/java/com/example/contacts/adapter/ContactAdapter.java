package com.example.contacts.adapter;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contacts.R;
import com.example.contacts.model.Contact;
import com.example.contacts.util.BaseUtil;
import com.example.contacts.util.Constants;

import java.util.List;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactHolder> {


    public interface OnItemClickListener {
        void onItemClicked(int position);
    }

    private final Context mContext;
    private final List<Contact> mList;
    private OnItemClickListener mOnItemClickListener;
    private final int mSize;

    public ContactAdapter(Context context, List<Contact> contacts, int orientation) {
        mContext = context;
        mList = contacts;
        if (orientation == ORIENTATION_LANDSCAPE) {
            mSize = (int) (BaseUtil.getScreenHeight((Activity) mContext) - mContext.getResources().getDimension(R.dimen.title_bar_height)) / Constants.ITEM_COUNT_CONTACT_LAND;
        } else {
            mSize = BaseUtil.getScreenWidth((Activity) mContext) / Constants.ITEM_COUNT_CONTACT_PORT;
        }
    }

    @NonNull
    @Override
    public ContactHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_contact, parent, false);
        return new ContactHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactHolder holder, final int position) {
        if (mList == null || position < 0 || position >= mList.size()) {
            return;
        }
        Contact contact = mList.get(position);
        if (contact != null) {
            if (contact.isDummy()) {
                holder.ivAvatar.setVisibility(View.INVISIBLE);
                holder.vHighLight.setVisibility(View.INVISIBLE);
                holder.vItem.setOnClickListener(null);
            } else {
                String pic = contact.getAvatarFilename();
                if (!TextUtils.isEmpty(pic)) {
                    pic = pic.replace(' ', '_').replace(".png", "").toLowerCase();
                    int res = mContext.getResources().getIdentifier(pic, "drawable", mContext.getPackageName());
                    holder.ivAvatar.setImageResource(res);
                    holder.ivAvatar.setVisibility(View.VISIBLE);
                }
                holder.vHighLight.setVisibility(contact.isSelected() ? View.VISIBLE : View.INVISIBLE);
                holder.vItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mOnItemClickListener != null) {
                            mOnItemClickListener.onItemClicked(position);
                        }
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    class ContactHolder extends RecyclerView.ViewHolder {

        private final ImageView ivAvatar;
        private final View vHighLight;
        private final View vItem;

        ContactHolder(@NonNull View itemView) {
            super(itemView);
            vItem = itemView;
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            vHighLight = itemView.findViewById(R.id.v_highlight);

            ViewGroup.LayoutParams lp = vItem.getLayoutParams();
            lp.width = mSize;
            lp.height = mSize;
            itemView.setLayoutParams(lp);

            int size = mSize - BaseUtil.dp2px(mContext, 16);
            lp = vHighLight.getLayoutParams();
            lp.width = size;
            lp.height = size;
            vHighLight.setLayoutParams(lp);

            lp = ivAvatar.getLayoutParams();
            size = mSize - BaseUtil.dp2px(mContext, 22);
            lp.width = size;
            lp.height = size;
            ivAvatar.setLayoutParams(lp);
        }
    }
}
