package uk.porcheron.co_curator.item;

import android.content.Context;
import android.view.View;

/**
 * A timeline item.
 */
public abstract class Item extends View {

    public Item(Context context) {
        super(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(Style.mItemFullWidth, Style.mItemFullHeight);
    }



}
