package me.elkhoudiry.pub3

import BuildConfig

fun helloFromPub3(){
    val config = BuildConfig()
    println("hello from: ${config.module}, version: ${config.version}")
}