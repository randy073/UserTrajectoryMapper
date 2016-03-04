package companyname.sensor2;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaScannerConnection;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private int samplenumber = 3;
    private int sample = 0;
    private float threshold = (float) 0.92;
    private SensorManager mSensorManager;
    private Sensor sensor;
    private float time_start;
    private int steps = 0;
    private float mstart_time = -1;
    private float astart_time = -1;
    private float lstart_time = -1;
    private float gstart_time = -1;
    private float amag_val_curr = 0;
    private float amag_val_old = 0;
    private float amag_mean_old = 0;
    private float amag_mean_curr = 0;
    private float amag_val_tenative_peak = -5000;
    private float amag_variance_old = 0;
    private float amag_std_sample = 0;
    private float amag_variance_curr = 100000;
    private float amag_std_dev = 0;
    private float [] amag_peaks = new float [samplenumber];
    private int counter = 0;
    private float time_stamp;
    private float[] gval = new float[3];
    private float[] aval = new float[3];
    private float lval;
    private float[] mval = new float[3];
    private boolean running = false;

    TextView time_text = null;
    TextView accel_text = null;
    TextView mag_text = null;
    TextView gyro_text = null;
    TextView light_text = null;
    EditText log_name = null;
    TextView step_text = null;
    Button button = null;

    ImageView drawingImageView;
    Paint paint;
    Canvas canvas;
    private int maxX;
    private int maxY;

    private int lastX;
    private int lastY;


    public BufferedWriter mBufferedWriter;
    public File mFile;
    boolean created;

    public void startstop(View view) {
        if (running == false) {

            try {
                long timestamp = System.currentTimeMillis();
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm:ss");
                Date mdate = new Date(timestamp);

                mFile = new File(this.getExternalFilesDir(null), log_name.getText() + "-" + sdf.format(mdate) + ".csv");
                created = mFile.createNewFile();

                mBufferedWriter = new BufferedWriter(new FileWriter(mFile, false));
                MediaScannerConnection.scanFile(this, new String[]{mFile.toString()}, null, null);
                if (mBufferedWriter == null)
                    Log.e("Sensortest", "didn't expect this");
            } catch (IOException e) {
                Log.e("SensorTest", "Unable to write to SensorLog.txt");

            }
            running = true;
            time_start = System.nanoTime();

            mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            button.setText("stop");
            mSensorManager.registerListener(aSensorEventListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);
            mSensorManager.registerListener(mSensorEventListener, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_FASTEST);
            mSensorManager.registerListener(gSensorEventListener, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_FASTEST);
            mSensorManager.registerListener(lSensorEventListener, mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_FASTEST);
        } else {
            button.setText("record");

            try {
                if (mBufferedWriter == null)
                    Log.e("sensortest", "something went horrible wrong");
                else
                    mBufferedWriter.close();
            } catch (IOException e) {
                Log.e("Sensorttest", "couldn't close the writer");
            }
            MediaScannerConnection.scanFile(this, new String[]{mFile.toString()}, null, null);
            running = false;
            mstart_time = -1;
            astart_time = -1;
            lstart_time = -1;
            gstart_time = -1;
            counter = 0;
            steps = 0;
            mSensorManager.unregisterListener(aSensorEventListener);
            mSensorManager.unregisterListener(mSensorEventListener);
            mSensorManager.unregisterListener(gSensorEventListener);
            mSensorManager.unregisterListener(lSensorEventListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (running) {
            mSensorManager.registerListener(aSensorEventListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);
            mSensorManager.registerListener(mSensorEventListener, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_FASTEST);
            mSensorManager.registerListener(gSensorEventListener, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_FASTEST);
            mSensorManager.registerListener(lSensorEventListener, mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_FASTEST);

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(aSensorEventListener);
        mSensorManager.unregisterListener(mSensorEventListener);
        mSensorManager.unregisterListener(gSensorEventListener);
        mSensorManager.unregisterListener(lSensorEventListener);
    }
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        drawingImageView = (ImageView) this.findViewById(R.id.DrawingImageView);
        Bitmap bitmap = Bitmap.createBitmap((int) getWindowManager()
                .getDefaultDisplay().getWidth(), (int) getWindowManager()
                .getDefaultDisplay().getHeight(), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        drawingImageView.setImageBitmap(bitmap);

        Display mdisp = getWindowManager().getDefaultDisplay();
        Point mdispSize = new Point();
        mdisp.getSize(mdispSize);
        maxX = mdispSize.x;
        maxY = mdispSize.y;

        // Line
        paint = new Paint();
        paint.setColor(Color.rgb(255, 153, 51));
        paint.setStrokeWidth(10);

        lastX = maxX/2;
        lastY = maxY - 10;

        canvas.drawCircle(lastX, lastY, 10, paint);




        time_start = System.nanoTime();
//        accel_text = (TextView) findViewById(R.id.accel_text);
//        mag_text = (TextView) findViewById(R.id.mag_text);
//        gyro_text = (TextView) findViewById(R.id.gyro_text);
//        light_text = (TextView) findViewById(R.id.light_text);
//        time_text = (TextView) findViewById(R.id.time_text);
        log_name = (EditText) findViewById(R.id.log_name);
//        step_text = (TextView) findViewById(R.id.step_text);
//
//        float current_time = (System.nanoTime() - time_start);
//        current_time = current_time / 1000000;
//
//        time_text.setText(current_time + "ms");

        button = (Button) findViewById(R.id.startstop_button);
        button.setText("record");
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    }

    private void drawPoint(int nextX, int nextY){
        canvas.drawLine(lastX, lastY, nextX, nextY, paint);
        canvas.drawCircle(nextX, nextY,10,paint);
    }
    private SensorEventListener mSensorEventListener = new SensorEventListener() {
        @Override

        public void onSensorChanged(SensorEvent event) {

            Sensor sensor = event.sensor;
            if (mstart_time == -1)
                mstart_time = event.timestamp;

            mval[0] = event.values[0];
            mval[1] = event.values[1];
            mval[2] = event.values[2];

            float current_time = (System.nanoTime() - time_start);
            current_time = current_time / 1000000;

           // time_text.setText(current_time + "ms");

            try {
                if (mBufferedWriter != null) {
                    mBufferedWriter.write(current_time + ", " + aval[0] + ", " + aval[1] + ", " + aval[2] + ", " + gval[0] + ", " + gval[1] + ", " + gval[2] +
                            ", " + mval[0] + ", " + mval[1] + ", " + mval[2] + ", " + lval + "\n");


                }
            } catch (IOException e) {
                Log.e("sensortest", "fail to write");
            }
            float mtime = (event.timestamp - mstart_time) / 1000000;
            if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                //mag_text.setText("Magnetic(MicroTesla) \n" + "time: " + mtime + "\nx: " + event.values[0] + "\ny: " + event.values[1] + "\nz: " + event.values[2]);

            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
    private SensorEventListener gSensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            Sensor sensor = event.sensor;
            if (gstart_time == -1)
                gstart_time = event.timestamp;
            gval[0] = event.values[0];
            gval[1] = event.values[1];
            gval[2] = event.values[2];
            float current_time = (System.nanoTime() - time_start);
            current_time = current_time / 1000000;

           // time_text.setText(current_time + "ms");

            try {
                if (mBufferedWriter != null) {
                    mBufferedWriter.write(current_time + ", " + aval[0] + ", " + aval[1] + ", " + aval[2] + ", " + gval[0] + ", " + gval[1] + ", " + gval[2] +
                            ", " + mval[0] + ", " + mval[1] + ", " + mval[2] + ", " + lval + "\n");


                }
            } catch (IOException e) {
                Log.e("sensortest", "fail to write");
            }
            float gtime = (event.timestamp - gstart_time) / 1000000;
            if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
               // gyro_text.setText("Gyroscope (rad/s) \n" + "time: " + gtime + "\nx: " + event.values[0] + "\ny: " + event.values[1] + "\nz: " + event.values[2]);

            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
    private SensorEventListener lSensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            Sensor sensor = event.sensor;
            if (lstart_time == -1)
                lstart_time = event.timestamp;

            lval = event.values[0];
            float current_time = (System.nanoTime() - time_start);
            current_time = current_time / 1000000;

           // time_text.setText(current_time + "ms");

            try {
                if (mBufferedWriter != null) {
                    mBufferedWriter.write(current_time + ", " + aval[0] + ", " + aval[1] + ", " + aval[2] + ", " + gval[0] + ", " + gval[1] + ", " + gval[2] +
                            ", " + mval[0] + ", " + mval[1] + ", " + mval[2] + ", " + lval + "\n");


                }
            } catch (IOException e) {
                Log.e("sensortest", "fail to write");
            }
            float ltime = (event.timestamp - lstart_time) / 1000000;
            if (sensor.getType() == Sensor.TYPE_LIGHT) {
                //light_text.setText("Light(lux units) \n" + "time: " + ltime + "\n" + event.values[0]);

            }

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
    private SensorEventListener aSensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {

            Sensor sensor = event.sensor;



            if (astart_time == -1)
                astart_time = event.timestamp;

            aval[0] = event.values[0];
            aval[1] = event.values[1];
            aval[2] = event.values[2];

            amag_val_old = amag_val_curr;
            amag_val_curr = (float) Math.sqrt( Math.pow((float)aval[0],2) + Math.pow((float)aval[1], 2) + Math.pow((float)aval[2], 2));



            if (amag_val_tenative_peak > amag_val_curr) {
                //Log.e("Hit2", amag_val_tenative_peak + "> " + amag_val_curr);
                if (sample != 0) {

                    if (Math.abs(amag_val_tenative_peak - amag_peaks[sample -1])  > 0.1) {
                        Log.e("hit",sample + " " + amag_val_tenative_peak + " " + amag_peaks[sample -1]);
                        amag_peaks[sample] = amag_val_tenative_peak;
                        sample++;
                    }
                }
                else {
                    amag_peaks[sample] = amag_val_tenative_peak;
                    sample++;
                }
            }

            if (amag_val_curr > amag_val_old)
            {
                //Log.e("Hit1", amag_val_old + " <" + amag_val_curr);
                amag_val_tenative_peak = amag_val_curr;


            }

            if (sample >= samplenumber) {
                //Log.e("Hit", "We hit the sampe!");
                if (amag_std_sample >= 1)
                {
                    for (int i = 0; i < samplenumber; i++){
                        if (amag_peaks[i] > (amag_mean_curr + (2* amag_std_dev)) || amag_peaks[i] < (amag_mean_curr - (2*amag_std_dev)))
                        {
                            Log.e("hit", "peak: " + amag_peaks[i] + " mean: " + amag_mean_curr + " std_dev: " + amag_std_dev );
                            steps++;
                            Log.d("STEPS", "Step detected");
                            int nextY = lastY - 50;
                            int nextX = lastX;
                            drawPoint(nextX,nextY);
                        }
                    }
                }
                sample = 0;
                counter = 0;

            }


            if (counter == 0)
            {
                amag_mean_curr = amag_val_curr;
                amag_variance_curr = 0;
                counter++;
                amag_std_sample = 0;
            }
            else
            {
                counter++;
                amag_mean_old = amag_mean_curr;
                amag_variance_old = amag_variance_curr;
                amag_mean_curr = amag_mean_old + ((amag_val_curr - amag_mean_old)/counter);
                amag_variance_curr = amag_variance_old + ((amag_val_curr - amag_mean_old) * (amag_val_curr -  amag_mean_curr));
                amag_std_sample = amag_variance_curr/ (counter - 1);
            }

            amag_std_dev = (float) Math.sqrt(amag_std_sample);
            //step_text.setText("mean:\n" + amag_mean_curr + "\nvariance\n" + (amag_std_sample) + "\nstd dev\n" + amag_std_dev + "\nsteps: " + steps);



            float current_time = (System.nanoTime() - time_start);
            current_time = current_time / 1000000;
            //time_text.setText(current_time + "ms");

            try {
                if (mBufferedWriter != null) {
                    mBufferedWriter.write(current_time + ", " + aval[0] + ", " + aval[1] + ", " + aval[2] + ", " + gval[0] + ", " + gval[1] + ", " + gval[2] +
                            ", " + mval[0] + ", " + mval[1] + ", " + mval[2] + ", " + lval + "\n");


                }
            } catch (IOException e) {
                Log.e("sensortest", "fail to write");
            }
            float atime = (event.timestamp - astart_time) / 1000000;
            if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
               // accel_text.setText("Accelerometer (m/s^2) \n" + "time: " + atime + "\nx: " + event.values[0] + "\ny: " + event.values[1] + "\nz: " + event.values[2]);

            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
