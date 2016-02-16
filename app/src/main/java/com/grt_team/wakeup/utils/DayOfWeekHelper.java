package com.grt_team.wakeup.utils;

import java.text.DateFormatSymbols;
import java.util.Calendar;

import com.grt_team.wakeup.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

public class DayOfWeekHelper {

	public static final int MONDAY = 1;
	public static final int TUESDAY = 2;
	public static final int WEDNESDAY = 4;
	public static final int THURSDAY = 8;
	public static final int FRIDAY = 16;
	public static final int SATURDAY = 32;
	public static final int SUNDAY = 64;

	private DayOfWeekHelper() {
	}

	public interface OnDayOfWeekChanged {
		public void onChanged(int newDayOfWeekSet);
	}

	private static boolean hasDay(int dayOfWeekSet, int day) {
		return day == (dayOfWeekSet & day);
	}

	private static String[] getDayOfWeekNames(boolean shortTitles) {
		DateFormatSymbols dfs = new DateFormatSymbols();
		String t[] = (shortTitles) ? dfs.getShortWeekdays() : dfs.getWeekdays();
		return new String[] { t[Calendar.MONDAY], t[Calendar.TUESDAY],
				t[Calendar.WEDNESDAY], t[Calendar.THURSDAY],
				t[Calendar.FRIDAY], t[Calendar.SATURDAY], t[Calendar.SUNDAY] };
	}

	public static String toString(Context context, int dayOfWeekSet,
			boolean shortTitles) {
		StringBuilder str = new StringBuilder();
		DateFormatSymbols dfs = new DateFormatSymbols();
		String t[] = (shortTitles) ? dfs.getShortWeekdays() : dfs.getWeekdays();

		if (dayOfWeekSet == 0) {
			return context.getResources().getString(R.string.day_picker_never);
		}

		if (dayOfWeekSet == 0x7f) {
			return context.getResources().getString(
					R.string.day_picker_every_day);
		}

		if (hasDay(dayOfWeekSet, MONDAY)) {
			str.append(" ");
			str.append(t[Calendar.MONDAY]);
		}
		if (hasDay(dayOfWeekSet, TUESDAY)) {
			str.append(" ");
			str.append(t[Calendar.TUESDAY]);
		}
		if (hasDay(dayOfWeekSet, WEDNESDAY)) {
			str.append(" ");
			str.append(t[Calendar.WEDNESDAY]);
		}
		if (hasDay(dayOfWeekSet, THURSDAY)) {
			str.append(" ");
			str.append(t[Calendar.THURSDAY]);
		}
		if (hasDay(dayOfWeekSet, FRIDAY)) {
			str.append(" ");
			str.append(t[Calendar.FRIDAY]);
		}
		if (hasDay(dayOfWeekSet, SATURDAY)) {
			str.append(" ");
			str.append(t[Calendar.SATURDAY]);
		}
		if (hasDay(dayOfWeekSet, SUNDAY)) {
			str.append(" ");
			str.append(t[Calendar.SUNDAY]);
		}

		return str.toString().trim();
	}

	private static int[] getDayOfWeekValues() {
		return new int[] { MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY,
				SATURDAY, SUNDAY };
	}

	private static boolean[] getSelectedValues(int dayOfWeek) {
		boolean[] selected = new boolean[7];

		selected[0] = MONDAY == (MONDAY & dayOfWeek);
		selected[1] = TUESDAY == (TUESDAY & dayOfWeek);
		selected[2] = WEDNESDAY == (WEDNESDAY & dayOfWeek);
		selected[3] = THURSDAY == (THURSDAY & dayOfWeek);
		selected[4] = FRIDAY == (FRIDAY & dayOfWeek);
		selected[5] = SATURDAY == (SATURDAY & dayOfWeek);
		selected[6] = SUNDAY == (SUNDAY & dayOfWeek);

		return selected;
	}

	public static AlertDialog getDayOfWeekPicker(Context context,
			int dayOfWeek, OnDayOfWeekChanged onDateChange) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(R.string.day_picker_title);

		final OnDayOfWeekChanged onChange = onDateChange;
		final int[] weekOfDayValues = getDayOfWeekValues();

		builder.setNegativeButton(android.R.string.cancel, null);
		builder.setPositiveButton(android.R.string.ok, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				AlertDialog dlg = (AlertDialog) dialog;
				Integer newDaysOfWeek = (Integer) dlg.getListView().getTag();
				onChange.onChanged(newDaysOfWeek);
			}

		});

		builder.setMultiChoiceItems(getDayOfWeekNames(false),
				getSelectedValues(dayOfWeek),
				new DialogInterface.OnMultiChoiceClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which,
							boolean isChecked) {
						AlertDialog dlg = (AlertDialog) dialog;
						Integer newDaysOfWeek = (Integer) dlg.getListView()
								.getTag();

						if (isChecked) {
							newDaysOfWeek |= weekOfDayValues[which];
						} else {
							newDaysOfWeek &= ~weekOfDayValues[which];
						}
						dlg.getListView().setTag(newDaysOfWeek);
					}

				});

		AlertDialog alertDialog = builder.create();
		alertDialog.getListView().setTag(dayOfWeek);
		return alertDialog;
	}

	private static boolean allowDate(Calendar cal, int dayOfWeek) {
		boolean allow = false;
		int calDay = cal.get(Calendar.DAY_OF_WEEK);
		switch (calDay) {
		case Calendar.MONDAY:
			allow = (dayOfWeek & MONDAY) == MONDAY;
			break;
		case Calendar.TUESDAY:
			allow = (dayOfWeek & TUESDAY) == TUESDAY;
			break;
		case Calendar.WEDNESDAY:
			allow = (dayOfWeek & WEDNESDAY) == WEDNESDAY;
			break;
		case Calendar.THURSDAY:
			allow = (dayOfWeek & THURSDAY) == THURSDAY;
			break;
		case Calendar.FRIDAY:
			allow = (dayOfWeek & FRIDAY) == FRIDAY;
			break;
		case Calendar.SATURDAY:
			allow = (dayOfWeek & SATURDAY) == SATURDAY;
			break;
		case Calendar.SUNDAY:
			allow = (dayOfWeek & SUNDAY) == SUNDAY;
			break;
		}
		return allow;
	}

	public static Calendar getNextDayOfWeek(int hour, int minutes, int dayOfWeek) {
		Calendar now = Calendar.getInstance();
		Calendar next = Calendar.getInstance();
		next.set(Calendar.HOUR_OF_DAY, hour);
		next.set(Calendar.MINUTE, minutes);
		next.set(Calendar.SECOND, 0);

		while (next.before(now) || !allowDate(next, dayOfWeek)
				|| now.equals(next)) {
			next.add(Calendar.DAY_OF_YEAR, 1);
		}

		return next;
	}
}