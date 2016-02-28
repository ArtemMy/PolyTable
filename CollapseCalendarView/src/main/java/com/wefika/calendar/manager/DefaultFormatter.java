package com.wefika.calendar.manager;

import android.support.annotation.NonNull;

import com.wefika.calendar.manager.CalendarUnit.CalendarType;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.Weeks;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Created by Blaz Solar on 26/04/15.
 */
public class DefaultFormatter implements Formatter {

    private final DateTimeFormatter dayFormatter;
    private final DateTimeFormatter weekHeaderFormatter;
    private final DateTimeFormatter monthHeaderFormatter;

    public DefaultFormatter() {
        this("E", "w 'неделя'", "MMMM yyyy");
    }

    public DefaultFormatter(@NonNull String dayPattern, @NonNull String weekPattern, @NonNull String monthPattern) {
        dayFormatter = DateTimeFormat.forPattern(dayPattern);
        weekHeaderFormatter = DateTimeFormat.forPattern(weekPattern);
        monthHeaderFormatter = DateTimeFormat.forPattern(monthPattern);
    }

    @Override public String getDayName(@NonNull LocalDate date) {
        return date.toString(dayFormatter);
    }

    @Override public String getHeaderText(@CalendarType int type, @NonNull LocalDate from, @NonNull LocalDate to) {
        switch (type) {
            case CalendarUnit.TYPE_WEEK:
                DateTime t;
                if(LocalDate.now().getMonthOfYear() > 8)
                    t = new DateTime(DateTime.now().getYear(), 9, 1, 1, 1);
                else
                    t = new DateTime(DateTime.now().getYear(), 2, 1, 1, 1);
                return (String.valueOf(Weeks.weeksBetween(t.toLocalDate(), DateTime.now().toLocalDate()).getWeeks()));

            case CalendarUnit.TYPE_MONTH:
                return from.toString(monthHeaderFormatter);
            default:
                throw new IllegalStateException("Unknown calendar type");
        }
    }
}
