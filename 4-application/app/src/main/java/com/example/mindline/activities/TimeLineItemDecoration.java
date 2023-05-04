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
    private int textColor;


    public TimeLineItemDecoration(Context context) {
        Resources resources = context.getResources();

        // Get the colors for the current theme
        int timelineColor = ContextCompat.getColor(context, R.color.timeline_color);
        textColor = ContextCompat.getColor(context, R.color.text_color);

        // Set up the paint for the timeline line
        linePaint = new Paint();
        linePaint.setColor(timelineColor);
        linePaint.setStrokeWidth(resources.getDimensionPixelSize(R.dimen.timeline_width));

        // Set up the paint for the year markers
        yearMarkerPaint = new Paint();
        yearMarkerPaint.setColor(timelineColor);
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
        // Get the adapter for the RecyclerView and retrieve the list of timeline items
        TimelineAdapter adapter = (TimelineAdapter) parent.getAdapter();
        List<Object> timelineItems = adapter.getTimelineItems();
        // Check if the list of items is not null and the position is within the bounds of the list
        if (timelineItems != null && timelineItems.size() > position) {
            // Get the item at the specified position
            Object item = timelineItems.get(position);
            // If the item is a Memory object, get the date and return the year component
            if (item instanceof Memory) {
                String dateStr = ((Memory) item).getDate();
                Date date = DateTimeUtils.parseDate(dateStr);
                // If the date can be parsed, return the year component
                if (date != null) {
                    return DateTimeUtils.getYearFromDate(date);
                }
            }
            // If the item is a string, return the string
            else if (item instanceof String) {
                return (String) item;
            }
        }
        // If the list is null or the position is out of bounds, return null
        return null;
    }

    private void drawYearMarkerText(Canvas canvas, String text, int cx, int cy) {
        // Create a new Paint object with the text color and 80% of the year marker radius
        Paint textPaint = new Paint();
        textPaint.setColor(textColor);
        textPaint.setTextSize(yearMarkerRadius * 0.8f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        // Draw the text on the canvas at the specified center position
        canvas.drawText(text, cx, cy + textPaint.getTextSize() / 3, textPaint);
    }

}