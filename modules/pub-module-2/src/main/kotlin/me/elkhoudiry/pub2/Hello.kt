package me.elkhoudiry.pub2

import BuildConfig

fun helloFromPub2(){
    val config = BuildConfig()
    println("hello from: ${config.module}, version: ${config.version}")
}