package me.elkhoudiry.pub1

import BuildConfig

fun helloFromPub1(){
    val config = BuildConfig()
    println("hello from: ${config.module}, version: ${config.version}")
}