package com.alain.epure.pdf.domain

// Bookmarks of one document: page indexes from 0, sorted, without duplicates (SPEC F-10.1, F-10.3).
// A bookmark is a page number only (DEC-0035). Adds the page when it is not marked, removes it otherwise.
fun toggledBookmarks(bookmarks: List<Int>, page: Int): List<Int> =
    if (page in bookmarks) bookmarks - page else (bookmarks + page).sorted()
