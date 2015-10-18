package clsTypes;

/**
 * The constant value for file operation.
 */
public enum FOP {
	NEW,
    COPY,
    CHANGED,
    BRANCH,
    UPLOAD,
    DOWNLOAD,
    ALREADYUPLOAD,
    LOCAL_HAS_DELETED,
    REMOTE_HAS_DELETED, 
    LOCAL_NEED_OVERWRITE,
    REMOTE_NEED_OVERWRITE, 
    MOVE,
    NONE,
    FAIL
}
