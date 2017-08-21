package com.polidea.samplelibrary

class LibSettings {

    val buildVariant: String
        get() = "${BuildConfig.FLAVOR}_${BuildConfig.BUILD_TYPE}"

    val propertyValue: String
        get() = "${Settings.lib_properties.lib_property_1}"
}