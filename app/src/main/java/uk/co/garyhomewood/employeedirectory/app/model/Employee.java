package uk.co.garyhomewood.employeedirectory.app.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import uk.co.garyhomewood.employeedirectory.app.EmployeeList;

public class Employee {
    private String name;
    private String title;
    private String bio;
    private String photoUrl;
    private Bitmap photo;
    private EmployeeList.EmployeeListAdapter adapter;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public Bitmap getPhoto() {
        return photo;
    }

    public void loadPhoto(EmployeeList.EmployeeListAdapter adapter) {
        this.adapter = adapter;
        if (photoUrl != null) {
            new PhotoLoader().execute(photoUrl);
        }
    }

    private class PhotoLoader extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap bitmap = getBitmapFromURL(params[0], 168);
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                photo = bitmap;
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }

    public static Bitmap getBitmapFromURL(String src, int radius) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            Bitmap scaledBitmap;
            if ((bitmap.getWidth() != radius) || (bitmap.getHeight() != radius)) {
                scaledBitmap = Bitmap.createScaledBitmap(bitmap, radius, radius, false);
            } else {
                scaledBitmap = bitmap;
            }

            Bitmap circular = Bitmap.createBitmap(scaledBitmap.getWidth(), scaledBitmap.getHeight(), Bitmap.Config.ARGB_8888);

            Canvas canvas = new Canvas(circular);

            Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
            RectF rectF = new RectF(rect);

            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setDither(true);

            canvas.drawARGB(0,0,0,0);
            canvas.drawCircle(scaledBitmap.getWidth() / 2 , scaledBitmap.getHeight() / 2 , scaledBitmap.getWidth() / 2 , paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(scaledBitmap, rect, rect, paint);

            return circular;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
