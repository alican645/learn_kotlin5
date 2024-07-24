package com.example.kotlinodev

import com.example.kotlinodev.databinding.RecyclerItemBinding
import com.example.kotlinodev.model.Tarif

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation

import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinodev.view.ListeFragmentDirections

class TarifAdapter(val tarifListesi : List<Tarif>) :
    RecyclerView.Adapter<TarifAdapter.TarifViewHolder>() {
        class TarifViewHolder (val binding: RecyclerItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, ViewType: Int) : TarifViewHolder {
        val binding = RecyclerItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return TarifViewHolder(binding)
    }
    override fun getItemCount(): Int {
        return tarifListesi.size
    }
    override fun onBindViewHolder(holder: TarifViewHolder, position: Int) {
        holder.binding.recyclerItem.text=tarifListesi[position].isim.toString()
        //bu kısım adapter içerisinde bulunan itemlere tıklanma özelliği ekler.
        holder.itemView.setOnClickListener{
            val action = ListeFragmentDirections.actionListeFragmentToTarifFragment(bilgi = "eski", id = tarifListesi[position].id)
            Navigation.findNavController(it).navigate(action)
        }
    }



}