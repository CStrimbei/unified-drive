package com.cstr.unifieddrive.fs.exception

class FsNodeNotFoundException(nodeId: String): RuntimeException("FS node: $nodeId could not be found")