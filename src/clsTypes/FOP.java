package clsTypes;

/**
 * The constant value for file operation.
 * FOP File Operation
 */
public enum FOP {
	NEW,
    COPY,
    MOVE_FROM_REF,
    MOVE_TO_REF,
    CHANGED,
    BRANCH,
    UPLOAD,
    DOWNLOAD,
    ALREADYUPLOAD,
    LOCAL_HAS_DELETED,
    LOCAL_NEED_OVERWRITE,
    LOCAL_NEED_TOBE_DELETED,       
    REMOTE_HAS_DELETED, 
    REMOTE_NEED_OVERWRITE,
    REMOTE_NEED_TOBE_DELETED,
    MOVE,
    NONE,
    FAIL
}
