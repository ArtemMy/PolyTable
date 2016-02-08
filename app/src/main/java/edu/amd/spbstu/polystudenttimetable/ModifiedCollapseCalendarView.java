package edu.amd.spbstu.polystudenttimetable;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.wefika.calendar.CollapseCalendarView;

/**
 * Created by artem on 12/1/15.
 */
public class ModifiedCollapseCalendarView extends CollapseCalendarView {
    public ModifiedCollapseCalendarView(Context context) {
        super(context);
    }
    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        super.onTouchEvent(event);
        Log.d("init", "click");
        Log.d("init", String.valueOf(getSelectedDate().dayOfMonth()));
        return true;
    }

}
