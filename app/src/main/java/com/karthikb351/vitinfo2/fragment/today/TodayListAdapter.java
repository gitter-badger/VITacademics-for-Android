/*
 * VITacademics
 * Copyright (C) 2015  Aneesh Neelam <neelam.aneesh@gmail.com>
 * Copyright (C) 2015  Gaurav Agerwala <gauravagerwala@gmail.com>
 * Copyright (C) 2015  Pulkit Juneja <pulkit.16296@gmail.com>
 * Copyright (C) 2015  Hemant Jain <hemanham@gmail.com>
 *
 * This file is part of VITacademics.
 * VITacademics is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * VITacademics is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with VITacademics.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.karthikb351.vitinfo2.fragment.today;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.util.Pair;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.karthikb351.vitinfo2.R;
import com.karthikb351.vitinfo2.contract.Course;
import com.karthikb351.vitinfo2.contract.Timing;
import com.karthikb351.vitinfo2.utility.Constants;
import com.karthikb351.vitinfo2.utility.DateTimeCalender;
import com.karthikb351.vitinfo2.utility.RecyclerViewOnClickListener;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TodayListAdapter extends RecyclerView.Adapter<TodayListAdapter.TodayViewHolder> {

    private Context context;
    private List<Pair<Course, Timing>> courseTimingPairs;
    private int dayOfWeek;
    private RecyclerViewOnClickListener<Course> OnclickListener;

    public TodayListAdapter(Context context, int dayOfWeek, List<Pair<Course, Timing>> courseTimingPairs) {
        this.context = context;
        this.dayOfWeek = dayOfWeek;
        this.courseTimingPairs = courseTimingPairs;
    }

    @Override
    public TodayViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CardView cardView = (CardView) LayoutInflater.from(context).inflate(R.layout.card_today, parent, false);
        return new TodayViewHolder(cardView);
    }

    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    @Override
    public void onBindViewHolder(TodayViewHolder todayViewHolder, int position) {

        int classLength = 1;
        int AttendanceP = 0;
        int calcGo = 100;
        int calcMiss = 0;
        long diff = 0;
        boolean ended = false;

        if (courseTimingPairs.get(position).first.getCourseType() == Constants.COURSE_TYPE_LBC) {
            classLength = (int) courseTimingPairs.get(position).first.getLtpc().charAt(2);
        }

        if (courseTimingPairs.get(position).first.getAttendance().isSupported()) {
            AttendanceP = courseTimingPairs.get(position).first.getAttendance().getAttendancePercentage();
            calcGo = (int) Math.ceil((double) (courseTimingPairs.get(position).first.getAttendance().getAttendedClasses() + classLength) * 100 / (courseTimingPairs.get(position).first.getAttendance().getTotalClasses() + classLength));
            calcMiss = (int) Math.ceil((double) courseTimingPairs.get(position).first.getAttendance().getAttendedClasses()) * 100 / (courseTimingPairs.get(position).first.getAttendance().getTotalClasses() + classLength);
        }

        if (courseTimingPairs.get(position).second.getDay() == dayOfWeek) {
            diff = getTimeDifference(courseTimingPairs.get(position).second);
            ended = checkIfSlotEnded(courseTimingPairs.get(position).second);
        }

        todayViewHolder.courseCode.setText(courseTimingPairs.get(position).first.getCourseCode());
        todayViewHolder.courseName.setText(courseTimingPairs.get(position).first.getCourseTitle());
        todayViewHolder.Venue.setText(courseTimingPairs.get(position).first.getVenue());
        todayViewHolder.Slot.setText(courseTimingPairs.get(position).first.getSlot());
        todayViewHolder.Attendance.setText(Integer.toString(AttendanceP));
        todayViewHolder.pbAttendance.setProgress(AttendanceP);
        todayViewHolder.go.setText(Integer.toString(calcGo));
        todayViewHolder.miss.setText(Integer.toString(calcMiss));
        todayViewHolder.TimeLeft.setText(getTimeDifferenceString(diff, ended));

        int sdk = android.os.Build.VERSION.SDK_INT;
        int bgColor = getAttendanceColor(AttendanceP);

        todayViewHolder.pbAttendance.getProgressDrawable().setColorFilter(bgColor, PorterDuff.Mode.SRC_IN);
        GradientDrawable txt_bgShape;
        txt_bgShape = (GradientDrawable) todayViewHolder.Attendance.getBackground();
        txt_bgShape.setColor(bgColor);

        if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            todayViewHolder.Attendance.setBackgroundDrawable(txt_bgShape);
        } else {
            todayViewHolder.Attendance.setBackground(txt_bgShape);
        }
    }

    public void setOnclickListener(RecyclerViewOnClickListener<Course> listener) {
        OnclickListener = listener;
    }

    @Override
    public int getItemCount() {
        return courseTimingPairs.size();
    }

    private String getTimeDifferenceString(long diff, boolean ended) {
        int hours;
        int minutes;
        if (diff < 0 && ended) {
            return context.getString(R.string.today_course_timing_done);
        } else if (diff > Constants.MILLISECONDS_IN_MINUTE) {
            hours = (int) TimeUnit.MILLISECONDS.toHours(diff);
            minutes = (int) (TimeUnit.MILLISECONDS.toMinutes(diff) - TimeUnit.HOURS.toMinutes(hours));
            if (hours == 0) {
                return context.getResources().getQuantityString(R.plurals.today_course_timing_minutes_later, minutes, minutes);
            } else if (minutes == 0) {
                return context.getResources().getQuantityString(R.plurals.today_course_timing_hours_later, hours, hours);
            } else {
                return context.getString(R.string.today_course_timing_hours_minutes_later, context.getResources().getQuantityString(R.plurals.today_course_timing_hours, hours, hours), context.getResources().getQuantityString(R.plurals.today_course_timing_minutes_later, minutes, minutes));
            }
        } else {
            return context.getString(R.string.today_course_timing_right_now);
        }
    }

    private long getTimeDifference(Timing timing) {
        Date now = new Date();
        try {
            Date courseStartTime = DateTimeCalender.getTodayTimeObject(timing.getStartTime());
            return courseStartTime.getTime() - now.getTime();
        } catch (ParseException ex) {
            ex.printStackTrace();
            return -1;
        }
    }

    private boolean checkIfSlotEnded(Timing timing) {
        Date now = new Date();
        try {
            Date courseEndTime = DateTimeCalender.getTodayTimeObject(timing.getEndTime());
            return courseEndTime.before(now);
        } catch (ParseException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public class TodayViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView courseName, courseCode, Attendance, Slot, Venue, TimeLeft, go, miss;
        public ProgressBar pbAttendance;

        public TodayViewHolder(View view) {
            super(view);
            courseName = (TextView) view.findViewById(R.id.tv_course_name);
            courseCode = (TextView) view.findViewById(R.id.tv_course_code);
            Attendance = (TextView) view.findViewById(R.id.tv_attendance);
            Slot = (TextView) view.findViewById(R.id.tv_slot);
            Venue = (TextView) view.findViewById(R.id.tv_venue);
            TimeLeft = (TextView) view.findViewById(R.id.tv_time_left);
            go = (TextView) view.findViewById(R.id.tv_go_attend);
            miss = (TextView) view.findViewById(R.id.tv_miss_attend);
            pbAttendance = (ProgressBar) view.findViewById(R.id.process_bar_attendance);
            pbAttendance.setMax(100);
        }

        public void onClick(View view) {
            Course course = courseTimingPairs.get(getAdapterPosition()).first;
            OnclickListener.onItemClick(course);
        }
    }

    private int getAttendanceColor(int attendance) {
        if (attendance >= 80) {
            return context.getResources().getColor(R.color.highAttend);
        } else if (attendance >= 75 && attendance < 80) {
            return context.getResources().getColor(R.color.midAttend);
        } else {
            return context.getResources().getColor(R.color.lowAttend);
        }
    }
}
