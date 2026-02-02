package com.shjprofessionals.careride1

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform