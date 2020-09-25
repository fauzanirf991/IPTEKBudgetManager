package com.fauzan.ukmbudgetmanager.helper;

public class UtilsApi {
    // 10.0.2.2 ini adalah localhost.
    //public static final String BASE_URL_API = "http://students.ti.elektro.polnep.ac.id/~m1716075/ukmbudget/";
    public static final String BASE_URL_API = "http://192.168.43.209/ukmbudget/";
    // Mendeklarasikan Interface BaseApiService
    public static BaseApiService getAPIService(){
        return RetrofitClient.getClient(BASE_URL_API).create(BaseApiService.class);
    }
}
