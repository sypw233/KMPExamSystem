package ovo.sypw.kmp.examsystem

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform