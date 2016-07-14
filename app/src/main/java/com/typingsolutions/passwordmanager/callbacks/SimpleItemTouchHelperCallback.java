package com.typingsolutions.passwordmanager.callbacks;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import com.typingsolutions.passwordmanager.BaseAdapter;
import com.typingsolutions.passwordmanager.BaseViewHolder;
import com.typingsolutions.passwordmanager.adapter.viewholder.IItemTouchHelperViewHolder;

public class SimpleItemTouchHelperCallback extends ItemTouchHelper.Callback {
  private Context mContext;
  private BaseAdapter mAdapter;
  private BaseViewHolder mViewHolder;

  public SimpleItemTouchHelperCallback(Context context, BaseAdapter adapter) {
    this.mContext = context;
    this.mAdapter = adapter;
  }

  @Override
  public boolean isLongPressDragEnabled() {
    return true;
  }

  @Override
  public boolean isItemViewSwipeEnabled() {
    return true;
  }

  @Override
  public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
    final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
    final int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
    return makeMovementFlags(dragFlags, swipeFlags);
  }

  @Override
  public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder source, RecyclerView.ViewHolder target) {
    mAdapter.onItemMove(source.getAdapterPosition(), target.getAdapterPosition());
    return true;
  }

  @Override
  public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
    mAdapter.onItemDismiss(viewHolder.getAdapterPosition());
  }

  @Override
  public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
    super.clearView(recyclerView, viewHolder);

    BaseViewHolder itemViewHolder = (BaseViewHolder) viewHolder;
    itemViewHolder.onItemClear();
  }

  @Override
  public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
    // TODO: check why onItemSelected isn't called the first time when dragged
    if(viewHolder != null && actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
      mViewHolder = (BaseViewHolder) viewHolder;
    }
    if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
      mViewHolder.onItemSelected();
    } else {
      mViewHolder.onItemReleased();
    }

    super.onSelectedChanged(viewHolder, actionState);
  }
}
