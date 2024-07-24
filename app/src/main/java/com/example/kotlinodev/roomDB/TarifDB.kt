package com.example.kotlinodev.roomDB

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.kotlinodev.model.Tarif

@Database(entities = [Tarif::class],version=1)
abstract class TarifDB : RoomDatabase(){
    abstract  fun tarifDao() : TarifDAO
}