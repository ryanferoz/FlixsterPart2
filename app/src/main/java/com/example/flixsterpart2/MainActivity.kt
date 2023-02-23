package com.example.flixsterpart2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import okhttp3.*
import java.io.IOException

@Parcelize
data class TvShow(
    val name: String,
    @SerializedName("poster_path") val posterPath: String,
    val overview: String,
    @SerializedName("first_air_date") val firstAirDate: String,
    @SerializedName("vote_average") val voteAverage: Double,
    @SerializedName("vote_count") val voteCount: Int
) : Parcelable

data class TvShowsResponse(
    val results: List<TvShow>
)

class TvShowsAdapter(
    private val tvShows: List<TvShow>,
    private val onClick: (TvShow) -> Unit
) : RecyclerView.Adapter<TvShowsAdapter.TvShowViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TvShowViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.tv_show_row, parent, false)
        return TvShowViewHolder(view)
    }

    override fun onBindViewHolder(holder: TvShowViewHolder, position: Int) {
        val tvShow = tvShows[position]

        Glide.with(holder.itemView)
            .load("https://image.tmdb.org/t/p/w500${tvShow.posterPath}")
            .into(holder.thumbnail)

        holder.title.text = tvShow.name
        holder.rating.text = tvShow.voteAverage.toString()

        holder.itemView.setOnClickListener {
            onClick(tvShow)
        }
    }

    override fun getItemCount() = tvShows.size

    inner class TvShowViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val thumbnail: ImageView = view.findViewById(R.id.thumbnail)
        val title: TextView = view.findViewById(R.id.title)
        val rating: TextView = view.findViewById(R.id.rating)
    }
}

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView

    companion object {
        const val TV_SHOW_EXTRA_KEY = "TV_SHOW"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val client = OkHttpClient()

        val request = Request.Builder()
            .url("https://api.themoviedb.org/3/tv/popular?api_key=a07e22bc18f5cb106bfe4cc1f83ad8ed&language=en-US&page=1")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                val gson = Gson()
                val tvShowsResponse = gson.fromJson(response.body?.string(), TvShowsResponse::class.java)

                runOnUiThread {
                    recyclerView.adapter = TvShowsAdapter(tvShowsResponse.results) { tvShow ->
                        val intent = Intent(this@MainActivity, TvShowDetailsActivity::class.java)
                        intent.putExtra(TV_SHOW_EXTRA_KEY, tvShow)
                        startActivity(intent)
                    }
                }
            }
        })
    }
}

class TvShowDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tv_show_details)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }}