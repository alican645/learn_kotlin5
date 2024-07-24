package com.example.kotlinodev.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.example.kotlinodev.TarifAdapter
import com.example.kotlinodev.databinding.FragmentListeBinding
import com.example.kotlinodev.model.Tarif
import com.example.kotlinodev.roomDB.TarifDAO
import com.example.kotlinodev.roomDB.TarifDB
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class ListeFragment : Fragment() {

    private var _binding: FragmentListeBinding? = null
    private val binding get() = _binding!!

    private var mDisposable= CompositeDisposable() //>>devamlı bir istek yapldığında bu istekler hafızada birikmesin diye kullan at şeklinde işlem yapan yapı
    private lateinit var db : TarifDB
    private lateinit var tarifDAO: TarifDAO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db= Room.databaseBuilder(requireContext(),TarifDB::class.java,"Tarifler")
            .build()
        tarifDAO = db.tarifDao()


    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentListeBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //fragmentlerın onCreated fonksiyonu

        binding.floatingActionButton.setOnClickListener { yeniEkle(it) }
        binding.tarifRV.layoutManager=LinearLayoutManager(requireContext())
        mDisposable.add(
            tarifDAO.getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponseForGetAll)
        )
    }

    private fun handleResponseForGetAll(tarifListesi:List<Tarif>){
        val adapter = TarifAdapter(tarifListesi)

        binding.tarifRV.adapter=adapter
    }

    fun yeniEkle(view: View){
        val action = ListeFragmentDirections.actionListeFragmentToTarifFragment(bilgi = "yeni", id = 0)
        Navigation.findNavController(view).navigate(action)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mDisposable.clear()
    }
}


