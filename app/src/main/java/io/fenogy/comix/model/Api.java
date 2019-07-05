package io.fenogy.comix.model;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * Created by prabashk on 5/23/2018.
 */

public interface Api {


    //String BASE_URL = "https://simplifiedcoding.net/demos/";
    //String BASE_URL = "http://ec2-54-88-153-171.compute-1.amazonaws.com:8080/hc/webapi/";

    @GET
    Call<ResponseBody> downloadFileWithDynamicUrlSync(@Url String fileUrl);

    @Streaming
    @GET
    Call<ResponseBody> downloadFileStream(@Url String fileUrl);

    @GET
    Call<ComicCatalogueJSONModel> getCatalogue(@Url String catalogueUrl);

//    @GET("male_horoscopes/{id}")
//    Call<HoroscopeJSONModel> getMaleHoroscope(@Path("id") int id);
//
//    @GET("female_horoscopes/{id}")
//    Call<HoroscopeJSONModel> getFemaleHoroscope(@Path("id") int id);
//
//    @GET("match_horoscopes/{id}")
//    Call<HoroscopeJSONModel> getPartnerHoroscope(@Path("id") int id);
//
//    @POST("male_horoscopes")
//    Call<HoroscopeJSONModel> setMaleHoroscope(@Body HoroscopeJSONModel horoscopeJSONModel);
//
//    @POST("female_horoscopes")
//    Call<HoroscopeJSONModel> setFemaleHoroscope(@Body HoroscopeJSONModel horoscopeJSONModel);

}
