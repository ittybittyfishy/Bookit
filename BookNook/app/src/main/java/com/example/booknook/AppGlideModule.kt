package com.example.booknook

import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions
import android.util.Log //this library is used for debugging
import com.bumptech.glide.GlideBuilder

@GlideModule //this method allows us to customize glides settings by configuring the glidebuilder

//this class inherits from appGlideModule, meaning this class will have its behaviors
//MyAppGlideModule is a subclass of appGlideModule
class MyAppGlideModule : AppGlideModule() {
    //here we override the function applyOptions
    //parameter named context of type android.content.context provides access to app resources and classes
    //builder of type glidebuilder is a class that configures various setting for glide library
    override fun applyOptions(context: android.content.Context, builder: GlideBuilder) {
        builder.setLogLevel(Log.DEBUG) //we configured glide to set logging to debug
    }
}
