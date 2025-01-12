package com.example.litomicisystem.placeholder


    data class PlaceholderItem(
        val id: String,
        val content: String,
        val date: String,
        val capacity: String,
    )

    // A list of sample items.
    public val ITEMS: ArrayList<PlaceholderItem> = ArrayList<PlaceholderItem>()


