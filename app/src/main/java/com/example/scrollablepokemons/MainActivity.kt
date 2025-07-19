package com.example.scrollablepokemons

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.codepath.asynchttpclient.AsyncHttpClient
import com.codepath.asynchttpclient.RequestParams
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import com.example.scrollablepokemons.MainActivity.PokemonData
import okhttp3.Headers
import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager


class MainActivity : AppCompatActivity() {

    lateinit var pokemonList: ArrayList<PokemonData>
    lateinit var rvPokemons: RecyclerView
    lateinit var adapter: PokemonAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        pokemonList = ArrayList()
        rvPokemons = findViewById<RecyclerView>(R.id.rvPokemons)
        adapter = PokemonAdapter(pokemonList)
        rvPokemons.adapter = adapter
        rvPokemons.layoutManager = LinearLayoutManager(this@MainActivity,LinearLayoutManager.HORIZONTAL, false)


        getPokemon()
    }

    class PokemonAdapter(private val pokemonList: List<PokemonData>): RecyclerView.Adapter<PokemonAdapter.ViewHolder>(){
        inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
            val pokemonImage: ImageView = itemView.findViewById(R.id.imageView2)
            val pokemonName: TextView = itemView.findViewById(R.id.nameDescription)
            val pokemonAbilities: TextView = itemView.findViewById(R.id.AbilityDescription)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pokemon_card,parent,false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val pokemon = pokemonList[position]
            Glide.with(holder.itemView.context).load(pokemon.imageUrl).into(holder.pokemonImage)
            holder.pokemonName.text = pokemon.name
            holder.pokemonAbilities.text = pokemon.abilities.toString().substring(1,pokemon.abilities.toString().length-1)
        }

        override fun getItemCount(): Int {
            return pokemonList.size
        }

    }

    data class PokemonData(
        val name: String,
        val abilities: List<String>,
        val imageUrl: String?
    )

    fun getPokemon(){
        val client = AsyncHttpClient()
        val params = RequestParams()
        for(i in 1..20){
            params["limit"] = "1"
            params["page"] = "0"
            val randomId = (1..1025).random()
            client["https://pokeapi.co/api/v2/pokemon/$randomId", params, object:
                JsonHttpResponseHandler() {
                override fun onSuccess(statusCode: Int, headers: Headers, json: JSON) {
                    // called when response HTTP status is "200 OK"
//                Log.d("DEBUG ARRAY", json.jsonArray.toString())
//                Log.d("DebugPokemon", "response successful")
                    //Get the Pokemon name
                    Log.d("DEBUG OBJECT", json.jsonObject.toString())
                    val name = json.jsonObject.getString("name")
                    //Get the Pokemon abilities
                    Log.d("POKEMON_Debug", "Name:" + name)
                    val abilities = json.jsonObject.getJSONArray("abilities")
                    val abilityArray = Array(abilities.length()){""}
                    for(i in 0 until abilities.length()){
                        Log.d("POKEMON_Debug",abilities.getJSONObject(i).toString())
                        abilityArray[i] = abilities.getJSONObject(i).getJSONObject("ability").getString("name")
                    }
                    Log.d("POKEMON_Debug", abilities.toString())
                    //Get the Pokemon image
                    val image = json.jsonObject.getJSONObject("sprites").getString("front_shiny")
                    Log.d("POKEMON_Debug",  "Image URL: " + image)


                    //returning the Pokemon data
                    var curPokemon = PokemonData(name,abilityArray.toList(),image)
                    runOnUiThread {
                        pokemonList.add(curPokemon)
                        adapter.notifyItemInserted(pokemonList.size - 1)
                    }

                }

                override fun onFailure(
                    statusCode: Int,
                    headers: Headers?,
                    errorResponse: String,
                    t: Throwable?
                ) {
                    // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                }
            }]
        }

    }

}
