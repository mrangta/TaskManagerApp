package com.mcc.g22

import android.net.Uri

class ImageModel {

    var name: String? = null
    var image_drawable: Int = 0
    var storagePath: String? = null

    fun getNames(): String {
        return name.toString()
    }

    fun setNames(name: String) {
        this.name = name
    }

    fun getImage_drawables(): Int {
        return image_drawable
    }

    fun setImage_drawables(image_drawable: Int) {
        this.image_drawable = image_drawable
    }

}