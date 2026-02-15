package com.cstr.unifieddrive.fs.service

import com.cstr.unifieddrive.fs.exception.FsNodeNotFoundException
import com.cstr.unifieddrive.fs.exception.InvalidFsOperationException
import com.cstr.unifieddrive.fs.model.FsNode
import com.cstr.unifieddrive.fs.model.NodeType
import com.cstr.unifieddrive.fs.model.ProviderType
import com.cstr.unifieddrive.fs.repo.FsNodeRepo
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class FsNodeService(private val repository : FsNodeRepo) {
    fun initRootIfMissing(ownerId: String) : FsNode {
        val existingRoots = repository.findByOwnerIdAndTypeAndParentIdIsNull(ownerId, NodeType.FOLDER)
        if(existingRoots.isNotEmpty()) {
            return existingRoots.first()
        }

        val root = FsNode(
            type = NodeType.FOLDER,
            name = "/",
            parentId = null,
            provider = ProviderType.VIRTUAL,
            providerResourceId = null,
            ownerId = ownerId
        )

        return repository.save(root)
    }

    fun listNodesByParent(ownerId: String, parentId: String?): List<FsNode> {
        val effectiveParentId = parentId ?: initRootIfMissing(ownerId).id
        return repository.findByOwnerIdAndParentId(ownerId, effectiveParentId)
    }

    fun createFolder(ownerId: String, name: String, parentId: String?): FsNode{
        val trimmedName = name.trim()
        if(trimmedName.isEmpty()){
            throw IllegalArgumentException("Folder name cannot be empty.")
        }

        if(parentId != null) {
            val parent = repository.findByIdAndOwnerId(parentId, ownerId)
                ?: throw IllegalArgumentException("Parent folder could not be found.")

            if (parent.type != NodeType.FOLDER) {
                throw IllegalArgumentException("Parent must be a folder.")
            }
        }
            val folder = FsNode(
                type = NodeType.FOLDER,
                name = trimmedName,
                parentId = parentId,
                provider = ProviderType.VIRTUAL,
                providerResourceId = null,
                ownerId = ownerId
            )

            return repository.save(folder)
    }

    fun renameNode(ownerId: String, nodeId: String, newName: String): FsNode{
        if(newName.isBlank()){
            throw InvalidFsOperationException("Name must not be empty.")
        }

        val node = repository.findByIdAndOwnerId(ownerId, nodeId)
            ?: throw FsNodeNotFoundException(nodeId)

        val updated = node.copy(
            name = newName.trim(),
            updatedAt = Instant.now()
        )

        return repository.save(updated)
    }

    fun moveNode(ownerId: String, nodeId: String, newParentId: String?): FsNode{
        val node = repository.findByIdAndOwnerId(ownerId, nodeId)
            ?: throw FsNodeNotFoundException(nodeId)

        val newParent: FsNode? = newParentId?.let {
            repository.findByIdAndOwnerId(it, ownerId)
                ?: throw InvalidFsOperationException("Parent folder could not be found.")
        }

        if(newParent != null && newParent.type != NodeType.FOLDER){
            throw InvalidFsOperationException("Parent must be a folder.")
        }

        if(newParentId == nodeId) {
            throw InvalidFsOperationException("Cannot move a node inside itself")
        }

        if(newParent != null){
            ensureNotMovingIntoDescendant(ownerId, node, newParent)
        }

        val updated = node.copy(
            parentId = newParentId,
            updatedAt = Instant.now()
        )

        return repository.save(updated)
    }

    private fun ensureNotMovingIntoDescendant(ownerId: String, node: FsNode, newParent: FsNode) {
        var current: FsNode? = newParent

        while (current != null) {
            if (current.id == node.id) {
                throw InvalidFsOperationException("Cannot move a node inside its own subtree")
            }

            val parentId = current.parentId
            current = parentId?.let {
                repository.findByIdAndOwnerId(it, ownerId)
            }
        }
    }

    @Transactional
    fun deleteNode(ownerId: String, nodeId: String, recursive: Boolean){
        val node = repository.findByIdAndOwnerId(ownerId, nodeId)
            ?: throw FsNodeNotFoundException(nodeId)

        if(!recursive && node.type == NodeType.FOLDER) {
            val children = repository.findByOwnerIdAndParentId(ownerId, node.id!!)
            if(children.isNotEmpty()){
                throw InvalidFsOperationException("Folder is not empty; use recursive delete.")
            }
        }

        if(recursive && node.type == NodeType.FOLDER){
            deleteSubtree(ownerId, node.id!!)
        } else {
            repository.delete(node)
        }
    }

    private fun deleteSubtree(ownerId: String, rootId: String) {
        val toVisit = ArrayDeque<String>()
        toVisit.add(rootId)

        val allIds = mutableListOf<String>()

        while(toVisit.isNotEmpty()) {
            val currentId = toVisit.removeFirst()
            allIds.add(currentId)

            val children = repository.findByOwnerIdAndParentId(ownerId, currentId)
            children.mapNotNullTo(toVisit) {
                it.id
            }
        }

        repository.deleteAllById(allIds)
    }
}