package com.example.islamiccorpusvault.ui.navigation

object Routes {
    const val HOME = "home"
    const val GENERAL_NOTES = "general_notes"
    const val SCHOLARS = "scholars"
    const val LIBRARY = "library"
    const val SETTINGS = "settings"

    // Detail
    const val SCHOLAR_DETAIL = "scholar_detail"

    const val CATEGORY = "category/{scholarId}/{scholarName}/{categoryName}"
    const val SUBCATEGORY = "subcategory/{scholarId}/{scholarName}/{categoryName}/{subcategoryId}/{subcategoryName}"
    const val NOTE_DETAIL = "note_detail/{noteId}"
    const val NOTE_EDITOR = "note_editor/{noteId}"
}
