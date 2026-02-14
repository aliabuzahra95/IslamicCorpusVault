package com.example.islamiccorpusvault.ui.navigation

object Routes {
    const val HOME = "home"
    const val SCHOLARS = "scholars"
    const val LIBRARY = "library"
    const val SETTINGS = "settings"

    // Detail
    const val SCHOLAR_DETAIL = "scholar_detail"

    const val CATEGORY = "category/{scholarName}/{categoryName}"
    const val SUBCATEGORY = "subcategory/{scholarName}/{categoryName}/{subcategoryId}/{subcategoryName}"
    const val NOTE_DETAIL = "note_detail?noteId={noteId}&title={title}&body={body}&citation={citation}"
}
