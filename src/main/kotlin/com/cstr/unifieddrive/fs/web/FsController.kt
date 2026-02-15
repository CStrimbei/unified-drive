package com.cstr.unifieddrive.fs.web

import com.cstr.unifieddrive.fs.dto.CreateFolderRequest
import com.cstr.unifieddrive.fs.dto.MoveNodeRequest
import com.cstr.unifieddrive.fs.dto.RenameNodeRequest
import com.cstr.unifieddrive.fs.model.FsNode
import com.cstr.unifieddrive.fs.service.FsNodeService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/fs")
class FsController(private val fsNodeService: FsNodeService) {

    private fun Authentication.userId(): String =
        this.principal as? String
            ?: throw IllegalStateException("Authentication principal must be a userId (String)")

    @PostMapping("/init-root")
    fun initRoot(authentication: Authentication): ResponseEntity<FsNode> {
        val userId = authentication.userId()
        val root = fsNodeService.initRootIfMissing(userId)
        return ResponseEntity.ok(root)
    }

    @GetMapping("/nodes")
    fun listNodes(
        authentication: Authentication,
        @RequestParam(required = false) parentId: String?
    ): List<FsNode> {
        val userId = authentication.userId()
        return fsNodeService.listNodesByParent(userId, parentId)
    }

    @PostMapping("/folders")
    fun createFolder(
        authentication: Authentication,
        @RequestBody body: CreateFolderRequest
    ): ResponseEntity<FsNode> {
        val userId = authentication.userId()
        val created = fsNodeService.createFolder(userId, body.name, body.parentId)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    @PatchMapping("/nodes/{id}/rename")
    fun renameNode(
        authentication: Authentication,
        @PathVariable id: String,
        @RequestBody body: RenameNodeRequest
    ): ResponseEntity<FsNode> {
        val userId = authentication.userId()
        val updated = fsNodeService.renameNode(userId, id, body.name)
        return ResponseEntity.ok(updated)
    }

    @PatchMapping("/nodes/{id}/move")
    fun moveNode(
        authentication: Authentication,
        @PathVariable id: String,
        @RequestBody body: MoveNodeRequest
    ): ResponseEntity<FsNode> {
        val userId = authentication.userId()
        val updated = fsNodeService.moveNode(userId, id, body.newParentId)
        return ResponseEntity.ok(updated)
    }

    @DeleteMapping("/nodes/{id}")
    fun deleteNode(
        authentication: Authentication,
        @PathVariable id: String,
        @RequestParam(required = false, defaultValue = "false") recursive: Boolean
    ): ResponseEntity<Void> {
        val userId = authentication.userId()
        fsNodeService.deleteNode(userId, id, recursive)
        return ResponseEntity.noContent().build()
    }
}
