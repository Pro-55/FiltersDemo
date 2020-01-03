package com.example.filtersdemo.model

import com.zomato.photofilters.imageprocessors.Filter

data class FilterItem(
    val name: String,
    val filter: Filter = Filter()
)