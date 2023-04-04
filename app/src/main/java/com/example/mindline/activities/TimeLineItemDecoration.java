package com.example.mindline.activities;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mindline.R;

import com.example.mindline.adapters.TimelineAdapter;
import com.example.mindline.models.Memory;
import com.example.mindline.utils.DateTimeUtils;

import java.util.Date;
import java.util.List;

public class TimeLineItemDecoration extends RecyclerView.ItemDecoration {

    private Paint linePaint;
    private Paint yearMarkerPaint;
    private int yearMarkerRadius;
    private int yearMarkerPadding;
    private int linePadding;

    public TimeLineItemDecoration(Context context) {
        Resources resources = context.getResources();

        // Set up the paint for the timeline line
        linePaint = new Paint();
        linePaint.setColor(ContextCompat.getColor(context, R.color.timeline_color));
        linePaint.setStrokeWidth(resources.getDimensionPixelSize(R.dimen.timeline_width));

        // Set up the paint for the year markers
        yearMarkerPaint = new Paint();
        yearMarkerPaint.setColor(ContextCompat.getColor(context, R.color.timeline_color));
        yearMarkerPaint.setStyle(Paint.Style.FILL);

        // Set the size and padding of the year markers
        yearMarkerRadius = resources.getDimensionPixelSize(R.dimen.year_marker_radius);
        yearMarkerPadding = resources.getDimensionPixelSize(R.dimen.year_marker_padding);

        // Set the padding between the timeline line and the edges of the RecyclerView
        linePadding = resources.getDimensionPixelSize(R.dimen.timeline_padding);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.top = yearMarkerRadius + yearMarkerPadding + linePadding;
        outRect.bottom = yearMarkerRadius + yearMarkerPadding + linePadding;
    }

    @Override
    public void onDraw(@NonNull Canvas canvas, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        if (parent.getChildCount() == 0 || parent.getChildAt(0) == null) {
            return; // skip drawing if there are no child views or the first child view is null
        }
        super.onDraw(canvas, parent, state);

        // Get the first child view and calculate the center x-coordinate for the timeline line
        View firstChild = parent.getChildAt(0);
        int cx = firstChild.getLeft() + linePadding / 2;

        // Draw the timeline line
        canvas.drawLine(cx, 0, cx, parent.getHeight(), linePaint);

        int childCount = parent.getChildCount();

        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);
            if (child == null) {
                continue;
            }
            int cy = child.getTop() + child.getHeight() / 2;
            int position = parent.getChildAdapterPosition(child);

            // Draw the year marker
            String year = getYear(parent, position);
            if (year != null) {
                canvas.drawCircle(cx, cy, yearMarkerRadius, yearMarkerPaint);
                drawYearMarkerText(canvas, year, cx, cy);
            }
        }
    }



    private String getYear(RecyclerView parent, int position) {
        TimelineAdapter adapter = (TimelineAdapter) parent.getAdapter();
        List<Object> timelineItems = adapter.getTimelineItems();
        if (timelineItems != null && timelineItems.size() > position) {
            Object item = timelineItems.get(position);
            if (item instanceof Memory) {
                String dateStr = ((Memory) item).getDate();
                Date date = DateTimeUtils.parseDate(dateStr);
                if (date != null) {
                    return DateTimeUtils.getYearFromDate(date);
                }
            } else if (item instanceof String) {
                return (String) item;
            }
        }
        return null;
    }




    private void drawYearMarkerText(Canvas canvas, String text, int cx, int cy) {
        Paint textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(yearMarkerRadius * 0.8f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        canvas.drawText(text, cx, cy + textPaint.getTextSize() / 3, textPaint);
    }
}
