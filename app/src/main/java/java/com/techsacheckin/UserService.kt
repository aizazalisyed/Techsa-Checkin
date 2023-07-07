package java.com.techsacheckin

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface UserService {


    @GET("current.json")
    fun getCurrentWeatherAsync(
        @Query("key") apiKey: String,
        @Query("q") location: String,
        @Query("aqi") aqi: String
    ): Call<WeatherResponse>

}