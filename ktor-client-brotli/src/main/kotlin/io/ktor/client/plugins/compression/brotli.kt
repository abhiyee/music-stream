// Flowtune Music by abhiram79
// github.com/abhiram79

package io.ktor.client.plugins.compression

fun ContentEncodingConfig.brotli(quality: Float? = null) {
    customEncoder(BrotliEncoder, quality)
}
