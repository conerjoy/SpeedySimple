package com.kye.coner.speedy.bean

interface SpeedyBean<T> {
    fun isSuccess() : Boolean

    fun msg() : String
}