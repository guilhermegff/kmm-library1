package com.project.kmm_library1

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform