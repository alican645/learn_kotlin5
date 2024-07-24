package com.example.kotlinodev.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.room.Room
import com.example.kotlinodev.databinding.FragmentTarifBinding
import com.example.kotlinodev.model.Tarif
import com.example.kotlinodev.roomDB.TarifDAO
import com.example.kotlinodev.roomDB.TarifDB
import com.google.android.material.snackbar.Snackbar
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.ByteArrayOutputStream
import java.io.IOException





class TarifFragment : Fragment() {

    private var _binding: FragmentTarifBinding? = null
    private val binding get() = _binding!!
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private var secilenBitmap : Bitmap? = null
    private var secilenGorselUri : Uri? = null


    private var mDisposable= CompositeDisposable() //>>devamlı bir istek yapldığında bu istekler hafızada birikmesin diye kullan at şeklinde işlem yapan yapı
    private lateinit var db : TarifDB
    private lateinit var tarifDAO: TarifDAO

    private lateinit var tarif1 : Tarif


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        registerLauncher()

        db= Room.databaseBuilder(requireContext(),TarifDB::class.java,"Tarifler")
            .build()
        tarifDAO = db.tarifDao()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTarifBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //fragmentlerın onCreated fonksiyonu

        binding.saveBtn.setOnClickListener { saveFunc(it) }
        binding.deleteBtn.setOnClickListener {deleteFunc(it) }
        binding.imageView.setOnClickListener { selectImage(it) }

        //gelen argümanlar boş değil ise çalışacak fonksiyon satırı
        arguments?.let {
            val bilgi = TarifFragmentArgs.fromBundle(it).bilgi
            val id = TarifFragmentArgs.fromBundle(it).id

            if (bilgi=="yeni")
                {
                    //yeni tarif eklenecek
                    binding.saveBtn.isEnabled=true
                    binding.deleteBtn.isEnabled=false

                }
            else
                {
                    //eski tarif gönderiliyor
                    binding.saveBtn.isEnabled=false
                    binding.deleteBtn.isEnabled=true
                    val id = TarifFragmentArgs.fromBundle(it).id

                    mDisposable.add(tarifDAO.findById(id)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        //subscribe() >> bu metodun için bu işlem sonucunda gerçekleşecek bir fonksiyon yazılabilir
                        // örneğin işlem sonucunda önceki sayfaya dönüş yaptıran fonksiyon gibi
                        .subscribe(this::handleResponseForFindById))
                }
        }
    }

    private fun  handleResponseForFindById(tarif : Tarif){
        binding.isimText.setText(tarif.isim)
        binding.tarifText.setText(tarif.malzeme)

        val bitmap=BitmapFactory.decodeByteArray(tarif.gorsel,0,tarif.gorsel.size)
        binding.imageView.setImageBitmap(bitmap)

        tarif1=tarif
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    fun saveFunc(view: View){
        val isim = binding.isimText.text.toString()
        val malzeme = binding.tarifText.text.toString()

        if(secilenBitmap!=null){
            val smallBitmap = smallBitmap(secilenBitmap = secilenBitmap!!, maxSize = 300)
            val outputStream= ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            val byteDizisi = outputStream.toByteArray()

            val tarif = Tarif(isim = isim, malzeme = malzeme, gorsel = byteDizisi)

            //RxJava İşlemleri
            mDisposable.add(
            tarifDAO.insert(tarif)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                //subscribe() >> bu metodun için bu işlem sonucunda gerçekleşecek bir fonksiyon yazılabilir
                // örneğin işlem sonucunda önceki sayfaya dönüş yaptıran fonksiyon gibi
                .subscribe(this::handleResponseForInsert))
        }
    }
    private fun handleResponseForInsert(){
        val action = TarifFragmentDirections.actionTarifFragmentToListeFragment()
        Navigation.findNavController(requireView()).navigate(action)
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    fun deleteFunc(view: View){
        mDisposable.add(tarifDAO.delete(tarif = tarif1)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            //subscribe() >> bu metodun için bu işlem sonucunda gerçekleşecek bir fonksiyon yazılabilir
            // örneğin işlem sonucunda önceki sayfaya dönüş yaptıran fonksiyon gibi
            .subscribe(this::handleResponseForDelete))
    }
    private fun handleResponseForDelete(){
        val action = TarifFragmentDirections.actionTarifFragmentToListeFragment()
        Navigation.findNavController(requireView(  )).navigate(action)
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    private fun registerLauncher() {
        activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val intentFromResult = result.data
                if (intentFromResult != null) {
                    secilenGorselUri = intentFromResult.data
                    try {
                        if (Build.VERSION.SDK_INT >= 28) {
                            val source = ImageDecoder.createSource(
                                requireActivity().contentResolver,
                                secilenGorselUri!!
                            )
                            secilenBitmap = ImageDecoder.decodeBitmap(source)
                            binding.imageView.setImageBitmap(secilenBitmap)
                        } else {
                            secilenBitmap = MediaStore.Images.Media.getBitmap(
                                requireActivity().contentResolver,
                                secilenGorselUri
                            )
                            binding.imageView.setImageBitmap(secilenBitmap)
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { result ->
            if (result) {
                //permission granted
                val intentToGallery =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            } else {
                //permission denied
                Toast.makeText(requireContext(), "Permisson needed!", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    fun selectImage(view: View){
        activity?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(
                        requireActivity().applicationContext,
                        Manifest.permission.READ_MEDIA_IMAGES
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            requireActivity(),
                            Manifest.permission.READ_MEDIA_IMAGES
                        )
                    ) {
                        Snackbar.make(
                            view,
                            "Permission needed for gallery",
                            Snackbar.LENGTH_INDEFINITE
                        ).setAction("Give Permission",
                            View.OnClickListener {
                                permissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
                            }).show()
                    } else {
                        permissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
                    }
                } else {
                    val intentToGallery =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    activityResultLauncher.launch(intentToGallery)

                }
            } else {
                if (ContextCompat.checkSelfPermission(
                        requireActivity().applicationContext,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            requireActivity(),
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        )
                    ) {
                        Snackbar.make(
                            view,
                            "Permission needed for gallery",
                            Snackbar.LENGTH_INDEFINITE
                        ).setAction("Give Permission",
                            View.OnClickListener {
                                permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                            }).show()
                    } else {
                        permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                } else {
                    val intentToGallery =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    activityResultLauncher.launch(intentToGallery)

                }
            }
        }
    }

    private fun smallBitmap(secilenBitmap: Bitmap,maxSize:Int) : Bitmap{
        var with = secilenBitmap.width
        var height = secilenBitmap.height

        val bitmapOran : Double = with!!.toDouble()/height!!.toDouble()
        if (bitmapOran>1){
            //görsel yatay
            with=maxSize
            val smallHeight=with*bitmapOran
            height = smallHeight.toInt()
        }else{
            //görsel dikkey
            height=maxSize
            val smallWidth=height*bitmapOran
        }

        return Bitmap.createScaledBitmap(secilenBitmap,100,100,true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mDisposable.clear()
    }
}