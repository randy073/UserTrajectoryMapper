package companyname.sensor2;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.widget.ImageView;

public class DrawActivity extends Activity {
    ImageView drawingImageView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);
        drawingImageView = (ImageView) this.findViewById(R.id.DrawingImageView);
        Bitmap bitmap = Bitmap.createBitmap((int) getWindowManager()
                .getDefaultDisplay().getWidth(), (int) getWindowManager()
                .getDefaultDisplay().getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawingImageView.setImageBitmap(bitmap);

        // Line
        Paint paint = new Paint();
        paint.setColor(Color.rgb(255, 153, 51));
        paint.setStrokeWidth(10);
        int startx = 50;
        int starty = 90;
        int endx = 150;
        int endy = 360;
        canvas.drawLine(startx, starty, endx, endy, paint);

    }

}