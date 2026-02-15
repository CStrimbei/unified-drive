package com.cstr.unifieddrive.fs.model

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document("fs_nodes")
data class FsNode(
    @Id
    val id: String? = null,
    @Indexed
    val ownerId: String,
    val type: NodeType,
    val name: String,
    val parentId: String? = null,
    val provider: ProviderType = ProviderType.VIRTUAL,
    val providerResourceId: String? = null,

    val mimeType: String? = null,
    val sizeBytes: Long? = null,

    @CreatedDate
    var createdAt: Instant? = null,

    @LastModifiedDate
    var updatedAt: Instant? = null,
)

