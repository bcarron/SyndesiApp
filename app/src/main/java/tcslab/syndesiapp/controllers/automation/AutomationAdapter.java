package tcslab.syndesiapp.controllers.automation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import tcslab.syndesiapp.R;
import tcslab.syndesiapp.controllers.sensor.SensorList;

import java.util.ArrayList;
import java.util.Date;

/**
 * View used to display one automation message.
 *
 * Created by Blaise on 02.06.2015.
 */
public class AutomationAdapter extends ArrayAdapter<AutomationStatus> {
private final Context mAppContext;
private final ArrayList<AutomationStatus> mStatuses;

public AutomationAdapter(Context context, ArrayList<AutomationStatus> mStatuses) {
        super(context, R.layout.automation_display, mStatuses);
        this.mAppContext = context;
        this.mStatuses = mStatuses;
        }

@Override
public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mAppContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View messageView = inflater.inflate(R.layout.automation_display, parent, false);
        TextView office = (TextView) messageView.findViewById(R.id.office);
        TextView message = (TextView) messageView.findViewById(R.id.message);
        ImageView image = (ImageView) messageView.findViewById(R.id.icon);

        AutomationStatus status = mStatuses.get(position);
        office.setText("Office " + status.getmOffice());
        message.setText(status.getmMessage());
        image.setImageResource(status.getmNodeType().getIcon(status.getmStatus()));

        return messageView;
        }
}
