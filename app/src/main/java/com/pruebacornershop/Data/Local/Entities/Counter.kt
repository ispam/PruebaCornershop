package com.pruebacornershop.Data.Local.Entities

data class Counter(
        var title: String,
        var count: Int? = 0
) {
        constructor(title: String, id: String, count: Int) : this(title, 0)
}