package com.example.contacts.adapter;

import android.content.Context;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contacts.R;
import com.example.contacts.model.Contact;

import java.util.List;

public class InfoAdapter extends RecyclerView.Adapter<InfoAdapter.InfoHolder> {

    private final Context mContext;
    private final List<Contact> mList;

    public InfoAdapter(Context context, List<Contact> contacts) {
        mContext = context;
        mList = contacts;
    }

    @NonNull
    @Override
    public InfoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_info, parent, false);
        return new InfoHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InfoHolder holder, int position) {
        if (mList == null || position < 0 || position >= mList.size()) {
            return;
        }
        Contact contact = mList.get(position);
        if (contact != null) {
            if (!TextUtils.isEmpty(contact.getFirstName()) && !TextUtils.isEmpty(contact.getLastName())) {
                String string = "<span><b>" + contact.getFirstName() + "    </b>" + contact.getLastName() + "</span>";
                holder.tvName.setText(Html.fromHtml(string));
            } else {
                holder.tvName.setText("");
            }
            if (!TextUtils.isEmpty(contact.getTitle())) {
                holder.tvTitle.setText(contact.getTitle());
            } else {
                holder.tvTitle.setText("");
            }
            if (!TextUtils.isEmpty(contact.getIntroduction())) {
                holder.tvIntro.setText(contact.getIntroduction());
            } else {
                holder.tvIntro.setText("");
            }
        }
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }

    class InfoHolder extends RecyclerView.ViewHolder {

        private final TextView tvName;
        private final TextView tvTitle;
        private final TextView tvIntro;

        InfoHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvIntro = itemView.findViewById(R.id.tv_intro);
        }
    }
}
