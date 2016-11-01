package tcslab.syndesiapp.controllers.sensor;

import android.hardware.Sensor;
import tcslab.syndesiapp.R;

/**
 * Created by Blaise on 27.05.2015.
 */
public class SensorList {
    public static Integer[] sensorUsed = new Integer[]
            {Sensor.TYPE_LIGHT, Sensor.TYPE_AMBIENT_TEMPERATURE, Sensor.TYPE_PRESSURE, Sensor.TYPE_RELATIVE_HUMIDITY/*, Sensor.TYPE_PROXIMITY*/};

    public static String getStringType(int sensorType){
        String stringType;
        switch (sensorType){
            case Sensor.TYPE_LIGHT: stringType = "illuminance"; break;
            case Sensor.TYPE_AMBIENT_TEMPERATURE: stringType = "temperature"; break;
            case Sensor.TYPE_PRESSURE: stringType = "pressure"; break;
            case Sensor.TYPE_RELATIVE_HUMIDITY: stringType = "humidity"; break;
//            case Sensor.TYPE_PROXIMITY: stringType = "proximity"; break;
            default: stringType = "UNDEFINED"; break;
        }
        return stringType;
    }

    public static String getStringUnit(int sensorType){
        String stringType;
        switch (sensorType){
            case Sensor.TYPE_LIGHT: stringType = "lux"; break;
            case Sensor.TYPE_AMBIENT_TEMPERATURE: stringType = "celsius"; break;
            case Sensor.TYPE_PRESSURE: stringType = "hPa"; break;
            case Sensor.TYPE_RELATIVE_HUMIDITY: stringType = "%25"; break;
//            case Sensor.TYPE_PROXIMITY: stringType = "cm"; break;
            default: stringType = "Undefined"; break;
        }
        return stringType;
    }

    public static int getIcon(int sensorType){
        int icon;
        switch (sensorType){
            case Sensor.TYPE_LIGHT: icon = R.drawable.sensor_light; break;
            case Sensor.TYPE_AMBIENT_TEMPERATURE: icon = R.drawable.sensor_temperature; break;
            case Sensor.TYPE_PRESSURE: icon = R.drawable.sensor_pressure; break;
            case Sensor.TYPE_RELATIVE_HUMIDITY: icon = R.drawable.sensor_humidity; break;
//            case Sensor.TYPE_PROXIMITY: icon = R.drawable.sensor_proximity; break;
            default: icon = R.drawable.sensor; break;
        }
        return icon;
    }
}
