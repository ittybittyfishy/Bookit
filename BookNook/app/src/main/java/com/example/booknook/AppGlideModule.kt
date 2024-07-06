package com.example.booknook

import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions
import android.util.Log
import com.bumptech.glide.GlideBuilder

@GlideModule
class MyAppGlideModule : AppGlideModule() {
    override fun applyOptions(context: android.content.Context, builder: GlideBuilder) {
        builder.setLogLevel(Log.DEBUG)
    }
}
