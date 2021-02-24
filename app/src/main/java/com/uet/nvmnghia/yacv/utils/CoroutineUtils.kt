package com.uet.nvmnghia.yacv.utils

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch


suspend fun <A> Iterable<A>.parallelForEach(f: suspend (A) -> Unit): Unit = coroutineScope {
    forEach { launch { f(it) } }
}