package vn.edu.tom.baithuchanh;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface Api {
    @GET("android/api.aspx")
    Call<LastIDModel> getLastIdData(@Query("action") String action);

}
