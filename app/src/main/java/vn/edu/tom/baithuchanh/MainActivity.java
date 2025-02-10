package vn.edu.tom.baithuchanh;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    TextView id_moi;
    Api api;
    int last_ID;
    private static final String CHANNEL_ID = "my_channel_id";
    SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        api = RetrofitClient.getClient().create(Api.class);
        id_moi = findViewById(R.id.id_moi);
        lastId();
        sharedPreferences = getSharedPreferences("mypref",MODE_PRIVATE);
        last_ID = sharedPreferences.getInt("lastid",0);
        Handler handler = new Handler(Looper.getMainLooper());

        Runnable periodicTask = new Runnable() {
            @Override
            public void run() {
                lastId();
                handler.postDelayed(this, 30000); // Lặp lại sau 30 giây
            }
        };
        handler.post(periodicTask);
    }

    private void lastId() {
        Call<LastIDModel> call = api.getLastIdData("last_id");
        call.enqueue(new Callback<LastIDModel>() {
            @Override
            public void onResponse(Call<LastIDModel> call, Response<LastIDModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LastIDModel myResponse = response.body();
                    if(last_ID < myResponse.getLast_id()) {
                        id_moi.setText("" + myResponse.getLast_id());

                        showNotification("last ID moi",myResponse.getLast_id()+"");
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt("lastid",myResponse.getLast_id());
                    }
                } else {

                }
            }

            @Override
            public void onFailure(Call<LastIDModel> call, Throwable t) {

                Log.d("loi",t.getMessage());
            }
        });
    }

    private void showNotification(String title, String body) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Tên kênh thông báo",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Mô tả kênh thông báo");
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager.notify(1, builder.build());
    }
}