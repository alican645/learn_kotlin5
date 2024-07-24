package com.example.kotlinodev.roomDB

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.kotlinodev.model.Tarif
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable

//flowable>>yapacağımız işlem sonucu bize bir veri dönecekse flowable kullanılır.
//completable >> eğer yapacağımız işlem sonucu bize bir veri dönmeyecekse insert etmek ve ya delete etmek gibi completable kullanırlır.
@Dao
interface TarifDAO {
    @Query("SELECT * FROM tarif")
    fun getAll() : Flowable<List<Tarif>>

    @Query("SELECT * FROM TARIF WHERE id= :id")
    fun findById(id : Int) : Flowable<Tarif>

    @Insert
    fun insert(tarif: Tarif) : Completable

    @Delete
    fun delete(tarif: Tarif) : Completable
}