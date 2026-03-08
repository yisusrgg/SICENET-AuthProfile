package com.example.sicenet_authprofile.data.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import com.example.sicenet_authprofile.data.local.SicenetDatabase

class SicenetContentProvider : ContentProvider() {

    companion object {
        const val AUTHORITY = "com.example.sicenet_authprofile.provider"
        
        // Caminos (Paths)
        const val PATH_CARGA_ACADEMICA = "carga_academica"
        const val PATH_KARDEX = "kardex"

        // URIs
        val CONTENT_URI_CARGA: Uri = Uri.parse("content://$AUTHORITY/$PATH_CARGA_ACADEMICA")
        val CONTENT_URI_KARDEX: Uri = Uri.parse("content://$AUTHORITY/$PATH_KARDEX")

        // Códigos para el matcher
        private const val CARGA_ACADEMICA = 1
        private const val KARDEX = 2

        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, PATH_CARGA_ACADEMICA, CARGA_ACADEMICA)
            addURI(AUTHORITY, PATH_KARDEX, KARDEX)
        }
    }

    private lateinit var database: SicenetDatabase

    override fun onCreate(): Boolean {
        database = SicenetDatabase.getDatabase(context!!)
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        val code = uriMatcher.match(uri)
        return when (code) {
            CARGA_ACADEMICA -> {
                database.openHelper.readableDatabase.query("SELECT * FROM carga_academica")
            }
            KARDEX -> {
                database.openHelper.readableDatabase.query("SELECT * FROM cardex")
            }
            else -> throw IllegalArgumentException("URI desconocida: $uri")
        }
    }

    override fun getType(uri: Uri): String? {
        return when (uriMatcher.match(uri)) {
            CARGA_ACADEMICA -> "vnd.android.cursor.dir/$AUTHORITY.$PATH_CARGA_ACADEMICA"
            KARDEX -> "vnd.android.cursor.dir/$AUTHORITY.$PATH_KARDEX"
            else -> throw IllegalArgumentException("URI desconocida: $uri")
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
}
