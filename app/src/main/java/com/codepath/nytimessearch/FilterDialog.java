package com.codepath.nytimessearch;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.view.View.OnClickListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class FilterDialog extends AppCompatDialogFragment {

    View mView;
    TextView datePicker;
    Spinner spSort;
    CheckBox art;
    CheckBox fashion;
    CheckBox sport;
    Button btnSave;

    DatePickerDialog datePickerDialog;
    SimpleDateFormat sdf;
    Date date;

    public interface FilterDialogListener {
        void onFinishEditDialog(Date date, String spinner, Boolean art, Boolean fashion, Boolean sport);
    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());

        mView = layoutInflater.inflate(R.layout.dialog_filter, null);

        datePicker = (TextView) mView.findViewById(R.id.tvDate);
        spSort = (Spinner) mView.findViewById(R.id.spSortOrder);
        art = (CheckBox) mView.findViewById(R.id.cbArt);
        fashion = (CheckBox) mView.findViewById(R.id.cbFashion);
        sport = (CheckBox) mView.findViewById(R.id.cbSport);
        btnSave = (Button) mView.findViewById(R.id.btnSave);

        ArrayAdapter adapter = ArrayAdapter.createFromResource(getContext(), R.array.sortBy_array, R.layout.spinner_item);
        spSort.setAdapter(adapter);

        addListenerOnDateClick();

        btnSave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("onClick", "Button Clicked");
                FilterDialogListener listener = (FilterDialogListener) getActivity();
                listener.onFinishEditDialog(date, spSort.getSelectedItem().toString(),
                        art.isChecked(), fashion.isChecked(), sport.isChecked());

                Log.d("ART STATE: ", "" + art.isChecked());
                Log.d("FASHION STATE: ", "" + fashion.isChecked());
                Log.d("SPORTS STATE: ", "" + sport.isChecked());
                // Close the dialog and return back to the parent activity
                dismiss();
            }
        });
        return mView;
    }

    public String formatDate(Date date){
        sdf = new SimpleDateFormat("MMM d, yyyy");
        String dateStr = sdf.format(date);
        return dateStr;
    }

    public void addListenerOnDateClick() {
        Calendar c = Calendar.getInstance();
        datePicker.setText(formatDate(c.getTime()));
        datePickerDialog = new DatePickerDialog(getActivity(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int month, int day) {
                        final Calendar c = Calendar.getInstance();
                        c.set(year, month, day);
                        datePicker.setText(formatDate(c.getTime()));
                        date = c.getTime();
                    }
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));

        datePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog.show();
            }
        });
    }

    public void setSpinnerToValue(Spinner spinner, String value) {
        int index = 0;
        SpinnerAdapter adapter = spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).equals(value)) {
                index = i;
                break;
            }
        }
        spinner.setSelection(index);
    }

}
