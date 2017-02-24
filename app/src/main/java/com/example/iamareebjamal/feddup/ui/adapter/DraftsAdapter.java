package com.example.iamareebjamal.feddup.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.iamareebjamal.feddup.R;
import com.example.iamareebjamal.feddup.data.models.PostDraft;
import com.example.iamareebjamal.feddup.ui.viewholder.DraftHolder;

import java.util.List;

public class DraftsAdapter extends RecyclerView.Adapter<DraftHolder> {

    private List<PostDraft> drafts;
    private Context context;

    public DraftsAdapter(Context context, List<PostDraft> drafts) {
        this.context = context;
        this.drafts = drafts;
    }

    @Override
    public DraftHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View thisItemsView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_draft, parent, false);

        return new DraftHolder(thisItemsView);
    }

    @Override
    public void onBindViewHolder(DraftHolder holder, int position) {
        holder.setDraft(drafts.get(position));
    }

    @Override
    public int getItemCount() {
        return drafts.size();
    }
}
