package com.pruebacornershop.Data.Local.Entities

data class Counter(
        var title: String,
        var count: Int? = 0,
        var id: String? = ""
) {
        constructor(title: String, id: String, count: Int) : this(title, 0, "")
}

data class CounterID(
        var id: String
)