package com.cstr.unifieddrive.fs.dto

data class CreateFolderRequest(
    val name: String,
    val parentId: String? = null
)