package tcslab.syndesiapp.controllers.localization;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import tcslab.syndesiapp.R;
import tcslab.syndesiapp.models.WifiScan;

import java.util.ArrayList;

/**
 * View used to display one automation message.
 *
 * Created by Blaise on 02.06.2015.
 */
public class ScanAdapter extends ArrayAdapter<WifiScan> {
private final Context mAppContext;
private final ArrayList<WifiScan> mScans;

public ScanAdapter(Context context, ArrayList<WifiScan> mScans) {
        super(context, R.layout.scan_display, mScans);
        this.mAppContext = context;
        this.mScans = mScans;
        }

@Override
public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mAppContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View messageView = inflater.inflate(R.layout.scan_display, parent, false);
        TextView room = (TextView) messageView.findViewById(R.id.scan_label);
        TextView readings = (TextView) messageView.findViewById(R.id.scan_readings);

        WifiScan scan = mScans.get(position);
        room.setText("Room " + scan.getmRoom());
        readings.setText(scan.getmResult());

        return messageView;
        }
}
