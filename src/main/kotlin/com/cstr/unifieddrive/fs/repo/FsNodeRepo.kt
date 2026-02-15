package com.cstr.unifieddrive.fs.repo

import com.cstr.unifieddrive.fs.model.FsNode
import com.cstr.unifieddrive.fs.model.NodeType
import org.springframework.data.mongodb.repository.MongoRepository

interface FsNodeRepo : MongoRepository<FsNode, String> {
    fun findByOwnerIdAndParentId(ownerId: String, parentId: String?): List<FsNode>
    fun findByIdAndOwnerId(id: String, ownerId: String): FsNode?
    fun findByOwnerIdAndTypeAndParentIdIsNull(ownerId: String, type: NodeType): List<FsNode>
    fun existsByOwnerIdAndTypeAndParentIdIsNull(ownerId: String, type: NodeType): Boolean
}