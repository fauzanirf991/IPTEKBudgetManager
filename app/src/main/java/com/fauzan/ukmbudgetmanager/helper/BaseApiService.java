package com.fauzan.ukmbudgetmanager.helper;

import com.fauzan.ukmbudgetmanager.divaset.aset;
import com.fauzan.ukmbudgetmanager.divdana.pagudana;
import com.fauzan.ukmbudgetmanager.divkas.kas;
import com.fauzan.ukmbudgetmanager.divkas.kastotal;
import com.fauzan.ukmbudgetmanager.divkegiatan.kegiatan;
import com.fauzan.ukmbudgetmanager.divlaporan.asetTotal;
import com.fauzan.ukmbudgetmanager.divlaporan.danaTotal;
import com.fauzan.ukmbudgetmanager.divlaporan.kegiatanTotal;
import com.fauzan.ukmbudgetmanager.divlaporan.sisaDana;
import com.fauzan.ukmbudgetmanager.divlaporan.ttlPengeluaran;
import com.fauzan.ukmbudgetmanager.divpengeluaran.eventneeds;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface BaseApiService {
    @FormUrlEncoded
    @POST("login.php")
    Call<ResponseBody> loginRequest(@Field("username") String username,
                                    @Field("password") String password);


    @FormUrlEncoded
    @POST("register.php")
    Call<ResponseBody> registerRequest(@Field("nama") String nama,
                                       @Field("username") String username,
                                       @Field("password") String password);

    @FormUrlEncoded
    @POST("checkemail.php")
    Call<ResponseBody>checkUsername(@Field("username") String username);

    @FormUrlEncoded
    @POST("changepassword.php")
    Call<ResponseBody>changePassword(@Field("username") String username,
                                     @Field("password") String password);

    //pagudana
    @FormUrlEncoded
    @POST("add_dana.php")
    Call<pagudana> add_dana(@Field("key") String key,
                            @Field("jumlah_dana") int jumlah_dana,
                            @Field("tgl_diterima") String tgl_diterima,
                            @Field("bukti") String bukti);

    @POST("get_dana.php")
    Call<List<pagudana>> get_dana();

    @POST("get_danasttc.php")
    Call<List<danaTotal>> get_danaTotal();

    @POST("get_ttlpengeluaran.php")
    Call<List<ttlPengeluaran>> get_ttlPengeluaran();

    @POST("get_sisadana.php")
    Call<List<sisaDana>> get_sisaDana();

    @FormUrlEncoded
    @POST("update_dana.php")
    Call<pagudana> update_dana(@Field("key") String key,
                               @Field("id_pagudana") int id_pagudana,
                               @Field("jumlah_dana") int jumlah_dana,
                               @Field("tgl_diterima") String tgl_diterima,
                               @Field("bukti") String bukti);

    @FormUrlEncoded
    @POST("delete_dana.php")
    Call<pagudana> delete_dana(@Field("key") String key,
                                @Field("id_pagudana") int id_pagudana,
                                @Field("bukti") String bukti);


    //kegiatan
    @FormUrlEncoded
    @POST("add_event.php")
    Call<kegiatan> add_kegiatan(@Field("key") String key,
                            @Field("nama_kegiatan") String nama_kegiatan,
                            @Field("tgl_kegiatan") String tgl_kegiatan);

    @POST("get_event.php")
    Call<List<kegiatan>> get_kegiatan();

    @FormUrlEncoded
    @POST("update_event.php")
    Call<kegiatan> update_kegiatan(@Field("key") String key,
                                @Field("id_kegiatan") int id_kegiatan,
                                @Field("nama_kegiatan") String nama_kegiatan,
                                @Field("tgl_kegiatan") String tgl_kegiatan);

    @FormUrlEncoded
    @POST("delete_event.php")
    Call<kegiatan> delete_kegiatan(@Field("key") String key,
                            @Field("id_kegiatan") int id_kegiatan);

    //kebutuhan kegiatan
    @FormUrlEncoded
    @POST("get_eventneeds.php")
    Call<List<eventneeds>> get_eventneeds( @Field("id_kegiatan") int id_kegiatan);

    @POST("get_eventtotal.php")
    Call<List<kegiatanTotal>> get_Totalkegiatan();

    @FormUrlEncoded
    @POST("add_eventneeds.php")
    Call<eventneeds> add_eventneeds(@Field("key") String key,
                        @Field("nama_pengeluaran") String nama_pengeluaran,
                        @Field("jumlah_item") int jumlah_item,
                        @Field("harga_satuan") int harga_satuan,
                        @Field("tgl_pengeluaran") String tgl_pengeluaran,
                        @Field("bukti") String bukti,
                        @Field("id_kegiatan") int id_kegiatan);

    @FormUrlEncoded
    @POST("update_eventneeds.php")
    Call<eventneeds> update_eventneeds(@Field("key") String key,
                        @Field("id_pengeluaran") int id_pengeluaran,
                        @Field("nama_pengeluaran") String nama_pengeluaran,
                        @Field("jumlah_item") int jumlah_item,
                        @Field("harga_satuan") int harga_satuan,
                        @Field("tgl_pengeluaran") String tgl_pengeluaran,
                        @Field("bukti") String bukti,
                        @Field("id_kegiatan") int id_kegiatan);

    @FormUrlEncoded
    @POST("delete_eventneeds.php")
    Call<eventneeds> delete_eventneeds(@Field("key") String key,
                           @Field("id_pengeluaran") int id_pengeluaran,
                           @Field("bukti") String bukti);


    //kas
    @POST("get_kas.php")
    Call<List<kas>> get_kas();

    @POST("get_kastotal.php")
    Call<List<kastotal>> getTotal_kas();

    @FormUrlEncoded
    @POST("add_kas.php")
    Call<kas> add_kas(@Field("key") String key,
                        @Field("nama_pembayar") String nama_pembayar,
                        @Field("jumlah_bayar") int jumlah_bayar,
                        @Field("tgl_bayar") String tgl_bayar);

    @FormUrlEncoded
    @POST("update_kas.php")
    Call<kas> update_kas(@Field("key") String key,
                           @Field("id_kas") int id_aset,
                           @Field("nama_pembayar") String nama_pembayar,
                           @Field("jumlah_bayar") int jumlah_bayar,
                           @Field("tgl_bayar") String tgl_bayar);

    @FormUrlEncoded
    @POST("delete_kas.php")
    Call<kas> delete_kas(@Field("key") String key,
                           @Field("id_kas") int id_kas);

    //aset
    @POST("get_aset.php")
    Call<List<aset>> get_aset();

    @POST("get_asettotal.php")
    Call<List<asetTotal>> get_Totalaset();

    @FormUrlEncoded
    @POST("add_aset.php")
    Call<aset> add_aset(@Field("key") String key,
                        @Field("nama_aset") String nama_aset,
                        @Field("harga_aset") int harga_aset,
                        @Field("tgl_aset") String tgl_aset,
                        @Field("bukti") String bukti);

    @FormUrlEncoded
    @POST("update_aset.php")
    Call<aset> update_aset(@Field("key") String key,
                           @Field("id_aset") int id_aset,
                           @Field("nama_aset") String nama_aset,
                           @Field("harga_aset") int harga_aset,
                           @Field("tgl_aset") String tgl_aset,
                           @Field("bukti") String bukti);

    @FormUrlEncoded
    @POST("delete_aset.php")
    Call<aset> delete_aset(@Field("key") String key,
                           @Field("id_aset") int id_aset,
                           @Field("bukti") String bukti);


}
