package settings;

/**
 * This enumerable type lists the various application-specific property types listed in the initial set of properties to
 * be loaded from the workspace properties <code>xml</code> file specified by the initialization parameters.
 *
 * @author Ritwik Banerjee
 * @see vilij.settings.InitializationParams
 */
@SuppressWarnings("SpellCheckingInspection")
public enum AppPropertyTypes {

    /* resource files and folders */
    DATA_RESOURCE_PATH,
    RESOURCES_RESOURCE_PATH,
    USER_DIRECTORY,
    CSS_PATH,


    /* user interface icon file names */
    SCREENSHOT_ICON,

    /* tooltips for user interface buttons */
    SCREENSHOT_TOOLTIP,

    /* error messages */
    RESOURCE_SUBDIR_NOT_FOUND,
    LINESAVE_ERROR_MSG,
    DUPLICATE_ERROR_MSG,

    /* application-specific message titles */
    SAVE_UNSAVED_WORK_TITLE,
    LOAD_WORK_TITLE,
    SCREENSHOT_TITLE,
    EXIT_TITLE,
    REFLECTION_ERROR_TITLE,


    /* application-specific messages */
    SAVE_UNSAVED_WORK,
    ALGORITHM_MSG,
    RUN_CONFIG,
    INVALID_DATA,
    REFLECTION_ERROR_MSG,
    DONE_RUNNING,


    /* application-specific parameters */
    DATA_FILE_EXT,
    DATA_FILE,
    DATA_FILE_EXT_DESC,
    TEXT_AREA,
    SPECIFIED_FILE,
    IMG_FILE_EXT,
    IMG_FILE,
    IMG_NAME,
    IMG_FILE_EXT_DESC,

    /* added label names */
    DISPLAY,
    GRAPH_TITLE,
    NULL,
    RUN,
    DONE,
    OK,
    OUT,
    ITERATIONS,



    /* CSS Names */
    LABEL,
    BLUE,
    LINE,
    SYMBOL,
    TRANSPARENT,

    /*Algorithm info*/
    classificationA,
    classificationB,
    classificationC,
    clusterA,
    clusterB,
    clusterC,
    Classification,
    Clustering,
    Settings,
    type,
    config,
    maximum,
    intervals,
    contRun,

    /*Data info*/
    dash,
    instance,
    load,
    label,
    labels,
    numLabels,
    AVERAGE,

    /*Algorithms*/
    RANDOM_CLASSIFIER,
    RANDOM_CLUSTERER,
    KMEANSCLUSTERER,
}
